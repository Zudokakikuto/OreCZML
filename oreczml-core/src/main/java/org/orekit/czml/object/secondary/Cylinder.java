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
import cesiumlanguagewriter.CesiumHeightReference;
import cesiumlanguagewriter.CesiumOutputStream;
import cesiumlanguagewriter.CylinderCesiumWriter;
import cesiumlanguagewriter.JulianDate;
import cesiumlanguagewriter.MaterialCesiumWriter;
import cesiumlanguagewriter.PacketCesiumWriter;
import cesiumlanguagewriter.SolidColorMaterialCesiumWriter;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.hipparchus.util.FastMath;
import org.orekit.czml.object.Position;
import org.orekit.czml.object.PositionType;
import org.orekit.czml.object.primary.entities.CzmlGroundStation;
import org.orekit.czml.object.primary.Header;
import org.orekit.czml.object.primary.entities.Spacecraft;
import org.orekit.frames.TopocentricFrame;
import org.orekit.utils.Constants;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Cylinder class
 *
 * <p> This class allows the user to display a cylinder (that can be shaped as a cone) with variables geometries.</p>
 *
 * @author Julien LEBLOND
 * @since 1.0.0
 */
public class Cylinder extends AbstractSecondaryObject {

    /**
     * The length of the cylinder.
     */
    private final double length;

    /**
     * The radius of the top base of the cylinder.
     */
    private final double topRadius;

    /**
     * The radius of the bottom base of the cylinder.
     */
    private final double bottomRadius;

    /**
     * The position of the cylinder.
     */
    private final Position position;

    /**
     * The height reference of the base.
     */
    private final CesiumHeightReference heightReference;

    /**
     * The color of the cylinder.
     */
    private Color color;

    /**
     * To show or not the cylinder.
     */
    private boolean show = false;

    // Constructors

    /**
     * The basic constructor of the cylinder, by defining all the primary characteristics.
     *
     * @param length          : The length of the cylinder.
     * @param topRadius       : The radius of the top base.
     * @param bottomRadius    : The radius of the bottom base.
     * @param color           : The color of the cylinder.
     * @param position        : The position of the cylinder.
     * @param heightReference : The height reference of the base.
     * @param header          : The header considered.
     */
    public Cylinder(final double length, final double topRadius, final double bottomRadius, final Color color,
                    final Position position, final CesiumHeightReference heightReference, final Header header) {
        this.length          = length;
        this.topRadius       = topRadius;
        this.bottomRadius    = bottomRadius;
        this.color           = color;
        this.position        = position;
        this.show            = true;
        this.heightReference = heightReference;
    }

    /**
     * The cylinder constructor from a czml station and a satellite. This helps define a visibility cone.
     *
     * @param topocentricFrame : The topocentric ram representing the ground station.
     * @param satellite        : The satellite that will be observed by the station.
     * @param angleOfAperture  : The angle of aperture of the visibility of the station in degrees.
     * @param header           : The header considered.
     */
    public Cylinder(final TopocentricFrame topocentricFrame, final Spacecraft satellite,
                    final double angleOfAperture, final Header header) {
        final Color color_temp = new Color(255, 255, 255, 50);

        final Vector3D positionInCartesian = topocentricFrame.getCartesianPoint();

        final double       x            = positionInCartesian.getX();
        final double       y            = positionInCartesian.getY();
        final double       z            = positionInCartesian.getZ();
        final PositionType positionType = PositionType.CARTESIAN_POSITION;

        this.length = satellite.getOrbits()
                               .get(0)
                               .getA() - Constants.WGS84_EARTH_EQUATORIAL_RADIUS;
        // Angle of aperture in degrees !
        this.topRadius       = length * FastMath.tan(FastMath.toRadians(angleOfAperture));
        this.bottomRadius    = 10.0;
        this.position        = new Position(x, y, z, positionType, header);
        this.color           = color_temp;
        this.show            = true;
        this.heightReference = CesiumHeightReference.CLAMP_TO_GROUND;
    }


