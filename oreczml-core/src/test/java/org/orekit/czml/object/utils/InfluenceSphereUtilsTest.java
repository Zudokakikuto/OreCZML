/* Copyright 2002-2025 CS GROUP
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
package org.orekit.czml.object.utils;

import org.hipparchus.ode.nonstiff.AdaptiveStepsizeIntegrator;
import org.hipparchus.ode.nonstiff.DormandPrince853Integrator;
import org.hipparchus.util.FastMath;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.orekit.bodies.CelestialBodyFactory;
import org.orekit.czml.archi.factory.BodyFactory;
import org.orekit.czml.file.AbstractTest;
import org.orekit.czml.file.CzmlFile;
import org.orekit.czml.object.primary.Header;
import org.orekit.czml.object.primary.entities.Body;
import org.orekit.czml.object.primary.entities.Spacecraft;
import org.orekit.czml.object.secondary.Clock;
import org.orekit.forces.ForceModel;
import org.orekit.forces.gravity.HolmesFeatherstoneAttractionModel;
import org.orekit.forces.gravity.SingleBodyAbsoluteAttraction;
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
import org.orekit.time.TimeScalesFactory;
import org.orekit.utils.AbsolutePVCoordinates;
import org.orekit.utils.Constants;
import org.orekit.utils.IERSConventions;

import java.awt.Color;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class InfluenceSphereUtilsTest
    extends
    AbstractTest {

    /** Initialise orekit data. */
    private final double data = initializeOrekitData();

    @Test
    void findCrossingSphereDatesTest()
        throws URISyntaxException,
            IOException {

        final String output = generateOutput();

        // Creation of the clock.

        final double durationOfSimulation = 4 * 24 * 3600.0; // in seconds;
        final Frame eme2000 = FramesFactory.getEME2000();
        final Frame itrf =
            FramesFactory.getITRF(IERSConventions.IERS_2010, true);
        final AbsoluteDate startDate =
            new AbsoluteDate(2024, 1, 16, 0, 0, 0.0,
                             TimeScalesFactory.getUTC());
        final AbsoluteDate finalDate =
            startDate.shiftedBy(durationOfSimulation);
        final Clock clock = new Clock(startDate, finalDate, 3600.0);

        final Header header =
            new Header("Example of usage of the influence sphere on the moon and the earth",
                       clock);

        // Influence sphere
        final Body earth = BodyFactory.getEarth(clock);
        earth.displayInfluenceSphere();
        final Body moon = BodyFactory.getMoon(clock);
        moon.displayInfluenceSphere();
        moon.displayOnlyOnePeriod(24 * 3600.0);
        final List<Body> bodies = new ArrayList<>();
        bodies.add(earth);
        bodies.add(moon);

        // Satellite 390900000
        final KeplerianOrbit initialOrbit =
            new KeplerianOrbit(740900000, 0.9, FastMath.toRadians(200),
                               FastMath.toRadians(200), FastMath.toRadians(0),
                               FastMath.toRadians(0), PositionAngleType.MEAN,
                               FramesFactory.getEME2000(), startDate,
                               Constants.WGS84_EARTH_MU);

        final AbsolutePVCoordinates absolutePVCoordinates =
            new AbsolutePVCoordinates(eme2000, initialOrbit.getPVCoordinates());

        final SpacecraftState initialState =
            new SpacecraftState(absolutePVCoordinates);
        final double[][] tolerances =
            NumericalPropagator.tolerances(10.0, initialOrbit,
                                           OrbitType.CARTESIAN);
        final AdaptiveStepsizeIntegrator integrator =
            new DormandPrince853Integrator(0.001, 1000.0, tolerances[0],
                                           tolerances[1]);
        final NumericalPropagator propagator =
            new NumericalPropagator(integrator);

        propagator.setIgnoreCentralAttraction(true);

        final ForceModel singleBodyEarth =
            new SingleBodyAbsoluteAttraction(CelestialBodyFactory.getEarth());
        final ForceModel singleBodyMoon =
            new SingleBodyAbsoluteAttraction(CelestialBodyFactory.getMoon());

        final NormalizedSphericalHarmonicsProvider provider =
            GravityFieldFactory.getNormalizedProvider(10, 10);
        final ForceModel holmesFeatherstoneEarth =
            new HolmesFeatherstoneAttractionModel(itrf, provider);

        propagator.setInitialState(initialState);
        propagator.addForceModel(singleBodyEarth);
        propagator.addForceModel(holmesFeatherstoneEarth);
        propagator.addForceModel(singleBodyMoon);
        propagator.setOrbitType(null);
        final EphemerisGenerator firstGenerator =
            propagator.getEphemerisGenerator();

        propagator.propagate(startDate, finalDate);
        final BoundedPropagator boundedPropagator =
            firstGenerator.getGeneratedEphemeris();

        // Satellite
        final Spacecraft spacecraft =
            Spacecraft.builder(boundedPropagator, clock).withReferenceSystem()
                .withColor(Color.BLUE)
                .displayInfluenceSphereChanges(bodies, earth).build();
        // Czml file
        final CzmlFile file =
            CzmlFile.builder(header).withSpacecraft(spacecraft).withBody(earth)
                .withBody(moon).build();

        // file writing
        file.write(output);

        final List<SpacecraftState> states = spacecraft.getSpaceCraftStates();
        final Frame spacecraftFrame = spacecraft.getFrame();

        final List<AbsoluteDate> dateChanges =
            InfluenceSphereUtils.findCrossingSphereDates(bodies, itrf, states,
                                                         spacecraftFrame,
                                                         finalDate);
        Assertions.assertEquals(4, dateChanges.size());
        final List<AbsoluteDate> datesRef = new ArrayList<>();
        datesRef.add(new AbsoluteDate(2024, 1, 16, 0, 0, 0.0,
                                      TimeScalesFactory.getUTC()));
        datesRef.add(new AbsoluteDate(2024, 1, 17, 16, 0, 0.0,
                                      TimeScalesFactory.getUTC()));
        datesRef.add(new AbsoluteDate(2024, 1, 19, 8, 0, 0.0,
                                      TimeScalesFactory.getUTC()));
        datesRef.add(new AbsoluteDate(2024, 1, 20, 0, 0, 0.0,
                                      TimeScalesFactory.getUTC()));
        Assertions.assertEquals(datesRef, dateChanges);
    }
}
