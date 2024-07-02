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

import cesiumlanguagewriter.GregorianDate;
import cesiumlanguagewriter.JulianDate;
import cesiumlanguagewriter.PacketCesiumWriter;
import cesiumlanguagewriter.Reference;
import cesiumlanguagewriter.TimeInterval;
import org.orekit.czml.CzmlEnum.TypeOfVisu;
import org.orekit.czml.CzmlObjects.CzmlShow;
import org.orekit.czml.CzmlObjects.Polyline;
import org.hipparchus.ode.events.Action;
import org.hipparchus.util.FastMath;
import org.orekit.frames.TopocentricFrame;
import org.orekit.propagation.BoundedPropagator;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.events.ElevationDetector;
import org.orekit.time.TimeScale;
import org.orekit.utils.TimeSpanMap;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Line of Visibility

 * <p>
 * This class will allows the user to build a line of visibility between a satellite and a ground station. The line
 * will be only visibly when the satellite is visible by the station in its own local sky.
 * </p>
 *
 * @since 1.0
 * @author Julien LEBLOND.
 */

public class LineOfVisibility extends AbstractPrimaryObject implements CzmlPrimaryObject {

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
    private List<String> singleSatMultipleId = new ArrayList<>();
    /** .*/
    private List<List<String>> multipleId = new ArrayList<>();
    /** .*/
    private List<String> singleSatMultipleName = new ArrayList<>();
    /** .*/
    private List<List<String>> multipleName = new ArrayList<>();
    /** .*/
    private List<TimeInterval> availabilities = new ArrayList<>();
    /** .*/
    private List<List<TimeInterval>> singleSatAvailabilities = new ArrayList<>();
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
    private List<Iterable<Reference>> singleSatReferences = new ArrayList<>();
    /** .*/
    private Satellite satellite = null;
    /** .*/
    private List<List<Satellite>> multipleSatellite = new ArrayList<>();
    /** .*/
    private List<CzmlShow> showList = new ArrayList<>();
    /** .*/
    private List<List<CzmlShow>> singleSatShowList = new ArrayList<>();
    /** .*/
    private List<List<List<CzmlShow>>> multipleShowList = new ArrayList<>();
    /** .*/
    private List<TimeInterval> timeIntervals = new ArrayList<>();
    /** .*/
    private List<List<TimeInterval>> singleSatTimeIntervals = new ArrayList<>();
    /** .*/
    private List<List<List<TimeInterval>>> multipleTimeIntervals = new ArrayList<>();
    /** .*/
    private List<Boolean> visuList = new ArrayList<>();
    /** .*/
    private List<List<Boolean>> singleSatVisuList = new ArrayList<>();
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
    /** .*/
    private TypeOfVisu typeOfVisu;

    // BUILDERS

    public LineOfVisibility(final TopocentricFrame topocentricFrame, final Satellite satellite) {
        this(topocentricFrame, satellite, DEFAULT_ANGLE_OF_APERTURE);
    }

    public LineOfVisibility(final TopocentricFrame topocentricFrame, final Satellite satellite, final double angleOfAperture) {
        this.angleOfAperture = angleOfAperture;

        this.setId(topocentricFrame.getName() + "/" + satellite.getId());
        this.setName(DEFAULT_LINE_BETWEEN + topocentricFrame.getName() + DEFAULT_AND + satellite.getName());

        final VisibilityCone visibilityCone1 = new VisibilityCone(topocentricFrame, satellite);
        visibilityCone1.noDisplay();
        allVisibilityCones.add(visibilityCone1);
        this.satellite = satellite;
        final Reference reference1 = new Reference(visibilityCone1.getId() + DEFAULT_H_POSITION);
        final Reference reference2 = new Reference(satellite.getId() + DEFAULT_H_POSITION);
        this.referenceGroundStation = reference1;
        this.referenceSatellite = reference2;
        final Reference[] referenceList = Arrays.asList(referenceGroundStation, referenceSatellite).toArray(new Reference[0]);
        this.references = convertToIterable(referenceList);

        this.timeIntervals = new ArrayList<>();
        this.visuList = new ArrayList<>();
        this.showList = new ArrayList<>();

        this.buildSingleTimeIntervalsAndVisu(topocentricFrame, satellite);
        this.buildShowList();
        this.availabilities = getVisibilityInterval(showList);
        this.typeOfVisu = TypeOfVisu.SINGLE_SAT_SINGLE_STATION;
    }

