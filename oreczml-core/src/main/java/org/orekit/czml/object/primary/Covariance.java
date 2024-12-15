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
package org.orekit.czml.object.primary;

import cesiumlanguagewriter.Cartesian;
import cesiumlanguagewriter.CesiumOutputStream;
import cesiumlanguagewriter.CesiumStreamWriter;
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
import org.orekit.czml.archi.builder.CovarianceBuilder;
import org.orekit.czml.object.Utils.DateUtils;
import org.orekit.czml.object.primary.entities.Satellite;
import org.orekit.czml.object.secondary.CzmlEllipsoid;
import org.orekit.czml.object.secondary.Orientation;
import org.orekit.frames.LOF;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.StateCovariance;
import org.orekit.utils.AngularCoordinates;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * covariance
 *
 * <p> This class builds the covariance as an ellipsoid around a satellite. </p>
 *
 * @author Julien LEBLOND
 * @since 1.0.0
 */
public class Covariance extends AbstractPrimaryObject {

    // STATICS
    /**
     * The default string to call the ID of the covariance.
     */
    public static final String DEFAULT_ID = "COV/";

    /**
     * The default string to call the name of the covariance.
     */
    public static final String DEFAULT_NAME = "Covariance of ";

    /**
     * The default string to call the reference position of an object.
     */
    public static final String DEFAULT_H_POSITION = "#position";

    /**
     * The default color of the ellipsoid.
     */
    public static final Color DEFAULT_COLOR = new Color(255, 255, 0, 255);

    // Parameters

    /**
     * The orientation used as a reference to keep the ellipsoid in the good orientation.
     */
    private final Reference orientationReference = null;

    /**
     * A list of all the State Covariance object {@link org.orekit.propagation.StateCovariance}.
     */
    private final List<StateCovariance> covarianceList;

    /**
     * The position used as a reference to keep the ellipsoid at the same place as the satellite.
     */
    private final Reference positionReference;


    // Intrinsic parameters

    /**
     * The satellite which the ellipsoid will be around.
     */
    private Satellite satellite;

    // Other arguments

    /**
     * The list of all the spacecraft states of the satellite.
     */
    private List<SpacecraftState> spaceCraftStates;

    /**
     * All the julian dates of each step of computation in a list.
     */
    private List<JulianDate> julianDates;

    /**
     * The dimensions of the ellipsoids in time in cartesian.
     */
    private List<Cartesian> dimensionsOfEllipsoids = new ArrayList<>();

    /**
     * A list of all the attitudes of the satellite used to orientate the covariance when the reference orientation si not used.
     */
    private List<Attitude> attitudes = new ArrayList<>();

    /**
     * When a single ellipsoid is computed, this argument will be used.
     */
    private CzmlEllipsoid uniqueEllipsoid;

    /** The header considered. */
    private Header header;

    // Constructors

    // The following constructors build several ellipsoids to follow the satellite

    /**
     * This builder classically uses the satellite and an initial covariance to build a covariance.
     *
     * @param satellite   : The satellite used to build the covariance around.
     * @param covariances : The list of all the covariances computed.
     * @param lof         : The lof of the satellite
     * @param header      : The header to consider when several are used.
     */
    public Covariance(final Satellite satellite, final List<StateCovariance> covariances, final LOF lof,
                      final Header header) {

        this(satellite, covariances, lof, DEFAULT_COLOR, DEFAULT_ID + satellite.getId(), header);
    }

    /**
     * The classic builder with a given color for the ellipsoid.
     *
     * @param satellite   : The satellite used to build the covariance around.
     * @param covariances : The initial covariance.
     * @param lof         : The lof of the satellite
     * @param color       : The color of the ellipsoid.
     * @param customID    : The custom ID of the covariance object
     * @param header      : The header to consider when several are used.
     */
    public Covariance(final Satellite satellite, final List<StateCovariance> covariances, final LOF lof,
                      final Color color, final String customID, final Header header) {

        this.satellite        = satellite;
        this.header           = header;
        this.spaceCraftStates = satellite.getSpaceCraftStates();
        this.setId(customID);
        this.setName(DEFAULT_NAME + satellite.getName());
        this.setAvailability(satellite.getAvailability());
        this.julianDates       = Collections.unmodifiableList(
                DateUtils.toJulianDates(satellite.getAbsoluteDateList(),
                        header.getTimeScale()));
        this.positionReference = new Reference(satellite.getId() + DEFAULT_H_POSITION);
        this.covarianceList    = new ArrayList<>(covariances);
        this.postComputation(color, lof);
    }

    // Builders

    /**
     * Builder covariance builder.
     *
     * @param satelliteInput   the satellite input
     * @param covariancesInput the covariances input
     * @param lofInput         the lof input
     * @param header           the header
     * @return the covariance builder
     */
    public static CovarianceBuilder builder(final Satellite satelliteInput,
                                            final List<StateCovariance> covariancesInput, final LOF lofInput,
                                            final Header header) {
        return new CovarianceBuilder(satelliteInput, covariancesInput, lofInput, header);
    }


    // Overrides

    /**
     * The generation function for the CZML file for the covariance.
     */
    @Override
    public void writeCzmlBlock(final CesiumStreamWriter stream, final CesiumOutputStream output) {
        output.setPrettyFormatting(true);
        try (PacketCesiumWriter packet = stream.openPacket(output)) {
            packet.writeId(getId());
            packet.writeName(getName());
            packet.writeAvailability(getAvailability());

            final Orientation orientation = Orientation.builder(attitudes, satellite.getFrame(), header)
                                                       .withInvertToITRF(false)
                                                       .build();
            orientation.write(packet, output);

            packet.writePositionPropertyReference(positionReference);

            this.uniqueEllipsoid.write(packet, output);
        }
    }


