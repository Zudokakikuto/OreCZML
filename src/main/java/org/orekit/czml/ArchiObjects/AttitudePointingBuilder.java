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
package org.orekit.czml.ArchiObjects;

import cesiumlanguagewriter.TimeInterval;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.AttitudePointing;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.Header;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.Satellite;

import java.awt.Color;

/**
 * Attitude pointing builder class
 * <p>
 * Builder for the {@link org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.AttitudePointing} class.
 * <p>
 * @author Julien LEBLOND
 * @since 1.0.0
 */

public class AttitudePointingBuilder {

    /** . */
    public static final Color DEFAULT_COLOR = Color.GREEN;

    /** . */
    public static final TimeInterval DEFAULT_AVAILABILITY = Header.getMasterClock()
                                                                  .getAvailability();
    /** . */
    private             Satellite    satellite;

    /** . */
    private OneAxisEllipsoid body;

    /** . */
    private Vector3D direction;


    // Optional arguments
    /** . */
    private TimeInterval availability = DEFAULT_AVAILABILITY;

    /** . */
    private Color color = DEFAULT_COLOR;

    /** . */
    private Boolean displayOnGround = false;

    /** . */
    private Boolean displayPointingPath = false;

    /** . */
    private Boolean displayPeriodPointingPath = false;

    public AttitudePointingBuilder(final Satellite satelliteInput, final OneAxisEllipsoid bodyInput,
                                   final Vector3D directionInput) {
        this.satellite = satelliteInput;
        this.body      = bodyInput;
        this.direction = directionInput;
    }

    public AttitudePointingBuilder withAvailability(final TimeInterval availabilityInput) {
        this.availability = availabilityInput;
        return this;
    }

    public AttitudePointingBuilder withColor(final Color colorInput) {
        this.color = colorInput;
        return this;
    }

    public AttitudePointingBuilder withDisplayOnGround(final boolean displayOnGroundInput) {
        this.displayOnGround = displayOnGroundInput;
        return this;
    }

    public AttitudePointingBuilder displayPointingPath() {
        this.displayPointingPath = true;
        return this;
    }

    public AttitudePointingBuilder displayPeriodPointingPath() {
        this.displayPeriodPointingPath = true;
        return this;
    }

    public AttitudePointing build() {
        final AttitudePointing toReturn = new AttitudePointing(satellite, body, direction, availability, color,
                displayOnGround);
        return this.checkAttributes(toReturn);
    }

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
