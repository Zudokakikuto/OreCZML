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
import org.junit.jupiter.api.DisplayName;
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
import java.util.Collections;
import java.util.List;

/**
 * The type Constellation test.
 */
@DefaultDataContext
public class ConstellationTest
    extends
    AbstractTest {

    /** Initialise orekit data. */
    private final double data = initializeOrekitData();

    // Header
    /** The header. */
    final Header header = dummyHeader();

    // Dates

    /** Start date. */
    final AbsoluteDate startDate =
        DateUtils.toAbsoluteDate(header.getAvailability().getStart());

    /** Final Date. */
    final AbsoluteDate finalDate = startDate.shiftedBy(60.0);

    /** ISS Model. */
    final String ISSModel = loadResources("Default3DModels/ISSModel.glb");

    /**
     * Constellation constructor test.
     *
     * @throws IOException the io exception
     * @throws URISyntaxException the uri syntax exception
     */
    @Test
    @DisplayName("Constellation constructor test with 5 spacecrafts")
    void ConstellationConstructorTest()
        throws IOException,
            URISyntaxException {

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

        final BoundedPropagator boundedPropagator1 =
            propagatorFromOrbit(startDate, finalDate, firstOrbit);
        final BoundedPropagator boundedPropagator2 =
            propagatorFromOrbit(startDate, finalDate, secondOrbit);
        final BoundedPropagator boundedPropagator3 =
            propagatorFromOrbit(startDate, finalDate, thirdOrbit);
        final BoundedPropagator boundedPropagator4 =
            propagatorFromOrbit(startDate, finalDate, fourthOrbit);
        final BoundedPropagator boundedPropagator5 =
            propagatorFromOrbit(startDate, finalDate, fifthOrbit);

        propagators.add(boundedPropagator1);
        propagators.add(boundedPropagator2);
        propagators.add(boundedPropagator3);
        propagators.add(boundedPropagator4);
        propagators.add(boundedPropagator5);

        final Constellation constellation =
            Constellation.builder(propagators, finalDate, header.getClock())
                .withModel(Collections.singletonList(ISSModel))
                .withCustomId("CustomID").displayAttitude()
                .displayOnlyOnePeriod().build();

        final String pathFile =
            loadResources("templateFile/object/primary/entities/constellation/ConstellationTemplate.txt");
        verifyFileOutput(pathFile, constellation.toString(), 1e-8);
    }

    private BoundedPropagator propagatorFromOrbit(final AbsoluteDate startDate,
                                                  final AbsoluteDate finalDate,
                                                  final KeplerianOrbit orbit) {

        final NormalizedSphericalHarmonicsProvider provider =
            GravityFieldFactory.getNormalizedProvider(10, 10);
        final ForceModel holmesFeatherstone =
            new HolmesFeatherstoneAttractionModel(FramesFactory.getEME2000(),
                                                  provider);

        final SpacecraftState state = new SpacecraftState(orbit);
        final double[][] tolerances =
            NumericalPropagator.tolerances(10, orbit, OrbitType.CARTESIAN);

        final AdaptiveStepsizeIntegrator integrator =
            new DormandPrince853Integrator(0.001, 1000.0, tolerances[0],
                                           tolerances[1]);
        final NumericalPropagator propagator =
            new NumericalPropagator(integrator);

        propagator.setOrbitType(OrbitType.CARTESIAN);
        propagator.addForceModel(holmesFeatherstone);
        propagator.setInitialState(state);

        final EphemerisGenerator generator = propagator.getEphemerisGenerator();
        propagator.propagate(startDate, finalDate);

        return generator.getGeneratedEphemeris();
    }
}
