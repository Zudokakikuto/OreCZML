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

public class InterSatVisu extends AbstractPrimaryObject implements CzmlPrimaryObject {

    /**
     * .
     */
    public static final String DEFAULT_ID = "INTER_SAT_VISU/";
    /**
     * .
     */
    public static final String DEFAULT_NAME = "Visualisation inter-satellite of : ";
    /**
     * .
     */
    public static final String DEFAULT_H_POSITION = "#position";
    /**
     * .
     */
    public static final String DEFAULT_CONSTELLATION_ID = "INTER_CONSTELLATION_VISU/";
    /**
     * .
     */
    public static final String DEFAULT_CONSTELLATION_NAME = "Visualisation inter-constellation of : ";

    /**
     * .
     */
    private Satellite satellite1;
    /**
     * .
     */
    private Satellite satellite2;
    /**
     * .
     */
    private BoundedPropagator boundedPropagatorFirstSat;
    /**
     * .
     */
    private BoundedPropagator boundedPropagatorSecondSat;
    /**
     * .
     */
    private Propagator propagatorFirstSat;
    /**
     * .
     */
    private Propagator propagatorSecondSat;
    /**
     * .
     */
    private OneAxisEllipsoid body;
    /**
     * .
     */
    private SpacecraftState initialState;
    /**
     * .
     */
    private List<AbsoluteDate> datesWhenVisu = new ArrayList<>();
    /**
     * .
     */
    private List<AbsoluteDate> datesWhenNotVisu = new ArrayList<>();
    /**
     * .
     */
    private AbsoluteDate startDate;
    /**
     * .
     */
    private AbsoluteDate stopDate;
    /**
     * .
     */
    private List<TimeInterval> timeIntervalsOfVisu = new ArrayList<>();
    /**
     * .
     */
    private Polyline polyline;
    /**
     * .
     */
    private Iterable<Reference> references;
    /**
     * .
     */
    private List<Boolean> booleanShowList = new ArrayList<>();
    /**
     * .
     */
    private List<CzmlShow> showList = new ArrayList<>();
    // Constellation parameters
    /**
     * .
     */
    private List<Satellite> allConstellationSatellites = new ArrayList<>();
    /**
     * .
     */
    private List<String> allIdsSatellites = new ArrayList<>();
    /**
     * .
     */
    private List<BoundedPropagator> allPropagators = new ArrayList<>();
    /**
     * .
     */
    private List<Polyline> polylines = new ArrayList<>();
    /**
     * .
     */
    private List<List<TimeInterval>> allTimeIntervalsOfVisu = new ArrayList<>();
    /**
     * .
     */
    private List<List<Boolean>> allBooleanShowList = new ArrayList<>();
    /**
     * .
     */
    private List<List<CzmlShow>> allShowList = new ArrayList<>();
    /**
     * .
     */
    private List<Iterable<Reference>> allReferences = new ArrayList<>();
    /**
     * .
     */
    private List<Orbit> allOrbits = new ArrayList<>();
    /**
     * .
     */
    private List<List<Satellite>> pairsOfSatellites = new ArrayList<>();

    public InterSatVisu(final Satellite satellite1Input, final Satellite satellite2Input, final boolean toPropagate) {
        this.satellite1 = satellite1Input;
        this.satellite2 = satellite2Input;
        this.setId(DEFAULT_ID + satellite1Input.getId() + "/" + satellite2Input.getId());
        this.setName(DEFAULT_NAME + satellite2Input.getName() + "/" + satellite2Input.getName());
        final TimeInterval minimumInterval = this.findMinimumAvailability(satellite1Input, satellite2Input);
        this.setAvailability(minimumInterval);
        final Reference referenceFirstSatellite = new Reference(satellite1Input.getId() + DEFAULT_H_POSITION);
        final Reference referenceSecondSatellite = new Reference(satellite2Input.getId() + DEFAULT_H_POSITION);
        final Reference[] referenceList = Arrays.asList(referenceFirstSatellite, referenceSecondSatellite)
                                                .toArray(new Reference[0]);
        this.references = convertToIterable(referenceList);
        this.boundedPropagatorFirstSat = satellite1Input.getBoundedPropagator();
        this.boundedPropagatorSecondSat = satellite2Input.getBoundedPropagator();
        this.propagatorFirstSat = satellite1Input.getSatellitePropagator();
        this.propagatorSecondSat = satellite2Input.getSatellitePropagator();
        final Frame ITRF = FramesFactory.getITRF(IERSConventions.IERS_2010, true);
        this.body = new OneAxisEllipsoid(Constants.WGS84_EARTH_EQUATORIAL_RADIUS, Constants.WGS84_EARTH_FLATTENING, ITRF);
        this.initialState = satellite1Input.getAllSpaceCraftStates()
                                           .get(0);
        if (toPropagate) {
            this.propagationInterSat();
        }
        this.timeIntervalsOfVisu = this.buildShowIntervals(datesWhenVisu, datesWhenNotVisu);
        this.polyline = Polyline.nonVectorBuilder()
                                .withFirstReference(referenceFirstSatellite)
                                .withSecondReference(referenceSecondSatellite)
                                .build();
        this.showList = this.buildShowList(timeIntervalsOfVisu, booleanShowList);
    }

