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
import org.orekit.czml.CzmlObjects.CzmlSecondaryObjects.Billboard;
import org.orekit.czml.CzmlObjects.CzmlSecondaryObjects.Label;
import org.orekit.czml.CzmlObjects.Position;
import org.orekit.czml.CzmlEnum.PositionType;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.estimation.measurements.GroundStation;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.frames.TopocentricFrame;
import org.orekit.utils.Constants;
import org.orekit.utils.IERSConventions;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/** CZML Ground Station
 * <p>
 * This class groups all the characteristics of a ground station. The ground station will be represented at the surface
 * of the central body with precise cartographic parameters. It can be build from the orekit ground station
 * {@link org.orekit.estimation.measurements.GroundStation}, or from the orekit topocentric frame {@link org.orekit.frames.TopocentricFrame}.
 * </p>
 * @since 1.0
 * @author Julien LEBLOND
 */


public class CzmlGroundStation extends AbstractPrimaryObject implements CzmlPrimaryObject {

    /** .*/
    public static final String DEFAULT_NAME = "Nameless ground station";
    /** .*/
    public static final String DEFAULT_IMAGE = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAADsMAAA7DAcdvqGQAAACvSURBVDhPrZDRDcMgDAU9GqN0lIzijw6SUbJJygUeNQgSqepJTyHG91LVVpwDdfxM3T9TSl1EXZvDwii471fivK73cBFFQNTT/d2KoGpfGOpSIkhUpgUMxq9DFEsWv4IXhlyCnhBFnZcFEEuYqbiUlNwWgMTdrZ3JbQFoEVG53rd8ztG9aPJMnBUQf/VFraBJeWnLS0RfjbKyLJA8FkT5seDYS1Qwyv8t0B/5C2ZmH2/eTGNNBgMmAAAAAElFTkSuQmCC";
    /** .*/
    private List<String> multipleName = new ArrayList<>();
    /** .*/
    private List<String> multipleId = new ArrayList<>();
    /** .*/
    private List<TimeInterval> multipleAvailability = new ArrayList<>();
    /** .*/
    private String description;
    /** .*/
    private List<String> multipleDescription = new ArrayList<>();
    /** .*/
    private Vector3D positionsList;
    /** .*/
    private List<Vector3D> multiplePositionsList = new ArrayList<>();
    /** .*/
    private Billboard billboard;
    /** .*/
    private List<Billboard> multipleBillboard = new ArrayList<>();
    /** .*/
    private GroundStation groundStation;
    /** .*/
    private List<GroundStation> multipleGroundStations = new ArrayList<>();
    /** .*/
    private TopocentricFrame topocentricFrame = null;
    /** .*/
    private List<TopocentricFrame> topocentricFrames = new ArrayList<>();

    // Intrinsic parameters
    /** .*/
    private Position positionObject;
    /** .*/
    private List<Position> multiplePositionObject = new ArrayList<>();

    public CzmlGroundStation(final String name, final String id, final TimeInterval availability, final String description, final Position positionObject, final Billboard billboard) {
        this.setId(id);
        this.setAvailability(availability);
        this.setName(name);
        this.description = description;
        this.billboard = billboard;
        final IERSConventions IERS = IERSConventions.IERS_2010;
        final Frame ITRF = FramesFactory.getITRF(IERS, true);
        final OneAxisEllipsoid earth = new OneAxisEllipsoid(Constants.WGS84_EARTH_EQUATORIAL_RADIUS, Constants.WGS84_EARTH_FLATTENING, ITRF);
        final double latitude = positionObject.getLatitude();
        final double longitude = positionObject.getLongitude();
        final GeodeticPoint geodeticPoint = new GeodeticPoint(latitude, longitude, 0);
        this.topocentricFrame = new TopocentricFrame(earth, geodeticPoint, "Nameless Topocentric Frame");
        this.groundStation = new org.orekit.estimation.measurements.GroundStation(topocentricFrame);
        this.positionObject = positionObject;
    }

    public CzmlGroundStation(final TopocentricFrame topocentricFrame) {
        this.topocentricFrame = topocentricFrame;
        this.positionsList = topocentricFrame.getCartesianPoint();
        this.setId(topocentricFrame.getName());
        this.setName(topocentricFrame.getName());
        this.setAvailability(Header.MASTER_CLOCK.getAvailability());
        this.groundStation = new org.orekit.estimation.measurements.GroundStation(topocentricFrame);
        this.billboard = new Billboard(DEFAULT_IMAGE);
        final PositionType positionType = PositionType.CARTOGRAPHIC_RADIANS;
        final double latitude = topocentricFrame.getPoint().getLatitude();
        final double longitude = topocentricFrame.getPoint().getLongitude();
        final double altitude = topocentricFrame.getPoint().getAltitude();
        this.positionObject = new Position(longitude, latitude, altitude, positionType);
    }

