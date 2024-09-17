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
import org.hipparchus.geometry.euclidean.threed.RotationOrder;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.hipparchus.ode.nonstiff.AdaptiveStepsizeIntegrator;
import org.hipparchus.ode.nonstiff.DormandPrince853Integrator;
import org.hipparchus.util.FastMath;
import org.orekit.attitudes.LofOffset;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.czml.object.primary.CzmlGroundStation;
import org.orekit.czml.object.primary.FieldOfObservation;
import org.orekit.czml.object.primary.Header;
import org.orekit.czml.object.primary.Satellite;
import org.orekit.czml.object.secondary.Clock;
import org.orekit.czml.file.CzmlFile;
import org.orekit.czml.file.CzmlFileBuilder;
import org.orekit.forces.ForceModel;
import org.orekit.forces.gravity.HolmesFeatherstoneAttractionModel;
import org.orekit.forces.gravity.potential.GravityFieldFactory;
import org.orekit.forces.gravity.potential.NormalizedSphericalHarmonicsProvider;
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
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.numerical.NumericalPropagator;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScale;
import org.orekit.time.TimeScalesFactory;
import org.orekit.utils.Constants;
import org.orekit.utils.IERSConventions;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import static org.example.TutorialUtils.loadResources;

public class Demonstrator {

