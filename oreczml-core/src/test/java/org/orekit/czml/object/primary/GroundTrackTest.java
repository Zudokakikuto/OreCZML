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
package org.orekit.czml.object.primary;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.orekit.czml.file.AbstractTest;
import org.orekit.czml.object.Utils.DateUtils;
import org.orekit.propagation.BoundedPropagator;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScalesFactory;

import java.awt.Color;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The type Ground track test.
 */
public class GroundTrackTest extends AbstractTest {

    /**
     * Ground track constructor test.
     *
     * @throws IOException        the io exception
     * @throws URISyntaxException the uri syntax exception
     */
    @Test
    void GroundTrackConstructorTest() throws IOException, URISyntaxException {

        loadOrekitData();

        final Header header = dummyHeader();
        final AbsoluteDate startDate = DateUtils.toAbsoluteDate(header.getAvailability()
                                                                      .getStart(), TimeScalesFactory.getUTC());
        final AbsoluteDate finalDate = startDate.shiftedBy(60.0);

        final BoundedPropagator propagator = dummyPropagator(startDate, finalDate);

        final Satellite satellite = new Satellite(propagator, header);

        final List<BoundedPropagator> propagators = new ArrayList<>(List.of(propagator));

        final Constellation constellation = new Constellation(propagators, finalDate, header);

        final GroundTrack groundTrack = new GroundTrack(satellite, getEarth(), header);

        final GroundTrack groundTrackWithBuilder = GroundTrack.builder(satellite, getEarth(), header)
                                                              .withColor(Color.ORANGE)
                                                              .withHeader(header)
                                                              .withCustomID("CustomID")
                                                              .build();

        final GroundTrack constellationGroundTrack = GroundTrack.builder(constellation, getEarth(), header)
                                                                .build();

        final String pathFile              = loadResources("templateFile/primary/GroundTrackTemplate.txt");
        final String builderPathFile       = loadResources("templateFile/primary/GroundTrackWithBuilderTemplate.txt");
        final String constellationPathFile = loadResources("templateFile/primary/GroundTrackConstellationTemplate.txt");

        Assertions.assertEquals(Files.readString(Path.of(pathFile)), groundTrack.toString());
        Assertions.assertEquals(Files.readString(Path.of(builderPathFile)), groundTrackWithBuilder.toString());
        Assertions.assertEquals(Files.readString(Path.of(constellationPathFile)), constellationGroundTrack.toString());
    }
}
