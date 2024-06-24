package org.example.CZMLObjects.CZMLSecondaryObects;

import cesiumlanguagewriter.CesiumOutputStream;
import cesiumlanguagewriter.GregorianDate;
import cesiumlanguagewriter.JulianDate;
import cesiumlanguagewriter.PacketCesiumWriter;
import org.orekit.time.*;

public interface CZMLSecondaryObject {

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
        final int year = date.getComponents(UTC).getDate().getYear();
        final int month = date.getComponents(UTC).getDate().getMonth();
        final int day = date.getComponents(UTC).getDate().getDay();
        final int hour = date.getComponents(UTC).getTime().getHour();
        final int min = date.getComponents(UTC).getTime().getMinute();
        final double sec = date.getComponents(UTC).getTime().getSecond();

        final GregorianDate gregorianDate = new GregorianDate(year, month, day, hour, min, sec);
        return new JulianDate(gregorianDate);
    }
}
