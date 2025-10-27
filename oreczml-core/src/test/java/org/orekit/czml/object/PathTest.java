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
package org.orekit.czml.object;

import cesiumlanguagewriter.TimeInterval;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.orekit.annotation.DefaultDataContext;
import org.orekit.czml.file.AbstractTest;
import org.orekit.czml.object.primary.Header;
import org.orekit.czml.object.secondary.Path;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * The type Path test.
 */
public class PathTest
    extends
    AbstractTest {

    /**
     * Path constructor test.
     *
     * @throws IOException the io exception
     */
    @Test
    @DefaultDataContext
    void PathConstructorTest()
        throws IOException,
            URISyntaxException {

        loadOrekitData();

        final Header header = dummyHeader();

        final TimeInterval availability = header.getAvailability();

        final Path path = new Path(availability);

        final Path pathCoverage = new Path(availability, true);

        final String pathFile = loadResources("templateFile/PathTemplate.txt");
        final String pathCoverageFile =
            loadResources("templateFile/PathCoverageTemplate.txt");

        verifyFileOutput(pathFile, path.toString(), 1e-8);
        verifyFileOutput(pathCoverageFile, pathCoverage.toString(), 1e-8);

        Assertions.assertTrue(pathCoverage.isShow());
        Assertions.assertEquals(availability, pathCoverage.getAvailability());
    }
}
