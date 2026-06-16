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
package org.orekit.czml.object.primary.pointing;

import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.orekit.annotation.DefaultDataContext;
import org.orekit.czml.file.AbstractTest;
import org.orekit.czml.object.primary.Header;
import org.orekit.czml.object.primary.entities.Spacecraft;
import org.orekit.czml.object.utils.DateUtils;
import org.orekit.propagation.BoundedPropagator;
import org.orekit.time.AbsoluteDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.orekit.czml.errors.OresiumException;

import java.awt.Color;

/**
 * The type Attitude pointing test.
 */
@DefaultDataContext
class AttitudePointingTest
    extends
    AbstractTest {

    /** Initialise orekit data. */
    private final double data = initializeOrekitData();

    /** Header. */
    final Header header = dummyHeader();

    // Dates

    /** Start date. */
    final AbsoluteDate startDate =
        DateUtils.toAbsoluteDate(header.getAvailability().getStart());

    /** Stop Date. */
    final AbsoluteDate stopDate = startDate.shiftedBy(60.0);

    /** Clock multiplier. */
    final double clockMultiplier = 10.0;

    /**
     * Attitude pointing constructor test. *
     */
    @Test
    void AttitudePointingConstructorTest() {

        final BoundedPropagator propagator =
            dummyPropagator(startDate, stopDate, dummyOrbit(startDate));

        final Spacecraft spacecraft =
            Spacecraft.builder(propagator, clockMultiplier)
                .withDisplayAttitude().build();

        final AttitudePointing attitudePointing =
            AttitudePointing.builder(spacecraft, getEarth(), Vector3D.MINUS_I,
                                     header.getClock())
                .build();
        attitudePointing.displayPointingPath();
        attitudePointing.displayPeriodPointingPath();

        final String pathFile =
            loadResources("templateFile/object/primary/pointing/attitudepointing/AttitudePointingTemplate.txt");

        verifyFileOutput(pathFile, attitudePointing.toString(), 1e-8);
    }

    @Test
    @DisplayName("Attitude Pointing with builder constructor Test")
    void AttitudePointingBuilderConstructorTest() {

        // Build Spacecraft
        final BoundedPropagator propagator =
            dummyPropagator(startDate, stopDate, dummyOrbit(startDate));
        final Spacecraft spacecraft =
            Spacecraft.builder(propagator, clockMultiplier)
                .withDisplayAttitude().build();

        final AttitudePointing attitudePointingWithBuilder =
            AttitudePointing
                .builder(spacecraft, getEarth(), Vector3D.MINUS_I,
                         header.getClock())
                .withCustomID("CustomID").withDisplayOnGround(false)
                .withColor(Color.ORANGE).build();

        // Reference file
        final String builderPathFile =
            loadResources("templateFile/object/primary/pointing/attitudepointing/AttitudePointingWithBuilderTemplate.txt");

        verifyFileOutput(builderPathFile,
                         attitudePointingWithBuilder.toString(), 1e-8);
    }

    @Test
    @DisplayName("Test displayPeriodPointingPath method - should throw when displayPointingPath is not set")
    void testDisplayPeriodPointingPath_throwsWhenPointingPathNotSet() {
        // Build Spacecraft
        final BoundedPropagator propagator =
            dummyPropagator(startDate, stopDate, dummyOrbit(startDate));
        final Spacecraft spacecraft =
            Spacecraft.builder(propagator, clockMultiplier)
                .withDisplayAttitude().build();

        final AttitudePointing attitudePointing =
            AttitudePointing.builder(spacecraft, getEarth(), Vector3D.MINUS_I,
                                     header.getClock())
                .build();

        // Should throw OresiumException because displayPointingPath is not set
        assertThrows(OresiumException.class,
                     () -> attitudePointing.displayPeriodPointingPath());
    }

    @Test
    @DisplayName("Test displayPeriodPointingPath method - should set flag when displayPointingPath is set")
    void testDisplayPeriodPointingPath_setsFlagWhenPointingPathIsSet() {
        // Build Spacecraft
        final BoundedPropagator propagator =
            dummyPropagator(startDate, stopDate, dummyOrbit(startDate));
        final Spacecraft spacecraft =
            Spacecraft.builder(propagator, clockMultiplier)
                .withDisplayAttitude().build();

        final AttitudePointing attitudePointing =
            AttitudePointing.builder(spacecraft, getEarth(), Vector3D.MINUS_I,
                                     header.getClock())
                .build();

        // First set displayPointingPath
        attitudePointing.displayPointingPath();

        // Then call displayPeriodPointingPath - should not throw
        attitudePointing.displayPeriodPointingPath();

        // Verify the flag is set
        assertTrue(attitudePointing.isDisplayPeriodPointingPath());
    }

    @Test
    @DisplayName("Test getSatellite method - should return the correct satellite")
    void testGetSatellite_returnsCorrectSatellite() {
        // Build Spacecraft
        final BoundedPropagator propagator =
            dummyPropagator(startDate, stopDate, dummyOrbit(startDate));
        final Spacecraft spacecraft =
            Spacecraft.builder(propagator, clockMultiplier)
                .withDisplayAttitude().build();

        final AttitudePointing attitudePointing =
            AttitudePointing.builder(spacecraft, getEarth(), Vector3D.MINUS_I,
                                     header.getClock())
                .build();

        // Verify the satellite is the same as the one used to create the
        // attitude pointing
        assertEquals(spacecraft, attitudePointing.getSatellite());
    }

    @Test
    @DisplayName("Test getJulianDates method - should return non-null and non-empty list")
    void testGetJulianDates_returnsNonNullAndNonEmptyList() {
        // Build Spacecraft
        final BoundedPropagator propagator =
            dummyPropagator(startDate, stopDate, dummyOrbit(startDate));
        final Spacecraft spacecraft =
            Spacecraft.builder(propagator, clockMultiplier)
                .withDisplayAttitude().build();

        final AttitudePointing attitudePointing =
            AttitudePointing.builder(spacecraft, getEarth(), Vector3D.MINUS_I,
                                     header.getClock())
                .build();

        // Verify the julian dates list is not null and not empty
        assertNotNull(attitudePointing.getJulianDates());
        assertFalse(attitudePointing.getJulianDates().isEmpty());
    }

    @Test
    @DisplayName("Test getBody method - should return the correct body")
    void testGetBody_returnsCorrectBody() {
        // Build Spacecraft
        final BoundedPropagator propagator =
            dummyPropagator(startDate, stopDate, dummyOrbit(startDate));
        final Spacecraft spacecraft =
            Spacecraft.builder(propagator, clockMultiplier)
                .withDisplayAttitude().build();

        final AttitudePointing attitudePointing =
            AttitudePointing.builder(spacecraft, getEarth(), Vector3D.MINUS_I,
                                     header.getClock())
                .build();

        // Verify the body is the same as the one used to create the attitude
        // pointing
        assertEquals(getEarth().getBodyFrame().getName(),
                     attitudePointing.getBody().getBodyFrame().getName());
    }

    @Test
    @DisplayName("Test setDisplayPeriodPointingPath method - should throw when displayPeriodPointingPath is not set")
    void testSetDisplayPeriodPointingPath_throwsWhenPeriodPointingPathNotSet() {
        // Build Spacecraft
        final BoundedPropagator propagator =
            dummyPropagator(startDate, stopDate, dummyOrbit(startDate));
        final Spacecraft spacecraft =
            Spacecraft.builder(propagator, clockMultiplier)
                .withDisplayAttitude().build();

        final AttitudePointing attitudePointing =
            AttitudePointing.builder(spacecraft, getEarth(), Vector3D.MINUS_I,
                                     header.getClock())
                .build();

        // Should throw OresiumException because displayPeriodPointingPath is
        // not set
        assertThrows(OresiumException.class,
                     () -> attitudePointing.setDisplayPeriodPointingPath(10.0));
    }

    @Test
    @DisplayName("Test setDisplayPeriodPointingPath method - should set period when displayPeriodPointingPath is set")
    void testSetDisplayPeriodPointingPath_setsPeriodWhenPeriodPointingPathIsSet() {
        // Build Spacecraft
        final BoundedPropagator propagator =
            dummyPropagator(startDate, stopDate, dummyOrbit(startDate));
        final Spacecraft spacecraft =
            Spacecraft.builder(propagator, clockMultiplier)
                .withDisplayAttitude().build();

        final AttitudePointing attitudePointing =
            AttitudePointing.builder(spacecraft, getEarth(), Vector3D.MINUS_I,
                                     header.getClock())
                .build();

        // First set displayPointingPath
        attitudePointing.displayPointingPath();

        // Then set displayPeriodPointingPath
        attitudePointing.displayPeriodPointingPath();

        // Set the period
        final double testPeriod = 20.0;
        attitudePointing.setDisplayPeriodPointingPath(testPeriod);

        // Verify the period is set correctly
        assertEquals(testPeriod, attitudePointing.getPeriodPointingPath());
    }

    @Test
    @DisplayName("Test getPeriodPointingPath method - should return the correct period")
    void testGetPeriodPointingPath_returnsCorrectPeriod() {
        // Build Spacecraft
        final BoundedPropagator propagator =
            dummyPropagator(startDate, stopDate, dummyOrbit(startDate));
        final Spacecraft spacecraft =
            Spacecraft.builder(propagator, clockMultiplier)
                .withDisplayAttitude().build();

        final AttitudePointing attitudePointing =
            AttitudePointing.builder(spacecraft, getEarth(), Vector3D.MINUS_I,
                                     header.getClock())
                .build();

        // First set displayPointingPath
        attitudePointing.displayPointingPath();

        // Then set displayPeriodPointingPath
        attitudePointing.displayPeriodPointingPath();

        // Set the period
        final double testPeriod = 15.0;
        attitudePointing.setDisplayPeriodPointingPath(testPeriod);

        // Verify the period is returned correctly
        assertEquals(testPeriod, attitudePointing.getPeriodPointingPath());
    }
}
