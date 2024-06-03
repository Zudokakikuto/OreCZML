package org.example.CZMLObjects;

import cesiumlanguagewriter.*;
import org.example.CZMLObjects.CZMLSecondaryObects.PositionType;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.hipparchus.util.FastMath;

public class Position {

    // Cartographic :

    private double height;

    // Cartographic radians
    private double longitude;
    private double latitude;
    // Cartographic degrees
    private double longitudeDeg;
    private double latitudeDeg;

    // Cartesian :

    // Cartesian3Value:
    private double x;
    private double y;
    private double z;
    // Cartesian3VelocityValue
    private double vx;
    private double vy;
    private double vz;

    private String ReferenceFrame;

    private PositionType positionType;

    public Position(double param1, double param2, double param3, PositionType positionType){
        if(positionType == org.example.CZMLObjects.CZMLSecondaryObects.PositionType.CARTESIAN_POSITION){
            this.x = param1;
            this.y = param2;
            this.z = param3;
        } else if (positionType == org.example.CZMLObjects.CZMLSecondaryObects.PositionType.CARTESIAN_VELOCITY) {
            this.vx = param1;
            this.vy = param2;
            this.vz = param3;
        } else if (positionType == org.example.CZMLObjects.CZMLSecondaryObects.PositionType.CARTOGRAPHIC_RADIANS) {
            this.longitude = param1;
            this.latitude = param2;
            this.height = param3;
        } else if (positionType == org.example.CZMLObjects.CZMLSecondaryObects.PositionType.CARTOGRAPHIC_DEGREES) {
            this.longitudeDeg = param1;
            this.latitudeDeg = param2;
            this.height = param3;
        }
        else{
            throw new RuntimeException("Position Type is not defined");
        }

        this.positionType = positionType;
        this.ReferenceFrame = "INERTIAL";
    }

    public void write(PacketCesiumWriter packetWriter, CesiumOutputStream output, TimeInterval availability) {
        try(PositionCesiumWriter positionWriter =  packetWriter.getPositionWriter()){
            positionWriter.open(output);
            positionWriter.writeInterval(availability);

            if(positionType == PositionType.CARTESIAN_POSITION){
                Cartesian cartesian = new Cartesian(this.x,this.y,this.z);
                positionWriter.writeCartesian(cartesian);
            } else if (positionType == PositionType.CARTESIAN_VELOCITY) {
                Cartesian velocityCartesian = new Cartesian(this.vx,this.vy,this.vz);
                Motion1<Cartesian> motionCartesian = new Motion1<>(velocityCartesian);
                positionWriter.writeCartesianVelocity(motionCartesian);
            } else if (positionType == PositionType.CARTOGRAPHIC_RADIANS) {
                Cartographic cartographicRadians = new Cartographic(this.longitude, this.latitude, this.height);
                positionWriter.writeCartographicRadians(cartographicRadians);
            } else if (positionType == PositionType.CARTOGRAPHIC_DEGREES) {
                Cartographic cartographicDegrees = new Cartographic(this.longitudeDeg, this.latitudeDeg, this.height);
                positionWriter.writeCartographicDegrees(cartographicDegrees);
            }
        }
    }

    public double getLatitude() {
        if(latitude != 0.0){
            return latitude;
        } else if (latitudeDeg != 0.0) {
            return latitudeDeg*(FastMath.PI/180);
        }
        else{
            throw new RuntimeException("latitude is not defined");
        }
    }

    public double getLongitude() {
        if(longitude != 0.0){
            return longitude;
        } else if (longitudeDeg != 0.0) {
            return longitudeDeg*(FastMath.PI/180);
        }
        else{
            throw new RuntimeException("longitude is not defined");
        }
    }

    public String getReferenceFrame() {
        if(ReferenceFrame != null){
            return ReferenceFrame;
        }
        else {
            throw new RuntimeException("Reference frame is not defined");
        }
    }

    public double getHeight() {
        if(height != 0.0){
            return height;
        }
        else{
            throw new RuntimeException("Height is not defined");
        }
    }

    public double getLatitudeDeg() {
        if(latitude != 0.0){
            return latitude*(180/ FastMath.PI);
        } else if (latitudeDeg != 0.0) {
            return latitudeDeg;
        }
        else{
            throw new RuntimeException("latitude is not defined");
        }
    }

    public double getLongitudeDeg() {
        if (longitude != 0.0) {
            return longitude * (180 / FastMath.PI);
        } else if (longitudeDeg != 0.0) {
            return longitudeDeg;
        } else {
            throw new RuntimeException("longitude is not defined");
        }
    }

    public double getX() {
        if(x != 0.0){
            return x;
        }
        else {
            throw new RuntimeException("x is not defined");
        }
    }

    public double getY() {
        if(y != 0.0){
            return y;
        }
        else {
            throw new RuntimeException("y is not defined");
        }
    }

    public double getZ() {
        if(z!= 0.0){
            return z;
        }
        else {
            throw new RuntimeException("z is not defined");
        }
    }

    public double getVx() {
        if(vx != 0.0){
            return vx;
        }
        else {
            throw new RuntimeException("vx is not defined");
        }
    }

    public double getVy() {
        if(vy != 0.0){
            return vy;
        }
        else {
            throw new RuntimeException("vy is not defined");
        }
    }

    public double getVz() {
        if(vz != 0.0){
            return vz;
        }
        else {
            throw new RuntimeException("vz is not defined");
        }
    }

    public PositionType getPositionType() {
        return positionType;
    }

    public Vector3D toVector3D() {
        if (positionType == PositionType.CARTESIAN_POSITION) {
            double Station_x = this.getX();
            double Station_y = this.getY();
            double Station_z = this.getZ();
            return new Vector3D(Station_x, Station_y, Station_z);

        } else if (positionType == PositionType.CARTESIAN_VELOCITY) {
            double Station_vx = this.getVx();
            double Station_vy = this.getVy();
            double Station_vz = this.getVz();
            return new Vector3D(Station_vx, Station_vy, Station_vz);

        } else if (positionType == PositionType.CARTOGRAPHIC_DEGREES) {
            double Station_longitude = this.getLongitude();
            double Station_latitude = this.getLatitude();
            double Station_height = this.getHeight();
            return new Vector3D(Station_longitude, Station_latitude, Station_height);

        } else if (positionType == PositionType.CARTOGRAPHIC_RADIANS) {
            double Station_longitudeDegree = this.getLongitudeDeg();
            double Station_latitudeDegree = this.getLatitudeDeg();
            double Station_height = this.getHeight();
            return new Vector3D(Station_longitudeDegree, Station_latitudeDegree, Station_height);
        } else {
            throw new RuntimeException("PositionType is not defined");
        }
    }
}
