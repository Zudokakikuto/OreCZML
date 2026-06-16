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
package org.orekit.czml.object;

import cesiumlanguagewriter.Cartesian;
import cesiumlanguagewriter.Cartographic;
import cesiumlanguagewriter.CesiumInterpolationAlgorithm;
import cesiumlanguagewriter.CesiumOutputStream;
import cesiumlanguagewriter.CesiumStreamWriter;
import cesiumlanguagewriter.PacketCesiumWriter;
import cesiumlanguagewriter.PositionCesiumWriter;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.hipparchus.util.FastMath;
import org.orekit.czml.errors.OresiumException;
import org.orekit.czml.errors.OresiumMessages;
import org.orekit.czml.object.secondary.Clock;
import org.orekit.czml.object.secondary.TimePosition;

import java.io.StringWriter;

/**
 * Position
 * <p>
 * The position object is used to represent a position of any object except the
 * satellite (see {@link TimePosition} for more information). The position
 * object can be build in many ways, following all the position type that exists
 * : {@link PositionType}.
 * </p>
 *
 * @author LEBLOND Julien.
 * @since 1.0.0
 */
public class Position {

    /**
     * The reference frame to use 'INERTIAL' or 'FIXED'.
     */
    private final String ReferenceFrame;

    /**
     * The {@link PositionType} that defines the type of position the Position
     * object has.
     */
    private final PositionType positionType;

    //// Cartographic :

    /**
     * The height of the object (used only when longitudes and latitudes are
     * defined).
     */
    private double height;

    // Cartographic radians

    /**
     * The longitude in radians.
     */
    private double longitude;

    /**
     * The latitude in radians.
     */
    private double latitude;

    // Cartographic degrees

    /**
     * The longitude in degrees.
     */
    private double longitudeDeg;

    /**
     * The latitude in degrees.
     */
    private double latitudeDeg;

    //// Cartesian :

    // Cartesian3Value:

    /**
     * The cartesian position in the x-axis.
     */
    private double x;

    /**
     * The cartesian position in the y-axis.
     */
    private double y;

    /**
     * The cartesian position in the z-axis.
     */
    private double z;

    /**
     * The timeframe for which the feature is visible.
     */
    private Clock clock;

    // Constructor

    /**
     * The basic constructor for the position object.
     *
     * @param param1 : The first parameter of the tuple (can be x, vx, longitude
     *        or longitude deg)
     * @param param2 : The second parameter of the tuple (can be y, vy, latitude
     *        or latitude deg)
     * @param param3 : The third parameter of the tuple (can be z, vz or height)
     * @param positionType : The type of the tuple.
     * @param clock : The availability of the position
     */
    public Position(final double param1, final double param2,
                    final double param3, final PositionType positionType,
                    final Clock clock) {
        if (positionType == PositionType.CARTESIAN_POSITION) {
            this.x = param1;
            this.y = param2;
            this.z = param3;
        } else if (positionType == PositionType.CARTOGRAPHIC_RADIANS) {
            this.longitude = param1;
            this.latitude = param2;
            this.height = param3;
        } else if (positionType == PositionType.CARTOGRAPHIC_DEGREES) {
            this.longitudeDeg = param1;
            this.latitudeDeg = param2;
            this.height = param3;
        } else {
            throw new OresiumException(OresiumMessages.POSITION_TYPE_UNKNOWN);
        }

        this.positionType = positionType;
        this.ReferenceFrame = "INERTIAL";
        this.clock = clock;
    }

    // Display functions

