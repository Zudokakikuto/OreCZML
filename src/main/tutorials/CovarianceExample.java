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

import org.hipparchus.linear.MatrixUtils;
import org.hipparchus.linear.RealMatrix;
import org.hipparchus.ode.nonstiff.AdaptiveStepsizeIntegrator;
import org.hipparchus.ode.nonstiff.DormandPrince853Integrator;
import org.hipparchus.util.FastMath;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.CovarianceDisplay;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.Header;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.Satellite;
import org.orekit.czml.CzmlObjects.CzmlSecondaryObjects.Clock;
import org.orekit.czml.Outputs.CzmlFile;
import org.orekit.data.DataContext;
import org.orekit.data.DataProvider;
import org.orekit.data.DirectoryCrawler;
import org.orekit.errors.OrekitException;
import org.orekit.forces.ForceModel;
import org.orekit.forces.gravity.HolmesFeatherstoneAttractionModel;
import org.orekit.forces.gravity.potential.GravityFieldFactory;
import org.orekit.forces.gravity.potential.NormalizedSphericalHarmonicsProvider;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.frames.LOFType;
import org.orekit.orbits.KeplerianOrbit;
import org.orekit.orbits.OrbitType;
import org.orekit.orbits.PositionAngleType;
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
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CovarianceExample {

    private CovarianceExample() {
    }

    public static void main(final String[] args) throws Exception {
        try {
            final File home = new File(System.getProperty("user.home"));
            final File orekitDir = new File(home, "orekit-data");
            final DataProvider provider = new DirectoryCrawler(orekitDir);
            DataContext.getDefault().getDataProvidersManager().addProvider(provider);
        } catch (OrekitException oe) {
            System.err.println(oe.getLocalizedMessage());
        }

        // Paths
        final String root = System.getProperty("user.dir").replace("\\", "/");
        final String outputPath = root + "/Output";
        final String outputName = "Output.czml";
        final String output = outputPath + "/" + outputName;
        final String IssModel = root + "/src/main/resources/ISSModel.glb";

        // File created
        final CzmlFile file = new CzmlFile(output);

        // Creation of the clock.
        final TimeScale UTC = TimeScalesFactory.getUTC();
        final double durationOfSimulation = 5 * 3600; // in seconds;
        final double stepBetweenEachInstant = 60.0; // in seconds
        final AbsoluteDate startDate = new AbsoluteDate(2024, 3, 15, 0, 0, 0.0, UTC);
        final AbsoluteDate finalDate = startDate.shiftedBy(durationOfSimulation);
        final Clock clock = new Clock(startDate, finalDate, UTC, stepBetweenEachInstant);

        final Header header = new Header("Setup of a covariance of a satellite", clock);
        file.addObject(header);

        // Build of a LEO orbit
        final Frame EME2000 = FramesFactory.getEME2000();
        final KeplerianOrbit initialOrbit = new KeplerianOrbit(7878000, 0, FastMath.toRadians(20), 0, FastMath.toRadians(0), FastMath.toRadians(0), PositionAngleType.MEAN, EME2000, startDate, Constants.WGS84_EARTH_MU);
        final SpacecraftState initialState = new SpacecraftState(initialOrbit);

        final Satellite satellite = new Satellite(initialOrbit, IssModel, Color.MAGENTA);
        satellite.displayOnlyOnePeriod();
        file.addObject(satellite);

        // Build of the propagator
        final double positionTolerance = 10.0;
        final double minStep = 0.001;
        final double maxStep = 1000.0;

        final double[][] tolerances = NumericalPropagator.tolerances(positionTolerance, initialOrbit, OrbitType.CARTESIAN);
        final AdaptiveStepsizeIntegrator integrator = new DormandPrince853Integrator(minStep, maxStep, tolerances[0], tolerances[1]);

        final NumericalPropagator propagator = new NumericalPropagator(integrator);

        final NormalizedSphericalHarmonicsProvider provider = GravityFieldFactory.getNormalizedProvider(10, 10);
        final ForceModel holmesFeatherstone = new HolmesFeatherstoneAttractionModel(EME2000, provider);

        propagator.setOrbitType(OrbitType.CARTESIAN);
        propagator.addForceModel(holmesFeatherstone);
        propagator.setInitialState(initialState);

        // Build of the covariance
        final RealMatrix realMatrix = MatrixUtils.createRealDiagonalMatrix(new double[] {1000, 100, 100, 1e-4, 1e-4, 1e-4});
        // With this datasArray, we obtain the same result as the tutorial, so the propagation is correct.
//        final double[][] datasArray = { {86.51816029065394, 56.8998712665727, -27.63870763721462, -0.02435617200936485, 0.020582741368923928, -0.005872883050786668},
//                                        {56.8998712665727, 70.7062432081459, 13.671209089262682, -0.006112622012706486, 0.007623626007527378, -0.012394131901568663},
//                                        {-27.63870763721462, 13.671209089262682, 181.1858898030072, 0.03143798991624274, -0.04963106558582378, -7.420114384979074E-4},
//                                        {-0.02435617200936485, -0.006112622012706486, 0.03143798991624274, 4.65707738862789E-5, 1.4699436343326518E-5, 3.328475593311651E-5},
//                                        {0.020582741368923928, 0.007623626007527378, -0.04963106558582378, 1.4699436343326518E-5, 3.950715933635926E-5, 2.5160442575178392E-5},
//                                        {-0.005872883050786668, -0.012394131901568663, -7.420114384979074E-4, 3.328475593311651E-5, 2.5160442575178392E-5, 3.5474661199690905E-5}};
//        final RealMatrix realMatrix1 = MatrixUtils.createRealMatrix(datasArray);
        final StateCovariance stateCovariance = new StateCovariance(realMatrix, startDate, LOFType.TNW);
        final CovarianceDisplay covariance = new CovarianceDisplay(satellite, stateCovariance, LOFType.TNW);
        file.addObject(covariance);
        covariancePropagation(propagator, satellite, stateCovariance);
        // Writing in the file
        file.write();
    }

    public static void covariancePropagation(final Propagator propagator, final Satellite inputSatellite, final StateCovariance initCovariance) {

        final List<StateCovariance> covarianceList = new ArrayList<>();
        final List<SpacecraftState> allSpaceCraftState = new ArrayList<>();
        inputSatellite.setAttitudes(new ArrayList<>());

        final String stm = "stm";

        final MatricesHarvester harvester = propagator.setupMatricesComputation(stm, null, null);

        final StateCovarianceMatrixProvider provider = new StateCovarianceMatrixProvider("covariance", stm,
                harvester, initCovariance);

        propagator.addAdditionalStateProvider(provider);

        propagator.getMultiplexer().add(Header.MASTER_CLOCK.getMultiplier(), spacecraftState -> {
            final StateCovariance covariance = provider.getStateCovariance(spacecraftState);
            covarianceList.add(covariance);
            allSpaceCraftState.add(spacecraftState);
        });

        propagator.propagate(inputSatellite.getOrbits().get(0).getDate(), inputSatellite.getOrbits().get(inputSatellite.getOrbits().size() - 1).getDate());

    }
}
