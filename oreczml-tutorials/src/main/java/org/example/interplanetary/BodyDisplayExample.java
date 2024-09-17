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
package org.example.interplanetary;

import org.example.TutorialUtils;
import org.orekit.czml.archi.factory.BodyDisplayFactory;
import org.orekit.czml.object.primary.BodyDisplay;
import org.orekit.czml.object.primary.Header;
import org.orekit.czml.object.secondary.Clock;
import org.orekit.czml.file.CzmlFile;
import org.orekit.czml.file.CzmlFileBuilder;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScale;
import org.orekit.time.TimeScalesFactory;

import java.util.ArrayList;
import java.util.List;

public class BodyDisplayExample {

    private BodyDisplayExample() {
    }

    public static void main(final String[] args) throws Exception {
        // Load orekit data
        TutorialUtils.loadOrekitData();

        // Paths
        final String root = System.getProperty("user.dir")
                                  .replace("\\", "/");
        final String outputPath = root + "/Output";
        final String outputName = "Output.czml";
        final String output     = outputPath + "/" + outputName;
        // Change the path here to your JavaScript>public folder.
        final String pathToJSFolder = root + "/Javascript/public/";

        // Creation of the clock.
        final TimeScale    UTC                    = TimeScalesFactory.getUTC();
        final double       durationOfSimulation   = 24 * 3600; // in seconds;
        final double       stepBetweenEachInstant = 60.0; // in seconds
        final AbsoluteDate startDate              = new AbsoluteDate(2024, 3, 15, 0, 0, 0.0, UTC);
        final AbsoluteDate finalDate              = startDate.shiftedBy(durationOfSimulation);
        final Clock        clock                  = new Clock(startDate, finalDate, UTC, stepBetweenEachInstant);

        final Header header = new Header("Setup of the solar system in the simulation", clock, pathToJSFolder);

        // Solar system
        final List<BodyDisplay> solarSystem    = new ArrayList<>();
        final BodyDisplay       sunDisplay     = BodyDisplayFactory.getSun();
        final BodyDisplay       moonDisplay    = BodyDisplayFactory.getMoon();
        final BodyDisplay       mercuryDisplay = BodyDisplayFactory.getMercury();
        final BodyDisplay       venusDisplay   = BodyDisplayFactory.getVenus();
        final BodyDisplay       marsDisplay    = BodyDisplayFactory.getMars();
        final BodyDisplay       jupiterDisplay = BodyDisplayFactory.getJupiter();
        final BodyDisplay       saturnDisplay  = BodyDisplayFactory.getSaturn();
        final BodyDisplay       uranusDisplay  = BodyDisplayFactory.getUranus();
        final BodyDisplay       neptuneDisplay = BodyDisplayFactory.getNeptune();
        final BodyDisplay       plutoDisplay   = BodyDisplayFactory.getPluto();
        solarSystem.add(sunDisplay);
        solarSystem.add(moonDisplay);
        solarSystem.add(mercuryDisplay);
        solarSystem.add(venusDisplay);
        solarSystem.add(marsDisplay);
        solarSystem.add(jupiterDisplay);
        solarSystem.add(saturnDisplay);
        solarSystem.add(uranusDisplay);
        solarSystem.add(neptuneDisplay);
        solarSystem.add(plutoDisplay);

        final CzmlFile file = new CzmlFileBuilder(output).withHeader(header)
                                                         .withBodyDisplay(solarSystem)
                                                         .build();
        // Writing in the file
        file.write();
    }
}
