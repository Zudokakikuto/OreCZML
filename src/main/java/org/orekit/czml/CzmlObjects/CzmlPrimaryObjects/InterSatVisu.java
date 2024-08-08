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

import cesiumlanguagewriter.JulianDate;
import cesiumlanguagewriter.PacketCesiumWriter;
import cesiumlanguagewriter.Reference;
import cesiumlanguagewriter.TimeInterval;
import org.hipparchus.ode.events.Action;
import org.hipparchus.util.FastMath;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.czml.CzmlObjects.CzmlShow;
import org.orekit.czml.CzmlObjects.Polyline;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.orbits.Orbit;
import org.orekit.propagation.BoundedPropagator;
import org.orekit.propagation.EphemerisGenerator;
import org.orekit.propagation.Propagator;
import org.orekit.propagation.PropagatorsParallelizer;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.events.EventDetector;
import org.orekit.propagation.events.InterSatDirectViewDetector;
import org.orekit.propagation.events.handlers.EventHandler;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScale;
import org.orekit.utils.Constants;
import org.orekit.utils.IERSConventions;
import org.orekit.utils.TimeSpanMap;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Inter-sat Visu class
 *
 * <p> The inter sat visu class aims at displaying a line when two satellite see each other. On a constellation, it can
 * be applied as each satellite seeing each other. </p>
 *
 * @author Julien LEBLOND
 * @since 1.0.0
 */

public class InterSatVisu extends AbstractPrimaryObject implements CzmlPrimaryObject {

    /** The default ID for the inter-sat visu object. */
    public static final String                    DEFAULT_ID                 = "INTER_SAT_VISU/";
    /** The default name for the inter-sat visu object. */
    public static final String                    DEFAULT_NAME               = "Visualisation inter-satellite of : ";
    /** This allows to create a reference for a position for an object. */
    public static final String                    DEFAULT_H_POSITION         = "#position";
    /** The default ID for the inter-sat visu object when built with a constellation. */
    public static final String                    DEFAULT_CONSTELLATION_NAME = "Visualisation inter-constellation of : ";
    /** The default timescale. */
    public static final TimeScale                 DEFAULT_TIME_SCALE         = Header.getTimeScale();
    /** The first satellite for the visu. */
    private             Satellite                 satellite1;
    /** The second satellite for the visu. */
    private             Satellite                 satellite2;
    /** The bounded propagator (ephemeris) of the first satellite. */
    private             BoundedPropagator         boundedPropagatorFirstSat;
    /** The bounded propagator (ephemeris) of the second satellite. */
    private             BoundedPropagator         boundedPropagatorSecondSat;
    /** The propagator of the first satellite. */
    private             Propagator                propagatorFirstSat;
    /** The propagator of the first satellite. */
    private             Propagator                propagatorSecondSat;
    /** The body which the satellites are orbiting around (future implementation around several bodies). */
    private             OneAxisEllipsoid          body;
    /** The initial state of the first satellite. */
    private             SpacecraftState           initialState;
    /** The list of the dates when satellites see each other. */
    private             List<AbsoluteDate>        datesWhenVisu              = new ArrayList<>();
    /** The list of dates when satellites don't see each other. */
    private             List<AbsoluteDate>        datesWhenNotVisu           = new ArrayList<>();
    /** The start date of the propagation. */
    private             AbsoluteDate              startDate;
    /** The stop date of the propagation. */
    private             AbsoluteDate              stopDate;
    /** The time intervals of visualisation. */
    private             List<TimeInterval>        timeIntervalsOfVisu        = new ArrayList<>();
    /** The line to draw. */
    private             Polyline                  polyline;
    /** The references of each satellite. */
    private             Iterable<Reference>       references;
    /** A list of boolean that show when to display or not the lines. */
    private             List<Boolean>             booleanShowList            = new ArrayList<>();
    /** A list of CzmlShow that contains all the information about when and if the line should be displayed. */
    private             List<CzmlShow>            showList                   = new ArrayList<>();
    // Constellation parameters
    /** All the satellites of the constellation. */
    private             List<Satellite>           allConstellationSatellites = new ArrayList<>();
    /** All the ids of the satellites of the constellation. */
    private             List<String>              allIdsSatellites           = new ArrayList<>();
    /** All the propagators of the constellation. */
    private             List<BoundedPropagator>   allPropagators             = new ArrayList<>();
    /** All the polylines of the constellation. */
    private             List<Polyline>            polylines                  = new ArrayList<>();
    /** ALl the time intervals of visualisation the constellation, ordered by pairs of satellite. */
    private             List<List<TimeInterval>>  allTimeIntervalsOfVisu     = new ArrayList<>();
    /** ALl the booleans of the constelation, ordered by pairs of satellite. */
    private             List<List<Boolean>>       allBooleanShowList         = new ArrayList<>();
    /** All the booleans to know if whether to display the line, ordered by pairs of satellite. */
    private             List<List<CzmlShow>>      allShowList                = new ArrayList<>();
    /** All the CzmlShow objects, ordered by pairs of satellite. */
    private             List<Iterable<Reference>> allReferences              = new ArrayList<>();
    /** All the references of each pair of satellites, ordered by pairs of satellite. */
    private             List<Orbit>               allOrbits                  = new ArrayList<>();
    /** All the pairs of satellites. */
    private             List<List<Satellite>>     pairsOfSatellites          = new ArrayList<>();

