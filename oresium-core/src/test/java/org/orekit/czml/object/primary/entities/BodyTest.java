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
import org.junit.jupiter.api.DisplayName;
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

/**
 * The type Body test.
 */
@DefaultDataContext
class BodyTest
    extends
    AbstractTest {

    /** Initialise orekit data. */
    private final double data = initializeOrekitData();

    /** Header. */
    private final Header header = dummyHeader();

    /** The model of Mars to use. */
    private final String pathToModel = loadResources("Bodies/mars.glb");

    /** Start Date. */
    private final AbsoluteDate startDate =
        DateUtils.toAbsoluteDate(header.getAvailability().getStart());

    /** Stop Date. */
    private final AbsoluteDate stopDate =
        DateUtils.toAbsoluteDate(header.getAvailability().getStop());

    /** Clock. */
    private final Clock clock = new Clock(startDate, stopDate, 10.0);

    /**
     * Body constructor test. *
     */
    @Test
    void BodyConstructorTest() {

        // Build the sun frame
        final Body sun = BodyFactory.getSun(clock);
        final Frame sunFrame = sun.getCelestialBody().getBodyOrientedFrame();

        // Build the body
        final Body body =
            Body.builder(CelestialBodyFactory.getMars(), pathToModel, sunFrame,
                         clock, sun)
                .build();
        sun.noOrbitDisplay();

        // Reference file
        final String bodyPathFile =
            loadResources("templateFile/object/primary/entities/body/BodyTemplate.txt");

        verifyFileOutput(bodyPathFile, body.toString(), 1e-8);
    }

    @Test
    @DisplayName("Test Body Builder constructor")
    void BodyBuilderConstructorTest() {

        // Build the sun frame
        final Body sun = BodyFactory.getSun(clock);
        final Frame sunFrame = sun.getCelestialBody().getBodyOrientedFrame();

        // Build the body
        final double marsOrbitalPeriod = 686.96 * 24 * 3600; // in sec
        final Body bodyBuilder =
            Body.builder(CelestialBodyFactory.getMars(), pathToModel, sunFrame,
                         clock, sun)
                .withCustomID("CustomID")
                .displayOnlyOnePeriod(marsOrbitalPeriod).build();

        // Reference file
        final String builderPathFiles =
            loadResources("templateFile/object/primary/entities/body/BodyWithBuilderTemplate.txt");

        verifyFileOutput(builderPathFiles, bodyBuilder.toString(), 1e-8);
        Assertions.assertEquals(CelestialBodyFactory.getMars(),
                                bodyBuilder.getCelestialBody());
        Assertions.assertTrue(bodyBuilder.isDisplayOrbit());
        Assertions.assertTrue(bodyBuilder.isDisplayOnlyOnePeriod());
    }

    @Test
    @DisplayName("Test body with model constructor")
    void BodyWithModelConstructorTest() {

        // Build the sun frame
        final Body sun = BodyFactory.getSun(clock);
        final Frame sunFrame = sun.getCelestialBody().getBodyOrientedFrame();

        // Build the body with the model
        final CzmlModel modelMars =
            CzmlModel.builder(pathToModel, false, clock).build();
        final Body bodyWithModel =
            Body.builder(CelestialBodyFactory.getMars(), modelMars, sunFrame,
                         clock, sun)
                .build();

        // Reference file
        final String bodyWithModelFiles =
            loadResources("templateFile/object/primary/entities/body/BodyWithModelTemplate.txt");

        verifyFileOutput(bodyWithModelFiles, bodyWithModel.toString(), 1e-8);
    }

    @Test
    @DisplayName("Test several bodies czml file constructor")
    void SeveralBodiesCzmlFileConstructorTest() {

        // Build all the bodies
        final Body sun = BodyFactory.getSun(clock);
        final Body mercury = BodyFactory.getMercury(clock);
        final Body earth = BodyFactory.getEarth(clock);
        final Body venus = BodyFactory.getVenus(clock);
        final Body jupiter = BodyFactory.getJupiter(clock);
        final Body saturn = BodyFactory.getSaturn(clock);
        final Body uranus = BodyFactory.getUranus(clock);
        final Body neptune = BodyFactory.getNeptune(clock);
        final Body pluto = BodyFactory.getPluto(clock);

        // Build the czml file
        final CzmlFile file =
            CzmlFile
                .builder(header).withBody(mercury, earth, venus, jupiter,
                                          saturn, uranus, neptune, pluto, sun)
                .build();

        // Reference file
        final String bodiesPathFiles =
            loadResources("templateFile/object/primary/entities/body/BodiesTemplate.txt");

        verifyFileOutput(bodiesPathFiles, file.toString(), 1e-8);
    }
}
