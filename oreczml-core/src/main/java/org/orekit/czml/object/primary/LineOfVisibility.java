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
package org.orekit.czml.object.primary;

import cesiumlanguagewriter.GregorianDate;
import cesiumlanguagewriter.JulianDate;
import cesiumlanguagewriter.PacketCesiumWriter;
import cesiumlanguagewriter.Reference;
import cesiumlanguagewriter.TimeInterval;
import org.hipparchus.ode.events.Action;
import org.hipparchus.util.FastMath;
import org.orekit.czml.archi.builder.LineOfVisibilityBuilder;
import org.orekit.czml.object.CzmlShow;
import org.orekit.czml.object.Polyline;
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
 * @since 1.0.0
 */

public class LineOfVisibility extends AbstractPrimaryObject implements CzmlPrimaryObject {

    /** The default angle of aperture of the station. */
    public static final double DEFAULT_ANGLE_OF_APERTURE = 80.0;

    /** The first default string for the name. */
    public static final String DEFAULT_LINE_BETWEEN = "Line between ";

    /** The default ID for the line of visibility. */
    public static final String DEFAULT_ID = "LINE_VISU/";

    /** The second default string for the name. */
    public static final String DEFAULT_AND = " and ";

    /** This allows creating a reference based on the position of an object. */
    public static final String DEFAULT_H_POSITION = "#position";

    /** This first string displays when the given satellite is not visible by the given station. */
    public static final String DEFAULT_NOT_SEEN = "The satellite is not visible by ";

    /** This second string displays when the given satellite is not visible by the given station. */
    public static final String DEFAULT_NOT_SEEN_2 = " for the given time interval";


    // Intrinsic parameters
    /** The references of the satellites and the station. */
    private Iterable<Reference> references;

    /** The satellite observed. */
    private Satellite satellite;

    /** A list of CzmlShow that contains all the information about the satellite's visualization by the station. */
    private List<CzmlShow> showList;

    /** A list of time intervals that represents when the satellite is visible or not. */
    private List<TimeInterval> timeIntervals;

    /** The list of boolean to display or not the line if the satellite is visible or not. */
    private List<Boolean> visuList;

    /** The angle of aperture of the station. */
    private double angleOfAperture;

    /** All the visibility cones of the station. */
    private List<VisibilityCone> allVisibilityCones = new ArrayList<>();


    // Constructors

    /**
     * The basic constructor of the line of visibility with default parameters.
     *
     * @param topocentricFrame : The topocentric frame where the ground station must be.
     * @param satellite        : The satellite that will be observed by the station.
     */
    public LineOfVisibility(final TopocentricFrame topocentricFrame,
                            final Satellite satellite) throws URISyntaxException, IOException {
        this(topocentricFrame, satellite, DEFAULT_ANGLE_OF_APERTURE);
    }

    /**
     * The constructor of the line of visibility with no default parameters.
     *
     * @param topocentricFrame : The topocentric frame where the ground station must be.
     * @param satellite        : The satellite that will be observed by the station.
     * @param angleOfAperture  : The angle of aperture of the station.
     */
    public LineOfVisibility(final TopocentricFrame topocentricFrame, final Satellite satellite,
                            final double angleOfAperture) throws URISyntaxException, IOException {
        this.angleOfAperture = angleOfAperture;

        this.setId(DEFAULT_ID + topocentricFrame.getName() + "/" + satellite.getId());
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
        this.visuList      = new ArrayList<>();
        this.showList      = new ArrayList<>();

        buildSingleTimeIntervalsAndVisu(topocentricFrame, satellite);
        buildShowList();
    }

    // Builder

    public static LineOfVisibilityBuilder builder(final TopocentricFrame topocentricFrameInput,
                                                  final Satellite satelliteInput) {
        return new LineOfVisibilityBuilder(topocentricFrameInput, satelliteInput);
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
        this.references         = null;
        this.satellite          = null;
        this.showList           = new ArrayList<>();
        this.timeIntervals      = new ArrayList<>();
        this.visuList           = new ArrayList<>();
        this.angleOfAperture    = 0.0;
        this.allVisibilityCones = new ArrayList<>();
    }


    // Getters

    public double getAngleOfAperture() {
        return angleOfAperture;
    }

    public Satellite getSatellite() {
        return satellite;
    }

    public List<Boolean> getVisuList() {
        return new ArrayList<>(visuList);
    }

    public List<CzmlShow> getShowList() {
        return new ArrayList<>(showList);
    }


    // Private functions

    public Iterable<Reference> getReferences() {
        return references;
    }

    public List<VisibilityCone> getAllVisibilityCones() {
        return new ArrayList<>(allVisibilityCones);
    }

    public List<TimeInterval> getTimeIntervals() {
        return new ArrayList<>(timeIntervals);
    }

