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
package org.orekit.czml.CzmlObjects.CzmlSecondaryObjects;

import cesiumlanguagewriter.CesiumInterpolationAlgorithm;
import cesiumlanguagewriter.CesiumOutputStream;
import cesiumlanguagewriter.JulianDate;
import cesiumlanguagewriter.OrientationCesiumWriter;
import cesiumlanguagewriter.PacketCesiumWriter;
import cesiumlanguagewriter.Reference;
import cesiumlanguagewriter.TimeInterval;
import cesiumlanguagewriter.UnitQuaternion;
import org.hipparchus.geometry.euclidean.threed.Rotation;
import org.hipparchus.geometry.euclidean.threed.RotationConvention;

import org.orekit.attitudes.Attitude;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.frames.Transform;
import org.orekit.utils.IERSConventions;

import java.util.ArrayList;
import java.util.List;


/** Orientation class.
 *
 * <p> This class ams at representing and writing in the czml file the orientation of an object knowing its Orekit Attitude. </p>
 *
 * @since 2.0.0
 * @author Julien LEBLOND
 */
public class Orientation implements CzmlSecondaryObject {

    /** .*/
    private UnitQuaternion unitQuaternion;
    /** .*/
    private List<UnitQuaternion> multipleQuaternions = new ArrayList<>();
    /** .*/
    private List<JulianDate> julianDates = new ArrayList<>();
    /** .*/
    private TimeInterval interval;
    /** .*/
    private Reference reference;
    /** .*/
    private Reference velocityReference;
    /** .*/
    private CesiumInterpolationAlgorithm interpolationAlgorithm;
    /** .*/
    private int interpolationDegree;
    /** .*/
    private boolean multipleAttitudes;

    public Orientation(final Attitude attitude, final Frame satelliteFrame, final Rotation optionalRotation) {
        this.multipleAttitudes = false;
        final Frame ITRF = FramesFactory.getITRF(IERSConventions.IERS_2010, true);
        final Transform transformSatelliteFrameToITRF = satelliteFrame.getTransformTo(ITRF, attitude.getDate());
        final Rotation rotationFromSatelliteFrameToITRF = transformSatelliteFrameToITRF.getRotation();
        final Rotation satelliteRotation = attitude.getRotation();
        final Rotation currentRotation = satelliteRotation.compose(rotationFromSatelliteFrameToITRF, RotationConvention.FRAME_TRANSFORM);
        final double Q0 = currentRotation.getQ0();
        final double Q1 = currentRotation.getQ0();
        final double Q2 = currentRotation.getQ0();
        final double Q3 = currentRotation.getQ0();
        this.unitQuaternion = new UnitQuaternion(Q1, Q2, Q3, Q0);
        this.interpolationAlgorithm = CesiumInterpolationAlgorithm.LAGRANGE;
        this.interpolationDegree = 5;
    }

    public Orientation(final List<Attitude> attitudes, final Frame satelliteFrame, final Rotation optionalRotation) {
        this.multipleAttitudes = true;
        final JulianDate startDate = absoluteDateToJulianDate(attitudes.get(0).getDate());
        final JulianDate finalDate = absoluteDateToJulianDate(attitudes.get(attitudes.size() - 1).getDate());
        final Frame ITRF = FramesFactory.getITRF(IERSConventions.IERS_2010, true);
        this.interval = new TimeInterval(startDate, finalDate);
        for (final Attitude currentAttitude : attitudes) {
            final JulianDate currentDate = absoluteDateToJulianDate(currentAttitude.getDate());
            this.julianDates.add(currentDate);

            final Transform transformSatelliteFrameToITRF = ITRF.getTransformTo(satelliteFrame, currentAttitude.getDate());
            final Rotation rotationFromSatelliteFrameToITRF = transformSatelliteFrameToITRF.getRotation();
            final Rotation satelliteRotation = currentAttitude.getRotation();
            //final Rotation rotation180 = new Rotation(Vector3D.PLUS_I, FastMath.PI, RotationConvention.VECTOR_OPERATOR);
            //final Rotation satelliteRotation180 = rotation180.compose(satelliteRotation, RotationConvention.VECTOR_OPERATOR);
            final Rotation currentRotation = satelliteRotation.compose(rotationFromSatelliteFrameToITRF, RotationConvention.VECTOR_OPERATOR);

            final double currentQ0 = currentRotation.getQ0();
            final double currentQ1 = currentRotation.getQ1();
            final double currentQ2 = currentRotation.getQ2();
            final double currentQ3 = currentRotation.getQ3();
            final UnitQuaternion currentUnitQuaternion = new UnitQuaternion(currentQ0, currentQ1, currentQ2, currentQ3);
            multipleQuaternions.add(currentUnitQuaternion);
        }
        this.interpolationAlgorithm = CesiumInterpolationAlgorithm.LAGRANGE;
        this.interpolationDegree = 5;
    }

    @Override
    public void write(final PacketCesiumWriter packetWriter, final CesiumOutputStream output) {
        if (!multipleAttitudes) {
            try (OrientationCesiumWriter orientationWriter = packetWriter.getOrientationWriter()) {
                orientationWriter.open(output);
                orientationWriter.writeUnitQuaternion(getUnitQuaternion());
                orientationWriter.writeInterpolationDegree(getInterpolationDegree());
                orientationWriter.writeInterpolationAlgorithm(getInterpolationAlgorithm());
            }
        }
        else {
            try (OrientationCesiumWriter orientationWriter = packetWriter.getOrientationWriter()) {
                orientationWriter.open(output);
                orientationWriter.writeInterval(getInterval());
                orientationWriter.writeUnitQuaternion(getJulianDates(), getMultipleQuaternions());
                orientationWriter.writeInterpolationAlgorithm(getInterpolationAlgorithm());
                orientationWriter.writeInterpolationDegree(getInterpolationDegree());
            }
        }
    }

    // Gets

    public int getInterpolationDegree() {
        return interpolationDegree;
    }

    public boolean getMultipleAttitudes() {
        return multipleAttitudes;
    }

    public TimeInterval getInterval() {
        return interval;
    }

    public List<JulianDate> getJulianDates() {
        return julianDates;
    }

    public List<UnitQuaternion> getMultipleQuaternions() {
        return multipleQuaternions;
    }

    public Reference getReference() {
        return reference;
    }

    public Reference getVelocityReference() {
        return velocityReference;
    }

    public UnitQuaternion getUnitQuaternion() {
        return unitQuaternion;
    }

    public CesiumInterpolationAlgorithm getInterpolationAlgorithm() {
        return interpolationAlgorithm;
    }
}
