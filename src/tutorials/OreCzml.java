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

import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.CzmlGroundStation;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.Header;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.LineOfVisibility;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.Satellite;
import org.orekit.czml.CzmlObjects.CzmlSecondaryObjects.HeaderObjects.Clock;
import org.orekit.czml.CzmlObjects.TerrestrialReferenceSystem;
import org.orekit.czml.Outputs.CzmlFile;
import org.hipparchus.linear.MatrixUtils;
import org.hipparchus.linear.RealMatrix;
import org.hipparchus.ode.nonstiff.AdaptiveStepsizeIntegrator;
import org.hipparchus.ode.nonstiff.DormandPrince853Integrator;
import org.hipparchus.util.FastMath;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.data.DataContext;
import org.orekit.data.DataProvider;
import org.orekit.data.DirectoryCrawler;
import org.orekit.errors.OrekitException;
import org.orekit.forces.ForceModel;
import org.orekit.forces.gravity.HolmesFeatherstoneAttractionModel;
import org.orekit.forces.gravity.potential.GravityFieldFactory;
import org.orekit.forces.gravity.potential.NormalizedSphericalHarmonicsProvider;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.frames.LOFType;
import org.orekit.frames.TopocentricFrame;
import org.orekit.orbits.KeplerianOrbit;
import org.orekit.orbits.OrbitType;
import org.orekit.orbits.PositionAngleType;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.StateCovariance;
import org.orekit.propagation.numerical.NumericalPropagator;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScale;
import org.orekit.time.TimeScalesFactory;
import org.orekit.utils.Constants;
import org.orekit.utils.IERSConventions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class OreCzml
{
    private OreCzml() {
    }

    public static void main(final String[] args ) throws Exception {

        try {
            final File home = new File(System.getProperty("user.home"));
            final File orekitDir = new File(home, "orekit-data");
            final DataProvider provider = new DirectoryCrawler(orekitDir);
            DataContext.getDefault().getDataProvidersManager().addProvider(provider);
        } catch (OrekitException oe) {
            System.err.println(oe.getLocalizedMessage());
        }

        final long t0 = System.currentTimeMillis();

        final String DEFAULT_SECONDS = " s";

        //// PATHS
        // Global
        final String root = System.getProperty("user.dir").replace("\\", "/");
        final String Javascript = root + "/JavaScript";
        final String JavaScriptOutput = Javascript + "/public";
        final String resources = root + "/src/main/resources";
        final String outputPath = root + "/Output";
        // OEM
        final String inputOemMission = "oemMission.kvn";
        final String inputOemChaser = "oemChaser.kvn";
        final String inputOemMissionPath = resources + "/" + inputOemMission;
        final String inputOemChaserPath = resources + "/" + inputOemChaser;
        // Output
        final String outputName = "Output.czml";
        final String output = outputPath + "/" + outputName;
        final String outputPathFileJavaScript = JavaScriptOutput + "/" + outputName;
        // xml / opm
        final String opm = "estimated.xml";
        final String opmPath = resources + "/" + opm;

        // JavaScript
        final String npm = Javascript + "/npm";

        // MODEL 3D
        final String ISSModelRelative = "./ISSModel.glb";
        final String ImageModel = "C:\\Users\\jleblond\\Pictures\\satellite.png";
        final String ISSModelAbsolute = "C:\\Users\\jleblond\\Pictures\\ISSModel.glb";

        final CzmlFile file = new CzmlFile(outputPathFileJavaScript);


        // Propagator
        final Frame EME2000 = FramesFactory.getEME2000();
        final TimeScale UTC = TimeScalesFactory.getUTC();
        final AbsoluteDate startDate = new AbsoluteDate(2022, 1, 18, 12, 0, 0.0, UTC);
        final AbsoluteDate finalDate = new AbsoluteDate(2022, 1, 19, 12, 0, 0.0, UTC);
        final KeplerianOrbit initialOrbit = new KeplerianOrbit(7878000, 0, FastMath.toRadians(30), 0, FastMath.toRadians(90), FastMath.toRadians(0), PositionAngleType.MEAN, EME2000, startDate, Constants.WGS84_EARTH_MU);

        final SpacecraftState initialState = new SpacecraftState(initialOrbit);

        final double step = 60;
        final double positionTolerance = 10.0;
        final double minStep = 0.001;
        final double maxStep = 1000.0;

        final double[][] tolerances = NumericalPropagator.tolerances(positionTolerance, initialOrbit, OrbitType.CARTESIAN);
        final AdaptiveStepsizeIntegrator integrator = new DormandPrince853Integrator(minStep, maxStep, tolerances[0], tolerances[1]);

        final NumericalPropagator propagator = new NumericalPropagator(integrator);

        final NormalizedSphericalHarmonicsProvider provider = GravityFieldFactory.getNormalizedProvider(10, 10);
        final ForceModel holmesFeatherstone = new HolmesFeatherstoneAttractionModel(EME2000, provider);

        final long timeVariablesDeclared = System.currentTimeMillis();
        final double dtTimeVariablesDeclared = (timeVariablesDeclared - t0) * 1.e-3;
        System.out.println("Temps variableDeclared " + dtTimeVariablesDeclared + DEFAULT_SECONDS);

        propagator.setOrbitType(OrbitType.CARTESIAN);
        propagator.addForceModel(holmesFeatherstone);
        propagator.setInitialState(initialState);

        //// Covariance
        // File
//        final File opmFile = new File(opmPath);
//        final ParserBuilder parserBuilder = new ParserBuilder();
//        final OpmParser opmParser = parserBuilder.buildOpmParser();
//        final DataSource opmSource = new DataSource(opmFile);
//        final Opm opmFinalParsed = opmParser.parseMessage(opmSource);
//        final CartesianCovariance initialCovariance = opmFinalParsed.getData().getCovarianceBlock();
        final RealMatrix realMatrix = MatrixUtils.createRealDiagonalMatrix(new double[] {100, 100, 100, 1e-4, 1e-4, 1e-4});
        final StateCovariance stateCovariance = new StateCovariance(realMatrix, startDate, LOFType.TNW);
        final StateCovariance finalCovariance = stateCovariance.changeCovarianceFrame(initialOrbit, initialOrbit.getFrame());

        // Topocentric frame
        final IERSConventions IERS = IERSConventions.IERS_2010;
        final Frame ITRF = FramesFactory.getITRF(IERS, true);
        final OneAxisEllipsoid earth = new OneAxisEllipsoid(Constants.WGS84_EARTH_EQUATORIAL_RADIUS, Constants.WGS84_EARTH_FLATTENING, ITRF);

        final long timeEarthCreated = System.currentTimeMillis();
        final double dtTimeTopocentricFramesDeclared = (timeEarthCreated - timeVariablesDeclared) * 1.e-3;
        System.out.println("Temps TopocentricFramesDeclared " + dtTimeTopocentricFramesDeclared + DEFAULT_SECONDS);

        //Position of Toulouse
        final GeodeticPoint toulouseFrame = new GeodeticPoint(FastMath.toRadians(43.6047), FastMath.toRadians(1.4442), 10);
        final TopocentricFrame topocentricToulouse = new TopocentricFrame(earth, toulouseFrame, "Toulouse Frame");
        final GeodeticPoint lasVegasFrame = new GeodeticPoint(FastMath.toRadians(36.1716), FastMath.toRadians(-115.1391), 10);
        final TopocentricFrame topocentricLasVegas = new TopocentricFrame(earth, lasVegasFrame, "Las Vegas Frame");

        final List<TopocentricFrame> topocentricFrameList = new ArrayList<>();
        topocentricFrameList.add(topocentricToulouse);
        topocentricFrameList.add(topocentricLasVegas);

        final long timeSetup = System.currentTimeMillis();
        final double dtSetup = (timeSetup - timeEarthCreated) * 1.e-3;
        System.out.println("Temps setup " + dtSetup + DEFAULT_SECONDS);

        // Header
        final Header header = new Header("test", new Clock(startDate, finalDate, UTC, step));
        file.addObject(header);

        final TerrestrialReferenceSystem system = new TerrestrialReferenceSystem();

        final long timeHeader = System.currentTimeMillis();
        final double dtHeader = (timeHeader - timeSetup) * 1.e-3;
        System.out.println("Temps Header " + dtHeader + DEFAULT_SECONDS);

        // Satellite
        final Satellite satellite = new Satellite(propagator, finalDate, ISSModelAbsolute);
        satellite.displayOnlyOnePeriod();
        file.addObject(satellite);

        final long timeSatellite = System.currentTimeMillis();
        final double dtSatellite = (timeSatellite - timeHeader) * 1.e-3;
        System.out.println("Temps satellite " + dtSatellite + DEFAULT_SECONDS);

        //Ground Station
        final CzmlGroundStation toulouseStation = new CzmlGroundStation(topocentricToulouse);
        file.addObject(toulouseStation);

        final long timeGroundStation = System.currentTimeMillis();
        final double dtGroundStation = (timeGroundStation - timeSatellite) * 1.e-3;
        System.out.println("Temps allStations " + dtGroundStation + DEFAULT_SECONDS);

        // Line of visibility
        final LineOfVisibility lineOfVisibility = new LineOfVisibility(topocentricToulouse, satellite);
        file.addObject(lineOfVisibility);

        final long timeLineOfVisibility = System.currentTimeMillis();
        final double dtLineOfVisibility = (timeLineOfVisibility - timeGroundStation) * 1.e-3;
        System.out.println("Temps LineOfVisibility " + dtLineOfVisibility + DEFAULT_SECONDS);

        file.write();

//      final JavaScriptRun run = new JavaScriptRun(Javascript);

        final long t1 = System.currentTimeMillis();
        final double dt = (t1 - t0) * 1.e-3;
        System.out.println("Final Time : " + dt + DEFAULT_SECONDS);
    }
}
