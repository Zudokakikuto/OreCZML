package org.example.CZMLObjects.CZMLSecondaryObects;

import cesiumlanguagewriter.*;

import java.awt.*;
import java.awt.desktop.OpenURIEvent;

public class CZMLEllipsoid implements CZMLSecondaryObject {

    /** .*/
    private boolean fill = false;
    /** .*/
    private TimeInterval availability;
    /** .*/
    private boolean outline = true;
    /** .*/
    private Color color;
    /** .*/
    private int slicePartition;
    /** .*/
    private int stackPartition;
    /** .*/
    private Cartesian cartesian;

    // Builders


    public CZMLEllipsoid(final TimeInterval availability, final Cartesian cartesian) {
        this(availability, cartesian, new Color(255, 255, 0, 255));
    }

    public CZMLEllipsoid(final TimeInterval availability, final Cartesian cartesian, final Color color) {
        this.cartesian = cartesian;
        this.availability = availability;
        this.fill = false;
        this.outline = true;
        this.color = color;
        this.slicePartition = 24;
        this.stackPartition = 36;
    }

    public CZMLEllipsoid(final TimeInterval availability, final Cartesian cartesian, final int slicePartition, final int stackPartition) {
        this(availability, cartesian, slicePartition, stackPartition, new Color(255, 255, 0, 255));
    }

    public CZMLEllipsoid(final TimeInterval availability, final Cartesian cartesian, final int slicePartition, final int stackPartition, final Color color) {
        this.cartesian = cartesian;
        this.availability = availability;
        this.fill = false;
        this.outline = true;
        this.color = color;
        this.slicePartition = slicePartition;
        this.stackPartition = stackPartition;
    }

    public CZMLEllipsoid(final TimeInterval availability, final Cartesian cartesian, final boolean fill, final boolean outline, final Color color, final int slicePartition, final int stackPartition) {
        this.cartesian = cartesian;
        this.availability = availability;
        this.fill = fill;
        this.outline = outline;
        this.color = color;
        this.slicePartition = slicePartition;
        this.stackPartition = stackPartition;
    }

    // GETS

    public Color getColor() {
        return color;
    }

    public Cartesian getCartesian() {
        return cartesian;
    }

    public int getSlicePartition() {
        return slicePartition;
    }

    public int getStackPartition() {
        return stackPartition;
    }

    public boolean getFill() {
        return fill;
    }

    public boolean getOutline() {
        return outline;
    }

    public TimeInterval getAvailability() {
        return availability;
    }

    @Override
    public void write(final PacketCesiumWriter packetWriter, final CesiumOutputStream output) {
        try (EllipsoidCesiumWriter ellipsoidCesiumWriter = packetWriter.getEllipsoidWriter()) {
            ellipsoidCesiumWriter.open(output);
            ellipsoidCesiumWriter.writeFillProperty(this.getFill());
            ellipsoidCesiumWriter.writeOutlineProperty(this.getOutline());
            ellipsoidCesiumWriter.writeOutlineColorProperty(this.getColor());
            ellipsoidCesiumWriter.writeSlicePartitionsProperty(this.getSlicePartition());
            ellipsoidCesiumWriter.writeStackPartitionsProperty(this.getStackPartition());
            ellipsoidCesiumWriter.writeInterval(this.getAvailability());

            try (EllipsoidRadiiCesiumWriter radiiWriter = ellipsoidCesiumWriter.getRadiiWriter()) {
                radiiWriter.open(output);
                radiiWriter.writeCartesian(this.getCartesian());
            }
        }
    }
}
