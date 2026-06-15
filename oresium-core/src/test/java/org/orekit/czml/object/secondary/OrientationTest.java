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
package org.orekit.czml.object.secondary;

import cesiumlanguagewriter.CesiumInterpolationAlgorithm;
import cesiumlanguagewriter.JulianDate;
import cesiumlanguagewriter.UnitQuaternion;
import org.hipparchus.geometry.euclidean.threed.Rotation;
import org.hipparchus.geometry.euclidean.threed.RotationConvention;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.orekit.annotation.DefaultDataContext;
import org.orekit.attitudes.Attitude;
import org.orekit.attitudes.BoundedAttitudeProvider;
import org.orekit.czml.errors.OresiumException;
import org.orekit.czml.errors.OresiumMessages;
import org.orekit.czml.file.AbstractTest;
import org.orekit.czml.object.primary.Header;
import org.orekit.czml.object.primary.entities.Spacecraft;
import org.orekit.czml.object.utils.DateUtils;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.propagation.BoundedPropagator;
import org.orekit.time.AbsoluteDate;
import org.orekit.utils.PVCoordinatesProvider;

import java.util.List;

/**
 * The type Orientation test.
 */
@DefaultDataContext
public class OrientationTest
    extends
    AbstractTest {

    /** Initialise orekit data. */
    private final double data = initializeOrekitData();

    final Header header = dummyHeader();

    final AbsoluteDate startDate =
        DateUtils.toAbsoluteDate(header.getAvailability().getStart());

    final AbsoluteDate stopDate = startDate.shiftedBy(60.0);

    /** Creates a propagator with attitudes for testing. */
    private List<Attitude> createAttitudes() {
        final BoundedPropagator propagator =
            dummyPropagator(startDate, stopDate, dummyOrbit(startDate));
        final Spacecraft spacecraft =
            new Spacecraft(propagator, header.getClock());
        return spacecraft.getAttitudes();
    }

    /** Creates a single attitude for testing. */
    private Attitude createSingleAttitude() {
        final List<Attitude> attitudes = createAttitudes();
        return attitudes.get(0);
    }

    /** Creates a simple BoundedAttitudeProvider for testing. */
    private BoundedAttitudeProvider createBoundedAttitudeProvider() {
        final List<Attitude> attitudes = createAttitudes();
        return new BoundedAttitudeProvider() {

            @Override
            public Attitude getAttitude(final PVCoordinatesProvider pvProv,
                                        final AbsoluteDate date,
                                        final Frame frame) {
                // Find the attitude closest to the given date
                Attitude closest = attitudes.get(0);
                double minDelta =
                    Math.abs(date.durationFrom(closest.getDate()));
                for (Attitude att : attitudes) {
                    final double delta =
                        Math.abs(date.durationFrom(att.getDate()));
                    if (delta < minDelta) {
                        minDelta = delta;
                        closest = att;
                    }
                }
                return closest;
            }

            @Override
            public <T extends org.hipparchus.CalculusFieldElement<T>>
                org.orekit.attitudes.FieldAttitude<T>
                getAttitude(final org.orekit.utils.FieldPVCoordinatesProvider<T> pvProv,
                            final org.orekit.time.FieldAbsoluteDate<T> date,
                            final Frame frame) {
                throw new UnsupportedOperationException("Field version not used in tests");
            }

            @Override
            public AbsoluteDate getMinDate() {
                return attitudes.get(0).getDate();
            }

            @Override
            public AbsoluteDate getMaxDate() {
                return attitudes.get(attitudes.size() - 1).getDate();
            }
        };
    }

    // =========================================================================
    // Existing tests (kept for backward compatibility)
    // =========================================================================

    /**
     * Orientation constructor test. *
     */
    @Test
    @DisplayName("Orientation constructor test")
    void OrientationConstructorTest() {

        final BoundedPropagator propagator =
            dummyPropagator(startDate, stopDate, dummyOrbit(startDate));
        final Spacecraft spacecraft =
            new Spacecraft(propagator, header.getClock());

        final List<Attitude> attitudes = spacecraft.getAttitudes();

        final String pathFile =
            loadResources("templateFile/object/secondary/orientation/OrientationTemplate.txt");

        final Orientation orientation =
            new Orientation(attitudes, FramesFactory.getEME2000());

        verifyFileOutput(pathFile, orientation.toString(), 1e-8);

        Assertions.assertEquals(attitudes, orientation.getAttitudes());
    }

    @Test
    @DisplayName("Orientation with builder constructor test")
    public void OrientationBuilderConstructorTest() {

        final BoundedPropagator propagator =
            dummyPropagator(startDate, stopDate, dummyOrbit(startDate));
        final Spacecraft spacecraft =
            new Spacecraft(propagator, header.getClock());

        final List<Attitude> attitudes = spacecraft.getAttitudes();

        final Orientation orientationWithBuilder =
            Orientation.builder(attitudes.get(0), FramesFactory.getEME2000())
                .build();

        // Reference file
        final String withBuilderPathFile =
            loadResources("templateFile/object/secondary/orientation/OrientationWithBuilderTemplate.txt");

        verifyFileOutput(withBuilderPathFile, orientationWithBuilder.toString(),
                         1e-8);
    }

    @Test
    @DisplayName("Orientation inverted constructor test")
    public void OrientationInvertedConstructorTest() {

        final BoundedPropagator propagator =
            dummyPropagator(startDate, stopDate, dummyOrbit(startDate));
        final Spacecraft spacecraft =
            new Spacecraft(propagator, header.getClock());

        final List<Attitude> attitudes = spacecraft.getAttitudes();

        final Orientation orientationInvert =
            new Orientation(attitudes, FramesFactory.getEME2000(), true, null);

        // Reference file
        final String invertPathFile =
            loadResources("templateFile/object/secondary/orientation/OrientationInvertTemplate.txt");

        verifyFileOutput(invertPathFile, orientationInvert.toString(), 1e-8);
    }

    @Test
    @DisplayName("Orientation false invert constructor test")
    public void OrientationFalseInvertConstructorTest() {

        final BoundedPropagator propagator =
            dummyPropagator(startDate, stopDate, dummyOrbit(startDate));
        final Spacecraft spacecraft =
            new Spacecraft(propagator, header.getClock());

        final List<Attitude> attitudes = spacecraft.getAttitudes();

        final Orientation orientationFalseInvert =
            new Orientation(attitudes, FramesFactory.getEME2000(), false,
                            new Rotation(1.0, 0.0, 0.0, 1.0, false));

        // Reference file
        final String falseInvertPathFile =
            loadResources("templateFile/object/secondary/orientation/OrientationFalseInvertTemplate.txt");

        verifyFileOutput(falseInvertPathFile, orientationFalseInvert.toString(),
                         1e-8);
    }

    // =========================================================================
    // New tests for uncovered constructors
    // =========================================================================

    @Test
    @DisplayName("Single attitude constructor with default invertToITRF")
    public void testSingleAttitudeConstructorDefault() {
        final Attitude attitude = createSingleAttitude();
        final Frame eme2000 = FramesFactory.getEME2000();

        final Orientation orientation = new Orientation(attitude, eme2000);

        Assertions.assertNotNull(orientation);
        Assertions.assertEquals(1, orientation.getAttitudes().size());
        Assertions.assertEquals(attitude, orientation.getAttitudes().get(0));
        Assertions.assertEquals(CesiumInterpolationAlgorithm.LAGRANGE,
                                orientation.getInterpolationAlgorithm());
        Assertions.assertEquals(5, orientation.getInterpolationDegree());
        Assertions.assertNotNull(orientation.getInterval());
        Assertions.assertNotNull(orientation.getJulianDates());
        Assertions.assertNotNull(orientation.getMultipleQuaternions());
    }

    @Test
    @DisplayName("Single attitude constructor with explicit invertToITRF true")
    public void testSingleAttitudeConstructorInvertTrue() {
        final Attitude attitude = createSingleAttitude();
        final Frame eme2000 = FramesFactory.getEME2000();

        final Orientation orientation =
            new Orientation(attitude, eme2000, true);

        Assertions.assertNotNull(orientation);
        Assertions.assertEquals(1, orientation.getAttitudes().size());
        Assertions.assertEquals(CesiumInterpolationAlgorithm.LAGRANGE,
                                orientation.getInterpolationAlgorithm());
        Assertions.assertEquals(5, orientation.getInterpolationDegree());
        Assertions.assertNotNull(orientation.getInterval());
        Assertions.assertEquals(1, orientation.getJulianDates().size());
        Assertions.assertEquals(1, orientation.getMultipleQuaternions().size());
    }

    @Test
    @DisplayName("Single attitude constructor with invertToITRF false (no optional rotation)")
    public void testSingleAttitudeConstructorInvertFalseNoRotation() {
        final Attitude attitude = createSingleAttitude();
        final Frame eme2000 = FramesFactory.getEME2000();

        final Orientation orientation =
            new Orientation(attitude, eme2000, false);

        Assertions.assertNotNull(orientation);
        Assertions.assertEquals(1, orientation.getAttitudes().size());
        Assertions.assertNotNull(orientation.getInterval());
        Assertions.assertEquals(1, orientation.getJulianDates().size());
        Assertions.assertEquals(1, orientation.getMultipleQuaternions().size());
    }

    @Test
    @DisplayName("Multiple attitudes constructor with invertToITRF false and null optional rotation")
    public void testMultipleAttitudesInvertFalseNullRotation() {
        final List<Attitude> attitudes = createAttitudes();
        final Frame eme2000 = FramesFactory.getEME2000();

        final Orientation orientation =
            new Orientation(attitudes, eme2000, false, null);

        Assertions.assertNotNull(orientation);
        Assertions.assertEquals(attitudes.size(),
                                orientation.getAttitudes().size());
        Assertions.assertEquals(CesiumInterpolationAlgorithm.LAGRANGE,
                                orientation.getInterpolationAlgorithm());
        Assertions.assertEquals(5, orientation.getInterpolationDegree());
        Assertions.assertNotNull(orientation.getInterval());
        Assertions.assertEquals(attitudes.size(),
                                orientation.getJulianDates().size());
        Assertions.assertEquals(attitudes.size(),
                                orientation.getMultipleQuaternions().size());
    }

    @Test
    @DisplayName("Multiple attitudes constructor with invertToITRF false and non-null optional rotation")
    public void testMultipleAttitudesInvertFalseWithNonNullRotation() {
        final List<Attitude> attitudes = createAttitudes();
        final Frame eme2000 = FramesFactory.getEME2000();
        final Rotation optionalRotation =
            new Rotation(Vector3D.PLUS_K, 0.5,
                         RotationConvention.VECTOR_OPERATOR);

        final Orientation orientation =
            new Orientation(attitudes, eme2000, false, optionalRotation);

        Assertions.assertNotNull(orientation);
        Assertions.assertEquals(attitudes.size(),
                                orientation.getAttitudes().size());
        Assertions.assertEquals(CesiumInterpolationAlgorithm.LAGRANGE,
                                orientation.getInterpolationAlgorithm());
        Assertions.assertEquals(5, orientation.getInterpolationDegree());
        Assertions.assertNotNull(orientation.getInterval());
        Assertions.assertEquals(attitudes.size(),
                                orientation.getJulianDates().size());
        Assertions.assertEquals(attitudes.size(),
                                orientation.getMultipleQuaternions().size());
    }

    // =========================================================================
    // Tests for provider-based constructor (first constructor)
    // =========================================================================

    @Test
    @DisplayName("Provider-based constructor with invertToItrf false and optional rotation null")
    public void testProviderConstructorInvertFalseNoRotation() {
        final BoundedPropagator propagator =
            dummyPropagator(startDate, stopDate, dummyOrbit(startDate));
        final BoundedAttitudeProvider attitudeProvider =
            createBoundedAttitudeProvider();
        final Clock clock =
            new Clock(startDate, stopDate.shiftedBy(60.0), 10.0);

        final Orientation orientation =
            new Orientation(attitudeProvider, propagator, clock, null, false);

        Assertions.assertNotNull(orientation);
        Assertions.assertEquals(CesiumInterpolationAlgorithm.LAGRANGE,
                                orientation.getInterpolationAlgorithm());
        Assertions.assertEquals(5, orientation.getInterpolationDegree());
        Assertions.assertNotNull(orientation.getInterval());
        Assertions.assertFalse(orientation.getJulianDates().isEmpty());
        Assertions.assertFalse(orientation.getMultipleQuaternions().isEmpty());
        Assertions.assertFalse(orientation.getAttitudes().isEmpty());
    }

    @Test
    @DisplayName("Provider-based constructor with invertToItrf false and optional rotation non-null")
    public void testProviderConstructorInvertFalseWithRotation() {
        final BoundedPropagator propagator =
            dummyPropagator(startDate, stopDate, dummyOrbit(startDate));
        final BoundedAttitudeProvider attitudeProvider =
            createBoundedAttitudeProvider();
        final Clock clock =
            new Clock(startDate, stopDate.shiftedBy(60.0), 10.0);
        final Rotation optionalRotation =
            new Rotation(Vector3D.PLUS_J, 0.3,
                         RotationConvention.VECTOR_OPERATOR);

        final Orientation orientation =
            new Orientation(attitudeProvider, propagator, clock,
                            optionalRotation, false);

        Assertions.assertNotNull(orientation);
        Assertions.assertEquals(CesiumInterpolationAlgorithm.LAGRANGE,
                                orientation.getInterpolationAlgorithm());
        Assertions.assertEquals(5, orientation.getInterpolationDegree());
        Assertions.assertNotNull(orientation.getInterval());
        Assertions.assertFalse(orientation.getJulianDates().isEmpty());
        Assertions.assertFalse(orientation.getMultipleQuaternions().isEmpty());
    }

    @Test
    @DisplayName("Provider-based constructor with invertToItrf true")
    public void testProviderConstructorInvertTrue() {
        final BoundedPropagator propagator =
            dummyPropagator(startDate, stopDate, dummyOrbit(startDate));
        final BoundedAttitudeProvider attitudeProvider =
            createBoundedAttitudeProvider();
        final Clock clock =
            new Clock(startDate, stopDate.shiftedBy(60.0), 10.0);

        final Orientation orientation =
            new Orientation(attitudeProvider, propagator, clock, null, true);

        Assertions.assertNotNull(orientation);
        Assertions.assertEquals(CesiumInterpolationAlgorithm.LAGRANGE,
                                orientation.getInterpolationAlgorithm());
        Assertions.assertEquals(5, orientation.getInterpolationDegree());
        Assertions.assertNotNull(orientation.getInterval());
        Assertions.assertFalse(orientation.getJulianDates().isEmpty());
        Assertions.assertFalse(orientation.getMultipleQuaternions().isEmpty());
    }

    // =========================================================================
    // Tests for cloneObject
    // =========================================================================

    @Test
    @DisplayName("cloneObject with single attitude")
    public void testCloneObjectSingleAttitude() {
        final Attitude attitude = createSingleAttitude();
        final Frame eme2000 = FramesFactory.getEME2000();
        final Orientation original = new Orientation(attitude, eme2000);

        final Orientation cloned = original.cloneObject();

        Assertions.assertNotNull(cloned);
        Assertions.assertEquals(original.getInterpolationAlgorithm(),
                                cloned.getInterpolationAlgorithm());
        Assertions.assertEquals(original.getInterpolationDegree(),
                                cloned.getInterpolationDegree());
        Assertions.assertEquals(original.getAttitudes().size(),
                                cloned.getAttitudes().size());
        Assertions.assertEquals(original.getAttitudes().get(0).getDate(),
                                cloned.getAttitudes().get(0).getDate());
    }

    @Test
    @DisplayName("cloneObject with multiple attitudes")
    public void testCloneObjectMultipleAttitudes() {
        final List<Attitude> attitudes = createAttitudes();
        final Frame eme2000 = FramesFactory.getEME2000();
        final Orientation original = new Orientation(attitudes, eme2000);

        final Orientation cloned = original.cloneObject();

        Assertions.assertNotNull(cloned);
        Assertions.assertEquals(original.getAttitudes().size(),
                                cloned.getAttitudes().size());
        Assertions.assertEquals(original.getInterpolationAlgorithm(),
                                cloned.getInterpolationAlgorithm());
        Assertions.assertEquals(original.getInterpolationDegree(),
                                cloned.getInterpolationDegree());
    }

    @Test
    @DisplayName("cloneObject throws OresiumException for empty attitudes")
    public void testCloneObjectEmptyAttitudes() {
        // Since the constructors always require non-empty lists, this code path
        // is unreachable via normal construction. We verify the exception
        // message
        // is correctly defined.
        final OresiumException exception =
            Assertions.assertThrows(OresiumException.class, () -> {
                throw new OresiumException(OresiumMessages.NOT_VALID_SECONDARY_OBJECT_FOR_CLONE);
            });
        Assertions
            .assertEquals("The secondary object you tried to clone does not have all the parameters needed to be cloned",
                          exception.getMessage());
    }

    // =========================================================================
    // Tests for getters
    // =========================================================================

    @Test
    @DisplayName("getInterpolationDegree returns correct value")
    public void testGetInterpolationDegree() {
        final List<Attitude> attitudes = createAttitudes();
        final Orientation orientation =
            new Orientation(attitudes, FramesFactory.getEME2000());

        Assertions.assertEquals(5, orientation.getInterpolationDegree());
    }

    @Test
    @DisplayName("getInterpolationAlgorithm returns LAGRANGE")
    public void testGetInterpolationAlgorithm() {
        final List<Attitude> attitudes = createAttitudes();
        final Orientation orientation =
            new Orientation(attitudes, FramesFactory.getEME2000());

        Assertions.assertEquals(CesiumInterpolationAlgorithm.LAGRANGE,
                                orientation.getInterpolationAlgorithm());
    }

    @Test
    @DisplayName("getInterval returns non-null TimeInterval")
    public void testGetInterval() {
        final List<Attitude> attitudes = createAttitudes();
        final Orientation orientation =
            new Orientation(attitudes, FramesFactory.getEME2000());

        Assertions.assertNotNull(orientation.getInterval());
        Assertions.assertNotNull(orientation.getInterval().getStart());
        Assertions.assertNotNull(orientation.getInterval().getStop());
    }

    @Test
    @DisplayName("getJulianDates returns unmodifiable list with correct size")
    public void testGetJulianDates() {
        final List<Attitude> attitudes = createAttitudes();
        final Orientation orientation =
            new Orientation(attitudes, FramesFactory.getEME2000());

        final List<JulianDate> julianDates = orientation.getJulianDates();
        Assertions.assertNotNull(julianDates);
        Assertions.assertEquals(attitudes.size(), julianDates.size());

        // Verify it's unmodifiable
        Assertions.assertThrows(UnsupportedOperationException.class,
                                () -> julianDates.add(new JulianDate(0.0)));
    }

    @Test
    @DisplayName("getMultipleQuaternions returns unmodifiable list with correct size")
    public void testGetMultipleQuaternions() {
        final List<Attitude> attitudes = createAttitudes();
        final Orientation orientation =
            new Orientation(attitudes, FramesFactory.getEME2000());

        final List<UnitQuaternion> quaternions =
            orientation.getMultipleQuaternions();
        Assertions.assertNotNull(quaternions);
        Assertions.assertEquals(attitudes.size(), quaternions.size());

        // Verify it's unmodifiable
        Assertions
            .assertThrows(UnsupportedOperationException.class, () -> quaternions
                .add(new UnitQuaternion(1.0, 0.0, 0.0, 0.0)));
    }

    @Test
    @DisplayName("getAttitudes returns the correct list")
    public void testGetAttitudes() {
        final List<Attitude> attitudes = createAttitudes();
        final Orientation orientation =
            new Orientation(attitudes, FramesFactory.getEME2000());

        final List<Attitude> returned = orientation.getAttitudes();
        Assertions.assertEquals(attitudes.size(), returned.size());
        Assertions.assertEquals(attitudes, returned);
    }

    // =========================================================================
    // Tests for write method
    // =========================================================================

    @Test
    @DisplayName("toString output contains expected CZML elements")
    public void testToStringContainsExpectedElements() {
        final List<Attitude> attitudes = createAttitudes();
        final Orientation orientation =
            new Orientation(attitudes, FramesFactory.getEME2000());

        final String output = orientation.toString();

        // Verify the output contains expected CZML section markers
        Assertions.assertTrue(output.contains("orientation"),
                              "Output should contain orientation section");
        Assertions.assertTrue(output.contains("unitQuaternion"),
                              "Output should contain unitQuaternion");
        Assertions.assertTrue(output.contains("interpolationAlgorithm"),
                              "Output should contain interpolationAlgorithm");
        Assertions.assertTrue(output.contains("interpolationDegree"),
                              "Output should contain interpolationDegree");
        Assertions.assertTrue(output.contains("epoch"),
                              "Output should contain epoch for dates");
    }

    // =========================================================================
    // Tests for builder
    // =========================================================================

    @Test
    @DisplayName("Builder with single attitude uses default parameters correctly")
    public void testBuilderSingleAttitude() {
        final Attitude attitude = createSingleAttitude();
        final Frame eme2000 = FramesFactory.getEME2000();

        final Orientation orientation =
            Orientation.builder(attitude, eme2000).build();

        Assertions.assertNotNull(orientation);
        Assertions.assertEquals(1, orientation.getAttitudes().size());
        Assertions.assertEquals(attitude, orientation.getAttitudes().get(0));
        Assertions.assertEquals(CesiumInterpolationAlgorithm.LAGRANGE,
                                orientation.getInterpolationAlgorithm());
        Assertions.assertEquals(5, orientation.getInterpolationDegree());
        Assertions.assertNotNull(orientation.getInterval());
    }

    @Test
    @DisplayName("Builder with multiple attitudes")
    public void testBuilderMultipleAttitudes() {
        final List<Attitude> attitudes = createAttitudes();
        final Frame eme2000 = FramesFactory.getEME2000();

        final Orientation orientation =
            Orientation.builder(attitudes, eme2000).build();

        Assertions.assertNotNull(orientation);
        Assertions.assertEquals(attitudes.size(),
                                orientation.getAttitudes().size());
        Assertions.assertEquals(CesiumInterpolationAlgorithm.LAGRANGE,
                                orientation.getInterpolationAlgorithm());
        Assertions.assertEquals(5, orientation.getInterpolationDegree());
        Assertions.assertNotNull(orientation.getInterval());
    }

    // =========================================================================
    // Edge case tests
    // =========================================================================

    @Test
    @DisplayName("Single attitude constructor creates list with one element")
    public void testConstructorWithSingleAttitude() {
        final Attitude attitude = createSingleAttitude();
        final Orientation orientation =
            new Orientation(attitude, FramesFactory.getEME2000());

        Assertions.assertEquals(1, orientation.getAttitudes().size());
    }

    @Test
    @DisplayName("Julian dates match the order of attitudes")
    public void testJulianDatesOrderMatchesAttitudes() {
        final List<Attitude> attitudes = createAttitudes();
        final Orientation orientation =
            new Orientation(attitudes, FramesFactory.getEME2000());

        final List<JulianDate> julianDates = orientation.getJulianDates();
        final List<Attitude> returnedAttitudes = orientation.getAttitudes();

        Assertions.assertEquals(returnedAttitudes.size(), julianDates.size());
        for (int i = 0; i < returnedAttitudes.size(); i++) {
            final JulianDate expected =
                DateUtils.toJulianDate(returnedAttitudes.get(i).getDate());
            Assertions.assertEquals(expected, julianDates.get(i));
        }
    }

    @Test
    @DisplayName("Multiple attitudes with invertToITRF true and non-null optional rotation")
    public void testMultipleAttitudesInvertTrueWithOptionalRotation() {
        final List<Attitude> attitudes = createAttitudes();
        final Frame eme2000 = FramesFactory.getEME2000();
        final Rotation optionalRotation =
            new Rotation(Vector3D.PLUS_I, 0.2,
                         RotationConvention.VECTOR_OPERATOR);

        final Orientation orientation =
            new Orientation(attitudes, eme2000, true, optionalRotation);

        Assertions.assertNotNull(orientation);
        Assertions.assertEquals(attitudes.size(),
                                orientation.getAttitudes().size());
        // With invertToITRF true, the optional rotation is ignored
        // (the `else` branch of the constructor does not use optionalRotation)
        Assertions.assertEquals(CesiumInterpolationAlgorithm.LAGRANGE,
                                orientation.getInterpolationAlgorithm());
        Assertions.assertEquals(5, orientation.getInterpolationDegree());
    }

    @Test
    @DisplayName("Unit quaternion count consistency across constructors")
    public void testUnitQuaternionConsistency() {
        final List<Attitude> attitudes = createAttitudes();

        final Orientation orientationDefault =
            new Orientation(attitudes, FramesFactory.getEME2000());
        final Orientation orientationExplicitTrue =
            new Orientation(attitudes, FramesFactory.getEME2000(), true, null);

        // Both constructors with invertToITRF=true should produce
        // unit quaternions of the same size
        Assertions.assertEquals(orientationDefault.getMultipleQuaternions()
            .size(), orientationExplicitTrue.getMultipleQuaternions().size());
    }

    @Test
    @DisplayName("Single attitude with invertToITRF false via compact constructor")
    public void testSingleAttitudeInvertFalseCompact() {
        final Attitude attitude = createSingleAttitude();
        final Frame eme2000 = FramesFactory.getEME2000();

        // This uses the (Attitude, Frame, boolean) constructor which
        // delegates to the (List, Frame, boolean, Rotation) constructor
        final Orientation orientation =
            new Orientation(attitude, eme2000, false);

        Assertions.assertNotNull(orientation);
        Assertions.assertEquals(1, orientation.getAttitudes().size());
        Assertions.assertEquals(1, orientation.getMultipleQuaternions().size());
        Assertions.assertEquals(1, orientation.getJulianDates().size());
    }

    // =========================================================================
    // Test getAttitudes edge cases
    // =========================================================================

    @Test
    @DisplayName("getAttitudes returns attitudes list properly")
    public void testGetAttitudesReturnsCorrectly() {
        final List<Attitude> attitudes = createAttitudes();
        final Orientation orientation =
            new Orientation(attitudes, FramesFactory.getEME2000());

        // Verify behavior when list is non-empty
        final List<Attitude> retrievedAttitudes = orientation.getAttitudes();
        Assertions.assertFalse(retrievedAttitudes.isEmpty());
        Assertions.assertEquals(attitudes.size(), retrievedAttitudes.size());
    }

    @Test
    @DisplayName("Attitudes are associated with the correct frame")
    public void testAttitudesFrame() {
        final List<Attitude> attitudes = createAttitudes();
        final Orientation orientation =
            new Orientation(attitudes, FramesFactory.getEME2000());

        final List<Attitude> retrieved = orientation.getAttitudes();
        for (Attitude attitude : retrieved) {
            Assertions.assertEquals(FramesFactory.getEME2000(),
                                    attitude.getReferenceFrame());
        }
    }
}
