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
import cesiumlanguagewriter.GregorianDate;
import cesiumlanguagewriter.JulianDate;
import cesiumlanguagewriter.PacketCesiumWriter;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.DateComponents;
import org.orekit.time.DateTimeComponents;
import org.orekit.time.TimeComponents;
import org.orekit.time.TimeScale;
import org.orekit.time.TimeScalesFactory;

public interface CzmlSecondaryObject {

    void write(PacketCesiumWriter packetWriter, CesiumOutputStream output);

    default double absoluteDateToDouble(AbsoluteDate date) {
        final DateTimeComponents dtc = date.getComponents(TimeScalesFactory.getUTC());
        final DateComponents dc = dtc.getDate();
        final TimeComponents tc = dtc.getTime();
        final double jd = dc.getMJD();
        final double fracDay = tc.getSecondsInUTCDay();
        return jd + fracDay / 86400 + 2400000.5;
    }

    default JulianDate absoluteDateToJulianDate(AbsoluteDate date) {

        final TimeScale UTC = TimeScalesFactory.getUTC();
        final int year = date.getComponents(UTC)
                             .getDate()
                             .getYear();
        final int month = date.getComponents(UTC)
                              .getDate()
                              .getMonth();
        final int day = date.getComponents(UTC)
                            .getDate()
                            .getDay();
        final int hour = date.getComponents(UTC)
                             .getTime()
                             .getHour();
        final int min = date.getComponents(UTC)
                            .getTime()
                            .getMinute();
        final double sec = date.getComponents(UTC)
                               .getTime()
                               .getSecond();

        final GregorianDate gregorianDate = new GregorianDate(year, month, day, hour, min, sec);
        return new JulianDate(gregorianDate);
    }
}
