package org.example.CZMLObjects.CZMLSecondaryObects.SatelliteObjects;

import cesiumlanguagewriter.*;
import org.example.CZMLObjects.CZMLSecondaryObects.CZMLSecondaryObject;

public class Path implements CZMLSecondaryObject {

    /** .*/
    private TimeInterval availability;
    /** .*/
    private PathCesiumWriter writer;
    /** .*/
    private static boolean show;

    //Optional parameters
    /** .*/
    private double periodToDisplay = 0.0;

    public Path(final TimeInterval availability, final PacketCesiumWriter writer, final boolean show) {
        this.availability = availability;
        this.writer = writer.getPathWriter();
        this.show = show;
    }

    public Path(final TimeInterval availability, final PacketCesiumWriter writer) {
        this.availability = availability;
        this.writer = writer.getPathWriter();
        this.show = true;
    }

    public Path(final TimeInterval availability, final PacketCesiumWriter writer, final double period) {
        this.availability = availability;
        this.writer = writer.getPathWriter();
        this.show = true;
        this.periodToDisplay = period;
    }

    @Override
    public void write(final PacketCesiumWriter packetWriter, final CesiumOutputStream output) {
        try (BooleanCesiumWriter showPath = writer.openShowProperty()) {
            showPath.writeInterval(availability.getStart(), availability.getStop());
            showPath.writeBoolean(show);
        }
    }

    public static boolean getShow() {
        return show;
    }
}
