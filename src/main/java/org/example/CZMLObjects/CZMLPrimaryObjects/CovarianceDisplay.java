package org.example.CZMLObjects.CZMLPrimaryObjects;

import cesiumlanguagewriter.*;
import org.example.CZMLObjects.CZMLSecondaryObects.CZMLEllipsoid;
import org.example.CZMLObjects.CZMLSecondaryObects.SatelliteObjects.SatellitePosition;
import org.example.CZMLObjects.Position;
import org.example.Inputs.InputFiles.OEMFile;
import org.hipparchus.geometry.euclidean.threed.Rotation;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.hipparchus.linear.MatrixUtils;
import org.hipparchus.linear.RealMatrix;
import org.hipparchus.util.FastMath;
import org.orekit.attitudes.Attitude;
import org.orekit.attitudes.AttitudeProvider;
import org.orekit.attitudes.LofOffset;
import org.orekit.files.ccsds.ndm.odm.CartesianCovariance;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.frames.LOFType;
import org.orekit.frames.Transform;
import org.orekit.orbits.OrbitType;
import org.orekit.orbits.PositionAngleType;
import org.orekit.propagation.*;
import org.orekit.propagation.sampling.OrekitFixedStepHandler;
import org.orekit.time.AbsoluteDate;
import org.orekit.utils.IERSConventions;
import org.orekit.utils.PVCoordinates;
import org.orekit.utils.TimeStampedPVCoordinates;