    public InterSatVisu(final Satellite satellite1Input, final Satellite satellite2Input, final boolean toPropagate) {
        this.satellite1 = satellite1Input;
        this.satellite2 = satellite2Input;
        this.setId(DEFAULT_ID + satellite1Input.getId() + "/" + satellite2Input.getId());
        this.setName(DEFAULT_NAME + satellite2Input.getName() + "/" + satellite2Input.getName());
        final TimeInterval minimumInterval = this.findMinimumAvailability(satellite1Input, satellite2Input);
        this.setAvailability(minimumInterval);
        this.boundedPropagatorFirstSat  = (BoundedPropagator) satellite1Input.getSatellitePropagator();
        this.boundedPropagatorSecondSat = (BoundedPropagator) satellite2Input.getSatellitePropagator();
        this.propagatorFirstSat         = satellite1Input.getSatellitePropagator();
        this.propagatorSecondSat        = satellite2Input.getSatellitePropagator();
        final Frame ITRF = FramesFactory.getITRF(IERSConventions.IERS_2010, true);
        this.body         = new OneAxisEllipsoid(Constants.WGS84_EARTH_EQUATORIAL_RADIUS,
                Constants.WGS84_EARTH_FLATTENING, ITRF);
        this.initialState = satellite1Input.getAllSpaceCraftStates()
                                           .get(0);

        final Reference referenceFirstSatellite  = new Reference(satellite1Input.getId() + DEFAULT_H_POSITION);
        final Reference referenceSecondSatellite = new Reference(satellite2Input.getId() + DEFAULT_H_POSITION);
        final Reference[] referenceList = Arrays.asList(referenceFirstSatellite, referenceSecondSatellite)
                                                .toArray(new Reference[0]);
        this.references = convertToIterable(referenceList);

        if (toPropagate) {
            this.propagationInterSat();
        }

        this.timeIntervalsOfVisu = this.buildShowIntervals(datesWhenVisu, datesWhenNotVisu);
        this.polyline            = Polyline.nonVectorBuilder()
                                           .withFirstReference(referenceFirstSatellite)
                                           .withSecondReference(referenceSecondSatellite)
                                           .build();
        this.showList            = this.buildShowList(timeIntervalsOfVisu, booleanShowList);
    }

    public InterSatVisu(final List<BoundedPropagator> allPropagators,
                        final AbsoluteDate finalDate) throws URISyntaxException, IOException {
        this(new Constellation(allPropagators, finalDate));
    }

