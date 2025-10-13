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
package org.orekit.czml.object.primary;

import cesiumlanguagewriter.CesiumOutputStream;
import cesiumlanguagewriter.CesiumStreamWriter;
import cesiumlanguagewriter.TimeInterval;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * CZML Primary Object Interface
 * <p>
 * This interface represents the objects that are directly displayed on
 * screen,they depend on no other objects.
 *
 * @author Julien LEBLOND
 * @since 1.0.0
 */
public interface CzmlPrimaryObject {

    // Overrides methods

    /**
     * The classic method that writes the object into the CzmlFile.
     *
     * @param stream : The stream that converts all the strings into
     *        understandable string for the CzmlFile
     * @param output : The output stream of cesium that will contain the strings
     *        to write into the CzmLFile.
     * @throws URISyntaxException the uri syntax exception
     * @throws IOException the io exception
     */
    void writeCzmlBlock(CesiumStreamWriter stream, CesiumOutputStream output)
        throws URISyntaxException,
            IOException;

    /**
     * Gets id.
     *
     * @return the id
     */
    String getId();

    /**
     * Sets id.
     *
     * @param s the s
     */
    void setId(String s);

    /**
     * Gets name.
     *
     * @return the name
     */
    String getName();

    /**
     * Sets name.
     *
     * @param name the name
     */
    void setName(String name);

    /**
     * Gets availability.
     *
     * @return the availability
     */
    TimeInterval getAvailability();

    /**
     * Sets availability.
     *
     * @param interval the interval
     */
    void setAvailability(TimeInterval interval);

    String toString();
}
