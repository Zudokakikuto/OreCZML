package org.example.CZMLObjects.CZMLSecondaryObects;

import cesiumlanguagewriter.ArcTypeCesiumWriter;
import cesiumlanguagewriter.TimeInterval;
import org.example.CZMLObjects.CZMLPrimaryObjects.LineOfVisibility;

import java.awt.*;

public class Polyline {

    private TimeInterval availability;
    private boolean show;
    private double width;
    private Color color;
    private String arcType;

    public Polyline(TimeInterval availability, double width, Color color, boolean show){
        this.availability = availability;
        this.width = width;
        this.color = color;
        this.show = show;
    }

    public Polyline(LineOfVisibility lineOfVisibility){

    }
}
