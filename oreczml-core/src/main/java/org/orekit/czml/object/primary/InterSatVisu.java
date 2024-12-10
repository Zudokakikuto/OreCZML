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

import cesiumlanguagewriter.CesiumOutputStream;
import cesiumlanguagewriter.CesiumStreamWriter;
import cesiumlanguagewriter.JulianDate;
import cesiumlanguagewriter.PacketCesiumWriter;
import cesiumlanguagewriter.Reference;
import cesiumlanguagewriter.TimeInterval;
import org.hipparchus.ode.events.Action;
import org.hipparchus.util.FastMath;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.czml.archi.builder.InterSatVisuBuilder;
import org.orekit.czml.object.CzmlShow;
import org.orekit.czml.object.Polyline;
import org.orekit.czml.object.Utils.DateUtils;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
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

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Inter-sat Visu class
 *
 * <p> The inter sat visu class aims at displaying a line when two satellite see each other. On a constellation, it can
 * be applied as each satellite seeing each other.
 *
 * @author Julien LEBLOND
 * @since 1.0.0
 */
public class InterSatVisu extends AbstractPrimaryObject {

    // Static
    /**
     * The default ID for the inter-sat visu object.
     */
    public static final String DEFAULT_ID = "INTER_SAT_VISU/";

    /**
     * The default name for the inter-sat visu object.
     */
    public static final String DEFAULT_NAME = "Visualisation inter-satellite of : ";

    /**
     * This allows creating a reference for a position for an object.
     */
    public static final String DEFAULT_H_POSITION = "#position";

    /**
     * The default ID for the inter-sat visu object when built with a constellation.
     */
    public static final String DEFAULT_CONSTELLATION_NAME = "Visualisation inter-constellation of : ";


    // Arguments
    /**
     * The first satellite for the visu.
     */
    private Satellite satellite1;

    /**
     * The second satellite for the visu.
     */
    private Satellite satellite2;

    /**
     * The body which the satellites are orbiting around (future implementation around several bodies).
     */
    private final OneAxisEllipsoid body;

    /**
     * The initial state of the first satellite.
     */
    private SpacecraftState initialState;

    /**
     * The list of the dates when satellites see each other.
     */
    private final List<AbsoluteDate> datesWhenVisu = new ArrayList<>();

    /**
     * The list of dates when satellites don't see each other.
     */
    private final List<AbsoluteDate> datesWhenNotVisu = new ArrayList<>();

    /**
     * The start date of the propagation.
     */
    private AbsoluteDate startDate;

    /**
     * The stop date of the propagation.
     */
    private final AbsoluteDate stopDate;

    /**
     * The time intervals of visualization.
     */
    private List<TimeInterval> singleTimeIntervalsOfVisu = new ArrayList<>();

    /**
     * The line to draw.
     */
    private Polyline polyline;

    /**
     * The references of each satellite.
     */
    private Iterable<Reference> references;

    /**
     * A list of boolean that show when to display or not the lines.
     */
    private final List<Boolean> booleanList = new ArrayList<>();

    /**
     * A list of CzmlShow that contains all the information about when and if the line should be displayed.
     */
    private List<CzmlShow> showList = new ArrayList<>();

    // Constellation parameters
    /**
     * All the satellites of the constellation.
     */
    private List<Satellite> constellationSatellites = new ArrayList<>();

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
     * ALl the time intervals of visualization the constellation, ordered by pairs of satellites.
     */
    private List<List<TimeInterval>> timeIntervalsOfVisu = new ArrayList<>();

    /**
     * ALl the booleans of the constellation, ordered by pairs of satellites.
     */
    private List<List<Boolean>> booleansList = new ArrayList<>();

    /**
     * All the booleans to know if whether to display the line, ordered by pairs of satellites.
     */
    private final List<List<CzmlShow>> showsList = new ArrayList<>();

    /**
     * All the CzmlShow objects, ordered by pairs of satellites.
     */
    private final List<Iterable<Reference>> referencesList = new ArrayList<>();

    /**
     * All the references of each pair of satellites, ordered by pairs of satellites.
     */
    private List<Orbit> orbits = new ArrayList<>();

