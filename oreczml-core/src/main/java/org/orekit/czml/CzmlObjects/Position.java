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
package org.orekit.czml.CzmlObjects;

import cesiumlanguagewriter.Cartesian;
import cesiumlanguagewriter.Cartographic;
import cesiumlanguagewriter.CesiumInterpolationAlgorithm;
import cesiumlanguagewriter.CesiumOutputStream;
import cesiumlanguagewriter.Motion1;
import cesiumlanguagewriter.PacketCesiumWriter;
import cesiumlanguagewriter.PositionCesiumWriter;
import cesiumlanguagewriter.TimeInterval;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.hipparchus.util.FastMath;
import org.orekit.czml.ArchiObjects.Exceptions.OreCzmlExceptions;
import org.orekit.czml.CzmlEnum.PositionType;
import org.orekit.czml.CzmlObjects.CzmlSecondaryObjects.TimePosition;

/**
 * Position
 *
 * <p>
 * The position object is used to represent a position of any object except the satellite (see {@link TimePosition} for more information).
 * The position object can be build in many ways, following all the position type that exists : {@link PositionType}.
 * </p>
 *
 * @author Julien LEBLOND.
 * @since 1.0.0
 */

public class Position {

    /** The reference frame to use 'INERTIAL' or 'FIXED'. */
    private final String ReferenceFrame;

    /** The {@link org.orekit.czml.CzmlEnum.PositionType} that defines the type of position the Position object has. */
    private final PositionType positionType;


    //// Cartographic :

    /** The height of the object (used only when longitudes and latitudes are defined). */
    private double height = 0.0;

    // Cartographic radians
    /** The longitude in radians. */
    private double longitude = 0.0;

    /** The latitude in radians. */
    private double latitude = 0.0;

    // Cartographic degrees
    /** The longitude in  degrees. */
    private double longitudeDeg = 0.0;

    /** The latitude in degrees. */
    private double latitudeDeg = 0.0;


    //// Cartesian :

    // Cartesian3Value:
    /** The cartesian position in the x-axis. */
    private double x = 0.0;

    /** The cartesian position in the y-axis. */
    private double y = 0.0;

    /** The cartesian position in the z-axis. */
    private double z = 0.0;

    // Cartesian3VelocityValue
    /** The cartesian velocity in the x-axis. */
    private double vx = 0.0;

    /** The cartesian velocity in the y-axis. */
    private double vy = 0.0;

    /** The cartesian velocity in the z-axis. */
    private double vz = 0.0;


    // Constructor

    /**
     * The basic constructor for the position object.
     *
     * @param param1       : The first parameter of the tuple (can be x, vx, longitude or longitude deg)
     * @param param2       : The second parameter of the tuple (can be y, vy, latitude or latitude deg)
     * @param param3       : The third parameter of the tuple (can be z, vz or height)
     * @param positionType : The type of the tuple.
     */
    public Position(final double param1, final double param2, final double param3, final PositionType positionType) {
        if (positionType == PositionType.CARTESIAN_POSITION) {
            this.x = param1;
            this.y = param2;
            this.z = param3;
        } else if (positionType == PositionType.CARTESIAN_VELOCITY) {
            this.vx = param1;
            this.vy = param2;
            this.vz = param3;
        } else if (positionType == PositionType.CARTOGRAPHIC_RADIANS) {
            this.longitude = param1;
            this.latitude  = param2;
            this.height    = param3;
        } else if (positionType == PositionType.CARTOGRAPHIC_DEGREES) {
            this.longitudeDeg = param1;
            this.latitudeDeg  = param2;
            this.height       = param3;
        } else {
            throw new RuntimeException(OreCzmlExceptions.POSITION_TYPE_UNKNOWN);
        }

        this.positionType   = positionType;
        this.ReferenceFrame = "INERTIAL";
    }


    // Display functions

