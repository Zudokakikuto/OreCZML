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
package org.example.other;

import org.example.TutorialUtils;
import org.hipparchus.linear.MatrixUtils;
import org.hipparchus.linear.RealMatrix;
import org.hipparchus.ode.nonstiff.AdaptiveStepsizeIntegrator;
import org.hipparchus.ode.nonstiff.DormandPrince853Integrator;
import org.hipparchus.util.FastMath;
import org.orekit.czml.object.primary.CollisionDisplay;
import org.orekit.czml.object.primary.Header;
import org.orekit.czml.object.primary.Satellite;
import org.orekit.czml.object.secondary.Clock;
import org.orekit.czml.file.CzmlFile;
import org.orekit.czml.file.CzmlFileBuilder;
import org.orekit.forces.ForceModel;
import org.orekit.forces.gravity.HolmesFeatherstoneAttractionModel;
import org.orekit.forces.gravity.potential.GravityFieldFactory;
import org.orekit.forces.gravity.potential.NormalizedSphericalHarmonicsProvider;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.frames.LOFType;
import org.orekit.orbits.KeplerianOrbit;
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
import org.orekit.time.TimeScale;
import org.orekit.time.TimeScalesFactory;
import org.orekit.utils.Constants;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import static org.example.TutorialUtils.loadResources;

public class CollisionDisplayExample {

    private CollisionDisplayExample() {
    }

    public static void main(final String[] args) throws Exception {
        // Load orekit data
        TutorialUtils.loadOrekitData();

        // Paths
        final String root = System.getProperty("user.dir")
                                  .replace("\\", "/");
        final String outputPath = root + "/Output";
        final String outputName = "Output.czml";
        final String output     = outputPath + "/" + outputName;
        // Change the path here to your JavaScript>public folder.
        final String pathToJSFolder = root + "/Javascript/public/";

        final String IssModel = loadResources("Default3DModels/ISSModel.glb");

        // Creation of the clock.
        final TimeScale    UTC                    = TimeScalesFactory.getUTC();
        final double       durationOfSimulation   = 5 * 3600; // in seconds;
        final double       stepBetweenEachInstant = 60.0; // in seconds
        final AbsoluteDate startDate              = new AbsoluteDate(2024, 3, 15, 0, 0, 0.0, UTC);
        final AbsoluteDate finalDate              = startDate.shiftedBy(durationOfSimulation);
        final Clock        clock                  = new Clock(startDate, finalDate, UTC, stepBetweenEachInstant);

        final Header header = new Header("Collision detection example", clock, pathToJSFolder);

        // Build of a LEO orbit
        final Frame EME2000 = FramesFactory.getEME2000();
        final KeplerianOrbit initialOrbit1 = new KeplerianOrbit(7878000, 0, FastMath.toRadians(20), 0,
                FastMath.toRadians(0), FastMath.toRadians(0), PositionAngleType.MEAN, EME2000, startDate,
                Constants.WGS84_EARTH_MU);
        final KeplerianOrbit initialOrbit2 = new KeplerianOrbit(7878100, 0, FastMath.toRadians(200), 0,
                FastMath.toRadians(0), FastMath.toRadians(0), PositionAngleType.MEAN, EME2000, startDate,
                Constants.WGS84_EARTH_MU);
        final SpacecraftState initialState1 = new SpacecraftState(initialOrbit1);
        final SpacecraftState initialState2 = new SpacecraftState(initialOrbit2);

        // Build of the propagator
        final double positionTolerance = 10.0;
        final double minStep           = 0.001;
        final double maxStep           = 1000.0;

        final double[][] tolerances1 = NumericalPropagator.tolerances(positionTolerance, initialOrbit1,
                OrbitType.CARTESIAN);
        final double[][] tolerances2 = NumericalPropagator.tolerances(positionTolerance, initialOrbit2,
                OrbitType.CARTESIAN);
        final AdaptiveStepsizeIntegrator integrator1 = new DormandPrince853Integrator(minStep, maxStep, tolerances1[0],
                tolerances1[1]);
        final AdaptiveStepsizeIntegrator integrator2 = new DormandPrince853Integrator(minStep, maxStep, tolerances2[0],
                tolerances2[1]);

        final NumericalPropagator propagator1 = new NumericalPropagator(integrator1);
        final NumericalPropagator propagator2 = new NumericalPropagator(integrator2);


        final NormalizedSphericalHarmonicsProvider provider = GravityFieldFactory.getNormalizedProvider(10,
                10);
        final ForceModel holmesFeatherstone = new HolmesFeatherstoneAttractionModel(EME2000,
                provider);

        propagator1.setOrbitType(OrbitType.CARTESIAN);
        propagator1.addForceModel(holmesFeatherstone);
        propagator1.setInitialState(initialState1);

        propagator2.setOrbitType(OrbitType.CARTESIAN);
        propagator2.addForceModel(holmesFeatherstone);
        propagator2.setInitialState(initialState2);

        final EphemerisGenerator generator1 = propagator1.getEphemerisGenerator();
        final EphemerisGenerator generator2 = propagator2.getEphemerisGenerator();

        propagator1.propagate(startDate, finalDate);
        propagator2.propagate(startDate, finalDate);

        final BoundedPropagator boundedPropagator1 = generator1.getGeneratedEphemeris();
        final BoundedPropagator boundedPropagator2 = generator2.getGeneratedEphemeris();

        final Satellite satellite1 = Satellite.builder(boundedPropagator1)
                                              .withModelPath(IssModel)
                                              .withColor(Color.MAGENTA)
                                              .withOnlyOnePeriod()
                                              .build();

        final Satellite satellite2 = Satellite.builder(boundedPropagator2)
                                              .withModelPath(IssModel)
                                              .withColor(Color.MAGENTA)
                                              .withOnlyOnePeriod()
                                              .build();

        // Build of the covariance
        final RealMatrix realMatrix = MatrixUtils.createRealDiagonalMatrix(
                new double[] {20000 * 20000, 1e-6, 1e-6, 1e-6, 1e-6, (36 * 4.848e-6) * (36 * 4.848e-6)});
        final StateCovariance stateCovariance = new StateCovariance(realMatrix, startDate, EME2000,
                OrbitType.EQUINOCTIAL, PositionAngleType.MEAN);
        final List<StateCovariance> allCovariances1 = covariancePropagation(satellite1, propagator1, stateCovariance);
        final List<StateCovariance> allCovariances2 = covariancePropagation(satellite2, propagator2, stateCovariance);

        final CollisionDisplay collisionDisplay = new CollisionDisplay(satellite1, satellite2, allCovariances1,
                allCovariances2, LOFType.TNW, LOFType.TNW);

        // Creation of the file
        final CzmlFile file = new CzmlFileBuilder(output).withHeader(header)
                                                         .withSatellite(satellite1)
                                                         .withSatellite(satellite2)
                                                         .withCollisionDisplay(collisionDisplay)
                                                         .build();

        // Writing in the file
        file.write();
    }

