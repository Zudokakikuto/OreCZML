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
package org.orekit.czml.file;

import org.hipparchus.geometry.euclidean.threed.RotationOrder;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.hipparchus.linear.MatrixUtils;
import org.hipparchus.linear.RealMatrix;
import org.hipparchus.ode.nonstiff.AdaptiveStepsizeIntegrator;
import org.hipparchus.ode.nonstiff.DormandPrince853Integrator;
import org.hipparchus.util.FastMath;
import org.junit.jupiter.api.Assertions;
import org.orekit.attitudes.AttitudesSequence;
import org.orekit.attitudes.CelestialBodyPointed;
import org.orekit.attitudes.LofOffset;
import org.orekit.bodies.CelestialBodyFactory;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.czml.archi.adaptor.AemAdaptor;
import org.orekit.czml.archi.adaptor.OemAdaptor;
import org.orekit.czml.object.primary.entities.SpacecraftBuilder;
import org.orekit.czml.archi.factory.BodyFactory;
import org.orekit.czml.object.primary.entities.Body;
import org.orekit.czml.object.primary.covariance.Collision;
import org.orekit.czml.object.primary.entities.Constellation;
import org.orekit.czml.object.primary.covariance.Covariance;
import org.orekit.czml.object.primary.entities.CzmlGroundStation;
import org.orekit.czml.object.primary.GroundTrack;
import org.orekit.czml.object.primary.Header;
import org.orekit.czml.object.primary.ManeuverSequence;
import org.orekit.czml.object.primary.entities.Spacecraft;
import org.orekit.czml.object.primary.pointing.AttitudePointing;
import org.orekit.czml.object.primary.pointing.CoveredSurfaceOnBody;
import org.orekit.czml.object.primary.systems.CentralBodyReferenceSystem;
import org.orekit.czml.object.primary.systems.LatLongLines;
import org.orekit.czml.object.primary.visu.FieldOfObservation;
import org.orekit.czml.object.primary.visu.InterSatVisu;
import org.orekit.czml.object.primary.visu.LineOfVisibility;
import org.orekit.czml.object.secondary.Clock;
import org.orekit.czml.object.secondary.Orientation;
import org.orekit.data.DataSource;
import org.orekit.files.ccsds.ndm.ParserBuilder;
import org.orekit.files.ccsds.ndm.adm.aem.Aem;
import org.orekit.files.ccsds.ndm.adm.aem.AemParser;
import org.orekit.files.ccsds.ndm.odm.oem.Oem;
import org.orekit.files.ccsds.ndm.odm.oem.OemParser;
import org.orekit.forces.ForceModel;
import org.orekit.forces.gravity.HolmesFeatherstoneAttractionModel;
import org.orekit.forces.gravity.potential.GravityFieldFactory;
import org.orekit.forces.gravity.potential.NormalizedSphericalHarmonicsProvider;
import org.orekit.forces.maneuvers.Maneuver;
import org.orekit.forces.maneuvers.propulsion.BasicConstantThrustPropulsionModel;
import org.orekit.forces.maneuvers.propulsion.PropulsionModel;
import org.orekit.forces.maneuvers.trigger.DateBasedManeuverTriggers;
import org.orekit.forces.maneuvers.trigger.ManeuverTriggers;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.frames.LOFType;
import org.orekit.frames.TopocentricFrame;
import org.orekit.frames.Transform;
import org.orekit.geometry.fov.DoubleDihedraFieldOfView;
import org.orekit.geometry.fov.FieldOfView;
import org.orekit.orbits.KeplerianOrbit;
import org.orekit.orbits.OrbitType;
import org.orekit.orbits.PositionAngleType;
import org.orekit.propagation.BoundedPropagator;
import org.orekit.propagation.EphemerisGenerator;
import org.orekit.propagation.Propagator;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.StateCovariance;
import org.orekit.propagation.events.DateDetector;
import org.orekit.propagation.events.EventDetector;
import org.orekit.propagation.events.handlers.ContinueOnEvent;
import org.orekit.propagation.numerical.NumericalPropagator;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScale;
import org.orekit.time.TimeScalesFactory;
import org.orekit.utils.AngularDerivativesFilter;
import org.orekit.utils.Constants;
import org.orekit.utils.IERSConventions;

import java.awt.Color;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * The type Global tests.
 */
