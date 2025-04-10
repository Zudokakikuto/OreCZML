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

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * The type Header test.
 */
public class HeaderTest extends AbstractTest {

    /**
     * Header constructor test.
     *
     * @throws IOException the io exception
     */
    @Test
    void HeaderConstructorTest() throws IOException, URISyntaxException {

        loadOrekitData();

        final Header header = dummyHeader();

        final String headerValue = "A header";
        final String headerVersionNumber = "1.0";
        final Header headerCoverage = new Header(headerValue, headerVersionNumber, header.getClock());

        final Header headerVersion = new Header(headerValue, headerVersionNumber, header.getClock(), "");

        final String pathFile         = loadResources("templateFile/primary/HeaderTemplate.txt");
        final String coveragePathFile = loadResources("templateFile/primary/HeaderCoverageTemplate.txt");
        final String versionPathFile  = loadResources("templateFile/primary/HeaderVersionTemplate.txt");

        verifyFileOutput(pathFile, header.toString(), 1e-8);
        verifyFileOutput(coveragePathFile, headerCoverage.toString(), 1e-8);
        verifyFileOutput(versionPathFile, headerVersion.toString(), 1e-8);
    }
}
