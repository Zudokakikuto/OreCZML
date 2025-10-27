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

import org.orekit.czml.object.secondary.Clock;

public class InfluenceSphereBuilder {

    /** The default ID. */
    public static final String DEFAULT_ID = "INFLUENCE_SPHERE/";

    /** The default name. */
    public static final String DEFAULT_NAME = "Sphere of influence of :";

    /** The body considered for the sphere of influence. */
    private final Body body;

    /** The clock considered. */
    private Clock clock;

    /** The customID that can be set up. */
    private String customID;

    /**
     * Classic constructor for the builder of the influence sphere.
     *
     * @param clock The clock used for the influence sphere
     * @param bodyInput The body used for the influence sphere
     */
    public InfluenceSphereBuilder(final Body bodyInput, final Clock clock) {
        this.body = bodyInput.cloneObject();
        this.clock = clock.cloneObject();
        this.customID = DEFAULT_ID + bodyInput.getName();
    }

    /**
     * This function set up a custom ID for the sphere of influence.
     *
     * @param customIDInput : The ID to set up
     * @return : The influence sphere builder with a custom ID.
     */
    public InfluenceSphereBuilder withCustomID(final String customIDInput) {
        this.customID = customIDInput;
        return this;
    }

    /**
     * This function builds the sphere of influence with all the parameters
     * given.
     *
     * @return An influence sphere with all the inputs given to the builder
     */
    public InfluenceSphere build() {
        final InfluenceSphere influenceSphere =
            new InfluenceSphere(body, clock);
        if (customID != null) {
            influenceSphere.setId(customID);
        }
        if (clock != null) {
            influenceSphere.setClock(clock);
        }
        return influenceSphere;
    }
}
