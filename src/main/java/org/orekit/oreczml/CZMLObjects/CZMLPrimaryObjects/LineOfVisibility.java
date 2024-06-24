/** .*/
package org.example.CZMLObjects.CZMLPrimaryObjects;
import cesiumlanguagewriter.*;
import org.example.CZMLObjects.CZMLShow;
import org.example.CZMLObjects.Polyline;
import org.hipparchus.ode.events.Action;
import org.hipparchus.util.FastMath;
import org.orekit.estimation.measurements.GroundStation;
import org.orekit.frames.TopocentricFrame;
import org.orekit.propagation.BoundedPropagator;
import org.orekit.propagation.events.ElevationDetector;
import org.orekit.time.TimeScale;
import org.orekit.time.TimeScalesFactory;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LineOfVisibility implements CZMLPrimaryObject {

    /** .*/
    public static final double DEFAULT_ANGLE_OF_APERTURE = 80.0;
    /** .*/
    public static final String DEFAULT_LINE_BETWEEN = "Line between ";
    /** .*/
    public static final String DEFAULT_AND = " and ";
    /** .*/
    public static final String DEFAULT_H_POSITION = "#position";
    /** .*/
    public static final String DEFAULT_NOT_SEEN = "The satellite is not visible by ";
    /** .*/
    public static final String DEFAULT_NOT_SEEN_2 =  " for the given time interval";
    /** .*/
    private String id = "";
    /** .*/
    private List<List<String>> multipleId = new ArrayList<>();
    /** .*/
    private String name = "";
    /** .*/
    private List<List<String>> multipleName = new ArrayList<>();
    /** .*/
    private List<TimeInterval> availability = new ArrayList<>();
    /** .*/
    private List<List<List<TimeInterval>>> multipleAvailability = new ArrayList<>();


    // Intrinsinc parameters
    /** .*/
    private Reference referenceGroundStation = null;
    /** .*/
    private Reference referenceSatellite = null;
    /** .*/
    private Iterable<Reference> references = null;
    /** .*/
    private List<List<Iterable<Reference>>> multipleReferences = new ArrayList<>();
    /** .*/
    private Satellite satellite = null;
    /** .*/
    private List<List<Satellite>> multipleSatellite = new ArrayList<>();
    /** .*/
    private List<CZMLShow> showList = new ArrayList<>();
    /** .*/
    private List<List<List<CZMLShow>>> multipleShowList = new ArrayList<>();
    /** .*/
    private List<TimeInterval> timeIntervals = new ArrayList<>();
    /** .*/
    private List<List<List<TimeInterval>>> multipleTimeIntervals = new ArrayList<>();
    /** .*/
    private List<Boolean> visuList = new ArrayList<>();
    /** .*/
    private List<List<List<Boolean>>> multipleVisuList = new ArrayList<>();
    /** .*/
    private double angleOfAperture = 0.0;
    /** .*/
    private List<List<Double>> multipleAngleOfAperture = new ArrayList<>();
    /** .*/
    private List<LineOfVisibility> allVisibilityLines = new ArrayList<>();
    /**.*/
    private List<VisibilityCone> allVisibilityCones = new ArrayList<>();

    // BUILDERS

    public LineOfVisibility(final VisibilityCone visibilityCone) {
        this(visibilityCone, DEFAULT_ANGLE_OF_APERTURE);
    }

    public LineOfVisibility(final VisibilityCone visibilityCone, final double angleOfAperture) {

        final GroundStation groundStation = visibilityCone.getGroundStation();
        assert visibilityCone.getSatellite() != null;
        this.angleOfAperture = angleOfAperture;
        this.satellite = visibilityCone.getSatellite();
        this.id = visibilityCone.getGroundStation().getBaseFrame().getName() + "/" + satellite.getId();
        this.name = DEFAULT_LINE_BETWEEN + visibilityCone.getGroundStation().getBaseFrame().getName() + DEFAULT_AND + satellite.getName();
        allVisibilityCones.add(visibilityCone);
        final Reference reference1 = new Reference(visibilityCone.getId() + DEFAULT_H_POSITION);
        final Reference reference2 = new Reference(satellite.getId() + DEFAULT_H_POSITION);
        this.referenceGroundStation = reference1;
        this.referenceSatellite = reference2;
        final Reference[] referenceList = Arrays.asList(referenceGroundStation, referenceSatellite).toArray(new Reference[0]);
        this.references = convertToIterable(referenceList);

        this.timeIntervals = new ArrayList<TimeInterval>();
        this.visuList = new ArrayList<Boolean>();
        this.showList = new ArrayList<CZMLShow>();
        this.buildSingleTimeIntervalsAndVisu(groundStation, visibilityCone.getSatellite());
        this.buildShowList();
        this.availability = getVisibilityInterval(showList);

    }

    public LineOfVisibility(final TopocentricFrame topocentricFrame, final Satellite satellite, final Header header) {
        this(topocentricFrame, satellite, header, DEFAULT_ANGLE_OF_APERTURE);
    }

    public LineOfVisibility(final TopocentricFrame topocentricFrame, final Satellite satellite, final Header header, final double angleOfAperture) {

        this.angleOfAperture = angleOfAperture;
        final GroundStation OrekitGroundStation = new GroundStation(topocentricFrame);
        final CZMLGroundStation groundStation = new CZMLGroundStation(OrekitGroundStation, header);
        final VisibilityCone visibilityCone1 = new VisibilityCone(groundStation, satellite, header);
        visibilityCone1.noDisplay();
        allVisibilityCones.add(visibilityCone1);
        this.id = OrekitGroundStation.getBaseFrame().getName() + "/" + satellite.getId();
        this.satellite = satellite;
        this.name = DEFAULT_LINE_BETWEEN + OrekitGroundStation.getBaseFrame().getName() + DEFAULT_AND + satellite.getName();
        final Reference reference1 = new Reference(visibilityCone1.getId() + DEFAULT_H_POSITION);
        final Reference reference2 = new Reference(satellite.getId() + DEFAULT_H_POSITION);
        this.referenceGroundStation = reference1;
        this.referenceSatellite = reference2;
        final Reference[] referenceList = Arrays.asList(referenceGroundStation, referenceSatellite).toArray(new Reference[0]);
        this.references = convertToIterable(referenceList);

        this.timeIntervals = new ArrayList<>();
        this.visuList = new ArrayList<>();
        this.showList = new ArrayList<>();

        this.buildSingleTimeIntervalsAndVisu(OrekitGroundStation, satellite);
        this.buildShowList();
        this.availability = getVisibilityInterval(showList);

        visibilityCone1.generateCZML();
    }

    public LineOfVisibility(final TopocentricFrame topocentricFrame, final Constellation constellation, final Header header) {

        this(topocentricFrame, constellation, header, DEFAULT_ANGLE_OF_APERTURE);
    }

    public LineOfVisibility(final TopocentricFrame topocentricFrame, final Constellation constellation, final Header header, final double angleOfAperture) {
        final List<Satellite> listOfSatellites = constellation.getAllSatellites();
        final List<LineOfVisibility> lineOfVisibilityList = new ArrayList<>();

        for (final Satellite currentSatellite : listOfSatellites) {
            final LineOfVisibility currentLineOfVisibility = new LineOfVisibility(topocentricFrame, currentSatellite, header, angleOfAperture);
            lineOfVisibilityList.add(currentLineOfVisibility);
        }
        this.allVisibilityLines = lineOfVisibilityList;
    }

    public LineOfVisibility(final List<TopocentricFrame> topocentricFrames, final Constellation constellation, final Header header) {
        this(topocentricFrames, constellation, header, DEFAULT_ANGLE_OF_APERTURE);
    }

    public LineOfVisibility(final List<TopocentricFrame> topocentricFrames, final Constellation constellation, final Header header, final double angleOfAperture) {
        final List<Satellite> listOfSatellites = constellation.getAllSatellites();
        this.buildMultipleLineOfVisilibility(topocentricFrames, listOfSatellites, header, angleOfAperture);
    }

    // Overrides
    @Override
    public void generateCZML() {

        if (this.allVisibilityLines.isEmpty()) {
            writeCZML();
        } else if (!multipleTimeIntervals.isEmpty()) {
            for (int i = 0; i < multipleTimeIntervals.size(); i++) {
                for (int j = 0; j < multipleTimeIntervals.get(0).size(); j++) {
                    this.angleOfAperture = multipleAngleOfAperture.get(i).get(j);
                    this.id = multipleId.get(i).get(j);
                    this.satellite = multipleSatellite.get(i).get(j);
                    this.name = multipleName.get(i).get(j);
                    this.references = multipleReferences.get(i).get(j);
                    this.timeIntervals = multipleTimeIntervals.get(i).get(j);
                    this.visuList = multipleVisuList.get(i).get(j);
                    this.showList = multipleShowList.get(i).get(j);
                    this.availability = multipleAvailability.get(i).get(j);
                    writeCZML();
                }
            }
            cleanObject();
        }
        else {
            for (int i = 0; i < allVisibilityLines.size(); i++) {
                final LineOfVisibility currentVisibilityLine = allVisibilityLines.get(i);
                this.angleOfAperture = currentVisibilityLine.angleOfAperture;
                this.id = currentVisibilityLine.id;
                this.satellite = currentVisibilityLine.satellite;
                this.references = currentVisibilityLine.references;
                this.timeIntervals = currentVisibilityLine.timeIntervals;
                this.visuList = currentVisibilityLine.visuList;
                this.showList = currentVisibilityLine.showList;
                this.availability = currentVisibilityLine.availability;
                writeCZML();
            }
        }
        cleanObject();
    }

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
        this.id = "";
        this.name = "";
        this.availability = new ArrayList<>();
        this.multipleAvailability = new ArrayList<>();
        this.referenceGroundStation = null;
        this.referenceSatellite = null;
        this.references = null;
        this.satellite = null;
        this.showList = new ArrayList<>();
        this.multipleShowList = new ArrayList<>();
        this.timeIntervals = new ArrayList<>();
        this.multipleTimeIntervals = new ArrayList<>();
        this.visuList = new ArrayList<>();
        this.multipleVisuList = new ArrayList<>();
        this.angleOfAperture = 0.0;
        this.allVisibilityLines = new ArrayList<>();
    }

    /** This function aims at clearing only the redundant parameters without destroying the multiple parameters. */
    public void clear() {
        this.id = "";
        this.name = "";
        this.availability = new ArrayList<>();
        this.referenceGroundStation = null;
        this.referenceSatellite = null;
        this.references = null;
        this.satellite = null;
        this.showList = new ArrayList<>();
        this.timeIntervals = new ArrayList<>();
        this.visuList = new ArrayList<>();
        this.angleOfAperture = 0.0;
    }

    // GETS
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Reference getReferenceGroundStation() {
        return referenceGroundStation;
    }

    public double getAngleOfAperture() {
        return angleOfAperture;
    }

    public Satellite getSatellite() {
        return satellite;
    }

    public Iterable<Reference> getReferences() {
        return references;
    }

    public List<TimeInterval> getAvailability() {
        return availability;
    }

    public List<LineOfVisibility> getAllVisibilityLines() {
        return allVisibilityLines;
    }

    // Intern methods
    private void buildSingleTimeIntervalsAndVisu(final GroundStation groundStation, final Satellite satellite_input) {

        final BoundedPropagator boundedPropagator = satellite_input.getBoundedPropagator();
        final TimeScale UTC = TimeScalesFactory.getUTC();
        final GregorianDate firstGregorianDate = new GregorianDate(1, 1, 1, 0, 0, 0.0);
        final JulianDate firstDate = new JulianDate(firstGregorianDate);
        final JulianDate lastDate = absoluteDateToJulianDate(satellite_input.getOrbits().get(satellite_input.getOrbits().size() - 1).getDate());
        final List<JulianDate> TimeStarts = new ArrayList<>();
        final List<JulianDate> TimeStops = new ArrayList<>();

        visuList.add(false);

        final ElevationDetector visuDetector = this.detectionVisu(groundStation, TimeStarts, TimeStops);
        boundedPropagator.addEventDetector(visuDetector);

        boundedPropagator.propagate(satellite_input.getOrbits().get(0).getDate(), satellite_input.getOrbits().get(satellite_input.getOrbits().size() - 1).getDate());

        if (TimeStarts.isEmpty()) {
            System.out.println(DEFAULT_NOT_SEEN + groundStation.getBaseFrame().getName() + DEFAULT_NOT_SEEN_2);
            timeIntervals.add(new TimeInterval(firstDate, lastDate));
            return;
        }

        if (!visuList.get(1)) {
            final TimeInterval firstTimeInterval = new TimeInterval(firstDate, TimeStops.get(0));
            final TimeInterval lastTimeInterval = new TimeInterval(TimeStops.get(TimeStarts.size() - 1), lastDate);

            TimeStarts.add(lastDate);

            timeIntervals.add(firstTimeInterval);

            for (int i = 0; i < visuList.size() - 3; i++) {
                if (!visuList.get(i)) {
                    if (i != TimeStarts.size() - 1) {
                        timeIntervals.add(new TimeInterval(TimeStops.get(i / 2), TimeStarts.get(i / 2)));
                    } else {
                        timeIntervals.add(new TimeInterval(TimeStarts.get(i / 2), lastDate));
                    }
                } else if (((i / 2) + 1) >= TimeStarts.size()) {
                    timeIntervals.add(new TimeInterval(TimeStops.get(i / 2), TimeStarts.get((i / 2) + 1)));
                } else {
                    timeIntervals.add(new TimeInterval(TimeStarts.get(i / 2), TimeStops.get(i / 2 + 1)));
                }
            }

            for (int i = 1; i < visuList.size(); i++) {
                final boolean tempVisu = visuList.get(i);
                if (tempVisu) {
                    visuList.set(i, false);
                }
                else {
                    visuList.set(i, true);
                }
            }
        }
        else {

            TimeStops.add(lastDate);

            final TimeInterval firstTimeInterval = new TimeInterval(firstDate, TimeStarts.get(0));

            final TimeInterval lastTimeInterval = new TimeInterval(TimeStops.get(TimeStops.size() - 1), lastDate);

            timeIntervals.add(firstTimeInterval);

            for (int i = 0; i < visuList.size() - 3; i++) {
                if (!visuList.get(i)) {
                    timeIntervals.add(new TimeInterval(TimeStarts.get(i / 2), TimeStops.get(i / 2)));
                }
                else {
                    timeIntervals.add(new TimeInterval(TimeStops.get(i / 2), TimeStarts.get(i / 2 + 1)));
                }
            }

            visuList.add(false);

            timeIntervals.add(lastTimeInterval);

        }

        boundedPropagator.clearEventsDetectors();
        satellite_input.setBoundedPropagator(boundedPropagator);
    }

    private void buildMultipleLineOfVisilibility(final List<TopocentricFrame> topocentricFrames, final List<Satellite> satelliteList, final Header header, final double inputAngleOfAperture) {

        for (int i = 0; i < topocentricFrames.size(); i++) {

            final List<List<TimeInterval>> tempMultipleIntervals = new ArrayList<>();
            final List<List<Boolean>> tempMultipleVisu = new ArrayList<>();
            final List<List<CZMLShow>> tempMultipleShow = new ArrayList<>();
            final List<List<TimeInterval>> tempMultipleAvailability = new ArrayList<>();
            final List<String> tempMultipleId = new ArrayList<>();
            final List<String> tempMultipleName = new ArrayList<>();
            final List<Double> tempAngleOfAperture = new ArrayList<>();
            final List<Iterable<Reference>> tempMultipleReferences = new ArrayList<>();
            final List<Satellite> tempMultipleSatellites = new ArrayList<>();

            final TopocentricFrame currentTopocentricFrame = topocentricFrames.get(i);
            for (int j = 0; j < satelliteList.size(); j++) {
                final Satellite currentSatellite = satelliteList.get(j);
                this.angleOfAperture = inputAngleOfAperture;
                final GroundStation currentOrekitGroundStation = new GroundStation(currentTopocentricFrame);
                final CZMLGroundStation currentGroundStation = new CZMLGroundStation(currentOrekitGroundStation, header);
                final VisibilityCone visibilityCone1 = new VisibilityCone(currentGroundStation, currentSatellite, header);
                visibilityCone1.noDisplay();
                this.id = currentOrekitGroundStation.getBaseFrame().getName() + "/" + currentSatellite.getId();
                this.satellite = currentSatellite;
                this.name = DEFAULT_LINE_BETWEEN + currentOrekitGroundStation.getBaseFrame().getName() + DEFAULT_AND + currentSatellite.getName();
                final Reference reference1 = new Reference(visibilityCone1.getId() + DEFAULT_H_POSITION);
                final Reference reference2 = new Reference(currentSatellite.getId() + DEFAULT_H_POSITION);
                this.referenceGroundStation = reference1;
                this.referenceSatellite = reference2;
                final Reference[] referenceList = Arrays.asList(referenceGroundStation, referenceSatellite).toArray(new Reference[0]);
                this.references = convertToIterable(referenceList);
                this.buildSingleTimeIntervalsAndVisu(currentOrekitGroundStation, currentSatellite);
                this.buildShowList();
                // Add to tempLists
                tempMultipleSatellites.add(satellite);
                tempMultipleIntervals.add(timeIntervals);
                tempMultipleVisu.add(visuList);
                tempMultipleShow.add(showList);
                tempAngleOfAperture.add(angleOfAperture);
                tempMultipleReferences.add(references);
                tempMultipleId.add(id);
                tempMultipleName.add(name);
                this.availability = this.getVisibilityInterval(showList);
                tempMultipleAvailability.add(availability);
                // Clear
                visibilityCone1.generateCZML();
                clear();
            }
            this.multipleName.add(tempMultipleName);
            this.multipleReferences.add(tempMultipleReferences);
            this.multipleId.add(tempMultipleId);
            this.multipleSatellite.add(tempMultipleSatellites);
            this.multipleAngleOfAperture.add(tempAngleOfAperture);
            this.multipleTimeIntervals.add(tempMultipleIntervals);
            this.multipleVisuList.add(tempMultipleVisu);
            this.multipleShowList.add(tempMultipleShow);
            this.multipleAvailability.add(tempMultipleAvailability);
        }
    }

    private ElevationDetector detectionVisu(final GroundStation groundStation, final List<JulianDate> TimeStarts, final List<JulianDate> TimeStops) {


        if (angleOfAperture != 0.0) {
            return new ElevationDetector(30, 0.001, groundStation.getBaseFrame())
                    .withConstantElevation(FastMath.toRadians(90.0 - angleOfAperture)).withHandler((spacecraftState, detector, increasing) -> {
                        if (increasing) {
                            TimeStarts.add(absoluteDateToJulianDate(spacecraftState.getDate()));
                            visuList.add(true);
                        } else {
                            TimeStops.add(absoluteDateToJulianDate(spacecraftState.getDate()));
                            visuList.add(false);
                        }
                        return Action.CONTINUE;
                    });
        } else {
            angleOfAperture = 80.0;
            return new ElevationDetector(30, 0.001, groundStation.getBaseFrame())
                    .withConstantElevation(FastMath.toRadians(90.0 - angleOfAperture)).withHandler((spacecraftState, detector, increasing) -> {
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
    }

    private void buildShowList() {
        for (int i = 0; i < timeIntervals.size(); i++) {
            final CZMLShow showTemp = new CZMLShow(visuList.get(i), timeIntervals.get(i));
            showList.add(showTemp);
        }
    }

    private static Iterable<Reference> convertToIterable(final Reference[] array) {
        return () -> Arrays.stream(array).iterator();
    }

    private List<TimeInterval> getVisibilityInterval(final List<CZMLShow> inputShowList) {
        final List<TimeInterval> toReturn = new ArrayList<TimeInterval>();
        JulianDate firstInterval = null;
        JulianDate lastInterval = null;
        if (inputShowList.size() == 1) {
            toReturn.add(inputShowList.get(0).getAvailability());
            return toReturn;
        } else {
            for (int i = 1; i < inputShowList.size(); i++) {
                final CZMLShow showTemp = inputShowList.get(i);
                final CZMLShow showBefore = inputShowList.get(i - 1);
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

    private void writePolyline(final PacketCesiumWriter packet) {
        final Polyline polylineInput = new Polyline();
        polylineInput.writePolylineOfVisibility(packet, OUTPUT, references, showList);
    }

    private void writeCZML() {
        OUTPUT.setPrettyFormatting(true);
        try (PacketCesiumWriter packet = STREAM.openPacket(OUTPUT)) {
            packet.writeId(this.id);
            packet.writeName(this.name);
            packet.writeAvailability(this.availability);
            this.writePolyline(packet);
        }
    }
}
