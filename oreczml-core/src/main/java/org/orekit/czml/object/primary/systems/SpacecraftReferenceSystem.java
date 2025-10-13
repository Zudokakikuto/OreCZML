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
package org.orekit.czml.object.primary.systems;

import cesiumlanguagewriter.CesiumOutputStream;
import cesiumlanguagewriter.CesiumStreamWriter;
import cesiumlanguagewriter.PacketCesiumWriter;
import cesiumlanguagewriter.Reference;
import org.orekit.czml.object.nonvisual.CzmlModel;
import org.orekit.czml.object.primary.AbstractPrimaryObject;
import org.orekit.czml.object.primary.entities.Spacecraft;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Objects;

/**
 * Spacecraft reference system class
 * <p>
 * The reference system of the Spacecraft and its axis. By default a 3D model of
 * 3 axis is used to define it.
 * </p>
 *
 * @author Julien LEBLOND.
 * @since 1.0.0
 */
public class SpacecraftReferenceSystem
    extends
    AbstractPrimaryObject {

    /**
     * The default ID for the reference system.
     */
    public static final String DEFAULT_ID = "REFERENCE SYSTEM/";

    /**
     * The default name for the reference system.
     */
    public static final String DEFAULT_NAME = "Reference system of : ";

    /**
     * This allows creating the reference of the position of an object.
     */
    public static final String DEFAULT_H_POSITION = "#position";

    /**
     * This allows creating the reference of the orientation of an object.
     */
    public static final String DEFAULT_H_ORIENTATION = "#orientation";

    /**
     * The default 3D model used to represent the Spacecraft reference system.
     */
    public static final String PATH_TO_REFERENCE_SYSTEM =
        Objects.requireNonNull(CzmlModel.class.getClassLoader()
            .getResource("referenceSystem.glb")).getPath();

    // Intrinsic parameters

    /**
     * The Spacecraft which the system will be around.
     */
    private final Spacecraft spacecraft;

    // Other parameters
    /**
     * The reference in position of the Spacecraft.
     */
    private final Reference referencePosition;

    /**
     * The reference in orientation of the Spacecraft.
     */
    private final Reference referenceOrientation;

    /**
     * The model loaded to represents the system.
     */
    private final CzmlModel referenceSystemModel;

    // Constructors

    /**
     * The basic constructor for the Spacecraft reference system, it uses
     * default parameters.
     *
     * @param spacecraft : The Spacecraft around which the reference system must
     *        be.
     */
    public SpacecraftReferenceSystem(final Spacecraft spacecraft) {
        this(spacecraft, 0.02, 200000, 250, DEFAULT_ID + spacecraft.getId());
    }

    /**
     * The constructor for the Spacecraft reference system with no default
     * parameters.
     *
     * @param spacecraft : The Spacecraft around which the reference system must
     *        be.
     * @param scale : The scale of the model to be loaded to define the
     *        reference system.
     * @param maximumScale : The maximum scale that the mode can take.
     * @param minimumPixelSize : The minimum pixel sie of the model.
     * @param customID : The custom ID of the Spacecraft reference system.
     */
    public SpacecraftReferenceSystem(final Spacecraft spacecraft,
                                     final double scale,
                                     final double maximumScale,
                                     final double minimumPixelSize,
                                     final String customID) {
        this.spacecraft = spacecraft;
        this.setId(customID);
        this.setName(DEFAULT_NAME + spacecraft.getName());
        this.setAvailability(spacecraft.getAvailability());
        this.referencePosition =
            new Reference(spacecraft.getId() + DEFAULT_H_POSITION);
        this.referenceOrientation =
            new Reference(spacecraft.getId() + DEFAULT_H_ORIENTATION);
        this.referenceSystemModel =
            new CzmlModel(PATH_TO_REFERENCE_SYSTEM, maximumScale,
                          minimumPixelSize, scale, false,
                          spacecraft.getAvailability());
    }

    // Overrides

    @Override
    public void writeCzmlBlock(final CesiumStreamWriter stream,
                               final CesiumOutputStream output) {
        output.setPrettyFormatting(true);
        try (PacketCesiumWriter packet = stream.openPacket(output)) {
            packet.writeId(getId());
            packet.writeName(getName());
            packet.writeAvailability(getAvailability());
            packet.writePositionPropertyReference(referencePosition);
            packet.writeOrientationPropertyReference(referenceOrientation);
            this.getReferenceSystemModel().generateCZML(packet, output);
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    // Getters

    /**
     * Gets Spacecraft.
     *
     * @return the Spacecraft
     */
    public Spacecraft getSpacecraft() {
        return spacecraft;
    }

    /**
     * Gets reference system model.
     *
     * @return the reference system model
     */
    public CzmlModel getReferenceSystemModel() {
        return referenceSystemModel;
    }
}
