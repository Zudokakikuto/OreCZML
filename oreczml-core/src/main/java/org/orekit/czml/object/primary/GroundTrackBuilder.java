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
package org.orekit.czml.object.primary;

import cesiumlanguagewriter.TimeInterval;
import org.orekit.bodies.BodyShape;
import org.orekit.czml.object.primary.entities.Constellation;
import org.orekit.czml.object.primary.entities.Spacecraft;

import java.awt.Color;

/**
 * Ground Track Builder class
 * <p>
 * Builder for the {@link GroundTrack} class.
 *
 * @author Julien LEBLOND
 * @since 1.0
 */
public class GroundTrackBuilder {

    /** The default ID for the ground track. */
    public static final String DEFAULT_ID = "GROUND_TRACK/";

    /**
     * The default color of the ground track on the body.
     */
    public static final Color DEFAULT_COLOR = new Color(255, 255, 255);

    /**
     * The satellite that will project the ground track on the ground.
     */
    private Spacecraft satellite;

    /**
     * The body where the ground track will be projected on.
     */
    private final BodyShape body;

    /**
     * The constellation that will project the ground track on the ground.
     */
    private Constellation constellation;

    /**
     * The color of the ground track.
     */
    private Color color = DEFAULT_COLOR;

    /**
     * The custom ID of the ground track.
     */
    private String customID;

    /** The availability to consider. */
    private TimeInterval availability = null;

    // Constructor

    /**
     * The constructor for the ground track builder using a single satellite.
     *
     * @param satellite : The satellite that will project the ground track on
     *        the ground.
     * @param body : The body where the ground track will be projected on.
     * @param availability : The availability considered.
     */
    public GroundTrackBuilder(final Spacecraft satellite, final BodyShape body,
                              final TimeInterval availability) {
        this.satellite = satellite;
        this.body = body;
        this.customID = DEFAULT_ID + satellite.getId();
        this.availability = availability;
    }

    /**
     * The constructor for the ground track builder using a constellation.
     *
     * @param constellation : The constellation that will project the ground
     *        track on the ground.
     * @param body : The body where the ground track will be projected on.
     * @param availability : The availability considered.
     */
    public GroundTrackBuilder(final Constellation constellation,
                              final BodyShape body,
                              final TimeInterval availability) {
        this.constellation = constellation;
        this.body = body;
        this.availability = availability;
        this.customID = DEFAULT_ID + constellation.getId();
    }

    /**
     * Function to set up the color of the ground track.
     *
     * @param colorInput : The color to set up.
     * @return : The ground track builder with a given color.
     */
    public GroundTrackBuilder withColor(final Color colorInput) {
        this.color = colorInput;
        return this;
    }

    /**
     * Function to set up a custom ID.
     *
     * @param customIDInput : The custom ID to set up.
     * @return : The ground track with a custom ID.
     */
    public GroundTrackBuilder withCustomID(final String customIDInput) {
        this.customID = customIDInput;
        return this;
    }

    /**
     * Function to set up the availability.
     *
     * @param availabilityInput : The availability to set up.
     * @return : The ground track builder with a given availability.
     */
    public GroundTrackBuilder
        withAvailability(final TimeInterval availabilityInput) {
        this.availability = availabilityInput;
        return this;
    }

    /**
     * Build ground track.
     *
     * @return the ground track
     */
    public GroundTrack build() {
        if (satellite != null) {
            return new GroundTrack(satellite, body, color, customID,
                                   availability);
        } else if (constellation != null) {
            return new GroundTrack(constellation, body, color, customID,
                                   availability);
        } else {
            return null;
        }
    }
}