    public LineOfVisibility(final TopocentricFrame topocentricFrame, final Constellation constellation) {

        this(topocentricFrame, constellation, DEFAULT_ANGLE_OF_APERTURE);
    }

    public LineOfVisibility(final TopocentricFrame topocentricFrame, final Constellation constellation, final double angleOfAperture) {
        final List<Satellite> listOfSatellites = constellation.getAllSatellites();
        final List<LineOfVisibility> lineOfVisibilityList = new ArrayList<>();

        for (final Satellite currentSatellite : listOfSatellites) {
            final LineOfVisibility currentLineOfVisibility = new LineOfVisibility(topocentricFrame, currentSatellite, angleOfAperture);
            lineOfVisibilityList.add(currentLineOfVisibility);
        }
        this.allVisibilityLines = lineOfVisibilityList;
        this.typeOfVisu = TypeOfVisu.MULTIPLE_SAT_SINGLE_STATION;
    }

    public LineOfVisibility(final List<TopocentricFrame> topocentricFrames, final Satellite satellite) throws URISyntaxException, IOException {
        this(topocentricFrames, satellite, DEFAULT_ANGLE_OF_APERTURE);
    }

    public LineOfVisibility(final List<TopocentricFrame> topocentricFrames, final Satellite satellite, final double angleOfAperture) throws URISyntaxException, IOException {
        this.buildSingleSatelliteTimeIntervalsAndVisu(topocentricFrames, satellite, angleOfAperture);
        this.typeOfVisu = TypeOfVisu.SINGLE_SAT_MULTIPLE_STATION;
    }

    public LineOfVisibility(final List<TopocentricFrame> topocentricFrames, final Constellation constellation) {
        this(topocentricFrames, constellation, DEFAULT_ANGLE_OF_APERTURE);
    }

    public LineOfVisibility(final List<TopocentricFrame> topocentricFrames, final Constellation constellation, final double angleOfAperture) {
        final List<Satellite> listOfSatellites = constellation.getAllSatellites();
        this.buildMultipleLineOfVisilibility(topocentricFrames, listOfSatellites, angleOfAperture);
        this.typeOfVisu = TypeOfVisu.MULTIPLE_SAT_MULTIPLE_STATION;
    }

