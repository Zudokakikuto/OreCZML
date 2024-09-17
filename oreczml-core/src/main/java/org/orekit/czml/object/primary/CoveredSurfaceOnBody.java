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
import cesiumlanguagewriter.PacketCesiumWriter;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.czml.archi.builder.PolygonBuilder;
import org.orekit.czml.object.nonvisual.AbstractPointOnBody;
import org.orekit.czml.object.secondary.Polygon;
import org.orekit.frames.Transform;
import org.orekit.geometry.fov.FieldOfView;

import java.awt.Color;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class CoveredSurfaceOnBody extends AbstractPrimaryObject implements CzmlPrimaryObject {

    /** The default ID for the covered surface. */
    public static final String DEFAULT_ID = "COVERED_SURFACE/";

    /** The default name for the covered surface. */
    public static final String DEFAULT_NAME = "Covered surface of : ";

    /** The satellite that will cover the surface. */
    private Satellite satellite;

    /** The field of view of the satellite that will observe the body. */
    private FieldOfView fov;

    /** The transform inputted that represents the fov of the object and how it looks at the body. */
    private Transform initialFovToBody;

    /** The field of observation of the satellite that will define the surface covered. */
    private FieldOfObservation fieldOfObservation;

    /** The body observed. */
    private OneAxisEllipsoid body;

    /** The list of the footprints in time. */
    private List<List<List<GeodeticPoint>>> footprintsInTime = new ArrayList<>();

    /** All the cartesians of all the points in time. */
    private List<List<Cartesian>> allPointsCartesiansInTime;

    /** List of all the polygons used to describe the surface covered. */
    private Polygon polygon;

    // Constructors

    /**
     * Basic constructor for the surface of the body.
     *
     * @param satelliteInput          : The satellite that will look at the covered surface.
     * @param fieldOfObservationInput : The field of observation of the satellite that will define the surface covered.
     */
    public CoveredSurfaceOnBody(final Satellite satelliteInput, final FieldOfObservation fieldOfObservationInput) {

        this.setId(DEFAULT_ID + satelliteInput.getId() + "/" + fieldOfObservationInput.getBody()
                                                                                      .getBodyFrame()
                                                                                      .toString());
        this.setName(DEFAULT_NAME + satelliteInput.getId() + " on : " + fieldOfObservationInput.getBody()
                                                                                               .getBodyFrame());
        this.setAvailability(satelliteInput.getAvailability());

        this.satellite          = satelliteInput;
        this.fieldOfObservation = fieldOfObservationInput;
        this.initialFovToBody   = fieldOfObservationInput.getInitialFovToBody();
        this.fov                = fieldOfObservationInput.getFov();

        final List<AbstractPointOnBody> pointsOnBody   = fieldOfObservation.getAllPoints();
        final List<List<Cartesian>>     tempCartesians = new ArrayList<>();
        for (final AbstractPointOnBody currentPoint : pointsOnBody) {
            final List<Cartesian> currentCartesian = currentPoint.getCartesians();
            tempCartesians.add(currentCartesian);
        }

        final List<Cartesian> cartesiansToBuildOnePolygon = new ArrayList<>();
        allPointsCartesiansInTime = sortingListList(tempCartesians);
        for (final List<Cartesian> currentCartesianList : allPointsCartesiansInTime) {
            cartesiansToBuildOnePolygon.addAll(currentCartesianList);
        }

        this.polygon = new PolygonBuilder(cartesiansToBuildOnePolygon).withAvailability(
                                                                              satelliteInput.getAvailability())
                                                                      .withColor(new Color(34, 155, 83))
                                                                      .withOutline(true)
                                                                      .withFill(false)
                                                                      .build();
    }


    // Overrides

    @Override
    public void writeCzmlBlock() throws URISyntaxException, IOException {
        OUTPUT.setPrettyFormatting(true);
        try (PacketCesiumWriter packet = STREAM.openPacket(OUTPUT)) {
            packet.writeId(getId() + "/" + polygon.toString());
            packet.writeName(getName() + " " + polygon);
            packet.writeAvailability(getAvailability());
            polygon.write(packet, OUTPUT);
        }
    }

    @Override
    public StringWriter getStringWriter() {
        return STRING_WRITER;
    }

    @Override
    public void cleanObject() {
        satellite                 = null;
        fov                       = null;
        initialFovToBody          = null;
        body                      = null;
        fieldOfObservation        = null;
        footprintsInTime          = new ArrayList<>();
        allPointsCartesiansInTime = new ArrayList<>();
        polygon                   = null;
    }

    // GETTERS

    public Satellite getSatellite() {
        return satellite;
    }

    public FieldOfView getFov() {
        return fov;
    }

    public Transform getInitialFovToBody() {
        return initialFovToBody;
    }

    public FieldOfObservation getFieldOfObservation() {
        return fieldOfObservation;
    }

    public OneAxisEllipsoid getBody() {
        return body;
    }

    public List<List<List<GeodeticPoint>>> getFootprintsInTime() {
        return new ArrayList<>(footprintsInTime);
    }

    public List<List<Cartesian>> getAllPointsCartesiansInTime() {
        return new ArrayList<>(allPointsCartesiansInTime);
    }

    public Polygon getPolygon() {
        return polygon;
    }
}
