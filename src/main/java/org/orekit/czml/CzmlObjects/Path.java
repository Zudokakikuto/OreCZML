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
package org.orekit.czml.CzmlObjects;

import cesiumlanguagewriter.BooleanCesiumWriter;
import cesiumlanguagewriter.CesiumOutputStream;
import cesiumlanguagewriter.PacketCesiumWriter;
import cesiumlanguagewriter.PathCesiumWriter;
import cesiumlanguagewriter.TimeInterval;
import org.orekit.czml.CzmlObjects.CzmlSecondaryObjects.CzmlSecondaryObject;

/**
 * Path class
 *
 * <p>
 * This class aims at displaying the path of a given object.
 * </p>
 *
 * @author Julien LEBLOND.
 * @since 1.0.0
 */

public class Path implements CzmlSecondaryObject {

    /** The availability of the object. */
    private TimeInterval availability;

    /** To write the path. */
    private PathCesiumWriter writer;

    /** Whether to show or not the path. */
    private boolean show;


    public Path(final TimeInterval availability, final PacketCesiumWriter writer, final boolean show) {
        this.availability = availability;
        this.writer       = writer.getPathWriter();
        this.show         = show;
    }

    public Path(final TimeInterval availability, final PacketCesiumWriter writer) {
        this.availability = availability;
        this.writer       = writer.getPathWriter();
        this.show         = true;
    }

    public Path(final TimeInterval availability, final PacketCesiumWriter writer, final double period) {
        this.availability    = availability;
        this.writer          = writer.getPathWriter();
        this.show            = true;
    }

    @Override
    public void write(final PacketCesiumWriter packetWriter, final CesiumOutputStream output) {
        try (BooleanCesiumWriter showPath = writer.openShowProperty()) {
            showPath.writeInterval(availability.getStart(), availability.getStop());
            showPath.writeBoolean(show);
        }
    }

    public boolean getShow() {
        return this.show;
    }
}
