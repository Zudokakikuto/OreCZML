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
package org.orekit.czml.fieldofviewtuto;

import org.orekit.czml.TutorialUtils;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.hipparchus.ode.nonstiff.AdaptiveStepsizeIntegrator;
import org.hipparchus.ode.nonstiff.DormandPrince853Integrator;
import org.hipparchus.util.FastMath;
import org.orekit.attitudes.LofOffset;
import org.orekit.czml.file.CzmlFile;
import org.orekit.czml.object.primary.visu.FieldOfObservation;
import org.orekit.czml.object.primary.Header;
import org.orekit.czml.object.primary.Satellite;
import org.orekit.czml.object.secondary.Clock;
import org.orekit.forces.ForceModel;
import org.orekit.forces.gravity.HolmesFeatherstoneAttractionModel;
import org.orekit.forces.gravity.potential.GravityFieldFactory;
import org.orekit.forces.gravity.potential.NormalizedSphericalHarmonicsProvider;
import org.orekit.frames.FramesFactory;
import org.orekit.frames.LOFType;
import org.orekit.frames.Transform;
import org.orekit.geometry.fov.DoubleDihedraFieldOfView;
import org.orekit.geometry.fov.FieldOfView;
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

import java.awt.Color;

/**
 * This tutorial provides an example of how a field of observation can be set up.
 */
public class FieldOfObservationExample {

    private FieldOfObservationExample () {
        // empty
    }

    /**
     * Main of the field of observation tutorial.
     *
     * @param args arguments of the main function
     * @throws Exception exception to throw
     */
    public static void main (final String[] args) throws Exception {
        // Load orekit data
        TutorialUtils.loadOrekitData();

        // Paths
        final String output = TutorialUtils.generateOutput();
        // !!! Here you need to change the path inside 'generateJsPath' to the path you are using for images or Model.
        // This folder can also be the public folder of your cesium javascript interface.
        final String pathToJSFolder = TutorialUtils.generateJSPath(
                System.getProperty("user.dir") + "/Javascript/public");

        final String IssModel = TutorialUtils.loadResources("Default3DModels/ISSModel.glb");

        // Creation of the clock.


        final AbsoluteDate startDate = new AbsoluteDate(2024, 3, 15, 0, 0, 0.0, TimeScalesFactory.getUTC());
        final AbsoluteDate finalDate = startDate.shiftedBy(TutorialUtils.CLASSIC_DURATION_OF_SIMULATION);
        final Clock clock = new Clock(startDate, finalDate, TimeScalesFactory.getUTC(),
                TutorialUtils.STEP_BETWEEN_EACH_INSTANT);

        final Header header = new Header("Setup of a satellite with a line of sight", clock, pathToJSFolder);


        //// Build of a satellite with a propagator
        // Build of a LEO orbit

        final KeplerianOrbit initialOrbit = new KeplerianOrbit(7878000, 0, FastMath.toRadians(20), 0,
                FastMath.toRadians(0), FastMath.toRadians(0), PositionAngleType.MEAN, FramesFactory.getEME2000(), startDate,
                Constants.WGS84_EARTH_MU);

        final SpacecraftState initialState = new SpacecraftState(initialOrbit);

        // Build of the propagator


        final double[][] tolerances = NumericalPropagator.tolerances(TutorialUtils.POSITION_TOLERANCE, initialOrbit,
                OrbitType.CARTESIAN);
        final AdaptiveStepsizeIntegrator integrator = new DormandPrince853Integrator(TutorialUtils.MIN_STEP,
                TutorialUtils.MAX_STEP, tolerances[0],
                tolerances[1]);

        final NumericalPropagator propagator = new NumericalPropagator(integrator);

        final NormalizedSphericalHarmonicsProvider provider = GravityFieldFactory.getNormalizedProvider(10,
                10);
        final ForceModel holmesFeatherstone = new HolmesFeatherstoneAttractionModel(FramesFactory.getITRF(
                IERSConventions.IERS_2010, true),
                provider);

        propagator.setOrbitType(OrbitType.CARTESIAN);
        propagator.addForceModel(holmesFeatherstone);
        propagator.setInitialState(initialState);

        final EphemerisGenerator generator = propagator.getEphemerisGenerator();

        final LofOffset lofOffset = new LofOffset(FramesFactory.getEME2000(), LOFType.TNW);
        propagator.setAttitudeProvider(lofOffset);

        propagator.propagate(startDate, finalDate);
        final BoundedPropagator boundedPropagator = generator.getGeneratedEphemeris();

        // Creation of the satellite
        final Satellite satellite = Satellite.builder(boundedPropagator, header)
                                             .withModelPath(IssModel)
                                             .withColor(Color.RED)
                                             .withOnlyOnePeriod()
                                             .withDisplayAttitude()
                                             .build();


        // Creation of the field of observation of the satellite, it describes the area the satellite sees
        final Transform initialInertToBody = initialState.getFrame()
                                                         .getTransformTo(TutorialUtils.getEarth().getBodyFrame(),
                                                                 initialState.getDate());
        final Transform initialFovBody = new Transform(initialState.getDate(), initialState.toTransform()
                                                                                           .getInverse(),
                initialInertToBody);
        // A circular field of view
        //final FieldOfView fov = new CircularFieldOfView(Vector3D.PLUS_J, FastMath.toRadians(50), 2);
        // A rectangular field of view
        final FieldOfView fov = new DoubleDihedraFieldOfView(Vector3D.PLUS_J, Vector3D.PLUS_I,
                FastMath.toRadians(20), Vector3D.PLUS_K, FastMath.toRadians(20), 2);
        final FieldOfObservation fieldOfObservation = new FieldOfObservation(satellite, fov, initialFovBody, header);

        // Creation of the file
        final CzmlFile file = CzmlFile.builder()
                                      .withHeader(header)
                                      .withSatellite(satellite)
                                      .withFieldOfObservation(fieldOfObservation)
                                      .build();

        // Writing in the file
        file.write(output);
    }
}

