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
package org.orekit.czml.adaptors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.orekit.czml.archi.adaptor.OemAdaptor;
import org.orekit.czml.file.AbstractTest;
import org.orekit.data.DataSource;
import org.orekit.files.ccsds.ndm.ParserBuilder;
import org.orekit.files.ccsds.ndm.odm.oem.Oem;
import org.orekit.files.ccsds.ndm.odm.oem.OemParser;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScalesFactory;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * The type Oem adaptor test.
 */
public class OemAdaptorTest
    extends
    AbstractTest {

    /**
     * Oem constructor test.
     *
     * @throws URISyntaxException the uri syntax exception
     * @throws IOException the io exception
     */
    @Test
    void oemConstructorTest()
        throws URISyntaxException,
            IOException {

        loadOrekitData();

        final String OemPath = loadResources("oemForOemTuto.xml");
        final DataSource dataSource = new DataSource(OemPath);
        final ParserBuilder parserBuilder = new ParserBuilder();
        final OemParser oemParser = parserBuilder.buildOemParser();
        final Oem oem = oemParser.parse(dataSource);

        final AbsoluteDate startDate =
            new AbsoluteDate(2025, 9, 1, 0, 0, 0.0, TimeScalesFactory.getUTC());
        final AbsoluteDate finalDate =
            new AbsoluteDate(2025, 9, 2, 0, 0, 0.0, TimeScalesFactory.getUTC());

        final OemAdaptor oemAdaptor = new OemAdaptor(oem);

        Assertions.assertEquals(oem, oemAdaptor.getOem());
        Assertions.assertEquals(startDate, oemAdaptor.buildStartDate());
        Assertions.assertEquals(finalDate, oemAdaptor.buildFinalDate());
    }
}
