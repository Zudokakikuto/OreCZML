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

import org.junit.jupiter.api.Test;
import org.orekit.annotation.DefaultDataContext;
import org.orekit.bodies.CelestialBodyFactory;
import org.orekit.czml.file.AbstractTest;
import org.orekit.czml.object.primary.Header;
import org.orekit.czml.object.secondary.Clock;
import org.orekit.czml.object.utils.DateUtils;
import org.orekit.frames.Frame;
import org.orekit.time.AbsoluteDate;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * The type Influence sphere test.
 */
@DefaultDataContext
public class InfluenceSphereTest
    extends
    AbstractTest {

    /** Initialise orekit data. */
    private final double data = initializeOrekitData();

    /** Header. */
    final Header header = dummyHeader();

    /** Start Date. */
    final AbsoluteDate startDate =
        DateUtils.toAbsoluteDate(header.getAvailability().getStart());

    /** Stop Date. */
    final AbsoluteDate stopDate =
        DateUtils.toAbsoluteDate(header.getAvailability().getStop());

    /** Clock. */
    final Clock clock = new Clock(startDate, stopDate, 10.0);

    /**
     * Body constructor test.
     *
     * @throws URISyntaxException the uri syntax exception
     * @throws IOException the io exception
     */
    @Test
    void InfluenceSphereConstructorTest()
        throws URISyntaxException,
            IOException {

        // Building of the body for the influence sphere
        final String pathToEarthModel = loadResources("Bodies/earth.glb");
        final String pathToSunModel = loadResources("Bodies/sun.glb");

        // Build of the sun frame
        final Frame sunFrame =
            CelestialBodyFactory.getSun().getBodyOrientedFrame();
        final Body sun =
            Body.builder(CelestialBodyFactory.getSun(), pathToSunModel,
                         sunFrame, clock, null)
                .build();

        // Build of the earth
        final Body earth =
            Body.builder(CelestialBodyFactory.getEarth(), pathToEarthModel,
                         sunFrame, clock, sun)
                .build();

        // Build of the influence sphere of the earth
        final InfluenceSphere influenceSphereWithCentralBody =
            InfluenceSphere.builder(earth, clock).build();

        // Reference frame
        final String influenceSphereWithCentralBodyTemplate =
            loadResources("templateFile/object/primary/entities/influencesphere/InfluenceSphereWithCentralBodyTemplate.txt");

        verifyFileOutput(influenceSphereWithCentralBodyTemplate,
                         influenceSphereWithCentralBody.toString(), 1e-8);
    }
}
