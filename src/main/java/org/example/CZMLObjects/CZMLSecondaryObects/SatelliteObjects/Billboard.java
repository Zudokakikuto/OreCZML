package org.example.CZMLObjects.CZMLSecondaryObects.SatelliteObjects;

import cesiumlanguagewriter.*;
import org.example.CZMLObjects.CZMLSecondaryObects.CZMLSecondaryObject;

import java.awt.*;

public class Billboard implements CZMLSecondaryObject {

    private final double scale;
    private final boolean show;
    private final String imageStr;
    private final CesiumHorizontalOrigin cesiumHorizontalOrigin;
    private final CesiumResourceBehavior cesiumResourceBehavior;
    private final int red;
    private final int blue;
    private final int green;
    private final int alpha;
    private BillboardCesiumWriter writer;

    public Billboard(CesiumResourceBehavior cesiumResourceBehavior, CesiumHorizontalOrigin cesiumHorizontalOrigin, String imageStr, boolean show, double scale, Color color){
        this.scale = scale;
        this.show = show;
        this.cesiumHorizontalOrigin = cesiumHorizontalOrigin;
        this.imageStr = imageStr;
        this.cesiumResourceBehavior = cesiumResourceBehavior;
        this.red = color.getRed();
        this.green = color.getGreen();
        this.blue = color.getBlue();
        this.alpha = color.getAlpha();
    }

    public Billboard(){
        this.scale = 1.5;
        this.show = true;
        this.imageStr = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAADsMAAA7DAcdvqGQAAADJSURBVDhPnZHRDcMgEEMZjVEYpaNklIzSEfLfD4qNnXAJSFWfhO7w2Zc0Tf9QG2rXrEzSUeZLOGm47WoH95x3Hl3jEgilvDgsOQUTqsNl68ezEwn1vae6lceSEEYvvWNT/Rxc4CXQNGadho1NXoJ+9iaqc2xi2xbt23PJCDIB6TQjOC6Bho/sDy3fBQT8PrVhibU7yBFcEPaRxOoeTwbwByCOYf9VGp1BYI1BA+EeHhmfzKbBoJEQwn1yzUZtyspIQUha85MpkNIXB7GizqDEECsAAAAASUVORK5CYII=";
        this.cesiumHorizontalOrigin = CesiumHorizontalOrigin.CENTER;
        this.cesiumResourceBehavior = CesiumResourceBehavior.LINK_TO;
        this.red = 0;
        this.green = 255;
        this.blue = 255;
        this.alpha = 195;
    }

    @Override
    public void write(PacketCesiumWriter packetWriter, CesiumOutputStream output) {
    }

    @Override
    public void endFile(CesiumOutputStream output) {

    }

    public double getScale() {
        return scale;
    }

    public boolean getShow() {
        return show;
    }

    public String getImageStr() {
        return imageStr;
    }

    public CesiumResourceBehavior getCesiumResourceBehavior() {
        return cesiumResourceBehavior;
    }

    public CesiumHorizontalOrigin getCesiumHorizontalOrigin() {
        return cesiumHorizontalOrigin;
    }

    public int getRed() {
        return red;
    }

    public int getGreen() {
        return green;
    }

    public int getBlue() {
        return blue;
    }

    public int getAlpha() {
        return alpha;
    }
}
