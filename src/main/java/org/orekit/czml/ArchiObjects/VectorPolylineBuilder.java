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
package org.orekit.czml.ArchiObjects;

import cesiumlanguagewriter.Cartesian;
import cesiumlanguagewriter.TimeInterval;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.Header;
import org.orekit.czml.CzmlObjects.Polyline;

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

    /** . */
    public static final Color DEFAULT_COLOR = new Color(0, 255, 255, 255);

    /** . */
    public static final TimeInterval DEFAULT_AVAILABILITY = Header.getMasterClock()
                                                                  .getAvailability();

    /** . */
    public static final double DEFAULT_NEAR_DISTANCE = 1;

    /** . */
    public static final double DEFAULT_FAR_DISTANCE = 100000000;


    /** . */
    private List<Cartesian> cartesianList;

    /** . */
    private TimeInterval availability = DEFAULT_AVAILABILITY;

    /** . */
    private Color color = DEFAULT_COLOR;

    /** . */
    private double nearDistance = DEFAULT_NEAR_DISTANCE;

    /** . */
    private double farDistance = DEFAULT_FAR_DISTANCE;


    public VectorPolylineBuilder(final List<Cartesian> cartesiansInput) {
        this.cartesianList = cartesiansInput;
    }

    public VectorPolylineBuilder withColor(final Color colorInput) {
        this.color = colorInput;
        return this;
    }

    public VectorPolylineBuilder withAvailability(final TimeInterval availabilityInput) {
        this.availability = availabilityInput;
        return this;
    }

    public VectorPolylineBuilder withNearDistance(final double nearDistanceInput) {
        this.nearDistance = nearDistanceInput;
        return this;
    }

    public VectorPolylineBuilder withFarDistance(final double farDistanceInput) {
        this.farDistance = farDistanceInput;
        return this;
    }

    public Polyline build() {
        return new Polyline(cartesianList, availability, color, nearDistance, farDistance);
    }

}
