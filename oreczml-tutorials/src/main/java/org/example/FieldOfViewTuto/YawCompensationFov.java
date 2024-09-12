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
package org.example.FieldOfViewTuto;

import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.hipparchus.ode.nonstiff.AdaptiveStepsizeIntegrator;
import org.hipparchus.ode.nonstiff.DormandPrince853Integrator;
import org.hipparchus.util.FastMath;
import org.orekit.attitudes.NadirPointing;
import org.orekit.attitudes.YawCompensation;
import org.orekit.attitudes.YawSteering;
import org.orekit.bodies.CelestialBodyFactory;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.czml.ArchiObjects.Builders.SatelliteBuilder;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.FieldOfObservation;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.Header;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.Satellite;
import org.orekit.czml.CzmlObjects.CzmlSecondaryObjects.Clock;
import org.orekit.czml.Outputs.CzmlFile;
import org.orekit.czml.Outputs.CzmlFileBuilder;
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
import org.orekit.utils.PVCoordinatesProvider;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class YawCompensationFov {

    private YawCompensationFov() {
        // empty
    }

    public static void main(final String[] args) throws Exception {
        try {
            final File         home      = new File(System.getProperty("user.home"));
            final File         orekitDir = new File(home, "orekit-data");
            final DataProvider provider  = new DirectoryCrawler(orekitDir);
            DataContext.getDefault()
                       .getDataProvidersManager()
                       .addProvider(provider);
        } catch (OrekitException oe) {
            System.err.println(oe.getLocalizedMessage());
        }

        // Paths
        final String root = System.getProperty("user.dir")
                                  .replace("\\", "/");
        final String outputPath = root + "/Output";
        final String outputName = "Output.czml";
        final String output     = outputPath + "/" + outputName;

        // Creation of the clock.
        final TimeScale    UTC                    = TimeScalesFactory.getUTC();
        final double       durationOfSimulation   = 10 * 3600; // in seconds;
        final double       stepBetweenEachInstant = 60.0; // in seconds
        final AbsoluteDate startDate              = new AbsoluteDate(2024, 3, 15, 0, 0, 0.0, UTC);
        final AbsoluteDate finalDate              = startDate.shiftedBy(durationOfSimulation);
        final Clock        clock                  = new Clock(startDate, finalDate, UTC, stepBetweenEachInstant);

        final Header header = new Header("Yaw compensation with 3 satellites with the same fov", clock);

        // Creation of the model of the earth.
        final IERSConventions IERS = IERSConventions.IERS_2010;
        final Frame           ITRF = FramesFactory.getITRF(IERS, true);
        final OneAxisEllipsoid earth = new OneAxisEllipsoid(Constants.WGS84_EARTH_EQUATORIAL_RADIUS,
                Constants.WGS84_EARTH_FLATTENING, ITRF);

        // Creation of the model of the sun
        final PVCoordinatesProvider sunModel = CelestialBodyFactory.getSun();

        // Build of a LEO orbit
        final Frame EME2000 = FramesFactory.getEME2000();
        final KeplerianOrbit firstOrbit = new KeplerianOrbit(7200000, 0, FastMath.toRadians(98), 0,
                FastMath.toRadians(0), FastMath.toRadians(0), PositionAngleType.MEAN, EME2000, startDate,
                Constants.WGS84_EARTH_MU);
        final KeplerianOrbit secondOrbit = new KeplerianOrbit(7200000, 0, FastMath.toRadians(98), 0,
                FastMath.toRadians(20), FastMath.toRadians(0), PositionAngleType.MEAN, EME2000, startDate,
                Constants.WGS84_EARTH_MU);
        final KeplerianOrbit thirdOrbit = new KeplerianOrbit(7200000, 0, FastMath.toRadians(98), 0,
                FastMath.toRadians(0), FastMath.toRadians(40), PositionAngleType.MEAN, EME2000, startDate,
                Constants.WGS84_EARTH_MU);

        final SpacecraftState firstState  = new SpacecraftState(firstOrbit);
        final SpacecraftState secondState = new SpacecraftState(secondOrbit);
        final SpacecraftState thirdState  = new SpacecraftState(thirdOrbit);

        // Build of the 3 bounded propagators
        final double positionTolerance = 10.0;
        final double minStep           = 0.001;
        final double maxStep           = 1000;

        final double[][] tolerances1 = NumericalPropagator.tolerances(positionTolerance, firstOrbit,
                OrbitType.CARTESIAN);
        final double[][] tolerances2 = NumericalPropagator.tolerances(positionTolerance, secondOrbit,
                OrbitType.CARTESIAN);
        final double[][] tolerances3 = NumericalPropagator.tolerances(positionTolerance, thirdOrbit,
                OrbitType.CARTESIAN);

        final AdaptiveStepsizeIntegrator integrator1 = new DormandPrince853Integrator(minStep, maxStep, tolerances1[0],
                tolerances1[1]);
        final AdaptiveStepsizeIntegrator integrator2 = new DormandPrince853Integrator(minStep, maxStep, tolerances2[0],
                tolerances2[1]);
        final AdaptiveStepsizeIntegrator integrator3 = new DormandPrince853Integrator(minStep, maxStep, tolerances3[0],
                tolerances3[1]);

        final NumericalPropagator propagator1 = new NumericalPropagator(integrator1);
        final NumericalPropagator propagator2 = new NumericalPropagator(integrator2);
        final NumericalPropagator propagator3 = new NumericalPropagator(integrator3);

        final NormalizedSphericalHarmonicsProvider provider = GravityFieldFactory.getNormalizedProvider(10,
                10);
        final ForceModel holmesFeatherstone = new HolmesFeatherstoneAttractionModel(EME2000,
                provider);

        propagator1.setOrbitType(OrbitType.CARTESIAN);
        propagator1.addForceModel(holmesFeatherstone);
        propagator1.setInitialState(firstState);

        propagator2.setOrbitType(OrbitType.CARTESIAN);
        propagator2.addForceModel(holmesFeatherstone);
        propagator2.setInitialState(secondState);

        propagator3.setOrbitType(OrbitType.CARTESIAN);
        propagator3.addForceModel(holmesFeatherstone);
        propagator3.setInitialState(thirdState);

        final EphemerisGenerator generator1 = propagator1.getEphemerisGenerator();
        final EphemerisGenerator generator2 = propagator2.getEphemerisGenerator();
        final EphemerisGenerator generator3 = propagator3.getEphemerisGenerator();

        final NadirPointing nadirPointing = new NadirPointing(EME2000, earth);

        final YawCompensation yawCompensation = new YawCompensation(EME2000, nadirPointing);

        final YawSteering yawSteering = new YawSteering(EME2000, nadirPointing, sunModel, Vector3D.PLUS_J);

        propagator1.setAttitudeProvider(nadirPointing);
        propagator2.setAttitudeProvider(yawCompensation);
        propagator3.setAttitudeProvider(yawSteering);

        propagator1.propagate(startDate, finalDate);
        propagator2.propagate(startDate, finalDate);
        propagator3.propagate(startDate, finalDate);

        final BoundedPropagator boundedPropagator1 = generator1.getGeneratedEphemeris();
        final BoundedPropagator boundedPropagator2 = generator2.getGeneratedEphemeris();
        final BoundedPropagator boundedPropagator3 = generator3.getGeneratedEphemeris();

        // Build of satellites
        final List<Satellite> satellites = new ArrayList<>();
        final Satellite satellite1 = new SatelliteBuilder(boundedPropagator1).withColor(Color.PINK)
                                                                             .withOnlyOnePeriod()
                                                                             .withDisplayAttitude()
                                                                             .withReferenceSystem()
                                                                             .build();

        final Satellite satellite2 = new SatelliteBuilder(boundedPropagator2).withColor(Color.BLUE)
                                                                             .withOnlyOnePeriod()
                                                                             .withDisplayAttitude()
                                                                             .withReferenceSystem()
                                                                             .build();

        final Satellite satellite3 = new SatelliteBuilder(boundedPropagator3).withColor(Color.WHITE)
                                                                             .withOnlyOnePeriod()
                                                                             .withDisplayAttitude()
                                                                             .withReferenceSystem()
                                                                             .build();
        satellites.add(satellite1);
        satellites.add(satellite2);
        satellites.add(satellite3);

        // Creation of the field of observation of the satellite, it describes the area the satellite see
        final List<FieldOfObservation> fobs = new ArrayList<>();

        final Transform initialInertToBody1 = firstState.getFrame()
                                                        .getTransformTo(earth.getBodyFrame(), firstState.getDate());

        final Transform initialFovBody1 = new Transform(firstState.getDate(), firstState.toTransform()
                                                                                        .getInverse(),
                initialInertToBody1);

        final FieldOfView fov1 = new DoubleDihedraFieldOfView(Vector3D.PLUS_K, Vector3D.PLUS_I, FastMath.toRadians(20),
                Vector3D.PLUS_J, FastMath.toRadians(5), 2);
        final FieldOfObservation fieldOfObservation1 = FieldOfObservation.builder(satellite1, fov1, initialFovBody1)
                                                                         .withColor(Color.GREEN)
                                                                         .build();

        final Transform initialInertToBody2 = secondState.getFrame()
                                                         .getTransformTo(earth.getBodyFrame(), secondState.getDate());

        final Transform initialFovBody2 = new Transform(secondState.getDate(), secondState.toTransform()
                                                                                          .getInverse(),
                initialInertToBody2);

        final FieldOfView fov2 = new DoubleDihedraFieldOfView(Vector3D.PLUS_K, Vector3D.PLUS_I, FastMath.toRadians(20),
                Vector3D.PLUS_J, FastMath.toRadians(5), 2);
        final FieldOfObservation fieldOfObservation2 = FieldOfObservation.builder(satellite2, fov2, initialFovBody2)
                                                                         .withColor(Color.GREEN)
                                                                         .build();

        final Transform initialInertToBody3 = thirdState.getFrame()
                                                        .getTransformTo(earth.getBodyFrame(), thirdState.getDate());

        final Transform initialFovBody3 = new Transform(thirdState.getDate(), thirdState.toTransform()
                                                                                        .getInverse(),
                initialInertToBody3);

        final FieldOfView fov3 = new DoubleDihedraFieldOfView(Vector3D.PLUS_K, Vector3D.PLUS_I, FastMath.toRadians(20),
                Vector3D.PLUS_J, FastMath.toRadians(5), 2);
        final FieldOfObservation fieldOfObservation3 = FieldOfObservation.builder(satellite3, fov3, initialFovBody3)
                                                                         .withColor(Color.GREEN)
                                                                         .build();

        fobs.add(fieldOfObservation1);
        fobs.add(fieldOfObservation2);
        fobs.add(fieldOfObservation3);

        // Creation of the file
        final CzmlFile file = new CzmlFileBuilder(output).withHeader(header)
                                                         .withSatellite(satellites)
                                                         .withFieldOfObservation(fobs)
                                                         .build();

        // Writing the file
        file.write();
    }
}