    /**
     * The cylinder constructor from a single station and an angle of aperture.
     *
     * @param InputGroundStation : The czml ground station that will have a visibility cone.
     * @param angleOfAperture    : The angle of aperture of the visibility of the station
     * @param header             : The header considered.
     */
    public Cylinder(final CzmlGroundStation InputGroundStation, final double angleOfAperture, final Header header) {

        final Color color_temp = new Color(255, 255, 255, 50);

        final double x = InputGroundStation.getPositions()
                                           .getX();
        final double y = InputGroundStation.getPositions()
                                           .getY();
        final double z = InputGroundStation.getPositions()
                                           .getZ();
        final PositionType positionType = PositionType.CARTESIAN_POSITION;

        this.length          = Constants.WGS84_EARTH_EQUATORIAL_RADIUS;
        this.topRadius       = length * FastMath.tan(FastMath.toRadians(angleOfAperture));
        this.bottomRadius    = 0.0;
        this.position        = new Position(x, y, z, positionType, header);
        this.color           = color_temp;
        this.heightReference = CesiumHeightReference.CLAMP_TO_GROUND;
    }

    /**
     * The cylinder constructor with a topocentric frame and an angle of aperture.
     *
     * @param topocentricFrame : The topocentric frame where the ground station must be.
     * @param angleOfAperture  : The angle of aperture of the visibility of the station
     * @param header           : The header considered.
     */
    public Cylinder(final TopocentricFrame topocentricFrame, final double angleOfAperture, final Header header) {
        final Color color_temp = new Color(255, 255, 255, 50);

        final double x = topocentricFrame.getCartesianPoint()
                                         .getX();
        final double y = topocentricFrame.getCartesianPoint()
                                         .getY();
        final double z = topocentricFrame.getCartesianPoint()
                                         .getZ();

        final PositionType positionType = PositionType.CARTESIAN_POSITION;

        this.length          = Constants.WGS84_EARTH_EQUATORIAL_RADIUS;
        this.topRadius       = length * FastMath.tan(FastMath.toRadians(angleOfAperture));
        this.bottomRadius    = 0.0;
        this.position        = new Position(x, y, z, positionType, header);
        this.color           = color_temp;
        this.heightReference = CesiumHeightReference.CLAMP_TO_GROUND;
    }


    // Overrides

