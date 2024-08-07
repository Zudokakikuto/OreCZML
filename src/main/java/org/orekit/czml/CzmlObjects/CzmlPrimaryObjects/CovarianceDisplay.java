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
package org.orekit.czml.CzmlObjects.CzmlPrimaryObjects;

import cesiumlanguagewriter.Cartesian;
import cesiumlanguagewriter.JulianDate;
import cesiumlanguagewriter.PacketCesiumWriter;
import cesiumlanguagewriter.Reference;
import org.hipparchus.exception.MathIllegalArgumentException;
import org.hipparchus.geometry.euclidean.threed.Rotation;
import org.hipparchus.linear.EigenDecompositionNonSymmetric;
import org.hipparchus.linear.RealMatrix;
import org.hipparchus.linear.RealVector;
import org.hipparchus.util.FastMath;
import org.orekit.attitudes.Attitude;
import org.orekit.czml.ArchiObjects.CovarianceDisplayBuilder;
import org.orekit.czml.CzmlObjects.CzmlSecondaryObjects.CzmlEllipsoid;
import org.orekit.czml.CzmlObjects.CzmlSecondaryObjects.Orientation;
import org.orekit.czml.CzmlObjects.Position;
import org.orekit.frames.LOF;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.StateCovariance;
import org.orekit.utils.AngularCoordinates;

import java.awt.Color;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Covariance Display
 *
 * <p> This class builds the covariance as an ellipsoid around a satellite. </p>
 *
 * @author Julien LEBLOND
 * @since 1.0.0
 */

public class CovarianceDisplay extends AbstractPrimaryObject implements CzmlPrimaryObject {

    // STATICS
    /** The default string to call the ID of the covariance. */
    public static final String DEFAULT_ID = "COV/";

    /** The default string to call the name of the covariance. */
    public static final String DEFAULT_NAME = "Covariance of ";

    /** The default string to call the reference position of an object. */
    public static final String DEFAULT_H_POSITION = "#position";


    // Parameters
    /** The reference frame of the covariance, here the inertial one is used by default. */
    private final String referenceFrame = "INERTIAL";

    /** The orientation used as a reference to keep the ellipsoid in the good orientation. */
    private final Reference orientationReference = null;

    /** A list of all the State Covariance object {@link org.orekit.propagation.StateCovariance}. */
    private List<StateCovariance> covarianceList;
    /** The position of the covariance. */
    private Position              position;

    /** The ellipsoid used to represents the covariance. */
    private CzmlEllipsoid czmlEllipsoid;

    /** The position used as a reference to keep the ellipsoid at the same place as the satellite. */
    private Reference positionReference;


    // Intrinsic parameters
    /** The list of all the spacecraft states of the satellite. */
    private List<SpacecraftState> allSpaceCraftStates = new ArrayList<>();

    /** The satellite which the ellipsoid will be around. */
    private Satellite satellite;

    /** A list of all the ellipsoids computed in time, gathered in a list. */
    private List<CzmlEllipsoid> ellipsoidList;

    /** All the julian dates of each step of computation in a list. */
    private List<JulianDate> julianDates;

    /** The dimensions of the ellipsoids in time in cartesian. */
    private List<Cartesian> dimensionsOfEllipsoids = new ArrayList<>();

    /** A list of all the attitudes of the satellite used to orientate the covariance when the reference orientation si not used. */
    private List<Attitude> attitudes = new ArrayList<>();

    /** A list containing all the cartesian in time of the satellite. */
    private List<Cartesian> satelliteCartesianList = new ArrayList<>();

    /** When a single ellipsoid is computed, this argument will be used. */
    private CzmlEllipsoid uniqueEllipsoid;


    // Constructors

    // The following constructors build several ellipsoids, in order to follow the satellite

    /**
     * This builder classically use the satellite and an initial covariance to build a covariance.
     *
     * @param satellite   : The satellite used to build the covariance around.
     * @param lof         : The lof of the satellite
     * @param covariances : The list of all the covariances computed.
     */
    public CovarianceDisplay(final Satellite satellite, final List<StateCovariance> covariances, final LOF lof) {
        this.satellite = satellite;
        this.setId(DEFAULT_ID + satellite.getId());
        this.setName(DEFAULT_NAME + satellite.getName());
        this.satelliteCartesianList = satellite.getCartesianArraylist();
        this.julianDates            = absoluteDatelistToJulianDateList(satellite.getAbsoluteDateList(),
                Header.getTimeScale());
        this.positionReference      = new Reference(satellite.getId() + DEFAULT_H_POSITION);
        this.covarianceList         = covariances;

        this.postComputation(Color.green, lof);
    }

