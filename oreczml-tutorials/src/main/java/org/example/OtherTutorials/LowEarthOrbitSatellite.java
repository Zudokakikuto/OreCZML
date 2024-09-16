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
package org.example.OtherTutorials;

import org.example.TutorialUtils;
import org.hipparchus.ode.nonstiff.AdaptiveStepsizeIntegrator;
import org.hipparchus.ode.nonstiff.DormandPrince853Integrator;
import org.hipparchus.util.FastMath;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.Header;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.Satellite;
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

public class LowEarthOrbitSatellite {

    private LowEarthOrbitSatellite() {
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

        // Creation of the clock
        final TimeScale    UTC                    = TimeScalesFactory.getUTC();
        final double       durationOfSimulation   = 5 * 3600; // in seconds;
        final double       stepBetweenEachInstant = 60.0; // in seconds
        final AbsoluteDate startDate              = new AbsoluteDate(2024, 3, 15, 0, 0, 0.0, UTC);
        final AbsoluteDate finalDate              = startDate.shiftedBy(durationOfSimulation);
        final Clock        clock                  = new Clock(startDate, finalDate, UTC, stepBetweenEachInstant);

        // Creation of the header
        final Header header = new Header("Low Earth Orbit Tutorial", clock, pathToJSFolder);

        // Build of the LEO orbit
        final Frame EME2000 = FramesFactory.getEME2000();
        final KeplerianOrbit initialOrbit = new KeplerianOrbit(7878000, 0, FastMath.toRadians(10), 0,
                FastMath.toRadians(90), FastMath.toRadians(0), PositionAngleType.MEAN, EME2000, startDate,
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

        final EphemerisGenerator generator = propagator.getEphemerisGenerator();

        propagator.setOrbitType(OrbitType.CARTESIAN);
        propagator.addForceModel(holmesFeatherstone);
        propagator.setInitialState(initialState);

        propagator.propagate(startDate, finalDate);
        final BoundedPropagator boundedPropagator = generator.getGeneratedEphemeris();

        // Build the LEO Satellite
        final Satellite leoSatellite = new Satellite(boundedPropagator);

        // Creation of the file
        final CzmlFile file = new CzmlFileBuilder(output).withHeader(header)
                                                         .withSatellite(leoSatellite)
                                                         .build();

        // Write inside the CzmlFile the objects
        file.write();
    }
}
