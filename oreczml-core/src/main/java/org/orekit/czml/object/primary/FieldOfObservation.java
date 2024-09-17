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
package org.orekit.czml.object.primary;

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
import org.orekit.czml.archi.builder.FieldOfObservationBuilder;
import org.orekit.czml.object.nonvisual.AbstractPointOnBody;
import org.orekit.czml.object.Polyline;
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

/**
 * Field of observation class
 *
 * <p> The field of observation defines the field of view of a satellite projected on a body. The field of observation
 * follows the attitude of the satellite. </p>
 *
 * @author Julien LEBLOND
 * @since 1.0.0
 */

public class FieldOfObservation extends AbstractPrimaryObject implements CzmlPrimaryObject {

    /** The default ID of the field of observation. */
    public static final String DEFAULT_ID = "FIELD_OF_OBSERVATION/";

    /** The default name of the field of observation. */
    public static final String DEFAULT_NAME = "Field of observation of : ";

    /** The default angular step of the field of observation. */
    public static final double DEFAULT_ANGULAR_STEP = FastMath.toRadians(36);

    /** This allows the reference the position of an object. */
    public static final String DEFAULT_H_POSITION = "#position";

    /** The default color of the lines defining the field of view. */
    public static final Color DEFAULT_COLOR = Color.CYAN;


    // Intrinsic parameters

    /** The fov of the satellite. */
    private FieldOfView fov;

    /** The transform inputted that represents the fov of the object and how it looks at the body. */
    private Transform initialFovToBody;

    /** The body to which the fov will be projected. */
    private OneAxisEllipsoid body;

    /** The angular step. */
    private double angularStep;

    /** The color of the polylines. */
    private Color polylineColor;

    // Other arguments

    /** The reference of position of the satellite. */
    private Reference referenceSatellite;

    /** The list of reference that defines the point on the ground that will move in time. */
    private List<Reference> groundReferences = new ArrayList<>();

    /**
     * The list of list of geodetic points that represents the point on the body that will evolve in time. Each list is attributed
     * to a specific point, then the sublist represents all the points on the body in time for this given point.
     */
    private List<List<GeodeticPoint>> initialFootprint;

    /** The list of the footprints in time. */
    private List<List<List<GeodeticPoint>>> footprintsInTime = new ArrayList<>();

    /** The list of all the abstract point on body that represents the limits of the field of view projected on the body. */
    private List<AbstractPointOnBody> allPoints = new ArrayList<>();

    /** A boolean that trigger if at least the fov is projected one time to the body. */
    private boolean noDetection;

    /** The julian dates when the field of observation will be displayed. */
    private List<JulianDate> julianDates;

    /**
     * The list of list of list of position in cartesian of the point on the body. The first separate the cartesian for each
     * julian date. Then the sub list separates the cartesian for each abstract point on earth for a given julian date. Then the sub-sub list
     * defines the points in time for the given point for the given julian date.
     */
    private List<List<List<Cartesian>>> cartesianListFootprint = new ArrayList<>();


    // Constructors

    /**
     * The basic constructor for the field of observation object with default parameters.
     *
     * @param satellite      : The satellite which is observing the body.
     * @param fovInput       : The field of view of the satellite.
     * @param fovToBodyInput : The transform between the fov and the frame of the body.
     */
    @DefaultDataContext
    public FieldOfObservation(final Satellite satellite, final FieldOfView fovInput,
                              final Transform fovToBodyInput) throws URISyntaxException, IOException {
        this(satellite, fovInput, fovToBodyInput, new OneAxisEllipsoid(Constants.WGS84_EARTH_EQUATORIAL_RADIUS,
                        Constants.WGS84_EARTH_FLATTENING, FramesFactory.getITRF(IERSConventions.IERS_2010, true)),
                DEFAULT_ANGULAR_STEP,
                DEFAULT_COLOR);
    }

