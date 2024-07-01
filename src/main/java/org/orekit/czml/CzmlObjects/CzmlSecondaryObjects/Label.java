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

import cesiumlanguagewriter.CesiumHorizontalOrigin;
import cesiumlanguagewriter.CesiumLabelStyle;
import cesiumlanguagewriter.CesiumOutputStream;
import cesiumlanguagewriter.CesiumVerticalOrigin;
import cesiumlanguagewriter.PacketCesiumWriter;

import java.awt.Color;

public class Label implements CZMLSecondaryObject {

    /** .*/
    private final Color color;
    /** .*/
    private final CesiumHorizontalOrigin horizontalOrigin;
    /** .*/
    private final boolean show;
    /** .*/
    private final CesiumLabelStyle labelStyle;
    /** .*/
    private final String text;
    /** .*/
    private final CesiumVerticalOrigin verticalOrigin;


    public Label(final Object object) {
        final int red = 0;
        final int blue = 255;
        final int green = 255;
        final int alpha = 255;
        this.color = new Color(red, blue, green, alpha);
        this.horizontalOrigin = CesiumHorizontalOrigin.LEFT;
        this.verticalOrigin = CesiumVerticalOrigin.CENTER;
        this.labelStyle = CesiumLabelStyle.FILL_AND_OUTLINE;
        this.text = object.toString();
        this.show = true;
    }

    public Label(final String text, final Color color, final CesiumHorizontalOrigin horizontalOrigin,
                 final CesiumVerticalOrigin verticalOrigin, final CesiumLabelStyle labelStyle, final boolean show) {
        this.color = color;
        this.horizontalOrigin = horizontalOrigin;
        this.verticalOrigin = verticalOrigin;
        this.text = text;
        this.labelStyle = labelStyle;
        this.show = show;
    }

    @Override
    public void write(final PacketCesiumWriter packetWriter, final CesiumOutputStream output) {
    }

    public Color getColor() {
        return color;
    }

    public String getText() {
        return text;
    }

    public CesiumVerticalOrigin getVerticalOrigin() {
        return verticalOrigin;
    }

    public CesiumHorizontalOrigin getHorizontalOrigin() {
        return horizontalOrigin;
    }

    public CesiumLabelStyle getLabelStyle() {
        return labelStyle;
    }

    public boolean getShow() {
        return show;
    }
}

