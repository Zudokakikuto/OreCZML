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

import org.orekit.czml.object.primary.CoveredSurfaceOnBody;
import org.orekit.czml.object.primary.FieldOfObservation;
import org.orekit.czml.object.primary.Header;
import org.orekit.czml.object.primary.Satellite;

/**
 * Covered Surface On Body Builder class
 * <p>
 * Builder for the {@link CoveredSurfaceOnBody} class.
 *
 * @author Julien LEBLOND
 * @since 1.0
 */
public class CoveredSurfaceOnBodyBuilder {

    /** The satellite considered for the coverage. */
    private final Satellite satellite;

    /** The field of observation of the satellite. */
    private final FieldOfObservation fieldOfObservation;

    /** The custom id of the covered surface. */
    private String customId;

    /** The header to consider when several are used. */
    private Header header = null;


    /**
     * The constructor of the covered surface on body builder.
     *
     * @param satelliteInput          : The satellite to consider for the coverage.
     * @param fieldOfObservationInput : The field of observation of the satellite.
     * @param headerInput             : The header considered.
     */
    public CoveredSurfaceOnBodyBuilder(final Satellite satelliteInput,
                                       final FieldOfObservation fieldOfObservationInput, final Header headerInput) {
        this.satellite          = satelliteInput;
        this.fieldOfObservation = fieldOfObservationInput;
        this.customId           = "COVERED_SURFACE/" + satelliteInput.getId() + "/" + fieldOfObservationInput.getBody()
                                                                                                             .getBodyFrame()
                                                                                                             .toString();
        this.header = headerInput;
    }

    /**
     * Function to set up a custom ID.
     *
     * @param customIdInput : The custom ID to set up
     * @return : A covered surface on body builder with a custom id.
     */
    public CoveredSurfaceOnBodyBuilder withCustomId(final String customIdInput) {
        this.customId = customIdInput;
        return this;
    }

    /**
     * Function to set up a header.
     *
     * @param headerInput : The header to set up
     * @return : A covered surface on body builder with a header.
     */
    public CoveredSurfaceOnBodyBuilder withHeader(final Header headerInput) {
        this.header = headerInput;
        return this;
    }

    /**
     * Build covered surface on body.
     *
     * @return the covered surface on body
     */
    public CoveredSurfaceOnBody build() {
        return new CoveredSurfaceOnBody(satellite, fieldOfObservation, customId, header);
    }
}
