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

import org.orekit.czml.errors.OreCzmlException;
import org.orekit.czml.errors.OreCzmlMessages;
import org.orekit.czml.object.primary.CzmlGroundStation;
import org.orekit.czml.object.primary.Header;
import org.orekit.czml.object.primary.Satellite;
import org.orekit.czml.object.primary.StationVisibilityCircle;
import org.orekit.frames.TopocentricFrame;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    private TopocentricFrame topocentricFrame;

    /**
     * The list of topocentric frame when several ground stations are used.
     */
    private final List<TopocentricFrame> multipleTopocentricFrame = new ArrayList<>();

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

    /**
     * The boolean to know id the ground station are multiple or not.
     */
    private final boolean multipleStations;

    /** The header to consider when several are used. */
    private Header header = null;

    private boolean displayCircle = false;

    private Satellite satellite;

    private double angleOfAperture;

    // Constructor

    /**
     * The constructor of the czml ground station builder.
     *
     * @param topocentricFrameInput : The topocentric frame where the station must be.
     * @param headerInput           : The header considered.
     */
    public CzmlGroundStationBuilder(final TopocentricFrame topocentricFrameInput, final Header headerInput) {
        this.topocentricFrame = topocentricFrameInput;
        this.multipleStations = false;
        this.header = headerInput;
    }

    /**
     * The constructor of the czml ground station builder for multiple stations.
     *
     * @param topocentricFramesInput : The list of the topocentric frames where the stations must be.
     * @param headerInput            : The header considered.
     */
    public CzmlGroundStationBuilder(final List<TopocentricFrame> topocentricFramesInput, final Header headerInput) {
        this.multipleTopocentricFrame.addAll(topocentricFramesInput);
        this.multipleStations = true;
        this.header = headerInput;
    }


    public CzmlGroundStationBuilder displayCircle(final Satellite satellite, final double angleOfAperture) {
        displayCircle = true;
        this.satellite = satellite;
        this.angleOfAperture = angleOfAperture;
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
     * Function to set up all the models to use for the ground stations.
     * The number of model paths must be the same as the number of ground stations.
     *
     * @param modelPathsInput : The list of all the paths of all the models to load.
     * @return : The czml ground station with the given models
     */
    public CzmlGroundStationBuilder withModel(final List<String> modelPathsInput) {
        if (!multipleStations) {
            throw new OreCzmlException(OreCzmlMessages.MULTIPLE_MODEL_SINGLE_STATION);
        } else {
            this.multipleModels.addAll(modelPathsInput);
            return this;
        }
    }

    /**
     * Function to set up all the models to use for the ground stations.
     * The number of model paths must be the same as the number of ground stations.
     *
     * @param modelPathsInput :Several paths of the models to set up
     * @return : The czml ground station with the given models
     */
    public CzmlGroundStationBuilder withModel(final String... modelPathsInput) {
        if (!multipleStations) {
            throw new OreCzmlException(OreCzmlMessages.MULTIPLE_MODEL_SINGLE_STATION);
        } else {
            this.multipleModels.addAll(Arrays.asList(modelPathsInput));
            return this;
        }
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
     * @return : A czml ground station object with the given parameters of the builder.
     * @throws URISyntaxException the uri syntax exception
     * @throws IOException        the io exception
     */
    public CzmlGroundStation build() throws URISyntaxException, IOException {
        CzmlGroundStation toReturn = null;
        if (!multipleStations) {
            toReturn = new CzmlGroundStation(topocentricFrame, modelPath, header);
        } else {
            if (modelPath != null) {
                toReturn = new CzmlGroundStation(multipleTopocentricFrame, modelPath, header);
            } else {
                toReturn = new CzmlGroundStation(multipleTopocentricFrame, multipleModels, customID, header);
            }
        }
        if (displayCircle) {
            toReturn.displayCircle(satellite, angleOfAperture);
        }
        return toReturn;
    }

}
