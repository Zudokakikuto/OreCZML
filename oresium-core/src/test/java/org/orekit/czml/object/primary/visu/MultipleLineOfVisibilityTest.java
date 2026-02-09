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
package org.orekit.czml.object.primary.visu;

import org.hipparchus.ode.nonstiff.AdaptiveStepsizeIntegrator;
import org.hipparchus.ode.nonstiff.DormandPrince853Integrator;
import org.hipparchus.util.FastMath;
import org.junit.jupiter.api.Test;
import org.orekit.annotation.DefaultDataContext;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.czml.file.AbstractTest;
import org.orekit.czml.file.CzmlFile;
import org.orekit.czml.object.primary.Header;
import org.orekit.czml.object.primary.entities.Spacecraft;
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

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Test class for the multiple line of visibility object
 *
 * @author Julien Leblond
 * @since 1.1.
 */
public class MultipleLineOfVisibilityTest
    extends
    AbstractTest {

    final double data = initializeOrekitData();

    // Date

    /** Start Date. */
    final AbsoluteDate startDate =
        new AbsoluteDate(2024, 3, 15, 0, 0, 0.0, TimeScalesFactory.getUTC());

    /** Final date. */
    final AbsoluteDate finalDate = startDate.shiftedBy(10 * 3600);

    /** Clock. */
    final Clock clock = new Clock(startDate, finalDate, 60.0);

    /**
     * Multiple line of visibility constructor test.
     *
     * @throws IOException the io exception
     * @throws URISyntaxException the uri syntax exception
     */
    @Test
    @DefaultDataContext
    void MultipleLineOfVisibilityConstructorTest()
        throws URISyntaxException,
            IOException {

        final String output = generateOutput();

        // Creation of the header
        final Header header =
            new Header("Tracking of a satellite by several stations", clock);

        final TopocentricFrame topocentricToulouse =
            buildTopocentric("Toulouse Frame", 43.6047, 1.4442, 10);
        final TopocentricFrame topocentricQuito =
            buildTopocentric("Quito", 0.1807, 11.5382, 2850);
        final TopocentricFrame topocentricSydney =
            buildTopocentric("Sydney", -33.8688, -241.2093, 100);

        final List<TopocentricFrame> topocentricFrames = new ArrayList<>();
        topocentricFrames.add(topocentricToulouse);
        topocentricFrames.add(topocentricQuito);
        topocentricFrames.add(topocentricSydney);

        final KeplerianOrbit initialOrbit =
            new KeplerianOrbit(7878000, 0, FastMath.toRadians(20), 0,
                               FastMath.toRadians(90), FastMath.toRadians(0),
                               PositionAngleType.MEAN,
                               FramesFactory.getEME2000(), startDate,
                               Constants.WGS84_EARTH_MU);

        final Spacecraft spacecraft =
            spacecraftFromOrbit(startDate, finalDate, initialOrbit);

        // Build of the multiple line of visibility object$
        final MultipleLineOfVisibility multipleLineOfVisibility =
            MultipleLineOfVisibility.builder(topocentricFrames, spacecraft)
                .build();

        final CzmlFile file =
            CzmlFile.builder(header).withSpacecraft(spacecraft)
                .withMultipleLineOfVisibility(multipleLineOfVisibility).build();

        file.write(output);
    }

    private TopocentricFrame
        buildTopocentric(final String frameName, final double latitude,
                         final double longitude, final double altitude) {
        final GeodeticPoint toulouseFrame =
            new GeodeticPoint(FastMath.toRadians(latitude),
                              FastMath.toRadians(longitude), altitude);
        return new TopocentricFrame(getEarth(), toulouseFrame, frameName);
    }

    private Spacecraft spacecraftFromOrbit(final AbsoluteDate startDate,
                                           final AbsoluteDate finalDate,
                                           final KeplerianOrbit orbit)
        throws URISyntaxException,
            IOException {
        final SpacecraftState initialState = new SpacecraftState(orbit);

        // Build of the propagator

        final double[][] tolerances =
            NumericalPropagator.tolerances(10.0, orbit, OrbitType.CARTESIAN);
        final AdaptiveStepsizeIntegrator integrator =
            new DormandPrince853Integrator(0.001, 1000.0, tolerances[0],
                                           tolerances[1]);

        final NumericalPropagator propagator =
            new NumericalPropagator(integrator);

        final NormalizedSphericalHarmonicsProvider provider =
            GravityFieldFactory.getNormalizedProvider(10, 10);
        final ForceModel holmesFeatherstone =
            new HolmesFeatherstoneAttractionModel(FramesFactory
                .getITRF(IERSConventions.IERS_2010, true), provider);

        propagator.setOrbitType(OrbitType.CARTESIAN);
        propagator.addForceModel(holmesFeatherstone);
        propagator.setInitialState(initialState);

        final EphemerisGenerator generator = propagator.getEphemerisGenerator();

        propagator.propagate(startDate, finalDate);
        final BoundedPropagator boundedPropagator =
            generator.getGeneratedEphemeris();

        // Build of the satellite
        return Spacecraft.builder(boundedPropagator, clock).withOnlyOnePeriod()
            .build();
    }
}
