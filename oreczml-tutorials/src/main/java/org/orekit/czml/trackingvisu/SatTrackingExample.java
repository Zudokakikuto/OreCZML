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
package org.orekit.czml.trackingvisu;

import org.orekit.czml.TutorialUtils;
import org.hipparchus.ode.nonstiff.AdaptiveStepsizeIntegrator;
import org.hipparchus.ode.nonstiff.DormandPrince853Integrator;
import org.hipparchus.util.FastMath;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.czml.file.CzmlFile;
import org.orekit.czml.object.primary.CzmlGroundStation;
import org.orekit.czml.object.primary.Header;
import org.orekit.czml.object.primary.Satellite;
import org.orekit.czml.object.secondary.Clock;
import org.orekit.forces.ForceModel;
import org.orekit.forces.gravity.HolmesFeatherstoneAttractionModel;
import org.orekit.forces.gravity.potential.GravityFieldFactory;
import org.orekit.forces.gravity.potential.NormalizedSphericalHarmonicsProvider;
import org.orekit.frames.FramesFactory;
import org.orekit.frames.TopocentricFrame;
import org.orekit.orbits.KeplerianOrbit;
import org.orekit.orbits.OrbitType;
import org.orekit.orbits.PositionAngleType;
import org.orekit.propagation.BoundedPropagator;
import org.orekit.propagation.EphemerisGenerator;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.numerical.NumericalPropagator;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScalesFactory;
import org.orekit.utils.Constants;
import org.orekit.utils.IERSConventions;

import java.util.ArrayList;
import java.util.List;

/**
 * This tutorial shows an example of mission where a satellite is tracked by several stations.
 */
public class SatTrackingExample {

    private SatTrackingExample () {
        // empty
    }

    /**
     * Main of sat tracking tutorial.
     *
     * @param args the args
     * @throws Exception the exception
     */
    public static void main (final String[] args) throws Exception {
        // Load orekit data
        TutorialUtils.loadOrekitData();

        // Paths
        final String output = TutorialUtils.generateOutput();
        // !!! Here you need to change the path inside 'generateJsPath' to the path you are using for images or Model.
        // This folder can also be the public folder of your cesium javascript interface.
        final String pathToJSFolder = TutorialUtils.generateJSPath(
                System.getProperty("user.dir") + "/Javascript/public");

        // Creation of the clock.


        final AbsoluteDate startDate = new AbsoluteDate(2024, 3, 15, 0, 0, 0.0, TimeScalesFactory.getUTC());
        final AbsoluteDate finalDate = startDate.shiftedBy(TutorialUtils.CLASSIC_DURATION_OF_SIMULATION);
        final Clock clock = new Clock(startDate, finalDate, TimeScalesFactory.getUTC(),
                TutorialUtils.STEP_BETWEEN_EACH_INSTANT);

        // Creation of the header
        final Header header = new Header("Tracking of a satellite by several stations", clock, pathToJSFolder);

        //// Creation of the satellite
        // Creation of the orbit

        final KeplerianOrbit initialOrbit = new KeplerianOrbit(7878000, 0, FastMath.toRadians(20), 0,
                FastMath.toRadians(90), FastMath.toRadians(0), PositionAngleType.MEAN, FramesFactory.getEME2000(), startDate,
                Constants.WGS84_EARTH_MU);

        final SpacecraftState initialState = new SpacecraftState(initialOrbit);

        // Build of the propagator


        final double[][] tolerances = NumericalPropagator.tolerances(TutorialUtils.POSITION_TOLERANCE, initialOrbit,
                OrbitType.CARTESIAN);
        final AdaptiveStepsizeIntegrator integrator = new DormandPrince853Integrator(TutorialUtils.MIN_STEP,
                TutorialUtils.MAX_STEP, tolerances[0],
                tolerances[1]);

        final NumericalPropagator propagator = new NumericalPropagator(integrator);

        final NormalizedSphericalHarmonicsProvider provider = GravityFieldFactory.getNormalizedProvider(10,
                10);
        final ForceModel holmesFeatherstone = new HolmesFeatherstoneAttractionModel(FramesFactory.getITRF(
                IERSConventions.IERS_2010, true),
                provider);

        propagator.setOrbitType(OrbitType.CARTESIAN);
        propagator.addForceModel(holmesFeatherstone);
        propagator.setInitialState(initialState);

        final EphemerisGenerator generator = propagator.getEphemerisGenerator();

        propagator.propagate(startDate, finalDate);
        final BoundedPropagator boundedPropagator = generator.getGeneratedEphemeris();

        // Build of the satellite
        final Satellite satellite = Satellite.builder(boundedPropagator, header)
                                             .withOnlyOnePeriod()
                                             .build();

        //// Creation of several ground stations


        // Creation of a topocentric frame around Toulouse.
        final GeodeticPoint toulouseFrame = new GeodeticPoint(FastMath.toRadians(43.6047),
                FastMath.toRadians(1.4442), 10);
        final TopocentricFrame topocentricToulouse = new TopocentricFrame(TutorialUtils.getEarth(), toulouseFrame,
                "Toulouse");
        // Creation of a topocentric frame around Quito
        final GeodeticPoint quitoFrame = new GeodeticPoint(FastMath.toRadians(0.1807),
                FastMath.toRadians(11.5382), 2850);
        final TopocentricFrame topocentricQuito = new TopocentricFrame(TutorialUtils.getEarth(), quitoFrame, "Quito");
        // Creation of a topocentric frame around Sydney
        final GeodeticPoint sydneyFrame = new GeodeticPoint(FastMath.toRadians(-33.8688),
                FastMath.toRadians(-241.2093), 100);
        final TopocentricFrame topocentricSydney = new TopocentricFrame(TutorialUtils.getEarth(), sydneyFrame, "Sydney");
        // Creation of a topocentric frame around gibraltar
        final GeodeticPoint gibraltarFrame = new GeodeticPoint(FastMath.toRadians(36.1408),
                FastMath.toRadians(5.3536), 400);
        final TopocentricFrame topocentricGibraltar = new TopocentricFrame(TutorialUtils.getEarth(), gibraltarFrame,
                "Gibraltar");
        // Creation of another topocentric frame around Las Vegas.
        final GeodeticPoint lasVegasFrame = new GeodeticPoint(FastMath.toRadians(36.1716),
                FastMath.toRadians(-115.1391), 10);
        final TopocentricFrame topocentricLasVegas = new TopocentricFrame(TutorialUtils.getEarth(), lasVegasFrame,
                "Las Vegas");

        // Creation of a list of topocentric frame containing both frames.
        final List<TopocentricFrame> stations = new ArrayList<>();
        stations.add(topocentricToulouse);
        stations.add(topocentricSydney);
        stations.add(topocentricQuito);
        stations.add(topocentricGibraltar);
        stations.add(topocentricLasVegas);

        final List<CzmlGroundStation> groundStations = new ArrayList<>();
        for (TopocentricFrame station : stations) {
            groundStations.add(new CzmlGroundStation(station, header));
        }

        //// Creation of a line of visu between the satellite and all the ground stations
        final CzmlFile file = CzmlFile.builder()
                                      .withHeader(header)
                                      .withSatellite(satellite)
                                      .withCzmlGroundStation(groundStations)
                                      .withLineOfVisibility(stations, satellite, header)
                                      .build();

        // Write inside the CzmlFile the objects
        file.write(output);
    }
}

