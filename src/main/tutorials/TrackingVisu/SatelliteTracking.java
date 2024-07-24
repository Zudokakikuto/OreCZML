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
package TrackingVisu;
import org.hipparchus.util.FastMath;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.CzmlGroundStation;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.Header;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.LineOfVisibility;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.Satellite;
import org.orekit.czml.CzmlObjects.CzmlSecondaryObjects.Clock;
import org.orekit.czml.Outputs.CzmlFile;
import org.orekit.data.DataContext;
import org.orekit.data.DataProvider;
import org.orekit.data.DirectoryCrawler;
import org.orekit.errors.OrekitException;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.frames.TopocentricFrame;
import org.orekit.orbits.KeplerianOrbit;
import org.orekit.orbits.PositionAngleType;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScale;
import org.orekit.time.TimeScalesFactory;
import org.orekit.utils.Constants;
import org.orekit.utils.IERSConventions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SatelliteTracking {

    private SatelliteTracking() {
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
        final double durationOfSimulation = 10 * 3600; // in seconds;
        final double stepBetweenEachInstant = 60.0; // in seconds
        final AbsoluteDate startDate = new AbsoluteDate(2024, 3, 15, 0, 0, 0.0, UTC);
        final AbsoluteDate finalDate = startDate.shiftedBy(durationOfSimulation);
        final Clock clock = new Clock(startDate, finalDate, UTC, stepBetweenEachInstant);

        // Creation of the header
        final Header header = new Header("Line of visibility between a satellite and a station", clock);
        file.addObject(header);

        //// Creation of the satellite
        // Creation of the orbit
        final Frame EME2000 = FramesFactory.getEME2000();
        final KeplerianOrbit initialOrbit = new KeplerianOrbit(7878000, 0, FastMath.toRadians(20), 0, FastMath.toRadians(90), FastMath.toRadians(0), PositionAngleType.MEAN, EME2000, startDate, Constants.WGS84_EARTH_MU);
        // Build of the satellite
        final Satellite satellite = new Satellite(initialOrbit);
        satellite.displayOnlyOnePeriod();
        file.addObject(satellite);

        //// Creation of several ground stations
        // Creation of the model of the earth.
        final IERSConventions IERS = IERSConventions.IERS_2010;
        final Frame ITRF = FramesFactory.getITRF(IERS, true);
        final OneAxisEllipsoid earth = new OneAxisEllipsoid(Constants.WGS84_EARTH_EQUATORIAL_RADIUS, Constants.WGS84_EARTH_FLATTENING, ITRF);

        // Creation of a topocentric frame around Toulouse.
        final GeodeticPoint toulouseFrame = new GeodeticPoint(FastMath.toRadians(43.6047), FastMath.toRadians(1.4442), 10);
        final TopocentricFrame topocentricToulouse = new TopocentricFrame(earth, toulouseFrame, "Toulouse Frame");
        // Creation of a topocentric frame around Quito
        final GeodeticPoint quitoFrame = new GeodeticPoint(FastMath.toRadians(0.1807), FastMath.toRadians(11.5382), 2850);
        final TopocentricFrame topocentricQuito = new TopocentricFrame(earth, quitoFrame, "Quito Frame");
        // Creation of a topocentric frame around Sydney
        final GeodeticPoint sydneyFrame = new GeodeticPoint(FastMath.toRadians(-33.8688), FastMath.toRadians(-241.2093), 100);
        final TopocentricFrame topocentricSydney = new TopocentricFrame(earth, sydneyFrame, "Sydney Frame");
        // Creation of a topocentric frame around gibraltar
        final GeodeticPoint gibraltarFrame = new GeodeticPoint(FastMath.toRadians(36.1408), FastMath.toRadians(5.3536), 400);
        final TopocentricFrame topocentricGibraltar = new TopocentricFrame(earth, gibraltarFrame, "Gibraltar Frame");
        // Creation of another topocentric frame around Las Vegas.
        final GeodeticPoint lasVegasFrame = new GeodeticPoint(FastMath.toRadians(36.1716), FastMath.toRadians(-115.1391), 10);
        final TopocentricFrame topocentricLasVegas = new TopocentricFrame(earth, lasVegasFrame, "Las Vegas Frame");

        // Creation of a list of topocentric frame containing both frames.
        final List<TopocentricFrame> allStations = new ArrayList<>();
        allStations.add(topocentricToulouse);
        allStations.add(topocentricSydney);
        allStations.add(topocentricQuito);
        allStations.add(topocentricGibraltar);
        allStations.add(topocentricLasVegas);

        // Build of the ground stations
        final CzmlGroundStation groundStations = new CzmlGroundStation(allStations);
        file.addObject(groundStations);

        //// Creation of a line of visu between the satellite and all the ground stations
        final LineOfVisibility lineOfVisibility = new LineOfVisibility(allStations, satellite);
        file.addObject(lineOfVisibility);

        // Write inside the CzmlFile the objects
        file.write();
    }
}

