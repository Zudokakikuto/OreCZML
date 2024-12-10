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
package org.orekit.czml.object.primary;

import cesiumlanguagewriter.CesiumOutputStream;
import cesiumlanguagewriter.CesiumStreamWriter;
import cesiumlanguagewriter.PacketCesiumWriter;
import org.hipparchus.util.FastMath;
import org.orekit.czml.errors.OreCzmlException;
import org.orekit.czml.errors.OreCzmlMessages;
import org.orekit.czml.object.Position;
import org.orekit.czml.object.secondary.Cylinder;
import org.orekit.frames.TopocentricFrame;

import java.awt.Color;

/**
 * Visibility cone class
 *
 * <p>
 * This class will allows the user to build a visibility cone of a ground station. The visibility cone can have a given
 * angle of aperture. The cone will define how wide is the visibility of the station, this object is mandatory to build
 * a line of visibility.
 *
 * @author Julien LEBLOND.
 * @since 1.0.0
 */
public class VisibilityCone extends AbstractPrimaryObject {

    /**
     * The default angle of aperture of the visibility cone.
     */
    public static final double DEFAULT_ANGLE_OF_APERTURE = 80.0;

    /**
     * The default satellite (null), uses this to create a visibility cone not limited by the altitude of a satellite.
     */
    public static final Satellite DEFAULT_SATELLITE_PARAMETER = null;

    /**
     * The default ID of the visibility cone.
     */
    public static final String DEFAULT_ID_VIS = "VIS/";

    /**
     * The first default name of the visibility cone.
     */
    public static final String DEFAULT_NAME = "Visibility of ";

    /**
     * The second default name of the visibility cone.
     */
    public static final String DEFAULT_LOOKING_AT = " looking at ";

    /**
     * The cylinder object that represents the geometry of the visibility cone.
     */
    private final Cylinder cylinder;

    /**
     * The position of the visibility cone.
     */
    private final Position position;

    /**
     * The satellite that enters the visibility cone.
     */
    private Satellite satellite;

    // Constructors

    /**
     * The visibility cone constructor with classic parameters.
     *
     * @param id       : The id of the visibility cone
     * @param name     : The name of the visibility cone
     * @param cylinder : The cylinder for the visibility cone
     * @param header   : The header considered.
     */
    public VisibilityCone(final String id, final String name, final Cylinder cylinder, final Header header) {
        this(id, name, cylinder, DEFAULT_SATELLITE_PARAMETER, header);
    }

    /**
     * The visibility cone constructor with classic parameters with a satellite.
     *
     * @param id        : The id of the visibility cone
     * @param name      : The name of the visibility cone
     * @param cylinder  : The cylinder for the visibility cone
     * @param satellite : The satellite that will enter the visibility cone
     * @param header    : The header considered.
     */
    public VisibilityCone(final String id, final String name, final Cylinder cylinder,
                          final Satellite satellite, final Header header) {
        this.setId(id);
        this.setName(name);
        this.setAvailability(header.getAvailability());
        this.cylinder  = cylinder;
        this.position  = cylinder.getPosition();
        this.satellite = satellite;
    }

    /**
     * The visibility cone constructor with a czml ground station.
     *
     * @param groundStation : The ground station that will be linked to the visibility cone
     * @param header        : The header to consider
     */
    public VisibilityCone(final CzmlGroundStation groundStation, final Header header) {

        this.setId(DEFAULT_ID_VIS + groundStation.getName());
        this.setName(DEFAULT_NAME + groundStation.getName());
        this.setAvailability(header.getAvailability());
        this.cylinder = new Cylinder(groundStation, FastMath.toRadians(DEFAULT_ANGLE_OF_APERTURE), header);
        this.position = cylinder.getPosition();
    }

