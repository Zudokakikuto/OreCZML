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
package org.orekit.czml.object.primary;

import cesiumlanguagewriter.BooleanCesiumWriter;
import cesiumlanguagewriter.Cartesian;
import cesiumlanguagewriter.CesiumOutputStream;
import cesiumlanguagewriter.CesiumStreamWriter;
import cesiumlanguagewriter.PacketCesiumWriter;
import cesiumlanguagewriter.PathCesiumWriter;
import cesiumlanguagewriter.PolylineMaterialCesiumWriter;
import cesiumlanguagewriter.PositionCesiumWriter;
import cesiumlanguagewriter.Reference;
import cesiumlanguagewriter.SolidColorMaterialCesiumWriter;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.orekit.bodies.BodyShape;
import org.orekit.czml.object.CzmlShow;
import org.orekit.czml.object.Path;
import org.orekit.czml.object.Polyline;
import org.orekit.czml.object.primary.entities.Constellation;
import org.orekit.czml.object.primary.entities.Spacecraft;
import org.orekit.czml.object.secondary.Clock;
import org.orekit.czml.object.secondary.TimePosition;
import org.orekit.propagation.SpacecraftState;
import org.orekit.time.AbsoluteDate;

import java.awt.Color;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Ground track class
 * <p>
 * The ground track class aims at displaying the ground track of a satellite
 * orbiting.
 * </p>
 *
 * @author Julien LEBLOND
 * @since 1.0.0
 */
