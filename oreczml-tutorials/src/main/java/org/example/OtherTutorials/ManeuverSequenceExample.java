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

import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.hipparchus.ode.nonstiff.AdaptiveStepsizeIntegrator;
import org.hipparchus.ode.nonstiff.DormandPrince853Integrator;
import org.hipparchus.util.FastMath;
import org.orekit.attitudes.AttitudesSequence;
import org.orekit.attitudes.CelestialBodyPointed;
import org.orekit.attitudes.LofOffset;
import org.orekit.bodies.CelestialBodyFactory;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.Header;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.ManeuverSequence;
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
import org.orekit.forces.maneuvers.Maneuver;
import org.orekit.forces.maneuvers.propulsion.BasicConstantThrustPropulsionModel;
import org.orekit.forces.maneuvers.propulsion.PropulsionModel;
import org.orekit.forces.maneuvers.trigger.DateBasedManeuverTriggers;
import org.orekit.forces.maneuvers.trigger.ManeuverTriggers;
import org.orekit.frames.Frame;
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
import org.orekit.time.TimeScale;
import org.orekit.time.TimeScalesFactory;
import org.orekit.utils.AngularDerivativesFilter;
import org.orekit.utils.Constants;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class ManeuverSequenceExample {

    private ManeuverSequenceExample() {
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
        final String IssModel   = root + "/src/main/resources/Default3DModels/ISSModel.glb";

        // Creation of the clock.
        final TimeScale    UTC                    = TimeScalesFactory.getUTC();
        final double       durationOfSimulation   = 36 * 3600; // in seconds;
        final double       stepBetweenEachInstant = 30.0; // in seconds
        final AbsoluteDate startDate              = new AbsoluteDate(2024, 3, 15, 0, 0, 0.0, UTC);
        final AbsoluteDate finalDate              = startDate.shiftedBy(durationOfSimulation);
        final Clock        clock                  = new Clock(startDate, finalDate, UTC, stepBetweenEachInstant);

        // Build of the header
        final Header header = new Header("Example of sequence of maneuvers", clock);

        // Creation of the list of maneuvers
        final List<Maneuver> maneuvers = new ArrayList<>();

        //// Creation of the satellite
        // build of the propagator

        final Frame EME2000 = FramesFactory.getEME2000();
        final KeplerianOrbit initialOrbit = new KeplerianOrbit(7878000, 0, FastMath.toRadians(20), 0,
                FastMath.toRadians(90), FastMath.toRadians(0), PositionAngleType.MEAN, EME2000, startDate,
                Constants.WGS84_EARTH_MU);

        final SpacecraftState initialState = new SpacecraftState(initialOrbit);

        final double positionTolerance = 10.0;
        final double minStep           = 0.001;
        final double maxStep           = 1000;
        final NormalizedSphericalHarmonicsProvider provider = GravityFieldFactory.getNormalizedProvider(10,
                10);
        final ForceModel holmesFeatherstone = new HolmesFeatherstoneAttractionModel(EME2000,
                provider);

        final double[][] tolerances = NumericalPropagator.tolerances(positionTolerance, initialOrbit,
                OrbitType.CARTESIAN);
        final AdaptiveStepsizeIntegrator integrator = new DormandPrince853Integrator(minStep, maxStep, tolerances[0],
                tolerances[1]);

        final NumericalPropagator propagator = new NumericalPropagator(integrator);

        ////// Add the maneuvers (MANEUVERS ABSOLUTELY NEED ATTITUDE OVERRIDES ARGUMENTS !)
        // Attitude providers
        final LofOffset lofTNW = new LofOffset(EME2000, LOFType.TNW);
        final CelestialBodyPointed bodyPointed = new CelestialBodyPointed(CelestialBodyFactory.getEarth()
                                                                                              .getBodyOrientedFrame(),
                CelestialBodyFactory.getSun(), Vector3D.PLUS_J, Vector3D.PLUS_I, Vector3D.PLUS_K);

        // Firing dates
        final AbsoluteDate firingDateLOF = new AbsoluteDate(2024, 3, 15, 5, 0, 0.0, clock.getTimeScale());
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

        // Build of the satellite
        final Satellite satellite = Satellite.builder(boundedPropagator)
                                             .withModelPath(IssModel)
                                             .withReferenceSystem()
                                             .withDisplayAttitude()
                                             .build();

        // Creation of the display of the maneuvers
        final ManeuverSequence maneuverSequence = ManeuverSequence.builder(sequence, maneuvers, satellite,
                                                                          accelerationDirection, LOFType.TNW)
                                                                  .build();

        // Creation of the file
        final CzmlFile file = new CzmlFileBuilder(output).withHeader(header)
                                                         .withSatellite(satellite)
                                                         .withManeuverSequence(maneuverSequence)
                                                         .build();

        // Write inside the CzmlFile the objects
        file.write();
    }
}
