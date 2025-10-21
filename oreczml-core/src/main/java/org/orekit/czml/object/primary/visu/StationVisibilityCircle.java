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
package org.orekit.czml.object.primary.visu;

import cesiumlanguagewriter.BooleanCesiumWriter;
import cesiumlanguagewriter.Cartesian;
import cesiumlanguagewriter.CesiumOutputStream;
import cesiumlanguagewriter.CesiumStreamWriter;
import cesiumlanguagewriter.MaterialCesiumWriter;
import cesiumlanguagewriter.PacketCesiumWriter;
import cesiumlanguagewriter.PolygonCesiumWriter;
import cesiumlanguagewriter.SolidColorMaterialCesiumWriter;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.hipparchus.util.FastMath;
import org.orekit.annotation.DefaultDataContext;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.czml.object.primary.AbstractPrimaryObject;
import org.orekit.czml.object.primary.entities.Spacecraft;
import org.orekit.czml.object.secondary.Clock;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.frames.TopocentricFrame;
import org.orekit.utils.Constants;
import org.orekit.utils.IERSConventions;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Station visibility circle.
 * <p>
 * This clas aims at displaying the visibility circle of a station. It has no
 * constructor and is called with the .displayCircle() method of the
 * CzmlGroundStation object.
 * </p>
 */

public class StationVisibilityCircle
    extends
    AbstractPrimaryObject {

    /** The default id for the visibility station circle. */
    public static final String DEFAULT_ID = "STATION_CIRCLE/";

    /** The default name of the visibility station circle. */
    public static final String DEFAULT_NAME =
        "Circle of visibility of the station : ";

    /** The spacecraft observed. */
    private final Spacecraft spacecraft;

    /** The list of geodetic point representing the visibility circle. */
    private List<GeodeticPoint> circleGeodetic = new ArrayList<>();

    /** The list of cartesians points representing the visibility circle. */
    private List<Cartesian> circleCartesian = new ArrayList<>();

    /**
     * Default constructor of the station visibility circle.
     *
     * @param topocentricFrame : The topocentric frame representing a ground
     *        station.
     * @param satellite : The satellite observed.
     * @param angleOfAperture : The angle of aperture of the visibility of the
     *        station.
     * @param clock : The clock considered.
     */
    StationVisibilityCircle(final TopocentricFrame topocentricFrame,
                            final Spacecraft satellite,
                            final double angleOfAperture, final Clock clock) {
        this.setId(DEFAULT_ID +
                   topocentricFrame.getName() + "/" + satellite.getId());
        this.setName(DEFAULT_NAME + topocentricFrame.getName());
        this.setAvailability(clock.getAvailability());
        this.spacecraft = satellite;
        // Visibility cone need to be built, even if not used.
        final VisibilityCone cone =
            new VisibilityCone(topocentricFrame, satellite, angleOfAperture,
                               clock);
        this.circleGeodetic =
            computePointPositions(topocentricFrame, satellite, angleOfAperture);
        this.circleCartesian = buildCartesianListGround(circleGeodetic);
    }

    // Builder

    /**
     * The builder of the station visibility circle.
     *
     * @param topocentricFrameInput : The topocentric frame representing a
     *        ground station.
     * @param satelliteInput : The satellite observed.
     * @param clockInput : The clock considered.
     * @return The station visibility builder
     */
    public static StationVisibilityCircleBuilder
        builder(final TopocentricFrame topocentricFrameInput,
                final Spacecraft satelliteInput, final Clock clockInput) {
        return new StationVisibilityCircleBuilder(topocentricFrameInput,
                                                  satelliteInput, clockInput);
    }

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
            try (PolygonCesiumWriter polygonCesiumWriter =
                packet.getPolygonWriter()) {
                polygonCesiumWriter.open(output);
                polygonCesiumWriter.writePositionsProperty(circleCartesian);
                polygonCesiumWriter.writeShowProperty(true);
                polygonCesiumWriter.writePerPositionHeightProperty(false);
                polygonCesiumWriter.writeFillProperty(false);
                try (BooleanCesiumWriter outlineWriter =
                    polygonCesiumWriter.openOutlineProperty()) {
                    outlineWriter.writeBoolean(true);
                }
                try (MaterialCesiumWriter materialCesiumWriter =
                    polygonCesiumWriter.openMaterialProperty()) {
                    try (SolidColorMaterialCesiumWriter solidColorMaterialCesiumWriter =
                        materialCesiumWriter.openSolidColorProperty()) {
                        solidColorMaterialCesiumWriter
                            .writeColorProperty(spacecraft.getColor());
                    }
                }
            }
        }
    }

    // Private functions

    final List<GeodeticPoint>
        computePointPositions(final TopocentricFrame topocentricFrameInput,
                              final Spacecraft satelliteInput,
                              final double angleOfAperture) {
        final List<GeodeticPoint> toReturn = new ArrayList<>();
        final double fixedElevation = 90.0 - angleOfAperture;
        final double radiansElevation = FastMath.toRadians(fixedElevation);
        for (int i = 0; i < 360; i = i + 5) {
            final double radiansI = FastMath.toRadians(i);
            final GeodeticPoint currentGeodetic =
                topocentricFrameInput.computeLimitVisibilityPoint(satelliteInput
                    .getOrbits().get(0).getA(), radiansI, radiansElevation);
            toReturn.add(currentGeodetic);
        }
        return toReturn;
    }

    /**
     * This function aims at building the list of cartesian that build the
     * circle projected on the ground.
     *
     * @param geodetics : The list of Geodetic Points needed to build the circle
     *        on the ground
     * @return A list of cartesian projected to the ground
     */
    @DefaultDataContext
    final List<Cartesian>
        buildCartesianListGround(final List<GeodeticPoint> geodetics) {
        final List<Cartesian> toReturn = new ArrayList<>();
        final Frame ITRF =
            FramesFactory.getITRF(IERSConventions.IERS_2010, true);
        final OneAxisEllipsoid earth =
            new OneAxisEllipsoid(Constants.EGM96_EARTH_EQUATORIAL_RADIUS,
                                 Constants.IERS2010_EARTH_FLATTENING, ITRF);
        for (GeodeticPoint geodetic : geodetics) {
            final TopocentricFrame currentTopocentric =
                new TopocentricFrame(earth, geodetic, "currentTopocentric");
            final Vector3D currentVector =
                currentTopocentric.getCartesianPoint();
            toReturn
                .add(new Cartesian(currentVector.getX(), currentVector.getY(),
                                   currentVector.getZ()));
        }
        return toReturn;
    }
}
