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
package org.example.InterVisu;

import org.example.TutorialUtils;
import org.hipparchus.ode.nonstiff.AdaptiveStepsizeIntegrator;
import org.hipparchus.ode.nonstiff.DormandPrince853Integrator;
import org.hipparchus.util.FastMath;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.Constellation;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.Header;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.InterSatVisu;
import org.orekit.czml.CzmlObjects.CzmlSecondaryObjects.Clock;
import org.orekit.czml.Outputs.CzmlFile;
import org.orekit.czml.Outputs.CzmlFileBuilder;
import org.orekit.forces.ForceModel;
import org.orekit.forces.gravity.HolmesFeatherstoneAttractionModel;
import org.orekit.forces.gravity.potential.GravityFieldFactory;
import org.orekit.forces.gravity.potential.NormalizedSphericalHarmonicsProvider;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
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

import java.util.ArrayList;
import java.util.List;

public class InterVisuConstellationExample {

    private InterVisuConstellationExample() {
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

        // Creation of the clock.
        final TimeScale    UTC                    = TimeScalesFactory.getUTC();
        final double       durationOfSimulation   = 32 * 3600; // in seconds;
        final double       stepBetweenEachInstant = 60.0; // in seconds
        final AbsoluteDate startDate              = new AbsoluteDate(2024, 3, 15, 0, 0, 0.0, UTC);
        final AbsoluteDate finalDate              = startDate.shiftedBy(durationOfSimulation);
        final Clock        clock                  = new Clock(startDate, finalDate, UTC, stepBetweenEachInstant);

        // Build of the Header
        final Header header = new Header("Setup of the visualisation inter-constellation of 5 satellites", clock,
                pathToJSFolder);

        // List of propagator
        final List<BoundedPropagator> allPropagators = new ArrayList<>();

        // Built the orbit
        final Frame EME2000 = FramesFactory.getEME2000();
        final KeplerianOrbit firstOrbit = new KeplerianOrbit(7878000, 0, FastMath.toRadians(20), 0,
                FastMath.toRadians(0), FastMath.toRadians(0), PositionAngleType.MEAN, EME2000, startDate,
                Constants.WGS84_EARTH_MU);
        final KeplerianOrbit secondOrbit = new KeplerianOrbit(8578000, 0, FastMath.toRadians(0), 0,
                FastMath.toRadians(0), FastMath.toRadians(0), PositionAngleType.MEAN, EME2000, startDate,
                Constants.WGS84_EARTH_MU);
        final KeplerianOrbit thirdOrbit = new KeplerianOrbit(6578000, 0, FastMath.toRadians(-20), 0,
                FastMath.toRadians(0), FastMath.toRadians(0), PositionAngleType.MEAN, EME2000, startDate,
                Constants.WGS84_EARTH_MU);
        final KeplerianOrbit fourthOrbit = new KeplerianOrbit(10578000, 0, FastMath.toRadians(0), 0,
                FastMath.toRadians(0), FastMath.toRadians(0), PositionAngleType.MEAN, EME2000, startDate,
                Constants.WGS84_EARTH_MU);
        final KeplerianOrbit fifthOrbit = new KeplerianOrbit(78578000, 0, FastMath.toRadians(98), 0,
                FastMath.toRadians(0), FastMath.toRadians(0), PositionAngleType.MEAN, EME2000, startDate,
                Constants.WGS84_EARTH_MU);

        final SpacecraftState firstState  = new SpacecraftState(firstOrbit);
        final SpacecraftState secondState = new SpacecraftState(secondOrbit);
        final SpacecraftState thirdState  = new SpacecraftState(thirdOrbit);
        final SpacecraftState fourthState = new SpacecraftState(fourthOrbit);
        final SpacecraftState fifthState  = new SpacecraftState(fifthOrbit);

        // Build of the propagator
        final double positionTolerance = 10.0;
        final double minStep           = 0.001;
        final double maxStep           = 1000;

        final double[][] tolerances1 = NumericalPropagator.tolerances(positionTolerance, firstOrbit,
                OrbitType.CARTESIAN);
        final double[][] tolerances2 = NumericalPropagator.tolerances(positionTolerance, secondOrbit,
                OrbitType.CARTESIAN);
        final double[][] tolerances3 = NumericalPropagator.tolerances(positionTolerance, thirdOrbit,
                OrbitType.CARTESIAN);
        final double[][] tolerances4 = NumericalPropagator.tolerances(positionTolerance, fourthOrbit,
                OrbitType.CARTESIAN);
        final double[][] tolerances5 = NumericalPropagator.tolerances(positionTolerance, fifthOrbit,
                OrbitType.CARTESIAN);

        final AdaptiveStepsizeIntegrator firstIntegrator = new DormandPrince853Integrator(minStep, maxStep,
                tolerances1[0], tolerances1[1]);
        final AdaptiveStepsizeIntegrator secondIntegrator = new DormandPrince853Integrator(minStep, maxStep,
                tolerances2[0], tolerances2[1]);
        final AdaptiveStepsizeIntegrator thirdIntegrator = new DormandPrince853Integrator(minStep, maxStep,
                tolerances3[0], tolerances3[1]);
        final AdaptiveStepsizeIntegrator fourthIntegrator = new DormandPrince853Integrator(minStep, maxStep,
                tolerances4[0], tolerances4[1]);
        final AdaptiveStepsizeIntegrator fifthIntegrator = new DormandPrince853Integrator(minStep, maxStep,
                tolerances5[0], tolerances5[1]);

        final NumericalPropagator firstPropagator  = new NumericalPropagator(firstIntegrator);
        final NumericalPropagator secondPropagator = new NumericalPropagator(secondIntegrator);
        final NumericalPropagator thirdPropagator  = new NumericalPropagator(thirdIntegrator);
        final NumericalPropagator fourthPropagator = new NumericalPropagator(fourthIntegrator);
        final NumericalPropagator fifthPropagator  = new NumericalPropagator(fifthIntegrator);

        final NormalizedSphericalHarmonicsProvider provider = GravityFieldFactory.getNormalizedProvider(10,
                10);
        final ForceModel holmesFeatherstone = new HolmesFeatherstoneAttractionModel(EME2000,
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

        allPropagators.add(firstBoundedPropagator);
        allPropagators.add(secondBoundedPropagator);
        allPropagators.add(thirdBoundedPropagator);
        allPropagators.add(fourthBoundedPropagator);
        allPropagators.add(fifthBoundedPropagator);

        final Constellation constellation = new Constellation(allPropagators, finalDate);
        constellation.displayOnlyOnePeriod();

        // Creation of the inter-sat visualization
        final InterSatVisu interSatVisu = new InterSatVisu(constellation, finalDate);

        // Creation of the file
        final CzmlFile file = new CzmlFileBuilder(output).withHeader(header)
                                                         .withConstellation(constellation)
                                                         .withInterSatVisu(interSatVisu)
                                                         .build();

        // Writing in the file
        file.write();
    }
}
