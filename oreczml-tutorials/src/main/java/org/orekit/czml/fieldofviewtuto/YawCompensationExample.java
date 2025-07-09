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
import org.orekit.attitudes.NadirPointing;
import org.orekit.attitudes.YawCompensation;
import org.orekit.attitudes.YawSteering;
import org.orekit.bodies.CelestialBodyFactory;
import org.orekit.czml.object.primary.entities.SpacecraftBuilder;
import org.orekit.czml.file.CzmlFile;
import org.orekit.czml.object.primary.visu.FieldOfObservation;
import org.orekit.czml.object.primary.Header;
import org.orekit.czml.object.primary.entities.Spacecraft;
import org.orekit.czml.object.secondary.Clock;
import org.orekit.forces.ForceModel;
import org.orekit.forces.gravity.HolmesFeatherstoneAttractionModel;
import org.orekit.forces.gravity.potential.GravityFieldFactory;
import org.orekit.forces.gravity.potential.NormalizedSphericalHarmonicsProvider;
import org.orekit.frames.FramesFactory;
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
import org.orekit.utils.PVCoordinatesProvider;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * An example of yaw compensation when using field of observations.
 */
public class YawCompensationExample {

    private YawCompensationExample() {
        // empty
    }

    /**
     * Main of the yaw compensation tutorial.
     *
     * @param args arguments of the main function
     * @throws Exception exception to throw
     */
    public static void main(final String[] args)
        throws Exception {
        // Load orekit data
        TutorialUtils.loadOrekitData();

        // Paths
        final String output = TutorialUtils.generateOutput();
        // !!! Here you need to change the path inside 'generateJsPath' to the
        // path you are using for images or Model.
        // This folder can also be the public folder of your cesium javascript
        // interface.
        final String pathToJSFolder =
            TutorialUtils.generateJSPath(System.getProperty("user.dir") +
                                         "/Javascript/public");

        // Creation of the clock.
        final AbsoluteDate startDate =
            new AbsoluteDate(2024, 3, 15, 0, 0, 0.0,
                             TimeScalesFactory.getUTC());
        final AbsoluteDate finalDate =
            startDate.shiftedBy(TutorialUtils.CLASSIC_DURATION_OF_SIMULATION);
        final Clock clock =
            new Clock(startDate, finalDate,
                      TutorialUtils.STEP_BETWEEN_EACH_INSTANT);

        final Header header =
            new Header("Yaw compensation with 3 satellites with the same fov",
                       clock, pathToJSFolder);

        // Creation of the model of the sun
        final PVCoordinatesProvider sunModel = CelestialBodyFactory.getSun();

        // Build of a LEO orbit

        final KeplerianOrbit firstOrbit =
            new KeplerianOrbit(7200000, 0, FastMath.toRadians(98), 0,
                               FastMath.toRadians(0), FastMath.toRadians(0),
                               PositionAngleType.MEAN,
                               FramesFactory.getEME2000(), startDate,
                               Constants.WGS84_EARTH_MU);
        final KeplerianOrbit secondOrbit =
            new KeplerianOrbit(7200000, 0, FastMath.toRadians(98), 0,
                               FastMath.toRadians(20), FastMath.toRadians(0),
                               PositionAngleType.MEAN,
                               FramesFactory.getEME2000(), startDate,
                               Constants.WGS84_EARTH_MU);
        final KeplerianOrbit thirdOrbit =
            new KeplerianOrbit(7200000, 0, FastMath.toRadians(98), 0,
                               FastMath.toRadians(0), FastMath.toRadians(40),
                               PositionAngleType.MEAN,
                               FramesFactory.getEME2000(), startDate,
                               Constants.WGS84_EARTH_MU);

        final SpacecraftState firstState = new SpacecraftState(firstOrbit);
        final SpacecraftState secondState = new SpacecraftState(secondOrbit);
        final SpacecraftState thirdState = new SpacecraftState(thirdOrbit);

        // Build of the 3 bounded propagators

        final double[][] tolerances1 =
            NumericalPropagator.tolerances(TutorialUtils.POSITION_TOLERANCE,
                                           firstOrbit, OrbitType.CARTESIAN);
        final double[][] tolerances2 =
            NumericalPropagator.tolerances(TutorialUtils.POSITION_TOLERANCE,
                                           secondOrbit, OrbitType.CARTESIAN);
        final double[][] tolerances3 =
            NumericalPropagator.tolerances(TutorialUtils.POSITION_TOLERANCE,
                                           thirdOrbit, OrbitType.CARTESIAN);

        final AdaptiveStepsizeIntegrator integrator1 =
            new DormandPrince853Integrator(TutorialUtils.MIN_STEP,
                                           TutorialUtils.MAX_STEP,
                                           tolerances1[0], tolerances1[1]);
        final AdaptiveStepsizeIntegrator integrator2 =
            new DormandPrince853Integrator(TutorialUtils.MIN_STEP,
                                           TutorialUtils.MAX_STEP,
                                           tolerances2[0], tolerances2[1]);
        final AdaptiveStepsizeIntegrator integrator3 =
            new DormandPrince853Integrator(TutorialUtils.MIN_STEP,
                                           TutorialUtils.MAX_STEP,
                                           tolerances3[0], tolerances3[1]);

        final NumericalPropagator propagator1 =
            new NumericalPropagator(integrator1);
        final NumericalPropagator propagator2 =
            new NumericalPropagator(integrator2);
        final NumericalPropagator propagator3 =
            new NumericalPropagator(integrator3);

        final NormalizedSphericalHarmonicsProvider provider =
            GravityFieldFactory.getNormalizedProvider(10, 10);
        final ForceModel holmesFeatherstone =
            new HolmesFeatherstoneAttractionModel(FramesFactory
                .getITRF(IERSConventions.IERS_2010, true), provider);

        propagator1.setOrbitType(OrbitType.CARTESIAN);
        propagator1.addForceModel(holmesFeatherstone);
        propagator1.setInitialState(firstState);

        propagator2.setOrbitType(OrbitType.CARTESIAN);
        propagator2.addForceModel(holmesFeatherstone);
        propagator2.setInitialState(secondState);

        propagator3.setOrbitType(OrbitType.CARTESIAN);
        propagator3.addForceModel(holmesFeatherstone);
        propagator3.setInitialState(thirdState);

        final EphemerisGenerator generator1 =
            propagator1.getEphemerisGenerator();
        final EphemerisGenerator generator2 =
            propagator2.getEphemerisGenerator();
        final EphemerisGenerator generator3 =
            propagator3.getEphemerisGenerator();

        final NadirPointing nadirPointing =
            new NadirPointing(FramesFactory.getEME2000(),
                              TutorialUtils.getEarth());

        final YawCompensation yawCompensation =
            new YawCompensation(FramesFactory.getEME2000(), nadirPointing);

        final YawSteering yawSteering =
            new YawSteering(FramesFactory.getEME2000(), nadirPointing, sunModel,
                            Vector3D.PLUS_J);

        propagator1.setAttitudeProvider(nadirPointing);
        propagator2.setAttitudeProvider(yawCompensation);
        propagator3.setAttitudeProvider(yawSteering);

        propagator1.propagate(startDate, finalDate);
        propagator2.propagate(startDate, finalDate);
        propagator3.propagate(startDate, finalDate);

        final BoundedPropagator boundedPropagator1 =
            generator1.getGeneratedEphemeris();
        final BoundedPropagator boundedPropagator2 =
            generator2.getGeneratedEphemeris();
        final BoundedPropagator boundedPropagator3 =
            generator3.getGeneratedEphemeris();

        // Build of satellites
        final List<Spacecraft> satellites = new ArrayList<>();
        final Spacecraft satellite1 =
            new SpacecraftBuilder(boundedPropagator1, header)
                .withColor(Color.PINK).withOnlyOnePeriod().withDisplayAttitude()
                .withReferenceSystem().build();

        final Spacecraft satellite2 =
            new SpacecraftBuilder(boundedPropagator2, header)
                .withColor(Color.BLUE).withOnlyOnePeriod().withDisplayAttitude()
                .withReferenceSystem().build();

        final Spacecraft satellite3 =
            new SpacecraftBuilder(boundedPropagator3, header)
                .withColor(Color.WHITE).withOnlyOnePeriod()
                .withDisplayAttitude().withReferenceSystem().build();
        satellites.add(satellite1);
        satellites.add(satellite2);
        satellites.add(satellite3);

        // Creation of the field of observation of the satellite, it describes
        // the area the satellite see
        final List<FieldOfObservation> fobs = new ArrayList<>();

        final Transform initialInertToBody1 =
            firstState.getFrame()
                .getTransformTo(TutorialUtils.getEarth().getBodyFrame(),
                                firstState.getDate());

        final Transform initialFovBody1 =
            new Transform(firstState.getDate(),
                          firstState.toTransform().getInverse(),
                          initialInertToBody1);

        final FieldOfView fov1 =
            new DoubleDihedraFieldOfView(Vector3D.PLUS_K, Vector3D.PLUS_I,
                                         FastMath.toRadians(20),
                                         Vector3D.PLUS_J, FastMath.toRadians(5),
                                         2);
        final FieldOfObservation fieldOfObservation1 =
            FieldOfObservation
                .builder(satellite1, fov1, initialFovBody1, header)
                .withColor(Color.GREEN).build();

        final Transform initialInertToBody2 =
            secondState.getFrame()
                .getTransformTo(TutorialUtils.getEarth().getBodyFrame(),
                                secondState.getDate());

        final Transform initialFovBody2 =
            new Transform(secondState.getDate(),
                          secondState.toTransform().getInverse(),
                          initialInertToBody2);

        final FieldOfView fov2 =
            new DoubleDihedraFieldOfView(Vector3D.PLUS_K, Vector3D.PLUS_I,
                                         FastMath.toRadians(20),
                                         Vector3D.PLUS_J, FastMath.toRadians(5),
                                         2);
        final FieldOfObservation fieldOfObservation2 =
            FieldOfObservation
                .builder(satellite2, fov2, initialFovBody2, header)
                .withColor(Color.GREEN).build();

        final Transform initialInertToBody3 =
            thirdState.getFrame()
                .getTransformTo(TutorialUtils.getEarth().getBodyFrame(),
                                thirdState.getDate());

        final Transform initialFovBody3 =
            new Transform(thirdState.getDate(),
                          thirdState.toTransform().getInverse(),
                          initialInertToBody3);

        final FieldOfView fov3 =
            new DoubleDihedraFieldOfView(Vector3D.PLUS_K, Vector3D.PLUS_I,
                                         FastMath.toRadians(20),
                                         Vector3D.PLUS_J, FastMath.toRadians(5),
                                         2);
        final FieldOfObservation fieldOfObservation3 =
            FieldOfObservation
                .builder(satellite3, fov3, initialFovBody3, header)
                .withColor(Color.GREEN).build();

        fobs.add(fieldOfObservation1);
        fobs.add(fieldOfObservation2);
        fobs.add(fieldOfObservation3);

        // Creation of the file
        final CzmlFile file =
            CzmlFile.builder().withHeader(header).withSpacecraft(satellites)
                .withFieldOfObservation(fobs).build();

        // Writing the file
        file.write(output);
    }
}
