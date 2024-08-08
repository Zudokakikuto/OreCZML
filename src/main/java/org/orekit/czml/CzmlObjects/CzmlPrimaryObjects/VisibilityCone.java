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
import org.orekit.czml.CzmlObjects.CzmlSecondaryObjects.Cylinder;
import org.orekit.czml.CzmlObjects.Position;
import org.orekit.frames.TopocentricFrame;

import java.awt.Color;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;

/**
 * Visibility cone class
 *
 * <p>
 * This class will allows the user to build a visibility cone of a ground station. The visibility cone can have a given
 * angle of aperture. The cone will define how wide is the visibility of the station, this object is mandatory to build
 * a line of visibility.
 * </p>
 *
 * @author Julien LEBLOND.
 * @since 1.0.0
 */

public class VisibilityCone extends AbstractPrimaryObject implements CzmlPrimaryObject {

    /** The default angle of aperture of the visibility cone. */
    public static final double DEFAULT_ANGLE_OF_APERTURE = 80.0;

    /** The default satellite (null), use this to create a visibility cone not limited by the altitude of a satellite. */
    public static final Satellite DEFAULT_SATELLITE_PARAMETER = null;

    /** The default ID of the visibility cone. */
    public static final String DEFAULT_ID_VIS = "VIS/";

    /** The first default name of the visibility cone. */
    public static final String DEFAULT_NAME = "Visibility of ";

    /** The second default name of the visibility cone. */
    public static final String DEFAULT_LOOKING_AT = " looking at ";

    /** The cylinder object that represents the geometry of the visibility cone. */
    private Cylinder cylinder;

    /** The position of the visibility cone. */
    private Position position;

    /** If the visibility cone has been cleaned or not. */
    private boolean clean = false;


    // Intrinsic parameters

    // Satellite check for line of visibility
    /** . */
    private Satellite satellite;


    public VisibilityCone(final String id, final String name, final Cylinder cylinder,
                          final TimeInterval availability) {
        this(id, name, cylinder, availability, DEFAULT_SATELLITE_PARAMETER);
    }

    public VisibilityCone(final String id, final String name, final Cylinder cylinder, final TimeInterval availability,
                          final Satellite satellite) {
        this.setId(id);
        this.setName(name);
        this.setAvailability(availability);
        this.cylinder  = cylinder;
        this.position  = cylinder.getPosition();
        this.satellite = satellite;
    }

    public VisibilityCone(final CzmlGroundStation groundStation) {
        this.setId(DEFAULT_ID_VIS + groundStation.getName());
        this.setName(DEFAULT_NAME + groundStation.getName());
        this.setAvailability(Header.getMasterClock()
                                   .getAvailability());
        this.cylinder = new Cylinder(groundStation, FastMath.toRadians(DEFAULT_ANGLE_OF_APERTURE));
        this.position = cylinder.getPosition();
    }

    public VisibilityCone(final CzmlGroundStation groundStation, final Satellite satellite) {
        this(groundStation, satellite, DEFAULT_ANGLE_OF_APERTURE);
    }

    public VisibilityCone(final CzmlGroundStation groundStation, final Satellite satellite,
                          final double angleOfAperture) {
        this.setId(DEFAULT_ID_VIS + groundStation.getName() + "/" + satellite.getName());
        this.setName(DEFAULT_NAME + groundStation.getName() + DEFAULT_LOOKING_AT + satellite.getName());
        this.setAvailability(Header.getMasterClock()
                                   .getAvailability());
        this.cylinder  = new Cylinder(groundStation, satellite, angleOfAperture);
        this.position  = cylinder.getPosition();
        this.satellite = satellite;
    }

    public VisibilityCone(final TopocentricFrame topocentricFrame,
                          final Satellite satellite) throws URISyntaxException, IOException {
        this(topocentricFrame, satellite, DEFAULT_ANGLE_OF_APERTURE);
    }

    public VisibilityCone(final TopocentricFrame topocentricFrame, final Satellite satellite,
                          final double angleOfAperture) throws URISyntaxException, IOException {
        final CzmlGroundStation groundStation1 = new CzmlGroundStation(topocentricFrame);
        this.setId(DEFAULT_ID_VIS + groundStation1.getName() + "/" + satellite.getName());
        this.setName(DEFAULT_NAME + groundStation1.getName() + DEFAULT_LOOKING_AT + satellite.getName());
        this.setAvailability(Header.getMasterClock()
                                   .getAvailability());
        this.cylinder  = new Cylinder(groundStation1, satellite, angleOfAperture);
        this.position  = cylinder.getPosition();
        this.satellite = satellite;
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
        this.position  = null;
        this.cylinder  = null;
        this.satellite = null;
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

    public void enableClean() {
        this.clean = true;
    }

    public void noDisplay() {
        this.cylinder.setColor(new Color(0, 0, 0, 0));
    }
}


