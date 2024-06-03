package org.example.CZMLObjects.CZMLSecondaryObects;

import cesiumlanguagewriter.*;
import cesiumlanguagewriter.advanced.ICesiumFontValuePropertyWriter;

import java.awt.*;

public class Label implements CZMLSecondaryObject{

    private Color color;
    private CesiumHorizontalOrigin horizontalOrigin;
    private boolean show;
    private CesiumLabelStyle labelStyle;
    private String text;
    private CesiumVerticalOrigin verticalOrigin;
    private int alpha;


    public Label(Object object){
        int red = 0;
        int blue = 255;
        int green = 255;
        int alpha = 255;
        this.color = new Color(red,blue,green, alpha);
        this.horizontalOrigin = CesiumHorizontalOrigin.LEFT;
        this.verticalOrigin = CesiumVerticalOrigin.CENTER;
        this.labelStyle = CesiumLabelStyle.FILL_AND_OUTLINE;
        this.text = object.toString();
        this.show = true;
    }

    public Label(String text,Color color, CesiumHorizontalOrigin horizontalOrigin,
                 CesiumVerticalOrigin verticalOrigin, CesiumLabelStyle labelStyle, boolean show){
        this.color = color;
        this.horizontalOrigin = horizontalOrigin;
        this.verticalOrigin = verticalOrigin;
        this.text = text;
        this.labelStyle = labelStyle;
        this.show = show;
    }

    @Override
    public void write(PacketCesiumWriter packetWriter, CesiumOutputStream output) {
    }

    public Color getColor(){
        return color;
    }

    public String getText() {
        return text;
    }

    public CesiumVerticalOrigin getVerticalOrigin() {
        return verticalOrigin;
    }

    public CesiumHorizontalOrigin getHorizontalOrigin() {
        return horizontalOrigin;
    }

    public CesiumLabelStyle getLabelStyle() {
        return labelStyle;
    }

    public boolean getShow(){
        return show;
    }
}

