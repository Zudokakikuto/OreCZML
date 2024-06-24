package org.example.CZMLObjects.CZMLSecondaryObects;

import cesiumlanguagewriter.*;

import java.awt.*;

public class Billboard implements CZMLSecondaryObject {

    /** .*/
    private final double scale;
    /** .*/
    private final boolean show;
    /** .*/
    private final String imageStr;
    /** .*/
    private final CesiumHorizontalOrigin cesiumHorizontalOrigin;
    /** .*/
    private final CesiumResourceBehavior cesiumResourceBehavior;
    /** .*/
    private final int red;
    /** .*/
    private final int blue;
    /** .*/
    private final int green;
    /** .*/
    private final int alpha;
    /** .*/
    private BillboardCesiumWriter writer;

    public Billboard(final CesiumResourceBehavior cesiumResourceBehavior, final CesiumHorizontalOrigin cesiumHorizontalOrigin, final String imageStr, final boolean show, final double scale, final Color color) {
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

    public Billboard(final String imageStr) {
        this.scale = 1.5;
        this.show = true;
        this.imageStr = imageStr;
        this.cesiumHorizontalOrigin = CesiumHorizontalOrigin.CENTER;
        this.cesiumResourceBehavior = CesiumResourceBehavior.LINK_TO;
        this.red = 0;
        this.green = 255;
        this.blue = 255;
        this.alpha = 195;
    }

    @Override
    public void write(final PacketCesiumWriter packetWriter, final CesiumOutputStream output) {
        try (BillboardCesiumWriter billboardWriter = packetWriter.getBillboardWriter()) {
            billboardWriter.open(output);
            billboardWriter.writeHorizontalOriginProperty(cesiumHorizontalOrigin);
            billboardWriter.writeColorProperty(red, green, blue, alpha);
            billboardWriter.writeScaleProperty(scale);
            billboardWriter.writeShowProperty(show);
            writeImage(billboardWriter, output);
        }

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

    // Functions

    private void writeImage(final BillboardCesiumWriter billboardWriter, final CesiumOutputStream output) {
        try (UriCesiumWriter imageBillBoard = billboardWriter.openImageProperty()) {
            imageBillBoard.writeUri(imageStr, cesiumResourceBehavior);
        }
    }
}