    /**
     * The constructor for the field of observation object with no default parameters.
     *
     * @param satellite        : The satellite which is observing the body.
     * @param fovInput         : The field of view of the satellite.
     * @param fovToBodyInput   : The transform between the fov and the frame of the body.
     * @param body             : The body that the satellite is pointing to.
     * @param angularStepInput : The angular step between two footprints on the body (projection of points on the body).
     * @param color            : The color of the lines representing the lines of the field of observation.
     */
    public FieldOfObservation(final Satellite satellite, final FieldOfView fovInput, final Transform fovToBodyInput,
                              final OneAxisEllipsoid body, final double angularStepInput,
                              final Color color) throws URISyntaxException, IOException {
        this.setId(DEFAULT_ID + fovInput.toString());
        this.setName(DEFAULT_NAME + satellite.getName());
        this.initialFovToBody = fovToBodyInput;
        this.fov              = fovInput;
        this.angularStep      = angularStepInput;
        this.polylineColor    = color;
        referenceSatellite    = new Reference(satellite.getId() + DEFAULT_H_POSITION);
        this.body             = body;
        initialFootprint      = fovInput.getFootprint(fovToBodyInput, body, angularStepInput);
        final List<SpacecraftState> allSatelliteSpaceCraftStates = satellite.getAllSpaceCraftStates();
        this.julianDates = absoluteDatelistToJulianDateList(satellite.getAbsoluteDateList(), Header.getTimeScale());
        for (int i = 0; i < julianDates.size(); i++) {
            final SpacecraftState currentState = allSatelliteSpaceCraftStates.get(i);
            final Transform currentInertToBody = currentState.getFrame()
                                                             .getTransformTo(body.getBodyFrame(),
                                                                     currentState.getDate());
            final Transform currentFovBody = new Transform(
                    julianDateToAbsoluteDate(julianDates.get(i), Header.getTimeScale()), currentState.toTransform()
                                                                                                     .getInverse(),
                    currentInertToBody);
            final List<List<GeodeticPoint>> currentFootprints = fovInput.getFootprint(currentFovBody, body,
                    angularStepInput);
            footprintsInTime.add(currentFootprints);
        }
        if (footprintsInTime.get(0)
                            .isEmpty()) {
            System.out.println("Could not display the field of view, it does not cross the surface of the geoid.");
            noDetection = true;
        } else {
            this.cartesianListFootprint = extractCartesianFromGeodetic(footprintsInTime);
            this.footprintsInTime       = sortingListListList(footprintsInTime);
            for (List<List<GeodeticPoint>> lists : footprintsInTime) {
                for (List<GeodeticPoint> list : lists) {
                    final AbstractPointOnBody currentPointOnEarth = new AbstractPointOnBody(julianDates, list, body);
                    allPoints.add(currentPointOnEarth);
                    groundReferences.add(new Reference(currentPointOnEarth.getId() + DEFAULT_H_POSITION));
                }
            }
        }
    }


    // Builder

    public static FieldOfObservationBuilder builder(final Satellite satellite, final FieldOfView fieldOfView,
                                                    final Transform fovToBodyInput) {
        return new FieldOfObservationBuilder(satellite, fieldOfView, fovToBodyInput);
    }


    // Overrides

    @Override
    public void writeCzmlBlock() throws URISyntaxException, IOException {
        OUTPUT.setPrettyFormatting(true);
        if (!noDetection) {
            for (AbstractPointOnBody point : allPoints) {
                point.writeCzmlBlock();
            }

            for (int i = 0; i < footprintsInTime.size(); i++) {
                final Reference secondReference = groundReferences.get(i);
                buildPolyline(polylineColor, referenceSatellite, secondReference, true);
            }
            // Display outlines of the line of sight (except the last one because of the -1)
            for (int i = 0; i < allPoints.size() - 1; i++) {
                final AbstractPointOnBody currentPoint = allPoints.get(i);
                final AbstractPointOnBody nextPoint    = allPoints.get(i + 1);
                final Reference currentPointReference = new Reference(
                        currentPoint.getId() + DEFAULT_H_POSITION);
                final Reference secondPointReference = new Reference(nextPoint.getId() + DEFAULT_H_POSITION);
                buildPolyline(polylineColor, currentPointReference, secondPointReference, false);
            }
            // Build of the line between the last and the first point
            final AbstractPointOnBody lastPoint           = allPoints.get(allPoints.size() - 1);
            final AbstractPointOnBody firstPoint          = allPoints.get(0);
            final Reference           lastPointReference  = new Reference(lastPoint.getId() + DEFAULT_H_POSITION);
            final Reference           firstPointReference = new Reference(firstPoint.getId() + DEFAULT_H_POSITION);
            buildPolyline(polylineColor, lastPointReference, firstPointReference, false);
        }
    }

    @Override
    public StringWriter getStringWriter() {
        return STRING_WRITER;
    }

