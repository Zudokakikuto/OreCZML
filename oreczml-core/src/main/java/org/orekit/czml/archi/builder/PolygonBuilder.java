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
import cesiumlanguagewriter.TimeInterval;
import org.orekit.czml.object.primary.Header;
import org.orekit.czml.object.secondary.Polygon;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class PolygonBuilder {

    /** The default color. */
    public static final Color DEFAULT_COLOR = new Color(243, 194, 32);

    /** The default availability. */
    public static final TimeInterval DEFAULT_AVAILABILITY = Header.getMasterClock()
                                                                  .getAvailability();

    /** Positions of the polygon. */
    private List<Cartesian> cartesians = new ArrayList<>();

    /** Time interval when the polygon is displayed . */
    private TimeInterval availability = DEFAULT_AVAILABILITY;

    /** The color of the polygon. */
    private Color color = DEFAULT_COLOR;

    /** To display the outline or not. */
    private boolean outline = false;

    /** To fill or not the polygon. */
    private boolean fill = true;


    // Constructor

    /**
     * The constructor of the polygon builder.
     *
     * @param cartesiansInput : The list of cartesians that will build the polygon.
     */
    public PolygonBuilder(final List<Cartesian> cartesiansInput) {
        this.cartesians = cartesiansInput;
    }

    /**
     * Function to set up an availability.
     *
     * @param availabilityInput : The availability to set up.
     * @return : The polygon builder with the given availability.
     */
    public PolygonBuilder withAvailability(final TimeInterval availabilityInput) {
        this.availability = availabilityInput;
        return this;
    }

    /**
     * Function to set up a color.
     *
     * @param colorInput : The color to set up.
     * @return : The polygon builder with the given color.
     */
    public PolygonBuilder withColor(final Color colorInput) {
        this.color = colorInput;
        return this;
    }

    /**
     * Function to set up the outline of the polygon.
     *
     * @param outlineInput : The outline to set up.
     * @return : The polygon builder with the given outline.
     */
    public PolygonBuilder withOutline(final boolean outlineInput) {
        this.outline = outlineInput;
        return this;
    }

    /**
     * Function to set up the fill of the polygon.
     *
     * @param fillInput : The fill to set up.
     * @return : The polygon builder with the given fill.
     */
    public PolygonBuilder withFill(final boolean fillInput) {
        this.fill = fillInput;
        return this;
    }

    /**
     * The build function that generates a polygon object.
     *
     * @return : A polygon object with the given parameters of the builder.
     */
    public Polygon build() {
        return new Polygon(cartesians, availability, color, outline, fill);
    }

}
