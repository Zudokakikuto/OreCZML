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

import org.hipparchus.geometry.euclidean.threed.Vector3D;
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

/**
 * The type Attitude pointing test.
 */
public class AttitudePointingTest extends AbstractTest {

    /**
     * Attitude pointing constructor test.
     *
     * @throws URISyntaxException the uri syntax exception
     * @throws IOException        the io exception
     */
    @Test
    void AttitudePointingConstructorTest() throws URISyntaxException, IOException {

        loadOrekitData();

        final Header header = dummyHeader();

        final AbsoluteDate startDate = DateUtils.toAbsoluteDate(header.getAvailability()
                                                                      .getStart(), TimeScalesFactory.getUTC());
        final AbsoluteDate stopDate = startDate.shiftedBy(60.0);

        final BoundedPropagator propagator = dummyPropagator(startDate, stopDate);

        final Satellite satellite = Satellite.builder(propagator, header)
                                             .withDisplayAttitude()
                                             .build();

        final AttitudePointing attitudePointing = new AttitudePointing(satellite, getEarth(), Vector3D.MINUS_I, header);

        final AttitudePointing attitudePointingWithBuilder = AttitudePointing.builder(satellite, getEarth(),
                                                                                     Vector3D.MINUS_I, header)
                                                                             .withCustomID("CustomID")
                                                                             .withDisplayOnGround(false)
                                                                             .withColor(Color.ORANGE)
                                                                             .withHeader(header)
                                                                             .build();

        final String pathFile = loadResources("templateFile/primary/AttitudePointingTemplate.txt");
        final String builderPathFile = loadResources("templateFile/primary/AttitudePointingWithBuilderTemplate.txt");

        Assertions.assertEquals(Files.readString(Path.of(pathFile)), attitudePointing.toString());
        Assertions.assertEquals(Files.readString(Path.of(builderPathFile)), attitudePointingWithBuilder.toString());
    }
}