    public InterSatVisu(final Constellation constellationPropagators) throws URISyntaxException, IOException {

        this.allOrbits = constellationPropagators.getAllInitialOrbits();
        this.setId(DEFAULT_ID + constellationPropagators.getId());
        this.setName(DEFAULT_NAME + constellationPropagators.getTotalOfSatellite() + " satellites");
        this.setAvailability(Header.getMasterClock()
                                   .getAvailability());
        this.allConstellationSatellites = constellationPropagators.getAllSatellites();
        this.allIdsSatellites           = constellationPropagators.getAllIds();
        this.startDate                  = julianDateToAbsoluteDate(Header.getMasterClock()
                                                                         .getAvailability()
                                                                         .getStart(), Header.getTimeScale());
        this.stopDate                   = julianDateToAbsoluteDate(Header.getMasterClock()
                                                                         .getAvailability()
                                                                         .getStop(), Header.getTimeScale());

        final Frame ITRF = FramesFactory.getITRF(IERSConventions.IERS_2010, true);
        this.body = new OneAxisEllipsoid(Constants.WGS84_EARTH_EQUATORIAL_RADIUS, Constants.WGS84_EARTH_FLATTENING,
                ITRF);

        this.allPropagators = constellationPropagators.getAllPropagators();
        this.propagationInterConstellation();

        for (int i = 0; i < allConstellationSatellites.size(); i++) {
            final Satellite firstSatellite          = allConstellationSatellites.get(i);
            final String    currentIdFirstSatellite = allIdsSatellites.get(i);
            final Reference firstReferenceSatellite = new Reference(currentIdFirstSatellite + DEFAULT_H_POSITION);

            final List<Satellite> currentPairOfSatellites = new ArrayList<>();


            for (int j = i + 1; j < allConstellationSatellites.size(); j++) {
                final Satellite secondSatellite          = allConstellationSatellites.get(j);
                final String    currentIdSecondSatellite = allIdsSatellites.get(j);
                final Reference secondReferenceSatellite = new Reference(currentIdSecondSatellite + DEFAULT_H_POSITION);
                final Reference[] referenceList = Arrays.asList(firstReferenceSatellite, secondReferenceSatellite)
                                                        .toArray(new Reference[0]);
                allReferences.add(convertToIterable(referenceList));
                polylines.add(Polyline.nonVectorBuilder()
                                      .withFirstReference(firstReferenceSatellite)
                                      .withSecondReference(secondReferenceSatellite)
                                      .build());
                currentPairOfSatellites.add(firstSatellite);
                currentPairOfSatellites.add(secondSatellite);
            }
            pairsOfSatellites.add(currentPairOfSatellites);
        }
        pairsOfSatellites = this.reorganiseSatelliteList(pairsOfSatellites);

        for (int i = 0; i < pairsOfSatellites.size(); i++) {
            final List<TimeInterval> currentTimeIntervals = allTimeIntervalsOfVisu.get(i);
            final List<Boolean>      currentBooleans      = allBooleanShowList.get(i);
            allShowList.add(this.buildShowList(currentTimeIntervals, currentBooleans));
        }
    }

    private static Iterable<Reference> convertToIterable(final Reference[] array) {
        return () -> Arrays.stream(array)
                           .iterator();
    }

    @Override
    public void writeCzmlBlock() throws URISyntaxException, IOException {
        if (allConstellationSatellites.isEmpty()) {
            if (!showList.isEmpty()) {
                OUTPUT.setPrettyFormatting(true);
                try (PacketCesiumWriter packet = STREAM.openPacket(OUTPUT)) {
                    packet.writeId(getId());
                    packet.writeName(getName());
                    packet.writeAvailability(getAvailability());

                    this.polyline.writePolylineOfVisibility(packet, OUTPUT, this.references, this.showList);
                }
            }
        } else {
            OUTPUT.setPrettyFormatting(true);
            for (int i = 0; i < pairsOfSatellites.size(); i++) {
                writePairOfSatellite(i);
            }
        }
    }

    @Override
    public StringWriter getStringWriter() {
        return STRING_WRITER;
    }


    // GETTERS

