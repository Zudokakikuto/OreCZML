/** .*/

package org.example.CZMLObjects.CZMLPrimaryObjects;

import cesiumlanguagewriter.*;
import org.example.CZMLAbstract.Model3D;
import org.example.CZMLObjects.CZMLSecondaryObects.Billboard;
import org.example.CZMLObjects.CZMLSecondaryObects.SatelliteObjects.SatelliteAttitude;
import org.example.CZMLObjects.CZMLSecondaryObects.SatelliteObjects.SatelliteReferenceSystem;
import org.example.CZMLObjects.PositionType;
import org.example.CZMLObjects.CZMLSecondaryObects.SatelliteObjects.Path;
import org.example.CZMLObjects.CZMLSecondaryObects.SatelliteObjects.SatellitePosition;
import org.example.CZMLObjects.Position;
import org.example.Inputs.InputFiles.OEMFile;
import org.example.Inputs.InputFiles.TLEFile;
import org.example.Inputs.OrbitInput.OrbitInput;
import org.example.Inputs.SpacecraftStateInput.SpacecraftStateListInput;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.hipparchus.ode.nonstiff.AdaptiveStepsizeIntegrator;
import org.hipparchus.ode.nonstiff.DormandPrince853Integrator;
import org.hipparchus.util.FastMath;
import org.orekit.attitudes.*;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.forces.ForceModel;
import org.orekit.forces.gravity.HolmesFeatherstoneAttractionModel;
import org.orekit.forces.gravity.potential.GravityFieldFactory;
import org.orekit.forces.gravity.potential.NormalizedSphericalHarmonicsProvider;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.orbits.KeplerianOrbit;
import org.orekit.orbits.Orbit;
import org.orekit.orbits.OrbitType;
import org.orekit.propagation.BoundedPropagator;
import org.orekit.propagation.EphemerisGenerator;
import org.orekit.propagation.Propagator;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.numerical.NumericalPropagator;
import org.orekit.propagation.sampling.OrekitFixedStepHandler;
import org.orekit.time.*;
import org.orekit.utils.Constants;
import org.orekit.utils.IERSConventions;
import org.orekit.utils.TimeStampedPVCoordinates;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class Satellite implements CZMLPrimaryObject {

    /** .*/
    public static final String DEFAULT_MODEL_3D_PATH = null;

    /** .*/
    public static final String DEFAULT_NAME = "Satellite";
    /** .*/
    public static final String DEFAULT_DESCRIPTION = "A satellite";
    /** .*/
    public static final String DEFAULT_FILE_SATELLITE_NAME = "Satellite created from : ";

    /** .*/
    private String Id;
    /** .*/
    private String name = "";
    /** .*/
    private TimeInterval availability;
    /** .*/
    private String description;
    /** .*/
    private List<TimeStampedPVCoordinates> Ephemeris = null;


    // Optional parameters
    /** .*/
    private boolean displayOnlyOnePeriod = false;
    /** .*/
    private boolean displayAttitude = false;
    /** .*/
    private boolean displayReferenceSystem = false;
    /** .*/
    private String model3DPath = "";
    /** .*/
    private Model3D model3D;

    // Intrinsic parameters
    /** .*/
    private double period;
    /** .*/
    private List<Vector3D> vector3DS;
    /** .*/
    private List<Double> timeList;
    /** .*/
    private List<AbsoluteDate> absoluteDateList;
    /** .*/
    private List<Cartesian> cartesianArraylist;
    /** .*/
    private List<Orbit> orbits;
    /** .*/
    private Frame frame = null;
    /** .*/
    private Billboard billboard = null;
    /** .*/
    private List<Position> positionsList;
    /** .*/
    private BoundedPropagator boundedPropagator;
    /** .*/
    private Propagator satellitePropagator;
    /** .*/
    private Header header;
    /** .*/
    private List<Attitude> attitudes = new ArrayList<>();
    /** .*/
    private SatelliteAttitude satelliteAttitude = null;
    /** .*/
    private SatelliteReferenceSystem satelliteReferenceSystem = null;

    //// BUILDERS

    public Satellite(final OEMFile file) {
        this(file, DEFAULT_MODEL_3D_PATH);
    }

    public Satellite(final OEMFile file, final String model3Dpath) {

        this.Id = DEFAULT_FILE_SATELLITE_NAME + file.getObjectID();
        this.name = DEFAULT_NAME;
        this.description = DEFAULT_DESCRIPTION;
        this.frame = file.getFrame();
        this.availability = header.getClock().getAvailability();
        this.Ephemeris = file.getEphemeris();

        final double step = this.getStepBetweenDates();
        final List<Vector3D> vector3DS1 = new ArrayList<>();
        this.timeList = new ArrayList<>();
        final List<Orbit> orbitList = new ArrayList<Orbit>();
        final List<Position> positions = new ArrayList<Position>();

        for (int i = 0; i < Ephemeris.size(); i++) {
            timeList.add(absDateToDouble(Ephemeris.get(i).getDate()));
            absoluteDateList.add(Ephemeris.get(i).getDate());
            vector3DS1.add(Ephemeris.get(i).getPosition());
            final double x = vector3DS1.get(i).getX();
            final double y = vector3DS1.get(i).getY();
            final double z = vector3DS1.get(i).getZ();
            final PositionType positionType = PositionType.CARTESIAN_POSITION;
            final Position tempPosition = new Position(x, y, z, positionType);
            positions.add(tempPosition);
            final KeplerianOrbit tempOrbit = new KeplerianOrbit(Ephemeris.get(i), frame, Constants.WGS84_EARTH_MU);
            orbitList.add(tempOrbit);
        }

        this.positionsList = positions;
        this.vector3DS = vector3DS1;
        this.orbits = orbitList;
        final List<Vector3D> toCartesians = propagationSat();
        this.cartesianArraylist = vectorToCartesian(toCartesians);

        this.model3DPath = model3Dpath;
        this.header = file.getHeader();
        this.period = orbits.get(0).getKeplerianPeriod();
    }

    public Satellite(final TLEFile tleFile) {
        this(tleFile, DEFAULT_MODEL_3D_PATH);
    }

    public Satellite(final TLEFile file, final String model3DPath) {
        this.Id = DEFAULT_FILE_SATELLITE_NAME + file.getId();
        this.name = DEFAULT_NAME;
        this.description = DEFAULT_DESCRIPTION;
        this.frame = file.getFrame();
        this.availability = header.getClock().getAvailability();
        this.header = file.getHeader();
        this.description = DEFAULT_DESCRIPTION;
        timeList.add(absDateToDouble(file.getStartTime()));
        timeList.add(absDateToDouble(file.getStopTime()));
        this.positionsList = new ArrayList<>();
        this.orbits = new ArrayList<>();
        final Orbit initialOrbit = file.getOrbit();
        orbits.add(initialOrbit);
        this.cartesianArraylist = propagationOrbit(initialOrbit);
        this.model3DPath = model3DPath;
        this.period = orbits.get(0).getKeplerianPeriod();
    }

    public Satellite(final Header header, final TimeInterval availability, final List<TimeStampedPVCoordinates> Ephemeris, final Frame frame) {
        this(header, DEFAULT_NAME, availability, DEFAULT_DESCRIPTION, Ephemeris, frame);
    }

    public Satellite(final Header header, final String name, final TimeInterval availability, final String description, final List<TimeStampedPVCoordinates> Ephemeris, final Frame frame) {
        this(header, name, availability, description, Ephemeris, frame, DEFAULT_MODEL_3D_PATH);
    }

    public Satellite(final Header header, final String name, final TimeInterval availability, final String description, final List<TimeStampedPVCoordinates> Ephemeris, final Frame frame, final String model3Dpath) {
        this.Id = DEFAULT_NAME;
        this.name = name;
        this.availability = availability;
        this.description = description;
        this.Ephemeris = Ephemeris;

        final List<Orbit> orbitList = new ArrayList<Orbit>();
        final List<Vector3D> vector3DS1 = new ArrayList<>();
        this.timeList = new ArrayList<>();
        final List<Position> positions = new ArrayList<Position>();

        for (int i = 0; i < Ephemeris.size(); i++) {
            timeList.add(absDateToDouble(Ephemeris.get(i).getDate()));
            absoluteDateList.add(Ephemeris.get(i).getDate());
            vector3DS1.add(Ephemeris.get(i).getPosition());
            final double x = vector3DS1.get(i).getX();
            final double y = vector3DS1.get(i).getY();
            final double z = vector3DS1.get(i).getZ();
            final PositionType positionType = PositionType.CARTESIAN_POSITION;
            final Position tempPosition = new Position(x, y, z, positionType);
            positions.add(tempPosition);
            final KeplerianOrbit tempOrbit = new KeplerianOrbit(Ephemeris.get(i), frame, Constants.WGS84_EARTH_MU);
            orbitList.add(tempOrbit);
        }

        this.positionsList = positions;
        this.vector3DS = vector3DS1;
        this.orbits = orbitList;
        this.frame = frame;
        final List<Vector3D> toCartesians = propagationSat();
        this.cartesianArraylist = vectorToCartesian(toCartesians);

        this.model3DPath = model3Dpath;
        this.header = header;
        this.period = orbits.get(0).getKeplerianPeriod();
    }

    public Satellite(final OrbitInput orbitInput) {
        this(orbitInput, DEFAULT_MODEL_3D_PATH);
    }

    public Satellite(final OrbitInput orbitInput, final String model3Dpath) {
        final AbsoluteDate startTime = orbitInput.getStartTime();
        final AbsoluteDate stopTime = orbitInput.getStopTime();
        this.timeList = new ArrayList<>();
        this.orbits = new ArrayList<>();
        this.positionsList = new ArrayList<>();
        this.header = orbitInput.getHeader();
        this.Id = orbitInput.getOrbit().toString();
        this.name = orbitInput.getOrbit().toString();
        this.description = DEFAULT_DESCRIPTION;
        this.availability = header.getClock().getAvailability();
        timeList.add(absDateToDouble(startTime));
        timeList.add(absDateToDouble(stopTime));
        orbits.add(orbitInput.getOrbit());
        this.frame = orbitInput.getFrame();
        this.cartesianArraylist = propagationOrbit(orbitInput.getOrbit());

        this.model3DPath = model3Dpath;
        this.period = orbits.get(0).getKeplerianPeriod();
    }

    public Satellite(final SpacecraftStateListInput input) {
        this(input, DEFAULT_MODEL_3D_PATH);
    }

    public Satellite(final SpacecraftStateListInput input, final String model3Dpath) {
        final List<Double> timeList1;
        final List<Orbit> orbits1;
        this.positionsList = new ArrayList<>();
        this.header = input.getHeader();

        this.Id = input.getOrbits().get(0).toString();
        this.name = DEFAULT_NAME;
        this.description = DEFAULT_DESCRIPTION;
        this.availability = header.getClock().getAvailability();
        orbits1 = input.getOrbits();
        this.orbits = orbits1;
        this.vector3DS = input.getPositions();
        timeList1 = input.getTimeList();
        this.timeList = timeList1;
        final List<Vector3D> toCartesians = propagationSat();
        this.cartesianArraylist = vectorToCartesian(toCartesians);

        this.model3DPath = model3Dpath;
        this.period = orbits.get(0).getKeplerianPeriod();
    }

    public Satellite(final Propagator propagator, final AbsoluteDate finalDate, final Header header) {
        this(propagator, finalDate, header, DEFAULT_MODEL_3D_PATH);
    }

    public Satellite(final Propagator propagator, final AbsoluteDate finalDate, final Header header, final String model3DPath) {

        if (propagator.getInitialState().getOrbit() == null) {
            throw new RuntimeException("The propagator has no initial state, please setup the propagator before building" +
                    "the satellite");
        }
        else {
            this.satellitePropagator = propagator;
            this.header = header;
            this.name = DEFAULT_NAME;
            this.availability = header.getClock().getAvailability();
            this.Id = propagator.getInitialState().getOrbit().toString();
            this.description = DEFAULT_DESCRIPTION;
            this.frame = propagator.getFrame();
            this.header = header;

            // Creation of empty list to be filled with multiplexerSetup
            this.timeList = new ArrayList<>();
            this.absoluteDateList = new ArrayList<>();
            this.orbits = new ArrayList<>();
            this.positionsList = new ArrayList<>();

            orbits.add(propagator.getInitialState().getOrbit());

            // Setup propagator
            final EphemerisGenerator generator = propagator.getEphemerisGenerator();
            this.vector3DS = multiplexerSetup(propagator);

            // Propagation
            propagator.propagate(finalDate);

            // Retrieve parameters from propagation
            this.boundedPropagator = generator.getGeneratedEphemeris();
            this.cartesianArraylist = vectorToCartesian(vector3DS);
        }

        this.model3DPath = model3DPath;
        this.period = orbits.get(0).getKeplerianPeriod();
    }

    // Overrides
    @Override
    public StringWriter getStringWriter() {
        return STRING_WRITER;
    }

    @Override
    public void endFile() {
        OUTPUT.writeEndSequence();
    }

    @Override
    public void cleanObject() {
        this.Id = "";
        this.name = "";
        this.availability = null;
        this.description = "";
        this.Ephemeris = new ArrayList<>();
        this.model3DPath = "";
        this.model3D = null;
        this.vector3DS = new ArrayList<>();
        this.timeList = new ArrayList<>();
        this.cartesianArraylist = new ArrayList<>();
        this.orbits = new ArrayList<>();
        this.frame = null;
        this.billboard = null;
        this.positionsList = new ArrayList<>();
        this.boundedPropagator = null;
        this.header = null;
        this.attitudes = new ArrayList<>();
    }

    // GETS
    public String getId() {
        return Id;
    }

    public String getDescription() {
        return description;
    }

    public List<AbsoluteDate> getAbsoluteDateList() {
        return absoluteDateList;
    }

    public String getName() {
        return name;
    }

    public List<Double> getTimeList() {
        return timeList;
    }

    public List<Attitude> getAttitudes() {
        return attitudes;
    }

    public List<Vector3D> getVector3DS() {
        return vector3DS;
    }

    public TimeInterval getAvailability() {
        return availability;
    }

    public List<TimeStampedPVCoordinates> getEphemeris() {
        if (Ephemeris == null) {
            throw new RuntimeException("The satellite was not build with an OEM, so the Ephemeris is empty, try using the getOrbits method to access the time and positions");
        } else {
            return Ephemeris;
        }
    }

    public List<Orbit> getOrbits() {
        return orbits;
    }

    public Propagator getSatellitePropagator() {
        return satellitePropagator;
    }

    public List<Cartesian> getCartesianArraylist() {
        return cartesianArraylist;
    }

    public Frame getFrame() {
        return frame;
    }

    public Header getHeader() {
        return header;
    }

    public List<Position> getPositionsList() {
        return positionsList;
    }

    public BoundedPropagator getBoundedPropagator() {
        return boundedPropagator;
    }

    public double getPeriod() {
        return period;
    }

    // SETS
    public void setAttitudes(final List<Attitude> attitudes) {
        this.attitudes = attitudes;
    }

    public void setBoundedPropagator(final BoundedPropagator boundedPropagator) {
        this.boundedPropagator = boundedPropagator;
    }

    // Functions

    public void displayOnlyOnePeriod() {
        displayOnlyOnePeriod = true;
    }

    public void displaySatelliteAttitude() {
        this.displayAttitude = true;
        this.satelliteAttitude = new SatelliteAttitude(this);
    }

    public void displaySatelliteReferenceSystem() {
        this.displayReferenceSystem = true;
        this.satelliteReferenceSystem = new SatelliteReferenceSystem(this);
    }

    @Override
    public void generateCZML() {
        OUTPUT.setPrettyFormatting(true);
        try (PacketCesiumWriter packet = STREAM.openPacket(OUTPUT)) {
            packet.writeId(Id);
            packet.writeName(name);
            packet.writeAvailability(availability);

            generateDisplay(packet);

            generatePath(packet);

            generatePosition(packet);

        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
        if (displayAttitude) {
            satelliteAttitude.generateCZML();
        }
        if (displayReferenceSystem) {
            satelliteReferenceSystem.generateCZML();
        }
    }

    // Functions
    private void generateDisplay(final PacketCesiumWriter packet) throws URISyntaxException, IOException {

        if (model3DPath == null) {
            final String imageStr = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAADsMAAA7DAcdvqGQAAADJSURBVDhPnZHRDcMgEEMZjVEYpaNklIzSEfLfD4qNnXAJSFWfhO7w2Zc0Tf9QG2rXrEzSUeZLOGm47WoH95x3Hl3jEgilvDgsOQUTqsNl68ezEwn1vae6lceSEEYvvWNT/Rxc4CXQNGadho1NXoJ+9iaqc2xi2xbt23PJCDIB6TQjOC6Bho/sDy3fBQT8PrVhibU7yBFcEPaRxOoeTwbwByCOYf9VGp1BYI1BA+EeHhmfzKbBoJEQwn1yzUZtyspIQUha85MpkNIXB7GizqDEECsAAAAASUVORK5CYII=";
            this.billboard = new Billboard(imageStr);
            billboard.write(packet, OUTPUT);
        } else {

            try (PositionCesiumWriter positionWriter = packet.getPositionWriter()) {
                positionWriter.open(OUTPUT);
                positionWriter.writeReferenceFrame("INERTIAL");
            }

            if (!displayAttitude) {

                try (OrientationCesiumWriter orientationWriter = packet.getOrientationWriter()) {
                    orientationWriter.open(OUTPUT);
                    orientationWriter.writeVelocityReference(this.getId() + "#position");
                }
            }

            this.model3D = new Model3D(model3DPath, header);
            model3D.generateCZML(packet, OUTPUT);
        }
    }

    private void generatePath(final PacketCesiumWriter packet) {
        try (PathCesiumWriter pathProperty = packet.openPathProperty()) {
            if (!displayOnlyOnePeriod) {
                final Path path = new Path(availability, packet);
                try (BooleanCesiumWriter showPath = pathProperty.openShowProperty()) {
                    showPath.writeInterval(availability.getStart(), availability.getStop());
                    showPath.writeBoolean(Path.getShow());
                }
            } else {
                final Path path = new Path(availability, packet);
                pathProperty.writeLeadTimeProperty(this.orbits.get(0).getKeplerianPeriod());
                pathProperty.writeTrailTimeProperty(0.0);
                try (BooleanCesiumWriter showPath = pathProperty.openShowProperty()) {
                    showPath.writeInterval(availability.getStart(), availability.getStop());
                    showPath.writeBoolean(Path.getShow());
                }
                displayOnlyOnePeriod = false;
            }
        }
    }

    private void generatePosition(final PacketCesiumWriter packet) {

        final SatellitePosition Pos = new SatellitePosition(this.cartesianArraylist, timeList);

        try (PositionCesiumWriter writer = packet.openPositionProperty()) {
            writer.writeReferenceFrame(Pos.getReferenceFrame());
            writer.writeInterpolationAlgorithm(Pos.getCesiumInterpolationAlgorithm());
            writer.writeInterpolationDegree(Pos.getInterpolationDegree());
            writer.writeCartesian(Pos.getDates(), Pos.getPositions());
        }
    }

    private double getStepBetweenDates() {
        final List<TimeStampedPVCoordinates> Ephemeris_temp = this.Ephemeris;

        final AbsoluteDate Date0 = Ephemeris_temp.get(0).getDate();
        final AbsoluteDate Date1 = Ephemeris_temp.get(1).getDate();

        return Date1.durationFrom(Date0);
    }

    private List<Cartesian> vectorToCartesian(final List<Vector3D> initPositionList) {

        final List<Cartesian> cartesianList = new ArrayList<>();

        for (Vector3D position : vector3DS) {
            final Cartesian posCartesian = new Cartesian(position.getX(), position.getY(), position.getZ());
            cartesianList.add(posCartesian);
        }
        return cartesianList;
    }

    private List<Vector3D> propagationSat() {

        final Orbit initialOrbit = this.orbits.get(0);
        final OrbitType propagationType = OrbitType.CARTESIAN;
        final SpacecraftState initialState = new SpacecraftState(initialOrbit);
        final List<Vector3D> toReturn;

        final double positionTolerance = 10.0;
        final double minStep = 0.001;
        final double maxStep = 1000.0;
        timeList = new ArrayList<>();
        absoluteDateList = new ArrayList<>();

        final double[][] tolerances = NumericalPropagator.tolerances(positionTolerance, initialOrbit, propagationType);
        final AdaptiveStepsizeIntegrator integrator = new DormandPrince853Integrator(minStep, maxStep, tolerances[0], tolerances[1]);

        final NumericalPropagator propagator = new NumericalPropagator(integrator);

        final NormalizedSphericalHarmonicsProvider provider = GravityFieldFactory.getNormalizedProvider(10, 10);
        final Frame frame_temp = this.frame;
        final ForceModel holmesFeatherstone = new HolmesFeatherstoneAttractionModel(frame_temp, provider);

        final Frame ITRF = FramesFactory.getITRF(IERSConventions.IERS_2010, true);
        final OneAxisEllipsoid earth = new OneAxisEllipsoid(Constants.WGS84_EARTH_EQUATORIAL_RADIUS, Constants.WGS84_EARTH_FLATTENING, ITRF);
        final NadirPointing nadirPointing = new NadirPointing(FramesFactory.getEME2000(), earth);

        propagator.setAttitudeProvider(nadirPointing);

        propagator.setOrbitType(propagationType);

        propagator.addForceModel(holmesFeatherstone);

        final EphemerisGenerator generator = propagator.getEphemerisGenerator();

        propagator.setInitialState(initialState);

        toReturn = multiplexerSetup(propagator);

        final GregorianDate gregorianDate = new GregorianDate(availability.getStop());
        final TimeScale UTC = TimeScalesFactory.getUTC();
        final AbsoluteDate stopDate = new AbsoluteDate(gregorianDate.getYear(), gregorianDate.getMonth(),
                gregorianDate.getDay(), gregorianDate.getHour(), gregorianDate.getMinute(), gregorianDate.getSecond(), UTC);

        final SpacecraftState finalState = propagator.propagate(stopDate);
        this.satellitePropagator = propagator;
        this.boundedPropagator = generator.getGeneratedEphemeris();

        return toReturn;
    }

    private List<Cartesian> propagationOrbit(final Orbit initialOrbit) {

        final List<Vector3D> propagatedParameters = propagationSat();

        this.vector3DS = propagatedParameters;

        final List<Cartesian> cartesians = vectorToCartesian(propagatedParameters);

        this.cartesianArraylist = cartesians;

        return cartesians;
    }

    private List<Vector3D> multiplexerSetup(final Propagator propagator) {

        final List<Vector3D> toReturn = new ArrayList<>();

        propagator.getMultiplexer().add(header.getStepSimulation(), new OrekitFixedStepHandler() {
            @Override
            public void handleStep(final SpacecraftState currentState) {
                final KeplerianOrbit o = (KeplerianOrbit) OrbitType.KEPLERIAN.convertType(currentState.getOrbit());
                toReturn.add(o.getPosition());
                final Attitude currentSpaceCraftAttitude = currentState.getAttitude();
                attitudes.add(currentSpaceCraftAttitude);
                final double x = toReturn.get(toReturn.size() - 1).getX();
                final double y = toReturn.get(toReturn.size() - 1).getY();
                final double z = toReturn.get(toReturn.size() - 1).getZ();
                final PositionType positionType = PositionType.CARTESIAN_POSITION;
                final Position tempPosition = new Position(x, y, z, positionType);
                positionsList.add(tempPosition);
                timeList.add(absDateToDouble(o.getDate()));
                absoluteDateList.add(o.getDate());
                orbits.add(o);
            }
        });
        return toReturn;
    }
}
