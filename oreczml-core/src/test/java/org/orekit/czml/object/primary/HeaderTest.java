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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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
    void HeaderConstructorTest() throws IOException {

        loadOrekitData();

        final Header header = dummyHeader();

        final Header headerCoverage = new Header("A header", "1.0", header.getClock());

        final Header headerVersion = new Header("A header", "1.0", header.getClock(), "");

        final String pathFile         = loadResources("templateFile/primary/HeaderTemplate.txt");
        final String coveragePathFile = loadResources("templateFile/primary/HeaderCoverageTemplate.txt");
        final String versionPathFile  = loadResources("templateFile/primary/HeaderVersionTemplate.txt");

        Assertions.assertEquals(Files.readString(Path.of(pathFile)), header.toString());
        Assertions.assertEquals(Files.readString(Path.of(coveragePathFile)), headerCoverage.toString());
        Assertions.assertEquals(Files.readString(Path.of(versionPathFile)), headerVersion.toString());
    }
}