    private Demonstrator() {
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

        final String IssModel = loadResources("Default3DModels/ISSModel.glb");

        // Creation of the clock.
        final TimeScale    UTC                    = TimeScalesFactory.getUTC();
        final double       durationOfSimulation   = 10 * 3600; // in seconds;
        final double       stepBetweenEachInstant = 60.0; // in seconds
        final AbsoluteDate startDate              = new AbsoluteDate(2024, 3, 15, 0, 0, 0.0, UTC);
        final AbsoluteDate finalDate              = startDate.shiftedBy(durationOfSimulation);
        final Clock        clock                  = new Clock(startDate, finalDate, UTC, stepBetweenEachInstant);

        final Header header = new Header("OtherTutorials.Demonstrator", clock, pathToJSFolder);

        // Creation of the model of the earth.
        final IERSConventions IERS = IERSConventions.IERS_2010;
        final Frame           ITRF = FramesFactory.getITRF(IERS, true);
        final OneAxisEllipsoid earth = new OneAxisEllipsoid(Constants.WGS84_EARTH_EQUATORIAL_RADIUS,
                Constants.WGS84_EARTH_FLATTENING, ITRF);

        // Creation of a topocentric frame around Toulouse.
        final GeodeticPoint toulouseFrame = new GeodeticPoint(FastMath.toRadians(43.6047),
                FastMath.toRadians(1.4442), 10);
        final TopocentricFrame topocentricToulouse = new TopocentricFrame(earth, toulouseFrame, "Toulouse");

        // Creation of another topocentric frame around Las Vegas.
        final GeodeticPoint lasVegasFrame = new GeodeticPoint(FastMath.toRadians(36.1716),
                FastMath.toRadians(-115.1391), 10);
        final TopocentricFrame topocentricLasVegas = new TopocentricFrame(earth, lasVegasFrame, "Las Vegas");

        // Creation of another topocentric frame around .
        final GeodeticPoint kirunaFrame = new GeodeticPoint(FastMath.toRadians(67.8558),
                FastMath.toRadians(20.2253), 10);
        final TopocentricFrame topocentricKiruna = new TopocentricFrame(earth, kirunaFrame, "Kiruna");

        // Creation of another topocentric frame around Troll.
        final GeodeticPoint trollFrame = new GeodeticPoint(FastMath.toRadians(-72.006),
                FastMath.toRadians(2.529), 10);
        final TopocentricFrame topocentricTroll = new TopocentricFrame(earth, trollFrame, "Troll");

        // Creation of a list of topocentric frame containing both frames.
        final List<TopocentricFrame> allStations = new ArrayList<>();
        allStations.add(topocentricToulouse);
        allStations.add(topocentricLasVegas);
        allStations.add(topocentricKiruna);
        allStations.add(topocentricTroll);

        //// Build of a satellite with a propagator
        // Build of a LEO orbit
        final Frame EME2000 = FramesFactory.getEME2000();
        final KeplerianOrbit initialOrbit = new KeplerianOrbit(7878000, 0, FastMath.toRadians(98), 0,
                FastMath.toRadians(0), FastMath.toRadians(0), PositionAngleType.MEAN, EME2000, startDate,
                Constants.WGS84_EARTH_MU);

        final SpacecraftState initialState = new SpacecraftState(initialOrbit);

        // Build of the propagator
        final double positionTolerance = 10.0;
        final double minStep           = 0.001;
        final double maxStep           = 1000.0;

        final double[][] tolerances = NumericalPropagator.tolerances(positionTolerance, initialOrbit,
                OrbitType.CARTESIAN);
        final AdaptiveStepsizeIntegrator integrator = new DormandPrince853Integrator(minStep, maxStep, tolerances[0],
                tolerances[1]);

        final NumericalPropagator propagator = new NumericalPropagator(integrator);

        final NormalizedSphericalHarmonicsProvider provider = GravityFieldFactory.getNormalizedProvider(10,
                10);
        final ForceModel holmesFeatherstone = new HolmesFeatherstoneAttractionModel(EME2000,
                provider);

        propagator.setOrbitType(OrbitType.CARTESIAN);
        propagator.addForceModel(holmesFeatherstone);
        propagator.setInitialState(initialState);

        final EphemerisGenerator generator = propagator.getEphemerisGenerator();

        final LofOffset lofOffset = new LofOffset(EME2000, LOFType.TNW, RotationOrder.XYZ, FastMath.toRadians(0),
                FastMath.toRadians(0), FastMath.toRadians(0));
        propagator.setAttitudeProvider(lofOffset);

        propagator.propagate(startDate, finalDate);
        final BoundedPropagator boundedPropagator = generator.getGeneratedEphemeris();

        // Creation of the satellite
        final Satellite satellite = Satellite.builder(boundedPropagator)
                                             .withModelPath(IssModel)
                                             .withColor(Color.RED)
                                             .withOnlyOnePeriod()
                                             .withDisplayAttitude()
                                             .build();

        // Build of the ground stations
        final List<CzmlGroundStation> allGroundStations = new ArrayList<>();
        for (TopocentricFrame allStation : allStations) {
            allGroundStations.add(new CzmlGroundStation(allStation));
        }

        // Creation of the field of observation of the satellite, it describes the area the satellite see
        final Transform initialInertToBody = initialState.getFrame()
                                                         .getTransformTo(earth.getBodyFrame(), initialState.getDate());
        final Transform initialFovBody = new Transform(initialState.getDate(), initialState.toTransform()
                                                                                           .getInverse(),
                initialInertToBody);
        // A circular field of view
        //final FieldOfView fov = new CircularFieldOfView(Vector3D.PLUS_J, FastMath.toRadians(50), 2);

        // A rectangular field of view
        final FieldOfView fov = new DoubleDihedraFieldOfView(Vector3D.PLUS_J, Vector3D.PLUS_I, FastMath.toRadians(20),
                Vector3D.PLUS_K, FastMath.toRadians(5), 2);
        final FieldOfObservation fieldOfObservation = FieldOfObservation.builder(satellite, fov, initialFovBody)
                                                                        .withColor(Color.PINK)
                                                                        .build();

        // Creation of the file
        final CzmlFile file = CzmlFile.builder(output).withHeader(header)
                                                         .withSatellite(satellite)
                                                         .withCzmlGroundStation(allGroundStations)
                                                         .withFieldOfObservation(fieldOfObservation)
                                                         .withLineOfVisibility(allStations, satellite)
                                                         .build();

        // Write inside the CzmlFile the objects
        file.write();
    }
}
