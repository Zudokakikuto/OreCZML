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
package org.orekit.czml.archi.builder;

import cesiumlanguagewriter.TimeInterval;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.czml.object.primary.AttitudePointing;
import org.orekit.czml.object.primary.Header;
import org.orekit.czml.object.primary.Satellite;

import java.awt.Color;

/**
 * Attitude pointing builder class
 * <p>
 * Builder for the {@link org.orekit.czml.object.primary.AttitudePointing} class.
 * <p>
 *
 * @author Julien LEBLOND
 * @since 1.0.0
 */

public class AttitudePointingBuilder {

    /** The default color of the pointing line. */
    public static final Color DEFAULT_COLOR = Color.GREEN;

    /** The default time interval where the attitude pointing will be displayed. */
    public static final TimeInterval DEFAULT_AVAILABILITY = Header.getMasterClock()
                                                                  .getAvailability();

    /** The satellite that has a pointing to the ground. */
    private Satellite satellite;

    /** The body that is pointed at. */
    private OneAxisEllipsoid body;

    /** The direction of the pointing. */
    private Vector3D direction;


    // Optional arguments
    /** The availability of the attitude pointing object. */
    private TimeInterval availability = DEFAULT_AVAILABILITY;

    /** The color of the line. */
    private Color color = DEFAULT_COLOR;

    /**
     * Director that manages the pointing or not at objects during the orbit.
     * Put this parameter on if the satellite is pointing at objects during the orbit.
     * This boolean will project the attitude on the ground when it is not pointing at objects.
     * When the attitude is pointing at objects, it will put the projection on the pointed object.
     * <p>
     * AttitudeTuto.AttitudePathAlongOrbit
     */
    private Boolean displayOnGround = false;

    /** To display or not the trace of the pointing on the ground. */
    private Boolean displayPointingPath = false;

    /** To display a period of the pointing path or not. */
    private Boolean displayPeriodPointingPath = false;

    // Constructor

    /**
     * The constructor of the builder.
     *
     * @param satelliteInput : The satellite that has a pointing.
     * @param bodyInput      : The body that is pointed at.
     * @param directionInput : The direction of the pointing.
     */
    public AttitudePointingBuilder(final Satellite satelliteInput, final OneAxisEllipsoid bodyInput,
                                   final Vector3D directionInput) {
        this.satellite = satelliteInput;
        this.body      = bodyInput;
        this.direction = directionInput;
    }

    /**
     * Function to set up an availability.
     *
     * @param availabilityInput : The availability to set up.
     * @return : The attitude pointing builder with the given availability.
     */
    public AttitudePointingBuilder withAvailability(final TimeInterval availabilityInput) {
        this.availability = availabilityInput;
        return this;
    }

    /**
     * Function to set up a color.
     *
     * @param colorInput : The color to set up.
     * @return : The attitude pointing builder with the given color.
     */
    public AttitudePointingBuilder withColor(final Color colorInput) {
        this.color = colorInput;
        return this;
    }

    /**
     * Function to set up the manager of pointing during the orbit.
     *
     * @param displayOnGroundInput : To use or not the manager.
     * @return : The attitude pointing builder with the manager used or not.
     */
    public AttitudePointingBuilder withDisplayOnGround(final boolean displayOnGroundInput) {
        this.displayOnGround = displayOnGroundInput;
        return this;
    }

    /**
     * Function to set up the pointing trace.
     *
     * @return : The attitude pointing builder with the pointing trace displayed or not.
     */
    public AttitudePointingBuilder displayPointingPath() {
        this.displayPointingPath = true;
        return this;
    }

    /**
     * Function to set up a period of the pointing trace.
     *
     * @return : The attitude pointing builder with a period set up of the pointing trace or not.
     */
    public AttitudePointingBuilder displayPeriodPointingPath() {
        this.displayPeriodPointingPath = true;
        return this;
    }

    /**
     * The build function that generates the attitude pointing object.
     *
     * @return : An attitude pointing object with the given parameters of the builder.
     */
    public AttitudePointing build() {
        final AttitudePointing toReturn = new AttitudePointing(satellite, body, direction, availability, color,
                displayOnGround);
        return this.checkAttributes(toReturn);
    }

    /**
     * This function checks if the pointing trace and the period of the pointing trace are on or not.
     *
     * @param attitudePointingInput : The attitude pointing object build with the build function.
     * @return : An attitude pointing with the pointing trace displayed or not.
     */
    private AttitudePointing checkAttributes(final AttitudePointing attitudePointingInput) {
        if (displayPointingPath) {
            attitudePointingInput.displayPointingPath();
        }
        if (displayPeriodPointingPath) {
            attitudePointingInput.displayPeriodPointingPath();
        }
        return attitudePointingInput;
    }
}
