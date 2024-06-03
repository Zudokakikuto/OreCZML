package org.example.CZMLObjects;

import cesiumlanguagewriter.CesiumArcType;
import cesiumlanguagewriter.CesiumOutputStream;
import cesiumlanguagewriter.PacketCesiumWriter;
import cesiumlanguagewriter.TimeInterval;

import java.awt.*;
import java.util.List;

public class Polyline {

    private TimeInterval availability;
    private boolean show;
    private double width;
    private Color color;
    private CesiumArcType arcType;

    public Polyline(TimeInterval availability, double width, Color color, boolean show){
        this.availability = availability;
        this.width = width;
        this.color = color;
        this.show = show;
    }

    public Polyline(){
        Color color = new Color(0,255,255,255);

        this.width = 1;
        this.arcType = CesiumArcType.NONE;
        this.color = color;
    }


    public void write(PacketCesiumWriter packetWriter, CesiumOutputStream output, List<Show> showList) {

    }

    public TimeInterval getAvailability() {
        return availability;
    }

    public Color getColor() {
        return color;
    }

    public boolean getShow() {
        return show;
    }

    public double getWidth() {
        return width;
    }

    public CesiumArcType getArcType() {
        return arcType;
    }
}
