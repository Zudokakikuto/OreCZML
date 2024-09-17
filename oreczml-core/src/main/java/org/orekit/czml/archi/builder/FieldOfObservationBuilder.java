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

import org.hipparchus.util.FastMath;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.czml.object.primary.FieldOfObservation;
import org.orekit.czml.object.primary.Satellite;
import org.orekit.frames.FramesFactory;
import org.orekit.frames.Transform;
import org.orekit.geometry.fov.FieldOfView;
import org.orekit.utils.Constants;
import org.orekit.utils.IERSConventions;

import java.awt.Color;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Field of observation builder class
 * <p>
 * Builder for the {@link FieldOfObservation} class.
 * <p>
 *
 * @author Julien LEBLOND
 * @since 1.0.0
 */

public class FieldOfObservationBuilder {

    /** The default color of the lines projected to the ground. */
    public static final Color DEFAULT_COLOR = Color.CYAN;

    /** The default angular step between each point projected on the ground. */
    public static final double DEFAULT_ANGULAR_STEP = FastMath.toRadians(36);

    /** The satellite that is observing the body. */
    private Satellite satellite;

    /** The field of view of the satellite. */
    private FieldOfView fieldOfView;

    /**
     * The transform that converts the frame of the fov to the frame of the body.
     * Check the tutorial 'FieldOfObservationSatellite'.
     */
    private Transform fovToBody;

    // Optional argument
    /** The color of the ellipsoid. */
    private Color color = DEFAULT_COLOR;

    /** The body around which the satellite is orbiting. */
    private OneAxisEllipsoid body = new OneAxisEllipsoid(Constants.WGS84_EARTH_EQUATORIAL_RADIUS,
            Constants.WGS84_EARTH_FLATTENING, FramesFactory.getITRF(IERSConventions.IERS_2010, true));

    /** The angular step between each point of the projected points. */
    private double angularStep = DEFAULT_ANGULAR_STEP;


    // Constructor

    /**
     * The constructor of the field of observation builder object.
     *
     * @param satelliteInput   : The satellite that is observing the body.
     * @param fieldOfViewInput : The field of view of the satellite.
     * @param fovToBodyInput   : The transform between the frame of the fov to the frame of the body.
     */
    public FieldOfObservationBuilder(final Satellite satelliteInput, final FieldOfView fieldOfViewInput,
                                     final Transform fovToBodyInput) {
        this.satellite   = satelliteInput;
        this.fieldOfView = fieldOfViewInput;
        this.fovToBody   = fovToBodyInput;
    }

    /**
     * Function to set up a color.
     *
     * @param colorInput : The color to set up.
     * @return : The field of observation builder with the given color.
     */
    public FieldOfObservationBuilder withColor(final Color colorInput) {
        this.color = colorInput;
        return this;
    }

    /**
     * Function to set up a body.
     *
     * @param bodyInput : The body to set up.
     * @return : The field of observation builder with the given body.
     */
    public FieldOfObservationBuilder withBody(final OneAxisEllipsoid bodyInput) {
        this.body = bodyInput;
        return this;
    }

    /**
     * Function to set up an angular step.
     *
     * @param angularStepInput : The angular step to set up.
     * @return : The field of observation builder with the given angular step.
     */
    public FieldOfObservationBuilder withAngularStep(final double angularStepInput) {
        this.angularStep = angularStepInput;
        return this;
    }

    /**
     * The build function that generates a field of observation object.
     *
     * @return : A field of observation object with the given parameters of the builder.
     */
    public FieldOfObservation build() throws URISyntaxException, IOException {
        return new FieldOfObservation(satellite, fieldOfView, fovToBody, body, angularStep, color);
    }

}
