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

import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.CentralBodyReferenceSystem;
import org.orekit.frames.FramesFactory;
import org.orekit.utils.Constants;
import org.orekit.utils.IERSConventions;

import java.awt.Color;

public class CentralBodyReferenceSystemBuilder {

    /** The default ID for the central body reference system. */
    public static final String DEFAULT_ID = "CENTRAL_BODY_REFERENCE_SYSTEM";

    /** The default name for the central body reference system. */
    public static final String DEFAULT_NAME = "Reference system of the central body";

    /** Default color for the X axis. */
    public static final Color DEFAULT_RED = new Color(255, 10, 10);

    /** Default color for the Y axis. */
    public static final Color DEFAULT_GREEN = new Color(10, 255, 10);

    /** Default color for the Z axis. */
    public static final Color DEFAULT_BLUE = new Color(10, 10, 255);

    /** The default body (the earth). */
    public static final OneAxisEllipsoid DEFAULT_BODY = new OneAxisEllipsoid(Constants.WGS84_EARTH_EQUATORIAL_RADIUS,
            Constants.WGS84_EARTH_FLATTENING, FramesFactory.getITRF(IERSConventions.IERS_2010, true));

    /** The color for the x-axis. */
    private Color color1 = DEFAULT_RED;

    /** The color for the x-axis. */
    private Color color2 = DEFAULT_BLUE;

    /** The color for the x-axis. */
    private Color color3 = DEFAULT_GREEN;

    /** The body that will be the reference for the system. */
    private OneAxisEllipsoid body = DEFAULT_BODY;

    /** The id of the central reference system. */
    private String id = DEFAULT_ID;

    /** The name of the central reference system. */
    private String name = DEFAULT_NAME;


    // Constructor

    /** The basic constructor for the central body reference system, it does not need an argument, all arguments have a default value. */
    public CentralBodyReferenceSystemBuilder() {
        // Empty
    }

    /**
     * Function to set up the colors of the axis.
     *
     * @param color1Input : The color to set up for the x-axis.
     * @param color2Input : The color to set up for the y-axis.
     * @param color3Input : The color to set up for the z-axis.
     * @return : The central body reference system builder with the given colors.
     */
    public CentralBodyReferenceSystemBuilder withColors(final Color color1Input, final Color color2Input,
                                                        final Color color3Input) {
        this.color1 = color1Input;
        this.color2 = color2Input;
        this.color3 = color3Input;
        return this;
    }

    /**
     * Function to set up the body.
     *
     * @param bodyInput : The body to set up.
     * @return : The central body reference system builder with the given body.
     */
    public CentralBodyReferenceSystemBuilder withBody(final OneAxisEllipsoid bodyInput) {
        this.body = bodyInput;
        return this;
    }

    /**
     * Function to set up the id.
     *
     * @param idInput : The id to set up.
     * @return : The central body reference system builder with the given id.
     */
    public CentralBodyReferenceSystemBuilder withId(final String idInput) {
        this.id = idInput;
        return this;
    }

    /**
     * Function to set up the name.
     *
     * @param nameInput : The name to set up.
     * @return : The central body reference system builder with the given name.
     */
    public CentralBodyReferenceSystemBuilder withName(final String nameInput) {
        this.name = nameInput;
        return this;
    }

    /**
     * The build function that generates a central body reference system object.
     *
     * @return : A central body reference system object with the given parameters of the builder.
     */
    public CentralBodyReferenceSystem build() {
        return new CentralBodyReferenceSystem(body, id, name, color1, color2, color3);
    }

}
