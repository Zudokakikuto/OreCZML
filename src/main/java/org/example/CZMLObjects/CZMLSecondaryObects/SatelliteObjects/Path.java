package org.example.CZMLObjects.CZMLSecondaryObects.SatelliteObjects;

import cesiumlanguagewriter.*;
import org.example.CZMLObjects.CZMLSecondaryObects.CZMLSecondaryObject;

public class Path implements CZMLSecondaryObject {

    private TimeInterval availability;
    private PathCesiumWriter writer;
    private static boolean show;

    public Path(TimeInterval availability, PacketCesiumWriter writer, boolean show){
        this.availability = availability;
        this.writer = writer.getPathWriter();
        this.show = show;
    }

    public Path(TimeInterval availability, PacketCesiumWriter writer){
        this.availability = availability;
        this.writer = writer.getPathWriter();
        this.show = true;
    }

    @Override
    public void write(PacketCesiumWriter packetWriter, CesiumOutputStream output) {
        try (BooleanCesiumWriter showPath = writer.openShowProperty()) {
            showPath.writeInterval(availability.getStart(), availability.getStop());
            showPath.writeBoolean(show);
        }
    }

    @Override
    public void endFile(CesiumOutputStream output) {

    }

    public static boolean getShow(){
        return show;
    }
}
