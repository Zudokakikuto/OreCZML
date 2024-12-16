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
package org.orekit.czml.object.primary.visu;

import cesiumlanguagewriter.Cartesian;
import cesiumlanguagewriter.CesiumArcType;
import cesiumlanguagewriter.CesiumOutputStream;
import cesiumlanguagewriter.CesiumStreamWriter;
import cesiumlanguagewriter.JulianDate;
import cesiumlanguagewriter.PacketCesiumWriter;
import cesiumlanguagewriter.Reference;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.hipparchus.util.FastMath;
import org.orekit.annotation.DefaultDataContext;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.czml.object.Polyline;
import org.orekit.czml.object.Utils.DateUtils;
import org.orekit.czml.object.nonvisual.PointOnBody;
import org.orekit.czml.object.primary.AbstractPrimaryObject;
import org.orekit.czml.object.primary.Header;
import org.orekit.czml.object.primary.entities.Spacecraft;
import org.orekit.data.DataContext;
import org.orekit.frames.TopocentricFrame;
import org.orekit.frames.Transform;
import org.orekit.geometry.fov.FieldOfView;
import org.orekit.propagation.SpacecraftState;
import org.orekit.utils.Constants;
import org.orekit.utils.IERSConventions;

import java.awt.Color;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Field of observation class
 *
 * <p> The field of observation defines the field of view of a satellite projected on a body. The field of observation
 * follows the attitude of the satellite.
 *
 * @author Julien LEBLOND
 * @since 1.0.0
 */
public class FieldOfObservation extends AbstractPrimaryObject {

    /**
     * The default ID of the field of observation.
     */
    public static final String DEFAULT_ID = "FIELD_OF_OBSERVATION/";

    /**
     * The default name of the field of observation.
     */
    public static final String DEFAULT_NAME = "Field of observation of : ";

    /**
     * The default angular step of the field of observation.
     */
    public static final double DEFAULT_ANGULAR_STEP = FastMath.toRadians(36);

    /**
     * This allows the reference the position of an object.
     */
    public static final String DEFAULT_H_POSITION = "#position";

    /**
     * The default color of the lines defining the field of view.
     */
    public static final Color DEFAULT_COLOR = Color.CYAN;


    // Intrinsic parameters

    /**
     * The fov of the satellite.
     */
    private final FieldOfView fov;

    /**
     * The transform inputted that represents the fov of the object and how it looks at the body.
     */
    private final Transform initialTransformFovToBody;

    /**
     * The body to which the fov will be projected.
     */
    private final OneAxisEllipsoid body;

    /**
     * The angular step.
     */
    private final double angularStep;

    /**
     * The color of the polylines.
     */
    private final Color polylineColor;

    // Other arguments

    /**
     * The reference of position of the satellite.
     */
    private final Reference referenceSatellite;

    /**
     * The list of reference that defines the point on the ground that will move in time.
     */
    private final List<Reference> groundReferences = new ArrayList<>();

    /**
     * The list of list of geodetic points that represents the point on the body that will evolve in time. Each list is attributed
     * to a specific point, then the sublist represents all the points on the body in time for this given point.
     */
    private final List<List<GeodeticPoint>> initialFootprint;

    /**
     * The list of the footprints in time.
     */
    private List<List<List<GeodeticPoint>>> footprintsInTime = new ArrayList<>();

    /**
     * The list of all the points on body that represents the limits of the field of view projected on the body.
     */
    private final List<PointOnBody> points = new ArrayList<>();

    /**
     * A boolean that trigger if at least the fov is projected one time to the body.
     */
    private boolean noDetection;

    /**
     * The julian dates when the field of observation will be displayed.
     */
    private final List<JulianDate> julianDates;

    /**
     * The list of list of list of position in cartesian of the point on the body. The first separates the cartesian for each
     * julian date. Then the sub list separates the cartesian for each point on earth for a given julian date. Then the sub-sub list
     * defines the points in time for the given point for the given julian date.
     */
    private List<List<List<Cartesian>>> cartesianListFootprint = new ArrayList<>();

    /** Number of polylines. */
    private int numberOfPolylines;

    /** The header considered. */
    private Header header;

    // Constructors

    /**
     * The basic constructor for the field of observation object with default parameters.
     *
     * @param satellite      : The satellite which is observing the body.
     * @param fovInput       : The field of view of the satellite.
     * @param fovToBodyInput : The transform between the fov and the frame of the body.
     * @param header         : The header considered.
     * @throws URISyntaxException the uri syntax exception
     * @throws IOException        the io exception
     */
    @DefaultDataContext
    FieldOfObservation(final Spacecraft satellite, final FieldOfView fovInput, final Transform fovToBodyInput,
                       final Header header) throws URISyntaxException, IOException {
        this(satellite, fovInput, fovToBodyInput,
                new OneAxisEllipsoid(Constants.WGS84_EARTH_EQUATORIAL_RADIUS, Constants.WGS84_EARTH_FLATTENING,
                        DataContext.getDefault().getFrames().getITRF(IERSConventions.IERS_2010, true)), DEFAULT_ANGULAR_STEP, DEFAULT_COLOR,
                DEFAULT_ID + satellite.getName() + "/" + new OneAxisEllipsoid(Constants.WGS84_EARTH_EQUATORIAL_RADIUS,
                        Constants.WGS84_EARTH_FLATTENING,
                        DataContext.getDefault().getFrames().getITRF(IERSConventions.IERS_2010, true)).getBodyFrame()
                                                                                                      .getName(), header);
    }

