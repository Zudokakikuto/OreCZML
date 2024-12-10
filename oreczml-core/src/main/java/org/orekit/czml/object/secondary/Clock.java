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

package org.orekit.czml.object.secondary;

import cesiumlanguagewriter.CesiumOutputStream;
import cesiumlanguagewriter.ClockCesiumWriter;
import cesiumlanguagewriter.ClockRange;
import cesiumlanguagewriter.ClockStep;
import cesiumlanguagewriter.JulianDate;
import cesiumlanguagewriter.PacketCesiumWriter;
import cesiumlanguagewriter.TimeInterval;
import org.hipparchus.util.FastMath;
import org.orekit.czml.object.Utils.DateUtils;
import org.orekit.files.ccsds.ndm.odm.oem.Oem;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScale;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Clock class.
 *
 * <p> This class aims at managing the time constants of the simulation.</p>
 *
 * @author Julien LEBLOND
 * @since 1.0.0
 */
public class Clock extends AbstractSecondaryObject {

    /**
     * The availability of the clock.
     */
    private final TimeInterval availability;

    /**
     * The current time.
     */
    private final JulianDate currentTime;

    /**
     * The multiplier, how many seconds between each step.
     */
    private final double multiplier;

    /**
     * The range of the clock: what should the simulation do when it is finished.
     */
    private final ClockRange range;

    /**
     * The step of the clock: how to manage the time between steps.
     */
    private final ClockStep step;

    /**
     * The timescale of the clock.
     */
    private TimeScale timeScale;

    /**
     * The list of all the julian dates of the simulation.
     */
    private List<JulianDate> JulianDateSimulation = new ArrayList<>();


    // Constructors

    /**
     * The basic constructor for the clock object, with default parameters.
     *
     * @param startDate  : The start date of the simulation
     * @param stopDate   : The stop date of the simulation
     * @param timeScale  : The timescale of the simulation
     * @param multiplier : Seconds between each step.
     */
    public Clock(final AbsoluteDate startDate, final AbsoluteDate stopDate, final TimeScale timeScale,
                 final double multiplier) {
        this.step = ClockStep.TICK_DEPENDENT;
        this.availability = new TimeInterval(DateUtils.toJulianDate(startDate, timeScale),
                DateUtils.toJulianDate(stopDate, timeScale));
        this.multiplier = multiplier;
        this.range = ClockRange.LOOP_STOP;
        this.currentTime = DateUtils.toJulianDate(startDate, timeScale);
        this.timeScale            = timeScale;
        this.JulianDateSimulation = computeJulianDates();
    }

    /**
     * The basic constructor for the clock object, with no default parameters.
     *
     * @param interval    : The start date of the simulation.
     * @param currentTime : The stop date of the simulation.
     * @param multiplier  : Seconds between each step.
     * @param range       : What should the simulation do when it is finished.
     * @param step        : How to manage the time between steps.
     */
    public Clock(final TimeInterval interval, final JulianDate currentTime, final double multiplier,
                 final ClockRange range, final ClockStep step) {
        this.availability = new TimeInterval(interval.getStart(), interval.getStop());
        this.currentTime = currentTime;
        this.multiplier = multiplier;
        this.range = range;
        this.step                 = step;
        this.JulianDateSimulation = computeJulianDates();
    }


    /**
     * The oem clock constructor. This constructor uses default parameters.
     *
     * @param oem : The Oem Orekit object.
     */
    public Clock(final Oem oem) {
        this(oem, 60.0);
    }

    /**
     * The oem clock constructor. This constructor doesn't use default parameters.
     *
     * @param oem        : The Oem Orekit object.
     * @param multiplier : Seconds between each step.
     */
    public Clock(final Oem oem, final double multiplier) {

        final AbsoluteDate startTime = oem.getSegments()
                                          .get(0)
                                          .getStart();
        final AbsoluteDate stopTime = oem.getSegments()
                                         .get(0)
                                         .getStop();

        this.timeScale = oem.getDataContext()
                            .getTimeScales()
                            .getUTC();

        final JulianDate startJulianDate = DateUtils.toJulianDate(startTime, timeScale);
        final JulianDate stopJulianDate  = DateUtils.toJulianDate(stopTime, timeScale);

        this.step = ClockStep.SYSTEM_CLOCK_MULTIPLIER;
        this.availability = new TimeInterval(startJulianDate, stopJulianDate);
        this.range = ClockRange.LOOP_STOP;
        this.multiplier = multiplier;
        this.currentTime          = startJulianDate;
        this.JulianDateSimulation = computeJulianDates();
    }


    // Overrides

    @Override
    public void write(final PacketCesiumWriter packetWriter, final CesiumOutputStream output) {

        final ClockCesiumWriter writer = packetWriter.getClockWriter();
        writer.open(output);
        writer.writeInterval(availability);
        writer.writeCurrentTime(currentTime);
        writer.writeMultiplier(multiplier);
        writer.writeRange(range);
        writer.writeStep(step);
    }


    // Getters

    /**
     * Gets current time.
     *
     * @return the current time
     */
    public JulianDate getCurrentTime() {
        return currentTime;
    }

    /**
     * Gets availability.
     *
     * @return the availability
     */
    public TimeInterval getAvailability() {
        return availability;
    }

    /**
     * Gets multiplier.
     *
     * @return the multiplier
     */
    public double getMultiplier() {
        return multiplier;
    }

    /**
     * Gets range.
     *
     * @return the range
     */
    public ClockRange getRange() {
        return range;
    }

    /**
     * Gets step.
     *
     * @return the step
     */
    public ClockStep getStep() {
        return step;
    }

    /**
     * Gets time scale.
     *
     * @return the time scale
     */
    public TimeScale getTimeScale() {
        return timeScale;
    }

    /**
     * Gets julian dates simulation.
     *
     * @return the julian dates simulation
     */
    public List<JulianDate> getJulianDatesSimulation() {
        return Collections.unmodifiableList(JulianDateSimulation);
    }


    // Private functions

    /**
     * This function aims at computing all the julian dates of the clock to store them.
     *
     * @return : The list of all the julian dates of the simulation.
     */
    private List<JulianDate> computeJulianDates() {
        final List<JulianDate> toReturn                    = new ArrayList<>();
        final JulianDate       startDate                   = availability.getStart();
        final JulianDate       stopDate                    = availability.getStop();
        final double           totalSeconds                = startDate.secondsDifference(stopDate);
        final double           numberOfIterationNotRounded = totalSeconds / multiplier;
        final int              numberOfIteration           = (int) FastMath.round(numberOfIterationNotRounded);
        for (int i = 0; i < numberOfIteration; i++) {
            toReturn.add(startDate.addSeconds(multiplier * i));
        }
        return toReturn;
    }
}
