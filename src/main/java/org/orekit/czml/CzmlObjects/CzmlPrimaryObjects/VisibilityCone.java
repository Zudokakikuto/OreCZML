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

import cesiumlanguagewriter.PacketCesiumWriter;
import cesiumlanguagewriter.TimeInterval;
import org.hipparchus.util.FastMath;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.czml.CzmlObjects.CzmlSecondaryObjects.Cylinder;
import org.orekit.czml.CzmlObjects.Position;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.frames.TopocentricFrame;
import org.orekit.utils.Constants;
import org.orekit.utils.IERSConventions;

import java.awt.Color;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;

/**
 * Visibility COne
 *
 * <p>
 * This class will allows the user to build a visibility cone of a ground station. The visibility cone can have a given
 * angle of aperture. The cone will define how wide is the visibility of the station, this object is mandatory to build
 * a line of visibility.
 * </p>
 *
 * @author Julien LEBLOND.
 * @since 1.0
 */

public class VisibilityCone extends AbstractPrimaryObject implements CzmlPrimaryObject {

    /**
     * .
     */
    public static final double DEFAULT_ANGLE_OF_APERTURE = 80.0;
    /**
     * .
     */
    public static final Satellite DEFAULT_SATELLITE_PARAMETER = null;
    /**
     * .
     */
    public static final String DEFAULT_NAME_FRAME = "Frame of the station";
    /**
     * .
     */
    public static final String DEFAULT_ID_VIS = "VIS/";
    /**
     * .
     */
    public static final String DEFAULT_NAME = "Visibility of ";
    /**
     * .
     */
    public static final String DEFAULT_LOOKING_AT = " looking at ";
    /**
     * .
     */
    private Cylinder cylinder;
    /**
     * .
     */
    private Position position;
    /**
     * .
     */
    private boolean clean = false;
    /**
     * .
     */
    private double angleOfAperture;

    // Intrinsic parameters
    /**
     * .
     */
    private org.orekit.estimation.measurements.GroundStation groundStation;

    // Satellite check for line of visibility
    /**
     * .
     */
    private Satellite satellite;


    public VisibilityCone(final String id, final String name, final Cylinder cylinder, final TimeInterval availability) {
        this(id, name, cylinder, availability, DEFAULT_SATELLITE_PARAMETER);
    }

    public VisibilityCone(final String id, final String name, final Cylinder cylinder, final TimeInterval availability, final Satellite satellite) {
        this.setId(id);
        this.setName(name);
        this.setAvailability(availability);
        this.cylinder = cylinder;
        this.position = cylinder.getPosition();

        final GeodeticPoint geodeticPoint = new GeodeticPoint(position.getLatitude(), position.getLongitude(), position.getHeight());
        final IERSConventions IERS = IERSConventions.IERS_2010;
        final Frame ITRF = FramesFactory.getITRF(IERS, true);
        final OneAxisEllipsoid earth = new OneAxisEllipsoid(Constants.WGS84_EARTH_EQUATORIAL_RADIUS, Constants.WGS84_EARTH_FLATTENING, ITRF);
        final TopocentricFrame topocentricFrame = new TopocentricFrame(earth, geodeticPoint, DEFAULT_NAME_FRAME);

        this.groundStation = new org.orekit.estimation.measurements.GroundStation(topocentricFrame);
        this.satellite = satellite;
    }

    public VisibilityCone(final CzmlGroundStation groundStation) {
        this.setId(DEFAULT_ID_VIS + groundStation.getName());
        this.setName(DEFAULT_NAME + groundStation.getName());
        this.setAvailability(Header.MASTER_CLOCK.getAvailability());
        this.cylinder = new Cylinder(groundStation, FastMath.toRadians(DEFAULT_ANGLE_OF_APERTURE));
        this.position = cylinder.getPosition();
        this.groundStation = groundStation.getOrekitGroundStation();
    }

    public VisibilityCone(final CzmlGroundStation groundStation, final Satellite satellite) {
        this(groundStation, satellite, DEFAULT_ANGLE_OF_APERTURE);
    }

    public VisibilityCone(final CzmlGroundStation groundStation, final Satellite satellite, final double angleOfAperture) {
        this.setId(DEFAULT_ID_VIS + groundStation.getName() + "/" + satellite.getName());
        this.setName(DEFAULT_NAME + groundStation.getName() + DEFAULT_LOOKING_AT + satellite.getName());
        this.setAvailability(Header.MASTER_CLOCK.getAvailability());
        this.cylinder = new Cylinder(groundStation, satellite, angleOfAperture);
        this.position = cylinder.getPosition();
        this.groundStation = groundStation.getOrekitGroundStation();
        this.satellite = satellite;
        this.angleOfAperture = angleOfAperture;
    }

    public VisibilityCone(final TopocentricFrame topocentricFrame, final Satellite satellite) throws URISyntaxException, IOException {
        this(topocentricFrame, satellite, DEFAULT_ANGLE_OF_APERTURE);
    }

    public VisibilityCone(final TopocentricFrame topocentricFrame, final Satellite satellite, final double angleOfAperture) throws URISyntaxException, IOException {
        final CzmlGroundStation groundStation1 = new CzmlGroundStation(topocentricFrame);
        this.setId(DEFAULT_ID_VIS + groundStation1.getName() + "/" + satellite.getName());
        this.setName(DEFAULT_NAME + groundStation1.getName() + DEFAULT_LOOKING_AT + satellite.getName());
        this.setAvailability(Header.MASTER_CLOCK.getAvailability());
        this.cylinder = new Cylinder(groundStation1, satellite, angleOfAperture);
        this.position = cylinder.getPosition();
        this.groundStation = groundStation1.getOrekitGroundStation();
        this.satellite = satellite;
        this.angleOfAperture = angleOfAperture;
    }

    @Override
    public void writeCzmlBlock() {
        OUTPUT.setPrettyFormatting(true);
        try (PacketCesiumWriter packet = STREAM.openPacket(OUTPUT)) {
            packet.writeId(this.getId());
            packet.writeName(this.getName());
            packet.writeAvailability(this.getAvailability());

            cylinder.write(packet, OUTPUT);

            position.write(packet, OUTPUT, getAvailability());
        }
        if (clean) {
            cleanObject();
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
        this.position = null;
        this.cylinder = null;
        this.satellite = null;
        this.groundStation = null;
        this.angleOfAperture = 0.0;
    }

    public Position getPosition() {
        return position;
    }

    public Cylinder getCylinder() {
        return cylinder;
    }

    public Satellite getSatellite() {
        if (satellite == null) {
            throw new RuntimeException("The Visibility cone was not defined with a given satellite");
        } else {
            return satellite;
        }
    }

    public org.orekit.estimation.measurements.GroundStation getGroundStation() {
        return groundStation;
    }

    public void enableClean() {
        this.clean = true;
    }

    public void noDisplay() {
        this.cylinder.setColor(new Color(0, 0, 0, 0));
    }
}


