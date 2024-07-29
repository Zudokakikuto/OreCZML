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
package InterVisu;

import org.hipparchus.util.FastMath;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.Header;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.InterSatVisu;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.Satellite;
import org.orekit.czml.CzmlObjects.CzmlSecondaryObjects.Clock;
import org.orekit.czml.Outputs.CzmlFile;
import org.orekit.data.DataContext;
import org.orekit.data.DataProvider;
import org.orekit.data.DirectoryCrawler;
import org.orekit.errors.OrekitException;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.orbits.KeplerianOrbit;
import org.orekit.orbits.PositionAngleType;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScale;
import org.orekit.time.TimeScalesFactory;
import org.orekit.utils.Constants;

import java.awt.Color;
import java.io.File;

public class InterSatExample {

    private InterSatExample() {
        // empty
    }

    public static void main(final String[] args) throws Exception {
        try {
            final File home = new File(System.getProperty("user.home"));
            final File orekitDir = new File(home, "orekit-data");
            final DataProvider provider = new DirectoryCrawler(orekitDir);
            DataContext.getDefault().getDataProvidersManager().addProvider(provider);
        } catch (OrekitException oe) {
            System.err.println(oe.getLocalizedMessage());
        }

        // Paths
        final String root = System.getProperty("user.dir").replace("\\", "/");
        final String outputPath = root + "/Output";
        final String outputName = "Output.czml";
        final String output = outputPath + "/" + outputName;

        // File created
        final CzmlFile file = new CzmlFile(output);

        // Creation of the clock.
        final TimeScale UTC = TimeScalesFactory.getUTC();
        final double durationOfSimulation = 32 * 3600; // in seconds;
        final double stepBetweenEachInstant = 60.0; // in seconds
        final AbsoluteDate startDate = new AbsoluteDate(2024, 3, 15, 0, 0, 0.0, UTC);
        final AbsoluteDate finalDate = startDate.shiftedBy(durationOfSimulation);
        final Clock clock = new Clock(startDate, finalDate, UTC, stepBetweenEachInstant);

        final Header header = new Header("Setup of the visualisation inter-satellites", clock);
        file.addObject(header);

        //// Build of the first satellite with an orbit with 20 degree inclination
        // Build of a LEO orbit
        final Frame EME2000 = FramesFactory.getEME2000();
        final KeplerianOrbit firstOrbit = new KeplerianOrbit(7878000, 0, FastMath.toRadians(0), FastMath.toRadians(0), FastMath.toRadians(0), FastMath.toRadians(0), PositionAngleType.MEAN, EME2000, startDate, Constants.WGS84_EARTH_MU);

        //// Build of the first satellite with an orbit with 40 degree inclination and omega of 90 degrees
        // Build of a LEO orbit
        final KeplerianOrbit secondOrbit = new KeplerianOrbit(7078000, 0, FastMath.toRadians(20), FastMath.toRadians(90), FastMath.toRadians(0), FastMath.toRadians(0), PositionAngleType.MEAN, EME2000, startDate, Constants.WGS84_EARTH_MU);

        // Creation of the satellites
        final Satellite firstSatellite = new Satellite(firstOrbit, Color.MAGENTA);
        final Satellite secondSatellite = new Satellite(secondOrbit, Color.GREEN);
        firstSatellite.displayOnlyOnePeriod();
        secondSatellite.displayOnlyOnePeriod();
        // Adding satellites to the file
        file.addObject(firstSatellite);
        file.addObject(secondSatellite);

        // Creation of the inter-sat visualisation
        final InterSatVisu interSatVisu = new InterSatVisu(firstSatellite, secondSatellite, true);
        file.addObject(interSatVisu);

        // Writing in the file
        file.write();
    }
}
