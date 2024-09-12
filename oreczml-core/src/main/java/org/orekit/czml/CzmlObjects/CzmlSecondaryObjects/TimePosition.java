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
package org.orekit.czml.CzmlObjects.CzmlSecondaryObjects;

import cesiumlanguagewriter.Cartesian;
import cesiumlanguagewriter.CesiumInterpolationAlgorithm;
import cesiumlanguagewriter.CesiumOutputStream;
import cesiumlanguagewriter.JulianDate;
import cesiumlanguagewriter.PacketCesiumWriter;

import java.util.ArrayList;
import java.util.List;

/**
 * Time position class
 *
 * <p> The class aims at representing a position and a time of an object.</p>
 *
 * @author Julien LEBLOND
 * @since 1.0.0
 */

public class TimePosition implements CzmlSecondaryObject {

    /** The julian dates when the position is defined. */
    private final List<JulianDate> dates;

    /** The cartesian coordinates that define the position of the object. */
    private final List<Cartesian> positions;

    /** The algorithm of interpolation used. The parameters available are: LINEAR, LAGRANGE, HERMITE. */
    private final CesiumInterpolationAlgorithm cesiumInterpolationAlgorithm;

    /** The degree of interpolation of the algorithm. */
    private final int interpolationDegree;

    /** The frame of reference. */
    private final String ReferenceFrame;


    // Constructors

    /**
     * The basic time position constructor.
     *
     * @param cartesians : The list of cartesian positions
     * @param timeList   : A list of double representing the number of seconds that separate the initial instant from all the instants of the simulation.
     */
    public TimePosition(final List<Cartesian> cartesians, final List<Double> timeList) {
        int cpt = 0;
        this.dates     = new ArrayList<>();
        this.positions = new ArrayList<>();

        for (Cartesian position : cartesians) {
            assert false;
            this.dates.add(new JulianDate(timeList.get(cpt)));
            this.positions.add(position);
            cpt++;
        }

        this.cesiumInterpolationAlgorithm = CesiumInterpolationAlgorithm.LAGRANGE;
        this.interpolationDegree          = 5;
        this.ReferenceFrame               = "INERTIAL";
    }


    // Overrides

    @Override
    public void write(final PacketCesiumWriter packetWriter, final CesiumOutputStream output) {
    }


    // Getters

    public List<Cartesian> getPositions() {
        return new ArrayList<>(positions);
    }

    public List<JulianDate> getDates() {
        return new ArrayList<>(dates);
    }

    public int getInterpolationDegree() {
        return interpolationDegree;
    }

    public String getReferenceFrame() {
        return ReferenceFrame;
    }

    public CesiumInterpolationAlgorithm getCesiumInterpolationAlgorithm() {
        return cesiumInterpolationAlgorithm;
    }
}

