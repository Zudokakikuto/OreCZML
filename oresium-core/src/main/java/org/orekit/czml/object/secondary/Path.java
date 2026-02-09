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

import cesiumlanguagewriter.BooleanCesiumWriter;
import cesiumlanguagewriter.CesiumOutputStream;
import cesiumlanguagewriter.CesiumStreamWriter;
import cesiumlanguagewriter.PacketCesiumWriter;
import cesiumlanguagewriter.PathCesiumWriter;
import cesiumlanguagewriter.TimeInterval;
import org.orekit.czml.errors.OresiumException;
import org.orekit.czml.errors.OresiumMessages;

import java.io.StringWriter;

/**
 * Path class
 * <p>
 * This class aims at displaying the path of a given object.
 * </p>
 *
 * @author Julien LEBLOND.
 * @since 1.0.0
 */
public class Path
    extends
    AbstractSecondaryObject<Path> {

    /**
     * The availability of the object.
     */
    private final TimeInterval availability;

    /**
     * Whether to show or not the path.
     */
    private Boolean show;

    // Constructors

    /**
     * The constructor of the path object with no default parameters.
     *
     * @param availability : The time interval when the path is displayed.
     */
    public Path(final TimeInterval availability) {
        this.availability = availability;
        this.show = true;
    }

    /**
     * The constructor of the path object with no default parameters.
     *
     * @param availability : The time interval when the path is displayed.
     * @param show : To display or not the path.
     */
    public Path(final TimeInterval availability, final boolean show) {
        this.availability = availability;
        this.show = show;
    }

    // Overrides

    @Override
    public void write(final PacketCesiumWriter packetWriter,
                      final CesiumOutputStream output) {
        try (PathCesiumWriter pathCesiumWriter = packetWriter.getPathWriter()) {
            pathCesiumWriter.open(output);
            try (BooleanCesiumWriter showPath =
                pathCesiumWriter.openShowProperty()) {
                showPath.writeInterval(availability.getStart(),
                                       availability.getStop());
                showPath.writeBoolean(show);
            }
        }
    }

    @Override
    public String toString() {
        final StringWriter writerTemp = new StringWriter();
        final CesiumOutputStream output = new CesiumOutputStream(writerTemp);
        final CesiumStreamWriter streamWriter = new CesiumStreamWriter();
        output.setPrettyFormatting(true);
        try (PacketCesiumWriter packet = streamWriter.openPacket(output)) {
            this.write(packet, output);
        }
        return writerTemp.toString();
    }

    @Override
    public Path cloneObject() {
        final Path toReturn;
        if (this.availability != null) {
            if (this.show != null) {
                toReturn = new Path(this.availability, this.show);
            } else {
                toReturn = new Path(this.availability);
            }
        } else {
            throw new OresiumException(OresiumMessages.NOT_VALID_SECONDARY_OBJECT_FOR_CLONE);
        }
        return toReturn;
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
     * Is show boolean.
     *
     * @return the boolean
     */
    public boolean isShow() {
        return show;
    }

    /**
     * Gets show.
     *
     * @return the show
     */
    public boolean getShow() {
        return this.show;
    }
}
