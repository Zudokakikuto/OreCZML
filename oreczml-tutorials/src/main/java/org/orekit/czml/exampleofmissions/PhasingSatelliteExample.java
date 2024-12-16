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

package org.orekit.czml.exampleofmissions;

import org.orekit.czml.TutorialUtils;
import org.hipparchus.ode.nonstiff.AdaptiveStepsizeIntegrator;
import org.hipparchus.ode.nonstiff.DormandPrince853Integrator;
import org.hipparchus.util.FastMath;
import org.orekit.czml.file.CzmlFile;
import org.orekit.czml.object.primary.Header;
import org.orekit.czml.object.primary.entities.Spacecraft;
import org.orekit.czml.object.secondary.Clock;
import org.orekit.forces.ForceModel;
import org.orekit.forces.gravity.HolmesFeatherstoneAttractionModel;
import org.orekit.forces.gravity.potential.GravityFieldFactory;
import org.orekit.forces.gravity.potential.NormalizedSphericalHarmonicsProvider;
import org.orekit.frames.FramesFactory;
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

import java.awt.Color;

/**
 * An example of a mission where two satellites are phasing.
 */
public class PhasingSatelliteExample {

    private PhasingSatelliteExample() {
        // empty
    }

    /**
     * Main of the phasing satellite tutorial.
     *
     * @param args arguments of the main function
     * @throws Exception exception to throw
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

        // Creation of the clock

        final double       durationOfSimulation = 22 * 3600 + 37 * 60 + 30; // in seconds;
        final AbsoluteDate startDate            = new AbsoluteDate(2020, 1, 1, 0, 0, 0.0, TimeScalesFactory.getUTC());
        final AbsoluteDate finalDate            = startDate.shiftedBy(durationOfSimulation);
        final Clock clock = new Clock(startDate, finalDate, TimeScalesFactory.getUTC(),
                TutorialUtils.STEP_BETWEEN_EACH_INSTANT);

        final Header header = new Header("Two satellites phasing", clock,
                pathToJSFolder);


        // Build of two satellites, one phasing the other.

        final KeplerianOrbit phasedOrbit = new KeplerianOrbit(45000000, 0, FastMath.toRadians(0), FastMath.toRadians(0),
                FastMath.toRadians(80), FastMath.toRadians(0), PositionAngleType.MEAN, FramesFactory.getEME2000(),
                startDate,
                Constants.WGS84_EARTH_MU);

        final KeplerianOrbit phasingOrbit = new KeplerianOrbit(46000000, 0, FastMath.toRadians(0),
                FastMath.toRadians(0), FastMath.toRadians(90), FastMath.toRadians(0), PositionAngleType.MEAN,
                FramesFactory.getEME2000(), startDate, Constants.WGS84_EARTH_MU);

        final SpacecraftState phasedState = new SpacecraftState(phasedOrbit);
        final SpacecraftState phasingtate = new SpacecraftState(phasingOrbit);

        // Build of the propagator

        final double[][] tolerancesPhased = NumericalPropagator.tolerances(TutorialUtils.POSITION_TOLERANCE,
                phasedOrbit,
                OrbitType.CARTESIAN);
        final double[][] tolerancesPhasing = NumericalPropagator.tolerances(TutorialUtils.POSITION_TOLERANCE,
                phasingOrbit,
                OrbitType.CARTESIAN);

        final AdaptiveStepsizeIntegrator integratorPhased = new DormandPrince853Integrator(TutorialUtils.MIN_STEP,
                TutorialUtils.MAX_STEP, tolerancesPhased[0], tolerancesPhased[1]);
        final AdaptiveStepsizeIntegrator integratorPhasing = new DormandPrince853Integrator(TutorialUtils.MIN_STEP,
                TutorialUtils.MAX_STEP, tolerancesPhasing[0], tolerancesPhasing[1]);

        final NumericalPropagator propagatorPhased  = new NumericalPropagator(integratorPhased);
        final NumericalPropagator propagatorPhasing = new NumericalPropagator(integratorPhased);

        final NormalizedSphericalHarmonicsProvider provider = GravityFieldFactory.getNormalizedProvider(10,
                10);
        final ForceModel holmesFeatherstone = new HolmesFeatherstoneAttractionModel(
                FramesFactory.getEME2000(), provider);

        propagatorPhased.setOrbitType(OrbitType.CARTESIAN);
        propagatorPhased.addForceModel(holmesFeatherstone);
        propagatorPhased.setInitialState(phasedState);

        propagatorPhasing.setOrbitType(OrbitType.CARTESIAN);
        propagatorPhasing.addForceModel(holmesFeatherstone);
        propagatorPhasing.setInitialState(phasingtate);

        final EphemerisGenerator generatorPhased  = propagatorPhased.getEphemerisGenerator();
        final EphemerisGenerator generatorPhasing = propagatorPhasing.getEphemerisGenerator();

        propagatorPhased.propagate(startDate, finalDate);
        propagatorPhasing.propagate(startDate, finalDate);

        final BoundedPropagator boundedPropagatorPhased  = generatorPhased.getGeneratedEphemeris();
        final BoundedPropagator boundedPropagatorPhasing = generatorPhasing.getGeneratedEphemeris();

        // Creation of the two satellites
        final Spacecraft satellitePhased = Spacecraft.builder(boundedPropagatorPhased, header)
                                                     .withColor(Color.RED)
                                                     .withOnlyOnePeriod()
                                                     .withDisplayAttitude()
                                                     .build();

        final Spacecraft satellitePhasing = Spacecraft.builder(boundedPropagatorPhasing, header)
                                                      .withColor(Color.GREEN)
                                                      .withOnlyOnePeriod()
                                                      .withDisplayAttitude()
                                                      .build();

        final CzmlFile file = CzmlFile.builder()
                                      .withHeader(header)
                                      .withSatellite(satellitePhased)
                                      .withSatellite(satellitePhasing)
                                      .build();

        file.write(output);
    }
}
