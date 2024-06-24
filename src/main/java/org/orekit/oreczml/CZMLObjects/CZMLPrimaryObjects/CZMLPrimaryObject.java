package org.example.CZMLObjects.CZMLPrimaryObjects;

import cesiumlanguagewriter.*;
import org.orekit.time.*;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public interface CZMLPrimaryObject<T> {

    /** .*/
    StringWriter STRING_WRITER = new StringWriter();
    /** .*/
    CesiumOutputStream OUTPUT = new CesiumOutputStream(STRING_WRITER);
    /** .*/
    CesiumStreamWriter STREAM = new CesiumStreamWriter();

    default double absDateToDouble(AbsoluteDate date) {
        final DateTimeComponents dtc = date.getComponents(TimeScalesFactory.getUTC());
        final DateComponents dc = dtc.getDate();
        final TimeComponents tc = dtc.getTime();
        final double jd = dc.getMJD();
        final double fracDay = tc.getSecondsInUTCDay();
        return jd + fracDay / 86400 + 2400000.5;
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

    default AbsoluteDate julianDateToAbsoluteDate(JulianDate julianDate) {
        final TimeScale UTC = TimeScalesFactory.getUTC();
        final GregorianDate gregorianDate = julianDate.toGregorianDate();
        final int year = gregorianDate.getYear();
        final int month = gregorianDate.getMonth();
        final int day = gregorianDate.getDay();
        final int hour = gregorianDate.getHour();
        final int min = gregorianDate.getMinute();
        final double sec = gregorianDate.getSecond();
        return new AbsoluteDate(year, month, day, hour, min, sec, UTC);
    }

    default List<AbsoluteDate> julianDateListToAbsoluteDateList(List<JulianDate> julianDates) {
        final List<AbsoluteDate> toReturn = new ArrayList<>();
        for (int i = 0; i < julianDates.size(); i++) {
            toReturn.add(julianDateToAbsoluteDate(julianDates.get(i)));
        }
        return toReturn;
    }

    default List<TimeInterval> julienDateListToTimeIntervals(List<JulianDate> julianDates) {

        final List<TimeInterval> toReturn = new ArrayList<>();
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

    String getId();
    String getName();
    void generateCZML();
    StringWriter getStringWriter();
    void endFile();
    void cleanObject();
}
