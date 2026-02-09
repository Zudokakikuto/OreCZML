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

package org.orekit.czml;

import org.hipparchus.geometry.euclidean.threed.Rotation;
import org.hipparchus.geometry.euclidean.threed.RotationConvention;
import org.hipparchus.geometry.euclidean.threed.RotationOrder;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.hipparchus.util.FastMath;
import org.orekit.attitudes.Attitude;
import org.orekit.attitudes.LofOffset;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.czml.object.primary.entities.Spacecraft;
import org.orekit.czml.object.secondary.Clock;
import org.orekit.data.DataContext;
import org.orekit.data.DataProvider;
import org.orekit.data.DirectoryCrawler;
import org.orekit.errors.OrekitException;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.frames.LOF;
import org.orekit.orbits.Orbit;
import org.orekit.propagation.MatricesHarvester;
import org.orekit.propagation.Propagator;
import org.orekit.propagation.StateCovariance;
import org.orekit.propagation.StateCovarianceMatrixProvider;
import org.orekit.time.AbsoluteDate;
import org.orekit.utils.AngularCoordinates;
import org.orekit.utils.Constants;
import org.orekit.utils.IERSConventions;
import org.orekit.utils.PVCoordinatesProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This class aims at giving the tutorial utilities to ease the understanding of
 * tutorials.
 */
public class TutorialUtils {

    /** The number of seconde between each step of the propagation. */
    public static final double STEP_BETWEEN_EACH_INSTANT = 60.0; // in seconds

    /** The minimum position tolerance for the numerical propagator. */
    public static final double POSITION_TOLERANCE = 10.0;

    /** The minimum step for the dormant prince integrator. */
    public static final double MIN_STEP = 0.001;

    /** The maximum step for the dormant prince integrator. */
    public static final double MAX_STEP = 1000.0;

    /** The classic duration of the simulation. */
    public static final double CLASSIC_DURATION_OF_SIMULATION = 10 * 3600; // in
    // seconds;

    /** user home. */
    private static final String USER_HOME = "user.home";

    /** orekit data. */
    private static final String OREKIT_DATA = "orekit-data";

    /** The root of the project. */
    private static String ROOT = System.getProperty("user.dir");

    private TutorialUtils() {
    }

    /**
     * Load orekit data.
     */
    public static void loadOrekitData() {
        try {
            final File home = new File(System.getProperty(USER_HOME));
            final File orekitDir = new File(home, OREKIT_DATA);
            final DataProvider provider = new DirectoryCrawler(orekitDir);
            DataContext.getDefault().getDataProvidersManager()
                .addProvider(provider);
        } catch (OrekitException oe) {
            System.err.println(oe.getLocalizedMessage());
        }
    }

    /**
     * Generate output string.
     *
     * @return the string
     */
    public static String generateOutput() {
        final String osName = System.getProperty("os.name");
        final String outputName = "Output.czml";
        final String outputFolder = "/Output";
        if (osName.contains("Windows")) {
            ROOT = ROOT.replace("\\", "/");
            final String outputPath = ROOT + outputFolder;
            return outputPath + "/" + outputName;
        } else if (osName.contains("Linux")) {
            final String outputPath =
                ROOT + "\\..\\oreczml-js-interface\\public";
            return outputPath + outputName;
        } else {
            ROOT = ROOT.replace("\\", "/");
            final String outputPath = ROOT + outputFolder;
            return outputPath + "/" + outputName;
        }
    }

    /**
     * Generate js path string.
     *
     * @param JsPath the js path
     * @return the string
     */
    public static String generateJSPath(final String JsPath) {
        final File javascriptFolder = new File(JsPath);
        final boolean out = javascriptFolder.mkdir();
        return JsPath;
    }

    /**
     * Load resources string.
     *
     * @param resourcePath the resource path
     * @return the string
     */
    public static String loadResources(final String resourcePath) {
        return new File(Objects.requireNonNull(TutorialUtils.class
            .getClassLoader().getResource(resourcePath)).getFile()).toPath()
            .toString();
    }

    /**
     * Gets earth.
     *
     * @return the earth
     */
    public static OneAxisEllipsoid getEarth() {
        final Frame ITRF =
            FramesFactory.getITRF(IERSConventions.IERS_2010, true);
        return new OneAxisEllipsoid(Constants.WGS84_EARTH_EQUATORIAL_RADIUS,
                                    Constants.WGS84_EARTH_FLATTENING, ITRF);
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
        satellite.resetAttitudes();

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

    /**
     * The type Sinusoidal lof.
     */
    public static class SinusoidalLof
        extends
        LofOffset {

        /** . */
        private final Frame inertialFrame;

        /** . */
        private final double period;

        /** . */
        private final AbsoluteDate initialDate;

        /** . */
        private final Vector3D axis;

        /** . */
        private final double maxAngle;

        /**
         * Instantiates a new Sinusoidal lof.
         *
         * @param inertialFrame the inertial frame
         * @param lof the lof
         * @param axis the axis
         * @param period the period
         * @param maxAngle the max angle
         * @param initialDate the initial date
         */
        public SinusoidalLof(final Frame inertialFrame, final LOF lof,
                             final Vector3D axis, final double period,
                             final double maxAngle,
                             final AbsoluteDate initialDate) {
            super(inertialFrame, lof);
            this.period = period;
            this.inertialFrame = inertialFrame;
            this.initialDate = initialDate;
            this.maxAngle = maxAngle;
            this.axis = axis;
        }

        /**
         * Instantiates a new Sinusoidal lof.
         *
         * @param inertialFrame the inertial frame
         * @param lof the lof
         * @param axis the axis
         * @param period the period
         * @param maxAngle the max angle
         * @param initialDate the initial date
         * @param order the order
         * @param alpha1 the alpha 1
         * @param alpha2 the alpha 2
         * @param alpha3 the alpha 3
         */
        SinusoidalLof(final Frame inertialFrame, final LOF lof,
                      final Vector3D axis, final double period,
                      final double maxAngle, final AbsoluteDate initialDate,
                      final RotationOrder order, final double alpha1,
                      final double alpha2, final double alpha3) {
            super(inertialFrame, lof, order, alpha1, alpha2, alpha3);
            this.inertialFrame = inertialFrame;
            this.period = period;
            this.initialDate = initialDate;
            this.maxAngle = maxAngle;
            this.axis = axis;
        }

        @Override
        public Attitude getAttitude(final PVCoordinatesProvider pvProv,
                                    final AbsoluteDate date,
                                    final Frame frame) {
            final double deltaT = date.durationFrom(initialDate);
            final double alpha =
                maxAngle * FastMath.sin(2 * FastMath.PI / period * deltaT);

            final Attitude lofAttitude =
                super.getAttitude(pvProv, date, inertialFrame);

            final Rotation rotationLof = lofAttitude.getRotation();
            final Rotation additionnalRotation =
                new Rotation(axis, alpha, RotationConvention.VECTOR_OPERATOR);

            final Rotation finalRotation =
                additionnalRotation.compose(rotationLof,
                                            RotationConvention.VECTOR_OPERATOR);
            final AngularCoordinates angularCoordinates =
                new AngularCoordinates(finalRotation);

            return new Attitude(date, inertialFrame, angularCoordinates);
        }
    }
}
