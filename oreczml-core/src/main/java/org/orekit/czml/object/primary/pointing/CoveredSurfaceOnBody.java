/* Copyright 2002-2025 CS GROUP
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
import cesiumlanguagewriter.JulianDate;
import cesiumlanguagewriter.PacketCesiumWriter;
import cesiumlanguagewriter.TimeInterval;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.czml.object.nonvisual.PointOnBody;
import org.orekit.czml.object.primary.AbstractPrimaryObject;
import org.orekit.czml.object.primary.entities.Spacecraft;
import org.orekit.czml.object.primary.visu.FieldOfObservation;
import org.orekit.czml.object.secondary.Polygon;
import org.orekit.frames.Transform;
import org.orekit.geometry.fov.FieldOfView;

import java.awt.Color;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * The type Covered surface on body.
 */
public class CoveredSurfaceOnBody
    extends
    AbstractPrimaryObject {

    /** The default color of the covered surface. */
    public static final Color DEFAULT_COLOR = new Color(34, 155, 83);

    /**
     * The default ID for the covered surface.
     */
    public static final String DEFAULT_ID = "COVERED_SURFACE/";

    /** String number used for the ID. */
    public static final String NUMBER = " Number ";

    /**
     * The default name for the covered surface.
     */
    public static final String DEFAULT_NAME = "Covered surface of : ";

    /**
     * The satellite that will cover the surface.
     */
    private final Spacecraft satellite;

    /**
     * The field of view of the satellite that will observe the body.
     */
    private final FieldOfView fov;

    /**
     * The transform inputted that represents the fov of the object and how it
     * looks at the body.
     */
    private final Transform initialFovToBody;

    /**
     * The field of observation of the satellite that will define the surface
     * covered.
     */
    private final FieldOfObservation fieldOfObservation;

    /**
     * The list of the footprints in time.
     */
    private final List<List<List<GeodeticPoint>>> footprintsInTime =
        new ArrayList<>();

    /**
     * All the cartesians of all the points in time.
     */
    private final List<List<Cartesian>> pointsCartesiansInTime;

    /**
     * List of all the polygons used to describe the surface covered.
     */
    private final List<Polygon> polygon;

    // Constructors

    /**
     * Basic constructor for the surface of the body.
     *
     * @param satelliteInput : The satellite that will look at the covered
     *        surface.
     * @param fieldOfObservationInput : The field of observation of the
     *        satellite that will define the surface covered.
     */
    CoveredSurfaceOnBody(final Spacecraft satelliteInput,
                         final FieldOfObservation fieldOfObservationInput) {
        this(satelliteInput, fieldOfObservationInput,
             DEFAULT_ID +
                                                      satelliteInput.getId() +
                                                      "/" +
                                                      fieldOfObservationInput
                                                          .getBody()
                                                          .getBodyFrame()
                                                          .toString(),
             false, true, DEFAULT_COLOR);
    }

    /**
     * Constructor with a custom ID.
     *
     * @param satelliteInput : The satellite that will look at the covered
     *        surface.
     * @param fieldOfObservationInput : The field of observation of the
     *        satellite that will define the surface covered.
     * @param customID : The custom ID of the covered surface on body object.
     * @param fill : Custom parameter for the fill property
     * @param outline : Custom parameter for the outline property
     * @param color : Custom parameter for the color of the polygons.
     */
    CoveredSurfaceOnBody(final Spacecraft satelliteInput,
                         final FieldOfObservation fieldOfObservationInput,
                         final String customID, final boolean fill,
                         final boolean outline, final Color color) {

        this.setId(customID);
        this.setName(DEFAULT_NAME +
                     satelliteInput.getId() + " on : " +
                     fieldOfObservationInput.getBody().getBodyFrame());
        this.setAvailability(satelliteInput.getAvailability());

        this.satellite = satelliteInput;
        this.fieldOfObservation = fieldOfObservationInput;
        this.initialFovToBody =
            fieldOfObservationInput.getInitialTransformFovToBody();
        this.fov = fieldOfObservationInput.getFov();

        final List<PointOnBody> pointsOnBody = fieldOfObservation.getPoints();
        final List<List<Cartesian>> tempCartesians = new ArrayList<>();
        for (final PointOnBody currentPoint : pointsOnBody) {
            final List<Cartesian> currentCartesian =
                currentPoint.getCartesians();
            tempCartesians.add(currentCartesian);
        }

        final List<Cartesian> cartesiansToBuildOnePolygon = new ArrayList<>();
        pointsCartesiansInTime = sortingListList(tempCartesians);
        for (final List<Cartesian> currentCartesianList : pointsCartesiansInTime) {
            cartesiansToBuildOnePolygon.addAll(currentCartesianList);
        }

        this.polygon = new ArrayList<>();
        for (int i = 0; i < pointsCartesiansInTime.size() - 1; i++) {

            // Technically we'll be leaving off the very last polygon, but is
            // that
            // really such a big deal?
            final JulianDate t0 = fieldOfObservation.getJulianDates().get(i);
            final JulianDate t1 =
                fieldOfObservation.getJulianDates().get(i + 1);
            final TimeInterval tInterval = new TimeInterval(t0, t1);
            ;

            // Get current polygon and close it off
            final List<Cartesian> currentCartesianList =
                pointsCartesiansInTime.get(i);

            // Add polygon to list
            this.polygon.add(Polygon.builder(currentCartesianList, tInterval)
                .withColor(color).withOutline(outline).withFill(fill).build());
        }

    }

    /**
     * Builder covered surface on body builder.
     *
     * @param satelliteInput the satellite input
     * @param fieldOfObservationInput the field of observation input
     * @return the covered surface on body builder
     */
    public static CoveredSurfaceOnBodyBuilder
        builder(final Spacecraft satelliteInput,
                final FieldOfObservation fieldOfObservationInput) {
        return new CoveredSurfaceOnBodyBuilder(satelliteInput,
                                               fieldOfObservationInput);
    }

    // Overrides

    @Override
    public void writeCzmlBlock(final CesiumStreamWriter stream,
                               final CesiumOutputStream output)
        throws URISyntaxException,
            IOException {

        output.setPrettyFormatting(true);

        int i = 0;
        for (Polygon poly : polygon) {
            try (PacketCesiumWriter packet = stream.openPacket(output)) {

                final String currentId =
                    getId() +
                                         "/" +
                                         fieldOfObservation.getBody()
                                             .getBodyFrame().getName() +
                                         NUMBER + i;

                packet.writeId(currentId);

                final String currentName =
                    getName() +
                                           " " + satellite.getName() + " on " +
                                           fieldOfObservation.getBody()
                                               .getBodyFrame().getName() +
                                           NUMBER + i;
                packet.writeName(currentName);
                packet.writeAvailability(poly.getAvailability());

                poly.write(packet, output);
            }
            i += 1;
        }
    }

    // GETTERS

    /**
     * Gets satellite.
     *
     * @return the satellite
     */
    public Spacecraft getSatellite() {
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
     * Gets polygon.
     *
     * @return the polygon
     */
    public List<Polygon> getPolygon() {
        return polygon;
    }

    // Private functions

    /**
     * This method aims at reorganizing the list of list of objects by inverting
     * indexes.
     *
     * @param objects : A list of objects.
     * @param <T> : An object to be sorted.
     * @return A sorted list of objects.
     */
    private <T> List<List<T>> sortingListList(final List<List<T>> objects) {
        final List<List<T>> toReturn = new ArrayList<>();
        for (int i = 0; i < objects.get(0).size(); i++) {
            final List<T> sortedList = new ArrayList<>();
            for (List<T> object : objects) {
                final T objectsToSort = object.get(i);
                sortedList.add(objectsToSort);
            }
            toReturn.add(sortedList);
        }
        return toReturn;
    }

}
