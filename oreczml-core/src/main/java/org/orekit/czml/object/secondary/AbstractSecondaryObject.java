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
package org.orekit.czml.object.secondary;

import cesiumlanguagewriter.CesiumOutputStream;
import cesiumlanguagewriter.CesiumStreamWriter;
import cesiumlanguagewriter.PacketCesiumWriter;

import java.io.StringWriter;

/**
 * The type Abstract secondary object.
 */
public abstract class AbstractSecondaryObject implements CzmlSecondaryObject {

    @Override
    public String toString() {
        final StringWriter       writer       = new StringWriter();
        final CesiumOutputStream output       = new CesiumOutputStream(writer);
        final CesiumStreamWriter streamWriter = new CesiumStreamWriter();
        output.setPrettyFormatting(true);
        try (PacketCesiumWriter packet = streamWriter.openPacket(output)) {
            this.write(packet, output);
        }
        return writer.toString();
    }
}
