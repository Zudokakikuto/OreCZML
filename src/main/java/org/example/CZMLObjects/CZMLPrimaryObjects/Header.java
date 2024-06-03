package org.example.CZMLObjects.CZMLPrimaryObjects;

import cesiumlanguagewriter.*;
import org.example.CZMLObjects.CZMLSecondaryObects.HeaderObjects.Clock;
import org.hipparchus.analysis.function.Abs;
import org.orekit.propagation.Propagator;
import org.orekit.time.AbsoluteDate;

import java.io.StringWriter;

public class Header implements CZMLPrimaryObject {

    private final String ID;
    private final String Name;
    private final String version;
    private final Clock clock;

    public Header(String ID, String Name, double version, Clock clock){
        this.ID = "document";
        this.Name = Name;
        this.version = "1.0";
        this.clock = clock;
    }

    public Header(Propagator propagator, AbsoluteDate finalDate){
        this.ID = "document";
        this.Name = propagator.toString();
        this.version = "1.0";
        JulianDate startDate = absoluteDateToJulianDate(propagator.getInitialState().getDate());
        JulianDate stopDate = absoluteDateToJulianDate(finalDate);
        TimeInterval timeInterval = new TimeInterval(startDate,stopDate);
        ClockRange clockRange = ClockRange.LOOP_STOP;
        ClockStep clockStep = ClockStep.SYSTEM_CLOCK_MULTIPLIER;
        this.clock = new Clock(timeInterval,startDate,60.0,clockRange,clockStep);
    }

    @Override
    public void generateCZML() {
        output.setPrettyFormatting(true);
        output.writeStartSequence();
        try(PacketCesiumWriter packet = stream.openPacket(output)){
            packet.writeId(this.ID);
            packet.writeVersion("1.0");
            packet.writeName(this.Name);
            try(ClockCesiumWriter clockWriter = packet.getClockWriter()){
                clock.write(packet,output);
            }
        }
    }

    @Override
    public StringWriter getStringWriter() {
        return stringWriter;
    }

    @Override
    public void endFile() {
        output.writeEndSequence();
    }

    public String getID() {
        return ID;
    }

    public Clock getClock() {
        return clock;
    }
}
