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
package org.orekit.czml.file;

import cesiumlanguagewriter.Cartesian;
import org.hipparchus.geometry.euclidean.threed.Rotation;
import org.hipparchus.geometry.euclidean.threed.RotationConvention;
import org.hipparchus.geometry.euclidean.threed.RotationOrder;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.hipparchus.ode.nonstiff.AdaptiveStepsizeIntegrator;
import org.hipparchus.ode.nonstiff.DormandPrince853Integrator;
import org.hipparchus.util.FastMath;
import org.junit.jupiter.api.Assertions;
import org.orekit.annotation.DefaultDataContext;
import org.orekit.attitudes.Attitude;
import org.orekit.attitudes.AttitudesSequence;
import org.orekit.attitudes.CelestialBodyPointed;
import org.orekit.attitudes.LofOffset;
import org.orekit.bodies.CelestialBodyFactory;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.czml.errors.OresiumException;
import org.orekit.czml.errors.OresiumMessages;
import org.orekit.czml.object.primary.Header;
import org.orekit.czml.object.primary.ManeuverSequence;
import org.orekit.czml.object.primary.entities.Spacecraft;
import org.orekit.czml.object.secondary.Clock;
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
import org.orekit.frames.LOF;
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
import org.orekit.propagation.events.DateDetector;
import org.orekit.propagation.events.EventDetector;
import org.orekit.propagation.events.handlers.ContinueOnEvent;
import org.orekit.propagation.numerical.NumericalPropagator;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScalesFactory;
import org.orekit.utils.AngularCoordinates;
import org.orekit.utils.AngularDerivativesFilter;
import org.orekit.utils.Constants;
import org.orekit.utils.IERSConventions;
import org.orekit.utils.PVCoordinatesProvider;

import java.io.File;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The type Abstract test.
 */
@DefaultDataContext
public class AbstractTest {

    /** The number of seconde between each step of the propagation. */
    public static final double STEP_BETWEEN_EACH_INSTANT = 60.0; // in seconds

    /** The minimum position tolerance for the numerical propagator. */
    public static final double POSITION_TOLERANCE = 10.0;

    /** The minimum step for the dormant prince integrator. */
    public static final double MIN_STEP = 0.001;

    /** The maximum step for the dormant prince integrator. */
    public static final double MAX_STEP = 1000.0;

    /** The classic duration of the simulation in seconds */
    public static final double CLASSIC_DURATION_OF_SIMULATION = 10 * 3600;

    /** The root of the project. */
    private static String ROOT =
        System.getProperty("user.dir").replace("oresium-core", "");

    /**
     * Load orekit data.
     */
    public static void loadOrekitData() {
        // Load orekit data
        // The local US is used to avoid having the problem of comma instead of
        // dots in numbers during the tests.
        Locale.setDefault(Locale.US);
        try {
            final String homePath = loadResources(".");
            final File orekitDir = new File(homePath, "orekit-data");
            final DataProvider provider = new DirectoryCrawler(orekitDir);
            DataContext.getDefault().getDataProvidersManager()
                .addProvider(provider);
        } catch (OrekitException oe) {
            System.err.println(oe.getLocalizedMessage());
        }
    }

    protected final double initializeOrekitData() {
        loadOrekitData();
        return 1;
    }

    /**
     * Load resources string.
     *
     * @param resourcePath the resource path
     * @return the string
     */
    public static String loadResources(final String resourcePath) {
        return new File(Objects.requireNonNull(AbstractTest.class
            .getClassLoader().getResource(resourcePath)).getFile()).toPath()
            .toString();
    }

    public static String loadOutputLocation() {
        return "Output";
    }

    public static String loadModelFile() {
        return "Default3DModels/ISSModel.glb";
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
     * Dummy header header.
     *
     * @return the header
     */
    public static Header dummyHeader() {
        final AbsoluteDate starDate =
            new AbsoluteDate(2024, 1, 1, 0, 0, 0.0, TimeScalesFactory.getUTC());
        final AbsoluteDate stopDate = starDate.shiftedBy(60.0);
        final Clock clockForHeader = new Clock(starDate, stopDate, 10.0);
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
                                  FastMath.toRadians(90), FastMath.toRadians(0),
                                  PositionAngleType.MEAN,
                                  FramesFactory.getEME2000(), startDate,
                                  Constants.WGS84_EARTH_MU);
    }