    /**
     * This method does not write a reference frame nor an interpolation degree/algorithm for the position.
     *
     * @param packetWriter : packet to write in the CZML
     * @param output       : Output that will contain the string
     * @param availability : when the position is displayed on Cesium
     */
    public void write(final PacketCesiumWriter packetWriter, final CesiumOutputStream output,
                      final TimeInterval availability) {
        try (PositionCesiumWriter positionWriter = packetWriter.getPositionWriter()) {
            positionWriter.open(output);
            positionWriter.writeInterval(availability);

            if (positionType == PositionType.CARTESIAN_POSITION) {
                final Cartesian cartesian = new Cartesian(this.x, this.y, this.z);
                positionWriter.writeCartesian(cartesian);
            } else if (positionType == PositionType.CARTESIAN_VELOCITY) {
                final Cartesian          velocityCartesian = new Cartesian(this.vx, this.vy, this.vz);
                final Motion1<Cartesian> motionCartesian   = new Motion1<>(velocityCartesian);
                positionWriter.writeCartesianVelocity(motionCartesian);
            } else if (positionType == PositionType.CARTOGRAPHIC_RADIANS) {
                final Cartographic cartographicRadians = new Cartographic(this.longitude, this.latitude, this.height);
                positionWriter.writeCartographicRadians(cartographicRadians);
            } else if (positionType == PositionType.CARTOGRAPHIC_DEGREES) {
                final Cartographic cartographicDegrees = new Cartographic(this.longitudeDeg, this.latitudeDeg,
                        this.height);
                positionWriter.writeCartographicDegrees(cartographicDegrees);
            }
        }
    }

    /**
     * This method allows the writing of a referenceFrame and of an interpolation algorithm and the degree of interpolation.
     *
     * @param packetWriter   : packet to write in the CZML
     * @param output         : Output that will contain the string
     * @param availability   : when the position is displayed on Cesium
     * @param referenceFrame : the frame where the position is referenced
     */
    public void write(final PacketCesiumWriter packetWriter, final CesiumOutputStream output,
                      final TimeInterval availability, final String referenceFrame) {
        try (PositionCesiumWriter positionWriter = packetWriter.getPositionWriter()) {
            positionWriter.open(output);
            positionWriter.writeInterval(availability);
            positionWriter.writeReferenceFrame(referenceFrame);
            positionWriter.writeInterpolationAlgorithm(CesiumInterpolationAlgorithm.LAGRANGE);
            positionWriter.writeInterpolationDegree(5);

            if (positionType == PositionType.CARTESIAN_POSITION) {
                final Cartesian cartesian = new Cartesian(this.x, this.y, this.z);
                positionWriter.writeCartesian(cartesian);
            } else if (positionType == PositionType.CARTESIAN_VELOCITY) {
                final Cartesian          velocityCartesian = new Cartesian(this.vx, this.vy, this.vz);
                final Motion1<Cartesian> motionCartesian   = new Motion1<>(velocityCartesian);
                positionWriter.writeCartesianVelocity(motionCartesian);
            } else if (positionType == PositionType.CARTOGRAPHIC_RADIANS) {
                final Cartographic cartographicRadians = new Cartographic(this.longitude, this.latitude, this.height);
                positionWriter.writeCartographicRadians(cartographicRadians);
            } else if (positionType == PositionType.CARTOGRAPHIC_DEGREES) {
                final Cartographic cartographicDegrees = new Cartographic(this.longitudeDeg, this.latitudeDeg,
                        this.height);
                positionWriter.writeCartographicDegrees(cartographicDegrees);
            }
        }
    }


    // Getters

    public double getLatitude() {
        if (latitude != 0.0) {
            return latitude;
        } else if (latitudeDeg != 0.0) {
            return latitudeDeg * (FastMath.PI / 180);
        } else {
            throw new RuntimeException(OreCzmlExceptions.DEFAULT_ERROR_LATITUDE);
        }
    }

