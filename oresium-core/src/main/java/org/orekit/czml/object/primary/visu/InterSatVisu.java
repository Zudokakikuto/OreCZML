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
import cesiumlanguagewriter.JulianDate;
import cesiumlanguagewriter.PacketCesiumWriter;
import cesiumlanguagewriter.Reference;
import cesiumlanguagewriter.TimeInterval;
import org.hipparchus.ode.events.Action;
import org.orekit.annotation.DefaultDataContext;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.czml.errors.OresiumException;
import org.orekit.czml.errors.OresiumMessages;
import org.orekit.czml.object.CzmlShow;
import org.orekit.czml.object.Polyline;
import org.orekit.czml.object.primary.AbstractPrimaryObject;
import org.orekit.czml.object.primary.entities.Constellation;
import org.orekit.czml.object.primary.entities.Spacecraft;
import org.orekit.czml.object.secondary.Clock;
import org.orekit.czml.object.utils.DateUtils;
import org.orekit.data.DataContext;
import org.orekit.frames.Frame;
import org.orekit.orbits.Orbit;
import org.orekit.propagation.BoundedPropagator;
import org.orekit.propagation.EphemerisGenerator;
import org.orekit.propagation.Propagator;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.events.EventDetector;
import org.orekit.propagation.events.InterSatDirectViewDetector;
import org.orekit.propagation.events.handlers.EventHandler;
import org.orekit.time.AbsoluteDate;
import org.orekit.utils.Constants;
import org.orekit.utils.IERSConventions;
import org.orekit.utils.TimeSpanMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Inter-sat Visu class
 * <p>
 * The inter sat visu class aims at displaying a line when two satellite see
 * each other. On a constellation, it can be applied as each satellite sees each
 * other.
 *
 * @author LEBLOND Julien
 * @since 1.0.0
 */
