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

import cesiumlanguagewriter.BooleanCesiumWriter;
import cesiumlanguagewriter.Cartesian;
import cesiumlanguagewriter.PacketCesiumWriter;
import cesiumlanguagewriter.PathCesiumWriter;
import cesiumlanguagewriter.PolylineMaterialCesiumWriter;
import cesiumlanguagewriter.PositionCesiumWriter;
import cesiumlanguagewriter.Reference;
import cesiumlanguagewriter.SolidColorMaterialCesiumWriter;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.orekit.bodies.BodyShape;
import org.orekit.czml.CzmlObjects.CzmlSecondaryObjects.TimePosition;
import org.orekit.czml.CzmlObjects.CzmlShow;
import org.orekit.czml.CzmlObjects.Path;
import org.orekit.czml.CzmlObjects.Polyline;
import org.orekit.frames.Frame;
import org.orekit.time.AbsoluteDate;

import java.awt.Color;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Ground track class
 *
 * <p> The ground track class aims at displaying the ground track of a satellite orbiting. </p>
 *
 * @author Julien LEBLOND
 * @since 1.0.0
 */

public class GroundTrack extends AbstractPrimaryObject implements CzmlPrimaryObject {

    /** The default ID of the ground track object. */
    public static final String DEFAULT_ID = "GROUND_TRACK/";

    /** The default name of the ground track object. */
    public static final String DEFAULT_NAME = "Ground track of : ";

    /** This allows to get the reference of an object. */
    public static final String DEFAULT_H_POSITION = "#position";

    /** The default of a ground track of a constellation. */
    public static final String DEFAULT_CONSTELLATION_ID = "GROUND_TRACK_OF_CONSTELLATION/";


    /** The default string to defines the number of satellites. */
    public static final String DEFAULT_CONSTELLATION_NUMBER_OF_SAT = " satellites";

    /** The default color of the ground track on the body. */
    public static final Color DEFAULT_COLOR = new Color(255, 255, 255);


    /** The satellite which the ground track is computed. */
    private Satellite satellite;

    /** To display or not the link between the satellite and the ground station. */
    private Boolean displayLinkSatellite = false;

    /** The list of cartesian that defines the positions of the satellite. */
    private List<Cartesian> cartesiansSatellite;

    /** A time position object that defines the position in time of the ground track. */
    private TimePosition clampedPositionOnBody;

    /** The color of the ground track. */
    private Color color;

    /** The list of all the ground track when several are computed. */
    private List<GroundTrack> allGroundTracks = new ArrayList<>();


    // Constructors
    public GroundTrack(final Satellite satellite, final BodyShape body) {
        this(satellite, body, DEFAULT_COLOR);
    }

    public GroundTrack(final Satellite satellite, final BodyShape body, final Color color) {
        this.satellite = satellite;
        this.setId(DEFAULT_ID + satellite.getId());
        this.setName(DEFAULT_NAME + satellite.getName());
        this.setAvailability(Header.getMasterClock()
                                   .getAvailability());
        this.color          = color;
        cartesiansSatellite = satellite.getCartesianArraylist();
        final List<AbsoluteDate> allSatelliteDates      = satellite.getAbsoluteDateList();
        final List<Cartesian>    projectedCartesianList = new ArrayList<>();
        for (int i = 0; i < cartesiansSatellite.size(); i++) {
            final Cartesian currentCartesian = cartesiansSatellite.get(i);
            final Vector3D currentVector3D = new Vector3D(currentCartesian.getX(), currentCartesian.getY(),
                    currentCartesian.getZ());
            final Frame bodyFrame = body.getBodyFrame();
            final Vector3D projectedVector3D = body.projectToGround(currentVector3D, allSatelliteDates.get(i),
                    bodyFrame);
            final Cartesian projectedCartesian = new Cartesian(projectedVector3D.getX(), projectedVector3D.getY(),
                    projectedVector3D.getZ());
            projectedCartesianList.add(projectedCartesian);
        }
        this.clampedPositionOnBody = new TimePosition(projectedCartesianList, satellite.getTimeList());
    }

    public GroundTrack(final Constellation constellation, final BodyShape body) {
        this(constellation, body, DEFAULT_COLOR);
    }

    public GroundTrack(final Constellation constellation, final BodyShape body, final Color color) {
        final List<Satellite> allSatellites = constellation.getAllSatellites();
        this.color           = color;
        this.allGroundTracks = new ArrayList<>();
        this.setId(DEFAULT_CONSTELLATION_ID + constellation.getId());
        this.setName(DEFAULT_NAME + constellation.getTotalOfSatellite() + DEFAULT_CONSTELLATION_NUMBER_OF_SAT);
        this.setAvailability(Header.getMasterClock()
                                   .getAvailability());
        for (final Satellite currentSat : allSatellites) {
            final GroundTrack currentGroundTrack = new GroundTrack(currentSat, body, currentSat.getColor());
            allGroundTracks.add(currentGroundTrack);
        }
    }

    private static Iterable<Reference> convertToIterable(final Reference[] array) {
        return () -> Arrays.stream(array)
                           .iterator();
    }

    // Getters

