/* Copyright 2002-2024 CS GROUP
 * Licensed to CS GROUP (CS) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * CS licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.orekit.czml.CzmlObjects.CzmlSecondaryObjects;

import cesiumlanguagewriter.Cartesian;
import cesiumlanguagewriter.CesiumOutputStream;
import cesiumlanguagewriter.EllipsoidCesiumWriter;
import cesiumlanguagewriter.EllipsoidRadiiCesiumWriter;
import cesiumlanguagewriter.PacketCesiumWriter;
import cesiumlanguagewriter.TimeInterval;

import java.awt.Color;

public class CzmlEllipsoid implements CzmlSecondaryObject {

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


    public CzmlEllipsoid(final TimeInterval availability, final Cartesian cartesian) {
        this(availability, cartesian, new Color(255, 255, 0, 255));
    }

    public CzmlEllipsoid(final TimeInterval availability, final Cartesian cartesian, final Color color) {
        this.cartesian = cartesian;
        this.availability = availability;
        this.fill = false;
        this.outline = true;
        this.color = color;
        this.slicePartition = 24;
        this.stackPartition = 36;
    }

    public CzmlEllipsoid(final TimeInterval availability, final Cartesian cartesian, final int slicePartition, final int stackPartition) {
        this(availability, cartesian, slicePartition, stackPartition, new Color(255, 255, 0, 255));
    }

    public CzmlEllipsoid(final TimeInterval availability, final Cartesian cartesian, final int slicePartition, final int stackPartition, final Color color) {
        this.cartesian = cartesian;
        this.availability = availability;
        this.fill = false;
        this.outline = true;
        this.color = color;
        this.slicePartition = slicePartition;
        this.stackPartition = stackPartition;
    }

    public CzmlEllipsoid(final TimeInterval availability, final Cartesian cartesian, final boolean fill, final boolean outline, final Color color, final int slicePartition, final int stackPartition) {
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