    public double getLongitude() {
        if (longitude != 0.0) {
            return longitude;
        } else if (longitudeDeg != 0.0) {
            return longitudeDeg * (FastMath.PI / 180);
        } else {
            throw new RuntimeException(OreCzmlExceptions.DEFAULT_ERROR_LONGITUDE);
        }
    }

    public String getReferenceFrame() {
        if (ReferenceFrame != null) {
            return ReferenceFrame;
        } else {
            throw new RuntimeException(OreCzmlExceptions.EMPTY_FRAME);
        }
    }

    public double getHeight() {
        if (height != 0.0) {
            return height;
        } else {
            throw new RuntimeException(OreCzmlExceptions.EMPTY_POSITION_HEIGHT);
        }
    }

    public double getLatitudeDeg() {
        if (latitude != 0.0) {
            return latitude * (180 / FastMath.PI);
        } else if (latitudeDeg != 0.0) {
            return latitudeDeg;
        } else {
            throw new RuntimeException(OreCzmlExceptions.DEFAULT_ERROR_LATITUDE);
        }
    }

    public double getLongitudeDeg() {
        if (longitude != 0.0) {
            return longitude * (180 / FastMath.PI);
        } else if (longitudeDeg != 0.0) {
            return longitudeDeg;
        } else {
            throw new RuntimeException(OreCzmlExceptions.DEFAULT_ERROR_LONGITUDE);
        }
    }

    public double getX() {
        if (x != 0.0) {
            return x;
        } else {
            throw new RuntimeException(OreCzmlExceptions.EMPTY_X);
        }
    }

    public double getY() {
        if (y != 0.0) {
            return y;
        } else {
            throw new RuntimeException(OreCzmlExceptions.EMPTY_Y);
        }
    }

    public double getZ() {
        if (z != 0.0) {
            return z;
        } else {
            throw new RuntimeException(OreCzmlExceptions.EMPTY_Z);
        }
    }

    public double getVx() {
        if (vx != 0.0) {
            return vx;
        } else {
            throw new RuntimeException(OreCzmlExceptions.EMPTY_VX);
        }
    }

    public double getVy() {
        if (vy != 0.0) {
            return vy;
        } else {
            throw new RuntimeException(OreCzmlExceptions.EMPTY_VY);
        }
    }

    public double getVz() {
        if (vz != 0.0) {
            return vz;
        } else {
            throw new RuntimeException(OreCzmlExceptions.EMPTY_VZ);
        }
    }

    public PositionType getPositionType() {
        return positionType;
    }


    // Usable functions

    public Vector3D toVector3D() {
        if (positionType == PositionType.CARTESIAN_POSITION) {
            final double Station_x = this.getX();
            final double Station_y = this.getY();
            final double Station_z = this.getZ();
            return new Vector3D(Station_x, Station_y, Station_z);

        } else if (positionType == PositionType.CARTESIAN_VELOCITY) {
            final double Station_vx = this.getVx();
            final double Station_vy = this.getVy();
            final double Station_vz = this.getVz();
            return new Vector3D(Station_vx, Station_vy, Station_vz);

        } else if (positionType == PositionType.CARTOGRAPHIC_DEGREES) {
            final double Station_longitude = this.getLongitude();
            final double Station_latitude  = this.getLatitude();
            final double Station_height    = this.getHeight();
            return new Vector3D(Station_longitude, Station_latitude, Station_height);

        } else if (positionType == PositionType.CARTOGRAPHIC_RADIANS) {
            final double Station_longitudeDegree = this.getLongitudeDeg();
            final double Station_latitudeDegree  = this.getLatitudeDeg();
            final double Station_height          = this.getHeight();
            return new Vector3D(Station_longitudeDegree, Station_latitudeDegree, Station_height);
        } else {
            throw new RuntimeException(OreCzmlExceptions.POSITION_TYPE_UNKNOWN);
        }
    }
}
