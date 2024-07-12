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
import cesiumlanguagewriter.OrientationCesiumWriter;
import cesiumlanguagewriter.PacketCesiumWriter;
import cesiumlanguagewriter.Reference;
import cesiumlanguagewriter.TimeInterval;
import cesiumlanguagewriter.UnitQuaternion;
import org.orekit.czml.CzmlObjects.CzmlSecondaryObjects.CzmlEllipsoid;
import org.orekit.czml.CzmlObjects.CzmlSecondaryObjects.SatelliteObjects.SatellitePosition;
import org.orekit.czml.CzmlObjects.Position;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.hipparchus.linear.RealMatrix;
import org.hipparchus.util.FastMath;
import org.orekit.attitudes.Attitude;
import org.orekit.files.ccsds.ndm.odm.CartesianCovariance;
import org.orekit.files.ccsds.ndm.odm.oem.Oem;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.frames.LOFType;
import org.orekit.frames.Transform;
import org.orekit.orbits.OrbitType;
import org.orekit.orbits.PositionAngleType;
import org.orekit.propagation.BoundedPropagator;
import org.orekit.propagation.MatricesHarvester;
import org.orekit.propagation.Propagator;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.StateCovariance;
import org.orekit.propagation.StateCovarianceMatrixProvider;
import org.orekit.propagation.sampling.OrekitFixedStepHandler;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScale;
import org.orekit.utils.IERSConventions;
import org.orekit.utils.PVCoordinates;
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

    // Intrinsic parameters
    /** The satellite which the ellipsoid will be around.*/
    private Satellite satellite;
    /** A list of all the ellipsoids computed in time, gathered in a list.*/
    private List<CzmlEllipsoid> ellipsoidList;
    /** All the julian dates of each step of computation in a list.*/
    private List<JulianDate> julianDates;
    /** A list of all the State Covariance object {@link org.orekit.propagation.StateCovariance}.*/
    private final List<StateCovariance> covarianceList = new ArrayList<>();
    /** A list of UnitQuaternions used to orientate the covariance when the reference orientation is not used.*/
    private final List<UnitQuaternion> unitQuaternions = new ArrayList<>();
    /** A list of all the attitudes of the satellite used to orientate the covariance when the reference orientation si not used.*/
    private final List<Attitude> attitudes = new ArrayList<>();
    /** A list containing all the cartesian in time of the satellite. */
    private List<Cartesian> satelliteCartesianList = new ArrayList<>();
    /** .*/
    private TimeScale timeScale = null;

    // BUILDERS

    // The following builders build several ellipsoids, in order to follow the satellite
    /** This builder classically use the satellite and a initial covariance to build a covariance.
     * @param satellite : The satellite used to build the covariance around.
     * @param initCovariance : The initial covariance.*/
    public CovarianceDisplay(final Satellite satellite, final StateCovariance initCovariance) {
        this.satellite = satellite;
        this.setId(DEFAULT_ID + satellite.getId());
        this.setName(DEFAULT_NAME + satellite.getName());
        final List<Cartesian> tempSatelliteCartesian = satellite.getCartesianArraylist();
        final List<Double> timeList = satellite.getTimeList();
        final List<CzmlEllipsoid> tempEllipsoids = new ArrayList<>();
        final SatellitePosition tempSatellitePositions = new SatellitePosition(tempSatelliteCartesian, timeList);
        this.julianDates = tempSatellitePositions.getDates();
        this.positionReference = new Reference(satellite.getId() + DEFAULT_H_POSITION);

        this.covariancePropagation(satellite, initCovariance);

        for (int i = 0; i < tempSatelliteCartesian.size() - 1; i++) {
            // Ellipsoids
            final TimeInterval currentTimeInterval = new TimeInterval(julianDates.get(i), julianDates.get(i).addSeconds(Header.MASTER_CLOCK.getMultiplier()));
            final StateCovariance currentCovariance = covarianceList.get(i);
            final RealMatrix currentMatrix = currentCovariance.getMatrix();
            final double toTransformSigmaX = FastMath.sqrt(currentMatrix.getRow(0)[0]);
            final double toTransformSigmaY = FastMath.sqrt(currentMatrix.getRow(1)[1]);
            final double toTransformSigmaZ = FastMath.sqrt(currentMatrix.getRow(2)[2]);

            final Vector3D vectorInertial = new Vector3D(toTransformSigmaX, toTransformSigmaY, toTransformSigmaZ);
            final PVCoordinates toTransformIntoITRF = new PVCoordinates(vectorInertial);
            final Transform transformFromInertialToIRTF = FramesFactory.getEME2000().getTransformTo(FramesFactory.getITRF(IERSConventions.IERS_2010, true), julianDateToAbsoluteDate(currentTimeInterval.getStart(), timeScale));
            final PVCoordinates transformedPVCoordinates = transformFromInertialToIRTF.transformPVCoordinates(toTransformIntoITRF);

            final double transformedSigmaX = transformedPVCoordinates.getPosition().getX();
            final double transformedSigmaY = transformedPVCoordinates.getPosition().getY();
            final double transformedSigmaZ = transformedPVCoordinates.getPosition().getZ();

            final Cartesian transformedCartesian = new Cartesian(transformedSigmaX, transformedSigmaY, transformedSigmaZ);
            tempEllipsoids.add(new CzmlEllipsoid(currentTimeInterval, transformedCartesian));
        }
        this.ellipsoidList = tempEllipsoids;
        this.satelliteCartesianList = tempSatelliteCartesian;
    }
    /** The classic builder with a given color for the ellipsoid.
     * @param satellite : The satellite used to build the covariance around.
     * @param initCovariance : The initial covariance.
     * @param color : The color of the ellipsoid. */
    public CovarianceDisplay(final Satellite satellite, final StateCovariance initCovariance, final Color color) {
        this.satellite = satellite;
        this.setId(DEFAULT_ID + satellite.getId());
        this.setName(DEFAULT_NAME + satellite.getName());
        final List<Cartesian> tempSatelliteCartesian = satellite.getCartesianArraylist();
        final List<Double> timeList = satellite.getTimeList();
        final List<CzmlEllipsoid> tempEllipsoids = new ArrayList<>();
        final SatellitePosition tempSatellitePositions = new SatellitePosition(tempSatelliteCartesian, timeList);
        this.julianDates = tempSatellitePositions.getDates();
        this.positionReference = new Reference(satellite.getId() + DEFAULT_H_POSITION);

        this.covariancePropagation(satellite, initCovariance);

        for (int i = 0; i < tempSatelliteCartesian.size() - 1; i++) {
            // Ellipsoids
            final TimeInterval currentTimeInterval = new TimeInterval(julianDates.get(i), julianDates.get(i).addSeconds(Header.MASTER_CLOCK.getMultiplier()));
            final StateCovariance currentCovariance = covarianceList.get(i);
            final RealMatrix currentMatrix = currentCovariance.getMatrix();
            final double initialSigmaX = FastMath.sqrt(currentMatrix.getRow(0)[0]);
            final double initialSigmaY = FastMath.sqrt(currentMatrix.getRow(1)[1]);
            final double initialSigmaZ = FastMath.sqrt(currentMatrix.getRow(2)[2]);

            final Cartesian transformedCartesian = new Cartesian(initialSigmaX, initialSigmaY, initialSigmaZ);
            tempEllipsoids.add(new CzmlEllipsoid(currentTimeInterval, transformedCartesian, color));
        }
        this.ellipsoidList = tempEllipsoids;
        this.satelliteCartesianList = tempSatelliteCartesian;
    }

    /** A covariance display builder from an OEMFile.
     * @param oem : An OEMFile in input.*/
    public CovarianceDisplay(final Oem oem) throws URISyntaxException, IOException {
        this(oem, new Color(255, 255, 0, 255));
    }
    /** A covariance display builder from an OEMFile with a given color for the ellipsoid.
     * @param oem : An OEMFile in input.
     * @param color : The color of the ellipsoid.*/
    public CovarianceDisplay(final Oem oem, final Color color) throws URISyntaxException, IOException {
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
        final List<Cartesian> tempSatelliteCartesian = satellite.getCartesianArraylist();
        final List<Double> timeList = satellite.getTimeList();
        final List<CzmlEllipsoid> tempEllipsoids = new ArrayList<>();
        final SatellitePosition tempSatellitePositions = new SatellitePosition(tempSatelliteCartesian, timeList);
        this.julianDates = tempSatellitePositions.getDates();
        this.positionReference = new Reference(satellite.getId() + DEFAULT_H_POSITION);

        this.covariancePropagation(satellite, initCovariance);

        for (int i = 0; i < tempSatelliteCartesian.size() - 1; i++) {
            // Ellipsoids
            final TimeInterval currentTimeInterval = new TimeInterval(julianDates.get(i), julianDates.get(i).addSeconds(Header.MASTER_CLOCK.getMultiplier()));
            final StateCovariance currentCovariance = covarianceList.get(i);
            final RealMatrix currentMatrix = currentCovariance.getMatrix();
            final double currentSigmaX = FastMath.sqrt(currentMatrix.getRow(0)[0]);
            final double currentSigmaY = FastMath.sqrt(currentMatrix.getRow(1)[1]);
            final double currentSigmaZ = FastMath.sqrt(currentMatrix.getRow(2)[2]);
            final Cartesian currentCartesianEllipsoid = new Cartesian(currentSigmaX, currentSigmaY, currentSigmaZ);
            tempEllipsoids.add(new CzmlEllipsoid(currentTimeInterval, currentCartesianEllipsoid, color));
        }
        this.ellipsoidList = tempEllipsoids;
        this.satelliteCartesianList = tempSatelliteCartesian;
    }

    /** A covariance display builder from an ephemeris and an initial covariance.
     * @param ephemeris : A list of TimeStampedPVCoordinates {@link org.orekit.utils.TimeStampedPVCoordinates} that defines the ephemeris of an object.
     * @param initCovariance : The initial covariance.
     */
    public CovarianceDisplay(final List<TimeStampedPVCoordinates> ephemeris, final StateCovariance initCovariance) throws URISyntaxException, IOException {
        this(ephemeris, initCovariance, new Color(255, 255, 0, 255));
    }
    /** A covariance display builder from an ephemeris and an initial covariance with a given color for the ellipsoid.
     * @param ephemeris : A list of TimeStampedPVCoordinates {@link org.orekit.utils.TimeStampedPVCoordinates} that defines the ephemeris of an object.
     * @param initCovariance : The initial covariance.
     * @param color : The color of the ellipsoid.*/
    public CovarianceDisplay(final List<TimeStampedPVCoordinates> ephemeris, final StateCovariance initCovariance, final Color color) throws URISyntaxException, IOException {
        this.satellite = new Satellite(Header.MASTER_CLOCK.getAvailability(), ephemeris, FramesFactory.getEME2000());
        this.setId(DEFAULT_ID + satellite.getId());
        this.setName(DEFAULT_NAME + satellite.getName());
        final List<Cartesian> tempSatelliteCartesian = satellite.getCartesianArraylist();
        final List<Double> timeList = satellite.getTimeList();
        final List<CzmlEllipsoid> tempEllipsoids = new ArrayList<>();
        final SatellitePosition tempSatellitePositions = new SatellitePosition(tempSatelliteCartesian, timeList);
        this.julianDates = tempSatellitePositions.getDates();
        this.positionReference = new Reference(satellite.getId() + DEFAULT_H_POSITION);

        this.covariancePropagation(satellite, initCovariance);

        for (int i = 0; i < tempSatelliteCartesian.size() - 1; i++) {
            // Ellipsoids
            final TimeInterval currentTimeInterval = new TimeInterval(julianDates.get(i), julianDates.get(i).addSeconds(Header.MASTER_CLOCK.getMultiplier()));
            final StateCovariance currentCovariance = covarianceList.get(i);
            final RealMatrix currentMatrix = currentCovariance.getMatrix();
            final double currentSigmaX = FastMath.sqrt(currentMatrix.getRow(0)[0]);
            final double currentSigmaY = FastMath.sqrt(currentMatrix.getRow(1)[1]);
            final double currentSigmaZ = FastMath.sqrt(currentMatrix.getRow(2)[2]);
            final Cartesian currentCartesianEllipsoid = new Cartesian(currentSigmaX, currentSigmaY, currentSigmaZ);
            tempEllipsoids.add(new CzmlEllipsoid(currentTimeInterval, currentCartesianEllipsoid, color));
        }
        this.ellipsoidList = tempEllipsoids;
        this.satelliteCartesianList = tempSatelliteCartesian;
    }

    // The following builders only build one ellipsoid
    /** A single ellipsoid builder to display it in a given position.
     * @param position : The position of the ellipsoid.
     * @param ellipsoid : The ellipsoid to display.
     */
    public CovarianceDisplay(final Position position, final CzmlEllipsoid ellipsoid) {
        this.setId(DEFAULT_ID + position.toString());
        this.setName(DEFAULT_NAME + position.toString());
        this.position = position;
        this.czmlEllipsoid = ellipsoid;
    }

    /** A single ellipsoid builder from basic components.
     * @param id : The id of the packet to write.
     * @param name : The name of the packet to write.
     * @param position : The position of the ellipsoid.
     * @param ellipsoid : The ellipsoid to display.
     */
    public CovarianceDisplay(final String id, final String name, final Position position, final CzmlEllipsoid ellipsoid) {
        this.setId(id);
        this.setName(name);
        this.position = position;
        this.czmlEllipsoid = ellipsoid;
    }

    /** The generation function for the CZML file for the covariance display.*/
    public void writeCzmlBlock() {
        if (ellipsoidList.isEmpty()) {
            OUTPUT.setPrettyFormatting(true);
            try (PacketCesiumWriter packet = STREAM.openPacket(OUTPUT)) {
                packet.writeId(getId());
                packet.writeName(getName());

                position.write(packet, OUTPUT, Header.MASTER_CLOCK.getAvailability(), referenceFrame);

                czmlEllipsoid.write(packet, OUTPUT);
            }
        } else {
            for (int i = 0; i < ellipsoidList.size(); i++) {
                OUTPUT.setPrettyFormatting(true);
                try (PacketCesiumWriter packet = STREAM.openPacket(OUTPUT)) {
                    packet.writeId(getId() + julianDates.get(i).toString());
                    packet.writeName(getName());
                    packet.writePositionPropertyReference(positionReference);
                    final CzmlEllipsoid currentEllipsoid = ellipsoidList.get(i);

                    try (OrientationCesiumWriter orientationWriter = packet.getOrientationWriter()) {
                        orientationWriter.open(OUTPUT);
                        orientationWriter.writeVelocityReference(satellite.getId() + DEFAULT_H_POSITION);
                    }

                    currentEllipsoid.write(packet, OUTPUT);
                }
            }
            cleanObject();
        }
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

    /** This getter return the list of unit quaternion for the orientation.
     * @return : The list of unit quaternion used. */
    public List<UnitQuaternion> getUnitQuaternions() {
        return unitQuaternions;
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

    /** This function cleans all the private attributes to be used for another object.*/
    @Override
    public void cleanObject() {
        this.setId("");
        this.setName("");
        this.position = null;
        this.czmlEllipsoid = null;
        this.satellite = null;
        this.ellipsoidList = new ArrayList<>();
    }

    // Functions
    /** This function propagates the covariance using the propagator of the satellite and an initial state covariance.
     * @param inputSatellite : The satellite which the covariance will be around.
     * @param initCovariance : The initial state covariance.*/
    private void covariancePropagation(final Satellite inputSatellite, final StateCovariance initCovariance) {
        final Propagator propagator = inputSatellite.getSatellitePropagator();

        satellite.setAttitudes(new ArrayList<>());

        final BoundedPropagator boundedPropagator = inputSatellite.getBoundedPropagator();

        final List<AbsoluteDate> absoluteDateList = inputSatellite.getAbsoluteDateList();

        final Frame covFrame = FramesFactory.getITRF(IERSConventions.IERS_2010, true);

        final RealMatrix covInitMatrix = initCovariance.getMatrix();

        final AbsoluteDate covDate = propagator.getInitialState().getDate();

        final StateCovariance covInit = new StateCovariance(covInitMatrix, covDate, covFrame, OrbitType.CARTESIAN, PositionAngleType.MEAN);

        final String stm = "State Transition Matrix ";

        final MatricesHarvester harvester = propagator.setupMatricesComputation(stm, null, null);

        final StateCovarianceMatrixProvider provider = new StateCovarianceMatrixProvider("covariance", stm,
                harvester, covInit);

        propagator.addAdditionalStateProvider(provider);

        propagator.getMultiplexer().add(Header.MASTER_CLOCK.getMultiplier(), new OrekitFixedStepHandler() {
            @Override
            public void handleStep(final SpacecraftState spacecraftState) {
                final StateCovariance matrix = provider.getStateCovariance(spacecraftState).changeCovarianceFrame(spacecraftState.getOrbit(), LOFType.TNW);
                covarianceList.add(matrix);
            }
        });

        propagator.propagate(satellite.getOrbits().get(0).getDate(), satellite.getOrbits().get(satellite.getOrbits().size() - 1).getDate());

        boundedPropagator.propagate(satellite.getOrbits().get(0).getDate(), satellite.getOrbits().get(satellite.getOrbits().size() - 1).getDate());

        for (final AbsoluteDate currentAbsDate : absoluteDateList) {
            final Attitude currentAttitude = boundedPropagator.getAttitudeProvider().getAttitude(boundedPropagator, currentAbsDate, covFrame);
            attitudes.add(currentAttitude);
        }
    }

    /** This function basically converts an array of reference into an iterable of reference.
     * @param array : The array to convert.
     * @return : An iterable of references.*/
    private static Iterable<Reference> convertToIterable(final Reference[] array) {
        return () -> Arrays.stream(array).iterator();
    }
}
