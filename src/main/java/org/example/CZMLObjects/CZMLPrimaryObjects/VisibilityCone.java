package org.example.CZMLObjects.CZMLPrimaryObjects;

import cesiumlanguagewriter.*;
import org.example.CZMLObjects.CZMLSecondaryObects.Cylinder;
import org.example.CZMLObjects.Position;
import org.hipparchus.util.FastMath;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.frames.TopocentricFrame;
import org.orekit.utils.Constants;
import org.orekit.utils.IERSConventions;

import java.awt.*;
import java.io.StringWriter;

public class VisibilityCone implements CZMLPrimaryObject{

    private String id;
    private String name;
    private Cylinder cylinder;
    private TimeInterval availability;
    private Position position;
    private double angleOfAperture;

    // Intrinsinc parameters
    private org.orekit.estimation.measurements.GroundStation groundStation;

    // Satellite check for line of visibility
    private Satellite satellite;

    public VisibilityCone(String id, String name, Cylinder cylinder, TimeInterval availability){
        this.id = id;
        this.name = name;
        this.cylinder = cylinder;
        this.availability = availability;
        this.position = cylinder.getPosition();

        GeodeticPoint geodeticPoint = new GeodeticPoint(position.getLatitude(),position.getLongitude(),position.getHeight());
        IERSConventions IERS = IERSConventions.IERS_2010;
        Frame ITRF = FramesFactory.getITRF(IERS,true);
        OneAxisEllipsoid earth = new OneAxisEllipsoid(Constants.WGS84_EARTH_EQUATORIAL_RADIUS,Constants.WGS84_EARTH_FLATTENING,ITRF);
        TopocentricFrame topocentricFrame = new TopocentricFrame(earth,geodeticPoint,"Frame of the station");

        this.groundStation = new org.orekit.estimation.measurements.GroundStation(topocentricFrame);
    }

    public VisibilityCone(String id, String name, Cylinder cylinder, TimeInterval availability, Satellite satellite){
        this.id = id;
        this.name = name;
        this.cylinder = cylinder;
        this.availability = availability;
        this.position = cylinder.getPosition();

        GeodeticPoint geodeticPoint = new GeodeticPoint(position.getLatitude(),position.getLongitude(),position.getHeight());
        IERSConventions IERS = IERSConventions.IERS_2010;
        Frame ITRF = FramesFactory.getITRF(IERS,true);
        OneAxisEllipsoid earth = new OneAxisEllipsoid(Constants.WGS84_EARTH_EQUATORIAL_RADIUS,Constants.WGS84_EARTH_FLATTENING,ITRF);
        TopocentricFrame topocentricFrame = new TopocentricFrame(earth,geodeticPoint,"Frame of the station");

        this.groundStation = new org.orekit.estimation.measurements.GroundStation(topocentricFrame);
        this.satellite = satellite;
    }

    public VisibilityCone(GroundStation groundStation,Header header){
        this.id = "VIS/" + groundStation.getName();
        this.name = "Visibility of " + groundStation.getName();
        this.cylinder = new Cylinder(groundStation, FastMath.toRadians(angleOfAperture));
        this.availability = header.getClock().getInterval();
        this.position = cylinder.getPosition();
        this.groundStation = groundStation.getOrekitGroundStation();
    }

    public VisibilityCone(GroundStation groundStation, Satellite satellite, Header header){
        this.id = "VIS/" + groundStation.getName() + "/" + satellite.getName();
        this.name = "Visibility of " + groundStation.getName() + " looking at " + satellite.getName();
        this.cylinder = new Cylinder(groundStation,satellite,90);
        this.availability = header.getClock().getInterval();
        this.position = cylinder.getPosition();
        this.groundStation = groundStation.getOrekitGroundStation();
        this.satellite = satellite;
    }

    public VisibilityCone(GroundStation groundStation, Satellite satellite, Header header, double angleOfAperture){
        this.id = "VIS/" + groundStation.getName() + "/" + satellite.getName();
        this.name = "Visibility of " + groundStation.getName() + " looking at " + satellite.getName();
        this.cylinder = new Cylinder(groundStation,satellite,angleOfAperture);
        this.availability = header.getClock().getInterval();
        this.position = cylinder.getPosition();
        this.groundStation = groundStation.getOrekitGroundStation();
        this.satellite = satellite;
        this.angleOfAperture = angleOfAperture;
    }

    public VisibilityCone(TopocentricFrame topocentricFrame, Satellite satellite, Header header){
        GroundStation groundStation1 = new GroundStation(topocentricFrame,header);
        this.id = "VIS/" + groundStation1.getName() + "/" + satellite.getName();
        this.name = "Visibility of " + groundStation1.getName() + " looking at " + satellite.getName();
        this.cylinder = new Cylinder(groundStation1,satellite,90);
        this.availability = header.getClock().getInterval();
        this.position = cylinder.getPosition();
        this.groundStation = groundStation1.getOrekitGroundStation();
        this.satellite = satellite;
    }

    public VisibilityCone(TopocentricFrame topocentricFrame, Satellite satellite, Header header, double angleOfAperture){
        GroundStation groundStation1 = new GroundStation(topocentricFrame,header);
        this.id = "VIS/" + groundStation1.getName() + "/" + satellite.getName();
        this.name = "Visibility of " + groundStation1.getName() + " looking at " + satellite.getName();
        this.cylinder = new Cylinder(groundStation1,satellite,angleOfAperture);
        this.availability = header.getClock().getInterval();
        this.position = cylinder.getPosition();
        this.groundStation = groundStation1.getOrekitGroundStation();
        this.satellite = satellite;
        this.angleOfAperture = angleOfAperture;
    }

    @Override
    public void generateCZML() {
        output.setPrettyFormatting(true);
        try (PacketCesiumWriter packet = stream.openPacket(output)) {
            packet.writeId(id);
            packet.writeName(name);
            packet.writeAvailability(availability);

            cylinder.write(packet,output);

            position.write(packet,output,availability);
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

    public String getId() {
        return id;
    }

    public Position getPosition() {
        return position;
    }

    public String getName() {
        return name;
    }

    public TimeInterval getAvailability() {
        return availability;
    }

    public Cylinder getCylinder() {
        return cylinder;
    }

    public Satellite getSatellite(){
        if(satellite == null){
            throw new RuntimeException("The Visibility cone was not defined with a given satellite");
        }
        else{
            return satellite;
        }
    }

    public org.orekit.estimation.measurements.GroundStation getGroundStation() {
        return groundStation;
    }

    public void noDisplay(){
        this.cylinder.setColor(new Color(0,0,0,0));
    }
}
