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

import cesiumlanguagewriter.Reference;
import org.hipparchus.ode.nonstiff.AdaptiveStepsizeIntegrator;
import org.hipparchus.ode.nonstiff.DormandPrince853Integrator;
import org.hipparchus.util.FastMath;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.czml.errors.OresiumException;
import org.orekit.czml.errors.OresiumMessages;
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
import org.orekit.orbits.Orbit;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The type Inter sat visu test.
 */
class InterSatVisuTest
    extends
    AbstractTest {

    final double data = initializeOrekitData();

    // Dates

    /** Start date. */
    final AbsoluteDate startDate =
        new AbsoluteDate(2024, 3, 15, 0, 0, 0.0, TimeScalesFactory.getUTC());

    /** Final date. */
    final AbsoluteDate finalDate = startDate.shiftedBy(32 * 3600);

    /** Clock. */
    final Clock clock = new Clock(startDate, finalDate, 10.0);

    /** Header. */
    final Header header = new Header("Test constructors inter sat", clock);

    // Orbits

    /** First orbit. */
    final KeplerianOrbit firstOrbit =
        new KeplerianOrbit(7878000, 0, FastMath.toRadians(0),
                           FastMath.toRadians(0), FastMath.toRadians(0),
                           FastMath.toRadians(0), PositionAngleType.MEAN,
                           FramesFactory.getEME2000(), startDate,
                           Constants.WGS84_EARTH_MU);

    /** Second orbit. */
    final KeplerianOrbit secondOrbit =
        new KeplerianOrbit(7078000, 0, FastMath.toRadians(20),
                           FastMath.toRadians(45), FastMath.toRadians(0),
                           FastMath.toRadians(0), PositionAngleType.MEAN,
                           FramesFactory.getEME2000(), startDate,
                           Constants.WGS84_EARTH_MU);

    final Spacecraft firstSpacecraft =
        Spacecraft
            .builder(dummyPropagator(startDate, finalDate, firstOrbit), clock)
            .build();

    final Spacecraft secondSpacecraft =
        Spacecraft
            .builder(dummyPropagator(startDate, finalDate, secondOrbit), clock)
            .build();

    final InterSatVisu dummyInterSatVisu =
        InterSatVisu
            .builder(firstSpacecraft, secondSpacecraft, finalDate, clock)
            .build();

    static Constellation constellation = null;

    static InterSatVisu interSatVisuConstellation = null;

    /** Empty constructor. */
    public InterSatVisuTest() {
    }

    @BeforeEach
    void setup() {
        final List<BoundedPropagator> propagators = new ArrayList<>();
        final Spacecraft firstSpacecraft =
            Spacecraft
                .builder(dummyPropagator(startDate, finalDate, firstOrbit),
                         clock)
                .build();
        final Spacecraft secondSpacecraft =
            Spacecraft
                .builder(dummyPropagator(startDate, finalDate, secondOrbit),
                         clock)
                .build();
        propagators.add(firstSpacecraft.getSpacecraftBoundedPropagator());
        propagators.add(secondSpacecraft.getSpacecraftBoundedPropagator());
        constellation =
            Constellation.builder(propagators, finalDate, clock).build();
        interSatVisuConstellation =
            InterSatVisu.builder(constellation, finalDate, clock).build();
    }

    /**
     * Inter sat visu constructor test. *
     */
    @Test
    @DisplayName("Inter sat visu constructor test")
    void interSatVisuConstructorTest() {

        final Spacecraft firstSat =
            spacecraftFromOrbit(startDate, finalDate, firstOrbit);
        final Spacecraft secondSat =
            spacecraftFromOrbit(startDate, finalDate, secondOrbit);
        firstSat.setOrbitColor(new Color(255, 0, 0, 255));
        secondSat.setOrbitColor(new Color(255, 127, 0, 255));

        final InterSatVisu interSatVisu =
            InterSatVisu
                .builder(firstSat, secondSat, finalDate, header.getClock())
                .build();

        final String pathFile =
            loadResources("templateFile/object/primary/visu/intersatvisu/InterSatVisuTemplate.txt");

        verifyFileOutput(pathFile, interSatVisu.toString(), 1e-8);

        Assertions.assertEquals(
                                secondSat.getSpaceCraftStates().get(0)
                                    .getPVCoordinates().toString(),
                                interSatVisu.getSpacecraft2()
                                    .getSpaceCraftStates().get(0)
                                    .getPVCoordinates().toString());
        Assertions.assertEquals(startDate, interSatVisu.getStartDate());
        Assertions.assertEquals(finalDate, interSatVisu.getFinalDate());
        Assertions.assertEquals(
                                firstSat.getSpaceCraftStates().get(0)
                                    .getPVCoordinates().toString(),
                                interSatVisu.getInitialState()
                                    .getPVCoordinates().toString());
        Assertions
            .assertEquals(Polyline.nonVectorBuilder(header.getClock()).build()
                .getClock().toString(),
                          interSatVisu.getPolyline().getClock().toString());
        Assertions.assertEquals(new ArrayList<>(Arrays
            .asList(true, false, true, false, true, false, true)),
                                interSatVisu.getBooleanList());

    }

    @Test
    @DisplayName("Inter sat visu with builder constructor test")
    void interSatVisuBuilderConstructorTets() {

        // Bounded Propagators
        final Spacecraft spacecraft =
            spacecraftFromOrbit(startDate, finalDate, firstOrbit);
        final Spacecraft spacecraft2 =
            spacecraftFromOrbit(startDate, finalDate, secondOrbit);

        final String pathFile =
            loadResources("templateFile/object/primary/visu/intersatvisu/InterSatVisuTemplate.txt");
        final InterSatVisu interSatVisuBuilder =
            InterSatVisu
                .builder(spacecraft, spacecraft2, finalDate, header.getClock())
                .build();
        verifyFileOutput(pathFile, interSatVisuBuilder.toString(), 1e-8);
    }

    @Test
    @DisplayName("Inter Sat visu with propagators")
    void interSatVisuPropagatorConstructorTest() {

        // Spacecrafts
        final Spacecraft spacecraft1 =
            spacecraftFromOrbit(startDate, finalDate, firstOrbit);
        final Spacecraft spacecraft2 =
            spacecraftFromOrbit(startDate, finalDate, secondOrbit);

        // Propagators
        final BoundedPropagator propagator1 =
            spacecraft1.getSpacecraftBoundedPropagator();
        final BoundedPropagator propagator2 =
            spacecraft2.getSpacecraftBoundedPropagator();
        final List<BoundedPropagator> propagators = new ArrayList<>();
        propagators.add(propagator1);
        propagators.add(propagator2);

        // Build inter sat visu with propagators
        final InterSatVisu interSatVisuPropagators =
            InterSatVisu.builder(propagators, finalDate, header.getClock())
                .build();

        // Reference files
        final String propagatorsInterSatPathFile =
            loadResources("templateFile/object/primary/visu/intersatvisu/InterSatVisuPropagatorsTemplate.txt");

        verifyFileOutput(propagatorsInterSatPathFile,
                         interSatVisuPropagators.toString(), 1e-8);
        Assertions.assertEquals(propagators,
                                interSatVisuPropagators.getPropagators());
    }

    @Test
    @DisplayName("Inter Sat visu with builder and propagators")
    void interSatVisuBuilderPropagatorConstructorTest() {

        // Spacecrafts
        final Spacecraft spacecraft1 =
            spacecraftFromOrbit(startDate, finalDate, firstOrbit);
        final Spacecraft spacecraft2 =
            spacecraftFromOrbit(startDate, finalDate, secondOrbit);

        // Propagators
        final BoundedPropagator propagator1 =
            spacecraft1.getSpacecraftBoundedPropagator();
        final BoundedPropagator propagator2 =
            spacecraft2.getSpacecraftBoundedPropagator();
        final List<BoundedPropagator> propagators = new ArrayList<>();
        propagators.add(propagator1);
        propagators.add(propagator2);

        // Build the inter sat visu with builder and propagators
        final InterSatVisu interSatVisuPropagatorsBuilder =
            InterSatVisu.builder(propagators, finalDate, header.getClock())
                .build();

        // Reference file
        final String propagatorsInterSatPathFile =
            loadResources("templateFile/object/primary/visu/intersatvisu/InterSatVisuPropagatorsTemplate.txt");

        verifyFileOutput(propagatorsInterSatPathFile,
                         interSatVisuPropagatorsBuilder.toString(), 1e-8);
    }

    @Test
    @DisplayName("Inter sat visu with builder propagators and ID")
    void interSatVisuBuilderPropagatorsIdConstructorTest() {

        // Spacecrafts
        final Spacecraft spacecraft1 =
            spacecraftFromOrbit(startDate, finalDate, firstOrbit);
        final Spacecraft spacecraft2 =
            spacecraftFromOrbit(startDate, finalDate, secondOrbit);

        // Propagators
        final BoundedPropagator propagator1 =
            spacecraft1.getSpacecraftBoundedPropagator();
        final BoundedPropagator propagator2 =
            spacecraft2.getSpacecraftBoundedPropagator();
        final List<BoundedPropagator> propagators = new ArrayList<>();
        propagators.add(propagator1);
        propagators.add(propagator2);

        // Build the inter sat visu with propagators and ID
        final InterSatVisu interSatVisuPropagatorsID =
            InterSatVisu.builder(propagators, finalDate, header.getClock())
                .withCustomId("CustomIDTest").build();

        // Reference file
        final String propagatorsIDInterSatPathFile =
            loadResources("templateFile/object/primary/visu/intersatvisu/InterSatVisuPropagatorsIDTemplate.txt");

        verifyFileOutput(propagatorsIDInterSatPathFile,
                         interSatVisuPropagatorsID.toString(), 1e-8);
    }

    @Test
    @DisplayName("Inter sat visu constellation builder")
    void interSatVisuConstellationBuilder() {

        // Spacecrafts
        final Spacecraft spacecraft1 =
            spacecraftFromOrbit(startDate, finalDate, firstOrbit);
        final Spacecraft spacecraft2 =
            spacecraftFromOrbit(startDate, finalDate, secondOrbit);

        // Propagators
        final BoundedPropagator propagator1 =
            spacecraft1.getSpacecraftBoundedPropagator();
        final BoundedPropagator propagator2 =
            spacecraft2.getSpacecraftBoundedPropagator();
        final List<BoundedPropagator> propagators = new ArrayList<>();
        propagators.add(propagator1);
        propagators.add(propagator2);

        final InterSatVisu interSatVisuFromConstellationBuilder =
            InterSatVisu.builder(Constellation
                .builder(propagators, finalDate, header.getClock()).build(),
                                 finalDate, header.getClock())
                .build();

        // Reference file
        final String constellationInterSatPathFile =
            loadResources("templateFile/object/primary/visu/intersatvisu/InterSatVisuConstellationTemplate.txt");

        verifyFileOutput(constellationInterSatPathFile,
                         interSatVisuFromConstellationBuilder.toString(), 1e-8);
        Assertions.assertEquals(new ArrayList<>(Arrays
            .asList(spacecraft1.getId(), spacecraft2.getId())),
                                interSatVisuFromConstellationBuilder
                                    .getIdsSatellites());
    }

    @Test
    @DisplayName("Inter sat visu from constructor")
    void interSatVisuFromConstructor() {

        // Spacecrafts
        final Spacecraft spacecraft1 =
            spacecraftFromOrbit(startDate, finalDate, firstOrbit);
        final Spacecraft spacecraft2 =
            spacecraftFromOrbit(startDate, finalDate, secondOrbit);

        // Builder the inter sat visu from the constructor
        final InterSatVisu interSatVisuFromConstructor =
            new InterSatVisu(spacecraft1, spacecraft2, finalDate);

        // Reference file
        final String constructorInterSatPathFile =
            loadResources("templateFile/object/primary/visu/intersatvisu/InterSatVisuFromConstructorTemplate.txt");

        verifyFileOutput(constructorInterSatPathFile,
                         interSatVisuFromConstructor.toString(), 1e-8);
    }

    @Test
    @DisplayName("Inter Sat Visu from propagators constructors")
    void interSatVisuPropagatorsConstructorTest() {

        // Spacecrafts
        final Spacecraft spacecraft1 =
            spacecraftFromOrbit(startDate, finalDate, firstOrbit);
        final Spacecraft spacecraft2 =
            spacecraftFromOrbit(startDate, finalDate, secondOrbit);

        // Propagators
        final BoundedPropagator propagator1 =
            spacecraft1.getSpacecraftBoundedPropagator();
        final BoundedPropagator propagator2 =
            spacecraft2.getSpacecraftBoundedPropagator();
        final List<BoundedPropagator> propagators = new ArrayList<>();
        propagators.add(propagator1);
        propagators.add(propagator2);

        // Build the inter sat visu from propagator
        final InterSatVisu interSatVisuFromPropagators =
            new InterSatVisu(propagators, finalDate, clock);

        // Reference file
        final String constructorPropagatorsInterSatPathFile =
            loadResources("templateFile/object/primary/visu/intersatvisu/InterSatVisuFromPropagatorsConstructorTemplate.txt");

        verifyFileOutput(constructorPropagatorsInterSatPathFile,
                         interSatVisuFromPropagators.toString(), 1e-8);
    }

    @Test
    @DisplayName("Inter sat visu from constellation constructor")
    void interSatVisuConstellationConstructorTest() {

        // Reference file
        final String constructorConstellationInterSatPathFile =
            loadResources("templateFile/object/primary/visu/intersatvisu/InterSatVisuFromConstellationConstructorTemplate.txt");

        verifyFileOutput(constructorConstellationInterSatPathFile,
                         interSatVisuConstellation.toString(), 1e-8);
    }

    /** Test for the propagators builder for inter sat visu. */
    @Test
    void insertSatVisuPropagatorsBuilderConstructorTest() {
        final List<BoundedPropagator> propagators = new ArrayList<>();
        propagators.add(dummyPropagator(startDate, finalDate, firstOrbit));
        propagators.add(dummyPropagator(startDate, finalDate, secondOrbit));

        final InterSatVisu interSatVisu =
            new InterSatVisu(propagators, finalDate, "10", clock);

        final String templateFile =
            loadResources("templateFile/object/primary/visu/intersatvisu/InterSatVisuPropagatorBuilderConstructorTemplate.txt");

        verifyFileOutput(templateFile, interSatVisu.toString(), 1e-8);
    }

    @Test
    void testCloneObject() {
        final Spacecraft spacecraft =
            Spacecraft
                .builder(dummyPropagator(startDate, finalDate, firstOrbit),
                         clock)
                .build();
        final Spacecraft spacecraft2 =
            Spacecraft
                .builder(dummyPropagator(startDate, finalDate, secondOrbit),
                         clock)
                .build();
        final InterSatVisu interSatVisu =
            InterSatVisu.builder(spacecraft, spacecraft2, finalDate, clock)
                .build();

        final InterSatVisu clonedIntersatVisu = interSatVisu.cloneObject();

        Assertions.assertEquals(interSatVisu.toString(),
                                clonedIntersatVisu.toString());
    }

    @Test
    void testClonedConstellationObject() {

        final InterSatVisu clonedIntersatVisu =
            interSatVisuConstellation.cloneObject();

        final List<BoundedPropagator> propagators =
            interSatVisuConstellation.getPropagators();
        interSatVisuConstellation.setPropagators(new ArrayList<>());

        Assertions.assertEquals(clonedIntersatVisu.toString(),
                                interSatVisuConstellation.toString());

        interSatVisuConstellation.setPropagators(propagators);
    }

    @Test
    @DisplayName("Cloning error : No spacecraft, no constellation, no propagators")
    void testCloningError() {
        final InterSatVisu interSatVisuEmpty =
            new InterSatVisu(new ArrayList<>(), finalDate, clock);

        // Assertions
        Assertions.assertThrows(OresiumException.class,
                                interSatVisuEmpty::cloneObject);
    }

    @Nested
    class GetterSetterTests {

        @Test
        void spacecraft1Test() {
            Assertions
                .assertEquals(dummyInterSatVisu.getSpacecraft1().toString(),
                              firstSpacecraft.toString());
        }

        @Test
        void spacecraft2Test() {
            Assertions
                .assertEquals(dummyInterSatVisu.getSpacecraft2().toString(),
                              secondSpacecraft.toString());
        }

        @Test
        void getReferencesTest() {
            final Reference reference1 =
                new Reference("SPACECRAFT/{P(7.87800000e+06, 0.00000000e+00, 0.00000000e+00), V(-0.00000000e+00, 7.11313252e+03, 0.00000000e+00)}#position");
            Assertions.assertEquals(dummyInterSatVisu.getReferences().iterator()
                .next(), reference1);
        }

        @Test
        void constellationTest() {

            Assertions.assertEquals(constellation.toString(),
                                    interSatVisuConstellation.getConstellation()
                                        .toString());
        }

        @Test
        void orbitsTest() {
            final List<Orbit> orbits = new ArrayList<>();
            orbits.add(firstSpacecraft.getOrbits().get(0));
            orbits.add(secondSpacecraft.getOrbits().get(0));
            Assertions
                .assertEquals(interSatVisuConstellation.getOrbits().toString(),
                              orbits.toString());
        }

        @Test
        @DisplayName("Get polyline")
        void getPolylineTest() {
            // The polyline created by the InterSatVisu should not be null
            Assertions.assertNotNull(dummyInterSatVisu.getPolyline());
            // The polyline's clock should match the polyline built with the
            // same clock
            final Polyline expectedPolyline =
                Polyline.nonVectorBuilder(clock).build();
            Assertions.assertEquals(expectedPolyline.getClock().toString(),
                                    dummyInterSatVisu.getPolyline().getClock()
                                        .toString());
            Assertions.assertEquals(expectedPolyline.getClock().toString(),
                                    dummyInterSatVisu.getPolyline().getClock()
                                        .toString());
            Assertions.assertEquals(expectedPolyline.getClock().getMultiplier(),
                                    dummyInterSatVisu.getPolyline().getClock()
                                        .getMultiplier());
        }

        @Test
        @DisplayName("Get initial state")
        void getInitialStateTest() {
            // The initial state should be the first state of the first
            // spacecraft
            final SpacecraftState expectedState =
                firstSpacecraft.getSpaceCraftStates().get(0);
            Assertions
                .assertEquals(expectedState.getDate(),
                              dummyInterSatVisu.getInitialState().getDate());
            Assertions.assertEquals(expectedState.getOrbit().getA(),
                                    dummyInterSatVisu.getInitialState()
                                        .getOrbit().getA(),
                                    1e-10);
        }

        @Test
        @DisplayName("Get body")
        void getBodyTest() {
            // The body should not be null
            final OneAxisEllipsoid body = dummyInterSatVisu.getBody();
            Assertions.assertNotNull(body);
            // Verify it's the WGS84 Earth ellipsoid
            Assertions.assertEquals(Constants.WGS84_EARTH_EQUATORIAL_RADIUS,
                                    body.getEquatorialRadius(), 1e-10);
            Assertions.assertEquals(Constants.WGS84_EARTH_FLATTENING,
                                    body.getFlattening(), 1e-10);
        }

        @Test
        @DisplayName("Get polyline from constellation")
        void getPolylineConstellationTest() {
            // For constellation-based InterSatVisu, polylines are stored per
            // pair
            // The polyline inside the visu block is accessed via writeCzmlBlock
            Assertions.assertNotNull(interSatVisuConstellation.toString());
        }

        @Test
        @DisplayName("Get final date")
        void getFinalDateTest() {
            Assertions.assertEquals(finalDate,
                                    dummyInterSatVisu.getFinalDate());
        }

        @Test
        @DisplayName("Set propagators")
        void setPropagatorsTest() {
            final List<BoundedPropagator> newPropagators = new ArrayList<>();
            newPropagators
                .add(dummyPropagator(startDate, finalDate, firstOrbit));
            newPropagators
                .add(dummyPropagator(startDate, finalDate, secondOrbit));

            final InterSatVisu interSatVisu =
                InterSatVisu.builder(firstSpacecraft, secondSpacecraft,
                                     finalDate, clock)
                    .build();

            interSatVisu.setPropagators(newPropagators);
            Assertions.assertEquals(newPropagators,
                                    interSatVisu.getPropagators());
        }
    }

    @Test
    @DisplayName("Clone from constellation path")
    void testCloneFromConstellationPath() {
        // Create InterSatVisu from propagators (which internally creates a
        // constellation)
        final List<BoundedPropagator> propagators = new ArrayList<>();
        final Spacecraft spacecraft1 =
            spacecraftFromOrbit(startDate, finalDate, firstOrbit);
        final Spacecraft spacecraft2 =
            spacecraftFromOrbit(startDate, finalDate, secondOrbit);
        propagators.add(spacecraft1.getSpacecraftBoundedPropagator());
        propagators.add(spacecraft2.getSpacecraftBoundedPropagator());

        final InterSatVisu interSatVisuFromPropagators =
            new InterSatVisu(propagators, finalDate, clock);

        // Clear the propagators list so the propagators clone path is skipped
        final List<BoundedPropagator> savedPropagators =
            new ArrayList<>(interSatVisuFromPropagators.getPropagators());
        interSatVisuFromPropagators.setPropagators(new ArrayList<>());

        // Clone should now go through the constellation path
        final InterSatVisu cloned = interSatVisuFromPropagators.cloneObject();
        Assertions.assertNotNull(cloned);
        Assertions.assertEquals(interSatVisuFromPropagators.getName(),
                                cloned.getName());

        // Restore propagators
        interSatVisuFromPropagators.setPropagators(savedPropagators);
    }

    @Test
    @DisplayName("Write CZML block with two satellites")
    void testWriteCzmlBlockTwoSatellites() {
        // The writeCzmlBlock method for two satellites (constellationSatellites
        // is empty)
        // should produce valid output that equals toString()
        final Spacecraft firstSat =
            spacecraftFromOrbit(startDate, finalDate, firstOrbit);
        final Spacecraft secondSat =
            spacecraftFromOrbit(startDate, finalDate, secondOrbit);

        final InterSatVisu interSatVisu =
            InterSatVisu.builder(firstSat, secondSat, finalDate, clock).build();

        final String czmlOutput = interSatVisu.toString();
        Assertions.assertNotNull(czmlOutput);
        Assertions.assertTrue(czmlOutput.contains("\"id\":\"INTER_SAT_VISU/"));
    }

    @Test
    @DisplayName("Write CZML block with constellation")
    void testWriteCzmlBlockConstellation() {
        // The writeCzmlBlock method for constellation should produce valid
        // output
        final String czmlOutput = interSatVisuConstellation.toString();
        Assertions.assertNotNull(czmlOutput);

        // Verify constellation output contains expected structure
        Assertions.assertTrue(czmlOutput.contains("INTER_SAT_VISU/"));
        Assertions.assertTrue(czmlOutput
            .contains("Visualisation inter-constellation"));
    }

    @Test
    @DisplayName("Ids satellite getter")
    void testGetIdsSatellites() {
        final List<String> ids = interSatVisuConstellation.getIdsSatellites();
        Assertions.assertFalse(ids.isEmpty());
        Assertions.assertEquals(2, ids.size());
    }

    @Test
    @DisplayName("Start date getter from constellation")
    void testStartDateFromConstellation() {
        Assertions.assertNotNull(interSatVisuConstellation.getStartDate());
    }

    @Test
    @DisplayName("Boolean list from two-sat InterSatVisu")
    void testBooleanList() {
        // The boolean list should contain alternating true/false values
        // representing visibility
        final List<Boolean> booleanList = dummyInterSatVisu.getBooleanList();
        Assertions.assertNotNull(booleanList);
        Assertions.assertFalse(booleanList.isEmpty());
        // Should start with true (visible at initial time)
        Assertions.assertTrue(booleanList.get(0));
    }

    @Test
    @DisplayName("Propagators getter on propagators-based InterSatVisu")
    void testPropagatorsGetter() {
        final List<BoundedPropagator> propagators = new ArrayList<>();
        propagators.add(dummyPropagator(startDate, finalDate, firstOrbit));
        propagators.add(dummyPropagator(startDate, finalDate, secondOrbit));
        final InterSatVisu interSatVisuProp =
            new InterSatVisu(propagators, finalDate, clock);
        Assertions.assertEquals(propagators.size(),
                                interSatVisuProp.getPropagators().size());
    }

    @Test
    @DisplayName("Builder with clock override")
    void testBuilderWithClockOverride() {
        final Spacecraft firstSat =
            spacecraftFromOrbit(startDate, finalDate, firstOrbit);
        final Spacecraft secondSat =
            spacecraftFromOrbit(startDate, finalDate, secondOrbit);
        final Clock overrideClock = new Clock(startDate, finalDate, 10.0);

        final InterSatVisu interSatVisu =
            InterSatVisu.builder(firstSat, secondSat, finalDate, clock)
                .withClock(overrideClock).build();

        final Polyline polyline = interSatVisu.getPolyline();
        Assertions.assertEquals(overrideClock.getMultiplier(),
                                polyline.getClock().getMultiplier());
    }

    @Test
    @DisplayName("Builder with custom ID")
    void testBuilderWithCustomId() {
        final Spacecraft firstSat =
            spacecraftFromOrbit(startDate, finalDate, firstOrbit);
        final Spacecraft secondSat =
            spacecraftFromOrbit(startDate, finalDate, secondOrbit);

        final InterSatVisu interSatVisu =
            InterSatVisu.builder(firstSat, secondSat, finalDate, clock)
                .withCustomId("CUSTOM_TEST_ID").build();

        Assertions
            .assertTrue(interSatVisu.toString().contains("CUSTOM_TEST_ID"));
    }

    @Test
    @DisplayName("Test references iterable")
    void testReferencesIterable() {
        // Test that getReferences() returns a non-empty iterable
        final Iterable<Reference> references =
            dummyInterSatVisu.getReferences();
        Assertions.assertNotNull(references);

        // Convert to list and verify it has the expected number of references
        final List<Reference> referenceList = new ArrayList<>();
        references.forEach(referenceList::add);
        Assertions.assertEquals(2, referenceList.size());
    }

    @Test
    @DisplayName("Test availability calculation with equal durations")
    void testFindMinimumAvailabilityEqualDurations() {
        // Create two spacecraft with equal availability durations
        final Spacecraft spacecraft1 =
            spacecraftFromOrbit(startDate, finalDate, firstOrbit);
        final Spacecraft spacecraft2 =
            spacecraftFromOrbit(startDate, finalDate, secondOrbit);

        // Create InterSatVisu to test the private method indirectly
        final InterSatVisu interSatVisu =
            new InterSatVisu(spacecraft1, spacecraft2, finalDate);

        // The start date should be the same as the spacecraft availability
        // start
        Assertions.assertEquals(startDate, interSatVisu.getStartDate());
    }

    @Test
    @DisplayName("Test event handler through propagation")
    void testEventHandlerThroughPropagation() {
        // Test that the event handler works by verifying that we get visibility
        // data
        // This indirectly tests the InterSatViewHandler functionality
        final List<Boolean> booleanList = dummyInterSatVisu.getBooleanList();
        Assertions.assertNotNull(booleanList);
        Assertions.assertFalse(booleanList.isEmpty());
    }

    @Test
    @DisplayName("Test reorganize satellite list")
    void testReorganizeSatelliteList() {
        // Create a constellation with 3 satellites to test the reorganization
        final List<BoundedPropagator> propagators = new ArrayList<>();
        final KeplerianOrbit thirdOrbit =
            new KeplerianOrbit(7500000, 0, FastMath.toRadians(40),
                               FastMath.toRadians(60), FastMath.toRadians(0),
                               FastMath.toRadians(0), PositionAngleType.MEAN,
                               FramesFactory.getEME2000(), startDate,
                               Constants.WGS84_EARTH_MU);

        propagators.add(dummyPropagator(startDate, finalDate, firstOrbit));
        propagators.add(dummyPropagator(startDate, finalDate, secondOrbit));
        propagators.add(dummyPropagator(startDate, finalDate, thirdOrbit));

        final Constellation testConstellation =
            Constellation.builder(propagators, finalDate, clock).build();
        final InterSatVisu interSatVisu =
            InterSatVisu.builder(testConstellation, finalDate, clock).build();

        // Should have 3 pairs for 3 satellites (n*(n-1)/2 = 3*2/2 = 3)
        Assertions.assertEquals(3,
                                interSatVisu.getConstellation()
                                    .getTotalOfSatellite() *
                                   (interSatVisu.getConstellation()
                                       .getTotalOfSatellite() -
                                    1) /
                                   2,
                                interSatVisu.getConstellation()
                                    .getTotalOfSatellite() > 1 ? 0.1 : 0);
    }

    @Test
    @DisplayName("Test visibility data generation")
    void testVisibilityDataGeneration() {
        // Test that visibility data is generated and has reasonable values
        final List<Boolean> booleanList = dummyInterSatVisu.getBooleanList();

        // Should have visibility data
        Assertions.assertNotNull(booleanList);
        Assertions.assertFalse(booleanList.isEmpty());

        // Should have alternating true/false values (visibility changes over
        // time)
        boolean hasTrue = false;
        boolean hasFalse = false;
        for (Boolean visibility : booleanList) {
            if (visibility)
                hasTrue = true;
            if (!visibility)
                hasFalse = true;
            if (hasTrue && hasFalse)
                break; // Found both true and false
        }

        // Should have both visible and non-visible periods
        Assertions.assertTrue(hasTrue, "Should have some visible periods");
        Assertions.assertTrue(hasFalse, "Should have some non-visible periods");
    }

    @Test
    @DisplayName("Test error when cloning with no valid objects")
    void testErrorWhenCloningWithNoValidObjects() {
        // Create an InterSatVisu with empty propagators
        final InterSatVisu interSatVisu =
            new InterSatVisu(new ArrayList<>(), finalDate, clock);

        // Should throw OresiumException
        final OresiumException exception =
            Assertions.assertThrows(OresiumException.class,
                                    interSatVisu::cloneObject);

        Assertions
            .assertEquals(OresiumMessages.NOT_VALID_PRIMARY_OBJECT_FOR_CLONE,
                          exception.getSpecifier());
    }

    @Test
    @DisplayName("Test getters for constellation-based InterSatVisu")
    void testGettersForConstellationBasedInterSatVisu() {
        // Test various getters on constellation-based InterSatVisu
        Assertions.assertNotNull(interSatVisuConstellation.getConstellation());
        Assertions.assertNotNull(interSatVisuConstellation.getIdsSatellites());
        Assertions.assertFalse(interSatVisuConstellation.getIdsSatellites()
            .isEmpty());
        Assertions.assertNotNull(interSatVisuConstellation.getOrbits());
        Assertions.assertFalse(interSatVisuConstellation.getOrbits().isEmpty());
        Assertions.assertNotNull(interSatVisuConstellation.getStartDate());
        Assertions.assertNotNull(interSatVisuConstellation.getFinalDate());
    }

    @Test
    @DisplayName("Test builder with all parameters")
    void testBuilderWithAllParameters() {
        final Spacecraft firstSat =
            spacecraftFromOrbit(startDate, finalDate, firstOrbit);
        final Spacecraft secondSat =
            spacecraftFromOrbit(startDate, finalDate, secondOrbit);
        final Clock customClock = new Clock(startDate, finalDate, 10.0);

        final InterSatVisu interSatVisu =
            InterSatVisu.builder(firstSat, secondSat, finalDate, clock)
                .withClock(customClock).withCustomId("FULL_BUILDER_TEST")
                .build();

        Assertions.assertEquals("FULL_BUILDER_TEST", interSatVisu.getId());
        Assertions.assertEquals(customClock.getMultiplier(), interSatVisu
            .getPolyline().getClock().getMultiplier());
    }

    @Test
    @DisplayName("Test time interval calculation")
    void testTimeIntervalCalculation() {
        final Spacecraft spacecraft1 =
            spacecraftFromOrbit(startDate, finalDate, firstOrbit);
        final Spacecraft spacecraft2 =
            spacecraftFromOrbit(startDate, finalDate, secondOrbit);

        final InterSatVisu interSatVisu =
            InterSatVisu.builder(spacecraft1, spacecraft2, finalDate, clock)
                .build();

        // Should have time intervals calculated
        Assertions.assertFalse(interSatVisu.getBooleanList().isEmpty());
    }

    private Spacecraft spacecraftFromOrbit(final AbsoluteDate startDate,
                                           final AbsoluteDate finalDate,
                                           final KeplerianOrbit orbit) {

        final SpacecraftState firstState = new SpacecraftState(orbit);

        final double[][] tolerances =
            NumericalPropagator.tolerances(10, orbit, OrbitType.CARTESIAN);

        final AdaptiveStepsizeIntegrator integrator =
            new DormandPrince853Integrator(0.001, 1000.0, tolerances[0],
                                           tolerances[1]);

        final NormalizedSphericalHarmonicsProvider provider =
            GravityFieldFactory.getNormalizedProvider(10, 10);
        final ForceModel holmesFeatherstone =
            new HolmesFeatherstoneAttractionModel(FramesFactory.getEME2000(),
                                                  provider);

        final NumericalPropagator propagator =
            new NumericalPropagator(integrator);

        propagator.setOrbitType(OrbitType.CARTESIAN);
        propagator.addForceModel(holmesFeatherstone);
        propagator.setInitialState(firstState);
        final EphemerisGenerator firstGenerator =
            propagator.getEphemerisGenerator();
        propagator.propagate(startDate, finalDate);
        final BoundedPropagator boundedPropagator =
            firstGenerator.getGeneratedEphemeris();
        return new Spacecraft(boundedPropagator, header.getClock());
    }
}
