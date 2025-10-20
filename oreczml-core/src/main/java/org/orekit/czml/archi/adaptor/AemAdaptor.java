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
package org.orekit.czml.archi.adaptor;

import org.hipparchus.geometry.euclidean.threed.Rotation;
import org.orekit.czml.object.secondary.Clock;
import org.orekit.czml.object.secondary.Orientation;
import org.orekit.files.ccsds.ndm.adm.aem.Aem;
import org.orekit.propagation.BoundedPropagator;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Aem adaptor
 * <p>
 * Adaptor for the {@link Aem} class, this helps build orientation objects from
 * those files.
 *
 * @param aem The aem to be used to create an orientation.
 * @author Julien LEBLOND
 * @since 1.0.0
 */
public record AemAdaptor(Aem aem) {

    /**
     * The constructor of the adaptor.
     *
     * @param aem : The aem to input.
    */
    public AemAdaptor { }

    /**
     * Build orientation orientation.
     *
     * @param propagator the propagator
     * @param clock the clock
     * @return the orientation
     * @throws URISyntaxException the uri syntax exception
     * @throws IOException the io exception
     */
    public Orientation buildOrientation(final BoundedPropagator propagator, final Clock clock) throws URISyntaxException, IOException {
        return new Orientation(aem.getSegments().get(0).getAttitudeProvider(), propagator, clock, Rotation.IDENTITY, false);
    }

    /**
     * Gets aem.
     *
     * @return the aem
     */
    @Override public Aem aem() {
        return aem;
    }
}
