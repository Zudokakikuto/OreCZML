/* Copyright 2002-2025 CS GROUP
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
package org.orekit.czml.object.primary.visu;

import cesiumlanguagewriter.CesiumOutputStream;
import cesiumlanguagewriter.CesiumStreamWriter;
import cesiumlanguagewriter.GregorianDate;
import cesiumlanguagewriter.JulianDate;
import cesiumlanguagewriter.PacketCesiumWriter;
import cesiumlanguagewriter.Reference;
import cesiumlanguagewriter.TimeInterval;
import org.hipparchus.ode.events.Action;
import org.hipparchus.util.FastMath;
import org.orekit.czml.errors.OreCzmlException;
import org.orekit.czml.errors.OreCzmlMessages;
import org.orekit.czml.object.CzmlShow;
import org.orekit.czml.object.Polyline;
import org.orekit.czml.object.primary.AbstractPrimaryObject;
import org.orekit.czml.object.primary.entities.Constellation;
import org.orekit.czml.object.primary.entities.Spacecraft;
import org.orekit.czml.object.secondary.Clock;
import org.orekit.czml.object.utils.DateUtils;
import org.orekit.frames.TopocentricFrame;
import org.orekit.propagation.BoundedPropagator;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.events.ElevationDetector;
import org.orekit.time.AbsoluteDate;
import org.orekit.utils.TimeSpanMap;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Line of Visibility
 * <p>
 * This class will allows the user to build a line of visibility between a
 * satellite and a ground station. The line will be only visibly when the
 * satellite is visible by the station in its own local sky.
 *
 * @author Julien LEBLOND.
 * @since 1.0.0
 */