    @Override
    public void write(final PacketCesiumWriter packetWriter, final CesiumOutputStream output) {

        try (CylinderCesiumWriter cylinderWriter = packetWriter.getCylinderWriter()) {
            cylinderWriter.open(output);
            cylinderWriter.writeBottomRadiusProperty(this.bottomRadius);
            cylinderWriter.writeTopRadiusProperty(this.topRadius);
            cylinderWriter.writeLengthProperty(this.length);

            final MaterialCesiumWriter materialWriter = cylinderWriter.getMaterialWriter();
            materialWriter.open(output);
            output.writeStartObject();

            final SolidColorMaterialCesiumWriter solidColorWriter = materialWriter.getSolidColorWriter();
            solidColorWriter.open(output);
            solidColorWriter.writeColorProperty(color);
            output.writeEndObject();
            solidColorWriter.close();
            materialWriter.close();

            cylinderWriter.writeHeightReferenceProperty(heightReference);
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
     * Sets color.
     *
     * @param color the color
     */
    public void setColor(final Color color) {
        this.color = color;
    }

    /**
     * Gets bottom radius.
     *
     * @return the bottom radius
     */
    public double getBottomRadius() {
        return bottomRadius;
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
     * Gets position.
     *
     * @return the position
     */
    public Position getPosition() {
        return position;
    }

    /**
     * Gets length.
     *
     * @return the length
     */
    public double getLength() {
        return length;
    }


    // Setters

    /**
     * Gets top radius.
     *
     * @return the top radius
     */
    public double getTopRadius() {
        return topRadius;
    }

    /**
     * Czml Ellipsoid builder class
     * <p>
     * Builder for the {@link CzmlEllipsoid} class.
     *
     * @author Julien LEBLOND
     * @since 1.0.0
     */
    public static class CzmlEllipsoidBuilder {

        /**
         * The default color for the ellipsoid.
         */
        public static final Color DEFAULT_COLOR = new Color(255, 255, 0, 255);

        /**
         * The default fill input.
         */
        public static final boolean DEFAULT_FILL = false;

        /**
         * The default outline input.
         */
        public static final boolean DEFAULT_OUTLINE = true;

        /**
         * The default number of slice partitions.
         */
        public static final int DEFAULT_SLICE_PARTITION = 24;

        /**
         * The default number of stack partitions.
         */
        public static final int DEFAULT_STACK_PARTITION = 36;

        /**
         * The color of the ellipsoid.
         */
        private Color color;

        /**
         * The number of slices (from one point on convergence of lines from the other).
         */
        private int slicePartition = DEFAULT_SLICE_PARTITION;

        /**
         * The number of stacks, (number of parallels lines around the ellipsoid).
         */
        private int stackPartition = DEFAULT_STACK_PARTITION;

        /**
         * To fill or not with the color the ellipsoid.
         */
        private boolean fill = DEFAULT_FILL;

        /**
         * To display the outline of not of the ellipsoid.
         */
        private boolean outline = DEFAULT_OUTLINE;

        /**
         * The cartesian position of the ellipsoid.
         */
        private Cartesian cartesian;

        /**
         * The julian dates when the ellipsoid will be displayed.
         */
        private List<JulianDate> julianDates = new ArrayList<>();

        /**
         * The list of cartesians representing the positions of the ellipsoid (if several positions are given).
         */
        private List<Cartesian> cartesians = new ArrayList<>();

        /**
         * A parameter to know if the builder was built with julian dates and cartesians or with an availability and a cartesian.
         */
        private final boolean multipleBuilder;

        /** The header considered. */
        private Header header;

        // Constructors

        /**
         * The constructor of the multiple ellipsoid builder.
         *
         * @param cartesianInput : The dimensions of the ellipsoid.
         * @param headerInput    : The header considered.
         */
        public CzmlEllipsoidBuilder(final Cartesian cartesianInput, final Header headerInput) {
            this.header          = headerInput;
            this.cartesian       = cartesianInput;
            this.multipleBuilder = true;
        }

        /**
         * The constructor of the single ellipsoid builder.
         *
         * @param julianDates : The dates where the ellipsoid should be displayed.
         * @param dimensions  : The dimensions of the ellipsoid.
         * @param headerInput : The header considered.
         */
        public CzmlEllipsoidBuilder(final List<JulianDate> julianDates, final List<Cartesian> dimensions,
                                    final Header headerInput) {
            this.header          = headerInput;
            this.julianDates     = new ArrayList<>(julianDates);
            this.cartesians      = new ArrayList<>(dimensions);
            this.multipleBuilder = false;
        }

        /**
         * Function to set up a color.
         *
         * @param colorInput : The color to set up.
         * @return : The ellipsoid builder with the given color.
         */
        public CzmlEllipsoidBuilder withColor(final Color colorInput) {
            this.color = colorInput;
            return this;
        }

        /**
         * Function to set up a slice and a stack partition.
         *
         * @param slicePartitionInput : The slice partition to set up.
         * @param stackPartitionInput : The stack partition to set up.
         * @return : The ellipsoid builder with the slice and stack partition.
         */
        public CzmlEllipsoidBuilder withSliceStackPartition(final int slicePartitionInput, final int stackPartitionInput) {
            this.slicePartition = slicePartitionInput;
            this.stackPartition = stackPartitionInput;
            return this;
        }

        /**
         * Function to set up if the ellipsoid must be filled or not.
         *
         * @param fillInput : The fill to set up.
         * @return : The ellipsoid builder with the given fill input.
         */
        public CzmlEllipsoidBuilder withFill(final boolean fillInput) {
            this.fill = fillInput;
            return this;
        }

        /**
         * Function to set up if the outline should be displayed or not.
         *
         * @param outlineInput : The outline input to set up.
         * @return : The ellipsoid builder with the given outline input.
         */
        public CzmlEllipsoidBuilder withOutline(final boolean outlineInput) {
            this.outline = outlineInput;
            return this;
        }

        /**
         * The build function that generates the czml ellipsoid object.
         *
         * @return : A czml ellipsoid object with the given parameters of the builder.
         */
        public CzmlEllipsoid build() {
            if (multipleBuilder) {
                return new CzmlEllipsoid(cartesian, fill, outline, slicePartition, stackPartition, color,
                        header);
            } else {
                return new CzmlEllipsoid(julianDates, cartesians, fill, outline, slicePartition, stackPartition,
                        color, header);
            }
        }
    }
}
