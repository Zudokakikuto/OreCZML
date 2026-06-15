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
import org.orekit.bodies.GeodeticPoint;
import org.orekit.czml.errors.OresiumException;
import org.orekit.czml.file.AbstractTest;
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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Unit tests for {@link MultipleLineOfVisibility}.
 *
 * @author Julien Leblond
 * @since 1.1
 */
public class MultipleLineOfVisibilityTest
    extends
    AbstractTest {

    /** Initialise orekit data. */
    private final double data = initializeOrekitData();

    /** Start Date. */
    final AbsoluteDate startDate =
        new AbsoluteDate(2024, 3, 15, 0, 0, 0.0, TimeScalesFactory.getUTC());

    /** Final date. */
    final AbsoluteDate finalDate = startDate.shiftedBy(10 * 3600);

    /** Clock. */
    final org.orekit.czml.object.secondary.Clock clock =
        new org.orekit.czml.object.secondary.Clock(startDate, finalDate, 60.0);

    // -----------------------------------------------------------------------
    // Constructor tests
    // -----------------------------------------------------------------------

    /**
     * Tests the construction of a {@link MultipleLineOfVisibility} with a
     * spacecraft. Verifies that the object is properly built, the list of lines
     * is populated, and the id is correctly set.
     */
    @Test
    @DefaultDataContext
    void multipleLineOfVisibilityWithSpacecraftTest() {
        // Build topocentric frames
        final List<TopocentricFrame> topocentricFrames =
            buildTopocentricFrames();

        // Build spacecraft
        final Spacecraft spacecraft = buildSpacecraft();

        // Build MultipleLineOfVisibility via builder
        final MultipleLineOfVisibility multipleLineOfVisibility =
            MultipleLineOfVisibility.builder(topocentricFrames, spacecraft)
                .build();

        // Verify object state
        Assertions.assertNotNull(multipleLineOfVisibility);
        Assertions.assertFalse(multipleLineOfVisibility.isEmpty());
        Assertions.assertEquals(topocentricFrames.size(),
                                multipleLineOfVisibility.size());
        Assertions.assertTrue(multipleLineOfVisibility.getId()
            .contains(MultipleLineOfVisibility.DEFAULT_ID));
        Assertions.assertTrue(multipleLineOfVisibility.getId()
            .contains(spacecraft.getId()));
        Assertions
            .assertEquals(topocentricFrames,
                          multipleLineOfVisibility.getTopocentricFrames());
    }

    /**
     * Tests the construction of a {@link MultipleLineOfVisibility} with a
     * constellation. Verifies that the object is properly built and the list of
     * lines is populated with one line per topocentric frame.
     */
    @Test
    @DefaultDataContext
    void multipleLineOfVisibilityWithConstellationTest() {
        // Build topocentric frames
        final List<TopocentricFrame> topocentricFrames =
            buildTopocentricFrames();

        // Build constellation
        final Constellation constellation = buildConstellation();

        // Build MultipleLineOfVisibility via builder
        final MultipleLineOfVisibility multipleLineOfVisibility =
            MultipleLineOfVisibility.builder(topocentricFrames, constellation)
                .build();

        // Verify object state
        Assertions.assertNotNull(multipleLineOfVisibility);
        Assertions.assertFalse(multipleLineOfVisibility.isEmpty());
        Assertions.assertEquals(topocentricFrames.size(),
                                multipleLineOfVisibility.size());
        Assertions.assertTrue(multipleLineOfVisibility.getId()
            .contains(MultipleLineOfVisibility.DEFAULT_ID));
        Assertions.assertTrue(multipleLineOfVisibility.getId()
            .contains(constellation.getId()));
        Assertions
            .assertEquals(topocentricFrames,
                          multipleLineOfVisibility.getTopocentricFrames());
    }

    // -----------------------------------------------------------------------
    // cloneObject tests
    // -----------------------------------------------------------------------

    /**
     * Tests {@link MultipleLineOfVisibility#cloneObject()} when the object was
     * created from a spacecraft. The clone should have the same number of
     * lines.
     */
    @Test
    @DefaultDataContext
    void cloneObjectWithSpacecraftTest() {
        final List<TopocentricFrame> topocentricFrames =
            buildTopocentricFrames();
        final Spacecraft spacecraft = buildSpacecraft();

        final MultipleLineOfVisibility original =
            MultipleLineOfVisibility.builder(topocentricFrames, spacecraft)
                .build();

        final MultipleLineOfVisibility clone = original.cloneObject();

        Assertions.assertNotNull(clone);
        Assertions.assertEquals(original.size(), clone.size());
        Assertions.assertEquals(original.getId(), clone.getId());
        Assertions.assertNotNull(clone.getTopocentricFrames());
    }

    /**
     * Tests {@link MultipleLineOfVisibility#cloneObject()} when the object was
     * created from a constellation. The clone should have the same number of
     * lines.
     */
    @Test
    @DefaultDataContext
    void cloneObjectWithConstellationTest() {
        final List<TopocentricFrame> topocentricFrames =
            buildTopocentricFrames();
        final Constellation constellation = buildConstellation();

        final MultipleLineOfVisibility original =
            MultipleLineOfVisibility.builder(topocentricFrames, constellation)
                .build();

        final MultipleLineOfVisibility clone = original.cloneObject();

        Assertions.assertNotNull(clone);
        Assertions.assertEquals(original.size(), clone.size());
        Assertions.assertEquals(original.getId(), clone.getId());
        Assertions.assertNotNull(clone.getTopocentricFrames());
    }

    /**
     * Tests that {@link MultipleLineOfVisibility#cloneObject()} throws an
     * {@link OresiumException} when neither a spacecraft nor a constellation
     * was set (an invalid state that cannot be cloned).
     */
    @Test
    @DefaultDataContext
    void cloneObjectThrowsExceptionWhenInvalidTest() {
        final List<TopocentricFrame> topocentricFrames =
            buildTopocentricFrames();
        final Spacecraft spacecraft = buildSpacecraft();

        final MultipleLineOfVisibility original =
            MultipleLineOfVisibility.builder(topocentricFrames, spacecraft)
                .build();

        // We cannot directly set spacecraft/constellation to null via public
        // API,
        // but we can use reflection to simulate an invalid state.
        // Alternatively, we rely on the internal logic: cloneObject checks
        // spacecraft and constellation fields. Since both constructors always
        // set
        // one of them, we need to verify that the logic correctly throws.
        // The exception path is exercised if both fields are null.
        // We can use reflection to set them to null.
        try {
            final java.lang.reflect.Field spacecraftField =
                MultipleLineOfVisibility.class.getDeclaredField("spacecraft");
            spacecraftField.setAccessible(true);
            spacecraftField.set(original, null);

            final java.lang.reflect.Field constellationField =
                MultipleLineOfVisibility.class
                    .getDeclaredField("constellation");
            constellationField.setAccessible(true);
            constellationField.set(original, null);
        } catch (Exception e) {
            Assertions
                .fail("Could not set fields via reflection: " + e.getMessage());
        }

        Assertions.assertThrows(OresiumException.class, original::cloneObject);
    }

    // -----------------------------------------------------------------------
    // writeCzmlBlock / toString tests
    // -----------------------------------------------------------------------

    /**
     * Tests that the {@link MultipleLineOfVisibility#writeCzmlBlock} produces
     * valid CZML output for a spacecraft-based object.
     */
    @Test
    @DefaultDataContext
    void writeCzmlBlockWithSpacecraftTest() {
        final List<TopocentricFrame> topocentricFrames =
            buildTopocentricFrames();
        final Spacecraft spacecraft = buildSpacecraft();

        final MultipleLineOfVisibility multipleLineOfVisibility =
            MultipleLineOfVisibility.builder(topocentricFrames, spacecraft)
                .build();

        final String czmlOutput = multipleLineOfVisibility.toString();
        Assertions.assertNotNull(czmlOutput);
        Assertions.assertFalse(czmlOutput.isEmpty());
        // The output should contain "packet" or "id" entries for each line
        Assertions.assertTrue(czmlOutput.contains("id"));
    }

    /**
     * Tests that the {@link MultipleLineOfVisibility#toString()} produces valid
     * CZML output for a constellation-based object.
     */
    @Test
    @DefaultDataContext
    void writeCzmlBlockWithConstellationTest() {
        final List<TopocentricFrame> topocentricFrames =
            buildTopocentricFrames();
        final Constellation constellation = buildConstellation();

        final MultipleLineOfVisibility multipleLineOfVisibility =
            MultipleLineOfVisibility.builder(topocentricFrames, constellation)
                .build();

        final String czmlOutput = multipleLineOfVisibility.toString();
        Assertions.assertNotNull(czmlOutput);
        Assertions.assertFalse(czmlOutput.isEmpty());
        Assertions.assertTrue(czmlOutput.contains("id"));
    }

    // -----------------------------------------------------------------------
    // List interface method tests
    // -----------------------------------------------------------------------

    /**
     * Tests the list methods of {@link MultipleLineOfVisibility}: {@code size},
     * {@code isEmpty}, {@code get}, {@code contains}, {@code indexOf},
     * {@code lastIndexOf}.
     */
    @Test
    @DefaultDataContext
    void listQueryMethodsTest() {
        final List<TopocentricFrame> topocentricFrames =
            buildTopocentricFrames();
        final Spacecraft spacecraft = buildSpacecraft();

        final MultipleLineOfVisibility lines =
            MultipleLineOfVisibility.builder(topocentricFrames, spacecraft)
                .build();

        // size and isEmpty
        Assertions.assertEquals(topocentricFrames.size(), lines.size());
        Assertions.assertFalse(lines.isEmpty());

        // get
        final LineOfVisibility firstLine = lines.get(0);
        Assertions.assertNotNull(firstLine);

        // contains
        Assertions.assertTrue(lines.contains(firstLine));
        Assertions.assertFalse(lines.contains(new Object()));

        // indexOf
        Assertions.assertEquals(0, lines.indexOf(firstLine));
        Assertions.assertEquals(-1, lines.indexOf(new Object()));

        // lastIndexOf
        Assertions.assertEquals(0, lines.lastIndexOf(firstLine));
        Assertions.assertEquals(-1, lines.lastIndexOf(new Object()));
    }

    /**
     * Tests the {@link MultipleLineOfVisibility#iterator()} and
     * {@link MultipleLineOfVisibility#listIterator()} methods.
     */
    @Test
    @DefaultDataContext
    void iteratorMethodsTest() {
        final List<TopocentricFrame> topocentricFrames =
            buildTopocentricFrames();
        final Spacecraft spacecraft = buildSpacecraft();

        final MultipleLineOfVisibility lines =
            MultipleLineOfVisibility.builder(topocentricFrames, spacecraft)
                .build();

        // iterator
        final Iterator<LineOfVisibility> iterator = lines.iterator();
        int count = 0;
        while (iterator.hasNext()) {
            Assertions.assertNotNull(iterator.next());
            count++;
        }
        Assertions.assertEquals(topocentricFrames.size(), count);

        // listIterator
        final ListIterator<LineOfVisibility> listIterator =
            lines.listIterator();
        Assertions.assertNotNull(listIterator);

        // listIterator(index)
        final ListIterator<LineOfVisibility> listIteratorAtIndex =
            lines.listIterator(1);
        Assertions.assertNotNull(listIteratorAtIndex);
    }

    /**
     * Tests the {@link MultipleLineOfVisibility#toArray()} methods.
     */
    @Test
    @DefaultDataContext
    void toArrayMethodsTest() {
        final List<TopocentricFrame> topocentricFrames =
            buildTopocentricFrames();
        final Spacecraft spacecraft = buildSpacecraft();

        final MultipleLineOfVisibility lines =
            MultipleLineOfVisibility.builder(topocentricFrames, spacecraft)
                .build();

        // toArray()
        final Object[] array = lines.toArray();
        Assertions.assertEquals(topocentricFrames.size(), array.length);

        // toArray(T[])
        final LineOfVisibility[] typedArray =
            lines.toArray(new LineOfVisibility[0]);
        Assertions.assertEquals(topocentricFrames.size(), typedArray.length);
    }

    /**
     * Tests the modification methods of {@link MultipleLineOfVisibility}:
     * {@code add}, {@code set}, {@code remove} by object and index, and
     * {@code clear}.
     */
    @Test
    @DefaultDataContext
    void listModificationMethodsTest() {
        final List<TopocentricFrame> topocentricFrames =
            buildTopocentricFrames();
        final Spacecraft spacecraft = buildSpacecraft();

        final MultipleLineOfVisibility lines =
            MultipleLineOfVisibility.builder(topocentricFrames, spacecraft)
                .build();

        final int originalSize = lines.size();

        // add(element)
        // Create a new line to add by reusing an existing topocentric frame
        final LineOfVisibility extraLine =
            LineOfVisibility.builder(topocentricFrames.get(0), spacecraft,
                                     spacecraft.getClock())
                .build();
        Assertions.assertTrue(lines.add(extraLine));
        Assertions.assertEquals(originalSize + 1, lines.size());

        // set(index, element)
        final LineOfVisibility replacementLine =
            LineOfVisibility
                .builder(topocentricFrames.get(1), spacecraft,
                         spacecraft.getClock())
                .withCustomID("ReplacementLine").build();
        final LineOfVisibility oldLine = lines.set(0, replacementLine);
        Assertions.assertNotNull(oldLine);
        Assertions.assertEquals(replacementLine, lines.get(0));

        // add(index, element)
        final LineOfVisibility insertedLine =
            LineOfVisibility
                .builder(topocentricFrames.get(2), spacecraft,
                         spacecraft.getClock())
                .withCustomID("InsertedLine").build();
        lines.add(0, insertedLine);
        Assertions.assertEquals(originalSize + 2, lines.size());

        // remove(index)
        final LineOfVisibility removedLine = lines.remove(0);
        Assertions.assertNotNull(removedLine);
        Assertions.assertEquals(originalSize + 1, lines.size());

        // remove(Object)
        Assertions.assertTrue(lines.remove(replacementLine));
        Assertions.assertEquals(originalSize, lines.size());

        // clear
        lines.clear();
        Assertions.assertTrue(lines.isEmpty());
        Assertions.assertEquals(0, lines.size());
    }

    /**
     * Tests the bulk modification methods of {@link MultipleLineOfVisibility}:
     * {@code addAll}, {@code removeAll}, {@code retainAll},
     * {@code containsAll}.
     */
    @Test
    @DefaultDataContext
    void bulkModificationMethodsTest() {
        final List<TopocentricFrame> topocentricFrames =
            buildTopocentricFrames();
        final Spacecraft spacecraft = buildSpacecraft();

        final MultipleLineOfVisibility lines =
            MultipleLineOfVisibility.builder(topocentricFrames, spacecraft)
                .build();

        // Create extra lines for bulk operations
        final List<LineOfVisibility> extraLines = new ArrayList<>();
        final LineOfVisibility line1 =
            LineOfVisibility
                .builder(topocentricFrames.get(0), spacecraft,
                         spacecraft.getClock())
                .withCustomID("BulkLine1").build();
        final LineOfVisibility line2 =
            LineOfVisibility
                .builder(topocentricFrames.get(1), spacecraft,
                         spacecraft.getClock())
                .withCustomID("BulkLine2").build();
        extraLines.add(line1);
        extraLines.add(line2);

        // addAll
        Assertions.assertTrue(lines.addAll(extraLines));
        Assertions.assertEquals(topocentricFrames.size() + 2, lines.size());

        // containsAll
        Assertions.assertTrue(lines.containsAll(extraLines));

        // removeAll
        Assertions.assertTrue(lines.removeAll(extraLines));
        Assertions.assertEquals(topocentricFrames.size(), lines.size());

        // addAll(index, ...)
        final List<LineOfVisibility> indexedLines = new ArrayList<>();
        final LineOfVisibility indexedLine1 =
            LineOfVisibility
                .builder(topocentricFrames.get(0), spacecraft,
                         spacecraft.getClock())
                .withCustomID("IndexedLine1").build();
        final LineOfVisibility indexedLine2 =
            LineOfVisibility
                .builder(topocentricFrames.get(0), spacecraft,
                         spacecraft.getClock())
                .withCustomID("IndexedLine2").build();
        indexedLines.add(indexedLine1);
        indexedLines.add(indexedLine2);
        Assertions.assertTrue(lines.addAll(0, indexedLines));
        Assertions.assertEquals(topocentricFrames.size() + 2, lines.size());

        // retainAll
        final List<LineOfVisibility> retainList =
            Collections.singletonList(indexedLine1);
        Assertions.assertTrue(lines.retainAll(retainList));
        Assertions.assertEquals(1, lines.size());
        Assertions.assertTrue(lines.contains(indexedLine1));
    }

    @Test
    @DefaultDataContext
    void subListTest() {
        final List<TopocentricFrame> topocentricFrames =
            buildTopocentricFrames();
        final Spacecraft spacecraft = buildSpacecraft();

        final MultipleLineOfVisibility lines =
            MultipleLineOfVisibility.builder(topocentricFrames, spacecraft)
                .build();

        // subList with valid range
        final List<LineOfVisibility> subList = lines.subList(0, 1);
        Assertions.assertNotNull(subList);
        Assertions.assertEquals(1, subList.size());
    }

    // -----------------------------------------------------------------------
    // Setters test
    // -----------------------------------------------------------------------

    /**
     * Tests {@link MultipleLineOfVisibility#setTopocentricFrames(List)}.
     */
    @Test
    @DefaultDataContext
    void setTopocentricFramesTest() {
        final List<TopocentricFrame> topocentricFrames =
            buildTopocentricFrames();
        final Spacecraft spacecraft = buildSpacecraft();

        final MultipleLineOfVisibility lines =
            MultipleLineOfVisibility.builder(topocentricFrames, spacecraft)
                .build();

        // Update the topocentric frames list
        final List<TopocentricFrame> newFrames = new ArrayList<>();
        newFrames.add(buildTopocentric("Quito", 0.1807, 11.5382, 2850));
        lines.setTopocentricFrames(newFrames);

        Assertions.assertEquals(newFrames, lines.getTopocentricFrames());
        Assertions.assertEquals(1, lines.getTopocentricFrames().size());
    }

    // -----------------------------------------------------------------------
    // Helper methods
    // -----------------------------------------------------------------------

    /**
     * Builds a list of three topocentric frames for testing.
     *
     * @return a list of topocentric frames
     */
    private List<TopocentricFrame> buildTopocentricFrames() {
        final TopocentricFrame topocentricToulouse =
            buildTopocentric("Toulouse Frame", 43.6047, 1.4442, 10);
        final TopocentricFrame topocentricQuito =
            buildTopocentric("Quito", 0.1807, 11.5382, 2850);
        final TopocentricFrame topocentricSydney =
            buildTopocentric("Sydney", -33.8688, -241.2093, 100);

        final List<TopocentricFrame> topocentricFrames = new ArrayList<>();
        topocentricFrames.add(topocentricToulouse);
        topocentricFrames.add(topocentricQuito);
        topocentricFrames.add(topocentricSydney);
        return topocentricFrames;
    }

    /**
     * Build a topocentric frame from a name, latitude, longitude and altitude.
     *
     * @param frameName the name of the frame
     * @param latitude the latitude in degrees
     * @param longitude the longitude in degrees
     * @param altitude the altitude in meters
     * @return the topocentric frame
     */
    private TopocentricFrame
        buildTopocentric(final String frameName, final double latitude,
                         final double longitude, final double altitude) {
        final GeodeticPoint point =
            new GeodeticPoint(FastMath.toRadians(latitude),
                              FastMath.toRadians(longitude), altitude);
        return new TopocentricFrame(getEarth(), point, frameName);
    }

    /**
     * Builds a spacecraft for testing.
     *
     * @return a spacecraft
     */
    private Spacecraft buildSpacecraft() {
        final KeplerianOrbit initialOrbit =
            new KeplerianOrbit(7878000, 0, FastMath.toRadians(20), 0,
                               FastMath.toRadians(90), FastMath.toRadians(0),
                               PositionAngleType.MEAN,
                               FramesFactory.getEME2000(), startDate,
                               Constants.WGS84_EARTH_MU);

        final SpacecraftState initialState = new SpacecraftState(initialOrbit);

        final double[][] tolerances =
            NumericalPropagator.tolerances(10.0, initialOrbit,
                                           OrbitType.CARTESIAN);
        final AdaptiveStepsizeIntegrator integrator =
            new DormandPrince853Integrator(0.001, 1000.0, tolerances[0],
                                           tolerances[1]);

        final NumericalPropagator propagator =
            new NumericalPropagator(integrator);

        final NormalizedSphericalHarmonicsProvider provider =
            GravityFieldFactory.getNormalizedProvider(10, 10);
        final ForceModel holmesFeatherstone =
            new HolmesFeatherstoneAttractionModel(FramesFactory
                .getITRF(org.orekit.utils.IERSConventions.IERS_2010, true),
                                                  provider);

        propagator.setOrbitType(OrbitType.CARTESIAN);
        propagator.addForceModel(holmesFeatherstone);
        propagator.setInitialState(initialState);

        final EphemerisGenerator generator = propagator.getEphemerisGenerator();

        propagator.propagate(startDate, finalDate);
        final BoundedPropagator boundedPropagator =
            generator.getGeneratedEphemeris();

        return Spacecraft.builder(boundedPropagator, clock).withOnlyOnePeriod()
            .build();
    }

    /**
     * Builds a constellation with two satellites for testing.
     *
     * @return a constellation
     */
    private Constellation buildConstellation() {
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

        return Constellation.builder(propagators, finalDate, constellationClock)
            .build();
    }
}
