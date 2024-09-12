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
package org.orekit.czml.CzmlObjects.CzmlPrimaryObjects;

import cesiumlanguagewriter.PacketCesiumWriter;
import cesiumlanguagewriter.Reference;
import org.orekit.czml.CzmlObjects.CzmlAbstractObjects.CzmlModel;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;

/**
 * Satellite reference system class
 *
 * <p> The reference system of the satellite and its axis. By default a 3D model of 3 axis is used to define it. </p>
 *
 * @author Julien LEBLOND.
 * @since 1.0.0
 */

public class SatelliteReferenceSystem extends AbstractPrimaryObject implements CzmlPrimaryObject {

    /** The default ID for the reference system. */
    public static final String DEFAULT_ID = "REFERENCE SYSTEM/";

    /** The default name for the reference system. */
    public static final String DEFAULT_NAME = "Reference system of : ";

    /** This allows creating the reference of the position of an object. */
    public static final String DEFAULT_H_POSITION = "#position";

    /** This allows creating the reference of the orientation of an object. */
    public static final String DEFAULT_H_ORIENTATION = "#orientation";

    /** The default 3D model used to represent the satellite reference system. */
    public static final String PATH_TO_REFERENCE_SYSTEM = Header.DEFAULT_RESOURCES + "/Default3DModels/referenceSystem.glb";


    // Intrinsic parameters

    /** The satellite which the system will be around. */
    private Satellite satellite;


    // Other parameters
    /** The reference in position of the satellite. */
    private Reference referencePosition;

    /** The reference in orientation of the satellite. */
    private Reference referenceOrientation;

    /** The model loaded to represents the system. */
    private CzmlModel referenceSystemModel;


    // Constructors

    /**
     * The basic constructor for the satellite reference system, it uses default parameters.
     *
     * @param satellite : The satellite around which the reference system must be.
     */
    SatelliteReferenceSystem(final Satellite satellite) throws URISyntaxException, IOException {
        this(satellite, 0.02, 200000, 250);
    }

    /**
     * The constructor for the satellite reference system with no default parameters.
     *
     * @param satellite        : The satellite around which the reference system must be.
     * @param scale            : The scale of the model to be loaded to define the reference system.
     * @param maximumScale     : The maximum scale that the mode can take.
     * @param minimumPixelSize : The minimum pixel sie of the model.
     */
    SatelliteReferenceSystem(final Satellite satellite, final double scale, final double maximumScale,
                             final double minimumPixelSize) throws URISyntaxException, IOException {
        this.satellite = satellite;
        this.setId(DEFAULT_ID + satellite.getId());
        this.setName(DEFAULT_NAME + satellite.getName());
        this.setAvailability(satellite.getAvailability());
        this.referencePosition    = new Reference(satellite.getId() + DEFAULT_H_POSITION);
        this.referenceOrientation = new Reference(satellite.getId() + DEFAULT_H_ORIENTATION);
        this.referenceSystemModel = new CzmlModel(PATH_TO_REFERENCE_SYSTEM, maximumScale, minimumPixelSize, scale);
    }


    // Overrides

    @Override
    public void writeCzmlBlock() {
        OUTPUT.setPrettyFormatting(true);
        try (PacketCesiumWriter packet = STREAM.openPacket(OUTPUT)) {
            packet.writeId(getId());
            packet.writeName(getName());
            packet.writeAvailability(getAvailability());
            packet.writePositionPropertyReference(referencePosition);
            packet.writeOrientationPropertyReference(referenceOrientation);
            this.getReferenceSystemModel()
                .generateCZML(packet, OUTPUT);
        }
    }

    @Override
    public StringWriter getStringWriter() {
        return STRING_WRITER;
    }

    @Override
    public void cleanObject() {
        referencePosition    = null;
        referenceOrientation = null;
        satellite            = null;
        referenceSystemModel = null;
        setAvailability(null);
        setName("");
        setId("");
    }


    // Getters

    public Satellite getSatellite() {
        return satellite;
    }

    public Reference getReferenceOrientation() {
        return referenceOrientation;
    }

    public Reference getReferencePosition() {
        return referencePosition;
    }

    public CzmlModel getReferenceSystemModel() {
        return referenceSystemModel;
    }
}
