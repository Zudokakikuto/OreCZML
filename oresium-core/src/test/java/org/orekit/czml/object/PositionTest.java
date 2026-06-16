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

import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.orekit.annotation.DefaultDataContext;
import org.orekit.czml.errors.OresiumException;
import org.orekit.czml.file.AbstractTest;
import org.orekit.czml.object.primary.Header;

/**
 * The type Position test.
 */
public class PositionTest
    extends
    AbstractTest {

    /** Initialise orekit data. */
    private final double data = initializeOrekitData();

    /** Header. */
    final Header header = dummyHeader();

    /**
     * Position constructor test.
     */
    @Test
    @DefaultDataContext
    @DisplayName("Position constructor test")
    void PositionConstructorTest() {

        final Position positionCartesian =
            new Position(1, 45, 20, PositionType.CARTESIAN_POSITION,
                         header.getClock());

        final String cartesianPathFile =
            loadResources("templateFile/object/unclassed/position/PositionCartesianTemplate.txt");
        final String referenceFramePathFile =
            loadResources("templateFile/object/unclassed/position/PositionCartesianWithReferenceFrameTemplate.txt");

        verifyFileOutput(cartesianPathFile, positionCartesian.toString(), 1e-8);
        verifyFileOutput(referenceFramePathFile,
                         positionCartesian.toString("INERTIAL"), 1e-8);

        // Method coverage
        Assertions.assertEquals(new Vector3D(1, 45, 20),
                                positionCartesian.toVector3D());

        Assertions.assertEquals(1, positionCartesian.getX());
        Assertions.assertEquals(45, positionCartesian.getY());
        Assertions.assertEquals(20, positionCartesian.getZ());

        Assertions.assertEquals("INERTIAL",
                                positionCartesian.getReferenceFrame());
        Assertions.assertEquals(PositionType.CARTESIAN_POSITION,
                                positionCartesian.getPositionType());
    }

    @Test
    public void PositionConstructorExceptiontest() {

        final PositionType type = PositionType.PositionType;

        Assertions.assertThrows(OresiumException.class,
                                () -> new Position(1.0, 1.0, 1.0, type,
                                                   dummyHeader().getClock()));
    }

    @Test
    @DisplayName("Position in degree constructor test")
    public void PositionDegreesConstructorTest() {

        final Position positionDegrees =
            new Position(1, 45, 20, PositionType.CARTOGRAPHIC_DEGREES,
                         header.getClock());

        // Reference file
        final String degreesPathFile =
            loadResources("templateFile/object/unclassed/position/PositionDegreesTemplate.txt");

        verifyFileOutput(degreesPathFile, positionDegrees.toString(), 1e-8);

        Assertions.assertEquals(new Vector3D(1, 45, 20),
                                positionDegrees.toVector3D());
        Assertions.assertEquals(1, positionDegrees.getLongitudeDeg());
        Assertions.assertEquals(45, positionDegrees.getLatitudeDeg());
    }

    @Test
    @DisplayName("Position in radians cosntructor test")
    public void PositionRadiansConstructorTest() {

        final Position positionRadians =
            new Position(1, 45, 20, PositionType.CARTOGRAPHIC_RADIANS,
                         header.getClock());

        // Reference file
        final String radiansPathFile =
            loadResources("templateFile/object/unclassed/position/PositionRadiansTemplate.txt");

        verifyFileOutput(radiansPathFile, positionRadians.toString(), 1e-8);

        Assertions.assertEquals(new Vector3D(1, 45, 20),
                                positionRadians.toVector3D());
        Assertions.assertEquals(1, positionRadians.getLongitudeRad());
        Assertions.assertEquals(45, positionRadians.getLatitudeRad());
        Assertions.assertEquals(20, positionRadians.getHeight());
    }

    /**
     * Test for write() method with CARTESIAN_POSITION.
     */
    @Test
    @DisplayName("Test write() method with CARTESIAN_POSITION")
    public void testWriteCartesianPosition() {
        final Position positionCartesian =
            new Position(1.0, 45.0, 20.0, PositionType.CARTESIAN_POSITION,
                         header.getClock());

        // Test without reference frame
        final String cartesianOutput = positionCartesian.toString();
        final String cartesianPathFile =
            loadResources("templateFile/object/unclassed/position/PositionCartesianTemplate.txt");
        verifyFileOutput(cartesianPathFile, cartesianOutput, 1e-8);

        // Test with reference frame
        final String cartesianWithFrameOutput =
            positionCartesian.toString("INERTIAL");
        final String cartesianWithFramePathFile =
            loadResources("templateFile/object/unclassed/position/PositionCartesianWithReferenceFrameTemplate.txt");
        verifyFileOutput(cartesianWithFramePathFile, cartesianWithFrameOutput,
                         1e-8);
    }

    /**
     * Test for write() method with CARTOGRAPHIC_RADIANS.
     */
    @Test
    @DisplayName("Test write() method with CARTOGRAPHIC_RADIANS")
    public void testWriteCartographicRadians() {
        final Position positionRadians =
            new Position(1.0, 45.0, 20.0, PositionType.CARTOGRAPHIC_RADIANS,
                         header.getClock());

        // Test without reference frame
        final String radiansOutput = positionRadians.toString();
        final String radiansPathFile =
            loadResources("templateFile/object/unclassed/position/PositionRadiansTemplate.txt");
        verifyFileOutput(radiansPathFile, radiansOutput, 1e-8);

        // Test with reference frame
        final String radiansWithFrameOutput =
            positionRadians.toString("INERTIAL");
        final String radiansWithFramePathFile =
            loadResources("templateFile/object/unclassed/position/PositionRadiansWithReferenceFrameTemplate.txt");
        verifyFileOutput(radiansWithFramePathFile, radiansWithFrameOutput,
                         1e-8);
    }

    /**
     * Test for write() method with CARTOGRAPHIC_DEGREES.
     */
    @Test
    @DisplayName("Test write() method with CARTOGRAPHIC_DEGREES")
    public void testWriteCartographicDegrees() {
        final Position positionDegrees =
            new Position(1.0, 45.0, 20.0, PositionType.CARTOGRAPHIC_DEGREES,
                         header.getClock());

        // Test without reference frame
        final String degreesOutput = positionDegrees.toString();
        final String degreesPathFile =
            loadResources("templateFile/object/unclassed/position/PositionDegreesTemplate.txt");
        verifyFileOutput(degreesPathFile, degreesOutput, 1e-8);

        // Test with reference frame
        final String degreesWithFrameOutput =
            positionDegrees.toString("INERTIAL");
        final String degreesWithFramePathFile =
            loadResources("templateFile/object/unclassed/position/PositionDegreesWithReferenceFrameTemplate.txt");
        verifyFileOutput(degreesWithFramePathFile, degreesWithFrameOutput,
                         1e-8);
    }

    /**
     * Test for getLatitudeRad() method with CARTOGRAPHIC_RADIANS. Should return
     * the latitude value directly when position is in radians.
     */
    @Test
    @DisplayName("Test getLatitudeRad() with CARTOGRAPHIC_RADIANS")
    public void testGetLatitudeRadWithCartographicRadians() {
        // π/4 radians = 45 degrees
        final double expectedLatitudeRad = Math.PI / 4.0;
        final Position positionRadians =
            new Position(1.0, expectedLatitudeRad, 20.0,
                         PositionType.CARTOGRAPHIC_RADIANS, header.getClock());

        final double actualLatitudeRad = positionRadians.getLatitudeRad();
        Assertions.assertEquals(expectedLatitudeRad, actualLatitudeRad, 1e-15);
    }

    /**
     * Test for getLatitudeRad() method with CARTOGRAPHIC_DEGREES. Should
     * convert degrees to radians when position is in degrees.
     */
    @Test
    @DisplayName("Test getLatitudeRad() with CARTOGRAPHIC_DEGREES")
    public void testGetLatitudeRadWithCartographicDegrees() {
        // 45 degrees should be converted to π/4 radians
        final double latitudeDeg = 45.0;
        final double expectedLatitudeRad = latitudeDeg * (Math.PI / 180.0);
        final Position positionDegrees =
            new Position(1.0, latitudeDeg, 20.0,
                         PositionType.CARTOGRAPHIC_DEGREES, header.getClock());

        final double actualLatitudeRad = positionDegrees.getLatitudeRad();
        Assertions.assertEquals(expectedLatitudeRad, actualLatitudeRad, 1e-15);
    }

    /**
     * Test for getLatitudeRad() method with CARTESIAN_POSITION. Should throw an
     * exception when position is cartesian.
     */
    @Test
    @DisplayName("Test getLatitudeRad() with CARTESIAN_POSITION")
    public void testGetLatitudeRadWithCartesianPosition() {
        final Position positionCartesian =
            new Position(1.0, 2.0, 3.0, PositionType.CARTESIAN_POSITION,
                         header.getClock());

        Assertions.assertThrows(OresiumException.class,
                                positionCartesian::getLatitudeRad);
    }

    /**
     * Test for getLatitudeRad() method with zero values. Should throw an
     * exception when both latitude and latitudeDeg are zero.
     */
    @Test
    @DisplayName("Test getLatitudeRad() with zero values")
    public void testGetLatitudeRadWithZeroValues() {
        // Create a position with both latitude and latitudeDeg = 0
        final Position positionZero =
            new Position(1.0, 0.0, 20.0, PositionType.CARTOGRAPHIC_DEGREES,
                         header.getClock());

        try {
            // This should throw an exception
            positionZero.getLatitudeRad();
            Assertions.fail("Expected OresiumException was not thrown");
        } catch (OresiumException e) {
            // Expected exception
            Assertions.assertTrue(e.getMessage().contains("Latitude"));
        }
    }

    /**
     * Test for getLongitudeRad() method with CARTOGRAPHIC_RADIANS. Should
     * return the longitude value directly when position is in radians.
     */
    @Test
    @DisplayName("Test getLongitudeRad() with CARTOGRAPHIC_RADIANS")
    public void testGetLongitudeRadWithCartographicRadians() {
        // π/3 radians ≈ 60 degrees
        final double expectedLongitudeRad = Math.PI / 3.0;
        final Position positionRadians =
            new Position(expectedLongitudeRad, 1.0, 20.0,
                         PositionType.CARTOGRAPHIC_RADIANS, header.getClock());

        final double actualLongitudeRad = positionRadians.getLongitudeRad();
        Assertions.assertEquals(expectedLongitudeRad, actualLongitudeRad,
                                1e-15);
    }

    /**
     * Test for getLongitudeRad() method with CARTOGRAPHIC_DEGREES. Should
     * convert degrees to radians when position is in degrees.
     */
    @Test
    @DisplayName("Test getLongitudeRad() with CARTOGRAPHIC_DEGREES")
    public void testGetLongitudeRadWithCartographicDegrees() {
        // 60 degrees should be converted to π/3 radians
        final double longitudeDeg = 60.0;
        final double expectedLongitudeRad = longitudeDeg * (Math.PI / 180.0);
        final Position positionDegrees =
            new Position(longitudeDeg, 30.0, 20.0,
                         PositionType.CARTOGRAPHIC_DEGREES, header.getClock());

        final double actualLongitudeRad = positionDegrees.getLongitudeRad();
        Assertions.assertEquals(expectedLongitudeRad, actualLongitudeRad,
                                1e-15);
    }

    /**
     * Test for getLongitudeRad() method with CARTESIAN_POSITION. Should throw
     * an exception when position is cartesian.
     */
    @Test
    @DisplayName("Test getLongitudeRad() with CARTESIAN_POSITION")
    public void testGetLongitudeRadWithCartesianPosition() {
        final Position positionCartesian =
            new Position(1.0, 2.0, 3.0, PositionType.CARTESIAN_POSITION,
                         header.getClock());

        Assertions.assertThrows(OresiumException.class,
                                positionCartesian::getLongitudeRad);
    }

    /**
     * Test for getLongitudeRad() method with zero values. Should throw an
     * exception when both longitude and longitudeDeg are zero.
     */
    @Test
    @DisplayName("Test getLongitudeRad() with zero values")
    public void testGetLongitudeRadWithZeroValues() {
        // Create a position with both longitude and longitudeDeg = 0
        final Position positionZero =
            new Position(0.0, 30.0, 20.0, PositionType.CARTOGRAPHIC_DEGREES,
                         header.getClock());

        try {
            // This should throw an exception
            positionZero.getLongitudeRad();
            Assertions.fail("Expected OresiumException was not thrown");
        } catch (OresiumException e) {
            // Expected exception
            Assertions.assertTrue(e.getMessage().contains("Longitude"));
        }
    }

    /**
     * Test for getHeight() method with non-zero height. Should return the
     * height value when it's not zero.
     */
    @Test
    @DisplayName("Test getHeight() with non-zero height")
    public void testGetHeightWithNonZeroValue() {
        final double expectedHeight = 1000.0;
        // Create position with non-zero height
        final Position position =
            new Position(1.0, 2.0, expectedHeight,
                         PositionType.CARTOGRAPHIC_RADIANS, header.getClock());

        final double actualHeight = position.getHeight();
        Assertions.assertEquals(expectedHeight, actualHeight, 1e-15);
    }

    /**
     * Test for getHeight() method with zero height. Should throw an exception
     * when height is zero.
     */
    @Test
    @DisplayName("Test getHeight() with zero height")
    public void testGetHeightWithZeroValue() {
        // Create position with zero height
        final Position position =
            new Position(1.0, 2.0, 0.0, PositionType.CARTOGRAPHIC_RADIANS,
                         header.getClock());

        Assertions.assertThrows(OresiumException.class, position::getHeight);
    }

    /**
     * Test for getX() method with non-zero x. Should return the x value when
     * it's not zero.
     */
    @Test
    @DisplayName("Test getX() with non-zero x")
    public void testGetXWithNonZeroValue() {
        final double expectedX = 123.456;
        // Create position with non-zero x (cartesian position)
        final Position position =
            new Position(expectedX, 2.0, 3.0, PositionType.CARTESIAN_POSITION,
                         header.getClock());

        final double actualX = position.getX();
        Assertions.assertEquals(expectedX, actualX, 1e-15);
    }

    /**
     * Test for getX() method with zero x. Should throw an exception when x is
     * zero.
     */
    @Test
    @DisplayName("Test getX() with zero x")
    public void testGetXWithZeroValue() {
        // Create position with zero x
        final Position position =
            new Position(0.0, 2.0, 3.0, PositionType.CARTESIAN_POSITION,
                         header.getClock());

        Assertions.assertThrows(OresiumException.class, position::getX);
    }

    /**
     * Test for getX() method with non-zero x. Should return the x value when
     * it's not zero.
     */
    @Test
    @DisplayName("Test getY() with non-zero y")
    public void testGetYWithNonZeroValue() {
        final double expectedY = 123.456;
        // Create position with non-zero y (cartesian position)
        final Position position =
            new Position(expectedY, 123.456, 3.0,
                         PositionType.CARTESIAN_POSITION, header.getClock());

        final double actualY = position.getY();
        Assertions.assertEquals(expectedY, actualY, 1e-15);
    }

    /**
     * Test for getX() method with zero x. Should throw an exception when x is
     * zero.
     */
    @Test
    @DisplayName("Test getY() with zero y")
    public void testGetYWithZeroValue() {
        // Create position with zero y
        final Position position =
            new Position(2.0, 0.0, 3.0, PositionType.CARTESIAN_POSITION,
                         header.getClock());

        Assertions.assertThrows(OresiumException.class, position::getY);
    }

    /**
     * Test for getX() method with non-zero x. Should return the x value when
     * it's not zero.
     */
    @Test
    @DisplayName("Test getZ() with non-zero z")
    public void testGetZWithNonZeroValue() {
        final double expectedZ = 123.456;
        // Create position with non-zero z (cartesian position)
        final Position position =
            new Position(1.0, 2.0, 123.456, PositionType.CARTESIAN_POSITION,
                         header.getClock());

        final double actualZ = position.getZ();
        Assertions.assertEquals(expectedZ, actualZ, 1e-15);
    }

    /**
     * Test for getX() method with zero x. Should throw an exception when x is
     * zero.
     */
    @Test
    @DisplayName("Test getZ() with zero z")
    public void testGetZWithZeroValue() {
        // Create position with zero z
        final Position position =
            new Position(2.0, 3.0, 0.0, PositionType.CARTESIAN_POSITION,
                         header.getClock());

        Assertions.assertThrows(OresiumException.class, position::getZ);
    }
}
