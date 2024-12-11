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

import cesiumlanguagewriter.CesiumHeightReference;
import org.hipparchus.util.FastMath;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.czml.file.AbstractTest;
import org.orekit.czml.object.Position;
import org.orekit.czml.object.PositionType;
import org.orekit.czml.object.Utils.DateUtils;
import org.orekit.czml.object.primary.visu.VisibilityCone;
import org.orekit.czml.object.secondary.Cylinder;
import org.orekit.frames.TopocentricFrame;
import org.orekit.propagation.BoundedPropagator;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScalesFactory;

import java.awt.Color;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * The type Visibility cone test.
 */
public class VisibilityConeTest extends AbstractTest {

    /**
     * Visibility cone constructor test.
     *
     * @throws IOException        the io exception
     * @throws URISyntaxException the uri syntax exception
     */
    @Test
    void VisibilityConeConstructorTest() throws IOException, URISyntaxException {

        loadOrekitData();

        final Header header = dummyHeader();
        final AbsoluteDate startDate = DateUtils.toAbsoluteDate(header.getAvailability()
                                                                      .getStart(), TimeScalesFactory.getUTC());
        final AbsoluteDate finalDate = startDate.shiftedBy(60.0);

        final GeodeticPoint toulouseFrame = new GeodeticPoint(FastMath.toRadians(43.6047),
                FastMath.toRadians(1.4442), 10);
        final TopocentricFrame topocentricToulouse = new TopocentricFrame(getEarth(), toulouseFrame,
                "Toulouse Frame");

        final BoundedPropagator propagator = dummyPropagator(startDate, finalDate);
        final Satellite         satellite  = new Satellite(propagator, header);

        final Cylinder coverageCylinder = new Cylinder(10, 20, 10, Color.RED,
                new Position(1, 2, 1, PositionType.CARTESIAN_POSITION, header), CesiumHeightReference.CLAMP_TO_GROUND,
                header);

        final CzmlGroundStation groundStation = new CzmlGroundStation(topocentricToulouse, header);

        final VisibilityCone cone = new VisibilityCone(topocentricToulouse, satellite, header);

        final VisibilityCone coverageCone = new VisibilityCone("An id", "a name", coverageCylinder, header);

        final VisibilityCone cylinderSatCone = new VisibilityCone("An id", "A name", coverageCylinder, satellite,
                header);

        final VisibilityCone groundStationCone = new VisibilityCone(groundStation, header);

        final VisibilityCone groundStationSatCone = new VisibilityCone(groundStation, satellite, header);

        final String pathFile                 = loadResources("templateFile/primary/VisibilityConeTemplate.txt");
        final String coveragePathFile         = loadResources(
                "templateFile/primary/VisibilityConeCoverageTemplate.txt");
        final String cylinderSatPathFile      = loadResources(
                "templateFile/primary/VisibilityConeCylinderSatTemplate.txt");
        final String groundStationPathFile    = loadResources(
                "templateFile/primary/VisibilityConeGroundStationTemplate.txt");
        final String GroundStationSatPathFile = loadResources(
                "templateFile/primary/VisibilityConeGroundStationSatTemplate.txt");

        Assertions.assertEquals(Files.readString(Path.of(pathFile)), cone.toString());
        Assertions.assertEquals(Files.readString(Path.of(coveragePathFile)), coverageCone.toString());
        Assertions.assertEquals(Files.readString(Path.of(cylinderSatPathFile)), cylinderSatCone.toString());
        Assertions.assertEquals(Files.readString(Path.of(groundStationPathFile)), groundStationCone.toString());
        Assertions.assertEquals(Files.readString(Path.of(GroundStationSatPathFile)), groundStationSatCone.toString());
    }
}


