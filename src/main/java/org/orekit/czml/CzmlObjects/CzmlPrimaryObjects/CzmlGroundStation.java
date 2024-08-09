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

import cesiumlanguagewriter.BillboardCesiumWriter;
import cesiumlanguagewriter.Cartesian;
import cesiumlanguagewriter.LabelCesiumWriter;
import cesiumlanguagewriter.PacketCesiumWriter;
import cesiumlanguagewriter.PositionCesiumWriter;
import cesiumlanguagewriter.TimeInterval;
import cesiumlanguagewriter.UriCesiumWriter;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.orekit.czml.ArchiObjects.CzmlGroundStationBuilder;
import org.orekit.czml.CzmlEnum.PositionType;
import org.orekit.czml.CzmlObjects.CzmlAbstractObjects.CzmlModel;
import org.orekit.czml.CzmlObjects.CzmlSecondaryObjects.Billboard;
import org.orekit.czml.CzmlObjects.CzmlSecondaryObjects.Label;
import org.orekit.czml.CzmlObjects.Position;
import org.orekit.frames.TopocentricFrame;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
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

    /** The list of the topocentric frames used to build the ground stations when multiple station are computed. */
    private List<TopocentricFrame> topocentricFrames = new ArrayList<>();

    /** The model used to define the station. This model can't be changed when several stations are used (to be developed). */
    private CzmlModel model;


    // Intrinsic parameters
    /** The position object of the station to be written in the CzmlFile. */
    private Position positionObject;

    /** The list of the position object of the stations when several stations are computed. */
    private List<Position> multiplePositionObject = new ArrayList<>();


    //// Constructors
    // Single Station Constructors
    public CzmlGroundStation(final TopocentricFrame topocentricFrame) throws URISyntaxException, IOException {
        this(topocentricFrame, DEFAULT_3D_MODEL);
    }

    public CzmlGroundStation(final TopocentricFrame topocentricFrame,
                             final String modelPath) throws URISyntaxException, IOException {
        this.topocentricFrame = topocentricFrame;
        this.positionOnEarth  = topocentricFrame.getCartesianPoint();
        this.setId(topocentricFrame.getName());
        this.setName(topocentricFrame.getName());
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
    public CzmlGroundStation(final List<TopocentricFrame> topocentricFrames) throws URISyntaxException, IOException {
        this(topocentricFrames, DEFAULT_3D_MODEL);
    }

    public CzmlGroundStation(final List<TopocentricFrame> topocentricFrames,
                             final String modelPath) throws URISyntaxException, IOException {
        this.topocentricFrames = topocentricFrames;
        this.setName("Packet containing " + topocentricFrames.size() + " ground stations");
        this.setId("GROUND_STATIONS_PACKED/" + topocentricFrames.size());
        for (final TopocentricFrame currentTopocentricFrame : topocentricFrames) {
            final CzmlGroundStation currentGroundStation = new CzmlGroundStation(currentTopocentricFrame);
            multipleAvailability.add(currentGroundStation.getAvailability());
            multipleId.add(currentGroundStation.getId());
            multipleBillboard.add(currentGroundStation.billboard);
            multipleName.add(currentGroundStation.getName());
            multiplePositionOnEarth.add(currentGroundStation.positionOnEarth);
            multiplePositionObject.add(currentGroundStation.positionObject);
        }
        if (modelPath.isEmpty()) {
            this.model = null;
        } else {
            this.model = new CzmlModel(modelPath, 50, 300, 2);
        }
    }

    // Builder
    public CzmlGroundStationBuilder builder(final TopocentricFrame topocentricFrameInput) {
        return new CzmlGroundStationBuilder(topocentricFrameInput);
    }

    @Override
    public void writeCzmlBlock() {
        if (getMultipleId().isEmpty()) {
            OUTPUT.setPrettyFormatting(true);
            try (PacketCesiumWriter packet = STREAM.openPacket(OUTPUT)) {
                packet.writeId(getId());
                packet.writeName(getName());
                packet.writeAvailability(getAvailability());

                writeBillBoard(packet);

                writeLabel(packet);

                writePosition(packet);
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

                    writeBillBoard(packet);

                    writeLabel(packet);

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

    private void writeBillBoard(final PacketCesiumWriter packet) {
        if (model == null) {
            try (BillboardCesiumWriter billboardWriter = packet.getBillboardWriter()) {
                billboardWriter.open(OUTPUT);
                billboardWriter.writeHorizontalOriginProperty(billboard.getCesiumHorizontalOrigin());
                billboardWriter.writeColorProperty(billboard.getRed(), billboard.getGreen(), billboard.getBlue(),
                        billboard.getAlpha());
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

    private void writeLabel(final PacketCesiumWriter packet) {

        try (LabelCesiumWriter labelWriter = packet.getLabelWriter()) {
            labelWriter.open(OUTPUT);
            final Label label = new Label(topocentricFrame);
            labelWriter.writeFillColorProperty(label.getColor());
            labelWriter.writeFontProperty("11pt Lucida Console");
            labelWriter.writeHorizontalOriginProperty(label.getHorizontalOrigin());
            labelWriter.writeVerticalOriginProperty(label.getVerticalOrigin());
            labelWriter.writeTextProperty(topocentricFrame.getName());
            labelWriter.writeShowProperty(label.getShow());
        }
    }

    private void writePosition(final PacketCesiumWriter packet) {

        try (PositionCesiumWriter positionWriter = packet.getPositionWriter()) {
            positionWriter.open(OUTPUT);
            positionWriter.writeInterval(this.getAvailability());
            final Cartesian cartesian = new Cartesian(positionOnEarth.getX(), positionOnEarth.getY(),
                    positionOnEarth.getZ());
            positionWriter.writeCartesian(cartesian);
        }
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
            throw new RuntimeException("The ground station was not build with several topocentric frames");
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
        return multipleBillboard;
    }

    public List<Position> getMultiplePositionObject() {
        return multiplePositionObject;
    }

    public List<TimeInterval> getMultipleAvailability() {
        return multipleAvailability;
    }

    public List<String> getMultipleId() {
        return multipleId;
    }

    public List<String> getMultipleName() {
        return multipleName;
    }

    public List<Vector3D> getMultiplePositionOnEarth() {
        return multiplePositionOnEarth;
    }

    public Vector3D getPositionOnEarth() {
        return positionOnEarth;
    }

    public List<TopocentricFrame> getTopocentricFrames() {
        return topocentricFrames;
    }

    public CzmlModel getModel() {
        return model;
    }
}
