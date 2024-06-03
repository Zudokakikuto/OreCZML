package org.example.CZMLObjects.CZMLSecondaryObects;

import cesiumlanguagewriter.CesiumOutputStream;
import cesiumlanguagewriter.GregorianDate;
import cesiumlanguagewriter.JulianDate;
import cesiumlanguagewriter.PacketCesiumWriter;
import org.orekit.time.*;

public interface CZMLSecondaryObject {

    void write(PacketCesiumWriter packetWriter, CesiumOutputStream output);

    public default double absoluteDateToDouble(AbsoluteDate date) {
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
}
