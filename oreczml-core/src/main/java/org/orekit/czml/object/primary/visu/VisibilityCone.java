/* Copyright 2002-2025 CS GROUP
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
package org.orekit.czml.object.primary.visu;

import cesiumlanguagewriter.CesiumOutputStream;
import cesiumlanguagewriter.CesiumStreamWriter;
import cesiumlanguagewriter.PacketCesiumWriter;
import org.hipparchus.util.FastMath;
import org.orekit.czml.errors.OreCzmlException;
import org.orekit.czml.errors.OreCzmlMessages;
import org.orekit.czml.object.Position;
import org.orekit.czml.object.primary.AbstractPrimaryObject;
import org.orekit.czml.object.primary.entities.CzmlGroundStation;
import org.orekit.czml.object.primary.entities.Spacecraft;
import org.orekit.czml.object.secondary.Clock;
import org.orekit.czml.object.secondary.Cylinder;
import org.orekit.frames.TopocentricFrame;

import java.awt.Color;

/**
 * Visibility cone class
 * <p>
 * This class will allows the user to build a visibility cone of a ground
 * station. The visibility cone can have a given angle of aperture. The cone
 * will define how wide is the visibility of the station, this object is
 * mandatory to build a line of visibility.
 *
 * @author Julien LEBLOND.
 * @since 1.0.0
 */