public class LineOfVisibility
    extends
    AbstractPrimaryObject<LineOfVisibility> {

    /**
     * The default angle of aperture of the station.
     */
    public static final double DEFAULT_ANGLE_OF_APERTURE = 80.0;

    /**
     * The first default string for the name.
     */
    public static final String DEFAULT_LINE_BETWEEN = "Line between ";

    /**
     * The default ID for the line of visibility.
     */
    public static final String DEFAULT_ID = "LINE_VISU/";

    /**
     * The second default string for the name.
     */
    public static final String DEFAULT_AND = " and ";

    /**
     * This allows creating a reference based on the position of an object.
     */
    public static final String DEFAULT_H_POSITION = "#position";

    // Intrinsic parameters

    /**
     * The references of the satellites and the station.
     */
    private Iterable<Reference> references;

    /**
     * The satellite observed.
     */
    private Spacecraft spacecraft;

    /** The satellites when a constellation is used. */
    private List<Spacecraft> satellites = new ArrayList<>();

    /**
     * A list of CzmlShow that contains all the information about the
     * satellite's visualization by the station.
     */
    private List<CzmlShow> showList;

    /**
     * A list of time intervals that represents when the satellite is visible or
     * not.
     */
    private List<TimeInterval> timeIntervals;

    /**
     * The list of boolean to display or not the line if the satellite is
     * visible or not.
     */
    private List<Boolean> visuList;

    /**
     * The angle of aperture of the station.
     */
    private double angleOfAperture;

    /**
     * All the visibility cones of the station.
     */
    private List<VisibilityCone> visibilityCones = new ArrayList<>();

    /** Lines when constellations. */
    private List<LineOfVisibility> lines = new ArrayList<>();

    /** Visibility triangle. */
    private VisibilityTriangle triangle;

    /** Visibility triangles. */
    private List<VisibilityTriangle> triangles = new ArrayList<>();

    /** The topocentric frame used. */
    private TopocentricFrame topocentricFrame;

    /** The constellation is one is used. */
    private Constellation constellation;

    /** The clock of the line of visibility. */
    private Clock clock;

    // Constructors

    /**
     * The basic constructor of the line of visibility with default parameters.
     *
     * @param topocentricFrame : The topocentric frame where the ground station
     *        must be.
     * @param satellite : The satellite that will be observed by the station.
     * @param clock : The clock considered.
     */
    LineOfVisibility(final TopocentricFrame topocentricFrame,
                     final Spacecraft satellite, final Clock clock) {
        this(topocentricFrame, satellite, DEFAULT_ANGLE_OF_APERTURE,
             DEFAULT_ID +
                                                                     topocentricFrame
                                                                         .getName() +
                                                                     "/" +
                                                                     DEFAULT_AND +
                                                                     satellite
                                                                         .getName(),
             clock);
    }

    /**
     * The constructor of the line of visibility with no default parameters.
     *
     * @param topocentricFrameInput : The topocentric frame where the ground
     *        station must be.
     * @param satelliteInput : The satellite that will be observed by the
     *        station.
     * @param angleOfApertureInput : The angle of aperture of the station.
     * @param customID : The custom ID of the line of visibility object.
     * @param clock : The clock considered.
     */
    LineOfVisibility(final TopocentricFrame topocentricFrameInput,
                     final Spacecraft satelliteInput,
                     final double angleOfApertureInput, final String customID,
                     final Clock clock) {

        this.angleOfAperture = angleOfApertureInput;
        this.setAvailability(clock.getAvailability());
        this.setId(customID);
        this.clock = clock;
        this.setName(DEFAULT_LINE_BETWEEN +
                     topocentricFrameInput.getName() + DEFAULT_AND +
                     satelliteInput.getName());

        final VisibilityCone visibilityCone1 =
            new VisibilityCone(topocentricFrameInput, satelliteInput, clock);
        visibilityCone1.noDisplay();
        visibilityCones.add(visibilityCone1);
        this.spacecraft = satelliteInput;
        final Reference reference1 =
            new Reference(visibilityCone1.getId() + DEFAULT_H_POSITION);
        final Reference reference2 =
            new Reference(satelliteInput.getId() + DEFAULT_H_POSITION);
        final Reference[] referenceList =
            Arrays.asList(reference1, reference2).toArray(new Reference[0]);
        this.references = convertToIterable(referenceList);
        this.topocentricFrame = topocentricFrameInput;

        this.timeIntervals = new ArrayList<>();
        this.visuList = new ArrayList<>();
        this.showList = new ArrayList<>();

        buildSingleTimeIntervalsAndVisu(topocentricFrameInput, satelliteInput,
                                        clock);
        buildShowList(topocentricFrameInput);
    }

    /**
     * Constructor for the line of visibility with a constellation and several
     * other parameters.
     *
     * @param topocentricFrame : The topocentric frame where the ground station
     *        must be.
     * @param constellation : The constellation that will be observed by the
     *        station.
     * @param angleOfAperture : The angle of aperture of the station.
     * @param customID : The custom ID of the line of visibility object.
     * @param clock : The clock considered. *
     */
    LineOfVisibility(final TopocentricFrame topocentricFrame,
                     final Constellation constellation,
                     final double angleOfAperture, final String customID,
                     final Clock clock) {
        this.setId(customID);
        this.setAvailability(clock.getAvailability());
        this.clock = clock;
        this.satellites = constellation.getSatellites();
        this.topocentricFrame = topocentricFrame;
        this.constellation = constellation;
        this.angleOfAperture = angleOfAperture;
        final List<Reference> referenceToConvert = new ArrayList<>();
        for (int i = 0; i < constellation.getTotalOfSatellite(); i++) {
            final Spacecraft currentSatellite =
                constellation.getSatellites().get(i);
            final VisibilityCone visibilityCone1 =
                new VisibilityCone(topocentricFrame, currentSatellite, clock);
            referenceToConvert.add(new Reference(visibilityCone1.getId() +
                                                 DEFAULT_H_POSITION));
            referenceToConvert.add(new Reference(currentSatellite.getId() +
                                                 DEFAULT_H_POSITION));
            final LineOfVisibility currentLine =
                new LineOfVisibility(topocentricFrame, currentSatellite,
                                     angleOfAperture,
                                     topocentricFrame.getName() +
                                                      currentSatellite.getId() +
                                                      customID,
                                     clock);
            lines.add(currentLine);
        }
        final Reference[] referenceList =
            referenceToConvert.toArray(new Reference[0]);
        this.references = convertToIterable(referenceList);
    }

    // Builder

    /**
     * Builder line of visibility builder with a spacecraft.
     *
     * @param topocentricFrameInput the topocentric frame input
     * @param satelliteInput the satellite input
     * @param clock the availability
     * @return the line of visibility builder
     */
    public static LineOfVisibilityBuilder
        builder(final TopocentricFrame topocentricFrameInput,
                final Spacecraft satelliteInput, final Clock clock) {
        return new LineOfVisibilityBuilder(topocentricFrameInput,
                                           satelliteInput, clock);
    }

    /**
     * Builder line of visibility builder with a constellation.
     *
     * @param topocentricFrameInput the topocentric frame input
     * @param constellationInput the constellation input
     * @param clock the availability
     * @return the line of visibility builder
     */
    public static LineOfVisibilityBuilder
        builder(final TopocentricFrame topocentricFrameInput,
                final Constellation constellationInput, final Clock clock)
            throws URISyntaxException,
                IOException {
        return new LineOfVisibilityBuilder(topocentricFrameInput,
                                           constellationInput, clock);
    }

    // Overrides

    @Override
    public void writeCzmlBlock(final CesiumStreamWriter stream,
                               final CesiumOutputStream output)
        throws URISyntaxException,
            IOException {
        if (this.triangle != null) {
            this.triangle.writeCzmlBlock(stream, output);
        } else if (!(triangles.isEmpty())) {
            for (final VisibilityTriangle currentTriangle : triangles) {
                currentTriangle.writeCzmlBlock(stream, output);
            }
        }

        if (lines.isEmpty()) {
            writeSingleLine(output, stream, this);
        } else {
            for (LineOfVisibility line : lines) {
                writeSingleLine(output, stream, line);
            }
        }
    }

    @Override
    public LineOfVisibility cloneObject() {
        final LineOfVisibility toReturn;
        try {
            if (this.spacecraft != null) {
                if (this.triangle != null) {
                    toReturn =
                        LineOfVisibility
                            .builder(this.topocentricFrame, this.spacecraft,
                                     this.clock)
                            .withAngleOfAperture(this.angleOfAperture)
                            .withCustomID(getId()).withVisibilityTriangle()
                            .build();
                } else {
                    toReturn =
                        LineOfVisibility
                            .builder(this.topocentricFrame, this.spacecraft,
                                     this.clock)
                            .withAngleOfAperture(this.angleOfAperture)
                            .withCustomID(getId()).build();
                }
                toReturn.setName(getName());
            } else if (!this.satellites.isEmpty()) {
                if (!this.triangles.isEmpty()) {
                    toReturn =
                        LineOfVisibility
                            .builder(this.topocentricFrame, this.constellation,
                                     this.clock)
                            .withCustomID(getId())
                            .withAngleOfAperture(this.angleOfAperture)
                            .withVisibilityTriangle().build();
                } else {
                    toReturn =
                        LineOfVisibility
                            .builder(this.topocentricFrame, this.constellation,
                                     this.clock)
                            .withCustomID(getId())
                            .withAngleOfAperture(this.angleOfAperture).build();
                }
                toReturn.setName(getName());
            } else {
                throw new OreCzmlException(OreCzmlMessages.NOT_VALID_PRIMARY_OBJECT_FOR_CLONE);
            }
            return toReturn;
        } catch (URISyntaxException | IOException e) {
            throw new OreCzmlException(OreCzmlMessages.NOT_VALID_PRIMARY_OBJECT_FOR_CLONE);
        }
    }

    /**
     * Clean object.
     */
    public void cleanObject() {
        this.setId("");
        this.setName("");
        this.references = null;
        this.spacecraft = null;
        this.showList = new ArrayList<>();
        this.timeIntervals = new ArrayList<>();
        this.visuList = new ArrayList<>();
        this.angleOfAperture = 0.0;
        this.visibilityCones = new ArrayList<>();
        this.lines = new ArrayList<>();
        this.triangles = new ArrayList<>();
    }

    public void displayTriangle() {
        if (lines.isEmpty()) {
            this.triangle = new VisibilityTriangle(this);
        } else {
            throw new OreCzmlException(OreCzmlMessages.NOT_A_SINGLE_TRIANGLE_LINE);
        }
    }

    public void displaySingleTriangle(final int i) {
        if (lines.isEmpty()) {
            throw new OreCzmlException(OreCzmlMessages.NOT_A_MULTIPLE_TRIANGLE_LINE);
        } else {
            this.triangles.add(new VisibilityTriangle(lines.get(i)));
        }
    }

    // Getters

    /**
     * Gets satellite.
     *
     * @return the satellite
     */
    public Spacecraft getSpacecraft() {
        return spacecraft;
    }

    /**
     * Gets show list.
     *
     * @return the show list
     */
    public List<CzmlShow> getShowList() {
        if (showList != null) {
            return Collections.unmodifiableList(showList);
        } else {
            throw new OreCzmlException(OreCzmlMessages.NOT_A_SINGLE_SAT_OR_STATION);
        }
    }

    // Private functions

    /**
     * Gets references.
     *
     * @return the references
     */
    public Iterable<Reference> getReferences() {
        return references;
    }

    /**
     * Get the list of satellites when a constellation is used.
     *
     * @return : The list of the satellites
     */
    public List<Spacecraft> getSatellites() {
        return Collections.unmodifiableList(satellites);
    }

    /**
     * Gets visibility cones.
     *
     * @return the visibility cones
     */
    public List<VisibilityCone> getVisibilityCones() {
        return Collections.unmodifiableList(visibilityCones);
    }

    /**
     * Gets time intervals.
     *
     * @return the time intervals
     */
    public List<TimeInterval> getTimeIntervals() {
        return Collections.unmodifiableList(timeIntervals);
    }

    /**
     * This function builds an elevation detector for the propagator to detect
     * when the station sees the satellite or not.
     *
     * @param topocentricFrameInput :The topocentric frame where the ground
     *        station must be.
     * @param visuMap : The time span map of boolean that will contain all the
     *        information about when the station sees the satellite or not.
     * @return : An elevation detector that allows the station to detect the
     *         satellite during the propagation.
     */
    private ElevationDetector
        detectionVisu(final TopocentricFrame topocentricFrameInput,
                      final TimeSpanMap<Boolean> visuMap) {
        return new ElevationDetector(30, 0.001, topocentricFrameInput)
            .withConstantElevation(FastMath.toRadians(90.0 - angleOfAperture))
            .withHandler((spacecraftState, detector, increasing) -> {
                if (increasing) {
                    visuMap.addValidAfter(true, spacecraftState.getDate(),
                                          true);
                } else {
                    visuMap.addValidAfter(false, spacecraftState.getDate(),
                                          true);
                }
                return Action.CONTINUE;
            });
    }

    /**
     * This function aims at building the time intervals and the list of
     * visualization containing the boolean showing if the station sees the
     * satellite or not.
     *
     * @param topocentricFrameInput :The topocentric frame where the ground
     *        station must be.
     * @param satellite_input : The satellite that will be observed by the
     *        station.
     * @param clockInput : The clock considered.
     */
    private void
        buildSingleTimeIntervalsAndVisu(final TopocentricFrame topocentricFrameInput,
                                        final Spacecraft satellite_input,
                                        final Clock clockInput) {

        final BoundedPropagator propagator =
            (BoundedPropagator) satellite_input.getSpacecraftPropagator();
        final GregorianDate firstGregorianDate =
            new GregorianDate(1, 1, 1, 0, 0, 0.0);
        final JulianDate firstStartDate = new JulianDate(firstGregorianDate);
        final JulianDate lastDate =
            DateUtils.toJulianDate(satellite_input.getOrbits()
                .get(satellite_input.getOrbits().size() - 1).getDate());

        final SpacecraftState initialState = propagator.getInitialState();

        final TimeSpanMap<Boolean> visuMap = new TimeSpanMap<>(null);
        // Add the first boolean false that represent the visibility out of the
        // scope of the simulation.
        visuMap.addValidBetween(false, initialState.getDate(),
                                propagator.getMaxDate());
        final ElevationDetector visuDetector =
            detectionVisu(topocentricFrameInput, visuMap);
        final double enVisu = visuDetector.g(initialState);

        if (enVisu > 0) {
            visuList.add(true);
        } else {
            visuList.add(false);
        }

        propagator.addEventDetector(visuDetector);
        propagator.propagate(propagator.getMinDate(), propagator.getMaxDate());

        for (TimeSpanMap.Span<Boolean> span = visuMap.getFirstNonNullSpan();
             span != null; span = span.next()) {
            if (span.getEnd().isAfter(DateUtils
                .toAbsoluteDate(clockInput.getAvailability().getStop()))) {
                if (visuList.get(visuList.size() - 1)) {
                    visuList.add(false);
                } else {
                    visuList.add(true);
                }
                final JulianDate startDate =
                    DateUtils.toJulianDate(span.getStart());
                final JulianDate stopDate =
                    clockInput.getAvailability().getStop();
                final TimeInterval currentTimeInterval =
                    new TimeInterval(startDate, stopDate);
                timeIntervals.add(currentTimeInterval);
            } else {
                visuList.add(span.getData());
                final JulianDate startDate =
                    DateUtils.toJulianDate(span.getStart());
                final JulianDate stopDate =
                    DateUtils.toJulianDate(span.getEnd());
                final TimeInterval currentTimeInterval =
                    new TimeInterval(startDate, stopDate);
                timeIntervals.add(currentTimeInterval);
            }
        }

        if (timeIntervals.size() > 1) {

            final JulianDate firstStopDate = timeIntervals.get(0).getStart();
            final TimeInterval firstTimeInterval =
                new TimeInterval(firstStartDate, firstStopDate);

            timeIntervals.add(0, firstTimeInterval);
        }
        if (timeIntervals.size() == 1) {
            // Not seen
            timeIntervals.add(new TimeInterval(firstStartDate, lastDate));
        }

        propagator.clearEventsDetectors();
        satellite_input.setPropagator(propagator);
    }

    /**
     * This function builds a list of czml show to write into the czml file when
     * the station sees the satellite or not.
     *
     * @param topocentricFrameInput : The topocentric frame related to the Czml
     *        show.
     */
    private void buildShowList(final TopocentricFrame topocentricFrameInput) {
        showList = new ArrayList<>();
        for (int i = 0; i < visuList.size(); i++) {
            final TimeInterval currentTimeInterval = timeIntervals.get(i);
            final double clockMultiplier = this.clock.getMultiplier();
            final AbsoluteDate startDate =
                DateUtils.toAbsoluteDate(currentTimeInterval.getStart());
            final AbsoluteDate finalDate =
                DateUtils.toAbsoluteDate(currentTimeInterval.getStop());
            final Clock currentClock =
                new Clock(startDate, finalDate, clockMultiplier);
            final CzmlShow showTemp =
                new CzmlShow(visuList.get(i), currentClock,
                             topocentricFrameInput);
            showList.add(showTemp);
        }
    }

    /**
     * This function writes the polyline that will be displayed when the
     * satellite is seen by the station.
     *
     * @param packet : The packet that will write in the czml file.
     * @param output : The output stream of cesium that will contain the strings
     *        to write into the CzmLFile.
     */
    private void writePolyline(final PacketCesiumWriter packet,
                               final CesiumOutputStream output) {
        final Polyline polylineInput =
            Polyline.nonVectorBuilder(this.clock).build();
        polylineInput.writePolylineOfVisibility(packet, output, references,
                                                showList);
    }

    /**
     * This function writes in the czml file a single line of visibility.
     *
     * @param output : The output stream of cesium that will contain the strings
     *        to write into the CzmlFile.
     * @param stream : The stream that will write into the file.
     * @param line : The line written
     */
    private void writeSingleLine(final CesiumOutputStream output,
                                 final CesiumStreamWriter stream,
                                 final LineOfVisibility line) {
        output.setPrettyFormatting(true);
        for (int i = 0; i < line.getVisibilityCones().size(); i++) {
            final VisibilityCone currentVisibilityCone =
                line.getVisibilityCones().get(i);
            currentVisibilityCone.writeCzmlBlock(stream, output);
        }
        try (PacketCesiumWriter packet = stream.openPacket(output)) {
            packet.writeId(line.getId());
            packet.writeName(line.getName());
            packet.writeAvailability(line.getTimeIntervals());
            line.writePolyline(packet, output);
        }
    }

    /**
     * This function aims at converting an array of references into an iterable
     * object.
     *
     * @param array : An array of references.
     * @return : An iterable object of references.
     */
    private static Iterable<Reference>
        convertToIterable(final Reference[] array) {
        return () -> Arrays.stream(array).iterator();
    }
}
