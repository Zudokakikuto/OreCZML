/* Copyright 2002-2026 CS GROUP
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

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.czml.object.nonvisual.CzmlModel;
import org.orekit.time.AbsoluteDate;
import org.orekit.utils.PVCoordinatesProvider;
import org.orekit.utils.TimeStampedPVCoordinates;

/**
 * GroundVehicle builder class
 * <p>
 * Builder for the {@link GroundVehicle} class.
 *
 * @author Brianna Aubin
 * @since 1.1
 */
public class GroundVehicleBuilder {

    /**
     * The default model path of the model for the GroundVehicle.
     */
    public static final String DEFAULT_MODEL_PATH =
        new File(GroundVehicle.class.getClassLoader()
            .getResource("airplane.glb").getFile()).toPath().toString();

    /**
     * The default color of the trajectory of the GroundVehicle.
     */
    public static final Color DEFAULT_COLOR = new Color(255, 255, 255);

    /** The default format for the ID. */
    public static final String DEFAULT_FORMAT =
        "GROUNDVEHICLE/" + "{P(%1.8e, %2.8e, %3.8e), V(%4.8e, %5.8e, %6.8e)}";

    /**
     * The default name of the GroundVehicle.
     */
    public static final String DEFAULT_NAME = "GroundVehicle";

    // Optional parameters

    /**
     * The coords provider used for propagation.
     */
    private PVCoordinatesProvider coordsProvider;

    /**
     * The start date of the propagation.
     */
    private AbsoluteDate startDate;

    /**
     * The final date of the propagation.
     */
    private AbsoluteDate finalDate;

    /**
     * The timestep multiplier.
     */
    private double clockMultiplier;

    /**
     * The path to the model file for the GroundVehicle.
     */
    private String modelPath = DEFAULT_MODEL_PATH;

    /**
     * The model of the GroundVehicle to use.
     */
    private CzmlModel model;

    /**
     * The color of the orbit.
     */
    private Color color = DEFAULT_COLOR;

    /**
     * The custom name of the GroundVehicle.
     */
    private String name;

    /**
     * To display the name of the GroundVehicle or not.
     */
    private boolean displayName = false;

    /**
     * The custom ID of the GroundVehicle.
     */
    private String customID;

    /**
     * The central body.
     */
    private OneAxisEllipsoid earth;

    /**
     * The constructor of the builder.
     *
     * @param coordsProvider : The propagator used to create the trajectory of
     *        the MovingGroundObject
     * @param startDate : The object start date.
     * @param stopDate : The object stop date.
     * @param clock : The clock multiplier value used to create the trajectory
     *        timestep.
     * @param earth : The body the GroundVehicle is moving with respect to.
     */
    public GroundVehicleBuilder(final PVCoordinatesProvider coordsProvider,
                                final AbsoluteDate startDate,
                                final AbsoluteDate finalDate,
                                final OneAxisEllipsoid earth,
                                final Double clockMultiplier) {

        this.coordsProvider = coordsProvider;
        this.startDate = startDate;
        this.finalDate = finalDate;
        this.earth = earth;
        this.clockMultiplier = clockMultiplier;

        TimeStampedPVCoordinates pvCoords =
            coordsProvider.getPVCoordinates(startDate, earth.getBodyFrame());
        this.customID =
            String.format(DEFAULT_FORMAT, pvCoords.getPosition().getX(),
                          pvCoords.getPosition().getY(),
                          pvCoords.getPosition().getZ(),
                          pvCoords.getVelocity().getX(),
                          pvCoords.getVelocity().getY(),
                          pvCoords.getVelocity().getZ());
        this.name = DEFAULT_NAME;
    }

    /**
     * Function to set up a model.
     *
     * @param modelPathInput : The model to set up.
     * @return : The GroundVehicle builder with the given model.
     * @throws URISyntaxException the uri syntax exception
     * @throws IOException the io exception
     */
    public GroundVehicleBuilder withModelPath(final String modelPathInput)
        throws URISyntaxException,
            IOException {
        this.modelPath = modelPathInput;
        return this;
    }

    /**
     * Function to set up a color.
     *
     * @param colorInput : The color to set up.
     * @return : The GroundVehicle builder with the given color.
     */
    public GroundVehicleBuilder withColor(final Color colorInput) {
        this.color = colorInput;
        return this;
    }

    /**
     * Function to give GroundVehicle a custom name.
     *
     * @param nameInput : The color to set up.
     * @return : The GroundVehicle builder with the given color.
     */
    public GroundVehicleBuilder withName(final String nameInput) {
        this.name = nameInput;
        return this;
    }

    /**
     * Function to enter a model.
     *
     * @param modelInput : the model to set
     * @return A GroundVehicle builder with a custom model
     */
    public GroundVehicleBuilder withModel(final CzmlModel modelInput) {
        this.model = modelInput;
        return this;
    }

    /**
     * Function to display the name for the GroundVehicle.
     *
     * @return : The GroundVehicle builder with the personalized orientation for
     *         the GroundVehicle.
     */
    public GroundVehicleBuilder withDisplayName() {
        this.displayName = true;
        return this;
    }

    /**
     * Function to set up a custom ID.
     *
     * @param customIDInput : The custom ID to set up.
     * @return : The GroundVehicle object with a custom ID.
     */
    public GroundVehicleBuilder withCustomID(final String customIDInput) {
        this.customID = customIDInput;
        return this;
    }

    /**
     * The build function that generates a GroundVehicle object.
     *
     * @return : A GroundVehicle object with the given parameters of the
     *         builder.
     * @throws URISyntaxException the uri syntax exception
     * @throws IOException the io exception
     */
    public GroundVehicle build()
        throws URISyntaxException,
            IOException {
        final GroundVehicle tempPlane =
            new GroundVehicle(coordsProvider, startDate, finalDate, earth,
                              clockMultiplier, modelPath, color, customID,
                              name);
        return this.checkAttributes(tempPlane);
    }

    /**
     * This function checks if the reference system, the attitude and the period
     * of the orbit must be displayed or not.
     *
     * @param groundVehicle : The GroundVehicle object build with the build
     *        function.
     * @return : A GroundVehicle with a reference system, an attitude and a
     *         period of the orbit, displayed or not.
     */
    private GroundVehicle checkAttributes(final GroundVehicle groundVehicle) {
        if (displayName) {
            groundVehicle.displayName();
        }
        if (model != null) {
            groundVehicle.setModel(model);
        }
        return groundVehicle;
    }

}
