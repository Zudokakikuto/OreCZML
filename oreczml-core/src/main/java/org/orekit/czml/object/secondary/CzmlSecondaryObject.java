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
import cesiumlanguagewriter.PacketCesiumWriter;

/**
 * CZML Secondary Object Interface
 *
 * <p>
 * This interface represents the objects that are not directly displayed on screen,they depends to primary objects.
 * </p>
 *
 * @author Julien LEBLOND
 * @since 1.0
 */
public interface CzmlSecondaryObject {

    /**
     * Write.
     *
     * @param packetWriter the packet writer
     * @param output       the output
     */
    void write(PacketCesiumWriter packetWriter, CesiumOutputStream output);

    String toString();
}
