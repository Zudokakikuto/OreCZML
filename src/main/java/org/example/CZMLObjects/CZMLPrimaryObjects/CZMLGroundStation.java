package org.example.CZMLObjects.CZMLPrimaryObjects;

import cesiumlanguagewriter.*;
import org.example.CZMLObjects.CZMLSecondaryObects.Billboard;
import org.example.CZMLObjects.CZMLSecondaryObects.Label;
import org.example.CZMLObjects.CZMLSecondaryObects.PositionType;
import org.example.CZMLObjects.Position;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.frames.TopocentricFrame;
import org.orekit.utils.Constants;
import org.orekit.utils.IERSConventions;

import java.io.StringWriter;

public class GroundStation implements CZMLPrimaryObject {

    /** .*/
    public static final String DEFAULT_NAME = "Nameless ground station";
    /** .*/
    public static final String DEFAULT_IMAGE = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAADsMAAA7DAcdvqGQAAACvSURBVDhPrZDRDcMgDAU9GqN0lIzijw6SUbJJygUeNQgSqepJTyHG91LVVpwDdfxM3T9TSl1EXZvDwii471fivK73cBFFQNTT/d2KoGpfGOpSIkhUpgUMxq9DFEsWv4IXhlyCnhBFnZcFEEuYqbiUlNwWgMTdrZ3JbQFoEVG53rd8ztG9aPJMnBUQf/VFraBJeWnLS0RfjbKyLJA8FkT5seDYS1Qwyv8t0B/5C2ZmH2/eTGNNBgMmAAAAAElFTkSuQmCC";

    /** .*/
    private String name;
    /** .*/
    private String id;
    /** .*/
    private TimeInterval availability;
    /** .*/
    private String description;
    /** .*/
    private Vector3D positionsList;
    /** .*/
    private Billboard billboard;
    /** .*/
    private org.orekit.estimation.measurements.GroundStation groundStation;

    // Intrinsic parameters
    /** .*/
    private Position positionObject;

    public GroundStation(final String name, final String id, final TimeInterval availability, final String description, final Position positionObject, final Billboard billboard) {
        this.id = id;
        this.availability = availability;
        this.description = description;
        this.billboard = billboard;
        final IERSConventions IERS = IERSConventions.IERS_2010;
        final Frame ITRF = FramesFactory.getITRF(IERS, true);
        final OneAxisEllipsoid earth = new OneAxisEllipsoid(Constants.WGS84_EARTH_EQUATORIAL_RADIUS, Constants.WGS84_EARTH_FLATTENING, ITRF);
        final double latitude = positionObject.getLatitude();
        final double longitude = positionObject.getLongitude();
        final GeodeticPoint geodeticPoint = new GeodeticPoint(latitude, longitude, 0);
        final TopocentricFrame topocentricFrame = new TopocentricFrame(earth, geodeticPoint, "Nameless Topocentric Frame");
        this.groundStation = new org.orekit.estimation.measurements.GroundStation(topocentricFrame);
        this.name = groundStation.getBaseFrame().getName();
        this.positionObject = positionObject;
    }

    public GroundStation(final org.orekit.estimation.measurements.GroundStation groundStation, final Header header) {
        this.positionsList = groundStation.getBaseFrame().getCartesianPoint();
        this.id = groundStation.getBaseFrame().getName();
        this.availability = header.getClock().getInterval();
        this.name = groundStation.getBaseFrame().getName();
        this.groundStation = groundStation;
        this.billboard = new Billboard(DEFAULT_IMAGE);
        final PositionType positionType = PositionType.CARTOGRAPHIC_RADIANS;
        final double latitude = groundStation.getBaseFrame().getPoint().getLatitude();
        final double longitude = groundStation.getBaseFrame().getPoint().getLongitude();
        final double altitude = groundStation.getBaseFrame().getPoint().getAltitude();
        this.positionObject = new Position(longitude, latitude, altitude, positionType);
    }

    public GroundStation(final TopocentricFrame topocentricFrame, final Header header) {
        this.positionsList = topocentricFrame.getCartesianPoint();
        this.id = topocentricFrame.getName();
        this.availability = header.getClock().getInterval();
        this.name = topocentricFrame.getName();
        this.groundStation = new org.orekit.estimation.measurements.GroundStation(topocentricFrame);
        this.billboard = new Billboard(DEFAULT_IMAGE);
        final PositionType positionType = PositionType.CARTOGRAPHIC_RADIANS;
        final double latitude = topocentricFrame.getPoint().getLatitude();
        final double longitude = topocentricFrame.getPoint().getLongitude();
        final double altitude = topocentricFrame.getPoint().getAltitude();
        this.positionObject = new Position(longitude, latitude, altitude, positionType);
    }

    @Override
    public void generateCZML() {
        OUTPUT.setPrettyFormatting(true);
        try (PacketCesiumWriter packet = STREAM.openPacket(OUTPUT)) {
            packet.writeId(id);
            packet.writeName(name);
            packet.writeAvailability(availability);

            writeBillBoard(packet);

            writeLabel(packet);

            writePosition(packet);
        }
        cleanObject();
    }



    @Override
    public StringWriter getStringWriter() {
        return STRING_WRITER;
    }

    @Override
    public void endFile() {
        OUTPUT.writeEndSequence();
    }

    @Override
    public void cleanObject() {
        this.name = "";
        this.id = "";
        this.availability = null;
        this.description = "";
        this.positionObject = null;
        this.positionsList = null;
        this.billboard = null;
        this.groundStation = null;
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
            positionWriter.writeInterval(this.availability);
            final Cartesian cartesian = new Cartesian(positionsList.getX(), positionsList.getY(), positionsList.getZ());
            positionWriter.writeCartesian(cartesian);
        }
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public TimeInterval getAvailability() {
        return availability;
    }

    public org.orekit.estimation.measurements.GroundStation getOrekitGroundStation() {
        return groundStation;
    }

    public Billboard getBillboard() {
        return billboard;
    }

    public Vector3D getPositions() {
        return positionsList;
    }

    public Position getPositionObject() {
        return positionObject;
    }
}
