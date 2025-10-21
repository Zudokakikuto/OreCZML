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

import org.hipparchus.ode.nonstiff.AdaptiveStepsizeIntegrator;
import org.hipparchus.ode.nonstiff.DormandPrince853Integrator;
import org.hipparchus.util.FastMath;
import org.junit.jupiter.api.Test;
import org.orekit.annotation.DefaultDataContext;
import org.orekit.czml.file.AbstractTest;
import org.orekit.czml.object.primary.Header;
import org.orekit.czml.object.utils.DateUtils;
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
import org.orekit.utils.Constants;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * The type Constellation test.
 */
@DefaultDataContext
public class ConstellationTest
    extends
    AbstractTest {

    /**
     * Constellation constructor test.
     *
     * @throws IOException the io exception
     * @throws URISyntaxException the uri syntax exception
     */
    @Test
    void ConstellationConstructorTest()
        throws IOException,
            URISyntaxException {

        loadOrekitData();

        final Header header = dummyHeader();
        final AbsoluteDate startDate =
            DateUtils.toAbsoluteDate(header.getAvailability().getStart());
        final AbsoluteDate finalDate = startDate.shiftedBy(60.0);

        final String ISSModel = loadResources("Default3DModels/ISSModel.glb");

        final List<BoundedPropagator> propagators = new ArrayList<>();

        final KeplerianOrbit firstOrbit =
            new KeplerianOrbit(7878000, 0, FastMath.toRadians(20), 0,
                               FastMath.toRadians(0), FastMath.toRadians(0),
                               PositionAngleType.MEAN,
                               FramesFactory.getEME2000(), startDate,
                               Constants.WGS84_EARTH_MU);
        final KeplerianOrbit secondOrbit =
            new KeplerianOrbit(8578000, 0, FastMath.toRadians(0), 0,
                               FastMath.toRadians(0), FastMath.toRadians(0),
                               PositionAngleType.MEAN,
                               FramesFactory.getEME2000(), startDate,
                               Constants.WGS84_EARTH_MU);
        final KeplerianOrbit thirdOrbit =
            new KeplerianOrbit(6578000, 0, FastMath.toRadians(-20), 0,
                               FastMath.toRadians(0), FastMath.toRadians(0),
                               PositionAngleType.MEAN,
                               FramesFactory.getEME2000(), startDate,
                               Constants.WGS84_EARTH_MU);
        final KeplerianOrbit fourthOrbit =
            new KeplerianOrbit(10578000, 0, FastMath.toRadians(0), 0,
                               FastMath.toRadians(0), FastMath.toRadians(0),
                               PositionAngleType.MEAN,
                               FramesFactory.getEME2000(), startDate,
                               Constants.WGS84_EARTH_MU);
        final KeplerianOrbit fifthOrbit =
            new KeplerianOrbit(78578000, 0, FastMath.toRadians(98), 0,
                               FastMath.toRadians(0), FastMath.toRadians(0),
                               PositionAngleType.MEAN,
                               FramesFactory.getEME2000(), startDate,
                               Constants.WGS84_EARTH_MU);

        final SpacecraftState firstState = new SpacecraftState(firstOrbit);
        final SpacecraftState secondState = new SpacecraftState(secondOrbit);
        final SpacecraftState thirdState = new SpacecraftState(thirdOrbit);
        final SpacecraftState fourthState = new SpacecraftState(fourthOrbit);
        final SpacecraftState fifthState = new SpacecraftState(fifthOrbit);

        // Build of the propagator

        final double[][] tolerances1 =
            NumericalPropagator.tolerances(10, firstOrbit, OrbitType.CARTESIAN);
        final double[][] tolerances2 =
            NumericalPropagator.tolerances(10, secondOrbit,
                                           OrbitType.CARTESIAN);
        final double[][] tolerances3 =
            NumericalPropagator.tolerances(10, thirdOrbit, OrbitType.CARTESIAN);
        final double[][] tolerances4 =
            NumericalPropagator.tolerances(10, fourthOrbit,
                                           OrbitType.CARTESIAN);
        final double[][] tolerances5 =
            NumericalPropagator.tolerances(10, fifthOrbit, OrbitType.CARTESIAN);

        final AdaptiveStepsizeIntegrator firstIntegrator =
            new DormandPrince853Integrator(0.001, 1000.0, tolerances1[0],
                                           tolerances1[1]);
        final AdaptiveStepsizeIntegrator secondIntegrator =
            new DormandPrince853Integrator(0.001, 1000.0, tolerances2[0],
                                           tolerances2[1]);
        final AdaptiveStepsizeIntegrator thirdIntegrator =
            new DormandPrince853Integrator(0.001, 1000.0, tolerances3[0],
                                           tolerances3[1]);
        final AdaptiveStepsizeIntegrator fourthIntegrator =
            new DormandPrince853Integrator(0.001, 1000.0, tolerances4[0],
                                           tolerances4[1]);
        final AdaptiveStepsizeIntegrator fifthIntegrator =
            new DormandPrince853Integrator(0.001, 1000.0, tolerances5[0],
                                           tolerances5[1]);

        final NumericalPropagator firstPropagator =
            new NumericalPropagator(firstIntegrator);
        final NumericalPropagator secondPropagator =
            new NumericalPropagator(secondIntegrator);
        final NumericalPropagator thirdPropagator =
            new NumericalPropagator(thirdIntegrator);
        final NumericalPropagator fourthPropagator =
            new NumericalPropagator(fourthIntegrator);
        final NumericalPropagator fifthPropagator =
            new NumericalPropagator(fifthIntegrator);

        final NormalizedSphericalHarmonicsProvider provider =
            GravityFieldFactory.getNormalizedProvider(10, 10);
        final ForceModel holmesFeatherstone =
            new HolmesFeatherstoneAttractionModel(FramesFactory.getEME2000(),
                                                  provider);

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

        thirdPropagator.setOrbitType(OrbitType.CARTESIAN);
        thirdPropagator.addForceModel(holmesFeatherstone);
        thirdPropagator.setInitialState(thirdState);
        final EphemerisGenerator thirdGenerator =
            thirdPropagator.getEphemerisGenerator();
        thirdPropagator.propagate(startDate, finalDate);
        final BoundedPropagator thirdBoundedPropagator =
            thirdGenerator.getGeneratedEphemeris();

        fourthPropagator.setOrbitType(OrbitType.CARTESIAN);
        fourthPropagator.addForceModel(holmesFeatherstone);
        fourthPropagator.setInitialState(fourthState);
        final EphemerisGenerator fourthGenerator =
            fourthPropagator.getEphemerisGenerator();
        fourthPropagator.propagate(startDate, finalDate);
        final BoundedPropagator fourthBoundedPropagator =
            fourthGenerator.getGeneratedEphemeris();

        fifthPropagator.setOrbitType(OrbitType.CARTESIAN);
        fifthPropagator.addForceModel(holmesFeatherstone);
        fifthPropagator.setInitialState(fifthState);
        final EphemerisGenerator fifthGenerator =
            fifthPropagator.getEphemerisGenerator();
        fifthPropagator.propagate(startDate, finalDate);
        final BoundedPropagator fifthBoundedPropagator =
            fifthGenerator.getGeneratedEphemeris();

        propagators.add(firstBoundedPropagator);
        propagators.add(secondBoundedPropagator);
        propagators.add(thirdBoundedPropagator);
        propagators.add(fourthBoundedPropagator);
        propagators.add(fifthBoundedPropagator);

        final Constellation constellation =
            Constellation.builder(propagators, finalDate, header.getClock())
                .withModel(ISSModel).withCustomId("CustomID").displayAttitude()
                .displayOnlyOnePeriod().build();

        final String pathFile =
            loadResources("templateFile/object/primary/entities/ConstellationTemplate.txt");
        verifyFileOutput(pathFile, constellation.toString(), 1e-8);
    }
}
