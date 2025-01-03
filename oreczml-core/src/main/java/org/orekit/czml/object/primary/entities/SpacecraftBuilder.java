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
import java.util.ArrayList;
import java.util.List;

/**
 * Spacecraft builder class
 * <p>
 * Builder for the {@link Spacecraft} class.
 *
 * @author Julien LEBLOND
 * @since 1.0
 */
public class SpacecraftBuilder {

    /**
     * The default model path of the model for the Spacecraft.
     */
    public static final String DEFAULT_MODEL_PATH = "";

    /**
     * The default color of the orbit of the Spacecraft.
     */
    public static final Color DEFAULT_COLOR = new Color(255, 255, 255);

    /** The default format for the ID. */
    public static final String DEFAULT_FORMAT = "SPACECRAFT/" + "{P(%1.8e, %2.8e, %3.8e), V(%4.8e, %5.8e, %6.8e)}";

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
     * The model of the Spacecraft.
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
     * To display the attitude of the Spacecraft or not.
     */
    private boolean displayAttitude = false;

    /**
     * To display the reference system of the Spacecraft or not.
     */
    private boolean displayReferenceSystem = false;

    /**
     * The orientation that can be set up to have a personalized orientation.
     */
    private Orientation orientation;

    /** The custom ID of the Spacecraft. */
    private String customID;

    /** The header to consider when several are used. */
    private Header header;

    /** An optional rotation for the orientation. */
    private Rotation rotation;

    /** The list of bodies considered when influence sphere are computed. */
    private List<Body> bodies = new ArrayList<>();

    /** The boolean to display or not the influence sphere. */
    private boolean displayInfluenceSphere = false;

    // Constructor

    /**
     * The constructor of the builder.
     *
     * @param propagator : The propagator used to build the Spacecraft.
     * @param header     : The header considered.
     */
    public SpacecraftBuilder(final BoundedPropagator propagator, final Header header) {
        this.propagator = propagator;
        this.finalDate  = propagator.getMaxDate();
        this.startDate  = propagator.getMinDate();
        this.customID   = String.format(DEFAULT_FORMAT,
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
     * @return : The Spacecraft builder with the given model.
     * @throws URISyntaxException the uri syntax exception
     * @throws IOException        the io exception
     */
    public SpacecraftBuilder withModelPath(final String modelPathInput) throws URISyntaxException, IOException {
        this.modelPath = modelPathInput;
        return this;
    }

    /**
     * Function to set up a color.
     *
     * @param colorInput : The color to set up.
     * @return : The Spacecraft builder with the given color.
     */
    public SpacecraftBuilder withColor(final Color colorInput) {
        this.color = colorInput;
        return this;
    }

    /**
     * Function to set up a start date.
     *
     * @param startDateInput : The start date to set up.
     * @return : The Spacecraft builder with the given start date.
     */
    public SpacecraftBuilder withStartDate(final AbsoluteDate startDateInput) {
        this.startDate = startDateInput;
        return this;
    }

    /**
     * Function to set up a stop date.
     *
     * @param stopDateInput : The stop date to set up.
     * @return : The Spacecraft builder with the given stop date.
     */
    public SpacecraftBuilder withFinalDate(final AbsoluteDate stopDateInput) {
        this.finalDate = stopDateInput;
        return this;
    }

    /**
     * Function to display the period of the orbit.
     *
     * @return : The Spacecraft builder with the given period for the orbit displayed.
     */
    public SpacecraftBuilder withOnlyOnePeriod() {
        displayOnlyOnePeriod = true;
        return this;
    }

    /**
     * Function to set up the orientation of the Spacecraft.
     *
     * @return : The Spacecraft builder with the personalized orientation for the Spacecraft.
     */
    public SpacecraftBuilder withDisplayAttitude() {
        this.displayAttitude = true;
        return this;
    }

    /**
     * With orientation Spacecraft builder.
     *
     * @param orientationInput the orientation input
     * @return the Spacecraft builder
     */
    public SpacecraftBuilder withOrientation(final Orientation orientationInput) {
        this.orientation     = orientationInput;
        this.displayAttitude = true;
        return this;
    }

    /**
     * Function to display the reference system of the Spacecraft.
     *
     * @return : The Spacecraft builder with the reference system displayed.
     */
    public SpacecraftBuilder withReferenceSystem() {
        displayReferenceSystem = true;
        return this;
    }

    /**
     * Function to set up a custom ID.
     *
     * @param customIDInput : The custom ID to set up.
     * @return : The Spacecraft object with a custom ID.
     */
    public SpacecraftBuilder withCustomID(final String customIDInput) {
        this.customID = customIDInput;
        return this;
    }

    /**
     * Function to set up a custom ID.
     *
     * @param optionalRotationInput : The custom ID to set up.
     * @return : The Spacecraft object with a custom ID.
     */
    public SpacecraftBuilder withOptionalRotation(final Rotation optionalRotationInput) {
        this.rotation = optionalRotationInput;
        return this;
    }

    /**
     * Function to set up a header.
     *
     * @param headerInput : The header to set up.
     * @return : The Spacecraft object with a header.
     */
    public SpacecraftBuilder withHeader(final Header headerInput) {
        this.header = headerInput;
        return this;
    }

    public SpacecraftBuilder displayInfluenceSphereChanges(final List<Body> bodiesInput) {
        this.displayInfluenceSphere = true;
        this.bodies                 = bodiesInput;
        return this;
    }

    /**
     * The build function that generates a Spacecraft object.
     *
     * @return : A Spacecraft object with the given parameters of the builder.
     * @throws URISyntaxException the uri syntax exception
     * @throws IOException        the io exception
     */
    public Spacecraft build() throws URISyntaxException, IOException {
        final Spacecraft tempSpacecraft = new Spacecraft(propagator, startDate, finalDate, modelPath, color, customID,
                header);
        tempSpacecraft.getSpacecraftBoundedPropagator()
                      .clearStepHandlers();
        tempSpacecraft.getSpacecraftBoundedPropagator()
                      .clearEventsDetectors();
        return this.checkAttributes(tempSpacecraft);
    }

    /**
     * This function checks if the reference system, the attitude and the period of the orbit must be displayed or not.
     *
     * @param spacecraft : The Spacecraft object build with the build function.
     * @return : A Spacecraft with a reference system, an attitude and a period of the orbit, displayed or not.
     */
    private Spacecraft checkAttributes(final Spacecraft spacecraft) {
        if (displayOnlyOnePeriod) {
            spacecraft.displayOnlyOnePeriod();
        }
        if (displayAttitude) {
            if (orientation != null) {
                spacecraft.setAttitudes(orientation.getAttitudes());
            }
            spacecraft.displaySpacecraftAttitude();
        }
        if (displayReferenceSystem) {
            spacecraft.displaySpacecraftReferenceSystem();
        }
        if (rotation != null) {
            spacecraft.setOptionalRotation(rotation);
        }
        if (displayInfluenceSphere) {
            spacecraft.displayInfluenceSphereChanges(bodies);
        }
        return spacecraft;
    }
}
