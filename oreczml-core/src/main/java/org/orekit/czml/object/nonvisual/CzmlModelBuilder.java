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

package org.orekit.czml.object.nonvisual;

import org.orekit.czml.object.secondary.Clock;

/**
 * This class aims at building the {@link CzmlModel} object.
 *
 * @author jleblond
 * @since 1.1
 */
public class CzmlModelBuilder {

    /** The absolute path to the model to load. */
    private final String pathToModel;

    /** If the model to load is a satellite or not. */
    private final boolean isSpacecraft;

    /** The clock to consider. */
    private final Clock clock;

    /** To show the model or not. */
    private boolean show = true;

    /** The scale of the model. */
    private double scale = 1;

    /**
     * The minimum pixel size of the model. It the minimum of pixels that will
     * be displayed on screen to show the model. (far away)
     */
    private double minimumPixelSize = 400;

    /**
     * The maximum pixel size of the model. It the maximum scale applied on the
     * model.
     */
    private double maximumScale = 5000000;

    /** The name of the object. */
    private String objectName;

    /**
     * The constructor of the {@link CzmlModelBuilder}.
     *
     * @param absolutePathToModel : The path to the model to load.
     * @param isSpacecraftInput : If the mode lto load is a satellite or not.
     * @param clock : The clock to consider.
     */
    public CzmlModelBuilder(final String absolutePathToModel,
                            final boolean isSpacecraftInput,
                            final Clock clock) {
        this.pathToModel = absolutePathToModel;
        this.isSpacecraft = isSpacecraftInput;
        this.clock = clock;
    }

    /**
     * Function to not display the model.
     *
     * @return The {@link CzmlModelBuilder} with a {@code show} set to false
     */
    public CzmlModelBuilder notShow() {
        this.show = false;
        return this;
    }

    /**
     * Function that sets the scale of the model.
     *
     * @param scaleInput : The scale to apply to the model
     * @return The {@link CzmlModelBuilder} with a given {@code scale}
     */
    public CzmlModelBuilder withScale(final double scaleInput) {
        this.scale = scaleInput;
        return this;
    }

    /**
     * Function that sets the minimum pixel size of the model.
     *
     * @param minimumPixelSizeInput : The minimum pixel size to apply to the
     *        model
     * @return The {@link CzmlModelBuilder} with a given
     *         {@code minimumPixelSizeInput}
     */
    public CzmlModelBuilder
        withMinimumPixelSize(final double minimumPixelSizeInput) {
        this.minimumPixelSize = minimumPixelSizeInput;
        return this;
    }

    /**
     * Function that sets the maximum pixel size of the model.
     *
     * @param maximumScaleInput : The maximum scale to apply to the model
     * @return The {@link CzmlModelBuilder} with a given
     *         {@code maximumPixelSizeInput}
     */
    public CzmlModelBuilder withMaximumScale(final double maximumScaleInput) {
        this.maximumScale = maximumScaleInput;
        return this;
    }

    /**
     * Function that sets the maximum pixel size of the model.
     *
     * @param objectNameInput : The object name input to apply to the model
     * @return The {@link CzmlModelBuilder} with a given
     *         {@code maximumPixelSizeInput}
     */
    public CzmlModelBuilder withObjectName(final String objectNameInput) {
        this.objectName = objectNameInput;
        return this;
    }

    /**
     * The build function that creates the Czml Model.
     *
     * @return : The Czml Model with all the parameters of the builder.
     */
    public CzmlModel build() {
        final CzmlModel toReturn =
            new CzmlModel(pathToModel, maximumScale, minimumPixelSize, scale,
                          isSpacecraft, clock);
        toReturn.setShow(show);
        if (objectName != null) {
            toReturn.setNameOfObject(objectName);
        }
        return toReturn;
    }
}
