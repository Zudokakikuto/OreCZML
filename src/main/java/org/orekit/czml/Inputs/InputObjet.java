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
package org.orekit.czml.Inputs;

import cesiumlanguagewriter.GregorianDate;
import cesiumlanguagewriter.JulianDate;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.Header;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.DateComponents;
import org.orekit.time.DateTimeComponents;
import org.orekit.time.TimeComponents;
import org.orekit.time.TimeScale;
import org.orekit.time.TimeScalesFactory;

/** Input Object

 * <p>
 * This interface aims at giving a common base for all possible input objects that are interfacing with OreCzml.
 * </p>
 *
 * @since 1.0
 * @author Julien LEBLOND.
 */
public interface InputObjet {
    Header getHeader();

    default JulianDate getJulianDate(AbsoluteDate AbsDate, TimeScale timeScale) {
        final DateTimeComponents components = AbsDate.getComponents(timeScale);
        final DateComponents dateComponents = components.getDate();
        final TimeComponents timeComponents = components.getTime();
        final int year = dateComponents.getYear();
        final int month = dateComponents.getMonth();
        final int day = dateComponents.getDay();
        final int hours = timeComponents.getHour();
        final int mins = timeComponents.getMinute();
        final double seconds = timeComponents.getSecond();
        final GregorianDate gregorianDate =  new GregorianDate(year, month, day, hours, mins, seconds);
        return new JulianDate(gregorianDate);
    }

    default double dateToDouble(AbsoluteDate date) {
        final DateTimeComponents dtc = date.getComponents(TimeScalesFactory.getUTC());
        final DateComponents dc = dtc.getDate();
        final TimeComponents tc = dtc.getTime();
        final double jd = dc.getMJD();
        final double fracDay = tc.getSecondsInUTCDay();
        return jd + fracDay / 86400 + 2400000.5;
    }
}
