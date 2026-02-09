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
package org.orekit.czml.object.primary.pointing;

import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.orekit.annotation.DefaultDataContext;
import org.orekit.czml.file.AbstractTest;
import org.orekit.czml.object.primary.Header;
import org.orekit.czml.object.primary.entities.Spacecraft;
import org.orekit.czml.object.utils.DateUtils;
import org.orekit.propagation.BoundedPropagator;
import org.orekit.time.AbsoluteDate;

import java.awt.Color;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * The type Attitude pointing test.
 */
@DefaultDataContext
public class AttitudePointingTest
    extends
    AbstractTest {

    /** Initialise orekit data. */
    private final double data = initializeOrekitData();

    /** Header. */
    final Header header = dummyHeader();

    // Dates

    /** Start date. */
    final AbsoluteDate startDate =
        DateUtils.toAbsoluteDate(header.getAvailability().getStart());

    /** Stop Date. */
    final AbsoluteDate stopDate = startDate.shiftedBy(60.0);

    /** Clock multiplier. */
    final double clockMultiplier = 10.0;

    /**
     * Attitude pointing constructor test.
     *
     * @throws URISyntaxException the uri syntax exception
     * @throws IOException the io exception
     */
    @Test
    void AttitudePointingConstructorTest()
        throws URISyntaxException,
            IOException {

        final BoundedPropagator propagator =
            dummyPropagator(startDate, stopDate, dummyOrbit(startDate));

        final Spacecraft spacecraft =
            Spacecraft.builder(propagator, clockMultiplier)
                .withDisplayAttitude().build();

        final AttitudePointing attitudePointing =
            AttitudePointing.builder(spacecraft, getEarth(), Vector3D.MINUS_I,
                                     header.getClock())
                .build();
        attitudePointing.displayPointingPath();
        attitudePointing.displayPeriodPointingPath();

        final String pathFile =
            loadResources("templateFile/object/primary/pointing/attitudepointing/AttitudePointingTemplate.txt");

        verifyFileOutput(pathFile, attitudePointing.toString(), 1e-8);
    }

    @Test
    @DisplayName("Attitude Pointing with builder constructor Test")
    public void AttitudePointingBuilderConstructorTest()
        throws URISyntaxException,
            IOException {

        // Build Spacecraft
        final BoundedPropagator propagator =
            dummyPropagator(startDate, stopDate, dummyOrbit(startDate));
        final Spacecraft spacecraft =
            Spacecraft.builder(propagator, clockMultiplier)
                .withDisplayAttitude().build();

        final AttitudePointing attitudePointingWithBuilder =
            AttitudePointing
                .builder(spacecraft, getEarth(), Vector3D.MINUS_I,
                         header.getClock())
                .withCustomID("CustomID").withDisplayOnGround(false)
                .withColor(Color.ORANGE).build();

        // Reference file
        final String builderPathFile =
            loadResources("templateFile/object/primary/pointing/attitudepointing/AttitudePointingWithBuilderTemplate.txt");

        verifyFileOutput(builderPathFile,
                         attitudePointingWithBuilder.toString(), 1e-8);
    }
}
