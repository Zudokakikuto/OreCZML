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

import cesiumlanguagewriter.*;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.orekit.bodies.BodyShape;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.frames.TopocentricFrame;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class AbstractPointOnEarth extends AbstractPrimaryObject implements CzmlPrimaryObject {

    /** .*/
    public static final String DEFAULT_ID = "ABSTRACT_POINT_ON_EARTH/";
    /** .*/
    public static final String DEFAULT_NAME = "Abstract point on the earth at location : ";
    /** .*/
    public static final String DEFAULT_H_POSITION = "#position";

    /** .*/
    private List<GeodeticPoint> footprintsInTime = new ArrayList<>();
    /** .*/
    private List<Vector3D> positionsList = new ArrayList<>();
    /** .*/
    private boolean displayPath = false;
    /** .*/
    private List<JulianDate> julianDates = new ArrayList<>();
    /** .*/
    private boolean displayPeriodPointingPath = false;
    /** .*/
    private List<Cartesian> cartesians = new ArrayList<>();
    /** .*/
    private double periodForPath;

    public AbstractPointOnEarth(final List<JulianDate> julianDates, final List<GeodeticPoint> geodeticPoints, final BodyShape body) {
        this.footprintsInTime = geodeticPoints;
        this.setId(DEFAULT_ID + footprintsInTime.toString());
        this.setName(DEFAULT_NAME + footprintsInTime.toString());
        this.setAvailability(Header.MASTER_CLOCK.getAvailability());
        this.julianDates = julianDates;
        for (final GeodeticPoint currentGeodeticPoint : geodeticPoints) {
            if (currentGeodeticPoint == null) {
                cartesians.add(new Cartesian(0, 0, 0));
            }
            else {
                final TopocentricFrame topocentricFrame = new TopocentricFrame(body, currentGeodeticPoint, "");
                positionsList.add(topocentricFrame.getCartesianPoint());
                final Cartesian currentCartesian = new Cartesian(topocentricFrame.getCartesianPoint().getX(),
                        topocentricFrame.getCartesianPoint().getY(), topocentricFrame.getCartesianPoint().getZ());
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
                    pathWriter.writeInterval(Header.MASTER_CLOCK.getAvailability());
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
        this.cartesians = new ArrayList<>();
        this.footprintsInTime = new ArrayList<>();
        this.julianDates = new ArrayList<>();
        this.positionsList = new ArrayList<>();
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

    public void setDisplayPath(final boolean displayPath) {
        this.displayPath = displayPath;
    }

    public void setDisplayPeriodPointingPath (final boolean displayPeriodPointingPathInput, final double period) {
        this.displayPeriodPointingPath = displayPeriodPointingPathInput;
        this.periodForPath = period;
    }

    // Private functions
    private void writePosition(final PacketCesiumWriter packet) {

        try (PositionCesiumWriter positionWriter = packet.getPositionWriter()) {
            positionWriter.open(OUTPUT);
            positionWriter.writeInterval(this.getAvailability());
            positionWriter.writeCartesian(julianDates, cartesians);
        }
    }

    private List<List<Cartesian>> vector3DToCartesianList(final List<List<Vector3D>> vectors) {
        final List<List<Cartesian>> toReturn = new ArrayList<>();
        for (int i = 0; i < vectors.size(); i++) {
            final List<Cartesian> tempCartesians = new ArrayList<>();
            for (int j = 0; j < vectors.get(0).size(); j++) {
                final double X = vectors.get(i).get(j).getX();
                final double Y = vectors.get(i).get(j).getY();
                final double Z = vectors.get(i).get(j).getZ();
                final Cartesian currentCartesian = new Cartesian(X, Y, Z);
                tempCartesians.add(currentCartesian);
            }
            toReturn.add(tempCartesians);
        }
        return toReturn;
    }
}
