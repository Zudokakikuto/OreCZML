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

public class VisibilityCone implements CZMLPrimaryObject {

    /** .*/
    public static final double DEFAULT_ANGLE_OF_APERTURE = 80.0;
    /** .*/
    public static final Satellite DEFAULT_SATELLITE_PARAMETER = null;
    /** .*/
    public static final String DEFAULT_NAME_FRAME = "Frame of the station";
    /** .*/
    public static final String DEFAULT_ID_VIS = "VIS/";
    /** .*/
    public static final String DEFAULT_NAME = "Visibility of ";
    /** .*/
    public static final String DEFAULT_LOOKING_AT = " looking at ";
    /** .*/
    private String id;
    /** .*/
    private String name;
    /** .*/
    private Cylinder cylinder;
    /** .*/
    private TimeInterval availability;
    /** .*/
    private Position position;
    /** .*/
    private double angleOfAperture;

    // Intrinsic parameters
    /** .*/
    private org.orekit.estimation.measurements.GroundStation groundStation;

    // Satellite check for line of visibility
    /** .*/
    private Satellite satellite;

    public VisibilityCone(final String id, final String name, final Cylinder cylinder, final TimeInterval availability) {
        this(id, name, cylinder, availability, DEFAULT_SATELLITE_PARAMETER);
    }

    public VisibilityCone(final String id, final String name, final Cylinder cylinder, final TimeInterval availability, final Satellite satellite) {
        this.id = id;
        this.name = name;
        this.cylinder = cylinder;
        this.availability = availability;
        this.position = cylinder.getPosition();

        final GeodeticPoint geodeticPoint = new GeodeticPoint(position.getLatitude(), position.getLongitude(), position.getHeight());
        final IERSConventions IERS = IERSConventions.IERS_2010;
        final Frame ITRF = FramesFactory.getITRF(IERS, true);
        final OneAxisEllipsoid earth = new OneAxisEllipsoid(Constants.WGS84_EARTH_EQUATORIAL_RADIUS, Constants.WGS84_EARTH_FLATTENING, ITRF);
        final TopocentricFrame topocentricFrame = new TopocentricFrame(earth, geodeticPoint, DEFAULT_NAME_FRAME);

        this.groundStation = new org.orekit.estimation.measurements.GroundStation(topocentricFrame);
        this.satellite = satellite;
    }

    public VisibilityCone(final CZMLGroundStation groundStation, final Header header) {
        this.id = DEFAULT_ID_VIS + groundStation.getName();
        this.name = DEFAULT_NAME + groundStation.getName();
        this.cylinder = new Cylinder(groundStation, FastMath.toRadians(DEFAULT_ANGLE_OF_APERTURE));
        this.availability = header.getClock().getAvailability();
        this.position = cylinder.getPosition();
        this.groundStation = groundStation.getOrekitGroundStation();
    }

    public VisibilityCone(final CZMLGroundStation groundStation, final Satellite satellite, final Header header) {
        this(groundStation, satellite, header, DEFAULT_ANGLE_OF_APERTURE);
    }

    public VisibilityCone(final CZMLGroundStation groundStation, final Satellite satellite, final Header header, final double angleOfAperture) {
        this.id = DEFAULT_ID_VIS + groundStation.getName() + "/" + satellite.getName();
        this.name = DEFAULT_NAME + groundStation.getName() + DEFAULT_LOOKING_AT + satellite.getName();
        this.cylinder = new Cylinder(groundStation, satellite, angleOfAperture);
        this.availability = header.getClock().getAvailability();
        this.position = cylinder.getPosition();
        this.groundStation = groundStation.getOrekitGroundStation();
        this.satellite = satellite;
        this.angleOfAperture = angleOfAperture;
    }

    public VisibilityCone(final TopocentricFrame topocentricFrame, final Satellite satellite, final Header header) {
        this(topocentricFrame, satellite, header, DEFAULT_ANGLE_OF_APERTURE);
    }

    public VisibilityCone(final TopocentricFrame topocentricFrame, final Satellite satellite, final Header header, final double angleOfAperture) {
        final CZMLGroundStation groundStation1 = new CZMLGroundStation(topocentricFrame, header);
        this.id = DEFAULT_ID_VIS + groundStation1.getName() + "/" + satellite.getName();
        this.name = DEFAULT_NAME + groundStation1.getName() + DEFAULT_LOOKING_AT + satellite.getName();
        this.cylinder = new Cylinder(groundStation1, satellite, angleOfAperture);
        this.availability = header.getClock().getAvailability();
        this.position = cylinder.getPosition();
        this.groundStation = groundStation1.getOrekitGroundStation();
        this.satellite = satellite;
        this.angleOfAperture = angleOfAperture;
    }

    @Override
    public void generateCZML() {
        OUTPUT.setPrettyFormatting(true);
        try (PacketCesiumWriter packet = STREAM.openPacket(OUTPUT)) {
            packet.writeId(id);
            packet.writeName(name);
            packet.writeAvailability(availability);

            cylinder.write(packet, OUTPUT);

            position.write(packet, OUTPUT, availability);
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
        this.id = "";
        this.name = "";
        this.position = null;
        this.availability = null;
        this.cylinder = null;
        this.satellite = null;
        this.groundStation = null;
        this.angleOfAperture = 0.0;
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

    public Satellite getSatellite() {
        if (satellite == null) {
            throw new RuntimeException("The Visibility cone was not defined with a given satellite");
        }
        else {
            return satellite;
        }
    }

    public org.orekit.estimation.measurements.GroundStation getGroundStation() {
        return groundStation;
    }

    public void noDisplay() {
        this.cylinder.setColor(new Color(0, 0, 0, 0));
    }
}
