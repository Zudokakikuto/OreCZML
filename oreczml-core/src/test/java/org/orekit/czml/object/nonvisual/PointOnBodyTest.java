/* Copyright 2002-2024 CS GROUP
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
import org.hipparchus.util.FastMath;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.czml.file.AbstractTest;
import org.orekit.czml.object.Utils.DateUtils;
import org.orekit.czml.object.primary.Header;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The type Point on body test.
 */
public class PointOnBodyTest extends AbstractTest {

    /**
     * Point on body constructor test.
     *
     * @throws IOException the io exception
     */
    @Test
    void PointOnBodyConstructorTest() throws IOException, URISyntaxException {

        loadOrekitData();

        final Header header = dummyHeader();

        final TimeInterval availability1 = header.getAvailability();
        final TimeInterval availability2 = DateUtils.shitfedBy(availability1, 10.0);
        final TimeInterval availability3 = DateUtils.shitfedBy(availability2, 10.0);
        final TimeInterval availability4 = DateUtils.shitfedBy(availability3, 10.0);

        final List<TimeInterval> availabilities = new ArrayList<>(Arrays.asList(availability1, availability2, availability3, availability4));

        final List<JulianDate> julianDates = DateUtils.toJulianDates(availabilities);

        final List<GeodeticPoint> points = getPoints();

        final PointOnBody pointOnBodyTest = new PointOnBody(julianDates, points, getEarth(), header);

        pointOnBodyTest.setDisplayPath(true);
        pointOnBodyTest.setDisplayPeriodPointingPath(true, 20.0);

        final String pathFile = loadResources("templateFile/nonvisual/PointOnBodyTemplate.txt");
        verifyFileOutput(pathFile, pointOnBodyTest.toString(), 1e-8);

        // Getters coverage

        Assertions.assertEquals(julianDates, pointOnBodyTest.getJulianDates());
        Assertions.assertTrue(pointOnBodyTest.isDisplayPath());
        Assertions.assertTrue(pointOnBodyTest.isDisplayPeriodPointingPath());
    }

    private static List<GeodeticPoint> getPoints() {
        final GeodeticPoint point1 = new GeodeticPoint(FastMath.toRadians(43),
                FastMath.toRadians(1.44), 10);
        final GeodeticPoint point2 = new GeodeticPoint(FastMath.toRadians(44),
                FastMath.toRadians(1.45), 10);
        final GeodeticPoint point3 = new GeodeticPoint(FastMath.toRadians(45),
                FastMath.toRadians(1.46), 10);
        final GeodeticPoint point4 = new GeodeticPoint(FastMath.toRadians(46),
                FastMath.toRadians(1.47), 10);
        final GeodeticPoint point5 = new GeodeticPoint(FastMath.toRadians(47),
                FastMath.toRadians(1.48), 10);
        return new ArrayList<>(Arrays.asList(point1, point2, point3, point4, point5));
    }
}
