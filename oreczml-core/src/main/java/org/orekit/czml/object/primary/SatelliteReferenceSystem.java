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
package org.orekit.czml.object.primary;

import cesiumlanguagewriter.CesiumOutputStream;
import cesiumlanguagewriter.CesiumStreamWriter;
import cesiumlanguagewriter.PacketCesiumWriter;
import cesiumlanguagewriter.Reference;
import org.orekit.czml.object.nonvisual.CzmlModel;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Objects;

/**
 * Satellite reference system class
 *
 * <p> The reference system of the satellite and its axis. By default a 3D model of 3 axis is used to define it. </p>
 *
 * @author Julien LEBLOND.
 * @since 1.0.0
 */
public class SatelliteReferenceSystem extends AbstractPrimaryObject {

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
     * The default 3D model used to represent the satellite reference system.
     */
    public static final String PATH_TO_REFERENCE_SYSTEM = Objects.requireNonNull(CzmlModel.class.getClassLoader()
                                                                                                .getResource(
                                                                                                        "referenceSystem.glb"))
                                                                 .getPath();

    // Intrinsic parameters

    /**
     * The satellite which the system will be around.
     */
    private final Satellite satellite;


    // Other parameters
    /**
     * The reference in position of the satellite.
     */
    private final Reference referencePosition;

    /**
     * The reference in orientation of the satellite.
     */
    private final Reference referenceOrientation;

    /**
     * The model loaded to represents the system.
     */
    private final CzmlModel referenceSystemModel;


    // Constructors

    /**
     * The basic constructor for the satellite reference system, it uses default parameters.
     *
     * @param satellite : The satellite around which the reference system must be.
     * @param header    : The header considered.
     */
    public SatelliteReferenceSystem(final Satellite satellite, final Header header) {
        this(satellite, 0.02, 200000, 250, DEFAULT_ID + satellite.getId(), header);
    }

    /**
     * The constructor for the satellite reference system with no default parameters.
     *
     * @param satellite        : The satellite around which the reference system must be.
     * @param scale            : The scale of the model to be loaded to define the reference system.
     * @param maximumScale     : The maximum scale that the mode can take.
     * @param minimumPixelSize : The minimum pixel sie of the model.
     * @param customID         : The custom ID of the satellite reference system.
     * @param header           : The header considered.
     */
    public SatelliteReferenceSystem(final Satellite satellite, final double scale, final double maximumScale,
                                    final double minimumPixelSize, final String customID, final Header header) {
        this.satellite = satellite;
        this.setId(customID);
        this.setName(DEFAULT_NAME + satellite.getName());
        this.setAvailability(satellite.getAvailability());
        this.referencePosition    = new Reference(satellite.getId() + DEFAULT_H_POSITION);
        this.referenceOrientation = new Reference(satellite.getId() + DEFAULT_H_ORIENTATION);
        this.referenceSystemModel = new CzmlModel(PATH_TO_REFERENCE_SYSTEM, maximumScale, minimumPixelSize, scale,
                header);
    }


    // Overrides

    @Override
    public void writeCzmlBlock(final CesiumStreamWriter stream, final CesiumOutputStream output) {
        output.setPrettyFormatting(true);
        try (PacketCesiumWriter packet = stream.openPacket(output)) {
            packet.writeId(getId());
            packet.writeName(getName());
            packet.writeAvailability(getAvailability());
            packet.writePositionPropertyReference(referencePosition);
            packet.writeOrientationPropertyReference(referenceOrientation);
            this.getReferenceSystemModel()
                .generateCZML(packet, output);
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }


    // Getters

    /**
     * Gets satellite.
     *
     * @return the satellite
     */
    public Satellite getSatellite() {
        return satellite;
    }

    /**
     * Gets reference orientation.
     *
     * @return the reference orientation
     */
    public Reference getReferenceOrientation() {
        return referenceOrientation;
    }

    /**
     * Gets reference position.
     *
     * @return the reference position
     */
    public Reference getReferencePosition() {
        return referencePosition;
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
