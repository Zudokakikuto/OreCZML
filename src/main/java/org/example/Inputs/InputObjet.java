package org.example.Inputs;

import cesiumlanguagewriter.GregorianDate;
import cesiumlanguagewriter.JulianDate;
import org.example.CZMLObjects.CZMLPrimaryObjects.Header;
import org.orekit.time.*;

public interface InputObjet {
    Header getHeader();
    public default JulianDate getJulianDate(AbsoluteDate AbsDate, TimeScale timeScale){
        DateTimeComponents components = AbsDate.getComponents(timeScale);
        DateComponents dateComponents = components.getDate();
        TimeComponents timeComponents = components.getTime();
        int year = dateComponents.getYear();
        int month = dateComponents.getMonth();
        int day = dateComponents.getDay();
        int hours = timeComponents.getHour();
        int mins = timeComponents.getMinute();
        double seconds = timeComponents.getSecond();
        GregorianDate gregorianDate =  new GregorianDate(year,month,day,hours,mins,seconds);
        return new JulianDate(gregorianDate);
    }

    public default double dateToDouble(AbsoluteDate date) {
        DateTimeComponents dtc = date.getComponents(TimeScalesFactory.getUTC());
        DateComponents dc = dtc.getDate();
        TimeComponents tc = dtc.getTime();
        double jd = dc.getMJD();
        double fracDay = tc.getSecondsInUTCDay();
        double finalDay = jd + fracDay / 86400 + 2400000.5;
        return finalDay;
    }
}
