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
                extends AbstractTest {

    /**
     * Ground track constructor test.
     *
     * @throws IOException        the io exception
     * @throws URISyntaxException the uri syntax exception
     */
    @Test
    void GroundTrackConstructorTest()
                    throws
                    IOException,
                    URISyntaxException {

        loadOrekitData();

        final Header       header    = dummyHeader();
        final AbsoluteDate startDate = DateUtils.toAbsoluteDate(header.getAvailability().getStart());
        final AbsoluteDate finalDate = startDate.shiftedBy(60.0);

        final BoundedPropagator propagator = dummyPropagator(startDate, finalDate);

        final Spacecraft satellite = Spacecraft.builder(propagator, header.getClock()).build();

        final List<BoundedPropagator> propagators = new ArrayList<>(List.of(propagator));

        final Constellation constellation = Constellation.builder(propagators, finalDate, header.getClock()).build();

        final GroundTrack groundTrack = GroundTrack.builder(satellite, getEarth(), header.getAvailability()).build();
        groundTrack.displayLinkSatellite();

        final GroundTrack groundTrackWithBuilder =
                        GroundTrack.builder(satellite, getEarth(), header.getAvailability()).withColor(Color.ORANGE).withCustomID("CustomID").build();

        final GroundTrack constellationGroundTrack = GroundTrack.builder(constellation, getEarth(), header.getAvailability()).build();
        constellationGroundTrack.displayLinkSatellite();

        final String pathFile              = loadResources("templateFile/object/primary/GroundTrackTemplate.txt");
        final String builderPathFile       = loadResources("templateFile/object/primary/GroundTrackWithBuilderTemplate.txt");
        final String constellationPathFile = loadResources("templateFile/object/primary/GroundTrackConstellationTemplate.txt");

        verifyFileOutput(pathFile, groundTrack.toString(), 1e-8);
        verifyFileOutput(builderPathFile, groundTrackWithBuilder.toString(), 1e-8);
        verifyFileOutput(constellationPathFile, constellationGroundTrack.toString(), 1e-8);
    }
}
