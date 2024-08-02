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

import cesiumlanguagewriter.JulianDate;
import cesiumlanguagewriter.PacketCesiumWriter;
import cesiumlanguagewriter.TimeInterval;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.orekit.czml.CzmlObjects.CzmlAbstractObjects.CzmlModel;
import org.orekit.forces.maneuvers.Maneuver;
import org.orekit.propagation.BoundedPropagator;
import org.orekit.propagation.SpacecraftState;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class ManeuverSequence extends AbstractPrimaryObject implements CzmlPrimaryObject {

    /**
     * .
     */
    public static final String DEFAULT_ID = "MANEUVER/";
    /**
     * .
     */
    public static final String DEFAULT_NAME = "Maneuvers : ";

    /**
     * .
     */
    private List<Maneuver> maneuvers;
    /**
     * .
     */
    private BoundedPropagator propagator;
    /**
     * .
     */
    private List<SpacecraftState> states;
    /**
     * .
     */
    private double timeShift;
    /**
     * .
     */
    private CzmlModel model;
    /**
     * .
     */
    private boolean shouldAnimate = false;
    /**
     * .
     */
    private List<Vector3D> accelerations = new ArrayList<>();

    public ManeuverSequence(final List<Maneuver> maneuversInput, final Satellite satellite, final double timeShiftInput) {
        this.maneuvers = maneuversInput;
        this.propagator = satellite.getBoundedPropagator();
        this.timeShift = timeShiftInput;
        final List<SpacecraftState> allStates = satellite.getAllSpaceCraftStates();
        int length = maneuversInput.size();
        if (length > 10) {
            length = 10;
        }
        this.setId(DEFAULT_ID + maneuversInput.subList(0, length));
        this.setName(DEFAULT_NAME + length + " applied to :" + propagator.toString());
        final JulianDate startDate = absoluteDateToJulianDate(propagator.getMinDate());
        final JulianDate stopDate = absoluteDateToJulianDate(propagator.getMaxDate());
        this.setAvailability(new TimeInterval(startDate, stopDate));
        SpacecraftState currentState = allStates.get(0);
        while (currentState.getDate()
                           .isBefore(allStates.get(allStates.size() - 1)
                                              .getDate())) {
            states.add(currentState);
            currentState = currentState.shiftedBy(timeShiftInput);
        }

        int indiceManeuver = 0;
        for (int i = 0; i < states.size() - 1; i++) {
            final SpacecraftState state = states.get(i);
            final SpacecraftState nextState = states.get(i + 1);
            final Maneuver currentManeuver = maneuvers.get(indiceManeuver);
            final Vector3D currentAcceleration = currentManeuver.acceleration(state, currentManeuver.getParameters(state.getDate()));
            final Vector3D nextAcceleration = currentManeuver.acceleration(nextState, currentManeuver.getParameters(nextState.getDate()));
            if (currentAcceleration.getNorm() > 0) {
                if (nextAcceleration.getNorm() < 0) {
                    indiceManeuver = indiceManeuver + 1;
                }
                accelerations.add(currentAcceleration);
            }
        }
    }

    @Override
    public void writeCzmlBlock() throws URISyntaxException, IOException {
        OUTPUT.setPrettyFormatting(true);
        for (int i = 0; i < maneuvers.size(); i++) {
            final Maneuver currentManeuver = maneuvers.get(i);
            try (PacketCesiumWriter packet = STREAM.openPacket(OUTPUT)) {
                packet.writeId(getId());
                packet.writeName(getName());
                packet.writeAvailability(getAvailability());

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
}
