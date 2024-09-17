///* Copyright 2002-2024 CS GROUP
// * Licensed to CS GROUP (CS) under one or more
// * contributor license agreements.  See the NOTICE file distributed with
// * this work for additional information regarding copyright ownership.
// * CS licenses this file to You under the Apache License, Version 2.0
// * (the "License"); you may not use this file except in compliance with
// * the License.  You may obtain a copy of the License at
// *
// *   http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package AttitudeTuto;
//
//import org.hipparchus.geometry.euclidean.threed.Vector3D;
//import org.hipparchus.ode.nonstiff.AdaptiveStepsizeIntegrator;
//import org.hipparchus.ode.nonstiff.DormandPrince853Integrator;
//import org.hipparchus.util.FastMath;
//import org.orekit.attitudes.AlignedAndConstrained;
//import org.orekit.attitudes.AttitudeProvider;
//import org.orekit.attitudes.AttitudesSequence;
//import org.orekit.attitudes.GroundPointTarget;
//import org.orekit.attitudes.LofOffset;
//import org.orekit.attitudes.PredefinedTarget;
//import org.orekit.bodies.CelestialBodyFactory;
//import org.orekit.bodies.GeodeticPoint;
//import org.orekit.bodies.OneAxisEllipsoid;
//import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.AttitudePointing;
//import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.CzmlGroundStation;
//import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.Header;
//import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.Satellite;
//import org.orekit.czml.CzmlObjects.CzmlSecondaryObjects.Clock;
//import org.orekit.czml.Outputs.CzmlFile;
//import org.orekit.czml.Outputs.CzmlFileBuilder;
//import org.orekit.data.DataContext;
//import org.orekit.data.DataProvider;
//import org.orekit.data.DirectoryCrawler;
//import org.orekit.errors.OrekitException;
//import org.orekit.forces.ForceModel;
//import org.orekit.forces.gravity.HolmesFeatherstoneAttractionModel;
//import org.orekit.forces.gravity.potential.GravityFieldFactory;
//import org.orekit.forces.gravity.potential.NormalizedSphericalHarmonicsProvider;
//import org.orekit.frames.Frame;
//import org.orekit.frames.FramesFactory;
//import org.orekit.frames.LOFType;
//import org.orekit.frames.TopocentricFrame;
//import org.orekit.orbits.KeplerianOrbit;
//import org.orekit.orbits.OrbitType;
//import org.orekit.orbits.PositionAngleType;
//import org.orekit.propagation.BoundedPropagator;
//import org.orekit.propagation.EphemerisGenerator;
//import org.orekit.propagation.SpacecraftState;
//import org.orekit.propagation.events.ElevationDetector;
//import org.orekit.propagation.events.EventDetector;
//import org.orekit.propagation.events.handlers.ContinueOnEvent;
//import org.orekit.propagation.numerical.NumericalPropagator;
//import org.orekit.time.AbsoluteDate;
//import org.orekit.time.TimeScale;
//import org.orekit.time.TimeScalesFactory;
//import org.orekit.utils.AngularDerivativesFilter;
//import org.orekit.utils.Constants;
//import org.orekit.utils.ExtendedPVCoordinatesProvider;
//import org.orekit.utils.IERSConventions;
//
//import java.awt.Color;
//import java.io.File;
//import java.util.ArrayList;
//import java.util.List;
//
//public class AttitudePathAlongOrbit {
//
//    private AttitudePathAlongOrbit() {
//        // empty
//    }
//
//    public static void main(final String[] args) throws Exception {
//        // Load orekit data
//        TutorialUtils.loadOrekitData();
//
//        // Paths
//        final String root = System.getProperty("user.dir")
//                                  .replace("\\", "/");
//        final String outputPath    = root + "/Output";
//        final String outputName    = "Output.czml";
//        final String output        = outputPath + "/" + outputName;
//        final String modelsPackage = root + "/src/main/resources/Default3DModels";
//        final String IssModel      = modelsPackage + "/ISSModel.glb";
//        // Change the path here to your JavaScript>public folder.
//        final String pathToJSFolder = root + "/Javascript/public/";

