package org.example.CZMLObjects.CZMLPrimaryObjects;

import cesiumlanguagewriter.*;
import org.example.CZMLObjects.Show;
import org.example.CZMLObjects.Polyline;
import org.hipparchus.ode.events.Action;
import org.hipparchus.util.FastMath;
import org.orekit.estimation.measurements.GroundStation;
import org.orekit.frames.TopocentricFrame;
import org.orekit.propagation.BoundedPropagator;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.events.ElevationDetector;
import org.orekit.propagation.events.EventDetector;
import org.orekit.time.TimeScale;
import org.orekit.time.TimeScalesFactory;

import java.io.StringWriter;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LineOfVisibility implements CZMLPrimaryObject {

    private String id;
    private String name;
    private final List<TimeInterval> availability;
    private Polyline polyline;

    // Intrinsinc parameters
    private Reference referenceGroundStation;
    private Reference referenceSatellite;
    private final Iterable<Reference> references;
    private VisibilityCone visibilityCone;
    private final Satellite satellite;
    private final List<Show> showList;
    private List<TimeInterval> timeIntervals;
    private List<Boolean> visuList;

    private JulianDate TimeStartTemp;
    private JulianDate TimeStopTemp;
    private double angleOfAperture;

    public LineOfVisibility(VisibilityCone visibilityCone) {

        assert visibilityCone.getSatellite() != null;
        this.satellite = visibilityCone.getSatellite();
        this.id = visibilityCone.getGroundStation().getBaseFrame().getName() + "/" + satellite.getId();
        this.name = "Line between " + visibilityCone.getGroundStation().getBaseFrame().getName() + " and " + satellite.getName();
        this.visibilityCone = visibilityCone;
        Reference reference1 = new Reference(visibilityCone.getId() + "#position");
        Reference reference2 = new Reference(satellite.getId() + "#position");
        this.referenceGroundStation = reference1;
        this.referenceSatellite = reference2;
        Reference[] referenceList = Arrays.asList(referenceGroundStation, referenceSatellite).toArray(new Reference[0]);
        this.references = convert(referenceList);

        this.timeIntervals = new ArrayList<TimeInterval>();
        this.visuList = new ArrayList<Boolean>();
        this.showList = new ArrayList<Show>();
        this.buildTimeIntervalsAndVisu(visibilityCone, satellite);
        this.buildShowList();
        this.availability = getVisibilityInterval();

    }

    public LineOfVisibility(VisibilityCone visibilityCone, double angleOfAperture) {

        assert visibilityCone.getSatellite() != null;
        this.angleOfAperture = angleOfAperture;
        this.satellite = visibilityCone.getSatellite();
        this.id = visibilityCone.getGroundStation().getBaseFrame().getName() + "/" + satellite.getId();
        this.name = "Line between " + visibilityCone.getGroundStation().getBaseFrame().getName() + " and " + satellite.getName();
        this.visibilityCone = visibilityCone;
        Reference reference1 = new Reference(visibilityCone.getId() + "#position");
        Reference reference2 = new Reference(satellite.getId() + "#position");
        this.referenceGroundStation = reference1;
        this.referenceSatellite = reference2;
        Reference[] referenceList = Arrays.asList(referenceGroundStation, referenceSatellite).toArray(new Reference[0]);
        this.references = convert(referenceList);

        this.timeIntervals = new ArrayList<TimeInterval>();
        this.visuList = new ArrayList<Boolean>();
        this.showList = new ArrayList<Show>();
        this.buildTimeIntervalsAndVisu(visibilityCone, satellite);
        this.buildShowList();
        this.availability = getVisibilityInterval();

    }

    public LineOfVisibility(TopocentricFrame topocentricFrame, Satellite satellite, Header header){
        GroundStation OrekitGroundStation = new GroundStation(topocentricFrame);
        org.example.CZMLObjects.CZMLPrimaryObjects.GroundStation groundStation = new org.example.CZMLObjects.CZMLPrimaryObjects.GroundStation(OrekitGroundStation,header);
        VisibilityCone visibilityCone1 = new VisibilityCone(groundStation, satellite, header);
        visibilityCone1.noDisplay();
        visibilityCone1.generateCZML();
        this.satellite = satellite;
        this.id = OrekitGroundStation.getBaseFrame().getName() + "/" + satellite.getId();
        this.name = "Line between " + OrekitGroundStation.getBaseFrame().getName() + " and " + satellite.getName();
        this.visibilityCone = visibilityCone1;
        Reference reference1 = new Reference(visibilityCone.getId() + "#position");
        Reference reference2 = new Reference(satellite.getId() + "#position");
        this.referenceGroundStation = reference1;
        this.referenceSatellite = reference2;
        Reference[] referenceList = Arrays.asList(referenceGroundStation, referenceSatellite).toArray(new Reference[0]);
        this.references = convert(referenceList);

        this.timeIntervals = new ArrayList<TimeInterval>();
        this.visuList = new ArrayList<Boolean>();
        this.showList = new ArrayList<Show>();
        this.buildTimeIntervalsAndVisu(visibilityCone, satellite);
        this.buildShowList();
        this.availability = getVisibilityInterval();
    }

    public LineOfVisibility(TopocentricFrame topocentricFrame, Satellite satellite, Header header, double angleOfAperture){

        this.angleOfAperture = angleOfAperture;
        GroundStation OrekitGroundStation = new GroundStation(topocentricFrame);
        org.example.CZMLObjects.CZMLPrimaryObjects.GroundStation groundStation = new org.example.CZMLObjects.CZMLPrimaryObjects.GroundStation(OrekitGroundStation,header);
        VisibilityCone visibilityCone1 = new VisibilityCone(groundStation, satellite, header);
        visibilityCone1.noDisplay();
        visibilityCone1.generateCZML();
        this.satellite = satellite;
        this.id = OrekitGroundStation.getBaseFrame().getName() + "/" + satellite.getId();
        this.name = "Line between " + OrekitGroundStation.getBaseFrame().getName() + " and " + satellite.getName();
        this.visibilityCone = visibilityCone1;
        Reference reference1 = new Reference(visibilityCone.getId() + "#position");
        Reference reference2 = new Reference(satellite.getId() + "#position");
        this.referenceGroundStation = reference1;
        this.referenceSatellite = reference2;
        Reference[] referenceList = Arrays.asList(referenceGroundStation, referenceSatellite).toArray(new Reference[0]);
        this.references = convert(referenceList);

        this.timeIntervals = new ArrayList<TimeInterval>();
        this.visuList = new ArrayList<Boolean>();
        this.showList = new ArrayList<Show>();
        this.buildTimeIntervalsAndVisu(visibilityCone, satellite);
        this.buildShowList();
        this.availability = getVisibilityInterval();
    }

    @Override
    public void generateCZML() {
        output.setPrettyFormatting(true);
        try (PacketCesiumWriter packet = stream.openPacket(output)) {
            packet.writeId(this.id);
            packet.writeName(this.name);
            packet.writeAvailability(this.availability);

            Polyline polylineInput = new Polyline();
            try (PolylineCesiumWriter polylineWriter = packet.getPolylineWriter()) {
                polylineWriter.open(output);
                polylineWriter.writeWidthProperty(polylineInput.getWidth());
                try (PolylineMaterialCesiumWriter materialWriter = polylineWriter.getMaterialWriter()) {
                    materialWriter.open(output);
                    output.writeStartObject();
                    try (SolidColorMaterialCesiumWriter solidColorWriter = materialWriter.getSolidColorWriter()) {
                        solidColorWriter.open(output);
                        solidColorWriter.writeColorProperty(polylineInput.getColor());
                    }
                    output.writeEndObject();
                }

                polylineWriter.writeArcTypeProperty(polylineInput.getArcType());

                try (PositionListCesiumWriter positionWriter = polylineWriter.getPositionsWriter()) {
                    positionWriter.open(output);
                    positionWriter.writeReferences(references);
                }
                try (BooleanCesiumWriter showWriter = polylineWriter.getShowWriter()) {
                    showWriter.open(output);
                    output.writeStartSequence();
                    if (showList.size() == 1) {
                        Show showTemp = showList.get(0);
                        showWriter.writeInterval(showTemp.getAvailability());
                        showWriter.writeBoolean(showTemp.getShow());
                    } else {
                        for (int i = 0; i < showList.size(); i++) {
                            Show showTemp = showList.get(i);
                            if (i == 0) {
                                showWriter.writeInterval(showTemp.getAvailability());
                                showWriter.writeBoolean(showTemp.getShow());
                                output.writeEndObject();
                            } else if (i != showList.size() - 1) {
                                output.writeStartObject();
                                showWriter.writeInterval(showTemp.getAvailability());
                                showWriter.writeBoolean(showTemp.getShow());
                                output.writeEndObject();
                            } else {
                                output.writeStartObject();
                                showWriter.writeInterval(showTemp.getAvailability());
                                showWriter.writeBoolean(showTemp.getShow());
                            }
                        }
                    }
                }
                output.writeEndSequence();
            }
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


    private void buildTimeIntervalsAndVisu(VisibilityCone visibilityCone, Satellite satellite) {

        BoundedPropagator boundedPropagator = satellite.getBoundedPropagator();

        TimeScale UTC = TimeScalesFactory.getUTC();

        GregorianDate gregorianDate = new GregorianDate(1,1,1,0,0,0.0);

        JulianDate firstDate = new JulianDate(gregorianDate);

        JulianDate lastDate = absoluteDateToJulianDate(satellite.getOrbits().get(satellite.getOrbits().size()-1).getDate());

        List<JulianDate> TimeStarts = new ArrayList<JulianDate>();

        List<JulianDate> TimeStops = new ArrayList<JulianDate>();

        visuList.add(false);

        EventDetector visuDetector = null;

        if(angleOfAperture != 0) {
            visuDetector = new ElevationDetector(60, 0.001, visibilityCone.getGroundStation()
                    .getBaseFrame()).withConstantElevation(FastMath.toRadians(90-angleOfAperture)).withHandler((spacecraftState, detector, increasing) -> {

                if (increasing) {
                    TimeStarts.add(absoluteDateToJulianDate(spacecraftState.getDate()));
                    visuList.add(true);
                } else {
                    TimeStops.add(absoluteDateToJulianDate(spacecraftState.getDate()));
                    visuList.add(false);
                }
                return Action.CONTINUE;
            });
        }
        else{
            angleOfAperture = 80.0;
            visuDetector = new ElevationDetector(60, 0.001, visibilityCone.getGroundStation()
                    .getBaseFrame()).withConstantElevation(FastMath.toRadians(90-angleOfAperture)).withHandler((spacecraftState, detector, increasing) -> {

                if (increasing) {
                    TimeStarts.add(absoluteDateToJulianDate(spacecraftState.getDate()));
                    visuList.add(true);
                } else {
                    TimeStops.add(absoluteDateToJulianDate(spacecraftState.getDate()));
                    visuList.add(false);
                }
                return Action.CONTINUE;
            });
        }

        boundedPropagator.addEventDetector(visuDetector);

        boundedPropagator.propagate(satellite.getOrbits().get(satellite.getOrbits().size()-1).getDate());

        if(TimeStarts.isEmpty()){
            throw new RuntimeException("The satellite is not visible by the station for the given time interval");
        }

        TimeInterval firstTimeInterval = new TimeInterval(firstDate,TimeStarts.get(0));

        TimeInterval lastTimeInterval = new TimeInterval(TimeStops.get(TimeStops.size()-1),lastDate);

        timeIntervals.add(firstTimeInterval);

        for (int i = 0; i < visuList.size()- 3; i++) {
            if (!visuList.get(i)) {
                timeIntervals.add(new TimeInterval(TimeStarts.get(i / 2), TimeStops.get(i / 2)));
            } else {
                timeIntervals.add(new TimeInterval(TimeStops.get(i / 2), TimeStarts.get(i / 2 + 1)));
            }
        }
        visuList.add(false);

        timeIntervals.add(lastTimeInterval);

    }

    private void buildShowList(){
        for (int i = 0; i < timeIntervals.size(); i++) {
            Show showTemp = new Show(visuList.get(i),timeIntervals.get(i));
            showList.add(showTemp);
        }
    }

    private static Iterable<Reference> convert(Reference[] array) {
        return () -> Arrays.stream(array).iterator();
    }

    private List<TimeInterval> getVisibilityInterval() {
        List<TimeInterval> toReturn = new ArrayList<TimeInterval>();
        JulianDate firstInterval = null;
        JulianDate lastInterval = null;
        if(showList.size()==1){
            toReturn.add(showList.get(0).getAvailability());
            return toReturn;
        }
        else {
            for (int i = 1; i < showList.size(); i++) {
                Show showTemp = showList.get(i);
                Show showBefore = showList.get(i - 1);
                if (showTemp.getShow()) {
                    if (!showBefore.getShow()) {
                        firstInterval = showTemp.getAvailability().getStart();
                    }
                }
                if (!showTemp.getShow()) {
                    if (showBefore.getShow()) {
                        lastInterval = showTemp.getAvailability().getStop();
                        assert firstInterval != null;
                        toReturn.add(new TimeInterval(firstInterval, lastInterval));
                    }
                }
            }
            return toReturn;
        }
    }
}
