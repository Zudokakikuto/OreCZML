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
package org.orekit.czml.object.nonvisual;

import cesiumlanguagewriter.JulianDate;
import cesiumlanguagewriter.TimeInterval;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.hipparchus.util.FastMath;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.orekit.annotation.DefaultDataContext;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.czml.errors.OresiumException;
import org.orekit.czml.errors.OresiumMessages;
import org.orekit.czml.file.AbstractTest;
import org.orekit.czml.object.primary.Header;
import org.orekit.czml.object.utils.DateUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The type Point on body test.
 */
@DefaultDataContext
public class PointOnBodyTest
    extends
    AbstractTest {

    /** Initialise orekit data. */
    private final double data = initializeOrekitData();

    private final Header header = dummyHeader();

    final TimeInterval availability1 = header.getAvailability();

    final TimeInterval availability2 = DateUtils.shitfedBy(availability1, 10.0);

    final TimeInterval availability3 = DateUtils.shitfedBy(availability2, 10.0);

    final TimeInterval availability4 = DateUtils.shitfedBy(availability3, 10.0);

    final List<TimeInterval> availabilities =
        new ArrayList<>(Arrays.asList(availability1, availability2,
                                      availability3, availability4));

    final List<JulianDate> julianDates =
        DateUtils.toJulianDateList(availabilities);

    final List<GeodeticPoint> points = getPoints();

    final PointOnBody pointOnBodyCoverage =
        new PointOnBody(julianDates, points, getEarth());

    /**
     * Point on body constructor test.
     */
    @Test
    void PointOnBodyConstructorTest() {

        final PointOnBody pointOnBodyTest =
            new PointOnBody(julianDates, points, getEarth());

        pointOnBodyTest.setDisplayPath(true);
        pointOnBodyTest.setDisplayPeriodPointingPath(true, 20.0);

        final String pathFile =
            loadResources("templateFile/nonvisual/pointonbody/PointOnBodyTemplate.txt");
        verifyFileOutput(pathFile, pointOnBodyTest.toString(), 1e-8);

        // Getters coverage

        Assertions.assertEquals(julianDates, pointOnBodyTest.getJulianDates());
        Assertions.assertTrue(pointOnBodyTest.isDisplayPath());
        Assertions.assertTrue(pointOnBodyTest.isDisplayPeriodPointingPath());
    }

    @Test
    @DisplayName("Test cloning point on body")
    void CloningPointOnBodyTest() {
        final PointOnBody pointOnBodyClonedFilled =
            pointOnBodyCoverage.cloneObject();

        final String pathFile =
            loadResources("templateFile/nonvisual/pointonbody/PointOnBodyFilledTemplate.txt");

        verifyFileOutput(pathFile, pointOnBodyClonedFilled.toString(), 1e-8);
    }

    @Test
    @DisplayName("cloneObject with empty lists throws OresiumException")
    void cloneObject_WithEmptyLists_ThrowsOresiumException() {
        // Given
        final PointOnBody pointEmpty =
            new PointOnBody(new ArrayList<>(), new ArrayList<>(), getEarth());

        // When & Then
        final OresiumException exception =
            Assertions.assertThrows(OresiumException.class,
                                    () -> pointEmpty.cloneObject());
        Assertions
            .assertEquals(OresiumMessages.NOT_VALID_SECONDARY_OBJECT_FOR_CLONE,
                          exception.getSpecifier());
    }

    @Nested
    class GetterSetterTests {

        @Test
        void BodyTest() {
            Assertions
                .assertEquals(pointOnBodyCoverage.getBody().getBodyFrame(),
                              getEarth().getBodyFrame());
        }

        @Test
        void PeriodForPathTest() {
            Assertions.assertEquals(0.0,
                                    pointOnBodyCoverage.getPeriodForPath());
            pointOnBodyCoverage.setPeriodForPath(60.0);
            Assertions.assertEquals(60.0,
                                    pointOnBodyCoverage.getPeriodForPath());
            pointOnBodyCoverage.setPeriodForPath(0.0);
        }

        @Test
        void PositionListTest() {
            final List<Vector3D> vectors =
                Arrays
                    .asList(new Vector3D(4670485.168196356, 117406.8163742912,
                                         4327508.579180574),
                            new Vector3D(4594012.0201616045, 116286.7483182724,
                                         4408098.559028037),
                            new Vector3D(4516131.340298485, 115104.1018633768,
                                         4487355.479933731),
                            new Vector3D(4436866.372943926, 113858.7357344475,
                                         4565254.734230921),
                            new Vector3D(4356240.809131441, 112550.5364193539,
                                         4641772.102357388));
            Assertions
                .assertEquals(pointOnBodyCoverage.getPositionsList().toString(),
                              vectors.toString());
        }
    }

    private static List<GeodeticPoint> getPoints() {
        final GeodeticPoint point1 =
            new GeodeticPoint(FastMath.toRadians(43), FastMath.toRadians(1.44),
                              10);
        final GeodeticPoint point2 =
            new GeodeticPoint(FastMath.toRadians(44), FastMath.toRadians(1.45),
                              10);
        final GeodeticPoint point3 =
            new GeodeticPoint(FastMath.toRadians(45), FastMath.toRadians(1.46),
                              10);
        final GeodeticPoint point4 =
            new GeodeticPoint(FastMath.toRadians(46), FastMath.toRadians(1.47),
                              10);
        final GeodeticPoint point5 =
            new GeodeticPoint(FastMath.toRadians(47), FastMath.toRadians(1.48),
                              10);
        return new ArrayList<>(Arrays.asList(point1, point2, point3, point4,
                                             point5));
    }
}
