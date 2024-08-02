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
import org.hipparchus.ode.events.Action;
import org.hipparchus.util.FastMath;
import org.orekit.czml.ArchiObjects.LineOfVisibilityBuilder;
import org.orekit.czml.CzmlObjects.CzmlShow;
import org.orekit.czml.CzmlObjects.Polyline;
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

/**
 * Line of Visibility
 *
 * <p>
 * This class will allows the user to build a line of visibility between a satellite and a ground station. The line
 * will be only visibly when the satellite is visible by the station in its own local sky.
 * </p>
 *
 * @author Julien LEBLOND.
 * @since 1.0
 */

public class LineOfVisibility extends AbstractPrimaryObject implements CzmlPrimaryObject {

    /**
     * .
     */
    public static final double DEFAULT_ANGLE_OF_APERTURE = 80.0;
    /**
     * .
     */
    public static final String DEFAULT_LINE_BETWEEN = "Line between ";
    /**
     * .
     */
    public static final String DEFAULT_AND = " and ";
    /**
     * .
     */
    public static final String DEFAULT_H_POSITION = "#position";
    /**
     * .
     */
    public static final String DEFAULT_NOT_SEEN = "The satellite is not visible by ";
    /**
     * .
     */
    public static final String DEFAULT_NOT_SEEN_2 = " for the given time interval";


    // Intrinsic parameters
    /**
     * .
     */
    private Iterable<Reference> references;
    /**
     * .
     */
    private Satellite satellite;
    /**
     * .
     */
    private List<CzmlShow> showList;
    /**
     * .
     */
    private List<TimeInterval> timeIntervals;
    /**
     * .
     */
    private List<Boolean> visuList;
    /**
     * .
     */
    private double angleOfAperture;
    /**
     * .
     */
    private List<VisibilityCone> allVisibilityCones = new ArrayList<>();

    // Constructors

    public LineOfVisibility(final TopocentricFrame topocentricFrame, final Satellite satellite) throws URISyntaxException, IOException {
        this(topocentricFrame, satellite, DEFAULT_ANGLE_OF_APERTURE);
    }

    public LineOfVisibility(final TopocentricFrame topocentricFrame, final Satellite satellite, final double angleOfAperture) throws URISyntaxException, IOException {
        this.angleOfAperture = angleOfAperture;

        this.setId(topocentricFrame.getName() + "/" + satellite.getId());
        this.setName(DEFAULT_LINE_BETWEEN + topocentricFrame.getName() + DEFAULT_AND + satellite.getName());

        final VisibilityCone visibilityCone1 = new VisibilityCone(topocentricFrame, satellite);
        visibilityCone1.noDisplay();
        allVisibilityCones.add(visibilityCone1);
        this.satellite = satellite;
        final Reference reference1 = new Reference(visibilityCone1.getId() + DEFAULT_H_POSITION);
        final Reference reference2 = new Reference(satellite.getId() + DEFAULT_H_POSITION);
        final Reference[] referenceList = Arrays.asList(reference1, reference2)
                                                .toArray(new Reference[0]);
        this.references = convertToIterable(referenceList);

        this.timeIntervals = new ArrayList<>();
        this.visuList = new ArrayList<>();
        this.showList = new ArrayList<>();

        buildSingleTimeIntervalsAndVisu(topocentricFrame, satellite);
        buildShowList();
    }

    // Builder

    public static LineOfVisibilityBuilder builder(final TopocentricFrame topocentricFrameInput, final Satellite satelliteInput) {
        return new LineOfVisibilityBuilder(topocentricFrameInput, satelliteInput);
    }

    private static Iterable<Reference> convertToIterable(final Reference[] array) {
        return () -> Arrays.stream(array)
                           .iterator();
    }

    // Overrides
    @Override
    public void writeCzmlBlock() {
        OUTPUT.setPrettyFormatting(true);
        for (int i = 0; i < this.getAllVisibilityCones()
                                .size(); i++) {
            final VisibilityCone currentVisibilityCone = this.getAllVisibilityCones()
                                                             .get(i);
            currentVisibilityCone.writeCzmlBlock();
        }
        try (PacketCesiumWriter packet = STREAM.openPacket(OUTPUT)) {
            packet.writeId(this.getId());
            packet.writeName(this.getName());
            packet.writeAvailability(this.getTimeIntervals());
            this.writePolyline(packet);
        }        //cleanObject();
    }

    // GETS

    @Override
    public StringWriter getStringWriter() {
        return STRING_WRITER;
    }

    // Multiples

