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
package org.orekit.czml.CzmlObjects.CzmlPrimaryObjects;

import cesiumlanguagewriter.BooleanCesiumWriter;
import cesiumlanguagewriter.Cartesian;
import cesiumlanguagewriter.PacketCesiumWriter;
import cesiumlanguagewriter.PathCesiumWriter;
import cesiumlanguagewriter.PositionCesiumWriter;
import org.orekit.czml.CzmlObjects.CzmlSecondaryObjects.SatelliteObjects.Path;
import org.orekit.czml.CzmlObjects.CzmlSecondaryObjects.SatelliteObjects.SatellitePosition;
import org.orekit.czml.CzmlEnum.OffsetType;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.orekit.attitudes.Attitude;
import org.orekit.utils.PVCoordinates;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/** Satellite Offset Path

 * <p>
 * This class aims at offsetting a path of a satellite in a given direction to build object with reference around this object.
 * </p>
 *
 * @since 1.0
 * @author Julien LEBLOND.
 */

public class SatelliteOffsetPath extends AbstractPrimaryObject implements CzmlPrimaryObject {

    /** .*/
    public static final String DEFAULT_ID = "OFFSET/";
    /** .*/
    public static final String DEFAULT_NAME = "Offset of : ";

    /** .*/
    private SatellitePosition positions;
    /** .*/
    private boolean display = false;
    /** .*/
    private boolean displayOnlyOnePeriod = false;
    /** .*/
    private SatellitePosition satellitePosition;
    /** .*/
    private Satellite satellite;

    // Builders

    public SatelliteOffsetPath(final Satellite satellite, final double offset, final OffsetType offsetType) {
        this.setId(DEFAULT_ID + satellite.getId());
        this.setName(DEFAULT_NAME + satellite.getName());
        this.setAvailability(satellite.getAvailability());
        this.satellite = satellite;
        final List<Cartesian> initialListOfCartesian = satellite.getCartesianArraylist();
        final List<Cartesian> listOfCartesianOffsetted = new ArrayList<>();
        final List<Double> timeList = satellite.getTimeList();
        final List<Cartesian> listCartesianOffset = getCartesianOffset(satellite, offset, offsetType);

        for (int i = 0; i < initialListOfCartesian.size(); i++) {
            final Cartesian cartesianOffset = listCartesianOffset.get(i);
            final Cartesian currentCartesian = initialListOfCartesian.get(i);
            final Cartesian currentOffsettedCartesian = currentCartesian.add(cartesianOffset);
            listOfCartesianOffsetted.add(currentOffsettedCartesian);
        }

        this.satellitePosition = new SatellitePosition(listOfCartesianOffsetted, timeList);
    }

    private static List<Cartesian> getCartesianOffset(final Satellite satellite, final double offset, final OffsetType offsetType) {

        final List<Attitude> attitudes = satellite.getAttitudes();
        final List<Cartesian> toReturn = new ArrayList<>();

        for (int i = 0; i < attitudes.size(); i++) {
            final Attitude currentAttitude = attitudes.get(i);
            if (offsetType == OffsetType.CARTESIAN_X) {
                toReturn.add(new Cartesian(offset, 0, 0));
            } else if (offsetType == OffsetType.CARTESIAN_Y) {
                toReturn.add(new Cartesian(0, offset, 0));
            } else if (offsetType == OffsetType.CARTESIAN_Z) {
                toReturn.add(new Cartesian(0, 0, offset));

            } else if (offsetType == OffsetType.VELOCITY_TANGENT) {

                final PVCoordinates PVCoordinatesOfSatellite = satellite.getOrbits().get(i).getPVCoordinates();
                final Vector3D positionSat =  PVCoordinatesOfSatellite.getPosition();
                final Vector3D toApply = currentAttitude.getRotation().applyTo(Vector3D.PLUS_I);
                final Vector3D transformedPosition = positionSat.add(toApply.scalarMultiply(offset));
                toReturn.add(new Cartesian(transformedPosition.getX(), transformedPosition.getY(), transformedPosition.getZ()));
            } else if (offsetType == OffsetType.W_VELOCITY) {

                toReturn.add(new Cartesian(0, 0, offset));
            } else if (offsetType == OffsetType.NORMAL_TO_PLANE) {

                toReturn.add(new Cartesian(0, 0, offset));
            } else {
                throw new RuntimeException("Offset type is not recognized");
            }
        }
        return toReturn;
    }

    // Getters

    public Satellite getSatellite() {
        return satellite;
    }

    public SatellitePosition getSatellitePosition() {
        return satellitePosition;
    }

    // Overrides

    @Override
    public void writeCzmlBlock() throws URISyntaxException, IOException {
        OUTPUT.setPrettyFormatting(true);
        try (PacketCesiumWriter packet = STREAM.openPacket(OUTPUT)) {
            packet.writeId(getId());
            packet.writeName(getName());
            packet.writeAvailability(getAvailability());

            try (PositionCesiumWriter writer = packet.openPositionProperty()) {
                writer.writeReferenceFrame(this.satellitePosition.getReferenceFrame());
                writer.writeInterpolationAlgorithm(this.satellitePosition.getCesiumInterpolationAlgorithm());
                writer.writeInterpolationDegree(this.satellitePosition.getInterpolationDegree());
                writer.writeCartesian(this.satellitePosition.getDates(), this.satellitePosition.getPositions());
            }
        }
    }

    @Override
    public StringWriter getStringWriter() {
        return STRING_WRITER;
    }

    @Override
    public void cleanObject() {

    }

    // Functions

    public void displayOffsetPath() {
        display = true;
    }

    private void generatePath(final PacketCesiumWriter packet) {
        try (PathCesiumWriter pathProperty = packet.openPathProperty()) {
            if (display) {
                if (!displayOnlyOnePeriod) {
                    final Path path = new Path(getAvailability(), packet);
                    try (BooleanCesiumWriter showPath = pathProperty.openShowProperty()) {
                        showPath.writeInterval(getAvailability().getStart(), getAvailability().getStop());
                        showPath.writeBoolean(path.getShow());
                    }
                } else {
                    final Path path = new Path(getAvailability(), packet);
                    pathProperty.writeLeadTimeProperty(this.satellite.getOrbits().get(0).getKeplerianPeriod());
                    pathProperty.writeTrailTimeProperty(0.0);
                    try (BooleanCesiumWriter showPath = pathProperty.openShowProperty()) {
                        showPath.writeInterval(getAvailability().getStart(), getAvailability().getStop());
                        showPath.writeBoolean(path.getShow());
                    }
                    displayOnlyOnePeriod = false;
                }
            }
        }
    }
}
