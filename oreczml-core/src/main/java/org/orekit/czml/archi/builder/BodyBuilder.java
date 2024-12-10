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

import org.orekit.bodies.CelestialBody;
import org.orekit.czml.object.primary.Body;
import org.orekit.czml.object.primary.Header;

/**
 * body builder class
 * <p>
 * Builder for the {@link Body} class.
 *
 * @author Julien LEBLOND
 * @since 1.0
 */
public class BodyBuilder {

    /** . */
    private final CelestialBody body;

    /** The path to the model to load. */
    private final String pathToModel;

    /** . */
    private String customId;

    /** The header to use if several are used. */
    private Header header;

    /**
     * The body builder constructor.
     *
     * @param bodyInput        : The body to consider
     * @param pathToModelInput : The model to load
     * @param headerInput      : The header considered.
     */
    public BodyBuilder(final CelestialBody bodyInput, final String pathToModelInput, final Header headerInput) {
        this.body        = bodyInput;
        this.pathToModel = pathToModelInput;
        this.customId    = "BODY/" + bodyInput.getName();
        this.header      = headerInput;
    }

    /**
     * Function to set up a custom ID.
     *
     * @param customIDInput : The custom ID to set up
     * @return : The builder with a custom ID set up.
     */
    public BodyBuilder withCustomID(final String customIDInput) {
        this.customId = customIDInput;
        return this;
    }

    /**
     * Function to set up a header when several are used.
     *
     * @param headerInput : The header to set up.
     * @return : The builder with a header set up.
     */
    public BodyBuilder withHeader(final Header headerInput) {
        this.header = headerInput;
        return this;
    }

    /**
     * The build function that generates the body object.
     *
     * @return : A body object with the given parameters of the builder.
     */
    public Body build() {
        return new Body(body, pathToModel, customId, header);
    }
}