    /**
     * The constructor for the field of observation object with no default parameters.
     *
     * @param satellite          : The satellite which is observing the body.
     * @param fovInput           : The field of view of the satellite.
     * @param transformFovToBody : The transform between the fov and the frame of the body.
     * @param body               : The body that the satellite is pointing to.
     * @param angularStepInput   : The angular step between two footprints on the body (projection of points on the body).
     * @param color              : The color of the lines representing the lines of the field of observation.
     * @param customID           : The custom ID of the field of observation object.
     * @param header             : The header to consider when several are used.
     * @throws URISyntaxException the uri syntax exception
     * @throws IOException        the io exception
     */
    FieldOfObservation(final Spacecraft satellite, final FieldOfView fovInput, final Transform transformFovToBody,
                       final OneAxisEllipsoid body, final double angularStepInput, final Color color,
                       final String customID, final Header header) throws URISyntaxException, IOException {

        this.setId(customID);
        this.setName(DEFAULT_NAME + satellite.getName());
        this.header                    = header;
        this.initialTransformFovToBody = transformFovToBody;
        this.fov                       = fovInput;
        this.angularStep               = angularStepInput;
        this.polylineColor             = color;
        referenceSatellite             = new Reference(satellite.getId() + DEFAULT_H_POSITION);
        this.body                      = body;
        initialFootprint               = fovInput.getFootprint(transformFovToBody, body, angularStepInput);
        final List<SpacecraftState> satelliteSpaceCraftStates = satellite.getSpaceCraftStates();
        this.julianDates = DateUtils.toJulianDates(satellite.getAbsoluteDateList(), header.getTimeScale());
        for (int i = 0; i < julianDates.size(); i++) {
            final SpacecraftState currentState = satelliteSpaceCraftStates.get(i);
            final Transform currentInertToBody = currentState.getFrame()
                                                             .getTransformTo(body.getBodyFrame(),
                                                                     currentState.getDate());
            final Transform currentTransformFovToBody = new Transform(
                    DateUtils.toAbsoluteDate(julianDates.get(i), header.getTimeScale()), currentState.toTransform()
                                                                                                     .getInverse(),
                    currentInertToBody);
            final List<List<GeodeticPoint>> currentFootprints = fovInput.getFootprint(currentTransformFovToBody, body,
                    angularStepInput);
            footprintsInTime.add(currentFootprints);
        }
        if (footprintsInTime.get(0)
                            .isEmpty()) {
            // Could not display the field of view, it does not cross the surface of the geoid.
            noDetection = true;
        } else {
            this.cartesianListFootprint = extractCartesianFromGeodetic(footprintsInTime);
            this.footprintsInTime       = sortingListListList(footprintsInTime);
            for (List<List<GeodeticPoint>> lists : footprintsInTime) {
                for (List<GeodeticPoint> list : lists) {
                    final PointOnBody currentPointOnEarth = new PointOnBody(julianDates, list, body, header);
                    points.add(currentPointOnEarth);
                    groundReferences.add(new Reference(currentPointOnEarth.getId() + DEFAULT_H_POSITION));
                }
            }
        }
    }


    // Builder

    /**
     * Builder field of observation builder.
     *
     * @param satellite               the satellite
     * @param fieldOfView             the field of view
     * @param transformFovToBodyInput the transform fov to body input
     * @param header                  the header
     * @return the field of observation builder
     */
    public static FieldOfObservationBuilder builder(final Spacecraft satellite, final FieldOfView fieldOfView,
                                                    final Transform transformFovToBodyInput, final Header header) {
        return new FieldOfObservationBuilder(satellite, fieldOfView, transformFovToBodyInput, header);
    }


    // Overrides

    @Override
    public void writeCzmlBlock(final CesiumStreamWriter stream,
                               final CesiumOutputStream output) throws URISyntaxException, IOException {
        output.setPrettyFormatting(true);
        numberOfPolylines = 0;
        if (!noDetection) {
            for (PointOnBody point : points) {
                point.writeCzmlBlock(stream, output);
            }

            for (int i = 0; i < footprintsInTime.size(); i++) {
                final Reference secondReference = groundReferences.get(i);
                buildPolyline(polylineColor, referenceSatellite, secondReference, true, stream, output);
            }
            // Display outlines of the line of sight (except the last one because of the -1)
            for (int i = 0; i < points.size() - 1; i++) {
                final PointOnBody currentPoint = points.get(i);
                final PointOnBody nextPoint    = points.get(i + 1);
                final Reference currentPointReference = new Reference(currentPoint.getId() + DEFAULT_H_POSITION);
                final Reference secondPointReference = new Reference(nextPoint.getId() + DEFAULT_H_POSITION);
                buildPolyline(polylineColor, currentPointReference, secondPointReference, false, stream, output);
            }
            // Build of the line between the last and the first point
            final PointOnBody lastPoint           = points.get(points.size() - 1);
            final PointOnBody firstPoint          = points.get(0);
            final Reference   lastPointReference  = new Reference(lastPoint.getId() + DEFAULT_H_POSITION);
            final Reference   firstPointReference = new Reference(firstPoint.getId() + DEFAULT_H_POSITION);
            buildPolyline(polylineColor, lastPointReference, firstPointReference, false, stream, output);
        }
    }