    public CzmlGroundStation(final List<TopocentricFrame> topocentricFrames) {
        this.topocentricFrames = topocentricFrames;
        this.setName("Packet containing " + topocentricFrames.size() + " ground stations");
        for (final TopocentricFrame currentTopocentricFrame : topocentricFrames) {
            final CzmlGroundStation currentGroundStation = new CzmlGroundStation(currentTopocentricFrame);
            multipleAvailability.add(currentGroundStation.getAvailability());
            multipleGroundStations.add(currentGroundStation.groundStation);
            multipleId.add(currentGroundStation.getId());
            multipleBillboard.add(currentGroundStation.billboard);
            multipleName.add(currentGroundStation.getName());
            multipleDescription.add(currentGroundStation.description);
            multiplePositionsList.add(currentGroundStation.positionsList);
            multiplePositionObject.add(currentGroundStation.positionObject);
        }
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
        }
        else {
            for (int i = 0; i < getMultipleId().size(); i++) {
                this.setId(getMultipleId().get(i));
                this.setName(getMultipleName().get(i));
                this.setAvailability(getMultipleAvailability().get(i));
                this.positionsList = getMultiplePositionsList().get(i);
                this.groundStation = getMultipleGroundStations().get(i);
                this.billboard = getMultipleBillboard().get(i);
                this.positionObject = getMultiplePositionObject().get(i);

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
        this.description = "";
        this.positionObject = null;
        this.positionsList = null;
        this.billboard = null;
        this.groundStation = null;
        topocentricFrame = null;
        topocentricFrames = new ArrayList<>();
    }

    private Cartesian vectorToCartesian(final Vector3D positions) {

        return new Cartesian(positions.getX(), positions.getY(), positions.getZ());
    }

    private void writeBillBoard(final PacketCesiumWriter packet) {
        try (BillboardCesiumWriter billboardWriter = packet.getBillboardWriter()) {
            billboardWriter.open(OUTPUT);
            billboardWriter.writeHorizontalOriginProperty(billboard.getCesiumHorizontalOrigin());
            billboardWriter.writeColorProperty(billboard.getRed(), billboard.getGreen(), billboard.getBlue(), billboard.getAlpha());
            try (UriCesiumWriter imageBillBoard = billboardWriter.openImageProperty()) {
                imageBillBoard.writeUri(billboard.getImageStr(), billboard.getCesiumResourceBehavior());
            }
            billboardWriter.writeScaleProperty(billboard.getScale());
            billboardWriter.writeShowProperty(billboard.getShow());
        }
    }

    private void writeLabel(final PacketCesiumWriter packet) {

        try (LabelCesiumWriter labelWriter = packet.getLabelWriter()) {
            labelWriter.open(OUTPUT);
            final Label label = new Label(groundStation);
            labelWriter.writeFillColorProperty(label.getColor());
            labelWriter.writeFontProperty("11pt Lucida Console");
            labelWriter.writeHorizontalOriginProperty(label.getHorizontalOrigin());
            labelWriter.writeVerticalOriginProperty(label.getVerticalOrigin());
            labelWriter.writeTextProperty(groundStation.getBaseFrame().getName());
            labelWriter.writeShowProperty(label.getShow());
        }
    }

    private void writePosition(final PacketCesiumWriter packet) {

        try (PositionCesiumWriter positionWriter = packet.getPositionWriter()) {
            positionWriter.open(OUTPUT);
            positionWriter.writeInterval(this.getAvailability());
            final Cartesian cartesian = new Cartesian(positionsList.getX(), positionsList.getY(), positionsList.getZ());
            positionWriter.writeCartesian(cartesian);
        }
    }

    // Getters

    public String getDescription() {
        return description;
    }

    public org.orekit.estimation.measurements.GroundStation getOrekitGroundStation() {
        return groundStation;
    }

    public Billboard getBillboard() {
        return billboard;
    }

    public TopocentricFrame getTopocentricFrame() {
        return topocentricFrame;
    }

    public List<TopocentricFrame> getMultipleTopocentricFrames() {
        if (topocentricFrames.isEmpty()) {
            throw new RuntimeException("The ground station was not build with several topocentric frames");
        }
        else {
            return topocentricFrames;
        }
    }

    public Vector3D getPositions() {
        return positionsList;
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

    public List<GroundStation> getMultipleGroundStations() {
        return multipleGroundStations;
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

    public List<String> getMultipleDescription() {
        return multipleDescription;
    }

    public List<Vector3D> getMultiplePositionsList() {
        return multiplePositionsList;
    }

}
