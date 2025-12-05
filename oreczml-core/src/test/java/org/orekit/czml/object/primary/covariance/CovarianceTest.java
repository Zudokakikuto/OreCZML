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
package org.orekit.czml.object.primary.covariance;

import org.hipparchus.linear.MatrixUtils;
import org.hipparchus.linear.RealMatrix;
import org.hipparchus.ode.nonstiff.AdaptiveStepsizeIntegrator;
import org.hipparchus.ode.nonstiff.DormandPrince853Integrator;
import org.hipparchus.util.FastMath;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.orekit.annotation.DefaultDataContext;
import org.orekit.czml.file.AbstractTest;
import org.orekit.czml.object.primary.Header;
import org.orekit.czml.object.primary.entities.Spacecraft;
import org.orekit.forces.ForceModel;
import org.orekit.forces.gravity.HolmesFeatherstoneAttractionModel;
import org.orekit.forces.gravity.potential.GravityFieldFactory;
import org.orekit.forces.gravity.potential.NormalizedSphericalHarmonicsProvider;
import org.orekit.frames.FramesFactory;
import org.orekit.frames.LOFType;
import org.orekit.orbits.KeplerianOrbit;
import org.orekit.orbits.OrbitType;
import org.orekit.orbits.PositionAngleType;
import org.orekit.propagation.BoundedPropagator;
import org.orekit.propagation.EphemerisGenerator;
import org.orekit.propagation.Propagator;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.StateCovariance;
import org.orekit.propagation.numerical.NumericalPropagator;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScalesFactory;
import org.orekit.utils.Constants;

import java.awt.Color;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The type Covariance test.
 */
public class CovarianceTest
    extends
    AbstractTest {

    /** Initialise orekit data. */
    private final double data = initializeOrekitData();

    final Header header = dummyHeader();

    // Dates

    /** Start date. */
    private final AbsoluteDate startDate =
        new AbsoluteDate(2024, 3, 15, 0, 0, 0.0, TimeScalesFactory.getUTC());

    /** Final date. */
    private final AbsoluteDate finalDate = startDate.shiftedBy(5 * 3600);

    /** Orbit. */
    final KeplerianOrbit initialOrbit =
        new KeplerianOrbit(7878000, 0, FastMath.toRadians(20), 0,
                           FastMath.toRadians(0), FastMath.toRadians(0),
                           PositionAngleType.MEAN, FramesFactory.getEME2000(),
                           startDate, Constants.WGS84_EARTH_MU);

    /** Initial State. */
    final SpacecraftState initialState = new SpacecraftState(initialOrbit);

    /**
     * Covariance constructor test.
     *
     * @throws IOException the io exception
     * @throws URISyntaxException the uri syntax exception
     */
    @Test
    @DefaultDataContext
    void CovarianceConstructorTest()
        throws IOException,
            URISyntaxException {

        final Map<Propagator, Spacecraft> mapPropSpacecraft =
            spacecraftBuild(header);

        final Optional<Propagator> propagatorOptional =
            mapPropSpacecraft.keySet().stream().findFirst();
        Propagator propagator = null;
        if (propagatorOptional.isPresent()) {
            propagator = propagatorOptional.get();
        }
        final Spacecraft spacecraft = mapPropSpacecraft.get(propagator);

        final RealMatrix realMatrix =
            MatrixUtils.createRealDiagonalMatrix(new double[] {
                1e-4, 1e-4, 2e-4, 1e-6, 1e-6, (36 * 4.848e-6) * (36 * 4.848e-6)
            });
        final StateCovariance stateCovariance =
            new StateCovariance(realMatrix, startDate,
                                FramesFactory.getEME2000(),
                                OrbitType.EQUINOCTIAL, PositionAngleType.MEAN);
        Assertions.assertNotNull(propagator);
        final List<StateCovariance> covariances =
            covariancePropagation(spacecraft, propagator, stateCovariance,
                                  header.getClock().getMultiplier());

        final Covariance covariance =
            Covariance.builder(spacecraft, covariances, LOFType.TNW)
                .withColor(Color.ORANGE).withCustomID("CustomID").build();

        final String pathFile =
            loadResources("templateFile/object/primary/covariance/CovarianceTemplate.txt");
        verifyFileOutput(pathFile, covariance.toString(), 1e-4);
    }

    private Map<Propagator, Spacecraft> spacecraftBuild(final Header header)
        throws URISyntaxException,
            IOException {

        final double[][] tolerances =
            NumericalPropagator.tolerances(10, initialOrbit,
                                           OrbitType.CARTESIAN);
        final AdaptiveStepsizeIntegrator integrator =
            new DormandPrince853Integrator(0.001, 1000.0, tolerances[0],
                                           tolerances[1]);

        final NumericalPropagator propagator =
            new NumericalPropagator(integrator);

        final NormalizedSphericalHarmonicsProvider provider =
            GravityFieldFactory.getNormalizedProvider(10, 10);
        final ForceModel holmesFeatherstone =
            new HolmesFeatherstoneAttractionModel(FramesFactory.getEME2000(),
                                                  provider);

        propagator.setOrbitType(OrbitType.CARTESIAN);
        propagator.addForceModel(holmesFeatherstone);
        propagator.setInitialState(initialState);

        final EphemerisGenerator generator = propagator.getEphemerisGenerator();

        propagator.propagate(startDate, finalDate);
        final BoundedPropagator boundedPropagator =
            generator.getGeneratedEphemeris();

        final Spacecraft spacecraft =
            new Spacecraft(boundedPropagator, header.getClock());

        final Map<Propagator, Spacecraft> mapToReturn = new HashMap<>();
        mapToReturn.put(propagator, spacecraft);
        return mapToReturn;
    }
}
