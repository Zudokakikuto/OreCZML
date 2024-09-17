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

import org.orekit.czml.object.primary.CovarianceDisplay;
import org.orekit.czml.object.primary.Satellite;
import org.orekit.frames.LOF;
import org.orekit.propagation.StateCovariance;

import java.awt.Color;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

/**
 * Covariance display builder class
 * <p>
 * Builder for the {@link CovarianceDisplay} class.
 * <p>
 *
 * @author Julien LEBLOND
 * @since 1.0.0
 */

public class CovarianceDisplayBuilder {

    /** The default color of the ellipsoid. */
    public static final Color DEFAULT_COLOR = new Color(255, 255, 0, 255);

    /** The local orbital frame of the satellite. */
    private LOF lof;

    /** The satellite used for the covariance. */
    private Satellite satellite;

    /** The list of state covariance used for the propagation of the covariance. */
    private List<StateCovariance> covariances;

    /** The color of the ellipsoid. */
    private Color color = DEFAULT_COLOR;


    // Constructors

    /**
     * The constructor of the covariance display builder.
     *
     * @param satelliteInput   : The satellite around which the covariance is computed.
     * @param covariancesInput : The list of state covariance used to start the propagation of the covariance.
     * @param lofInput         : The local orbital frame of the satellite.
     */
    public CovarianceDisplayBuilder(final Satellite satelliteInput, final List<StateCovariance> covariancesInput,
                                    final LOF lofInput) {
        this.satellite   = satelliteInput;
        this.covariances = covariancesInput;
        this.lof         = lofInput;
    }

    /**
     * Function to set up a color.
     *
     * @param colorInput : The color to set up.
     * @return : The covariance display builder with the given color.
     */
    public CovarianceDisplayBuilder withColor(final Color colorInput) {
        this.color = colorInput;
        return this;
    }

    /**
     * The build function that generates the covariance display object.
     *
     * @return : A covariance display object with the given parameters of the builder.
     */
    public CovarianceDisplay build() throws URISyntaxException, IOException {
        if (satellite != null && !covariances.isEmpty()) {
            return new CovarianceDisplay(satellite, covariances, lof, color);
        }
        return null;
    }
}