    // Getters

    /**
     * This getter returns the satellite.
     *
     * @return : The satellite used.
     */
    public Satellite getSatellite() {
        return satellite;
    }


    /**
     * This getter returns the reference frame of the ellipsoid.
     *
     * @return : The reference frame used, always inertial.
     */
    public String getReferenceFrame() {
        return "INERTIAL";
    }

    /**
     * This getter returns the reference used for the orientation of the ellipsoid.
     *
     * @return : The reference for the orientation used.
     */
    public Reference getOrientationReference() {
        return orientationReference;
    }

    /**
     * This getter returns the list of state covariance of the satellite.
     *
     * @return : The list of state covariance used.
     */
    public List<StateCovariance> getCovarianceList() {
        return Collections.unmodifiableList(covarianceList);
    }

    /**
     * This getter returns the attitudes of the satellite.
     *
     * @return : The attitudes of the satellite.
     */
    public List<Attitude> getAttitudes() {
        return Collections.unmodifiableList(attitudes);
    }

    /**
     * This getter returns the reference used for the position of the ellipsoid.
     *
     * @return : The reference used for the position.
     */
    public Reference getPositionReference() {
        return positionReference;
    }

    /**
     * This getter returns the list of julian date at each step of time.
     *
     * @return : The list of julian date used.
     */
    public List<JulianDate> getJulianDates() {
        return Collections.unmodifiableList(julianDates);
    }

    /**
     * Gets space craft states.
     *
     * @return the space craft states
     */
    public List<SpacecraftState> getSpaceCraftStates() {
        return Collections.unmodifiableList(spaceCraftStates);
    }

    /**
     * Gets dimensions of ellipsoids.
     *
     * @return the dimensions of ellipsoids
     */
    public List<Cartesian> getDimensionsOfEllipsoids() {
        return Collections.unmodifiableList(dimensionsOfEllipsoids);
    }

    /**
     * Gets unique ellipsoid.
     *
     * @return the unique ellipsoid
     */
    public CzmlEllipsoid getUniqueEllipsoid() {
        return uniqueEllipsoid;
    }

    // Private functions

    /**
     * This function aims at compute all the arguments after the propagation.
     *
     * @param color    : The color of the ellipsoid
     * @param lofInput : The local orbital frame of the satellite
     */
    private void postComputation(final Color color, final LOF lofInput) {
        for (int i = 0; i < covarianceList.size(); i++) {
            final StateCovariance covariance = covarianceList.get(i);

            final StateCovariance covarianceLof = covariance.changeCovarianceFrame(spaceCraftStates.get(i)
                                                                                                   .getOrbit(),
                    lofInput);

            final RealMatrix realMatrix = covarianceLof.getMatrix()
                                                       .getSubMatrix(0, 2, 0, 2);

            final EigenDecompositionNonSymmetric decomposition      = new EigenDecompositionNonSymmetric(realMatrix);
            final RealMatrix                     matrixEigenVectors = decomposition.getV();
            final RealMatrix                     copyOfEigenVectors = matrixEigenVectors.copy();
            final double[][] dataEigenValues = decomposition.getD()
                                                            .getData();

            final boolean inverted = invertedDetermination(matrixEigenVectors, copyOfEigenVectors);

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
                    spaceCraftStates.get(i)
                                    .getPVCoordinates());
            final AngularCoordinates angularCoordinatesRotated = new AngularCoordinates(rotationFromLOF);
            final Attitude currentAttitudeCovariance = new Attitude(spaceCraftStates.get(i)
                                                                                    .getDate(),
                    spaceCraftStates.get(0)
                                    .getFrame(), angularCoordinatesRotated);
            attitudes.add(currentAttitudeCovariance);
        }

        this.uniqueEllipsoid = CzmlEllipsoid.builder(julianDates, dimensionsOfEllipsoids, header)
                                            .withColor(color)
                                            .build();
    }

    /**
     * This function aims at determining the orientation of the satellite system if it is left or right-handed.
     *
     * @param matrixEigenVectors : The matrix of the eigen vectors of the covariance.
     * @param copyOfEigenVectors : The copy of the first matrix.
     * @return : A boolean that is true if it is left-handed, false if it is right-handed.
     */
    private boolean invertedDetermination(final RealMatrix matrixEigenVectors, final RealMatrix copyOfEigenVectors) {

        boolean inverted = false;

        try {
            new Rotation(matrixEigenVectors.getData(), 0.01);
        }

        // If the rotation failed, it's that the determinant of the matrix is equal to -1, this means that the system is
        // left-handed; to invert it, we need to invert two columns; this way the determinant will be multiplied by -1.
        // We will need to invert the eigen values related to the eigen vectors that we inverted.
        catch (MathIllegalArgumentException e) {
            inverted = true;
            final RealVector firstColumnMatrix  = matrixEigenVectors.getColumnVector(0);
            final RealVector secondColumnMatrix = matrixEigenVectors.getColumnVector(1);
            final RealVector thirdColumnMatrix  = matrixEigenVectors.getColumnVector(2);
            copyOfEigenVectors.setColumnVector(0, firstColumnMatrix);
            copyOfEigenVectors.setColumnVector(1, thirdColumnMatrix);
            copyOfEigenVectors.setColumnVector(2, secondColumnMatrix);
        }
        return inverted;
    }
}
