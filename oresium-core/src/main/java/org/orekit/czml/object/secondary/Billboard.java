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

import cesiumlanguagewriter.BillboardCesiumWriter;
import cesiumlanguagewriter.CesiumHorizontalOrigin;
import cesiumlanguagewriter.CesiumOutputStream;
import cesiumlanguagewriter.CesiumResourceBehavior;
import cesiumlanguagewriter.NearFarScalar;
import cesiumlanguagewriter.PacketCesiumWriter;
import cesiumlanguagewriter.UriCesiumWriter;

import java.awt.Color;

/**
 * Billboard class.
 * <p>
 * This class aims at displaying an image to an object when one is defined.
 * </p>
 *
 * @author Julien LEBLOND
 * @since 1.0.0
 */
public class Billboard
    extends
    AbstractSecondaryObject<Billboard> {

    /**
     * The default scale of the image used by the billboard.
     */
    public static final double DEFAULT_SCALE = 1.5;

    /**
     * The horizontal origin of the image. Available parameters are: LEFT,
     * CENTER, RIGHT
     */
    private final CesiumHorizontalOrigin cesiumHorizontalOrigin;

    /**
     * Parameter of the billboard to know where to link the image.
     */
    private final CesiumResourceBehavior cesiumResourceBehavior;

    /**
     * The string of the image to use (URI).
     */
    private final String imageStr;

    /**
     * Whether to show or not the billboard.
     */
    private final boolean show;

    /**
     * The scale of the image.
     */
    private double scale = DEFAULT_SCALE;

    /**
     * The color of the image.
     */
    private final Color color;

    /**
     * Property to know how close and far the image can display.
     */
    private NearFarScalar nearFarScalar;

    // Constructors

    /**
     * The basic constructor for the billboard, with default parameters.
     *
     * @param imageStr : The string of the image to use (URI).
     */
    public Billboard(final String imageStr) {
        this(imageStr, DEFAULT_SCALE);
    }

    /**
     * The constructor of the billboard with a given scale.
     *
     * @param imageStr : The string of the image to use (URI).
     * @param scale : The scale of the image.
     */
    public Billboard(final String imageStr, final double scale) {
        this.scale = scale;
        this.show = true;
        this.imageStr = imageStr;
        this.cesiumHorizontalOrigin = CesiumHorizontalOrigin.CENTER;
        this.cesiumResourceBehavior = CesiumResourceBehavior.LINK_TO;
        this.color = new Color(0, 255, 255, 195);
    }

    /**
     * The constructor of the billboard with a given scale.
     *
     * @param imageStr : The string of the image to use (URI).
     * @param nearFarScalar : How close and far the image should be displayed.
     */
    public Billboard(final String imageStr, final NearFarScalar nearFarScalar) {
        this.nearFarScalar = nearFarScalar;
        this.show = true;
        this.imageStr = imageStr;
        this.cesiumHorizontalOrigin = CesiumHorizontalOrigin.CENTER;
        this.cesiumResourceBehavior = CesiumResourceBehavior.LINK_TO;
        this.color = new Color(0, 255, 255, 195);
    }

    /**
     * The billboard constructor with no default parameters.
     *
     * @param cesiumResourceBehavior : Parameter of the billboard to know where
     *        to link the image.
     * @param cesiumHorizontalOrigin : The horizontal origin of the image.
     * @param imageStr : The string of the image to use (URI).
     * @param show : Whether to show or not the billboard.
     * @param scale : The scale of the image.
     * @param color : The color of the image.
     * @param nearFarScalar : How close and far the image should be displayed.
     */
    public Billboard(final CesiumResourceBehavior cesiumResourceBehavior,
                     final CesiumHorizontalOrigin cesiumHorizontalOrigin,
                     final String imageStr, final boolean show,
                     final double scale, final Color color,
                     final NearFarScalar nearFarScalar) {
        this.scale = scale;
        this.show = show;
        this.cesiumHorizontalOrigin = cesiumHorizontalOrigin;
        this.imageStr = imageStr;
        this.cesiumResourceBehavior = cesiumResourceBehavior;
        this.color = color;
        this.nearFarScalar = nearFarScalar;
    }

    // Overrides

    @Override
    public void write(final PacketCesiumWriter packetWriter,
                      final CesiumOutputStream output) {
        try (BillboardCesiumWriter billboardWriter =
            packetWriter.getBillboardWriter()) {
            billboardWriter.open(output);
            billboardWriter
                .writeHorizontalOriginProperty(cesiumHorizontalOrigin);
            billboardWriter.writeColorProperty(color);
            if (nearFarScalar == null) {
                billboardWriter.writeScaleProperty(scale);
            } else {
                billboardWriter.writeScaleByDistanceProperty(nearFarScalar);
            }
            billboardWriter.writeShowProperty(show);
            writeImage(billboardWriter);
        }
    }

    @Override
    public Billboard cloneObject() {
        final Billboard copy =
            new Billboard(this.getImageStr(), this.getScale());
        copy.setNearFarScalar(this.getNearFarScalar());
        return copy;
    }

    // Getters

    /**
     * Gets scale.
     *
     * @return the scale
     */
    public double getScale() {
        return scale;
    }

    /**
     * Gets image str.
     *
     * @return the image str
     */
    public String getImageStr() {
        return imageStr;
    }

    /**
     * Gets cesium resource behavior.
     *
     * @return the cesium resource behavior
     */
    public CesiumResourceBehavior getCesiumResourceBehavior() {
        return cesiumResourceBehavior;
    }

    /**
     * Gets cesium horizontal origin.
     *
     * @return the cesium horizontal origin
     */
    public CesiumHorizontalOrigin getCesiumHorizontalOrigin() {
        return cesiumHorizontalOrigin;
    }

    /**
     * Gets show.
     *
     * @return the show
     */
    public boolean getShow() {
        return show;
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
     * Gets near far scalar.
     *
     * @return the near far scalar
     */
    public NearFarScalar getNearFarScalar() {
        return nearFarScalar;
    }

    // Setters

    public void setNearFarScalar(final NearFarScalar nearFarScalar) {
        this.nearFarScalar = nearFarScalar;
    }

    // Private functions

    /**
     * This function aims at writing the image of the billboard with a billboard
     * writer.
     *
     * @param billboardWriter : The writer of the billboard.
     */
    private void writeImage(final BillboardCesiumWriter billboardWriter) {
        try (UriCesiumWriter imageBillBoard =
            billboardWriter.openImageProperty()) {
            imageBillBoard.writeUri(imageStr, cesiumResourceBehavior);
        }
    }

}
