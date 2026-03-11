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

import org.junit.jupiter.api.Test;
import org.orekit.annotation.DefaultDataContext;
import org.orekit.czml.archi.factory.BodyFactory;
import org.orekit.czml.file.AbstractTest;
import org.orekit.czml.object.nonvisual.CzmlModel;
import org.orekit.czml.object.primary.Header;
import org.orekit.czml.object.secondary.Clock;
import org.orekit.czml.object.utils.DateUtils;
import org.orekit.propagation.BoundedPropagator;
import org.orekit.time.AbsoluteDate;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * The type Satellite test.
 */
@DefaultDataContext
public class SpacecraftTest
    extends
    AbstractTest {

    /** Initialise orekit data. */
    private final double data = initializeOrekitData();

    /** Header. */
    final Header header = dummyHeader();

    /** Start Date. */
    final AbsoluteDate startDate =
        DateUtils.toAbsoluteDate(header.getAvailability().getStart());

    final AbsoluteDate finalDate = startDate.shiftedBy(60.0);

    /**
     * Satellite constructor test.
     *
     * @throws IOException the io exception
     * @throws URISyntaxException the uri syntax exception
     */
    @Test
    void SatelliteConstructorTest()
        throws IOException,
            URISyntaxException {

        final BoundedPropagator propagator =
            dummyPropagator(startDate, finalDate, dummyOrbit(startDate));
        final Spacecraft spacecraft =
            new Spacecraft(propagator, header.getClock());

        final String IssModel = loadResources("Default3DModels/ISSModel.glb");
        final CzmlModel model =
            CzmlModel.builder(IssModel, true, header.getClock()).build();

        final Spacecraft spacecraftWithModel =
            Spacecraft.builder(propagator, header.getClock()).withModel(model)
                .build();

        final String pathFile =
            loadResources("templateFile/object/primary/entities/spacecraft/SpacecraftTemplate.txt");
        final String pathFileWithModel =
            loadResources("templateFile/object/primary/entities/spacecraft/SpacecraftBuilderWithModelTemplate.txt");

        verifyFileOutput(pathFileWithModel, spacecraftWithModel.toString(),
                         1e-8);
        verifyFileOutput(pathFile, spacecraft.toString(), 1e-8);
    }

    /**
     * Satellite constructor test.
     *
     * @throws IOException the io exception
     * @throws URISyntaxException the uri syntax exception
     */
    @Test
    void SatelliteLabelTest()
        throws IOException,
            URISyntaxException {

        final BoundedPropagator propagator =
            dummyPropagator(startDate, finalDate, dummyOrbit(startDate));

        final Spacecraft spacecraft =
            Spacecraft.builder(propagator, header.getClock().getMultiplier())
                .withName("Spacecraft").withDisplayName().build();

        final String pathFile =
            loadResources("templateFile/object/primary/entities/spacecraft/SpacecraftLabelTemplate.txt");

        verifyFileOutput(pathFile, spacecraft.toString(), 1e-8);
    }

    /**
     * Attitude constructor test.
     *
     * @throws URISyntaxException the uri syntax exception
     * @throws IOException the io exception
     */
    @Test
    @DefaultDataContext
    void attitudeConstructorTest()
        throws URISyntaxException,
            IOException {

        final BoundedPropagator propagator =
            dummyPropagator(startDate, finalDate, dummyOrbit(startDate));

        final Spacecraft spacecraft =
            Spacecraft.builder(propagator, header.getClock())
                .withDisplayAttitude().build();

        final String pathFile =
            loadResources("templateFile/object/primary/entities/spacecraft/SpacecraftAttitudeTemplate.txt");

        verifyFileOutput(pathFile, spacecraft.toString(), 1e-8);
    }

    /** Test for the display of the influence sphere. */
    @Test
    @DefaultDataContext
    void influenceSphereDisplayTest()
        throws URISyntaxException,
            IOException {

        final AbsoluteDate finalDate = startDate.shiftedBy(60.0);

        final Clock clock = new Clock(startDate, finalDate, 1.0);

        // Build of the spacecraft
        final BoundedPropagator propagator =
            dummyPropagator(startDate, finalDate, dummyOrbit(startDate));

        final Spacecraft spacecraft =
            Spacecraft.builder(propagator, header.getClock())
                .withDisplayAttitude().build();

        // Build of two bodies : Earth and Moon
        final Body earth = BodyFactory.getEarth(clock);
        final Body moon = BodyFactory.getMoon(clock);
        final List<Body> bodyList = new ArrayList<>();
        bodyList.add(earth);
        bodyList.add(moon);

        spacecraft.displayInfluenceSphereChanges(bodyList, earth);

        final String spacecraftWithInfluenceSphereTemplate =
            loadResources("templateFile/object/primary/entities/spacecraft/SpacecraftInfluenceSphereTemplate.txt");

        verifyFileOutput(spacecraftWithInfluenceSphereTemplate,
                         spacecraft.toString(), 1e-8);
    }
}
