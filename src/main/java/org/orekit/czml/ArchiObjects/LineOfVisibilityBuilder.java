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

import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.LineOfVisibility;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.Satellite;
import org.orekit.frames.TopocentricFrame;

import java.io.IOException;
import java.net.URISyntaxException;

public class LineOfVisibilityBuilder {

    /**
     * .
     */
    public static final double DEFAULT_ANGLE_OF_APERTURE = 80.0;

    /**
     * .
     */
    private TopocentricFrame topocentricFrame;
    /**
     * .
     */
    private Satellite satellite;

    // Optional parameters
    /**
     * .
     */
    private double angleOfAperture = DEFAULT_ANGLE_OF_APERTURE;

    public LineOfVisibilityBuilder(final TopocentricFrame topocentricFrameInput, final Satellite satelliteInput) {
        this.satellite = satelliteInput;
        this.topocentricFrame = topocentricFrameInput;
    }

    public LineOfVisibilityBuilder withAngleOfAperture(final double angleOfApertureInput) {
        this.angleOfAperture = angleOfApertureInput;
        return this;
    }

    public LineOfVisibility build() throws URISyntaxException, IOException {
        return new LineOfVisibility(topocentricFrame, satellite, angleOfAperture);
    }

}

