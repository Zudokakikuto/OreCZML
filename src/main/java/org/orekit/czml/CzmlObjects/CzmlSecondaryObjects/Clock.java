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
package org.orekit.czml.CzmlObjects.CzmlSecondaryObjects;

import cesiumlanguagewriter.CesiumOutputStream;
import cesiumlanguagewriter.ClockCesiumWriter;
import cesiumlanguagewriter.ClockRange;
import cesiumlanguagewriter.ClockStep;
import cesiumlanguagewriter.GregorianDate;
import cesiumlanguagewriter.JulianDate;
import cesiumlanguagewriter.PacketCesiumWriter;
import cesiumlanguagewriter.TimeInterval;
import org.hipparchus.util.FastMath;
import org.orekit.files.ccsds.ndm.odm.oem.Oem;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.DateComponents;
import org.orekit.time.DateTimeComponents;
import org.orekit.time.TimeComponents;
import org.orekit.time.TimeScale;
import org.orekit.utils.TimeStampedPVCoordinates;

import java.util.List;

public class Clock implements CzmlSecondaryObject {

    /** . */
    private TimeInterval availability;

    /** . */
    private JulianDate currentTime;

    /** . */
    private double multiplier;

    /** . */
    private ClockRange range;

    /** . */
    private ClockStep step;

    /** . */
    private TimeScale timeScale;


    public Clock(final AbsoluteDate startDate, final AbsoluteDate stopDate, final TimeScale timeScale,
                 final double step) {
        this.step         = ClockStep.TICK_DEPENDENT;
        this.availability = new TimeInterval(this.getJulianDate(startDate, timeScale),
                this.getJulianDate(stopDate, timeScale));
        this.multiplier   = step;
        this.range        = ClockRange.LOOP_STOP;
        this.currentTime  = this.getJulianDate(startDate, timeScale);
        this.timeScale    = timeScale;
    }

    public Clock(final TimeInterval interval, final JulianDate currentTime, final double multiplier,
                 final ClockRange range, final ClockStep step) {
        this.availability = new TimeInterval(interval.getStart(), interval.getStop());
        this.currentTime  = currentTime;
        this.multiplier   = multiplier;
        this.range        = range;
        this.step         = step;
    }

    public Clock(final Oem oem) {
        this(oem, 60.0);
    }

    public Clock(final Oem oem, final double step) {
        final List<TimeStampedPVCoordinates> Ephemeris = oem.getSegments()
                                                            .get(0)
                                                            .getData()
                                                            .getEphemeridesDataLines();
        final int length = Ephemeris.size();
        final AbsoluteDate startTime = oem.getSegments()
                                          .get(0)
                                          .getStart();
        final AbsoluteDate stopTime = oem.getSegments()
                                         .get(0)
                                         .getStop();
        final int step_rounded = (int) FastMath.round(stopTime.durationFrom(startTime) / length);

        this.timeScale = oem.getDataContext()
                            .getTimeScales()
                            .getUTC();
        final JulianDate startJulianDate = this.getJulianDate(startTime, timeScale);
        final JulianDate stopJulianDate  = this.getJulianDate(stopTime, timeScale);

        final ClockStep clockStep       = ClockStep.getFromValue(step_rounded);
        final double    multiplier_temp = step;

        this.step         = clockStep;
        this.availability = new TimeInterval(startJulianDate, stopJulianDate);
        this.range        = ClockRange.LOOP_STOP;
        this.multiplier   = multiplier_temp;
        this.currentTime  = startJulianDate;
    }

    private JulianDate getJulianDate(final AbsoluteDate AbsDate, final TimeScale timeScaleInput) {
        final DateTimeComponents components     = AbsDate.getComponents(timeScaleInput);
        final DateComponents     dateComponents = components.getDate();
        final TimeComponents     timeComponents = components.getTime();
        final int                year           = dateComponents.getYear();
        final int                month          = dateComponents.getMonth();
        final int                day            = dateComponents.getDay();
        final int                hours          = timeComponents.getHour();
        final int                mins           = timeComponents.getMinute();
        final double             seconds        = timeComponents.getSecond();
        final GregorianDate      gregorianDate  = new GregorianDate(year, month, day, hours, mins, seconds);
        return new JulianDate(gregorianDate);
    }

    public JulianDate getCurrentTime() {
        return currentTime;
    }

    public TimeInterval getAvailability() {
        return availability;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public ClockRange getRange() {
        return range;
    }

    public ClockStep getStep() {
        return step;
    }

    public TimeScale getTimeScale() {
        return timeScale;
    }

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

}
