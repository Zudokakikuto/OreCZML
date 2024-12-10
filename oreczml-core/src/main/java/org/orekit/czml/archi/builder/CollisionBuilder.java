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

import org.orekit.czml.object.primary.Collision;
import org.orekit.czml.object.primary.Header;
import org.orekit.czml.object.primary.Satellite;
import org.orekit.frames.LOF;
import org.orekit.propagation.StateCovariance;

import java.util.List;

/**
 * collision builder class
 * <p>
 * Builder for the {@link Collision} class.
 *
 * @author Julien LEBLOND
 * @since 1.0
 */
public class CollisionBuilder {

    /** The first satellite to consider. */
    private final Satellite firstSatellite;

    /** The second satellite to consider. */
    private final Satellite secondSatellite;

    /** The list of state covariances of the first satellite. */
    private final List<StateCovariance> firstCovarianceList;

    /** The list of state covariances of the second satellite. */
    private final List<StateCovariance> secondCovarianceList;

    /** The local orbital frame of the first satellite. */
    private final LOF firstLof;

    /** The local orbital frame of the second satellite. */
    private final LOF secondLof;

    /** The custom ID of the collision object. */
    private String customId;

    /** The header to use when several headers are used. */
    private Header header;

    /**
     * The constructor for the collision builder object.
     *
     * @param firstSatelliteInput       : The first satellite considered in the collision probability.
     * @param secondSatelliteInput      : The second satellite considered in the collision probability.
     * @param firstCovarianceListInput  : The list of initial states covariances of the first satellite.
     * @param secondCovarianceListInput : The list of initial states covariances of the second satellite.
     * @param firstLofInput             : The local orbital frame of the first satellite.
     * @param secondLofInput            : The local orbital frame of the second satellite.
     * @param header                    : The header considered.
     */
    public CollisionBuilder(final Satellite firstSatelliteInput, final Satellite secondSatelliteInput,
                            final List<StateCovariance> firstCovarianceListInput,
                            final List<StateCovariance> secondCovarianceListInput, final LOF firstLofInput,
                            final LOF secondLofInput, final Header header) {
        this.firstSatellite       = firstSatelliteInput;
        this.secondSatellite      = secondSatelliteInput;
        this.firstCovarianceList  = firstCovarianceListInput;
        this.secondCovarianceList = secondCovarianceListInput;
        this.firstLof             = firstLofInput;
        this.secondLof            = secondLofInput;
        this.customId             = "COLLISION/" + firstSatelliteInput.getId() + "/" + secondSatelliteInput.getId();
        this.header               = header;
    }

    /**
     * Function to set up a custom ID.
     *
     * @param customIdInput : The custom ID to set up
     * @return : The collision builder object with a custom id set up.
     */
    public CollisionBuilder withCustomId(final String customIdInput) {
        this.customId = customIdInput;
        return this;
    }

    /**
     * Function to set up a header when several are used.
     *
     * @param headerInput : The header to set up
     * @return : The collision builder object with a header set up.
     */
    public CollisionBuilder withHeader(final Header headerInput) {
        this.header = headerInput;
        return this;
    }

    /**
     * Build collision.
     *
     * @return the collision
     */
    public Collision build() {
        return new Collision(firstSatellite, secondSatellite, firstCovarianceList, secondCovarianceList,
                firstLof, secondLof, customId, header);
    }
}