    @Override
    public void cleanObject() {
        satellite1                 = null;
        satellite2                 = null;
        boundedPropagatorFirstSat  = null;
        boundedPropagatorSecondSat = null;
        allPropagators             = new ArrayList<>();
        allConstellationSatellites = new ArrayList<>();
    }

    public Satellite getSatellite1() {
        return satellite1;
    }

    public Satellite getSatellite2() {
        return satellite2;
    }

    public BoundedPropagator getBoundedPropagatorFirstSat() {
        return boundedPropagatorFirstSat;
    }

    public BoundedPropagator getBoundedPropagatorSecondSat() {
        return boundedPropagatorSecondSat;
    }

    public Propagator getPropagatorFirstSat() {
        return propagatorFirstSat;
    }

    public Propagator getPropagatorSecondSat() {
        return propagatorSecondSat;
    }

    public Iterable<Reference> getReferences() {
        return references;
    }

    public List<TimeInterval> getTimeIntervalsOfVisu() {
        return timeIntervalsOfVisu;
    }

    public Polyline getPolyline() {
        return polyline;
    }

    public SpacecraftState getInitialState() {
        return initialState;
    }

    public OneAxisEllipsoid getBody() {
        return body;
    }

    public List<AbsoluteDate> getDatesWhenNotVisu() {
        return datesWhenNotVisu;
    }

    public List<AbsoluteDate> getDatesWhenVisu() {
        return datesWhenVisu;
    }

    public List<CzmlShow> getShowList() {
        return showList;
    }

    public List<Boolean> getBooleanShowList() {
        return booleanShowList;
    }

    public AbsoluteDate getStartDate() {
        return startDate;
    }

    public AbsoluteDate getStopDate() {
        return stopDate;
    }

    public List<Satellite> getAllConstellationSatellites() {
        return allConstellationSatellites;
    }

    public List<BoundedPropagator> getAllPropagators() {
        return allPropagators;
    }

    public List<String> getAllIdsSatellites() {
        return allIdsSatellites;
    }

    public List<Polyline> getPolylines() {
        return polylines;
    }

    public List<List<TimeInterval>> getAllTimeIntervalsOfVisu() {
        return allTimeIntervalsOfVisu;
    }

    public List<List<Boolean>> getAllBooleanShowList() {
        return allBooleanShowList;
    }

    public List<List<CzmlShow>> getAllShowList() {
        return allShowList;
    }

    public List<Iterable<Reference>> getAllReferences() {
        return allReferences;
    }

    public List<Orbit> getAllOrbits() {
        return allOrbits;
    }


    // Private functions

    public List<List<Satellite>> getPairsOfSatellites() {
        return pairsOfSatellites;
    }

    private TimeInterval findMinimumAvailability(final Satellite satellite1Input, final Satellite satellite2Input) {
        final double durationAvailabilityFirstSat = satellite1Input.getAvailability()
                                                                   .getStart()
                                                                   .secondsDifference(satellite1Input.getAvailability()
                                                                                                     .getStop());

        final double durationAvailabilitySecondSat = satellite2Input.getAvailability()
                                                                    .getStart()
                                                                    .secondsDifference(satellite2Input.getAvailability()
                                                                                                      .getStop());

        final TimeInterval minimumInterval;
        if (durationAvailabilityFirstSat < durationAvailabilitySecondSat) {

            this.startDate  = julianDateToAbsoluteDate(satellite1Input.getAvailability()
                                                                      .getStart(), Header.getTimeScale());
            minimumInterval = satellite1Input.getAvailability();

        } else {

            this.startDate  = julianDateToAbsoluteDate(satellite2Input.getAvailability()
                                                                      .getStart(), Header.getTimeScale());
            minimumInterval = satellite2Input.getAvailability();
        }
        return minimumInterval;
    }

