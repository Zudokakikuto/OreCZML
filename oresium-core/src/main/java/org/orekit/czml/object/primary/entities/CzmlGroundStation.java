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

package org.orekit.czml.object.primary.entities;

import cesiumlanguagewriter.BillboardCesiumWriter;
import cesiumlanguagewriter.Cartesian;
import cesiumlanguagewriter.CesiumOutputStream;
import cesiumlanguagewriter.CesiumStreamWriter;
import cesiumlanguagewriter.PacketCesiumWriter;
import cesiumlanguagewriter.PositionCesiumWriter;
import cesiumlanguagewriter.TimeStandard;
import cesiumlanguagewriter.UriCesiumWriter;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.orekit.czml.object.nonvisual.CzmlModel;
import org.orekit.czml.object.primary.AbstractPrimaryObject;
import org.orekit.czml.object.primary.visu.StationVisibilityCircle;
import org.orekit.czml.object.secondary.Billboard;
import org.orekit.czml.object.secondary.Clock;
import org.orekit.czml.object.secondary.Label;
import org.orekit.frames.TopocentricFrame;

import java.util.ArrayList;
import java.util.List;

/**
 * CZML Ground Station
 * <p>
 * This class groups all the characteristics of a ground station. The ground
 * station will be represented at the surface of the central body with precise
 * cartographic parameters. It can be built from the orekit ground station
 * {@link org.orekit.estimation.measurements.GroundStation}, or from the orekit
 * topocentric frame {@link org.orekit.frames.TopocentricFrame}.
 * </p>
 *
 * @author LEBLOND Julien
 * @since 1.0.0
 */
