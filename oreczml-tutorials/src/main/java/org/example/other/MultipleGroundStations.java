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
package org.example.other;

import org.example.TutorialUtils;
import org.hipparchus.util.FastMath;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.czml.object.primary.CzmlGroundStation;
import org.orekit.czml.object.primary.Header;
import org.orekit.czml.object.secondary.Clock;
import org.orekit.czml.file.CzmlFile;
import org.orekit.czml.file.CzmlFileBuilder;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.frames.TopocentricFrame;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScale;
import org.orekit.time.TimeScalesFactory;
import org.orekit.utils.Constants;
import org.orekit.utils.IERSConventions;

import java.util.ArrayList;
import java.util.List;

public class MultipleGroundStations {

    private MultipleGroundStations() {
        // empty
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
        final double       durationOfSimulation   = 5 * 3600; // in seconds;
        final double       stepBetweenEachInstant = 60.0; // in seconds
        final AbsoluteDate startDate              = new AbsoluteDate(2024, 3, 15, 0, 0, 0.0, UTC);
        final AbsoluteDate finalDate              = startDate.shiftedBy(durationOfSimulation);
        final Clock        clock                  = new Clock(startDate, finalDate, UTC, stepBetweenEachInstant);

        // Creation of the header.
        final Header header = new Header("Multiple Ground Stations", clock, pathToJSFolder);

        // Creation of the model of the earth.
        final IERSConventions IERS = IERSConventions.IERS_2010;
        final Frame           ITRF = FramesFactory.getITRF(IERS, true);
        final OneAxisEllipsoid earth = new OneAxisEllipsoid(Constants.WGS84_EARTH_EQUATORIAL_RADIUS,
                Constants.WGS84_EARTH_FLATTENING, ITRF);

        // Creation of a topocentric frame around Toulouse.
        final GeodeticPoint toulouseFrame = new GeodeticPoint(FastMath.toRadians(43.6047),
                FastMath.toRadians(1.4442), 10);
        final TopocentricFrame topocentricToulouse = new TopocentricFrame(earth, toulouseFrame, "Toulouse Frame");

        // Creation of another topocentric frame around Las Vegas.
        final GeodeticPoint lasVegasFrame = new GeodeticPoint(FastMath.toRadians(36.1716),
                FastMath.toRadians(-115.1391), 10);
        final TopocentricFrame topocentricLasVegas = new TopocentricFrame(earth, lasVegasFrame, "Las Vegas Frame");

        // Creation of all the ground stations
        final List<CzmlGroundStation> allGroundStation = new ArrayList<>();
        allGroundStation.add(new CzmlGroundStation(topocentricToulouse));
        allGroundStation.add(new CzmlGroundStation(topocentricLasVegas));

        // Creation of the file
        final CzmlFile file = CzmlFile.builder(output).withHeader(header)
                                                         .withCzmlGroundStation(allGroundStation)
                                                         .build();

        // Writing in the file
        file.write();
    }
}
