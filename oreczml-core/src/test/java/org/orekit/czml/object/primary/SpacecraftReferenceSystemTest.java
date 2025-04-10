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

import org.junit.jupiter.api.Test;
import org.orekit.czml.file.AbstractTest;
import org.orekit.czml.object.Utils.DateUtils;
import org.orekit.czml.object.primary.entities.Spacecraft;
import org.orekit.czml.object.primary.systems.SpacecraftReferenceSystem;
import org.orekit.propagation.BoundedPropagator;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScalesFactory;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * The type Satellite reference system test.
 */
public class SpacecraftReferenceSystemTest extends AbstractTest {

    /**
     * Satellite reference systemc constructor test.
     *
     * @throws IOException        the io exception
     * @throws URISyntaxException the uri syntax exception
     */
    @Test
    void SatelliteReferenceSystemcConstructorTest() throws IOException, URISyntaxException {

        loadOrekitData();

        final Header header = dummyHeader();
        final AbsoluteDate startDate = DateUtils.toAbsoluteDate(header.getAvailability().getStart(), TimeScalesFactory.getUTC());
        final AbsoluteDate finalDate = startDate.shiftedBy(60.0);

        final BoundedPropagator propagator = dummyPropagator(startDate, finalDate);
        final Spacecraft        satellite  = new Spacecraft(propagator, header);

        final SpacecraftReferenceSystem system = new SpacecraftReferenceSystem(satellite, header);

        final String pathFile = loadResources("templateFile/primary/SpacecraftReferenceSystemTemplate.txt");

        verifyFileOutput(pathFile, system.toString(), 1e-8);
    }
}