public class InterSatVisu
    extends
    AbstractPrimaryObject<InterSatVisu> {

    // Static

    /**
     * The default ID for the inter-sat visu object.
     */
    public static final String DEFAULT_ID = "INTER_SAT_VISU/";

    /**
     * The default name for the inter-sat visu object.
     */
    public static final String DEFAULT_NAME =
        "Visualisation inter-satellite of : ";

    /**
     * This allows creating a reference for a position for an object.
     */
    public static final String DEFAULT_H_POSITION = "#position";

    /**
     * The default ID for the inter-sat visu object when built with a
     * constellation.
     */
    public static final String DEFAULT_CONSTELLATION_NAME =
        "Visualisation inter-constellation of : ";

    // Arguments

    /**
     * The first satellite for the visu.
     */
    private Spacecraft spacecraft1;

    /**
     * The second satellite for the visu.
     */
    private Spacecraft spacecraft2;

    /**
     * The body which the satellites are orbiting around (future implementation
     * around several bodies).
     */
    private final OneAxisEllipsoid body;

    /**
     * The initial state of the first satellite.
     */
    private SpacecraftState initialState;

    /**
     * A time map used to track whether the satellites are visible in the event
     * detector.
     */
    private final TimeSpanMap<Boolean> timeSpanMap = new TimeSpanMap<>(null);

    /**
     * A list of time intervals that represents when the satellite is visible or
     * not.
     */
    private final List<TimeInterval> timeIntervals = new ArrayList<>();

    /**
     * The list of boolean to display or not the line if the satellite is
     * visible or not.
     */
    private final List<Boolean> visuList = new ArrayList<>();

    /**
     * The start date of the propagation.
     */
    private AbsoluteDate startDate;

    /**
     * The line to draw.
     */
    private Polyline polyline;

    /**
     * The references of each satellite.
     */
    private Iterable<Reference> references;

    /**
     * A list of CzmlShow that contains all the information about when and if
     * the line should be displayed.
     */
    private List<CzmlShow> showList = new ArrayList<>();

    // Constellation parameters

    /**
     * All the satellites of the constellation.
     */
    private List<Spacecraft> constellationSatellites = new ArrayList<>();

    /**
     * All the ids of the satellites of the constellation.
     */
    private List<String> idsSatellites = new ArrayList<>();

    /**
     * All the propagators of the constellation.
     */
    private List<BoundedPropagator> propagators = new ArrayList<>();

    /**
     * All the polylines of the constellation.
     */
    private final List<Polyline> polylines = new ArrayList<>();

    /**
     * ALl the time intervals of visualization the constellation, ordered by
     * pairs of satellites.
     */
    private List<List<TimeInterval>> timeIntervalsOfVisu = new ArrayList<>();

    /**
     * ALl the booleans of the constellation, ordered by pairs of satellites.
     */
    private List<List<Boolean>> booleansList = new ArrayList<>();

    /**
     * All the booleans to know if whether to display the line, ordered by pairs
     * of satellites.
     */
    private final List<List<CzmlShow>> showsList = new ArrayList<>();

    /**
     * All the CzmlShow objects, ordered by pairs of satellites.
     */
    private final List<Iterable<Reference>> referencesList = new ArrayList<>();

    /**
     * All the references of each pair of satellites, ordered by pairs of
     * satellites.
     */
    private List<Orbit> orbits = new ArrayList<>();

    /**
     * All the pairs of satellites.
     */
    private List<List<Spacecraft>> pairsOfSatellites = new ArrayList<>();

    /** The final date of the propagation. */
    private final AbsoluteDate finalDate;

    /** The clock of the inter sat visu. */
    private final Clock clock;

    /** The constellation if one is used. */
    private Constellation constellation;

    // Constructors

    /**
     * The constructor for the inter-sat visu object between two satellites.
     *
     * @param satellite1Input : The first satellite for the visu.
     * @param satellite2Input : The second satellite for the visu.
     * @param finalDate : The final date for the propagation.
     */
    InterSatVisu(final Spacecraft satellite1Input,
                 final Spacecraft satellite2Input,
                 final AbsoluteDate finalDate) {
        this(satellite1Input, satellite2Input, finalDate,
             DEFAULT_ID +
                                                          satellite1Input
                                                              .getId() +
                                                          "/" + satellite2Input
                                                              .getId());
    }

    /**
     * The constructor for the inter-sat visu object between two satellites.
     *
     * @param satellite1Input : The first satellite for the visu.
     * @param satellite2Input : The second satellite for the visu.
     * @param finalDateInput : The final date for the propagation.
     * @param customID : The custom ID of the inter sat visu object.
     */
    @DefaultDataContext
    InterSatVisu(final Spacecraft satellite1Input,
                 final Spacecraft satellite2Input,
                 final AbsoluteDate finalDateInput, final String customID) {

        this.spacecraft1 = satellite1Input;
        this.spacecraft2 = satellite2Input;
        this.finalDate = finalDateInput;
        this.setId(customID);
        this.setName(DEFAULT_NAME +
                     satellite2Input.getName() + "/" +
                     satellite2Input.getName());
        final TimeInterval minimumInterval =
            this.findMinimumAvailability(satellite1Input, satellite2Input);
        this.setAvailability(minimumInterval);
        final Frame ITRF =
            DataContext.getDefault().getFrames()
                .getITRF(IERSConventions.IERS_2010, true);
        this.body =
            new OneAxisEllipsoid(Constants.WGS84_EARTH_EQUATORIAL_RADIUS,
                                 Constants.WGS84_EARTH_FLATTENING, ITRF);

        final List<Spacecraft> spacecrafts = new ArrayList<>();
        spacecrafts.add(spacecraft1);
        spacecrafts.add(spacecraft2);
        this.clock = findMinimumClock(spacecrafts, minimumInterval);
        this.initialState = satellite1Input.getSpaceCraftStates().get(0);

        final Reference referenceFirstSatellite =
            new Reference(satellite1Input.getId() + DEFAULT_H_POSITION);
        final Reference referenceSecondSatellite =
            new Reference(satellite2Input.getId() + DEFAULT_H_POSITION);
        final Reference[] referenceList =
            Arrays.asList(referenceFirstSatellite, referenceSecondSatellite)
                .toArray(new Reference[0]);
        this.references = convertToIterable(referenceList);

        this.buildSingleTimeIntervalsAndVisu(spacecraft1, spacecraft2);
        this.polyline =
            Polyline.nonVectorBuilder(this.clock)
                .withFirstReference(referenceFirstSatellite)
                .withSecondReference(referenceSecondSatellite).build();
        this.showList = this.buildShowList(timeIntervals, visuList);
    }

    /**
     * The constructor for several satellites, the inter-sat visu will be
     * computed between each satellite with all the other.
     *
     * @param propagators : A list of all the bounded propagator that represents
     *        all the satellites.
     * @param finalDateInput : The final date for the end of the propagation.
     * @param clockInput : The clock considered. *
     */
    InterSatVisu(final List<BoundedPropagator> propagators,
                 final AbsoluteDate finalDateInput, final Clock clockInput) {
        this(Constellation.builder(propagators, finalDateInput, clockInput)
            .build(), finalDateInput, clockInput);
    }

    /**
     * The constructor for several satellites, the inter-sat visu will be
     * computed between each satellite with all the other.
     *
     * @param propagators : A list of all the bounded propagator that represents
     *        all the satellites.
     * @param finalDateInput : The final date for the end of the propagation.
     * @param customID : The custom ID of the inter sat visu.
     * @param clockInput : The clock. *
     */
    InterSatVisu(final List<BoundedPropagator> propagators,
                 final AbsoluteDate finalDateInput, final String customID,
                 final Clock clockInput) {
        this(Constellation.builder(propagators, finalDateInput, clockInput)
            .build(), finalDateInput, customID, clockInput);
    }

    /**
     * The constructor for a constellation, the inter-sat visu will be computed
     * between each satellite with all the other.
     *
     * @param constellationPropagators : The constellation object
     * @param finalDate : The final date for the propagation
     * @param clock : The clock considered.
     */
    InterSatVisu(final Constellation constellationPropagators,
                 final AbsoluteDate finalDate, final Clock clock) {
        this(constellationPropagators, finalDate,
             DEFAULT_ID + constellationPropagators.getId(), clock);
    }

    /**
     * The constructor for a constellation, the inter-sat visu will be computed
     * between each satellite with all the other.
     *
     * @param constellationPropagators : The constellation object
     * @param finalDateInput : The final date for the propagation
     * @param customID : The custom ID of the inter sat visu.
     * @param clock : The clock considered.
     */
    @DefaultDataContext
    InterSatVisu(final Constellation constellationPropagators,
                 final AbsoluteDate finalDateInput, final String customID,
                 final Clock clock) {

        this.setAvailability(clock.getAvailability());
        this.orbits = constellationPropagators.getInitialOrbits();
        this.setId(customID);
        this.setName(DEFAULT_NAME +
                     constellationPropagators.getTotalOfSatellite() +
                     " satellites");
        this.setAvailability(getAvailability());

        this.constellationSatellites = constellationPropagators.getSatellites();

        this.clock =
            findMinimumClock(constellationSatellites, clock.getAvailability());

        this.idsSatellites = constellationPropagators.getIds();
        this.startDate = DateUtils.toAbsoluteDate(getAvailability().getStart());

        this.startDate =
            DateUtils.toAbsoluteDate(clock.getAvailability().getStart());
        this.finalDate = finalDateInput;

        final Frame ITRF =
            DataContext.getDefault().getFrames()
                .getITRF(IERSConventions.IERS_2010, true);
        this.body =
            new OneAxisEllipsoid(Constants.WGS84_EARTH_EQUATORIAL_RADIUS,
                                 Constants.WGS84_EARTH_FLATTENING, ITRF);

        this.propagators = constellationPropagators.getPropagators();
        this.constellation =
            Constellation.builder(propagators, finalDateInput, clock).build();
        this.propagationInterConstellation(finalDateInput,
                                           clock.getAvailability());

        for (int i = 0; i < constellationSatellites.size(); i++) {
            final Spacecraft firstSatellite = constellationSatellites.get(i);
            final String currentIdFirstSatellite = idsSatellites.get(i);
            final Reference firstReferenceSatellite =
                new Reference(currentIdFirstSatellite + DEFAULT_H_POSITION);

            final List<Spacecraft> currentPairOfSatellites = new ArrayList<>();

            for (int j = i + 1; j < constellationSatellites.size(); j++) {
                final Spacecraft secondSatellite =
                    constellationSatellites.get(j);
                final String currentIdSecondSatellite = idsSatellites.get(j);
                final Reference secondReferenceSatellite =
                    new Reference(currentIdSecondSatellite +
                                  DEFAULT_H_POSITION);
                final Reference[] referenceList =
                    Arrays
                        .asList(firstReferenceSatellite,
                                secondReferenceSatellite)
                        .toArray(new Reference[0]);
                referencesList.add(convertToIterable(referenceList));
                polylines.add(Polyline.nonVectorBuilder(this.clock)
                    .withFirstReference(firstReferenceSatellite)
                    .withSecondReference(secondReferenceSatellite).build());
                currentPairOfSatellites.add(firstSatellite);
                currentPairOfSatellites.add(secondSatellite);
            }
            pairsOfSatellites.add(currentPairOfSatellites);
        }
        pairsOfSatellites = this.reorganiseSatelliteList(pairsOfSatellites);

        for (int i = 0; i < pairsOfSatellites.size(); i++) {
            final List<TimeInterval> currentTimeIntervals =
                timeIntervalsOfVisu.get(i);
            final List<Boolean> currentBooleans = booleansList.get(i);
            showsList
                .add(this.buildShowList(currentTimeIntervals, currentBooleans));
        }
    }

    // Builders

    /**
     * Builder inter sat visu builder.
     *
     * @param satellite1Input the satellite 1 input
     * @param satellite2Input the satellite 2 input
     * @param finalDateInput the final date
     * @param clockInput the clock
     * @return the inter sat visu builder
     */
    public static InterSatVisuBuilder builder(final Spacecraft satellite1Input,
                                              final Spacecraft satellite2Input,
                                              final AbsoluteDate finalDateInput,
                                              final Clock clockInput) {
        return new InterSatVisuBuilder(satellite1Input, satellite2Input,
                                       finalDateInput, clockInput);
    }

    /**
     * Builder inter sat visu builder.
     *
     * @param allPropagatorsInput the all propagators input
     * @param finalDateInput the final date
     * @param clockInput the clock
     * @return the inter sat visu builder *
     */
    public static InterSatVisuBuilder
        builder(final List<BoundedPropagator> allPropagatorsInput,
                final AbsoluteDate finalDateInput, final Clock clockInput) {
        return new InterSatVisuBuilder(allPropagatorsInput, finalDateInput,
                                       clockInput);
    }

    /**
     * Builder inter sat visu builder.
     *
     * @param constellationInput : the constellation input
     * @param finalDateInput : the final date
     * @param clockInput : the clock
     * @return the inter sat visu builder
     */
    public static InterSatVisuBuilder
        builder(final Constellation constellationInput,
                final AbsoluteDate finalDateInput, final Clock clockInput) {
        return new InterSatVisuBuilder(constellationInput, finalDateInput,
                                       clockInput);
    }

    // Overrides

    @Override
    public void writeCzmlBlock(final CesiumStreamWriter stream,
                               final CesiumOutputStream output) {
        if (constellationSatellites.isEmpty()) {
            if (!showList.isEmpty()) {
                output.setPrettyFormatting(true);
                try (PacketCesiumWriter packet = stream.openPacket(output)) {
                    packet.writeId(getId());
                    packet.writeName(getName());
                    packet.writeAvailability(getAvailability());

                    this.polyline.writePolylineOfVisibility(packet, output,
                                                            this.references,
                                                            this.showList);
                }
            }
        } else {
            output.setPrettyFormatting(true);
            for (int i = 0; i < pairsOfSatellites.size(); i++) {
                writePairOfSatellite(i, stream, output);
            }
        }
    }

    @Override
    public InterSatVisu cloneObject() {
        if (this.spacecraft1 != null && this.spacecraft2 != null) {
            final InterSatVisu copy =
                InterSatVisu.builder(this.spacecraft1, this.spacecraft2,
                                     this.finalDate, this.clock)
                    .withCustomId(getId()).build();
            copy.setName(getName());
            return copy;
        } else if (!propagators.isEmpty()) {
            final InterSatVisu copy =
                InterSatVisu
                    .builder(this.propagators, this.finalDate, this.clock)
                    .withCustomId(getId()).build();
            copy.setName(getName());
            return copy;
        } else if (!(this.constellation.getPropagators().isEmpty())) {
            final InterSatVisu copy =
                InterSatVisu
                    .builder(this.constellation, this.finalDate, this.clock)
                    .withCustomId(getId()).build();
            copy.setName(getName());
            return copy;
        } else {
            throw new OresiumException(OresiumMessages.NOT_VALID_PRIMARY_OBJECT_FOR_CLONE);
        }
    }

    // Getters

    /**
     * Gets satellite 1.
     *
     * @return the satellite 1
     */
    public Spacecraft getSpacecraft1() {
        return spacecraft1.cloneObject();
    }

    /**
     * Gets satellite 2.
     *
     * @return the satellite 2
     */
    public Spacecraft getSpacecraft2() {
        return spacecraft2.cloneObject();
    }

    /**
     * Gets references.
     *
     * @return the references
     */
    public Iterable<Reference> getReferences() {
        return references;
    }

    /**
     * Gets polyline.
     *
     * @return the polyline
     */
    public Polyline getPolyline() {
        return polyline;
    }

    /**
     * Gets the constellation if it exists.
     *
     * @return the constellation
     */
    public Constellation getConstellation() {
        return constellation.cloneObject();
    }

    /**
     * Gets initial state.
     *
     * @return the initial state
     */
    public SpacecraftState getInitialState() {
        return initialState;
    }

    /**
     * Gets body.
     *
     * @return the body
     */
    public OneAxisEllipsoid getBody() {
        return body;
    }

    /**
     * Gets boolean list.
     *
     * @return the boolean list
     */
    public List<Boolean> getBooleanList() {
        return Collections.unmodifiableList(visuList);
    }

    /**
     * Gets start date.
     *
     * @return the start date
     */
    public AbsoluteDate getStartDate() {
        return startDate;
    }

    /**
     * Gets propagators.
     *
     * @return the propagators
     */
    public List<BoundedPropagator> getPropagators() {
        return Collections.unmodifiableList(propagators);
    }

    public AbsoluteDate getFinalDate() {
        return finalDate;
    }

    /**
     * Gets ids satellites.
     *
     * @return the ids satellites
     */
    public List<String> getIdsSatellites() {
        return Collections.unmodifiableList(idsSatellites);
    }

    /**
     * Gets orbits.
     *
     * @return the orbits
     */
    public List<Orbit> getOrbits() {
        return Collections.unmodifiableList(orbits);
    }

    // Setters

    /**
     * Sets the propagators.
     *
     * @param propagators : The propagators to use
     */
    public void setPropagators(final List<BoundedPropagator> propagators) {
        this.propagators = propagators;
    }

    /**
     * Aims at finding the lowest time interval to set up the availability of
     * the inter-sat object. The inter-sat object can't have a greater
     * availability than the satellite that has the lowest availability.
     *
     * @param spacecraft1Input : The first satellite of the inter sat
     *        visualization
     * @param spacecraft2Input : The second satellite of the inter sat
     *        visualization
     * @return : The lowest time interval between the two satellites.
     */
    private TimeInterval
        findMinimumAvailability(final Spacecraft spacecraft1Input,
                                final Spacecraft spacecraft2Input) {

        final double durationAvailabilityFirstSat =
            spacecraft1Input.getAvailability().getStart()
                .secondsDifference(spacecraft1Input.getAvailability()
                    .getStop());

        final double durationAvailabilitySecondSat =
            spacecraft2Input.getAvailability().getStart()
                .secondsDifference(spacecraft2Input.getAvailability()
                    .getStop());

        final TimeInterval minimumInterval;
        if (durationAvailabilityFirstSat < durationAvailabilitySecondSat) {

            this.startDate =
                DateUtils.toAbsoluteDate(spacecraft1Input.getAvailability()
                    .getStart());
            minimumInterval = spacecraft1Input.getAvailability();

        } else {

            this.startDate =
                DateUtils.toAbsoluteDate(spacecraft2Input.getAvailability()
                    .getStart());
            minimumInterval = spacecraft2Input.getAvailability();
        }
        return minimumInterval;
    }

    /**
     * Aims at finding the clock to use when two satellites are used in the
     * inter sat visu.
     *
     * @param spacecrafts : The list of spacecrafts of the inter sat
     *        visualization
     * @param minimumAvailability : The minimum availability of the inter sat
     *        visu
     * @return : The clock with the minimum availability and the lowest
     *         multiplier between the two clocks of the spacecrafts
     */
    private Clock findMinimumClock(final List<Spacecraft> spacecrafts,
                                   final TimeInterval minimumAvailability) {
        final AbsoluteDate startDateInput =
            DateUtils.toAbsoluteDate(minimumAvailability.getStart());
        final AbsoluteDate finalDateInput =
            DateUtils.toAbsoluteDate(minimumAvailability.getStop());

        double minimumClockMultiplier = 1e20;
        for (final Spacecraft currentSpacecraft : spacecrafts) {
            final Clock currentClock = currentSpacecraft.getClock();
            if (minimumClockMultiplier > currentClock.getMultiplier()) {
                minimumClockMultiplier = currentClock.getMultiplier();
            }
        }
        return new Clock(startDateInput, finalDateInput,
                         minimumClockMultiplier);
    }

    // Private functions

    /**
     * Add a detector for the interring sat view between the two satellites.
     * Then this function propagates the propagator of the first satellite.
     *
     * @param boundedPropagatorSat1 : The propagator for the first satellite
     * @param boundedPropagatorSat2 : The propagator for the second satellite
     * @return : The visibility at the beginning of the propagation interval
     */
    private boolean
        propagationInterSat(final BoundedPropagator boundedPropagatorSat1,
                            final BoundedPropagator boundedPropagatorSat2) {

        final InterSatDirectViewDetector detector =
            new InterSatDirectViewDetector(this.getBody(),
                                           boundedPropagatorSat2)
                .withHandler((spacecraftState, currentDetector, increasing) -> {
                    if (increasing) {
                        timeSpanMap.addValidAfter(true,
                                                  spacecraftState.getDate(),
                                                  true);
                    } else {
                        timeSpanMap.addValidAfter(false,
                                                  spacecraftState.getDate(),
                                                  true);
                    }
                    return Action.CONTINUE;
                });

        final AbsoluteDate initDate =
            DateUtils.toAbsoluteDate(getClock().getAvailability().getStart());
        final AbsoluteDate stopDate =
            DateUtils.toAbsoluteDate(getClock().getAvailability().getStop());

        final boolean initiallyVisible =
            detector.g(boundedPropagatorSat1.getInitialState()) > 0.0;
        boundedPropagatorSat1.addEventDetector(detector);
        boundedPropagatorSat1.propagate(initDate, stopDate);
        return initiallyVisible;
    }

    /**
     * Add detectors at each propagator of each satellite to detect the
     * inter-sat view. Then this function propagates those propagators to the
     * final date.
     *
     * @param finalDateInput : The final date of the propagation.
     * @param availability : The availability considered
     */
    private void
        propagationInterConstellation(final AbsoluteDate finalDateInput,
                                      final TimeInterval availability) {

        final List<EphemerisGenerator> generators = new ArrayList<>();

        for (int i = 0; i < orbits.size(); i++) {
            final BoundedPropagator currentPropagator = propagators.get(i);
            generators.add(currentPropagator.getEphemerisGenerator());
        }

        final List<BoundedPropagator> boundedPropagators =
            new ArrayList<>(propagators);
        final List<Propagator> propagatorsTemp = new ArrayList<>(propagators);
        // Get the ephemeris

        generators
            .forEach(gen -> propagatorsTemp.add(gen.getGeneratedEphemeris()));

        // Add inter-sat view detectors
        final SortedMap<String, InterSatViewHandler> handlers = new TreeMap<>();

        for (final Propagator currentPropagator : propagatorsTemp) {
            currentPropagator.clearStepHandlers();
            currentPropagator.clearEventsDetectors();
        }

        for (int i = 0; i < boundedPropagators.size() - 1; i++) {

            final Propagator propagator = propagatorsTemp.get(i);

            for (int j = i + 1; j < boundedPropagators.size(); j++) {
                ephemerisDetectorAddition(i, j, handlers, propagator,
                                          boundedPropagators);
            }
        }

        for (Propagator prop : propagatorsTemp) {
            prop.propagate(startDate, finalDateInput);
        }

        postPropagationConstellationRetrieve(handlers, availability);
    }

    /**
     * Handle the computation after the propagation of the constellation. It
     * computes the time intervals when if the satellites are visible to each
     * other during those intervals.
     *
     * @param handlers : A sorted map of string and inter-sat handler used in
     *        the propagation.
     * @param availability : The availability considered.
     */
    private void
        postPropagationConstellationRetrieve(final SortedMap<String, InterSatViewHandler> handlers,
                                             final TimeInterval availability) {
        // Retrieve
        booleansList = new ArrayList<>();
        timeIntervalsOfVisu = new ArrayList<>();
        for (Map.Entry<String, InterSatViewHandler> entry : handlers
            .entrySet()) {
            final InterSatViewHandler handler = entry.getValue();

            final List<Boolean> tempBooleansList = new ArrayList<>();
            final List<TimeInterval> tempTimeIntervals = new ArrayList<>();

            for (TimeSpanMap.Span<Boolean> span =
                handler.viewMap.getFirstSpan(); span != null;
                 span = span.next()) {
                availabilitiesAndShowFilling(span, tempBooleansList,
                                             tempTimeIntervals, availability);
            }
            if (!tempTimeIntervals.isEmpty()) {
                timeIntervalsOfVisu.add(tempTimeIntervals);
            }
            if (!tempBooleansList.isEmpty()) {
                booleansList.add(tempBooleansList);
            }
        }
    }

    /**
     * This function aims at filling a time intervals list and a boolean list
     * from a span. The span contains the time interval and the boolean that
     * represent if and when the satellites see each other.
     *
     * @param span : The span that contains the information
     * @param tempBooleansList : The boolean list to fill
     * @param tempTimeIntervals : The time interval list to fill
     * @param availability : The availability considered
     */
    private void
        availabilitiesAndShowFilling(final TimeSpanMap.Span<Boolean> span,
                                     final List<Boolean> tempBooleansList,
                                     final List<TimeInterval> tempTimeIntervals,
                                     final TimeInterval availability) {

        if (span.getData() != null) {
            AbsoluteDate stopTime = span.getEnd();
            final AbsoluteDate startTime = span.getStart();
            if (span.getEnd()
                .isAfter(DateUtils.toAbsoluteDate(availability.getStop()))) {
                stopTime = DateUtils.toAbsoluteDate(availability.getStop());
            }
            tempBooleansList.add(span.getData());
            tempTimeIntervals
                .add(new TimeInterval(DateUtils.toJulianDate(startTime),
                                      DateUtils.toJulianDate(stopTime)));
        }
    }

    private void
        ephemerisDetectorAddition(final int firstIterationNumber,
                                  final int secondIterationNumber,
                                  final SortedMap<String, InterSatViewHandler> handlers,
                                  final Propagator propagator,
                                  final List<BoundedPropagator> propagatorsInput) {

        final BoundedPropagator second =
            propagatorsInput.get(secondIterationNumber);

        // Build detector
        final InterSatViewHandler handler = new InterSatViewHandler();
        final InterSatDirectViewDetector detector =
            new InterSatDirectViewDetector(body, second).withHandler(handler)
                .withMaxCheck(60.);

        // Add to main
        propagator.addEventDetector(detector);

        // Store handler
        handlers.put(firstIterationNumber + " → " + secondIterationNumber,
                     handler);
    }

    /**
     * This function aims at building the time intervals and the list of
     * visualization containing the boolean showing if the station sees the
     * satellite or not.
     *
     * @param satellite1Input : The first spacecraft.
     * @param satellite2Input : The second spacecraft.
     */
    private void
        buildSingleTimeIntervalsAndVisu(final Spacecraft satellite1Input,
                                        final Spacecraft satellite2Input) {

        // Get satellite propagators
        final BoundedPropagator boundedPropagatorSat1 =
            (BoundedPropagator) satellite1Input.getSpacecraftPropagator();
        final BoundedPropagator boundedPropagatorSat2 =
            (BoundedPropagator) satellite2Input.getSpacecraftPropagator();

        // Get min date and max date for line propagation
        final AbsoluteDate initDate =
            DateUtils.toAbsoluteDate(getClock().getAvailability().getStart());
        final AbsoluteDate stopDate =
            DateUtils.toAbsoluteDate(getClock().getAvailability().getStop());

        // Fills out timeIntervals and visuList, and returns the initial
        // visibility between
        // the satellites
        final boolean initiallyVisible =
            propagationInterSat(boundedPropagatorSat1, boundedPropagatorSat2);

        // Solver adds all but the very first interval, so we add it manually
        timeSpanMap.addValidAfter(initiallyVisible, initDate, false);

        // Goes through the timeSpanMap to create the list of visibility
        // intervals
        for (TimeSpanMap.Span<Boolean> span = timeSpanMap.getFirstNonNullSpan();
             span != null; span = span.next()) {
            visuList.add(span.getData());
            final JulianDate startJulian =
                DateUtils.toJulianDate(span.getStart());
            JulianDate stopJulian = DateUtils.toJulianDate(span.getEnd());

            if (span == timeSpanMap.getLastNonNullSpan()) {
                // last span: ensure it ends exactly at maxDate
                stopJulian = DateUtils.toJulianDate(stopDate);
            }
            timeIntervals.add(new TimeInterval(startJulian, stopJulian));
        }

        boundedPropagatorSat1.clearEventsDetectors();
    }

    /**
     * Aims at building a list of Czml show that will write in the czml file
     * when the satellites see each other.
     *
     * @param timeIntervalsInput : The list of time intervals, organized
     *        chronologically when the satellites see or not each other.
     * @param booleanListInput : The list of boolean depicting if they see each
     *        other, alternatively true, false corresponding to the time
     *        intervals.
     * @return : A list of CzmlShow objects built to represent when satellites
     *         see or not each other.
     */
    private List<CzmlShow>
        buildShowList(final List<TimeInterval> timeIntervalsInput,
                      final List<Boolean> booleanListInput) {
        final List<CzmlShow> toReturn = new ArrayList<>();
        if (!(timeIntervalsInput.isEmpty())) {
            for (int i = 0; i < timeIntervalsInput.size(); i++) {
                final TimeInterval currentTimeInterval =
                    timeIntervalsInput.get(i);
                final Clock currentClock =
                    new Clock(DateUtils
                        .toAbsoluteDate(currentTimeInterval.getStart()),
                              DateUtils.toAbsoluteDate(currentTimeInterval
                                  .getStop()),
                              DEFAULT_INTERVAL_BETWEEN_STEPS);
                toReturn
                    .add(new CzmlShow(booleanListInput.get(i), currentClock));
            }
        }
        return toReturn;
    }

    /**
     * Aims at reorganizing the satellite list of list to form a list of
     * satellite pairs.
     *
     * @param inputList : The list of list of satellite not organized by pair.
     * @return : A list of pairs of satellites.
     */
    private List<List<Spacecraft>>
        reorganiseSatelliteList(final List<List<Spacecraft>> inputList) {
        final List<List<Spacecraft>> toReturn = new ArrayList<>();
        for (List<Spacecraft> satellites : inputList) {
            List<Spacecraft> tempPairOfSatellite = new ArrayList<>();
            for (int j = 0; j < satellites.size(); j = j + 2) {
                final Spacecraft currentFirstSatellite = satellites.get(j);
                final Spacecraft currentSecondSatellite = satellites.get(j + 1);
                tempPairOfSatellite.add(currentFirstSatellite);
                tempPairOfSatellite.add(currentSecondSatellite);
                toReturn.add(tempPairOfSatellite);
                tempPairOfSatellite = new ArrayList<>();
            }
        }
        return toReturn;
    }

    /**
     * This function writes the pairs of satellites into a packet. It was made
     * to work into a for function with the given iteration number of the for
     * loop.
     *
     * @param iterationNumber : The number of the iteration of the for loop.
     * @param stream : The stream that converts all the strings into
     *        understandable string for the CzmlFile.
     * @param output : The output stream of cesium that will contain the strings
     *        to write into the CzmLFile.
     */
    private void writePairOfSatellite(final int iterationNumber,
                                      final CesiumStreamWriter stream,
                                      final CesiumOutputStream output) {
        try (PacketCesiumWriter packet = stream.openPacket(output)) {
            final List<CzmlShow> currentShowList =
                showsList.get(iterationNumber);
            if (!(currentShowList == null)) {
                final Spacecraft currentFirstSatellite =
                    pairsOfSatellites.get(iterationNumber).get(0);
                final Spacecraft currentSecondSatellite =
                    pairsOfSatellites.get(iterationNumber).get(1);
                packet.writeId(DEFAULT_ID +
                               currentFirstSatellite.getId() + "/" +
                               currentSecondSatellite.getId());
                packet.writeName(DEFAULT_CONSTELLATION_NAME +
                                 currentFirstSatellite.getName() + " " +
                                 currentSecondSatellite.getName());
                final TimeInterval minimumInterval =
                    this.findMinimumAvailability(currentFirstSatellite,
                                                 currentSecondSatellite);
                packet.writeAvailability(minimumInterval);

                final Iterable<Reference> currentReferences =
                    referencesList.get(iterationNumber);
                final Polyline currentPolyline = polylines.get(iterationNumber);

                currentPolyline.writePolylineOfVisibility(packet, output,
                                                          currentReferences,
                                                          currentShowList);
            }
        }
    }

    /**
     * This class was designed to handle the inter-sat view between satellites
     * of a constellation. It will build a time span map of boolean that will
     * group the time intervals and the boolean corresponding to the inter-sat
     * view.
     */
    private static class InterSatViewHandler
        implements
        EventHandler {

        /**
         * .
         */
        private final TimeSpanMap<Boolean> viewMap;

        /**
         * Instantiates a new Inter sat view handler.
         */
        InterSatViewHandler() {
            viewMap = new TimeSpanMap<>(null);
        }

        public void init(final SpacecraftState initialState,
                         final AbsoluteDate target,
                         final EventDetector detector) {
            final double g = detector.g(initialState);
            final boolean visible = g >= 0;
            viewMap.addValidAfter(visible, initialState.getDate(), true);
        }

        @Override
        public Action eventOccurred(final SpacecraftState s,
                                    final EventDetector detector,
                                    final boolean increasing) {
            viewMap.addValidAfter(increasing, s.getDate(), true);
            return Action.CONTINUE;
        }

    }

    /**
     * Aims at converting an array of references into an iterable of references.
     *
     * @param array : An array of references
     * @return : An iterable of references
     */
    private static Iterable<Reference>
        convertToIterable(final Reference[] array) {
        return () -> Arrays.stream(array).iterator();
    }
}
