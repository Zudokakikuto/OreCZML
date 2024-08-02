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
import org.orekit.attitudes.Attitude;
import org.orekit.attitudes.AttitudeProvider;
import org.orekit.czml.CzmlObjects.CzmlAbstractObjects.CzmlModel;
import org.orekit.czml.CzmlObjects.CzmlSecondaryObjects.Orientation;
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
    /** .*/
    public static final String DEFAULT_PATH_MODEL = Header.DEFAULT_RESOURCES + "/maneuver_model.glb";
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
    /** .*/
    private List<List<Attitude>> attitudesManeuvers = new ArrayList<>();
    /** .*/
    private List<TimeInterval> availabilitiesManeuvers = new ArrayList<>();
    /**
     * .
     */
    private double timeShift;
    /**
     * .
     */
    private CzmlModel model;
    /** .*/
    private List<Orientation> orientations = new ArrayList<>();
    /**
     * .
     */
    private boolean shouldAnimate = false;

    public ManeuverSequence(final List<Maneuver> maneuversInput, final Satellite satellite, final double timeShift) throws URISyntaxException, IOException {
        this(maneuversInput, satellite, timeShift, DEFAULT_PATH_MODEL);
    }

    public ManeuverSequence(final List<Maneuver> maneuversInput, final Satellite satellite, final double timeShiftInput, final String pathModel) throws URISyntaxException, IOException {
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
        this.states = generateAllDiscreteStates(allStates, timeShiftInput);
        this.attitudesManeuvers = generateAllAttitudesManeuvers(states, maneuvers);
        this.model = new CzmlModel(pathModel);
        this.orientations = generateOrientationManeuvers(attitudesManeuvers);
        this.availabilitiesManeuvers = generateAvailabilitiesManeuvers(attitudesManeuvers);
    }

    @Override
    public void writeCzmlBlock() throws URISyntaxException, IOException {
        OUTPUT.setPrettyFormatting(true);
        for (int i = 0; i < maneuvers.size(); i++) {
            final Orientation currentOrientation = orientations.get(i);
            final TimeInterval currentAvailability = availabilitiesManeuvers.get(i);
            try (PacketCesiumWriter packet = STREAM.openPacket(OUTPUT)) {
                packet.writeId(getId());
                packet.writeName(getName());
                packet.writeAvailability(currentAvailability);

                currentOrientation.write(packet, OUTPUT);

                model.generateCZML(packet, OUTPUT);
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


    private List<SpacecraftState> generateAllDiscreteStates(final List<SpacecraftState> allStates, final double timeShiftInput) {
        final List<SpacecraftState> statesTemp = new ArrayList<>();
        SpacecraftState currentState = allStates.get(0);
        while (currentState.getDate()
                           .isBefore(allStates.get(allStates.size() - 1)
                                              .getDate())) {
            statesTemp.add(currentState);
            currentState = currentState.shiftedBy(timeShiftInput);
        }
        return statesTemp;
    }

    private List<List<Attitude>> generateAllAttitudesManeuvers(final List<SpacecraftState> statesInput, final List<Maneuver> maneuversInput) {
        final List<List<Attitude>> toReturn = new ArrayList<>();
        final List<Attitude> tempToReturn = new ArrayList<>();
        for (int i = 0; i < statesInput.size() - 1; i++) {

            final SpacecraftState state = statesInput.get(i);
            final SpacecraftState nextState = statesInput.get(i + 1);

            for (int j = 0; j < maneuversInput.size(); j++) {

                final Maneuver currentManeuver = maneuversInput.get(j);
                final AttitudeProvider attitudeOverride = currentManeuver.getAttitudeOverride();

                final Vector3D currentAcceleration = currentManeuver.acceleration(state, currentManeuver.getParameters(state.getDate()));
                final Vector3D nextAcceleration = currentManeuver.acceleration(nextState, currentManeuver.getParameters(nextState.getDate()));

                if (currentAcceleration.getNorm() > 0) {
                    tempToReturn.add(attitudeOverride.getAttitude(state.getOrbit(), state.getDate(), state.getFrame()));
                }

                if (currentAcceleration.getNorm() > 0 && nextAcceleration.getNorm() < 0) {
                    break;
                }
            }
            toReturn.add(tempToReturn);
        }
        return toReturn;
    }

    private List<Orientation> generateOrientationManeuvers(final List<List<Attitude>> allAttitudeByManeuver) {
        final List<Orientation> toReturn = new ArrayList<>();
        for (final List<Attitude> attitudesGivenManeuver : allAttitudeByManeuver) {
            toReturn.add(new Orientation(attitudesGivenManeuver, propagator.getFrame()));
        }
        return toReturn;
    }

    private List<TimeInterval> generateAvailabilitiesManeuvers(final List<List<Attitude>> allAttitudeByManeuver) {
        final List<TimeInterval> toReturn = new ArrayList<>();
        for (final List<Attitude> currentListOfAttitude : allAttitudeByManeuver) {
            final JulianDate startDate = absoluteDateToJulianDate(currentListOfAttitude.get(0)
                                                                                       .getDate());
            final JulianDate stopDate = absoluteDateToJulianDate(currentListOfAttitude.get(currentListOfAttitude.size() - 1)
                                                                                      .getDate());
            toReturn.add(new TimeInterval(startDate, stopDate));
        }
        return toReturn;
    }
}
