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
import org.orekit.czml.object.primary.systems.CentralBodyReferenceSystem;

import java.awt.Color;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * The type Central body reference system test.
 */
public class CentralBodyReferenceSystemTest extends AbstractTest {

    /**
     * Central body reference system constructor test.
     *
     * @throws IOException the io exception
     */
    @Test
    void CentralBodyReferenceSystemConstructorTest() throws IOException {

        loadOrekitData();

        final Header header = dummyHeader();

        final CentralBodyReferenceSystem system = new CentralBodyReferenceSystem(header);

        final CentralBodyReferenceSystem systemBuilder = CentralBodyReferenceSystem.builder(header)
                                                                                   .withHeader(header)
                                                                                   .withBody(getEarth())
                                                                                   .withColors(Color.BLUE, Color.GREEN,
                                                                                           Color.RED)
                                                                                   .withName("A name")
                                                                                   .withCustomId("CustomID")
                                                                                   .build();

        final String pathFile = loadResources("templateFile/primary/CentralBodyReferenceSystemTemplate.txt");
        final String builderPathFile = loadResources("templateFile/primary/CentralBodyReferenceSystemWithBuilderTemplate.txt");

        Assertions.assertEquals(Files.readString(Path.of(pathFile)), system.toString());
        Assertions.assertEquals(Files.readString(Path.of(builderPathFile)), systemBuilder.toString());

    }

}
