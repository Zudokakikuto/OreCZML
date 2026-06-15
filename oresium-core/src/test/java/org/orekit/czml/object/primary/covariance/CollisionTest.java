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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The type Collision test.
 */
class CollisionTest
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

    /* Propagators . */

    /** Propagator 1. */
    Propagator propagator1 = null;

    /** Propagator 2. */
    Propagator propagator2 = null;

    /* Bounded propagators. */

    /** Bounded Propagator 1. */
    BoundedPropagator boundedPropagator1 = null;

    /** Bounded Propagator 2. */
    BoundedPropagator boundedPropagator2 = null;

    /* Spacecrafts. */

    /** Spacecraft 1. */
    Spacecraft spacecraft1 = null;

    /** Spacecraft 2. */
    Spacecraft spacecraft2 = null;

    /* State covariance. */

    /** State covariance 1. */
    List<StateCovariance> covariances1 = null;

    /** State covariance 2. */
    List<StateCovariance> covariances2 = null;

    /** Collision coverage. */
    Collision collisionCoverage = null;

    @BeforeEach
    void setup() {
        // Bounded Propagators for spacecrafts
        final Map<Propagator, BoundedPropagator> mapPropagators1 =
            propagatorFromOrbit(startDate, finalDate, initialOrbit1);
        final Map<Propagator, BoundedPropagator> mapPropagators2 =
            propagatorFromOrbit(startDate, finalDate, initialOrbit2);

        final Optional<Propagator> propagatorOptional1 =
            mapPropagators1.keySet().stream().findFirst();
        final Optional<Propagator> propagatorOptional2 =
            mapPropagators2.keySet().stream().findFirst();

        propagatorOptional1.ifPresent(propagator -> propagator1 = propagator);
        propagatorOptional2.ifPresent(propagator -> propagator2 = propagator);

        boundedPropagator1 = mapPropagators1.get(propagator1);
        boundedPropagator2 = mapPropagators2.get(propagator2);

        // Spacecrafts
        spacecraft1 =
            Spacecraft
                .builder(boundedPropagator1, header.getClock().getMultiplier())
                .withOnlyOnePeriod().build();
        spacecraft2 =
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
        covariances1 =
            covariancePropagation(spacecraft1, propagator1, stateCovariance,
                                  header.getClock().getMultiplier());
        covariances2 =
            covariancePropagation(spacecraft2, propagator2, stateCovariance,
                                  header.getClock().getMultiplier());

        /** Collision coverage. */
        collisionCoverage =
            Collision.builder(spacecraft1, spacecraft2, covariances1,
                              covariances2, LOFType.TNW, LOFType.TNW)
                .build();
    }

    /** Basic constructor test. */
    @DisplayName("Basic Collision constructor test")
    @Test
    void BasicConstructorTest() {

        final Collision collision =
            new Collision(spacecraft1, spacecraft2, covariances1, covariances2,
                          LOFType.TNW, LOFType.TNW);

        final String pathFile =
            loadResources("templateFile/object/primary/covariances/collision/CollisionBasicConstructorTemplate.txt");

        verifyFileOutput(pathFile, collision.toString(), 1e-4);
    }

    /**
     * Collision constructor test. *
     */
    @DisplayName("Collision constructor test")
    @Test
    @DefaultDataContext
    void CollisionConstructorTest() {

        // Collision object
        final Collision collision =
            Collision.builder(spacecraft1, spacecraft2, covariances1,
                              covariances2, LOFType.TNW, LOFType.TNW)
                .build();

        // Reference file
        final String pathFile =
            loadResources("templateFile/object/primary/covariances/collision/CollisionTemplate.txt");

        // Assertions
        Assertions.assertNotNull(propagator1);
        Assertions.assertNotNull(propagator2);
        verifyFileOutput(pathFile, collision.toString(), 1e-4);
    }

    /**
     * Collision Builder constructor test. *
     */
    @DisplayName("Collision Builder constructor test")
    @Test
    @DefaultDataContext
    void CollisionBuilderConstructorTest() {

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
            loadResources("templateFile/object/primary/covariances/collision/CollisionWithBuilderTemplate.txt");

        verifyFileOutput(builderPathFile, collisionBuilder.toString(), 1e-4);
    }

    @Nested
    public class GetterSetterTests {

        @Test
        public void CovarianceFirstSatelliteTest() {
            final Covariance covariance =
                new Covariance(spacecraft1, covariances1, LOFType.TNW);

            Assertions.assertEquals(covariance.toString(), collisionCoverage
                .getCovarianceFirstSatellite().toString());

            final Covariance covarianceToSet =
                new Covariance(spacecraft2, covariances2, LOFType.TNW);
            collisionCoverage.setCovarianceFirstSatellite(covarianceToSet);

            Assertions.assertEquals(covarianceToSet.toString(),
                                    collisionCoverage
                                        .getCovarianceFirstSatellite()
                                        .toString());

            collisionCoverage.setCovarianceFirstSatellite(covariance);
        }

        @Test
        public void CovarianceSecondSatelliteTest() {
            final Covariance covariance =
                new Covariance(spacecraft2, covariances2, LOFType.TNW);

            Assertions.assertEquals(covariance.toString(), collisionCoverage
                .getCovarianceSecondSatellite().toString());

            final Covariance covarianceToSet =
                new Covariance(spacecraft1, covariances1, LOFType.TNW);
            collisionCoverage.setCovarianceFirstSatellite(covarianceToSet);

            Assertions.assertEquals(covarianceToSet.toString(),
                                    collisionCoverage
                                        .getCovarianceFirstSatellite()
                                        .toString());

            collisionCoverage.setCovarianceFirstSatellite(covariance);
        }

        @Test
        public void FirstSpacecraftTest() {
            Assertions.assertEquals(spacecraft1,
                                    collisionCoverage.getFirstSpacecraft());

            collisionCoverage.setFirstSpacecraft(spacecraft2);

            Assertions.assertEquals(spacecraft2,
                                    collisionCoverage.getFirstSpacecraft());

            collisionCoverage.setFirstSpacecraft(spacecraft1);
        }

        @Test
        public void SecondSpacecraftTest() {
            Assertions.assertEquals(spacecraft2,
                                    collisionCoverage.getSecondSpacecraft());

            collisionCoverage.setSecondSpacecraft(spacecraft1);

            Assertions.assertEquals(spacecraft1,
                                    collisionCoverage.getFirstSpacecraft());

            collisionCoverage.setSecondSpacecraft(spacecraft2);
        }

        @Test
        public void FirstCovarianceListTest() {
            Assertions.assertEquals(covariances1,
                                    collisionCoverage.getFirstCovarianceList());

            collisionCoverage.setFirstCovarianceList(covariances2);

            Assertions.assertEquals(covariances2,
                                    collisionCoverage.getFirstCovarianceList());

            collisionCoverage.setFirstCovarianceList(covariances1);
        }

        @Test
        public void SecondCovarianceListTest() {
            Assertions
                .assertEquals(covariances2,
                              collisionCoverage.getSecondCovarianceList());

            collisionCoverage.setSecondCovarianceList(covariances1);

            Assertions.assertEquals(covariances1,
                                    collisionCoverage.getFirstCovarianceList());

            collisionCoverage.setSecondCovarianceList(covariances2);
        }

        @Test
        public void firstLOF() {
            Assertions.assertEquals(LOFType.TNW,
                                    collisionCoverage.getFirstLof());

            collisionCoverage.setFirstLof(LOFType.ENU);

            Assertions.assertEquals(LOFType.ENU,
                                    collisionCoverage.getFirstLof());

            collisionCoverage.setFirstLof(LOFType.TNW);
        }

        @Test
        public void secondLOF() {
            Assertions.assertEquals(LOFType.TNW,
                                    collisionCoverage.getSecondLof());

            collisionCoverage.setSecondLof(LOFType.ENU);

            Assertions.assertEquals(LOFType.ENU,
                                    collisionCoverage.getSecondLof());

            collisionCoverage.setSecondLof(LOFType.TNW);
        }
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
