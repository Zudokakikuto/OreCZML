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
package org.orekit.czml.CzmlObjects.CzmlSecondaryObjects;

import cesiumlanguagewriter.Cartesian;
import cesiumlanguagewriter.CesiumOutputStream;
import cesiumlanguagewriter.MaterialCesiumWriter;
import cesiumlanguagewriter.PacketCesiumWriter;
import cesiumlanguagewriter.PolygonCesiumWriter;
import cesiumlanguagewriter.PositionListCesiumWriter;
import cesiumlanguagewriter.SolidColorMaterialCesiumWriter;
import cesiumlanguagewriter.TimeInterval;
import org.orekit.czml.ArchiObjects.Builders.PolygonBuilder;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.Header;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;


/**
 * Polygon class.
 *
 * <p> This class aims at displaying polygons that can be presents at the surface of any body, on even floating in the simulation. </p>
 *
 * @author Julien LEBLOND
 * @since 1.0.0
 */
public class Polygon implements CzmlSecondaryObject {

    /** The default color. */
    public static final Color DEFAULT_COLOR = new Color(243, 194, 32);

    /** The default availability. */
    public static final TimeInterval DEFAULT_AVAILABILITY = Header.getMasterClock()
                                                                  .getAvailability();

    /** Positions of the polygon. */
    private List<Cartesian> cartesians = new ArrayList<>();

    /** Time interval when the polygon is displayed. */
    private TimeInterval availability;

    /** The color of the polygon. */
    private Color color;

    /** The outline of the polygon. */
    private boolean outline;

    /** To fill the polygon or not. */
    private boolean fill;


    // Constructors

    /**
     * The default constructor for the polygon object with default parameters.
     *
     * @param cartesiansInput : The list of the positions of the polygon.
     */
    public Polygon(final List<Cartesian> cartesiansInput) {
        this.cartesians   = cartesiansInput;
        this.availability = DEFAULT_AVAILABILITY;
        this.color        = DEFAULT_COLOR;
        this.outline      = false;
        this.fill         = true;
    }

    /**
     * The constructor of the polygon object with no default parameters.
     *
     * @param cartesiansInput   : The list of the positions of the polygon.
     * @param availabilityInput :The availability of the polygon.
     * @param colorInput        : The color of the polygon.
     * @param outline           : The outline of the polygon.
     * @param fill              : To fill or not with color the polygon. (might cause some lags if put to true)
     */
    public Polygon(final List<Cartesian> cartesiansInput, final TimeInterval availabilityInput,
                   final Color colorInput, final boolean outline, final boolean fill) {
        this.cartesians   = cartesiansInput;
        this.availability = availabilityInput;
        this.color        = colorInput;
        this.outline      = outline;
        this.fill         = fill;
    }


    // Builders

    @Override
    public void write(final PacketCesiumWriter packetWriter, final CesiumOutputStream output) {
        try (PolygonCesiumWriter polygonWriter = packetWriter.getPolygonWriter()) {
            polygonWriter.open(output);
            polygonWriter.writeOutlineProperty(outline);
            polygonWriter.writeFillProperty(fill);
            polygonWriter.writeOutlineColorProperty(color);
            try (PositionListCesiumWriter positionListWriter = polygonWriter.getPositionsWriter()) {
                positionListWriter.open(output);
                positionListWriter.writeInterval(availability);
                positionListWriter.writeCartesian(cartesians);
            }
            try (MaterialCesiumWriter materialWriter = polygonWriter.getMaterialWriter()) {
                materialWriter.open(output);
                output.writeStartObject();
                try (SolidColorMaterialCesiumWriter solidColorMaterialWriter = materialWriter.getSolidColorWriter()) {
                    solidColorMaterialWriter.open(output);
                    solidColorMaterialWriter.writeColorProperty(color);
                }
                output.writeEndObject();
            }
        }
    }


    // Overrides

    public PolygonBuilder builder(final List<Cartesian> cartesiansInput) {
        return new PolygonBuilder(cartesiansInput);
    }


    // Getters

    public List<Cartesian> getCartesians() {
        return new ArrayList<>(cartesians);
    }

    public TimeInterval getAvailability() {
        return availability;
    }

    public Color getColor() {
        return color;
    }

    public boolean isOutline() {
        return outline;
    }

    public boolean isFill() {
        return fill;
    }
}
