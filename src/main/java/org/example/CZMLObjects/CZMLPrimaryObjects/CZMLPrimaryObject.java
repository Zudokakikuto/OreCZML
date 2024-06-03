package org.example.CZMLObjects.CZMLPrimaryObjects;

import cesiumlanguagewriter.CesiumOutputStream;
import cesiumlanguagewriter.CesiumStreamWriter;
import cesiumlanguagewriter.GregorianDate;
import cesiumlanguagewriter.JulianDate;
import org.orekit.time.*;

import java.io.StringWriter;

public interface CZMLPrimaryObject<T>{
    StringWriter stringWriter = new StringWriter();
    CesiumOutputStream output = new CesiumOutputStream(stringWriter);
    CesiumStreamWriter stream = new CesiumStreamWriter();

    public default double dateToDouble(AbsoluteDate date) {
        DateTimeComponents dtc = date.getComponents(TimeScalesFactory.getUTC());
        DateComponents dc = dtc.getDate();
        TimeComponents tc = dtc.getTime();
        double jd = dc.getMJD();
        double fracDay = tc.getSecondsInUTCDay();
        double finalDay = jd + fracDay / 86400 + 2400000.5;
        return finalDay;
    }

    public default JulianDate absoluteDateToJulianDate(AbsoluteDate date){

        TimeScale UTC = TimeScalesFactory.getUTC();
        int year = date.getComponents(UTC).getDate().getYear();
        int month = date.getComponents(UTC).getDate().getMonth();
        int day = date.getComponents(UTC).getDate().getDay();
        int hour = date.getComponents(UTC).getTime().getHour();
        int min = date.getComponents(UTC).getTime().getMinute();
        double sec = date.getComponents(UTC).getTime().getSecond();

        GregorianDate gregorianDate = new GregorianDate(year,month,day,hour,min,sec);
        return new JulianDate(gregorianDate);
    }

    public default AbsoluteDate julianDateToAbsoluteDate(JulianDate date){

        TimeScale UTC = TimeScalesFactory.getUTC();
        GregorianDate gregorianDate = date.toGregorianDate();
        int year = gregorianDate.getYear();
        int month = gregorianDate.getMonth();
        int day = gregorianDate.getDay();
        int hour = gregorianDate.getHour();
        int min = gregorianDate.getMinute();
        double sec = gregorianDate.getSecond();
        return new AbsoluteDate(year,month,day,hour,min,sec,UTC);
    }

    void generateCZML();
    StringWriter getStringWriter();
    void endFile();
}
