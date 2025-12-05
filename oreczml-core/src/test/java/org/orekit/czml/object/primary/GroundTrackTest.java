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
package org.orekit.czml.object.primary;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.orekit.czml.file.AbstractTest;
import org.orekit.czml.object.primary.entities.Constellation;
import org.orekit.czml.object.primary.entities.Spacecraft;
import org.orekit.czml.object.utils.DateUtils;
import org.orekit.propagation.BoundedPropagator;
import org.orekit.time.AbsoluteDate;

import java.awt.Color;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * The type Ground track test.
 */
public class GroundTrackTest
    extends
    AbstractTest {

    /** Initialise orekit data. */
    private final double data = initializeOrekitData();

    /** Header. */
    final Header header = dummyHeader();

    // Dates

    /** Start Date. */
    final AbsoluteDate startDate =
        DateUtils.toAbsoluteDate(header.getAvailability().getStart());

    /** Final Date. */
    final AbsoluteDate finalDate = startDate.shiftedBy(60.0);

    /**
     * Ground track constructor test.
     *
     * @throws IOException the io exception
     * @throws URISyntaxException the uri syntax exception
     */
    @Test
    void GroundTrackConstructorTest()
        throws IOException,
            URISyntaxException {

        final BoundedPropagator propagator =
            dummyPropagator(startDate, finalDate, dummyOrbit(startDate));

        final Spacecraft satellite =
            Spacecraft.builder(propagator, header.getClock()).build();

        final GroundTrack groundTrack =
            new GroundTrack(satellite, getEarth(), header.getClock());
        groundTrack.displayLinkSatellite();

        final String pathFile =
            loadResources("templateFile/object/primary/GroundTrackTemplate.txt");

        verifyFileOutput(pathFile, groundTrack.toString(), 1e-8);
    }

    @Test
    @DisplayName("Ground track builder constructor test")
    public void GroundTrackBuilderConstructorTest()
        throws URISyntaxException,
            IOException {

        final BoundedPropagator propagator =
            dummyPropagator(startDate, finalDate, dummyOrbit(startDate));

        final Spacecraft satellite =
            Spacecraft.builder(propagator, header.getClock()).build();

        // Build ground track from builder
        final GroundTrack groundTrackWithBuilder =
            GroundTrack.builder(satellite, getEarth(), header.getClock())
                .withColor(Color.ORANGE).withCustomID("CustomID").build();

        // Reference file
        final String builderPathFile =
            loadResources("templateFile/object/primary/GroundTrackWithBuilderTemplate.txt");

        verifyFileOutput(builderPathFile, groundTrackWithBuilder.toString(),
                         1e-8);
    }

    @Test
    @DisplayName("Ground Track constellation constructor test")
    public void GroundTrackConstellationConstructorTest()
        throws URISyntaxException,
            IOException {

        final BoundedPropagator propagator =
            dummyPropagator(startDate, finalDate, dummyOrbit(startDate));

        final List<BoundedPropagator> propagators =
            new ArrayList<>(List.of(propagator));

        final Constellation constellation =
            Constellation.builder(propagators, finalDate, header.getClock())
                .build();

        // Build ground track from constellation
        final GroundTrack constellationGroundTrack =
            GroundTrack.builder(constellation, getEarth(), header.getClock())
                .build();
        constellationGroundTrack.displayLinkSatellite();

        // Reference file
        final String constellationPathFile =
            loadResources("templateFile/object/primary/GroundTrackConstellationTemplate.txt");

        verifyFileOutput(constellationPathFile,
                         constellationGroundTrack.toString(), 1e-8);
    }
}
