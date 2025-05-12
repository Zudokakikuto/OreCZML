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
package org.orekit.czml.object.primary.entities;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.orekit.czml.object.primary.Header;
import org.orekit.frames.TopocentricFrame;

/**
 * Ground station builder class
 * <p>
 * Builder for the {@link CzmlGroundStation} class.
 *
 * @author Julien LEBLOND
 * @since 1.0.0
 */
public class CzmlGroundStationBuilder {

    /**
     * The topocentric frame of the ground station.
     */
    private final TopocentricFrame topocentricFrame;

    /**
     * The list of topocentric frame when several ground stations are used.
     */
    private final List<TopocentricFrame> multipleTopocentricFrame =
        new ArrayList<>();

    // Optional arguments
    /**
     * The path of the model (if used) of the ground station.
     */
    private String modelPath = "";

    /**
     * The paths of the models when multiple models are used.
     */
    private final List<String> multipleModels = new ArrayList<>();

    /** The custom ID for the czml ground station. */
    private String customID = CzmlGroundStation.DEFAULT_ID;

    /** The header to consider when several are used. */
    private Header header;

    /**
     * A boolean to display or not the circle of visibility of the ground
     * station.
     */
    private boolean displayCircle = false;

    /** The spacecraft considered. */
    private Spacecraft spacecraft;

    /** The angle of aperture of the ground station. */
    private double angleOfAperture;

    // Constructor

    /**
     * The constructor of the czml ground station builder.
     *
     * @param topocentricFrameInput : The topocentric frame where the station
     *        must be.
     * @param headerInput : The header considered.
     */
    public CzmlGroundStationBuilder(final TopocentricFrame topocentricFrameInput,
                                    final Header headerInput) {
        this.topocentricFrame = topocentricFrameInput;
        this.header = headerInput;
    }

    public CzmlGroundStationBuilder
        displayCircle(final Spacecraft spacecraftInput,
                      final double angleOfApertureInput) {
        displayCircle = true;
        spacecraft = spacecraftInput;
        angleOfAperture = angleOfApertureInput;
        return this;
    }

    /**
     * Function to set up a model for the ground station.
     *
     * @param modelPathInput : The model path(s) to set up.
     * @return : The czml ground station builder with the given model.
     */
    public CzmlGroundStationBuilder withModel(final String modelPathInput) {
        this.modelPath = modelPathInput;
        return this;
    }

    /**
     * Function to set up a custom ID.
     *
     * @param customIDInput : The custom ID to set up.
     * @return : The ground station object with a custom ID.
     */
    public CzmlGroundStationBuilder withCustomID(final String customIDInput) {
        this.customID = customIDInput;
        return this;
    }

    /**
     * Function to set up a header.
     *
     * @param headerInput : The header to set up.
     * @return : The ground station object with a header.
     */
    public CzmlGroundStationBuilder withHeader(final Header headerInput) {
        this.header = headerInput;
        return this;
    }

    /**
     * The build function that generates the czml ground station object.
     *
     * @return : A czml ground station object with the given parameters of the
     *         builder.
     * @throws URISyntaxException the uri syntax exception
     * @throws IOException the io exception
     */
    public CzmlGroundStation build()
        throws URISyntaxException,
            IOException {
        final CzmlGroundStation toReturn;
        toReturn = new CzmlGroundStation(topocentricFrame, modelPath, header);
        if (displayCircle) {
            toReturn.displayCircle(spacecraft, angleOfAperture);
        }
        return toReturn;
    }

}
