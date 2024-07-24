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
import org.orekit.czml.CzmlObjects.CzmlSecondaryObjects.CzmlEllipsoid;
import org.orekit.czml.CzmlObjects.CzmlSecondaryObjects.Orientation;
import org.orekit.czml.CzmlObjects.CzmlSecondaryObjects.TimePosition;
import org.orekit.czml.CzmlObjects.Position;
import org.orekit.attitudes.Attitude;
import org.orekit.files.ccsds.ndm.odm.CartesianCovariance;
import org.orekit.files.ccsds.ndm.odm.oem.Oem;
import org.orekit.frames.FramesFactory;
import org.orekit.frames.LOF;
import org.orekit.frames.LOFType;
import org.orekit.propagation.MatricesHarvester;
import org.orekit.propagation.Propagator;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.StateCovariance;
import org.orekit.propagation.StateCovarianceMatrixProvider;
import org.orekit.time.TimeScale;
import org.orekit.utils.AngularCoordinates;
import org.orekit.utils.TimeStampedPVCoordinates;

import java.awt.Color;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Covariance Display

 * <p> This class builds the covariance as an ellipsoid around a satellite. </p>
 *
 * @since 1.0
 * @author Julien LEBLOND
 */

public class CovarianceDisplay extends AbstractPrimaryObject implements CzmlPrimaryObject {

    // STATICS
    /** The default string to call the ID of the covariance.*/
    public static final String DEFAULT_ID = "COV/";
    /** The default string to call the name of the covariance.*/
    public static final String DEFAULT_NAME = "Covariance of ";
    /** The default string to call the reference position of an object.*/
    public static final String DEFAULT_H_POSITION = "#position";

    // Parameters
    /** The position of the covariance.*/
    private Position position;
    /** The ellipsoid used to represents the covariance.*/
    private CzmlEllipsoid czmlEllipsoid;
    /** The reference frame of the covariance, here the inertial one is used by default.*/
    private final String referenceFrame = "INERTIAL";
    /** The position used as a reference to keep the ellipsoid at the same place as the satellite.*/
    private Reference positionReference = null;
    /** The orientation used as a reference to keep the ellipsoid in the good orientation.*/
    private final Reference orientationReference = null;
    /** .*/
    private Attitude attitude;
    /** .*/
    private List<SpacecraftState> allSpaceCraftStates = new ArrayList<>();

    // Intrinsic parameters
    /** The satellite which the ellipsoid will be around.*/
    private Satellite satellite;
    /** A list of all the ellipsoids computed in time, gathered in a list.*/
    private List<CzmlEllipsoid> ellipsoidList;
    /** All the julian dates of each step of computation in a list.*/
    private List<JulianDate> julianDates;
    /** A list of all the State Covariance object {@link org.orekit.propagation.StateCovariance}.*/
    private final List<StateCovariance> covarianceList = new ArrayList<>();
    /** .*/
    private List<Cartesian> dimensionsOfEllipsoids = new ArrayList<>();
    /** A list of all the attitudes of the satellite used to orientate the covariance when the reference orientation si not used.*/
    private List<Attitude> attitudes = new ArrayList<>();
    /** A list containing all the cartesian in time of the satellite. */
    private List<Cartesian> satelliteCartesianList = new ArrayList<>();
    /** .*/
    private TimeScale timeScale = null;
    /** .*/
    private CzmlEllipsoid uniqueEllipsoid;

    // BUILDERS

    // The following builders build several ellipsoids, in order to follow the satellite
    /** This builder classically use the satellite and an initial covariance to build a covariance.
     * @param satellite : The satellite used to build the covariance around.
     * @param lof : The lof of the satellite
     * @param initCovariance : The initial covariance.*/
    public CovarianceDisplay(final Satellite satellite, final StateCovariance initCovariance, final LOF lof) {
        this.satellite = satellite;
        this.setId(DEFAULT_ID + satellite.getId());
        this.setName(DEFAULT_NAME + satellite.getName());
        this.satelliteCartesianList = satellite.getCartesianArraylist();
        this.julianDates = absoluteDatelistToJulianDateList(satellite.getAbsoluteDateList());
        this.positionReference = new Reference(satellite.getId() + DEFAULT_H_POSITION);

        this.covariancePropagation(satellite, initCovariance);

        this.postPropagationComputation(Color.green, lof);
    }
    /** The classic builder with a given color for the ellipsoid.
     * @param satellite : The satellite used to build the covariance around.
     * @param initCovariance : The initial covariance.
     * @param lof : The lof of the satellite
     * @param color : The color of the ellipsoid. */
    public CovarianceDisplay(final Satellite satellite, final StateCovariance initCovariance, final LOF lof, final Color color) {
        this.satellite = satellite;
        this.setId(DEFAULT_ID + satellite.getId());
        this.setName(DEFAULT_NAME + satellite.getName());
        final List<Cartesian> tempSatelliteCartesian = satellite.getCartesianArraylist();
        final List<Double> timeList = satellite.getTimeList();
        final TimePosition tempTimePositions = new TimePosition(tempSatelliteCartesian, timeList);
        this.julianDates = tempTimePositions.getDates();
        this.positionReference = new Reference(satellite.getId() + DEFAULT_H_POSITION);

        this.covariancePropagation(satellite, initCovariance);

        this.postPropagationComputation(color, lof);
    }

