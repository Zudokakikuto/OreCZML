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
package org.orekit.czml.ArchiObjects.Builders;

import cesiumlanguagewriter.CesiumArcType;
import cesiumlanguagewriter.Reference;
import cesiumlanguagewriter.TimeInterval;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.Header;
import org.orekit.czml.CzmlObjects.Polyline;

import java.awt.Color;

/**
 * Non-vector polyline builder class
 * <p>
 * Builder for the {@link Polyline} class to be built as a non-vector.
 * <p>
 *
 * @author Julien LEBLOND
 * @since 1.0.0
 */

public class NonVectorPolylineBuilder {

    /** The default color of the polyline. */
    public static final Color DEFAULT_COLOR = new Color(0, 255, 255, 255);

    /** The default width of the polyline. */
    public static final double DEFAULT_WIDTH = 1;

    /** The arc type of the polyline. (a straight line by default). The available parameters are: NONE, GEODESIC, RHUMB; */
    public static final CesiumArcType DEFAULT_ARC_TYPE = CesiumArcType.NONE;

    /** To show or not the polyline. */
    public static final boolean DEFAULT_SHOW = true;

    /** The default time interval when the polyline must be displayed. */
    public static final TimeInterval DEFAULT_AVAILABILITY = Header.getMasterClock()
                                                                  .getAvailability();

    /** The reference of the polyline. (to give one of the extremities of the polyline a position) */
    public static final Reference DEFAULT_REFERENCE = null;

    /** The default near distance to display the polyline. */
    public static final double DEFAULT_NEAR_DISTANCE = 1;

    /** The default far distance where the polyline must not be displayed anymore. */
    public static final double DEFAULT_FAR_DISTANCE = 100000000;

    /** The color of the polyline. */
    private Color color = DEFAULT_COLOR;

    /** The width of the polyline. */
    private double width = DEFAULT_WIDTH;

    /** The arc type of the polyline. I can take several type : NONE, GEODESIC, RHUMB. */
    private CesiumArcType arcType = DEFAULT_ARC_TYPE;

    /** To show or not the polyline. */
    private boolean show = DEFAULT_SHOW;

    /** The time interval when the polyline must be displayed. */
    private TimeInterval availability = DEFAULT_AVAILABILITY;

    /** The reference of the first extremity of the polyline. */
    private Reference firstReference = DEFAULT_REFERENCE;

    /** The reference of the second extremity of the polyline. */
    private Reference secondReference = DEFAULT_REFERENCE;

    /** The near distance where the polyline must be displayed. */
    private double nearDistance = DEFAULT_NEAR_DISTANCE;

    /** The far distance where the polyline must not be displayed anymore. */
    private double farDistance = DEFAULT_FAR_DISTANCE;

    // Constructor

    /** Empty constructor. */
    public NonVectorPolylineBuilder() {
    }

    /**
     * Function to set up a color.
     *
     * @param colorInput : The color to set up.
     * @return : The non-vector polyline builder with the given color.
     */
    public NonVectorPolylineBuilder withColor(final Color colorInput) {
        this.color = colorInput;
        return this;
    }

    /**
     * Function to set up a width.
     *
     * @param widthInput : The width to set up.
     * @return : The non-vector polyline builder with the given width.
     */
    public NonVectorPolylineBuilder withWidth(final double widthInput) {
        this.width = widthInput;
        return this;
    }

    /**
     * Function to set up an arc type.
     *
     * @param arcTypeInput : The arc type to set up.
     * @return : The non-vector polyline builder with the given arc type.
     */
    public NonVectorPolylineBuilder withArcType(final CesiumArcType arcTypeInput) {
        this.arcType = arcTypeInput;
        return this;
    }


    public NonVectorPolylineBuilder withShow(final boolean showInput) {
        this.show = showInput;
        return this;
    }

    /**
     * Function to set up an availability.
     *
     * @param availabilityInput : The availability to set up.
     * @return : The non-vector polyline builder with the given availability.
     */
    public NonVectorPolylineBuilder withAvailability(final TimeInterval availabilityInput) {
        this.availability = availabilityInput;
        return this;
    }

    /**
     * Function to set up a reference for the first extremity.
     *
     * @param firstReferenceInput : The reference for the first extremity to set up.
     * @return : The non-vector polyline builder with the given reference for the first extremity.
     */
    public NonVectorPolylineBuilder withFirstReference(final Reference firstReferenceInput) {
        this.firstReference = firstReferenceInput;
        return this;
    }

    /**
     * Function to set up a reference for the second extremity.
     *
     * @param secondReferenceInput : The reference for the second extremity to set up.
     * @return : The non-vector polyline builder with the given reference for the second extremity.
     */
    public NonVectorPolylineBuilder withSecondReference(final Reference secondReferenceInput) {
        this.secondReference = secondReferenceInput;
        return this;
    }

    /**
     * Function to set up a near distance.
     *
     * @param nearDistanceInput : The near distance to set up.
     * @return : The non-vector polyline builder with the given near distance.
     */
    public NonVectorPolylineBuilder withNearDistance(final double nearDistanceInput) {
        this.nearDistance = nearDistanceInput;
        return this;
    }

    /**
     * Function to set up a far distance.
     *
     * @param farDistanceInput : The far distance to set up.
     * @return : The non-vector polyline builder with the given far distance.
     */
    public NonVectorPolylineBuilder withFarDistance(final double farDistanceInput) {
        this.farDistance = farDistanceInput;
        return this;
    }

    /**
     * The build function that generates a polyline object defined as a non-vector.
     *
     * @return : A polyline object with the given parameters of the builder.
     */
    public Polyline build() {
        return new Polyline(firstReference, secondReference, availability, color, width, show, arcType, nearDistance,
                farDistance);
    }
}
