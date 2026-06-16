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
package org.orekit.czml.object.secondary;

import cesiumlanguagewriter.Cartesian;
import cesiumlanguagewriter.CesiumOutputStream;
import cesiumlanguagewriter.MaterialCesiumWriter;
import cesiumlanguagewriter.PacketCesiumWriter;
import cesiumlanguagewriter.PolygonCesiumWriter;
import cesiumlanguagewriter.PositionListCesiumWriter;
import cesiumlanguagewriter.SolidColorMaterialCesiumWriter;

import java.awt.Color;
import java.util.Collections;
import java.util.List;

/**
 * Polygon class.
 * <p>
 * This class aims at displaying polygons that can be presents at the surface of
 * any body, on even floating in the simulation.
 * </p>
 *
 * @author LEBLOND Julien
 * @since 1.0.0
 */
public class Polygon
    extends
    AbstractSecondaryObject<Polygon> {

    /**
     * The default color.
     */
    public static final Color DEFAULT_COLOR = new Color(243, 194, 32);

    /**
     * Positions of the polygon.
     */
    private final List<Cartesian> cartesians;

    /**
     * Time interval when the polygon is displayed.
     */
    private final Clock clock;

    /**
     * The color of the polygon.
     */
    private final Color color;

    /**
     * The outline of the polygon.
     */
    private final boolean outline;

    /**
     * To fill the polygon or not.
     */
    private final boolean fill;

    // Constructors

    /**
     * The default constructor for the polygon object with default parameters.
     *
     * @param cartesiansInput : The list of the positions of the polygon.
     * @param clock : The clock of the polygon
     */
    public Polygon(final List<Cartesian> cartesiansInput, final Clock clock) {
        this.cartesians = cartesiansInput;
        this.clock = clock;
        this.color = DEFAULT_COLOR;
        this.outline = false;
        this.fill = true;
    }

    /**
     * The constructor of the polygon object with no default parameters.
     *
     * @param cartesiansInput : The list of the positions of the polygon.
     * @param colorInput : The color of the polygon.
     * @param outline : The outline of the polygon.
     * @param fill : To fill or not with color the polygon. (might cause some
     *        lags if put to true)
     * @param clock : The clock of the polygon
     */
    public Polygon(final List<Cartesian> cartesiansInput,
                   final Color colorInput, final boolean outline,
                   final boolean fill, final Clock clock) {
        this.cartesians = cartesiansInput;
        this.clock = clock;
        this.color = colorInput;
        this.outline = outline;
        this.fill = fill;
    }

    @Override
    public void write(final PacketCesiumWriter packetWriter,
                      final CesiumOutputStream output) {
        try (PolygonCesiumWriter polygonWriter =
            packetWriter.getPolygonWriter()) {
            polygonWriter.open(output);
            polygonWriter.writeOutlineProperty(outline);
            polygonWriter.writeFillProperty(fill);
            polygonWriter.writeOutlineColorProperty(color);
            try (PositionListCesiumWriter positionListWriter =
                polygonWriter.getPositionsWriter()) {
                positionListWriter.open(output);
                positionListWriter.writeInterval(clock.getAvailability());
                positionListWriter.writeCartesian(cartesians);
            }
            try (MaterialCesiumWriter materialWriter =
                polygonWriter.getMaterialWriter()) {
                materialWriter.open(output);
                output.writeStartObject();
                try (SolidColorMaterialCesiumWriter solidColorMaterialWriter =
                    materialWriter.getSolidColorWriter()) {
                    solidColorMaterialWriter.open(output);
                    solidColorMaterialWriter.writeColorProperty(color);
                }
                output.writeEndObject();
            }
        }
    }

    @Override
    public Polygon cloneObject() {
        return Polygon.builder(this.cartesians, this.clock)
            .withColor(this.color).withFill(this.fill).withOutline(this.outline)
            .build();
    }

    // Overrides

    /**
     * Builder polygon builder.
     *
     * @param cartesiansInput the cartesians input
     * @param clockInput the clock
     * @return the polygon builder
     */
    public static PolygonBuilder builder(final List<Cartesian> cartesiansInput,
                                         final Clock clockInput) {
        return new PolygonBuilder(cartesiansInput, clockInput);
    }

    // Getters

    /**
     * Gets cartesians.
     *
     * @return the cartesians
     */
    public List<Cartesian> getCartesians() {
        return Collections.unmodifiableList(cartesians);
    }

    /**
     * Gets the clock.
     *
     * @return the clock
     */
    public Clock getClock() {
        return clock;
    }

    /**
     * Gets color.
     *
     * @return the color
     */
    public Color getColor() {
        return color;
    }

    /**
     * Is outline boolean.
     *
     * @return the boolean
     */
    public boolean isOutline() {
        return outline;
    }

    /**
     * Is fill boolean.
     *
     * @return the boolean
     */
    public boolean isFill() {
        return fill;
    }
}
