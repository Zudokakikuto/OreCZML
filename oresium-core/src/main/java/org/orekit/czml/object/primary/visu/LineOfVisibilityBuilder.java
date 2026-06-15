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

import org.orekit.czml.object.primary.entities.Constellation;
import org.orekit.czml.object.primary.entities.Spacecraft;
import org.orekit.czml.object.secondary.Clock;
import org.orekit.frames.TopocentricFrame;

/**
 * Line of visibility builder class
 * <p>
 * Builder for the {@link LineOfVisibility} class.
 *
 * @author Julien LEBLOND
 * @since 1.0.0
 */
public class LineOfVisibilityBuilder {

    /**
     * The default angle of aperture of the ground station.
     */
    public static final double DEFAULT_ANGLE_OF_APERTURE = 80.0;

    /**
     * The topocentric frame where the ground station is.
     */
    private TopocentricFrame topocentricFrame;

    /**
     * The satellite observed.
     */
    private Spacecraft spacecraft;

    // Optional parameters

    /**
     * The angle of aperture of the station.
     */
    private double angleOfAperture = DEFAULT_ANGLE_OF_APERTURE;

    /**
     * The custom ID of the line of visibility.
     */
    private String customID;

    /** The constellation of the lines. */
    private Constellation constellation;

    /** Boolean to display the triangle or not. */
    private boolean displayTriangle = false;

    /** The clock considered. */
    private Clock clock;

    // Constructor

    /**
     * The constructor of the line of visibility builder with a spacecraft.
     *
     * @param topocentricFrameInput : The topocentric frame where the ground
     *        station is.
     * @param spacecraftInput : The spacecraft observed.
     * @param clock : The clock considered.
     */
    public LineOfVisibilityBuilder(final TopocentricFrame topocentricFrameInput,
                                   final Spacecraft spacecraftInput,
                                   final Clock clock) {
        this.spacecraft = spacecraftInput;
        this.topocentricFrame = topocentricFrameInput;
        this.customID =
            LineOfVisibility.DEFAULT_ID +
                        topocentricFrameInput.getName() + "/" +
                        spacecraftInput.getId();
        this.clock = clock;
    }

    /**
     * The constructor fo the line of visibility builder with a constellation.
     *
     * @param topocentricFrameInput : The topocentric frame where the ground
     *        station is.
     * @param constellationInput : The constellation observed.
     * @param clock : The clock considered.
     */
    public LineOfVisibilityBuilder(final TopocentricFrame topocentricFrameInput,
                                   final Constellation constellationInput,
                                   final Clock clock) {
        this.constellation = constellationInput;
        this.topocentricFrame = topocentricFrameInput;
        this.customID =
            LineOfVisibility.DEFAULT_ID +
                        topocentricFrameInput.getName() + "/" +
                        constellationInput.getId();
        this.clock = clock;
    }

    /**
     * Function to set up an angle of aperture.
     *
     * @param angleOfApertureInput : The angle of aperture to set up.
     * @return : The line of visibility builder with the given angle of
     *         aperture.
     */
    public LineOfVisibilityBuilder
        withAngleOfAperture(final double angleOfApertureInput) {
        this.angleOfAperture = angleOfApertureInput;
        return this;
    }

    /**
     * Function to set up a custom ID.
     *
     * @param customIDInput : The custom ID to set up.
     * @return : The line of visibility object with a custom ID.
     */
    public LineOfVisibilityBuilder withCustomID(final String customIDInput) {
        this.customID = customIDInput;
        return this;
    }

    /**
     * Function to set up a clock.
     *
     * @param clockInput : The clock to set up.
     * @return : The line of visibility builder with a clock.
     */
    public LineOfVisibilityBuilder withClock(final Clock clockInput) {
        this.clock = clockInput;
        return this;
    }

    /**
     * Function to display the visibility triangle of the line of visibility.
     *
     * @return : The line of visibility builder with visibility triangles
     *         displayed
     */
    public LineOfVisibilityBuilder withVisibilityTriangle() {
        this.displayTriangle = true;
        return this;
    }

    /**
     * The build function that generates a line of visibility object.
     *
     * @return : A line of visibility object with the given parameters of the
     *         builder. *
     */
    public LineOfVisibility build() {
        LineOfVisibility toReturn = null;
        if (spacecraft != null) {
            toReturn =
                new LineOfVisibility(topocentricFrame, spacecraft,
                                     angleOfAperture, customID, clock);

        }
        if (constellation != null) {
            toReturn =
                new LineOfVisibility(topocentricFrame, constellation,
                                     angleOfAperture, customID, clock);
        }
        if (displayTriangle) {
            if (spacecraft != null) {
                toReturn.displayTriangle();
            } else {
                if (constellation != null) {
                    for (int i = 0; i < constellation.getTotalOfSatellite();
                         i++) {
                        toReturn.displaySingleTriangle(i);
                    }
                }
            }
        }
        return toReturn;
    }
}
