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
package org.orekit.czml.object.primary;

import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.hipparchus.ode.nonstiff.AdaptiveStepsizeIntegrator;
import org.hipparchus.ode.nonstiff.DormandPrince853Integrator;
import org.hipparchus.util.FastMath;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.orekit.attitudes.AttitudesSequence;
import org.orekit.attitudes.CelestialBodyPointed;
import org.orekit.attitudes.LofOffset;
import org.orekit.bodies.CelestialBodyFactory;
import org.orekit.czml.file.AbstractTest;
import org.orekit.forces.ForceModel;
import org.orekit.forces.gravity.HolmesFeatherstoneAttractionModel;
import org.orekit.forces.gravity.potential.GravityFieldFactory;
import org.orekit.forces.gravity.potential.NormalizedSphericalHarmonicsProvider;
import org.orekit.forces.maneuvers.Maneuver;
import org.orekit.forces.maneuvers.propulsion.BasicConstantThrustPropulsionModel;
import org.orekit.forces.maneuvers.propulsion.PropulsionModel;
import org.orekit.forces.maneuvers.trigger.DateBasedManeuverTriggers;
import org.orekit.forces.maneuvers.trigger.ManeuverTriggers;
import org.orekit.frames.FramesFactory;
import org.orekit.frames.LOFType;
import org.orekit.orbits.KeplerianOrbit;
import org.orekit.orbits.OrbitType;
import org.orekit.orbits.PositionAngleType;
import org.orekit.propagation.BoundedPropagator;
import org.orekit.propagation.EphemerisGenerator;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.events.DateDetector;
import org.orekit.propagation.events.EventDetector;
import org.orekit.propagation.events.handlers.ContinueOnEvent;
import org.orekit.propagation.numerical.NumericalPropagator;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScalesFactory;
import org.orekit.utils.AngularDerivativesFilter;
import org.orekit.utils.Constants;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * The type Maneuver sequence test.
 */
public class ManeuverSequenceTest extends AbstractTest {

