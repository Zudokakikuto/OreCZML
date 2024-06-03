package org.example.CZMLObjects.CZMLPrimaryObjects;

import cesiumlanguagewriter.*;
import org.example.CZMLAbstract.Model3D;
import org.example.CZMLObjects.CZMLSecondaryObects.Billboard;
import org.example.CZMLObjects.CZMLSecondaryObects.PositionType;
import org.example.CZMLObjects.CZMLSecondaryObects.SatelliteObjects.Path;
import org.example.CZMLObjects.CZMLSecondaryObects.SatelliteObjects.SatellitePosition;
import org.example.CZMLObjects.Position;
import org.example.Inputs.InputFiles.OEMFile;
import org.example.Inputs.OrbitInput.OrbitInput;
import org.example.Inputs.SpacecraftStateInput.SpacecraftStateListInput;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.hipparchus.ode.nonstiff.AdaptiveStepsizeIntegrator;
import org.hipparchus.ode.nonstiff.DormandPrince853Integrator;
import org.orekit.forces.ForceModel;
import org.orekit.forces.gravity.HolmesFeatherstoneAttractionModel;
import org.orekit.forces.gravity.potential.GravityFieldFactory;
import org.orekit.forces.gravity.potential.NormalizedSphericalHarmonicsProvider;
import org.orekit.frames.Frame;
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
import org.orekit.utils.TimeStampedPVCoordinates;

