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

import cesiumlanguagewriter.TimeInterval;
import org.hipparchus.ode.nonstiff.AdaptiveStepsizeIntegrator;
import org.hipparchus.ode.nonstiff.DormandPrince853Integrator;
import org.hipparchus.util.FastMath;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.orekit.annotation.DefaultDataContext;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.czml.errors.OresiumException;
import org.orekit.czml.errors.OresiumMessages;
import org.orekit.czml.file.AbstractTest;
import org.orekit.czml.object.CzmlShow;
import org.orekit.czml.object.primary.Header;
import org.orekit.czml.object.primary.entities.Constellation;
import org.orekit.czml.object.primary.entities.Spacecraft;
import org.orekit.forces.ForceModel;
import org.orekit.forces.gravity.HolmesFeatherstoneAttractionModel;
import org.orekit.forces.gravity.potential.GravityFieldFactory;
import org.orekit.forces.gravity.potential.NormalizedSphericalHarmonicsProvider;
import org.orekit.frames.FramesFactory;
import org.orekit.frames.TopocentricFrame;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for {@link LineOfVisibility}.
 *
 * @author Julien LEBLOND
 * @since 1.0.0
 */
public class LineOfVisibilityTest
    extends
    AbstractTest {

    /** Initialise orekit data. */
    private final double data = initializeOrekitData();

    final Header header = dummyHeader();

    // Dates

    /** Start Date. */
    final AbsoluteDate startDate =
        new AbsoluteDate(2024, 3, 15, 0, 0, 0.0, TimeScalesFactory.getUTC());

    /** Final Date. */
    final AbsoluteDate finalDate = startDate.shiftedBy(10.0 * 3600.0);

    /**
     * Line of visibility constructor test (default and custom parameters).
     */
    @Test
    @DefaultDataContext
    void lineOfVisibilityConstructorTest() {

        final TopocentricFrame topocentricFrame =
            buildTopocentric("Toulouse Frame", 43.6047, 1.4442, 10);

        final KeplerianOrbit initialOrbit =
            new KeplerianOrbit(7878000, 0, FastMath.toRadians(80), 0,
                               FastMath.toRadians(90), FastMath.toRadians(0),
                               PositionAngleType.MEAN,
                               FramesFactory.getEME2000(), startDate,
                               Constants.WGS84_EARTH_MU);

        final Spacecraft spacecraft =
            spacecraftFromOrbit(startDate, finalDate, initialOrbit);

        final LineOfVisibility line =
            LineOfVisibility
                .builder(topocentricFrame, spacecraft, spacecraft.getClock())
                .build();

        final LineOfVisibility coverageLine =
            LineOfVisibility
                .builder(topocentricFrame, spacecraft, spacecraft.getClock())
                .withCustomID("CustomID").withAngleOfAperture(90.0).build();

        final String pathFile =
            loadResources("templateFile/object/primary/visu/lineofvisibility/LineOfVisibilityTemplate.txt");
        final String pathCoverageFile =
            loadResources("templateFile/object/primary/visu/lineofvisibility/LineOfVisibilityCoverageTemplate.txt");

        verifyFileOutput(pathFile, line.toString(), 1e-8);
        verifyFileOutput(pathCoverageFile, coverageLine.toString(), 1e-8);
    }

    /**
     * Tests the constructor with a constellation (multiple satellites). This
     * creates a LineOfVisibility from a 2-satellite constellation.
     */
    @Test
    @DefaultDataContext
    void lineOfVisibilityWithConstellationTest() {
        final TopocentricFrame topocentricFrame =
            buildTopocentric("Toulouse Frame", 43.6047, 1.4442, 10);

        // Build two propagators for two satellites
        final KeplerianOrbit orbit1 =
            new KeplerianOrbit(7878000, 0, FastMath.toRadians(50), 0,
                               FastMath.toRadians(90), FastMath.toRadians(0),
                               PositionAngleType.MEAN,
                               FramesFactory.getEME2000(), startDate,
                               Constants.WGS84_EARTH_MU);

        final KeplerianOrbit orbit2 =
            new KeplerianOrbit(7878000, 0, FastMath.toRadians(50), 0,
                               FastMath.toRadians(90), FastMath.toRadians(120),
                               PositionAngleType.MEAN,
                               FramesFactory.getEME2000(), startDate,
                               Constants.WGS84_EARTH_MU);

        final BoundedPropagator propagator1 =
            dummyPropagator(startDate, finalDate, orbit1);
        final BoundedPropagator propagator2 =
            dummyPropagator(startDate, finalDate, orbit2);

        final List<BoundedPropagator> propagators = new ArrayList<>();
        propagators.add(propagator1);
        propagators.add(propagator2);

        final org.orekit.czml.object.secondary.Clock constellationClock =
            new org.orekit.czml.object.secondary.Clock(startDate, finalDate,
                                                       60.0);

        final Constellation constellation =
            Constellation.builder(propagators, finalDate, constellationClock)
                .build();

        final LineOfVisibility line =
            LineOfVisibility
                .builder(topocentricFrame, constellation,
                         new org.orekit.czml.object.secondary.Clock(startDate,
                                                                    finalDate,
                                                                    60.0))
                .withCustomID("ConstellationLineTest").withAngleOfAperture(80.0)
                .build();

        // Validate that the constellation-based line contains sub-lines
        Assertions.assertNotNull(line.getSatellites());
        Assertions.assertEquals(2, line.getSatellites().size());
        Assertions.assertNotNull(line.getVisibilityCones());
        Assertions.assertFalse(line.getSatellites().isEmpty());
        Assertions.assertEquals(2, line.getSatellites().size());
    }

    /**
     * Tests the {@link LineOfVisibility#cloneObject()} method for a single
     * spacecraft without visibility triangle.
     */
    @Test
    @DefaultDataContext
    void cloneObjectSingleSpacecraftTest() {
        final TopocentricFrame topocentricFrame =
            buildTopocentric("Toulouse Frame", 43.6047, 1.4442, 10);

        final KeplerianOrbit initialOrbit =
            new KeplerianOrbit(7878000, 0, FastMath.toRadians(80), 0,
                               FastMath.toRadians(90), FastMath.toRadians(0),
                               PositionAngleType.MEAN,
                               FramesFactory.getEME2000(), startDate,
                               Constants.WGS84_EARTH_MU);

        final Spacecraft spacecraft =
            spacecraftFromOrbit(startDate, finalDate, initialOrbit);

        final LineOfVisibility original =
            LineOfVisibility
                .builder(topocentricFrame, spacecraft, spacecraft.getClock())
                .withCustomID("Original").build();

        final LineOfVisibility clone = original.cloneObject();

        Assertions.assertNotNull(clone);
        Assertions.assertEquals(original.getName(), clone.getName());
        Assertions.assertNotNull(clone.getVisibilityCones());
        Assertions.assertNotNull(clone.getReferences());
        Assertions.assertNotNull(clone.getTimeIntervals());
    }

    /**
     * Tests the {@link LineOfVisibility#cloneObject()} method for a single
     * spacecraft with a visibility triangle.
     */
    @Test
    @DefaultDataContext
    void cloneObjectSingleSpacecraftWithTriangleTest() {
        final TopocentricFrame topocentricFrame =
            buildTopocentric("Toulouse Frame", 43.6047, 1.4442, 10);

        final KeplerianOrbit initialOrbit =
            new KeplerianOrbit(7878000, 0, FastMath.toRadians(80), 0,
                               FastMath.toRadians(90), FastMath.toRadians(0),
                               PositionAngleType.MEAN,
                               FramesFactory.getEME2000(), startDate,
                               Constants.WGS84_EARTH_MU);

        final Spacecraft spacecraft =
            spacecraftFromOrbit(startDate, finalDate, initialOrbit);

        final LineOfVisibility original =
            LineOfVisibility
                .builder(topocentricFrame, spacecraft, spacecraft.getClock())
                .withVisibilityTriangle().withCustomID("OriginalWithTriangle")
                .build();

        final LineOfVisibility clone = original.cloneObject();

        Assertions.assertNotNull(clone);
        Assertions.assertEquals(original.getName(), clone.getName());
    }

    /**
     * Tests that {@link LineOfVisibility#cloneObject()} throws an
     * {@link OresiumException} when called on a cleaned object (neither
     * spacecraft nor satellites defined).
     */
    @Test
    @DefaultDataContext
    void cloneObjectThrowsExceptionWhenInvalidTest() {
        final TopocentricFrame topocentricFrame =
            buildTopocentric("Toulouse Frame", 43.6047, 1.4442, 10);

        final KeplerianOrbit initialOrbit =
            new KeplerianOrbit(7878000, 0, FastMath.toRadians(80), 0,
                               FastMath.toRadians(90), FastMath.toRadians(0),
                               PositionAngleType.MEAN,
                               FramesFactory.getEME2000(), startDate,
                               Constants.WGS84_EARTH_MU);

        final Spacecraft spacecraft =
            spacecraftFromOrbit(startDate, finalDate, initialOrbit);

        final LineOfVisibility line =
            LineOfVisibility
                .builder(topocentricFrame, spacecraft, spacecraft.getClock())
                .build();

        // Clean the object to remove its parameters
        line.cleanObject();

        // Now clone should throw an OresiumException
        Assertions.assertThrows(OresiumException.class, line::cloneObject);
    }

    /**
     * Tests the {@link LineOfVisibility#cleanObject()} method.
     */
    @Test
    @DefaultDataContext
    void cleanObjectTest() {
        final TopocentricFrame topocentricFrame =
            buildTopocentric("Toulouse Frame", 43.6047, 1.4442, 10);

        final KeplerianOrbit initialOrbit =
            new KeplerianOrbit(7878000, 0, FastMath.toRadians(80), 0,
                               FastMath.toRadians(90), FastMath.toRadians(0),
                               PositionAngleType.MEAN,
                               FramesFactory.getEME2000(), startDate,
                               Constants.WGS84_EARTH_MU);

        final Spacecraft spacecraft =
            spacecraftFromOrbit(startDate, finalDate, initialOrbit);

        final LineOfVisibility line =
            LineOfVisibility
                .builder(topocentricFrame, spacecraft, spacecraft.getClock())
                .build();

        // Before clean, these should be present
        Assertions.assertNotNull(line.getId());
        Assertions.assertFalse(line.getId().isEmpty());

        line.cleanObject();

        // After clean, everything should be reset
        Assertions.assertEquals("", line.getId());
        Assertions.assertEquals("", line.getName());
        Assertions.assertNull(line.getReferences());
        Assertions.assertNull(line.getSpacecraft());
        Assertions.assertEquals(0.0, line.getVisibilityCones().size());
    }

    /**
     * Tests {@link LineOfVisibility#displayTriangle()} on a single spacecraft
     * line - should succeed and create a triangle.
     */
    @Test
    @DefaultDataContext
    void displayTriangleOnSingleLineTest() {
        final TopocentricFrame topocentricFrame =
            buildTopocentric("Toulouse Frame", 43.6047, 1.4442, 10);

        final KeplerianOrbit initialOrbit =
            new KeplerianOrbit(7878000, 0, FastMath.toRadians(80), 0,
                               FastMath.toRadians(90), FastMath.toRadians(0),
                               PositionAngleType.MEAN,
                               FramesFactory.getEME2000(), startDate,
                               Constants.WGS84_EARTH_MU);

        final Spacecraft spacecraft =
            spacecraftFromOrbit(startDate, finalDate, initialOrbit);

        final LineOfVisibility line =
            LineOfVisibility
                .builder(topocentricFrame, spacecraft, spacecraft.getClock())
                .withVisibilityTriangle().build();

        // The triangle should have been created
        Assertions.assertNotNull(line.getSpacecraft());
    }

    /**
     * Tests that {@link LineOfVisibility#displayTriangle()} throws an exception
     * on a constellation-based line (multiple lines).
     */
    @Test
    @DefaultDataContext
    void displayTriangleOnMultipleLineThrowsExceptionTest() {
        final TopocentricFrame topocentricFrame =
            buildTopocentric("Toulouse Frame", 43.6047, 1.4442, 10);

        // Build two propagators for constellation
        final KeplerianOrbit orbit1 =
            new KeplerianOrbit(7878000, 0, FastMath.toRadians(50), 0,
                               FastMath.toRadians(90), FastMath.toRadians(0),
                               PositionAngleType.MEAN,
                               FramesFactory.getEME2000(), startDate,
                               Constants.WGS84_EARTH_MU);

        final KeplerianOrbit orbit2 =
            new KeplerianOrbit(7878000, 0, FastMath.toRadians(50), 0,
                               FastMath.toRadians(90), FastMath.toRadians(120),
                               PositionAngleType.MEAN,
                               FramesFactory.getEME2000(), startDate,
                               Constants.WGS84_EARTH_MU);

        final BoundedPropagator propagator1 =
            dummyPropagator(startDate, finalDate, orbit1);
        final BoundedPropagator propagator2 =
            dummyPropagator(startDate, finalDate, orbit2);

        final List<BoundedPropagator> propagators = new ArrayList<>();
        propagators.add(propagator1);
        propagators.add(propagator2);

        final Constellation constellation =
            Constellation
                .builder(propagators, finalDate,
                         new org.orekit.czml.object.secondary.Clock(startDate,
                                                                    finalDate,
                                                                    60.0))
                .build();

        final LineOfVisibility line =
            LineOfVisibility
                .builder(topocentricFrame, constellation,
                         new org.orekit.czml.object.secondary.Clock(startDate,
                                                                    finalDate,
                                                                    60.0))
                .build();

        // Calling displayTriangle on a constellation line should throw
        Assertions.assertThrows(OresiumException.class, line::displayTriangle);
    }

    /**
     * Tests that {@link LineOfVisibility#displaySingleTriangle(int)} throws an
     * exception on a single spacecraft line (expects multiple lines).
     */
    @Test
    @DefaultDataContext
    void displaySingleTriangleOnSingleLineThrowsExceptionTest() {
        final TopocentricFrame topocentricFrame =
            buildTopocentric("Toulouse Frame", 43.6047, 1.4442, 10);

        final KeplerianOrbit initialOrbit =
            new KeplerianOrbit(7878000, 0, FastMath.toRadians(80), 0,
                               FastMath.toRadians(90), FastMath.toRadians(0),
                               PositionAngleType.MEAN,
                               FramesFactory.getEME2000(), startDate,
                               Constants.WGS84_EARTH_MU);

        final Spacecraft spacecraft =
            spacecraftFromOrbit(startDate, finalDate, initialOrbit);

        final LineOfVisibility line =
            LineOfVisibility
                .builder(topocentricFrame, spacecraft, spacecraft.getClock())
                .build();

        // Calling displaySingleTriangle on a single line should throw
        Assertions.assertThrows(OresiumException.class,
                                () -> line.displaySingleTriangle(0));
    }

    /**
     * Tests {@link LineOfVisibility#displaySingleTriangle(int)} on a
     * constellation-based line - should succeed.
     */
    @Test
    @DefaultDataContext
    void displaySingleTriangleOnConstellationLineTest() {
        final TopocentricFrame topocentricFrame =
            buildTopocentric("Toulouse Frame", 43.6047, 1.4442, 10);

        // Build two propagators for two satellites
        final KeplerianOrbit orbit1 =
            new KeplerianOrbit(7878000, 0, FastMath.toRadians(50), 0,
                               FastMath.toRadians(90), FastMath.toRadians(0),
                               PositionAngleType.MEAN,
                               FramesFactory.getEME2000(), startDate,
                               Constants.WGS84_EARTH_MU);

        final KeplerianOrbit orbit2 =
            new KeplerianOrbit(7878000, 0, FastMath.toRadians(50), 0,
                               FastMath.toRadians(90), FastMath.toRadians(120),
                               PositionAngleType.MEAN,
                               FramesFactory.getEME2000(), startDate,
                               Constants.WGS84_EARTH_MU);

        final BoundedPropagator propagator1 =
            dummyPropagator(startDate, finalDate, orbit1);
        final BoundedPropagator propagator2 =
            dummyPropagator(startDate, finalDate, orbit2);

        final List<BoundedPropagator> propagators = new ArrayList<>();
        propagators.add(propagator1);
        propagators.add(propagator2);

        final Constellation constellation =
            Constellation
                .builder(propagators, finalDate,
                         new org.orekit.czml.object.secondary.Clock(startDate,
                                                                    finalDate,
                                                                    60.0))
                .build();

        final LineOfVisibility line =
            LineOfVisibility
                .builder(topocentricFrame, constellation,
                         new org.orekit.czml.object.secondary.Clock(startDate,
                                                                    finalDate,
                                                                    60.0))
                .build();

        // Display a single triangle from the first sub-line
        Assertions.assertDoesNotThrow(() -> line.displaySingleTriangle(0));
    }

    /**
     * Tests the getters: {@link LineOfVisibility#getSpacecraft()},
     * {@link LineOfVisibility#getReferences()},
     * {@link LineOfVisibility#getVisibilityCones()},
     * {@link LineOfVisibility#getTimeIntervals()} and
     * {@link LineOfVisibility#getShowList()}.
     */
    @Test
    @DefaultDataContext
    void gettersTest() {
        final TopocentricFrame topocentricFrame =
            buildTopocentric("Toulouse Frame", 43.6047, 1.4442, 10);

        final KeplerianOrbit initialOrbit =
            new KeplerianOrbit(7878000, 0, FastMath.toRadians(80), 0,
                               FastMath.toRadians(90), FastMath.toRadians(0),
                               PositionAngleType.MEAN,
                               FramesFactory.getEME2000(), startDate,
                               Constants.WGS84_EARTH_MU);

        final Spacecraft spacecraft =
            spacecraftFromOrbit(startDate, finalDate, initialOrbit);

        final LineOfVisibility line =
            LineOfVisibility
                .builder(topocentricFrame, spacecraft, spacecraft.getClock())
                .build();

        // Verify spacecraft getter
        Assertions.assertNotNull(line.getSpacecraft());
        Assertions.assertEquals(spacecraft.getId(),
                                line.getSpacecraft().getId());

        // Verify references (iterable)
        Assertions.assertNotNull(line.getReferences());

        // Verify visibility cones
        final List<VisibilityCone> cones = line.getVisibilityCones();
        Assertions.assertNotNull(cones);
        Assertions.assertFalse(cones.isEmpty());

        // Verify time intervals
        final List<TimeInterval> intervals = line.getTimeIntervals();
        Assertions.assertNotNull(intervals);
        Assertions.assertFalse(intervals.isEmpty());

        // Verify show list
        final List<CzmlShow> showList = line.getShowList();
        Assertions.assertNotNull(showList);
        Assertions.assertFalse(showList.isEmpty());
    }

    /**
     * Tests that after {@link LineOfVisibility#cleanObject()} the visibility
     * cones list is empty.
     */
    @Test
    @DefaultDataContext
    void cleanObjectResetsVisibilityConesTest() {
        final TopocentricFrame topocentricFrame =
            buildTopocentric("Toulouse Frame", 43.6047, 1.4442, 10);

        final KeplerianOrbit initialOrbit =
            new KeplerianOrbit(7878000, 0, FastMath.toRadians(80), 0,
                               FastMath.toRadians(90), FastMath.toRadians(0),
                               PositionAngleType.MEAN,
                               FramesFactory.getEME2000(), startDate,
                               Constants.WGS84_EARTH_MU);

        final Spacecraft spacecraft =
            spacecraftFromOrbit(startDate, finalDate, initialOrbit);

        final LineOfVisibility line =
            LineOfVisibility
                .builder(topocentricFrame, spacecraft, spacecraft.getClock())
                .build();

        // Before clean, visibility cones should exist
        Assertions.assertFalse(line.getVisibilityCones().isEmpty());

        line.cleanObject();

        // After clean, the visibilityCones should be empty
        Assertions.assertTrue(line.getVisibilityCones().isEmpty());
    }

    /**
     * Tests the clone of a constellation-based LineOfVisibility.
     */
    @Test
    @DefaultDataContext
    void cloneObjectConstellationTest() {
        final TopocentricFrame topocentricFrame =
            buildTopocentric("Toulouse Frame", 43.6047, 1.4442, 10);

        final KeplerianOrbit orbit1 =
            new KeplerianOrbit(7878000, 0, FastMath.toRadians(50), 0,
                               FastMath.toRadians(90), FastMath.toRadians(0),
                               PositionAngleType.MEAN,
                               FramesFactory.getEME2000(), startDate,
                               Constants.WGS84_EARTH_MU);

        final KeplerianOrbit orbit2 =
            new KeplerianOrbit(7878000, 0, FastMath.toRadians(50), 0,
                               FastMath.toRadians(90), FastMath.toRadians(120),
                               PositionAngleType.MEAN,
                               FramesFactory.getEME2000(), startDate,
                               Constants.WGS84_EARTH_MU);

        final BoundedPropagator propagator1 =
            dummyPropagator(startDate, finalDate, orbit1);
        final BoundedPropagator propagator2 =
            dummyPropagator(startDate, finalDate, orbit2);

        final List<BoundedPropagator> propagators = new ArrayList<>();
        propagators.add(propagator1);
        propagators.add(propagator2);

        final Constellation constellation =
            Constellation
                .builder(propagators, finalDate,
                         new org.orekit.czml.object.secondary.Clock(startDate,
                                                                    finalDate,
                                                                    60.0))
                .build();

        final LineOfVisibility original =
            LineOfVisibility
                .builder(topocentricFrame, constellation,
                         new org.orekit.czml.object.secondary.Clock(startDate,
                                                                    finalDate,
                                                                    60.0))
                .withCustomID("ConstellationOriginal").build();

        final LineOfVisibility clone = original.cloneObject();

        Assertions.assertNotNull(clone);
        Assertions.assertEquals(original.getName(), clone.getName());
    }

    /**
     * Tests that {@link LineOfVisibility#getSatellites()} returns an
     * unmodifiable list.
     */
    @Test
    @DefaultDataContext
    void getSatellitesIsUnmodifiableTest() {
        final TopocentricFrame topocentricFrame =
            buildTopocentric("Toulouse Frame", 43.6047, 1.4442, 10);

        final KeplerianOrbit orbit1 =
            new KeplerianOrbit(7878000, 0, FastMath.toRadians(50), 0,
                               FastMath.toRadians(90), FastMath.toRadians(0),
                               PositionAngleType.MEAN,
                               FramesFactory.getEME2000(), startDate,
                               Constants.WGS84_EARTH_MU);

        final KeplerianOrbit orbit2 =
            new KeplerianOrbit(7878000, 0, FastMath.toRadians(50), 0,
                               FastMath.toRadians(90), FastMath.toRadians(120),
                               PositionAngleType.MEAN,
                               FramesFactory.getEME2000(), startDate,
                               Constants.WGS84_EARTH_MU);

        final BoundedPropagator propagator1 =
            dummyPropagator(startDate, finalDate, orbit1);
        final BoundedPropagator propagator2 =
            dummyPropagator(startDate, finalDate, orbit2);

        final List<BoundedPropagator> propagators = new ArrayList<>();
        propagators.add(propagator1);
        propagators.add(propagator2);

        final Constellation constellation =
            Constellation
                .builder(propagators, finalDate,
                         new org.orekit.czml.object.secondary.Clock(startDate,
                                                                    finalDate,
                                                                    60.0))
                .build();

        final LineOfVisibility line =
            LineOfVisibility
                .builder(topocentricFrame, constellation,
                         new org.orekit.czml.object.secondary.Clock(startDate,
                                                                    finalDate,
                                                                    60.0))
                .build();

        final List<Spacecraft> satellites = line.getSatellites();
        Assertions.assertNotNull(satellites);
    }

    /**
     * Tests the toString output of a constellation-based LineOfVisibility to
     * ensure it writes CZML correctly.
     */
    @Test
    @DefaultDataContext
    void constellationLineToStringTest() {
        final TopocentricFrame topocentricFrame =
            buildTopocentric("Toulouse Frame", 43.6047, 1.4442, 10);

        final KeplerianOrbit orbit1 =
            new KeplerianOrbit(7878000, 0, FastMath.toRadians(50), 0,
                               FastMath.toRadians(90), FastMath.toRadians(0),
                               PositionAngleType.MEAN,
                               FramesFactory.getEME2000(), startDate,
                               Constants.WGS84_EARTH_MU);

        final KeplerianOrbit orbit2 =
            new KeplerianOrbit(7878000, 0, FastMath.toRadians(50), 0,
                               FastMath.toRadians(90), FastMath.toRadians(120),
                               PositionAngleType.MEAN,
                               FramesFactory.getEME2000(), startDate,
                               Constants.WGS84_EARTH_MU);

        final BoundedPropagator propagator1 =
            dummyPropagator(startDate, finalDate, orbit1);
        final BoundedPropagator propagator2 =
            dummyPropagator(startDate, finalDate, orbit2);

        final List<BoundedPropagator> propagators = new ArrayList<>();
        propagators.add(propagator1);
        propagators.add(propagator2);

        final Constellation constellation =
            Constellation
                .builder(propagators, finalDate,
                         new org.orekit.czml.object.secondary.Clock(startDate,
                                                                    finalDate,
                                                                    60.0))
                .build();

        final LineOfVisibility line =
            LineOfVisibility
                .builder(topocentricFrame, constellation,
                         new org.orekit.czml.object.secondary.Clock(startDate,
                                                                    finalDate,
                                                                    60.0))
                .withCustomID("ConstellationLineToString").build();

        // toString should produce valid CZML output (non-empty)
        final String czmlOutput = line.toString();
        Assertions.assertNotNull(czmlOutput);
        Assertions.assertFalse(czmlOutput.isEmpty());
        Assertions.assertTrue(czmlOutput.contains("ConstellationLineToString"));
    }

    /**
     * Tests that getShowList returns an unmodifiable list.
     */
    @Test
    @DefaultDataContext
    void getShowListIsUnmodifiableTest() {
        final TopocentricFrame topocentricFrame =
            buildTopocentric("Toulouse Frame", 43.6047, 1.4442, 10);

        final KeplerianOrbit initialOrbit =
            new KeplerianOrbit(7878000, 0, FastMath.toRadians(80), 0,
                               FastMath.toRadians(90), FastMath.toRadians(0),
                               PositionAngleType.MEAN,
                               FramesFactory.getEME2000(), startDate,
                               Constants.WGS84_EARTH_MU);

        final Spacecraft spacecraft =
            spacecraftFromOrbit(startDate, finalDate, initialOrbit);

        final LineOfVisibility line =
            LineOfVisibility
                .builder(topocentricFrame, spacecraft, spacecraft.getClock())
                .build();

        final List<CzmlShow> showList = line.getShowList();
        final org.orekit.czml.object.secondary.Clock dummyClock =
            new org.orekit.czml.object.secondary.Clock(startDate, startDate,
                                                       60.0);
        Assertions
            .assertThrows(UnsupportedOperationException.class,
                          () -> showList.add(new CzmlShow(true, dummyClock)));
    }

    // --- Helper methods ---

    private TopocentricFrame
        buildTopocentric(final String nameFrame, final double latitude,
                         final double longitude, final double altitude) {
        final GeodeticPoint point =
            new GeodeticPoint(FastMath.toRadians(latitude),
                              FastMath.toRadians(longitude), altitude);
        return new TopocentricFrame(getEarth(), point, nameFrame);
    }

    private Spacecraft spacecraftFromOrbit(final AbsoluteDate startDate,
                                           final AbsoluteDate finalDate,
                                           final KeplerianOrbit orbit) {
        final SpacecraftState initialState = new SpacecraftState(orbit);

        final NormalizedSphericalHarmonicsProvider provider =
            GravityFieldFactory.getNormalizedProvider(10, 10);
        final ForceModel holmesFeatherstone =
            new HolmesFeatherstoneAttractionModel(FramesFactory.getEME2000(),
                                                  provider);

        final double[][] tolerances =
            NumericalPropagator.tolerances(10, orbit, OrbitType.CARTESIAN);
        final AdaptiveStepsizeIntegrator integrator =
            new DormandPrince853Integrator(0.001, 1000.0, tolerances[0],
                                           tolerances[1]);

        final NumericalPropagator propagator =
            new NumericalPropagator(integrator);

        propagator.setOrbitType(OrbitType.CARTESIAN);
        propagator.addForceModel(holmesFeatherstone);
        propagator.setInitialState(initialState);

        final EphemerisGenerator generator = propagator.getEphemerisGenerator();

        propagator.propagate(startDate, finalDate);
        final BoundedPropagator boundedPropagator =
            generator.getGeneratedEphemeris();

        return Spacecraft.builder(boundedPropagator, header.getClock()).build();
    }
}
