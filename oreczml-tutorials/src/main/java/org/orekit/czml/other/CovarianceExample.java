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

import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.hipparchus.linear.BlockRealMatrix;
import org.hipparchus.linear.RealMatrix;
import org.hipparchus.ode.nonstiff.AdaptiveStepsizeIntegrator;
import org.hipparchus.ode.nonstiff.DormandPrince853Integrator;
import org.orekit.czml.TutorialUtils;
import org.orekit.czml.file.CzmlFile;
import org.orekit.czml.object.primary.Header;
import org.orekit.czml.object.primary.covariance.Covariance;
import org.orekit.czml.object.primary.entities.Spacecraft;
import org.orekit.czml.object.secondary.Clock;
import org.orekit.forces.ForceModel;
import org.orekit.forces.gravity.HolmesFeatherstoneAttractionModel;
import org.orekit.forces.gravity.potential.GravityFieldFactory;
import org.orekit.forces.gravity.potential.NormalizedSphericalHarmonicsProvider;
import org.orekit.frames.FramesFactory;
import org.orekit.frames.LOFType;
import org.orekit.orbits.CartesianOrbit;
import org.orekit.orbits.Orbit;
import org.orekit.orbits.OrbitType;
import org.orekit.orbits.PositionAngleType;
import org.orekit.propagation.BoundedPropagator;
import org.orekit.propagation.EphemerisGenerator;
import org.orekit.propagation.MatricesHarvester;
import org.orekit.propagation.Propagator;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.StateCovariance;
import org.orekit.propagation.StateCovarianceMatrixProvider;
import org.orekit.propagation.numerical.NumericalPropagator;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScalesFactory;
import org.orekit.utils.Constants;
import org.orekit.utils.IERSConventions;
import org.orekit.utils.PVCoordinates;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * This tutorial provides an example of how to set up a covariance object from a
 * matrix.
 */
public class CovarianceExample {

    private CovarianceExample() {
    }

    /**
     * Main of the covariance tutorial.
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

        final double durationOfSimulation = 5 * 3600; // in seconds;
        final AbsoluteDate startDate =
            new AbsoluteDate(2024, 3, 15, 0, 0, 0.0,
                             TimeScalesFactory.getUTC());
        final AbsoluteDate finalDate =
            startDate.shiftedBy(durationOfSimulation);
        final Clock clock =
            new Clock(startDate, finalDate,
                      TutorialUtils.STEP_BETWEEN_EACH_INSTANT);

        final Header header =
            new Header("Setup of a covariance of a satellite (HOTFIX)", clock,
                       pathToJSFolder);

        // Build of a LEO orbit

        // final KeplerianOrbit initialOrbit = new KeplerianOrbit(7878000, 0,
        // FastMath.toRadians(20), 0,
        // FastMath.toRadians(0), FastMath.toRadians(0), PositionAngleType.MEAN,
        // FramesFactory.getEME2000(),
        // startDate,
        // Constants.WGS84_EARTH_MU);
        final Orbit initialOrbit =
            new CartesianOrbit(new PVCoordinates(new Vector3D(2.33052185175137e3,
                                                              -1.10370451050201e6,
                                                              7.10588764299718e6),
                                                 new Vector3D(-7.44286282871773e3,
                                                              -6.13734743652660e-1,
                                                              3.95136139293349e0)),
                               FramesFactory.getEME2000(), startDate,
                               Constants.IERS2010_EARTH_MU);
        final SpacecraftState initialState = new SpacecraftState(initialOrbit);

        // Build of the propagator

        final double[][] tolerances =
            NumericalPropagator.tolerances(TutorialUtils.POSITION_TOLERANCE,
                                           initialOrbit, OrbitType.CARTESIAN);
        final AdaptiveStepsizeIntegrator integrator =
            new DormandPrince853Integrator(TutorialUtils.MIN_STEP,
                                           TutorialUtils.MAX_STEP,
                                           tolerances[0], tolerances[1]);

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

        final Spacecraft satellite =
            Spacecraft.builder(boundedPropagator, clock).withModelPath(IssModel)
                .withColor(Color.MAGENTA).withOnlyOnePeriod().build();

        // Build of the covariance
        // @formatter:off
        final RealMatrix realMatrix =
            new BlockRealMatrix(new double[][] {
                {
                    9.31700905887535e1, -2.623398113500550e2,
                    2.360382173935300e1, 0, 0, 0
                }, {
                    -2.623398113500550e2, 1.77796454279511e4,
                    -9.331225387386501e1, 0, 0, 0
                }, {
                    2.360382173935300e1, -9.331225387386501e1,
                    1.917372231880040e1, 0, 0, 0
                }, {
                    0, 0, 0, 0, 0, 0
                }, {
                    0, 0, 0, 0, 0, 0
                }, {
                    0, 0, 0, 0, 0, 0
                }
            });
        // @formatter:on
        final StateCovariance stateCovariance =
            new StateCovariance(realMatrix, startDate,
                                FramesFactory.getEME2000(),
                                OrbitType.EQUINOCTIAL, PositionAngleType.MEAN);
        final List<StateCovariance> covariances =
            covariancePropagation(satellite, propagator, stateCovariance,
                                  clock);
        final Covariance covariance =
            Covariance.builder(satellite, covariances, LOFType.TNW)
                .withColor(Color.MAGENTA).build();

        // Creation of the file
        final CzmlFile file =
            CzmlFile.builder(header).withSpacecraft(satellite)
                .withCovariance(covariance).build();

        // Writing in the file
        file.write(output);
    }

    /**
     * Covariance propagation list.
     *
     * @param satellite the satellite
     * @param propagator the propagator
     * @param initCovariance the init covariance
     * @param clock the clock
     * @return the list
     */
    public static List<StateCovariance>
        covariancePropagation(final Spacecraft satellite,
                              final Propagator propagator,
                              final StateCovariance initCovariance,
                              final Clock clock) {

        final List<StateCovariance> covarianceListTemp = new ArrayList<>();

        final List<Orbit> orbits = satellite.getOrbits();

        final String stm = "stm";

        final MatricesHarvester harvester =
            propagator.setupMatricesComputation(stm, null, null);

        final StateCovarianceMatrixProvider provider =
            new StateCovarianceMatrixProvider("covariance", stm, harvester,
                                              initCovariance);

        propagator.addAdditionalStateProvider(provider);

        propagator.getMultiplexer().add(clock.getMultiplier(),
                                        spacecraftState -> {
                                            final StateCovariance covariance =
                                                provider
                                                    .getStateCovariance(spacecraftState);
                                            covarianceListTemp.add(covariance);
                                        });

        propagator.propagate(orbits.get(0).getDate(),
                             orbits.get(orbits.size() - 1).getDate());
        return covarianceListTemp;
    }
}
