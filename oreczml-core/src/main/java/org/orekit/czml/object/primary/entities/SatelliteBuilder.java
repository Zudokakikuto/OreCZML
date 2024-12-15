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

import org.hipparchus.geometry.euclidean.threed.Rotation;
import org.orekit.czml.object.primary.Header;
import org.orekit.czml.object.secondary.Orientation;
import org.orekit.propagation.BoundedPropagator;
import org.orekit.time.AbsoluteDate;

import java.awt.Color;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Satellite builder class
 * <p>
 * Builder for the {@link Satellite} class.
 *
 * @author Julien LEBLOND
 * @since 1.0
 */
public class SatelliteBuilder {

    /**
     * The default model path of the model for the satellite.
     */
    public static final String DEFAULT_MODEL_PATH = "";

    /**
     * The default color of the orbit of the satellite.
     */
    public static final Color DEFAULT_COLOR = new Color(255, 255, 255);

    /** The default format for the ID. */
    public static final String DEFAULT_FORMAT = "SAT/" + "{P(%1.8e, %2.8e, %3.8e), V(%4.8e, %5.8e, %6.8e)}";

    // Optional parameters
    /**
     * The bounded propagator used for propagation.
     */
    private final BoundedPropagator propagator;

    /**
     * The final date of the propagation.
     */
    private AbsoluteDate finalDate;

    /**
     * The start date of the propagation.
     */
    private AbsoluteDate startDate;

    /**
     * The model of the satellite.
     */
    private String modelPath = DEFAULT_MODEL_PATH;

    /**
     * The color of the orbit.
     */
    private Color color = DEFAULT_COLOR;

    /**
     * To display or not only one period of the orbit.
     */
    private boolean displayOnlyOnePeriod = false;

    // Intrinsic parameters
    /**
     * To display the attitude of the satellite or not.
     */
    private boolean displayAttitude = false;

    /**
     * To display the reference system of the satellite or not.
     */
    private boolean displayReferenceSystem = false;

    /**
     * The orientation that can be set up to have a personalized orientation.
     */
    private Orientation orientation;

    /** The custom ID of the satellite. */
    private String customID;

    /** The header to consider when several are used. */
    private Header header;

    /** An optional rotation for the orientation. */
    private Rotation rotation;

    // Constructor

    /**
     * The constructor of the builder.
     *
     * @param propagator : The propagator used to build the satellite.
     * @param header     : The header considered.
     */
    public SatelliteBuilder(final BoundedPropagator propagator, final Header header) {
        this.propagator = propagator;
        this.finalDate  = propagator.getMaxDate();
        this.startDate  = propagator.getMinDate();
        this.customID = String.format(DEFAULT_FORMAT,
                propagator.getInitialState()
                          .getPosition()
                          .getX(),
                propagator.getInitialState()
                          .getPosition()
                          .getY(),
                propagator.getInitialState()
                          .getPosition()
                          .getZ(),
                propagator.getInitialState()
                          .getPVCoordinates()
                          .getVelocity()
                          .getX(),
                propagator.getInitialState()
                          .getPVCoordinates()
                          .getVelocity()
                          .getY(),
                propagator.getInitialState()
                          .getPVCoordinates()
                          .getVelocity()
                          .getZ());

        this.header = header;
    }

    /**
     * Function to set up a model.
     *
     * @param modelPathInput : The model to set up.
     * @return : The satellite builder with the given model.
     * @throws URISyntaxException the uri syntax exception
     * @throws IOException        the io exception
     */
    public SatelliteBuilder withModelPath(final String modelPathInput) throws URISyntaxException, IOException {
        this.modelPath = modelPathInput;
        return this;
    }

    /**
     * Function to set up a color.
     *
     * @param colorInput : The color to set up.
     * @return : The satellite builder with the given color.
     */
    public SatelliteBuilder withColor(final Color colorInput) {
        this.color = colorInput;
        return this;
    }

