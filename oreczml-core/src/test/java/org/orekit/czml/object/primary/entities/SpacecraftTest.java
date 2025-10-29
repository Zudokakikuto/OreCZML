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

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.orekit.annotation.DefaultDataContext;
import org.orekit.czml.archi.factory.BodyFactory;
import org.orekit.czml.file.AbstractTest;
import org.orekit.czml.object.primary.Header;
import org.orekit.czml.object.secondary.Clock;
import org.orekit.czml.object.utils.DateUtils;
import org.orekit.propagation.BoundedPropagator;
import org.orekit.time.AbsoluteDate;

/**
 * The type Satellite test.
 */
@DefaultDataContext
public class SpacecraftTest
    extends
    AbstractTest {

    /**
     * Satellite constructor test.
     *
     * @throws IOException the io exception
     * @throws URISyntaxException the uri syntax exception
     */
    @Test
    void SatelliteConstructorTest()
        throws IOException,
            URISyntaxException {

        loadOrekitData();

        final Header header = dummyHeader();
        final AbsoluteDate startDate =
            DateUtils.toAbsoluteDate(header.getAvailability().getStart());
        final AbsoluteDate finalDate = startDate.shiftedBy(60.0);

        final BoundedPropagator propagator =
            dummyPropagator(startDate, finalDate, dummyOrbit(startDate));
        final Spacecraft spacecraft =
            new Spacecraft(propagator, header.getClock());

        final String pathFile =
            loadResources("templateFile/object/primary/entities/SpacecraftTemplate.txt");

        verifyFileOutput(pathFile, spacecraft.toString(), 1e-8);
    }

    /**
     * Satellite constructor test.
     *
     * @throws IOException the io exception
     * @throws URISyntaxException the uri syntax exception
     */
    @Test
    void SatelliteLabelTest()
        throws IOException,
            URISyntaxException {

        loadOrekitData();

        final Header header = dummyHeader();
        final AbsoluteDate startDate =
            DateUtils.toAbsoluteDate(header.getAvailability().getStart());
        final AbsoluteDate finalDate = startDate.shiftedBy(60.0);

        final BoundedPropagator propagator =
            dummyPropagator(startDate, finalDate, dummyOrbit(startDate));

        final Spacecraft spacecraft =
            Spacecraft.builder(propagator, header.getClock().getMultiplier())
                .withName("Spacecraft").withDisplayName().build();

        final String pathFile =
            loadResources("templateFile/object/primary/entities/SpacecraftLabelTemplate.txt");

        verifyFileOutput(pathFile, spacecraft.toString(), 1e-8);
    }

    /**
     * Attitude constructor test.
     *
     * @throws URISyntaxException the uri syntax exception
     * @throws IOException the io exception
     */
    @Test
    @DefaultDataContext
    void attitudeConstructorTest()
        throws URISyntaxException,
            IOException {

        loadOrekitData();

        final Header header = dummyHeader();

        final AbsoluteDate startDate =
            DateUtils.toAbsoluteDate(header.getAvailability().getStart());
        final AbsoluteDate finalDate = startDate.shiftedBy(60.0);

        final BoundedPropagator propagator =
            dummyPropagator(startDate, finalDate, dummyOrbit(startDate));

        final Spacecraft spacecraft =
            Spacecraft.builder(propagator, header.getClock())
                .withDisplayAttitude().build();

        final String pathFile =
            loadResources("templateFile/object/primary/entities/SpacecraftAttitudeTemplate.txt");

        Assertions.assertEquals(Files.readString(Path.of(pathFile)),
                                spacecraft.toString());
        verifyFileOutput(pathFile, spacecraft.toString(), 1e-8);
    }

    /** Test for the display of the influence sphere. */
    @Test
    @DefaultDataContext
    void influenceSphereDisplayTest()
        throws URISyntaxException,
            IOException {

        // Load of the orekit data
        loadOrekitData();

        // Header
        final Header header = dummyHeader();

        // Build of the clock
        final AbsoluteDate startDate =
            DateUtils.toAbsoluteDate(header.getAvailability().getStart());
        final AbsoluteDate finalDate = startDate.shiftedBy(60.0);

        final Clock clock = new Clock(startDate, finalDate, 1.0);

        // Build of the spacecraft
        final BoundedPropagator propagator =
            dummyPropagator(startDate, finalDate, dummyOrbit(startDate));

        final Spacecraft spacecraft =
            Spacecraft.builder(propagator, header.getClock())
                .withDisplayAttitude().build();

        // Build of two bodies : Earth and Moon
        final Body earth = BodyFactory.getEarth(clock);
        final Body moon = BodyFactory.getMoon(clock);
        final List<Body> bodyList = new ArrayList<>();
        bodyList.add(earth);
        bodyList.add(moon);

        spacecraft.displayInfluenceSphereChanges(bodyList, earth);

        final String spacecraftWithInfluenceSphereTemplate =
            loadResources("templateFile/object/primary/entities/SpacecraftInfluenceSphereTemplate.txt");

        verifyFileOutput(spacecraftWithInfluenceSphereTemplate,
                         spacecraft.toString(), 1e-8);
    }
}
