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


import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.CovarianceDisplay;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.Satellite;
import org.orekit.files.ccsds.ndm.odm.oem.Oem;
import org.orekit.frames.LOF;
import org.orekit.propagation.StateCovariance;

import java.awt.Color;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public class CovarianceDisplayBuilder {
    /** . */
    public static final Color DEFAULT_COLOR = new Color(255, 255, 0, 255);

    /** . */
    private LOF lof;

    /** . */
    private Satellite satellite;

    /** . */
    private List<StateCovariance> covariances;

    /** . */
    private Color color = DEFAULT_COLOR;

    /** . */
    private Oem oem;

    public CovarianceDisplayBuilder(final Satellite satelliteInput, final List<StateCovariance> covariancesInput,
                                    final LOF lofInput) {
        this.satellite   = satelliteInput;
        this.covariances = covariancesInput;
        this.lof         = lofInput;
    }

    public CovarianceDisplayBuilder withColor(final Color colorInput) {
        this.color = colorInput;
        return this;
    }

    public CovarianceDisplay build() throws URISyntaxException, IOException {
        if (satellite != null && !covariances.isEmpty()) {
            return new CovarianceDisplay(satellite, covariances, lof, color);
        }
        return null;
    }
}
