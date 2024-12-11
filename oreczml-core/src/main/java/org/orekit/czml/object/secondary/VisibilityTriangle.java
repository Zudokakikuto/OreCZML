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
package org.orekit.czml.object.secondary;

import cesiumlanguagewriter.Cartesian;
import cesiumlanguagewriter.CesiumArcType;
import cesiumlanguagewriter.CesiumOutputStream;
import cesiumlanguagewriter.CesiumStreamWriter;
import cesiumlanguagewriter.MaterialCesiumWriter;
import cesiumlanguagewriter.PacketCesiumWriter;
import cesiumlanguagewriter.PolygonCesiumWriter;
import cesiumlanguagewriter.PositionCesiumWriter;
import cesiumlanguagewriter.Reference;
import cesiumlanguagewriter.SolidColorMaterialCesiumWriter;
import cesiumlanguagewriter.TimeInterval;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.orekit.czml.object.CzmlShow;
import org.orekit.czml.object.Utils.DateUtils;
import org.orekit.czml.object.primary.AbstractPrimaryObject;
import org.orekit.czml.object.primary.Header;
import org.orekit.czml.object.primary.visu.LineOfVisibility;
import org.orekit.frames.TopocentricFrame;
import org.orekit.propagation.SpacecraftState;
import org.orekit.time.AbsoluteDate;

import java.awt.Color;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Visibility Triangle.
 *
 * <p> This class aims at creating a triangle visible when a line of visibility is created. It shows the entire
 * plan where the line will navigate. </p>
 */

public class VisibilityTriangle extends AbstractPrimaryObject {

    /** The default string for the ID of the triangle. */
    public static final String DEFAULT_ID = "TRIANGLE_VIS/";

    /** The default string for references points. */
    public static final String REFERENCE_POINT = "REF_POINT/";

    /** The default string for the reference to the position. */
    public static final String DEFAULT_H_POSITION = "#position";

    /** The default name for the visibility triangle. */
    public static final String DEFAULT_NAME = "Visibility triangle for ";

    /** Line of visibility created to build the object. */
    private LineOfVisibility line;

    /** The positions in cartesian of the satellite. */
    private List<SpacecraftState> spacecraftStatesSatellite;

    /** The topocentric frame representing the ground station. */
    private TopocentricFrame topocentricStation;

    /** The list of the availability of the triangles. */
    private List<TimeInterval> availabilityTriangles = new ArrayList<>();

    /** The topocentric frame when several stations are considered. */
    private List<TopocentricFrame> topocentricFrames = new ArrayList<>();

    /** Availability of the triangles. */
    private List<CzmlShow> shows;

    /** Header of the simulations. */
    private Header header;

    /** The list containing all the id of the references points. */
    private List<List<String>> idRefPoints = new ArrayList<>();

    /** List of three points for each triangle. */
    private List<List<Cartesian>> trianglesCartesians = new ArrayList<>();

    public VisibilityTriangle(final LineOfVisibility line, final Header header) {
        this.shows = line.getShowList();
        this.setId(DEFAULT_ID + line.getSatellite()
                                    .getId());
        this.setName(DEFAULT_NAME + line.getSatellite()
                                        .getName());
        this.line                      = line;
        this.header                    = header;
        this.spacecraftStatesSatellite = line.getSatellite()
                                             .getSpaceCraftStates();
        this.trianglesCartesians       = buildTriangleCartesians(shows, spacecraftStatesSatellite);
        this.availabilityTriangles     = buildTrueIntervals(shows);
    }

    @Override
    public void writeCzmlBlock(final CesiumStreamWriter stream,
                               final CesiumOutputStream output) throws URISyntaxException, IOException {
        output.setPrettyFormatting(true);
        for (int i = 0; i < trianglesCartesians.size(); i++) {
            final List<String> idStartStopPoints = new ArrayList<>();
            for (int j = 1; j < trianglesCartesians.get(i)
                                                   .size(); j++) {
                try (PacketCesiumWriter packet = stream.openPacket(output)) {
                    final String idCurrentRefPoint = REFERENCE_POINT + line.getSatellite()
                                                                           .getId() + "N" + i + "." + j;
                    packet.writeId(idCurrentRefPoint);
                    idStartStopPoints.add(idCurrentRefPoint);
                    packet.writeName("Reference point number " + i + "." + j + " for satellite " + line.getSatellite()
                                                                                                       .getName());
                    packet.writeAvailability(availabilityTriangles.get(i));
                    try (PositionCesiumWriter positionCesiumWriter = packet.getPositionWriter()) {
                        positionCesiumWriter.open(output);
                        positionCesiumWriter.writeInterval(availabilityTriangles.get(i));
                        positionCesiumWriter.writeCartesian(trianglesCartesians.get(i)
                                                                               .get(j));
                    }
                }
                this.idRefPoints.add(idStartStopPoints);
            }
        }

        final List<List<Reference>> referencesBuilt = buildReferences(idRefPoints, line.getReferenceList());

        for (int i = 0; i < trianglesCartesians.size(); i++) {
            try (PacketCesiumWriter packet = stream.openPacket(output)) {
                packet.writeId(getId());
                packet.writeName(getName());
                packet.writeAvailability(availabilityTriangles.get(i));
                try (PolygonCesiumWriter polygonCesiumWriter = packet.getPolygonWriter()) {
                    polygonCesiumWriter.open(output);
                    polygonCesiumWriter.writeFillProperty(true);
                    polygonCesiumWriter.writeArcTypeProperty(CesiumArcType.NONE);
                    polygonCesiumWriter.writeShowProperty(true);
                    polygonCesiumWriter.writePositionsPropertyReferences(referencesBuilt.get(i));
                    try (MaterialCesiumWriter materialCesiumWriter = polygonCesiumWriter.openMaterialProperty()) {
                        try (SolidColorMaterialCesiumWriter solidColorMaterialCesiumWriter = materialCesiumWriter.openSolidColorProperty()) {
                            solidColorMaterialCesiumWriter.writeColorProperty(new Color(238, 144, 37, 104));
                        }
                    }
                }
            }
        }
    }


