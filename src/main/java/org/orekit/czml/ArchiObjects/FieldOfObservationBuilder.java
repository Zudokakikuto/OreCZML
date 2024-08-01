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

import org.hipparchus.util.FastMath;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.FieldOfObservation;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.Satellite;
import org.orekit.frames.FramesFactory;
import org.orekit.frames.Transform;
import org.orekit.geometry.fov.FieldOfView;
import org.orekit.utils.Constants;
import org.orekit.utils.IERSConventions;

import java.awt.Color;

public class FieldOfObservationBuilder {
    /**
     * .
     */
    public static final Color DEFAULT_COLOR = Color.CYAN;
    /**
     * .
     */
    public static final double DEFAULT_ANGULAR_STEP = FastMath.toRadians(36);

    /**
     * .
     */
    private Satellite satellite;
    /**
     * .
     */
    private FieldOfView fieldOfView;
    /**
     * .
     */
    private Transform fovToBody;

    // Optional argument
    /**
     * .
     */
    private Color color = DEFAULT_COLOR;
    /**
     * .
     */
    private OneAxisEllipsoid body = new OneAxisEllipsoid(Constants.WGS84_EARTH_EQUATORIAL_RADIUS,
                                                         Constants.WGS84_EARTH_FLATTENING, FramesFactory.getITRF(IERSConventions.IERS_2010, true));
    /**
     * .
     */
    private double angularStep = DEFAULT_ANGULAR_STEP;

    public FieldOfObservationBuilder(final Satellite satelliteInput, final FieldOfView fieldOfViewInput, final Transform fovToBodyInput) {
        this.satellite = satelliteInput;
        this.fieldOfView = fieldOfViewInput;
        this.fovToBody = fovToBodyInput;
    }

    public FieldOfObservationBuilder withColor(final Color colorInput) {
        this.color = colorInput;
        return this;
    }

    public FieldOfObservationBuilder withBody(final OneAxisEllipsoid bodyInput) {
        this.body = bodyInput;
        return this;
    }

    public FieldOfObservationBuilder withAngularStep(final double angularStepInput) {
        this.angularStep = angularStepInput;
        return this;
    }

    public FieldOfObservation build() {
        return new FieldOfObservation(satellite, fieldOfView, fovToBody, body, angularStep, color);
    }

}
