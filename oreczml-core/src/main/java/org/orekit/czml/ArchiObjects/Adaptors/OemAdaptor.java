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
package org.orekit.czml.ArchiObjects.Adaptors;

import org.orekit.files.ccsds.ndm.odm.oem.Oem;
import org.orekit.propagation.Propagator;
import org.orekit.time.AbsoluteDate;

/**
 * Oem adaptor
 * <p>
 * Adaptor for the {@link Oem} class, this helps build satellites from Oems. With all the parameters available in this class
 * a satellite object can be built.
 * <p>
 *
 * @author Julien LEBLOND
 * @since 1.0.0
 */

public class OemAdaptor {

    /** The oem Orekit object. */
    private Oem oem;

    /** The initial propagator of the oem. */
    private Propagator initialPropagator;

    /** The final date of the propagation. */
    private AbsoluteDate finalDate;

    /** The start date of the propagation. */
    private AbsoluteDate startDate;

    // Constructor

    /**
     * The constructor of the adaptor.
     *
     * @param oemInput : The oem Orekit object that will need to be converted.
     */
    public OemAdaptor(final Oem oemInput) {
        this.oem = oemInput;
    }

    /**
     * This function builds the propagator from the oem.
     *
     * @return : A propagator extracted from the oem.
     */
    public Propagator buildPropagator() {
        this.initialPropagator = oem.getSegments()
                                    .get(0)
                                    .getPropagator();
        return initialPropagator;
    }

    /**
     * This function builds the final date from the oem.
     *
     * @return : A final date extracted from the oem.
     */
    public AbsoluteDate buildFinalDate() {
        this.finalDate = oem.getSegments()
                            .get(oem.getSegments()
                                    .size() - 1)
                            .getStop();
        return finalDate;
    }

    /**
     * This function builds the start date from the oem.
     *
     * @return : A start date extracted from the oem.
     */
    public AbsoluteDate buildStartDate() {
        this.startDate = oem.getSegments()
                            .get(0)
                            .getStart();
        return startDate;
    }
}