    /** A covariance display builder from an OEMFile.
     * @param oem : An OEMFile in input.
     * @param lof : The lof of the satellite*/
    public CovarianceDisplay(final Oem oem, final LOF lof) throws URISyntaxException, IOException {
        this(oem, lof, new Color(255, 255, 0, 255));
    }
    /** A covariance display builder from an OEMFile with a given color for the ellipsoid.
     * @param oem : An OEMFile in input.
     * @param lof : The lof of the satellite
     * @param color : The color of the ellipsoid.*/
    public CovarianceDisplay(final Oem oem, final LOF lof, final Color color) throws URISyntaxException, IOException {
        //System.out.println("CHANGER TIMESCALE COVARIANCE DISPLAY");
        final Satellite satelliteInit = new Satellite(oem);

        final List<List<CartesianCovariance>> listOfCovariances = new ArrayList<>();
        for (int i = 0; i < oem.getSegments().size(); i++) {
            listOfCovariances.add(oem.getSegments().get(i).getCovarianceMatrices());
        }
        final List<CartesianCovariance> covariances = listOfCovariances.get(0);
        final CartesianCovariance initCartesianCovariance = covariances.get(0);
        final StateCovariance initInertialCovariance = new StateCovariance(initCartesianCovariance.getCovarianceMatrix(),
                julianDateToAbsoluteDate(Header.MASTER_CLOCK.getAvailability().getStart(), timeScale), LOFType.TNW);
        final StateCovariance initCovariance = initInertialCovariance.changeCovarianceFrame(satelliteInit.getOrbits().get(0), satelliteInit.getFrame());

        this.setId(DEFAULT_ID + satellite.getId());
        this.setName(DEFAULT_NAME + satellite.getName());
        this.satelliteCartesianList = satellite.getCartesianArraylist();
        final List<Double> timeList = satellite.getTimeList();
        final TimePosition tempTimePositions = new TimePosition(satelliteCartesianList, timeList);
        this.julianDates = tempTimePositions.getDates();
        this.positionReference = new Reference(satellite.getId() + DEFAULT_H_POSITION);

        this.covariancePropagation(satellite, initCovariance);

        this.postPropagationComputation(color, lof);
    }

    /** A covariance display builder from an ephemeris and an initial covariance.
     * @param ephemeris : A list of TimeStampedPVCoordinates {@link org.orekit.utils.TimeStampedPVCoordinates} that defines the ephemeris of an object.
     * @param lof : The lof of the satellite
     * @param initCovariance : The initial covariance.
     */
    public CovarianceDisplay(final List<TimeStampedPVCoordinates> ephemeris, final StateCovariance initCovariance, final LOF lof) throws URISyntaxException, IOException {
        this(ephemeris, initCovariance, lof, new Color(255, 0, 125, 255));
    }
    /** A covariance display builder from an ephemeris and an initial covariance with a given color for the ellipsoid.
     * @param ephemeris : A list of TimeStampedPVCoordinates {@link org.orekit.utils.TimeStampedPVCoordinates} that defines the ephemeris of an object.
     * @param lof : The lof of the satellite
     * @param initCovariance : The initial covariance.
     * @param color : The color of the ellipsoid.*/
    public CovarianceDisplay(final List<TimeStampedPVCoordinates> ephemeris, final StateCovariance initCovariance, final LOF lof, final Color color) throws URISyntaxException, IOException {
        this.satellite = new Satellite(Header.MASTER_CLOCK.getAvailability(), ephemeris, FramesFactory.getEME2000());
        this.setId(DEFAULT_ID + satellite.getId());
        this.setName(DEFAULT_NAME + satellite.getName());
        this.satelliteCartesianList = satellite.getCartesianArraylist();
        final List<Double> timeList = satellite.getTimeList();
        final TimePosition tempTimePositions = new TimePosition(satelliteCartesianList, timeList);
        this.julianDates = tempTimePositions.getDates();
        this.positionReference = new Reference(satellite.getId() + DEFAULT_H_POSITION);

        this.covariancePropagation(satellite, initCovariance);

        this.postPropagationComputation(color, lof);
    }

