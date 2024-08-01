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
import cesiumlanguagewriter.PacketCesiumWriter;
import cesiumlanguagewriter.PositionCesiumWriter;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.czml.CzmlObjects.Polyline;
import org.orekit.frames.FramesFactory;
import org.orekit.utils.Constants;
import org.orekit.utils.IERSConventions;

import java.awt.Color;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;


/**
 * Terrestrial reference system
 *
 * <p>
 * The terrestrial reference systems aims at representing the cartesian system of a body to be displayed as an help for the user
 * during the simulation.
 * </p>
 *
 * @author Julien LEBLOND.
 * @since 1.0
 */

public class CentralBodyReferenceSystem extends AbstractPrimaryObject implements CzmlPrimaryObject {
    /**
     * .
     */
    public static final String DEFAULT_ID = "CENTRAL_BODY_REFERENCE_SYSTEM";
    /**
     * .
     */
    public static final String DEFAULT_NAME = "Reference system of the central body";
    /**
     * .
     */
    public static final Color DEFAULT_RED = new Color(255, 10, 10);
    /**
     * .
     */
    public static final Color DEFAULT_GREEN = new Color(10, 255, 10);
    /**
     * .
     */
    public static final Color DEFAULT_BLUE = new Color(10, 10, 255);

    /**
     * .
     */
    private List<Polyline> polylines = new ArrayList<>();


    public CentralBodyReferenceSystem(final OneAxisEllipsoid body, final String id, final String name, final Color color1,
                                      final Color color2, final Color color3) {
        this.setId(id);
        this.setName(name);
        this.setAvailability(Header.MASTER_CLOCK.getAvailability());

        final Cartesian centralCartesian = new Cartesian(0.1, 0.1, 0.1);
        final double depth = body.getEquatorialRadius() * 3;

        final Cartesian plusXCartesian = new Cartesian(depth, 0, 0);
        final Cartesian plusYCartesian = new Cartesian(0, depth, 0);
        final Cartesian plusZCartesian = new Cartesian(0, 0, depth);

        final List<Cartesian> vectorToX = new ArrayList<>();
        final List<Cartesian> vectorToY = new ArrayList<>();
        final List<Cartesian> vectorToZ = new ArrayList<>();
        vectorToX.add(centralCartesian);
        vectorToX.add(plusXCartesian);

        vectorToY.add(centralCartesian);
        vectorToY.add(plusYCartesian);

        vectorToZ.add(centralCartesian);
        vectorToZ.add(plusZCartesian);

        final Polyline XPolyline = new Polyline(vectorToX, getAvailability(), color1, 1, 1e9);
        final Polyline YPolyline = new Polyline(vectorToY, getAvailability(), color2, 1, 1e9);
        final Polyline ZPolyline = new Polyline(vectorToZ, getAvailability(), color3, 1, 1e9);
        this.polylines.add(XPolyline);
        this.polylines.add(YPolyline);
        this.polylines.add(ZPolyline);
    }

    public CentralBodyReferenceSystem() {
        this(new OneAxisEllipsoid(Constants.WGS84_EARTH_EQUATORIAL_RADIUS,
                                  Constants.WGS84_EARTH_FLATTENING,
                                  FramesFactory.getITRF(IERSConventions.IERS_2010, true)),
             DEFAULT_ID,
             DEFAULT_NAME,
             DEFAULT_RED,
             DEFAULT_GREEN,
             DEFAULT_GREEN);
    }

    public CentralBodyReferenceSystem(final Color color1, final Color color2, final Color color3) {
        this(new OneAxisEllipsoid(Constants.WGS84_EARTH_EQUATORIAL_RADIUS,
                                  Constants.WGS84_EARTH_FLATTENING,
                                  FramesFactory.getITRF(IERSConventions.IERS_2010, true)),
             DEFAULT_ID,
             DEFAULT_NAME,
             color1,
             color2,
             color3);
    }


    public void writeCzmlBlock() {
        OUTPUT.setPrettyFormatting(true);
        for (int i = 0; i < 3; i++) {
            try (PacketCesiumWriter packet = STREAM.openPacket(OUTPUT)) {
                packet.writeId(getId() + " " + i);
                packet.writeName(getName());
                packet.writeAvailability(getAvailability());

                try (PositionCesiumWriter positionWriter = packet.getPositionWriter()) {
                    positionWriter.open(OUTPUT);
                    positionWriter.writeCartesian(new Cartesian(0, 0, 0));
                }

                polylines.get(i)
                         .writePolylineVectorFixed(packet, OUTPUT);
            }
        }
        cleanObject();
    }

    @Override
    public StringWriter getStringWriter() {
        return null;
    }

    @Override
    public void cleanObject() {
        setId("");
        setName("");
        polylines = new ArrayList<>();
    }
}
