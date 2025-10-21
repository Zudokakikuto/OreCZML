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
package org.orekit.czml.object.secondary;

import cesiumlanguagewriter.Cartesian;
import cesiumlanguagewriter.JulianDate;
import org.hipparchus.ode.nonstiff.AdaptiveStepsizeIntegrator;
import org.hipparchus.ode.nonstiff.DormandPrince853Integrator;
import org.hipparchus.util.FastMath;
import org.junit.jupiter.api.Test;
import org.orekit.annotation.DefaultDataContext;
import org.orekit.czml.file.AbstractTest;
import org.orekit.czml.object.primary.Header;
import org.orekit.czml.object.primary.entities.Spacecraft;
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

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

/**
 * The type Time position test.
 */
public class TimePositionTest
    extends
    AbstractTest {

    /**
     * Time position constructor test.
     *
     * @throws IOException the io exception
     * @throws URISyntaxException the uri syntax exception
     */
    @Test
    @DefaultDataContext
    void TimePositionConstructorTest()
        throws IOException,
            URISyntaxException {

        loadOrekitData();

        final Header header = dummyHeader();

        final AbsoluteDate startDate =
            new AbsoluteDate(2024, 3, 15, 0, 0, 0.0,
                             TimeScalesFactory.getUTC());
        final AbsoluteDate finalDate = startDate.shiftedBy(5 * 3600);

        final KeplerianOrbit initialOrbit =
            new KeplerianOrbit(7878000, 0, FastMath.toRadians(10), 0,
                               FastMath.toRadians(90), FastMath.toRadians(0),
                               PositionAngleType.MEAN,
                               FramesFactory.getEME2000(), startDate,
                               Constants.WGS84_EARTH_MU);

        final SpacecraftState initialState = new SpacecraftState(initialOrbit);

        // Build of the propagator

        final double[][] tolerances =
            NumericalPropagator.tolerances(10, initialOrbit,
                                           OrbitType.CARTESIAN);
        final AdaptiveStepsizeIntegrator integrator =
            new DormandPrince853Integrator(0.001, 1000.0, tolerances[0],
                                           tolerances[1]);

        final NumericalPropagator propagator =
            new NumericalPropagator(integrator);

        final NormalizedSphericalHarmonicsProvider provider =
            GravityFieldFactory.getNormalizedProvider(10, 10);
        final ForceModel holmesFeatherstone =
            new HolmesFeatherstoneAttractionModel(FramesFactory.getEME2000(),
                                                  provider);

        final EphemerisGenerator generator = propagator.getEphemerisGenerator();

        propagator.setOrbitType(OrbitType.CARTESIAN);
        propagator.addForceModel(holmesFeatherstone);
        propagator.setInitialState(initialState);

        propagator.propagate(startDate, finalDate);
        final BoundedPropagator boundedPropagator =
            generator.getGeneratedEphemeris();

        final Spacecraft satellite =
            new Spacecraft(boundedPropagator, header.getClock());

        final List<JulianDate> julianDates = satellite.getJulianDates();

        final List<Cartesian> randomCartesians =
            computeRandomCartesians(julianDates.size());

        final TimePosition timePosition =
            new TimePosition(randomCartesians, julianDates);

        final String pathFile =
            loadResources("templateFile/object/secondary/TimePositionTemplate.txt");

        verifyFileOutput(pathFile, timePosition.toString(), 1e-8);
    }
}