    // Overrides
    @Override
    public void writeCzmlBlock() throws URISyntaxException, IOException {
        if (allGroundTracks.isEmpty()) {
            OUTPUT.setPrettyFormatting(true);
            try (PacketCesiumWriter packet = STREAM.openPacket(OUTPUT)) {
                packet.writeId(getId());
                packet.writeName(getName());
                packet.writeAvailability(getAvailability());

                writePosition(packet);
                writeCzmlPath(packet);
                if (displayLinkSatellite) {
                    final Reference groundTrackReference = new Reference(this.getId() + DEFAULT_H_POSITION);
                    final Reference satelliteReference = new Reference(this.getSatellite()
                                                                           .getId() + DEFAULT_H_POSITION);
                    final Reference[] referenceList = Arrays.asList(groundTrackReference, satelliteReference)
                                                            .toArray(new Reference[0]);
                    final Iterable<Reference> referenceIterable = convertToIterable(referenceList);
                    final CzmlShow show = new CzmlShow(true, Header.getMasterClock()
                                                                   .getAvailability());
                    final List<CzmlShow> shows = new ArrayList<>();
                    shows.add(show);
                    final Polyline polylineInput = new Polyline();
                    polylineInput.writePolylineOfVisibility(packet, OUTPUT, referenceIterable, shows);
                }
            }
            cleanObject();
        } else {
            OUTPUT.setPrettyFormatting(true);
            for (final GroundTrack currentGroundTrack : allGroundTracks) {
                try (PacketCesiumWriter packet = STREAM.openPacket(OUTPUT)) {
                    packet.writeId(currentGroundTrack.getId());
                    packet.writeName(currentGroundTrack.getName());
                    packet.writeAvailability(currentGroundTrack.getAvailability());

                    this.satellite             = currentGroundTrack.getSatellite();
                    this.clampedPositionOnBody = currentGroundTrack.clampedPositionOnBody;
                    this.color                 = currentGroundTrack.getColor();

                    writePosition(packet);
                    writeCzmlPath(packet);
                    if (displayLinkSatellite) {
                        final Reference groundTrackReference = new Reference(
                                currentGroundTrack.getId() + DEFAULT_H_POSITION);
                        final Reference satelliteReference = new Reference(currentGroundTrack.getSatellite()
                                                                                             .getId() + DEFAULT_H_POSITION);
                        final Reference[] referenceList = Arrays.asList(groundTrackReference, satelliteReference)
                                                                .toArray(new Reference[0]);
                        final Iterable<Reference> referenceIterable = convertToIterable(referenceList);
                        final CzmlShow show = new CzmlShow(true, Header.getMasterClock()
                                                                       .getAvailability());
                        final List<CzmlShow> shows = new ArrayList<>();
                        shows.add(show);
                        final Polyline polylineInput = Polyline.nonVectorBuilder()
                                                               .withColor(currentGroundTrack.getColor())
                                                               .build();
                        polylineInput.writePolylineOfVisibility(packet, OUTPUT, referenceIterable, shows);
                    }
                }
            }
            cleanObject();
        }
    }

    @Override
    public StringWriter getStringWriter() {
        return STRING_WRITER;
    }

    // GETTERS

    @Override
    public void cleanObject() {
        this.setId("");
        this.setName("");
        this.setAvailability(null);
        this.satellite             = null;
        this.cartesiansSatellite   = new ArrayList<>();
        this.clampedPositionOnBody = null;
        this.allGroundTracks       = new ArrayList<>();
    }

    public Satellite getSatellite() {
        return satellite;
    }

    public TimePosition getClampedPositionOnBody() {
        return clampedPositionOnBody;
    }

    public List<Cartesian> getCartesiansSatellite() {
        return cartesiansSatellite;
    }

    public Color getColor() {
        return color;
    }

    public List<GroundTrack> getAllGroundTracks() {
        if (allGroundTracks.isEmpty()) {
            throw new RuntimeException(
                    "The ground tracks are empty, either the file is already written or the ground track is not build with a constellation");
        }
        return allGroundTracks;
    }

    public void displayLinkSatellite() {
        displayLinkSatellite = true;
    }

    // Private functions

    public Boolean getDisplayLinkSatellite() {
        return displayLinkSatellite;
    }

    private void writePosition(final PacketCesiumWriter packet) {
        try (PositionCesiumWriter positionWriter = packet.getPositionWriter()) {
            positionWriter.open(OUTPUT);
            positionWriter.writeReferenceFrame(clampedPositionOnBody.getReferenceFrame());
            positionWriter.writeInterpolationAlgorithm(clampedPositionOnBody.getCesiumInterpolationAlgorithm());
            positionWriter.writeInterpolationDegree(clampedPositionOnBody.getInterpolationDegree());
            positionWriter.writeCartesian(clampedPositionOnBody.getDates(), clampedPositionOnBody.getPositions());
        }
    }

    private void writeCzmlPath(final PacketCesiumWriter packet) {
        try (PathCesiumWriter pathProperty = packet.openPathProperty()) {
            if (!satellite.getDisplayOnlyOnePeriod()) {
                final Path path = new Path(getAvailability(), packet);
                try (BooleanCesiumWriter showPath = pathProperty.openShowProperty()) {
                    showPath.writeInterval(getAvailability().getStart(), getAvailability().getStop());
                    showPath.writeBoolean(path.getShow());
                }
            } else {
                final Path path = new Path(getAvailability(), packet);
                pathProperty.writeLeadTimeProperty(satellite.getOrbits()
                                                            .get(0)
                                                            .getKeplerianPeriod());
                pathProperty.writeTrailTimeProperty(0.0);
                try (BooleanCesiumWriter showPath = pathProperty.openShowProperty()) {
                    showPath.writeInterval(getAvailability().getStart(), getAvailability().getStop());
                    showPath.writeBoolean(path.getShow());
                }
            }

            try (PolylineMaterialCesiumWriter materialWriter = pathProperty.getMaterialWriter()) {
                materialWriter.open(OUTPUT);
                OUTPUT.writeStartObject();
                try (SolidColorMaterialCesiumWriter solidColorWriter = materialWriter.getSolidColorWriter()) {
                    solidColorWriter.open(OUTPUT);
                    solidColorWriter.writeColorProperty(color);
                }
                OUTPUT.writeEndObject();
            }
        }
    }

}