//        // Creation of the clock.
//        final TimeScale    UTC                    = TimeScalesFactory.getUTC();
//        final double       durationOfSimulation   = 10 * 3600; // in seconds;
//        final double       stepBetweenEachInstant = 60.0; // in seconds
//        final AbsoluteDate startDate              = new AbsoluteDate(2024, 3, 15, 0, 0, 0.0, UTC);
//        final AbsoluteDate finalDate              = startDate.shiftedBy(durationOfSimulation);
//        final Clock        clock                  = new Clock(startDate, finalDate, UTC, stepBetweenEachInstant);
//
//        final Header header = new Header("Initialisation of an attitude following a given path", clock, pathToJSFolder);
//
//        // Creation of the model of the earth.
//        final IERSConventions IERS = IERSConventions.IERS_2010;
//        final Frame           ITRF = FramesFactory.getITRF(IERS, true);
//        final OneAxisEllipsoid earth = new OneAxisEllipsoid(Constants.WGS84_EARTH_EQUATORIAL_RADIUS,
//                Constants.WGS84_EARTH_FLATTENING, ITRF);
//
//        // Creation of the model of the sun
//        final ExtendedPVCoordinatesProvider sunModel = CelestialBodyFactory.getSun();
//
//        // List of points on earth to look at :
//        final GeodeticPoint mexico = new GeodeticPoint(FastMath.toRadians(23.6345),
//                FastMath.toRadians(-102.5528), 0);
//        final TopocentricFrame topocentricMexico = new TopocentricFrame(earth, mexico, "Mexico");
//
//        final GeodeticPoint madagascar = new GeodeticPoint(FastMath.toRadians(-18.7669),
//                FastMath.toRadians(46.8691), 0);
//        final TopocentricFrame topocentricMadagascar = new TopocentricFrame(earth, madagascar, "Madagascar");
//
//        final GeodeticPoint portMoresby = new GeodeticPoint(FastMath.toRadians(-9.4790),
//                FastMath.toRadians(147.1494), 0);
//        final TopocentricFrame topocentricPortMoresby = new TopocentricFrame(earth, portMoresby, "Port Moresby");
//
//        final GeodeticPoint maracaibo = new GeodeticPoint(FastMath.toRadians(10.6410),
//                FastMath.toRadians(-71.6074), 0);
//        final TopocentricFrame topocentricMaracaibo = new TopocentricFrame(earth, maracaibo, "Maracaibo");
//
//        final Vector3D mexicoPositionOnEarth      = topocentricMexico.getCartesianPoint();
//        final Vector3D madagascarPositionOnEarth  = topocentricMadagascar.getCartesianPoint();
//        final Vector3D portMoresbyPositionOnEarth = topocentricPortMoresby.getCartesianPoint();
//        final Vector3D maracaiboPositionOnEarth   = topocentricMaracaibo.getCartesianPoint();
//
//        //// Build of a satellite with a propagator
//        // Build of a LEO orbit
//        final Frame EME2000 = FramesFactory.getEME2000();
//        final KeplerianOrbit initialOrbit = new KeplerianOrbit(7878000, 0, FastMath.toRadians(20), 0,
//                FastMath.toRadians(0), FastMath.toRadians(0), PositionAngleType.MEAN, EME2000, startDate,
//                Constants.WGS84_EARTH_MU);
//
//        final SpacecraftState initialState = new SpacecraftState(initialOrbit);
//
//        // Build of the propagator
//        final double positionTolerance = 10.0;
//        final double minStep           = 0.001;
//        final double maxStep           = 1000;
//
//        final double[][] tolerances = NumericalPropagator.tolerances(positionTolerance, initialOrbit,
//                OrbitType.CARTESIAN);
//        final AdaptiveStepsizeIntegrator integrator = new DormandPrince853Integrator(minStep, maxStep, tolerances[0],
//                tolerances[1]);
//
//        final NumericalPropagator propagator = new NumericalPropagator(integrator);
//
//        final NormalizedSphericalHarmonicsProvider provider = GravityFieldFactory.getNormalizedProvider(10,
//                10);
//        final ForceModel holmesFeatherstone = new HolmesFeatherstoneAttractionModel(EME2000,
//                provider);
//
//        propagator.setOrbitType(OrbitType.CARTESIAN);
//        propagator.addForceModel(holmesFeatherstone);
//        propagator.setInitialState(initialState);
//
//        final GroundPointTarget mexicoTarget      = new GroundPointTarget(mexicoPositionOnEarth);
//        final GroundPointTarget madagascarTarget  = new GroundPointTarget(madagascarPositionOnEarth);
//        final GroundPointTarget portMoresbyTarget = new GroundPointTarget(portMoresbyPositionOnEarth);
//        final GroundPointTarget maracaiboTarget   = new GroundPointTarget(maracaiboPositionOnEarth);
//
//        final AlignedAndConstrained attitudeProviderMexico = new AlignedAndConstrained(Vector3D.PLUS_I,
//                mexicoTarget, Vector3D.PLUS_J, PredefinedTarget.SUN, sunModel, earth);
//        final AlignedAndConstrained attitudeProviderMadagascar = new AlignedAndConstrained(Vector3D.PLUS_I,
//                madagascarTarget, Vector3D.PLUS_J, PredefinedTarget.SUN, sunModel, earth);
//        final AlignedAndConstrained attitudeProviderPortMoresby = new AlignedAndConstrained(Vector3D.PLUS_I,
//                portMoresbyTarget, Vector3D.PLUS_J, PredefinedTarget.SUN, sunModel, earth);
//        final AlignedAndConstrained attitudeProviderMaracaibo = new AlignedAndConstrained(Vector3D.PLUS_I,
//                maracaiboTarget, Vector3D.PLUS_J, PredefinedTarget.SUN, sunModel, earth);
//
//        final AttitudesSequence attitudesSequence       = new AttitudesSequence();
//        final AttitudeProvider  attitudeProviderToEarth = new LofOffset(EME2000, LOFType.TNW);
//
//        final EventDetector mexicoToEarth = new ElevationDetector(topocentricMexico).withMaxCheck(60.)
//                                                                                    .withHandler(new ContinueOnEvent());
//        final EventDetector earthToMexico = new ElevationDetector(topocentricMexico).withMaxCheck(60.)
//                                                                                    .withHandler(new ContinueOnEvent());
//
//        final EventDetector madagascarToEarth = new ElevationDetector(topocentricMadagascar).withMaxCheck(60.)
//                                                                                            .withHandler(
//                                                                                                    new ContinueOnEvent());
//        final EventDetector earthToMadagascar = new ElevationDetector(topocentricMadagascar).withMaxCheck(60.)
//                                                                                            .withHandler(
//                                                                                                    new ContinueOnEvent());
//
//        final EventDetector portMoresbyToEarth = new ElevationDetector(topocentricPortMoresby).withMaxCheck(60.)
//                                                                                              .withHandler(
//                                                                                                      new ContinueOnEvent());
//        final EventDetector earthToPortMoresby = new ElevationDetector(topocentricPortMoresby).withMaxCheck(60.)
//                                                                                              .withHandler(
//                                                                                                      new ContinueOnEvent());
//
//        final EventDetector maracaiboToEarth = new ElevationDetector(topocentricMaracaibo).withMaxCheck(60.)
//                                                                                          .withHandler(
//                                                                                                  new ContinueOnEvent());
//        final EventDetector earthToMaracaibo = new ElevationDetector(topocentricMaracaibo).withMaxCheck(60.)
//                                                                                          .withHandler(
//                                                                                                  new ContinueOnEvent());
//
//        final EventDetector mexicoToMaracaibo = new ElevationDetector(topocentricMaracaibo).withMaxCheck(60.)
//                                                                                           .withHandler(
//                                                                                                   new ContinueOnEvent());
//
//        //// Add switching condition to attitudes sequence
//        // Add classic station to earth and earth to station
//        attitudesSequence.addSwitchingCondition(attitudeProviderToEarth, attitudeProviderMexico, earthToMexico, true,
//                false, 10.0, AngularDerivativesFilter.USE_R, null);
//        attitudesSequence.addSwitchingCondition(attitudeProviderMexico, attitudeProviderToEarth, mexicoToEarth, false,
//                true, 10.0, AngularDerivativesFilter.USE_R, null);
//
//        attitudesSequence.addSwitchingCondition(attitudeProviderToEarth, attitudeProviderMadagascar, earthToMadagascar,
//                true, false, 10.0, AngularDerivativesFilter.USE_R, null);
//        attitudesSequence.addSwitchingCondition(attitudeProviderMadagascar, attitudeProviderToEarth, madagascarToEarth,
//                false, true, 10.0, AngularDerivativesFilter.USE_R, null);
//
//        attitudesSequence.addSwitchingCondition(attitudeProviderToEarth, attitudeProviderPortMoresby,
//                earthToPortMoresby, true, false, 10.0, AngularDerivativesFilter.USE_R, null);
//        attitudesSequence.addSwitchingCondition(attitudeProviderPortMoresby, attitudeProviderToEarth,
//                portMoresbyToEarth, false, true, 10.0, AngularDerivativesFilter.USE_R, null);
//
//        attitudesSequence.addSwitchingCondition(attitudeProviderToEarth, attitudeProviderMaracaibo, earthToMaracaibo,
//                true, false, 10.0, AngularDerivativesFilter.USE_R, null);
//        attitudesSequence.addSwitchingCondition(attitudeProviderMaracaibo, attitudeProviderToEarth, maracaiboToEarth,
//                false, true, 10.0, AngularDerivativesFilter.USE_R, null);
//
//        // Add a switching condition between mexico and maracaibo, because they are close enough
//        attitudesSequence.addSwitchingCondition(attitudeProviderMexico, attitudeProviderMaracaibo, mexicoToMaracaibo,
//                true, false, 10.0, AngularDerivativesFilter.USE_R, null);
//
//        attitudesSequence.resetActiveProvider(attitudeProviderToEarth);
//
//        propagator.setAttitudeProvider(attitudesSequence);
//
//        attitudesSequence.registerSwitchEvents(propagator);
//
//        final EphemerisGenerator generator = propagator.getEphemerisGenerator();
//
//        propagator.propagate(startDate, finalDate);
//        final BoundedPropagator boundedPropagator = generator.getGeneratedEphemeris();
//
//        final Satellite satellite = Satellite.builder(boundedPropagator)
//                                             .withModelPath(IssModel)
//                                             .withColor(Color.PINK)
//                                             .withOnlyOnePeriod()
//                                             .withDisplayAttitude()
//                                             .build();
//
//        final AttitudePointing attitudePointing = AttitudePointing.builder(satellite, earth, Vector3D.PLUS_I)
//                                                                  .withAvailability(Header.getMasterClock()
//                                                                                          .getAvailability())
//                                                                  .withColor(Color.cyan)
//                                                                  .withDisplayOnGround(true)
//                                                                  .displayPointingPath()
//                                                                  .displayPeriodPointingPath()
//                                                                  .build();
//
//        final CzmlGroundStation       mexicoGroundStation      = new CzmlGroundStation(topocentricMexico);
//        final CzmlGroundStation       madagascarGroundStation  = new CzmlGroundStation(topocentricMadagascar);
//        final CzmlGroundStation       portMoresbyGroundStation = new CzmlGroundStation(topocentricPortMoresby);
//        final CzmlGroundStation       maracaiboGroundStation   = new CzmlGroundStation(topocentricMaracaibo);
//        final List<CzmlGroundStation> allGroundStation         = new ArrayList<>();
//        allGroundStation.add(mexicoGroundStation);
//        allGroundStation.add(madagascarGroundStation);
//        allGroundStation.add(portMoresbyGroundStation);
//        allGroundStation.add(maracaiboGroundStation);
//
//        // Creation of the file
//        final CzmlFile file = CzmlFile.builder(output).withHeader(header)
//                                                         .withSatellite(satellite)
//                                                         .withAttitudePointing(attitudePointing)
//                                                         .withCzmlGroundStation(allGroundStation)
//                                                         .build();
//
//        // Writing the file
//        file.write();
//    }
//}
