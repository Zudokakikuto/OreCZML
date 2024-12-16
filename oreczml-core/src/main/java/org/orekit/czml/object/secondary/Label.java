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

package org.orekit.czml.object.secondary;

import cesiumlanguagewriter.Cartesian;
import cesiumlanguagewriter.CesiumHorizontalOrigin;
import cesiumlanguagewriter.CesiumLabelStyle;
import cesiumlanguagewriter.CesiumOutputStream;
import cesiumlanguagewriter.CesiumVerticalOrigin;
import cesiumlanguagewriter.LabelCesiumWriter;
import cesiumlanguagewriter.PacketCesiumWriter;
import org.orekit.czml.object.primary.Header;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Label class
 *
 * <p> This class allows the user to display a specific text of an object during the simulation.</p>
 *
 * @author Julien LEBLOND
 * @since 1.0.0
 */
public class Label extends AbstractSecondaryObject {


    /**
     * The default police for the labels.
     */
    public static final String DEFAULT_POLICE = "11pt Lucida Console";

    /**
     * The color of the label.
     */
    private final Color color;

    /**
     * The horizontal origin of the label. Available parameters are: LEFT, CENTER, RIGHT
     */
    private final CesiumHorizontalOrigin horizontalOrigin;

    /**
     * The vertical origin of the label. Available parameters are: LEFT, CENTER, RIGHT
     */
    private final CesiumVerticalOrigin verticalOrigin;

    /**
     * The style of the label, three are available: FILL, OUTLINE, FILL_AND_OUTLINE.
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


    // Constructors

    /**
     * The constructor of the label of an object, the text will be the name of the object. This constructor uses default parameters.
     *
     * @param object : The object that will be references with the label.
     */
    public Label(final Object object) {
        this.color = new Color(0, 255, 255, 255);
        this.horizontalOrigin = CesiumHorizontalOrigin.LEFT;
        this.verticalOrigin = CesiumVerticalOrigin.CENTER;
        this.labelStyle = CesiumLabelStyle.FILL_AND_OUTLINE;
        this.text = object.toString();
        this.show = true;
    }

    /**
     * The constructor of the label of a given text with a given color.
     *
     * @param text  : The text to enter into the label.
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
     * @param text             : The text to enter into the label.
     * @param color            : The color of the label.
     * @param horizontalOrigin : The horizontal origin of the label.
     * @param verticalOrigin   : The vertical origin of the label.
     * @param labelStyle       : The style of the label.
     * @param show             : To display or not the label.
     */
    public Label(final String text, final Color color, final CesiumHorizontalOrigin horizontalOrigin,
                 final CesiumVerticalOrigin verticalOrigin, final CesiumLabelStyle labelStyle, final boolean show) {
        this.color = color;
        this.horizontalOrigin = horizontalOrigin;
        this.verticalOrigin = verticalOrigin;
        this.text = text;
        this.labelStyle = labelStyle;
        this.show = show;
    }


    // Overrides

    @Override
    public void write(final PacketCesiumWriter packet, final CesiumOutputStream output) {
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
     * Gets label style.
     *
     * @return the label style
     */
    public CesiumLabelStyle getLabelStyle() {
        return labelStyle;
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
     * Polygon Builder class
     * <p>
     * Builder for the {@link Polygon} class.
     *
     * @author Julien LEBLOND
     * @since 1.0
     */
    public static class PolygonBuilder {

        /**
         * The default color.
         */
        public static final Color DEFAULT_COLOR = new Color(243, 194, 32);

        /**
         * Positions of the polygon.
         */
        private final List<Cartesian> cartesians;

        /**
         * The color of the polygon.
         */
        private Color color = DEFAULT_COLOR;

        /**
         * To display the outline or not.
         */
        private boolean outline = false;

        /**
         * To fill or not the polygon.
         */
        private boolean fill = true;

        /** The header considered. */
        private Header header;

        // Constructor

        /**
         * The constructor of the polygon builder.
         *
         * @param cartesiansInput : The list of cartesians that will build the polygon.
         * @param headerInput     : The header considered.
         */
        public PolygonBuilder(final List<Cartesian> cartesiansInput, final Header headerInput) {
            this.header     = headerInput;
            this.cartesians = new ArrayList<>(cartesiansInput);
        }

        /**
         * Function to set up a color.
         *
         * @param colorInput : The color to set up.
         * @return : The polygon builder with the given color.
         */
        public PolygonBuilder withColor(final Color colorInput) {
            this.color = colorInput;
            return this;
        }

        /**
         * Function to set up the outline of the polygon.
         *
         * @param outlineInput : The outline to set up.
         * @return : The polygon builder with the given outline.
         */
        public PolygonBuilder withOutline(final boolean outlineInput) {
            this.outline = outlineInput;
            return this;
        }

        /**
         * Function to set up the fill of the polygon.
         *
         * @param fillInput : The fill to set up.
         * @return : The polygon builder with the given fill.
         */
        public PolygonBuilder withFill(final boolean fillInput) {
            this.fill = fillInput;
            return this;
        }

        /**
         * Function to set up the header of the polygon.
         *
         * @param headerInput : The header considered to set up.
         * @return : The polygon builder with a header considered.
         */
        public PolygonBuilder withHeader(final Header headerInput) {
            this.header = headerInput;
            return this;
        }

        /**
         * The build function that generates a polygon object.
         *
         * @return : A polygon object with the given parameters of the builder.
         */
        public Polygon build() {
            return new Polygon(cartesians, color, outline, fill, header);
        }

    }
}

