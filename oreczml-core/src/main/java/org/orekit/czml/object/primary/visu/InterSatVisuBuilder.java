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

import org.orekit.annotation.DefaultDataContext;
import org.orekit.czml.object.primary.entities.Constellation;
import org.orekit.czml.object.primary.entities.Spacecraft;
import org.orekit.czml.object.secondary.Clock;
import org.orekit.propagation.BoundedPropagator;
import org.orekit.time.AbsoluteDate;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

/**
 * Inter Sat Visu Builder class
 * <p>
 * Builder for the {@link InterSatVisu} class.
 *
 * @author Julien LEBLOND
 * @since 1.0
 */
public class InterSatVisuBuilder {

    /** The default id for the inter sat visu object. */
    public static final String DEFAULT_ID = "INTER_SAT_VISU/";

    /** The first spacecraft to consider for the inter sat visu. */
    private Spacecraft spacecraft1;

    /** The second spacecraft to consider for the inter sat visu. */
    private Spacecraft spacecraft2;

    /** The final date of the propagation. */
    private final AbsoluteDate finalDate;

    /** The constellation if used. */
    private Constellation constellation;

    /** The custom ID of the inter sat visu. */
    private String customId;

    /** The clock to consider when several are used. */
    private Clock clock;

    /**
     * Instantiates a new Inter sat visu builder.
     *
     * @param spacecraft1Input the spacecraft 1 input
     * @param spacecraft2Input the spacecraft 2 input
     * @param finalDateInput the final date input
     * @param clock the clock input
     */
    public InterSatVisuBuilder(final Spacecraft spacecraft1Input,
                               final Spacecraft spacecraft2Input,
                               final AbsoluteDate finalDateInput,
                               final Clock clock) {
        this.spacecraft1 = spacecraft1Input.cloneObject();
        this.spacecraft2 = spacecraft2Input.cloneObject();
        this.finalDate = finalDateInput;
        this.customId =
            DEFAULT_ID +
                        spacecraft1Input.getId() + "/" +
                        spacecraft2Input.getId();
        this.clock = clock.cloneObject();
    }

    /**
     * Instantiates a new Inter sat visu builder.
     *
     * @param propagatorsInput the propagators input
     * @param finalDateInput the final date input
     * @param clock the clock input
     * @throws URISyntaxException the uri syntax exception
     * @throws IOException the io exception
     */
    public InterSatVisuBuilder(final List<BoundedPropagator> propagatorsInput,
                               final AbsoluteDate finalDateInput,
                               final Clock clock)
        throws URISyntaxException,
            IOException {
        this(Constellation.builder(propagatorsInput, finalDateInput, clock)
            .build(), finalDateInput, clock);
    }

    /**
     * Instantiates a new Inter sat visu builder.
     *
     * @param constellationInput the constellation input
     * @param finalDateInput the final date input
     * @param clock the clock input
     */
    public InterSatVisuBuilder(final Constellation constellationInput,
                               final AbsoluteDate finalDateInput,
                               final Clock clock) {
        this.constellation = constellationInput;
        this.finalDate = finalDateInput;
        this.customId = DEFAULT_ID + constellationInput.getId();
        this.clock = clock;
    }

    /**
     * Function to set up a custom ID.
     *
     * @param customIdInput : The custom ID to set up.
     * @return : An inter sat visu builder with the given custom ID.
     */
    public InterSatVisuBuilder withCustomId(final String customIdInput) {
        this.customId = customIdInput;
        return this;
    }

    /**
     * Function to set up a clock.
     *
     * @param clockInput : The clock to set up.
     * @return : An inter sat visu builder with the given clock.
     */
    public InterSatVisuBuilder withClock(final Clock clockInput) {
        this.clock = clockInput.cloneObject();
        return this;
    }

    /**
     * Build inter sat visu.
     *
     * @return the inter sat visu
     */
    @DefaultDataContext
    public InterSatVisu build()
        throws URISyntaxException,
            IOException {
        if (constellation != null) {
            return new InterSatVisu(constellation, finalDate, customId, clock);
        } else {
            return new InterSatVisu(spacecraft1, spacecraft2, finalDate,
                                    customId);
        }
    }
}
