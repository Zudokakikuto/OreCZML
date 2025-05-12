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
import org.orekit.czml.object.primary.Header;
import org.orekit.czml.object.primary.entities.Constellation;
import org.orekit.czml.object.primary.entities.CzmlGroundStation;
import org.orekit.czml.object.primary.visu.LineOfVisibility;
import org.orekit.czml.object.secondary.Clock;
import org.orekit.forces.ForceModel;
import org.orekit.forces.gravity.HolmesFeatherstoneAttractionModel;
import org.orekit.forces.gravity.potential.GravityFieldFactory;
import org.orekit.forces.gravity.potential.NormalizedSphericalHarmonicsProvider;
import org.orekit.frames.FramesFactory;
import org.orekit.frames.TopocentricFrame;
import org.orekit.orbits.KeplerianOrbit;
import org.orekit.orbits.Orbit;
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
 * This tutorial provides an example of how lines of visibility between ground
 * stations and satellites of a constellation can be set up.
 */
public class ConstellationVisuExample {

    private ConstellationVisuExample() {
        // empty
    }

    /**
     * Main of the constellation visu tutorial.
     *
     * @param args the args
     * @throws Exception the exception
     */
    public static void main(final String[] args)
        throws Exception {
        // Load orekit data
        TutorialUtils.loadOrekitData();

        // Paths
        final String output = TutorialUtils.generateOutput();
        // !!! Here you need to change the path inside 'generateJsPath' to the
        // path you are using for images or Model.
        // This folder can also be the public folder of your cesium javascript
        // interface.
        final String pathToJSFolder =
            TutorialUtils.generateJSPath(System.getProperty("user.dir") +
                                         "/Javascript/public");

        // Creation of the clock

        final double durationOfSimulation = 8 * 3600; // in seconds;
        final AbsoluteDate startDate =
            new AbsoluteDate(2024, 3, 15, 0, 0, 0.0,
                             TimeScalesFactory.getUTC());
        final AbsoluteDate finalDate =
            startDate.shiftedBy(durationOfSimulation);
        final Clock clock =
            new Clock(startDate, finalDate, TimeScalesFactory.getUTC(),
                      TutorialUtils.STEP_BETWEEN_EACH_INSTANT);

        // Creation of the header
        final Header header =
            new Header("Visualisation of a constellation by multiple ground station",
                       clock, pathToJSFolder);

        //// Creation of the constellation
        // Build of propagators

        final NormalizedSphericalHarmonicsProvider provider =
            GravityFieldFactory.getNormalizedProvider(10, 10);
        final ForceModel holmesFeatherstone =
            new HolmesFeatherstoneAttractionModel(FramesFactory
                .getITRF(IERSConventions.IERS_2010, true), provider);

        final List<BoundedPropagator> propagators = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            final Orbit currentOrbit =
                new KeplerianOrbit(10878000, 0, FastMath.toRadians(i * 10), 0,
                                   FastMath.toRadians(90 * FastMath.pow(-1, i)),
                                   FastMath.toRadians(0),
                                   PositionAngleType.MEAN,
                                   FramesFactory.getEME2000(), startDate,
                                   Constants.WGS84_EARTH_MU);
            final SpacecraftState state = new SpacecraftState(currentOrbit);
            final double[][] currentTolerance =
                NumericalPropagator.tolerances(TutorialUtils.POSITION_TOLERANCE,
                                               currentOrbit,
                                               OrbitType.CARTESIAN);
            final AdaptiveStepsizeIntegrator currentIntegrator =
                new DormandPrince853Integrator(TutorialUtils.MIN_STEP,
                                               TutorialUtils.MAX_STEP,
                                               currentTolerance[0],
                                               currentTolerance[1]);
            final NumericalPropagator propagator =
                new NumericalPropagator(currentIntegrator);
            propagator.setOrbitType(OrbitType.CARTESIAN);
            propagator.addForceModel(holmesFeatherstone);
            propagator.setInitialState(state);
            final EphemerisGenerator generator =
                propagator.getEphemerisGenerator();
            propagator.propagate(startDate, finalDate);
            final BoundedPropagator boundedPropagator =
                generator.getGeneratedEphemeris();
            propagators.add(boundedPropagator);
        }

        // Build of the constellation
        final Constellation constellation =
            Constellation.builder(propagators, finalDate, header).build();

        //// Creation of the ground station

        // Creation of a topocentric frame around Toulouse.
        final GeodeticPoint toulouseFrame =
            new GeodeticPoint(FastMath.toRadians(43.6047),
                              FastMath.toRadians(1.4442), 10);
        final TopocentricFrame topocentricToulouse =
            new TopocentricFrame(TutorialUtils.getEarth(), toulouseFrame,
                                 "Toulouse Frame");

        // Creation of another topocentric frame around Las Vegas.
        final GeodeticPoint lasVegasFrame =
            new GeodeticPoint(FastMath.toRadians(36.1716),
                              FastMath.toRadians(-115.1391), 10);
        final TopocentricFrame topocentricLasVegas =
            new TopocentricFrame(TutorialUtils.getEarth(), lasVegasFrame,
                                 "Las Vegas Frame");

        // Creation of a list of topocentric frame containing both frames.
        final List<TopocentricFrame> stations = new ArrayList<>();
        stations.add(topocentricToulouse);
        stations.add(topocentricLasVegas);

        // Build of the ground stations
        final List<CzmlGroundStation> groundStation = new ArrayList<>();
        for (TopocentricFrame station : stations) {
            groundStation.add(new CzmlGroundStation(station, header));
        }

        final LineOfVisibility lineOfVisibilityToulouse =
            LineOfVisibility.builder(topocentricToulouse, constellation, header)
                .withVisibilityTriangle().build();
        final LineOfVisibility lineOfVisibilityLasVegas =
            LineOfVisibility.builder(topocentricLasVegas, constellation, header)
                .withVisibilityTriangle().build();

        final CzmlFile file =
            CzmlFile.builder().withHeader(header)
                .withConstellation(constellation)
                .withCzmlGroundStation(groundStation)
                .withLineOfVisibility(lineOfVisibilityToulouse)
                .withLineOfVisibility(lineOfVisibilityLasVegas).build();

        // Writing in the file
        file.write(output);
    }
}
