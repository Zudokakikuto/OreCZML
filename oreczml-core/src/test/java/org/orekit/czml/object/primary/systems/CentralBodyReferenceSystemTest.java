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
package org.orekit.czml.object.primary.systems;

import org.junit.jupiter.api.Test;
import org.orekit.czml.file.AbstractTest;
import org.orekit.czml.object.primary.Header;

import java.awt.Color;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * The type Central body reference system test.
 */
public class CentralBodyReferenceSystemTest
    extends
    AbstractTest {

    /**
     * Central body reference system constructor test.
     *
     * @throws URISyntaxException the uri syntax exception
     * @throws IOException the io exception
     */
    @Test
    void CentralBodyReferenceSystemConstructorTest()
        throws URISyntaxException,
            IOException {

        loadOrekitData();

        final Header header = dummyHeader();

        final CentralBodyReferenceSystem system =
            CentralBodyReferenceSystem.builder(header.getAvailability())
                .build();

        final CentralBodyReferenceSystem systemBuilder =
            CentralBodyReferenceSystem.builder(header.getAvailability())
                .withBody(getEarth())
                .withColors(Color.BLUE, Color.GREEN, Color.RED)
                .withName("A name").withCustomId("CustomID").build();

        final String pathFile =
            loadResources("templateFile/object/primary/systems/CentralBodyReferenceSystemTemplate.txt");
        final String builderPathFile =
            loadResources("templateFile/object/primary/systems/CentralBodyReferenceSystemWithBuilderTemplate.txt");

        verifyFileOutput(pathFile, system.toString(), 1e-8);
        verifyFileOutput(builderPathFile, systemBuilder.toString(), 1e-8);
    }

}
