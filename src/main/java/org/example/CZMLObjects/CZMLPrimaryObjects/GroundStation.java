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

public class GroundStation implements CZMLPrimaryObject{

    private final String name;
    private final String id;
    private final TimeInterval availability;
    private String description;
    private Vector3D positionsList;
    private final Billboard billboard;
    private final org.orekit.estimation.measurements.GroundStation groundStation;

    // Intrinsic parameters
    private final Position positionObject;

    public GroundStation(String name, String id, TimeInterval availability, String description, Position positionObject, Billboard billboard){
        this.id = id;
        this.availability = availability;
        this.description = description;
        this.billboard = billboard;
        IERSConventions IERS = IERSConventions.IERS_2010;
        Frame ITRF = FramesFactory.getITRF(IERS,true);
        OneAxisEllipsoid earth = new OneAxisEllipsoid(Constants.WGS84_EARTH_EQUATORIAL_RADIUS,Constants.WGS84_EARTH_FLATTENING,ITRF);
        double latitude = positionObject.getLatitude();
        double longitude = positionObject.getLongitude();
        GeodeticPoint geodeticPoint = new GeodeticPoint(latitude,longitude,0);
        TopocentricFrame topocentricFrame = new TopocentricFrame(earth,geodeticPoint,"Nameless Topocentric Frame");
        this.groundStation = new org.orekit.estimation.measurements.GroundStation(topocentricFrame);
        this.name = groundStation.getBaseFrame().getName();
        this.positionObject = positionObject;
    }

    public GroundStation(org.orekit.estimation.measurements.GroundStation groundStation, Header header){
        this.positionsList = groundStation.getBaseFrame().getCartesianPoint();
        this.id = groundStation.getBaseFrame().getName();
        this.availability = header.getClock().getInterval();
        this.name = "Nameless Ground Station";
        this.groundStation = groundStation;
        String imageStr = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAADsMAAA7DAcdvqGQAAACvSURBVDhPrZDRDcMgDAU9GqN0lIzijw6SUbJJygUeNQgSqepJTyHG91LVVpwDdfxM3T9TSl1EXZvDwii471fivK73cBFFQNTT/d2KoGpfGOpSIkhUpgUMxq9DFEsWv4IXhlyCnhBFnZcFEEuYqbiUlNwWgMTdrZ3JbQFoEVG53rd8ztG9aPJMnBUQf/VFraBJeWnLS0RfjbKyLJA8FkT5seDYS1Qwyv8t0B/5C2ZmH2/eTGNNBgMmAAAAAElFTkSuQmCC";
        this.billboard = new Billboard(imageStr);
        PositionType positionType = PositionType.CARTOGRAPHIC_RADIANS;
        double latitude = groundStation.getBaseFrame().getPoint().getLatitude();
        double longitude = groundStation.getBaseFrame().getPoint().getLongitude();
        double altitude = groundStation.getBaseFrame().getPoint().getAltitude();
        this.positionObject = new Position(longitude,latitude,altitude,positionType);
    }

    public GroundStation(TopocentricFrame topocentricFrame, Header header){
        this.positionsList = topocentricFrame.getCartesianPoint();
        this.id = topocentricFrame.getName();
        this.availability = header.getClock().getInterval();
        this.name = "Nameless Ground Station";
        this.groundStation = new org.orekit.estimation.measurements.GroundStation(topocentricFrame);
        String imageStr = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAADsMAAA7DAcdvqGQAAACvSURBVDhPrZDRDcMgDAU9GqN0lIzijw6SUbJJygUeNQgSqepJTyHG91LVVpwDdfxM3T9TSl1EXZvDwii471fivK73cBFFQNTT/d2KoGpfGOpSIkhUpgUMxq9DFEsWv4IXhlyCnhBFnZcFEEuYqbiUlNwWgMTdrZ3JbQFoEVG53rd8ztG9aPJMnBUQf/VFraBJeWnLS0RfjbKyLJA8FkT5seDYS1Qwyv8t0B/5C2ZmH2/eTGNNBgMmAAAAAElFTkSuQmCC";
        this.billboard = new Billboard(imageStr);
        PositionType positionType = PositionType.CARTOGRAPHIC_RADIANS;
        double latitude = topocentricFrame.getPoint().getLatitude();
        double longitude = topocentricFrame.getPoint().getLongitude();
        double altitude = topocentricFrame.getPoint().getAltitude();
        this.positionObject = new Position(longitude,latitude,altitude,positionType);
    }

    @Override
    public void generateCZML() {
        output.setPrettyFormatting(true);
        try(PacketCesiumWriter packet = stream.openPacket(output)){
            packet.writeId(id);
            packet.writeName(name);
            packet.writeAvailability(availability);

            writeBillBoard(packet);

            writeLabel(packet);

            writePosition(packet);
        }
    }



    @Override
    public StringWriter getStringWriter() {
        return stringWriter;
    }

    @Override
    public void endFile() {
        output.writeEndSequence();
    }

    private Cartesian vectorToCartesian(Vector3D positions) {

        return new Cartesian(positions.getX(), positions.getY(), positions.getZ());
    }

    private void writeBillBoard(PacketCesiumWriter packet){
        try (BillboardCesiumWriter billboardWriter = packet.getBillboardWriter()) {
            billboardWriter.open(output);
            billboardWriter.writeHorizontalOriginProperty(billboard.getCesiumHorizontalOrigin());
            billboardWriter.writeColorProperty(billboard.getRed(), billboard.getGreen(), billboard.getBlue(), billboard.getAlpha());
            try (UriCesiumWriter imageBillBoard = billboardWriter.openImageProperty()) {
                imageBillBoard.writeUri(billboard.getImageStr(), billboard.getCesiumResourceBehavior());
            }
            billboardWriter.writeScaleProperty(billboard.getScale());
            billboardWriter.writeShowProperty(billboard.getShow());
        }
    }

    private void writeLabel(PacketCesiumWriter packet){
        try(LabelCesiumWriter labelWriter = packet.getLabelWriter()){
            labelWriter.open(output);
            Label label = new Label(groundStation);
            labelWriter.writeFillColorProperty(label.getColor());
            labelWriter.writeFontProperty("11pt Lucida Console");
            labelWriter.writeHorizontalOriginProperty(label.getHorizontalOrigin());
            labelWriter.writeVerticalOriginProperty(label.getVerticalOrigin());
            labelWriter.writeTextProperty(groundStation.getBaseFrame().getName());
            labelWriter.writeShowProperty(label.getShow());
        }
    }

    private void writePosition(PacketCesiumWriter packet){
        try(PositionCesiumWriter positionWriter = packet.getPositionWriter()){
            positionWriter.open(output);
            positionWriter.writeInterval(this.availability);
            Cartesian cartesian = new Cartesian(positionsList.getX(),positionsList.getY(),positionsList.getZ());
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