    /**
     * All the pairs of satellites.
     */
    private List<List<Satellite>> pairsOfSatellites = new ArrayList<>();

    /** The header used. */
    private final Header header;

    // Constructors

    /**
     * The constructor for the inter-sat visu object between two satellites.
     *
     * @param satellite1Input : The first satellite for the visu.
     * @param satellite2Input : The second satellite for the visu.
     * @param finalDate       : The final date for the propagation.
     * @param header          : The header considered.
     */
    public InterSatVisu(final Satellite satellite1Input, final Satellite satellite2Input, final AbsoluteDate finalDate,
                        final Header header) {
        this(satellite1Input, satellite2Input, finalDate,
                DEFAULT_ID + satellite1Input.getId() + "/" + satellite2Input.getId(), header);
    }

    /**
     * The constructor for the inter-sat visu object between two satellites.
     *
     * @param satellite1Input : The first satellite for the visu.
     * @param satellite2Input : The second satellite for the visu.
     * @param finalDate       : The final date for the propagation.
     * @param customID        : The custom ID of the inter sat visu object.
     * @param header          : The header of the
     */
    public InterSatVisu(final Satellite satellite1Input, final Satellite satellite2Input, final AbsoluteDate finalDate,
                        final String customID, final Header header) {

        this.satellite1 = satellite1Input;
        this.satellite2 = satellite2Input;
        this.header     = header;
        this.setId(customID);
        this.setName(DEFAULT_NAME + satellite2Input.getName() + "/" + satellite2Input.getName());
        final TimeInterval minimumInterval = this.findMinimumAvailability(satellite1Input, satellite2Input, header);
        this.setAvailability(minimumInterval);
        final Frame ITRF = FramesFactory.getITRF(IERSConventions.IERS_2010, true);
        this.body         = new OneAxisEllipsoid(Constants.WGS84_EARTH_EQUATORIAL_RADIUS,
                Constants.WGS84_EARTH_FLATTENING, ITRF);
        this.initialState = satellite1Input.getSpaceCraftStates()
                                           .get(0);

        final Reference referenceFirstSatellite  = new Reference(satellite1Input.getId() + DEFAULT_H_POSITION);
        final Reference referenceSecondSatellite = new Reference(satellite2Input.getId() + DEFAULT_H_POSITION);
        final Reference[] referenceList = Arrays.asList(referenceFirstSatellite, referenceSecondSatellite)
                                                .toArray(new Reference[0]);
        this.references = convertToIterable(referenceList);

        this.propagationInterSat(finalDate, satellite1Input, satellite2Input, header);

        this.singleTimeIntervalsOfVisu = this.buildIntervals(datesWhenVisu, datesWhenNotVisu, header);
        this.polyline                  = Polyline.nonVectorBuilder(header)
                                                 .withFirstReference(referenceFirstSatellite)
                                                 .withSecondReference(referenceSecondSatellite)
                                                 .build();
        this.showList                  = this.buildShowList(singleTimeIntervalsOfVisu, booleanList);
        this.stopDate = DateUtils.toAbsoluteDate(header.getAvailability().getStop(), header.getTimeScale());
    }

    /**
     * The constructor for several satellites, the inter-sat visu will be computed between each satellite with all the other.
     *
     * @param propagators : A list of all the bounded propagator that represents all the satellites.
     * @param finalDate   : The final date for the end of the propagation.
     * @param header      : The header considered.
     * @throws URISyntaxException the uri syntax exception
     * @throws IOException        the io exception
     */
    public InterSatVisu(final List<BoundedPropagator> propagators, final AbsoluteDate finalDate,
                        final Header header) throws URISyntaxException, IOException {
        this(new Constellation(propagators, finalDate, header), finalDate, header);
    }

    /**
     * The constructor for several satellites, the inter-sat visu will be computed between each satellite with all the other.
     *
     * @param propagators : A list of all the bounded propagator that represents all the satellites.
     * @param finalDate   : The final date for the end of the propagation.
     * @param customID    : The custom ID of the inter sat visu.
     * @param header      : The header considered when several are used.
     * @throws URISyntaxException the uri syntax exception
     * @throws IOException        the io exception
     */
    public InterSatVisu(final List<BoundedPropagator> propagators, final AbsoluteDate finalDate, final String customID,
                        final Header header) throws URISyntaxException, IOException {
        this(new Constellation(propagators, finalDate, header), finalDate, customID, header);
    }

