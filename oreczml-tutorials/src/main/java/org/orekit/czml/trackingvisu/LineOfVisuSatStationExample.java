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

import org.hipparchus.ode.nonstiff.AdaptiveStepsizeIntegrator;
import org.hipparchus.ode.nonstiff.DormandPrince853Integrator;
import org.hipparchus.util.FastMath;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.czml.TutorialUtils;
import org.orekit.czml.file.CzmlFile;
import org.orekit.czml.object.primary.entities.CzmlGroundStation;
import org.orekit.czml.object.primary.Header;
import org.orekit.czml.object.primary.entities.Satellite;
import org.orekit.czml.object.primary.visu.LineOfVisibility;
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

/**
 * This tutorial provides an example of how a line of visu between a satellite and a ground station.
 */
public class LineOfVisuSatStationExample {

    private LineOfVisuSatStationExample() {
        // empty
    }

    /**
     * Main of the line of visu sat station tutorial.
     *
     * @param args the args
     * @throws Exception the exception
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

        final String groundStationModel = TutorialUtils.loadResources("Default3DModels/ground_Station.glb");

        // Creation of the clock.


        final AbsoluteDate startDate = new AbsoluteDate(2024, 3, 15, 0, 0, 0.0, TimeScalesFactory.getUTC());
        final AbsoluteDate finalDate = startDate.shiftedBy(TutorialUtils.CLASSIC_DURATION_OF_SIMULATION);
        final Clock clock = new Clock(startDate, finalDate, TimeScalesFactory.getUTC(),
                TutorialUtils.STEP_BETWEEN_EACH_INSTANT);

        final Header header = new Header("Line of visibility between a satellite and a station", clock, pathToJSFolder);


        // Creation of a topocentric frame around Toulouse.
        final GeodeticPoint toulouseFrame = new GeodeticPoint(FastMath.toRadians(43.6047),
                FastMath.toRadians(1.4442), 0);
        final TopocentricFrame topocentricToulouse = new TopocentricFrame(TutorialUtils.getEarth(), toulouseFrame,
                "Toulouse Frame");


        // Build of an orbit that will fly above toulouse

        final KeplerianOrbit initialOrbit = new KeplerianOrbit(7878000, 0, FastMath.toRadians(80), 0,
                FastMath.toRadians(90), FastMath.toRadians(0), PositionAngleType.MEAN, FramesFactory.getEME2000(),
                startDate,
                Constants.WGS84_EARTH_MU);

        final SpacecraftState initialState = new SpacecraftState(initialOrbit);


        final NormalizedSphericalHarmonicsProvider provider = GravityFieldFactory.getNormalizedProvider(10,
                10);
        final ForceModel holmesFeatherstone = new HolmesFeatherstoneAttractionModel(FramesFactory.getITRF(
                IERSConventions.IERS_2010, true),
                provider);

        final double[][] tolerances = NumericalPropagator.tolerances(TutorialUtils.POSITION_TOLERANCE, initialOrbit,
                OrbitType.CARTESIAN);
        final AdaptiveStepsizeIntegrator integrator = new DormandPrince853Integrator(TutorialUtils.MIN_STEP,
                TutorialUtils.MAX_STEP, tolerances[0],
                tolerances[1]);

        final NumericalPropagator propagator = new NumericalPropagator(integrator);

        propagator.setOrbitType(OrbitType.CARTESIAN);
        propagator.addForceModel(holmesFeatherstone);
        propagator.setInitialState(initialState);

        final EphemerisGenerator generator = propagator.getEphemerisGenerator();

        propagator.propagate(startDate, finalDate);
        final BoundedPropagator boundedPropagator = generator.getGeneratedEphemeris();

        // Creation of a ground Station at Toulouse
        final CzmlGroundStation toulouseStation = new CzmlGroundStation(topocentricToulouse, groundStationModel,
                header);

        // Creation of the satellite
        final Satellite satellite = Satellite.builder(boundedPropagator, header)
                                             .withOnlyOnePeriod()
                                             .build();

        final LineOfVisibility lineOfVisibility = LineOfVisibility.builder(topocentricToulouse, satellite, header)
                                                                  .withVisibilityTriangle()
                                                                  .build();

        final CzmlFile file = CzmlFile.builder().
                                      withHeader(header).
                                      withCzmlGroundStation(toulouseStation).
                                      withSatellite(satellite).
                                      withLineOfVisibility(lineOfVisibility).
                                      build();

        // Writing in the file
        file.write(output);
    }
}
