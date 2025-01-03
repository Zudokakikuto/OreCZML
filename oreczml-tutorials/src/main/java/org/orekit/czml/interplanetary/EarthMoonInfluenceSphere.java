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

package org.orekit.czml.interplanetary;

import org.hipparchus.ode.nonstiff.AdaptiveStepsizeIntegrator;
import org.hipparchus.ode.nonstiff.DormandPrince853Integrator;
import org.hipparchus.util.FastMath;
import org.orekit.bodies.CelestialBodyFactory;
import org.orekit.czml.TutorialUtils;
import org.orekit.czml.archi.factory.BodyFactory;
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
import org.orekit.forces.inertia.InertialForces;
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
import java.util.ArrayList;
import java.util.List;

public class EarthMoonInfluenceSphere {

    private EarthMoonInfluenceSphere() {
        /** . */
    }

    /**
     * Main.
     *
     * @param args the args
     * @throws Exception the exception
     */
    public static void main(final String[] args) throws Exception {
        // Load orekit data
        TutorialUtils.loadOrekitData();

        // Paths
        final String output = "C:\\Users\\jleblond\\Documents\\git\\OreCzmlJSInterface\\public\\Output.czml";
        // !!! Here you need to change the path inside 'generateJsPath' to the path you are using for images or Model.
        // This folder can also be the public folder of your cesium javascript interface.
        final String pathToJSFolder = "C:\\Users\\jleblond\\Documents\\git\\OreCzmlJSInterface\\public";

        // Creation of the clock.

        final double       durationOfSimulation = 3 * 24 * 3600; // in seconds;
        final AbsoluteDate startDate            = new AbsoluteDate(2024, 1, 16, 0, 0, 0.0, TimeScalesFactory.getUTC());
        final AbsoluteDate finalDate            = startDate.shiftedBy(durationOfSimulation);
        final Clock clock = new Clock(startDate, finalDate, TimeScalesFactory.getUTC(),
                TutorialUtils.STEP_BETWEEN_EACH_INSTANT);

        final Header header = new Header("Example of usage of the influence sphere on the moon and the earth", clock,
                pathToJSFolder);

        // Influence sphere
        final Body earth = BodyFactory.getEarth(header);
        earth.displayInfluenceSphere();
        final Body moon = BodyFactory.getMoon(header);
        moon.displayInfluenceSphere(earth);
        moon.displayOnlyOnePeriod(24 * 3600);
        final List<Body> bodies = new ArrayList<>();
        bodies.add(earth);
        bodies.add(moon);

        // Satellite 390900000
        final KeplerianOrbit initialOrbit = new KeplerianOrbit(140900000, 0.3, FastMath.toRadians(180),
                FastMath.toRadians(90),
                FastMath.toRadians(0), FastMath.toRadians(0), PositionAngleType.MEAN, FramesFactory.getEME2000(),
                startDate,
                Constants.WGS84_EARTH_MU);
        final AbsolutePVCoordinates absolutePVCoordinates = new AbsolutePVCoordinates(
                FramesFactory.getITRF(IERSConventions.IERS_2010, true), initialOrbit.getPVCoordinates());

        final SpacecraftState initialState = new SpacecraftState(absolutePVCoordinates);
        final double[][] tolerances = NumericalPropagator.tolerances(TutorialUtils.POSITION_TOLERANCE, initialOrbit,
                OrbitType.CARTESIAN);
        final AdaptiveStepsizeIntegrator integrator = new DormandPrince853Integrator(TutorialUtils.MIN_STEP,
                TutorialUtils.MAX_STEP,
                tolerances[0], tolerances[1]);
        final NumericalPropagator propagator = new NumericalPropagator(integrator);

        propagator.setIgnoreCentralAttraction(true);

        final ForceModel singleBodyEarth = new SingleBodyAbsoluteAttraction(CelestialBodyFactory.getEarth());
        final ForceModel singleBodyMoon  = new SingleBodyAbsoluteAttraction(CelestialBodyFactory.getMoon());
        final InertialForces model = new InertialForces(CelestialBodyFactory.getEarthMoonBarycenter()
                                                                            .getInertiallyOrientedFrame());

        final NormalizedSphericalHarmonicsProvider provider = GravityFieldFactory.getNormalizedProvider(10,
                10);
        final ForceModel holmesFeatherstoneEarth = new HolmesFeatherstoneAttractionModel(FramesFactory.getITRF(
                IERSConventions.IERS_2010, true),
                provider);
        final ForceModel holmesFeatherstoneMoon = new HolmesFeatherstoneAttractionModel(CelestialBodyFactory.getMoon()
                                                                                                            .getBodyOrientedFrame(),
                provider);

        propagator.setInitialState(initialState);
        propagator.addForceModel(singleBodyEarth);
        propagator.addForceModel(holmesFeatherstoneEarth);
        propagator.addForceModel(holmesFeatherstoneMoon);
        propagator.addForceModel(model);
        propagator.addForceModel(singleBodyMoon);
        propagator.setOrbitType(null);
        final EphemerisGenerator firstGenerator = propagator.getEphemerisGenerator();

        propagator.propagate(startDate, finalDate);
        final BoundedPropagator boundedPropagator = firstGenerator.getGeneratedEphemeris();

        // Satellite
        final Spacecraft spacecraft = Spacecraft.builder(boundedPropagator, header)
                                                .withReferenceSystem()
                                                .withColor(Color.BLUE)
                                                .displayInfluenceSphereChanges(bodies)
                                                .build();
        // Czml file
        final CzmlFile file = CzmlFile.builder()
                                      .withSpacecraft(spacecraft)
                                      .withBody(earth)
                                      .withBody(moon)
                                      .withHeader(header)
                                      .build();

        // file writing
        file.write(output);
    }
}