    /**
     * The visibility cone constructor with a czml ground station and a satellite.
     *
     * @param groundStation : The ground station that will be linked to the visibility cone
     * @param satellite     : The satellite that will go to the visibility cone, the height of the cone will be limited to the altitude of the satellite.
     * @param header        : The header considered.
     */
    public VisibilityCone(final CzmlGroundStation groundStation, final Satellite satellite, final Header header) {
        this(groundStation, satellite, DEFAULT_ANGLE_OF_APERTURE, header);
    }


    /**
     * The visibility cone constructor with no default parameters with a ground station and a satellite.
     *
     * @param groundStation   : The ground station that will be linked to the visibility cone
     * @param satellite       : The satellite that will go to the visibility cone, the height of the cone will be limited                        to the altitude of the satellite.
     * @param angleOfAperture : The angle of aperture of the ground station.
     * @param header          : The header considered.
     */
    public VisibilityCone(final CzmlGroundStation groundStation, final Satellite satellite,
                          final double angleOfAperture, final Header header) {

        this.setId(DEFAULT_ID_VIS + groundStation.getName() + "/" + satellite.getName());
        this.setName(DEFAULT_NAME + groundStation.getName() + DEFAULT_LOOKING_AT + satellite.getName());
        this.setAvailability(header.getAvailability());
        this.cylinder  = new Cylinder(groundStation.getTopocentricFrame(), satellite, angleOfAperture, header);
        this.position  = cylinder.getPosition();
        this.satellite = satellite;
    }

    /**
     * The visibility cone constructor for a topocentric frame and a satellite with default parameters.
     *
     * @param topocentricFrame : The topocentric frame where the station must be.
     * @param satellite        : The satellite that will go through the visibility cone.
     * @param header           : The header considered.
     */
    public VisibilityCone(final TopocentricFrame topocentricFrame,
                          final Satellite satellite, final Header header) {
        this(topocentricFrame, satellite, DEFAULT_ANGLE_OF_APERTURE, header);
    }

    /**
     * The visibility cone constructor for a topocentric frame and a satellite with no default parameters.
     *
     * @param topocentricFrame : The topocentric frame where the station must be.
     * @param satellite        : The satellite that will go through the visibility cone.
     * @param angleOfAperture  : The angle of aperture of the ground station.
     * @param header           : The header to consider.
     */
    public VisibilityCone(final TopocentricFrame topocentricFrame, final Satellite satellite,
                          final double angleOfAperture, final Header header) {

        this.setId(DEFAULT_ID_VIS + topocentricFrame.getName() + "/" + satellite.getName());
        this.setName(DEFAULT_NAME + topocentricFrame.getName() + DEFAULT_LOOKING_AT + satellite.getName());
        this.setAvailability(header.getAvailability());
        this.cylinder  = new Cylinder(topocentricFrame, satellite, angleOfAperture, header);
        this.position  = cylinder.getPosition();
        this.satellite = satellite;
    }


    // Overrides

    @Override
    public void writeCzmlBlock(final CesiumStreamWriter stream, final CesiumOutputStream output) {
        output.setPrettyFormatting(true);
        try (PacketCesiumWriter packet = stream.openPacket(output)) {
            packet.writeId(this.getId());
            packet.writeName(this.getName());
            packet.writeAvailability(this.getAvailability());

            cylinder.write(packet, output);

            position.write(packet, output, getAvailability());
        }
    }

    // Getters

    /**
     * Gets position.
     *
     * @return the position
     */
    public Position getPosition() {
        return position;
    }

    /**
     * Gets cylinder.
     *
     * @return the cylinder
     */
    public Cylinder getCylinder() {
        return cylinder;
    }

    /**
     * Gets satellite.
     *
     * @return the satellite
     */
    public Satellite getSatellite() {
        if (satellite == null) {
            throw new OreCzmlException(OreCzmlMessages.NO_SAT_VISIBILITY_CONE);
        } else {
            return satellite;
        }
    }

    // Display functions

    /**
     * No display.
     */
    public void noDisplay() {
        this.cylinder.setColor(new Color(0, 0, 0, 0));
    }
}