    // The following builders only build one ellipsoid
    /** A single ellipsoid builder to display it in a given position.
     * @param position : The position of the ellipsoid.
     * @param ellipsoid : The ellipsoid to display.
     * @param attitude  : The 'attitude' of the covariance. */
    public CovarianceDisplay(final Position position, final CzmlEllipsoid ellipsoid, final Attitude attitude) {
        this.setId(DEFAULT_ID + position.toString());
        this.setName(DEFAULT_NAME + position);
        this.position = position;
        this.czmlEllipsoid = ellipsoid;
        this.attitude = attitude;
    }

    /** A single ellipsoid builder from basic components.
     * @param id : The id of the packet to write.
     * @param name : The name of the packet to write.
     * @param position : The position of the ellipsoid.
     * @param ellipsoid : The ellipsoid to display.
     * @param attitude : The 'attitude of the covariance. */
    public CovarianceDisplay(final String id, final String name, final Position position, final CzmlEllipsoid ellipsoid, final Attitude attitude) {
        this.setId(id);
        this.setName(name);
        this.position = position;
        this.czmlEllipsoid = ellipsoid;
        this.attitude = attitude;
    }

    /** The generation function for the CZML file for the covariance display.*/
    public void writeCzmlBlock() {
        if (julianDates.isEmpty()) {
            OUTPUT.setPrettyFormatting(true);
            try (PacketCesiumWriter packet = STREAM.openPacket(OUTPUT)) {
                packet.writeId(getId());
                packet.writeName(getName());

                //final Orientation orientation = new Orientation(attitude, satellite.getFrame(), true);
                //orientation.write(packet, OUTPUT);

                position.write(packet, OUTPUT, Header.MASTER_CLOCK.getAvailability(), referenceFrame);

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

    /** This getter return the satellite.
     * @return : The satellite used. */
    public Satellite getSatellite() {
        return satellite;
    }

    /** This getter return the position of the ellipsoid.
     * @return : The position used. */
    public Position getPosition() {
        return position;
    }

    /** This getter return the reference frame of the ellipsoid.
     * @return : The reference frame used. */
    public String getReferenceFrame() {
        return referenceFrame;
    }

    /** This getter return the reference used for the orientation of the ellipsoid.
     * @return : The reference for the orientation used. */
    public Reference getOrientationReference() {
        return orientationReference;
    }

    /** This getter return the list of cartesian position of the satellite.
     * @return : The list of cartesian used. */
    public List<Cartesian> getSatelliteCartesianList() {
        return satelliteCartesianList;
    }

    /** This getter return the list of state covariance of the satellite.
     * @return : The list of state covariance used. */
    public List<StateCovariance> getCovarianceList() {
        return covarianceList;
    }

    /** This getter return the attitudes of the satellite.
     * @return : The attitudes of the satellite.*/
    public List<Attitude> getAttitudes() {
        return attitudes;
    }

    /** This getter return the reference used for the position of the ellipsoid.
     * @return : The reference used for the position. */
    public Reference getPositionReference() {
        return positionReference;
    }

    /** This getter return the list of julian date at each step of time.
     * @return : The list of julian date used. */
    public List<JulianDate> getJulianDates() {
        return julianDates;
    }

    // Overrides
    /** This function returns the string writer of the covariance display.*/
    @Override
    public StringWriter getStringWriter() {
        return null;
    }

    public CzmlEllipsoid getCzmlEllipsoid() {
        return czmlEllipsoid;
    }

    public Attitude getAttitude() {
        return attitude;
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

    public TimeScale getTimeScale() {
        return timeScale;
    }

    public CzmlEllipsoid getUniqueEllipsoid() {
        return uniqueEllipsoid;
    }

    /** This function cleans all the private attributes to be used for another object.*/
    @Override
    public void cleanObject() {
        this.setId("");
        this.setName("");
        this.position = null;
        this.czmlEllipsoid = null;
        this.satellite = null;
        this.ellipsoidList = new ArrayList<>();
        this.attitude = null;
        this.attitudes = new ArrayList<>();
    }

    // Functions
    /** This function propagates the covariance using the propagator of the satellite and an initial state covariance.
     * @param inputSatellite : The satellite which the covariance will be around.
     * @param initCovariance : The initial state covariance.*/
    private void covariancePropagation(final Satellite inputSatellite, final StateCovariance initCovariance) {
        final Propagator propagator = inputSatellite.getSatellitePropagator();

        propagator.resetInitialState(satellite.getAllSpaceCraftStates().get(0));

        inputSatellite.setAttitudes(new ArrayList<>());

//        final RealMatrix covInitMatrix = initCovariance.getMatrix();
//        System.out.println(covInitMatrix);

        final String stm = "stm";

        final MatricesHarvester harvester = propagator.setupMatricesComputation(stm, null, null);

        final StateCovarianceMatrixProvider provider = new StateCovarianceMatrixProvider("covariance", stm,
                harvester, initCovariance);

        propagator.addAdditionalStateProvider(provider);

        propagator.getMultiplexer().add(Header.MASTER_CLOCK.getMultiplier(), spacecraftState -> {
            final StateCovariance covariance = provider.getStateCovariance(spacecraftState);
            covarianceList.add(covariance);
            allSpaceCraftStates.add(spacecraftState);
        });

        propagator.propagate(inputSatellite.getOrbits().get(0).getDate(), inputSatellite.getOrbits().get(inputSatellite.getOrbits().size() - 1).getDate());

    }

    private void postPropagationComputation(final Color color, final LOF lofInput) {
        for (int i = 0; i < covarianceList.size(); i++) {
            final StateCovariance covariance = covarianceList.get(i);
            final RealMatrix realMatrix = covariance.getMatrix().getSubMatrix(0, 2, 0, 2);
//            System.out.println(realMatrix);
            //final RealMatrix realMatrix = MatrixUtils.createRealDiagonalMatrix(new double[] {1000, 100, 100, 1e-2, 1e-2, 1e-2}).getSubMatrix(0, 2, 0, 2);

            final EigenDecompositionNonSymmetric decomposition = new EigenDecompositionNonSymmetric(realMatrix);
            final RealMatrix matrixEigenVectors = decomposition.getV();
            final RealMatrix copyOfEigenVectors = matrixEigenVectors.copy();
            final double[][] dataEigenValues = decomposition.getD().getData();
            boolean inverted = false;
            Rotation currentRotation;
            try {
                currentRotation = new Rotation(matrixEigenVectors.getData(), 0.01);
            }
            // If the rotation failed, it's that the determinant of the matrix is equal to -1, this means that the system is
            // left-handed, to invert it we need to invert two columns, this way the determinant will be multiplied by -1.
            // We will need to invert the eigen values related to the eigen vectors that we inverted.
            catch (MathIllegalArgumentException e) {
                inverted = true;
                final RealVector firstColumnMatrix = matrixEigenVectors.getColumnVector(0);
                final RealVector secondColumnMatrix = matrixEigenVectors.getColumnVector(1);
                final RealVector thirdColumnMatrix = matrixEigenVectors.getColumnVector(2);
                copyOfEigenVectors.setColumnVector(0, firstColumnMatrix);
                copyOfEigenVectors.setColumnVector(1, thirdColumnMatrix);
                copyOfEigenVectors.setColumnVector(2, secondColumnMatrix);
                currentRotation = new Rotation(copyOfEigenVectors.getData(), 0.01);
            }

            if (inverted) {
                dimensionsOfEllipsoids.add(new Cartesian(dataEigenValues[0][0], dataEigenValues[2][2], dataEigenValues[1][1]));
            }
            else {
                dimensionsOfEllipsoids.add(new Cartesian(dataEigenValues[0][0], dataEigenValues[1][1], dataEigenValues[2][2]));
            }

            final Rotation rotationFromLOF = lofInput.rotationFromInertial(allSpaceCraftStates.get(i).getDate(), allSpaceCraftStates.get(i).getPVCoordinates());
//            final Rotation finalRotation = currentRotation.compose(rotationFromLOF, RotationConvention.VECTOR_OPERATOR);
            final AngularCoordinates angularCoordinatesRotated = new AngularCoordinates(rotationFromLOF);
            final Attitude currentAttitudeCovariance = new Attitude(allSpaceCraftStates.get(i).getDate(), allSpaceCraftStates.get(0).getFrame(), angularCoordinatesRotated);
            attitudes.add(currentAttitudeCovariance);
        }
        this.uniqueEllipsoid = new CzmlEllipsoid(julianDates, dimensionsOfEllipsoids, color);
    }

    /** This function basically converts an array of reference into an iterable of reference.
     * @param array : The array to convert.
     * @return : An iterable of references.*/
    private static Iterable<Reference> convertToIterable(final Reference[] array) {
        return () -> Arrays.stream(array).iterator();
    }
}
