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
import org.orekit.czml.object.secondary.Clock;

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
    private Clock clock;

    /** The second object related to the show. */
    private Object object;

    // Constructors

    /**
     * The basic czml show constructor. It has no objects related to it.
     *
     * @param toShow : The boolean that represent if the object is displayed or
     *        not.
     * @param clock : The clock
     */
    public CzmlShow(final boolean toShow, final Clock clock) {
        this.toShow = toShow;
        this.clock = clock;
    }

    /**
     * The czml show constructor with an object related to it.
     *
     * @param toShow : The boolean that represent if the object is displayed or
     *        not.
     * @param clock : The clock
     * @param objectInput : The second object related to the show.
     */
    public CzmlShow(final boolean toShow, final Clock clock,
                    final Object objectInput) {
        this.toShow = toShow;
        this.clock = clock;
        this.object = objectInput;
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
                showWriter.writeInterval(clock.getAvailability());
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
    public Clock getClock() {
        return clock;
    }

    /**
     * Sets the clock.
     *
     * @param clock the clock
     */
    public void setClock(final Clock clock) {
        this.clock = clock;
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
    public Object getObject() {
        return object;
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