    /**
     * This method does not write a reference frame nor an interpolation
     * degree/algorithm for the position.
     *
     * @param packetWriter : packet to write in the CZML
     * @param output : Output that will contain the string
     */
    public void write(final PacketCesiumWriter packetWriter,
                      final CesiumOutputStream output) {
        try (PositionCesiumWriter positionWriter =
            packetWriter.getPositionWriter()) {
            positionWriter.open(output);
            positionWriter.writeInterval(clock.getAvailability());

            if (positionType == PositionType.CARTESIAN_POSITION) {
                final Cartesian cartesian =
                    new Cartesian(this.x, this.y, this.z);
                positionWriter.writeCartesian(cartesian);
            } else if (positionType == PositionType.CARTOGRAPHIC_RADIANS) {
                final Cartographic cartographicRadians =
                    new Cartographic(this.longitude, this.latitude,
                                     this.height);
                positionWriter.writeCartographicRadians(cartographicRadians);
            } else if (positionType == PositionType.CARTOGRAPHIC_DEGREES) {
                final Cartographic cartographicDegrees =
                    new Cartographic(this.longitudeDeg, this.latitudeDeg,
                                     this.height);
                positionWriter.writeCartographicDegrees(cartographicDegrees);
            }
        }
    }

    /**
     * This method allows the writing of a referenceFrame and of an
     * interpolation algorithm and the degree of interpolation.
     *
     * @param packetWriter : packet to write in the CZML
     * @param output : Output that will contain the string
     * @param referenceFrame : the frame where the position is referenced
     */
    public void write(final PacketCesiumWriter packetWriter,
                      final CesiumOutputStream output,
                      final String referenceFrame) {
        try (PositionCesiumWriter positionWriter =
            packetWriter.getPositionWriter()) {
            positionWriter.open(output);
            positionWriter.writeInterval(clock.getAvailability());
            positionWriter.writeReferenceFrame(referenceFrame);
            positionWriter
                .writeInterpolationAlgorithm(CesiumInterpolationAlgorithm.LAGRANGE);
            positionWriter.writeInterpolationDegree(5);

            if (positionType == PositionType.CARTESIAN_POSITION) {
                final Cartesian cartesian =
                    new Cartesian(this.x, this.y, this.z);
                positionWriter.writeCartesian(cartesian);
            } else if (positionType == PositionType.CARTOGRAPHIC_RADIANS) {
                final Cartographic cartographicRadians =
                    new Cartographic(this.longitude, this.latitude,
                                     this.height);
                positionWriter.writeCartographicRadians(cartographicRadians);
            } else if (positionType == PositionType.CARTOGRAPHIC_DEGREES) {
                final Cartographic cartographicDegrees =
                    new Cartographic(this.longitudeDeg, this.latitudeDeg,
                                     this.height);
                positionWriter.writeCartographicDegrees(cartographicDegrees);
            }
        }
    }

    /**
     * Method to return the string of the position object written in the czml
     * file.
     *
     * @return : A string that would be written in a czml file.
     */
    public String toString() {
        final StringWriter writerTemp = new StringWriter();
        final CesiumOutputStream output = new CesiumOutputStream(writerTemp);
        final CesiumStreamWriter streamWriter = new CesiumStreamWriter();
        output.setPrettyFormatting(true);
        try (PacketCesiumWriter packet = streamWriter.openPacket(output)) {
            this.write(packet, output);
        }
        return writerTemp.toString();
    }

    /**
     * This method to return the string of the position object written with a
     * given reference frame.
     *
     * @param referenceFrame : The reference frame where the position is
     *        referenced.
     * @return : A string that would be written in a czml file.
     */
    public String toString(final String referenceFrame) {
        final StringWriter writerTemp = new StringWriter();
        final CesiumOutputStream output = new CesiumOutputStream(writerTemp);
        final CesiumStreamWriter streamWriter = new CesiumStreamWriter();
        output.setPrettyFormatting(true);
        try (PacketCesiumWriter packet = streamWriter.openPacket(output)) {
            this.write(packet, output, referenceFrame);
        }
        return writerTemp.toString();
    }

    // Getters

