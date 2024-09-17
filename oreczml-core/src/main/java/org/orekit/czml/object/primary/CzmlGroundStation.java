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

import cesiumlanguagewriter.BillboardCesiumWriter;
import cesiumlanguagewriter.Cartesian;
import cesiumlanguagewriter.LabelCesiumWriter;
import cesiumlanguagewriter.PacketCesiumWriter;
import cesiumlanguagewriter.PositionCesiumWriter;
import cesiumlanguagewriter.TimeInterval;
import cesiumlanguagewriter.UriCesiumWriter;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.orekit.czml.archi.builder.CzmlGroundStationBuilder;
import org.orekit.czml.errors.OreCzmlException;
import org.orekit.czml.errors.OreCzmlMessages;
import org.orekit.czml.object.PositionType;
import org.orekit.czml.object.nonvisual.CzmlModel;
import org.orekit.czml.object.secondary.Billboard;
import org.orekit.czml.object.secondary.Label;
import org.orekit.czml.object.Position;
import org.orekit.frames.TopocentricFrame;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * CZML Ground Station
 * <p>
 * This class groups all the characteristics of a ground station. The ground station will be represented at the surface
 * of the central body with precise cartographic parameters. It can be build from the orekit ground station
 * {@link org.orekit.estimation.measurements.GroundStation}, or from the orekit topocentric frame {@link org.orekit.frames.TopocentricFrame}.
 * </p>
 *
 * @author Julien LEBLOND
 * @since 1.0.0
 */


public class CzmlGroundStation extends AbstractPrimaryObject implements CzmlPrimaryObject {

    /** The default image used when no image/model is used for the station. */
    public static final String DEFAULT_IMAGE = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAADsMAAA7DAcdvqGQAAACvSURBVDhPrZDRDcMgDAU9GqN0lIzijw6SUbJJygUeNQgSqepJTyHG91LVVpwDdfxM3T9TSl1EXZvDwii471fivK73cBFFQNTT/d2KoGpfGOpSIkhUpgUMxq9DFEsWv4IXhlyCnhBFnZcFEEuYqbiUlNwWgMTdrZ3JbQFoEVG53rd8ztG9aPJMnBUQf/VFraBJeWnLS0RfjbKyLJA8FkT5seDYS1Qwyv8t0B/5C2ZmH2/eTGNNBgMmAAAAAElFTkSuQmCC";

    /** The default police for the labels. */
    public static final String DEFAULT_POLICE = "11pt Lucida Console";

    /** The default id for ground stations. */
    public static final String DEFAULT_ID = "GROUND_STATION/";

    /** The default name for ground stations. */
    public static final String DEFAULT_NAME = "Ground station : ";

    /** The default 3d model : empty. */
    public static final String DEFAULT_3D_MODEL = "";

    /** The list containing all the names of all the stations. */
    private List<String> multipleName = new ArrayList<>();

    /** The list of all the ids of all the stations. */
    private List<String> multipleId = new ArrayList<>();

    /** The list of all the availabilities of the ground stations. */
    private List<TimeInterval> multipleAvailability = new ArrayList<>();

    /** The position on earth of a single station, it is the cartesian vector from the geodetic point. */
    private Vector3D positionOnEarth;

    /** The list of all the position on earth of all the stations. */
    private List<Vector3D> multiplePositionOnEarth = new ArrayList<>();

    /** The billboard that will display the image of the station. */
    private Billboard billboard;

    /** The billboards of the stations. */
    private List<Billboard> multipleBillboard = new ArrayList<>();

    /** The topocentric frame used to build the ground station when a single station is computed. */
    private TopocentricFrame topocentricFrame = null;

    /** The list of the topocentric frames used to build the ground stations when multiple stations are computed. */
    private List<TopocentricFrame> topocentricFrames = new ArrayList<>();

    /** The model used to define the station if only one model is used. */
    private CzmlModel model;

    /** The list of the models used to build ground stations models when multiple stations are computed. */
    private List<CzmlModel> multipleModel = new ArrayList<>();

    // Intrinsic parameters
    /** The position object of the station to be written in the CzmlFile. */
    private Position positionObject;

