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
package org.orekit.czml.object;

import cesiumlanguagewriter.BooleanCesiumWriter;
import cesiumlanguagewriter.CesiumOutputStream;
import cesiumlanguagewriter.CesiumStreamWriter;
import cesiumlanguagewriter.PacketCesiumWriter;
import cesiumlanguagewriter.PolylineCesiumWriter;
import cesiumlanguagewriter.TimeInterval;

import java.io.StringWriter;

/**
 * CZML Show class
 * <p>
 * This class represents the show objects that will allow other primary objects
 * to be displayed or not in a given time interval. It can be related to up to
 * two objects.
 * </p>
 *
 * @author Julien LEBLOND.
 * @since 1.0.0
 */
public class CzmlShow {

    /**
     * To show or not the object.
     */
    private boolean toShow;

    /**
     * When the object should be displayed or not.
     */
    private TimeInterval availability;

    /** The first object related to the show. */
    private Object object1;

    /** The second object related to the show. */
    private Object object2;

    // Constructors

    /**
     * The basic czml show constructor. It has no objects related to it.
     *
     * @param toShow : The boolean that represent if the object is displayed or
     *        not.
     * @param availability : The time interval when the object is displayed or
     *        not.
     */
    public CzmlShow(final boolean toShow, final TimeInterval availability) {
        this.toShow = toShow;
        this.availability = availability;
    }

    /**
     * The czml show constructor with two objects related to it.
     *
     * @param toShow : The boolean that represent if the object is displayed or
     *        not.
     * @param availability : The time interval when the object is displayed or
     *        not.
     * @param object1Input : The first object related to the show.
     * @param object2Input : The second object related to the show.
     */
    public CzmlShow(final boolean toShow, final TimeInterval availability,
                    final Object object1Input, final Object object2Input) {
        this.toShow = toShow;
        this.availability = availability;
        this.object1 = object1Input;
        this.object2 = object2Input;
    }

    // Display functions

    /**
     * This function aims at writing the czml show object into a polyline
     * writer, it is the only usage of the czml shw for the moment.
     *
     * @param packet : The packet that will contain the string written.
     * @param output : The output that will write into the czml file.
     */
    public void write(final PacketCesiumWriter packet,
                      final CesiumOutputStream output) {

        try (PolylineCesiumWriter polylineCesiumWriter =
            packet.getPolylineWriter()) {
            polylineCesiumWriter.open(output);
            try (BooleanCesiumWriter showWriter =
                polylineCesiumWriter.getShowWriter()) {
                showWriter.open(output);
                showWriter.writeInterval(availability);
                showWriter.writeBoolean(toShow);
            }
        }
    }

    @Override
    public String toString() {
        final StringWriter writer = new StringWriter();
        final CesiumOutputStream output = new CesiumOutputStream(writer);
        final CesiumStreamWriter streamWriter = new CesiumStreamWriter();
        output.setPrettyFormatting(true);
        try (PacketCesiumWriter packet = streamWriter.openPacket(output)) {
            this.write(packet, output);
        }
        return writer.toString();
    }

    // Getters

    /**
     * Gets availability.
     *
     * @return the availability
     */
    public TimeInterval getAvailability() {
        return availability;
    }

    /**
     * Sets availability.
     *
     * @param availability the availability
     */
    public void setAvailability(final TimeInterval availability) {
        this.availability = availability;
    }

    /**
     * Gets show.
     *
     * @return the show
     */
    public boolean getShow() {
        return toShow;
    }

    /**
     * Get the second object.
     *
     * @return the second object
     */
    public Object getObject2() {
        return object2;
    }

    // Setters

    /**
     * Sets show.
     *
     * @param toShow_temp the to show temp
     */
    public void setShow(final boolean toShow_temp) {
        this.toShow = toShow_temp;
    }
}