    /**
     * Gets latitude rad.
     *
     * @return the latitude rad
     */
    public double getLatitudeRad() {
        if (latitude != 0.0) {
            return latitude;
        } else if (latitudeDeg != 0.0) {
            return latitudeDeg * (FastMath.PI / 180);
        } else {
            throw new OresiumException(OresiumMessages.DEFAULT_ERROR_LATITUDE);
        }
    }

    /**
     * Gets longitude rad.
     *
     * @return the longitude rad
     */
    public double getLongitudeRad() {
        if (longitude != 0.0) {
            return longitude;
        } else if (longitudeDeg != 0.0) {
            return longitudeDeg * (FastMath.PI / 180);
        } else {
            throw new OresiumException(OresiumMessages.DEFAULT_ERROR_LONGITUDE);
        }
    }

    /**
     * Gets reference frame.
     *
     * @return the reference frame
     */
    public String getReferenceFrame() {
        return ReferenceFrame;
    }

    /**
     * Gets height.
     *
     * @return the height
     */
    public double getHeight() {
        if (height != 0.0) {
            return height;
        } else {
            throw new OresiumException(OresiumMessages.EMPTY_POSITION_HEIGHT);
        }
    }

    /**
     * Gets latitude deg.
     *
     * @return the latitude deg
     */
    public double getLatitudeDeg() {
        if (latitude != 0.0) {
            return latitude * (180 / FastMath.PI);
        } else if (latitudeDeg != 0.0) {
            return latitudeDeg;
        } else {
            throw new OresiumException(OresiumMessages.DEFAULT_ERROR_LATITUDE);
        }
    }

    /**
     * Gets longitude deg.
     *
     * @return the longitude deg
     */
    public double getLongitudeDeg() {
        if (longitude != 0.0) {
            return longitude * (180 / FastMath.PI);
        } else if (longitudeDeg != 0.0) {
            return longitudeDeg;
        } else {
            throw new OresiumException(OresiumMessages.DEFAULT_ERROR_LONGITUDE);
        }
    }

    /**
     * Gets x.
     *
     * @return the x
     */
    public double getX() {
        if (x != 0.0) {
            return x;
        } else {
            throw new OresiumException(OresiumMessages.EMPTY_X);
        }
    }

    /**
     * Gets y.
     *
     * @return the y
     */
    public double getY() {
        if (y != 0.0) {
            return y;
        } else {
            throw new OresiumException(OresiumMessages.EMPTY_Y);
        }
    }

    /**
     * Gets z.
     *
     * @return the z
     */
    public double getZ() {
        if (z != 0.0) {
            return z;
        } else {
            throw new OresiumException(OresiumMessages.EMPTY_Z);
        }
    }

    /**
     * Gets position type.
     *
     * @return the position type
     */
    public PositionType getPositionType() {
        return positionType;
    }

    // Usable functions

    /**
     * To vector 3 d vector 3 d.
     *
     * @return the vector 3 d
     */
    public Vector3D toVector3D() {
        if (positionType == PositionType.CARTESIAN_POSITION) {
            final double Station_x = this.getX();
            final double Station_y = this.getY();
            final double Station_z = this.getZ();
            return new Vector3D(Station_x, Station_y, Station_z);

        } else if (positionType == PositionType.CARTOGRAPHIC_DEGREES) {
            final double Station_longitude = this.getLongitudeDeg();
            final double Station_latitude = this.getLatitudeDeg();
            final double Station_height = this.getHeight();
            return new Vector3D(Station_longitude, Station_latitude,
                                Station_height);

        } else if (positionType == PositionType.CARTOGRAPHIC_RADIANS) {
            final double Station_longitudeDegree = this.getLongitudeRad();
            final double Station_latitudeDegree = this.getLatitudeRad();
            final double Station_height = this.getHeight();
            return new Vector3D(Station_longitudeDegree, Station_latitudeDegree,
                                Station_height);
        } else {
            throw new OresiumException(OresiumMessages.POSITION_TYPE_UNKNOWN);
        }
    }
}
