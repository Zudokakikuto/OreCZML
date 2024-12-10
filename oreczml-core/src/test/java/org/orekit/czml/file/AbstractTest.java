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
package org.orekit.czml.file;

import cesiumlanguagewriter.Cartesian;
import org.hipparchus.ode.nonstiff.AdaptiveStepsizeIntegrator;
import org.hipparchus.ode.nonstiff.DormandPrince853Integrator;
import org.hipparchus.util.FastMath;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.czml.object.primary.Header;
import org.orekit.czml.object.primary.Satellite;
import org.orekit.czml.object.secondary.Clock;
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
import org.orekit.time.TimeScalesFactory;
import org.orekit.utils.Constants;
import org.orekit.utils.IERSConventions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The type Abstract test.
 */
public class AbstractTest {

    /**
     * Load orekit data.
     */
    public static void loadOrekitData() {
        // Load orekit data
        // The local US is used to avoid having the problem of comma instead of dots in numbers during the tests.
        Locale.setDefault(Locale.US);
        try {
            final String       homePath  = loadResources(".");
            final File         orekitDir = new File(homePath, "orekit-data");
            final DataProvider provider  = new DirectoryCrawler(orekitDir);
            DataContext.getDefault()
                       .getDataProvidersManager()
                       .addProvider(provider);
        } catch (
                OrekitException oe) {
            System.err.println(oe.getLocalizedMessage());
        }
    }

    /**
     * Load resources string.
     *
     * @param resourcePath the resource path
     * @return the string
     */
    public static String loadResources(final String resourcePath) {
        return new File(GlobalTests.class.getClassLoader()
                                         .getResource(resourcePath)
                                         .getFile())
                .toPath()
                .toString();
    }

    /**
     * Dummy header header.
     *
     * @return the header
     */
    public static Header dummyHeader() {
        final AbsoluteDate starDate       = new AbsoluteDate(2024, 1, 1, 0, 0, 0.0, TimeScalesFactory.getUTC());
        final AbsoluteDate stopDate       = starDate.shiftedBy(60.0);
        final Clock        clockForHeader = new Clock(starDate, stopDate, TimeScalesFactory.getUTC(), 10.0);
        return new Header("Dummy_Header", clockForHeader);
    }

    /**
     * Dummy orbit orbit.
     *
     * @param startDate the start date
     * @return the orbit
     */
    public static Orbit dummyOrbit(final AbsoluteDate startDate) {
        return new KeplerianOrbit(7878000, 0, FastMath.toRadians(10), 0,
                FastMath.toRadians(90), FastMath.toRadians(0), PositionAngleType.MEAN, FramesFactory.getEME2000(),
                startDate,
                Constants.WGS84_EARTH_MU);
    }

    /**
     * Dummy propagator bounded propagator.
     *
     * @param startDate the start date
     * @param finalDate the final date
     * @return the bounded propagator
     */
    public static BoundedPropagator dummyPropagator(final AbsoluteDate startDate, final AbsoluteDate finalDate) {
        final double[][] tolerances = NumericalPropagator.tolerances(10.0, dummyOrbit(startDate),
                OrbitType.CARTESIAN);
        final AdaptiveStepsizeIntegrator integrator = new DormandPrince853Integrator(0.001,
                1000.0, tolerances[0],
                tolerances[1]);

        final NumericalPropagator propagator = new NumericalPropagator(integrator);

        final NormalizedSphericalHarmonicsProvider provider = GravityFieldFactory.getNormalizedProvider(10,
                10);
        final ForceModel holmesFeatherstone = new HolmesFeatherstoneAttractionModel(FramesFactory.getEME2000(),
                provider);

        final EphemerisGenerator generator = propagator.getEphemerisGenerator();

        final SpacecraftState initialState = new SpacecraftState(dummyOrbit(startDate));

        propagator.setOrbitType(OrbitType.CARTESIAN);
        propagator.addForceModel(holmesFeatherstone);
        propagator.setInitialState(initialState);

        propagator.propagate(startDate, finalDate);
        return generator.getGeneratedEphemeris();
    }

    /**
     * Compute random cartesians list.
     *
     * @param size the size
     * @return the list
     */
    protected List<Cartesian> computeRandomCartesians(final int size) {
        final List<Cartesian> toReturn = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            toReturn.add(new Cartesian(i, 2*i, i));
        }
        return toReturn;
    }

    /**
     * Gets earth.
     *
     * @return the earth
     */
    public static OneAxisEllipsoid getEarth() {
        final Frame            ITRF  = FramesFactory.getITRF(IERSConventions.IERS_2010, true);
        return new OneAxisEllipsoid(Constants.WGS84_EARTH_EQUATORIAL_RADIUS, Constants.WGS84_EARTH_FLATTENING, ITRF);
    }

    /**
     * Covariance propagation list.
     *
     * @param satellite      the satellite
     * @param propagator     the propagator
     * @param initCovariance the init covariance
     * @param header         the header
     * @return the list
     */
    public static List<StateCovariance> covariancePropagation(final Satellite satellite, final Propagator propagator,
                                                              final StateCovariance initCovariance, final Header header) {

        final List<StateCovariance> covarianceListTemp = new ArrayList<>();

        final List<Orbit> orbits = satellite.getOrbits();

        final String stm = "stm";

        final MatricesHarvester harvester = propagator.setupMatricesComputation(stm, null, null);

        final StateCovarianceMatrixProvider provider = new StateCovarianceMatrixProvider("covariance", stm, harvester,
                initCovariance);

        propagator.addAdditionalStateProvider(provider);

        propagator.getMultiplexer()
                  .add(header.getClock().getMultiplier(),
                          spacecraftState -> {
                              final StateCovariance covariance = provider.getStateCovariance(spacecraftState);
                              covarianceListTemp.add(covariance);
                          });

        propagator.propagate(orbits.get(0)
                                   .getDate(), orbits.get(orbits.size() - 1)
                                                     .getDate());
        return covarianceListTemp;
    }

}