    /** The list of the position object of the stations when several stations are computed. */
    private List<Position> multiplePositionObject = new ArrayList<>();

    /** The list of all the paths of the models if several are used. */
    private List<String> modelPaths = new ArrayList<>();

    //// Constructors
    // Single Station Constructors

    /**
     * The constructor of the czml ground station object with a default model.
     *
     * @param topocentricFrame : The topocentric frame where the ground station must be located.
     */
    public CzmlGroundStation(final TopocentricFrame topocentricFrame) throws URISyntaxException, IOException {
        this(topocentricFrame, DEFAULT_3D_MODEL);
    }

    /**
     * The constructor of the czml ground station object with no default parameters.
     *
     * @param topocentricFrame : The topocentric frame where the ground station must be located.
     * @param modelPath        : The path of the model to load.
     */
    public CzmlGroundStation(final TopocentricFrame topocentricFrame,
                             final String modelPath) throws URISyntaxException, IOException {
        this.topocentricFrame = topocentricFrame;
        this.positionOnEarth  = topocentricFrame.getCartesianPoint();
        this.setId(DEFAULT_ID + topocentricFrame.getName());
        this.setName(DEFAULT_NAME + topocentricFrame.getName());
        this.setAvailability(Header.getMasterClock()
                                   .getAvailability());
        this.billboard = new Billboard(DEFAULT_IMAGE);
        final PositionType positionType = PositionType.CARTOGRAPHIC_RADIANS;
        final double latitude = topocentricFrame.getPoint()
                                                .getLatitude();
        final double longitude = topocentricFrame.getPoint()
                                                 .getLongitude();
        final double altitude = topocentricFrame.getPoint()
                                                .getAltitude();
        this.positionObject = new Position(longitude, latitude, altitude, positionType);
        // If the modelPath is empty
        if (modelPath.isEmpty()) {
            this.model = null;
        } else {
            this.model = new CzmlModel(modelPath, 10, 300, 2);
        }
    }

    // Multiple Stations Constructors

    /**
     * The constructor of several ground stations with a default model.
     *
     * @param topocentricFrames : The list of topocentric frame where the station must be located.
     */
    public CzmlGroundStation(final List<TopocentricFrame> topocentricFrames) throws URISyntaxException, IOException {
        this(topocentricFrames, DEFAULT_3D_MODEL);
    }

    /**
     * The constructor of several ground stations with no default parameters.
     *
     * @param topocentricFrames : The list of topocentric frame where the station must be located.
     * @param modelPath         : The path of the model to load (same for all stations, a feature when each station will be able
     *                          to have its own model it under development)
     */
    public CzmlGroundStation(final List<TopocentricFrame> topocentricFrames,
                             final String modelPath) throws URISyntaxException, IOException {
        this(topocentricFrames, Collections.singletonList(modelPath));
    }


    public CzmlGroundStation(final List<TopocentricFrame> topocentricFrames,
                             final List<String> modelPathsInput) throws URISyntaxException, IOException {
        this.topocentricFrames = topocentricFrames;
        this.setName("Packet containing " + topocentricFrames.size() + " ground stations");
        this.setId("GROUND_STATIONS_PACKED/" + topocentricFrames.size());
        this.modelPaths = modelPathsInput;
        for (final TopocentricFrame currentTopocentricFrame : topocentricFrames) {
            final CzmlGroundStation currentGroundStation = new CzmlGroundStation(currentTopocentricFrame);
            this.multipleAvailability.add(currentGroundStation.getAvailability());
            this.multipleId.add(currentGroundStation.getId());
            this.multipleBillboard.add(currentGroundStation.billboard);
            this.multipleName.add(currentGroundStation.getName());
            this.multiplePositionOnEarth.add(currentGroundStation.positionOnEarth);
            this.multiplePositionObject.add(currentGroundStation.positionObject);
        }

        if (modelPathsInput.isEmpty()) {
            this.multipleModel = null;
        } else {
            if (modelPathsInput.size() == 1 && modelPathsInput.get(0)
                                                              .isEmpty()) {
                this.model = null;
            } else if (modelPathsInput.size() == 1) {
                this.model = new CzmlModel(modelPathsInput.get(0), 50, 300, 2);
            } else {
                for (final String currentPathModel : modelPathsInput) {
                    this.multipleModel.add(new CzmlModel(currentPathModel, 50, 300, 2));
                }
            }
        }
    }

