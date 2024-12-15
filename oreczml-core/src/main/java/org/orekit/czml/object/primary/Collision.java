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
import cesiumlanguagewriter.GregorianDate;
import cesiumlanguagewriter.JulianDate;
import cesiumlanguagewriter.TimeInterval;
import org.hipparchus.ode.events.Action;
import org.orekit.czml.archi.builder.CollisionBuilder;
import org.orekit.czml.errors.OreCzmlException;
import org.orekit.czml.errors.OreCzmlMessages;
import org.orekit.czml.object.Utils.DateUtils;
import org.orekit.czml.object.primary.entities.Satellite;
import org.orekit.errors.OrekitException;
import org.orekit.frames.LOF;
import org.orekit.propagation.BoundedPropagator;
import org.orekit.propagation.Propagator;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.StateCovariance;
import org.orekit.propagation.events.EventDetector;
import org.orekit.propagation.events.EventSlopeFilter;
import org.orekit.propagation.events.ExtremumApproachDetector;
import org.orekit.propagation.events.FilterType;
import org.orekit.propagation.events.handlers.ContinueOnEvent;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScale;
import org.orekit.utils.TimeSpanMap;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;


/**
 * Collision class
 *
 * <p>
 * This class aims at representing the probability of collision between two satellites. In order to do so, the class uses
 * the covariances of each satellites and checks if there is an intersection of the covariance in time.
 *
 * @author Julien LEBLOND.
 * @since 1.0.0
 */
public class Collision extends AbstractPrimaryObject {

    /**
     * The default id for the collision object.
     */
    public static final String DEFAULT_ID = "COLLISION/";

    /**
     * The default name for the collision object.
     */
    public static final String DEFAULT_NAME = "Collision between : ";

    /**
     * The first satellite.
     */
    private final Satellite firstSatellite;

    /**
     * The second satellite.
     */
    private final Satellite secondSatellite;

    /**
     * The covariance computed of the first satellite.
     */
    private final Covariance covarianceFirstSatellite;

    /**
     * The covariance computed of the second satellite.
     */
    private final Covariance covarianceSecondSatellite;


    // Constructors

    /**
     * The constructor of the collision object.
     *
     * @param firstSatelliteInput  : The first satellite considered in the collision probability.
     * @param secondSatelliteInput : The second satellite considered in the collision probability.
     * @param firstCovarianceList  : The list of initial states covariances of the first satellite.
     * @param secondCovarianceList : The list of initial states covariances of the second satellite.
     * @param firstLof             : The local orbital frame of the first satellite.
     * @param secondLof            : The local orbital frame of the second satellite.
     * @param header               : The header considered.
     */
    public Collision(final Satellite firstSatelliteInput, final Satellite secondSatelliteInput,
                     final List<StateCovariance> firstCovarianceList,
                     final List<StateCovariance> secondCovarianceList, final LOF firstLof,
                     final LOF secondLof, final Header header) {
        this(firstSatelliteInput, secondSatelliteInput, firstCovarianceList, secondCovarianceList, firstLof, secondLof,
                DEFAULT_ID + firstSatelliteInput.getId() + "/" + secondSatelliteInput.getId(), header);
    }