    public static List<StateCovariance> covariancePropagation(final Satellite satellite, final Propagator propagator,
                                                              final StateCovariance initCovariance) {

        final List<StateCovariance> covarianceListTemp     = new ArrayList<>();
        final List<SpacecraftState> allSpaceCraftStateTemp = new ArrayList<>();
        satellite.setAttitudes(new ArrayList<>());

        final List<Orbit> allOrbits = satellite.getOrbits();
        // Reset the spacecraft states of the satellite, mandatory !
        satellite.resetSpacecraftStates();

        final String stm = "stm";

        final MatricesHarvester harvester = propagator.setupMatricesComputation(stm, null, null);

        final StateCovarianceMatrixProvider provider = new StateCovarianceMatrixProvider("covariance", stm,
                harvester, initCovariance);

        propagator.addAdditionalStateProvider(provider);

        propagator.getMultiplexer()
                  .add(Header.getMasterClock()
                             .getMultiplier(),
                          spacecraftState -> {
                              final StateCovariance covariance = provider.getStateCovariance(spacecraftState);
                              covarianceListTemp.add(covariance);
                              allSpaceCraftStateTemp.add(spacecraftState);
                          });

        satellite.setAllSpaceCraftStates(allSpaceCraftStateTemp);
        propagator.propagate(allOrbits.get(0)
                                      .getDate(),
                allOrbits.get(allOrbits.size() - 1)
                         .getDate());
        return covarianceListTemp;
    }
}