public class GroundTrack
    extends
    AbstractPrimaryObject {

    /**
     * The default ID of the ground track object.
     */
    public static final String DEFAULT_ID = "GROUND_TRACK/";

    /**
     * The default name of the ground track object.
     */
    public static final String DEFAULT_NAME = "Ground track of : ";

    /**
     * This allows getting the reference of an object.
     */
    public static final String DEFAULT_H_POSITION = "#position";

    /**
     * The default string that defines the number of satellites.
     */
    public static final String DEFAULT_CONSTELLATION_NUMBER_OF_SAT =
        " satellites";

    /**
     * The default color of the ground track on the body.
     */
    public static final Color DEFAULT_COLOR = new Color(255, 255, 255);

    // Intrinsic arguments

    /**
     * The satellite which the ground track is computed.
     */
    private Spacecraft spacecraft;

    /**
     * The color of the ground track.
     */
    private Color color;

    // Other arguments

    /**
     * The list of all the ground track when several are computed.
     */
    private List<GroundTrack> groundTracks = new ArrayList<>();

    /** The list of the spacecraft states. */
    private List<SpacecraftState> states = new ArrayList<>();

    /**
     * A time position object that defines the position in time of the ground
     * track.
     */
    private TimePosition clampedPositionOnBody;

    /**
     * To display or not the link between the satellite and the ground station.
     */
    private Boolean displayLinkSatellite = false;

    // Constructors

    /**
     * The basic constructor for the ground track object, with default
     * parameters for a satellite.
     *
     * @param spacecraft : The satellite object that the ground track will
     *        represent.
     * @param body : The body in which the ground track must be projected to.
     * @param clock : The clock considered.
     */
    GroundTrack(final Spacecraft spacecraft, final BodyShape body,
                final Clock clock) {
        this(spacecraft, body, DEFAULT_COLOR, DEFAULT_ID + spacecraft.getId(),
             clock);
    }

    /**
     * The constructor for the ground track object with no default parameters
     * for a satellite.
     *
     * @param spacecraft : The satellite that the ground track will follow.
     * @param body : The body in which the ground track must be projected to.
     * @param color : The color of the ground track.
     * @param customID : The custom ID for the ground track
     * @param clock : The clock considered.
     */
    GroundTrack(final Spacecraft spacecraft, final BodyShape body,
                final Color color, final String customID, final Clock clock) {

        this.spacecraft = spacecraft;
        this.setId(customID);
        this.setName(DEFAULT_NAME + spacecraft.getName());
        this.setAvailability(clock.getAvailability());
        this.color = color;
        final List<AbsoluteDate> satelliteDates =
            spacecraft.getAbsoluteDateList();
        this.states = spacecraft.getSpaceCraftStates();
        final List<Cartesian> projectedCartesianList = new ArrayList<>();
        for (int i = 0; i < states.size(); i++) {
            final Vector3D projectedVector3D =
                body.projectToGround(states.get(i).getPosition(),
                                     satelliteDates.get(i),
                                     body.getBodyFrame());
            projectedCartesianList.add(new Cartesian(projectedVector3D.getX(),
                                                     projectedVector3D.getY(),
                                                     projectedVector3D.getZ()));
        }
        this.clampedPositionOnBody =
            new TimePosition(projectedCartesianList,
                             spacecraft.getJulianDates());
    }

    /**
     * The basic constructor for the ground track object with default parameters
     * for a constellation.
     *
     * @param constellation : The constellation object that the ground track
     *        will represent.
     * @param body : The body in which the ground track must be projected to.
     * @param clock : The clock considered.
     */
    GroundTrack(final Constellation constellation, final BodyShape body,
                final Clock clock) {
        this(constellation, body, DEFAULT_COLOR,
             DEFAULT_ID + constellation.getId(), clock);
    }

    /**
     * The constructor for the ground track object with no default parameters
     * for a constellation.
     *
     * @param constellation : The constellation object that the ground track
     *        will represent.
     * @param body : The body in which the ground track must be projected to.
     * @param color : The color of the ground track.
     * @param customID : The custom ID for the ground track
     * @param clock : The clock considered.
     */
    GroundTrack(final Constellation constellation, final BodyShape body,
                final Color color, final String customID, final Clock clock) {

        final List<Spacecraft> satellites = constellation.getSatellites();
        this.color = color;
        this.groundTracks = new ArrayList<>();
        this.setId(customID);
        this.setName(DEFAULT_NAME +
                     constellation.getTotalOfSatellite() +
                     DEFAULT_CONSTELLATION_NUMBER_OF_SAT);
        this.setAvailability(clock.getAvailability());
        for (final Spacecraft currentSat : satellites) {
            final GroundTrack currentGroundTrack =
                new GroundTrack(currentSat, body, currentSat.getColor(),
                                customID + currentSat.getId(), clock);
            groundTracks.add(currentGroundTrack);
        }
    }

    // Builders

    /**
     * Builder ground track builder.
     *
     * @param satellite : The satellite
     * @param body : The body
     * @param clock : The clock
     * @return the ground track builder
     */
    public static GroundTrackBuilder builder(final Spacecraft satellite,
                                             final BodyShape body,
                                             final Clock clock) {
        return new GroundTrackBuilder(satellite, body, clock);
    }

    /**
     * Builder ground track builder.
     *
     * @param constellation : The constellation
     * @param body : The body
     * @param clock : The clock
     * @return the ground track builder
     */
    public static GroundTrackBuilder builder(final Constellation constellation,
                                             final BodyShape body,
                                             final Clock clock) {

        return new GroundTrackBuilder(constellation, body, clock);
    }

    // Overrides

    @Override
    public void writeCzmlBlock(final CesiumStreamWriter stream,
                               final CesiumOutputStream output)
        throws URISyntaxException,
            IOException {
        if (groundTracks.isEmpty()) {
            output.setPrettyFormatting(true);
            try (PacketCesiumWriter packet = stream.openPacket(output)) {
                packet.writeId(getId());
                packet.writeName(getName());
                packet.writeAvailability(getAvailability());

                writePosition(packet, output);
                writeCzmlPath(packet, output);
                if (displayLinkSatellite) {
                    final Reference groundTrackReference =
                        new Reference(this.getId() + DEFAULT_H_POSITION);
                    final Reference satelliteReference =
                        new Reference(this.getSpacecraft().getId() +
                                      DEFAULT_H_POSITION);
                    final Reference[] referenceList =
                        Arrays.asList(groundTrackReference, satelliteReference)
                            .toArray(new Reference[0]);
                    final Iterable<Reference> referenceIterable =
                        convertToIterable(referenceList);
                    final CzmlShow show = new CzmlShow(true, getAvailability());
                    final List<CzmlShow> shows = new ArrayList<>();
                    shows.add(show);
                    final Polyline polylineInput =
                        Polyline.nonVectorBuilder(getSpacecraft().getClock())
                            .build();
                    polylineInput.writePolylineOfVisibility(packet, output,
                                                            referenceIterable,
                                                            shows);
                }
            }
        } else {
            output.setPrettyFormatting(true);
            for (final GroundTrack currentGroundTrack : groundTracks) {
                try (PacketCesiumWriter packet = stream.openPacket(output)) {
                    packet.writeId(currentGroundTrack.getId());
                    packet.writeName(currentGroundTrack.getName());
                    packet.writeAvailability(currentGroundTrack
                        .getAvailability());

                    this.spacecraft = currentGroundTrack.getSpacecraft();
                    this.clampedPositionOnBody =
                        currentGroundTrack.clampedPositionOnBody;
                    this.color = currentGroundTrack.getColor();

                    writePosition(packet, output);
                    writeCzmlPath(packet, output);
                    if (displayLinkSatellite) {
                        final Reference groundTrackReference =
                            new Reference(currentGroundTrack.getId() +
                                          DEFAULT_H_POSITION);
                        final Reference satelliteReference =
                            new Reference(currentGroundTrack.getSpacecraft()
                                .getId() + DEFAULT_H_POSITION);
                        final Reference[] referenceList =
                            Arrays
                                .asList(groundTrackReference,
                                        satelliteReference)
                                .toArray(new Reference[0]);
                        final Iterable<Reference> referenceIterable =
                            convertToIterable(referenceList);
                        final CzmlShow show =
                            new CzmlShow(true, getAvailability());
                        final List<CzmlShow> shows = new ArrayList<>();
                        shows.add(show);
                        final Polyline polylineInput =
                            Polyline.nonVectorBuilder(spacecraft.getClock())
                                .withColor(currentGroundTrack.getColor())
                                .build();
                        polylineInput
                            .writePolylineOfVisibility(packet, output,
                                                       referenceIterable,
                                                       shows);
                    }
                }
            }
        }
    }

    // Getters

    /**
     * Gets satellite.
     *
     * @return the satellite
     */
    public Spacecraft getSpacecraft() {
        return spacecraft;
    }

    /**
     * Gets color.
     *
     * @return the color
     */
    public Color getColor() {
        return color;
    }
    // Setters

    /**
     * Display link satellite.
     */
    public void displayLinkSatellite() {
        displayLinkSatellite = true;
    }

    // Private functions

    /**
     * This function aims at writing the position of the ground track into a
     * given packet.
     *
     * @param packet : The packet that will write in the czml file.
     * @param output : The output stream of cesium that will contain the strings
     *        to write into the CzmLFile.
     */
    private void writePosition(final PacketCesiumWriter packet,
                               final CesiumOutputStream output) {
        try (PositionCesiumWriter positionWriter = packet.getPositionWriter()) {
            positionWriter.open(output);
            positionWriter
                .writeReferenceFrame(clampedPositionOnBody.getReferenceFrame());
            positionWriter.writeInterpolationAlgorithm(clampedPositionOnBody
                .getCesiumInterpolationAlgorithm());
            positionWriter.writeInterpolationDegree(clampedPositionOnBody
                .getInterpolationDegree());
            positionWriter.writeCartesian(clampedPositionOnBody.getDates(),
                                          clampedPositionOnBody.getPositions());
        }
    }

    /**
     * This function aims at writing the path of the ground track into a given
     * packet.
     *
     * @param packet : The packet that will write in the czml file.
     * @param output : The output stream of cesium that will contain the strings
     *        to write into the CzmLFile.
     */
    private void writeCzmlPath(final PacketCesiumWriter packet,
                               final CesiumOutputStream output) {
        try (PathCesiumWriter pathProperty = packet.openPathProperty()) {
            if (!spacecraft.getDisplayOnlyOnePeriod()) {
                final Path path = new Path(getAvailability());
                try (BooleanCesiumWriter showPath =
                    pathProperty.openShowProperty()) {
                    showPath.writeInterval(getAvailability().getStart(),
                                           getAvailability().getStop());
                    showPath.writeBoolean(path.getShow());
                }
            } else {
                final Path path = new Path(getAvailability());
                pathProperty.writeLeadTimeProperty(spacecraft.getOrbits().get(0)
                    .getKeplerianPeriod());
                pathProperty.writeTrailTimeProperty(0.0);
                try (BooleanCesiumWriter showPath =
                    pathProperty.openShowProperty()) {
                    showPath.writeInterval(getAvailability().getStart(),
                                           getAvailability().getStop());
                    showPath.writeBoolean(path.getShow());
                }
            }

            try (PolylineMaterialCesiumWriter materialWriter =
                pathProperty.getMaterialWriter()) {
                materialWriter.open(output);
                output.writeStartObject();
                try (SolidColorMaterialCesiumWriter solidColorWriter =
                    materialWriter.getSolidColorWriter()) {
                    solidColorWriter.open(output);
                    solidColorWriter.writeColorProperty(color);
                }
                output.writeEndObject();
            }
        }
    }

    /**
     * This function aims at converting an array of references into an iterable
     * object.
     *
     * @param array : An array of references.
     * @return : An iterable object of references.
     */
    private static Iterable<Reference>
        convertToIterable(final Reference[] array) {
        return () -> Arrays.stream(array).iterator();
    }
}
