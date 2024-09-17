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
import org.orekit.attitudes.Attitude;
import org.orekit.attitudes.BoundedAttitudeProvider;
import org.orekit.czml.archi.builder.OrientationBuilder;
import org.orekit.czml.errors.OreCzmlException;
import org.orekit.czml.errors.OreCzmlMessages;
import org.orekit.czml.object.primary.Header;
import org.orekit.czml.object.primary.Satellite;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.frames.Transform;
import org.orekit.propagation.BoundedPropagator;
import org.orekit.time.AbsoluteDate;
import org.orekit.utils.IERSConventions;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;


/**
 * Orientation class.
 *
 * <p> This class ams at representing and writing in the czml file the orientation of an object knowing its Orekit Attitude. </p>
 *
 * @author Julien LEBLOND
 * @since 1.0.0
 */

public class Orientation implements CzmlSecondaryObject {

    /** The unit quaternion that defines the orientation in the space of the spacecraft. */
    private UnitQuaternion unitQuaternion;

    /** If multiple unit quaternions are necessary, this list will be used. */
    private List<UnitQuaternion> multipleQuaternions = new ArrayList<>();

    /** The julian dates of when the orientation must be done. */
    private List<JulianDate> julianDates = new ArrayList<>();

    /** The time interval of the orientation object. */
    private TimeInterval interval;

    /**
     * The interpolation algorithm between the different points that exists to fit the different instants of the simulation.
     * The available parameters are: LINEAR, LAGRANGE, HERMITE;
     */
    private CesiumInterpolationAlgorithm interpolationAlgorithm;

    /** The degree of the interpolation algorithm. */
    private int interpolationDegree;

    /** If multiple attitudes are necessary, this boolean will be true. */
    private boolean multipleAttitudes;

    /** The list of the attitudes of the spacecraft. */
    private List<Attitude> attitudes = new ArrayList<>();

    /** If a single attitude is necessary, this argument will be used. */
    private Attitude singleAttitude;


    // Constructors

