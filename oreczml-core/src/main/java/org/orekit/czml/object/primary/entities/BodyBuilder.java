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

package org.orekit.czml.object.primary.entities;

import org.orekit.bodies.CelestialBody;
import org.orekit.czml.object.secondary.Clock;
import org.orekit.czml.object.secondary.Orientation;
import org.orekit.frames.Frame;

/**
 * body builder class
 * <p>
 * Builder for the {@link Body} class.
 *
 * @author Julien LEBLOND
 * @since 1.0
 */
public class BodyBuilder {

    /** . */
    private final CelestialBody body;

    /** The path to the model to load. */
    private final String pathToModel;

    /** . */
    private String customId;

    /** The clock to use. */
    private final Clock clock;

    /** To know if the orbit must be displayed or not. */
    private boolean noOrbitDisplay;

    /** To know if one period must be displayed or not for the body. */
    private boolean displayOnlyOnePeriod;

    /** The period of the orbit. */
    private double period;

    /** The maximum scale of the model. */
    private double modelMaximumScale;

    /** The minimum pixel size of the model. */
    private double modelMinimumPixelSize;

    /** The scale of the model. */
    private double modelScale;

    /** The orientation of the model. */
    private Orientation orientation;

    /** The description of the model. */
    private String description;

    /** To display or not the influence sphere. */
    private boolean displayInfluenceSphere = false;

    /** The frame in which the body must be computed. */
    private Frame frameToExpress;

    /** The central body associated to the body. */
    private Body centralBody;

    /**
     * The body builder constructor.
     *
     * @param bodyInput : The body to consider
     * @param pathToModelInput : The model to load
     * @param frameToExpressInput : The model to load
     * @param clock : The clock considered.
     * @param centralBody : The central body considered
     */
    public BodyBuilder(final CelestialBody bodyInput,
                       final String pathToModelInput,
                       final Frame frameToExpressInput, final Clock clock,
                       final Body centralBody) {
        this.body = bodyInput;
        this.pathToModel = pathToModelInput;
        this.customId = "BODY/" + bodyInput.getName();
        this.clock = clock.cloneObject();
        this.frameToExpress = frameToExpressInput;
        this.centralBody = centralBody;
    }

    /**
     * Function to display the influence sphere.
     *
     * @return The body builder object with an influence sphere to display
     */
    public BodyBuilder displayInfluenceSphere() {
        this.displayInfluenceSphere = true;
        return this;
    }

    /**
     * Function to set up a custom ID.
     *
     * @param customIDInput : The custom ID to set up
     * @return : The builder with a custom ID set up.
     */
    public BodyBuilder withCustomID(final String customIDInput) {
        this.customId = customIDInput;
        return this;
    }

    /**
     * Function to display only one period.
     *
     * @param periodInput : The period of the orbit.
     * @return : The builder with one period to display.
     */
    public BodyBuilder displayOnlyOnePeriod(final double periodInput) {
        displayOnlyOnePeriod = true;
        this.period = periodInput;
        return this;
    }

    /**
     * With model maximum scale body.
     *
     * @param modelMaximumScaleInput the model maximum scale input
     * @return the body
     */
    public BodyBuilder
        withModelMaximumScale(final double modelMaximumScaleInput) {
        this.modelMaximumScale = modelMaximumScaleInput;
        return this;
    }

    /**
     * With model minimum pixel size body.
     *
     * @param modelMinimumPixelSizeInput the model minimum pixel size input
     * @return the body
     */
    public BodyBuilder
        withModelMinimumPixelSize(final double modelMinimumPixelSizeInput) {
        this.modelMinimumPixelSize = modelMinimumPixelSizeInput;
        return this;
    }

    /**
     * With model scale body.
     *
     * @param modelScaleInput the model scale input
     * @return the body
     */
    public BodyBuilder withModelScale(final double modelScaleInput) {
        this.modelScale = modelScaleInput;
        return this;
    }

    /**
     * With orientation body.
     *
     * @param orientationInput the orientation input
     * @return the body
     */
    public BodyBuilder withOrientation(final Orientation orientationInput) {
        this.orientation = orientationInput.cloneObject();
        return this;
    }

    /**
     * With custom expressed frame.
     *
     * @param frameToExpressInput : The frame to define the position of the
     *        body.
     * @return : The body builder object with a custom frame to express the
     *         position of the body.
     */
    public BodyBuilder
        withCustomExpressedFrame(final Frame frameToExpressInput) {
        this.frameToExpress = frameToExpressInput;
        return this;
    }

    /**
     * With description body.
     *
     * @param descriptionInput : The description input
     * @return the body
     */
    public BodyBuilder withDescription(final String descriptionInput) {
        this.description = descriptionInput;
        return this;
    }

    /**
     * Function to not display the orbit.
     *
     * @return : The builder with no orbit displayed.
     */
    public BodyBuilder noOrbitDisplay() {
        noOrbitDisplay = true;
        return this;
    }

    /**
     * Function to set up a custom central body.
     *
     * @param centralBodyInput The central body to input
     * @return The builder with a custom central body
     */
    public BodyBuilder withCentralBody(final Body centralBodyInput) {
        this.centralBody = centralBodyInput.cloneObject();
        return this;
    }

    /**
     * The build function that generates the body object.
     *
     * @return : A body object with the given parameters of the builder.
     */
    public Body build() {
        final Body tempBody =
            new Body(body, pathToModel, frameToExpress, customId, clock,
                     centralBody);
        return checkAttributes(tempBody);
    }

    private Body checkAttributes(final Body bodyInput) {
        if (orientation != null) {
            bodyInput.withOrientation(orientation);
        }
        if (modelScale != 0.0) {
            bodyInput.withModelScale(modelScale);
        }
        if (modelMaximumScale != 0.0) {
            bodyInput.withModelMaximumScale(modelMaximumScale);
        }
        if (modelMinimumPixelSize != 0.0) {
            bodyInput.withModelMinimumPixelSize(modelMinimumPixelSize);
        }
        if (!(description == null)) {
            bodyInput.withDescription(description);
        }
        if (noOrbitDisplay) {
            bodyInput.noOrbitDisplay();
        }
        if (displayOnlyOnePeriod) {
            bodyInput.displayOnlyOnePeriod(period);
        }
        if (displayInfluenceSphere) {
            bodyInput.displayInfluenceSphere();
        }
        return bodyInput;
    }
}
