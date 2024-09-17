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
package org.orekit.czml.archi.builder;

import cesiumlanguagewriter.Cartesian;
import cesiumlanguagewriter.JulianDate;
import cesiumlanguagewriter.TimeInterval;
import org.orekit.czml.object.secondary.CzmlEllipsoid;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Czml Ellipsoid builder class
 * <p>
 * Builder for the {@link CzmlEllipsoid} class.
 * <p>
 *
 * @author Julien LEBLOND
 * @since 1.0.0
 */

public class CzmlEllipsoidBuilder {

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
    private Color color;

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

    /** A parameter to know if the builder was built with julian dates and cartesians or with an availability and a cartesian. */
    private boolean multipleBuilder;

    // Constructors

    /**
     * The constructor of the multiple ellipsoid builder.
     *
     * @param availabilityInput : The time interval where the ellipsoid should be displayed.
     * @param cartesianInput    : The dimensions of the ellipsoid.
     */
    public CzmlEllipsoidBuilder(final TimeInterval availabilityInput, final Cartesian cartesianInput) {
        this.availability    = availabilityInput;
        this.cartesian       = cartesianInput;
        this.multipleBuilder = true;
    }

    /**
     * The constructor of the single ellipsoid builder.
     *
     * @param julianDates : The dates where the ellipsoid should be displayed.
     * @param dimensions  : The dimensions of the ellipsoid.
     */
    public CzmlEllipsoidBuilder(final List<JulianDate> julianDates, final List<Cartesian> dimensions) {
        this.allJulianDates  = julianDates;
        this.allCartesians   = dimensions;
        this.multipleBuilder = false;
    }

    /**
     * Function to set up a color.
     *
     * @param colorInput : The color to set up.
     * @return : The ellipsoid builder with the given color.
     */
    public CzmlEllipsoidBuilder withColor(final Color colorInput) {
        this.color = colorInput;
        return this;
    }

    /**
     * Function to set up a slice and a stack partition.
     *
     * @param slicePartitionInput : The slice partition to set up.
     * @param stackPartitionInput : The stack partition to set up.
     * @return : The ellipsoid builder with the slice and stack partition.
     */
    public CzmlEllipsoidBuilder withSliceStackPartition(final int slicePartitionInput, final int stackPartitionInput) {
        this.slicePartition = slicePartitionInput;
        this.stackPartition = stackPartitionInput;
        return this;
    }

    /**
     * Function to set up if the ellipsoid must be filled or not.
     *
     * @param fillInput : The fill to set up.
     * @return : The ellipsoid builder with the given fill input.
     */
    public CzmlEllipsoidBuilder withFill(final boolean fillInput) {
        this.fill = fillInput;
        return this;
    }

    /**
     * Function to set up if the outline should be displayed or not.
     *
     * @param outlineInput : The outline input to set up.
     * @return : The ellipsoid builder with the given outline input.
     */
    public CzmlEllipsoidBuilder withOutline(final boolean outlineInput) {
        this.outline = outlineInput;
        return this;
    }

    /**
     * The build function that generates the czml ellipsoid object.
     *
     * @return : A czml ellipsoid object with the given parameters of the builder.
     */
    public CzmlEllipsoid build() {
        if (multipleBuilder) {
            return new CzmlEllipsoid(availability, cartesian, fill, outline, slicePartition, stackPartition, color);
        } else {
            return new CzmlEllipsoid(allJulianDates, allCartesians, fill, outline, slicePartition, stackPartition,
                    color);
        }
    }
}
