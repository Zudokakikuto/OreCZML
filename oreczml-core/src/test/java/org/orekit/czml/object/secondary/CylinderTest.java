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
package org.orekit.czml.object.secondary;

import cesiumlanguagewriter.CesiumHeightReference;
import org.hipparchus.util.FastMath;
import org.junit.jupiter.api.Test;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.czml.file.AbstractTest;
import org.orekit.czml.object.Position;
import org.orekit.czml.object.PositionType;
import org.orekit.czml.object.Utils.DateUtils;
import org.orekit.czml.object.primary.entities.CzmlGroundStation;
import org.orekit.czml.object.primary.Header;
import org.orekit.czml.object.primary.entities.Spacecraft;
import org.orekit.frames.TopocentricFrame;
import org.orekit.propagation.BoundedPropagator;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScalesFactory;

import java.awt.Color;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * The type Cylinder test.
 */
public class CylinderTest
    extends
    AbstractTest {

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

        loadOrekitData();

        final Header header = dummyHeader();
        final AbsoluteDate startDate =
            DateUtils.toAbsoluteDate(header.getAvailability().getStart(),
                                     TimeScalesFactory.getUTC());
        final AbsoluteDate finalDate = startDate.shiftedBy(60.0);
        final GeodeticPoint toulouseFrame =
            new GeodeticPoint(FastMath.toRadians(43.6047),
                              FastMath.toRadians(1.4442), 10);
        final TopocentricFrame topocentricToulouse =
            new TopocentricFrame(getEarth(), toulouseFrame, "Toulouse Frame");

        final BoundedPropagator propagator =
            dummyPropagator(startDate, finalDate);
        final Spacecraft satellite = new Spacecraft(propagator, header);

        final Cylinder cylinder =
            new Cylinder(topocentricToulouse, satellite, 90.0, header);

        final Cylinder coverageCylinder =
            new Cylinder(10.0, 20.0, 1.0, Color.BLUE,
                         new Position(1, 45, 20,
                                      PositionType.CARTESIAN_POSITION, header),
                         CesiumHeightReference.CLAMP_TO_GROUND, header);

        final CzmlGroundStation groundStation =
            new CzmlGroundStation(topocentricToulouse, header);

        final Cylinder groundStationCylinder =
            new Cylinder(groundStation, 80.0, header);

        final Cylinder topocentricCylinder =
            new Cylinder(topocentricToulouse, 90.0, header);

        final String pathFile =
            loadResources("templateFile/secondary/CylinderTemplate.txt");
        final String coveragePathFile =
            loadResources("templateFile/secondary/CylinderCoverageTemplate.txt");
        final String groundStationPathFile =
            loadResources("templateFile/secondary/CylinderGroundStationTemplate.txt");
        final String topocentricPathFile =
            loadResources("templateFile/secondary/CylinderTopocentricTemplate.txt");

        verifyFileOutput(pathFile, cylinder.toString(), 1e-8);
        verifyFileOutput(coveragePathFile, coverageCylinder.toString(), 1e-8);
        verifyFileOutput(groundStationPathFile,
                         groundStationCylinder.toString(), 1e-8);
        verifyFileOutput(topocentricPathFile, topocentricCylinder.toString(),
                         1e-8);
    }
}
