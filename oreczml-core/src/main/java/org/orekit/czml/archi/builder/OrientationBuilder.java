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

import org.hipparchus.geometry.euclidean.threed.Rotation;
import org.orekit.attitudes.Attitude;
import org.orekit.czml.object.secondary.Orientation;
import org.orekit.frames.Frame;

import java.util.ArrayList;
import java.util.List;

/**
 * Orientation builder class
 * <p>
 * Builder for the {@link Orientation} class.
 * <p>
 *
 * @author Julien LEBLOND
 * @since 1.0.0
 */
public class OrientationBuilder {

    /** If a single attitude is necessary, this argument will be used. */
    private Attitude singleAttitude;

    /** The list of the attitudes of the spacecraft. */
    private List<Attitude> attitudes = new ArrayList<>();

    /** The frame of the object considered. */
    private Frame objectFrame;

    /** The boolean to know whether the orientation should be converted into the ITRF or not. */
    private boolean invertToITRF = true;

    /** Built from a single attitude or not. */
    private boolean singleAttitudeBuilt;

    /** Optional rotation for multiple attitude. */
    private Rotation optionalRotation = Rotation.IDENTITY;


    // Constructors

    /**
     * The constructor of the orientation builder.
     *
     * @param attitude    : The attitude of the object to consider.
     * @param objectFrame : The frame of the object.
     */
    public OrientationBuilder(final Attitude attitude, final Frame objectFrame) {
        this.singleAttitude      = attitude;
        this.objectFrame         = objectFrame;
        this.singleAttitudeBuilt = true;
    }

    /**
     * The constructor of the orientation builder.
     *
     * @param attitudes   : The attitudes of the object to consider.
     * @param objectFrame : The frame of the object.
     */
    public OrientationBuilder(final List<Attitude> attitudes, final Frame objectFrame) {
        this.attitudes           = attitudes;
        this.objectFrame         = objectFrame;
        this.singleAttitudeBuilt = false;
    }


    /**
     * Function to set up the conversion to the ITRF.
     *
     * @param invertToITRFInput : The boolean to convert to the ITRF.
     * @return : The orientation builder with the given conversion.
     */
    public OrientationBuilder withInvertToITRF(final boolean invertToITRFInput) {
        this.invertToITRF = invertToITRFInput;
        return this;
    }

    /**
     * Function to set up an optional rotation.
     *
     * @param optionalRotationInput : The optional rotation to set up.
     * @return : The orientation builder with the given optional rotation.
     */
    public OrientationBuilder withOptionalRotation(final Rotation optionalRotationInput) {
        this.optionalRotation = optionalRotationInput;
        return this;
    }

    /**
     * The build function that generates an orientation object.
     *
     * @return : An orientation object with the given parameters of the builder.
     */
    public Orientation build() {
        if (singleAttitudeBuilt) {
            return new Orientation(singleAttitude, objectFrame, invertToITRF);
        } else {
            return new Orientation(attitudes, objectFrame, invertToITRF, optionalRotation);
        }
    }
}
