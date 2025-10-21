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
package org.orekit.czml.object.primary.visu;

import org.hipparchus.ode.nonstiff.AdaptiveStepsizeIntegrator;
import org.hipparchus.ode.nonstiff.DormandPrince853Integrator;
import org.hipparchus.util.FastMath;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.orekit.annotation.DefaultDataContext;
import org.orekit.czml.file.AbstractTest;
import org.orekit.czml.object.Polyline;
import org.orekit.czml.object.primary.Header;
import org.orekit.czml.object.primary.entities.Constellation;
import org.orekit.czml.object.primary.entities.Spacecraft;
import org.orekit.czml.object.secondary.Clock;
import org.orekit.forces.ForceModel;
import org.orekit.forces.gravity.HolmesFeatherstoneAttractionModel;
import org.orekit.forces.gravity.potential.GravityFieldFactory;
import org.orekit.forces.gravity.potential.NormalizedSphericalHarmonicsProvider;
import org.orekit.frames.FramesFactory;
import org.orekit.orbits.KeplerianOrbit;
import org.orekit.orbits.OrbitType;
import org.orekit.orbits.PositionAngleType;
import org.orekit.propagation.BoundedPropagator;
import org.orekit.propagation.EphemerisGenerator;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.numerical.NumericalPropagator;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScalesFactory;
import org.orekit.utils.Constants;

import java.awt.Color;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The type Inter sat visu test.
 */
