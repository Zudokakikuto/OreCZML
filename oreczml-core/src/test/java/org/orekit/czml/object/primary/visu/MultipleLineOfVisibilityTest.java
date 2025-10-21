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

        loadOrekitData();

        final String output = generateOutput();

        final AbsoluteDate startDate =
            new AbsoluteDate(2024, 3, 15, 0, 0, 0.0,
                             TimeScalesFactory.getUTC());
        final AbsoluteDate finalDate = startDate.shiftedBy(10 * 3600);
        final Clock clock = new Clock(startDate, finalDate, 60.0);

        // Creation of the header
        final Header header =
            new Header("Tracking of a satellite by several stations", clock);

        final GeodeticPoint toulouseFrame =
            new GeodeticPoint(FastMath.toRadians(43.6047),
                              FastMath.toRadians(1.4442), 10);
        final TopocentricFrame topocentricToulouse =
            new TopocentricFrame(getEarth(), toulouseFrame, "Toulouse Frame");
        final GeodeticPoint quitoFrame =
            new GeodeticPoint(FastMath.toRadians(0.1807),
                              FastMath.toRadians(11.5382), 2850);
        final TopocentricFrame topocentricQuito =
            new TopocentricFrame(getEarth(), quitoFrame, "Quito");
        final GeodeticPoint sydneyFrame =
            new GeodeticPoint(FastMath.toRadians(-33.8688),
                              FastMath.toRadians(-241.2093), 100);
        final TopocentricFrame topocentricSydney =
            new TopocentricFrame(getEarth(), sydneyFrame, "Sydney");

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

        final SpacecraftState initialState = new SpacecraftState(initialOrbit);

        // Build of the propagator

        final double[][] tolerances =
            NumericalPropagator.tolerances(10.0, initialOrbit,
                                           OrbitType.CARTESIAN);
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
        final Spacecraft satellite =
            Spacecraft.builder(boundedPropagator, clock).withOnlyOnePeriod()
                .build();

        // Build of the multiple line of visibility object$
        final MultipleLineOfVisibility multipleLineOfVisibility =
            MultipleLineOfVisibility.builder(topocentricFrames, satellite)
                .build();

        final CzmlFile file =
            CzmlFile.builder(header).withSpacecraft(satellite)
                .withMultipleLineOfVisibility(multipleLineOfVisibility).build();

        file.write(output);
    }
}
