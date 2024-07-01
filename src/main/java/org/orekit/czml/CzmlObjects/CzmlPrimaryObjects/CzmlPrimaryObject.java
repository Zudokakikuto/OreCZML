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

import cesiumlanguagewriter.CesiumOutputStream;
import cesiumlanguagewriter.CesiumStreamWriter;
import cesiumlanguagewriter.GregorianDate;
import cesiumlanguagewriter.JulianDate;
import cesiumlanguagewriter.TimeInterval;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScale;
import org.orekit.time.TimeScalesFactory;
import org.orekit.utils.Constants;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/** CZMLPrimaryObject

 * <p>
 *    This interface represents the objects that are directly displayed on screen,they depends to no other objects except the header object.
 * </p>
 *
 * @since 1.0
 * @author Julien LEBLOND
 */
public interface CzmlPrimaryObject {

    /** .*/
    StringWriter STRING_WRITER = new StringWriter();
    /** .*/
    CesiumOutputStream OUTPUT = new CesiumOutputStream(STRING_WRITER);
    /** .*/
    CesiumStreamWriter STREAM = new CesiumStreamWriter();

    default double absoluteDateToJulianDateDelta(AbsoluteDate date) {
        final double dt_s = date.durationFrom(AbsoluteDate.JULIAN_EPOCH);
        return dt_s / Constants.JULIAN_DAY;
    }

    default JulianDate absoluteDateToJulianDate(AbsoluteDate date) {

        final TimeScale UTC = TimeScalesFactory.getUTC();
        final int year = date.getComponents(UTC).getDate().getYear();
        final int month = date.getComponents(UTC).getDate().getMonth();
        final int day = date.getComponents(UTC).getDate().getDay();
        final int hour = date.getComponents(UTC).getTime().getHour();
        final int min = date.getComponents(UTC).getTime().getMinute();
        final double sec = date.getComponents(UTC).getTime().getSecond();

        final GregorianDate gregorianDate = new GregorianDate(year, month, day, hour, min, sec);
        return new JulianDate(gregorianDate);
    }

    default List<JulianDate> absoluteDatelistToJulianDateList(List<AbsoluteDate> absoluteDates) {
        final List<JulianDate> toReturn = new ArrayList<>();
        for (AbsoluteDate absoluteDate : absoluteDates) {
            toReturn.add(absoluteDateToJulianDate(absoluteDate));
        }
        return toReturn;
    }

    default AbsoluteDate julianDateToAbsoluteDate(JulianDate julianDate, TimeScale timeScale) {
        return AbsoluteDate.createJDDate(julianDate.getDay(), julianDate.getSecondsOfDay(), timeScale);
    }

    default List<AbsoluteDate> julianDateListToAbsoluteDateList(List<JulianDate> julianDates, TimeScale timeScale) {
        final List<AbsoluteDate> toReturn = new ArrayList<>();
        for (JulianDate julianDate : julianDates) {
            toReturn.add(julianDateToAbsoluteDate(julianDate, timeScale));
        }
        return toReturn;
    }

    default List<TimeInterval> julienDateListToTimeIntervals(List<JulianDate> julianDates) {

        final List<TimeInterval> toReturn = new ArrayList<>();
        //
        final JulianDate firstJulianDate = new JulianDate(new GregorianDate(1900, 1, 1, 0, 0, 0.0));
        toReturn.add(new TimeInterval(firstJulianDate, julianDates.get(0)));

        for (int i = 0; i < julianDates.size() - 1; i++) {
            final JulianDate currentJulianDate = julianDates.get(i);
            final JulianDate nextJulianDate = julianDates.get(i + 1);
            final TimeInterval currentTimeInterval = new TimeInterval(currentJulianDate, nextJulianDate);
            toReturn.add(currentTimeInterval);
        }
        return toReturn;
    }

    void writeCzmlBlock() throws URISyntaxException, IOException;
    StringWriter getStringWriter();
    void cleanObject();
}
