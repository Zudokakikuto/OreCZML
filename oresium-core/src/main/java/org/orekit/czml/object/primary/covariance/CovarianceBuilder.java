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
package org.orekit.czml.object.primary.covariance;

import org.orekit.czml.object.primary.entities.Spacecraft;
import org.orekit.frames.LOF;
import org.orekit.propagation.StateCovariance;

import java.awt.Color;
import java.util.List;

/**
 * Covariance display builder class
 * <p>
 * Builder for the {@link Covariance} class.
 *
 * @author LEBLOND Julien
 * @since 1.0.0
 */
public class CovarianceBuilder {

    /**
     * The default color of the ellipsoid.
     */
    public static final Color DEFAULT_COLOR = new Color(255, 255, 0, 255);

    /**
     * The local orbital frame of the satellite.
     */
    private final LOF lof;

    /**
     * The satellite used for the covariance.
     */
    private final Spacecraft satellite;

    /** The custom ID for the covariance display. */
    private String customID;

    /**
     * The list of state covariance used for the propagation of the covariance.
     */
    private final List<StateCovariance> covariances;

    /**
     * The color of the ellipsoid.
     */
    private Color color = DEFAULT_COLOR;

    // Constructors

    /**
     * The constructor of the covariance display builder.
     *
     * @param satelliteInput : The satellite around which the covariance is
     *        computed.
     * @param covariancesInput : The list of state covariance used to start the
     *        propagation of the covariance.
     * @param lofInput : The local orbital frame of the satellite.
     */
    public CovarianceBuilder(final Spacecraft satelliteInput,
                             final List<StateCovariance> covariancesInput,
                             final LOF lofInput) {
        this.satellite = satelliteInput;
        this.covariances = covariancesInput;
        this.lof = lofInput;
        this.customID = Covariance.DEFAULT_ID + satelliteInput.getId();
    }

    /**
     * Function to set up a color.
     *
     * @param colorInput : The color to set up.
     * @return : The covariance display builder with the given color.
     */
    public CovarianceBuilder withColor(final Color colorInput) {
        this.color = colorInput;
        return this;
    }

    /**
     * Function to set up a custom ID.
     *
     * @param customIDInput : The custom ID to set up.
     * @return : The covariance display object with a custom ID.
     */
    public CovarianceBuilder withCustomID(final String customIDInput) {
        this.customID = customIDInput;
        return this;
    }

    /**
     * The build function that generates the covariance display object.
     *
     * @return : A covariance display object with the given parameters of the
     *         builder. *
     */
    public Covariance build() {
        if (this.satellite != null && !this.covariances.isEmpty()) {
            return new Covariance(satellite, covariances, lof, color, customID);
        }
        return null;
    }
}
