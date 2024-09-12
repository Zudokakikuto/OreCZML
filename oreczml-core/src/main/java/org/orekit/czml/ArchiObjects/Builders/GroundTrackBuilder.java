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
package org.orekit.czml.ArchiObjects.Builders;

import org.orekit.bodies.BodyShape;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.Constellation;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.GroundTrack;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.Satellite;

import java.awt.Color;

public class GroundTrackBuilder {


    /** The default color of the ground track on the body. */
    public static final Color DEFAULT_COLOR = new Color(255, 255, 255);

    /** The satellite that will project the ground track on the ground. */
    private Satellite satellite;

    /** The body where the ground track will be projected on. */
    private BodyShape body;

    /** The constellation that will project the ground track on the ground. */
    private Constellation constellation;

    /** The color of the ground track. */
    private Color color = DEFAULT_COLOR;

    // Constructor

    /**
     * The constructor for the ground track builder using a single satellite.
     *
     * @param satellite : The satellite that will project the ground track on the ground.
     * @param body      : The body where the ground track will be projected on.
     */
    public GroundTrackBuilder(final Satellite satellite, final BodyShape body) {
        this.satellite = satellite;
        this.body      = body;
    }

    /**
     * The constructor for the ground track builder using a constellation.
     *
     * @param constellation : The constellation that will project the ground track on the ground.
     * @param body          : The body where the ground track will be projected on.
     */
    public GroundTrackBuilder(final Constellation constellation, final BodyShape body) {
        this.constellation = constellation;
        this.body          = body;
    }

    /**
     * Function to set up the color of the ground track.
     *
     * @param colorInput : The color to set up.
     * @return : The ground track builder with a given color.
     */
    public GroundTrackBuilder withColor(final Color colorInput) {
        this.color = colorInput;
        return this;
    }

    public GroundTrack build() {
        if (satellite != null) {
            return new GroundTrack(satellite, body, color);
        } else if (constellation != null) {
            return new GroundTrack(constellation, body, color);
        } else {
            return null;
        }
    }
}
