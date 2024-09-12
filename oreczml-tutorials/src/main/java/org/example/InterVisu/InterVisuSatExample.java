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

import org.hipparchus.ode.nonstiff.AdaptiveStepsizeIntegrator;
import org.hipparchus.ode.nonstiff.DormandPrince853Integrator;
import org.hipparchus.util.FastMath;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.Header;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.InterSatVisu;
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

import java.awt.Color;
import java.io.File;

public class InterVisuSatExample {

    private InterVisuSatExample() {
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
        final double       durationOfSimulation   = 32 * 3600; // in seconds;
        final double       stepBetweenEachInstant = 60.0; // in seconds
        final AbsoluteDate startDate              = new AbsoluteDate(2024, 3, 15, 0, 0, 0.0, UTC);
        final AbsoluteDate finalDate              = startDate.shiftedBy(durationOfSimulation);
        final Clock        clock                  = new Clock(startDate, finalDate, UTC, stepBetweenEachInstant);

        final Header header = new Header("Setup of the visualisation inter-satellites", clock);

        //// Build of the first satellite with an orbit with 20 degree inclination
        // Build of a LEO orbit
        final Frame EME2000 = FramesFactory.getEME2000();
        final KeplerianOrbit firstOrbit = new KeplerianOrbit(7878000, 0, FastMath.toRadians(0), FastMath.toRadians(0),
                FastMath.toRadians(0), FastMath.toRadians(0), PositionAngleType.MEAN, EME2000, startDate,
                Constants.WGS84_EARTH_MU);
        final SpacecraftState firstState = new SpacecraftState(firstOrbit);

        //// Build of the first satellite with an orbit with 40 degree inclination and omega of 90 degrees
        // Build of a LEO orbit
        final KeplerianOrbit secondOrbit = new KeplerianOrbit(7078000, 0, FastMath.toRadians(20),
                FastMath.toRadians(45), FastMath.toRadians(0), FastMath.toRadians(0), PositionAngleType.MEAN, EME2000,
                startDate, Constants.WGS84_EARTH_MU);
        final SpacecraftState secondState = new SpacecraftState(secondOrbit);

        // Build of the propagator
        final double positionTolerance = 10.0;
        final double minStep           = 0.001;
        final double maxStep           = 1000;

        final double[][] tolerances1 = NumericalPropagator.tolerances(positionTolerance, firstOrbit,
                OrbitType.CARTESIAN);
        final double[][] tolerances2 = NumericalPropagator.tolerances(positionTolerance, secondOrbit,
                OrbitType.CARTESIAN);
        final AdaptiveStepsizeIntegrator integrator1 = new DormandPrince853Integrator(minStep, maxStep, tolerances1[0],
                tolerances1[1]);
        final AdaptiveStepsizeIntegrator integrator2 = new DormandPrince853Integrator(minStep, maxStep, tolerances2[0],
                tolerances2[1]);

        final NormalizedSphericalHarmonicsProvider provider = GravityFieldFactory.getNormalizedProvider(10,
                10);
        final ForceModel holmesFeatherstone = new HolmesFeatherstoneAttractionModel(EME2000,
                provider);

        final NumericalPropagator firstPropagator  = new NumericalPropagator(integrator1);
        final NumericalPropagator secondPropagator = new NumericalPropagator(integrator2);

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

        // Creation of the satellites
        final Satellite firstSatellite = Satellite.builder(firstBoundedPropagator)
                                                  .withColor(Color.MAGENTA)
                                                  .withOnlyOnePeriod()
                                                  .build();

        final Satellite secondSatellite = Satellite.builder(secondBoundedPropagator)
                                                   .withColor(Color.GREEN)
                                                   .withOnlyOnePeriod()
                                                   .build();

        // Creation of the inter-sat visualisation
        final InterSatVisu interSatVisu = new InterSatVisu(firstSatellite, secondSatellite, finalDate);

        // Creation of the file
        final CzmlFile file = new CzmlFileBuilder(output).withHeader(header)
                                                         .withSatellite(firstSatellite)
                                                         .withSatellite(secondSatellite)
                                                         .withInterSatVisu(interSatVisu)
                                                         .build();
        // Writing in the file
        file.write();
    }
}
