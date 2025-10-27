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

import cesiumlanguagewriter.CesiumHorizontalOrigin;
import cesiumlanguagewriter.CesiumLabelStyle;
import cesiumlanguagewriter.CesiumOutputStream;
import cesiumlanguagewriter.CesiumVerticalOrigin;
import cesiumlanguagewriter.LabelCesiumWriter;
import cesiumlanguagewriter.PacketCesiumWriter;
import org.orekit.czml.errors.OreCzmlException;
import org.orekit.czml.errors.OreCzmlMessages;

import java.awt.Color;

/**
 * Label class
 * <p>
 * This class allows the user to display a specific text of an object during the
 * simulation.
 * </p>
 *
 * @author Julien LEBLOND
 * @since 1.0.0
 */
public class Label
    extends
    AbstractSecondaryObject<Label> {

    /**
     * The default police for the labels.
     */
    public static final String DEFAULT_POLICE = "11pt Lucida Console";

    /**
     * The color of the label.
     */
    private final Color color;

    /**
     * The horizontal origin of the label. Available parameters are: LEFT,
     * CENTER, RIGHT
     */
    private final CesiumHorizontalOrigin horizontalOrigin;

    /**
     * The vertical origin of the label. Available parameters are: LEFT, CENTER,
     * RIGHT
     */
    private final CesiumVerticalOrigin verticalOrigin;

    /**
     * The style of the label, three are available: FILL, OUTLINE,
     * FILL_AND_OUTLINE.
     */
    private final CesiumLabelStyle labelStyle;

    /**
     * The text to put into the label.
     */
    private final String text;

    /**
     * To display or not the label.
     */
    private final boolean show;

    /** The object used if one is used. */
    private Object object;

    // Constructors

    /**
     * The constructor of the label of an object, the text will be the name of
     * the object. This constructor uses default parameters.
     *
     * @param objectInput : The object that will be references with the label.
     */
    public Label(final Object objectInput) {
        this.color = new Color(0, 255, 255, 255);
        this.horizontalOrigin = CesiumHorizontalOrigin.LEFT;
        this.verticalOrigin = CesiumVerticalOrigin.CENTER;
        this.labelStyle = CesiumLabelStyle.FILL_AND_OUTLINE;
        this.text = objectInput.toString();
        this.show = true;
        this.object = objectInput;
    }

    /**
     * The constructor of the label of a given text with a given color.
     *
     * @param text : The text to enter into the label.
     * @param color : The color of the label.
     */
    public Label(final String text, final Color color) {
        this.color = color;
        this.horizontalOrigin = CesiumHorizontalOrigin.LEFT;
        this.verticalOrigin = CesiumVerticalOrigin.CENTER;
        this.labelStyle = CesiumLabelStyle.FILL_AND_OUTLINE;
        this.text = text;
        this.show = true;
    }

    /**
     * The constructor of the label, with no default parameters.
     *
     * @param text : The text to enter into the label.
     * @param color : The color of the label.
     * @param horizontalOrigin : The horizontal origin of the label.
     * @param verticalOrigin : The vertical origin of the label.
     * @param labelStyle : The style of the label.
     * @param show : To display or not the label.
     */
    public Label(final String text, final Color color,
                 final CesiumHorizontalOrigin horizontalOrigin,
                 final CesiumVerticalOrigin verticalOrigin,
                 final CesiumLabelStyle labelStyle, final boolean show) {
        this.color = color;
        this.horizontalOrigin = horizontalOrigin;
        this.verticalOrigin = verticalOrigin;
        this.text = text;
        this.labelStyle = labelStyle;
        this.show = show;
    }

    // Overrides

    @Override
    public void write(final PacketCesiumWriter packet,
                      final CesiumOutputStream output) {
        try (LabelCesiumWriter labelWriter = packet.getLabelWriter()) {
            labelWriter.open(output);
            labelWriter.writeFillColorProperty(color);
            labelWriter.writeFontProperty(DEFAULT_POLICE);
            labelWriter.writeHorizontalOriginProperty(horizontalOrigin);
            labelWriter.writeVerticalOriginProperty(verticalOrigin);
            labelWriter.writeTextProperty(text);
            labelWriter.writeShowProperty(show);
        }
    }

    @Override
    public Label cloneObject() {
        final Label toReturn;
        if (object != null) {
            toReturn = new Label(this.object);
        } else if (this.text != null && this.color != null) {
            if (this.horizontalOrigin != null && this.verticalOrigin != null) {
                toReturn =
                    new Label(this.text, this.color, this.horizontalOrigin,
                              this.verticalOrigin, this.labelStyle, this.show);
            } else {
                toReturn = new Label(this.text, this.color);
            }
        } else {
            throw new OreCzmlException(OreCzmlMessages.NOT_VALID_SECONDARY_OBJECT_FOR_CLONE);
        }
        return toReturn;
    }

    // Getters

    /**
     * Gets color.
     *
     * @return the color
     */
    public Color getColor() {
        return color;
    }

    /**
     * Gets text.
     *
     * @return the text
     */
    public String getText() {
        return text;
    }

    /**
     * Gets vertical origin.
     *
     * @return the vertical origin
     */
    public CesiumVerticalOrigin getVerticalOrigin() {
        return verticalOrigin;
    }

    /**
     * Gets horizontal origin.
     *
     * @return the horizontal origin
     */
    public CesiumHorizontalOrigin getHorizontalOrigin() {
        return horizontalOrigin;
    }

    /**
     * Gets show.
     *
     * @return the show
     */
    public boolean getShow() {
        return show;
    }

}
