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
package org.orekit.czml.object.primary.visu;

import cesiumlanguagewriter.CesiumHeightReference;
import org.hipparchus.util.FastMath;
import org.junit.jupiter.api.Test;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.czml.file.AbstractTest;
import org.orekit.czml.object.Position;
import org.orekit.czml.object.PositionType;
import org.orekit.czml.object.primary.Header;
import org.orekit.czml.object.utils.DateUtils;
import org.orekit.czml.object.primary.entities.CzmlGroundStation;
import org.orekit.czml.object.primary.entities.Spacecraft;
import org.orekit.czml.object.secondary.Cylinder;
import org.orekit.frames.TopocentricFrame;
import org.orekit.propagation.BoundedPropagator;
import org.orekit.time.AbsoluteDate;

import java.awt.Color;
import java.io.IOException;
import java.net.URISyntaxException;

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
            DateUtils.toAbsoluteDate(header.getAvailability().getStart());
        final AbsoluteDate finalDate = startDate.shiftedBy(60.0);

        final GeodeticPoint toulouseFrame =
            new GeodeticPoint(FastMath.toRadians(43.6047),
                              FastMath.toRadians(1.4442), 10);
        final TopocentricFrame topocentricToulouse =
            new TopocentricFrame(getEarth(), toulouseFrame, "Toulouse Frame");

        final BoundedPropagator propagator =
            dummyPropagator(startDate, finalDate);
        final Spacecraft satellite =
            new Spacecraft(propagator, header.getClock());

        final Cylinder coverageCylinder =
            new Cylinder(10, 20, 10, Color.RED,
                         new Position(1, 2, 1, PositionType.CARTESIAN_POSITION,
                                      header.getAvailability()),
                         CesiumHeightReference.CLAMP_TO_GROUND,
                         header.getAvailability());

        final CzmlGroundStation groundStation =
            new CzmlGroundStation(topocentricToulouse,
                                  header.getAvailability());

        final VisibilityCone cone =
            new VisibilityCone(topocentricToulouse, satellite,
                               header.getAvailability());

        final VisibilityCone coverageCone =
            new VisibilityCone("An id", "a name", coverageCylinder,
                               header.getAvailability());

        final VisibilityCone cylinderSatCone =
            new VisibilityCone("An id", "A name", coverageCylinder, satellite,
                               header.getAvailability());

        final VisibilityCone groundStationCone =
            new VisibilityCone(groundStation, header.getAvailability());

        final VisibilityCone groundStationSatCone =
            new VisibilityCone(groundStation, satellite,
                               header.getAvailability());

        final String pathFile =
            loadResources("templateFile/object/primary/visu/VisibilityConeTemplate.txt");
        final String coveragePathFile =
            loadResources("templateFile/object/primary/visu/VisibilityConeCoverageTemplate.txt");
        final String cylinderSatPathFile =
            loadResources("templateFile/object/primary/visu/VisibilityConeCylinderSatTemplate.txt");
        final String groundStationPathFile =
            loadResources("templateFile/object/primary/visu/VisibilityConeGroundStationTemplate.txt");
        final String GroundStationSatPathFile =
            loadResources("templateFile/object/primary/visu/VisibilityConeGroundStationSatTemplate.txt");

        verifyFileOutput(pathFile, cone.toString(), 1e-8);
        verifyFileOutput(coveragePathFile, coverageCone.toString(), 1e-8);
        verifyFileOutput(cylinderSatPathFile, cylinderSatCone.toString(), 1e-8);
        verifyFileOutput(groundStationPathFile, groundStationCone.toString(),
                         1e-8);
        verifyFileOutput(GroundStationSatPathFile,
                         groundStationSatCone.toString(), 1e-8);
    }
}