    /**
     * The classic builder with a given color for the ellipsoid.
     *
     * @param satellite   : The satellite used to build the covariance around.
     * @param covariances : The initial covariance.
     * @param lof         : The lof of the satellite
     * @param color       : The color of the ellipsoid.
     */
    public CovarianceDisplay(final Satellite satellite, final List<StateCovariance> covariances, final LOF lof,
                             final Color color) {
        this.satellite           = satellite;
        this.allSpaceCraftStates = satellite.getAllSpaceCraftStates();
        this.setId(DEFAULT_ID + satellite.getId());
        this.setName(DEFAULT_NAME + satellite.getName());
        this.julianDates       = absoluteDatelistToJulianDateList(satellite.getAbsoluteDateList(),
                Header.getTimeScale());
        this.positionReference = new Reference(satellite.getId() + DEFAULT_H_POSITION);
        this.covarianceList    = covariances;
        this.postComputation(color, lof);
    }

    // Builders

    public static CovarianceDisplayBuilder builder(final Satellite satelliteInput,
                                                   final List<StateCovariance> covariancesInput, final LOF lofInput) {
        return new CovarianceDisplayBuilder(satelliteInput, covariancesInput, lofInput);
    }

    /** The generation function for the CZML file for the covariance display. */
    public void writeCzmlBlock() {
        if (julianDates.isEmpty()) {
            OUTPUT.setPrettyFormatting(true);
            try (PacketCesiumWriter packet = STREAM.openPacket(OUTPUT)) {
                packet.writeId(getId());
                packet.writeName(getName());

                position.write(packet, OUTPUT, Header.getMasterClock()
                                                     .getAvailability(), referenceFrame);

                czmlEllipsoid.write(packet, OUTPUT);
            }
        } else {
            OUTPUT.setPrettyFormatting(true);
            try (PacketCesiumWriter packet = STREAM.openPacket(OUTPUT)) {
                packet.writeId(getId() + Arrays.toString(Arrays.copyOfRange(julianDates.toArray(), 0, 10)));
                packet.writeName(getName());

                final Orientation orientation = new Orientation(attitudes, satellite.getFrame(), false);
                orientation.write(packet, OUTPUT);

                packet.writePositionPropertyReference(positionReference);

                this.uniqueEllipsoid.write(packet, OUTPUT);
            }
        }
        cleanObject();
    }

    // GETS

    /**
     * This getter return the satellite.
     *
     * @return : The satellite used.
     */
    public Satellite getSatellite() {
        return satellite;
    }

    /**
     * This getter return the position of the ellipsoid.
     *
     * @return : The position used.
     */
    public Position getPosition() {
        return position;
    }

    /**
     * This getter return the reference frame of the ellipsoid.
     *
     * @return : The reference frame used.
     */
    public String getReferenceFrame() {
        return referenceFrame;
    }

    /**
     * This getter return the reference used for the orientation of the ellipsoid.
     *
     * @return : The reference for the orientation used.
     */
    public Reference getOrientationReference() {
        return orientationReference;
    }

    /**
     * This getter return the list of cartesian position of the satellite.
     *
     * @return : The list of cartesian used.
     */
    public List<Cartesian> getSatelliteCartesianList() {
        return satelliteCartesianList;
    }

    /**
     * This getter return the list of state covariance of the satellite.
     *
     * @return : The list of state covariance used.
     */
    public List<StateCovariance> getCovarianceList() {
        return covarianceList;
    }

    /**
     * This getter return the attitudes of the satellite.
     *
     * @return : The attitudes of the satellite.
     */
    public List<Attitude> getAttitudes() {
        return attitudes;
    }

    /**
     * This getter return the reference used for the position of the ellipsoid.
     *
     * @return : The reference used for the position.
     */
    public Reference getPositionReference() {
        return positionReference;
    }

