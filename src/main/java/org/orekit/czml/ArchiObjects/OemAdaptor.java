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
package org.orekit.czml.ArchiObjects;

import org.orekit.files.ccsds.ndm.odm.oem.Oem;
import org.orekit.propagation.EphemerisGenerator;
import org.orekit.propagation.Propagator;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.events.EventDetector;
import org.orekit.time.AbsoluteDate;

import java.util.Collection;
import java.util.List;

/**
 * Oem adaptor
 * <p>
 * Adaptor for the {@link Oem} class, this helps build satellites from Oems.
 * <p>
 *
 * @author Julien LEBLOND
 * @since 1.0.0
 */

public class OemAdaptor extends AbstractAdaptor<Oem> {

    /** . */
    private Oem oem;

    /** . */
    private Propagator initialPropagator;

    /** . */
    private AbsoluteDate finalDate;

    /** . */
    private AbsoluteDate startDate;

    public OemAdaptor(final Oem oemInput) {
        this.oem = oemInput;
    }

    public Propagator buildPropagator() {
        this.initialPropagator = oem.getSegments()
                                    .get(0)
                                    .getPropagator();
        return initialPropagator;
    }

    public AbsoluteDate buildFinalDate() {
        this.finalDate = oem.getSegments()
                            .get(oem.getSegments()
                                    .size() - 1)
                            .getStop();
        return finalDate;
    }

    public AbsoluteDate buildStartDate() {
        this.startDate = oem.getSegments()
                            .get(0)
                            .getStart();
        return startDate;
    }

    @Override
    public EphemerisGenerator getEphemerisGenerator() {
        return null;
    }

    @Override
    public <T extends EventDetector> void addEventDetector(final T detector) {

    }

    @Override
    public Collection<EventDetector> getEventsDetectors() {
        return List.of();
    }

    @Override
    public void clearEventsDetectors() {

    }

    @Override
    public SpacecraftState propagate(final AbsoluteDate start, final AbsoluteDate target) {
        return null;
    }
}