    /**
     * The constructor for a constellation, the inter-sat visu will be computed between each satellite with all the other.
     *
     * @param constellationPropagators : The constellation object
     * @param finalDate                : The final date for the propagation
     * @param header                   : The header considered.
     */
    public InterSatVisu(final Constellation constellationPropagators, final AbsoluteDate finalDate,
                        final Header header) {
        this(constellationPropagators, finalDate, DEFAULT_ID + constellationPropagators.getId(), header);
    }

    /**
     * The constructor for a constellation, the inter-sat visu will be computed between each satellite with all the other.
     *
     * @param constellationPropagators : The constellation object
     * @param finalDate                : The final date for the propagation
     * @param customID                 : The custom ID of the inter sat visu.
     * @param header                   : The header considered when several are used.
     */
    public InterSatVisu(final Constellation constellationPropagators, final AbsoluteDate finalDate,
                        final String customID, final Header header) {

        this.header = header;
        this.orbits = constellationPropagators.getInitialOrbits();
        this.setId(customID);
        this.setName(DEFAULT_NAME + constellationPropagators.getTotalOfSatellite() + " satellites");
        this.setAvailability(header.getAvailability());

        this.constellationSatellites = constellationPropagators.getSatellites();

        this.idsSatellites = constellationPropagators.getIds();
        this.startDate     = DateUtils.toAbsoluteDate((header.getAvailability()).getStart(), header.getTimeScale());

        this.stopDate = finalDate;

        final Frame ITRF = FramesFactory.getITRF(IERSConventions.IERS_2010, true);
        this.body = new OneAxisEllipsoid(Constants.WGS84_EARTH_EQUATORIAL_RADIUS, Constants.WGS84_EARTH_FLATTENING,
                ITRF);

        this.propagators = constellationPropagators.getPropagators();
        this.propagationInterConstellation(finalDate, header);

        for (int i = 0; i < constellationSatellites.size(); i++) {
            final Satellite firstSatellite          = constellationSatellites.get(i);
            final String    currentIdFirstSatellite = idsSatellites.get(i);
            final Reference firstReferenceSatellite = new Reference(currentIdFirstSatellite + DEFAULT_H_POSITION);

            final List<Satellite> currentPairOfSatellites = new ArrayList<>();

            for (int j = i + 1; j < constellationSatellites.size(); j++) {
                final Satellite secondSatellite          = constellationSatellites.get(j);
                final String    currentIdSecondSatellite = idsSatellites.get(j);
                final Reference secondReferenceSatellite = new Reference(currentIdSecondSatellite + DEFAULT_H_POSITION);
                final Reference[] referenceList = Arrays.asList(firstReferenceSatellite, secondReferenceSatellite)
                                                        .toArray(new Reference[0]);
                referencesList.add(convertToIterable(referenceList));
                polylines.add(Polyline.nonVectorBuilder(header)
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
            final List<TimeInterval> currentTimeIntervals = timeIntervalsOfVisu.get(i);
            final List<Boolean>      currentBooleans      = booleansList.get(i);
            showsList.add(this.buildShowList(currentTimeIntervals, currentBooleans));
        }
    }


    /**
     * Builder inter sat visu builder.
     *
     * @param satellite1Input the satellite 1 input
     * @param satellite2Input the satellite 2 input
     * @param finalDate       the final date
     * @param header          the header
     * @return the inter sat visu builder
     */
//Builders
    public static InterSatVisuBuilder builder(final Satellite satellite1Input, final Satellite satellite2Input,
                                              final AbsoluteDate finalDate, final Header header) {
        return new InterSatVisuBuilder(satellite1Input, satellite2Input, finalDate, header);
    }

    /**
     * Builder inter sat visu builder.
     *
     * @param allPropagatorsInput the all propagators input
     * @param finalDate           the final date
     * @param header              the header
     * @return the inter sat visu builder
     * @throws URISyntaxException the uri syntax exception
     * @throws IOException        the io exception
     */
    public static InterSatVisuBuilder builder(final List<BoundedPropagator> allPropagatorsInput,
                                              final AbsoluteDate finalDate,
                                              final Header header) throws URISyntaxException, IOException {
        return new InterSatVisuBuilder(allPropagatorsInput, finalDate, header);
    }

    /**
     * Builder inter sat visu builder.
     *
     * @param constellationInput the constellation input
     * @param finalDate          the final date
     * @param header             the header
     * @return the inter sat visu builder
     */
    public static InterSatVisuBuilder builder(final Constellation constellationInput, final AbsoluteDate finalDate,
                                              final Header header) {
        return new InterSatVisuBuilder(constellationInput, finalDate, header);
    }

    // Overrides

    @Override
    public void writeCzmlBlock(final CesiumStreamWriter stream,
                               final CesiumOutputStream output) throws URISyntaxException, IOException {
        if (constellationSatellites.isEmpty()) {
            if (!showList.isEmpty()) {
                output.setPrettyFormatting(true);
                try (PacketCesiumWriter packet = stream.openPacket(output)) {
                    packet.writeId(getId());
                    packet.writeName(getName());
                    packet.writeAvailability(getAvailability());

                    this.polyline.writePolylineOfVisibility(packet, output, this.references, this.showList);
                }
            }
        } else {
            output.setPrettyFormatting(true);
            for (int i = 0; i < pairsOfSatellites.size(); i++) {
                writePairOfSatellite(i, stream, output, header);
            }
        }
    }


    /**
     * Gets satellite 1.
     *
     * @return the satellite 1
     */
    public Satellite getSatellite1() {
        return satellite1;
    }


    // Getters

    /**
     * Gets satellite 2.
     *
     * @return the satellite 2
     */
    public Satellite getSatellite2() {
        return satellite2;
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
     * Gets single time intervals of visu.
     *
     * @return the single time intervals of visu
     */
    public List<TimeInterval> getSingleTimeIntervalsOfVisu() {
        return Collections.unmodifiableList(singleTimeIntervalsOfVisu);
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
     * Gets dates when not visu.
     *
     * @return the dates when not visu
     */
    public List<AbsoluteDate> getDatesWhenNotVisu() {
        return Collections.unmodifiableList(datesWhenNotVisu);
    }

    /**
     * Gets dates when visu.
     *
     * @return the dates when visu
     */
    public List<AbsoluteDate> getDatesWhenVisu() {
        return Collections.unmodifiableList(datesWhenVisu);
    }

    /**
     * Gets show list.
     *
     * @return the show list
     */
    public List<CzmlShow> getShowList() {
        return Collections.unmodifiableList(showList);
    }

    /**
     * Gets boolean list.
     *
     * @return the boolean list
     */
    public List<Boolean> getBooleanList() {
        return Collections.unmodifiableList(booleanList);
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
     * Gets stop date.
     *
     * @return the stop date
     */
    public AbsoluteDate getStopDate() {
        return stopDate;
    }

    /**
     * Gets constellation satellites.
     *
     * @return the constellation satellites
     */
    public List<Satellite> getConstellationSatellites() {
        return Collections.unmodifiableList(constellationSatellites);
    }

    /**
     * Gets propagators.
     *
     * @return the propagators
     */
    public List<BoundedPropagator> getPropagators() {
        return Collections.unmodifiableList(propagators);
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
     * Gets polylines.
     *
     * @return the polylines
     */
    public List<Polyline> getPolylines() {
        return Collections.unmodifiableList(polylines);
    }

    /**
     * Gets time intervals of visu.
     *
     * @return the time intervals of visu
     */
    public List<List<TimeInterval>> getTimeIntervalsOfVisu() {
        return Collections.unmodifiableList(timeIntervalsOfVisu);
    }

    /**
     * Gets booleans list.
     *
     * @return the booleans list
     */
    public List<List<Boolean>> getBooleansList() {
        return Collections.unmodifiableList(booleansList);
    }

    /**
     * Gets shows list.
     *
     * @return the shows list
     */
    public List<List<CzmlShow>> getShowsList() {
        return Collections.unmodifiableList(showsList);
    }

    /**
     * Gets references list.
     *
     * @return the references list
     */
    public List<Iterable<Reference>> getReferencesList() {
        return Collections.unmodifiableList(referencesList);
    }

    /**
     * Gets orbits.
     *
     * @return the orbits
     */
    public List<Orbit> getOrbits() {
        return Collections.unmodifiableList(orbits);
    }

    /**
     * Gets pairs of satellites.
     *
     * @return the pairs of satellites
     */
    public List<List<Satellite>> getPairsOfSatellites() {
        return Collections.unmodifiableList(pairsOfSatellites);
    }

    /**
     * Aims at finding the lowest time interval to set up the availability of the inter-sat object.
     * The inter-sat object can't have a greater availability than the satellite that has the lowest availability.
     *
     * @param satellite1Input : The first satellite of the inter sat visualization
     * @param satellite2Input : The second satellite of the inter sat visualization
     * @param headerInput     : The header to consider when several are used.
     * @return : The lowest time interval between the two satellites.
     */
    private TimeInterval findMinimumAvailability(final Satellite satellite1Input, final Satellite satellite2Input,
                                                 final Header headerInput) {

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

            this.startDate  = DateUtils.toAbsoluteDate(satellite1Input.getAvailability()
                                                                      .getStart(), headerInput.getTimeScale());
            minimumInterval = satellite1Input.getAvailability();

        } else {

            this.startDate  = DateUtils.toAbsoluteDate(satellite2Input.getAvailability()
                                                                      .getStart(), headerInput.getTimeScale());
            minimumInterval = satellite2Input.getAvailability();
        }
        return minimumInterval;
    }


    // Private functions

    /**
     * Add a detector for the interring sat view between the two satellites.
     * Then this function propagates the propagator of the first satellite.
     *
     * @param finalDate       : The final date of the propagation.
     * @param satellite1Input : The first satellite of the couple
     * @param satellite2Input : The second satellite of the couple
     * @param headerInput     : The header to consider when several are used.
     */
    private void propagationInterSat(final AbsoluteDate finalDate, final Satellite satellite1Input,
                                     final Satellite satellite2Input, final Header headerInput) {

        final BoundedPropagator boundedPropagatorSat1 = satellite1Input.getSatelliteBoundedPropagator();
        final BoundedPropagator boundedPropagatorSat2 = satellite2Input.getSatelliteBoundedPropagator();

        final InterSatDirectViewDetector detector = new InterSatDirectViewDetector(this.getBody(),
                boundedPropagatorSat2).withHandler((spacecraftState, currentDetector, increasing) -> {
                    final double detected = currentDetector.g(spacecraftState);
                    if (detected >= 0) {
                        this.datesWhenNotVisu.add(spacecraftState.getDate());
                        this.booleanList.add(true);
                    } else if (detected < 0) {
                        this.datesWhenVisu.add(spacecraftState.getDate());
                        this.booleanList.add(false);
                    }
                    return Action.CONTINUE;
                });

        final TimeInterval availabilityOfTheSatellite = this.getSatellite1()
                                                            .getAvailability();

        final AbsoluteDate startDateTemp = DateUtils.toAbsoluteDate(availabilityOfTheSatellite.getStart(),
                headerInput.getTimeScale());

        boundedPropagatorSat1.addEventDetector(detector);

        boundedPropagatorSat1.propagate(startDateTemp, finalDate);
    }

    /**
     * Add detectors at each propagator of each satellite to detect the inter-sat view.
     * Then this function propagates those propagators to the final date.
     *
     * @param finalDate   : The final date of the propagation.
     * @param headerInput : The header to consider when several are used
     */
    private void propagationInterConstellation(final AbsoluteDate finalDate, final Header headerInput) {

        final List<EphemerisGenerator> generators = new ArrayList<>();

        for (int i = 0; i < orbits.size(); i++) {
            final BoundedPropagator currentPropagator = propagators.get(i);
            generators.add(currentPropagator.getEphemerisGenerator());
        }

        final List<BoundedPropagator> boundedPropagators = new ArrayList<>(propagators);
        final List<Propagator>        propagatorsTemp    = new ArrayList<>(propagators);
        // Get the ephemeris

        generators.forEach(gen -> propagatorsTemp.add(gen.getGeneratedEphemeris()));

        // Add inter-sat view detectors
        final SortedMap<String, InterSatViewHandler> handlers = new TreeMap<>();

        for (final Propagator currentPropagator : propagatorsTemp) {
            currentPropagator.clearStepHandlers();
            currentPropagator.clearEventsDetectors();
        }

        for (int i = 0; i < boundedPropagators.size() - 1; i++) {

            final Propagator propagator = propagatorsTemp.get(i);

            for (int j = i + 1; j < boundedPropagators.size(); j++) {
                ephemerisDetectorAddition(i, j, handlers, propagator, boundedPropagators);
            }
        }

        for (Propagator prop : propagatorsTemp) {
            prop.propagate(startDate, finalDate);
        }

        postPropagationConstellationRetrieve(handlers, headerInput);
    }

    /**
     * Handle the computation after the propagation of the constellation.
     * It computes the time intervals when if the satellites are visible to each other during those intervals.
     *
     * @param handlers    : A sorted map of string and inter-sat handler used in the propagation.
     * @param headerInput : The header considered when several are used.
     */
    private void postPropagationConstellationRetrieve(final SortedMap<String, InterSatViewHandler> handlers,
                                                      final Header headerInput) {
        // Retrieve
        booleansList        = new ArrayList<>();
        timeIntervalsOfVisu = new ArrayList<>();
        for (String key : handlers.keySet()) {
            final List<Boolean>      tempBooleansList  = new ArrayList<>();
            final List<TimeInterval> tempTimeIntervals = new ArrayList<>();
            for (TimeSpanMap.Span<Boolean> span = handlers.get(
                    key).viewMap.getFirstSpan(); span != null; span = span.next()) {
                availabilitiesAndShowFilling(span, tempBooleansList, tempTimeIntervals, headerInput);
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
     * This function aims at filling a time intervals list and a boolean list from a span.
     * The span contains the time interval and the boolean that represent if and when the satellites see each other.
     *
     * @param span              : The span that contains the information
     * @param tempBooleansList  : The boolean list to fill
     * @param tempTimeIntervals : The time interval list to fill
     * @param headerInput       : The header to consider when several are used
     */
    private void availabilitiesAndShowFilling(final TimeSpanMap.Span<Boolean> span,
                                              final List<Boolean> tempBooleansList,
                                              final List<TimeInterval> tempTimeIntervals, final Header headerInput) {

        if (span.getData() != null) {
            AbsoluteDate       stopTime  = span.getEnd();
            final AbsoluteDate startTime = span.getStart();
            if (span.getEnd()
                    .isAfter(DateUtils.toAbsoluteDate(headerInput.getAvailability().getStop(), headerInput.getTimeScale()))) {
                stopTime = DateUtils.toAbsoluteDate(headerInput.getAvailability().getStop(), headerInput.getTimeScale());
            }
            tempBooleansList.add(span.getData());
            tempTimeIntervals.add(new TimeInterval(DateUtils.toJulianDate(startTime, headerInput.getTimeScale()),
                    DateUtils.toJulianDate(stopTime, headerInput.getTimeScale())));
        }
    }

    private void ephemerisDetectorAddition(final int firstIterationNumber, final int secondIterationNumber,
                                           final SortedMap<String, InterSatViewHandler> handlers,
                                           final Propagator propagator,
                                           final List<BoundedPropagator> propagatorsInput) {

        final BoundedPropagator second = propagatorsInput.get(secondIterationNumber);

        // Build detector
        final InterSatViewHandler handler = new InterSatViewHandler();
        final InterSatDirectViewDetector detector = new InterSatDirectViewDetector(body, second).withHandler(handler)
                                                                                                .withMaxCheck(60.);

        // Add to main
        propagator.addEventDetector(detector);

        // Store handler
        handlers.put(firstIterationNumber + " → " + secondIterationNumber, handler);
    }

    /**
     * Aims at building the time intervals from a list when satellites see each other, and a list when
     * satellite doesn't see each other.
     *
     * @param datesWhenVisuInput    : A list of absolute dates that contains all the dates when satellites start to see each other.
     * @param datesWhenNotVisuInput : A list of absolute dates that contains all the dates when satellites stop to see each other.
     * @param headerInput           : The header considered when several are used.
     * @return : A list of time intervals that represents the time chronologically when satellites see and don't see each other.
     */
    private List<TimeInterval> buildIntervals(final List<AbsoluteDate> datesWhenVisuInput,
                                              final List<AbsoluteDate> datesWhenNotVisuInput,
                                              final Header headerInput) {

        if (!datesWhenVisuInput.isEmpty() && !datesWhenNotVisuInput.isEmpty()) {

            AbsoluteDate minimalDate;

            boolean seenAtTheBeginning = false;

            minimalDate = datesWhenVisuInput.get(0);

            if (minimalDate.isAfter(datesWhenNotVisuInput.get(0))) {
                minimalDate        = datesWhenNotVisuInput.get(0);
                seenAtTheBeginning = true;
            }

            final int minimumLength = FastMath.min(datesWhenNotVisuInput.size(), datesWhenVisuInput.size());

            final List<TimeInterval> toReturn = new ArrayList<>();

            buildTimeShowIntervals(seenAtTheBeginning, datesWhenVisuInput, datesWhenNotVisuInput, minimumLength,
                    toReturn, headerInput);

            addTheLastTimeInterval(datesWhenVisuInput, datesWhenNotVisuInput, toReturn, headerInput);

            return toReturn;
        }

        return new ArrayList<>();
    }

    /**
     * This function aims at building the list of boolean and of time interval from two lists of start and stop date.
     * Those lists represent the dates when satellites start and stop to see each other.
     *
     * @param seenAtTheBeginning    : If the satellites have sawed each other since the beginning of the simulation.
     * @param datesWhenVisuInput    : The list of absolute date that contains all the start date of when satellites see each other.
     * @param datesWhenNotVisuInput : The list of absolute date that contains all the stop date of when satellites stop seeing each others.
     * @param minimumLength         : The minimum length of between the two lists of absolute dates.
     * @param toReturn              : The list of time intervals that will be returned with added time intervals.
     * @param headerInput           : The header considered when several are used.
     */
    private void buildTimeShowIntervals(final boolean seenAtTheBeginning, final List<AbsoluteDate> datesWhenVisuInput,
                                        final List<AbsoluteDate> datesWhenNotVisuInput, final int minimumLength,
                                        final List<TimeInterval> toReturn, final Header headerInput) {

        if (!seenAtTheBeginning) {
            toReturn.add(new TimeInterval(headerInput.getAvailability().getStart(),
                    DateUtils.toJulianDate(datesWhenVisuInput.get(0), headerInput.getTimeScale())));
            this.booleanList.add(true);
            for (int i = 0; i < minimumLength; i++) {
                toReturn.add(new TimeInterval(DateUtils.toJulianDate(datesWhenVisuInput.get(i), headerInput.getTimeScale()),
                        DateUtils.toJulianDate(datesWhenNotVisuInput.get(i), headerInput.getTimeScale())));
                toReturn.add(new TimeInterval(DateUtils.toJulianDate(datesWhenNotVisuInput.get(i), headerInput.getTimeScale()),
                        DateUtils.toJulianDate(datesWhenVisuInput.get(i + 1), headerInput.getTimeScale())));
            }
        } else {
            toReturn.add(new TimeInterval(headerInput.getAvailability().getStart(),
                    DateUtils.toJulianDate(datesWhenNotVisuInput.get(0), headerInput.getTimeScale())));
            this.booleanList.add(false);
            for (int i = 0; i < minimumLength; i++) {
                toReturn.add(new TimeInterval(DateUtils.toJulianDate(datesWhenNotVisuInput.get(i), headerInput.getTimeScale()),
                        DateUtils.toJulianDate(datesWhenVisuInput.get(i), headerInput.getTimeScale())));
                toReturn.add(new TimeInterval(DateUtils.toJulianDate(datesWhenVisuInput.get(i), headerInput.getTimeScale()),
                        DateUtils.toJulianDate(datesWhenNotVisuInput.get(i), headerInput.getTimeScale())));
            }
        }
    }

    /**
     * This function aims at adding the last time interval to the list of time intervals already built.
     * Depending on the number of start and stop dates from the list of absolute dates, the time interval that will be added
     * will use either the last stop date or the last start date.
     *
     * @param datesWhenVisuInput    : The list of absolute date that contains all the start date of when satellites see each other.
     * @param datesWhenNotVisuInput : The list of absolute date that contains all the stop date of when satellites stop seeing each others.
     * @param toReturn              : The list of time intervals that will be returned with the last time interval added.
     * @param headerInput           : The header considered when several are used.
     */
    private void addTheLastTimeInterval(final List<AbsoluteDate> datesWhenVisuInput,
                                        final List<AbsoluteDate> datesWhenNotVisuInput,
                                        final List<TimeInterval> toReturn, final Header headerInput) {

        if (datesWhenVisuInput.size() > datesWhenNotVisuInput.size()) {

            final JulianDate finalDate = DateUtils.toJulianDate(datesWhenVisuInput.get(datesWhenVisuInput.size() - 1),
                    headerInput.getTimeScale());
            toReturn.add(new TimeInterval(finalDate, headerInput.getAvailability().getStop()));

        } else if (datesWhenVisuInput.size() < datesWhenNotVisuInput.size()) {

            final JulianDate finalDate = DateUtils.toJulianDate(
                    datesWhenNotVisuInput.get(datesWhenNotVisuInput.size() - 1), headerInput.getTimeScale());
            toReturn.add(new TimeInterval(finalDate, headerInput.getAvailability().getStop()));
        }
    }

    /**
     * Aims at building a list of Czml show that will write in the czml file when the satellites see each other.
     *
     * @param timeIntervalsInput : The list of time intervals, organized chronologically when the satellites see or not each other.
     * @param booleanListInput   : The list of boolean depicting if they see each other, alternatively true, false corresponding to the time intervals.
     * @return : A list of CzmlShow objects built to represent when satellites see or not each other.
     */
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

    /**
     * Aims at reorganizing the satellite list of list to form a list of satellite pairs.
     *
     * @param inputList : The list of list of satellite not organized by pair.
     * @return : A list of pairs of satellites.
     */
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

    /**
     * This function writes the pairs of satellites into a packet. It was made to work into a for function with the
     * given iteration number of the for loop.
     *
     * @param iterationNumber : The number of the iteration of the for loop.
     * @param stream          : The stream that converts all the strings into understandable string for the CzmlFile.
     * @param output          : The output stream of cesium that will contain the strings to write into the CzmLFile.
     * @param headerInput     : The header considered when several are used.
     */
    private void writePairOfSatellite(final int iterationNumber, final CesiumStreamWriter stream,
                                      final CesiumOutputStream output, final Header headerInput) {
        try (PacketCesiumWriter packet = stream.openPacket(output)) {
            final List<CzmlShow> currentShowList = showsList.get(iterationNumber);
            if (!(currentShowList == null)) {
                final Satellite currentFirstSatellite = pairsOfSatellites.get(iterationNumber)
                                                                         .get(0);
                final Satellite currentSecondSatellite = pairsOfSatellites.get(iterationNumber)
                                                                          .get(1);
                packet.writeId(DEFAULT_ID + currentFirstSatellite.getId() + "/" + currentSecondSatellite.getId());
                packet.writeName(
                        DEFAULT_CONSTELLATION_NAME + currentFirstSatellite.getName() + " " + currentSecondSatellite.getName());
                final TimeInterval minimumInterval = this.findMinimumAvailability(currentFirstSatellite,
                        currentSecondSatellite, headerInput);
                packet.writeAvailability(minimumInterval);

                final Iterable<Reference> currentReferences = referencesList.get(iterationNumber);
                final Polyline            currentPolyline   = polylines.get(iterationNumber);

                currentPolyline.writePolylineOfVisibility(packet, output, currentReferences, currentShowList);
            }
        }
    }

    /**
     * This class was designed to handle the inter-sat view between satellites of a constellation.
     * It will build a time span map of boolean that will group the time intervals and the boolean corresponding to the
     * inter-sat view.
     */
    private static class InterSatViewHandler implements EventHandler {

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

    /**
     * Aims at converting an array of references into an iterable of references.
     *
     * @param array : An array of references
     * @return : An iterable of references
     */
    private static Iterable<Reference> convertToIterable(final Reference[] array) {
        return () -> Arrays.stream(array)
                           .iterator();
    }
}