public class CzmlGroundStation
    extends
    AbstractPrimaryObject<CzmlGroundStation> {

    /** The default image used when no image/model is used for the station. */
    public static final String DEFAULT_IMAGE =
        "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAADsMAAA7DAcdvqGQAAACvSURBVDhPrZDRDcMgDAU9GqN0lIzijw6SUbJJygUeNQgSqepJTyHG91LVVpwDdfxM3T9TSl1EXZvDwii471fivK73cBFFQNTT/d2KoGpfGOpSIkhUpgUMxq9DFEsWv4IXhlyCnhBFnZcFEEuYqbiUlNwWgMTdrZ3JbQFoEVG53rd8ztG9aPJMnBUQf/VFraBJeWnLS0RfjbKyLJA8FkT5seDYS1Qwyv8t0B/5C2ZmH2/eTGNNBgMmAAAAAElFTkSuQmCC";

    /** The default id for ground stations. */
    public static final String DEFAULT_ID = "GROUND_STATION/";

    /** The default name for ground stations. */
    public static final String DEFAULT_NAME = "Ground station : ";

    /** The default 3d model: empty. */
    public static final String DEFAULT_3D_MODEL = "";

    /**
     * The position on earth of a single station, it is the cartesian vector
     * from the geodetic point.
     */
    private Vector3D positionOnEarth;

    /** The list of all the position on earth of all the stations. */
    private final List<Vector3D> positionsOnEarth = new ArrayList<>();

    /** The billboard that will display the image of the station. */
    private final Billboard billboard;

    /** The topocentric frame of the station. */
    private final TopocentricFrame topocentricFrame;

    /** The description of the station. */
    private final String description;

    /** The model used to define the station if only one model is used. */
    private CzmlModel model;

    // Intrinsic parameters

    /** The visibility circle of the station. */
    private StationVisibilityCircle visibilityCircle;

    /** Boolean to know if the visibility circle is created or not. */
    private boolean displayCircle = false;

    /** The clock considered. */
    private final Clock clock;

    //// Constructors
    // Single Station Constructors

    /**
     * The constructor of the czml ground station object with a default model.
     *
     * @param topocentricFrame : The topocentric frame where the ground station
     *        must be located.
     * @param clock : The clock considered. *
     */
    public CzmlGroundStation(final TopocentricFrame topocentricFrame,
                             final Clock clock) {
        this(topocentricFrame, DEFAULT_3D_MODEL, clock);
    }

    /**
     * The constructor of the czml ground station object with no default
     * parameters.
     *
     * @param topocentricFrame : The topocentric frame where the ground station
     *        must be located.
     * @param modelPath : The path of the model to load.
     * @param clock : The availability of the ground station. *
     */
    public CzmlGroundStation(final TopocentricFrame topocentricFrame,
                             final String modelPath, final Clock clock) {

        this.topocentricFrame = topocentricFrame;
        this.setName(DEFAULT_NAME + topocentricFrame.getName());
        this.setId(DEFAULT_ID + topocentricFrame.getName());
        this.clock = clock;
        this.setAvailability(clock.getAvailability()
            .toTimeStandard(TimeStandard.COORDINATED_UNIVERSAL_TIME));
        final double latitude = topocentricFrame.getPoint().getLatitude();
        final double longitude = topocentricFrame.getPoint().getLongitude();
        this.description =
            "<!--HTML-->\r\n<p>Id : " +
                           DEFAULT_ID + topocentricFrame.getName() +
                           "</p\r\n<p>Longitude : " + longitude +
                           "</p>\r\n<p>Latitude : " + latitude +
                           "</p>\r\n<p>Simulated from : " +
                           getAvailability().getStart() + " to " +
                           getAvailability().getStop() + "</p>";
        this.positionsOnEarth.add(topocentricFrame.getCartesianPoint());
        this.billboard = new Billboard(DEFAULT_IMAGE);

        if (modelPath.isEmpty()) {
            this.model = null;
        } else {
            this.model = new CzmlModel(modelPath, 50, 300, 2, false, clock);
        }
    }

    // Builders

    /**
     * Builder czml ground station builder.
     *
     * @param topocentricFrameInput the topocentric frame input
     * @param clock : The clock.
     * @return the czml ground station builder
     */
    public static CzmlGroundStationBuilder
        builder(final TopocentricFrame topocentricFrameInput,
                final Clock clock) {
        return new CzmlGroundStationBuilder(topocentricFrameInput, clock);
    }

    // Overrides

    @Override
    public void writeCzmlBlock(final CesiumStreamWriter stream,
                               final CesiumOutputStream output) {
        this.positionOnEarth = positionsOnEarth.get(0);
        output.setPrettyFormatting(true);
        try (PacketCesiumWriter packet = stream.openPacket(output)) {
            packet.writeId(getId());
            packet.writeName(getName());
            packet.writeAvailability(getAvailability());
            packet.writeDescriptionProperty(description);

            writeModel(packet, output);

            writeLabel(packet, output);

            writePosition(packet, output);
        }
        if (displayCircle) {
            visibilityCircle.writeCzmlBlock(stream, output);
        }
    }

    @Override
    public CzmlGroundStation cloneObject() {
        final CzmlGroundStation copy;
        if (model != null) {
            copy =
                CzmlGroundStation.builder(this.topocentricFrame, this.clock)
                    .withModel(this.model).build();
        } else {
            copy =
                CzmlGroundStation.builder(this.topocentricFrame, this.clock)
                    .build();
        }
        copy.setId(getId());
        copy.setName(getName());
        return copy;

    }

    public void displayCircle(final Spacecraft satellite,
                              final double angleOfAperture) {
        visibilityCircle =
            StationVisibilityCircle
                .builder(topocentricFrame, satellite, this.clock)
                .withAngleOfAperture(angleOfAperture).build();
        displayCircle = true;

    }

    // Getters

    /**
     * Gets billboard.
     *
     * @return the billboard
     */
    public Billboard getBillboard() {
        return billboard;
    }

    /**
     * Gets the clock.
     *
     * @return the clock
     */
    public Clock getClock() {
        return clock;
    }

    /**
     * Gets topocentric frame.
     *
     * @return the topocentric frame
     */
    public TopocentricFrame getTopocentricFrame() {
        return topocentricFrame;
    }

    /**
     * Gets positions.
     *
     * @return the positions
     */
    public Vector3D getPositions() {
        return positionsOnEarth.get(0);
    }

    /**
     * Gets model.
     *
     * @return the model
     */
    public CzmlModel getModel() {
        return model;
    }

    // Setters

    /**
     * Sets the model of the ground station.
     *
     * @param model : The model to set
     */
    public void setModel(final CzmlModel model) {
        this.model = model;
    }

    // Private functions

    /**
     * This function aims at writing the billboard of the ground station (if one
     * is necessary).
     *
     * @param packet : The packet that will write in the czml file.
     * @param output : The output stream of cesium that will contain the strings
     *        to write into the CzmLFile.
     */
    private void writeBillBoard(final PacketCesiumWriter packet,
                                final CesiumOutputStream output) {
        if (model == null) {
            try (BillboardCesiumWriter billboardWriter =
                packet.getBillboardWriter()) {
                billboardWriter.open(output);
                billboardWriter.writeHorizontalOriginProperty(billboard
                    .getCesiumHorizontalOrigin());
                billboardWriter.writeColorProperty(billboard.getColor());
                try (UriCesiumWriter imageBillBoard =
                    billboardWriter.openImageProperty()) {
                    imageBillBoard
                        .writeUri(billboard.getImageStr(),
                                  billboard.getCesiumResourceBehavior());
                }
                billboardWriter.writeScaleProperty(billboard.getScale());
                billboardWriter.writeShowProperty(billboard.getShow());
            }
        } else {
            model.generateCZML(packet, output);
        }
    }

    /**
     * This function will write the specific model for the given station when
     * multiple stations are used.
     *
     * @param packet : The packet that will write in the czml file.
     * @param output : The output stream of cesium that will contain the strings
     *        to write into the CzmLFile.
     */
    private void writeModel(final PacketCesiumWriter packet,
                            final CesiumOutputStream output) {
        if (model == null) {
            writeBillBoard(packet, output);
        } else {
            this.model.generateCZML(packet, output);
        }
    }

    /**
     * This function aims at writing multiple models when several are loaded.
     * The number of models should be the same as the number of ground stations
     * wanted.
     *
     * @param packet : The packet that will write in the czml file.
     * @param output : The output stream of cesium that will contain the strings
     *        to write into the CzmLFile.
     */
    private void writeLabel(final PacketCesiumWriter packet,
                            final CesiumOutputStream output) {
        final Label label = new Label(topocentricFrame.getName());
        label.write(packet, output);
    }

    /**
     * This function aims at writing the position of the ground station.
     *
     * @param packet : The packet that will write in the czml file.
     * @param output : The output stream of cesium that will contain the strings
     *        to write into the CzmLFile.
     */
    private void writePosition(final PacketCesiumWriter packet,
                               final CesiumOutputStream output) {

        try (PositionCesiumWriter positionWriter = packet.getPositionWriter()) {
            positionWriter.open(output);
            positionWriter.writeInterval(this.getAvailability());
            final Cartesian cartesian =
                new Cartesian(positionOnEarth.getX(), positionOnEarth.getY(),
                              positionOnEarth.getZ());
            positionWriter.writeCartesian(cartesian);
        }
    }
}