    private void propagationInterSat() {
        final InterSatDirectViewDetector detector = new InterSatDirectViewDetector(this.getBody(),
                this.getBoundedPropagatorSecondSat())
                .withHandler((spacecraftState, currentDetector, increasing) -> {
                    final double detected = currentDetector.g(spacecraftState);
                    if (detected >= 0) {
                        this.datesWhenNotVisu.add(spacecraftState.getDate());
                        this.booleanShowList.add(true);
                    } else if (detected < 0) {
                        this.datesWhenVisu.add(spacecraftState.getDate());
                        this.booleanShowList.add(false);
                    }
                    return Action.CONTINUE;
                });

        final Propagator propagatorFirstSatTemp = getPropagatorFirstSat();
        final TimeInterval availabilityOfTheSatellite = this.getSatellite1()
                                                            .getAvailability();

        final AbsoluteDate startDateTemp = julianDateToAbsoluteDate(availabilityOfTheSatellite.getStart(),
                Header.getTimeScale());
        final AbsoluteDate stopDateTemp = julianDateToAbsoluteDate(availabilityOfTheSatellite.getStop(),
                Header.getTimeScale());
        propagatorFirstSatTemp.addEventDetector(detector);
        propagatorFirstSatTemp.propagate(startDateTemp, stopDateTemp);

    }

    private void propagationInterConstellation() {

        final List<EphemerisGenerator> generators = new ArrayList<>();

        for (int i = 0; i < allOrbits.size(); i++) {
            final BoundedPropagator currentPropagator = allPropagators.get(i);
            generators.add(currentPropagator.getEphemerisGenerator());
        }

        final List<Propagator> propagators = new ArrayList<>(allPropagators);

        final PropagatorsParallelizer parallelizer = new PropagatorsParallelizer(propagators, interpolators -> {
        });
        parallelizer.propagate(startDate, stopDate);

        // Get the ephemeris
        final List<Propagator> propagatorsTemp = new ArrayList<>();
        generators.forEach(gen -> propagatorsTemp.add(gen.getGeneratedEphemeris()));

        // Add inter-sat view detectors
        final SortedMap<String, InterSatViewHandler> handlers = new TreeMap<>();

        for (int i = 0; i < propagatorsTemp.size() - 1; i++) {

            final Propagator propagator = propagatorsTemp.get(i);

            for (int j = i + 1; j < propagatorsTemp.size(); j++) {
                ephemerisDetectorAddition(i, j, handlers, propagator, propagators);
            }
        }

        for (Propagator prop : propagatorsTemp) {
            prop.propagate(startDate, stopDate);
        }

        postPropagationConstellationRetrieve(handlers);
    }

    private void postPropagationConstellationRetrieve(final SortedMap<String, InterSatViewHandler> handlers) {
        // Retrieve
        allBooleanShowList     = new ArrayList<>();
        allTimeIntervalsOfVisu = new ArrayList<>();
        for (String key : handlers.keySet()) {
            final List<Boolean>      tempBooleansList  = new ArrayList<>();
            final List<TimeInterval> tempTimeIntervals = new ArrayList<>();
            for (TimeSpanMap.Span<Boolean> span = handlers.get(
                    key).viewMap.getFirstSpan(); span != null; span = span.next()) {
                availabilitiesAndShowFilling(span, tempBooleansList, tempTimeIntervals);
            }
            if (!tempTimeIntervals.isEmpty()) {
                allTimeIntervalsOfVisu.add(tempTimeIntervals);
            }
            if (!tempBooleansList.isEmpty()) {
                allBooleanShowList.add(tempBooleansList);
            }
        }
    }

    private void availabilitiesAndShowFilling(final TimeSpanMap.Span<Boolean> span,
                                              final List<Boolean> tempBooleansList,
                                              final List<TimeInterval> tempTimeIntervals) {
        if (span.getData() != null) {
            AbsoluteDate       stopTime  = span.getEnd();
            final AbsoluteDate startTime = span.getStart();
            if (span.getEnd()
                    .isAfter(julianDateToAbsoluteDate(Header.getMasterClock()
                                                            .getAvailability()
                                                            .getStop(), Header.getTimeScale()))) {
                stopTime = julianDateToAbsoluteDate(Header.getMasterClock()
                                                          .getAvailability()
                                                          .getStop(), Header.getTimeScale());
            }
            tempBooleansList.add(span.getData());
            tempTimeIntervals.add(new TimeInterval(absoluteDateToJulianDate(startTime, DEFAULT_TIME_SCALE),
                    absoluteDateToJulianDate(stopTime, DEFAULT_TIME_SCALE)));
        }
    }