import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class Satellite implements CZMLPrimaryObject {

    private String Id;
    private String name;
    private TimeInterval availability;
    private String description;
    private List<TimeStampedPVCoordinates> Ephemeris;

    // Optional parameters
    private String model3Dpath;
    private Model3D model3D;

    // Intrinsic parameters
    private List<Vector3D> vector3DS;
    private List<Double> timeList;
    private List<Cartesian> cartesianArraylist;
    private List<Orbit> orbits;
    private Frame frame;
    private Billboard billboard;
    private List<Position> positionsList;
    private BoundedPropagator boundedPropagator;
    private Header header;

    public Satellite(OEMFile file, Header header) {

        this.Id = file.getObjectID();
        this.name = "Satellite";
        this.description = "A satellite";
        Frame frame = file.getFrame();
        this.availability = header.getClock().getInterval();
        this.Ephemeris = file.getEphemeris();

        double step = this.getStepBetweenDates();
        List<Vector3D> vector3DS1 = new ArrayList<>();
        List<Double> timeList = new ArrayList<>();
        List<Orbit> orbitList = new ArrayList<Orbit>();
        List<Position> positions = new ArrayList<Position>();

        for (int i = 0; i < Ephemeris.size(); i++) {
            timeList.add(dateToDouble(Ephemeris.get(i).getDate()));
            vector3DS1.add(Ephemeris.get(i).getPosition());
            double x = vector3DS1.get(i).getX();
            double y = vector3DS1.get(i).getY();
            double z = vector3DS1.get(i).getZ();
            PositionType positionType = PositionType.CARTESIAN_POSITION;
            Position tempPosition = new Position(x, y, z, positionType);
            positions.add(tempPosition);
            KeplerianOrbit tempOrbit = new KeplerianOrbit(Ephemeris.get(i), frame, Constants.WGS84_EARTH_MU);
            orbitList.add(tempOrbit);
        }

        this.positionsList = positions;
        this.vector3DS = vector3DS1;
        this.timeList = timeList;
        this.orbits = orbitList;
        this.frame = frame;
        List<Vector3D> toCartesians = propagationSat();
        this.cartesianArraylist = vectorToCartesian(toCartesians);
        this.header = header;
    }

    public Satellite(OEMFile file, Header header, String model3Dpath) {

        this.Id = file.getObjectID();
        this.name = "Satellite";
        this.description = "A satellite";
        Frame frame = file.getFrame();
        this.availability = header.getClock().getInterval();
        this.Ephemeris = file.getEphemeris();

        double step = this.getStepBetweenDates();
        List<Vector3D> vector3DS1 = new ArrayList<>();
        List<Double> timeList = new ArrayList<>();
        List<Orbit> orbitList = new ArrayList<Orbit>();
        List<Position> positions = new ArrayList<Position>();

        for (int i = 0; i < Ephemeris.size(); i++) {
            timeList.add(dateToDouble(Ephemeris.get(i).getDate()));
            vector3DS1.add(Ephemeris.get(i).getPosition());
            double x = vector3DS1.get(i).getX();
            double y = vector3DS1.get(i).getY();
            double z = vector3DS1.get(i).getZ();
            PositionType positionType = PositionType.CARTESIAN_POSITION;
            Position tempPosition = new Position(x, y, z, positionType);
            positions.add(tempPosition);
            KeplerianOrbit tempOrbit = new KeplerianOrbit(Ephemeris.get(i), frame, Constants.WGS84_EARTH_MU);
            orbitList.add(tempOrbit);
        }

        this.positionsList = positions;
        this.vector3DS = vector3DS1;
        this.timeList = timeList;
        this.orbits = orbitList;
        this.frame = frame;
        List<Vector3D> toCartesians = propagationSat();
        this.cartesianArraylist = vectorToCartesian(toCartesians);

        this.model3Dpath = model3Dpath;
        this.header = header;
    }

    public Satellite(Header header, String ID, String name, TimeInterval availability, String description, List<TimeStampedPVCoordinates> Ephemeris, Frame frame) {
        this.Id = "Satellite";
        this.name = name;
        this.availability = availability;
        this.description = "A test satellite";
        this.Ephemeris = Ephemeris;

        List<Orbit> orbitList = new ArrayList<Orbit>();
        double step = this.getStepBetweenDates();
        List<Vector3D> vector3DS1 = new ArrayList<>();
        List<Double> timeList = new ArrayList<>();
        List<Position> positions = new ArrayList<Position>();

        for (int i = 0; i < Ephemeris.size(); i++) {
            timeList.add(dateToDouble(Ephemeris.get(i).getDate()));
            vector3DS1.add(Ephemeris.get(i).getPosition());
            double x = vector3DS1.get(i).getX();
            double y = vector3DS1.get(i).getY();
            double z = vector3DS1.get(i).getZ();
            PositionType positionType = PositionType.CARTESIAN_POSITION;
            Position tempPosition = new Position(x, y, z, positionType);
            positions.add(tempPosition);
            KeplerianOrbit tempOrbit = new KeplerianOrbit(Ephemeris.get(i), frame, Constants.WGS84_EARTH_MU);
            orbitList.add(tempOrbit);
        }

        this.positionsList = positions;
        this.vector3DS = vector3DS1;
        this.timeList = timeList;
        this.orbits = orbitList;
        this.frame = frame;
        List<Vector3D> toCartesians = propagationSat();
        this.cartesianArraylist = vectorToCartesian(toCartesians);
        this.header = header;

    }

    public Satellite(Header header, String ID, String name, TimeInterval availability, String description, List<TimeStampedPVCoordinates> Ephemeris, Frame frame, String model3Dpath) {
        this.Id = "Satellite";
        this.name = name;
        this.availability = availability;
        this.description = "A test satellite";
        this.Ephemeris = Ephemeris;

        List<Orbit> orbitList = new ArrayList<Orbit>();
        double step = this.getStepBetweenDates();
        List<Vector3D> vector3DS1 = new ArrayList<>();
        List<Double> timeList = new ArrayList<>();
        List<Position> positions = new ArrayList<Position>();

        for (int i = 0; i < Ephemeris.size(); i++) {
            timeList.add(dateToDouble(Ephemeris.get(i).getDate()));
            vector3DS1.add(Ephemeris.get(i).getPosition());
            double x = vector3DS1.get(i).getX();
            double y = vector3DS1.get(i).getY();
            double z = vector3DS1.get(i).getZ();
            PositionType positionType = PositionType.CARTESIAN_POSITION;
            Position tempPosition = new Position(x, y, z, positionType);
            positions.add(tempPosition);
            KeplerianOrbit tempOrbit = new KeplerianOrbit(Ephemeris.get(i), frame, Constants.WGS84_EARTH_MU);
            orbitList.add(tempOrbit);
        }

        this.positionsList = positions;
        this.vector3DS = vector3DS1;
        this.timeList = timeList;
        this.orbits = orbitList;
        this.frame = frame;
        List<Vector3D> toCartesians = propagationSat();
        this.cartesianArraylist = vectorToCartesian(toCartesians);

        this.model3Dpath = model3Dpath;
        this.header = header;
    }

    public Satellite(OrbitInput orbitInput, Header header) {
        AbsoluteDate startTime = orbitInput.getStartTime();
        AbsoluteDate stopTime = orbitInput.getStopTime();
        this.timeList = new ArrayList<Double>();
        this.orbits = new ArrayList<Orbit>();
        this.positionsList = new ArrayList<Position>();

        this.Id = orbitInput.getOrbit().toString();
        this.name = "A nameless satellite";
        this.description = "A satellite from a defined orbit";
        this.availability = header.getClock().getInterval();
        timeList.add(dateToDouble(startTime));
        timeList.add(dateToDouble(stopTime));
        orbits.add(orbitInput.getOrbit());
        this.frame = orbitInput.getFrame();
        this.cartesianArraylist = propagationOrbit(orbitInput.getOrbit());
        this.header = header;

    }

    public Satellite(OrbitInput orbitInput, Header header, String model3Dpath) {
        AbsoluteDate startTime = orbitInput.getStartTime();
        AbsoluteDate stopTime = orbitInput.getStopTime();
        this.timeList = new ArrayList<Double>();
        this.orbits = new ArrayList<Orbit>();
        this.positionsList = new ArrayList<Position>();

        this.Id = orbitInput.getOrbit().toString();
        this.name = "A nameless satellite";
        this.description = "A satellite from a defined orbit";
        this.availability = header.getClock().getInterval();
        timeList.add(dateToDouble(startTime));
        timeList.add(dateToDouble(stopTime));
        orbits.add(orbitInput.getOrbit());
        this.frame = orbitInput.getFrame();
        this.cartesianArraylist = propagationOrbit(orbitInput.getOrbit());

        this.model3Dpath = model3Dpath;
        this.header = header;
    }

    public Satellite(SpacecraftStateListInput input, Header header) {

        AbsoluteDate startTime = input.getStartTime();
        AbsoluteDate stopTime = input.getStopTime();
        this.timeList = new ArrayList<Double>();
        this.orbits = new ArrayList<Orbit>();
        this.positionsList = new ArrayList<Position>();

        this.Id = input.getOrbits().get(0).toString();
        this.name = "NameLess Satellite";
        this.description = "A satellite from SpaceCraftStates";
        this.availability = header.getClock().getInterval();
        this.orbits = input.getOrbits();
        this.vector3DS = input.getPositions();
        this.timeList = input.getTimeList();
        List<Vector3D> toCartesians = propagationSat();
        this.cartesianArraylist = vectorToCartesian(toCartesians);
        this.header = header;

    }

    public Satellite(SpacecraftStateListInput input, Header header, String model3Dpath) {

        AbsoluteDate startTime = input.getStartTime();
        AbsoluteDate stopTime = input.getStopTime();
        this.timeList = new ArrayList<Double>();
        this.orbits = new ArrayList<Orbit>();
        this.positionsList = new ArrayList<Position>();

        this.Id = input.getOrbits().get(0).toString();
        this.name = "NameLess Satellite";
        this.description = "A satellite from SpaceCraftStates";
        this.availability = header.getClock().getInterval();
        this.orbits = input.getOrbits();
        this.vector3DS = input.getPositions();
        this.timeList = input.getTimeList();
        List<Vector3D> toCartesians = propagationSat();
        this.cartesianArraylist = vectorToCartesian(toCartesians);

        this.model3Dpath = model3Dpath;
        this.header = header;
    }

    public Satellite(Propagator propagator, AbsoluteDate finalDate, Header header) {

        if(propagator.getInitialState().getOrbit() == null){
            throw new RuntimeException("The propagator has no initial state, please setup the propagator before building" +
                    "the satellite");
        }
        else {
            this.availability = header.getClock().getInterval();
            this.Id = propagator.getInitialState().getOrbit().toString();
            this.description = "A satellite created with a propagator";
            this.frame = propagator.getFrame();
            this.header = header;

            // Creation of empty list to be filled with multiplexerSetup
            this.timeList = new ArrayList<Double>();
            this.orbits = new ArrayList<Orbit>();
            this.positionsList = new ArrayList<Position>();

            orbits.add(propagator.getInitialState().getOrbit());

            // Setup propagator
            EphemerisGenerator generator = propagator.getEphemerisGenerator();
            this.vector3DS = multiplexerSetup(propagator);

            // Propagation
            propagator.propagate(finalDate);

            // Retrieve parameters from propagation
            this.boundedPropagator = generator.getGeneratedEphemeris();
            this.cartesianArraylist = vectorToCartesian(vector3DS);
        }
    }

    @Override
    public StringWriter getStringWriter() {
        return stringWriter;
    }

    @Override
    public void endFile() {
        output.writeEndSequence();
    }

    public String getId() {
        return Id;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public List<Double> getTimeList() {
        return timeList;
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

    public List<Cartesian> getCartesianArraylist() {
        return cartesianArraylist;
    }

    public Frame getFrame() {
        return frame;
    }

    public List<Position> getPositionsList() {
        return positionsList;
    }

    public BoundedPropagator getBoundedPropagator() {
        return boundedPropagator;
    }

    @Override
    public void generateCZML() {
        output.setPrettyFormatting(true);
        try (PacketCesiumWriter packet = stream.openPacket(output)) {
            packet.writeId(Id);
            packet.writeName(name);
            packet.writeAvailability(availability);

            generateDisplay(packet);

            generatePath(packet);

            generatePosition(packet);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private void generateDisplay(PacketCesiumWriter packet) throws URISyntaxException {

        if (model3Dpath == null) {
            try (BillboardCesiumWriter billboardWriter = packet.getBillboardWriter()) {
                billboardWriter.open(output);
                String imageStr = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAADsMAAA7DAcdvqGQAAADJSURBVDhPnZHRDcMgEEMZjVEYpaNklIzSEfLfD4qNnXAJSFWfhO7w2Zc0Tf9QG2rXrEzSUeZLOGm47WoH95x3Hl3jEgilvDgsOQUTqsNl68ezEwn1vae6lceSEEYvvWNT/Rxc4CXQNGadho1NXoJ+9iaqc2xi2xbt23PJCDIB6TQjOC6Bho/sDy3fBQT8PrVhibU7yBFcEPaRxOoeTwbwByCOYf9VGp1BYI1BA+EeHhmfzKbBoJEQwn1yzUZtyspIQUha85MpkNIXB7GizqDEECsAAAAASUVORK5CYII=";
                Billboard billboard = new Billboard(imageStr);
                billboardWriter.writeHorizontalOriginProperty(billboard.getCesiumHorizontalOrigin());
                billboardWriter.writeColorProperty(billboard.getRed(), billboard.getGreen(), billboard.getBlue(), billboard.getAlpha());
                try (UriCesiumWriter imageBillBoard = billboardWriter.openImageProperty()) {
                    imageBillBoard.writeUri(billboard.getImageStr(), billboard.getCesiumResourceBehavior());
                }
                billboardWriter.writeScaleProperty(billboard.getScale());
                billboardWriter.writeShowProperty(billboard.getShow());
            }
        } else {
            this.model3D = new Model3D(model3Dpath, header);
            model3D.generateCZML(packet, output);
        }
    }

    private void generatePath(PacketCesiumWriter packet) {
        try (PathCesiumWriter pathProperty = packet.openPathProperty()) {
            Path path = new Path(availability, packet);
            try (BooleanCesiumWriter showPath = pathProperty.openShowProperty()) {
                showPath.writeInterval(availability.getStart(), availability.getStop());
                showPath.writeBoolean(Path.getShow());
            }
        }
    }

    private void generatePosition(PacketCesiumWriter packet) {
        ArrayList<JulianDate> dates = new ArrayList<>();
        ArrayList<Cartesian> positions = new ArrayList<>();

        SatellitePosition Pos = new SatellitePosition(this.cartesianArraylist, timeList);

        try (PositionCesiumWriter writer = packet.openPositionProperty()) {
            writer.writeReferenceFrame(Pos.getReferenceFrame());
            writer.writeInterpolationAlgorithm(Pos.getCesiumInterpolationAlgorithm());
            writer.writeInterpolationDegree(Pos.getInterpolationDegree());
            writer.writeCartesian(Pos.getDates(), Pos.getPositions());
        }
    }

    private double getStepBetweenDates() {
        List<TimeStampedPVCoordinates> Ephemeris = this.Ephemeris;

        AbsoluteDate Date0 = Ephemeris.get(0).getDate();
        AbsoluteDate Date1 = Ephemeris.get(1).getDate();
        double step = Date1.durationFrom(Date0);

        return step;
    }


    private List<Cartesian> vectorToCartesian(List<Vector3D> initPositionList) {

        List<Cartesian> cartesianList = new ArrayList<>();

        for (Vector3D position : vector3DS) {
            Cartesian posCartesian = new Cartesian(position.getX(), position.getY(), position.getZ());
            cartesianList.add(posCartesian);
        }
        return cartesianList;
    }

    private List<Vector3D> propagationSat() {

        final Orbit initialOrbit = this.orbits.get(0);
        final OrbitType propagationType = OrbitType.CARTESIAN;
        final SpacecraftState initialState = new SpacecraftState(initialOrbit);
        List<Vector3D> toReturn;

        final double positionTolerance = 10.0;
        final double minStep = 0.001;
        final double maxStep = 1000.0;
        timeList = new ArrayList<>();

        final double[][] tolerances = NumericalPropagator.tolerances(positionTolerance, initialOrbit, propagationType);
        final AdaptiveStepsizeIntegrator integrator = new DormandPrince853Integrator(minStep, maxStep, tolerances[0], tolerances[1]);

        final NumericalPropagator propagator = new NumericalPropagator(integrator);


        final NormalizedSphericalHarmonicsProvider provider = GravityFieldFactory.getNormalizedProvider(10, 10);
        Frame frame = this.frame;
        final ForceModel holmesFeatherstone = new HolmesFeatherstoneAttractionModel(frame, provider);

        propagator.setOrbitType(propagationType);

        propagator.addForceModel(holmesFeatherstone);

        EphemerisGenerator generator = propagator.getEphemerisGenerator();

        propagator.setInitialState(initialState);

        toReturn = multiplexerSetup(propagator);

        GregorianDate gregorianDate = new GregorianDate(availability.getStop());
        TimeScale UTC = TimeScalesFactory.getUTC();
        AbsoluteDate stopDate = new AbsoluteDate(gregorianDate.getYear(), gregorianDate.getMonth(),
                gregorianDate.getDay(), gregorianDate.getHour(), gregorianDate.getMinute(), gregorianDate.getSecond(), UTC);

        final SpacecraftState finalState = propagator.propagate(stopDate);
        final KeplerianOrbit finalOrbit = (KeplerianOrbit) OrbitType.KEPLERIAN.convertType(finalState.getOrbit());

        this.boundedPropagator = generator.getGeneratedEphemeris();

        return toReturn;
    }

    private List<Cartesian> propagationOrbit(Orbit initialOrbit) {

        List<Vector3D> propagatedParameters = propagationSat();

        this.vector3DS = propagatedParameters;

        List<Cartesian> cartesians = vectorToCartesian(propagatedParameters);

        this.cartesianArraylist = cartesians;

        return cartesians;
    }

    private List<Vector3D> multiplexerSetup(Propagator propagator) {
        List<Vector3D> toReturn = new ArrayList<Vector3D>();
        propagator.getMultiplexer().add(60.0, new OrekitFixedStepHandler() {
            @Override
            public void handleStep(SpacecraftState currentState) {
                final KeplerianOrbit o = (KeplerianOrbit) OrbitType.KEPLERIAN.convertType(currentState.getOrbit());
                toReturn.add(o.getPosition());
                double x = toReturn.get(toReturn.size() - 1).getX();
                double y = toReturn.get(toReturn.size() - 1).getY();
                double z = toReturn.get(toReturn.size() - 1).getZ();
                PositionType positionType = PositionType.CARTESIAN_POSITION;
                Position tempPosition = new Position(x, y, z, positionType);
                positionsList.add(tempPosition);
                timeList.add(dateToDouble(o.getDate()));
                orbits.add(o);
            }
        });
        return toReturn;
    }
}
