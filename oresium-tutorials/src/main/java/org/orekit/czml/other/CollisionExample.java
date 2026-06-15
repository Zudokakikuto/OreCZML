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
package org.orekit.czml.other;

import org.hipparchus.linear.MatrixUtils;
import org.hipparchus.linear.RealMatrix;
import org.hipparchus.ode.nonstiff.AdaptiveStepsizeIntegrator;
import org.hipparchus.ode.nonstiff.DormandPrince853Integrator;
import org.hipparchus.util.FastMath;
import org.orekit.czml.TutorialUtils;
import org.orekit.czml.file.CzmlFile;
import org.orekit.czml.object.primary.Header;
import org.orekit.czml.object.primary.covariance.Collision;
import org.orekit.czml.object.primary.entities.Spacecraft;
import org.orekit.czml.object.secondary.Clock;
import org.orekit.forces.ForceModel;
import org.orekit.forces.gravity.HolmesFeatherstoneAttractionModel;
import org.orekit.forces.gravity.potential.GravityFieldFactory;
import org.orekit.forces.gravity.potential.NormalizedSphericalHarmonicsProvider;
import org.orekit.frames.FramesFactory;
import org.orekit.frames.LOFType;
import org.orekit.orbits.KeplerianOrbit;
import org.orekit.orbits.OrbitType;
import org.orekit.orbits.PositionAngleType;
import org.orekit.propagation.BoundedPropagator;
import org.orekit.propagation.EphemerisGenerator;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.StateCovariance;
import org.orekit.propagation.numerical.NumericalPropagator;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScalesFactory;
import org.orekit.utils.Constants;
import org.orekit.utils.IERSConventions;

import java.awt.Color;
import java.util.List;

/**
 * This tutorial provides an example of how to set up a collision object.
 */
public class CollisionExample {

    private CollisionExample() {
    }

    /**
     * Main of the collision tutorial.
     *
     * @param args the args
     * @throws Exception the exception
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

        final String IssModel =
            TutorialUtils.loadResources("Default3DModels/ISSModel.glb");

        // Creation of the clock.
        // Duration of the simulation in seconds
        final double durationOfSimulation = 5.0 * 3600;
        final AbsoluteDate startDate =
            new AbsoluteDate(2024, 3, 15, 0, 0, 0.0,
                             TimeScalesFactory.getUTC());
        final AbsoluteDate finalDate =
            startDate.shiftedBy(durationOfSimulation);
        final Clock clock =
            new Clock(startDate, finalDate,
                      TutorialUtils.STEP_BETWEEN_EACH_INSTANT);

        final Header header =
            new Header("Collision detection example", clock, pathToJSFolder);

        // Build of a LEO orbit

        final KeplerianOrbit initialOrbit1 =
            new KeplerianOrbit(7878000, 0, FastMath.toRadians(20), 0,
                               FastMath.toRadians(0), FastMath.toRadians(0),
                               PositionAngleType.MEAN,
                               FramesFactory.getEME2000(), startDate,
                               Constants.WGS84_EARTH_MU);
        final KeplerianOrbit initialOrbit2 =
            new KeplerianOrbit(7878100, 0, FastMath.toRadians(200), 0,
                               FastMath.toRadians(0), FastMath.toRadians(0),
                               PositionAngleType.MEAN,
                               FramesFactory.getEME2000(), startDate,
                               Constants.WGS84_EARTH_MU);
        final SpacecraftState initialState1 =
            new SpacecraftState(initialOrbit1);
        final SpacecraftState initialState2 =
            new SpacecraftState(initialOrbit2);

        // Build of the propagator

        final double[][] tolerances1 =
            NumericalPropagator.tolerances(TutorialUtils.POSITION_TOLERANCE,
                                           initialOrbit1, OrbitType.CARTESIAN);
        final double[][] tolerances2 =
            NumericalPropagator.tolerances(TutorialUtils.POSITION_TOLERANCE,
                                           initialOrbit2, OrbitType.CARTESIAN);
        final AdaptiveStepsizeIntegrator integrator1 =
            new DormandPrince853Integrator(TutorialUtils.MIN_STEP,
                                           TutorialUtils.MAX_STEP,
                                           tolerances1[0], tolerances1[1]);
        final AdaptiveStepsizeIntegrator integrator2 =
            new DormandPrince853Integrator(TutorialUtils.MIN_STEP,
                                           TutorialUtils.MAX_STEP,
                                           tolerances2[0], tolerances2[1]);

        final NumericalPropagator propagator1 =
            new NumericalPropagator(integrator1);
        final NumericalPropagator propagator2 =
            new NumericalPropagator(integrator2);

        final NormalizedSphericalHarmonicsProvider provider =
            GravityFieldFactory.getNormalizedProvider(10, 10);
        final ForceModel holmesFeatherstone =
            new HolmesFeatherstoneAttractionModel(FramesFactory
                .getITRF(IERSConventions.IERS_2010, true), provider);

        propagator1.setOrbitType(OrbitType.CARTESIAN);
        propagator1.addForceModel(holmesFeatherstone);
        propagator1.setInitialState(initialState1);

        propagator2.setOrbitType(OrbitType.CARTESIAN);
        propagator2.addForceModel(holmesFeatherstone);
        propagator2.setInitialState(initialState2);

        final EphemerisGenerator generator1 =
            propagator1.getEphemerisGenerator();
        final EphemerisGenerator generator2 =
            propagator2.getEphemerisGenerator();

        propagator1.propagate(startDate, finalDate);
        propagator2.propagate(startDate, finalDate);

        final BoundedPropagator boundedPropagator1 =
            generator1.getGeneratedEphemeris();
        final BoundedPropagator boundedPropagator2 =
            generator2.getGeneratedEphemeris();

        final Spacecraft satellite1 =
            Spacecraft.builder(boundedPropagator1, clock)
                .withModelPath(IssModel).withColor(Color.MAGENTA)
                .withOnlyOnePeriod().build();

        final Spacecraft satellite2 =
            Spacecraft.builder(boundedPropagator2, clock)
                .withModelPath(IssModel).withColor(Color.MAGENTA)
                .withOnlyOnePeriod().build();

        // Build of the covariance
        final RealMatrix realMatrix =
            MatrixUtils.createRealDiagonalMatrix(new double[] {
                20000 *
                                                                20000,
                1e-6, 1e-6, 1e-6, 1e-6, (36 * 4.848e-6) * (36 * 4.848e-6)
            });
        final StateCovariance stateCovariance =
            new StateCovariance(realMatrix, startDate,
                                FramesFactory.getEME2000(),
                                OrbitType.EQUINOCTIAL, PositionAngleType.MEAN);
        final List<StateCovariance> covariances1 =
            TutorialUtils.covariancePropagation(satellite1, propagator1,
                                                stateCovariance, clock);
        final List<StateCovariance> covariances2 =
            TutorialUtils.covariancePropagation(satellite2, propagator2,
                                                stateCovariance, clock);

        final Collision collision =
            Collision.builder(satellite1, satellite2, covariances1,
                              covariances2, LOFType.TNW, LOFType.TNW)
                .build();

        // Creation of the file
        final CzmlFile file =
            CzmlFile.builder(header).withSpacecraft(satellite1)
                .withSpacecraft(satellite2).withCollision(collision).build();

        // Writing in the file
        file.write(output);
    }
}
