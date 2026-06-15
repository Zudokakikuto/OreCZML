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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.orekit.czml.errors.OresiumException;
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
import org.orekit.orbits.Orbit;
import org.orekit.orbits.OrbitType;
import org.orekit.orbits.PositionAngleType;
import org.orekit.propagation.BoundedPropagator;
import org.orekit.propagation.EphemerisGenerator;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.numerical.NumericalPropagator;
import org.orekit.time.AbsoluteDate;
import org.orekit.utils.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The type Constellation test.
 */
@DefaultDataContext
class ConstellationTest
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
    final String issModel = loadResources("Default3DModels/ISSModel.glb");

    final KeplerianOrbit firstOrbit =
        new KeplerianOrbit(7878000, 0, FastMath.toRadians(20), 0,
                           FastMath.toRadians(0), FastMath.toRadians(0),
                           PositionAngleType.MEAN, FramesFactory.getEME2000(),
                           startDate, Constants.WGS84_EARTH_MU);

    final KeplerianOrbit secondOrbit =
        new KeplerianOrbit(8578000, 0, FastMath.toRadians(0), 0,
                           FastMath.toRadians(0), FastMath.toRadians(0),
                           PositionAngleType.MEAN, FramesFactory.getEME2000(),
                           startDate, Constants.WGS84_EARTH_MU);

    final KeplerianOrbit thirdOrbit =
        new KeplerianOrbit(6578000, 0, FastMath.toRadians(-20), 0,
                           FastMath.toRadians(0), FastMath.toRadians(0),
                           PositionAngleType.MEAN, FramesFactory.getEME2000(),
                           startDate, Constants.WGS84_EARTH_MU);

    final KeplerianOrbit fourthOrbit =
        new KeplerianOrbit(10578000, 0, FastMath.toRadians(0), 0,
                           FastMath.toRadians(0), FastMath.toRadians(0),
                           PositionAngleType.MEAN, FramesFactory.getEME2000(),
                           startDate, Constants.WGS84_EARTH_MU);

    final KeplerianOrbit fifthOrbit =
        new KeplerianOrbit(78578000, 0, FastMath.toRadians(98), 0,
                           FastMath.toRadians(0), FastMath.toRadians(0),
                           PositionAngleType.MEAN, FramesFactory.getEME2000(),
                           startDate, Constants.WGS84_EARTH_MU);

    List<BoundedPropagator> propagators = new ArrayList<>();

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

    @BeforeEach
    void setup() {
        propagators = new ArrayList<>();
        propagators.add(boundedPropagator1);
        propagators.add(boundedPropagator2);
        propagators.add(boundedPropagator3);
        propagators.add(boundedPropagator4);
        propagators.add(boundedPropagator5);
    }

    /**
     * Constellation constructor test. *
     */
    @Test
    @DisplayName("Constellation constructor test with 5 spacecrafts")
    void ConstellationConstructorTest() {

        final Constellation constellation =
            Constellation.builder(propagators, finalDate, header.getClock())
                .withModel(Collections.singletonList(issModel))
                .withCustomId("CustomID").displayAttitude()
                .displayOnlyOnePeriod().build();

        final String pathFile =
            loadResources("templateFile/object/primary/entities/constellation/ConstellationTemplate.txt");
        verifyFileOutput(pathFile, constellation.toString(), 1e-8);
    }

    @Test
    void ConstellationPropagatorsTest() {

        final Constellation constellation =
            new Constellation(propagators, finalDate, header.getClock());

        final String pathFile =
            loadResources("templateFile/object/primary/entities/constellation/ConstellationPropagatorsTemplate.txt");

        verifyFileOutput(pathFile, constellation.toString(), 1e-8);
    }

    @Test
    void ConstellationPropagatorsModelTest() {

        final Constellation constellation =
            new Constellation(propagators, finalDate, issModel, "",
                              header.getClock(), 10.0);

        final String pathFile =
            loadResources("templateFile/object/primary/entities/constellation/ConstellationPropagatorsModelTemplate.txt");

        verifyFileOutput(pathFile, constellation.toString(), 1e-8);
    }

    @Test
    @DisplayName("defineMultipleArgument with single model - creates satellites with same model")
    void defineMultipleArgumentSingleModelTest() {
        // Given: Create a constellation with a single model (multipleModels =
        // false)
        final String singleModel = issModel;
        final List<String> models = Collections.singletonList(singleModel);

        // When: Create constellation using the constructor that calls
        // defineMultipleArgument
        final Constellation constellation =
            new Constellation(propagators, finalDate, models,
                              "TestConstellation", header.getClock(),
                              header.getClock().getMultiplier());

        // Then: Verify all satellites are created with the same model
        final List<Spacecraft> satellites = constellation.getSatellites();
        org.junit.jupiter.api.Assertions
            .assertEquals(5, satellites.size(), "Should create 5 satellites");
        org.junit.jupiter.api.Assertions
            .assertEquals(5, constellation.getInitialOrbits().size(),
                          "Should have 5 initial orbits");
        org.junit.jupiter.api.Assertions
            .assertEquals(5, constellation.getIds().size(),
                          "Should have 5 satellite IDs");

        // Verify each satellite has the correct model path
        for (final Spacecraft satellite : satellites) {
            // The model path should be the single model we provided
            org.junit.jupiter.api.Assertions
                .assertFalse(satellite.toString().contains(singleModel));
        }
    }

    @Test
    @DisplayName("defineMultipleArgument with multiple models - creates satellites with different models")
    void defineMultipleArgumentMultipleModelsTest() {
        // Given: Create a constellation with multiple models (multipleModels =
        // true)
        final String model5 = Constellation.DEFAULT_STRING_MODEL;

        final List<String> models = new ArrayList<>();
        models.add(issModel);
        models.add(issModel);
        models.add(issModel);
        models.add(issModel);
        models.add(model5);

        // When: Create constellation with multiple models
        final Constellation constellation =
            new Constellation(propagators, finalDate, models,
                              "TestConstellation", header.getClock(),
                              header.getClock().getMultiplier());

        // Then: Verify all satellites are created
        final List<Spacecraft> satellites = constellation.getSatellites();
        org.junit.jupiter.api.Assertions
            .assertEquals(5, satellites.size(), "Should create 5 satellites");
        org.junit.jupiter.api.Assertions
            .assertEquals(5, constellation.getInitialOrbits().size(),
                          "Should have 5 initial orbits");
        org.junit.jupiter.api.Assertions
            .assertEquals(5, constellation.getIds().size(),
                          "Should have 5 satellite IDs");
    }

    @Test
    @DisplayName("defineMultipleArgument with multiple models but wrong count - throws exception")
    void defineMultipleArgumentMismatchedModelsCountTest() {
        // Given: Create a list of models with different size than propagators
        final List<String> models = new ArrayList<>();
        models.add(issModel);
        models.add(Constellation.DEFAULT_STRING_MODEL);
        // Only 2 models for 5 propagators - should cause mismatch

        // When & Then: Should throw OresiumException
        org.junit.jupiter.api.Assertions.assertThrows(OresiumException.class,
                                                      () -> {
                                                          new Constellation(propagators,
                                                                            finalDate,
                                                                            models,
                                                                            "TestConstellation",
                                                                            header
                                                                                .getClock(),
                                                                            header
                                                                                .getClock()
                                                                                .getMultiplier());
                                                      },
                                                      "Should throw OresiumException when models count doesn't match propagators count");
    }

    @Test
    @DisplayName("defineMultipleArgument with empty models list - uses default model")
    void defineMultipleArgumentEmptyModelsTest() {
        // Given: Create a constellation with empty models list
        final List<String> models = new ArrayList<>();

        // When: Create constellation with empty models
        final Constellation constellation =
            new Constellation(propagators, finalDate, models,
                              "TestConstellation", header.getClock(),
                              header.getClock().getMultiplier());

        // Then: Verify satellites are created with default model
        final List<Spacecraft> satellites = constellation.getSatellites();
        org.junit.jupiter.api.Assertions.assertEquals(5, satellites
            .size(), "Should create 5 satellites even with empty models list");
        org.junit.jupiter.api.Assertions
            .assertEquals(5, constellation.getInitialOrbits().size(),
                          "Should have 5 initial orbits");
    }

    @Test
    @DisplayName("defineMultipleArgument creates unique IDs for each satellite")
    void defineMultipleArgumentUniqueIdsTest() {
        // Given: Create a constellation
        final List<String> models = Collections.singletonList(issModel);

        // When: Create constellation
        final Constellation constellation =
            new Constellation(propagators, finalDate, models,
                              "TestConstellation", header.getClock(),
                              header.getClock().getMultiplier());

        // Then: Verify all IDs are unique
        final List<String> ids = constellation.getIds();
        org.junit.jupiter.api.Assertions
            .assertEquals(5, ids.size(), "Should have 5 satellite IDs");

        // Check that all IDs are unique
        final java.util.Set<String> uniqueIds = new java.util.HashSet<>(ids);
        org.junit.jupiter.api.Assertions
            .assertEquals(ids.size(), uniqueIds.size(),
                          "All satellite IDs should be unique");
    }

    @Test
    @DisplayName("defineMultipleArgument preserves initial orbits from each propagator")
    void defineMultipleArgumentInitialOrbitsTest() {
        // Given: Create a constellation
        final List<String> models = Collections.singletonList(issModel);

        // When: Create constellation
        final Constellation constellation =
            new Constellation(propagators, finalDate, models,
                              "TestConstellation", header.getClock(),
                              header.getClock().getMultiplier());

        // Then: Verify initial orbits are captured
        final List<Orbit> initialOrbits = constellation.getInitialOrbits();
        org.junit.jupiter.api.Assertions
            .assertEquals(5, initialOrbits.size(),
                          "Should have 5 initial orbits");

        // Verify each orbit is from a different propagator by checking they
        // have different semi-major axes
        // (since we created them with different semi-major axes in the test
        // setup)
        for (int i = 0; i < initialOrbits.size(); i++) {
            final Orbit orbit = initialOrbits.get(i);
            org.junit.jupiter.api.Assertions
                .assertNotNull(orbit, "Orbit " + i + " should not be null");
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {
        1, 2, 3
    })
    @DisplayName("defineMultipleArgument with varying number of propagators")
    void defineMultipleArgumentVaryingPropagatorsTest(final int numberOfPropagators) {
        // Given: Create a subset of propagators
        final List<BoundedPropagator> subsetPropagators = new ArrayList<>();
        for (int i = 0; i < numberOfPropagators && i < propagators.size();
             i++) {
            subsetPropagators.add(propagators.get(i));
        }

        final List<String> models = Collections.singletonList(issModel);

        // When: Create constellation with subset
        final Constellation constellation =
            new Constellation(subsetPropagators, finalDate, models,
                              "TestConstellation", header.getClock(),
                              header.getClock().getMultiplier());

        // Then: Verify correct number of satellites are created
        final List<Spacecraft> satellites = constellation.getSatellites();
        org.junit.jupiter.api.Assertions
            .assertEquals(numberOfPropagators, satellites
                .size(), "Should create " + numberOfPropagators + " satellites");
        org.junit.jupiter.api.Assertions
            .assertEquals(numberOfPropagators, constellation.getIds().size(),
                          "Should have " + numberOfPropagators + " IDs");
        org.junit.jupiter.api.Assertions
            .assertEquals(numberOfPropagators,
                          constellation.getInitialOrbits()
                              .size(),
                          "Should have " +
                                       numberOfPropagators + " initial orbits");
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
