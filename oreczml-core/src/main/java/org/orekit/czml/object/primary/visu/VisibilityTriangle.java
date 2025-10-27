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
package org.orekit.czml.object.primary.visu;

import cesiumlanguagewriter.Cartesian;
import cesiumlanguagewriter.CesiumOutputStream;
import cesiumlanguagewriter.CesiumStreamWriter;
import cesiumlanguagewriter.MaterialCesiumWriter;
import cesiumlanguagewriter.PacketCesiumWriter;
import cesiumlanguagewriter.PolygonCesiumWriter;
import cesiumlanguagewriter.PositionListCesiumWriter;
import cesiumlanguagewriter.SolidColorMaterialCesiumWriter;
import cesiumlanguagewriter.TimeInterval;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.czml.object.CzmlShow;
import org.orekit.czml.object.primary.AbstractPrimaryObject;
import org.orekit.czml.object.utils.DateUtils;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.frames.TopocentricFrame;
import org.orekit.propagation.SpacecraftState;
import org.orekit.time.AbsoluteDate;
import org.orekit.utils.Constants;
import org.orekit.utils.IERSConventions;

import java.awt.Color;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Visibility Triangle.
 * <p>
 * This class aims at creating a triangle visible when a line of visibility is
 * created. It shows the entire plan where the line will navigate.
 * </p>
 * <p>
 * This object has no constructor usable, and is called with the
 * .displayTriangle() method from the line of visibility.
 * </p>
 *
 * @author Julien LEBLOND
 * @since 1.1
 */