import java.awt.*;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CovarianceDisplay implements CZMLPrimaryObject {

    // STATICS
    /** .*/
    public static final String DEFAULT_ID = "COV/";
    /** .*/
    public static final String DEFAULT_NAME = "Covariance of ";
    /** .*/
    public static final String DEFAULT_H_POSITION = "#position";

    // Parameters
    /** .*/
    private String id;
    /** .*/
    private String name;
    /** .*/
    private Position position;
    /** .*/
    private CZMLEllipsoid czmlEllipsoid;
    /** .*/
    private String referenceFrame = "INERTIAL";
    /** .*/
    private Header header;
    /** .*/
    private Reference positionReference = null;
    /** .*/
    private Reference orientationReference = null;

    // Intrinsic parameters
    /** .*/
    private Satellite satellite;
    /** .*/
    private List<CZMLEllipsoid> ellipsoidList;
    /** .*/
    private List<JulianDate> julianDates;
    /** .*/
    private List<StateCovariance> covarianceList = new ArrayList<>();
    /** .*/
    private List<UnitQuaternion> unitQuaternions = new ArrayList<>();
    /** .*/
    private List<Attitude> attitudes = new ArrayList<>();
    /** .*/
    private SatellitePosition satellitePosition;
    /** .*/
    private List<Cartesian> satelliteCartesianList = new ArrayList<>();

    // BUILDERS

    // The following builders build several ellipsoids, in order to follow the satellite
    public CovarianceDisplay(final Satellite satellite, final StateCovariance initCovariance, final Header header) {
        this.satellite = satellite;
        this.id = DEFAULT_ID + satellite.getId();
        this.name = DEFAULT_NAME + satellite.getName();
        final List<Cartesian> tempSatelliteCartesian = satellite.getCartesianArraylist();
        final List<Double> timeList = satellite.getTimeList();
        final List<CZMLEllipsoid> tempEllipsoids = new ArrayList<>();
        final SatellitePosition tempSatellitePositions = new SatellitePosition(tempSatelliteCartesian, timeList);
        this.satellitePosition = tempSatellitePositions;
        this.julianDates = tempSatellitePositions.getDates();
        this.header = header;
        this.positionReference = new Reference(satellite.getId() + DEFAULT_H_POSITION);

        this.covariancePropagation(satellite, initCovariance);

        for (int i = 0; i < tempSatelliteCartesian.size() - 1; i++) {
            // Ellipsoids
            final TimeInterval currentTimeInterval = new TimeInterval(julianDates.get(i), julianDates.get(i).addSeconds(header.getStepSimulation()));
            final StateCovariance currentCovariance = covarianceList.get(i);
            final RealMatrix currentMatrix = currentCovariance.getMatrix();
            final double toTransformSigmaX = FastMath.sqrt(currentMatrix.getRow(0)[0]);
            final double toTransformSigmaY = FastMath.sqrt(currentMatrix.getRow(1)[1]);
            final double toTransformSigmaZ = FastMath.sqrt(currentMatrix.getRow(2)[2]);

            final Vector3D vectorInertial = new Vector3D(toTransformSigmaX, toTransformSigmaY, toTransformSigmaZ);
            final PVCoordinates toTransformIntoITRF = new PVCoordinates(vectorInertial);
            final Transform transformFromInertialToIRTF = FramesFactory.getEME2000().getTransformTo(FramesFactory.getITRF(IERSConventions.IERS_2010, true), julianDateToAbsoluteDate(currentTimeInterval.getStart()));
            final PVCoordinates transformedPVCoordinates = transformFromInertialToIRTF.transformPVCoordinates(toTransformIntoITRF);

            final double transformedSigmaX = transformedPVCoordinates.getPosition().getX();
            final double transformedSigmaY = transformedPVCoordinates.getPosition().getY();
            final double transformedSigmaZ = transformedPVCoordinates.getPosition().getZ();

            final Cartesian transformedCartesian = new Cartesian(transformedSigmaX, transformedSigmaY, transformedSigmaZ);
            tempEllipsoids.add(new CZMLEllipsoid(currentTimeInterval, transformedCartesian));
        }
        this.ellipsoidList = tempEllipsoids;
        this.satelliteCartesianList = tempSatelliteCartesian;
    }

    public CovarianceDisplay(final Satellite satellite, final StateCovariance initCovariance, final Header header, final Color color) {
        this.satellite = satellite;
        this.id = DEFAULT_ID + satellite.getId();
        this.name = DEFAULT_NAME + satellite.getName();
        final List<Cartesian> tempSatelliteCartesian = satellite.getCartesianArraylist();
        final List<Double> timeList = satellite.getTimeList();
        final List<CZMLEllipsoid> tempEllipsoids = new ArrayList<>();
        final SatellitePosition tempSatellitePositions = new SatellitePosition(tempSatelliteCartesian, timeList);
        this.satellitePosition = tempSatellitePositions;
        this.julianDates = tempSatellitePositions.getDates();
        this.header = header;
        this.positionReference = new Reference(satellite.getId() + DEFAULT_H_POSITION);

        this.covariancePropagation(satellite, initCovariance);

        for (int i = 0; i < tempSatelliteCartesian.size() - 1; i++) {
            // Ellipsoids
            final TimeInterval currentTimeInterval = new TimeInterval(julianDates.get(i), julianDates.get(i).addSeconds(header.getStepSimulation()));
            final StateCovariance currentCovariance = covarianceList.get(i);
            final RealMatrix currentMatrix = currentCovariance.getMatrix();
            final double initialSigmaX = FastMath.sqrt(currentMatrix.getRow(0)[0]);
            final double initialSigmaY = FastMath.sqrt(currentMatrix.getRow(1)[1]);
            final double initialSigmaZ = FastMath.sqrt(currentMatrix.getRow(2)[2]);

            final Cartesian transformedCartesian = new Cartesian(initialSigmaX, initialSigmaY, initialSigmaZ);
            tempEllipsoids.add(new CZMLEllipsoid(currentTimeInterval, transformedCartesian, color));
        }
        this.ellipsoidList = tempEllipsoids;
        this.satelliteCartesianList = tempSatelliteCartesian;
    }

    public CovarianceDisplay(final OEMFile oemFile) {
        this(oemFile, new Color(255, 255, 0, 255));
    }

    public CovarianceDisplay(final OEMFile oemFile, final Color color) {
        final Header headerInit = oemFile.getHeader();
        final Satellite satelliteInit = new Satellite(oemFile);

        final List<CartesianCovariance> covariances = oemFile.getCovariances().get(0);
        final CartesianCovariance initCartesianCovariance = covariances.get(0);
        final StateCovariance initInertialCovariance = new StateCovariance(initCartesianCovariance.getCovarianceMatrix(),
                headerInit.julianDateToAbsoluteDate(headerInit.getClock().getAvailability().getStart()), LOFType.TNW);
        final StateCovariance initCovariance = initInertialCovariance.changeCovarianceFrame(satelliteInit.getOrbits().get(0), satelliteInit.getFrame());

        this.id = DEFAULT_ID + satellite.getId();
        this.name = DEFAULT_NAME + satellite.getName();
        final List<Cartesian> tempSatelliteCartesian = satellite.getCartesianArraylist();
        final List<Double> timeList = satellite.getTimeList();
        final List<CZMLEllipsoid> tempEllipsoids = new ArrayList<>();
        final SatellitePosition tempSatellitePositions = new SatellitePosition(tempSatelliteCartesian, timeList);
        this.satellitePosition = tempSatellitePositions;
        this.julianDates = tempSatellitePositions.getDates();
        this.positionReference = new Reference(satellite.getId() + DEFAULT_H_POSITION);

        this.covariancePropagation(satellite, initCovariance);

        for (int i = 0; i < tempSatelliteCartesian.size() - 1; i++) {
            // Ellipsoids
            final TimeInterval currentTimeInterval = new TimeInterval(julianDates.get(i), julianDates.get(i).addSeconds(headerInit.getStepSimulation()));
            final StateCovariance currentCovariance = covarianceList.get(i);
            final RealMatrix currentMatrix = currentCovariance.getMatrix();
            final double currentSigmaX = FastMath.sqrt(currentMatrix.getRow(0)[0]);
            final double currentSigmaY = FastMath.sqrt(currentMatrix.getRow(1)[1]);
            final double currentSigmaZ = FastMath.sqrt(currentMatrix.getRow(2)[2]);
            final Cartesian currentCartesianEllipsoid = new Cartesian(currentSigmaX, currentSigmaY, currentSigmaZ);
            tempEllipsoids.add(new CZMLEllipsoid(currentTimeInterval, currentCartesianEllipsoid, color));
        }
        this.ellipsoidList = tempEllipsoids;
        this.satelliteCartesianList = tempSatelliteCartesian;
    }

    public CovarianceDisplay(final List<TimeStampedPVCoordinates> ephemeris, final StateCovariance initCovariance, final Header header) {
        this(ephemeris, initCovariance, header, new Color(255, 255, 0, 255));
    }

    public CovarianceDisplay(final List<TimeStampedPVCoordinates> ephemeris, final StateCovariance initCovariance, final Header header, final Color color) {
        final Satellite satelliteInit = new Satellite(header, header.getClock().getAvailability(), ephemeris, FramesFactory.getEME2000());
        this.id = DEFAULT_ID + satellite.getId();
        this.name = DEFAULT_NAME + satellite.getName();
        final List<Cartesian> tempSatelliteCartesian = satellite.getCartesianArraylist();
        final List<Double> timeList = satellite.getTimeList();
        final List<CZMLEllipsoid> tempEllipsoids = new ArrayList<>();
        final SatellitePosition tempSatellitePositions = new SatellitePosition(tempSatelliteCartesian, timeList);
        this.satellitePosition = tempSatellitePositions;
        this.julianDates = tempSatellitePositions.getDates();
        this.positionReference = new Reference(satellite.getId() + DEFAULT_H_POSITION);

        this.covariancePropagation(satellite, initCovariance);

        for (int i = 0; i < tempSatelliteCartesian.size() - 1; i++) {
            // Ellipsoids
            final TimeInterval currentTimeInterval = new TimeInterval(julianDates.get(i), julianDates.get(i).addSeconds(header.getStepSimulation()));
            final StateCovariance currentCovariance = covarianceList.get(i);
            final RealMatrix currentMatrix = currentCovariance.getMatrix();
            final double currentSigmaX = FastMath.sqrt(currentMatrix.getRow(0)[0]);
            final double currentSigmaY = FastMath.sqrt(currentMatrix.getRow(1)[1]);
            final double currentSigmaZ = FastMath.sqrt(currentMatrix.getRow(2)[2]);
            final Cartesian currentCartesianEllipsoid = new Cartesian(currentSigmaX, currentSigmaY, currentSigmaZ);
            tempEllipsoids.add(new CZMLEllipsoid(currentTimeInterval, currentCartesianEllipsoid, color));
        }
        this.ellipsoidList = tempEllipsoids;
        this.satelliteCartesianList = tempSatelliteCartesian;
    }

    // The following builders only build one ellipsoid
    public CovarianceDisplay(final Position position, final CZMLEllipsoid ellipsoid, final Header header) {
        this.id = DEFAULT_ID + position.toString();
        this.name = DEFAULT_NAME + position.toString();
        this.position = position;
        this.czmlEllipsoid = ellipsoid;
        this.header = header;
    }

    public CovarianceDisplay(final String id, final String name, final Position position, final CZMLEllipsoid ellipsoid, final Header header) {
        this.id = id;
        this.name = name;
        this.position = position;
        this.czmlEllipsoid = ellipsoid;
        this.header = header;
    }

    public void generateCZML() {
        if (ellipsoidList.isEmpty()) {
            OUTPUT.setPrettyFormatting(true);
            try (PacketCesiumWriter packet = STREAM.openPacket(OUTPUT)) {
                packet.writeId(id);
                packet.writeName(name);

                position.write(packet, OUTPUT, header.getClock().getAvailability(), referenceFrame);

                czmlEllipsoid.write(packet, OUTPUT);
            }
        } else {
            for (int i = 0; i < ellipsoidList.size(); i++) {
                OUTPUT.setPrettyFormatting(true);
                try (PacketCesiumWriter packet = STREAM.openPacket(OUTPUT)) {
                    packet.writeId(id + julianDates.get(i).toString());
                    packet.writeName(name);
                    packet.writePositionPropertyReference(positionReference);
                    final CZMLEllipsoid currentEllipsoid = ellipsoidList.get(i);

                    try (OrientationCesiumWriter orientationWriter = packet.getOrientationWriter()) {
                        orientationWriter.open(OUTPUT);
                        orientationWriter.writeVelocityReference(satellite.getId() + "#position");
                    }

                    currentEllipsoid.write(packet, OUTPUT);
                }
            }
            cleanObject();
        }
    }

    // GETS
    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public Satellite getSatellite() {
        return satellite;
    }

    public Position getPosition() {
        return position;
    }

    public Header getHeader() {
        return header;
    }

    public String getReferenceFrame() {
        return referenceFrame;
    }

    public List<UnitQuaternion> getUnitQuaternions() {
        return unitQuaternions;
    }

    public Reference getOrientationReference() {
        return orientationReference;
    }

    public List<Cartesian> getSatelliteCartesianList() {
        return satelliteCartesianList;
    }

    public List<StateCovariance> getCovarianceList() {
        return covarianceList;
    }

    public List<Attitude> getAttitudes() {
        return attitudes;
    }

    public Reference getPositionReference() {
        return positionReference;
    }

    public List<JulianDate> getJulianDates() {
        return julianDates;
    }

    // Overrides
    @Override
    public StringWriter getStringWriter() {
        return null;
    }

    @Override
    public void endFile() {
        OUTPUT.writeEndSequence();
    }

    @Override
    public void cleanObject() {
        this.id = "";
        this.name = "";
        this.position = null;
        this.czmlEllipsoid = null;
        this.satellite = null;
        this.ellipsoidList = new ArrayList<>();
    }


    // Functions
    private void covariancePropagation(final Satellite inputSatellite, final StateCovariance covariance) {
        final Propagator propagator = inputSatellite.getSatellitePropagator();

        satellite.setAttitudes(new ArrayList<>());

        final BoundedPropagator boundedPropagator = inputSatellite.getBoundedPropagator();

        final List<AbsoluteDate> absoluteDateList = inputSatellite.getAbsoluteDateList();

        final Frame covFrame = FramesFactory.getITRF(IERSConventions.IERS_2010, true);

        final RealMatrix covInitMatrix = covariance.getMatrix();

        final AbsoluteDate covDate = propagator.getInitialState().getDate();

        final StateCovariance covInit = new StateCovariance(covInitMatrix, covDate, covFrame, OrbitType.CARTESIAN, PositionAngleType.MEAN);

        final String stm = "State Transition Matrix ";

        final MatricesHarvester harvester = propagator.setupMatricesComputation(stm, null, null);

        final StateCovarianceMatrixProvider provider = new StateCovarianceMatrixProvider("covariance", stm,
                harvester, covInit);

        propagator.addAdditionalStateProvider(provider);

        propagator.getMultiplexer().add(header.getStepSimulation(), new OrekitFixedStepHandler() {
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

    private static Iterable<Reference> convertToIterable(final Reference[] array) {
        return () -> Arrays.stream(array).iterator();
    }
}