class GlobalTests extends AbstractTest {
    /**
     * Test the adaptors, the attitude pointing, the satellite, the ground stations,
     * the satellite reference system, the central body reference system, the body,
     * the lat-long display.
     *
     * @throws URISyntaxException the uri syntax exception
     * @throws IOException        the io exception
     */
    @org.junit.jupiter.api.Test
    void test1() throws URISyntaxException, IOException {

        loadOrekitData();

        // Paths
        final String output = loadResources("Output") + "/Output1.czml";
        // Change the path here to your JavaScript>public folder.
        final String pathToJSFolder = loadResources("");

        final String OemPath            = loadResources("oemForAemTuto.xml");
        final String AemPath            = loadResources("aemForAemTuto.xml");
        final String IssModel           = loadResources("Default3DModels/ISSModel.glb");
        final String referenceFilePath1 = loadResources("test1.czml");

        // Creation of the Oem
        final TimeScale UTC  = TimeScalesFactory.getUTC();
        final Frame     ITRF = FramesFactory.getITRF(IERSConventions.IERS_2010, true);
        final OneAxisEllipsoid earth = new OneAxisEllipsoid(Constants.WGS84_EARTH_EQUATORIAL_RADIUS,
                Constants.WGS84_EARTH_FLATTENING, ITRF);
        final DataSource    dataSourceOem    = new DataSource(OemPath);
        final ParserBuilder parserBuilderOem = new ParserBuilder();
        final OemParser     oemParser        = parserBuilderOem.buildOemParser();
        final Oem           oem              = oemParser.parse(dataSourceOem);

        // Creation of the Aem
        final DataSource    dataSourceAem    = new DataSource(AemPath);
        final ParserBuilder parserBuilderAem = new ParserBuilder();
        final AemParser     aemParser        = parserBuilderAem.buildAemParser();
        final Aem           aem              = aemParser.parse(dataSourceAem);

        // Adaptor for oem
        final OemAdaptor         oemAdaptor    = new OemAdaptor(oem);
        final Propagator         oemPropagator = oemAdaptor.buildPropagator();
        final AbsoluteDate       startDate     = new AbsoluteDate(2020, 12, 1, 0, 0, 0.0, UTC);
        final AbsoluteDate       finalDate     = new AbsoluteDate(2020, 12, 1, 0, 30, 0.0, UTC);
        final EphemerisGenerator generator     = oemPropagator.getEphemerisGenerator();
        oemPropagator.propagate(startDate, finalDate);
        final BoundedPropagator oemBoundedPropagator = generator.getGeneratedEphemeris();

        // Creation of the clock
        final double stepBetweenEachInstant = 60.0; // in seconds
        final Clock  clock                  = new Clock(startDate, finalDate, UTC, stepBetweenEachInstant);

        // Creation of the header
        final Header header = new Header("Generated test 1", clock, pathToJSFolder);

        // Creation of the orientation for the satellite with the aem adaptor
        // Careful here, the header must be set before else way the bounded propagator does not have a reference for the timescale.
        final AemAdaptor  aemAdaptor  = new AemAdaptor(aem);
        final Orientation orientation = aemAdaptor.buildOrientation(oemBoundedPropagator, header);

        // Creation of the satellite
        final Spacecraft satellite = new SpacecraftBuilder(oemBoundedPropagator, header).withModelPath(IssModel)
                                                                                        .withOrientation(orientation)
                                                                                        .withReferenceSystem()
                                                                                        .build();

        final TopocentricFrame topocentricForStation = new TopocentricFrame(earth, new GeodeticPoint(0, 0, 0),
                "Station");
        final CzmlGroundStation groundStation = new CzmlGroundStation(topocentricForStation, "", header);

        // Attitude pointing
        final AttitudePointing pointing = AttitudePointing.builder(satellite, earth, Vector3D.MINUS_I, header)
                                                          .build();

        // Body
        final Body jupiter = BodyFactory.getJupiter(header);

        // CentralBodyReferenceSystem
        final CentralBodyReferenceSystem system = CentralBodyReferenceSystem.builder(header).build();

        // Latitude longitude lines display
        final LatLongLines latLong = LatLongLines.builder(header)
                                                 .build();

        final List<Spacecraft> satellites = new ArrayList<>();
        satellites.add(satellite);

        final List<BoundedPropagator> propagators = new ArrayList<>();
        propagators.add(oemBoundedPropagator);
        final Constellation       constellation  = Constellation.builder(propagators, finalDate, header).build();
        final List<Constellation> constellations = new ArrayList<>();
        constellations.add(constellation);

        final List<AttitudePointing> pointings = new ArrayList<>();
        pointings.add(pointing);

        final List<LatLongLines> lines = new ArrayList<>();
        lines.add(latLong);

        // The Czml file
        final CzmlFile file = CzmlFile.builder()
                                      .withHeader(header)
                                      .withAttitudePointing(pointing)
                                      .withBody(jupiter)
                                      .withCzmlGroundStation(groundStation)
                                      .withSpacecraft(satellite)
                                      .withCentralBodyReferenceSystem(system)
                                      .withLatLong(latLong)
                                      .build();

        // Coverage for CzmlFile
        final CzmlFile coverageFile = CzmlFile.builder()
                                              .withHeader(header)
                                              .withSpacecraft(satellites)
                                              .withConstellation(constellations)
                                              .withAttitudePointing(pointings)
                                              .withLatLong(lines)
                                              .build();

        // File writing
        file.write(output);

        // Comparing of generated file and reference file.
        final String stringReference = Files.readString(Path.of(referenceFilePath1));
        final String stringGenerated = Files.readString(Path.of(output));

        final String pathToCoverageTemplateCzmlFile = loadResources(
                "templateFile/file/Test1CzmlFileTemplateCoverage.txt");

        Assertions.assertEquals(stringReference, stringGenerated);
        Assertions.assertEquals(Files.readString(Path.of(pathToCoverageTemplateCzmlFile)), coverageFile.toString());
    }