    public Orientation(final BoundedAttitudeProvider provider, final BoundedPropagator propagator,
                       final Rotation optionalRotation,
                       final boolean invertToItrf) throws URISyntaxException, IOException {

        final List<Attitude> attitudesTemp = new ArrayList<>();

        final List<AbsoluteDate> dateList = new Satellite(propagator).getAbsoluteDateList();

        final Frame objectFrame = propagator.getFrame();

        for (final AbsoluteDate currentDate : dateList) {
            attitudesTemp.add(provider.getAttitude(propagator, currentDate, objectFrame));
        }

        if (!invertToItrf) {
            this.multipleAttitudes = true;
            this.attitudes         = attitudesTemp;
            final JulianDate startDate = absoluteDateToJulianDate(attitudes.get(0)
                                                                           .getDate(), Header.getTimeScale());
            final JulianDate finalDate = absoluteDateToJulianDate(attitudes.get(attitudes.size() - 1)
                                                                           .getDate()
                                                                           .shiftedBy(Header.getMasterClock()
                                                                                            .getMultiplier()),
                    Header.getTimeScale());
            final Frame ITRF = FramesFactory.getITRF(IERSConventions.IERS_2010, true);
            this.interval = new TimeInterval(startDate, finalDate);

            for (final Attitude currentAttitude : attitudes) {
                final JulianDate currentDate = absoluteDateToJulianDate(currentAttitude.getDate(),
                        Header.getTimeScale());
                this.julianDates.add(currentDate);

                final Rotation rotationFromObjectFrameToITRF = ITRF.getTransformTo(objectFrame,
                                                                           currentAttitude.getDate())
                                                                   .getRotation();
                final Rotation rotationITOJ     = new Rotation(Vector3D.PLUS_I, Vector3D.PLUS_J);
                final Rotation attitudeRotation = currentAttitude.getRotation();
                if (optionalRotation == null) {
                    final Rotation currentRotation = attitudeRotation.compose(rotationFromObjectFrameToITRF,
                            RotationConvention.VECTOR_OPERATOR);
                    final Rotation finalRotation = rotationITOJ.compose(currentRotation,
                            RotationConvention.VECTOR_OPERATOR);
                    final double currentQ0 = finalRotation.getQ0();
                    final double currentQ1 = finalRotation.getQ1();
                    final double currentQ2 = finalRotation.getQ2();
                    final double currentQ3 = finalRotation.getQ3();
                    final UnitQuaternion currentUnitQuaternion = new UnitQuaternion(currentQ0, currentQ1, currentQ2,
                            currentQ3);
                    multipleQuaternions.add(currentUnitQuaternion);
                } else {
                    final Rotation tempRotation = attitudeRotation.compose(rotationFromObjectFrameToITRF,
                            RotationConvention.VECTOR_OPERATOR);
                    final Rotation currentRotation = tempRotation.compose(optionalRotation,
                            RotationConvention.VECTOR_OPERATOR);
                    final Rotation finalRotation = rotationITOJ.compose(currentRotation,
                            RotationConvention.VECTOR_OPERATOR);
                    final double currentQ0 = finalRotation.getQ0();
                    final double currentQ1 = finalRotation.getQ1();
                    final double currentQ2 = finalRotation.getQ2();
                    final double currentQ3 = finalRotation.getQ3();
                    final UnitQuaternion currentUnitQuaternion = new UnitQuaternion(currentQ0, currentQ1, currentQ2,
                            currentQ3);
                    multipleQuaternions.add(currentUnitQuaternion);
                }
            }
            this.interpolationAlgorithm = CesiumInterpolationAlgorithm.LAGRANGE;
            this.interpolationDegree    = 5;
        } else {
            this.multipleAttitudes = true;
            this.attitudes         = attitudesTemp;
            final JulianDate startDate = absoluteDateToJulianDate(attitudes.get(0)
                                                                           .getDate(), Header.getTimeScale());
            final JulianDate finalDate = absoluteDateToJulianDate(attitudes.get(attitudes.size() - 1)
                                                                           .getDate()
                                                                           .shiftedBy(Header.getMasterClock()
                                                                                            .getMultiplier()),
                    Header.getTimeScale());
            this.interval = new TimeInterval(startDate, finalDate);

            for (final Attitude currentAttitude : attitudes) {
                final JulianDate currentDate = absoluteDateToJulianDate(currentAttitude.getDate(),
                        Header.getTimeScale());
                this.julianDates.add(currentDate);

                final Rotation objectRotation = currentAttitude.getRotation();
                if (optionalRotation == null) {
                    final double currentQ0 = objectRotation.getQ0();
                    final double currentQ1 = objectRotation.getQ1();
                    final double currentQ2 = objectRotation.getQ2();
                    final double currentQ3 = objectRotation.getQ3();
                    final UnitQuaternion currentUnitQuaternion = new UnitQuaternion(currentQ0, currentQ1, currentQ2,
                            currentQ3);
                    multipleQuaternions.add(currentUnitQuaternion);
                } else {
                    final double currentQ0 = objectRotation.getQ0();
                    final double currentQ1 = objectRotation.getQ1();
                    final double currentQ2 = objectRotation.getQ2();
                    final double currentQ3 = objectRotation.getQ3();
                    final UnitQuaternion currentUnitQuaternion = new UnitQuaternion(currentQ0, currentQ1, currentQ2,
                            currentQ3);
                    multipleQuaternions.add(currentUnitQuaternion);
                }
            }
            this.interpolationAlgorithm = CesiumInterpolationAlgorithm.LAGRANGE;
            this.interpolationDegree    = 5;
        }
    }

    /**
     * The orientation constructor for a single attitude with default parameters.
     *
     * @param attitude    : The attitude of the object.
     * @param objectFrame : The frame of the considered object.
     */
    public Orientation(final Attitude attitude, final Frame objectFrame) {
        this(attitude, objectFrame, true);
    }

    /**
     * The orientation constructor for a single attitude with no default parameters.
     *
     * @param attitude     : The attitude of the object.
     * @param objectFrame  : The frame of the considered object.
     * @param invertToITRF : To convert the object into the ITRF or not, by default it is true.
     *                     (The default is true because cesium only understands the ITRF as a base for the position).
     */
    public Orientation(final Attitude attitude, final Frame objectFrame, final boolean invertToITRF) {
        if (invertToITRF) {
            this.multipleAttitudes = false;
            singleAttitude         = attitude;
            final Frame     ITRF                          = FramesFactory.getITRF(IERSConventions.IERS_2010, true);
            final Transform transformObjectFrameToITRF    = objectFrame.getTransformTo(ITRF, attitude.getDate());
            final Rotation  rotationFromObjectFrameToITRF = transformObjectFrameToITRF.getRotation();
            final Rotation  objectRotation                = attitude.getRotation();
            final Rotation currentRotation = objectRotation.compose(rotationFromObjectFrameToITRF,
                    RotationConvention.FRAME_TRANSFORM);
            final double Q0 = currentRotation.getQ0();
            final double Q1 = currentRotation.getQ1();
            final double Q2 = currentRotation.getQ2();
            final double Q3 = currentRotation.getQ3();
            this.unitQuaternion         = new UnitQuaternion(Q1, Q2, Q3, Q0);
            this.interpolationAlgorithm = CesiumInterpolationAlgorithm.LAGRANGE;
            this.interpolationDegree    = 5;
        } else {
            this.multipleAttitudes = false;
            singleAttitude         = attitude;
            final Rotation objectRotation = attitude.getRotation();
            final double   Q0             = objectRotation.getQ0();
            final double   Q1             = objectRotation.getQ1();
            final double   Q2             = objectRotation.getQ2();
            final double   Q3             = objectRotation.getQ3();
            this.unitQuaternion         = new UnitQuaternion(Q1, Q2, Q3, Q0);
            this.interpolationAlgorithm = CesiumInterpolationAlgorithm.LAGRANGE;
            this.interpolationDegree    = 5;
        }
    }

