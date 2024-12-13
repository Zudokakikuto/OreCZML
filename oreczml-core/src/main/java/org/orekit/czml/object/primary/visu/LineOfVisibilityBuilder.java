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
package org.orekit.czml.object.primary.visu;

import org.orekit.czml.object.primary.Constellation;
import org.orekit.czml.object.primary.Header;
import org.orekit.czml.object.primary.Satellite;
import org.orekit.frames.TopocentricFrame;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

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

    /** The string to call stations in the id. */
    public static final String DEFAULT_STATIONS = "STATIONS : ";

    /**
     * The topocentric frame where the ground station is.
     */
    private TopocentricFrame topocentricFrame;

    /** Topocentrics when several stations are considered. */
    private final List<TopocentricFrame> topocentricFrames = new ArrayList<>();

    /**
     * The satellite observed.
     */
    private Satellite satellite;

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

    /** The constellation of the lines. */
    private Constellation constellation;

    /** Boolean to display the triangle or not. */
    private boolean displayTriangle = false;

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
        this.header           = headerInput;
    }

    public LineOfVisibilityBuilder(final TopocentricFrame topocentricFrameInput, final Constellation constellationInput,
                                   final Header headerInput) {
        this.constellation    = constellationInput;
        this.topocentricFrame = topocentricFrameInput;
        this.customID         = LineOfVisibility.DEFAULT_ID + topocentricFrameInput.getName() + "/" + constellationInput.getId();
        this.header           = headerInput;
    }

    public LineOfVisibilityBuilder(final List<TopocentricFrame> topocentricFramesInput, final Satellite satellite,
                                   final Header headerInput) {
        this.satellite = satellite;
        this.topocentricFrames.addAll(topocentricFramesInput);
        this.customID = LineOfVisibility.DEFAULT_ID + DEFAULT_STATIONS + topocentricFrames.size() + "/" + satellite.getId();
        this.header   = headerInput;
    }

    public LineOfVisibilityBuilder(final List<TopocentricFrame> topocentricFramesInput,
                                   final Constellation constellationInput, final Header headerInput) {
        this.constellation = constellationInput;
        this.topocentricFrames.addAll(topocentricFramesInput);
        this.customID = LineOfVisibility.DEFAULT_ID + DEFAULT_STATIONS + topocentricFrames.size() + "/" + constellationInput.getIds();
        this.header   = headerInput;
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

    public LineOfVisibilityBuilder withVisibilityTriangle() {
        this.displayTriangle = true;
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
        LineOfVisibility toReturn = null;
        if (satellite != null) {
            if (topocentricFrames.isEmpty()) {
                toReturn = new LineOfVisibility(topocentricFrame, satellite, angleOfAperture, customID, header);
            } else {
                toReturn = new LineOfVisibility(topocentricFrames, satellite, angleOfAperture, customID, header);
            }
        }
        if (constellation != null) {
            if (topocentricFrames.isEmpty()) {
                toReturn = new LineOfVisibility(topocentricFrame, constellation, angleOfAperture, customID, header);
            } else {
                toReturn = new LineOfVisibility(topocentricFrames, constellation, angleOfAperture, customID, header);
            }
        }
        if (displayTriangle) {
            assert toReturn != null;
            if (satellite != null && topocentricFrames.isEmpty()) {
                toReturn.displayTriangle();
            } else {
                if (constellation != null && !(topocentricFrames.isEmpty())) {
                    for (int i = 0; i < constellation.getTotalOfSatellite(); i++) {
                        for (int j = 0; j < topocentricFrames.size(); j++) {
                            toReturn.displaySingleTriangle(j);
                        }
                    }
                }
                else if (constellation != null) {
                    for (int i = 0; i < constellation.getTotalOfSatellite(); i++) {
                        toReturn.displaySingleTriangle(i);
                    }
                } else if (!(topocentricFrames.isEmpty())) {
                    for (int i = 0; i < topocentricFrames.size(); i++) {
                        toReturn.displaySingleTriangle(i);
                    }
                }
            }
        }
        return toReturn;
    }

}