    /**
     * This getter return the list of julian date at each step of time.
     *
     * @return : The list of julian date used.
     */
    public List<JulianDate> getJulianDates() {
        return julianDates;
    }

    public CzmlEllipsoid getCzmlEllipsoid() {
        return czmlEllipsoid;
    }

    public List<SpacecraftState> getAllSpaceCraftStates() {
        return allSpaceCraftStates;
    }

    public List<CzmlEllipsoid> getEllipsoidList() {
        return ellipsoidList;
    }

    public List<Cartesian> getDimensionsOfEllipsoids() {
        return dimensionsOfEllipsoids;
    }

    public CzmlEllipsoid getUniqueEllipsoid() {
        return uniqueEllipsoid;
    }

    // Overrides

    /** This function returns the string writer of the covariance display. */
    @Override
    public StringWriter getStringWriter() {
        return null;
    }

    // Functions

    /** This function cleans all the private attributes to be used for another object. */
    @Override
    public void cleanObject() {
        this.setId("");
        this.setName("");
        this.position        = null;
        this.czmlEllipsoid   = null;
        this.satellite       = null;
        this.ellipsoidList   = new ArrayList<>();
        this.attitudes       = new ArrayList<>();
        this.uniqueEllipsoid = null;
    }


    private void postComputation(final Color color, final LOF lofInput) {
        for (int i = 0; i < covarianceList.size(); i++) {
            final StateCovariance covariance = covarianceList.get(i);
            final RealMatrix realMatrix = covariance.getMatrix()
                                                    .getSubMatrix(0, 2, 0, 2);

            final EigenDecompositionNonSymmetric decomposition      = new EigenDecompositionNonSymmetric(realMatrix);
            final RealMatrix                     matrixEigenVectors = decomposition.getV();
            final RealMatrix                     copyOfEigenVectors = matrixEigenVectors.copy();
            final double[][] dataEigenValues = decomposition.getD()
                                                            .getData();
            boolean  inverted = false;
            Rotation currentRotation;
            try {
                currentRotation = new Rotation(matrixEigenVectors.getData(), 0.01);
            }
            // If the rotation failed, it's that the determinant of the matrix is equal to -1, this means that the system is
            // left-handed, to invert it we need to invert two columns, this way the determinant will be multiplied by -1.
            // We will need to invert the eigen values related to the eigen vectors that we inverted.
            catch (MathIllegalArgumentException e) {
                inverted = true;
                final RealVector firstColumnMatrix  = matrixEigenVectors.getColumnVector(0);
                final RealVector secondColumnMatrix = matrixEigenVectors.getColumnVector(1);
                final RealVector thirdColumnMatrix  = matrixEigenVectors.getColumnVector(2);
                copyOfEigenVectors.setColumnVector(0, firstColumnMatrix);
                copyOfEigenVectors.setColumnVector(1, thirdColumnMatrix);
                copyOfEigenVectors.setColumnVector(2, secondColumnMatrix);
                currentRotation = new Rotation(copyOfEigenVectors.getData(), 0.01);
            }

            if (inverted) {
                dimensionsOfEllipsoids.add(
                        new Cartesian(FastMath.sqrt(dataEigenValues[0][0]), FastMath.sqrt(dataEigenValues[2][2]),
                                FastMath.sqrt(dataEigenValues[1][1])));
            } else {
                dimensionsOfEllipsoids.add(
                        new Cartesian(FastMath.sqrt(dataEigenValues[0][0]), FastMath.sqrt(dataEigenValues[1][1]),
                                FastMath.sqrt(dataEigenValues[2][2])));
            }

            final Rotation rotationFromLOF = lofInput.rotationFromInertial(covariance.getDate(),
                    allSpaceCraftStates.get(i)
                                       .getPVCoordinates());
            final AngularCoordinates angularCoordinatesRotated = new AngularCoordinates(rotationFromLOF);
            final Attitude currentAttitudeCovariance = new Attitude(allSpaceCraftStates.get(i)
                                                                                       .getDate(),
                    allSpaceCraftStates.get(0)
                                       .getFrame(), angularCoordinatesRotated);
            attitudes.add(currentAttitudeCovariance);
        }

        this.uniqueEllipsoid = new CzmlEllipsoid(julianDates, dimensionsOfEllipsoids, color);
    }
}