public class VisibilityCone
    extends
    AbstractPrimaryObject<VisibilityCone> {

    /**
     * The default angle of aperture of the visibility cone.
     */
    public static final double DEFAULT_ANGLE_OF_APERTURE = 80.0;

    /**
     * The default satellite (null), uses this to create a visibility cone not
     * limited by the altitude of a satellite.
     */
    public static final Spacecraft DEFAULT_SPACECRAFT_PARAMETER = null;

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
    private Spacecraft spacecraft;

    /** The ground station if one is used. */
    private CzmlGroundStation groundStation;

    /** The angle of aperture of the station if used. */
    private double angleOfAperture = DEFAULT_ANGLE_OF_APERTURE;

    /** The topocentric frame if one is used. */
    private TopocentricFrame topocentricFrame;

    /** The clock of the visibility cone. */
    private Clock clock;

    // Constructors

    /**
     * The visibility cone constructor with classic parameters.
     *
     * @param id : The id of the visibility cone
     * @param name : The name of the visibility cone
     * @param cylinder : The cylinder for the visibility cone
     * @param clock : The clock considered.
     */
    public VisibilityCone(final String id, final String name,
                          final Cylinder cylinder, final Clock clock) {
        this(id, name, cylinder, DEFAULT_SPACECRAFT_PARAMETER, clock);
    }

    /**
     * The visibility cone constructor with classic parameters with a satellite.
     *
     * @param id : The id of the visibility cone
     * @param name : The name of the visibility cone
     * @param cylinder : The cylinder for the visibility cone
     * @param spacecraft : The spacecraft that will enter the visibility cone
     * @param clock : The clock
     */
    public VisibilityCone(final String id, final String name,
                          final Cylinder cylinder, final Spacecraft spacecraft,
                          final Clock clock) {
        this.setId(id);
        this.setName(name);
        this.setAvailability(clock.getAvailability());
        this.cylinder = cylinder;
        this.position = cylinder.getPosition();
        this.spacecraft = spacecraft;
        this.clock = clock;
    }

    /**
     * The visibility cone constructor with a czml ground station.
     *
     * @param groundStation : The ground station that will be linked to the
     *        visibility cone
     * @param clock : The time frame for which the feature is available.
     */
    public VisibilityCone(final CzmlGroundStation groundStation,
                          final Clock clock) {

        this.setId(DEFAULT_ID_VIS + groundStation.getName());
        this.setName(DEFAULT_NAME + groundStation.getName());
        this.groundStation = groundStation;
        this.setAvailability(clock.getAvailability());
        this.cylinder =
            new Cylinder(groundStation,
                         FastMath.toRadians(DEFAULT_ANGLE_OF_APERTURE), clock);
        this.position = cylinder.getPosition();
        this.clock = clock;
    }

    /**
     * The visibility cone constructor with a czml ground station and a
     * satellite.
     *
     * @param groundStation : The ground station that will be linked to the
     *        visibility cone
     * @param satellite : The satellite that will go to the visibility cone, the
     *        height of the cone will be limited to the altitude of the
     *        satellite.
     * @param clock : The clock
     */
    public VisibilityCone(final CzmlGroundStation groundStation,
                          final Spacecraft satellite, final Clock clock) {
        this(groundStation, satellite, DEFAULT_ANGLE_OF_APERTURE, clock);
    }

    /**
     * The visibility cone constructor with no default parameters with a ground
     * station and a satellite.
     *
     * @param groundStation : The ground station that will be linked to the
     *        visibility cone
     * @param spacecraft : The satellite that will go to the visibility cone,
     *        the height of the cone will be limited to the altitude of the
     *        satellite.
     * @param angleOfAperture : The angle of aperture of the ground station.
     * @param clock : The clock
     */
    public VisibilityCone(final CzmlGroundStation groundStation,
                          final Spacecraft spacecraft,
                          final double angleOfAperture, final Clock clock) {

        this.setId(DEFAULT_ID_VIS +
                   groundStation.getName() + "/" + spacecraft.getName());
        this.setName(DEFAULT_NAME +
                     groundStation.getName() + DEFAULT_LOOKING_AT +
                     spacecraft.getName());
        this.setAvailability(clock.getAvailability());
        this.groundStation = groundStation;
        this.cylinder =
            new Cylinder(groundStation.getTopocentricFrame(), spacecraft,
                         angleOfAperture, clock);
        this.position = cylinder.getPosition();
        this.angleOfAperture = angleOfAperture;
        this.spacecraft = spacecraft;
        this.clock = clock;
    }

    /**
     * The visibility cone constructor for a topocentric frame and a satellite
     * with default parameters.
     *
     * @param topocentricFrame : The topocentric frame where the station must
     *        be.
     * @param satellite : The satellite that will go through the visibility
     *        cone.
     * @param clock : The clock
     */
    public VisibilityCone(final TopocentricFrame topocentricFrame,
                          final Spacecraft satellite, final Clock clock) {
        this(topocentricFrame, satellite, DEFAULT_ANGLE_OF_APERTURE, clock);
    }

    /**
     * The visibility cone constructor for a topocentric frame and a satellite
     * with no default parameters.
     *
     * @param topocentricFrame : The topocentric frame where the station must
     *        be.
     * @param spacecraft : The satellite that will go through the visibility
     *        cone.
     * @param angleOfAperture : The angle of aperture of the ground station.
     * @param clock : The clock
     */
    public VisibilityCone(final TopocentricFrame topocentricFrame,
                          final Spacecraft spacecraft,
                          final double angleOfAperture, final Clock clock) {

        this.setId(DEFAULT_ID_VIS +
                   topocentricFrame.getName() + "/" + spacecraft.getName());
        this.setName(DEFAULT_NAME +
                     topocentricFrame.getName() + DEFAULT_LOOKING_AT +
                     spacecraft.getName());
        this.setAvailability(clock.getAvailability());
        this.cylinder =
            new Cylinder(topocentricFrame, spacecraft, angleOfAperture, clock);
        this.position = cylinder.getPosition();
        this.spacecraft = spacecraft;
        this.topocentricFrame = topocentricFrame;
        this.angleOfAperture = angleOfAperture;
        this.clock = clock;
    }

    // Overrides

    @Override
    public void writeCzmlBlock(final CesiumStreamWriter stream,
                               final CesiumOutputStream output) {
        output.setPrettyFormatting(true);
        try (PacketCesiumWriter packet = stream.openPacket(output)) {
            packet.writeId(this.getId());
            packet.writeName(this.getName());
            packet.writeAvailability(this.getAvailability());

            cylinder.write(packet, output);

            position.write(packet, output);
        }
    }

    @Override
    public VisibilityCone cloneObject() {
        final VisibilityCone toReturn;
        // Two first constructors using the id and the spacecraft
        if (this.getId() != null) {
            if (this.spacecraft != null) {
                toReturn =
                    new VisibilityCone(this.getId(), this.getName(),
                                       this.cylinder, this.spacecraft,
                                       this.clock);
            } else {
                toReturn =
                    new VisibilityCone(this.getId(), this.getName(),
                                       this.cylinder, this.clock);
            }
            // Three other constructors using the ground station, the spacecraft
            // and the angle of aperture
        } else if (this.groundStation != null) {
            if (this.spacecraft != null) {
                if (this.angleOfAperture != DEFAULT_ANGLE_OF_APERTURE) {
                    toReturn =
                        new VisibilityCone(this.groundStation, this.spacecraft,
                                           this.angleOfAperture, this.clock);
                } else {
                    toReturn =
                        new VisibilityCone(this.groundStation, this.spacecraft,
                                           this.clock);
                }
            } else {
                toReturn = new VisibilityCone(this.groundStation, this.clock);
            }
            // The two last constructors using the topocentric frame and the
            // angle of aperture
        } else if (this.topocentricFrame != null) {
            if (this.angleOfAperture != DEFAULT_ANGLE_OF_APERTURE) {
                toReturn =
                    new VisibilityCone(this.topocentricFrame, this.spacecraft,
                                       this.angleOfAperture, this.clock);
            } else {
                toReturn =
                    new VisibilityCone(this.topocentricFrame, this.spacecraft,
                                       this.clock);
            }
        } else {
            throw new OreCzmlException(OreCzmlMessages.NOT_VALID_PRIMARY_OBJECT_FOR_CLONE);
        }
        return toReturn;
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
    public Spacecraft getSpacecraft() {
        if (spacecraft == null) {
            throw new OreCzmlException(OreCzmlMessages.NO_SAT_VISIBILITY_CONE);
        } else {
            return spacecraft;
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
