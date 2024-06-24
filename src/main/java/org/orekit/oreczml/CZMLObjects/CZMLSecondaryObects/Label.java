/** .*/

package org.example.CZMLObjects.CZMLSecondaryObects;

import cesiumlanguagewriter.*;

import java.awt.*;

public class Label implements CZMLSecondaryObject {

    /** .*/
    private final Color color;
    /** .*/
    private final CesiumHorizontalOrigin horizontalOrigin;
    /** .*/
    private final boolean show;
    /** .*/
    private final CesiumLabelStyle labelStyle;
    /** .*/
    private final String text;
    /** .*/
    private final CesiumVerticalOrigin verticalOrigin;


    public Label(final Object object) {
        final int red = 0;
        final int blue = 255;
        final int green = 255;
        final int alpha = 255;
        this.color = new Color(red, blue, green, alpha);
        this.horizontalOrigin = CesiumHorizontalOrigin.LEFT;
        this.verticalOrigin = CesiumVerticalOrigin.CENTER;
        this.labelStyle = CesiumLabelStyle.FILL_AND_OUTLINE;
        this.text = object.toString();
        this.show = true;
    }

    public Label(final String text, final Color color, final CesiumHorizontalOrigin horizontalOrigin,
                 final CesiumVerticalOrigin verticalOrigin, final CesiumLabelStyle labelStyle, final boolean show) {
        this.color = color;
        this.horizontalOrigin = horizontalOrigin;
        this.verticalOrigin = verticalOrigin;
        this.text = text;
        this.labelStyle = labelStyle;
        this.show = show;
    }

    @Override
    public void write(final PacketCesiumWriter packetWriter, final CesiumOutputStream output) {
    }

    public Color getColor() {
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

    public boolean getShow() {
        return show;
    }
}

