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
package org.orekit.czml.intervisu;

import org.orekit.czml.TutorialUtils;
import org.hipparchus.ode.nonstiff.AdaptiveStepsizeIntegrator;
import org.hipparchus.ode.nonstiff.DormandPrince853Integrator;
import org.hipparchus.util.FastMath;
import org.orekit.czml.file.CzmlFile;
import org.orekit.czml.object.primary.Constellation;
import org.orekit.czml.object.primary.Header;
import org.orekit.czml.object.primary.visu.InterSatVisu;
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
import org.orekit.utils.IERSConventions;

import java.util.ArrayList;
import java.util.List;

/**
 * This tutorial provides an example of how to set up inter-visualization between satellites of the same constellation.
 */
public class InterVisuConstellationExample {

    private InterVisuConstellationExample () {
        // empty
    }

    /**
     * Main of the inter visu constellation tutorial.
     *
     * @param args arguments of the main function
     * @throws Exception exception to throw
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

        final double       durationOfSimulation = 32 * 3600; // in seconds;
        final AbsoluteDate startDate            = new AbsoluteDate(2024, 3, 15, 0, 0, 0.0, TimeScalesFactory.getUTC());
        final AbsoluteDate finalDate            = startDate.shiftedBy(durationOfSimulation);
        final Clock clock = new Clock(startDate, finalDate, TimeScalesFactory.getUTC(),
                TutorialUtils.STEP_BETWEEN_EACH_INSTANT);

        // Build of the Header
        final Header header = new Header("Setup of the visualisation inter-constellation of 5 satellites", clock,
                pathToJSFolder);

        // List of propagator
        final List<BoundedPropagator> propagators = new ArrayList<>();

        // Built the orbit

        final KeplerianOrbit firstOrbit = new KeplerianOrbit(7878000, 0, FastMath.toRadians(20), 0,
                FastMath.toRadians(0), FastMath.toRadians(0), PositionAngleType.MEAN, FramesFactory.getEME2000(), startDate,
                Constants.WGS84_EARTH_MU);
        final KeplerianOrbit secondOrbit = new KeplerianOrbit(8578000, 0, FastMath.toRadians(0), 0,
                FastMath.toRadians(0), FastMath.toRadians(0), PositionAngleType.MEAN, FramesFactory.getEME2000(), startDate,
                Constants.WGS84_EARTH_MU);
        final KeplerianOrbit thirdOrbit = new KeplerianOrbit(6578000, 0, FastMath.toRadians(-20), 0,
                FastMath.toRadians(0), FastMath.toRadians(0), PositionAngleType.MEAN, FramesFactory.getEME2000(), startDate,
                Constants.WGS84_EARTH_MU);
        final KeplerianOrbit fourthOrbit = new KeplerianOrbit(10578000, 0, FastMath.toRadians(0), 0,
                FastMath.toRadians(0), FastMath.toRadians(0), PositionAngleType.MEAN, FramesFactory.getEME2000(), startDate,
                Constants.WGS84_EARTH_MU);
        final KeplerianOrbit fifthOrbit = new KeplerianOrbit(78578000, 0, FastMath.toRadians(98), 0,
                FastMath.toRadians(0), FastMath.toRadians(0), PositionAngleType.MEAN, FramesFactory.getEME2000(), startDate,
                Constants.WGS84_EARTH_MU);

        final SpacecraftState firstState  = new SpacecraftState(firstOrbit);
        final SpacecraftState secondState = new SpacecraftState(secondOrbit);
        final SpacecraftState thirdState  = new SpacecraftState(thirdOrbit);
        final SpacecraftState fourthState = new SpacecraftState(fourthOrbit);
        final SpacecraftState fifthState  = new SpacecraftState(fifthOrbit);

        // Build of the propagator


        final double[][] tolerances1 = NumericalPropagator.tolerances(TutorialUtils.POSITION_TOLERANCE, firstOrbit,
                OrbitType.CARTESIAN);
        final double[][] tolerances2 = NumericalPropagator.tolerances(TutorialUtils.POSITION_TOLERANCE, secondOrbit,
                OrbitType.CARTESIAN);
        final double[][] tolerances3 = NumericalPropagator.tolerances(TutorialUtils.POSITION_TOLERANCE, thirdOrbit,
                OrbitType.CARTESIAN);
        final double[][] tolerances4 = NumericalPropagator.tolerances(TutorialUtils.POSITION_TOLERANCE, fourthOrbit,
                OrbitType.CARTESIAN);
        final double[][] tolerances5 = NumericalPropagator.tolerances(TutorialUtils.POSITION_TOLERANCE, fifthOrbit,
                OrbitType.CARTESIAN);

        final AdaptiveStepsizeIntegrator firstIntegrator = new DormandPrince853Integrator(TutorialUtils.MIN_STEP,
                TutorialUtils.MAX_STEP,
                tolerances1[0], tolerances1[1]);
        final AdaptiveStepsizeIntegrator secondIntegrator = new DormandPrince853Integrator(TutorialUtils.MIN_STEP,
                TutorialUtils.MAX_STEP,
                tolerances2[0], tolerances2[1]);
        final AdaptiveStepsizeIntegrator thirdIntegrator = new DormandPrince853Integrator(TutorialUtils.MIN_STEP,
                TutorialUtils.MAX_STEP,
                tolerances3[0], tolerances3[1]);
        final AdaptiveStepsizeIntegrator fourthIntegrator = new DormandPrince853Integrator(TutorialUtils.MIN_STEP,
                TutorialUtils.MAX_STEP,
                tolerances4[0], tolerances4[1]);
        final AdaptiveStepsizeIntegrator fifthIntegrator = new DormandPrince853Integrator(TutorialUtils.MIN_STEP,
                TutorialUtils.MAX_STEP,
                tolerances5[0], tolerances5[1]);

        final NumericalPropagator firstPropagator  = new NumericalPropagator(firstIntegrator);
        final NumericalPropagator secondPropagator = new NumericalPropagator(secondIntegrator);
        final NumericalPropagator thirdPropagator  = new NumericalPropagator(thirdIntegrator);
        final NumericalPropagator fourthPropagator = new NumericalPropagator(fourthIntegrator);
        final NumericalPropagator fifthPropagator  = new NumericalPropagator(fifthIntegrator);

        final NormalizedSphericalHarmonicsProvider provider = GravityFieldFactory.getNormalizedProvider(10,
                10);
        final ForceModel holmesFeatherstone = new HolmesFeatherstoneAttractionModel(FramesFactory.getITRF(
                IERSConventions.IERS_2010, true),
                provider);

        firstPropagator.setOrbitType(OrbitType.CARTESIAN);
        firstPropagator.addForceModel(holmesFeatherstone);
        firstPropagator.setInitialState(firstState);
        final EphemerisGenerator firstGenerator = firstPropagator.getEphemerisGenerator();
        firstPropagator.propagate(startDate, finalDate);
        final BoundedPropagator firstBoundedPropagator = firstGenerator.getGeneratedEphemeris();

        secondPropagator.setOrbitType(OrbitType.CARTESIAN);
        secondPropagator.addForceModel(holmesFeatherstone);
        secondPropagator.setInitialState(secondState);
        final EphemerisGenerator secondGenerator = secondPropagator.getEphemerisGenerator();
        secondPropagator.propagate(startDate, finalDate);
        final BoundedPropagator secondBoundedPropagator = secondGenerator.getGeneratedEphemeris();

        thirdPropagator.setOrbitType(OrbitType.CARTESIAN);
        thirdPropagator.addForceModel(holmesFeatherstone);
        thirdPropagator.setInitialState(thirdState);
        final EphemerisGenerator thirdGenerator = thirdPropagator.getEphemerisGenerator();
        thirdPropagator.propagate(startDate, finalDate);
        final BoundedPropagator thirdBoundedPropagator = thirdGenerator.getGeneratedEphemeris();

        fourthPropagator.setOrbitType(OrbitType.CARTESIAN);
        fourthPropagator.addForceModel(holmesFeatherstone);
        fourthPropagator.setInitialState(fourthState);
        final EphemerisGenerator fourthGenerator = fourthPropagator.getEphemerisGenerator();
        fourthPropagator.propagate(startDate, finalDate);
        final BoundedPropagator fourthBoundedPropagator = fourthGenerator.getGeneratedEphemeris();

        fifthPropagator.setOrbitType(OrbitType.CARTESIAN);
        fifthPropagator.addForceModel(holmesFeatherstone);
        fifthPropagator.setInitialState(fifthState);
        final EphemerisGenerator fifthGenerator = fifthPropagator.getEphemerisGenerator();
        fifthPropagator.propagate(startDate, finalDate);
        final BoundedPropagator fifthBoundedPropagator = fifthGenerator.getGeneratedEphemeris();

        propagators.add(firstBoundedPropagator);
        propagators.add(secondBoundedPropagator);
        propagators.add(thirdBoundedPropagator);
        propagators.add(fourthBoundedPropagator);
        propagators.add(fifthBoundedPropagator);

        final Constellation constellation = new Constellation(propagators, finalDate, header);
        constellation.displayOnlyOnePeriod();

        // Creation of the inter-sat visualization
        final InterSatVisu interSatVisu = new InterSatVisu(constellation, finalDate, header);

        // Creation of the file
        final CzmlFile file = CzmlFile.builder()
                                      .withHeader(header)
                                      .withConstellation(constellation)
                                      .withInterSatVisu(interSatVisu)
                                      .build();

        // Writing in the file
        file.write(output);
    }
}
