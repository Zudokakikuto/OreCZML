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
}
