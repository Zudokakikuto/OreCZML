package org.example.CZMLObjects.CZMLPrimaryObjects;

import cesiumlanguagewriter.*;
import org.example.CZMLObjects.CZMLSecondaryObects.SatelliteObjects.Billboard;
import org.example.CZMLObjects.CZMLSecondaryObects.SatelliteObjects.Path;
import org.example.CZMLObjects.CZMLSecondaryObects.SatelliteObjects.Position;
import org.example.Inputs.InputFiles.OEMFile;
import org.example.Inputs.OrbitInput.OrbitInput;
import org.example.Inputs.SpacecraftStateInput.SpacecraftStateListInput;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.hipparchus.ode.nonstiff.AdaptiveStepsizeIntegrator;
import org.hipparchus.ode.nonstiff.DormandPrince853Integrator;
import org.hipparchus.util.FastMath;
import org.orekit.forces.ForceModel;
import org.orekit.forces.gravity.HolmesFeatherstoneAttractionModel;
import org.orekit.forces.gravity.potential.GravityFieldFactory;
import org.orekit.forces.gravity.potential.NormalizedSphericalHarmonicsProvider;
import org.orekit.frames.Frame;
import org.orekit.orbits.KeplerianOrbit;
import org.orekit.orbits.Orbit;
import org.orekit.orbits.OrbitType;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.numerical.NumericalPropagator;
import org.orekit.propagation.sampling.OrekitFixedStepHandler;
import org.orekit.time.*;
import org.orekit.utils.Constants;
import org.orekit.utils.TimeStampedPVCoordinates;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class Satellite implements CZMLPrimaryObject {

    private String ID;
    private String name;
    private TimeInterval availability;
    private String description;
    private List<TimeStampedPVCoordinates> Ephemeris;

    // Intrinsic parameters
    private List<Vector3D> positionList;
    private List<Double> timeList;
    private List<Cartesian> cartesianArraylist;
    private List<Orbit> orbits;
    private Frame frame;

    public Satellite(OEMFile file) {
        this.ID = file.getObjectID();
        this.name = "Satellite";
        this.description = "A satellite";
        JulianDate startJd = file.getJulianDate(file.getStartTime(), file.getTimeScale());
        JulianDate stopJd = file.getJulianDate(file.getStopTime(), file.getTimeScale());
        Frame frame = file.getFrame();
        this.availability = new TimeInterval(startJd, stopJd);
        this.Ephemeris = file.getEphemeris();

        double step = this.getStepBetweenDates();
        List<Vector3D> positionList = new ArrayList<>();
        List<Double> timeList = new ArrayList<>();
        List<Orbit> orbitList = new ArrayList<Orbit>();

        for (int i = 0; i < Ephemeris.size(); i++) {
            timeList.add(dateToDouble(Ephemeris.get(i).getDate()));
            positionList.add(Ephemeris.get(i).getPosition());
            KeplerianOrbit tempOrbit = new KeplerianOrbit(Ephemeris.get(i), frame, Constants.WGS84_EARTH_MU);
            orbitList.add(tempOrbit);
        }

        this.positionList = positionList;
        this.timeList = timeList;
        this.orbits = orbitList;
        this.frame = frame;
        List<Vector3D> toCartesians = checkPropagation(positionList);
        this.cartesianArraylist = vectorToCartesian(toCartesians);
    }

    public Satellite(String ID, String name, TimeInterval availability, String description, List<TimeStampedPVCoordinates> Ephemeris, Frame frame) {
        this.ID = "Satellite";
        this.name = name;
        this.availability = availability;
        this.description = "A test satellite";
        this.Ephemeris = Ephemeris;

        List<Orbit> orbitList = new ArrayList<Orbit>();
        double step = this.getStepBetweenDates();
        List<Vector3D> positionList = new ArrayList<>();
        List<Double> timeList = new ArrayList<>();

        for (int i = 0; i < Ephemeris.size(); i++) {
            timeList.add(dateToDouble(Ephemeris.get(i).getDate()));
            positionList.add(Ephemeris.get(i).getPosition());
            KeplerianOrbit tempOrbit = new KeplerianOrbit(Ephemeris.get(i), frame, Constants.WGS84_EARTH_MU);
            orbitList.add(tempOrbit);
        }

        this.positionList = positionList;
        this.timeList = timeList;
        this.orbits = orbitList;
        this.frame = frame;
        List<Vector3D> toCartesians = checkPropagation(positionList);
        this.cartesianArraylist = vectorToCartesian(toCartesians);
    }

    public Satellite(OrbitInput orbitInput) {
        AbsoluteDate startTime = orbitInput.getStartTime();
        AbsoluteDate stopTime = orbitInput.getStopTime();
        this.timeList = new ArrayList<Double>();
        this.orbits = new ArrayList<Orbit>();

        this.ID = "Satellite";
        this.name = "NameLess Satellite";
        this.description = "A satellite from a defined orbit";
        this.availability = orbitInput.getTimeInterval();
        timeList.add(dateToDouble(startTime));
        timeList.add(dateToDouble(stopTime));
        orbits.add(orbitInput.getOrbit());
        this.frame = orbitInput.getFrame();
        this.cartesianArraylist = propagationOrbit(orbitInput.getOrbit());
    }

    public Satellite(SpacecraftStateListInput input){

        AbsoluteDate startTime = input.getStartTime();
        AbsoluteDate stopTime = input.getStopTime();
        this.timeList = new ArrayList<Double>();
        this.orbits = new ArrayList<Orbit>();

        this.ID = "Satellite";
        this.name = "NameLess Satellite";
        this.description = "A satellite from SpaceCraftStates";
        this.availability = input.getTimeInterval();
        this.orbits = input.getOrbits();
        this.positionList = input.getPositions();
        this.timeList = input.getTimeList();
        List<Vector3D> toCartesians = checkPropagation(positionList);
        this.cartesianArraylist = vectorToCartesian(toCartesians);
    }

    @Override
    public StringWriter getStringWriter() {
        return stringWriter;
    }

    @Override
    public void endFile() {
        output.writeEndSequence();
    }

    public String getID() {
        return ID;
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

    public List<Vector3D> getPositionList() {
        return positionList;
    }

    public TimeInterval getAvailability() {
        return availability;
    }

    public List<TimeStampedPVCoordinates> getEphemeris() {
        return Ephemeris;
    }

    @Override
    public void write() {
        output.setPrettyFormatting(true);
        try (PacketCesiumWriter packet = stream.openPacket(output)) {
            packet.writeId(ID);
            packet.writeName(name);
            packet.writeAvailability(availability);

            writeBillBoard(packet);

            writePath(packet);

            writePosition(packet);
        }
    }

    private void writeBillBoard(PacketCesiumWriter packet){
        try (BillboardCesiumWriter billboardWriter = packet.getBillboardWriter()) {
            billboardWriter.open(output);
            Billboard billboard = new Billboard();
            billboardWriter.writeHorizontalOriginProperty(billboard.getCesiumHorizontalOrigin());
            billboardWriter.writeColorProperty(billboard.getRed(), billboard.getGreen(), billboard.getBlue(), billboard.getAlpha());
            try (UriCesiumWriter imageBillBoard = billboardWriter.openImageProperty()) {
                imageBillBoard.writeUri(billboard.getImageStr(), billboard.getCesiumResourceBehavior());
            }
            billboardWriter.writeScaleProperty(billboard.getScale());
            billboardWriter.writeShowProperty(billboard.getShow());
        }
    }

    private void writePath(PacketCesiumWriter packet){
        try (PathCesiumWriter pathProperty = packet.openPathProperty()) {
            Path path = new Path(availability,packet);
            try (BooleanCesiumWriter showPath = pathProperty.openShowProperty()) {
                showPath.writeInterval(availability.getStart(), availability.getStop());
                showPath.writeBoolean(Path.getShow());
            }
        }
    }

    private void writePosition(PacketCesiumWriter packet){
        ArrayList<JulianDate> dates = new ArrayList<>();
        ArrayList<Cartesian> positions = new ArrayList<>();

        Position Pos = new Position(this.cartesianArraylist,timeList);

        try(PositionCesiumWriter writer = packet.openPositionProperty()){
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

        for (Vector3D position : positionList) {
            Cartesian posCartesian = new Cartesian(position.getX(), position.getY(), position.getZ());
            cartesianList.add(posCartesian);
        }
        return cartesianList;
    }

    private List<Vector3D> checkPropagation(List<Vector3D> initialPositionList) {

        double timeInterval = FastMath.abs(timeList.get(0) - timeList.get(1));

        if (timeInterval > 60.0) {
            final Orbit initialOrbit = this.orbits.get(0);
            final OrbitType propagationType = OrbitType.CARTESIAN;
            final SpacecraftState initialState = new SpacecraftState(initialOrbit);
            List<Vector3D> toReturn = new ArrayList<Vector3D>();

            final double positionTolerance = 10.0;
            final double minStep = 0.001;
            final double maxStep = 1000.0;
            timeList = new ArrayList<Double>();

            final double[][] tolerances = NumericalPropagator.tolerances(positionTolerance, initialOrbit, propagationType);
            final AdaptiveStepsizeIntegrator integrator = new DormandPrince853Integrator(minStep, maxStep, tolerances[0], tolerances[1]);

            final NumericalPropagator propagator = new NumericalPropagator(integrator);

            final NormalizedSphericalHarmonicsProvider provider = GravityFieldFactory.getNormalizedProvider(10, 10);
            Frame frame = this.frame;
            final ForceModel holmesFeatherstone = new HolmesFeatherstoneAttractionModel(frame, provider);

            propagator.setOrbitType(propagationType);

            propagator.addForceModel(holmesFeatherstone);

            propagator.setInitialState(initialState);

            propagator.getMultiplexer().add(60.0, new OrekitFixedStepHandler() {
                @Override
                public void handleStep(SpacecraftState currentState) {
                    final KeplerianOrbit o = (KeplerianOrbit) OrbitType.KEPLERIAN.convertType(currentState.getOrbit());
                    toReturn.add(o.getPosition());
                    timeList.add(dateToDouble(o.getDate()));
                }
            });

            GregorianDate gregorianDate = new GregorianDate(availability.getStop());
            System.out.println(gregorianDate);
            TimeScale UTC = TimeScalesFactory.getUTC();
            AbsoluteDate stopDate = new AbsoluteDate(gregorianDate.getYear(),gregorianDate.getMonth(),
                    gregorianDate.getDay(),gregorianDate.getHour(),gregorianDate.getMinute(),gregorianDate.getSecond(),UTC);

            final SpacecraftState finalState = propagator.propagate(stopDate);
            final KeplerianOrbit finalOrbit = (KeplerianOrbit) OrbitType.KEPLERIAN.convertType(finalState.getOrbit());

            return toReturn;
        } else {
            return initialPositionList;
        }
    }

    private List<Cartesian> propagationOrbit(Orbit initialOrbit){

        List<Vector3D> initialParameters = new ArrayList<Vector3D>();
        initialParameters.add(initialOrbit.getPosition());

        List<Vector3D> propagatedParameters = checkPropagation(initialParameters);

        this.positionList = propagatedParameters;

        List<Cartesian> cartesians = vectorToCartesian(propagatedParameters);

        this.cartesianArraylist = cartesians;

        return cartesians;
    }
}
