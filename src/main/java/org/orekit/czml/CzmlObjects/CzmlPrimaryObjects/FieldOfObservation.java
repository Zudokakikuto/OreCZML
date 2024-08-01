/* Copyright 2002-2024 CS GROUP
 * Licensed to CS GROUP (CS) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * CS licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.orekit.czml.CzmlObjects.CzmlPrimaryObjects;

import cesiumlanguagewriter.Cartesian;
import cesiumlanguagewriter.CesiumArcType;
import cesiumlanguagewriter.JulianDate;
import cesiumlanguagewriter.PacketCesiumWriter;
import cesiumlanguagewriter.Reference;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.hipparchus.util.FastMath;
import org.orekit.annotation.DefaultDataContext;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.czml.ArchiObjects.FieldOfObservationBuilder;
import org.orekit.czml.CzmlObjects.Polyline;
import org.orekit.frames.FramesFactory;
import org.orekit.frames.TopocentricFrame;
import org.orekit.frames.Transform;
import org.orekit.geometry.fov.FieldOfView;
import org.orekit.propagation.SpacecraftState;
import org.orekit.utils.Constants;
import org.orekit.utils.IERSConventions;

import java.awt.Color;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class FieldOfObservation extends AbstractPrimaryObject implements CzmlPrimaryObject {

    /**
     * .
     */
    public static final String DEFAULT_ID = "LINE_OBSERVATION/";
    /**
     * .
     */
    public static final String DEFAULT_NAME = "Line of observation of : ";
    /**
     * .
     */
    public static final double DEFAULT_ANGULAR_STEP = FastMath.toRadians(36);
    /**
     * .
     */
    public static final String DEFAULT_H_POSITION = "#position";
    /**
     * .
     */
    public static final Color DEFAULT_COLOR = Color.CYAN;

    /**
     * .
     */
    private Reference referenceSatellite;
    /**
     * .
     */
    private List<Reference> groundReferences = new ArrayList<>();
    /**
     * .
     */
    private List<List<GeodeticPoint>> initialFootprint;
    /**
     * .
     */
    private List<List<List<GeodeticPoint>>> footprintsInTime = new ArrayList<>();
    /**
     * .
     */
    private List<AbstractPointOnBody> allPoints = new ArrayList<>();
    /**
     * .
     */
    private Color polylineColor;
    /**
     * .
     */
    private Transform initialFovToBody;
    /**
     * .
     */
    private boolean noDetection;
    /**
     * .
     */
    private OneAxisEllipsoid body;
    /**
     * .
     */
    private List<JulianDate> julianDates;
    /**
     * .
     */
    private List<List<List<Cartesian>>> cartesianListFootprint = new ArrayList<>();

    @DefaultDataContext
    public FieldOfObservation(final Satellite satellite, final FieldOfView fieldOfView, final Transform fovToBodyInput) {
        this(satellite, fieldOfView, fovToBodyInput, new OneAxisEllipsoid(Constants.WGS84_EARTH_EQUATORIAL_RADIUS,
                                                                          Constants.WGS84_EARTH_FLATTENING, FramesFactory.getITRF(IERSConventions.IERS_2010, true)),
             DEFAULT_ANGULAR_STEP,
             DEFAULT_COLOR);
    }

    public FieldOfObservation(final Satellite satellite, final FieldOfView fov, final Transform initialFovBody, final OneAxisEllipsoid body, final double angularStep, final Color color) {
        this.setId(DEFAULT_ID + fov.toString());
        this.setName(DEFAULT_NAME + satellite.getName());
        this.initialFovToBody = initialFovBody;
        this.polylineColor = color;
        referenceSatellite = new Reference(satellite.getId() + DEFAULT_H_POSITION);
        this.body = body;
        initialFootprint = fov.getFootprint(initialFovBody, body, angularStep);
        final List<SpacecraftState> allSatelliteSpaceCraftStates = satellite.getAllSpaceCraftStates();
        this.julianDates = absoluteDatelistToJulianDateList(satellite.getAbsoluteDateList());
        for (int i = 0; i < julianDates.size(); i++) {
            final SpacecraftState currentState = allSatelliteSpaceCraftStates.get(i);
            final Transform currentInertToBody = currentState.getFrame()
                                                             .getTransformTo(body.getBodyFrame(), currentState.getDate());
            final Transform currentFovBody = new Transform(julianDateToAbsoluteDate(julianDates.get(i), Header.TIME_SCALE), currentState.toTransform()
                                                                                                                                        .getInverse(), currentInertToBody);
            final List<List<GeodeticPoint>> currentFootprints = fov.getFootprint(currentFovBody, body, angularStep);
            footprintsInTime.add(currentFootprints);
        }
        if (footprintsInTime.get(0)
                            .isEmpty()) {
            System.out.println("Could not display the field of view, it does not cross the surface of the geoid.");
            noDetection = true;
        } else {
            this.cartesianListFootprint = extractCartesianFromGeodetic(footprintsInTime);
            this.footprintsInTime = sortingListListList(footprintsInTime);
        }
    }

    public static FieldOfObservationBuilder builder(final Satellite satellite, final FieldOfView fieldOfView, final Transform fovToBodyInput) {
        return new FieldOfObservationBuilder(satellite, fieldOfView, fovToBodyInput);
    }

    @Override
    public void writeCzmlBlock() throws URISyntaxException, IOException {
        OUTPUT.setPrettyFormatting(true);
        if (!noDetection) {
            for (List<List<GeodeticPoint>> lists : footprintsInTime) {
                for (List<GeodeticPoint> list : lists) {
                    final AbstractPointOnBody currentPointOnEarth = new AbstractPointOnBody(julianDates, list, body);
                    allPoints.add(currentPointOnEarth);
                    currentPointOnEarth.writeCzmlBlock();
                    groundReferences.add(new Reference(currentPointOnEarth.getId() + DEFAULT_H_POSITION));
                }
            }

            for (int i = 0; i < footprintsInTime.size(); i++) {
                try (PacketCesiumWriter packet = STREAM.openPacket(OUTPUT)) {
                    final Polyline currentPolyline = new Polyline(referenceSatellite, groundReferences.get(i), polylineColor);
                    currentPolyline.setArcType(CesiumArcType.NONE);
                    packet.writeId(currentPolyline.toString());
                    packet.writeName("Line of the Line of observation : " + currentPolyline);
                    currentPolyline.writeReferencesPolyline(packet, OUTPUT);
                }
            }
            // Display outlines of the line of sight
            for (int i = 0; i < allPoints.size() - 1; i++) {
                final AbstractPointOnBody currentPoint = allPoints.get(i);
                final AbstractPointOnBody nextPoint = allPoints.get(i + 1);
                final Reference currentPointReference = new Reference(currentPoint.getId() + DEFAULT_H_POSITION);
                final Reference secondPointReference = new Reference(nextPoint.getId() + DEFAULT_H_POSITION);
                try (PacketCesiumWriter packet = STREAM.openPacket(OUTPUT)) {
                    final Polyline currentPolyline = new Polyline(currentPointReference, secondPointReference, polylineColor);
                    currentPolyline.setArcType(CesiumArcType.NONE);
                    packet.writeId(currentPolyline.toString());
                    packet.writeName("Outline of the line of observation : " + currentPolyline);
                    currentPolyline.writeReferencesPolyline(packet, OUTPUT);
                }
            }
            final AbstractPointOnBody lastPoint = allPoints.get(allPoints.size() - 1);
            final AbstractPointOnBody firstPoint = allPoints.get(0);
            final Reference lastPointReference = new Reference(lastPoint.getId() + DEFAULT_H_POSITION);
            final Reference firstPointReference = new Reference(firstPoint.getId() + DEFAULT_H_POSITION);
            try (PacketCesiumWriter packet = STREAM.openPacket(OUTPUT)) {
                final Polyline currentPolyline = new Polyline(lastPointReference, firstPointReference, polylineColor);
                currentPolyline.setArcType(CesiumArcType.NONE);
                packet.writeId(currentPolyline.toString());
                packet.writeName("Outline of the last line of observation : " + currentPolyline);
                currentPolyline.writeReferencesPolyline(packet, OUTPUT);
            }
        }
    }

    @Override
    public StringWriter getStringWriter() {
        return STRING_WRITER;
    }

    @Override
    public void cleanObject() {
        groundReferences = new ArrayList<>();
        initialFootprint = new ArrayList<>();
        footprintsInTime = new ArrayList<>();
        allPoints = new ArrayList<>();
        julianDates = new ArrayList<>();
        cartesianListFootprint = new ArrayList<>();
        referenceSatellite = null;
        body = null;
        initialFovToBody = null;
    }

    public Reference getReferenceSatellite() {
        return referenceSatellite;
    }

    public List<Reference> getGroundReferences() {
        return groundReferences;
    }

    public List<List<GeodeticPoint>> getInitialFootprint() {
        return initialFootprint;
    }

    public List<List<List<GeodeticPoint>>> getFootprintsInTime() {
        return footprintsInTime;
    }

    public List<AbstractPointOnBody> getAllPoints() {
        return allPoints;
    }

    public Color getPolylineColor() {
        return polylineColor;
    }

    public Transform getInitialFovToBody() {
        return initialFovToBody;
    }

    public OneAxisEllipsoid getBody() {
        return body;
    }

    public List<JulianDate> getJulianDates() {
        return julianDates;
    }

    public List<List<List<Cartesian>>> getCartesianListFootprint() {
        return cartesianListFootprint;
    }

    private List<List<List<Cartesian>>> extractCartesianFromGeodetic(final List<List<List<GeodeticPoint>>> geodeticPoints) {
        final List<List<List<Cartesian>>> toReturn = new ArrayList<>();
        for (int i = 0; i < geodeticPoints.size(); i++) {
            final List<List<Cartesian>> tempToReturn = new ArrayList<>();
            for (int j = 0; j < geodeticPoints.get(0)
                                              .size(); j++) {
                final List<Cartesian> tempCartesians = new ArrayList<>();
                for (int k = 0; k < geodeticPoints.get(i)
                                                  .get(j)
                                                  .size(); k++) {
                    final GeodeticPoint currentGeodeticPoint = geodeticPoints.get(i)
                                                                             .get(j)
                                                                             .get(k);
                    final TopocentricFrame currentTopocentricFrame = new TopocentricFrame(body, currentGeodeticPoint, "");
                    final Vector3D currentVector3D = currentTopocentricFrame.getCartesianPoint();
                    final Cartesian currentCartesian = new Cartesian(currentVector3D.getX(), currentVector3D.getY(), currentVector3D.getZ());
                    tempCartesians.add(currentCartesian);
                }
                tempToReturn.add(tempCartesians);
            }
            toReturn.add(tempToReturn);
        }
        return toReturn;
    }
}