    @Override
    public void cleanObject() {
        groundReferences       = new ArrayList<>();
        initialFootprint       = new ArrayList<>();
        footprintsInTime       = new ArrayList<>();
        allPoints              = new ArrayList<>();
        julianDates            = new ArrayList<>();
        cartesianListFootprint = new ArrayList<>();
        referenceSatellite     = null;
        body                   = null;
        initialFovToBody       = null;
    }


    // Getters

    public Reference getReferenceSatellite() {
        return referenceSatellite;
    }

    public double getAngularStep() {
        return angularStep;
    }

    public List<Reference> getGroundReferences() {
        return new ArrayList<>(groundReferences);
    }

    public List<List<GeodeticPoint>> getInitialFootprint() {
        return new ArrayList<>(initialFootprint);
    }

    public List<List<List<GeodeticPoint>>> getFootprintsInTime() {
        return new ArrayList<>(footprintsInTime);
    }

    public List<AbstractPointOnBody> getAllPoints() {
        return new ArrayList<>(allPoints);
    }

    public Color getPolylineColor() {
        return polylineColor;
    }

    public FieldOfView getFov() {
        return fov;
    }

    public Transform getInitialFovToBody() {
        return initialFovToBody;
    }

    public OneAxisEllipsoid getBody() {
        return body;
    }

    public List<JulianDate> getJulianDates() {
        return new ArrayList<>(julianDates);
    }

    public List<List<List<Cartesian>>> getCartesianListFootprint() {
        return new ArrayList<>(cartesianListFootprint);
    }

    public boolean isNoDetection() {
        return noDetection;
    }


    // Private functions

    /**
     * This functions aims at extracting the cartesian coordinates from the geodetic coordinates.
     *
     * @param geodeticPoints : A list of list of list of geodetic points that represents the footprints of the field of
     *                       view on the body in time. Each list is attributed to a specific projected point, then the sublist represents all the
     *                       points on the body in time for this given point.
     * @return : A list of list of list of cartesian that represents the positions of the projected point on the body in time.
     * Each list is attributed to a specific projected point, then each sublist represents all the points on the body
     * in time for this given point.
     */
    private List<List<List<Cartesian>>> extractCartesianFromGeodetic(
            final List<List<List<GeodeticPoint>>> geodeticPoints) {

        final List<List<List<Cartesian>>> toReturn = new ArrayList<>();

        for (List<List<GeodeticPoint>> geodeticPoint : geodeticPoints) {
            final List<List<Cartesian>> tempToReturn = new ArrayList<>();
            for (int j = 0; j < geodeticPoints.get(0)
                                              .size(); j++) {

                final List<Cartesian> tempCartesians = new ArrayList<>();
                for (int k = 0; k < geodeticPoint.get(j)
                                                 .size(); k++) {

                    final GeodeticPoint currentGeodeticPoint = geodeticPoint.get(j)
                                                                            .get(k);
                    final TopocentricFrame currentTopocentricFrame = new TopocentricFrame(body, currentGeodeticPoint,
                            "");
                    final Vector3D currentVector3D = currentTopocentricFrame.getCartesianPoint();
                    final Cartesian currentCartesian = new Cartesian(currentVector3D.getX(),
                            currentVector3D.getY(), currentVector3D.getZ());
                    tempCartesians.add(currentCartesian);
                }
                tempToReturn.add(tempCartesians);
            }
            toReturn.add(tempToReturn);
        }
        return toReturn;
    }

    /**
     * This function aims at build a polyline with given parameters.
     *
     * @param polylineColorInput   : The color of the polyline
     * @param firstPointReference  : The first point of reference for the extremity of the polyline
     * @param secondPointReference : The second point of reference for the extremity of the polyline
     * @param line                 : To know if the polyline is a line linked with the satellite, or a line linking two points on the ground
     */
    private void buildPolyline(final Color polylineColorInput, final Reference firstPointReference,
                               final Reference secondPointReference, final boolean line) {
        try (PacketCesiumWriter packet = STREAM.openPacket(OUTPUT)) {
            final Polyline currentPolyline = Polyline.nonVectorBuilder()
                                                     .withFirstReference(firstPointReference)
                                                     .withSecondReference(secondPointReference)
                                                     .withColor(polylineColorInput)
                                                     .build();

            currentPolyline.setArcType(CesiumArcType.NONE);
            packet.writeId(currentPolyline.toString());
            if (line) {
                packet.writeName("Line of the Line of observation : " + currentPolyline);
            } else {
                packet.writeName("Outline of the line of observation : " + currentPolyline);
            }
            currentPolyline.writeReferencesPolyline(packet, OUTPUT);
        }
    }
}
