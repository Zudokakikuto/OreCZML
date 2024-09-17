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
package org.orekit.czml.object;

import cesiumlanguagewriter.BooleanCesiumWriter;
import cesiumlanguagewriter.CesiumOutputStream;
import cesiumlanguagewriter.PolylineCesiumWriter;
import cesiumlanguagewriter.TimeInterval;

/**
 * CZML Show class
 *
 * <p>
 * This class represents the show objects that will allows other primary objects to be displayed or not in a given time interval.
 * </p>
 *
 * @author Julien LEBLOND.
 * @since 1.0.0
 */

public class CzmlShow {

    /** To show or not the object. */
    private boolean toShow;

    /** When the object should be displayed or not. */
    private TimeInterval availability;


    // Constructors

    /**
     * The basic czml show constructor.
     *
     * @param toShow       : The boolean that represent if the object is displayed or not.
     * @param availability : The time interval when the object is displayed or not.
     */
    public CzmlShow(final boolean toShow, final TimeInterval availability) {
        this.toShow       = toShow;
        this.availability = availability;
    }


    // Display functions

    /**
     * This function aims at writing the czml show object into a polyline writer, it is the only usage of the czml shw for the moment.
     *
     * @param polylineWriter : A polyline writer to write a polyline.
     * @param output         : The output that will write into the czml file.
     */
    public void write(final PolylineCesiumWriter polylineWriter, final CesiumOutputStream output) {

        try (BooleanCesiumWriter showWriter = polylineWriter.getShowWriter()) {
            showWriter.open(output);
            showWriter.writeInterval(availability);
            showWriter.writeBoolean(toShow);
        }
    }


    // Getters

    public TimeInterval getAvailability() {
        return availability;
    }

    public void setAvailability(final TimeInterval availability) {
        this.availability = availability;
    }

    public boolean getShow() {
        return toShow;
    }

    // Setters

    public void setShow(final boolean toShow_temp) {
        this.toShow = toShow_temp;
    }
}
