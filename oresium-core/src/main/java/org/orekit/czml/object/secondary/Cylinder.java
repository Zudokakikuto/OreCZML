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

import cesiumlanguagewriter.CesiumHeightReference;
import cesiumlanguagewriter.CesiumOutputStream;
import cesiumlanguagewriter.CylinderCesiumWriter;
import cesiumlanguagewriter.MaterialCesiumWriter;
import cesiumlanguagewriter.PacketCesiumWriter;
import cesiumlanguagewriter.SolidColorMaterialCesiumWriter;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.hipparchus.util.FastMath;
import org.orekit.czml.object.Position;
import org.orekit.czml.object.PositionType;
import org.orekit.czml.object.primary.entities.CzmlGroundStation;
import org.orekit.czml.object.primary.entities.Spacecraft;
import org.orekit.frames.TopocentricFrame;
import org.orekit.utils.Constants;

import java.awt.Color;

/**
 * Cylinder class
 * <p>
 * This class allows the user to display a cylinder (that can be shaped as a
 * cone) with variables geometries.
 * </p>
 *
 * @author LEBLOND Julien
 * @since 1.0.0
 */
public class Cylinder
    extends
    AbstractSecondaryObject<Cylinder> {

    /**
     * The length of the cylinder.
     */
    private final double length;

    /**
     * The radius of the top base of the cylinder.
     */
    private double topRadius;

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
    private CesiumHeightReference heightReference;

    /**
     * The color of the cylinder.
     */
    private Color color;

    /**
     * The clock.
     */
    private final Clock clock;

    /**
     * To show or not the cylinder.
     */
    private boolean show = false;

    /** The topocentric frame if one is used. */
    private TopocentricFrame topocentricFrame;

    /** The ground station if one is used. */
    private CzmlGroundStation groundStation;

    /** The spacecraft if one is used. */
    private Spacecraft spacecraft;

    /** The angle of the aperture of the cylinder (cone). */
    private double angleOfAperture;

    // Constructors

    /**
     * The basic constructor of the cylinder, by defining all the primary
     * characteristics.
     *
     * @param length : The length of the cylinder.
     * @param topRadius : The radius of the top base.
     * @param bottomRadius : The radius of the bottom base.
     * @param color : The color of the cylinder.
     * @param position : The position of the cylinder.
     * @param heightReference : The height reference of the base.
     * @param clock : The availability of the cylinder.
     */
    public Cylinder(final double length, final double topRadius,
                    final double bottomRadius, final Color color,
                    final Position position,
                    final CesiumHeightReference heightReference,
                    final Clock clock) {
        this.length = length;
        this.topRadius = topRadius;
        this.bottomRadius = bottomRadius;
        this.color = color;
        this.position = position;
        this.show = true;
        this.heightReference = heightReference;
        this.clock = clock;
    }

    /**
     * The cylinder constructor from a czml station and a satellite. This helps
     * define a visibility cone.
     *
     * @param topocentricFrame : The topocentric ram representing the ground
     *        station.
     * @param satellite : The satellite that will be observed by the station.
     * @param angleOfAperture : The angle of aperture of the visibility of the
     *        station in degrees.
     * @param clock : The clock considered.
     */
    public Cylinder(final TopocentricFrame topocentricFrame,
                    final Spacecraft satellite, final double angleOfAperture,
                    final Clock clock) {
        final Color colorTemp = new Color(255, 255, 255, 50);

        final Vector3D positionInCartesian =
            topocentricFrame.getCartesianPoint();

        final double x = positionInCartesian.getX();
        final double y = positionInCartesian.getY();
        final double z = positionInCartesian.getZ();
        final PositionType positionType = PositionType.CARTESIAN_POSITION;

        this.length =
            satellite.getOrbits().get(0).getA() -
                      Constants.WGS84_EARTH_EQUATORIAL_RADIUS;
        // Angle of aperture in degrees !
        this.topRadius =
            length * FastMath.tan(FastMath.toRadians(angleOfAperture));
        this.bottomRadius = 10.0;
        this.position = new Position(x, y, z, positionType, clock);
        this.color = colorTemp;
        this.show = true;
        this.heightReference = CesiumHeightReference.CLAMP_TO_GROUND;
        this.clock = clock;
    }

    /**
     * The cylinder constructor from a single station and an angle of aperture.
     *
     * @param inputGroundStation : The czml ground station that will have a
     *        visibility cone.
     * @param angleOfApertureInput : The angle of aperture of the visibility of
     *        the station
     * @param clock : The clock considered.
     */
    public Cylinder(final CzmlGroundStation inputGroundStation,
                    final double angleOfApertureInput, final Clock clock) {

        final Color colorTemp = new Color(255, 255, 255, 50);

        final double x = inputGroundStation.getPositions().getX();
        final double y = inputGroundStation.getPositions().getY();
        final double z = inputGroundStation.getPositions().getZ();
        final PositionType positionType = PositionType.CARTESIAN_POSITION;

        this.angleOfAperture = angleOfApertureInput;
        this.groundStation = inputGroundStation;
        this.length = Constants.WGS84_EARTH_EQUATORIAL_RADIUS;
        this.topRadius =
            length * FastMath.tan(FastMath.toRadians(angleOfAperture));
        this.bottomRadius = 0.0;
        this.position = new Position(x, y, z, positionType, clock);
        this.color = colorTemp;
        this.heightReference = CesiumHeightReference.CLAMP_TO_GROUND;
        this.clock = clock;
    }

    /**
     * The cylinder constructor with a topocentric frame and an angle of
     * aperture.
     *
     * @param topocentricFrame : The topocentric frame where the ground station
     *        must be.
     * @param angleOfApertureInput : The angle of aperture of the visibility of
     *        the station
     * @param clock : The clock considered.
     */
    public Cylinder(final TopocentricFrame topocentricFrame,
                    final double angleOfApertureInput, final Clock clock) {
        final Color colorTemp = new Color(255, 255, 255, 50);

        final double x = topocentricFrame.getCartesianPoint().getX();
        final double y = topocentricFrame.getCartesianPoint().getY();
        final double z = topocentricFrame.getCartesianPoint().getZ();

        this.angleOfAperture = angleOfApertureInput;
        this.topocentricFrame = topocentricFrame;
        final PositionType positionType = PositionType.CARTESIAN_POSITION;

        this.length = Constants.WGS84_EARTH_EQUATORIAL_RADIUS;
        this.topRadius =
            length * FastMath.tan(FastMath.toRadians(angleOfApertureInput));
        this.bottomRadius = 0.0;
        this.position = new Position(x, y, z, positionType, clock);
        this.color = colorTemp;
        this.heightReference = CesiumHeightReference.CLAMP_TO_GROUND;
        this.clock = clock;
    }

    /**
     * The cylinder constructor from a czml station and a satellite. This helps
     * define a visibility cone.
     *
     * @param topocentricFrame : The topocentric ram representing the ground
     *        station.
     * @param spacecraftInput : The satellite that will be observed by the
     *        station.
     * @param angleOfApertureInput : The angle of aperture of the visibility of
     *        the station in degrees.
     * @param clock : The clock considered.
     */
    public Cylinder(final TopocentricFrame topocentricFrame,
                    final double angleOfApertureInput, final Clock clock,
                    final Spacecraft spacecraftInput) {
        final Color colorTemp = new Color(255, 255, 255, 50);

        final Vector3D positionInCartesian =
            topocentricFrame.getCartesianPoint();
        this.topocentricFrame = topocentricFrame;

        final double x = positionInCartesian.getX();
        final double y = positionInCartesian.getY();
        final double z = positionInCartesian.getZ();
        final PositionType positionType = PositionType.CARTESIAN_POSITION;
        this.spacecraft = spacecraftInput;

        this.length =
            spacecraftInput.getOrbits().get(0).getA() -
                      Constants.WGS84_EARTH_EQUATORIAL_RADIUS;
        // Angle of aperture in degrees !
        this.topRadius =
            length * FastMath.tan(FastMath.toRadians(angleOfApertureInput));
        this.bottomRadius = 10.0;
        this.position = new Position(x, y, z, positionType, clock);
        this.color = colorTemp;
        this.show = true;
        this.heightReference = CesiumHeightReference.CLAMP_TO_GROUND;
        this.clock = clock;
    }

    // Overrides

    @Override
    public void write(final PacketCesiumWriter packetWriter,
                      final CesiumOutputStream output) {

        try (CylinderCesiumWriter cylinderWriter =
            packetWriter.getCylinderWriter()) {
            cylinderWriter.open(output);
            cylinderWriter.writeBottomRadiusProperty(this.bottomRadius);
            cylinderWriter.writeTopRadiusProperty(this.topRadius);
            cylinderWriter.writeLengthProperty(this.length);

            final MaterialCesiumWriter materialWriter =
                cylinderWriter.getMaterialWriter();
            materialWriter.open(output);
            output.writeStartObject();

            final SolidColorMaterialCesiumWriter solidColorWriter =
                materialWriter.getSolidColorWriter();
            solidColorWriter.open(output);
            solidColorWriter.writeColorProperty(color);
            output.writeEndObject();
            solidColorWriter.close();
            materialWriter.close();

            cylinderWriter.writeHeightReferenceProperty(heightReference);
        }
    }

    @Override
    public Cylinder cloneObject() {
        final Cylinder toReturn;
        if (this.topocentricFrame != null) {
            if (this.spacecraft != null) {
                toReturn =
                    new Cylinder(this.topocentricFrame, this.angleOfAperture,
                                 this.clock, this.spacecraft);
            } else {
                toReturn =
                    new Cylinder(this.topocentricFrame, this.angleOfAperture,
                                 this.clock);
            }
        } else if (this.groundStation != null) {
            toReturn =
                new Cylinder(this.groundStation, this.angleOfAperture,
                             this.clock);
        } else {
            toReturn =
                new Cylinder(this.length, this.topRadius, this.bottomRadius,
                             this.color, this.position, this.heightReference,
                             this.clock);
        }
        toReturn.setShow(this.show);
        toReturn.setTopRadius(this.topRadius);
        toReturn.setColor(this.color);
        toReturn.setHeightReference(this.heightReference);
        toReturn.setAngleOfAperture(this.angleOfAperture);
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
     * Sets color.
     *
     * @param color the color
     */
    public void setColor(final Color color) {
        this.color = color;
    }

    /**
     * Sets heightReference.
     *
     * @param heightReference the heightReference
     */
    public void
        setHeightReference(final CesiumHeightReference heightReference) {
        this.heightReference = heightReference;
    }

    /**
     * Sets radius.
     *
     * @param radius the radius
     */
    public void setTopRadius(final double radius) {
        this.topRadius = radius;
    }

    /**
     * Sets show.
     *
     * @param show the show
     */
    public void setShow(final boolean show) {
        this.show = show;
    }

    /**
     * Sets topocentricFrame.
     *
     * @param topocentricFrame the topocentricFrame
     */
    public void setTopocentricFrame(final TopocentricFrame topocentricFrame) {
        this.topocentricFrame = topocentricFrame;
    }

    /**
     * Sets groundStation.
     *
     * @param groundStation the groundStation
     */
    public void setGroundStation(final CzmlGroundStation groundStation) {
        this.groundStation = groundStation.cloneObject();
    }

    /**
     * Sets spacecraft.
     *
     * @param spacecraft the spacecraft
     */
    public void setSpacecraft(final Spacecraft spacecraft) {
        this.spacecraft = spacecraft.cloneObject();
    }

    /**
     * Sets angleOfAperture.
     *
     * @param angleOfAperture the angleOfAperture
     */
    public void setAngleOfAperture(final double angleOfAperture) {
        this.angleOfAperture = angleOfAperture;
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

    /**
     * Gets top radius.
     *
     * @return the top radius
     */
    public double getTopRadius() {
        return topRadius;
    }

    /**
     * Gets time frame for which feature is available.
     *
     * @return the time interval value
     */
    public Clock getClock() {
        return clock;
    }

    /**
     * Gets the topocentric frame.
     *
     * @return the topocentric frame
     */
    public TopocentricFrame getTopocentricFrame() {
        return topocentricFrame;
    }

    /**
     * Gets the ground station.
     *
     * @return the ground station
     */
    public CzmlGroundStation getGroundStation() {
        return groundStation.cloneObject();
    }

    /**
     * Gets the spacecraft.
     *
     * @return the spacecraft
     */
    public Spacecraft getSpacecraft() {
        return spacecraft.cloneObject();
    }
}