    public InterSatVisu(final List<BoundedPropagator> allPropagators, final AbsoluteDate finalDate) throws URISyntaxException, IOException {
        this(new Constellation(allPropagators, finalDate));
    }

    public InterSatVisu(final Constellation constellationPropagators) throws URISyntaxException, IOException {

        this.allOrbits = constellationPropagators.getAllOrbit();
        this.setId(DEFAULT_ID + constellationPropagators.getId());
        this.setName(DEFAULT_NAME + constellationPropagators.getTotalOfSatellite() + " satellites");
        this.setAvailability(Header.MASTER_CLOCK.getAvailability());
        this.allConstellationSatellites = constellationPropagators.getAllSatellites();
        this.allIdsSatellites = constellationPropagators.getAllIds();
        this.startDate = julianDateToAbsoluteDate(Header.MASTER_CLOCK.getAvailability()
                                                                     .getStart(), Header.TIME_SCALE);
        this.stopDate = julianDateToAbsoluteDate(Header.MASTER_CLOCK.getAvailability()
                                                                    .getStop(), Header.TIME_SCALE);

        final Frame ITRF = FramesFactory.getITRF(IERSConventions.IERS_2010, true);
        this.body = new OneAxisEllipsoid(Constants.WGS84_EARTH_EQUATORIAL_RADIUS, Constants.WGS84_EARTH_FLATTENING, ITRF);

        this.allPropagators = constellationPropagators.getAllPropagators();
        this.propagationInterConstellation();

        for (int i = 0; i < allConstellationSatellites.size(); i++) {
            final Satellite firstSatellite = allConstellationSatellites.get(i);
            final String currentIdFirstSatellite = allIdsSatellites.get(i);
            final Reference firstReferenceSatellite = new Reference(currentIdFirstSatellite + DEFAULT_H_POSITION);

            final List<Satellite> currentPairOfSatellites = new ArrayList<>();


            for (int j = i + 1; j < allConstellationSatellites.size(); j++) {
                final Satellite secondSatellite = allConstellationSatellites.get(j);
                final String currentIdSecondSatellite = allIdsSatellites.get(j);
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
            final List<Boolean> currentBooleans = allBooleanShowList.get(i);
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
                try (PacketCesiumWriter packet = STREAM.openPacket(OUTPUT)) {
                    final List<CzmlShow> currentShowList = allShowList.get(i);
                    if (!(currentShowList == null)) {
                        final Satellite currentFirstSatellite = pairsOfSatellites.get(i)
                                                                                 .get(0);
                        final Satellite currentSecondSatellite = pairsOfSatellites.get(i)
                                                                                  .get(1);
                        packet.writeId(DEFAULT_ID + currentFirstSatellite.getId() + "/" + currentSecondSatellite.getId());
                        packet.writeName(DEFAULT_CONSTELLATION_NAME + currentFirstSatellite.getName() + " " + currentSecondSatellite.getName());
                        final TimeInterval minimumInterval = this.findMinimumAvailability(currentFirstSatellite, currentSecondSatellite);
                        packet.writeAvailability(minimumInterval);

                        final Iterable<Reference> currentReferences = allReferences.get(i);
                        final Polyline currentPolyline = polylines.get(i);

                        currentPolyline.writePolylineOfVisibility(packet, OUTPUT, currentReferences, currentShowList);
                    }
                }
            }
        }
    }

