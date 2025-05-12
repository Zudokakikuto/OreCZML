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
package org.orekit.czml.object.primary;

import java.awt.Color;
import java.io.IOException;
import java.net.URISyntaxException;

import org.hipparchus.util.FastMath;
import org.junit.jupiter.api.Test;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.czml.file.AbstractTest;
import org.orekit.czml.object.Position;
import org.orekit.czml.object.PositionType;
import org.orekit.czml.object.Utils.DateUtils;
import org.orekit.czml.object.primary.entities.CzmlGroundStation;
import org.orekit.czml.object.primary.entities.Spacecraft;
import org.orekit.czml.object.primary.visu.VisibilityCone;
import org.orekit.czml.object.secondary.Cylinder;
import org.orekit.frames.TopocentricFrame;
import org.orekit.propagation.BoundedPropagator;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScalesFactory;

import cesiumlanguagewriter.CesiumHeightReference;

/**
 * The type Visibility cone test.
 */
public class VisibilityConeTest
    extends
    AbstractTest {

    /**
     * Visibility cone constructor test.
     *
     * @throws IOException the io exception
     * @throws URISyntaxException the uri syntax exception
     */
    @Test
    void VisibilityConeConstructorTest()
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

        final Cylinder coverageCylinder =
            new Cylinder(10, 20, 10, Color.RED,
                         new Position(1, 2, 1, PositionType.CARTESIAN_POSITION,
                                      header),
                         CesiumHeightReference.CLAMP_TO_GROUND, header);

        final CzmlGroundStation groundStation =
            new CzmlGroundStation(topocentricToulouse, header);

        final VisibilityCone cone =
            new VisibilityCone(topocentricToulouse, satellite, header);

        final VisibilityCone coverageCone =
            new VisibilityCone("id 1", "name 1", coverageCylinder, header);

        final VisibilityCone cylinderSatCone =
            new VisibilityCone("id 2", "name 2", coverageCylinder, satellite,
                               header);

        final VisibilityCone groundStationCone =
            new VisibilityCone(groundStation, header);

        final VisibilityCone groundStationSatCone =
            new VisibilityCone(groundStation, satellite, header);

        final String pathFile =
            loadResources("templateFile/primary/VisibilityConeTemplate.txt");
        final String coveragePathFile =
            loadResources("templateFile/primary/VisibilityConeCoverageTemplate.txt");
        final String cylinderSatPathFile =
            loadResources("templateFile/primary/VisibilityConeCylinderSatTemplate.txt");
        final String groundStationPathFile =
            loadResources("templateFile/primary/VisibilityConeGroundStationTemplate.txt");
        final String GroundStationSatPathFile =
            loadResources("templateFile/primary/VisibilityConeGroundStationSatTemplate.txt");

        verifyFileOutput(pathFile, cone.toString(), 1e-8);
        verifyFileOutput(coveragePathFile, coverageCone.toString(), 1e-8);
        verifyFileOutput(cylinderSatPathFile, cylinderSatCone.toString(), 1e-8);
        verifyFileOutput(groundStationPathFile, groundStationCone.toString(),
                         1e-8);
        verifyFileOutput(GroundStationSatPathFile,
                         groundStationSatCone.toString(), 1e-8);
    }
}
