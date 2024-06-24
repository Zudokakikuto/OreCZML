
package org.example.CZMLObjects.CZMLSecondaryObects;

import cesiumlanguagewriter.*;
import org.example.CZMLObjects.CZMLPrimaryObjects.CZMLGroundStation;
import org.example.CZMLObjects.CZMLPrimaryObjects.Header;
import org.example.CZMLObjects.Position;
import org.example.CZMLObjects.CZMLPrimaryObjects.Satellite;
import org.example.CZMLObjects.PositionType;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.hipparchus.util.FastMath;
import org.orekit.frames.TopocentricFrame;
import org.orekit.utils.Constants;

import java.awt.*;

public class Cylinder implements CZMLSecondaryObject {
    /** .*/
    private final double lenght;
    /** .*/
    private final double topRadius;
    /** .*/
    private final double bottomRadius;
    /** .*/
    private Color color;
    /** .*/
    private final Position position;
    /** .*/
    private boolean show = false;
    /** .*/
    private final TimeInterval availability;
    /** .*/
    private final CesiumHeightReference heightReference;

    public Cylinder(final double lenght, final double topRadius, final double bottomRadius, final Color color, final Position position, final TimeInterval availability, final CesiumHeightReference heightReference) {
        this.lenght = lenght;
        this.topRadius = topRadius;
        this.bottomRadius = bottomRadius;
        this.color = color;
        this.position = position;
        this.show = true;
        this.availability = availability;
        this.heightReference = heightReference;
    }

    public Cylinder(final CZMLGroundStation InputGroundStation, final Satellite satellite, final double angleOfAperture) {
        final Color color_temp = new Color(255, 255, 255, 50);

        final Vector3D positionInCartesian = InputGroundStation.getPositions();

        final double x = positionInCartesian.getX();
        final double y = positionInCartesian.getY();
        final double z = positionInCartesian.getZ();
        final PositionType positionType = PositionType.CARTESIAN_POSITION;

        this.lenght = satellite.getOrbits().get(0).getA() / 2;
        this.topRadius = lenght * FastMath.tan(angleOfAperture);
        this.bottomRadius = 10.0;
        this.position = new Position(x, y, z, positionType);
        this.color = color_temp;
        this.show = true;
        this.availability = satellite.getAvailability();
        this.heightReference = CesiumHeightReference.CLAMP_TO_GROUND;
    }

    public Cylinder(final CZMLGroundStation InputGroundStation, final double angleOfAperture) {

        final Color color_temp = new Color(255, 255, 255, 50);

        final double x = InputGroundStation.getPositions().getX();
        final double y = InputGroundStation.getPositions().getY();
        final double z = InputGroundStation.getPositions().getZ();
        final PositionType positionType = PositionType.CARTESIAN_POSITION;

        this.lenght = Constants.WGS84_EARTH_EQUATORIAL_RADIUS;
        this.topRadius = lenght * FastMath.tan(angleOfAperture);
        this.bottomRadius = 0.0;
        this.position = new Position(x, y, z, positionType);
        this.color = color_temp;
        this.availability = InputGroundStation.getAvailability();
        this.heightReference = CesiumHeightReference.CLAMP_TO_GROUND;
    }

    public Cylinder(final TopocentricFrame topocentricFrame, final Header header, final double angleOfAperture) {
        final Color color_temp = new Color(255, 255, 255, 50);

        final double x = topocentricFrame.getCartesianPoint().getX();
        final double y = topocentricFrame.getCartesianPoint().getY();
        final double z = topocentricFrame.getCartesianPoint().getZ();

        final PositionType positionType = PositionType.CARTESIAN_POSITION;

        this.lenght = Constants.WGS84_EARTH_EQUATORIAL_RADIUS;
        this.topRadius = lenght * FastMath.tan(angleOfAperture);
        this.bottomRadius = 0.0;
        this.position = new Position(x, y, z, positionType);
        this.availability = header.getClock().getAvailability();
        this.color = color_temp;
        this.heightReference = CesiumHeightReference.CLAMP_TO_GROUND;
    }


    @Override
    public void write(final PacketCesiumWriter packetWriter, final CesiumOutputStream output) {

        try (CylinderCesiumWriter cylinderWriter = packetWriter.getCylinderWriter()) {
            cylinderWriter.open(output);
            cylinderWriter.writeBottomRadiusProperty(this.bottomRadius);
            cylinderWriter.writeTopRadiusProperty(this.topRadius);
            cylinderWriter.writeLengthProperty(this.lenght);

            final MaterialCesiumWriter materialWriter = cylinderWriter.getMaterialWriter();
            materialWriter.open(output);
            output.writeStartObject();

            final SolidColorMaterialCesiumWriter solidColorWriter = materialWriter.getSolidColorWriter();
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

    public void setColor(final Color color) {
        this.color = color;
    }
}
