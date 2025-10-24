/* Copyright 2002-2025 CS GROUP
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
import cesiumlanguagewriter.JulianDate;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Czml Ellipsoid builder class
 * <p>
 * Builder for the {@link CzmlEllipsoid} class.
 *
 * @author Julien LEBLOND
 * @since 1.0.0
 */
public class CzmlEllipsoidBuilder {

    /**
     * The default color for the ellipsoid.
     */
    public static final Color DEFAULT_COLOR = new Color(255, 255, 0, 255);

    /**
     * The default fill input.
     */
    public static final boolean DEFAULT_FILL = false;

    /**
     * The default outline input.
     */
    public static final boolean DEFAULT_OUTLINE = true;

    /**
     * The default number of slice partitions.
     */
    public static final int DEFAULT_SLICE_PARTITION = 24;

    /**
     * The default number of stack partitions.
     */
    public static final int DEFAULT_STACK_PARTITION = 36;

    /**
     * The color of the ellipsoid.
     */
    private Color color = DEFAULT_COLOR;

    /**
     * The number of slices (from one point on convergence of lines from the
     * other).
     */
    private int slicePartition = DEFAULT_SLICE_PARTITION;

    /**
     * The number of stacks, (number of parallels lines around the ellipsoid).
     */
    private int stackPartition = DEFAULT_STACK_PARTITION;

    /**
     * To fill or not with the color the ellipsoid.
     */
    private boolean fill = DEFAULT_FILL;

    /**
     * To display the outline of not of the ellipsoid.
     */
    private boolean outline = DEFAULT_OUTLINE;

    /**
     * The cartesian position of the ellipsoid.
     */
    private Cartesian cartesian;

    /**
     * The julian dates when the ellipsoid will be displayed.
     */
    private List<JulianDate> julianDates = new ArrayList<>();

    /**
     * The list of cartesians representing the positions of the ellipsoid (if
     * several positions are given).
     */
    private List<Cartesian> cartesians = new ArrayList<>();

    /**
     * A parameter to know if the builder was built with julian dates and
     * cartesians or with an availability and a cartesian.
     */
    private final boolean multipleBuilder;

    /** The availability considered. */
    private Clock clock;

    // Constructors

    /**
     * The constructor of the multiple ellipsoid builder.
     *
     * @param cartesianInput : The dimensions of the ellipsoid.
     * @param clockInput : The clock considered.
     */
    public CzmlEllipsoidBuilder(final Cartesian cartesianInput,
                                final Clock clockInput) {
        this.clock = clockInput;
        this.cartesian = cartesianInput;
        this.multipleBuilder = true;
    }

    /**
     * The constructor of the single ellipsoid builder.
     *
     * @param julianDates : The dates where the ellipsoid should be displayed.
     * @param dimensions : The dimensions of the ellipsoid.
     * @param clockInput : The clock considered.
     */
    public CzmlEllipsoidBuilder(final List<JulianDate> julianDates,
                                final List<Cartesian> dimensions,
                                final Clock clockInput) {
        this.clock = clockInput;
        this.julianDates = new ArrayList<>(julianDates);
        this.cartesians = new ArrayList<>(dimensions);
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
    public CzmlEllipsoidBuilder
        withSliceStackPartition(final int slicePartitionInput,
                                final int stackPartitionInput) {
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
     * Function to set up a clock.
     *
     * @param clockInput : The clock to set up.
     * @return : The ellipsoid builder with the given clock.
     */
    public CzmlEllipsoidBuilder withClock(final Clock clockInput) {
        this.clock = clockInput;
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
     * @return : A czml ellipsoid object with the given parameters of the
     *         builder.
     */
    public CzmlEllipsoid build() {
        if (multipleBuilder) {
            return new CzmlEllipsoid(cartesian, fill, outline, slicePartition,
                                     stackPartition, color, clock);
        } else {
            return new CzmlEllipsoid(julianDates, cartesians, fill, outline,
                                     slicePartition, stackPartition, color,
                                     clock);
        }
    }
}
