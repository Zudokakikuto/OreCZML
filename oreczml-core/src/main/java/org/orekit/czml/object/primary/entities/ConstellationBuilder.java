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

package org.orekit.czml.object.primary.entities;

import cesiumlanguagewriter.TimeInterval;
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
    private List<String> pathToModel = new ArrayList<>();

    /**
     * The custom ID for the constellation.
     */
    private String customID;

    /** The availability of the constellation. */
    private TimeInterval availability;

    /**  */
    private double clockMultiplier;

    /** Boolean to display or not the attitude of satellites. */
    private boolean displayAttitude;

    /** Boolean to know is only one period should be displayed or not. */
    private boolean displayOnlyOnePeriod;

    /**
     * The default builder for the constellation builder.
     *
     * @param propagatorsInput : All the propagators that represent the satellites.
     * @param finalDateInput   : The final date of the propagation.
     * @param availability     : The availability of the constellation.
     * @param clockMultiplier  : The clock considered.
     */
    public ConstellationBuilder(final List<BoundedPropagator> propagatorsInput, final AbsoluteDate finalDateInput, final TimeInterval availability,
                                final double clockMultiplier) {
        this.propagators     = new ArrayList<>(propagatorsInput);
        this.finalDate       = finalDateInput;
        this.availability    = availability;
        this.clockMultiplier = clockMultiplier;
        this.customID        = DEFAULT_ID + propagatorsInput.size() + " " + DEFAULT_NUMBER_OF_SAT;
        this.pathToModel.add("");
    }

    /**
     * Function to set up one or several custom models.
     *
     * @param pathToModelsInput : The list of paths to the models.
     * @return : The constellation builder with custom models.
     */
    public ConstellationBuilder withModel(final List<String> pathToModelsInput) {
        this.pathToModel.clear();
        this.pathToModel = pathToModelsInput;
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
     * Function to set a time interval.
     *
     * @param availabilityInput : The availability to set up.
     * @return : The constellation builder with a custom availability.
     */
    public ConstellationBuilder withTimeInterval(final TimeInterval availabilityInput) {
        this.availability = availabilityInput;
        return this;
    }

    /**
     * Function to set up a clock multiplier.
     *
     * @param clockMultiplierInput : The multiplier to set up.
     * @return : The constellation builder with a custom ID.
     */
    public ConstellationBuilder withClockMultiplier(final double clockMultiplierInput) {
        this.clockMultiplier = clockMultiplierInput;
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
     * @return : A constellation object with the given parameters of the builder.
     * @throws URISyntaxException the uri syntax exception
     * @throws IOException        the io exception
     */
    public Constellation build()
                    throws
                    URISyntaxException,
                    IOException {
        final Constellation tempConstellation = new Constellation(propagators, finalDate, pathToModel, customID, availability, clockMultiplier);
        return checkAttributes(tempConstellation);
    }

    private Constellation checkAttributes(final Constellation constellationInput) {
        if (displayAttitude) {
            constellationInput.displayAttitude();
        }
        if (displayOnlyOnePeriod) {
            constellationInput.displayOnlyOnePeriod();
        }
        return constellationInput;
    }
}
