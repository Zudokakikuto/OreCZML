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

package org.orekit.czml.object.primary.covariance;

import cesiumlanguagewriter.CesiumOutputStream;
import cesiumlanguagewriter.CesiumStreamWriter;
import cesiumlanguagewriter.GregorianDate;
import cesiumlanguagewriter.JulianDate;
import cesiumlanguagewriter.TimeInterval;
import org.hipparchus.ode.events.Action;
import org.orekit.czml.errors.OresiumException;
import org.orekit.czml.errors.OresiumMessages;
import org.orekit.czml.object.primary.AbstractPrimaryObject;
import org.orekit.czml.object.primary.CzmlPrimaryObject;
import org.orekit.czml.object.primary.entities.Spacecraft;
import org.orekit.czml.object.utils.DateUtils;
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
import org.orekit.time.AbsoluteDate;
import org.orekit.utils.TimeSpanMap;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Collision class
 * <p>
 * This class aims at representing the probability of collision between two
 * satellites. In order to do so, the class uses the covariances of each
 * satellites and checks if there is an intersection of the covariance in time.
 *
 * @author Julien LEBLOND.
 * @since 1.0.0
 */
public class Collision
    extends
    AbstractPrimaryObject<Collision>
    implements
    CzmlPrimaryObject<Collision> {

    /**
     * The default id for the collision object.
     */
    public static final String DEFAULT_ID = "COLLISION/";

    /**
     * The default name for the collision object.
     */
    public static final String DEFAULT_NAME = "Collision between : ";

    /**
     * The covariance computed of the first satellite.
     */
    private Covariance covarianceFirstSatellite;

    /** The covariance computed of the second satellite. */
    private Covariance covarianceSecondSatellite;

    /** The first spacecraft. */
    private Spacecraft firstSpacecraft;

    /** The second satellite. */
    private Spacecraft secondSpacecraft;

    /** The list of initial states covariances of the first satellite. */
    private List<StateCovariance> firstCovarianceList;

    /** The list of initial states covariances of the second satellite. */
    private List<StateCovariance> secondCovarianceList;

    /** The local orbital frame of the first satellite. */
    private LOF firstLof;

    /** The local orbital frame of the second satellite. */
    private LOF secondLof;

    // Constructors

    /**
     * The constructor of the collision object.
     *
     * @param firstSatelliteInput : The first satellite considered in the
     *        collision probability.
     * @param secondSatelliteInput : The second satellite considered in the
     *        collision probability.
     * @param firstCovarianceListInput : The list of initial states covariances
     *        of the first satellite.
     * @param secondCovarianceListInput : The list of initial states covariances
     *        of the second satellite.
     * @param firstLofInput : The local orbital frame of the first satellite.
     * @param secondLofInput : The local orbital frame of the second satellite.
     */
    Collision(final Spacecraft firstSatelliteInput,
              final Spacecraft secondSatelliteInput,
              final List<StateCovariance> firstCovarianceListInput,
              final List<StateCovariance> secondCovarianceListInput,
              final LOF firstLofInput, final LOF secondLofInput) {
        this(firstSatelliteInput, secondSatelliteInput,
             firstCovarianceListInput, secondCovarianceListInput, firstLofInput,
             secondLofInput,
             DEFAULT_ID +
                             firstSatelliteInput.getId() + "/" +
                             secondSatelliteInput.getId());
    }

    /**
     * The constructor of the collision object with a custom ID.
     *
     * @param firstSatelliteInput : The first satellite considered in the
     *        collision probability.
     * @param secondSatelliteInput : The second satellite considered in the
     *        collision probability.
     * @param firstCovarianceListInput : The list of initial states covariances
     *        of the first satellite.
     * @param secondCovarianceListInput : The list of initial states covariances
     *        of the second satellite.
     * @param firstLofInput : The local orbital frame of the first satellite.
     * @param secondLofInput : The local orbital frame of the second satellite.
     * @param customID : The custom ID for the collision object.
     */
    Collision(final Spacecraft firstSatelliteInput,
              final Spacecraft secondSatelliteInput,
              final List<StateCovariance> firstCovarianceListInput,
              final List<StateCovariance> secondCovarianceListInput,
              final LOF firstLofInput, final LOF secondLofInput,
              final String customID) {

        this.setId(customID);
        this.setName(DEFAULT_NAME +
                     firstSatelliteInput.getName() + " and " +
                     secondSatelliteInput.getName());

        this.firstSpacecraft = firstSatelliteInput;
        this.secondSpacecraft = secondSatelliteInput;

        this.firstCovarianceList = firstCovarianceListInput;
        this.secondCovarianceList = secondCovarianceListInput;

        this.firstLof = firstLofInput;
        this.secondLof = secondLofInput;

        this.covarianceFirstSatellite =
            new Covariance(firstSatelliteInput, firstCovarianceListInput,
                           firstLofInput);
        this.covarianceSecondSatellite =
            new Covariance(secondSatelliteInput, secondCovarianceListInput,
                           secondLofInput);
        final BoundedPropagator propagatorFirstSat =
            firstSatelliteInput.getSpacecraftBoundedPropagator();
        final BoundedPropagator propagatorSecondSat =
            secondSatelliteInput.getSpacecraftBoundedPropagator();

        final TimeSpanMap<Boolean> visuMap = new TimeSpanMap<>(null);
        final EventDetector closeApproachDetector =
            collisionPropagation(propagatorFirstSat, propagatorSecondSat,
                                 visuMap);

        final List<TimeInterval> intervalsOfClosing =
            postPropagationProcessing(closeApproachDetector, visuMap,
                                      firstSatelliteInput);
        covarianceFirstSatellite.setAvailabilities(intervalsOfClosing);
        covarianceSecondSatellite.setAvailabilities(intervalsOfClosing);
    }

    /**
     * Builder collision builder.
     *
     * @param firstSatelliteInput the first satellite input
     * @param secondSatelliteInput the second satellite input
     * @param firstCovarianceListInput the first covariance list input
     * @param secondCovarianceListInput the second covariance list input
     * @param firstLofInput the first lof input
     * @param secondLofInput the second lof input
     * @return the collision builder
     */
    // Builder
    public static CollisionBuilder
        builder(final Spacecraft firstSatelliteInput,
                final Spacecraft secondSatelliteInput,
                final List<StateCovariance> firstCovarianceListInput,
                final List<StateCovariance> secondCovarianceListInput,
                final LOF firstLofInput, final LOF secondLofInput) {
        return new CollisionBuilder(firstSatelliteInput, secondSatelliteInput,
                                    firstCovarianceListInput,
                                    secondCovarianceListInput, firstLofInput,
                                    secondLofInput);
    }

    // Overrides

    @Override
    public void writeCzmlBlock(final CesiumStreamWriter stream,
                               final CesiumOutputStream output)
        throws URISyntaxException,
            IOException {
        covarianceFirstSatellite.writeCzmlBlock(stream, output);
        covarianceSecondSatellite.writeCzmlBlock(stream, output);
    }

    @Override
    public Collision cloneObject() {
        final Collision copy =
            Collision
                .builder(this.firstSpacecraft, this.secondSpacecraft,
                         this.firstCovarianceList, this.secondCovarianceList,
                         this.firstLof, this.secondLof)
                .build();
        copy.setAvailability(this.getAvailability());
        copy.setId(getId());
        copy.setName(getName());
        return copy;
    }

    // Getters

    public Covariance getCovarianceFirstSatellite() {
        return covarianceFirstSatellite;
    }

    public Covariance getCovarianceSecondSatellite() {
        return covarianceSecondSatellite;
    }

    public Spacecraft getFirstSpacecraft() {
        return firstSpacecraft;
    }

    public Spacecraft getSecondSpacecraft() {
        return secondSpacecraft;
    }

    public List<StateCovariance> getFirstCovarianceList() {
        return firstCovarianceList;
    }

    public List<StateCovariance> getSecondCovarianceList() {
        return secondCovarianceList;
    }

    public LOF getFirstLof() {
        return firstLof;
    }

    public LOF getSecondLof() {
        return secondLof;
    }

    // Setters

    public void
        setCovarianceFirstSatellite(final Covariance covarianceFirstSatellite) {
        this.covarianceFirstSatellite = covarianceFirstSatellite;
    }

    public void
        setCovarianceSecondSatellite(final Covariance covarianceSecondSatellite) {
        this.covarianceSecondSatellite = covarianceSecondSatellite;
    }

    public void setFirstSpacecraft(final Spacecraft firstSpacecraft) {
        this.firstSpacecraft = firstSpacecraft;
    }

    public void setSecondSpacecraft(final Spacecraft secondSpacecraft) {
        this.secondSpacecraft = secondSpacecraft;
    }

    public void
        setFirstCovarianceList(final List<StateCovariance> firstCovarianceList) {
        this.firstCovarianceList = firstCovarianceList;
    }

    public void
        setSecondCovarianceList(final List<StateCovariance> secondCovarianceList) {
        this.secondCovarianceList = secondCovarianceList;
    }

    public void setFirstLof(final LOF firstLof) {
        this.firstLof = firstLof;
    }

    public void setSecondLof(final LOF secondLof) {
        this.secondLof = secondLof;
    }

    // Private functions

    /**
     * This function aims at adding an event detector to the propagator of the
     * first sat to detect if the second satellite has a high probability of
     * collision with the first one.
     *
     * @param propagatorFirstSat : The propagator of the first satellite that
     *        will have an event detector added.
     * @param propagatorSecondSat : The propagator of the second satellite that
     *        will be detected.
     * @param visuMap : The time span map that will register all the time
     *        intervals when the satellites meet.
     * @return : The event detector that was added to the propagator. // TODO :
     *         When Orekit 14 is released, put the event detector on the
     *         EventSlopeFilter with .withHandler() [Oresium-80]
     */
    private EventDetector
        collisionPropagation(final BoundedPropagator propagatorFirstSat,
                             final Propagator propagatorSecondSat,
                             final TimeSpanMap<Boolean> visuMap) {

        final AbsoluteDate startDate = propagatorFirstSat.getMinDate();
        final AbsoluteDate stopDate = propagatorFirstSat.getMaxDate();
        final ExtremumApproachDetector extremumApproachDetector =
            new ExtremumApproachDetector(propagatorSecondSat)
                .withHandler((s, detector, increasing) -> {
                    visuMap.addValidAfter(true, s.getDate(), true);
                    return Action.CONTINUE;
                });
        final EventDetector closeApproachDetector =
            new EventSlopeFilter<>(extremumApproachDetector,
                                   FilterType.TRIGGER_ONLY_INCREASING_EVENTS);
        propagatorFirstSat.addEventDetector(closeApproachDetector);
        propagatorFirstSat.propagate(startDate, stopDate);
        return closeApproachDetector;
    }

    /**
     * This function aims at computing the time intervals when the covariance
     * will be displayed.
     *
     * @param approachDetector : The event detector added to the propagator
     * @param visuMap : The visu map that contains all the time intervals when
     *        the satellites meet.
     * @param firstSatelliteInput : The first satellite defined with the
     *        propagator where the event detector was added
     * @return : A list of time intervals that represents the intervals when the
     *         covariances will be displayed.
     */
    private List<TimeInterval>
        postPropagationProcessing(final EventDetector approachDetector,
                                  final TimeSpanMap<Boolean> visuMap,
                                  final Spacecraft firstSatelliteInput) {

        final List<TimeInterval> toReturn = new ArrayList<>();
        final SpacecraftState initialState =
            firstSatelliteInput.getSpacecraftPropagator().getInitialState();
        final JulianDate firstJulianDate =
            new JulianDate(new GregorianDate(1, 1, 1, 0, 0, 0.0));

        final double g = approachDetector.g(initialState);
        final boolean close;
        final boolean firstTimeIntervalFilled = false;
        close = g > 0;

        checkEmptyTimeSpanMap(visuMap);

        for (TimeSpanMap.Span<Boolean> span = visuMap.getFirstNonNullSpan();
             span != null; span = span.next()) {
            if (span.getData()) {
                if (!close) {
                    final TimeInterval currentTimeClosing =
                        spanToInterval(span);
                    toReturn.add(currentTimeClosing);
                } else {
                    final TimeInterval currentTimeClosing;
                    if (!firstTimeIntervalFilled) {
                        currentTimeClosing =
                            new TimeInterval(firstJulianDate, DateUtils
                                .toJulianDate(span.getEnd()));
                    } else {
                        currentTimeClosing = spanToInterval(span);
                    }
                    toReturn.add(currentTimeClosing);
                }
            }
        }
        return toReturn;
    }

    // TODO : Replace with visuMap.isEmpty() when available on Orekit.
    private static void
        checkEmptyTimeSpanMap(final TimeSpanMap<Boolean> visuMap)
            throws OresiumException {
        try {
            visuMap.getFirstNonNullSpan();
        } catch (OrekitException e) {
            throw new OresiumException(OresiumMessages.NOT_CLOSE_ENOUGH);
        }
    }

    /**
     * This function aims at converting a span into a time interval.
     *
     * @param span : A span from a time span map, here a boolean one.
     * @return : A time interval from the start to the end of the given span.
     */
    private TimeInterval spanToInterval(final TimeSpanMap.Span<Boolean> span) {
        return new TimeInterval(DateUtils.toJulianDate(span.getStart()),
                                DateUtils.toJulianDate(span.getEnd()));
    }
}