    // Private functions

    /**
     * This function builds the cartesians of each visibility triangle.
     *
     * @param showsInput  : The list of CzmlShow corresponding to the line of visibility.
     * @param statesInput : The list of spacecraft states of the satellite.
     * @return : A list of list of cartesians representing a list of triplets of positions of points for each triangle.
     */
    private List<List<Cartesian>> buildTriangleCartesians(final List<CzmlShow> showsInput,
                                                          final List<SpacecraftState> statesInput) {
        final List<List<Cartesian>> toReturn = new ArrayList<>();

        for (final CzmlShow currentShow : showsInput) {
            // The triplet for the triangle that will contain the three positions:
            // the station, the positions at the start and at the end of the approach.
            final List<Cartesian> currentTriplet = new ArrayList<>();
            // The second object of the czml show built by a line of visibility is a topocentric frame
            final TopocentricFrame topocentricShow         = (TopocentricFrame) currentShow.getObject2();
            final Vector3D         positionTopocentricShow = topocentricShow.getCartesianPoint();

            // If the time interval considered is one where the satellite is above the station :
            if (currentShow.getShow()) {

                // Add the position of the station to the triplet
                currentTriplet.add(new Cartesian(positionTopocentricShow.getX(), positionTopocentricShow.getY(),
                        positionTopocentricShow.getZ()));

                // Get the interval and the boundaries of the interval.
                final TimeInterval currentTimeInterval = currentShow.getAvailability();
                final AbsoluteDate startInterval = DateUtils.toAbsoluteDate(currentTimeInterval.getStart(),
                        header.getTimeScale());
                final AbsoluteDate stopInterval = DateUtils.toAbsoluteDate(currentTimeInterval.getStop(),
                        header.getTimeScale());

                for (final SpacecraftState currentState : statesInput) {
                    // If the state is the starting state of the approach
                    if (currentState.getDate()
                                    .isCloseTo(startInterval, 31)) {
                        final Vector3D position = currentState.getPosition();
                        final Cartesian positionCartesian = new Cartesian(position.getX(), position.getY(),
                                position.getZ());
                        currentTriplet.add(positionCartesian);
                    }
                    // If the state is the stopping state of the approach
                    else if (currentState.getDate()
                                         .isCloseTo(stopInterval, 31)) {
                        final Vector3D position = currentState.getPosition();
                        final Cartesian positionCartesian = new Cartesian(position.getX(), position.getY(),
                                position.getZ());
                        currentTriplet.add(positionCartesian);
                    }
                }
                toReturn.add(currentTriplet);
            }
        }
        return toReturn;
    }

    /**
     * This function builds the time interval where the satellite is above the station.
     *
     * @param showsInput : The czml show considered
     * @return : A list of time intervals where the satellite is above the station
     */
    private List<TimeInterval> buildTrueIntervals(final List<CzmlShow> showsInput) {
        final List<TimeInterval> toReturn = new ArrayList<>();
        for (CzmlShow czmlShow : showsInput) {
            if (czmlShow
                    .getShow()) {
                toReturn.add(czmlShow
                        .getAvailability());
            }
        }
        return toReturn;
    }

    /**
     * This function aims at building the list of references used for positionning the triangles.
     *
     * @param idsPoints            : The ids of the reference points used to place the start and stop points of visibility.
     * @param referencesLineOfVisu : The references from the line of visibility
     * @return : A list of list of the references. Each sublist attributed to each time interval of visibility.
     */
    private List<List<Reference>> buildReferences(final List<List<String>> idsPoints,
                                                  final List<Reference> referencesLineOfVisu) {
        // Extract the reference of the station that is the first reference of the list
        final Reference stationReference = referencesLineOfVisu.get(0);

        final List<List<Reference>> toReturn = new ArrayList<>();

        for (List<String> idsPoint : idsPoints) {
            final List<Reference> tempReturn = new ArrayList<>();
            tempReturn.add(new Reference(idsPoint.get(0) + DEFAULT_H_POSITION));
            tempReturn.add(new Reference(idsPoint.get(1) + DEFAULT_H_POSITION));
            tempReturn.add(stationReference);
            toReturn.add(tempReturn);
        }
        return toReturn;
    }
}
