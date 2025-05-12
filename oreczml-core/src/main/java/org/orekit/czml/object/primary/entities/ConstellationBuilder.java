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

package org.orekit.czml.object.primary.entities;

import org.orekit.czml.object.primary.Header;
import org.orekit.propagation.BoundedPropagator;
import org.orekit.time.AbsoluteDate;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Constellation Builder class
 * <p>
 * Builder for the {@link Constellation} class.
 *
 * @author Julien LEBLOND
 * @since 1.0
 */
public class ConstellationBuilder {

    /** Default Id of the constellation. */
    public static final String DEFAULT_ID = "Constellation/";

    /**
     * A default string to give the number of satellites in the constellation.
     */
    public static final String DEFAULT_NUMBER_OF_SAT = " satellites ";

    /**
     * List of propagators representing the satellites.
     */
    private final List<BoundedPropagator> propagators;

    /**
     * The final date of the propagation.
     */
    private final AbsoluteDate finalDate;

    /**
     * Path to the model.
     */
    private String pathToModel = Constellation.DEFAULT_STRING_MODEL;

    /**
     * The custom ID for the constellation.
     */
    private String customID = Constellation.DEFAULT_STRING_MODEL;

    /** The header to use when several are used. */
    private Header header;

    /** Boolean to display or not the attitude of satellites. */
    private boolean displayAttitude;

    /** Boolean to know is only one period should be displayed or not. */
    private boolean displayOnlyOnePeriod;

    /**
     * The default builder for the constellation builder.
     *
     * @param propagatorsInput : All the propagators that represent the
     *        satellites.
     * @param finalDateInput : The final date of the propagation.
     * @param headerInput : The header considered.
     */
    public ConstellationBuilder(final List<BoundedPropagator> propagatorsInput,
                                final AbsoluteDate finalDateInput,
                                final Header headerInput) {
        this.propagators = new ArrayList<>(propagatorsInput);
        this.finalDate = finalDateInput;
        this.header = headerInput;
        this.customID =
            DEFAULT_ID + propagatorsInput.size() + " " + DEFAULT_NUMBER_OF_SAT;
    }

    /**
     * Function to set up a custom model.
     *
     * @param pathToModelInput : The path to the model.
     * @return : The constellation builder with a custom model.
     */
    public ConstellationBuilder withModel(final String pathToModelInput) {
        this.pathToModel = pathToModelInput;
        return this;
    }

    /**
     * Function to set up a custom ID.
     *
     * @param customIdInput : The custom ID to set up.
     * @return : The constellation builder with a custom ID.
     */
    public ConstellationBuilder withCustomId(final String customIdInput) {
        this.customID = customIdInput;
        return this;
    }

    /**
     * Function to set up a header.
     *
     * @param headerInput : The header to set up.
     * @return : The constellation builder with a custom ID.
     */
    public ConstellationBuilder withHeader(final Header headerInput) {
        this.header = headerInput;
        return this;
    }

    public ConstellationBuilder displayAttitude() {
        this.displayAttitude = true;
        return this;
    }

    public ConstellationBuilder displayOnlyOnePeriod() {
        this.displayOnlyOnePeriod = true;
        return this;
    }

    /**
     * The build function that generates a constellation object.
     *
     * @return : A constellation object with the given parameters of the
     *         builder.
     * @throws URISyntaxException the uri syntax exception
     * @throws IOException the io exception
     */
    public Constellation build()
        throws URISyntaxException,
            IOException {
        final Constellation tempConstellation =
            new Constellation(propagators, finalDate, pathToModel, customID,
                              header);
        return checkAttributes(tempConstellation);
    }

    private Constellation
        checkAttributes(final Constellation constellationInput) {
        if (displayAttitude) {
            constellationInput.displayAttitude();
        }
        if (displayOnlyOnePeriod) {
            constellationInput.displayOnlyOnePeriod();
        }
        return constellationInput;
    }
}
