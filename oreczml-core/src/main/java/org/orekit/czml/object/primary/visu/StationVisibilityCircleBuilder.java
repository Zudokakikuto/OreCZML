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

import org.orekit.czml.object.primary.entities.Spacecraft;
import org.orekit.czml.object.secondary.Clock;
import org.orekit.frames.TopocentricFrame;

public class StationVisibilityCircleBuilder {

    /** The default angle of aperture of the station. */
    public static final double DEFAULT_ANGLE_OF_APERTURE = 90.0;

    /** The topocentric frame representing the station. */
    private final TopocentricFrame topocentricFrame;

    /** The satellite observed. */
    private final Spacecraft satellite;

    /** The angle of aperture of the station. */
    private double angleOfAperture = DEFAULT_ANGLE_OF_APERTURE;

    /** The clock considered. */
    private Clock clock;

    /**
     * The default constructor for the station visibility circle builder.
     *
     * @param topocentricFrameInput : The topocentric frame representing the
     *        ground station.
     * @param satelliteInput : The satellite observed.
     * @param clock : The clock considered.
     */
    public StationVisibilityCircleBuilder(final TopocentricFrame topocentricFrameInput,
                                          final Spacecraft satelliteInput,
                                          final Clock clock) {
        this.topocentricFrame = topocentricFrameInput;
        this.satellite = satelliteInput;
        this.clock = clock;
    }

    /**
     * This functions sets an angle of aperture.
     *
     * @param angleOfApertureInput : The angle of aperture to set.
     * @return : The station visibility circle builder with an angle of aperture
     *         set.
     */
    public StationVisibilityCircleBuilder
        withAngleOfAperture(final double angleOfApertureInput) {
        this.angleOfAperture = angleOfApertureInput;
        return this;
    }

    /**
     * This function builds the visibility circle.
     *
     * @return A station visibility circle with the given input to the builder
     */
    public StationVisibilityCircle build() {
        return new StationVisibilityCircle(topocentricFrame, satellite,
                                           angleOfAperture, clock);
    }

}
