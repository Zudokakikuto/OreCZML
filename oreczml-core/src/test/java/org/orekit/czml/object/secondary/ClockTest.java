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
package org.orekit.czml.object.secondary;

import cesiumlanguagewriter.ClockRange;
import cesiumlanguagewriter.ClockStep;
import cesiumlanguagewriter.TimeInterval;
import org.junit.jupiter.api.Test;
import org.orekit.czml.file.AbstractTest;
import org.orekit.czml.object.utils.DateUtils;
import org.orekit.czml.object.primary.Header;
import org.orekit.data.DataSource;
import org.orekit.files.ccsds.ndm.ParserBuilder;
import org.orekit.files.ccsds.ndm.odm.oem.Oem;
import org.orekit.files.ccsds.ndm.odm.oem.OemParser;
import org.orekit.time.AbsoluteDate;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * The type Clock test.
 */
public class ClockTest
    extends
    AbstractTest {

    /**
     * Clock constructor test.
     *
     * @throws IOException the io exception
     */
    @Test
    void ClockConstructorTest()
        throws IOException,
            URISyntaxException {

        loadOrekitData();

        final Header header = dummyHeader();
        final AbsoluteDate startDate =
            DateUtils.toAbsoluteDate(header.getAvailability().getStart());
        final AbsoluteDate finalDate = startDate.shiftedBy(60.0);
        final TimeInterval interval =
            new TimeInterval(header.getAvailability().getStart(),
                             header.getAvailability().getStop());

        final String OemPath = loadResources("oemForOemTuto.xml");
        final DataSource dataSource = new DataSource(OemPath);
        final ParserBuilder parserBuilder = new ParserBuilder();
        final OemParser oemParser = parserBuilder.buildOemParser();
        final Oem oem = oemParser.parse(dataSource);

        final Clock clock = new Clock(startDate, finalDate, 10.0);

        final Clock clockCoverage =
            new Clock(interval, header.getAvailability().getStart(), 60.0,
                      ClockRange.LOOP_STOP, ClockStep.SYSTEM_CLOCK_MULTIPLIER);

        final Clock oemClock = new Clock(oem);

        final String pathFile =
            loadResources("templateFile/object/secondary/ClockTemplate.txt");
        final String coveragePathFile =
            loadResources("templateFile/object/secondary/ClockCoverageTemplate.txt");
        final String oemClockPathFile =
            loadResources("templateFile/object/secondary/OemClockTemplate.txt");

        verifyFileOutput(pathFile, clock.toString(), 1e-8);
        verifyFileOutput(coveragePathFile, clockCoverage.toString(), 1e-8);
        verifyFileOutput(oemClockPathFile, oemClock.toString(), 1e-8);

    }
}