    private void ephemerisDetectorAddition(final int firstIterationNumber, final int secondIterationNumber,
                                           final SortedMap<String, InterSatViewHandler> handlers,
                                           final Propagator propagator, final List<Propagator> propagators) {
        final Propagator second = propagators.get(secondIterationNumber);

        // Build detector
        final InterSatViewHandler handler = new InterSatViewHandler();
        final InterSatDirectViewDetector detector = new InterSatDirectViewDetector(body, second).withHandler(handler)
                                                                                                .withMaxCheck(60.);

        // Add to main
        propagator.addEventDetector(detector);

        // Store handler
        handlers.put(firstIterationNumber + " → " + secondIterationNumber, handler);
    }

    private List<TimeInterval> buildShowIntervals(final List<AbsoluteDate> datesWhenVisuInput,
                                                  final List<AbsoluteDate> datesWhenNotVisuInput) {

        if (!(datesWhenVisuInput.isEmpty() && datesWhenNotVisuInput.isEmpty())) {

            AbsoluteDate minimalDate;

            boolean seenAtTheBeginning = false;

            minimalDate = datesWhenVisuInput.get(0);

            if (minimalDate.isAfter(datesWhenNotVisuInput.get(0))) {
                minimalDate        = datesWhenNotVisuInput.get(0);
                seenAtTheBeginning = true;
            }

            final int minimumLength = FastMath.min(datesWhenNotVisuInput.size(), datesWhenVisuInput.size());

            final List<TimeInterval> toReturn = new ArrayList<>();
            if (!seenAtTheBeginning) {
                toReturn.add(new TimeInterval(Header.getMasterClock()
                                                    .getAvailability()
                                                    .getStart(),
                        absoluteDateToJulianDate(datesWhenVisuInput.get(0), DEFAULT_TIME_SCALE)));
                if (booleanShowList != null) {
                    this.booleanShowList.add(true);
                }
                for (int i = 0; i < minimumLength; i++) {
                    toReturn.add(
                            new TimeInterval(absoluteDateToJulianDate(datesWhenVisuInput.get(i), DEFAULT_TIME_SCALE),
                                    absoluteDateToJulianDate(datesWhenNotVisuInput.get(i), DEFAULT_TIME_SCALE)));
                    toReturn.add(
                            new TimeInterval(absoluteDateToJulianDate(datesWhenNotVisuInput.get(i), DEFAULT_TIME_SCALE),
                                    absoluteDateToJulianDate(datesWhenVisuInput.get(i + 1), DEFAULT_TIME_SCALE)));
                }
            } else {
                toReturn.add(new TimeInterval(Header.getMasterClock()
                                                    .getAvailability()
                                                    .getStart(),
                        absoluteDateToJulianDate(datesWhenNotVisuInput.get(0), DEFAULT_TIME_SCALE)));
                if (booleanShowList != null) {
                    this.booleanShowList.add(false);
                }
                for (int i = 0; i < minimumLength; i++) {
                    toReturn.add(
                            new TimeInterval(absoluteDateToJulianDate(datesWhenNotVisuInput.get(i), DEFAULT_TIME_SCALE),
                                    absoluteDateToJulianDate(datesWhenVisuInput.get(i), DEFAULT_TIME_SCALE)));
                    toReturn.add(
                            new TimeInterval(absoluteDateToJulianDate(datesWhenVisuInput.get(i), DEFAULT_TIME_SCALE),
                                    absoluteDateToJulianDate(datesWhenNotVisuInput.get(i), DEFAULT_TIME_SCALE)));
                }
            }

            if (datesWhenVisuInput.size() > datesWhenNotVisuInput.size()) {

                final JulianDate finalDate = absoluteDateToJulianDate(
                        datesWhenVisuInput.get(datesWhenVisuInput.size() - 1), DEFAULT_TIME_SCALE);
                toReturn.add(new TimeInterval(finalDate, Header.getMasterClock()
                                                               .getAvailability()
                                                               .getStop()));

            } else if (datesWhenVisuInput.size() < datesWhenNotVisuInput.size()) {

                final JulianDate finalDate = absoluteDateToJulianDate(
                        datesWhenNotVisuInput.get(datesWhenNotVisuInput.size() - 1), DEFAULT_TIME_SCALE);
                toReturn.add(new TimeInterval(finalDate, Header.getMasterClock()
                                                               .getAvailability()
                                                               .getStop()));
            }
            return toReturn;
        }
        return new ArrayList<>();
    }

