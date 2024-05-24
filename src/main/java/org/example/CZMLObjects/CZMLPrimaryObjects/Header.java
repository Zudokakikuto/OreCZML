package org.example.CZMLObjects.CZMLPrimaryObjects;

import cesiumlanguagewriter.ClockCesiumWriter;
import cesiumlanguagewriter.PacketCesiumWriter;
import org.example.CZMLObjects.CZMLSecondaryObects.HeaderObjects.Clock;

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

    @Override
    public void write() {
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
        output.writeEndObject();
        output.writeEndSequence();
    }
}
