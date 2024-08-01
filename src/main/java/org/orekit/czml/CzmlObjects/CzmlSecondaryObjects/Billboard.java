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

import cesiumlanguagewriter.BillboardCesiumWriter;
import cesiumlanguagewriter.CesiumHorizontalOrigin;
import cesiumlanguagewriter.CesiumOutputStream;
import cesiumlanguagewriter.CesiumResourceBehavior;
import cesiumlanguagewriter.NearFarScalar;
import cesiumlanguagewriter.PacketCesiumWriter;
import cesiumlanguagewriter.UriCesiumWriter;

import java.awt.Color;

public class Billboard implements CzmlSecondaryObject {

    /**
     * .
     */
    public static final double DEFAULT_SCALE = 1.5;
    /**
     * .
     */
    private final boolean show;
    /**
     * .
     */
    private final String imageStr;
    /**
     * .
     */
    private final CesiumHorizontalOrigin cesiumHorizontalOrigin;
    /**
     * .
     */
    private final CesiumResourceBehavior cesiumResourceBehavior;
    /**
     * .
     */
    private final int red;
    /**
     * .
     */
    private final int blue;
    /**
     * .
     */
    private final int green;
    /**
     * .
     */
    private final int alpha;
    /**
     * .
     */
    private double scale = DEFAULT_SCALE;
    /**
     * .
     */
    private BillboardCesiumWriter writer;
    /**
     * .
     */
    private NearFarScalar nearFarScalar;

    public Billboard(final CesiumResourceBehavior cesiumResourceBehavior, final CesiumHorizontalOrigin cesiumHorizontalOrigin, final String imageStr, final boolean show, final double scale, final Color color) {
        this.scale = scale;
        this.show = show;
        this.cesiumHorizontalOrigin = cesiumHorizontalOrigin;
        this.imageStr = imageStr;
        this.cesiumResourceBehavior = cesiumResourceBehavior;
        this.red = color.getRed();
        this.green = color.getGreen();
        this.blue = color.getBlue();
        this.alpha = color.getAlpha();
    }

    public Billboard(final String imageStr) {
        this(imageStr, DEFAULT_SCALE);
    }

    public Billboard(final String imageStr, final double scale) {
        this.scale = scale;
        this.show = true;
        this.imageStr = imageStr;
        this.cesiumHorizontalOrigin = CesiumHorizontalOrigin.CENTER;
        this.cesiumResourceBehavior = CesiumResourceBehavior.LINK_TO;
        this.red = 0;
        this.green = 255;
        this.blue = 255;
        this.alpha = 195;
    }

    public Billboard(final String imageStr, final NearFarScalar nearFarScalar) {
        this.nearFarScalar = nearFarScalar;
        this.show = true;
        this.imageStr = imageStr;
        this.cesiumHorizontalOrigin = CesiumHorizontalOrigin.CENTER;
        this.cesiumResourceBehavior = CesiumResourceBehavior.LINK_TO;
        this.red = 0;
        this.green = 255;
        this.blue = 255;
        this.alpha = 195;
    }

    @Override
    public void write(final PacketCesiumWriter packetWriter, final CesiumOutputStream output) {
        try (BillboardCesiumWriter billboardWriter = packetWriter.getBillboardWriter()) {
            billboardWriter.open(output);
            billboardWriter.writeHorizontalOriginProperty(cesiumHorizontalOrigin);
            billboardWriter.writeColorProperty(red, green, blue, alpha);
            if (nearFarScalar == null) {
                billboardWriter.writeScaleProperty(scale);
            } else {
                billboardWriter.writeScaleByDistanceProperty(nearFarScalar);
            }
            billboardWriter.writeShowProperty(show);
            writeImage(billboardWriter, output);
        }
    }

    public double getScale() {
        return scale;
    }

    public boolean getShow() {
        return show;
    }

    public String getImageStr() {
        return imageStr;
    }

    public CesiumResourceBehavior getCesiumResourceBehavior() {
        return cesiumResourceBehavior;
    }

    public CesiumHorizontalOrigin getCesiumHorizontalOrigin() {
        return cesiumHorizontalOrigin;
    }

    public int getRed() {
        return red;
    }

    public int getGreen() {
        return green;
    }

    public int getBlue() {
        return blue;
    }

    public int getAlpha() {
        return alpha;
    }

    // Functions

    private void writeImage(final BillboardCesiumWriter billboardWriter, final CesiumOutputStream output) {
        try (UriCesiumWriter imageBillBoard = billboardWriter.openImageProperty()) {
            imageBillBoard.writeUri(imageStr, cesiumResourceBehavior);
        }
    }
}