    // Overrides
    @Override
    public void writeCzmlBlock() {

        if (this.typeOfVisu == TypeOfVisu.SINGLE_SAT_SINGLE_STATION) {
            writeCZML();
        } else if (this.typeOfVisu == TypeOfVisu.MULTIPLE_SAT_MULTIPLE_STATION) {
            for (int i = 0; i < getMultipleTimeIntervals().size(); i++) {
                for (int j = 0; j < getMultipleTimeIntervals().get(0).size(); j++) {
                    this.setId(getMultipleId().get(i).get(j));
                    this.setName(getMultipleName().get(i).get(j));
                    this.angleOfAperture = getMultipleAngleOfAperture().get(i).get(j);
                    this.satellite = getMultipleSatellite().get(i).get(j);
                    this.references = getMultipleReferences().get(i).get(j);
                    this.timeIntervals = getMultipleTimeIntervals().get(i).get(j);
                    this.visuList = getMultipleVisuList().get(i).get(j);
                    this.showList = getMultipleShowList().get(i).get(j);
                    this.availabilities = getMultipleAvailability().get(i).get(j);
                    writeCZML();
                }
            }
        }
        else if (this.typeOfVisu == TypeOfVisu.MULTIPLE_SAT_SINGLE_STATION) {
            for (int i = 0; i < getAllVisibilityLines().size(); i++) {
                final LineOfVisibility currentVisibilityLine = getAllVisibilityLines().get(i);
                this.setId(currentVisibilityLine.getId());
                this.setName(currentVisibilityLine.getName());
                this.angleOfAperture = currentVisibilityLine.getAngleOfAperture();
                this.satellite = currentVisibilityLine.getSatellite();
                this.references = currentVisibilityLine.getReferences();
                this.timeIntervals = currentVisibilityLine.getTimeIntervals();
                this.visuList = currentVisibilityLine.getVisuList();
                this.showList = currentVisibilityLine.getShowList();
                this.availabilities = currentVisibilityLine.getAvailabilities();
                writeCZML();
            }
        } else if (this.typeOfVisu == TypeOfVisu.SINGLE_SAT_MULTIPLE_STATION) {
            for (int i = 0; i < singleSatMultipleId.size(); i++) {
                this.setId(singleSatMultipleId.get(i));
                this.setName(singleSatMultipleName.get(i));
                this.references = singleSatReferences.get(i);
                this.timeIntervals = singleSatTimeIntervals.get(i);
                this.visuList = singleSatVisuList.get(i);
                this.showList = singleSatShowList.get(i);
                this.availabilities = singleSatAvailabilities.get(i);
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
    public void cleanObject() {
        this.setId("");
        this.setName("");
        this.availabilities = new ArrayList<>();
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
        this.setId("");
        this.setName("");
        this.availabilities = new ArrayList<>();
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

    public Reference getReferenceGroundStation() {
        return referenceGroundStation;
    }

    public double getAngleOfAperture() {
        return angleOfAperture;
    }

    public Satellite getSatellite() {
        return satellite;
    }

    public List<Boolean> getVisuList() {
        return visuList;
    }

    public List<CzmlShow> getShowList() {
        return showList;
    }

    public Iterable<Reference> getReferences() {
        return references;
    }

    public List<TimeInterval> getAvailabilities() {
        return availabilities;
    }

    // Multiples

    public List<LineOfVisibility> getAllVisibilityLines() {
        return allVisibilityLines;
    }

    public List<VisibilityCone> getAllVisibilityCones() {
        return allVisibilityCones;
    }

    public List<List<String>> getMultipleName() {
        return multipleName;
    }

    public List<List<Double>> getMultipleAngleOfAperture() {
        return multipleAngleOfAperture;
    }

    public List<List<Satellite>> getMultipleSatellite() {
        return multipleSatellite;
    }

    public List<List<Iterable<Reference>>> getMultipleReferences() {
        return multipleReferences;
    }

    public List<List<List<Boolean>>> getMultipleVisuList() {
        return multipleVisuList;
    }

    public List<List<String>> getMultipleId() {
        return multipleId;
    }

    public List<List<List<CzmlShow>>> getMultipleShowList() {
        return multipleShowList;
    }

    public List<List<List<TimeInterval>>> getMultipleTimeIntervals() {
        return multipleTimeIntervals;
    }

    public List<List<List<TimeInterval>>> getMultipleAvailability() {
        return multipleAvailability;
    }

    public List<TimeInterval> getTimeIntervals() {
        return timeIntervals;
    }

    // Intern methods
    private void buildSingleTimeIntervalsAndVisu(final TopocentricFrame topocentricFrame, final Satellite satellite_input) {

        visuList = new ArrayList<>();
        timeIntervals = new ArrayList<>();

        final BoundedPropagator boundedPropagator = satellite_input.getBoundedPropagator();
        final GregorianDate firstGregorianDate = new GregorianDate(1, 1, 1, 0, 0, 0.0);
        final JulianDate firstStartDate = new JulianDate(firstGregorianDate);
        final JulianDate lastDate = absoluteDateToJulianDate(satellite_input.getOrbits().get(satellite_input.getOrbits().size() - 1).getDate());
        final TimeScale timeScale = Header.TIME_SCALE;

        final SpacecraftState initialState = boundedPropagator.propagate(boundedPropagator.getMinDate());

        final TimeSpanMap<Boolean> visuMap = new TimeSpanMap<>(null);
        visuMap.addValidBetween(false, initialState.getDate(), boundedPropagator.getMaxDate());
        final ElevationDetector visuDetector = this.detectionVisu(topocentricFrame, visuMap);
        final double enVisu = visuDetector.g(initialState);

        if (enVisu > 0) {
            visuList.add(true);
        }
        else {
            visuList.add(false);
        }

        boundedPropagator.addEventDetector(visuDetector);
        boundedPropagator.propagate(boundedPropagator.getMinDate(), boundedPropagator.getMaxDate());

        for (TimeSpanMap.Span<Boolean> span = visuMap.getFirstNonNullSpan(); span != null; span = span.next()) {
            if (span.getEnd().isAfter(julianDateToAbsoluteDate(Header.MASTER_CLOCK.getAvailability().getStop(), timeScale))) {
                visuList.add(false);
                final JulianDate startDate = absoluteDateToJulianDate(span.getStart());
                final JulianDate stopDate = Header.MASTER_CLOCK.getAvailability().getStop();
                final TimeInterval currentTimeInterval = new TimeInterval(startDate, stopDate);
                timeIntervals.add(currentTimeInterval);
            }
            else {
                visuList.add(span.getData());
                final JulianDate startDate = absoluteDateToJulianDate(span.getStart());
                final JulianDate stopDate = absoluteDateToJulianDate(span.getEnd());
                final TimeInterval currentTimeInterval = new TimeInterval(startDate, stopDate);
                timeIntervals.add(currentTimeInterval);
            }
        }

        if (timeIntervals.size() > 1) {

            final JulianDate firstStopDate = timeIntervals.get(0).getStart();
            final TimeInterval firstTimeInterval = new TimeInterval(firstStartDate, firstStopDate);

            if (enVisu > 0) {
                timeIntervals.add(0, firstTimeInterval);
            } else {
                timeIntervals.add(0, firstTimeInterval);
            }
        }
        if (timeIntervals.size() == 1) {
            System.out.println(DEFAULT_NOT_SEEN + topocentricFrame.getName() + DEFAULT_NOT_SEEN_2);
            timeIntervals.add(new TimeInterval(firstStartDate, lastDate));
            return;
        }

        boundedPropagator.clearEventsDetectors();
        satellite_input.setBoundedPropagator(boundedPropagator);
    }

    private void buildSingleSatelliteTimeIntervalsAndVisu(final List<TopocentricFrame> topocentricFrames, final Satellite satellite_input, final double inputAngleOfAperture) {

        final List<List<Boolean>> tempMultipleVisu = new ArrayList<>();
        final List<List<CzmlShow>> tempMultipleShow = new ArrayList<>();
        final List<List<TimeInterval>> tempMultipleAvailability = new ArrayList<>();
        final List<String> tempMultipleId = new ArrayList<>();
        final List<String> tempMultipleName = new ArrayList<>();
        final List<Iterable<Reference>> tempReferences = new ArrayList<>();
        final List<List<TimeInterval>> tempMultipleIntervals = new ArrayList<>();

        this.angleOfAperture = inputAngleOfAperture;
        this.satellite = satellite_input;
        final Reference satReference = new Reference(satellite.getId() + DEFAULT_H_POSITION);

        for (final TopocentricFrame currentTopocentricFrame : topocentricFrames) {
            final String currentId = currentTopocentricFrame.getName() + "/" + satellite_input.getId();
            final String currentName = DEFAULT_LINE_BETWEEN + currentTopocentricFrame.getName() + DEFAULT_AND + satellite_input.getName();
            this.setId(currentId);
            this.setName(currentName);
            tempMultipleId.add(currentId);
            tempMultipleName.add(currentName);
            final VisibilityCone currentVisibilityCone = new VisibilityCone(currentTopocentricFrame, satellite_input);
            currentVisibilityCone.noDisplay();
            allVisibilityCones.add(currentVisibilityCone);
            final Reference currentTopocentricFrameReference = new Reference(currentVisibilityCone.getId() + DEFAULT_H_POSITION);
            final Reference[] referenceList = Arrays.asList(satReference, currentTopocentricFrameReference).toArray(new Reference[0]);
            this.references = convertToIterable(referenceList);
            tempReferences.add(references);

            this.buildSingleTimeIntervalsAndVisu(currentTopocentricFrame, satellite_input);
            this.buildShowList();
            this.availabilities = this.getVisibilityInterval(showList);
            tempMultipleAvailability.add(availabilities);
            tempMultipleVisu.add(visuList);
            tempMultipleShow.add(showList);
            tempMultipleIntervals.add(timeIntervals);
        }
        this.singleSatTimeIntervals = tempMultipleIntervals;
        this.singleSatShowList = tempMultipleShow;
        this.singleSatVisuList = tempMultipleVisu;
        this.singleSatMultipleId = tempMultipleId;
        this.singleSatMultipleName = tempMultipleName;
        this.singleSatReferences = tempReferences;
        this.singleSatAvailabilities = tempMultipleAvailability;
    }

    private void buildMultipleLineOfVisilibility(final List<TopocentricFrame> topocentricFrames, final List<Satellite> satelliteList, final double inputAngleOfAperture) {

        for (int i = 0; i < topocentricFrames.size(); i++) {

            final List<List<TimeInterval>> tempMultipleIntervals = new ArrayList<>();
            final List<List<Boolean>> tempMultipleVisu = new ArrayList<>();
            final List<List<CzmlShow>> tempMultipleShow = new ArrayList<>();
            final List<List<TimeInterval>> tempMultipleAvailability = new ArrayList<>();
            final List<String> tempMultipleId = new ArrayList<>();
            final List<String> tempMultipleName = new ArrayList<>();
            final List<Double> tempAngleOfAperture = new ArrayList<>();
            final List<Iterable<Reference>> tempMultipleReferences = new ArrayList<>();
            final List<Satellite> tempMultipleSatellites = new ArrayList<>();

            final TopocentricFrame currentTopocentricFrame = topocentricFrames.get(i);

            for (final Satellite currentSatellite : satelliteList) {
                this.angleOfAperture = inputAngleOfAperture;
                final String currentId = currentTopocentricFrame.getName() + "/" + currentSatellite.getId();
                final String currentName = DEFAULT_LINE_BETWEEN + currentTopocentricFrame.getName() + DEFAULT_AND + currentSatellite.getName();
                this.setId(currentId);
                this.setName(currentName);
                final VisibilityCone visibilityCone1 = new VisibilityCone(currentTopocentricFrame, currentSatellite);
                visibilityCone1.noDisplay();
                allVisibilityCones.add(visibilityCone1);

                this.satellite = currentSatellite;
                final Reference reference1 = new Reference(visibilityCone1.getId() + DEFAULT_H_POSITION);
                final Reference reference2 = new Reference(currentSatellite.getId() + DEFAULT_H_POSITION);
                this.referenceGroundStation = reference1;
                this.referenceSatellite = reference2;
                final Reference[] referenceList = Arrays.asList(referenceGroundStation, referenceSatellite).toArray(new Reference[0]);
                this.references = convertToIterable(referenceList);
                this.buildSingleTimeIntervalsAndVisu(currentTopocentricFrame, currentSatellite);
                this.buildShowList();
                // Add to tempLists
                tempMultipleSatellites.add(satellite);
                tempMultipleIntervals.add(timeIntervals);
                tempMultipleVisu.add(visuList);
                tempMultipleShow.add(showList);
                tempAngleOfAperture.add(angleOfAperture);
                tempMultipleReferences.add(references);
                tempMultipleId.add(currentId);
                tempMultipleName.add(currentName);
                this.availabilities = this.getVisibilityInterval(showList);
                tempMultipleAvailability.add(availabilities);
                // Clear
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

    private ElevationDetector detectionVisu(final TopocentricFrame topocentricFrame, final TimeSpanMap<Boolean> visuMap) {

        if (angleOfAperture != 0.0) {
            return new ElevationDetector(30, 0.001, topocentricFrame)
                    .withConstantElevation(FastMath.toRadians(90.0 - angleOfAperture)).withHandler((spacecraftState, detector, increasing) -> {
                        if (increasing) {
                            visuMap.addValidAfter(true, spacecraftState.getDate(), true);
                        } else {
                            visuMap.addValidAfter(false, spacecraftState.getDate(), true);
                        }
                        return Action.CONTINUE;
                    });
        } else {
            angleOfAperture = 80.0;
            return new ElevationDetector(30, 0.001, topocentricFrame)
                    .withConstantElevation(FastMath.toRadians(90.0 - angleOfAperture)).withHandler((spacecraftState, detector, increasing) -> {
                        if (increasing) {
                            visuList.add(true);
                            visuMap.addValidAfter(true, spacecraftState.getDate(), true);
                        } else {
                            visuList.add(false);
                            visuMap.addValidAfter(false, spacecraftState.getDate(), true);
                        }
                        return Action.CONTINUE;
                    });
        }
    }

    private void buildShowList() {
        showList = new ArrayList<>();
        for (int i = 0; i < visuList.size(); i++) {
            final CzmlShow showTemp = new CzmlShow(visuList.get(i), timeIntervals.get(i));
            showList.add(showTemp);
        }
    }

    private static Iterable<Reference> convertToIterable(final Reference[] array) {
        return () -> Arrays.stream(array).iterator();
    }

    private List<TimeInterval> getVisibilityInterval(final List<CzmlShow> inputShowList) {
        final List<TimeInterval> toReturn = new ArrayList<>();
        JulianDate firstInterval = null;
        JulianDate lastInterval = null;
        if (inputShowList.size() == 1) {
            toReturn.add(inputShowList.get(0).getAvailability());
            return toReturn;
        } else {
            for (int i = 1; i < inputShowList.size(); i++) {
                final CzmlShow showTemp = inputShowList.get(i);
                final CzmlShow showBefore = inputShowList.get(i - 1);
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
        for (int i = 0; i < this.getAllVisibilityCones().size(); i++) {
            final VisibilityCone currentVisibilityCone = this.getAllVisibilityCones().get(i);
            currentVisibilityCone.writeCzmlBlock();
        }
        try (PacketCesiumWriter packet = STREAM.openPacket(OUTPUT)) {
            packet.writeId(this.getId());
            packet.writeName(this.getName());
            packet.writeAvailability(this.availabilities);
            this.writePolyline(packet);
        }
    }
}