    // Builders

    public static CzmlGroundStationBuilder builder(final TopocentricFrame topocentricFrameInput) {
        return new CzmlGroundStationBuilder(topocentricFrameInput);
    }

    public static CzmlGroundStationBuilder builder(final List<TopocentricFrame> topocentricFramesInput) {
        return new CzmlGroundStationBuilder(topocentricFramesInput);
    }

    // Overrides

    @Override
    public void writeCzmlBlock() throws IOException, URISyntaxException {
        if (getMultipleId().isEmpty()) {
            OUTPUT.setPrettyFormatting(true);
            try (PacketCesiumWriter packet = STREAM.openPacket(OUTPUT)) {
                packet.writeId(getId());
                packet.writeName(getName());
                packet.writeAvailability(getAvailability());

                writeBillBoard(packet);

                writeLabel(packet);

                writePosition(packet);
            } catch (IOException | URISyntaxException e) {
                throw new RuntimeException(e);
            }
            cleanObject();
        } else {
            for (int i = 0; i < getMultipleId().size(); i++) {
                this.setId(getMultipleId().get(i));
                this.setName(getMultipleName().get(i));
                this.setAvailability(getMultipleAvailability().get(i));
                this.positionOnEarth = getMultiplePositionOnEarth().get(i);
                this.billboard       = getMultipleBillboard().get(i);
                this.positionObject  = getMultiplePositionObject().get(i);

                OUTPUT.setPrettyFormatting(true);
                try (PacketCesiumWriter packet = STREAM.openPacket(OUTPUT)) {
                    packet.writeId(getId());
                    packet.writeName(getName());
                    packet.writeAvailability(getAvailability());

                    writeMultipleModels(packet, i);

                    writeMultipleLabel(packet, i);

                    writePosition(packet);
                }
            }
        }
    }

    @Override
    public StringWriter getStringWriter() {
        return STRING_WRITER;
    }

    @Override
    public void cleanObject() {
        this.setId("");
        this.setName("");
        this.setAvailability(null);
        this.positionObject     = null;
        this.positionOnEarth    = null;
        this.billboard          = null;
        topocentricFrame        = null;
        topocentricFrames       = new ArrayList<>();
        multipleName            = new ArrayList<>();
        multipleId              = new ArrayList<>();
        multipleAvailability    = new ArrayList<>();
        multiplePositionOnEarth = new ArrayList<>();
        multipleBillboard       = new ArrayList<>();
        multiplePositionObject  = new ArrayList<>();
        model                   = null;
    }


    // Getters

    public Billboard getBillboard() {
        return billboard;
    }

    public TopocentricFrame getTopocentricFrame() {
        return topocentricFrame;
    }

    public List<TopocentricFrame> getMultipleTopocentricFrames() {
        if (topocentricFrames.isEmpty()) {
            throw new OreCzmlException(OreCzmlMessages.SINGLE_STATION_GET_MULTIPLE_GROUND_STATION);
        } else {
            return topocentricFrames;
        }
    }

    public Vector3D getPositions() {
        return positionOnEarth;
    }

    public Position getPositionObject() {
        return positionObject;
    }

    public List<Billboard> getMultipleBillboard() {
        return new ArrayList<>(multipleBillboard);
    }

    public List<Position> getMultiplePositionObject() {
        return new ArrayList<>(multiplePositionObject);
    }

    public List<TimeInterval> getMultipleAvailability() {
        return new ArrayList<>(multipleAvailability);
    }

    public List<String> getMultipleId() {
        return new ArrayList<>(multipleId);
    }

    public List<String> getMultipleName() {
        return new ArrayList<>(multipleName);
    }

    public List<Vector3D> getMultiplePositionOnEarth() {
        return new ArrayList<>(multiplePositionOnEarth);
    }

    public Vector3D getPositionOnEarth() {
        return positionOnEarth;
    }