    /**
     * The constructor of the collision object with a custom ID.
     *
     * @param firstSatelliteInput  : The first satellite considered in the collision probability.
     * @param secondSatelliteInput : The second satellite considered in the collision probability.
     * @param firstCovarianceList  : The list of initial states covariances of the first satellite.
     * @param secondCovarianceList : The list of initial states covariances of the second satellite.
     * @param firstLof             : The local orbital frame of the first satellite.
     * @param secondLof            : The local orbital frame of the second satellite.
     * @param customID             : The custom ID for the collision object.
     * @param header               : The header to set up if several headers are used.
     */
    public Collision(final Satellite firstSatelliteInput, final Satellite secondSatelliteInput,
                     final List<StateCovariance> firstCovarianceList,
                     final List<StateCovariance> secondCovarianceList, final LOF firstLof,
                     final LOF secondLof, final String customID, final Header header) {

        this.setId(customID);
        this.setName(DEFAULT_NAME + firstSatelliteInput.getName() + " and " + secondSatelliteInput.getName());
        this.firstSatellite            = firstSatelliteInput;
        this.secondSatellite           = secondSatelliteInput;
        this.covarianceFirstSatellite  = new Covariance(firstSatelliteInput, firstCovarianceList,
                firstLof, header);
        this.covarianceSecondSatellite = new Covariance(secondSatelliteInput, secondCovarianceList,
                secondLof, header);
        final BoundedPropagator propagatorFirstSat  = firstSatelliteInput.getSatelliteBoundedPropagator();
        final BoundedPropagator propagatorSecondSat = secondSatelliteInput.getSatelliteBoundedPropagator();

        final TimeSpanMap<Boolean> visuMap = new TimeSpanMap<>(null);
        final EventDetector closeApproachDetector = collisionPropagation(propagatorFirstSat, propagatorSecondSat,
                visuMap);

        final List<TimeInterval> intervalsOfClosing = postPropagationProcessing(closeApproachDetector, visuMap,
                firstSatelliteInput, header);
        covarianceFirstSatellite.setAvailabilities(intervalsOfClosing);
        covarianceSecondSatellite.setAvailabilities(intervalsOfClosing);
    }

    /**
     * Builder collision builder.
     *
     * @param firstSatelliteInput       the first satellite input
     * @param secondSatelliteInput      the second satellite input
     * @param firstCovarianceListInput  the first covariance list input
     * @param secondCovarianceListInput the second covariance list input
     * @param firstLofInput             the first lof input
     * @param secondLofInput            the second lof input
     * @param header                    the header
     * @return the collision builder
     */
// Builder
    public static CollisionBuilder builder(final Satellite firstSatelliteInput,
                                           final Satellite secondSatelliteInput,
                                           final List<StateCovariance> firstCovarianceListInput,
                                           final List<StateCovariance> secondCovarianceListInput,
                                           final LOF firstLofInput,
                                           final LOF secondLofInput, final Header header) {
        return new CollisionBuilder(firstSatelliteInput, secondSatelliteInput, firstCovarianceListInput,
                secondCovarianceListInput, firstLofInput, secondLofInput, header);
    }

    // Overrides

    @Override
    public void writeCzmlBlock(final CesiumStreamWriter stream,
                               final CesiumOutputStream output) throws URISyntaxException, IOException {
        covarianceFirstSatellite.writeCzmlBlock(stream, output);
        covarianceSecondSatellite.writeCzmlBlock(stream, output);
    }


    // GETTERS

    /**
     * Gets first satellite.
     *
     * @return the first satellite
     */
    public Satellite getFirstSatellite() {
        return firstSatellite;
    }

    /**
     * Gets second satellite.
     *
     * @return the second satellite
     */
    public Satellite getSecondSatellite() {
        return secondSatellite;
    }

    /**
     * Gets covariance display first satellite.
     *
     * @return the covariance display first satellite
     */
    public Covariance getCovarianceDisplayFirstSatellite() {
        return covarianceFirstSatellite;
    }

    /**
     * Gets covariance display second satellite.
     *
     * @return the covariance display second satellite
     */
    public Covariance getCovarianceDisplaySecondSatellite() {
        return covarianceSecondSatellite;
    }


    // Private functions

