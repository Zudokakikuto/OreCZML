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

import org.hipparchus.util.FastMath;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.orekit.annotation.DefaultDataContext;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.czml.errors.OresiumException;
import org.orekit.czml.errors.OresiumMessages;
import org.orekit.czml.file.AbstractTest;
import org.orekit.czml.object.primary.entities.Constellation;
import org.orekit.czml.object.primary.entities.Spacecraft;
import org.orekit.czml.object.secondary.Clock;
import org.orekit.frames.TopocentricFrame;
import org.orekit.propagation.BoundedPropagator;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScalesFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Unit tests for {@link MultipleLineOfVisibilityBuilder}.
 *
 * @author Julien Leblond
 * @since 1.1
 */
public class MultipleLineOfVisibilityBuilderTest
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
    final Clock clock = new Clock(startDate, finalDate, 60.0);

    // -----------------------------------------------------------------------
    // Constructor tests
    // -----------------------------------------------------------------------

    /**
     * Tests the constructor with spacecraft.
     */
    @Test
    @DefaultDataContext
    void constructorWithSpacecraftTest() {
        // Build test objects
        final List<TopocentricFrame> topocentricFrames =
            buildTopocentricFrames();
        final BoundedPropagator propagator =
            dummyPropagator(startDate, finalDate, dummyOrbit(startDate));
        final Spacecraft spacecraft =
            Spacecraft.builder(propagator, clock).build();
        // Create builder
        final MultipleLineOfVisibilityBuilder builder =
            new MultipleLineOfVisibilityBuilder(topocentricFrames, spacecraft);

        // Verify builder state
        Assertions.assertNotNull(builder);
    }

    /**
     * Tests the constructor with constellation.
     */
    @Test
    @DefaultDataContext
    void constructorWithConstellationTest() {
        // Build test objects
        final List<TopocentricFrame> topocentricFrames =
            buildTopocentricFrames();
        final BoundedPropagator propagator =
            dummyPropagator(startDate, finalDate, dummyOrbit(startDate));
        final List<BoundedPropagator> propagators = new ArrayList<>();
        propagators.add(propagator);
        final Constellation constellation =
            Constellation.builder(propagators, finalDate, clock).build();

        // Create builder
        final MultipleLineOfVisibilityBuilder builder =
            new MultipleLineOfVisibilityBuilder(topocentricFrames,
                                                constellation);

        // Verify builder state
        Assertions.assertNotNull(builder);
    }

    @DisplayName("Test build function with no spacecraft or constellation")
    @Test
    public void MultipleLineOfVisibilityBuildThrowTest() {
        final Spacecraft spacecraft = null;
        Assertions.assertThrows(OresiumException.class,
                                () -> MultipleLineOfVisibility
                                    .builder(new ArrayList<>(), spacecraft)
                                    .build());
        try {
            final MultipleLineOfVisibility multipleLineOfVisibility =
                MultipleLineOfVisibility.builder(new ArrayList<>(), spacecraft)
                    .build();
        } catch (final OresiumException e) {
            Assertions
                .assertEquals(e.getMessage(),
                              OresiumMessages.NO_SPACECRAFT_OR_CONSTELLATION
                                  .getSourceString());
        }
    }

    @DisplayName("Test build function with a constellation in the multiple line visibility")
    @Test
    public void MultipleLineOfVisibilityBuildFunctionConstellationTest() {
        final List<TopocentricFrame> topocentricFrames =
            buildTopocentricFrames();
        final BoundedPropagator propagator =
            dummyPropagator(startDate, finalDate, dummyOrbit(startDate));
        final List<BoundedPropagator> propagators = new ArrayList<>();
        propagators.add(propagator);
        final Constellation constellation =
            Constellation.builder(propagators, finalDate, clock).build();

        final MultipleLineOfVisibility multipleLineOfVisibility =
            MultipleLineOfVisibility.builder(topocentricFrames, constellation)
                .build();

        final String templateFile =
            loadResources("templateFile/object/primary/visu/multiplelineofvisibility/MultipleLineOfVisibilityBuildCOnstellationTemplate.txt");

        // Verify file output
        verifyFileOutput(templateFile, multipleLineOfVisibility.toString(),
                         1e-3);
    }

    // -----------------------------------------------------------------------
    // Builder method tests
    // -----------------------------------------------------------------------

    /**
     * Tests the withCustomIds method.
     */
    @Test
    @DefaultDataContext
    void withCustomIdsTest() {
        // Build test objects
        final List<TopocentricFrame> topocentricFrames =
            buildTopocentricFrames();
        final BoundedPropagator propagator =
            dummyPropagator(startDate, finalDate, dummyOrbit(startDate));
        final Spacecraft spacecraft =
            Spacecraft.builder(propagator, clock).build();
        final List<String> customIds = Arrays.asList("id1", "id2", "id3");

        // Create builder with custom IDs
        final MultipleLineOfVisibilityBuilder builder =
            new MultipleLineOfVisibilityBuilder(topocentricFrames, spacecraft)
                .withCustomIds(customIds);

        // Verify builder state
        Assertions.assertNotNull(builder);
    }

    /**
     * Tests the withCustomClocks method.
     */
    @Test
    @DefaultDataContext
    void withCustomClocksTest() {
        // Build test objects
        final List<TopocentricFrame> topocentricFrames =
            buildTopocentricFrames();
        final BoundedPropagator propagator =
            dummyPropagator(startDate, finalDate, dummyOrbit(startDate));
        final Spacecraft spacecraft =
            Spacecraft.builder(propagator, clock).build();
        final List<Clock> customClocks = Arrays.asList(clock, clock, clock);

        // Create builder with custom clocks
        final MultipleLineOfVisibilityBuilder builder =
            new MultipleLineOfVisibilityBuilder(topocentricFrames, spacecraft)
                .withCustomClocks(customClocks);

        // Verify builder state
        Assertions.assertNotNull(builder);
    }

    /**
     * Tests the withVisibilityTriangle method.
     */
    @Test
    @DefaultDataContext
    void withVisibilityTriangleTest() {
        // Build test objects
        final List<TopocentricFrame> topocentricFrames =
            buildTopocentricFrames();
        final BoundedPropagator propagator =
            dummyPropagator(startDate, finalDate, dummyOrbit(startDate));
        final Spacecraft spacecraft =
            Spacecraft.builder(propagator, clock).build();
        // Create builder with visibility triangle
        final MultipleLineOfVisibilityBuilder builder =
            new MultipleLineOfVisibilityBuilder(topocentricFrames, spacecraft)
                .withVisibilityTriangle();

        // Verify builder state
        Assertions.assertNotNull(builder);
    }

    /**
     * Tests method chaining with multiple builder methods.
     */
    @Test
    @DefaultDataContext
    void methodChainingTest() {
        // Build test objects
        final List<TopocentricFrame> topocentricFrames =
            buildTopocentricFrames();
        final BoundedPropagator propagator =
            dummyPropagator(startDate, finalDate, dummyOrbit(startDate));
        final Spacecraft spacecraft =
            Spacecraft.builder(propagator, clock).build();
        final List<String> customIds = Arrays.asList("id1", "id2", "id3");
        final List<Clock> customClocks = Arrays.asList(clock, clock, clock);

        // Create builder with method chaining
        final MultipleLineOfVisibilityBuilder builder =
            new MultipleLineOfVisibilityBuilder(topocentricFrames, spacecraft)
                .withCustomIds(customIds).withCustomClocks(customClocks)
                .withVisibilityTriangle();

        // Verify builder state
        Assertions.assertNotNull(builder);
    }

    // -----------------------------------------------------------------------
    // build() method tests
    // -----------------------------------------------------------------------

    /**
     * Tests the build method with spacecraft.
     */
    @Test
    @DefaultDataContext
    void buildWithSpacecraftTest() {
        // Build test objects
        final List<TopocentricFrame> topocentricFrames =
            buildTopocentricFrames();
        final BoundedPropagator propagator =
            dummyPropagator(startDate, finalDate, dummyOrbit(startDate));
        final Spacecraft spacecraft =
            Spacecraft.builder(propagator, clock).build();
        // Create and build
        final MultipleLineOfVisibility multipleLineOfVisibility =
            new MultipleLineOfVisibilityBuilder(topocentricFrames, spacecraft)
                .build();

        // Verify result
        Assertions.assertNotNull(multipleLineOfVisibility);
        Assertions.assertFalse(multipleLineOfVisibility.isEmpty());
        Assertions.assertEquals(topocentricFrames.size(),
                                multipleLineOfVisibility.size());
    }

    /**
     * Tests the build method with constellation.
     */
    @Test
    @DefaultDataContext
    void buildWithConstellationTest() {
        // Build test objects
        final List<TopocentricFrame> topocentricFrames =
            buildTopocentricFrames();
        final BoundedPropagator propagator =
            dummyPropagator(startDate, finalDate, dummyOrbit(startDate));
        final List<BoundedPropagator> propagators = new ArrayList<>();
        propagators.add(propagator);
        final Constellation constellation =
            Constellation.builder(propagators, finalDate, clock).build();

        // Create and build
        final MultipleLineOfVisibility multipleLineOfVisibility =
            new MultipleLineOfVisibilityBuilder(topocentricFrames,
                                                constellation)
                .build();

        // Verify result
        Assertions.assertNotNull(multipleLineOfVisibility);
        Assertions.assertFalse(multipleLineOfVisibility.isEmpty());
        Assertions.assertEquals(topocentricFrames.size(),
                                multipleLineOfVisibility.size());
    }

    /**
     * Tests the build method with custom IDs.
     */
    @Test
    @DefaultDataContext
    void buildWithCustomIdsTest() {
        // Build test objects
        final List<TopocentricFrame> topocentricFrames =
            buildTopocentricFrames();
        final BoundedPropagator propagator =
            dummyPropagator(startDate, finalDate, dummyOrbit(startDate));
        final Spacecraft spacecraft =
            Spacecraft.builder(propagator, clock).build();
        final List<String> customIds = Arrays.asList("id1", "id2", "id3");

        // Create and build with custom IDs
        final MultipleLineOfVisibility multipleLineOfVisibility =
            new MultipleLineOfVisibilityBuilder(topocentricFrames, spacecraft)
                .withCustomIds(customIds).build();

        // Verify result
        Assertions.assertNotNull(multipleLineOfVisibility);
        Assertions.assertEquals(topocentricFrames.size(),
                                multipleLineOfVisibility.size());
    }

    /**
     * Tests the build method with custom clocks.
     */
    @Test
    @DefaultDataContext
    void buildWithCustomClocksTest() {
        // Build test objects
        final List<TopocentricFrame> topocentricFrames =
            buildTopocentricFrames();
        final BoundedPropagator propagator =
            dummyPropagator(startDate, finalDate, dummyOrbit(startDate));
        final Spacecraft spacecraft =
            Spacecraft.builder(propagator, clock).build();
        final List<Clock> customClocks = Arrays.asList(clock, clock, clock);

        // Create and build with custom clocks
        final MultipleLineOfVisibility multipleLineOfVisibility =
            new MultipleLineOfVisibilityBuilder(topocentricFrames, spacecraft)
                .withCustomClocks(customClocks).build();

        // Verify result
        Assertions.assertNotNull(multipleLineOfVisibility);
        Assertions.assertEquals(topocentricFrames.size(),
                                multipleLineOfVisibility.size());
    }

    /**
     * Tests the build method with visibility triangle.
     */
    @Test
    @DefaultDataContext
    void buildWithVisibilityTriangleTest() {
        // Build test objects
        final List<TopocentricFrame> topocentricFrames =
            buildTopocentricFrames();
        final BoundedPropagator propagator =
            dummyPropagator(startDate, finalDate, dummyOrbit(startDate));
        final Spacecraft spacecraft =
            Spacecraft.builder(propagator, clock).build();
        // Create and build with visibility triangle
        final MultipleLineOfVisibility multipleLineOfVisibility =
            new MultipleLineOfVisibilityBuilder(topocentricFrames, spacecraft)
                .withVisibilityTriangle().build();

        // Verify result
        Assertions.assertNotNull(multipleLineOfVisibility);
        Assertions.assertEquals(topocentricFrames.size(),
                                multipleLineOfVisibility.size());
    }

    /**
     * Tests the build method with all builder options.
     */
    @Test
    @DefaultDataContext
    void buildWithAllOptionsTest() {
        // Build test objects
        final List<TopocentricFrame> topocentricFrames =
            buildTopocentricFrames();
        final BoundedPropagator propagator =
            dummyPropagator(startDate, finalDate, dummyOrbit(startDate));
        final Spacecraft spacecraft =
            Spacecraft.builder(propagator, clock).build();
        final List<String> customIds = Arrays.asList("id1", "id2", "id3");
        final List<Clock> customClocks = Arrays.asList(clock, clock, clock);

        // Create and build with all options
        final MultipleLineOfVisibility multipleLineOfVisibility =
            new MultipleLineOfVisibilityBuilder(topocentricFrames, spacecraft)
                .withCustomIds(customIds).withCustomClocks(customClocks)
                .withVisibilityTriangle().build();

        // Verify result
        Assertions.assertNotNull(multipleLineOfVisibility);
        Assertions.assertEquals(topocentricFrames.size(),
                                multipleLineOfVisibility.size());
    }

    // -----------------------------------------------------------------------
    // Error case tests
    // -----------------------------------------------------------------------

    /**
     * Tests that build() throws exception when neither spacecraft nor
     * constellation is set.
     */
    @Test
    @DefaultDataContext
    void buildThrowsExceptionWhenNoSpacecraftOrConstellationTest() {
        // Build test objects
        final List<TopocentricFrame> topocentricFrames =
            buildTopocentricFrames();

        // Create builder (this should work)
        final MultipleLineOfVisibilityBuilder builder =
            new MultipleLineOfVisibilityBuilder(topocentricFrames,
                                                (Spacecraft) null);

        // Use reflection to set both spacecraft and constellation to null
        try {
            final java.lang.reflect.Field spacecraftField =
                MultipleLineOfVisibilityBuilder.class
                    .getDeclaredField("spacecraft");
            spacecraftField.setAccessible(true);
            spacecraftField.set(builder, null);

            final java.lang.reflect.Field constellationField =
                MultipleLineOfVisibilityBuilder.class
                    .getDeclaredField("constellation");
            constellationField.setAccessible(true);
            constellationField.set(builder, null);
        } catch (Exception e) {
            Assertions
                .fail("Could not set fields via reflection: " + e.getMessage());
        }

        // Verify exception is thrown
        Assertions.assertThrows(OresiumException.class, builder::build);
    }

    /**
     * Tests that build() throws exception when custom IDs list size doesn't
     * match topocentric frames.
     */
    @Test
    @DefaultDataContext
    void buildThrowsExceptionWhenCustomIdsSizeMismatchTest() {
        // Build test objects
        final List<TopocentricFrame> topocentricFrames =
            buildTopocentricFrames();
        final BoundedPropagator propagator =
            dummyPropagator(startDate, finalDate, dummyOrbit(startDate));
        final Spacecraft spacecraft =
            Spacecraft.builder(propagator, clock).build();
        final List<String> wrongSizeIds = Arrays.asList("id1", "id2"); // Only 2
        // IDs
        // for 3
        // frames

        // Verify exception is thrown
        Assertions.assertThrows(OresiumException.class, () -> {
            new MultipleLineOfVisibilityBuilder(topocentricFrames, spacecraft)
                .withCustomIds(wrongSizeIds).build();
        });
    }

    /**
     * Tests that build() throws exception when custom clocks list size doesn't
     * match topocentric frames.
     */
    @Test
    @DefaultDataContext
    void buildThrowsExceptionWhenCustomClocksSizeMismatchTest() {
        // Build test objects
        final List<TopocentricFrame> topocentricFrames =
            buildTopocentricFrames();
        final BoundedPropagator propagator =
            dummyPropagator(startDate, finalDate, dummyOrbit(startDate));
        final Spacecraft spacecraft =
            Spacecraft.builder(propagator, clock).build();
        final List<Clock> wrongSizeClocks = Arrays.asList(clock, clock); // Only
        // 2
        // clocks
        // for
        // 3
        // frames

        // Verify exception is thrown
        Assertions.assertThrows(OresiumException.class, () -> {
            new MultipleLineOfVisibilityBuilder(topocentricFrames, spacecraft)
                .withCustomClocks(wrongSizeClocks).build();
        });
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
}
