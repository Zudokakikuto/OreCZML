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

import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.Satellite;
import org.orekit.propagation.BoundedPropagator;
import org.orekit.time.AbsoluteDate;

import java.awt.Color;
import java.io.IOException;
import java.net.URISyntaxException;

public class SatelliteBuilder {

    /**
     * .
     */
    public static final String DEFAULT_MODEL_PATH = "";
    /**
     * .
     */
    public static final Color DEFAULT_COLOR = new Color(255, 255, 255);

    // Optional parameters
    /**
     * .
     */
    private final BoundedPropagator propagator;
    /**
     * .
     */
    private final AbsoluteDate finalDate;
    /**
     * .
     */
    private AbsoluteDate startDate;
    /**
     * .
     */
    private String modelPath = DEFAULT_MODEL_PATH;
    /**
     * .
     */
    private Color color = DEFAULT_COLOR;
    /**
     * .
     */
    private boolean displayOnlyOnePeriod = false;

    // Intrinsic parameters
    /**
     * .
     */
    private boolean displayAttitude = false;
    /**
     * .
     */
    private boolean displayReferenceSystem = false;

    public SatelliteBuilder(final BoundedPropagator propagator, final AbsoluteDate finalDate) {
        this.propagator = propagator;
        this.finalDate = finalDate;
        this.startDate = propagator.getInitialState()
                                   .getDate();
    }

    public SatelliteBuilder withModelPath(final String modelPathInput) throws URISyntaxException, IOException {
        this.modelPath = modelPathInput;
        return this;
    }

    public SatelliteBuilder withColor(final Color colorInput) {
        this.color = colorInput;
        return this;
    }

    public SatelliteBuilder withStartDate(final AbsoluteDate startDateInput) {
        this.startDate = startDateInput;
        return this;
    }

    public SatelliteBuilder displayOnlyOnePeriod() {
        displayOnlyOnePeriod = true;
        return this;
    }

    public SatelliteBuilder displayAttitude() {
        displayAttitude = true;
        return this;
    }

    public SatelliteBuilder displayReferenceSystem() {
        displayReferenceSystem = true;
        return this;
    }

    public Satellite build() throws URISyntaxException, IOException {
        final Satellite tempSatellite = new Satellite(propagator, startDate, finalDate, modelPath, color);
        return this.checkAttributes(tempSatellite);
    }

    private Satellite checkAttributes(final Satellite satellite) throws URISyntaxException, IOException {
        if (displayOnlyOnePeriod) {
            satellite.displayOnlyOnePeriod();
        }
        if (displayAttitude) {
            satellite.displaySatelliteAttitude();
        }
        if (displayReferenceSystem) {
            satellite.displaySatelliteReferenceSystem();
        }
        return satellite;
    }
}
