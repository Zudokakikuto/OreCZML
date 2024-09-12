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

import cesiumlanguagewriter.CesiumHeightReference;
import cesiumlanguagewriter.CesiumOutputStream;
import cesiumlanguagewriter.CylinderCesiumWriter;
import cesiumlanguagewriter.MaterialCesiumWriter;
import cesiumlanguagewriter.PacketCesiumWriter;
import cesiumlanguagewriter.SolidColorMaterialCesiumWriter;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.hipparchus.util.FastMath;
import org.orekit.czml.CzmlEnum.PositionType;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.CzmlGroundStation;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.Satellite;
import org.orekit.czml.CzmlObjects.Position;
import org.orekit.frames.TopocentricFrame;
import org.orekit.utils.Constants;

import java.awt.Color;

/**
 * Cylinder class
 *
 * <p> This class allows the user to display a cylinder (that can be shaped as a cone) with variables geometries.</p>
 *
 * @author Julien LEBLOND
 * @since 1.0.0
 */

public class Cylinder implements CzmlSecondaryObject {

    /** The length of the cylinder. */
    private final double length;

    /** The radius of the top base of the cylinder. */
    private final double topRadius;

    /** The radius of the bottom base of the cylinder. */
    private final double                bottomRadius;
    /** The position of the cylinder. */
    private final Position              position;
    /** The height reference of the base. */
    private final CesiumHeightReference heightReference;
    /** The color of the cylinder. */
    private       Color                 color;
    /** To show or not the cylinder. */
    private       boolean               show = false;


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
     */
    public Cylinder(final double length, final double topRadius, final double bottomRadius, final Color color,
                    final Position position,
                    final CesiumHeightReference heightReference) {
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
     * @param InputGroundStation : The czml ground station that will have a visibility cone.
     * @param satellite          : The satellite that will be observed by the station.
     * @param angleOfAperture    : The angle of aperture of the visibility of the station
     */
    public Cylinder(final CzmlGroundStation InputGroundStation, final Satellite satellite,
                    final double angleOfAperture) {
        final Color color_temp = new Color(255, 255, 255, 50);

        final Vector3D positionInCartesian = InputGroundStation.getPositions();

        final double       x            = positionInCartesian.getX();
        final double       y            = positionInCartesian.getY();
        final double       z            = positionInCartesian.getZ();
        final PositionType positionType = PositionType.CARTESIAN_POSITION;

        this.length          = satellite.getOrbits()
                                        .get(0)
                                        .getA() / 2;
        this.topRadius       = length * FastMath.tan(angleOfAperture);
        this.bottomRadius    = 10.0;
        this.position        = new Position(x, y, z, positionType);
        this.color           = color_temp;
        this.show            = true;
        this.heightReference = CesiumHeightReference.CLAMP_TO_GROUND;
    }


    /**
     * The cylinder constructor from a single station and an angle of aperture.
     *
     * @param InputGroundStation : The czml ground station that will have a visibility cone.
     * @param angleOfAperture    : The angle of aperture of the visibility of the station
     */
    public Cylinder(final CzmlGroundStation InputGroundStation, final double angleOfAperture) {

        final Color color_temp = new Color(255, 255, 255, 50);

        final double x = InputGroundStation.getPositions()
                                           .getX();
        final double y = InputGroundStation.getPositions()
                                           .getY();
        final double z = InputGroundStation.getPositions()
                                           .getZ();
        final PositionType positionType = PositionType.CARTESIAN_POSITION;

        this.length          = Constants.WGS84_EARTH_EQUATORIAL_RADIUS;
        this.topRadius       = length * FastMath.tan(angleOfAperture);
        this.bottomRadius    = 0.0;
        this.position        = new Position(x, y, z, positionType);
        this.color           = color_temp;
        this.heightReference = CesiumHeightReference.CLAMP_TO_GROUND;
    }

    /**
     * The cylinder constructor with a topocentric frame and an angle of aperture.
     *
     * @param topocentricFrame : The topocentric frame where the ground station must be.
     * @param angleOfAperture  : The angle of aperture of the visibility of the station
     */
    public Cylinder(final TopocentricFrame topocentricFrame, final double angleOfAperture) {
        final Color color_temp = new Color(255, 255, 255, 50);

        final double x = topocentricFrame.getCartesianPoint()
                                         .getX();
        final double y = topocentricFrame.getCartesianPoint()
                                         .getY();
        final double z = topocentricFrame.getCartesianPoint()
                                         .getZ();

        final PositionType positionType = PositionType.CARTESIAN_POSITION;

        this.length          = Constants.WGS84_EARTH_EQUATORIAL_RADIUS;
        this.topRadius       = length * FastMath.tan(angleOfAperture);
        this.bottomRadius    = 0.0;
        this.position        = new Position(x, y, z, positionType);
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

    public Color getColor() {
        return color;
    }

    public void setColor(final Color color) {
        this.color = color;
    }

    public double getBottomRadius() {
        return bottomRadius;
    }

    public boolean getShow() {
        return show;
    }

    public Position getPosition() {
        return position;
    }

    public double getLength() {
        return length;
    }


    // Setters

    public double getTopRadius() {
        return topRadius;
    }
}
