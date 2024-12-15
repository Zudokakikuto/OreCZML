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

package org.orekit.czml.object.primary.pointing;

import cesiumlanguagewriter.Cartesian;
import cesiumlanguagewriter.CesiumOutputStream;
import cesiumlanguagewriter.CesiumStreamWriter;
import cesiumlanguagewriter.PacketCesiumWriter;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.czml.archi.builder.PolygonBuilder;
import org.orekit.czml.object.nonvisual.PointOnBody;
import org.orekit.czml.object.primary.AbstractPrimaryObject;
import org.orekit.czml.object.primary.Header;
import org.orekit.czml.object.primary.entities.Satellite;
import org.orekit.czml.object.primary.visu.FieldOfObservation;
import org.orekit.czml.object.secondary.Polygon;
import org.orekit.frames.Transform;
import org.orekit.geometry.fov.FieldOfView;

import java.awt.Color;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The type Covered surface on body.
 */
public class CoveredSurfaceOnBody extends AbstractPrimaryObject {

    /**
     * The default ID for the covered surface.
     */
    public static final String DEFAULT_ID = "COVERED_SURFACE/";

    /**
     * The default name for the covered surface.
     */
    public static final String DEFAULT_NAME = "Covered surface of : ";

    /**
     * The satellite that will cover the surface.
     */
    private final Satellite satellite;

    /**
     * The field of view of the satellite that will observe the body.
     */
    private final FieldOfView fov;

    /**
     * The transform inputted that represents the fov of the object and how it looks at the body.
     */
    private final Transform initialFovToBody;

    /**
     * The field of observation of the satellite that will define the surface covered.
     */
    private final FieldOfObservation fieldOfObservation;

    /**
     * The list of the footprints in time.
     */
    private final List<List<List<GeodeticPoint>>> footprintsInTime = new ArrayList<>();

    /**
     * All the cartesians of all the points in time.
     */
    private final List<List<Cartesian>> pointsCartesiansInTime;

    /**
     * List of all the polygons used to describe the surface covered.
     */
    private final Polygon polygon;

    // Constructors

    /**
     * Basic constructor for the surface of the body.
     *
     * @param satelliteInput          : The satellite that will look at the covered surface.
     * @param fieldOfObservationInput : The field of observation of the satellite that will define the surface covered.
     * @param header                  : The header considered.
     */
    CoveredSurfaceOnBody(final Satellite satelliteInput, final FieldOfObservation fieldOfObservationInput,
                                final Header header) {
        this(satelliteInput, fieldOfObservationInput,
                DEFAULT_ID + satelliteInput.getId() + "/" + fieldOfObservationInput.getBody()
                                                                                   .getBodyFrame()
                                                                                   .toString(), header);
    }

    /**
     * Constructor with a custom ID.
     *
     * @param satelliteInput          : The satellite that will look at the covered surface.
     * @param fieldOfObservationInput : The field of observation of the satellite that will define the surface covered.
     * @param customID                : The custom ID of the covered surface on body object.
     * @param header                  : The header to consider when several headers are used.
     */
    CoveredSurfaceOnBody(final Satellite satelliteInput, final FieldOfObservation fieldOfObservationInput,
                                final String customID, final Header header) {

        this.setId(customID);
        this.setName(DEFAULT_NAME + satelliteInput.getId() + " on : " + fieldOfObservationInput.getBody()
                                                                                               .getBodyFrame());
        this.setAvailability(satelliteInput.getAvailability());

        this.satellite          = satelliteInput;
        this.fieldOfObservation = fieldOfObservationInput;
        this.initialFovToBody   = fieldOfObservationInput.getInitialTransformFovToBody();
        this.fov                = fieldOfObservationInput.getFov();

        final List<PointOnBody>     pointsOnBody   = fieldOfObservation.getPoints();
        final List<List<Cartesian>> tempCartesians = new ArrayList<>();
        for (final PointOnBody currentPoint : pointsOnBody) {
            final List<Cartesian> currentCartesian = currentPoint.getCartesians();
            tempCartesians.add(currentCartesian);
        }

        final List<Cartesian> cartesiansToBuildOnePolygon = new ArrayList<>();
        pointsCartesiansInTime = sortingListList(tempCartesians);
        for (final List<Cartesian> currentCartesianList : pointsCartesiansInTime) {
            cartesiansToBuildOnePolygon.addAll(currentCartesianList);
        }

        this.polygon = new PolygonBuilder(cartesiansToBuildOnePolygon, header).withColor(new Color(34, 155, 83))
                                                                              .withOutline(true)
                                                                              .withFill(false)
                                                                              .build();
    }


    /**
     * Builder covered surface on body builder.
     *
     * @param satelliteInput          the satellite input
     * @param fieldOfObservationInput the field of observation input
     * @param header                  the header
     * @return the covered surface on body builder
     */
    public static CoveredSurfaceOnBodyBuilder builder(final Satellite satelliteInput,
                                                      final FieldOfObservation fieldOfObservationInput, final Header header) {
        return new CoveredSurfaceOnBodyBuilder(satelliteInput, fieldOfObservationInput, header);
    }

    // Overrides

    @Override
    public void writeCzmlBlock(final CesiumStreamWriter stream,
                               final CesiumOutputStream output) throws URISyntaxException, IOException {
        output.setPrettyFormatting(true);
        try (PacketCesiumWriter packet = stream.openPacket(output)) {
            packet.writeId(getId() + "/" + fieldOfObservation.getBody()
                                                             .getBodyFrame()
                                                             .getName());
            packet.writeName(getName() + " " + satellite.getName() + " on " + fieldOfObservation.getBody()
                                                                                                .getBodyFrame()
                                                                                                .getName());
            packet.writeAvailability(getAvailability());
            polygon.write(packet, output);
        }
    }


    // GETTERS

    /**
     * Gets satellite.
     *
     * @return the satellite
     */
    public Satellite getSatellite() {
        return satellite;
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
     * Gets initial fov to body.
     *
     * @return the initial fov to body
     */
    public Transform getInitialFovToBody() {
        return initialFovToBody;
    }

    /**
     * Gets field of observation.
     *
     * @return the field of observation
     */
    public FieldOfObservation getFieldOfObservation() {
        return fieldOfObservation;
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
     * Gets points cartesians in time.
     *
     * @return the points cartesians in time
     */
    public List<List<Cartesian>> getPointsCartesiansInTime() {
        return Collections.unmodifiableList(pointsCartesiansInTime);
    }

    /**
     * Gets polygon.
     *
     * @return the polygon
     */
    public Polygon getPolygon() {
        return polygon;
    }


    // Private functions

    /**
     * This method aims at reorganizing the list of list of objects by inverting indexes.
     *
     * @param objects : A list of objects.
     * @param <T>     : An object to be sorted.
     * @return A sorted list of objects.
     */
    private <T> List<List<T>> sortingListList(final List<List<T>> objects) {
        final List<List<T>> toReturn = new ArrayList<>();
        for (int i = 0; i < objects.get(0)
                                   .size(); i++) {
            final List<T> sortedList = new ArrayList<>();
            for (List<T> object : objects) {
                final T objectsToSort = object
                        .get(i);
                sortedList.add(objectsToSort);
            }
            toReturn.add(sortedList);
        }
        return toReturn;
    }

}
