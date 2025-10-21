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

package org.orekit.czml.object.secondary;

import cesiumlanguagewriter.CesiumInterpolationAlgorithm;
import cesiumlanguagewriter.CesiumOutputStream;
import cesiumlanguagewriter.JulianDate;
import cesiumlanguagewriter.OrientationCesiumWriter;
import cesiumlanguagewriter.PacketCesiumWriter;
import cesiumlanguagewriter.TimeInterval;
import cesiumlanguagewriter.UnitQuaternion;
import org.hipparchus.geometry.euclidean.threed.Rotation;
import org.hipparchus.geometry.euclidean.threed.RotationConvention;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.orekit.annotation.DefaultDataContext;
import org.orekit.attitudes.Attitude;
import org.orekit.attitudes.BoundedAttitudeProvider;
import org.orekit.czml.errors.OreCzmlException;
import org.orekit.czml.errors.OreCzmlMessages;
import org.orekit.czml.object.utils.DateUtils;
import org.orekit.czml.object.primary.entities.Spacecraft;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.propagation.BoundedPropagator;
import org.orekit.time.AbsoluteDate;
import org.orekit.utils.IERSConventions;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Orientation class.
 * <p>
 * This class ams at representing and writing in the czml file the orientation
 * of an object knowing its Orekit Attitude.
 * </p>
 *
 * @author Julien LEBLOND
 * @since 1.0.0
 */
