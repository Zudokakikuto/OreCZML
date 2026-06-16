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
package org.orekit.czml.object;

import cesiumlanguagewriter.Cartesian;
import cesiumlanguagewriter.CesiumArcType;
import cesiumlanguagewriter.CesiumOutputStream;
import cesiumlanguagewriter.CesiumStreamWriter;
import cesiumlanguagewriter.Duration;
import cesiumlanguagewriter.PacketCesiumWriter;
import cesiumlanguagewriter.Reference;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.orekit.annotation.DefaultDataContext;
import org.orekit.czml.errors.OresiumException;
import org.orekit.czml.errors.OresiumMessages;
import org.orekit.czml.file.AbstractTest;
import org.orekit.czml.object.primary.Header;
import org.orekit.czml.object.secondary.Clock;
import org.orekit.czml.object.utils.DateUtils;
import org.orekit.time.AbsoluteDate;

import java.awt.Color;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The type Polyline test.
 */
public class PolylineTest
    extends
    AbstractTest {

    /** Initialise orekit data. */
    private final double data = initializeOrekitData();

    /** Header. */
    private final Header header = dummyHeader();

    /**
     * Polyline constructor test.
     */
    @Test
    @DefaultDataContext
    void PolylineConstructorTest() {

        final Polyline polyline =
            Polyline.nonVectorBuilder(header.getClock()).build();

        final String pathFile =
            loadResources("templateFile/object/unclassed/polyline/PolylineTemplate.txt");

        verifyFileOutput(pathFile, polyline.toString(), 1e-8);
    }

    @Test
    @DisplayName("Polyline non vector constructor test")
    public void PolylineNonVectorConstructorTest() {

        final Polyline polylineNonVector =
            Polyline.nonVectorBuilder(header.getClock()).withColor(Color.ORANGE)
                .withArcType(CesiumArcType.NONE).withShow(true).withWidth(10.0)
                .withFarDistance(10.0).withNearDistance(1.0)
                .withFirstReference(new Reference("sat#position"))
                .withSecondReference(new Reference("groundstation#position"))
                .build();

        // Reference file
        final String nonVectorPathFile =
            loadResources("templateFile/object/unclassed/polyline/PolylineNonVectorTemplate.txt");

        verifyFileOutput(nonVectorPathFile, polylineNonVector.toString(), 1e-8);
    }

    /**
     * Test constructor 1: Polyline(final Clock clock)
     */
    @Test
    @DefaultDataContext
    void testConstructor1() {
        // Given
        final Clock clock = header.getClock();

        // When
        final Polyline polyline = new Polyline(clock);

        // Then
        assertEquals(Polyline.DEFAULT_REFERENCE, polyline.getFirstReference());
        assertEquals(Polyline.DEFAULT_REFERENCE, polyline.getSecondReference());
        assertEquals(Polyline.DEFAULT_COLOR, polyline.getColor());
        assertEquals(Polyline.DEFAULT_WIDTH, polyline.getWidth());
        assertEquals(Polyline.DEFAULT_SHOW, polyline.getShow());
        assertEquals(Polyline.DEFAULT_ARC_TYPE, polyline.getArcType());
        assertEquals(Polyline.DEFAULT_NEAR_DISTANCE,
                     polyline.getNearDistance());
        assertEquals(Polyline.DEFAULT_FAR_DISTANCE, polyline.getFarDistance());
        assertEquals(clock, polyline.getClock());
    }

    /**
     * Test constructor 3: Polyline(final List<Cartesian> cartesians, final
     * Color color, final double nearDistance, final double farDistance, final
     * Clock clock)
     */
    @Test
    @DefaultDataContext
    void testConstructor3() {
        // Given
        final Clock clock = header.getClock();
        final List<Cartesian> cartesians =
            Arrays.asList(new Cartesian(0, 0, 0), new Cartesian(1000, 0, 0));
        final Color color = Color.RED;
        final double nearDistance = 5.0;
        final double farDistance = 5000.0;

        // When
        final Polyline polyline =
            new Polyline(cartesians, color, nearDistance, farDistance, clock);

        // Then
        assertTrue(polyline.getShow());
        assertEquals(2.0, polyline.getWidth());
        assertEquals(CesiumArcType.NONE, polyline.getArcType());
        assertEquals(color, polyline.getColor());
        assertEquals(nearDistance, polyline.getNearDistance());
        assertEquals(farDistance, polyline.getFarDistance());
        assertEquals(clock, polyline.getClock());
    }

    /**
     * Test throw of constructor 4: Polyline(final List<Cartesian> cartesians,
     * ...) when cartesians.size() != 2
     */
    @Test
    @DefaultDataContext
    void testConstructor4Throw() {
        // Given
        final Clock clock = header.getClock();
        final List<Cartesian> cartesians =
            Arrays.asList(new Cartesian(0, 0, 0), new Cartesian(1000, 0, 0),
                          new Cartesian(2000, 0, 0));
        final Color color = Color.RED;
        final double nearDistance = 5.0;
        final double farDistance = 5000.0;

        // When & Then
        final OresiumException exception =
            assertThrows(OresiumException.class,
                         () -> new Polyline(cartesians, color, nearDistance,
                                            farDistance, clock));

        assertEquals(OresiumMessages.MORE_THAN_2_CARTESIAN_POLYLINE,
                     exception.getSpecifier());
    }

    /**
     * Test setAvailability method
     */
    @Test
    @DefaultDataContext
    void testSetAvailability() {
        // Given
        final Clock clock1 = header.getClock();
        final AbsoluteDate startDate =
            DateUtils.toAbsoluteDate(clock1.getAvailability().getStart());
        final AbsoluteDate finalDate =
            DateUtils.toAbsoluteDate(clock1.getAvailability().getStop())
                .shiftedBy(3600.0);
        final Clock clock2 = new Clock(startDate, finalDate, 60.0);
        final Polyline polyline = new Polyline(clock1);

        // When
        polyline.setAvailability(clock2);

        // Then
        assertEquals(clock2, polyline.getClock());
    }

    /**
     * Test getShow method
     */
    @Test
    @DefaultDataContext
    void testGetShow() {
        // Given
        final Clock clock = header.getClock();
        final Polyline polylineNonVector =
            new Polyline(new Reference("sat#position"),
                         new Reference("groundstation#position"), Color.BLUE,
                         5.0, false, CesiumArcType.GEODESIC, 1.0, 10000.0,
                         clock);

        final Polyline polylineVector =
            new Polyline(Arrays.asList(new Cartesian(0, 0, 0),
                                       new Cartesian(1000, 0, 0)),
                         clock);

        // When & Then
        assertFalse(polylineNonVector.getShow());
        assertTrue(polylineVector.getShow());
    }

    /**
     * Test getFirstReference method
     */
    @Test
    @DefaultDataContext
    void testGetFirstReference() {
        // Given
        final Clock clock = header.getClock();
        final Reference firstReference = new Reference("sat#position");
        final Reference secondReference =
            new Reference("groundstation#position");
        final Polyline polyline =
            new Polyline(firstReference, secondReference, Color.BLUE, 5.0, true,
                         CesiumArcType.GEODESIC, 1.0, 10000.0, clock);

        // When & Then
        assertEquals(firstReference, polyline.getFirstReference());
    }

    /**
     * Test getSecondReference method
     */
    @Test
    @DefaultDataContext
    void testGetSecondReference() {
        // Given
        final Clock clock = header.getClock();
        final Reference firstReference = new Reference("sat#position");
        final Reference secondReference =
            new Reference("groundstation#position");
        final Polyline polyline =
            new Polyline(firstReference, secondReference, Color.BLUE, 5.0, true,
                         CesiumArcType.GEODESIC, 1.0, 10000.0, clock);

        // When & Then
        assertEquals(secondReference, polyline.getSecondReference());
    }

    /**
     * Test getNearDistance method
     */
    @Test
    @DefaultDataContext
    void testGetNearDistance() {
        // Given
        final Clock clock = header.getClock();
        final double nearDistance = 5.0;
        final double farDistance = 5000.0;

        // Test for non-vector polyline
        final Polyline polylineNonVector =
            new Polyline(new Reference("sat#position"),
                         new Reference("groundstation#position"), Color.BLUE,
                         5.0, true, CesiumArcType.GEODESIC, nearDistance,
                         farDistance, clock);

        // Test for vector polyline
        final Polyline polylineVector =
            new Polyline(Arrays.asList(new Cartesian(0, 0, 0),
                                       new Cartesian(1000, 0, 0)),
                         Color.RED, nearDistance, farDistance, clock);

        // When & Then
        assertEquals(nearDistance, polylineNonVector.getNearDistance());
        assertEquals(nearDistance, polylineVector.getNearDistance());
    }

    /**
     * Test getFarDistance method
     */
    @Test
    @DefaultDataContext
    void testGetFarDistance() {
        // Given
        final Clock clock = header.getClock();
        final double nearDistance = 5.0;
        final double farDistance = 5000.0;

        // Test for non-vector polyline
        final Polyline polylineNonVector =
            new Polyline(new Reference("sat#position"),
                         new Reference("groundstation#position"), Color.BLUE,
                         5.0, true, CesiumArcType.GEODESIC, nearDistance,
                         farDistance, clock);

        // Test for vector polyline
        final Polyline polylineVector =
            new Polyline(Arrays.asList(new Cartesian(0, 0, 0),
                                       new Cartesian(1000, 0, 0)),
                         Color.RED, nearDistance, farDistance, clock);

        // When & Then
        assertEquals(farDistance, polylineNonVector.getFarDistance());
        assertEquals(farDistance, polylineVector.getFarDistance());
    }

    /**
     * Test writePolylineVectorInertial method
     */
    @Test
    @DefaultDataContext
    void testWritePolylineVectorInertial() {
        // Given
        final Clock clock = header.getClock();
        final List<Cartesian> cartesians =
            Arrays.asList(new Cartesian(0, 0, 0), new Cartesian(1000, 0, 0));
        final Polyline polyline = new Polyline(cartesians, clock);

        final StringWriter writer = new StringWriter();
        final CesiumOutputStream output = new CesiumOutputStream(writer);
        final CesiumStreamWriter streamWriter = new CesiumStreamWriter();
        output.setPrettyFormatting(true);

        // When
        try (PacketCesiumWriter packet = streamWriter.openPacket(output)) {
            polyline.writePolylineVectorInertial(packet, output);
        }

        // Then
        final String result = writer.toString();
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    /**
     * Test throw of writePolylineVectorInertial when arrow is false
     */
    @Test
    @DefaultDataContext
    void testWritePolylineVectorInertialThrow() {
        // Given
        final Clock clock = header.getClock();
        final Polyline polyline =
            new Polyline(new Reference("sat#position"),
                         new Reference("groundstation#position"), Color.BLUE,
                         5.0, true, CesiumArcType.GEODESIC, 1.0, 10000.0,
                         clock);

        final StringWriter writer = new StringWriter();
        final CesiumOutputStream output = new CesiumOutputStream(writer);
        final CesiumStreamWriter streamWriter = new CesiumStreamWriter();
        output.setPrettyFormatting(true);

        // When & Then
        try (PacketCesiumWriter packet = streamWriter.openPacket(output)) {
            final OresiumException exception =
                assertThrows(OresiumException.class, () -> polyline
                    .writePolylineVectorInertial(packet, output));

            assertEquals(OresiumMessages.DEFAULT_CANT_CALL,
                         exception.getSpecifier());
        }
    }
}
