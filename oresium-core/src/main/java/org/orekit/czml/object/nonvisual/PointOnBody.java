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
package org.orekit.czml.object.nonvisual;

import cesiumlanguagewriter.Cartesian;
import cesiumlanguagewriter.CesiumOutputStream;
import cesiumlanguagewriter.CesiumStreamWriter;
import cesiumlanguagewriter.JulianDate;
import cesiumlanguagewriter.PacketCesiumWriter;
import cesiumlanguagewriter.PathCesiumWriter;
import cesiumlanguagewriter.PositionCesiumWriter;
import cesiumlanguagewriter.TimeInterval;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.orekit.bodies.BodyShape;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.czml.object.primary.AbstractPrimaryObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Point on body class.
 * <p>
 * This class build a point on the body that evolves in time, given a list of
 * geodetic point and a list of julian dates. This point can be used to build
 * objects like ground tracks, or the pointing of an attitude, or any moving
 * object on a body that need a like with an orbiting object. When the point on
 * body is build, the reference of the position can be used for example to build
 * new objects.
 * </p>
 *
 * @author Julien LEBLOND
 * @since 1.0.0
 */
public class PointOnBody
    extends
    AbstractPrimaryObject {

    /**
     * The default ID for the point of body.
     */
    public static final String DEFAULT_ID = "POINT_ON_BODY/";

    /**
     * The default name of the point on the body.
     */
    public static final String DEFAULT_NAME =
        "Point on the body at location(s) : ";

    // Intrinsic arguments

    /**
     * The list of julian dates when the point needs to be displayed.
     */
    private final List<JulianDate> julianDates;

    /**
     * The list of geodetic points in the body to be built.
     */
    private final List<GeodeticPoint> footprintsInTime;

    // Other arguments

    /**
     * The list of Vector3D that defines the position of the points on the body.
     */
    private final List<Vector3D> positionsList = new ArrayList<>();

    /**
     * The list of cartesians that represents the positions of the point in
     * time.
     */
    private final List<Cartesian> cartesians = new ArrayList<>();

    /**
     * To display or not the path of the point.
     */
    private boolean displayPath = false;

    /**
     * To display or not the path period by period with a given interval in time
     * (period for a path).
     */
    private boolean displayPeriodPointingPath = false;

    /**
     * The number of seconds when the path should be displayed, period by
     * period.
     */
    private double periodForPath;

    /** The availability of the point on body. */

    // Constructor

    /**
     * The constructor of the point on the body.
     *
     * @param julianDates : The dates when the point must be displayed.
     * @param geodeticPoints : Must be of the same size of julianDates. This
     *        list represents the points at the surface of the body in time that
     *        will describe the trajectory of the point.
     * @param body : The body to which the geodetic points are projected to.
     */
    public PointOnBody(final List<JulianDate> julianDates,
                       final List<GeodeticPoint> geodeticPoints,
                       final BodyShape body) {
        this.footprintsInTime = new ArrayList<>(geodeticPoints);
        final String stringFootprints =
            Arrays.toString(Arrays.copyOfRange(footprintsInTime.toArray(), 0,
                                               10));

        // Taking only the 10 first geodetic points for the id and the name to
        // not surcharge the czml file
        this.setId(DEFAULT_ID + stringFootprints);
        this.setName(DEFAULT_NAME + stringFootprints);

        // Use the julianDates array to determine the time interval
        final int sz = julianDates.size();
        this.setAvailability(new TimeInterval(julianDates.get(0),
                                              julianDates.get(sz - 1)));

        this.julianDates = new ArrayList<>(julianDates);
        for (final GeodeticPoint currentGeodeticPoint : geodeticPoints) {
            if (currentGeodeticPoint == null) {
                cartesians.add(new Cartesian(0, 0, 0));
            } else {
                final Vector3D projectedPoint =
                    body.transform(currentGeodeticPoint);
                positionsList.add(body.transform(currentGeodeticPoint));
                final Cartesian currentCartesian =
                    new Cartesian(projectedPoint.getX(), projectedPoint.getY(),
                                  projectedPoint.getZ());
                cartesians.add(currentCartesian);
            }
        }
    }

    // Overrides

    @Override
    public void writeCzmlBlock(final CesiumStreamWriter stream,
                               final CesiumOutputStream output)
        throws URISyntaxException,
            IOException {
        output.setPrettyFormatting(true);
        try (PacketCesiumWriter packet = stream.openPacket(output)) {
            packet.writeId(getId());
            packet.writeName(getName());
            packet.writeAvailability(getAvailability());

            writePosition(packet, output);
            if (displayPath) {
                writePath(packet, output, getAvailability());
            }
        }
    }

    @Override
    public PointOnBody cloneObject() {
        return null;
    }

    // Gets

    /**
     * Gets julian dates.
     *
     * @return the julian dates
     */
    public List<JulianDate> getJulianDates() {
        return Collections.unmodifiableList(julianDates);
    }

    /**
     * Gets cartesians.
     *
     * @return the cartesians
     */
    public List<Cartesian> getCartesians() {
        return Collections.unmodifiableList(cartesians);
    }

    /**
     * Is display path boolean.
     *
     * @return the boolean
     */
    public boolean isDisplayPath() {
        return displayPath;
    }

    /**
     * Sets display path.
     *
     * @param displayPath the display path
     */
    public void setDisplayPath(final boolean displayPath) {
        this.displayPath = displayPath;
    }

    /**
     * Is display period pointing path boolean.
     *
     * @return the boolean
     */
    public boolean isDisplayPeriodPointingPath() {
        return displayPeriodPointingPath;
    }

    /**
     * Sets period for path.
     *
     * @param periodForPath the period for path
     */
    public void setPeriodForPath(final double periodForPath) {
        this.periodForPath = periodForPath;
    }

    /**
     * Sets display period pointing path.
     *
     * @param displayPeriodPointingPathInput the display period pointing path
     *        input
     * @param period the period
     */
    public void
        setDisplayPeriodPointingPath(final boolean displayPeriodPointingPathInput,
                                     final double period) {
        this.displayPeriodPointingPath = displayPeriodPointingPathInput;
        this.periodForPath = period;
    }

    // Private functions

    /**
     * This function aims at writing the position of the point given a packet to
     * write into.
     *
     * @param packet : The packet where the information of the czml file will be
     *        written.
     * @param output : The output stream of cesium that will contain the strings
     *        to write into the CzmLFile.
     */
    private void writePosition(final PacketCesiumWriter packet,
                               final CesiumOutputStream output) {

        try (PositionCesiumWriter positionWriter = packet.getPositionWriter()) {
            positionWriter.open(output);
            positionWriter.writeInterval(this.getAvailability());
            positionWriter.writeCartesian(julianDates, cartesians);
        }
    }

    /**
     * This function aims at writing the path of the point given a packet to
     * write into.
     *
     * @param packet : The packet where the information of the czml file will be
     *        written.
     * @param output : The output stream of cesium that will contain the strings
     *        to write into the CzmLFile.
     * @param availability : The availaibility of the point on body
     */
    private void writePath(final PacketCesiumWriter packet,
                           final CesiumOutputStream output,
                           final TimeInterval availability) {
        try (PathCesiumWriter pathWriter = packet.getPathWriter()) {
            pathWriter.open(output);
            pathWriter.writeShowProperty(true);
            pathWriter.writeInterval(availability);
            if (displayPeriodPointingPath) {
                pathWriter.writeTrailTimeProperty(0.0);
                pathWriter.writeLeadTimeProperty(this.periodForPath);
            }
        }
    }
}