    public List<TopocentricFrame> getTopocentricFrames() {
        return new ArrayList<>(topocentricFrames);
    }

    public CzmlModel getModel() {
        return model;
    }


    // Private functions

    /**
     * This function aims at writing the billboard of the ground station (if one is necessary).
     *
     * @param packet : The packet that will write in the czml file.
     */
    private void writeBillBoard(final PacketCesiumWriter packet) throws IOException, URISyntaxException {
        if (model == null) {
            try (BillboardCesiumWriter billboardWriter = packet.getBillboardWriter()) {
                billboardWriter.open(OUTPUT);
                billboardWriter.writeHorizontalOriginProperty(billboard.getCesiumHorizontalOrigin());
                billboardWriter.writeColorProperty(billboard.getColor());
                try (UriCesiumWriter imageBillBoard = billboardWriter.openImageProperty()) {
                    imageBillBoard.writeUri(billboard.getImageStr(), billboard.getCesiumResourceBehavior());
                }
                billboardWriter.writeScaleProperty(billboard.getScale());
                billboardWriter.writeShowProperty(billboard.getShow());
            }
        } else {
            model.generateCZML(packet, OUTPUT);
        }
    }

    /**
     * This function will write the specific model for the given station when multiple stations are used.
     *
     * @param packet          : The packet that will write in the czml file.
     * @param iterationNumber : The iteration number of the current station.
     */
    private void writeMultipleModels(final PacketCesiumWriter packet,
                                     final int iterationNumber) throws IOException, URISyntaxException {
        if (multipleModel.isEmpty()) {
            writeBillBoard(packet);
        } else if (modelPaths.size() == 1) {
            if (modelPaths.get(0)
                          .isEmpty()) {
                writeBillBoard(packet);
            } else {
                this.model.generateCZML(packet, OUTPUT);
            }
        } else {
            final CzmlModel currentModel = multipleModel.get(iterationNumber);
            currentModel.generateCZML(packet, OUTPUT);
        }
    }

    /**
     * This function aims at writing the label of the ground station.
     *
     * @param packet : The packet that will write in the czml file.
     */
    private void writeLabel(final PacketCesiumWriter packet) {

        try (LabelCesiumWriter labelWriter = packet.getLabelWriter()) {
            labelWriter.open(OUTPUT);
            final Label label = new Label(topocentricFrame);
            labelWriter.writeFillColorProperty(label.getColor());
            labelWriter.writeFontProperty(DEFAULT_POLICE);
            labelWriter.writeHorizontalOriginProperty(label.getHorizontalOrigin());
            labelWriter.writeVerticalOriginProperty(label.getVerticalOrigin());
            labelWriter.writeTextProperty(topocentricFrame.getName());
            labelWriter.writeShowProperty(label.getShow());
        }
    }

    private void writeMultipleLabel(final PacketCesiumWriter packet, final int iterationNumber) {
        try (LabelCesiumWriter labelWriter = packet.getLabelWriter()) {
            labelWriter.open(OUTPUT);
            final Label label = new Label(topocentricFrames.get(iterationNumber));
            labelWriter.writeFillColorProperty(label.getColor());
            labelWriter.writeFontProperty(DEFAULT_POLICE);
            labelWriter.writeHorizontalOriginProperty(label.getHorizontalOrigin());
            labelWriter.writeVerticalOriginProperty(label.getVerticalOrigin());
            labelWriter.writeTextProperty(topocentricFrames.get(iterationNumber)
                                                           .getName());
            labelWriter.writeShowProperty(label.getShow());
        }
    }


    /**
     * This function aims at writing the position of the ground station.
     *
     * @param packet : The packet that will write in the czml file.
     */
    private void writePosition(final PacketCesiumWriter packet) {

        try (PositionCesiumWriter positionWriter = packet.getPositionWriter()) {
            positionWriter.open(OUTPUT);
            positionWriter.writeInterval(this.getAvailability());
            final Cartesian cartesian = new Cartesian(positionOnEarth.getX(), positionOnEarth.getY(),
                    positionOnEarth.getZ());
            positionWriter.writeCartesian(cartesian);
        }
    }

}