    @Override
    public void cleanObject() {
        this.setId("");
        this.setName("");
        this.references = null;
        this.satellite = null;
        this.showList = new ArrayList<>();
        this.timeIntervals = new ArrayList<>();
        this.visuList = new ArrayList<>();
        this.angleOfAperture = 0.0;
        this.allVisibilityCones = new ArrayList<>();
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

    // Intern methods

    public List<VisibilityCone> getAllVisibilityCones() {
        return allVisibilityCones;
    }

    public List<TimeInterval> getTimeIntervals() {
        return timeIntervals;
    }

    private void buildSingleTimeIntervalsAndVisu(final TopocentricFrame topocentricFrame, final Satellite satellite_input) {

        final List<Boolean> visuListTemp = new ArrayList<>();
        final List<TimeInterval> timeIntervalsTemp = new ArrayList<>();

        final BoundedPropagator boundedPropagator = satellite_input.getBoundedPropagator();
        final GregorianDate firstGregorianDate = new GregorianDate(1, 1, 1, 0, 0, 0.0);
        final JulianDate firstStartDate = new JulianDate(firstGregorianDate);
        final JulianDate lastDate = absoluteDateToJulianDate(satellite_input.getOrbits()
                                                                            .get(satellite_input.getOrbits()
                                                                                                .size() - 1)
                                                                            .getDate());
        final TimeScale timeScale = Header.TIME_SCALE;

        final SpacecraftState initialState = boundedPropagator.getInitialState();

        final TimeSpanMap<Boolean> visuMap = new TimeSpanMap<>(null);
        // Add the first boolean false that represent the visibility out of the scope of the simulation.
        visuMap.addValidBetween(false, initialState.getDate(), boundedPropagator.getMaxDate());
        final ElevationDetector visuDetector = this.detectionVisu(topocentricFrame, visuMap);
        final double enVisu = visuDetector.g(initialState);

        if (enVisu > 0) {
            visuListTemp.add(true);
        } else {
            visuListTemp.add(false);
        }

        boundedPropagator.addEventDetector(visuDetector);
        boundedPropagator.propagate(boundedPropagator.getMinDate(), boundedPropagator.getMaxDate());

        for (TimeSpanMap.Span<Boolean> span = visuMap.getFirstNonNullSpan(); span != null; span = span.next()) {
            if (span.getEnd()
                    .isAfter(julianDateToAbsoluteDate(Header.MASTER_CLOCK.getAvailability()
                                                                         .getStop(), timeScale))) {
                if (visuListTemp.get(visuListTemp.size() - 1)) {
                    visuListTemp.add(false);
                } else {
                    visuListTemp.add(true);
                }
                final JulianDate startDate = absoluteDateToJulianDate(span.getStart());
                final JulianDate stopDate = Header.MASTER_CLOCK.getAvailability()
                                                               .getStop();
                final TimeInterval currentTimeInterval = new TimeInterval(startDate, stopDate);
                timeIntervalsTemp.add(currentTimeInterval);
            } else {
                visuListTemp.add(span.getData());
                final JulianDate startDate = absoluteDateToJulianDate(span.getStart());
                final JulianDate stopDate = absoluteDateToJulianDate(span.getEnd());
                final TimeInterval currentTimeInterval = new TimeInterval(startDate, stopDate);
                timeIntervalsTemp.add(currentTimeInterval);
            }
        }

        if (timeIntervalsTemp.size() > 1) {

            final JulianDate firstStopDate = timeIntervalsTemp.get(0)
                                                              .getStart();
            final TimeInterval firstTimeInterval = new TimeInterval(firstStartDate, firstStopDate);

            if (enVisu > 0) {
                timeIntervalsTemp.add(0, firstTimeInterval);
            } else {
                timeIntervalsTemp.add(0, firstTimeInterval);
            }
        }
        if (timeIntervalsTemp.size() == 1) {
            System.out.println(DEFAULT_NOT_SEEN + topocentricFrame.getName() + DEFAULT_NOT_SEEN_2);
            timeIntervalsTemp.add(new TimeInterval(firstStartDate, lastDate));
        }

        boundedPropagator.clearEventsDetectors();
        satellite_input.setBoundedPropagator(boundedPropagator);
    }

    private ElevationDetector detectionVisu(final TopocentricFrame topocentricFrame, final TimeSpanMap<Boolean> visuMap) {
        return new ElevationDetector(30, 0.001, topocentricFrame)
                .withConstantElevation(FastMath.toRadians(90.0 - angleOfAperture))
                .withHandler((spacecraftState, detector, increasing) -> {
                    if (increasing) {
                        visuMap.addValidAfter(true, spacecraftState.getDate(), true);
                    } else {
                        visuMap.addValidAfter(false, spacecraftState.getDate(), true);
                    }
                    return Action.CONTINUE;
                });
    }

    private void buildShowList() {
        showList = new ArrayList<>();
        for (int i = 0; i < visuList.size(); i++) {
            final CzmlShow showTemp = new CzmlShow(visuList.get(i), timeIntervals.get(i));
            showList.add(showTemp);
        }
    }

    private void writePolyline(final PacketCesiumWriter packet) {
        final Polyline polylineInput = new Polyline();
        polylineInput.writePolylineOfVisibility(packet, OUTPUT, references, showList);
    }

}
