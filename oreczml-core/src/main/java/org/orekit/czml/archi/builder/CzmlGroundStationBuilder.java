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
import org.orekit.frames.TopocentricFrame;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Ground station builder class
 * <p>
 * Builder for the {@link CzmlGroundStation} class.
 * <p>
 *
 * @author Julien LEBLOND
 * @since 1.0.0
 */

public class CzmlGroundStationBuilder {


    /** The topocentric frame of the ground station. */
    private TopocentricFrame topocentricFrame;

    /** The list of topocentric frame when several ground stations are used. */
    private List<TopocentricFrame> multipleTopocentricFrame = new ArrayList<>();

    // Optional arguments
    /** The path of the model (if used) of the ground station. */
    private String modelPath = "";

    /** The paths of the models when multiple models are used. */
    private List<String> multipleModels = new ArrayList<>();

    /** The boolean to know id the ground station are multiple or not. */
    private boolean multipleStations;

    // Constructor

    /**
     * The constructor of the czml ground station builder.
     *
     * @param topocentricFrameInput : The topocentric frame where the station must be.
     */
    public CzmlGroundStationBuilder(final TopocentricFrame topocentricFrameInput) {
        this.topocentricFrame = topocentricFrameInput;
        this.multipleStations = false;
    }

    /**
     * The constructor of the czml ground station builder for multiple stations.
     *
     * @param topocentricFramesInput : The list of the topocentric frames where the stations must be.
     */
    public CzmlGroundStationBuilder(final List<TopocentricFrame> topocentricFramesInput) {
        this.multipleTopocentricFrame = topocentricFramesInput;
        this.multipleStations         = true;
    }

    /**
     * Function to set up a model for the ground station.
     *
     * @param modelPathInput : The model path to set up.
     * @return : The covariance display builder with the given model.
     */
    public CzmlGroundStationBuilder withModel(final String modelPathInput) {
        this.modelPath = modelPathInput;
        return this;
    }

    public CzmlGroundStationBuilder withModel(final List<String> modelPathsInput) {
        if (!multipleStations) {
            throw new OreCzmlException(OreCzmlMessages.MULTIPLE_MODEL_SINGLE_STATION);
        } else {
            this.multipleModels = modelPathsInput;
            return this;
        }
    }

    /**
     * The build function that generates the czml ground station object.
     *
     * @return : A czml ground station object with the given parameters of the builder.
     */
    public CzmlGroundStation build() throws URISyntaxException, IOException {
        if (!multipleStations) {
            return new CzmlGroundStation(topocentricFrame, modelPath);
        } else {
            if (modelPath != null) {
                return new CzmlGroundStation(multipleTopocentricFrame, modelPath);
            } else {
                return new CzmlGroundStation(multipleTopocentricFrame, multipleModels);
            }
        }
    }

}
