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
import org.junit.jupiter.api.DisplayName;
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

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The type Collision test.
 */
public class CollisionTest
    extends
    AbstractTest {

    /** Initialise orekit data. */
    private final double data = initializeOrekitData();

    /** Header. */
    final Header header = dummyHeader();

    // Dates

    /** Start date. */
    private final AbsoluteDate startDate =
        new AbsoluteDate(2024, 3, 15, 0, 0, 0.0, TimeScalesFactory.getUTC());

    /** Final date. */
    private final AbsoluteDate finalDate = startDate.shiftedBy(5 * 3600);

    // Orbits

    /** The first orbit. */
    final KeplerianOrbit initialOrbit1 =
        new KeplerianOrbit(7878000, 0, FastMath.toRadians(20), 0,
                           FastMath.toRadians(0), FastMath.toRadians(0),
                           PositionAngleType.MEAN, FramesFactory.getEME2000(),
                           startDate, Constants.WGS84_EARTH_MU);

    /** The second orbit. */
    final KeplerianOrbit initialOrbit2 =
        new KeplerianOrbit(7878100, 0, FastMath.toRadians(200), 0,
                           FastMath.toRadians(0), FastMath.toRadians(0),
                           PositionAngleType.MEAN, FramesFactory.getEME2000(),
                           startDate, Constants.WGS84_EARTH_MU);

    /**
     * Collision constructor test.
     *
     * @throws URISyntaxException the uri syntax exception
     * @throws IOException the io exception
     */
    @DisplayName("Collision constructor test")
    @Test
    @DefaultDataContext
    void CollisionConstructorTest()
        throws URISyntaxException,
            IOException {

        // Bounded Propagators for spacecrafts
        final Map<Propagator, BoundedPropagator> mapPropagators1 =
            propagatorFromOrbit(startDate, finalDate, initialOrbit1);
        final Map<Propagator, BoundedPropagator> mapPropagators2 =
            propagatorFromOrbit(startDate, finalDate, initialOrbit2);

        final Optional<Propagator> propagatorOptional1 =
            mapPropagators1.keySet().stream().findFirst();
        final Optional<Propagator> propagatorOptional2 =
            mapPropagators2.keySet().stream().findFirst();

        Propagator propagator1 = null;
        Propagator propagator2 = null;
        if (propagatorOptional1.isPresent()) {
            propagator1 = propagatorOptional1.get();
        }
        if (propagatorOptional2.isPresent()) {
            propagator2 = propagatorOptional2.get();
        }

        final BoundedPropagator boundedPropagator1 =
            mapPropagators1.get(propagator1);
        final BoundedPropagator boundedPropagator2 =
            mapPropagators2.get(propagator2);

        // Spacecrafts
        final Spacecraft satellite1 =
            Spacecraft
                .builder(boundedPropagator1, header.getClock().getMultiplier())
                .withOnlyOnePeriod().build();
        final Spacecraft satellite2 =
            Spacecraft
                .builder(boundedPropagator2, header.getClock().getMultiplier())
                .withOnlyOnePeriod().build();

        // Covariance propagation
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
        Assertions.assertNotNull(propagator1);
        final List<StateCovariance> covariances1 =
            covariancePropagation(satellite1, propagator1, stateCovariance,
                                  header.getClock().getMultiplier());
        Assertions.assertNotNull(propagator2);
        final List<StateCovariance> covariances2 =
            covariancePropagation(satellite2, propagator2, stateCovariance,
                                  header.getClock().getMultiplier());

        // Collision object
        final Collision collision =
            Collision.builder(satellite1, satellite2, covariances1,
                              covariances2, LOFType.TNW, LOFType.TNW)
                .build();

        // Reference file
        final String pathFile =
            loadResources("templateFile/object/primary/covariance/CollisionTemplate.txt");

        verifyFileOutput(pathFile, collision.toString(), 1e-4);
    }

    /**
     * Collision Builder constructor test.
     *
     * @throws URISyntaxException the uri syntax exception
     * @throws IOException the io exception
     */
    @DisplayName("Collision Builder constructor test")
    @Test
    @DefaultDataContext
    void CollisionBuilderConstructorTest()
        throws URISyntaxException,
            IOException {

        // Bounded Propagators for spacecrafts
        final Map<Propagator, BoundedPropagator> mapPropagators1 =
            propagatorFromOrbit(startDate, finalDate, initialOrbit1);
        final Map<Propagator, BoundedPropagator> mapPropagators2 =
            propagatorFromOrbit(startDate, finalDate, initialOrbit2);

        final Optional<Propagator> propagatorOptional1 =
            mapPropagators1.keySet().stream().findFirst();
        final Optional<Propagator> propagatorOptional2 =
            mapPropagators2.keySet().stream().findFirst();

        Propagator propagator1 = null;
        Propagator propagator2 = null;
        if (propagatorOptional1.isPresent()) {
            propagator1 = propagatorOptional1.get();
        }
        if (propagatorOptional2.isPresent()) {
            propagator2 = propagatorOptional2.get();
        }

        final BoundedPropagator boundedPropagator1 =
            mapPropagators1.get(propagator1);
        final BoundedPropagator boundedPropagator2 =
            mapPropagators2.get(propagator2);

        // Spacecrafts
        final Spacecraft satellite1 =
            Spacecraft
                .builder(boundedPropagator1, header.getClock().getMultiplier())
                .withOnlyOnePeriod().build();
        final Spacecraft satellite2 =
            Spacecraft
                .builder(boundedPropagator2, header.getClock().getMultiplier())
                .withOnlyOnePeriod().build();

        // Covariance propagation
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
        Assertions.assertNotNull(propagator1);
        final List<StateCovariance> covariances1 =
            covariancePropagation(satellite1, propagator1, stateCovariance,
                                  header.getClock().getMultiplier());
        Assertions.assertNotNull(propagator2);
        final List<StateCovariance> covariances2 =
            covariancePropagation(satellite2, propagator2, stateCovariance,
                                  header.getClock().getMultiplier());

        // Collision
        final Collision collisionBuilder =
            Collision
                .builder(satellite1, satellite2, covariances1, covariances2,
                         LOFType.TNW, LOFType.TNW)
                .withCustomId("CustomID").build();

        // Reference file
        final String builderPathFile =
            loadResources("templateFile/object/primary/covariance/CollisionWithBuilderTemplate.txt");

        verifyFileOutput(builderPathFile, collisionBuilder.toString(), 1e-4);
    }

    private Map<Propagator, BoundedPropagator>
        propagatorFromOrbit(final AbsoluteDate startDate,
                            final AbsoluteDate finalDate,
                            final KeplerianOrbit initialOrbit) {

        final double[][] tolerances1 =
            NumericalPropagator.tolerances(10, initialOrbit,
                                           OrbitType.CARTESIAN);

        final SpacecraftState initialState = new SpacecraftState(initialOrbit);
        final AdaptiveStepsizeIntegrator integrator1 =
            new DormandPrince853Integrator(0.001, 1000.0, tolerances1[0],
                                           tolerances1[1]);
        final NumericalPropagator propagator =
            new NumericalPropagator(integrator1);
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
        final Map<Propagator, BoundedPropagator> mapToReturn = new HashMap<>();
        mapToReturn.put(propagator, boundedPropagator);
        return mapToReturn;
    }
}