    /**
     * Function to set up a start date.
     *
     * @param startDateInput : The start date to set up.
     * @return : The satellite builder with the given start date.
     */
    public SatelliteBuilder withStartDate(final AbsoluteDate startDateInput) {
        this.startDate = startDateInput;
        return this;
    }

    /**
     * Function to set up a stop date.
     *
     * @param stopDateInput : The stop date to set up.
     * @return : The satellite builder with the given stop date.
     */
    public SatelliteBuilder withFinalDate(final AbsoluteDate stopDateInput) {
        this.finalDate = stopDateInput;
        return this;
    }

    /**
     * Function to display the period of the orbit.
     *
     * @return : The satellite builder with the given period for the orbit displayed.
     */
    public SatelliteBuilder withOnlyOnePeriod() {
        displayOnlyOnePeriod = true;
        return this;
    }

    /**
     * Function to set up the orientation of the satellite.
     *
     * @return : The satellite builder with the personalized orientation for the satellite.
     */
    public SatelliteBuilder withDisplayAttitude() {
        this.displayAttitude = true;
        return this;
    }

    /**
     * With orientation satellite builder.
     *
     * @param orientationInput the orientation input
     * @return the satellite builder
     */
    public SatelliteBuilder withOrientation(final Orientation orientationInput) {
        this.orientation     = orientationInput;
        this.displayAttitude = true;
        return this;
    }

    /**
     * Function to display the reference system of the satellite.
     *
     * @return : The satellite builder with the reference system displayed.
     */
    public SatelliteBuilder withReferenceSystem() {
        displayReferenceSystem = true;
        return this;
    }

    /**
     * Function to set up a custom ID.
     *
     * @param customIDInput : The custom ID to set up.
     * @return : The satellite object with a custom ID.
     */
    public SatelliteBuilder withCustomID(final String customIDInput) {
        this.customID = customIDInput;
        return this;
    }

    /**
     * Function to set up a custom ID.
     *
     * @param optionalRotationInput : The custom ID to set up.
     * @return : The satellite object with a custom ID.
     */
    public SatelliteBuilder withOptionalRotation(final Rotation optionalRotationInput) {
        this.rotation = optionalRotationInput;
        return this;
    }

    /**
     * Function to set up a header.
     *
     * @param headerInput : The header to set up.
     * @return : The satellite object with a header.
     */
    public SatelliteBuilder withHeader(final Header headerInput) {
        this.header = headerInput;
        return this;
    }

    /**
     * The build function that generates a satellite object.
     *
     * @return : A satellite object with the given parameters of the builder.
     * @throws URISyntaxException the uri syntax exception
     * @throws IOException        the io exception
     */
    public Satellite build() throws URISyntaxException, IOException {
        final Satellite tempSatellite = new Satellite(propagator, startDate, finalDate, modelPath, color, customID,
                header);
        tempSatellite.getSatelliteBoundedPropagator()
                     .clearStepHandlers();
        tempSatellite.getSatelliteBoundedPropagator()
                     .clearEventsDetectors();
        return this.checkAttributes(tempSatellite);
    }

    /**
     * This function checks if the reference system, the attitude and the period of the orbit must be displayed or not.
     *
     * @param satellite : The satellite object build with the build function.
     * @return : A satellite with a reference system, an attitude and a period of the orbit, displayed or not.
     */
    private Satellite checkAttributes(final Satellite satellite) {
        if (displayOnlyOnePeriod) {
            satellite.displayOnlyOnePeriod();
        }
        if (displayAttitude) {
            if (orientation != null) {
                satellite.setAttitudes(orientation.getAttitudes());
            }
            satellite.displaySatelliteAttitude();
        }
        if (displayReferenceSystem) {
            satellite.displaySatelliteReferenceSystem();
        }
        if (rotation != null) {
            satellite.setOptionalRotation(rotation);
        }
        return satellite;
    }
}