    private List<CzmlShow> buildShowList(final List<TimeInterval> timeIntervalsInput,
                                         final List<Boolean> booleanListInput) {
        final List<CzmlShow> toReturn = new ArrayList<>();
        if (!(timeIntervalsInput.isEmpty())) {
            for (int i = 0; i < timeIntervalsInput.size(); i++) {
                final TimeInterval currentTimeInterval = timeIntervalsInput.get(i);
                toReturn.add(new CzmlShow(booleanListInput.get(i), currentTimeInterval));
            }
        }
        return toReturn;
    }

    private List<List<Satellite>> reorganiseSatelliteList(final List<List<Satellite>> inputList) {
        final List<List<Satellite>> toReturn = new ArrayList<>();
        for (List<Satellite> satellites : inputList) {
            List<Satellite> tempPairOfSatellite = new ArrayList<>();
            for (int j = 0; j < satellites.size(); j = j + 2) {
                final Satellite currentFirstSatellite  = satellites.get(j);
                final Satellite currentSecondSatellite = satellites.get(j + 1);
                tempPairOfSatellite.add(currentFirstSatellite);
                tempPairOfSatellite.add(currentSecondSatellite);
                toReturn.add(tempPairOfSatellite);
                tempPairOfSatellite = new ArrayList<>();
            }
        }
        return toReturn;
    }

    private void writePairOfSatellite(final int iterationNumber) {
        try (PacketCesiumWriter packet = STREAM.openPacket(OUTPUT)) {
            final List<CzmlShow> currentShowList = allShowList.get(iterationNumber);
            if (!(currentShowList == null)) {
                final Satellite currentFirstSatellite = pairsOfSatellites.get(iterationNumber)
                                                                         .get(0);
                final Satellite currentSecondSatellite = pairsOfSatellites.get(iterationNumber)
                                                                          .get(1);
                packet.writeId(DEFAULT_ID + currentFirstSatellite.getId() + "/" + currentSecondSatellite.getId());
                packet.writeName(
                        DEFAULT_CONSTELLATION_NAME + currentFirstSatellite.getName() + " " + currentSecondSatellite.getName());
                final TimeInterval minimumInterval = this.findMinimumAvailability(currentFirstSatellite,
                        currentSecondSatellite);
                packet.writeAvailability(minimumInterval);

                final Iterable<Reference> currentReferences = allReferences.get(iterationNumber);
                final Polyline            currentPolyline   = polylines.get(iterationNumber);

                currentPolyline.writePolylineOfVisibility(packet, OUTPUT, currentReferences, currentShowList);
            }
        }
    }

    private static class InterSatViewHandler implements EventHandler {

        /** . */
        private TimeSpanMap<Boolean> viewMap;

        InterSatViewHandler() {
            viewMap = new TimeSpanMap<>(null);
        }

        public void init(final SpacecraftState initialState, final AbsoluteDate target, final EventDetector detector) {
            final double  g       = detector.g(initialState);
            final boolean visible = g >= 0;
            viewMap.addValidAfter(visible, initialState.getDate(), true);
        }

        @Override
        public Action eventOccurred(final SpacecraftState s, final EventDetector detector, final boolean increasing) {
            viewMap.addValidAfter(increasing, s.getDate(), true);
            return Action.CONTINUE;
        }

    }
}