    /**
     * The orientation constructor for multiple attitudes with default parameters.
     *
     * @param attitudes   : The attitudes of the object.
     * @param objectFrame : The frame of the considered object.
     */
    public Orientation(final List<Attitude> attitudes, final Frame objectFrame) {
        this(attitudes, objectFrame, true, null);
    }

    /**
     * The orientation constructor for multiple attitudes with default parameters.
     *
     * @param attitudes        : The attitudes of the object.
     * @param objectFrame      : The frame of the considered object.
     * @param invertToITRF     : To convert the object into the ITRF or not, by default it is true.
     *                         (The default is true because cesium only understands the ITRF as a base for the position).
     * @param optionalRotation : An optional rotation that can be applied to the attitude.
     */
    public Orientation(final List<Attitude> attitudes, final Frame objectFrame, final boolean invertToITRF,
                       final Rotation optionalRotation) {
        // The invert to ITRF allows the user to put object frame in topocentric frame,
        // usually it is advised to put invertToITRF true for the study of satellites.
        // This way the orientation computed is in the local orbital frame.
        if (!invertToITRF) {
            this.multipleAttitudes = true;
            this.attitudes         = attitudes;
            final JulianDate startDate = absoluteDateToJulianDate(attitudes.get(0)
                                                                           .getDate(), Header.getTimeScale());
            final JulianDate finalDate = absoluteDateToJulianDate(attitudes.get(attitudes.size() - 1)
                                                                           .getDate()
                                                                           .shiftedBy(Header.getMasterClock()
                                                                                            .getMultiplier()),
                    Header.getTimeScale());
            final Frame ITRF = FramesFactory.getITRF(IERSConventions.IERS_2010, true);
            this.interval = new TimeInterval(startDate, finalDate);

            for (final Attitude currentAttitude : attitudes) {
                final JulianDate currentDate = absoluteDateToJulianDate(currentAttitude.getDate(),
                        Header.getTimeScale());
                this.julianDates.add(currentDate);

                final Rotation rotationFromObjectFrameToITRF = ITRF.getTransformTo(objectFrame,
                                                                           currentAttitude.getDate())
                                                                   .getRotation();
                final Rotation rotationITOJ     = new Rotation(Vector3D.PLUS_I, Vector3D.PLUS_J);
                final Rotation attitudeRotation = currentAttitude.getRotation();
                if (optionalRotation == null) {
                    final Rotation currentRotation = attitudeRotation.compose(rotationFromObjectFrameToITRF,
                            RotationConvention.VECTOR_OPERATOR);
                    final Rotation finalRotation = rotationITOJ.compose(currentRotation,
                            RotationConvention.VECTOR_OPERATOR);
                    final double currentQ0 = finalRotation.getQ0();
                    final double currentQ1 = finalRotation.getQ1();
                    final double currentQ2 = finalRotation.getQ2();
                    final double currentQ3 = finalRotation.getQ3();
                    final UnitQuaternion currentUnitQuaternion = new UnitQuaternion(currentQ0, currentQ1, currentQ2,
                            currentQ3);
                    multipleQuaternions.add(currentUnitQuaternion);
                } else {
                    final Rotation tempRotation = attitudeRotation.compose(rotationFromObjectFrameToITRF,
                            RotationConvention.VECTOR_OPERATOR);
                    final Rotation currentRotation = tempRotation.compose(optionalRotation,
                            RotationConvention.VECTOR_OPERATOR);
                    final Rotation finalRotation = rotationITOJ.compose(currentRotation,
                            RotationConvention.VECTOR_OPERATOR);
                    final double currentQ0 = finalRotation.getQ0();
                    final double currentQ1 = finalRotation.getQ1();
                    final double currentQ2 = finalRotation.getQ2();
                    final double currentQ3 = finalRotation.getQ3();
                    final UnitQuaternion currentUnitQuaternion = new UnitQuaternion(currentQ0, currentQ1, currentQ2,
                            currentQ3);
                    multipleQuaternions.add(currentUnitQuaternion);
                }
            }
            this.interpolationAlgorithm = CesiumInterpolationAlgorithm.LAGRANGE;
            this.interpolationDegree    = 5;
        } else {
            this.multipleAttitudes = true;
            this.attitudes         = attitudes;
            final JulianDate startDate = absoluteDateToJulianDate(attitudes.get(0)
                                                                           .getDate(), Header.getTimeScale());
            final JulianDate finalDate = absoluteDateToJulianDate(attitudes.get(attitudes.size() - 1)
                                                                           .getDate()
                                                                           .shiftedBy(Header.getMasterClock()
                                                                                            .getMultiplier()),
                    Header.getTimeScale());
            this.interval = new TimeInterval(startDate, finalDate);

            for (final Attitude currentAttitude : attitudes) {
                final JulianDate currentDate = absoluteDateToJulianDate(currentAttitude.getDate(),
                        Header.getTimeScale());
                this.julianDates.add(currentDate);

                final Rotation objectRotation = currentAttitude.getRotation();
                if (optionalRotation == null) {
                    final double currentQ0 = objectRotation.getQ0();
                    final double currentQ1 = objectRotation.getQ1();
                    final double currentQ2 = objectRotation.getQ2();
                    final double currentQ3 = objectRotation.getQ3();
                    final UnitQuaternion currentUnitQuaternion = new UnitQuaternion(currentQ0, currentQ1, currentQ2,
                            currentQ3);
                    multipleQuaternions.add(currentUnitQuaternion);
                } else {
                    final double currentQ0 = objectRotation.getQ0();
                    final double currentQ1 = objectRotation.getQ1();
                    final double currentQ2 = objectRotation.getQ2();
                    final double currentQ3 = objectRotation.getQ3();
                    final UnitQuaternion currentUnitQuaternion = new UnitQuaternion(currentQ0, currentQ1, currentQ2,
                            currentQ3);
                    multipleQuaternions.add(currentUnitQuaternion);
                }
            }
            this.interpolationAlgorithm = CesiumInterpolationAlgorithm.LAGRANGE;
            this.interpolationDegree    = 5;
        }
    }


