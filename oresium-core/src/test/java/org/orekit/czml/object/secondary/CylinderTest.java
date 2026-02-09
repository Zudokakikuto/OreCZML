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

import cesiumlanguagewriter.CesiumHeightReference;
import org.hipparchus.util.FastMath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.czml.file.AbstractTest;
import org.orekit.czml.object.Position;
import org.orekit.czml.object.PositionType;
import org.orekit.czml.object.primary.Header;
import org.orekit.czml.object.primary.entities.CzmlGroundStation;
import org.orekit.czml.object.primary.entities.Spacecraft;
import org.orekit.czml.object.utils.DateUtils;
import org.orekit.frames.TopocentricFrame;
import org.orekit.propagation.BoundedPropagator;
import org.orekit.time.AbsoluteDate;

import java.awt.Color;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * The type Cylinder test.
 */
public class CylinderTest
    extends
    AbstractTest {

    /** Initialise orekit data. */
    private final double data = initializeOrekitData();

    /** Header. */
    final Header header = dummyHeader();

    // Dates

    /** First Date. */
    final AbsoluteDate startDate =
        DateUtils.toAbsoluteDate(header.getAvailability().getStart());

    /** Final date. */
    final AbsoluteDate finalDate = startDate.shiftedBy(60.0);

    // Topocentric

    /** Toulouse point. */
    final GeodeticPoint toulouseFrame =
        new GeodeticPoint(FastMath.toRadians(43.6047),
                          FastMath.toRadians(1.4442), 10);

    /** Toulouse frame. */
    final TopocentricFrame topocentricToulouse =
        new TopocentricFrame(getEarth(), toulouseFrame, "Toulouse Frame");

    /**
     * Cylinder constructor test.
     *
     * @throws IOException the io exception
     * @throws URISyntaxException the uri syntax exception
     */
    @Test
    void CylinderConstructorTest()
        throws IOException,
            URISyntaxException {

        final BoundedPropagator propagator =
            dummyPropagator(startDate, finalDate, dummyOrbit(startDate));
        final Spacecraft spacecraft =
            new Spacecraft(propagator, header.getClock());

        final Cylinder cylinder =
            new Cylinder(topocentricToulouse, spacecraft, 90.0,
                         header.getClock());

        final String pathFile =
            loadResources("templateFile/object/secondary/cylinder/CylinderTemplate.txt");

        verifyFileOutput(pathFile, cylinder.toString(), 1e-8);
    }

    @Test
    @DisplayName("Cylinder coverage constructor test")
    public void CylinderCoverageConstructorTest()
        throws URISyntaxException,
            IOException {
        final Cylinder coverageCylinder =
            new Cylinder(10.0, 20.0, 1.0, Color.BLUE,
                         new Position(1, 45, 20,
                                      PositionType.CARTESIAN_POSITION,
                                      header.getClock()),
                         CesiumHeightReference.CLAMP_TO_GROUND,
                         header.getClock());

        // Reference file
        final String coveragePathFile =
            loadResources("templateFile/object/secondary/cylinder/CylinderCoverageTemplate.txt");

        verifyFileOutput(coveragePathFile, coverageCylinder.toString(), 1e-8);
    }

    @Test
    @DisplayName("Cylinder from ground station constructor test")
    public void CylinderGroundStationConstructorTest()
        throws URISyntaxException,
            IOException {

        final CzmlGroundStation groundStation =
            new CzmlGroundStation(topocentricToulouse, header.getClock());

        final Cylinder groundStationCylinder =
            new Cylinder(groundStation, 80.0, header.getClock());

        // Reference file
        final String groundStationPathFile =
            loadResources("templateFile/object/secondary/cylinder/CylinderGroundStationTemplate.txt");

        verifyFileOutput(groundStationPathFile,
                         groundStationCylinder.toString(), 1e-8);
    }

    @Test
    @DisplayName("Cylinder from topocentric frame constructor test")
    public void CylinderTopocentricConstructorTest()
        throws URISyntaxException,
            IOException {

        final Cylinder topocentricCylinder =
            new Cylinder(topocentricToulouse, 90.0, header.getClock());

        // Reference file
        final String topocentricPathFile =
            loadResources("templateFile/object/secondary/cylinder/CylinderTopocentricTemplate.txt");

        verifyFileOutput(topocentricPathFile, topocentricCylinder.toString(),
                         1e-8);
    }
}
