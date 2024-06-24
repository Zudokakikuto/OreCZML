package org.example.Inputs;

import cesiumlanguagewriter.GregorianDate;
import cesiumlanguagewriter.JulianDate;
import org.example.CZMLObjects.CZMLPrimaryObjects.Header;
import org.orekit.time.*;

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