    /**
     * Test the field of observation, the ground tracks, the inter-visu, the constellation, the covariance display, the model loading,
     * the surface covered on the body, the collision.
     *
     * @throws URISyntaxException the uri syntax exception
     * @throws IOException        the io exception
     */
    @org.junit.jupiter.api.Test
    void test2() throws URISyntaxException, IOException {

        loadOrekitData();

        // Paths
        final String output = loadResources("Output") + "/Output2.czml";
        // Change the path here to your JavaScript>public folder.
        final String pathToJSFolder = loadResources(".");

        final String IssModel           = loadResources("Default3DModels/ISSModel.glb");
        final String referenceFilePath2 = loadResources("test2.czml");

        final TimeScale    UTC       = TimeScalesFactory.getUTC();
        final AbsoluteDate startDate = new AbsoluteDate(2024, 3, 15, 0, 0, 0.0, UTC);
        final AbsoluteDate finalDate = new AbsoluteDate(2024, 3, 15, 5, 0, 0.0, UTC);

        // Creation of the clock
        final double stepBetweenEachInstant = 60.0; // in seconds
        final Clock  clock                  = new Clock(startDate, finalDate, UTC, stepBetweenEachInstant);

        final IERSConventions IERS = IERSConventions.IERS_2010;
        final Frame           ITRF = FramesFactory.getITRF(IERS, true);
        final OneAxisEllipsoid earth = new OneAxisEllipsoid(Constants.WGS84_EARTH_EQUATORIAL_RADIUS,
                Constants.WGS84_EARTH_FLATTENING, ITRF);

        final Header header = new Header("Generated test 2", clock, pathToJSFolder);

        //// Build of a satellite with a propagator
        // Build of a LEO orbit
        final Frame EME2000 = FramesFactory.getEME2000();
        final KeplerianOrbit firstOrbit = new KeplerianOrbit(7878000, 0, FastMath.toRadians(20), 0,
                FastMath.toRadians(0), FastMath.toRadians(0), PositionAngleType.MEAN, EME2000, startDate,
                Constants.WGS84_EARTH_MU);
        final KeplerianOrbit secondOrbit = new KeplerianOrbit(7878100, 0, FastMath.toRadians(200), 0,
                FastMath.toRadians(0), FastMath.toRadians(0), PositionAngleType.MEAN, EME2000, startDate,
                Constants.WGS84_EARTH_MU);

        final KeplerianOrbit orbitConstellation1 = new KeplerianOrbit(10579800, 0, FastMath.toRadians(80), 0,
                FastMath.toRadians(0), FastMath.toRadians(0), PositionAngleType.MEAN, EME2000, startDate,
                Constants.EGM96_EARTH_MU);

        final KeplerianOrbit orbitConstellation2 = new KeplerianOrbit(10579800, 0, FastMath.toRadians(80), 0,
                FastMath.toRadians(0), FastMath.toRadians(90), PositionAngleType.MEAN, EME2000, startDate,
                Constants.EGM96_EARTH_MU);

        // Initial States
        final SpacecraftState firstState  = new SpacecraftState(firstOrbit);
        final SpacecraftState secondState = new SpacecraftState(secondOrbit);

        final SpacecraftState constellationFirstState  = new SpacecraftState(orbitConstellation1);
        final SpacecraftState constellationSecondState = new SpacecraftState(orbitConstellation2);

        // Build of the propagator
        final double positionTolerance = 10.0;
        final double minStep           = 0.001;
        final double maxStep           = 1000.0;

        final double[][] tolerances1 = NumericalPropagator.tolerances(positionTolerance, firstOrbit,
                OrbitType.CARTESIAN);
        final double[][] tolerances2 = NumericalPropagator.tolerances(positionTolerance, secondOrbit,
                OrbitType.CARTESIAN);

        final double[][] tolerancesConstellation1 = NumericalPropagator.tolerances(positionTolerance,
                orbitConstellation1, OrbitType.CARTESIAN);
        final double[][] tolerancesConstellation2 = NumericalPropagator.tolerances(positionTolerance,
                orbitConstellation2, OrbitType.CARTESIAN);


        final AdaptiveStepsizeIntegrator integrator1 = new DormandPrince853Integrator(minStep, maxStep, tolerances1[0],
                tolerances1[1]);
        final AdaptiveStepsizeIntegrator integrator2 = new DormandPrince853Integrator(minStep, maxStep, tolerances2[0],
                tolerances2[1]);

        final AdaptiveStepsizeIntegrator integratorConstellation1 = new DormandPrince853Integrator(minStep, maxStep,
                tolerancesConstellation1[0], tolerancesConstellation1[1]);
        final AdaptiveStepsizeIntegrator integratorConstellation2 = new DormandPrince853Integrator(minStep, maxStep,
                tolerancesConstellation2[0], tolerancesConstellation2[1]);

        final NumericalPropagator firstPropagator  = new NumericalPropagator(integrator1);
        final NumericalPropagator secondPropagator = new NumericalPropagator(integrator2);

        final NumericalPropagator firstPropagatorConstellation  = new NumericalPropagator(integratorConstellation1);
        final NumericalPropagator secondPropagatorConstellation = new NumericalPropagator(integratorConstellation2);

        final NormalizedSphericalHarmonicsProvider provider = GravityFieldFactory.getNormalizedProvider(10,
                10);
        final ForceModel holmesFeatherstone = new HolmesFeatherstoneAttractionModel(EME2000,
                provider);

        firstPropagator.setOrbitType(OrbitType.CARTESIAN);
        firstPropagator.addForceModel(holmesFeatherstone);
        firstPropagator.setInitialState(firstState);

        secondPropagator.setOrbitType(OrbitType.CARTESIAN);
        secondPropagator.addForceModel(holmesFeatherstone);
        secondPropagator.setInitialState(secondState);

        firstPropagatorConstellation.setOrbitType(OrbitType.CARTESIAN);
        firstPropagatorConstellation.addForceModel(holmesFeatherstone);
        firstPropagatorConstellation.setInitialState(constellationFirstState);

        secondPropagatorConstellation.setOrbitType(OrbitType.CARTESIAN);
        secondPropagatorConstellation.addForceModel(holmesFeatherstone);
        secondPropagatorConstellation.setInitialState(constellationSecondState);

        final LofOffset lofOffset = new LofOffset(EME2000, LOFType.TNW, RotationOrder.XYZ, FastMath.toRadians(0),
                FastMath.toRadians(0), FastMath.toRadians(0));
        firstPropagator.setAttitudeProvider(lofOffset);
        secondPropagator.setAttitudeProvider(lofOffset);

        final EphemerisGenerator firstGenerator  = firstPropagator.getEphemerisGenerator();
        final EphemerisGenerator secondGenerator = secondPropagator.getEphemerisGenerator();

        final EphemerisGenerator firstGeneratorConstellation  = firstPropagatorConstellation.getEphemerisGenerator();
        final EphemerisGenerator secondGeneratorConstellation = secondPropagatorConstellation.getEphemerisGenerator();

        firstPropagator.propagate(startDate, finalDate);
        secondPropagator.propagate(startDate, finalDate);

        firstPropagatorConstellation.propagate(startDate, finalDate);
        secondPropagatorConstellation.propagate(startDate, finalDate);

        final BoundedPropagator firstBoundedPropagator  = firstGenerator.getGeneratedEphemeris();
        final BoundedPropagator secondBoundedPropagator = secondGenerator.getGeneratedEphemeris();

        final List<BoundedPropagator> listForConstellation = new ArrayList<>();
        listForConstellation.add(firstGeneratorConstellation.getGeneratedEphemeris());
        listForConstellation.add(secondGeneratorConstellation.getGeneratedEphemeris());

        // Creation of the satellite
        final Spacecraft firstSatellite = Spacecraft.builder(firstBoundedPropagator, header)
                                                    .withModelPath(IssModel)
                                                    .withColor(Color.RED)
                                                    .withOnlyOnePeriod()
                                                    .withDisplayAttitude()
                                                    .build();

        final Spacecraft secondSatellite = Spacecraft.builder(secondBoundedPropagator, header)
                                                     .withColor(Color.RED)
                                                     .withOnlyOnePeriod()
                                                     .withDisplayAttitude()
                                                     .build();

        // Constellation
        final Constellation constellation = Constellation.builder(listForConstellation, finalDate, header).build();

        // Covariance display
        final RealMatrix realMatrix = MatrixUtils.createRealDiagonalMatrix(
                new double[] {20000 * 20000, 1e-6, 1e-6, 1e-6, 1e-6, (36 * 4.848e-6) * (36 * 4.848e-6)});
        final StateCovariance stateCovariance = new StateCovariance(realMatrix, startDate, EME2000,
                OrbitType.EQUINOCTIAL, PositionAngleType.MEAN);
        final List<StateCovariance> covariances1 = covariancePropagation(firstSatellite, firstPropagator,
                stateCovariance, header);
        final List<StateCovariance> covariances2 = covariancePropagation(secondSatellite, secondPropagator,
                stateCovariance, header);

        final Covariance covariance1 = Covariance.builder(firstSatellite, covariances1, LOFType.TNW, header)
                                                 .build();

        // Creation of the field of observation of the satellite
        final Transform initialInertToBody = firstState.getFrame()
                                                       .getTransformTo(earth.getBodyFrame(), firstState.getDate());
        final Transform initialFovBody = new Transform(firstState.getDate(), firstState.toTransform()
                                                                                       .getInverse(),
                initialInertToBody);
        // A circular field of view
        //final FieldOfView fov = new CircularFieldOfView(Vector3D.PLUS_J, FastMath.toRadians(50), 2);
        // A rectangular field of view
        final FieldOfView fov = new DoubleDihedraFieldOfView(Vector3D.PLUS_J, Vector3D.PLUS_I, FastMath.toRadians(20),
                Vector3D.PLUS_K, FastMath.toRadians(20), 2);

        final FieldOfObservation fieldOfObservation = FieldOfObservation.builder(firstSatellite, fov, initialFovBody,
                                                                                header)
                                                                        .build();

        // Ground track
        final GroundTrack groundTrack = GroundTrack.builder(firstSatellite, earth, header).build();

        final GroundTrack groundTrackConstellation = GroundTrack.builder(constellation, earth, header).build();

        // Covered surface on body
        final CoveredSurfaceOnBody surface = CoveredSurfaceOnBody.builder(firstSatellite, fieldOfObservation, header)
                                                                 .build();

        // Inter visu
        final InterSatVisu interVisu = InterSatVisu.builder(firstSatellite, secondSatellite, finalDate, header)
                                                   .build();

        // collision
        final Collision collision = Collision.builder(firstSatellite, secondSatellite, covariances1,
                covariances2, LOFType.TNW, LOFType.TNW, header).build();


        final List<GroundTrack> groundTracks = new ArrayList<>();
        groundTracks.add(groundTrack);

        final List<FieldOfObservation> fields = new ArrayList<>();
        fields.add(fieldOfObservation);

        final List<InterSatVisu> interSatVisusList = new ArrayList<>();
        interSatVisusList.add(interVisu);

        final List<Covariance> covariances = new ArrayList<>();
        covariances.add(covariance1);

        final List<CoveredSurfaceOnBody> surfaces = new ArrayList<>();
        surfaces.add(surface);

        final List<Collision> collisions = new ArrayList<>();
        collisions.add(collision);

        final CzmlFile file = CzmlFile.builder()
                                      .withHeader(header)
                                      .withSpacecraft(firstSatellite)
                                      .withFieldOfObservation(fieldOfObservation)
                                      .withGroundTrack(groundTrack)
                                      .withGroundTrack(groundTrackConstellation)
                                      .withInterSatVisu(interVisu)
                                      .withConstellation(constellation)
                                      .withCovariance(covariance1)
                                      .withCoveredSurfaceOnBody(surface)
                                      .withCollision(collision)
                                      .build();

        final CzmlFile coverageFile = CzmlFile.builder()
                                              .withHeader(header)
                                              .withSpacecraft(firstSatellite)
                                              .withGroundTrack(groundTracks)
                                              .withFieldOfObservation(fields)
                                              .withInterSatVisu(interSatVisusList)
                                              .withCovariance(covariances)
                                              .withCoveredSurfaceOnBody(surfaces)
                                              .withCollision(collisions)
                                              .build();


        file.write(output);

        // Comparing of generated file and reference file.
        final String stringReference = Files.readString(Path.of(referenceFilePath2));
        final String stringGenerated = Files.readString(Path.of(output));
        final String coverageCzmLFilePathTemplate = loadResources(
                "templateFile/file/Test2CzmlFileTemplateCoverage.txt");

        Assertions.assertEquals(Files.readString(Path.of(coverageCzmLFilePathTemplate)), coverageFile.toString());
        Assertions.assertEquals(stringReference, stringGenerated);
    }

