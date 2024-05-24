package org.example.CZMLObjects.CZMLPrimaryObjects;

import cesiumlanguagewriter.CesiumOutputStream;
import cesiumlanguagewriter.CesiumStreamWriter;
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
    void write();
    StringWriter getStringWriter();
    void endFile();
}
