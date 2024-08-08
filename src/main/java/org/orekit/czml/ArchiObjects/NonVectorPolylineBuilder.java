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

import cesiumlanguagewriter.CesiumArcType;
import cesiumlanguagewriter.Reference;
import cesiumlanguagewriter.TimeInterval;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.Header;
import org.orekit.czml.CzmlObjects.Polyline;

import java.awt.Color;

public class NonVectorPolylineBuilder {

    /** . */
    public static final Color DEFAULT_COLOR = new Color(0, 255, 255, 255);

    /** . */
    public static final double DEFAULT_WIDTH = 1;

    /** . */
    public static final CesiumArcType DEFAULT_ARC_TYPE = CesiumArcType.NONE;

    /** . */
    public static final boolean DEFAULT_SHOW = true;

    /** . */
    public static final TimeInterval DEFAULT_AVAILABILITY = Header.getMasterClock()
                                                                  .getAvailability();

    /** . */
    public static final Reference DEFAULT_REFERENCE = null;

    /** . */
    public static final double DEFAULT_NEAR_DISTANCE = 1;

    /** . */
    public static final double DEFAULT_FAR_DISTANCE = 100000000;

    /** . */
    private Color color = DEFAULT_COLOR;

    /** . */
    private double width = DEFAULT_WIDTH;

    /** . */
    private CesiumArcType arcType = DEFAULT_ARC_TYPE;

    /** . */
    private boolean show = DEFAULT_SHOW;

    /** . */
    private TimeInterval availability = DEFAULT_AVAILABILITY;

    /** . */
    private Reference firstReference = DEFAULT_REFERENCE;

    /** . */
    private Reference secondReference = DEFAULT_REFERENCE;

    /** . */
    private double nearDistance = DEFAULT_NEAR_DISTANCE;

    /** . */
    private double farDistance = DEFAULT_FAR_DISTANCE;

    public NonVectorPolylineBuilder() {
    }

    public NonVectorPolylineBuilder withColor(final Color colorInput) {
        this.color = colorInput;
        return this;
    }

    public NonVectorPolylineBuilder withWidth(final double widthInput) {
        this.width = widthInput;
        return this;
    }

    public NonVectorPolylineBuilder withArcType(final CesiumArcType arcTypeInput) {
        this.arcType = arcTypeInput;
        return this;
    }

    public NonVectorPolylineBuilder withShow(final boolean showInput) {
        this.show = showInput;
        return this;
    }

    public NonVectorPolylineBuilder withAvailability(final TimeInterval availabilityInput) {
        this.availability = availabilityInput;
        return this;
    }

    public NonVectorPolylineBuilder withFirstReference(final Reference firstReferenceInput) {
        this.firstReference = firstReferenceInput;
        return this;
    }

    public NonVectorPolylineBuilder withSecondReference(final Reference secondReferenceInput) {
        this.secondReference = secondReferenceInput;
        return this;
    }

    public NonVectorPolylineBuilder withNearDistance(final double nearDistanceInput) {
        this.nearDistance = nearDistanceInput;
        return this;
    }

    public NonVectorPolylineBuilder withFarDistance(final double farDistanceInput) {
        this.farDistance = farDistanceInput;
        return this;
    }

    public Polyline build() {
        return new Polyline(firstReference, secondReference, availability, color, width, show, arcType, nearDistance,
                farDistance);
    }
}
