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
import cesiumlanguagewriter.TimeInterval;
import org.hipparchus.ode.events.Action;
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
import org.orekit.utils.TimeSpanMap;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;


/**
 * Collision display class
 *
 * <p>
 * This class aims at representing the probability of collision between two satellites. In order to do so, the class uses
 * the covariances of each satellites and checks if there is an intersection of the coraviance in time.
 * </p>
 *
 * @author Julien LEBLOND.
 * @since 1.0.0
 */
public class CollisionDisplay extends AbstractPrimaryObject implements CzmlPrimaryObject {

    /** The default id for the collision display object. */
    public static final String DEFAULT_ID = "COLLISION/";

    /** The default name for the collision display object. */
    public static final String DEFAULT_NAME = "Collision display between : ";

    /** The first satellite. */
    private Satellite firstSatellite;

    /** The second satellite. */
    private Satellite secondSatellite;

    /** The covariance display computed of the first satellite. */
    private CovarianceDisplay covarianceDisplayFirstSatellite;

    /** The covariance display computed of the second satellite. */
    private CovarianceDisplay covarianceDisplaySecondSatellite;

    /** The list of all the timeIntervals when the satellites are close. */
    private List<TimeInterval> intervalsOfClosing = new ArrayList<>();


    // Constructors

    /**
     * The constructor of the collision display object.
     *
     * @param firstSatelliteInput  : The first satellite considered in the collision probability.
     * @param secondSatelliteInput : The second satellite considered in the collision probability.
     * @param firstCovarianceList  : The list of initial states covariances of the first satellite.
     * @param secondCovarianceList : The list of initial states covariances of the second satellite.
     * @param firstLof             : The local orbital frame of the first satellite.
     * @param secondLof            : The local orbital frame of the second satellite.
     */
    public CollisionDisplay(final Satellite firstSatelliteInput, final Satellite secondSatelliteInput,
                            final List<StateCovariance> firstCovarianceList,
                            final List<StateCovariance> secondCovarianceList, final LOF firstLof,
                            final LOF secondLof) {

        this.setId(DEFAULT_ID + firstSatelliteInput.getId() + "/" + secondSatelliteInput.getId());
        this.setName(DEFAULT_NAME + firstSatelliteInput.getName() + " and " + secondSatelliteInput.getName());
        this.firstSatellite                   = firstSatelliteInput;
        this.secondSatellite                  = secondSatelliteInput;
        this.covarianceDisplayFirstSatellite  = new CovarianceDisplay(firstSatelliteInput, firstCovarianceList,
                firstLof);
        this.covarianceDisplaySecondSatellite = new CovarianceDisplay(secondSatelliteInput, secondCovarianceList,
                secondLof);
        final BoundedPropagator propagatorFirstSat  = firstSatelliteInput.getSatelliteBoundedPropagator();
        final BoundedPropagator propagatorSecondSat = secondSatelliteInput.getSatelliteBoundedPropagator();

        final TimeSpanMap<Boolean> visuMap = new TimeSpanMap<>(null);
        final EventDetector closeApproachDetector = collisionPropagation(propagatorFirstSat, propagatorSecondSat,
                visuMap);
        this.intervalsOfClosing = postPropagationProcessing(closeApproachDetector, visuMap, firstSatelliteInput);
        covarianceDisplayFirstSatellite.setAvailabilities(intervalsOfClosing);
        covarianceDisplaySecondSatellite.setAvailabilities(intervalsOfClosing);
    }


    // Overrides

    @Override
    public void writeCzmlBlock() throws URISyntaxException, IOException {
        covarianceDisplayFirstSatellite.writeCzmlBlock();
        covarianceDisplaySecondSatellite.writeCzmlBlock();
    }

    @Override
    public StringWriter getStringWriter() {
        return STRING_WRITER;
    }

    @Override
    public void cleanObject() {
        this.firstSatellite                   = null;
        this.secondSatellite                  = null;
        this.covarianceDisplayFirstSatellite  = null;
        this.covarianceDisplaySecondSatellite = null;
    }


    // GETTERS

    public Satellite getFirstSatellite() {
        return firstSatellite;
    }

    public Satellite getSecondSatellite() {
        return secondSatellite;
    }

    public CovarianceDisplay getCovarianceDisplayFirstSatellite() {
        return covarianceDisplayFirstSatellite;
    }

    public CovarianceDisplay getCovarianceDisplaySecondSatellite() {
        return covarianceDisplaySecondSatellite;
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
     * @return : A list of time intervals that represents the intervals when the covariances will be displayed.
     */
    private List<TimeInterval> postPropagationProcessing(final EventDetector approachDetector,
                                                         final TimeSpanMap<Boolean> visuMap,
                                                         final Satellite firstSatelliteInput) {
        final List<TimeInterval> toReturn = new ArrayList<>();
        final SpacecraftState initialState = firstSatelliteInput.getSatellitePropagator()
                                                                .getInitialState();
        final JulianDate firstJulianDate = new JulianDate(new GregorianDate(1, 1, 1, 0, 0, 0.0));

        final double  g                       = approachDetector.g(initialState);
        final boolean close;
        final boolean firstTimeIntervalFilled = false;
        close = g > 0;

        for (TimeSpanMap.Span<Boolean> span = visuMap.getFirstNonNullSpan(); span != null; span = span.next()) {
            if (span.getData()) {
                if (!close) {
                    final TimeInterval currentTimeClosing = spanToInterval(span);
                    toReturn.add(currentTimeClosing);
                } else {
                    final TimeInterval currentTimeClosing;
                    if (!firstTimeIntervalFilled) {
                        currentTimeClosing = new TimeInterval(firstJulianDate,
                                absoluteDateToJulianDate(span.getEnd(), Header.getTimeScale()));
                    } else {
                        currentTimeClosing = spanToInterval(span);
                    }
                    toReturn.add(currentTimeClosing);
                }
            }
        }
        return toReturn;
    }


    /**
     * This function aims at converting a span into a time interval.
     *
     * @param span : A span from a time span map, here a boolean one.
     * @return : A time interval from the start to the end of the given span.
     */
    private TimeInterval spanToInterval(final TimeSpanMap.Span<Boolean> span) {
        return new TimeInterval(
                absoluteDateToJulianDate(span.getStart(), Header.getTimeScale()),
                absoluteDateToJulianDate(span.getEnd(), Header.getTimeScale()));
    }
}