    // Getters

    /**
     * Gets reference satellite.
     *
     * @return the reference satellite
     */
    public Reference getReferenceSatellite() {
        return referenceSatellite;
    }

    /**
     * Gets angular step.
     *
     * @return the angular step
     */
    public double getAngularStep() {
        return angularStep;
    }

    /**
     * Gets ground references.
     *
     * @return the ground references
     */
    public List<Reference> getGroundReferences() {
        return Collections.unmodifiableList(groundReferences);
    }

    /**
     * Gets initial footprint.
     *
     * @return the initial footprint
     */
    public List<List<GeodeticPoint>> getInitialFootprint() {
        return Collections.unmodifiableList(initialFootprint);
    }

    /**
     * Gets footprints in time.
     *
     * @return the footprints in time
     */
    public List<List<List<GeodeticPoint>>> getFootprintsInTime() {
        return Collections.unmodifiableList(footprintsInTime);
    }

    /**
     * Gets points.
     *
     * @return the points
     */
    public List<PointOnBody> getPoints() {
        return Collections.unmodifiableList(points);
    }

    /**
     * Gets polyline color.
     *
     * @return the polyline color
     */
    public Color getPolylineColor() {
        return polylineColor;
    }

    /**
     * Gets fov.
     *
     * @return the fov
     */
    public FieldOfView getFov() {
        return fov;
    }

    /**
     * Gets initial transform fov to body.
     *
     * @return the initial transform fov to body
     */
    public Transform getInitialTransformFovToBody() {
        return initialTransformFovToBody;
    }

    /**
     * Gets body.
     *
     * @return the body
     */
    public OneAxisEllipsoid getBody() {
        return body;
    }

    /**
     * Gets julian dates.
     *
     * @return the julian dates
     */
    public List<JulianDate> getJulianDates() {
        return Collections.unmodifiableList(julianDates);
    }

    /**
     * Gets cartesian list footprint.
     *
     * @return the cartesian list footprint
     */
    public List<List<List<Cartesian>>> getCartesianListFootprint() {
        return Collections.unmodifiableList(cartesianListFootprint);
    }

    /**
     * Is no detection boolean.
     *
     * @return the boolean
     */
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
                    final Cartesian currentCartesian = new Cartesian(currentVector3D.getX(), currentVector3D.getY(),
                            currentVector3D.getZ());
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
     * @param stream               : The stream that converts all the strings into understandable string for the CzmlFile.
     * @param output               : The output stream of cesium that will contain the strings to write into the CzmLFile.
     */
    private void buildPolyline(final Color polylineColorInput, final Reference firstPointReference,
                               final Reference secondPointReference, final boolean line,
                               final CesiumStreamWriter stream, final CesiumOutputStream output) {
        try (PacketCesiumWriter packet = stream.openPacket(output)) {
            final Polyline currentPolyline = Polyline.nonVectorBuilder(header)
                                                     .withFirstReference(firstPointReference)
                                                     .withSecondReference(secondPointReference)
                                                     .withColor(polylineColorInput)
                                                     .build();

            currentPolyline.setArcType(CesiumArcType.NONE);
            packet.writeId("Polyline number " + numberOfPolylines);
            if (line) {
                packet.writeName("Line of the Line of observation : " + numberOfPolylines);
            } else {
                packet.writeName("Outline of the line of observation : " + numberOfPolylines);
            }
            currentPolyline.writeReferencesPolyline(packet, output);
            numberOfPolylines = numberOfPolylines + 1;
        }
    }

    /**
     * This method aims at reorganizing the list of list of objects by inverting first and third indexes.
     *
     * @param objects : A list of objects.
     * @param <T>     : An object to be sorted.
     * @return A sorted list of objects.
     */
    private <T> List<List<List<T>>> sortingListListList(final List<List<List<T>>> objects) {
        final List<List<List<T>>> toReturn = new ArrayList<>();
        for (int i = 0; i < objects.get(0)
                                   .get(0)
                                   .size(); i++) {
            final List<List<T>> tempListList = new ArrayList<>();
            for (int j = 0; j < objects.get(0)
                                       .size(); j++) {
                final List<T> tempList = new ArrayList<>();
                for (List<List<T>> object : objects) {
                    final T objectToSort = object.get(j)
                                                 .get(i);
                    tempList.add(objectToSort);
                }
                tempListList.add(tempList);
            }
            toReturn.add(tempListList);
        }
        return toReturn;
    }
}
