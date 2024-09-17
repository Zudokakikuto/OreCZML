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
import org.orekit.czml.object.Polyline;

import java.awt.Color;
import java.util.List;

/**
 * Vector polyline builder class
 * <p>
 * Builder for the {@link Polyline} class to be built as a vector.
 * <p>
 *
 * @author Julien LEBLOND
 * @since 1.0.0
 */

public class VectorPolylineBuilder {

    /** The default color of the polyline. */
    public static final Color DEFAULT_COLOR = new Color(0, 255, 255, 255);

    /** The default time interval when the polyline must be displayed. */
    public static final TimeInterval DEFAULT_AVAILABILITY = Header.getMasterClock()
                                                                  .getAvailability();

    /** The default near distance where the polyline is displayed. */
    public static final double DEFAULT_NEAR_DISTANCE = 1;

    /** The default far distance where the polyline must not be displayed anymore. */
    public static final double DEFAULT_FAR_DISTANCE = 100000000;


    /** The color of the polyline. */
    private Color color = DEFAULT_COLOR;

    /** The time interval when the polyline must be displayed. */
    private TimeInterval availability = DEFAULT_AVAILABILITY;

    /** The near distance where the polyline must be displayed. */
    private double nearDistance = DEFAULT_NEAR_DISTANCE;

    /** The far distance where the polyline must not be displayed anymore. */
    private double farDistance = DEFAULT_FAR_DISTANCE;

    /** A list of cartesian that represents the extremities of the polyline. */
    private List<Cartesian> cartesianList;


    // Constructors

    /**
     * The constructor of the vector polyline builder.
     *
     * @param cartesiansInput : The list of cartesian that represents the extremities of the polyline.
     */
    public VectorPolylineBuilder(final List<Cartesian> cartesiansInput) {
        this.cartesianList = cartesiansInput;
    }

    /**
     * Function to set up a color.
     *
     * @param colorInput : The color to set up.
     * @return : The vector polyline builder with the given color.
     */
    public VectorPolylineBuilder withColor(final Color colorInput) {
        this.color = colorInput;
        return this;
    }

    /**
     * Function to set up an availability.
     *
     * @param availabilityInput : The availability to set up.
     * @return : The vector polyline builder with the given availability.
     */
    public VectorPolylineBuilder withAvailability(final TimeInterval availabilityInput) {
        this.availability = availabilityInput;
        return this;
    }

    /**
     * Function to set up a near distance.
     *
     * @param nearDistanceInput : The near distance to set up.
     * @return : The vector polyline builder with the given near distance.
     */
    public VectorPolylineBuilder withNearDistance(final double nearDistanceInput) {
        this.nearDistance = nearDistanceInput;
        return this;
    }

    /**
     * Function to set up a far distance.
     *
     * @param farDistanceInput : The far distance to set up.
     * @return : The vector polyline builder with the given far distance.
     */
    public VectorPolylineBuilder withFarDistance(final double farDistanceInput) {
        this.farDistance = farDistanceInput;
        return this;
    }

    /**
     * The build function that generates a polyline object defined as a vector.
     *
     * @return : A polyline object with the given parameters of the builder.
     */
    public Polyline build() {
        return new Polyline(cartesianList, availability, color, nearDistance, farDistance);
    }

}