    /**
     * This function builds an elevation detector for the propagator to detect when the station sees the satellite or not.
     *
     * @param topocentricFrame :The topocentric frame where the ground station must be.
     * @param visuMap          : The time span map of boolean that will contain all the information about when the station sees the satellite or not.
     * @return : An elevation detector that allows the station to detect the satellite during the propagation.
     */
    private ElevationDetector detectionVisu(final TopocentricFrame topocentricFrame,
                                            final TimeSpanMap<Boolean> visuMap) {
        return new ElevationDetector(30, 0.001, topocentricFrame).withConstantElevation(
                                                                         FastMath.toRadians(90.0 - angleOfAperture))
                                                                 .withHandler(
                                                                         (spacecraftState, detector, increasing) -> {
                                                                             if (increasing) {
                                                                                 visuMap.addValidAfter(true,
                                                                                         spacecraftState.getDate(),
                                                                                         true);
                                                                             } else {
                                                                                 visuMap.addValidAfter(false,
                                                                                         spacecraftState.getDate(),
                                                                                         true);
                                                                             }
                                                                             return Action.CONTINUE;
                                                                         });
    }

    /**
     * This function aims at building the time intervals and the list of visualization containing the boolean showing
     * if the station sees the satellite or not.
     *
     * @param topocentricFrame :The topocentric frame where the ground station must be.
     * @param satellite_input  : The satellite that will be observed by the station.
     */
    private void buildSingleTimeIntervalsAndVisu(final TopocentricFrame topocentricFrame,
                                                 final Satellite satellite_input) {


        final BoundedPropagator propagator         = (BoundedPropagator) satellite_input.getSatellitePropagator();
        final GregorianDate     firstGregorianDate = new GregorianDate(1, 1, 1, 0, 0, 0.0);
        final JulianDate        firstStartDate     = new JulianDate(firstGregorianDate);
        final JulianDate lastDate = absoluteDateToJulianDate(satellite_input.getOrbits()
                                                                            .get(satellite_input.getOrbits()
                                                                                                .size() - 1)
                                                                            .getDate(), Header.getTimeScale());
        final TimeScale timeScale = Header.getTimeScale();

        final SpacecraftState initialState = propagator.getInitialState();

        final TimeSpanMap<Boolean> visuMap = new TimeSpanMap<>(null);
        // Add the first boolean false that represent the visibility out of the scope of the simulation.
        visuMap.addValidBetween(false, initialState.getDate(), propagator.getMaxDate());
        final ElevationDetector visuDetector = detectionVisu(topocentricFrame, visuMap);
        final double            enVisu       = visuDetector.g(initialState);

        if (enVisu > 0) {
            visuList.add(true);
        } else {
            visuList.add(false);
        }

        propagator.addEventDetector(visuDetector);
        propagator.propagate(propagator.getMinDate(), propagator.getMaxDate());

        for (TimeSpanMap.Span<Boolean> span = visuMap.getFirstNonNullSpan(); span != null; span = span.next()) {
            if (span.getEnd()
                    .isAfter(julianDateToAbsoluteDate(Header.getMasterClock()
                                                            .getAvailability()
                                                            .getStop(), timeScale))) {
                if (visuList.get(visuList.size() - 1)) {
                    visuList.add(false);
                } else {
                    visuList.add(true);
                }
                final JulianDate startDate = absoluteDateToJulianDate(span.getStart(), Header.getTimeScale());
                final JulianDate stopDate = Header.getMasterClock()
                                                  .getAvailability()
                                                  .getStop();
                final TimeInterval currentTimeInterval = new TimeInterval(startDate, stopDate);
                timeIntervals.add(currentTimeInterval);
            } else {
                visuList.add(span.getData());
                final JulianDate startDate = absoluteDateToJulianDate(span.getStart(),
                        Header.getTimeScale());
                final JulianDate   stopDate            = absoluteDateToJulianDate(span.getEnd(), Header.getTimeScale());
                final TimeInterval currentTimeInterval = new TimeInterval(startDate, stopDate);
                timeIntervals.add(currentTimeInterval);
            }
        }

        if (timeIntervals.size() > 1) {

            final JulianDate firstStopDate = timeIntervals.get(0)
                                                          .getStart();
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
        }

        propagator.clearEventsDetectors();
        satellite_input.setPropagator(propagator);
    }

    /** This function builds a list of czml show to write into the czml file when the station sees the satellite or not. */
    private void buildShowList() {
        showList = new ArrayList<>();
        for (int i = 0; i < visuList.size(); i++) {
            final CzmlShow showTemp = new CzmlShow(visuList.get(i), timeIntervals.get(i));
            showList.add(showTemp);
        }
    }

    /**
     * This function writes the polyline that will be displayed when the satellite in seen by the station.
     *
     * @param packet : The packet that will write in the czml file.
     */
    private void writePolyline(final PacketCesiumWriter packet) {
        final Polyline polylineInput = new Polyline();
        polylineInput.writePolylineOfVisibility(packet, OUTPUT, references, showList);
    }

    /**
     * This function aims at converting an array of references into an iterable object.
     *
     * @param array : An array of references.
     * @return : An iterable object of references.
     */
    private static Iterable<Reference> convertToIterable(final Reference[] array) {
        return () -> Arrays.stream(array)
                           .iterator();
    }
}