    // Builders

    public static OrientationBuilder builder(final Attitude attitude, final Frame objectFrame) {
        return new OrientationBuilder(attitude, objectFrame);
    }

    public static OrientationBuilder builder(final List<Attitude> attitudes, final Frame objectFrame) {
        return new OrientationBuilder(attitudes, objectFrame);
    }


    // Overrides

    @Override
    public void write(final PacketCesiumWriter packetWriter, final CesiumOutputStream output) {
        if (!multipleAttitudes) {
            try (OrientationCesiumWriter orientationWriter = packetWriter.getOrientationWriter()) {
                orientationWriter.open(output);
                orientationWriter.writeUnitQuaternion(getUnitQuaternion());
                orientationWriter.writeInterpolationDegree(getInterpolationDegree());
                orientationWriter.writeInterpolationAlgorithm(getInterpolationAlgorithm());
            }
        } else {
            try (OrientationCesiumWriter orientationWriter = packetWriter.getOrientationWriter()) {
                orientationWriter.open(output);
                orientationWriter.writeInterval(getInterval());
                orientationWriter.writeUnitQuaternion(getJulianDates(), getMultipleQuaternions());
                orientationWriter.writeInterpolationAlgorithm(getInterpolationAlgorithm());
                orientationWriter.writeInterpolationDegree(getInterpolationDegree());
            }
        }
    }


    // Getters

    public int getInterpolationDegree() {
        return interpolationDegree;
    }

    public boolean getMultipleAttitudes() {
        return multipleAttitudes;
    }

    public TimeInterval getInterval() {
        return interval;
    }

    public Attitude getSingleAttitude() {
        if (singleAttitude == null) {
            throw new OreCzmlException(OreCzmlMessages.MULTIPLE_ATTITUDES_SINGLE_GET);
        } else {
            return singleAttitude;
        }
    }

    public List<Attitude> getAttitudes() {
        if (attitudes.isEmpty()) {
            throw new OreCzmlException(OreCzmlMessages.SINGLE_ATTITUDE_MULTIPLE_GET);
        } else {
            return attitudes;
        }
    }

    public List<JulianDate> getJulianDates() {
        return new ArrayList<>(julianDates);
    }

    public List<UnitQuaternion> getMultipleQuaternions() {
        return new ArrayList<>(multipleQuaternions);
    }

    public UnitQuaternion getUnitQuaternion() {
        return unitQuaternion;
    }

    public CesiumInterpolationAlgorithm getInterpolationAlgorithm() {
        return interpolationAlgorithm;
    }
}
