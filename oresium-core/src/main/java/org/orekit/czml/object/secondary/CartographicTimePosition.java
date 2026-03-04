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

import cesiumlanguagewriter.Cartographic;
import cesiumlanguagewriter.CesiumInterpolationAlgorithm;
import cesiumlanguagewriter.CesiumOutputStream;
import cesiumlanguagewriter.JulianDate;
import cesiumlanguagewriter.PacketCesiumWriter;
import cesiumlanguagewriter.PositionCesiumWriter;
import org.orekit.czml.errors.OresiumException;
import org.orekit.czml.errors.OresiumMessages;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Earth-based time position class
 * <p>
 * The class aims at representing a position and a time of an object as a
 * lat/lon/alt value in the earth body frame.
 * </p>
 *
 * @author Brianna Aubin
 * @since 1.1
 */
public class CartographicTimePosition
    extends
    AbstractSecondaryObject<CartographicTimePosition> {

    /**
     * The julian dates when the position is defined.
     */
    private final List<JulianDate> dates;

    /**
     * The cartesian coordinates that define the position of the object.
     */
    private final List<Cartographic> positions;

    /**
     * The algorithm of interpolation used. The parameters available are:
     * LINEAR, LAGRANGE, HERMITE.
     */
    private final CesiumInterpolationAlgorithm cesiumInterpolationAlgorithm;

    /**
     * The degree of interpolation of the algorithm.
     */
    private final int interpolationDegree;

    /**
     * The frame of reference.
     */
    private final String referenceFrame;

    // Constructors

    /**
     * The basic time position constructor.
     *
     * @param cartographics : The list of cartesian positions
     * @param julianDates : A list of double representing the number of seconds
     *        that separate the initial instant from all the instants of the
     *        simulation.
     */
    public CartographicTimePosition(final List<Cartographic> cartographics,
                                    final List<JulianDate> julianDates) {
        this.dates = new ArrayList<>(julianDates);
        this.positions = new ArrayList<>();
        this.positions.addAll(cartographics);

        this.cesiumInterpolationAlgorithm =
            CesiumInterpolationAlgorithm.LAGRANGE;
        this.interpolationDegree = 5;
        this.referenceFrame = "EARTH_FRAME";
    }

    // Overrides

    @Override
    public void write(final PacketCesiumWriter packet,
                      final CesiumOutputStream output) {
        try (PositionCesiumWriter writer = packet.openPositionProperty()) {
            writer.writeReferenceFrame(referenceFrame);
            writer.writeInterpolationAlgorithm(cesiumInterpolationAlgorithm);
            writer.writeInterpolationDegree(interpolationDegree);
            writer.writeCartographicRadians(dates, positions);
        }
    }

    @Override
    public CartographicTimePosition cloneObject() {
        if (!positions.isEmpty() && !dates.isEmpty()) {
            return new CartographicTimePosition(this.positions, this.dates);
        } else {
            throw new OresiumException(OresiumMessages.NOT_VALID_SECONDARY_OBJECT_FOR_CLONE);
        }
    }

    // Getters

    /**
     * Gets positions.
     *
     * @return the positions
     */
    public List<Cartographic> getPositions() {
        return Collections.unmodifiableList(positions);
    }

    /**
     * Gets dates.
     *
     * @return the dates
     */
    public List<JulianDate> getDates() {
        return Collections.unmodifiableList(dates);
    }

    /**
     * Gets interpolation degree.
     *
     * @return the interpolation degree
     */
    public int getInterpolationDegree() {
        return interpolationDegree;
    }

    /**
     * Gets reference frame.
     *
     * @return the reference frame
     */
    public String getReferenceFrame() {
        return referenceFrame;
    }

    /**
     * Gets cesium interpolation algorithm.
     *
     * @return the cesium interpolation algorithm
     */
    public CesiumInterpolationAlgorithm getCesiumInterpolationAlgorithm() {
        return cesiumInterpolationAlgorithm;
    }
}