public class InterSatVisuTest
    extends
    AbstractTest {

    /**
     * Inter sat visu constructor test.
     *
     * @throws IOException the io exception
     * @throws URISyntaxException the uri syntax exception
     */
    @Test
    @DefaultDataContext
    void InterSatVisuConstructorTest()
        throws IOException,
            URISyntaxException {

        loadOrekitData();

        final AbsoluteDate startDate =
            new AbsoluteDate(2024, 3, 15, 0, 0, 0.0,
                             TimeScalesFactory.getUTC());
        final AbsoluteDate finalDate = startDate.shiftedBy(32 * 3600);

        final Clock clock = new Clock(startDate, finalDate, 10.0);

        final Header header = new Header("Test constructors inter sat", clock);

        final KeplerianOrbit firstOrbit =
            new KeplerianOrbit(7878000, 0, FastMath.toRadians(0),
                               FastMath.toRadians(0), FastMath.toRadians(0),
                               FastMath.toRadians(0), PositionAngleType.MEAN,
                               FramesFactory.getEME2000(), startDate,
                               Constants.WGS84_EARTH_MU);
        final SpacecraftState firstState = new SpacecraftState(firstOrbit);

        //// Build of the first satellite with an orbit with 40 degree
        //// inclination and omega of 90 degrees
        // Build of a LEO orbit
        final KeplerianOrbit secondOrbit =
            new KeplerianOrbit(7078000, 0, FastMath.toRadians(20),
                               FastMath.toRadians(45), FastMath.toRadians(0),
                               FastMath.toRadians(0), PositionAngleType.MEAN,
                               FramesFactory.getEME2000(), startDate,
                               Constants.WGS84_EARTH_MU);
        final SpacecraftState secondState = new SpacecraftState(secondOrbit);

        // Build of the propagator

        final double[][] tolerances1 =
            NumericalPropagator.tolerances(10, firstOrbit, OrbitType.CARTESIAN);

        final double[][] tolerances2 =
            NumericalPropagator.tolerances(10, secondOrbit,
                                           OrbitType.CARTESIAN);

        final AdaptiveStepsizeIntegrator integrator1 =
            new DormandPrince853Integrator(0.001, 1000.0, tolerances1[0],
                                           tolerances1[1]);

        final AdaptiveStepsizeIntegrator integrator2 =
            new DormandPrince853Integrator(0.001, 1000.0, tolerances2[0],
                                           tolerances2[1]);

        final NormalizedSphericalHarmonicsProvider provider =
            GravityFieldFactory.getNormalizedProvider(10, 10);
        final ForceModel holmesFeatherstone =
            new HolmesFeatherstoneAttractionModel(FramesFactory.getEME2000(),
                                                  provider);

        final NumericalPropagator firstPropagator =
            new NumericalPropagator(integrator1);
        final NumericalPropagator secondPropagator =
            new NumericalPropagator(integrator2);

        firstPropagator.setOrbitType(OrbitType.CARTESIAN);
        firstPropagator.addForceModel(holmesFeatherstone);
        firstPropagator.setInitialState(firstState);
        final EphemerisGenerator firstGenerator =
            firstPropagator.getEphemerisGenerator();
        firstPropagator.propagate(startDate, finalDate);
        final BoundedPropagator firstBoundedPropagator =
            firstGenerator.getGeneratedEphemeris();

        secondPropagator.setOrbitType(OrbitType.CARTESIAN);
        secondPropagator.addForceModel(holmesFeatherstone);
        secondPropagator.setInitialState(secondState);
        final EphemerisGenerator secondGenerator =
            secondPropagator.getEphemerisGenerator();
        secondPropagator.propagate(startDate, finalDate);
        final BoundedPropagator secondBoundedPropagator =
            secondGenerator.getGeneratedEphemeris();

        final List<BoundedPropagator> propagators = new ArrayList<>();
        propagators.add(firstBoundedPropagator);
        propagators.add(secondBoundedPropagator);

        final List<Spacecraft> satellites = new ArrayList<>();
        final Spacecraft firstSat =
            new Spacecraft(firstBoundedPropagator, header.getClock());
        final Spacecraft secondSat =
            new Spacecraft(secondBoundedPropagator, header.getClock());
        firstSat.setOrbitColor(new Color(255, 0, 0, 255));
        secondSat.setOrbitColor(new Color(255, 127, 0, 255));
        satellites.add(firstSat);
        satellites.add(secondSat);

        final InterSatVisu interSatVisu =
            InterSatVisu
                .builder(firstSat, secondSat, finalDate, header.getClock())
                .build();

        firstBoundedPropagator.clearEventsDetectors();
        firstBoundedPropagator.clearStepHandlers();

        final InterSatVisu interSatVisuBuilder =
            InterSatVisu
                .builder(firstSat, secondSat, finalDate, header.getClock())
                .build();

        firstBoundedPropagator.clearEventsDetectors();
        firstBoundedPropagator.clearStepHandlers();

        final InterSatVisu interSatVisuPropagators =
            InterSatVisu.builder(propagators, finalDate, header.getClock())
                .build();

        firstBoundedPropagator.clearEventsDetectors();
        firstBoundedPropagator.clearStepHandlers();
        secondBoundedPropagator.clearStepHandlers();
        secondBoundedPropagator.clearEventsDetectors();

        final InterSatVisu interSatVisuPropagatorsBuilder =
            InterSatVisu.builder(propagators, finalDate, header.getClock())
                .build();

        firstBoundedPropagator.clearEventsDetectors();
        firstBoundedPropagator.clearStepHandlers();
        secondBoundedPropagator.clearStepHandlers();
        secondBoundedPropagator.clearEventsDetectors();

        final InterSatVisu interSatVisuPropagatorsID =
            InterSatVisu.builder(propagators, finalDate, header.getClock())
                .withCustomId("CustomIDTest").build();

        firstBoundedPropagator.clearEventsDetectors();
        firstBoundedPropagator.clearStepHandlers();
        secondBoundedPropagator.clearStepHandlers();
        secondBoundedPropagator.clearEventsDetectors();

        final InterSatVisu interSatVisuFromConstellationBuilder =
            InterSatVisu.builder(Constellation
                .builder(propagators, finalDate, header.getClock()).build(),
                                 finalDate, header.getClock())
                .build();

        firstBoundedPropagator.clearEventsDetectors();
        firstBoundedPropagator.clearStepHandlers();
        secondBoundedPropagator.clearStepHandlers();
        secondBoundedPropagator.clearEventsDetectors();

        final InterSatVisu interSatVisuFromConstructor =
            new InterSatVisu(firstSat, secondSat, finalDate);
        firstBoundedPropagator.clearEventsDetectors();
        firstBoundedPropagator.clearStepHandlers();
        secondBoundedPropagator.clearStepHandlers();
        secondBoundedPropagator.clearEventsDetectors();

        final InterSatVisu interSatVisuFromPropagators =
            new InterSatVisu(propagators, finalDate, clock);
        firstBoundedPropagator.clearEventsDetectors();
        firstBoundedPropagator.clearStepHandlers();
        secondBoundedPropagator.clearStepHandlers();
        secondBoundedPropagator.clearEventsDetectors();

        final Constellation constellation =
            Constellation.builder(propagators, finalDate, clock).build();
        final InterSatVisu interSatVisuFromConstellation =
            new InterSatVisu(constellation, finalDate, header.getClock());

        final String pathFile =
            loadResources("templateFile/object/primary/visu/InterSatVisuTemplate.txt");
        final String propagatorsInterSatPathFile =
            loadResources("templateFile/object/primary/visu/InterSatVisuPropagatorsTemplate.txt");
        final String propagatorsIDInterSatPathFile =
            loadResources("templateFile/object/primary/visu/InterSatVisuPropagatorsIDTemplate.txt");
        final String constellationInterSatPathFile =
            loadResources("templateFile/object/primary/visu/InterSatVisuConstellationTemplate.txt");
        final String constructorInterSatPathFile =
            loadResources("templateFile/object/primary/visu/InterSatVisuFromConstructorTemplate.txt");
        final String constructorPropagatorsInterSatPathFile =
            loadResources("templateFile/object/primary/visu/InterSatVisuFromPropagatorsConstructorTemplate.txt");
        final String constructorConstellationInterSatPathFile =
            loadResources("templateFile/object/primary/visu/InterSatVisuFromConstellationConstructorTemplate.txt");

        verifyFileOutput(pathFile, interSatVisu.toString(), 1e-8);
        verifyFileOutput(pathFile, interSatVisuBuilder.toString(), 1e-8);
        verifyFileOutput(propagatorsInterSatPathFile,
                         interSatVisuPropagators.toString(), 1e-8);
        verifyFileOutput(propagatorsInterSatPathFile,
                         interSatVisuPropagatorsBuilder.toString(), 1e-8);
        verifyFileOutput(propagatorsIDInterSatPathFile,
                         interSatVisuPropagatorsID.toString(), 1e-8);
        verifyFileOutput(constellationInterSatPathFile,
                         interSatVisuFromConstellationBuilder.toString(), 1e-8);
        verifyFileOutput(constructorInterSatPathFile,
                         interSatVisuFromConstructor.toString(), 1e-8);
        verifyFileOutput(constructorPropagatorsInterSatPathFile,
                         interSatVisuFromPropagators.toString(), 1e-8);
        verifyFileOutput(constructorConstellationInterSatPathFile,
                         interSatVisuFromConstellation.toString(), 1e-8);

        // Getters coverage
        Assertions.assertEquals(
                                secondSat.getSpaceCraftStates().get(0)
                                    .getPVCoordinates(),
                                interSatVisu.getSatellite2()
                                    .getSpaceCraftStates().get(0)
                                    .getPVCoordinates());
        Assertions.assertEquals(startDate, interSatVisu.getStartDate());
        Assertions.assertEquals(finalDate, interSatVisu.getStopDate());
        Assertions
            .assertEquals(firstSat.getSpaceCraftStates().get(0)
                .getPVCoordinates(),
                          interSatVisu.getInitialState().getPVCoordinates());
        Assertions.assertEquals(propagators,
                                interSatVisuPropagators.getPropagators());
        Assertions.assertEquals(
                                new ArrayList<>(Arrays.asList(firstSat.getId(),
                                                              secondSat
                                                                  .getId())),
                                interSatVisuFromConstellationBuilder
                                    .getIdsSatellites());
        Assertions
            .assertEquals(Polyline.nonVectorBuilder(header.getClock()).build()
                .getClock().toString(),
                          interSatVisu.getPolyline().getClock().toString());
        Assertions.assertEquals(new ArrayList<>(Arrays
            .asList(true, false, true, false, true, false, true)),
                                interSatVisu.getBooleanList());

    }
}