public class VisibilityTriangle
    extends
    AbstractPrimaryObject<VisibilityTriangle> {

    /** The default string for the ID of the triangle. */
    public static final String DEFAULT_ID = "TRIANGLE_VIS/";

    /** The default name for the visibility triangle. */
    public static final String DEFAULT_NAME = "Visibility triangle for ";

    /** The list of the availability of the triangles. */
    private List<TimeInterval> availabilityTriangles = new ArrayList<>();

    /** List of three points for each triangle. */
    private List<List<Cartesian>> trianglesCartesians = new ArrayList<>();

    /** The line of visibility used. */
    private LineOfVisibility line;

    // Constructor

    /**
     * The classic constructor of the visibility triangle object.
     *
     * @param lineInput : The line of visibility used for the visibility
     *        triangle
     */
    VisibilityTriangle(final LineOfVisibility lineInput) {
        /* Availability of the triangles. */
        final List<CzmlShow> shows = lineInput.getShowList();
        this.setId(DEFAULT_ID + lineInput.getSpacecraft().getId());
        this.setName(DEFAULT_NAME + lineInput.getSpacecraft().getName());
        this.line = lineInput;
        final List<SpacecraftState> spacecraftStatesSatellite =
            lineInput.getSpacecraft().getSpaceCraftStates();
        this.trianglesCartesians =
            buildTriangleCartesians(shows, spacecraftStatesSatellite);
        this.availabilityTriangles = buildTrueIntervals(shows);
    }

    @Override
    public void writeCzmlBlock(final CesiumStreamWriter stream,
                               final CesiumOutputStream output)
        throws URISyntaxException,
            IOException {
        output.setPrettyFormatting(true);
        for (int i = 0; i < trianglesCartesians.size(); i++) {
            try (PacketCesiumWriter packet = stream.openPacket(output)) {
                packet.writeId(getId() +
                               "//" + availabilityTriangles.get(i).getStart() +
                               "/" + availabilityTriangles.get(i).getStop());
                packet.writeName(getName());
                packet.writeAvailability(availabilityTriangles.get(i));
                try (PolygonCesiumWriter polygonCesiumWriter =
                    packet.getPolygonWriter()) {
                    polygonCesiumWriter.open(output);
                    polygonCesiumWriter.writeShowProperty(true);
                    polygonCesiumWriter.writePerPositionHeightProperty(true);
                    try (PositionListCesiumWriter positionCesiumWriter =
                        polygonCesiumWriter.openPositionsProperty()) {
                        positionCesiumWriter
                            .writeCartesian(trianglesCartesians.get(i));
                        positionCesiumWriter
                            .writeInterval(availabilityTriangles.get(i));
                    }
                    try (MaterialCesiumWriter materialCesiumWriter =
                        polygonCesiumWriter.openMaterialProperty()) {
                        try (SolidColorMaterialCesiumWriter solidColorMaterialCesiumWriter =
                            materialCesiumWriter.openSolidColorProperty()) {
                            solidColorMaterialCesiumWriter
                                .writeColorProperty(new Color(238, 144, 37,
                                                              104));
                        }
                    }
                }
            }
        }
    }

    @Override
    public VisibilityTriangle cloneObject() {
        final VisibilityTriangle copy = new VisibilityTriangle(this.line);
        copy.setAvailability(getAvailability());
        copy.setId(getId());
        copy.setName(getName());
        return copy;
    }

    // Private functions

    /**
     * This function builds the cartesians of each visibility triangle.
     *
     * @param showsInput : The list of CzmlShow corresponding to the line of
     *        visibility.
     * @param statesInput : The list of spacecraft states of the satellite.
     * @return : A list of list of cartesians representing a list of triplets of
     *         positions of points for each triangle.
     */
    private List<List<Cartesian>>
        buildTriangleCartesians(final List<CzmlShow> showsInput,
                                final List<SpacecraftState> statesInput) {
        final List<List<Cartesian>> toReturn = new ArrayList<>();

        final Frame ITRF =
            FramesFactory.getITRF(IERSConventions.IERS_2010, true);
        final OneAxisEllipsoid earth =
            new OneAxisEllipsoid(Constants.IERS2010_EARTH_EQUATORIAL_RADIUS,
                                 Constants.IERS2010_EARTH_FLATTENING, ITRF);

        for (final CzmlShow currentShow : showsInput) {
            // The triplet for the triangle that will contain the three
            // positions:
            // the station, the positions at the start and at the end of the
            // approach.
            final List<Cartesian> pointsPositions = new ArrayList<>();
            // The second object of the czml show built by a line of visibility
            // is a topocentric frame
            final TopocentricFrame topocentricShow =
                (TopocentricFrame) currentShow.getObject();
            final Vector3D positionTopocentricShow =
                topocentricShow.getCartesianPoint();

            // If the time interval considered is one where the satellite is
            // above the station :
            if (currentShow.getShow()) {

                // Add the position of the station to the triplet
                pointsPositions
                    .add(new Cartesian(positionTopocentricShow.getX(),
                                       positionTopocentricShow.getY(),
                                       positionTopocentricShow.getZ()));

                // Get the interval and the boundaries of the interval.
                final TimeInterval currentTimeInterval =
                    currentShow.getClock().getAvailability();
                final AbsoluteDate startInterval =
                    DateUtils.toAbsoluteDate(currentTimeInterval.getStart());
                final AbsoluteDate stopInterval =
                    DateUtils.toAbsoluteDate(currentTimeInterval.getStop());

                for (final SpacecraftState currentState : statesInput) {
                    // If the state is the starting state of the approach
                    if (currentState.getDate().isCloseTo(startInterval, 31) ||
                        currentState.getDate().isCloseTo(stopInterval, 31)) {
                        final Cartesian positionCartesian =
                            buildCartesian(currentState, earth);
                        pointsPositions.add(positionCartesian);
                    } else if (currentState.getDate().isBetween(startInterval,
                                                                stopInterval)) {
                        final Cartesian positionCartesian =
                            buildCartesian(currentState, earth);
                        pointsPositions.add(positionCartesian);
                    }
                }
                toReturn.add(pointsPositions);
            }
        }
        return toReturn;
    }

    /**
     * This function builds the time interval where the satellite is above the
     * station.
     *
     * @param showsInput : The czml show considered
     * @return : A list of time intervals where the satellite is above the
     *         station
     */
    private List<TimeInterval>
        buildTrueIntervals(final List<CzmlShow> showsInput) {
        final List<TimeInterval> toReturn = new ArrayList<>();
        for (CzmlShow czmlShow : showsInput) {
            if (czmlShow.getShow()) {
                toReturn.add(czmlShow.getClock().getAvailability());
            }
        }
        return toReturn;
    }

    /**
     * Build the position point to add to the list.
     *
     * @param currentState : The current spacecraft state considered
     * @param earth : A model of the earth
     * @return : A cartesian point projected on the earth frame to be added to
     *         the list of cartesians.
     */
    private Cartesian buildCartesian(final SpacecraftState currentState,
                                     final OneAxisEllipsoid earth) {
        final Vector3D position = currentState.getPosition();
        final GeodeticPoint currentGeodetic =
            earth.transform(position, currentState.getFrame(),
                            currentState.getDate());
        final TopocentricFrame topocentricFrame =
            new TopocentricFrame(earth, currentGeodetic, "currentGeodetic");
        final Vector3D currentProjected = topocentricFrame.getCartesianPoint();
        return new Cartesian(currentProjected.getX(), currentProjected.getY(),
                             currentProjected.getZ());
    }
}