    /**
     * Dummy orbit from all keplerian parameters
     *
     * @param startDate the start date
     * @param sma the semi major orbit
     * @param ecc the eccentricity
     * @param incl the inclination
     * @param pa the periapsis argument
     * @param raan the right ascension of the ascending node
     * @param m the mean anomaly
     * @return the orbit
     */
    public static Orbit dummyOrbit(final AbsoluteDate startDate,
                                   final double sma, final double ecc,
                                   final double incl, final double pa,
                                   final double raan, final double m) {
        return new KeplerianOrbit(sma, ecc, FastMath.toRadians(incl), pa,
                                  FastMath.toRadians(raan),
                                  FastMath.toRadians(m), PositionAngleType.MEAN,
                                  FramesFactory.getEME2000(), startDate,
                                  Constants.WGS84_EARTH_MU);
    }

    /**
     * Dummy propagator bounded propagator.
     *
     * @param startDate the start date
     * @param finalDate the final date
     * @return the bounded propagator
     */
    @DefaultDataContext
    public static BoundedPropagator
        dummyPropagator(final AbsoluteDate startDate,
                        final AbsoluteDate finalDate, final Orbit orbit) {
        final double[][] tolerances =
            NumericalPropagator.tolerances(POSITION_TOLERANCE,
                                           dummyOrbit(startDate),
                                           OrbitType.CARTESIAN);
        final AdaptiveStepsizeIntegrator integrator =
            new DormandPrince853Integrator(MIN_STEP, MAX_STEP, tolerances[0],
                                           tolerances[1]);

        final NumericalPropagator propagator =
            new NumericalPropagator(integrator);

        final NormalizedSphericalHarmonicsProvider provider =
            GravityFieldFactory.getNormalizedProvider(10, 10);
        final ForceModel holmesFeatherstone =
            new HolmesFeatherstoneAttractionModel(FramesFactory.getEME2000(),
                                                  provider);

        final EphemerisGenerator generator = propagator.getEphemerisGenerator();

        final SpacecraftState initialState = new SpacecraftState(orbit);

        propagator.setOrbitType(OrbitType.CARTESIAN);
        propagator.addForceModel(holmesFeatherstone);
        propagator.setInitialState(initialState);

        propagator.propagate(startDate, finalDate);
        return generator.getGeneratedEphemeris();
    }

