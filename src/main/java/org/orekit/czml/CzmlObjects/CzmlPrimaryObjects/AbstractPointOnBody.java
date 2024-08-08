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

import cesiumlanguagewriter.Cartesian;
import cesiumlanguagewriter.JulianDate;
import cesiumlanguagewriter.PacketCesiumWriter;
import cesiumlanguagewriter.PathCesiumWriter;
import cesiumlanguagewriter.PositionCesiumWriter;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.orekit.bodies.BodyShape;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.frames.TopocentricFrame;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Abstract point on body class.
 *
 * <p> This class build an abstract point on the body that evolves in time, given a list of geodetic point and a list of julian dates.
 * This point can be used to build objects like ground tracks, or the pointing of an attitude, or any moving object on a body that need a
 * like with an orbiting object. When the abstract point on body is build, the reference of the position can be used for example to build new objects.</p>
 *
 * @author Julien LEBLOND
 * @since 1.0.0
 */

public class AbstractPointOnBody extends AbstractPrimaryObject implements CzmlPrimaryObject {

    /** The default ID for the abstract point of body. */
    public static final String DEFAULT_ID = "ABSTRACT_POINT_ON_BODY/";

    /** The default name of the abstract point on body. */
    public static final String DEFAULT_NAME = "Abstract point on the body at location(s) : ";

    /** Allows the reference the position of an object. */
    public static final String DEFAULT_H_POSITION = "#position";

    /** The list of geodetic points in the body to be built. */
    private List<GeodeticPoint> footprintsInTime;

    /** The list of Vector3D that defines the position of the points on the body. */
    private List<Vector3D> positionsList = new ArrayList<>();

    /** To display or not the path of the point. */
    private boolean displayPath = false;

    /** The list of julian dates when the point need to be displayed. */
    private List<JulianDate> julianDates;

    /** To display or not the path period by period with a given interval in time (period for path). */
    private boolean displayPeriodPointingPath = false;

    /** The list of cartesians that represents the positions of the point in time. */
    private List<Cartesian> cartesians = new ArrayList<>();

    /** The number of seconds when the path should be displayed, period by period. */
    private double periodForPath;

    public AbstractPointOnBody(final List<JulianDate> julianDates, final List<GeodeticPoint> geodeticPoints,
                               final BodyShape body) {
        this.footprintsInTime = geodeticPoints;
        // Taking only the 10 first geodetic points for the id and the name to not surcharge the czml file
        this.setId(DEFAULT_ID + Arrays.toString(Arrays.copyOfRange(footprintsInTime.toArray(), 0, 10)));
        this.setName(DEFAULT_NAME + Arrays.toString(Arrays.copyOfRange(footprintsInTime.toArray(), 0, 10)));
        this.setAvailability(Header.getMasterClock()
                                   .getAvailability());
        this.julianDates = julianDates;
        for (final GeodeticPoint currentGeodeticPoint : geodeticPoints) {
            if (currentGeodeticPoint == null) {
                cartesians.add(new Cartesian(0, 0, 0));
            } else {
                final TopocentricFrame topocentricFrame = new TopocentricFrame(body, currentGeodeticPoint, "");
                positionsList.add(topocentricFrame.getCartesianPoint());
                final Cartesian currentCartesian = new Cartesian(topocentricFrame.getCartesianPoint()
                                                                                 .getX(),
                        topocentricFrame.getCartesianPoint()
                                        .getY(), topocentricFrame.getCartesianPoint()
                                                                 .getZ());
                cartesians.add(currentCartesian);
            }
        }
    }

    @Override
    public void writeCzmlBlock() throws URISyntaxException, IOException {
        OUTPUT.setPrettyFormatting(true);
        try (PacketCesiumWriter packet = STREAM.openPacket(OUTPUT)) {
            packet.writeId(getId());
            packet.writeName(getName());
            packet.writeAvailability(getAvailability());

            writePosition(packet);
            if (displayPath) {
                try (PathCesiumWriter pathWriter = packet.getPathWriter()) {
                    pathWriter.open(OUTPUT);
                    pathWriter.writeShowProperty(true);
                    pathWriter.writeInterval(Header.getMasterClock()
                                                   .getAvailability());
                    if (displayPeriodPointingPath) {
                        pathWriter.writeTrailTimeProperty(0.0);
                        pathWriter.writeLeadTimeProperty(this.periodForPath);
                    }
                }
            }
        }
    }

    @Override
    public StringWriter getStringWriter() {
        return STRING_WRITER;
    }

    @Override
    public void cleanObject() {
        footprintsInTime = null;
        this.setId("");
        this.setName("");
        this.cartesians       = new ArrayList<>();
        this.footprintsInTime = new ArrayList<>();
        this.julianDates      = new ArrayList<>();
        this.positionsList    = new ArrayList<>();
    }

    // Gets

    public List<GeodeticPoint> getFootprintsInTime() {
        return footprintsInTime;
    }

    public List<Vector3D> getPositionsList() {
        return positionsList;
    }

    public List<JulianDate> getJulianDates() {
        return julianDates;
    }

    public List<Cartesian> getCartesians() {
        return cartesians;
    }

    public boolean isDisplayPath() {
        return displayPath;
    }

    public void setDisplayPath(final boolean displayPath) {
        this.displayPath = displayPath;
    }

    public boolean isDisplayPeriodPointingPath() {
        return displayPeriodPointingPath;
    }

    public double getPeriodForPath() {
        return periodForPath;
    }

    public void setPeriodForPath(final double periodForPath) {
        this.periodForPath = periodForPath;
    }

    public void setDisplayPeriodPointingPath(final boolean displayPeriodPointingPathInput, final double period) {
        this.displayPeriodPointingPath = displayPeriodPointingPathInput;
        this.periodForPath             = period;
    }

    // Private functions
    private void writePosition(final PacketCesiumWriter packet) {

        try (PositionCesiumWriter positionWriter = packet.getPositionWriter()) {
            positionWriter.open(OUTPUT);
            positionWriter.writeInterval(this.getAvailability());
            positionWriter.writeCartesian(julianDates, cartesians);
        }
    }

}