    /**
     * This function aims at adding an event detector to the propagator of the first sat to detect if the second satellite
     * has a high probability of collision with the first one.
     *
     * @param propagatorFirstSat  : The propagator of the first satellite that will have an event detector added.
     * @param propagatorSecondSat : The propagator of the second satellite that will be detected.
     * @param visuMap             : The time span map that will register all the time intervals when the satellites meet.
     * @return : The event detector that was added to the propagator.
     */
    private EventDetector collisionPropagation(final BoundedPropagator propagatorFirstSat,
                                               final Propagator propagatorSecondSat,
                                               final TimeSpanMap<Boolean> visuMap) {

        final AbsoluteDate startDate = propagatorFirstSat.getMinDate();
        final AbsoluteDate stopDate  = propagatorFirstSat.getMaxDate();
        final ExtremumApproachDetector extremumApproachDetector = new ExtremumApproachDetector(
                propagatorSecondSat).withHandler(new ContinueOnEvent());
        final EventDetector closeApproachDetector = new EventSlopeFilter<>(extremumApproachDetector,
                FilterType.TRIGGER_ONLY_INCREASING_EVENTS)
                .withHandler((s, detector, increasing) -> {
                    if (increasing) {
                        visuMap.addValidAfter(true, s.getDate(), true);
                    }
                    if (!increasing) {
                        visuMap.addValidAfter(false, s.getDate(), true);
                    }
                    return Action.CONTINUE;
                });
        propagatorFirstSat.addEventDetector(closeApproachDetector);
        propagatorFirstSat.propagate(startDate, stopDate);
        return closeApproachDetector;
    }

    /**
     * This function aims at computing the time intervals when the covariance will be displayed.
     *
     * @param approachDetector    : The event detector added to the propagator
     * @param visuMap             : The visu map that contains all the time intervals when the satellites meet.
     * @param firstSatelliteInput : The first satellite defined with the propagator where the event detector was added
     * @param header              : The header to use if several are used.
     * @return : A list of time intervals that represents the intervals when the covariances will be displayed.
     */
    private List<TimeInterval> postPropagationProcessing(final EventDetector approachDetector,
                                                         final TimeSpanMap<Boolean> visuMap,
                                                         final Satellite firstSatelliteInput, final Header header) {

        final List<TimeInterval> toReturn = new ArrayList<>();
        final SpacecraftState initialState = firstSatelliteInput.getSatellitePropagator()
                                                                .getInitialState();
        final JulianDate firstJulianDate = new JulianDate(new GregorianDate(1, 1, 1, 0, 0, 0.0));

        final double  g                       = approachDetector.g(initialState);
        final boolean close;
        final boolean firstTimeIntervalFilled = false;
        close = g > 0;

        checkEmptyTimeSpanMap(visuMap);

        for (TimeSpanMap.Span<Boolean> span = visuMap.getFirstNonNullSpan(); span != null; span = span.next()) {
            if (span.getData()) {
                if (!close) {
                    final TimeInterval currentTimeClosing = spanToInterval(span, header.getTimeScale());
                    toReturn.add(currentTimeClosing);
                } else {
                    final TimeInterval currentTimeClosing;
                    if (!firstTimeIntervalFilled) {
                        currentTimeClosing = new TimeInterval(firstJulianDate,
                                DateUtils.toJulianDate(span.getEnd(), header.getTimeScale()));
                    } else {
                        currentTimeClosing = spanToInterval(span, header.getTimeScale());
                    }
                    toReturn.add(currentTimeClosing);
                }
            }
        }
        return toReturn;
    }

    // TODO : Replace with visuMap.isEmpty() when available on Orekit.
    private static void checkEmptyTimeSpanMap(final TimeSpanMap<Boolean> visuMap) throws OreCzmlException {
        try {
            visuMap.getFirstNonNullSpan();
        } catch (OrekitException e) {
            throw new OreCzmlException(OreCzmlMessages.NOT_CLOSE_ENOUGH);
        }
    }


    /**
     * This function aims at converting a span into a time interval.
     *
     * @param span      : A span from a time span map, here a boolean one.
     * @param timeScale : The time scale to use.
     * @return : A time interval from the start to the end of the given span.
     */
    private TimeInterval spanToInterval(final TimeSpanMap.Span<Boolean> span, final TimeScale timeScale) {
        return new TimeInterval(
                DateUtils.toJulianDate(span.getStart(), timeScale),
                DateUtils.toJulianDate(span.getEnd(), timeScale));
    }
}
