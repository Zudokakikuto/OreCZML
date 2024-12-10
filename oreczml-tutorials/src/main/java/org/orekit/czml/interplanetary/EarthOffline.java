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

package org.orekit.czml.interplanetary;

import org.orekit.czml.TutorialUtils;
import org.orekit.czml.archi.factory.BodyFactory;
import org.orekit.czml.file.CzmlFile;
import org.orekit.czml.object.primary.Body;
import org.orekit.czml.object.primary.Header;
import org.orekit.czml.object.secondary.Clock;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScalesFactory;

/**
 * This tutorial shows how the earth can be implemented
 * if the simulation is offline can the earth needs to be loaded anyway.
 */
public class EarthOffline {

    private EarthOffline() {
    }

    /**
     * Main of the Earth offline tutorial.
     *
     * @param args arguments of the main function
     * @throws Exception exception to throw
     */
    public static void main(final String[] args) throws Exception {
        // Load orekit data
        TutorialUtils.loadOrekitData();

        // Paths
        final String output = TutorialUtils.generateOutput();
        // !!! Here you need to change the path inside 'generateJsPath' to the path you are using for images or Model.
        // This folder can also be the public folder of your cesium javascript interface.
        final String pathToJSFolder = TutorialUtils.generateJSPath(
                System.getProperty("user.dir") + "/Javascript/public");

        // Creation of the clock.

        final double       durationOfSimulation = 24 * 3600; // in seconds;
        final AbsoluteDate startDate            = new AbsoluteDate(2024, 3, 15, 0, 0, 0.0, TimeScalesFactory.getUTC());
        final AbsoluteDate finalDate            = startDate.shiftedBy(durationOfSimulation);
        final Clock clock = new Clock(startDate, finalDate, TimeScalesFactory.getUTC(),
                TutorialUtils.STEP_BETWEEN_EACH_INSTANT);

        final Header header = new Header("Earth Display when the interface is offline", clock, pathToJSFolder);

        // Careful, you can't zoom in of this model of the earth, but you can zoom out. To be able to zoom in,
        // please use a token from CesiumIon, available here : https://ion.cesium.com/tokens?page=1
        final Body earth = BodyFactory.getEarth(header);

        final CzmlFile file = CzmlFile.builder()
                                      .withHeader(header)
                                      .withBody(earth)
                                      .build();

        file.write(output);
    }
}
