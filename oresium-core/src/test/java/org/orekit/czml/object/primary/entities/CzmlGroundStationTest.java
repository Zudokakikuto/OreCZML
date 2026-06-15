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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.orekit.annotation.DefaultDataContext;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.czml.file.AbstractTest;
import org.orekit.czml.object.nonvisual.CzmlModel;
import org.orekit.czml.object.primary.Header;
import org.orekit.frames.TopocentricFrame;

/**
 * The type Czml ground station test.
 */
public class CzmlGroundStationTest
    extends
    AbstractTest {

    /** Initialise orekit data. */
    private final double data = initializeOrekitData();

    /** Header. */
    final Header header = dummyHeader();

    /** Toulouse station name. */
    final String toulouseName = "Toulouse Frame";

    /** Topocentric frame of Toulouse. */
    final TopocentricFrame topocentricToulouse =
        buildTopocentric(toulouseName, 43.6047, 1.4442, 10);

    // Models

    /** ISS Model. */
    final String modelISS = loadResources("Default3DModels/ISSModel.glb");

    /** Juno Model. */
    final String modelJuno =
        loadResources("Default3DModels/ground_Station.glb");

    /**
     * Czml ground station constructor test. *
     */
    @Test
    @DefaultDataContext
    void CzmlGroundStationConstructorTest() {

        final CzmlGroundStation station =
            new CzmlGroundStation(topocentricToulouse, header.getClock());

        final String pathFile =
            loadResources("templateFile/object/primary/entities/czmlgroundstation/CzmlGroundStationTemplate.txt");

        verifyFileOutput(pathFile, station.toString(), 1e-8);
    }

    @Test
    @DisplayName("Czml Ground Station with Builder constructor")
    public void CzmlGroundStationBuilderConstructorTest() {

        // Build of the topocentric frame
        final TopocentricFrame topocentricToulouse =
            buildTopocentric(toulouseName, 43.6047, 1.4442, 10);

        // Build of the ground station with the builder
        final CzmlGroundStation stationBuilder =
            CzmlGroundStation.builder(topocentricToulouse, header.getClock())
                .withModelPath(modelISS).build();

        // Reference file
        final String builderPathFile =
            loadResources("templateFile/object/primary/entities/czmlgroundstation/CzmlGroundStationWithBuilderTemplate.txt");
        verifyFileOutput(builderPathFile, stationBuilder.toString(), 1e-8);
    }

    @Test
    @DisplayName("Test of Czml Ground Station with a model")
    public void CzmlGroundStationWithModelConstructorTest() {

        // Build of the model
        final CzmlModel model =
            CzmlModel.builder(modelJuno, false, header.getClock()).build();

        // Build of the ground station with a model
        final CzmlGroundStation stationWithModel =
            CzmlGroundStation.builder(topocentricToulouse, header.getClock())
                .withModel(model).build();

        // Reference file
        final String builderWithModelPathFile =
            loadResources("templateFile/object/primary/entities/czmlgroundstation/CzmlGroundStationBuilderWithModelTemplate.txt");

        verifyFileOutput(builderWithModelPathFile, stationWithModel.toString(),
                         1e-8);
    }

    final TopocentricFrame
        buildTopocentric(final String stationName, final double latitude,
                         final double longitude, final double altitude) {
        final GeodeticPoint frame =
            new GeodeticPoint(FastMath.toRadians(latitude),
                              FastMath.toRadians(longitude), altitude);
        return new TopocentricFrame(getEarth(), frame, stationName);
    }
}