    /**
     * Builds a NumericalPropagator and returns both it and its generated
     * ephemeris after propagation.
     *
     * @param startDate start date of propagation
     * @param finalDate end date of propagation
     * @param orbit initial orbit
     * @return a container holding the propagator and its generated ephemeris
     */
    @DefaultDataContext
    public static PropagatorWithEphemeris
        dummyNumericalPropagatorWithEphemeris(final AbsoluteDate startDate,
                                              final AbsoluteDate finalDate,
                                              final Orbit orbit) {
        // Build integrator and propagator
        final double[][] tol =
            NumericalPropagator.tolerances(POSITION_TOLERANCE, orbit,
                                           OrbitType.CARTESIAN);
        final AdaptiveStepsizeIntegrator integrator =
            new DormandPrince853Integrator(MIN_STEP, MAX_STEP, tol[0], tol[1]);

        final NumericalPropagator propagator =
            new NumericalPropagator(integrator);
        propagator.setOrbitType(OrbitType.CARTESIAN);

        // Add a gravity model
        final NormalizedSphericalHarmonicsProvider provider =
            GravityFieldFactory.getNormalizedProvider(10, 10);
        final ForceModel holmesFeatherstone =
            new HolmesFeatherstoneAttractionModel(FramesFactory.getEME2000(),
                                                  provider);
        propagator.addForceModel(holmesFeatherstone);

        // Set the initial state
        propagator.setInitialState(new SpacecraftState(orbit));

        // Create generator *before* propagation
        final EphemerisGenerator generator = propagator.getEphemerisGenerator();

        // Do the propagation
        propagator.propagate(startDate, finalDate);

        // Generate the ephemeris
        final BoundedPropagator ephemeris = generator.getGeneratedEphemeris();

        return new PropagatorWithEphemeris(propagator, ephemeris);
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
            toReturn.add(new Cartesian(i, 2 * i, i));
        }
        return toReturn;
    }

    /**
     * Gets earth.
     *
     * @return the earth
     */
    @DefaultDataContext
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
     * @param clockMultiplier clock multiplier
     * @return the list
     */
    @DefaultDataContext
    public static List<StateCovariance>
        covariancePropagation(final Spacecraft satellite,
                              final Propagator propagator,
                              final StateCovariance initCovariance,
                              final double clockMultiplier) {

        final List<StateCovariance> covarianceListTemp = new ArrayList<>();

        final List<Orbit> orbits = satellite.getOrbits();

        final String stm = "stm";

        final MatricesHarvester harvester =
            propagator.setupMatricesComputation(stm, null, null);

        final StateCovarianceMatrixProvider provider =
            new StateCovarianceMatrixProvider("covariance", stm, harvester,
                                              initCovariance);

        propagator.addAdditionalDataProvider(provider);

        propagator.getMultiplexer().add(clockMultiplier, spacecraftState -> {
            final StateCovariance covariance =
                provider.getStateCovariance(spacecraftState);
            covarianceListTemp.add(covariance);
        });

        propagator.propagate(orbits.get(0).getDate(),
                             orbits.get(orbits.size() - 1).getDate());
        return covarianceListTemp;
    }

    public static ManeuverSequence
        dummyManeuverSequence(final AbsoluteDate startDate,
                              final AbsoluteDate finalDate,
                              final Spacecraft spacecraft) {

        final List<Maneuver> maneuvers = new ArrayList<>();

        final NumericalPropagator propagator =
            dummyNumericalPropagatorWithEphemeris(startDate, finalDate,
                                                  spacecraft.getOrbits().get(0))
                .getPropagator();
        final SpacecraftState initialState =
            spacecraft.getSpacecraftBoundedPropagator().getInitialState();

        propagator.resetInitialState(initialState);

        final CelestialBodyPointed bodyPointed =
            new CelestialBodyPointed(CelestialBodyFactory.getEarth()
                .getBodyOrientedFrame(), CelestialBodyFactory.getSun(),
                                     Vector3D.PLUS_J, Vector3D.PLUS_I,
                                     Vector3D.PLUS_K);

        final AttitudesSequence sequence = new AttitudesSequence();

        final double duration = finalDate.durationFrom(startDate) / 2.0;
        final LofOffset lofTNW =
            new LofOffset(FramesFactory.getEME2000(), LOFType.TNW);

        // Event detector for the attitude sequence
        final EventDetector detectorFiringDate =
            new DateDetector(startDate).withHandler(new ContinueOnEvent());
        final EventDetector detectorStopFiringDate =
            new DateDetector(startDate.shiftedBy(duration))
                .withHandler(new ContinueOnEvent());

        final EventDetector secondFiringDate =
            new DateDetector(startDate.shiftedBy(duration))
                .withHandler(new ContinueOnEvent());
        final EventDetector secondStopFiringDate =
            new DateDetector(startDate.shiftedBy(duration + duration / 2.0))
                .withHandler(new ContinueOnEvent());

        // Switches for attitude sequence
        sequence.addSwitchingCondition(bodyPointed, lofTNW, detectorFiringDate,
                                       true, false, duration / 10.0,
                                       AngularDerivativesFilter.USE_R, null);
        sequence.addSwitchingCondition(lofTNW, bodyPointed,
                                       detectorStopFiringDate, true, false,
                                       duration / 10.0,
                                       AngularDerivativesFilter.USE_R, null);

        sequence.addSwitchingCondition(bodyPointed, lofTNW, secondFiringDate,
                                       true, false, duration / 10.0,
                                       AngularDerivativesFilter.USE_R, null);
        sequence.addSwitchingCondition(lofTNW, bodyPointed,
                                       secondStopFiringDate, true, false,
                                       duration / 10.0,
                                       AngularDerivativesFilter.USE_R, null);

        sequence.resetActiveProvider(bodyPointed);

        propagator.setAttitudeProvider(sequence);

        // Trigger for the maneuver
        final ManeuverTriggers firstTriggers =
            new DateBasedManeuverTriggers(startDate, duration);
        final ManeuverTriggers secondTriggers =
            new DateBasedManeuverTriggers(startDate.shiftedBy(duration),
                                          duration);

        // Propulsion model
        final double thrust = 400;
        final double isp = 380;
        final Vector3D accelerationDirection = Vector3D.PLUS_I;
        final PropulsionModel firstPropulsionModel =
            new BasicConstantThrustPropulsionModel(thrust, isp,
                                                   accelerationDirection,
                                                   "first thrust");
        final PropulsionModel secondPropulsionModel =
            new BasicConstantThrustPropulsionModel(thrust, isp,
                                                   accelerationDirection,
                                                   "second thrust");

        // Maneuver
        final Maneuver firstManeuver =
            new Maneuver(sequence, firstTriggers, firstPropulsionModel);
        final Maneuver secondManeuver =
            new Maneuver(sequence, secondTriggers, secondPropulsionModel);
        maneuvers.add(firstManeuver);
        maneuvers.add(secondManeuver);

        // Setup propagator
        propagator.setOrbitType(OrbitType.CARTESIAN);
        propagator.addForceModel(firstManeuver);
        propagator.addForceModel(secondManeuver);
        propagator.setInitialState(initialState);

        final EphemerisGenerator generator = propagator.getEphemerisGenerator();

        propagator.propagate(startDate, finalDate);
        final BoundedPropagator boundedPropagator =
            generator.getGeneratedEphemeris();

        final Spacecraft spacecraftManeuvers =
            Spacecraft.builder(boundedPropagator, spacecraft.getClock())
                .build();

        return ManeuverSequence
            .builder(sequence, maneuvers, spacecraftManeuvers,
                     accelerationDirection, LOFType.TNW, spacecraft.getClock())
            .build();
    }

    /**
     * Verifies unit test output.
     *
     * @param templateFileName Desired unit test output file name
     * @param testString Current unit test output data string
     * @param accuracy Required significant figure accuracy
     */
    public static void verifyFileOutput(final String templateFileName,
                                        final String testString,
                                        final double accuracy) {

        try {
            // Get template file string data
            final String templateFile =
                Files.readString(Path.of(templateFileName));

            // Stores files as list of string and double values in sequential
            // order
            final List<Pair<Integer, Object>> templateValues =
                readValues(templateFile);
            final List<Pair<Integer, Object>> testValues =
                readValues(testString);

            // Determines locations of newline characters to aid in finding
            // output
            // error location
            final NavigableSet<Integer> lineStartValues =
                new TreeSet<>(findNewlineChars(templateFile));

            // Compares unit test output to template value
            final Pair<Integer, String> testOutput =
                compareValues(templateValues, testValues, lineStartValues,
                              accuracy);

            if (testOutput.first() == -1) {
                Assertions.assertEquals(-1, testOutput.first());
            } else {
                final String message =
                    testOutput.second() +
                                       " found at line " + testOutput.first() +
                                       " of " + templateFileName;
                throw new AssertionError(message);
            }
        } catch (IOException e) {
            throw new OresiumException(OresiumMessages.TEMPLATE_FILE_MALFORMED);
        }
    }

    /**
     * Breaks unit test output into strings and numbers.
     *
     * @param text Raw unit test output
     * @return the list
     */
    private static List<Pair<Integer, Object>> readValues(final String text) {

        // List holds broken down string data as a series of text strings and
        // numeric values
        final List<Pair<Integer, Object>> valueList = new ArrayList<>();

        // REGEX to recognize all integer, float, and scientific notation
        // numbers
        final Pattern pattern =
            Pattern.compile("-?\\d+(\\.\\d+)?([Ee][+-]?\\d+)?");
        final Matcher matcher = pattern.matcher(text);

        // Used to ensure that in case of multiple same numbers being present in
        // the text, we
        // are comparing the *latest* number pulled.
        int startText = 0;
        int prevStart = 0;
        int stopText = 0;

        while (matcher.find()) {
            final String match = matcher.group();
            try {

                // Finds next decimal number in line
                final double decimal = Double.parseDouble(match);

                // Update text starting point
                startText = text.indexOf(match, stopText);

                // Add text preceding current number and following after last
                // number
                valueList.add(new Pair<Integer, Object>(prevStart, text
                    .substring(prevStart, startText)));

                // Add number
                valueList.add(new Pair<Integer, Object>(startText, decimal));

                // Update other index values
                stopText = startText + match.length();
                prevStart = stopText;

            } catch (OresiumException e) {
                // Handle cases where the matched string is not a valid decimal
                throw new OresiumException(OresiumMessages.NOT_A_NUMBER);
            }
        }

        // Adds final text string to Object list
        valueList
            .add(new Pair<Integer, Object>(stopText, text.substring(stopText)));

        // Return list of text/number objects in file along with start character
        // index values
        return valueList;
    }

    /**
     * Verifies unit test output.
     *
     * @param templateValues Desired unit test output
     * @param testValues Current unit test output
     * @param lineStartValues Character values where file lines start in the
     *        current unit test output
     * @param accuracy Required significant figure level of accuracy
     * @return boolean
     */
    private static Pair<Integer, String>
        compareValues(final List<Pair<Integer, Object>> templateValues,
                      final List<Pair<Integer, Object>> testValues,
                      final NavigableSet<Integer> lineStartValues,
                      final double accuracy) {

        int maxVal = templateValues.size();
        if (templateValues.size() > testValues.size()) {
            maxVal = testValues.size();
        }

        // Compare individual values in the arrays.
        for (int i = 0; i < maxVal; i++) {

            final Object templateValue = templateValues.get(i).second();
            final Object testValue = testValues.get(i).second();

            // Compare two doubles
            if (templateValue instanceof Double &&
                testValue instanceof Double) {
                boolean accurate =
                    compareDoubles(templateValue, testValue, accuracy);
                if (!accurate) {
                    final Integer lineValue =
                        findErrorLineValue(lineStartValues,
                                           templateValues.get(i));
                    return new Pair<>(lineValue, "Numeric error");
                }
                // Compare two strings of text
            } else if (templateValue instanceof String &&
                       testValue instanceof String) {

                // Replace return line chars to avoid end-of-file return
                final String replaceValue = "([\\r\\n])";
                final String str1 =
                    ((String) templateValue).replaceAll(replaceValue, "");
                final String str2 =
                    ((String) testValue).replaceAll(replaceValue, "");

                if (str1.compareTo(str2) != 0) {
                    final Integer lineValue =
                        findErrorLineValue(lineStartValues,
                                           templateValues.get(i));
                    return new Pair<>(lineValue, "Text error");
                }
            }
            // Type mismatch error - means there is a mismatch in the data
            // files.
            else {
                final Integer lineValue =
                    findErrorLineValue(lineStartValues, templateValues.get(i));
                return new Pair<>(lineValue, "Type mismatch error");
            }
        }

        // Can only reach this point if the files match perfectly.
        return new Pair<>(-1, "test succeeded");

    }

    /**
     * Figures out which character values are at the start of file textlines.
     *
     * @param text Desired unit test output
     * @return List
     */
    static List<Integer> findNewlineChars(final String text) {

        final List<Integer> output = new ArrayList<Integer>();

        final Pattern pattern = Pattern.compile("\r?\n");
        final Matcher matcher = pattern.matcher(text);

        int startText = 0;
        int prevStart = 0;
        int stopText = 0;
        while (matcher.find()) {
            final String match = matcher.group();

            // Update text starting point
            startText = text.indexOf(match, stopText);

            // Add text preceding current number and following after last number
            output.add(prevStart);

            // Update other index values
            stopText = startText + match.length();
            prevStart = stopText - 1;
        }

        // Capture start character of final line of text
        output.add(text.length() - 2);

        return output;
    }

    /**
     * Verifies unit test output.
     *
     * @param value1 Double/Object value
     * @param value2 Double/Object value
     * @param accuracy Accuracy
     * @return Double
     */
    static boolean compareDoubles(final Object value1, final Object value2,
                                  final double accuracy) {

        double decimalValue1 = (double) value1;
        double decimalValue2 = (double) value2;

        // Ensures that string compared only has 1 value in front of decimal
        // point
        if (FastMath.abs(decimalValue1) >= 10) {
            int decVal =
                String.valueOf(FastMath.abs(decimalValue1)).indexOf(".");
            double mult =
                Double.parseDouble("1e-" + String.valueOf(decVal - 1));
            decimalValue1 *= mult;
            decimalValue2 *= mult;
        }

        // Removes negative sign from consideration
        if (decimalValue1 < 0) {
            decimalValue1 *= -1.0;
            decimalValue2 *= -1.0;
        }

        // Converting back to string keeps decimal values exact
        String str1 = String.valueOf(decimalValue1);
        String str2 = String.valueOf(decimalValue2);

        // Takes care of possibility of new string having scientific notation,
        // as
        // zeros that come right after the decimal point are significant figures
        // when
        // checking accuracy
        if (str1.substring(str1.length() - 2, str1.length() - 1).equals("-")) {
            int moveUp = Integer.valueOf(str1.substring(str1.length() - 1)) - 1;
            String filler = new String(new char[moveUp]).replace('\0', '0');
            str1 =
                "0." +
                   filler + str1.substring(0, 1) +
                   str1.substring(2, str1.length() - 3);
            str2 =
                "0." +
                   filler + str2.substring(0, 1) +
                   str2.substring(2, str2.length() - 3);
        }

        // Makes sure we do not call a character after the end of the string
        // length value
        double check =
            Double.parseDouble(String.valueOf(accuracy)
                .substring(String.valueOf(accuracy).length() - 1));
        double strMax =
            str1.length() < str2.length() ? str1.length() : str2.length();
        if (check + 2 > strMax) {
            check = strMax - 2;
        }

        return str1.substring(2, (int) (2 + check))
            .equals(str2.substring(2, (int) (2 + check)));
    }

    /**
     * Determines file line value of error.
     *
     * @param lineStartValues Character number values of line starts in template
     *        output file
     * @param templateValue Failed template output value
     * @return Integer
     */
    private static Integer
        findErrorLineValue(final NavigableSet<Integer> lineStartValues,
                           final Pair<Integer, Object> templateValue) {
        final Integer lineStartCharValue =
            lineStartValues.lower(templateValue.first());
        return lineStartValues.headSet(lineStartCharValue).size() + 1;
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

        propagator.addAdditionalDataProvider(provider);

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
     * Container holding both a NumericalPropagator and its generated ephemeris.
     */
    public static class PropagatorWithEphemeris {

        private final NumericalPropagator propagator;

        private final BoundedPropagator ephemeris;

        public PropagatorWithEphemeris(NumericalPropagator propagator,
                                       BoundedPropagator ephemeris) {
            this.propagator = propagator;
            this.ephemeris = ephemeris;
        }

        public NumericalPropagator getPropagator() {
            return propagator;
        }

        public BoundedPropagator getEphemeris() {
            return ephemeris;
        }
    }

    /**
     * The type Sinusoidal lof.
     */
    public static class SinusoidalLof
        extends
        LofOffset {

        /**
         * .
         */
        private final Frame inertialFrame;

        /**
         * .
         */

        private final double period;

        /**
         * .
         */

        private final AbsoluteDate initialDate;

        /**
         * .
         */

        private final Vector3D axis;

        /**
         * .
         */

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
        public SinusoidalLof(final Frame inertialFrame, final LOF lof,
                             final Vector3D axis, final double period,
                             final double maxAngle,
                             final AbsoluteDate initialDate,
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
