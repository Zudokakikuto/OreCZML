package org.example.CZMLObjects.CZMLSecondaryObects;

import cesiumlanguagewriter.CesiumOutputStream;
import cesiumlanguagewriter.PacketCesiumWriter;
import org.orekit.time.*;

public interface CZMLSecondaryObject {

    void write(PacketCesiumWriter packetWriter, CesiumOutputStream output);

    void endFile(CesiumOutputStream output);

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
