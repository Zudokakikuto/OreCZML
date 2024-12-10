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
package org.orekit.czml.archi.builder;

import org.orekit.czml.object.primary.Header;
import org.orekit.czml.object.primary.LineOfVisibility;
import org.orekit.czml.object.primary.Satellite;
import org.orekit.frames.TopocentricFrame;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Line of visibility builder class
 * <p>
 * Builder for the {@link LineOfVisibility} class.
 *
 * @author Julien LEBLOND
 * @since 1.0.0
 */
public class LineOfVisibilityBuilder {

    /**
     * The default angle of aperture of the ground station.
     */
    public static final double DEFAULT_ANGLE_OF_APERTURE = 80.0;

    /**
     * The topocentric frame where the ground station is.
     */
    private final TopocentricFrame topocentricFrame;

    /**
     * The satellite observed.
     */
    private final Satellite satellite;

    // Optional parameters
    /**
     * The angle of aperture of the station.
     */
    private double angleOfAperture = DEFAULT_ANGLE_OF_APERTURE;

    /**
     * The custom ID of the line of visibility.
     */
    private String customID;

    /** The header considered when several are used. */
    private Header header = null;

    // Constructor

    /**
     * The constructor of the line of visibility builder.
     *
     * @param topocentricFrameInput : The topocentric frame where the ground station is.
     * @param satelliteInput        : The satellite observed.
     * @param headerInput           : The header considered.
     */
    public LineOfVisibilityBuilder(final TopocentricFrame topocentricFrameInput, final Satellite satelliteInput,
                                   final Header headerInput) {
        this.satellite        = satelliteInput;
        this.topocentricFrame = topocentricFrameInput;
        this.customID         = LineOfVisibility.DEFAULT_ID + topocentricFrameInput.getName() + "/" + satelliteInput.getId();
        this.header = headerInput;
    }

    /**
     * Function to set up an angle of aperture.
     *
     * @param angleOfApertureInput : The angle of aperture to set up.
     * @return : The line of visibility builder with the given angle of aperture.
     */
    public LineOfVisibilityBuilder withAngleOfAperture(final double angleOfApertureInput) {
        this.angleOfAperture = angleOfApertureInput;
        return this;
    }

    /**
     * Function to set up a custom ID.
     *
     * @param customIDInput : The custom ID to set up.
     * @return : The line of visibility object with a custom ID.
     */
    public LineOfVisibilityBuilder withCustomID(final String customIDInput) {
        this.customID = customIDInput;
        return this;
    }

    /**
     * Function to set up a header.
     *
     * @param headerInput : The header to set up.
     * @return : The line of visibility object with a header.
     */
    public LineOfVisibilityBuilder withHeader(final Header headerInput) {
        this.header = headerInput;
        return this;
    }

    /**
     * The build function that generates a line of visibility object.
     *
     * @return : A line of visibility object with the given parameters of the builder.
     * @throws URISyntaxException the uri syntax exception
     * @throws IOException        the io exception
     */
    public LineOfVisibility build() throws URISyntaxException, IOException {
        return new LineOfVisibility(topocentricFrame, satellite, angleOfAperture, customID, header);
    }

}

