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

import org.orekit.czml.object.primary.Header;
import org.orekit.czml.object.primary.LatLongLines;

/**
 * Lat Long Lines Builder class
 * <p>
 * Builder for the {@link LatLongLines} class.
 *
 * @author Julien LEBLOND
 * @since 1.0
 */
public class LatLongLinesBuilder {

    /** The angular step of the latitude. */
    private int latitudeAngularStep = 15;

    /** The angular step of the longitude. */
    private int longitudeAngularStep = 15;

    /** If the labels should be displayed or not. */
    private boolean displayLabels = false;

    /** The custom ID of the lat long lines display object. */
    private String customID;

    /** The header considered when several are used. */
    private Header header;


    /**
     * The builder of the lat long line display builder object.
     *
     * @param headerInput : The header considered.
     */
    public LatLongLinesBuilder(final Header headerInput) {
        this.customID = "LAT_LONG_DISP";
        this.header   = headerInput;
    }

    /**
     * The function to set up a custom ID.
     *
     * @param customIDInput : The custom ID to set up
     * @return : The lat long lines display builder object with a given custom ID.
     */
    public LatLongLinesBuilder withCustomID(final String customIDInput) {
        this.customID = customIDInput;
        return this;
    }

    /**
     * The function to set up a header.
     *
     * @param headerInput : The header to set up
     * @return : The lat long lines display builder object with a given header.
     */
    public LatLongLinesBuilder withHeader(final Header headerInput) {
        this.header = headerInput;
        return this;
    }

    /**
     * The function to set up an angular step for the latitude.
     *
     * @param latitudeAngularStepInput : The angular step for the latitude to set up
     * @return : The lat long lines display builder object with a given angular step.
     */
    public LatLongLinesBuilder withLatitudeAngularStep(final int latitudeAngularStepInput) {
        this.latitudeAngularStep = latitudeAngularStepInput;
        return this;
    }

    /**
     * The function to set up an angular step for the longitude.
     *
     * @param longitudeAngularStepInput : The angular step for the longitude to set up
     * @return : The lat long lines display builder object with a given angular step.
     */
    public LatLongLinesBuilder withLongitudeAngularStep(final int longitudeAngularStepInput) {
        this.longitudeAngularStep = longitudeAngularStepInput;
        return this;
    }

    /**
     * The function to show or not the labels.
     *
     * @param displayLabelsInput : The boolean to show or not the labels to set up
     * @return : The lat long lines display builder object with labels displayed or not.
     */
    public LatLongLinesBuilder withDisplay(final boolean displayLabelsInput) {
        this.displayLabels = displayLabelsInput;
        return this;
    }

    /**
     * The build function that generates a lat long lines display object.
     *
     * @return : A lat long lines display object with the given parameters of the builder.
     */
    public LatLongLines build() {
        return new LatLongLines(latitudeAngularStep, longitudeAngularStep, displayLabels, customID, header);
    }

}
