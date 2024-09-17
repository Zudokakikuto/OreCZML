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
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScale;
import org.orekit.utils.Constants;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * CZML Primary Object Interface
 *
 * <p>
 * This interface represents the objects that are directly displayed on screen,they depends to no other objects except the header object.
 * </p>
 *
 * @author Julien LEBLOND
 * @since 1.0.0
 */

public interface CzmlPrimaryObject {

    /** The string writer that allows to write inside the CzmLFile. */
    StringWriter STRING_WRITER = new StringWriter();

    /** The output stream of cesium that will contain the strings to write into the CzmLFile. */
    CesiumOutputStream OUTPUT = new CesiumOutputStream(STRING_WRITER);

    /** The stream that converts all the strings into understandable string for the CzmlFile. */
    CesiumStreamWriter STREAM = new CesiumStreamWriter();


    // Overrides methods

    /** The classic method that writes the object into the CzmlFile. */
    void writeCzmlBlock() throws URISyntaxException, IOException;

    /**
     * The classic method that returns the string writer of the object.
     *
     * @return : The string writer of the object
     */
    StringWriter getStringWriter();

    /** The classic method that cleans all the arguments of the object. */
    void cleanObject();


    // Usable methods

    /**
     * Gives the delta of time between the Julian Epoch and the current absolute date.
     *
     * @param date : The absolute date to get the delta from
     * @return : The delta of time between the Julian Epoch and the current absolute date.
     */
    default double absoluteDateToJulianDateOriginDelta(AbsoluteDate date) {
        final double dt_s = date.durationFrom(AbsoluteDate.JULIAN_EPOCH);
        return dt_s / Constants.JULIAN_DAY;
    }

    /**
     * Converts an absolute date to a julian date.
     *
     * @param date      : The absolute date to convert
     * @param timeScale : The timescale in which the absolute date is expressed
     * @return : The julian date from the conversion
     */
    default JulianDate absoluteDateToJulianDate(AbsoluteDate date, TimeScale timeScale) {

        final int year = date.getComponents(timeScale)
                             .getDate()
                             .getYear();
        final int month = date.getComponents(timeScale)
                              .getDate()
                              .getMonth();
        final int day = date.getComponents(timeScale)
                            .getDate()
                            .getDay();
        final int hour = date.getComponents(timeScale)
                             .getTime()
                             .getHour();
        final int min = date.getComponents(timeScale)
                            .getTime()
                            .getMinute();
        final double sec = date.getComponents(timeScale)
                               .getTime()
                               .getSecond();

        final GregorianDate gregorianDate = new GregorianDate(year, month, day, hour, min, sec);
        return new JulianDate(gregorianDate);
    }

    /**
     * Converts a list of absolute date into a list of julian dates.
     *
     * @param timeScale     : The timescale in which the absolute dates are expressed
     * @param absoluteDates : The list of absolute dates to convert
     * @return : A list of julian dates from the conversion
     */
    default List<JulianDate> absoluteDatelistToJulianDateList(List<AbsoluteDate> absoluteDates, TimeScale timeScale) {
        final List<JulianDate> toReturn = new ArrayList<>();
        for (AbsoluteDate absoluteDate : absoluteDates) {
            toReturn.add(absoluteDateToJulianDate(absoluteDate, timeScale));
        }
        return toReturn;
    }

    /**
     * Converts a julian date to an absolute date, given the timescale.
     *
     * @param julianDate : The julian date to convert
     * @param timeScale  : The timescale in which the absolute date must be expressed.
     * @return : The absolute date from the conversion in the timescale.
     */
    default AbsoluteDate julianDateToAbsoluteDate(JulianDate julianDate, TimeScale timeScale) {
        return AbsoluteDate.createJDDate(julianDate.getDay(), julianDate.getSecondsOfDay(), timeScale);
    }

    /**
     * Convert a list of julian date into a list a absolute date, given the timescale.
     *
     * @param julianDates : The list of julian dates to convert
     * @param timeScale   : The timescale in which the absolute date will be expressed
     * @return : A list of absolute date from the conversion
     */
    default List<AbsoluteDate> julianDateListToAbsoluteDateList(List<JulianDate> julianDates, TimeScale timeScale) {
        final List<AbsoluteDate> toReturn = new ArrayList<>();
        for (JulianDate julianDate : julianDates) {
            toReturn.add(julianDateToAbsoluteDate(julianDate, timeScale));
        }
        return toReturn;
    }

    /**
     * Converts a list of julianDate to a list of TimeInterval.
     *
     * @param julianDates : The list of julian dates to convert, the list cannot contain only one element.
     * @return : A list of TimeInterval that correspond to each interval between each date.
     */
    default List<TimeInterval> julienDateListToTimeIntervals(List<JulianDate> julianDates) {

        final List<TimeInterval> toReturn = new ArrayList<>();
        //
        final JulianDate firstJulianDate = new JulianDate(new GregorianDate(1900, 1, 1, 0, 0, 0.0));
        toReturn.add(new TimeInterval(firstJulianDate, julianDates.get(0)));

        for (int i = 0; i < julianDates.size() - 1; i++) {
            final JulianDate   currentJulianDate   = julianDates.get(i);
            final JulianDate   nextJulianDate      = julianDates.get(i + 1);
            final TimeInterval currentTimeInterval = new TimeInterval(currentJulianDate, nextJulianDate);
            toReturn.add(currentTimeInterval);
        }
        return toReturn;
    }

    /**
     * This method aims at reorganizing the list of list of objects by inverting indexes.
     *
     * @param objects : A list of objects.
     * @param <T>     : An object to be sorted.
     * @return A sorted list of objects.
     */
    default <T> List<List<T>> sortingListList(final List<List<T>> objects) {
        final List<List<T>> toReturn = new ArrayList<>();
        for (int i = 0; i < objects.get(0)
                                   .size(); i++) {
            final List<T> sortedList = new ArrayList<>();
            for (List<T> object : objects) {
                final T objectsToSort = object
                        .get(i);
                sortedList.add(objectsToSort);
            }
            toReturn.add(sortedList);
        }
        return toReturn;
    }

    /**
     * This method aims at reorganizing the list of list of objects by inverting first and third indexes.
     *
     * @param objects : A list of objects.
     * @param <T>     : An object to be sorted.
     * @return A sorted list of objects.
     */
    default <T> List<List<List<T>>> sortingListListList(final List<List<List<T>>> objects) {
        final List<List<List<T>>> toReturn = new ArrayList<>();
        for (int i = 0; i < objects.get(0)
                                   .get(0)
                                   .size(); i++) {
            final List<List<T>> tempListList = new ArrayList<>();
            for (int j = 0; j < objects.get(0)
                                       .size(); j++) {
                final List<T> tempList = new ArrayList<>();
                for (List<List<T>> object : objects) {
                    final T objectToSort = object
                            .get(j)
                            .get(i);
                    tempList.add(objectToSort);
                }
                tempListList.add(tempList);
            }
            toReturn.add(tempListList);
        }
        return toReturn;
    }
}
