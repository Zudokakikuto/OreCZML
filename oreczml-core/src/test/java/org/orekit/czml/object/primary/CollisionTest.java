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

import org.hipparchus.linear.MatrixUtils;
import org.hipparchus.linear.RealMatrix;
import org.hipparchus.ode.nonstiff.AdaptiveStepsizeIntegrator;
import org.hipparchus.ode.nonstiff.DormandPrince853Integrator;
import org.hipparchus.util.FastMath;
import org.junit.jupiter.api.Test;
import org.orekit.czml.file.AbstractTest;
import org.orekit.czml.object.primary.covariance.Collision;
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
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.StateCovariance;
import org.orekit.propagation.numerical.NumericalPropagator;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScalesFactory;
import org.orekit.utils.Constants;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

/**
 * The type Collision test.
 */
public class CollisionTest
    extends
    AbstractTest {

    /**
     * Collision constructor test.
     *
     * @throws URISyntaxException the uri syntax exception
     * @throws IOException the io exception
     */
    @Test
    void CollisionConstructorTest()
        throws URISyntaxException,
            IOException {

        loadOrekitData();

        final Header header = dummyHeader();

        final AbsoluteDate startDate =
            new AbsoluteDate(2024, 3, 15, 0, 0, 0.0,
                             TimeScalesFactory.getUTC());
        final AbsoluteDate finalDate = startDate.shiftedBy(5 * 3600);

        final KeplerianOrbit initialOrbit1 =
            new KeplerianOrbit(7878000, 0, FastMath.toRadians(20), 0,
                               FastMath.toRadians(0), FastMath.toRadians(0),
                               PositionAngleType.MEAN,
                               FramesFactory.getEME2000(), startDate,
                               Constants.WGS84_EARTH_MU);
        final KeplerianOrbit initialOrbit2 =
            new KeplerianOrbit(7878100, 0, FastMath.toRadians(200), 0,
                               FastMath.toRadians(0), FastMath.toRadians(0),
                               PositionAngleType.MEAN,
                               FramesFactory.getEME2000(), startDate,
                               Constants.WGS84_EARTH_MU);
        final SpacecraftState initialState1 =
            new SpacecraftState(initialOrbit1);
        final SpacecraftState initialState2 =
            new SpacecraftState(initialOrbit2);

        final double[][] tolerances1 =
            NumericalPropagator.tolerances(10, initialOrbit1,
                                           OrbitType.CARTESIAN);
        final double[][] tolerances2 =
            NumericalPropagator.tolerances(10, initialOrbit2,
                                           OrbitType.CARTESIAN);
        final AdaptiveStepsizeIntegrator integrator1 =
            new DormandPrince853Integrator(0.001, 1000.0, tolerances1[0],
                                           tolerances1[1]);
        final AdaptiveStepsizeIntegrator integrator2 =
            new DormandPrince853Integrator(0.001, 1000.0, tolerances2[0],
                                           tolerances2[1]);

        final NumericalPropagator propagator1 =
            new NumericalPropagator(integrator1);
        final NumericalPropagator propagator2 =
            new NumericalPropagator(integrator2);

        final NormalizedSphericalHarmonicsProvider provider =
            GravityFieldFactory.getNormalizedProvider(10, 10);
        final ForceModel holmesFeatherstone =
            new HolmesFeatherstoneAttractionModel(FramesFactory.getEME2000(),
                                                  provider);

        propagator1.setOrbitType(OrbitType.CARTESIAN);
        propagator1.addForceModel(holmesFeatherstone);
        propagator1.setInitialState(initialState1);

        propagator2.setOrbitType(OrbitType.CARTESIAN);
        propagator2.addForceModel(holmesFeatherstone);
        propagator2.setInitialState(initialState2);

        final EphemerisGenerator generator1 =
            propagator1.getEphemerisGenerator();
        final EphemerisGenerator generator2 =
            propagator2.getEphemerisGenerator();

        propagator1.propagate(startDate, finalDate);
        propagator2.propagate(startDate, finalDate);

        final BoundedPropagator boundedPropagator1 =
            generator1.getGeneratedEphemeris();
        final BoundedPropagator boundedPropagator2 =
            generator2.getGeneratedEphemeris();

        final Spacecraft satellite1 =
            Spacecraft.builder(boundedPropagator1, header).withOnlyOnePeriod()
                .build();
        final Spacecraft satellite2 =
            Spacecraft.builder(boundedPropagator2, header).withOnlyOnePeriod()
                .build();

        final RealMatrix realMatrix =
            MatrixUtils.createRealDiagonalMatrix(new double[] {
                20000 *
                                                                20000,
                1e-6, 1e-6, 1e-6, 1e-6, (36 * 4.848e-6) * (36 * 4.848e-6)
            });
        final StateCovariance stateCovariance =
            new StateCovariance(realMatrix, startDate,
                                FramesFactory.getEME2000(),
                                OrbitType.EQUINOCTIAL, PositionAngleType.MEAN);
        final List<StateCovariance> covariances1 =
            covariancePropagation(satellite1, propagator1, stateCovariance,
                                  header);
        final List<StateCovariance> covariances2 =
            covariancePropagation(satellite2, propagator2, stateCovariance,
                                  header);

        final Collision collision =
            Collision.builder(satellite1, satellite2, covariances1,
                              covariances2, LOFType.TNW, LOFType.TNW, header)
                .build();

        final Collision collisionBuilder =
            Collision
                .builder(satellite1, satellite2, covariances1, covariances2,
                         LOFType.TNW, LOFType.TNW, header)
                .withCustomId("CustomID").withHeader(header).build();

        final String pathFile =
            loadResources("templateFile/primary/CollisionTemplate.txt");
        final String builderPathFile =
            loadResources("templateFile/primary/CollisionWithBuilderTemplate.txt");

        verifyFileOutput(pathFile, collision.toString(), 1e-8);
        verifyFileOutput(builderPathFile, collisionBuilder.toString(), 1e-8);
    }

}
