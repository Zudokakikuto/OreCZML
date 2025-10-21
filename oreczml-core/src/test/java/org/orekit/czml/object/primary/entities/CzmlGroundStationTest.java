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
package org.orekit.czml.object.primary.entities;

import org.hipparchus.util.FastMath;
import org.junit.jupiter.api.Test;
import org.orekit.annotation.DefaultDataContext;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.czml.file.AbstractTest;
import org.orekit.czml.object.primary.Header;
import org.orekit.frames.TopocentricFrame;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * The type Czml ground station test.
 */
public class CzmlGroundStationTest
    extends
    AbstractTest {

    /**
     * Czml ground station constructor test.
     *
     * @throws URISyntaxException the uri syntax exception
     * @throws IOException the io exception
     */
    @Test
    @DefaultDataContext
    void CzmlGroundStationConstructorTest()
        throws URISyntaxException,
            IOException {

        loadOrekitData();

        final Header header = dummyHeader();

        final String frameName = "Toulouse Frame";
        final GeodeticPoint toulouseFrame =
            new GeodeticPoint(FastMath.toRadians(43.6047),
                              FastMath.toRadians(1.4442), 10);
        final TopocentricFrame topocentricToulouse =
            new TopocentricFrame(getEarth(), toulouseFrame, frameName);

        final GeodeticPoint randomPoint =
            new GeodeticPoint(FastMath.toRadians(1.6047),
                              FastMath.toRadians(10.4442), 10);
        final TopocentricFrame topocentricRandom =
            new TopocentricFrame(getEarth(), randomPoint, frameName);

        final List<TopocentricFrame> topocentrics = new ArrayList<>();
        topocentrics.add(topocentricToulouse);
        topocentrics.add(topocentricRandom);

        final String modelISS = loadResources("Default3DModels/ISSModel.glb");
        final String modelJuno =
            loadResources("Default3DModels/ground_Station.glb");

        final List<String> strings = new ArrayList<>();
        strings.add(modelISS);
        strings.add(modelJuno);

        final CzmlGroundStation station =
            new CzmlGroundStation(topocentricToulouse, header.getClock());

        final CzmlGroundStation stationBuilder =
            CzmlGroundStation.builder(topocentricToulouse, header.getClock())
                .withModel(modelISS).build();

        final String pathFile =
            loadResources("templateFile/object/primary/entities/CzmlGroundStationTemplate.txt");
        final String builderPathFile =
            loadResources("templateFile/object/primary/entities/CzmlGroundStationWithBuilderTemplate.txt");

        verifyFileOutput(pathFile, station.toString(), 1e-8);
        verifyFileOutput(builderPathFile, stationBuilder.toString(), 1e-8);
    }
}
