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
package org.orekit.czml.object.primary.entities;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.orekit.annotation.DefaultDataContext;
import org.orekit.bodies.CelestialBodyFactory;
import org.orekit.czml.archi.factory.BodyFactory;
import org.orekit.czml.file.AbstractTest;
import org.orekit.czml.file.CzmlFile;
import org.orekit.czml.object.nonvisual.CzmlModel;
import org.orekit.czml.object.primary.Header;
import org.orekit.czml.object.secondary.Clock;
import org.orekit.czml.object.utils.DateUtils;
import org.orekit.frames.Frame;
import org.orekit.time.AbsoluteDate;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * The type Body test.
 */
@DefaultDataContext
public class BodyTest
    extends
    AbstractTest {

    /**
     * Body constructor test.
     *
     * @throws URISyntaxException the uri syntax exception
     * @throws IOException the io exception
     */
    @Test
    void BodyConstructorTest()
        throws URISyntaxException,
            IOException {

        loadOrekitData();

        final Header header = dummyHeader();

        final String pathToModel = loadResources("Bodies/mars.glb");

        final AbsoluteDate startDate =
            DateUtils.toAbsoluteDate(header.getAvailability().getStart());
        final AbsoluteDate stopDate =
            DateUtils.toAbsoluteDate(header.getAvailability().getStop());
        final Clock clock = new Clock(startDate, stopDate, 10.0);

        final CzmlModel modelMars =
            CzmlModel.builder(pathToModel, false, clock).build();

        final Body sun = BodyFactory.getSun(clock);
        final Frame sunFrame = sun.getCelestialBody().getBodyOrientedFrame();

        final Body body =
            Body.builder(CelestialBodyFactory.getMars(), pathToModel, sunFrame,
                         clock, sun)
                .build();

        final double marsOrbitalPeriod = 686.96 * 24 * 3600; // in sec

        final Body bodyBuilder =
            Body.builder(CelestialBodyFactory.getMars(), pathToModel, sunFrame,
                         clock, sun)
                .withCustomID("CustomID")
                .displayOnlyOnePeriod(marsOrbitalPeriod).build();

        final Body mercury = BodyFactory.getMercury(clock);
        final Body earth = BodyFactory.getEarth(clock);
        final Body venus = BodyFactory.getVenus(clock);
        final Body jupiter = BodyFactory.getJupiter(clock);
        final Body saturn = BodyFactory.getSaturn(clock);
        final Body uranus = BodyFactory.getUranus(clock);
        final Body neptune = BodyFactory.getNeptune(clock);
        final Body pluto = BodyFactory.getPluto(clock);

        sun.noOrbitDisplay();

        final Body bodyWithModel =
            Body.builder(CelestialBodyFactory.getMars(), modelMars, sunFrame,
                         clock, sun)
                .build();

        final String bodyPathFile =
            loadResources("templateFile/object/primary/entities/BodyTemplate.txt");
        final CzmlFile file =
            CzmlFile
                .builder(header).withBody(mercury, earth, venus, jupiter,
                                          saturn, uranus, neptune, pluto, sun)
                .build();

        final String bodiesPathFiles =
            loadResources("templateFile/object/primary/entities/BodiesTemplate.txt");
        final String builderPathFiles =
            loadResources("templateFile/object/primary/entities/BodyWithBuilderTemplate.txt");
        final String bodyWithModelFiles =
            loadResources("templateFile/object/primary/entities/BodyWithModelTemplate.txt");

        verifyFileOutput(bodyPathFile, body.toString(), 1e-8);
        verifyFileOutput(builderPathFiles, bodyBuilder.toString(), 1e-8);
        verifyFileOutput(bodiesPathFiles, file.toString(), 1e-8);
        verifyFileOutput(bodyWithModelFiles, bodyWithModel.toString(), 1e-8);
        Assertions.assertEquals(CelestialBodyFactory.getMars(),
                                bodyBuilder.getCelestialBody());
        Assertions.assertTrue(bodyBuilder.isDisplayOrbit());
        Assertions.assertTrue(bodyBuilder.isDisplayOnlyOnePeriod());
    }
}