    /**
     * Test the maneuver sequence, multiple ground stations, the line of visu sat station
     *
     * @throws URISyntaxException the uri syntax exception
     * @throws IOException        the io exception
     */
    @org.junit.jupiter.api.Test
    void test3() throws URISyntaxException, IOException {

        loadOrekitData();

        // Paths
        final String output = loadResources("Output") + "/Output3.czml";
        // Change the path here to your JavaScript>public folder.
        final String pathToJSFolder = loadResources(".");

        final String IssModel           = loadResources("Default3DModels/ISSModel.glb");
        final String referenceFilePath3 = loadResources("test3.czml");

        // Creation of the clock.
        final TimeScale    UTC                    = TimeScalesFactory.getUTC();
        final double       durationOfSimulation   = 5 * 3600; // in seconds;
        final double       stepBetweenEachInstant = 30.0; // in seconds
        final AbsoluteDate startDate              = new AbsoluteDate(2024, 3, 15, 0, 0, 0.0, UTC);
        final AbsoluteDate finalDate              = startDate.shiftedBy(durationOfSimulation);
        final Clock        clock                  = new Clock(startDate, finalDate, UTC, stepBetweenEachInstant);

        // Build of the header
        final Header header = new Header("Generated test 3", clock, pathToJSFolder);


        // Creation of the list of maneuvers
        final List<Maneuver> maneuvers = new ArrayList<>();

        //// Creation of the satellite
        // build of the propagator

        final Frame EME2000 = FramesFactory.getEME2000();
        final KeplerianOrbit initialOrbit = new KeplerianOrbit(7878000, 0, FastMath.toRadians(20), 0,
                FastMath.toRadians(90), FastMath.toRadians(0), PositionAngleType.MEAN, EME2000, startDate,
                Constants.WGS84_EARTH_MU);

        final IERSConventions IERS = IERSConventions.IERS_2010;
        final Frame           ITRF = FramesFactory.getITRF(IERS, true);
        final OneAxisEllipsoid earth = new OneAxisEllipsoid(Constants.WGS84_EARTH_EQUATORIAL_RADIUS,
                Constants.WGS84_EARTH_FLATTENING, ITRF);

        final SpacecraftState initialState = new SpacecraftState(initialOrbit);

        final double positionTolerance = 10.0;
        final double minStep           = 0.001;
        final double maxStep           = 1000;
        final NormalizedSphericalHarmonicsProvider provider = GravityFieldFactory.getNormalizedProvider(10,
                10);
        final ForceModel holmesFeatherstone = new HolmesFeatherstoneAttractionModel(EME2000,
                provider);

        final double[][] tolerances = NumericalPropagator.tolerances(positionTolerance, initialOrbit,
                OrbitType.CARTESIAN);
        final AdaptiveStepsizeIntegrator integrator = new DormandPrince853Integrator(minStep, maxStep, tolerances[0],
                tolerances[1]);

        final NumericalPropagator propagator = new NumericalPropagator(integrator);

        ////// Add the maneuvers (MANEUVERS ABSOLUTELY NEED ATTITUDE OVERRIDES ARGUMENTS !)
        // Attitude providers
        final LofOffset lofTNW = new LofOffset(EME2000, LOFType.TNW);
        final CelestialBodyPointed bodyPointed = new CelestialBodyPointed(CelestialBodyFactory.getEarth()
                                                                                              .getBodyOrientedFrame(),
                CelestialBodyFactory.getSun(), Vector3D.PLUS_J, Vector3D.PLUS_I, Vector3D.PLUS_K);

        // Firing dates
        final AbsoluteDate firingDateLOF = new AbsoluteDate(2024, 3, 15, 2, 0, 0.0, clock.getTimeScale());
        final double       duration      = 3600;

        //// Attitude sequence to modelize the maneuver
        final AttitudesSequence sequence = new AttitudesSequence();

        // Event detector for the attitude sequence
        final EventDetector detectorFiringDate = new DateDetector(firingDateLOF).withHandler(new ContinueOnEvent());
        final EventDetector detectorStopFiringDate = new DateDetector(firingDateLOF.shiftedBy(duration)).withHandler(
                new ContinueOnEvent());

        final EventDetector secondFiringDate = new DateDetector(startDate.shiftedBy(3 * 3600.0)).withHandler(
                new ContinueOnEvent());
        final EventDetector secondStopFiringDate = new DateDetector(startDate.shiftedBy(4 * 3600)).withHandler(
                new ContinueOnEvent());

        // Switches for attitude sequence
        sequence.addSwitchingCondition(bodyPointed, lofTNW, detectorFiringDate, true, false, 200.0,
                AngularDerivativesFilter.USE_R, null);
        sequence.addSwitchingCondition(lofTNW, bodyPointed, detectorStopFiringDate, true, false, 200.0,
                AngularDerivativesFilter.USE_R, null);

        sequence.addSwitchingCondition(bodyPointed, lofTNW, secondFiringDate, true, false, 200.0,
                AngularDerivativesFilter.USE_R, null);
        sequence.addSwitchingCondition(lofTNW, bodyPointed, secondStopFiringDate, true, false, 200.0,
                AngularDerivativesFilter.USE_R, null);

        sequence.resetActiveProvider(bodyPointed);

        propagator.setAttitudeProvider(sequence);

        sequence.registerSwitchEvents(propagator);

        // Trigger for the maneuver
        final ManeuverTriggers firstTriggers = new DateBasedManeuverTriggers(firingDateLOF, duration);
        final ManeuverTriggers secondTriggers = new DateBasedManeuverTriggers(startDate.shiftedBy(3 * 3600.0),
                duration);

        // Propulsion model
        final double   thrust                = 400;
        final double   isp                   = 380;
        final Vector3D accelerationDirection = Vector3D.PLUS_I;
        final PropulsionModel firstPropulsionModel = new BasicConstantThrustPropulsionModel(thrust, isp,
                accelerationDirection, "first thrust");
        final PropulsionModel secondPropulsionModel = new BasicConstantThrustPropulsionModel(thrust, isp,
                accelerationDirection, "second thrust");


        // Maneuver
        final Maneuver firstManeuver  = new Maneuver(sequence, firstTriggers, firstPropulsionModel);
        final Maneuver secondManeuver = new Maneuver(sequence, secondTriggers, secondPropulsionModel);
        maneuvers.add(firstManeuver);
        maneuvers.add(secondManeuver);

        // Setup propagator
        propagator.setOrbitType(OrbitType.CARTESIAN);
        propagator.addForceModel(holmesFeatherstone);
        propagator.addForceModel(firstManeuver);
        propagator.addForceModel(secondManeuver);
        propagator.setInitialState(initialState);

        final EphemerisGenerator generator = propagator.getEphemerisGenerator();

        propagator.propagate(startDate, finalDate);
        final BoundedPropagator boundedPropagator = generator.getGeneratedEphemeris();

        // Build of the satellite
        final Spacecraft satellite = Spacecraft.builder(boundedPropagator, header)
                                               .withModelPath(IssModel)
                                               .withReferenceSystem()
                                               .withDisplayAttitude()
                                               .build();

        final Orientation orientationSatelliteToStock = satellite.getOrientation();
        final double      periodSatelliteToStock      = satellite.getPeriod();

        // Creation of the display of the maneuvers
        final ManeuverSequence maneuverSequence = ManeuverSequence.builder(sequence, maneuvers, satellite,
                                                                          accelerationDirection, LOFType.TNW, header)
                                                                  .build();

        // Multiple Ground Stations
        // Creation of a topocentric frame around Toulouse.
        final GeodeticPoint toulouseFrame = new GeodeticPoint(FastMath.toRadians(43.6047), FastMath.toRadians(1.4442),
                10);
        final TopocentricFrame topocentricToulouse = new TopocentricFrame(earth, toulouseFrame, "Toulouse Frame");

        // Creation of another topocentric frame around Las Vegas.
        final GeodeticPoint lasVegasFrame = new GeodeticPoint(FastMath.toRadians(36.1716),
                FastMath.toRadians(-115.1391), 10);
        final TopocentricFrame topocentricLasVegas = new TopocentricFrame(earth, lasVegasFrame, "Las Vegas Frame");

        // Creation of all the ground stations
        final List<CzmlGroundStation> groundStation         = new ArrayList<>();
        final CzmlGroundStation       groundStationToulouse = new CzmlGroundStation(topocentricToulouse, header);
        final CzmlGroundStation       groundStationLasVegas = new CzmlGroundStation(topocentricLasVegas, header);
        groundStation.add(groundStationToulouse);
        groundStation.add(groundStationLasVegas);
        final List<TopocentricFrame> topocentrics = new ArrayList<>();
        topocentrics.add(topocentricToulouse);
        topocentrics.add(topocentricLasVegas);
        final CzmlGroundStation groundStationCoverageList = CzmlGroundStation.builder(topocentrics, header)
                                                                             .build();
        final CzmlGroundStation soloGroundStation = new CzmlGroundStation(topocentricToulouse, header);

        groundStationCoverageList.getTopocentricFrames();
        groundStationCoverageList.getBillboards();
        groundStationCoverageList.getAvailabilities();
        groundStationCoverageList.getNames();
        groundStationCoverageList.getIds();
        groundStationCoverageList.getPositionsObjects();
        groundStationCoverageList.getPositionsOnEarth();

        soloGroundStation.getTopocentricFrame();
        soloGroundStation.getPositionsObjects();
        soloGroundStation.getPositionsOnEarth();

        final List<ManeuverSequence> sequences = new ArrayList<>();
        sequences.add(maneuverSequence);

        final List<BoundedPropagator> propagators = new ArrayList<>();
        propagators.add(boundedPropagator);
        final Constellation constellation = Constellation.builder(propagators, finalDate, header).build();

        final LineOfVisibility lineToulouse = LineOfVisibility.builder(topocentricToulouse, satellite,
                                                                      header)
                                                              .build();
        final LineOfVisibility lineVegasAperture = LineOfVisibility.builder(topocentricLasVegas, satellite,
                                                                           header)
                                                                   .withAngleOfAperture(90.0)
                                                                   .build();

        final LineOfVisibility lineVegasConstellation = LineOfVisibility.builder(topocentricLasVegas,
                                                                                constellation, header)
                                                                        .build();
        final LineOfVisibility lineVegasConstellationAperture = LineOfVisibility.builder(topocentricLasVegas,
                                                                                        constellation, header)
                                                                                .withAngleOfAperture(90.0)
                                                                                .build();

        final LineOfVisibility lineTopocentric = LineOfVisibility.builder(topocentrics, satellite, header)
                                                                 .build();

        final LineOfVisibility lineTopocentricAperture = LineOfVisibility.builder(topocentrics, satellite, header)
                                                                         .withAngleOfAperture(90.0)
                                                                         .build();

        final LineOfVisibility lineTopocentricConstellation = LineOfVisibility.builder(topocentrics, constellation,
                                                                                      header)
                                                                              .build();

        final LineOfVisibility lineTopocentricConstellationAperture = LineOfVisibility.builder(topocentrics,
                                                                                              constellation, header)
                                                                                      .withAngleOfAperture(90.0)
                                                                                      .build();

        final CzmlFile file = CzmlFile.builder()
                                      .withHeader(header)
                                      .withSpacecraft(satellite)
                                      .withManeuverSequence(maneuverSequence)
                                      .withCzmlGroundStation(groundStation)
                                      .withLineOfVisibility(lineToulouse)
                                      .build();

        final CzmlFile coverageFile = CzmlFile.builder()
                                              .withHeader(header)
                                              .withManeuverSequence(sequences)
                                              .withLineOfVisibility(lineVegasAperture)
                                              .withLineOfVisibility(lineVegasConstellation)
                                              .withLineOfVisibility(lineVegasConstellationAperture)
                                              .withLineOfVisibility(lineTopocentric)
                                              .withLineOfVisibility(lineTopocentricAperture)
                                              .withLineOfVisibility(lineTopocentricConstellation)
                                              .withLineOfVisibility(lineTopocentricConstellationAperture)
                                              .build();

        // Writing the file
        file.write(output);

        // Comparing of generated file and reference file.
        final String stringReference = Files.readString(Path.of(referenceFilePath3));
        final String stringGenerated = Files.readString(Path.of(output));

        final String coveragePathFile = loadResources("templateFile/file/Test3CzmlFileTemplateCoverage.txt");

        Assertions.assertEquals(stringReference, stringGenerated);
        Assertions.assertEquals(Files.readString(Path.of(coveragePathFile)), coverageFile.toString());
    }
}