    @Override
    public StringWriter getStringWriter() {
        return STRING_WRITER;
    }

    @Override
    public void cleanObject() {
        satellite1 = null;
        satellite2 = null;
        boundedPropagatorFirstSat = null;
        boundedPropagatorSecondSat = null;
        allPropagators = new ArrayList<>();
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
            this.startDate = julianDateToAbsoluteDate(satellite1Input.getAvailability()
                                                                     .getStart(), Header.TIME_SCALE);
            minimumInterval = satellite1Input.getAvailability();
        } else {
            this.startDate = julianDateToAbsoluteDate(satellite2Input.getAvailability()
                                                                     .getStart(), Header.TIME_SCALE);
            minimumInterval = satellite2Input.getAvailability();
        }
        return minimumInterval;
    }

    private void propagationInterSat() {
        final InterSatDirectViewDetector detector = new InterSatDirectViewDetector(this.getBody(), this.getBoundedPropagatorSecondSat()).withHandler((spacecraftState, currentDetector, increasing) -> {
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
        final AbsoluteDate startDateTemp = julianDateToAbsoluteDate(availabilityOfTheSatellite.getStart(), Header.TIME_SCALE);
        final AbsoluteDate stopDateTemp = julianDateToAbsoluteDate(availabilityOfTheSatellite.getStop(), Header.TIME_SCALE);
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
        final List<Propagator> constellationEphemeris = new ArrayList<>();
        generators.forEach(gen -> constellationEphemeris.add(gen.getGeneratedEphemeris()));

        // Add intersatview detectors
        final SortedMap<String, InterSatViewHandler> handlers = new TreeMap<>();

        for (int i = 0; i < constellationEphemeris.size() - 1; i++) {

            final Propagator main = constellationEphemeris.get(i);

            for (int j = i + 1; j < constellationEphemeris.size(); j++) {
                final Propagator second = constellationEphemeris.get(j);

                // Build detector
                final InterSatViewHandler handler = new InterSatViewHandler();
                final InterSatDirectViewDetector detector = new InterSatDirectViewDetector(body, second).withHandler(handler)
                                                                                                        .withMaxCheck(60.);

                // Add to main
                main.addEventDetector(detector);

                // Store handler
                handlers.put(i + " → " + j, handler);
            }
        }

        for (Propagator prop : constellationEphemeris) {
            prop.propagate(startDate, stopDate);
        }


        // Retrieve
        allBooleanShowList = new ArrayList<>();
        allTimeIntervalsOfVisu = new ArrayList<>();
        for (String key : handlers.keySet()) {
            final List<Boolean> tempBooleansList = new ArrayList<>();
            final List<TimeInterval> tempTimeIntervals = new ArrayList<>();
            for (TimeSpanMap.Span<Boolean> span = handlers.get(key).viewMap.getFirstSpan(); span != null; span = span.next()) {
                if (span.getData() != null) {
                    //System.out.println("\t" + span.getData() + ": " + span.getStart() + " → " + span.getEnd());
                    AbsoluteDate stopTime = span.getEnd();
                    final AbsoluteDate startTime = span.getStart();
                    if (span.getEnd()
                            .isAfter(julianDateToAbsoluteDate(Header.MASTER_CLOCK.getAvailability()
                                                                                 .getStop(), Header.TIME_SCALE))) {
                        stopTime = julianDateToAbsoluteDate(Header.MASTER_CLOCK.getAvailability()
                                                                               .getStop(), Header.TIME_SCALE);
                    }
                    tempBooleansList.add(span.getData());
                    tempTimeIntervals.add(new TimeInterval(absoluteDateToJulianDate(startTime), absoluteDateToJulianDate(stopTime)));
                }
            }
            if (!tempTimeIntervals.isEmpty()) {
                allTimeIntervalsOfVisu.add(tempTimeIntervals);
            }
            if (!tempBooleansList.isEmpty()) {
                allBooleanShowList.add(tempBooleansList);
            }
        }
    }

    private List<TimeInterval> buildShowIntervals(final List<AbsoluteDate> datesWhenVisuInput, final List<AbsoluteDate> datesWhenNotVisuInput) {

        if (!(datesWhenVisuInput.isEmpty() && datesWhenNotVisuInput.isEmpty())) {

            AbsoluteDate minimalDate;

            boolean seenAtTheBeginning = false;

            minimalDate = datesWhenVisuInput.get(0);

            if (minimalDate.isAfter(datesWhenNotVisuInput.get(0))) {
                minimalDate = datesWhenNotVisuInput.get(0);
                seenAtTheBeginning = true;
            }

            final int minimumLength = FastMath.min(datesWhenNotVisuInput.size(), datesWhenVisuInput.size());

            final List<TimeInterval> toReturn = new ArrayList<>();
            if (!seenAtTheBeginning) {
                toReturn.add(new TimeInterval(Header.MASTER_CLOCK.getAvailability()
                                                                 .getStart(), absoluteDateToJulianDate(datesWhenVisuInput.get(0))));
                if (booleanShowList != null) {
                    this.booleanShowList.add(true);
                }
                for (int i = 0; i < minimumLength; i++) {
                    toReturn.add(new TimeInterval(absoluteDateToJulianDate(datesWhenVisuInput.get(i)), absoluteDateToJulianDate(datesWhenNotVisuInput.get(i))));
                    toReturn.add(new TimeInterval(absoluteDateToJulianDate(datesWhenNotVisuInput.get(i)), absoluteDateToJulianDate(datesWhenVisuInput.get(i + 1))));
                }
            } else {
                toReturn.add(new TimeInterval(Header.MASTER_CLOCK.getAvailability()
                                                                 .getStart(), absoluteDateToJulianDate(datesWhenNotVisuInput.get(0))));
                if (booleanShowList != null) {
                    this.booleanShowList.add(false);
                }
                for (int i = 0; i < minimumLength; i++) {
                    toReturn.add(new TimeInterval(absoluteDateToJulianDate(datesWhenNotVisuInput.get(i)), absoluteDateToJulianDate(datesWhenVisuInput.get(i))));
                    toReturn.add(new TimeInterval(absoluteDateToJulianDate(datesWhenVisuInput.get(i)), absoluteDateToJulianDate(datesWhenNotVisuInput.get(i))));
                }
            }

            if (datesWhenVisuInput.size() > datesWhenNotVisuInput.size()) {
                final JulianDate finalDate = absoluteDateToJulianDate(datesWhenVisuInput.get(datesWhenVisuInput.size() - 1));
                toReturn.add(new TimeInterval(finalDate, Header.MASTER_CLOCK.getAvailability()
                                                                            .getStop()));
            } else if (datesWhenVisuInput.size() < datesWhenNotVisuInput.size()) {
                final JulianDate finalDate = absoluteDateToJulianDate(datesWhenNotVisuInput.get(datesWhenNotVisuInput.size() - 1));
                toReturn.add(new TimeInterval(finalDate, Header.MASTER_CLOCK.getAvailability()
                                                                            .getStop()));
            }
            return toReturn;
        }
        return new ArrayList<>();
    }

    private List<CzmlShow> buildShowList(final List<TimeInterval> timeIntervalsInput, final List<Boolean> booleanListInput) {
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
                final Satellite currentFirstSatellite = satellites.get(j);
                final Satellite currentSecondSatellite = satellites.get(j + 1);
                tempPairOfSatellite.add(currentFirstSatellite);
                tempPairOfSatellite.add(currentSecondSatellite);
                toReturn.add(tempPairOfSatellite);
                tempPairOfSatellite = new ArrayList<>();
            }
        }
        return toReturn;
    }

    private static class InterSatViewHandler implements EventHandler {

        /**
         * .
         */
        private TimeSpanMap<Boolean> viewMap;

        InterSatViewHandler() {
            viewMap = new TimeSpanMap<>(null);
        }

        public void init(final SpacecraftState initialState, final AbsoluteDate target, final EventDetector detector) {
            final double g = detector.g(initialState);
            final boolean visible = g >= 0;
            viewMap.addValidAfter(visible, initialState.getDate(), true);
        }

        @Override
        public Action eventOccurred(final SpacecraftState s, final EventDetector detector,
                                    final boolean increasing) {
            viewMap.addValidAfter(increasing, s.getDate(), true);
            return Action.CONTINUE;
        }

    }

}
