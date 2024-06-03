package org.example.CZMLObjects.CZMLSecondaryObects;

import cesiumlanguagewriter.*;
import org.example.CZMLObjects.CZMLPrimaryObjects.GroundStation;
import org.example.CZMLObjects.CZMLPrimaryObjects.Header;
import org.example.CZMLObjects.Position;
import org.example.CZMLObjects.CZMLPrimaryObjects.Satellite;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.hipparchus.util.FastMath;
import org.orekit.frames.TopocentricFrame;
import org.orekit.utils.Constants;

import java.awt.*;
import java.util.List;

public class Cylinder implements CZMLSecondaryObject{

    private double lenght;
    private double topRadius;
    private double bottomRadius;
    private Color color;
    private Position position;
    private boolean show;
    private TimeInterval availability;
    private CesiumHeightReference heightReference;

    public Cylinder(double lenght, double topRadius, double bottomRadius, Color color, Position position, TimeInterval availability, CesiumHeightReference heightReference){
        this.lenght = lenght;
        this.topRadius = topRadius;
        this.bottomRadius = bottomRadius;
        this.color = color;
        this.position = position;
        this.show = true;
        this.availability = availability;
        this.heightReference = heightReference;
    }

    public Cylinder(GroundStation InputGroundStation, Satellite satellite,double angleOfAperture){
        Color color = new Color(255,255,255,50);

        Vector3D positionInCartesian = InputGroundStation.getPositions();

        double x = positionInCartesian.getX();
        double y = positionInCartesian.getY();
        double z = positionInCartesian.getZ();
        PositionType positionType = PositionType.CARTESIAN_POSITION;

        this.lenght = satellite.getOrbits().get(0).getA()/2;
        this.topRadius = lenght*FastMath.tan(angleOfAperture);
        this.bottomRadius = 10.0;
        this.position = new Position(x,y,z,positionType);
        this.color = color;
        this.show = true;
        this.availability = satellite.getAvailability();
        this.heightReference = CesiumHeightReference.CLAMP_TO_GROUND;
    }

    public Cylinder(GroundStation InputGroundStation,double angleOfAperture){

        Color color = new Color(255,255,255,50);

        double x = InputGroundStation.getPositions().getX();
        double y = InputGroundStation.getPositions().getY();
        double z = InputGroundStation.getPositions().getZ();
        PositionType positionType = PositionType.CARTESIAN_POSITION;

        this.lenght = Constants.WGS84_EARTH_EQUATORIAL_RADIUS;
        this.topRadius = lenght*FastMath.tan(angleOfAperture);
        this.bottomRadius = 0.0;
        this.position = new Position(x,y,z,positionType);
        this.color = color;
        this.availability = InputGroundStation.getAvailability();
        this.heightReference = CesiumHeightReference.CLAMP_TO_GROUND;
    }

    public Cylinder(TopocentricFrame topocentricFrame, Header header, double angleOfAperture){
        Color color = new Color(255,255,255,50);

        double x = topocentricFrame.getCartesianPoint().getX();
        double y = topocentricFrame.getCartesianPoint().getY();
        double z = topocentricFrame.getCartesianPoint().getZ();

        PositionType positionType = PositionType.CARTESIAN_POSITION;

        this.lenght = Constants.WGS84_EARTH_EQUATORIAL_RADIUS;
        this.topRadius = lenght*FastMath.tan(angleOfAperture);
        this.bottomRadius = 0.0;
        this.position = new Position(x,y,z,positionType);
        this.availability = header.getClock().getInterval();
        this.color = color;
        this.heightReference = CesiumHeightReference.CLAMP_TO_GROUND;
    }


    @Override
    public void write(PacketCesiumWriter packetWriter, CesiumOutputStream output) {
        try(CylinderCesiumWriter cylinderWriter = packetWriter.getCylinderWriter()){
            cylinderWriter.open(output);
            cylinderWriter.writeBottomRadiusProperty(this.bottomRadius);
            cylinderWriter.writeTopRadiusProperty(this.topRadius);
            cylinderWriter.writeLengthProperty(this.lenght);
            MaterialCesiumWriter materialWriter = cylinderWriter.getMaterialWriter();
            materialWriter.open(output);
            output.writeStartObject();
            SolidColorMaterialCesiumWriter solidColorWriter = materialWriter.getSolidColorWriter();
            solidColorWriter.open(output);
            solidColorWriter.writeColorProperty(color);
            output.writeEndObject();
            solidColorWriter.close();
            materialWriter.close();
            cylinderWriter.writeHeightReferenceProperty(heightReference);
        }
    }

    public Color getColor() {
        return color;
    }

    public double getBottomRadius() {
        return bottomRadius;
    }

    public boolean getShow() {
        return show;
    }

    public Position getPosition() {
        return position;
    }

    public double getLenght() {
        return lenght;
    }

    public double getTopRadius() {
        return topRadius;
    }

    public void setColor(Color color){
        this.color = color;
    }
}