    /**
     * Maneuver sequence constructor test.
     *
     * @throws IOException        the io exception
     * @throws URISyntaxException the uri syntax exception
     */
    @Test
    void ManeuverSequenceConstructorTest() throws IOException, URISyntaxException {

        loadOrekitData();

        final Header       header    = dummyHeader();
        final AbsoluteDate startDate = new AbsoluteDate(2024, 3, 15, 0, 0, 0.0, TimeScalesFactory.getUTC());
        final AbsoluteDate finalDate = startDate.shiftedBy(36 * 3600);

        // Creation of the list of maneuvers
        final List<Maneuver> maneuvers = new ArrayList<>();

        //// Creation of the satellite
        // build of the propagator


        final KeplerianOrbit initialOrbit = new KeplerianOrbit(7878000, 0, FastMath.toRadians(20), 0,
                FastMath.toRadians(90), FastMath.toRadians(0), PositionAngleType.MEAN, FramesFactory.getEME2000(),
                startDate,
                Constants.WGS84_EARTH_MU);

        final SpacecraftState initialState = new SpacecraftState(initialOrbit);


        final NormalizedSphericalHarmonicsProvider provider = GravityFieldFactory.getNormalizedProvider(10,
                10);
        final ForceModel holmesFeatherstone = new HolmesFeatherstoneAttractionModel(FramesFactory.getEME2000(),
                provider);

        final double[][] tolerances = NumericalPropagator.tolerances(10, initialOrbit,
                OrbitType.CARTESIAN);
        final AdaptiveStepsizeIntegrator integrator = new DormandPrince853Integrator(0.001,
                1000.0, tolerances[0],
                tolerances[1]);

        final NumericalPropagator propagator = new NumericalPropagator(integrator);

        ////// Add the maneuvers (MANEUVERS ABSOLUTELY NEED ATTITUDE OVERRIDES ARGUMENTS !)
        // Attitude providers
        final LofOffset lofTNW = new LofOffset(FramesFactory.getEME2000(), LOFType.TNW);
        final CelestialBodyPointed bodyPointed = new CelestialBodyPointed(CelestialBodyFactory.getEarth()
                                                                                              .getBodyOrientedFrame(),
                CelestialBodyFactory.getSun(), Vector3D.PLUS_J, Vector3D.PLUS_I, Vector3D.PLUS_K);

        // Firing dates
        final AbsoluteDate firingDateLOF = new AbsoluteDate(2024, 3, 15, 5, 0, 0.0, header.getTimeScale());
        final double       duration      = 3600;

        //// Attitude sequence to modelize the maneuver
        final AttitudesSequence sequence = new AttitudesSequence();

        // Event detector for the attitude sequence
        final EventDetector detectorFiringDate = new DateDetector(firingDateLOF).withHandler(new ContinueOnEvent());
        final EventDetector detectorStopFiringDate = new DateDetector(firingDateLOF.shiftedBy(duration)).withHandler(
                new ContinueOnEvent());

        final EventDetector secondFiringDate = new DateDetector(startDate.shiftedBy(17 * 3600.0)).withHandler(
                new ContinueOnEvent());
        final EventDetector secondStopFiringDate = new DateDetector(startDate.shiftedBy(18 * 3600)).withHandler(
                new ContinueOnEvent());

        // Switches for attitude sequence
        sequence.addSwitchingCondition(bodyPointed, lofTNW, detectorFiringDate, true, false, 200.0,
                AngularDerivativesFilter.USE_R, null);
        sequence.addSwitchingCondition(lofTNW, bodyPointed, detectorStopFiringDate, true, false, 200.0,
                AngularDerivativesFilter.USE_R, null);

        sequence.addSwitchingCondition(bodyPointed, lofTNW, secondFiringDate, true, false, 200.0,
                AngularDerivativesFilter.USE_R, null);
        sequence.addSwitchingCondition(lofTNW, bodyPointed, secondStopFiringDate, true, false, 200.0,
                AngularDerivativesFilter.USE_R, null);

        sequence.resetActiveProvider(bodyPointed);

        propagator.setAttitudeProvider(sequence);

        sequence.registerSwitchEvents(propagator);

        // Trigger for the maneuver
        final ManeuverTriggers firstTriggers = new DateBasedManeuverTriggers(firingDateLOF, duration);
        final ManeuverTriggers secondTriggers = new DateBasedManeuverTriggers(startDate.shiftedBy(17 * 3600.0),
                duration);

        // Propulsion model
        final double   thrust                = 400;
        final double   isp                   = 380;
        final Vector3D accelerationDirection = Vector3D.PLUS_I;
        final PropulsionModel firstPropulsionModel = new BasicConstantThrustPropulsionModel(thrust, isp,
                accelerationDirection, "first thrust");
        final PropulsionModel secondPropulsionModel = new BasicConstantThrustPropulsionModel(thrust, isp,
                accelerationDirection, "second thrust");


        // Maneuver
        final Maneuver firstManeuver  = new Maneuver(sequence, firstTriggers, firstPropulsionModel);
        final Maneuver secondManeuver = new Maneuver(sequence, secondTriggers, secondPropulsionModel);
        maneuvers.add(firstManeuver);
        maneuvers.add(secondManeuver);

        // Setup propagator
        propagator.setOrbitType(OrbitType.CARTESIAN);
        propagator.addForceModel(holmesFeatherstone);
        propagator.addForceModel(firstManeuver);
        propagator.addForceModel(secondManeuver);
        propagator.setInitialState(initialState);

        final EphemerisGenerator generator = propagator.getEphemerisGenerator();

        propagator.propagate(startDate, finalDate);
        final BoundedPropagator boundedPropagator = generator.getGeneratedEphemeris();

        final Satellite satellite = new Satellite(boundedPropagator, header);

        final List<Vector3D> accelerations = new ArrayList<>();
        accelerations.add(Vector3D.PLUS_I);
        accelerations.add(Vector3D.PLUS_J);

        final ManeuverSequence maneuverSequence = ManeuverSequence.builder(sequence, maneuvers, satellite,
                                                                          Vector3D.PLUS_I, LOFType.TNW, header)
                                                                  .build();

        final ManeuverSequence maneuverSequenceSimple = ManeuverSequence.builder(sequence, firstManeuver, satellite,
                                                                                Vector3D.PLUS_I, LOFType.TNW, header)
                                                                        .build();

        final ManeuverSequence maneuverSequenceMultiple = ManeuverSequence.builder(sequence, maneuvers, satellite,
                                                                                  accelerations, LOFType.TNW, header)
                                                                          .build();

        final String maneuversPathFile = loadResources("templateFile/primary/ManeuverSequenceTemplate.txt");

        final String maneuverSimplePathFile = loadResources("templateFile/primary/ManeuverSequenceSimpleTemplate.txt");

        final String maneuverMultiplePathFile = loadResources(
                "templateFile/primary/ManeuverSequenceMultipleTemplate.txt");

        Assertions.assertEquals(Files.readString(Path.of(maneuversPathFile)), maneuverSequence.toString());

        Assertions.assertEquals(Files.readString(Path.of(maneuverSimplePathFile)), maneuverSequenceSimple.toString());

        Assertions.assertEquals(Files.readString(Path.of(maneuverMultiplePathFile)),
                maneuverSequenceMultiple.toString());

        // Getters coverage

        Assertions.assertEquals(maneuvers, maneuverSequence.getManeuvers());
        Assertions.assertEquals(boundedPropagator.getInitialState()
                                                 .getPVCoordinates()
                                                 .toString(), maneuverSequence.getPropagator()
                                                                              .getInitialState()
                                                                              .getPVCoordinates()
                                                                              .toString());

        Assertions.assertEquals(accelerations, maneuverSequenceMultiple.getArrowsDirection());
        Assertions.assertEquals(sequence, maneuverSequence.getSequence());
        Assertions.assertEquals(LOFType.TNW, maneuverSequence.getLof());
        Assertions.assertFalse(maneuverSequence.isShowTrust());
    }
}
