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
package org.orekit.czml.object.secondary;

import cesiumlanguagewriter.Cartesian;
import cesiumlanguagewriter.CesiumOutputStream;
import cesiumlanguagewriter.EllipsoidCesiumWriter;
import cesiumlanguagewriter.EllipsoidRadiiCesiumWriter;
import cesiumlanguagewriter.JulianDate;
import cesiumlanguagewriter.PacketCesiumWriter;
import cesiumlanguagewriter.TimeInterval;
import org.orekit.czml.archi.builder.CzmlEllipsoidBuilder;
import org.orekit.czml.object.primary.Header;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Czml Ellipsoid class
 *
 * <p> This class allows the user to display an ellipsoid with various geometries.</p>
 *
 * @author Julien LEBLOND
 * @since 1.0.0
 */

public class CzmlEllipsoid implements CzmlSecondaryObject {

    /** The default color for the ellipsoid. */
    public static final Color DEFAULT_COLOR = new Color(255, 255, 0, 255);

    /** The default fill input. */
    public static final boolean DEFAULT_FILL = true;

    /** The default outline input. */
    public static final boolean DEFAULT_OUTLINE = false;

    /** The default number of slice partitions. */
    public static final int DEFAULT_SLICE_PARTITION = 24;

    /** The default number of stack partitions. */
    public static final int DEFAULT_STACK_PARTITION = 36;


    /** The availability of the ellipsoid. */
    private TimeInterval availability;

    /** The color of the ellipsoid. */
    private Color color = DEFAULT_COLOR;

    /** The number of slices (from one point on convergence of lines from the other). */
    private int slicePartition = DEFAULT_SLICE_PARTITION;

    /** The number of stacks, (number of parallels lines around the ellipsoid). */
    private int stackPartition = DEFAULT_STACK_PARTITION;

    /** To fill or not with the color the ellipsoid. */
    private boolean fill = DEFAULT_FILL;

    /** To display the outline of not of the ellipsoid. */
    private boolean outline = DEFAULT_OUTLINE;

    /** The cartesian position of the ellipsoid. */
    private Cartesian cartesian;

    /** The julian dates when the ellipsoid will be displayed. */
    private List<JulianDate> allJulianDates = new ArrayList<>();

    /** The list of cartesians representing the positions of the ellipsoid (if several positions are given). */
    private List<Cartesian> allCartesians = new ArrayList<>();

    /** A parameter to make the ellipsoid a display group of several ellipsoids to animate it in time. */
    private boolean multipleEllipsoids = true;


    // Constructors

    /// MULTIPLE ELLIPSOIDS (Those constructors are not optimal because they try to create several ellipsoids in simulate
    /// the motion of the ellipsoid, which make it choppy.

    /**
     * The constructor of the multiple ellipsoid object given an availability and a position, with default parameters.
     *
     * @param availability : The availability of the ellipsoid.
     * @param cartesian    : The dimensions of the ellipsoid.
     */
    public CzmlEllipsoid(final TimeInterval availability, final Cartesian cartesian) {
        this(availability, cartesian, DEFAULT_FILL, DEFAULT_OUTLINE, DEFAULT_SLICE_PARTITION, DEFAULT_STACK_PARTITION,
                DEFAULT_COLOR);
    }

    /**
     * The constructor of the multiple ellipsoid object with no default parameters.
     *
     * @param availability   : The availability of the ellipsoid.
     * @param cartesian      : The dimensions of the ellipsoid.
     * @param fill           : To fill or not the ellipsoid with the color.
     * @param outline        : To display or not the outline of the ellipsoid.
     * @param slicePartition : The number of slices of the ellipsoid (number of lines from one point of convergence of the line to the other)
     * @param stackPartition : The number of stacks of the ellipsoid (number of parallels lines in the vertical direction)
     * @param color          : The color of the ellipsoid.
     */
    public CzmlEllipsoid(final TimeInterval availability, final Cartesian cartesian, final boolean fill,
                         final boolean outline, final int slicePartition, final int stackPartition,
                         final Color color) {
        this.cartesian      = cartesian;
        this.availability   = availability;
        this.fill           = fill;
        this.outline        = outline;
        this.color          = color;
        this.slicePartition = slicePartition;
        this.stackPartition = stackPartition;
    }

    // UNIQUE ELLIPSOIDS

    /**
     * Unique ellipsoid made to follow an object, this ellipsoid will need several cartesians and several dates.
     *
     * @param julianDates : ALl the dates where the ellipsoid must be computed.
     * @param dimensions  : These cartesians represent the dimensions of the ellipsoid (x,y,z), each value is the distance from the center for each dimension.
     */
    public CzmlEllipsoid(final List<JulianDate> julianDates, final List<Cartesian> dimensions) {
        this(julianDates, dimensions, DEFAULT_FILL, DEFAULT_OUTLINE, DEFAULT_SLICE_PARTITION, DEFAULT_STACK_PARTITION,
                DEFAULT_COLOR);
    }

    /**
     * Unique ellipsoid made to follow an object, this ellipsoid will need several cartesians and several dates.
     *
     * @param julianDates    : ALl the dates where the ellipsoid must be computed.
     * @param dimensions     : These cartesians represent the dimensions of the ellipsoid (x,y,z), each value is the distance from the center for each dimension.
     * @param fill           : To fill the ellipsoid or not.
     * @param outline        : To display the outline or not.
     * @param color          : The color of the ellipsoid.
     * @param slicePartition : The number of slices of the ellipsoid (number of lines from one point of convergence of the line to the other).
     * @param stackPartition : The number of stacks of the ellipsoid (number of parallels lines in the vertical direction).
     */
    public CzmlEllipsoid(final List<JulianDate> julianDates, final List<Cartesian> dimensions, final boolean fill,
                         final boolean outline, final int slicePartition, final int stackPartition,
                         final Color color) {
        this.fill               = fill;
        this.outline            = outline;
        this.color              = color;
        this.availability       = Header.getMasterClock()
                                        .getAvailability();
        this.slicePartition     = slicePartition;
        this.stackPartition     = stackPartition;
        this.allJulianDates     = julianDates;
        this.allCartesians      = dimensions;
        this.multipleEllipsoids = false;
    }


    // Builder

    public static CzmlEllipsoidBuilder builder(final TimeInterval availability, final Cartesian cartesian) {
        return new CzmlEllipsoidBuilder(availability, cartesian);
    }

    public static CzmlEllipsoidBuilder builder(final List<JulianDate> julianDates, final List<Cartesian> dimensions) {
        return new CzmlEllipsoidBuilder(julianDates, dimensions);
    }

    // Overrides

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

            if (multipleEllipsoids) {
                try (EllipsoidRadiiCesiumWriter radiiWriter = ellipsoidCesiumWriter.getRadiiWriter()) {
                    radiiWriter.open(output);
                    radiiWriter.writeCartesian(this.getCartesian());
                }
            } else {
                try (EllipsoidRadiiCesiumWriter radiiWriter = ellipsoidCesiumWriter.getRadiiWriter()) {
                    radiiWriter.open(output);
                    radiiWriter.writeCartesian(allJulianDates, allCartesians);
                }
            }
        }
    }


    // Getters

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
}
