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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.orekit.annotation.DefaultDataContext;
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

    /** Initialise orekit data. */
    private final double data = initializeOrekitData();

    /** Header. */
    final Header header = dummyHeader();

    /**
     * Central body reference system constructor test.
     *
     * @throws URISyntaxException the uri syntax exception
     * @throws IOException the io exception
     */
    @Test
    @DefaultDataContext
    @DisplayName("Central body reference system constructor test")
    void CentralBodyReferenceSystemConstructorTest()
        throws URISyntaxException,
            IOException {

        final CentralBodyReferenceSystem system =
            CentralBodyReferenceSystem.builder(header.getClock()).build();

        final String pathFile =
            loadResources("templateFile/object/primary/systems/centralbodyreferencesystem/CentralBodyReferenceSystemTemplate.txt");

        verifyFileOutput(pathFile, system.toString(), 1e-8);
    }

    @Test
    @DisplayName("Central body reference system with builder constructor test")
    public void CentralBodyReferenceSystemBuilderConstructorTest()
        throws URISyntaxException,
            IOException {

        final CentralBodyReferenceSystem systemBuilder =
            CentralBodyReferenceSystem.builder(header.getClock())
                .withBody(getEarth())
                .withColors(Color.BLUE, Color.GREEN, Color.RED)
                .withName("A name").withCustomId("CustomID").build();

        // Reference file
        final String builderPathFile =
            loadResources("templateFile/object/primary/systems/centralbodyreferencesystem/CentralBodyReferenceSystemWithBuilderTemplate.txt");

        verifyFileOutput(builderPathFile, systemBuilder.toString(), 1e-8);
    }

}