public class Orientation
    extends
    AbstractSecondaryObject {

    /**
     * If multiple unit quaternions are necessary, this list will be used.
     */
    private final List<UnitQuaternion> multipleQuaternions = new ArrayList<>();

    /**
     * The julian dates of when the orientation must be done.
     */
    private final List<JulianDate> julianDates = new ArrayList<>();

    /**
     * The time interval of the orientation object.
     */
    private final TimeInterval interval;

    /**
     * The interpolation algorithm between the different points that exists to
     * fit the different instants of the simulation. The available parameters
     * are: LINEAR, LAGRANGE, HERMITE;
     */
    private final CesiumInterpolationAlgorithm interpolationAlgorithm;

    /**
     * The degree of the interpolation algorithm.
     */
    private final int interpolationDegree;

    /**
     * The list of the attitudes of the spacecraft.
     */
    private final List<Attitude> attitudes;

    // Constructors

    /**
     * The orientation constructor from a bounded attitude provider object.
     *
     * @param provider : The bounded attitude provider that will determine the
     *        attitudes.
     * @param propagator : The propagator considered.
     * @param clock : provides time interval information
     * @param optionalRotation : An optional rotation to add to the orientation
     * @param invertToItrf : To put the referential into the ITR frame
     * @throws URISyntaxException the uri syntax exception
     * @throws IOException the io exception
     */
    @DefaultDataContext
    public Orientation(final BoundedAttitudeProvider provider,
                       final BoundedPropagator propagator, final Clock clock,
                       final Rotation optionalRotation,
                       final boolean invertToItrf)
        throws URISyntaxException,
            IOException {

        final List<Attitude> attitudesTemp = new ArrayList<>();

        final List<AbsoluteDate> dateList =
            new Spacecraft(propagator, clock).getAbsoluteDateList();

        final Frame objectFrame = propagator.getFrame();

        for (final AbsoluteDate currentDate : dateList) {
            attitudesTemp.add(provider.getAttitude(propagator, currentDate,
                                                   objectFrame));
        }

        if (!invertToItrf) {
            this.attitudes = attitudesTemp;
            final JulianDate startDate =
                DateUtils.toJulianDate(attitudes.get(0).getDate());
            final JulianDate finalDate =
                DateUtils.toJulianDate(attitudes.get(attitudes.size() - 1)
                    .getDate());
            final Frame ITRF =
                FramesFactory.getITRF(IERSConventions.IERS_2010, true);
            this.interval = new TimeInterval(startDate, finalDate);

            for (final Attitude currentAttitude : attitudes) {
                final JulianDate currentDate =
                    DateUtils.toJulianDate(currentAttitude.getDate());
                this.julianDates.add(currentDate);

                final Rotation rotationFromObjectFrameToITRF =
                    ITRF.getTransformTo(objectFrame, currentAttitude.getDate())
                        .getRotation();
                final Rotation rotationITOJ =
                    new Rotation(Vector3D.PLUS_I, Vector3D.PLUS_J);
                final Rotation attitudeRotation = currentAttitude.getRotation();
                if (optionalRotation == null) {
                    final Rotation currentRotation =
                        attitudeRotation
                            .compose(rotationFromObjectFrameToITRF,
                                     RotationConvention.VECTOR_OPERATOR);
                    final Rotation finalRotation =
                        rotationITOJ
                            .compose(currentRotation,
                                     RotationConvention.VECTOR_OPERATOR);
                    final double currentQ0 = finalRotation.getQ0();
                    final double currentQ1 = finalRotation.getQ1();
                    final double currentQ2 = finalRotation.getQ2();
                    final double currentQ3 = finalRotation.getQ3();
                    final UnitQuaternion currentUnitQuaternion =
                        new UnitQuaternion(currentQ0, currentQ1, currentQ2,
                                           currentQ3);
                    multipleQuaternions.add(currentUnitQuaternion);
                } else {
                    final Rotation tempRotation =
                        attitudeRotation
                            .compose(rotationFromObjectFrameToITRF,
                                     RotationConvention.VECTOR_OPERATOR);
                    final Rotation currentRotation =
                        tempRotation
                            .compose(optionalRotation,
                                     RotationConvention.VECTOR_OPERATOR);
                    final Rotation finalRotation =
                        rotationITOJ
                            .compose(currentRotation,
                                     RotationConvention.VECTOR_OPERATOR);
                    final double currentQ0 = finalRotation.getQ0();
                    final double currentQ1 = finalRotation.getQ1();
                    final double currentQ2 = finalRotation.getQ2();
                    final double currentQ3 = finalRotation.getQ3();
                    final UnitQuaternion currentUnitQuaternion =
                        new UnitQuaternion(currentQ0, currentQ1, currentQ2,
                                           currentQ3);
                    multipleQuaternions.add(currentUnitQuaternion);
                }
            }
            this.interpolationAlgorithm = CesiumInterpolationAlgorithm.LAGRANGE;
            this.interpolationDegree = 5;
        } else {
            this.attitudes = attitudesTemp;
            final JulianDate startDate =
                DateUtils.toJulianDate(attitudes.get(0).getDate());
            final JulianDate finalDate =
                DateUtils.toJulianDate(attitudes.get(attitudes.size() - 1)
                    .getDate().shiftedBy(clock.getMultiplier()));
            this.interval = new TimeInterval(startDate, finalDate);

            for (final Attitude currentAttitude : attitudes) {
                final JulianDate currentDate =
                    DateUtils.toJulianDate(currentAttitude.getDate());
                this.julianDates.add(currentDate);

                final Rotation objectRotation = currentAttitude.getRotation();
                final double currentQ0 = objectRotation.getQ0();
                final double currentQ1 = objectRotation.getQ1();
                final double currentQ2 = objectRotation.getQ2();
                final double currentQ3 = objectRotation.getQ3();
                final UnitQuaternion currentUnitQuaternion =
                    new UnitQuaternion(currentQ0, currentQ1, currentQ2,
                                       currentQ3);
                multipleQuaternions.add(currentUnitQuaternion);
            }
            this.interpolationAlgorithm = CesiumInterpolationAlgorithm.LAGRANGE;
            this.interpolationDegree = 5;
        }
    }

    /**
     * The orientation constructor for a single attitude with default
     * parameters.
     *
     * @param attitude : The attitude of the object.
     * @param objectFrame : The frame of the considered object.
     */
    public Orientation(final Attitude attitude, final Frame objectFrame) {
        this(attitude, objectFrame, true);
    }

    /**
     * The orientation constructor for a single attitude with no default
     * parameters.
     *
     * @param attitude : The attitude of the object.
     * @param objectFrame : The frame of the considered object.
     * @param invertToITRF : To convert the object into the ITRF or not, by
     *        default it is true. (The default is true because cesium only
     *        understands the ITRF as a base for the position).
     */
    public Orientation(final Attitude attitude, final Frame objectFrame,
                       final boolean invertToITRF) {
        this(Collections.singletonList(attitude), objectFrame, invertToITRF,
             null);
    }

    /**
     * The orientation constructor for multiple attitudes with default
     * parameters.
     *
     * @param attitudes : The attitudes of the object.
     * @param objectFrame : The frame of the considered object
     */
    public Orientation(final List<Attitude> attitudes,
                       final Frame objectFrame) {
        this(attitudes, objectFrame, true, null);
    }

    /**
     * The orientation constructor for multiple attitudes with default
     * parameters.
     *
     * @param attitudes : The attitudes of the object.
     * @param objectFrame : The frame of the considered object.
     * @param invertToITRF : To convert the object into the ITRF or not, by
     *        default it is true. (The default is true because cesium only
     *        understands the ITRF as a base for the position).
     * @param optionalRotation : An optional rotation that can be applied to the
     *        attitude.
     */
    @DefaultDataContext
    public Orientation(final List<Attitude> attitudes, final Frame objectFrame,
                       final boolean invertToITRF,
                       final Rotation optionalRotation) {
        // The invert to ITRF allows the user to put an object frame in
        // topocentric frame,
        // usually it is advised to put invertToITRF true for the study of
        // satellites.
        // This way the orientation computed is in the local orbital frame.
        if (!invertToITRF) {
            this.attitudes = attitudes;
            final JulianDate startDate =
                DateUtils.toJulianDate(attitudes.get(0).getDate());
            final JulianDate finalDate =
                DateUtils.toJulianDate(attitudes.get(attitudes.size() - 1)
                    .getDate());
            final Frame ITRF =
                FramesFactory.getITRF(IERSConventions.IERS_2010, true);
            this.interval = new TimeInterval(startDate, finalDate);

            for (final Attitude currentAttitude : attitudes) {
                final JulianDate currentDate =
                    DateUtils.toJulianDate(currentAttitude.getDate());
                this.julianDates.add(currentDate);

                final Rotation rotationFromObjectFrameToITRF =
                    ITRF.getTransformTo(objectFrame, currentAttitude.getDate())
                        .getRotation();
                final Rotation rotationITOJ =
                    new Rotation(Vector3D.PLUS_I, Vector3D.PLUS_J);
                final Rotation attitudeRotation = currentAttitude.getRotation();
                if (optionalRotation == null) {
                    final Rotation currentRotation =
                        attitudeRotation
                            .compose(rotationFromObjectFrameToITRF,
                                     RotationConvention.VECTOR_OPERATOR);
                    final Rotation finalRotation =
                        rotationITOJ
                            .compose(currentRotation,
                                     RotationConvention.VECTOR_OPERATOR);
                    final double currentQ0 = finalRotation.getQ0();
                    final double currentQ1 = finalRotation.getQ1();
                    final double currentQ2 = finalRotation.getQ2();
                    final double currentQ3 = finalRotation.getQ3();
                    final UnitQuaternion currentUnitQuaternion =
                        new UnitQuaternion(currentQ0, currentQ1, currentQ2,
                                           currentQ3);
                    multipleQuaternions.add(currentUnitQuaternion);
                } else {
                    final Rotation tempRotation =
                        attitudeRotation
                            .compose(rotationFromObjectFrameToITRF,
                                     RotationConvention.VECTOR_OPERATOR);
                    final Rotation currentRotation =
                        tempRotation
                            .compose(optionalRotation,
                                     RotationConvention.VECTOR_OPERATOR);
                    final Rotation finalRotation =
                        rotationITOJ
                            .compose(currentRotation,
                                     RotationConvention.VECTOR_OPERATOR);
                    final double currentQ0 = finalRotation.getQ0();
                    final double currentQ1 = finalRotation.getQ1();
                    final double currentQ2 = finalRotation.getQ2();
                    final double currentQ3 = finalRotation.getQ3();
                    final UnitQuaternion currentUnitQuaternion =
                        new UnitQuaternion(currentQ0, currentQ1, currentQ2,
                                           currentQ3);
                    multipleQuaternions.add(currentUnitQuaternion);
                }
            }
            this.interpolationAlgorithm = CesiumInterpolationAlgorithm.LAGRANGE;
            this.interpolationDegree = 5;
        } else {
            this.attitudes = attitudes;
            final JulianDate startDate =
                DateUtils.toJulianDate(attitudes.get(0).getDate());
            final JulianDate finalDate =
                DateUtils.toJulianDate(attitudes.get(attitudes.size() - 1)
                    .getDate());
            this.interval = new TimeInterval(startDate, finalDate);

            for (final Attitude currentAttitude : attitudes) {
                final JulianDate currentDate =
                    DateUtils.toJulianDate(currentAttitude.getDate());
                this.julianDates.add(currentDate);

                final Rotation objectRotation = currentAttitude.getRotation();
                final double currentQ0 = objectRotation.getQ0();
                final double currentQ1 = objectRotation.getQ1();
                final double currentQ2 = objectRotation.getQ2();
                final double currentQ3 = objectRotation.getQ3();
                final UnitQuaternion currentUnitQuaternion =
                    new UnitQuaternion(currentQ0, currentQ1, currentQ2,
                                       currentQ3);
                multipleQuaternions.add(currentUnitQuaternion);
            }
            this.interpolationAlgorithm = CesiumInterpolationAlgorithm.LAGRANGE;
            this.interpolationDegree = 5;
        }
    }

    // Builders

    /**
     * Builder orientation builder.
     *
     * @param attitude the attitude
     * @param objectFrame the object frame
     * @return the orientation builder
     */
    public static OrientationBuilder builder(final Attitude attitude,
                                             final Frame objectFrame) {
        return new OrientationBuilder(attitude, objectFrame);
    }

    /**
     * Builder orientation builder.
     *
     * @param attitudes the attitudes
     * @param objectFrame the object frame
     * @return the orientation builder
     */
    public static OrientationBuilder builder(final List<Attitude> attitudes,
                                             final Frame objectFrame) {
        return new OrientationBuilder(attitudes, objectFrame);
    }

    // Overrides

    @Override
    public void write(final PacketCesiumWriter packetWriter,
                      final CesiumOutputStream output) {
        try (OrientationCesiumWriter orientationWriter =
            packetWriter.getOrientationWriter()) {
            orientationWriter.open(output);
            orientationWriter.writeInterval(getInterval());
            orientationWriter.writeUnitQuaternion(getJulianDates(),
                                                  getMultipleQuaternions());
            orientationWriter
                .writeInterpolationAlgorithm(getInterpolationAlgorithm());
            orientationWriter
                .writeInterpolationDegree(getInterpolationDegree());
        }
    }

    // Getters

    /**
     * Gets interpolation degree.
     *
     * @return the interpolation degree
     */
    public int getInterpolationDegree() {
        return interpolationDegree;
    }

    /**
     * Gets interval.
     *
     * @return the interval
     */
    public TimeInterval getInterval() {
        return interval;
    }

    /**
     * Gets attitudes.
     *
     * @return the attitudes
     */
    public List<Attitude> getAttitudes() {
        if (attitudes.isEmpty()) {
            throw new OreCzmlException(OreCzmlMessages.SINGLE_ATTITUDE_MULTIPLE_GET);
        } else {
            return attitudes;
        }
    }

    /**
     * Gets julian dates.
     *
     * @return the julian dates
     */
    public List<JulianDate> getJulianDates() {
        return Collections.unmodifiableList(julianDates);
    }

    /**
     * Gets multiple quaternions.
     *
     * @return the multiple quaternions
     */
    public List<UnitQuaternion> getMultipleQuaternions() {
        return Collections.unmodifiableList(multipleQuaternions);
    }

    /**
     * Gets interpolation algorithm.
     *
     * @return the interpolation algorithm
     */
    public CesiumInterpolationAlgorithm getInterpolationAlgorithm() {
        return interpolationAlgorithm;
    }
}
