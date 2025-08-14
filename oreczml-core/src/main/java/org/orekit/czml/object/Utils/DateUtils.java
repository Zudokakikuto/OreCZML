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
package org.orekit.czml.object.Utils;

import cesiumlanguagewriter.GregorianDate;
import cesiumlanguagewriter.JulianDate;
import cesiumlanguagewriter.TimeInterval;
import cesiumlanguagewriter.TimeStandard;
import org.hipparchus.util.FastMath;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScalesFactory;
import org.orekit.utils.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Date Utils class.
 * <p>
 * This class aims at giving utilities functions on dates to be used in the
 * code.
 * </p>
 *
 * @author Julien LEBLOND
 * @since 1.0.0
 */
public class DateUtils {

    private DateUtils() {
    }

    /**
     * Gives the delta of time between the Julian Epoch and the current absolute
     * date.
     *
     * @param date : The absolute date to get the delta from
     * @return : The delta of time between the Julian Epoch and the current
     *         absolute date.
     */
    public static double secondsSinceJulianEpoch(final AbsoluteDate date) {
        final double dt_s = date.durationFrom(AbsoluteDate.JULIAN_EPOCH);
        return dt_s / Constants.JULIAN_DAY;
    }

    /**
     * Determines the number of seconds since the last integer Julian date.
     *
     * @param date : The absolute date to convert
     * @return : The number of seconds since the last integer Julian date
     */
    public static double secondsInJulianDate(final AbsoluteDate date) {

        // Pull these as integers to avoid rounding error
        final int hour =
            date.getComponents(TimeScalesFactory.getTAI()).getTime().getHour();
        final int min =
            date.getComponents(TimeScalesFactory.getTAI()).getTime()
                .getMinute();

        // Number of seconds since the last full minute, including fractional
        // seconds
        final double sec =
            FastMath.floor(date.getComponents(TimeScalesFactory.getTAI())
                .getTime().getSecond());

        // UTC time starts at midnight, but JD epoch is at noon.
        return ((double) hour * 3600 + (double) min * 60 + sec + 43200.0) %
               86400.0;
    }

    // TODO: Asd a parameter to select if seconds should be rounded or not.

    /**
     * Converts an absolute date to a julian date.
     *
     * @param date : The absolute date to convert
     * @return : The julian date from the conversion
     */
    public static JulianDate toJulianDate(final AbsoluteDate date) {

        // Note: using "round" for now on assumption that output time will
        // always want to be to nearest second.
        final int numDays =
            (int) FastMath.floor(date.getJD(TimeScalesFactory.getTAI()));
        final double numSeconds = FastMath.round(secondsInJulianDate(date));
        return new JulianDate(numDays, numSeconds,
                              TimeStandard.INTERNATIONAL_ATOMIC_TIME);
    }

    /**
     * Converts a list of absolute date into a list of julian dates.
     *
     * @param absoluteDates : The list of absolute dates to convert
     * @return : A list of julian dates from the conversion
     */
    public static List<JulianDate>
        toJulianDates(final List<AbsoluteDate> absoluteDates) {
        final List<JulianDate> toReturn = new ArrayList<>();
        for (AbsoluteDate absoluteDate : absoluteDates) {
            toReturn.add(DateUtils.toJulianDate(absoluteDate));
        }
        return toReturn;
    }

    /**
     * Converts a list of time intervals into a list of julian dates.
     *
     * @param timeIntervals : The list of time intervals to convert.
     * @return : A list of julian dates from the conversion.
     */
    public static List<JulianDate>
        toJulianDateList(final List<TimeInterval> timeIntervals) {
        final List<JulianDate> toReturn = new ArrayList<>();
        for (TimeInterval timeInterval : timeIntervals) {
            toReturn.add(timeInterval.getStart());
        }
        toReturn.add(timeIntervals.get(timeIntervals.size() - 1).getStop());
        return toReturn;
    }

    /**
     * Add seconds for the start and the stop of a time interval to build
     * another one shifted by this duration.
     *
     * @param input : The time interval inputted
     * @param duration : The shift to add in seconds.
     * @return : A new time interval object shifted by the number of seconds
     *         given.
     */
    public static TimeInterval shitfedBy(final TimeInterval input,
                                         final double duration) {
        return new TimeInterval(input.getStart().addSeconds(duration),
                                input.getStop().addSeconds(duration));
    }

    /**
     * Converts a julian date to an absolute date, given the timescale.
     *
     * @param julianDate : The julian date to convert
     * @return : The absolute date from the conversion in the timescale.
     */
    public static AbsoluteDate toAbsoluteDate(final JulianDate julianDate) {
        return AbsoluteDate.createJDDate(julianDate.getDay(),
                                         julianDate.getSecondsOfDay(),
                                         TimeScalesFactory.getTAI());
    }

    /**
     * Convert a list of julian date into a list of absolute dates, given the
     * timescale.
     *
     * @param julianDates : The list of julian dates to convert
     * @return : A list of absolute date from the conversion
     */
    public static List<AbsoluteDate>
        toAbsoluteDates(final List<JulianDate> julianDates) {
        final List<AbsoluteDate> toReturn = new ArrayList<>();
        for (JulianDate julianDate : julianDates) {
            toReturn.add(DateUtils.toAbsoluteDate(julianDate));
        }
        return toReturn;
    }

    /**
     * Converts a list of julianDate to a list of TimeInterval.
     *
     * @param julianDates : The list of julian dates to convert, the list cannot
     *        contain only one element.
     * @return : A list of TimeInterval that correspond to each interval between
     *         each date.
     */
    public static List<TimeInterval>
        createTimeIntervals(final List<JulianDate> julianDates) {

        final List<TimeInterval> toReturn = new ArrayList<>();
        //
        final JulianDate firstJulianDate =
            new JulianDate(new GregorianDate(1900, 1, 1, 0, 0, 0.0));
        toReturn.add(new TimeInterval(firstJulianDate, julianDates.get(0)));

        for (int i = 0; i < julianDates.size() - 1; i++) {
            final JulianDate currentJulianDate = julianDates.get(i);
            final JulianDate nextJulianDate = julianDates.get(i + 1);
            final TimeInterval currentTimeInterval =
                new TimeInterval(currentJulianDate, nextJulianDate);
            toReturn.add(currentTimeInterval);
        }
        return toReturn;
    }
}
