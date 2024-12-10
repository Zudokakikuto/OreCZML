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
import org.orekit.bodies.CelestialBodyFactory;
import org.orekit.czml.archi.factory.BodyFactory;
import org.orekit.czml.file.AbstractTest;
import org.orekit.czml.file.CzmlFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * The type Body test.
 */
public class BodyTest extends AbstractTest {

    /**
     * Body constructor test.
     *
     * @throws IOException the io exception
     */
    @Test
    void BodyConstructorTest() throws IOException {

        loadOrekitData();

        final Header header = dummyHeader();

        final String pathToModel = loadResources("Bodies/mars.glb");

        final Body body = new Body(CelestialBodyFactory.getMars(), pathToModel, header);

        final double marsOrbitalPeriod = 686.96 * 24 * 3600; // in sec

        final Body bodyBuilder = Body.builder(CelestialBodyFactory.getMars(), pathToModel, header)
                                     .withHeader(header)
                                     .withCustomID("CustomID")
                                     .displayOnlyOnePeriod(marsOrbitalPeriod)
                                     .build();

        final Body mercury = BodyFactory.getMercury(header);
        final Body earth   = BodyFactory.getEarth(header);
        final Body venus   = BodyFactory.getVenus(header);
        final Body jupiter = BodyFactory.getJupiter(header);
        final Body saturn  = BodyFactory.getSaturn(header);
        final Body uranus  = BodyFactory.getUranus(header);
        final Body neptune = BodyFactory.getNeptune(header);
        final Body pluto   = BodyFactory.getPluto(header);
        final Body sun     = BodyFactory.getSun(header);

        sun.noOrbitDisplay();

        final String bodyPathFile = loadResources("templateFile/primary/BodyTemplate.txt");
        final CzmlFile file = CzmlFile.builder()
                                      .withHeader(header)
                                      .withBody(mercury, earth, venus, jupiter, saturn, uranus, neptune,
                                              pluto, sun)
                                      .build();

        final String bodiesPathFiles  = loadResources("templateFile/primary/BodiesTemplate.txt");
        final String builderPathFiles = loadResources("templateFile/primary/BodyWithBuilderTemplate.txt");

        Assertions.assertEquals(Files.readString(Path.of(bodyPathFile)), body.toString());
        Assertions.assertEquals(Files.readString(Path.of(builderPathFiles)), bodyBuilder.toString());

        Assertions.assertEquals(Files.readString(Path.of(bodiesPathFiles)), file.toString());

        Assertions.assertEquals(CelestialBodyFactory.getMars(), bodyBuilder.getBody());
        Assertions.assertTrue(bodyBuilder.isDisplayOrbit());
        Assertions.assertTrue(bodyBuilder.isDisplayOnlyOnePeriod());
    }
}
