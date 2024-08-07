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
import cesiumlanguagewriter.Reference;
import cesiumlanguagewriter.TimeInterval;
import org.hipparchus.geometry.euclidean.threed.Rotation;
import org.hipparchus.geometry.euclidean.threed.RotationConvention;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.orekit.attitudes.Attitude;
import org.orekit.attitudes.AttitudesSequence;
import org.orekit.czml.ArchiObjects.ManeuverSequenceBuilder;
import org.orekit.czml.CzmlObjects.CzmlAbstractObjects.CzmlModel;
import org.orekit.czml.CzmlObjects.CzmlSecondaryObjects.Orientation;
import org.orekit.forces.maneuvers.Maneuver;
import org.orekit.forces.maneuvers.trigger.AbstractManeuverTriggers;
import org.orekit.forces.maneuvers.trigger.ManeuverTriggers;
import org.orekit.frames.LOF;
import org.orekit.propagation.BoundedPropagator;
import org.orekit.propagation.SpacecraftState;
import org.orekit.time.AbsoluteDate;
import org.orekit.utils.TimeSpanMap;

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
    public static final String DEFAULT_PATH_MODEL = Header.DEFAULT_RESOURCES + "/maneuver_model.glb";
    /**
     * .
     */
    public static final String DEFAULT_H_POSITION = "#position";
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
    private List<SpacecraftState> states = new ArrayList<>();
    /**
     * .
     */
    private List<List<Attitude>> attitudesManeuvers = new ArrayList<>();
    /**
     * .
     */
    private List<TimeInterval> availabilitiesManeuvers = new ArrayList<>();
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
    private Reference satellitePositionReference;
    /**
     * .
     */
    private Vector3D accelerationDirection;
    /**
     * .
     */
    private List<Orientation> orientations;
    /**
     * .
     */
    private List<List<Attitude>> attitudesWithManeuver = new ArrayList<>();
    /**
     * .
     */
    private AttitudesSequence sequence;
    /**
     * .
     */
    private LOF lof;
    /** .*/
    private boolean showTrust;
    /**
     * .
     */
    private boolean shouldAnimate = false;

    public ManeuverSequence(final AttitudesSequence sequence, final List<Maneuver> maneuversInput, final Satellite satellite, final Vector3D accelerationDirection, final LOF lofInput) throws URISyntaxException, IOException {
        this(sequence, maneuversInput, satellite, accelerationDirection, lofInput, false, Header.MASTER_CLOCK.getMultiplier(), DEFAULT_PATH_MODEL);
    }

    public ManeuverSequence(final AttitudesSequence sequenceInput, final List<Maneuver> maneuversInput, final Satellite satellite, final Vector3D accelerationDirection, final LOF lofInput, final boolean showTrustInput, final double timeShiftInput, final String pathModel) throws URISyntaxException, IOException {
        this.maneuvers = maneuversInput;
        this.propagator = satellite.getBoundedPropagator();
        this.timeShift = timeShiftInput;
        this.states = satellite.getAllSpaceCraftStates();
        this.accelerationDirection = accelerationDirection;
        this.lof = lofInput;
        this.sequence = sequenceInput;
        this.showTrust = showTrustInput;
        int length = maneuversInput.size();
        if (length > 10) {
            length = 10;
        }
        this.setId(DEFAULT_ID + maneuversInput.subList(0, length));
        this.setName(DEFAULT_NAME + length + ", applied to :" + propagator.toString());
        final JulianDate startDate = absoluteDateToJulianDate(propagator.getMinDate());
        final JulianDate stopDate = absoluteDateToJulianDate(propagator.getMaxDate());
        this.setAvailability(new TimeInterval(startDate, stopDate));
        this.satellitePositionReference = new Reference(satellite.getId() + DEFAULT_H_POSITION);

        this.attitudesWithManeuver = generateAllAttitudesManeuvers(states, maneuversInput);
        this.model = new CzmlModel(pathModel, 500000, 40, 5E-05);
        this.availabilitiesManeuvers = generateAvailabilitiesManeuvers(maneuvers);
        this.orientations = generateOrientationManeuvers(attitudesWithManeuver);
    }

    // Builder

    public static ManeuverSequenceBuilder builder(final AttitudesSequence sequenceInput, final List<Maneuver> maneuversInput, final Satellite satellite, final Vector3D accelerationDirection, final LOF lofInput) {
        return new ManeuverSequenceBuilder(sequenceInput, maneuversInput, satellite, accelerationDirection, lofInput);
    }

    @Override
    public void writeCzmlBlock() throws URISyntaxException, IOException {
        OUTPUT.setPrettyFormatting(true);
        for (int i = 0; i < maneuvers.size(); i++) {
            final Maneuver currentManeuver = maneuvers.get(i);
            final Orientation currentOrientation = orientations.get(i);
            final TimeInterval currentAvailability = availabilitiesManeuvers.get(i);
            try (PacketCesiumWriter packet = STREAM.openPacket(OUTPUT)) {
                packet.writeId(DEFAULT_ID + currentManeuver);
                packet.writeName(DEFAULT_NAME + currentManeuver);
                packet.writeAvailability(currentAvailability);
                packet.writePositionPropertyReference(satellitePositionReference);

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


    private List<List<Attitude>> generateAllAttitudesManeuvers(final List<SpacecraftState> statesInput, final List<Maneuver> maneuversInput) {
        final List<List<Attitude>> toReturn = new ArrayList<>();

        // Iteration for each maneuver
        for (Maneuver maneuver : maneuversInput) {
            toReturn.add(generateAttitudeForOneManeuver(statesInput, maneuver));
        }
        return toReturn;
    }

    private List<TimeInterval> generateAvailabilitiesManeuvers(final List<Maneuver> maneuverList) {
        final List<TimeInterval> toReturn = new ArrayList<>();
        for (final Maneuver currentManeuver : maneuverList) {
            final AbstractManeuverTriggers currentTrigger = (AbstractManeuverTriggers) currentManeuver.getManeuverTriggers();
            final TimeSpanMap<Boolean> map = currentTrigger.getFirings();
            for (TimeSpanMap.Span<Boolean> span = map.getFirstNonNullSpan(); span != null; span = span.next()) {
                if (span.getData()) {
                    if (span.getEnd()
                            .isAfter(julianDateToAbsoluteDate(Header.MASTER_CLOCK.getAvailability()
                                                                                 .getStop(), Header.TIME_SCALE))) {
                        toReturn.add(new TimeInterval(absoluteDateToJulianDate(span.getStart()), Header.MASTER_CLOCK.getAvailability()
                                                                                                                    .getStop()));
                    } else if (span.getStart()
                                   .isBefore(julianDateToAbsoluteDate(Header.MASTER_CLOCK.getAvailability()
                                                                                         .getStart(), Header.TIME_SCALE))) {
                        toReturn.add(new TimeInterval(Header.MASTER_CLOCK.getAvailability()
                                                                         .getStart(), absoluteDateToJulianDate(span.getEnd())));
                    } else {
                        toReturn.add(new TimeInterval(absoluteDateToJulianDate(span.getStart()), absoluteDateToJulianDate(span.getEnd())));
                    }
                }
            }
        }
        return toReturn;
    }

    private List<Orientation> generateOrientationManeuvers(final List<List<Attitude>> allAttitudeByManeuver) {
        final List<Orientation> toReturn = new ArrayList<>();
        for (final List<Attitude> attitudesGivenManeuver : allAttitudeByManeuver) {
            if (!attitudesGivenManeuver.isEmpty()) {
                toReturn.add(new Orientation(attitudesGivenManeuver, propagator.getFrame(), false));
            }
        }
        return toReturn;
    }

    private List<Attitude> generateAttitudeForOneManeuver(final List<SpacecraftState> statesInput, final Maneuver currentManeuver) {
        final ManeuverTriggers currentTrigger = currentManeuver.getManeuverTriggers();
        boolean firstFiringDateFound = false;
        AbsoluteDate dateFinalTime = null;
        SpacecraftState previousState = null;
        AbsoluteDate firstFiringDate;

        final List<Attitude> toReturn = new ArrayList<>();

        for (int j = 0; j < statesInput.size(); j++) {

            final SpacecraftState state = statesInput.get(j);
            if (j != 0) {
                previousState = statesInput.get(j - 1);
            }
            final double[] maneuversParameters = currentManeuver.getParameters();
            final double finalLocalTime = maneuversParameters[maneuversParameters.length - 1];

            // If the maneuver is firing
            if (currentTrigger.isFiring(state.getDate(), currentManeuver.getParameters())) {
                // If this is the first time the firing occurs for the maneuver then we will save it
                if (!firstFiringDateFound) {
                    firstFiringDateFound = true;
                    firstFiringDate = state.getDate();
                    dateFinalTime = firstFiringDate.shiftedBy(finalLocalTime);
                }
                definitionOfAttitudes(state, toReturn);
            }

            // If we already found the first date, and that the previous state is firing and the next is not, but
            // the time indicates that we did not reach the end of the maneuver. We will add the current state as if it
            // is firing. This way the maneuver is entirely covered in display. Else way, the arrow maneuver stopped being
            // displayed before the end of the maneuver.
            if (firstFiringDateFound) {
                assert previousState != null;
                if (currentTrigger.isFiring(previousState.getDate(), currentManeuver.getParameters()) && !(currentTrigger.isFiring(state.getDate(), currentManeuver.getParameters()))) {
                    assert dateFinalTime != null;
                    if (previousState.getDate().isBefore(dateFinalTime)) {

                        definitionOfAttitudes(state, toReturn);
                    }
                }
            }
        }
        return toReturn;
    }

    private void definitionOfAttitudes(final SpacecraftState state, final List<Attitude> toReturn) {

        // The default direction of thrust of the 3D model is PLUS_J, so we will need to make sure when the
        // direction of thrust asked is PLUS_J in Local Orbital Frame, we will need just to retrieve the
        // attitude from the sequence.
        if (accelerationDirection != Vector3D.PLUS_J) {

            final Attitude currentAttitude = sequence.getAttitude(state.getOrbit(), state.getDate(), state.getFrame());
            final Rotation currentRotation = currentAttitude.getRotation();
            // We need the acceleration direction to NOT be PLUS_J else way we can't compute the following rotation :
            final Rotation rotationFromXtoDirection = new Rotation(Vector3D.PLUS_J, accelerationDirection.negate());
            // If we want to display the thrust and not the acceleration, we need to rotate the vector by 180°
            if (showTrust) {
                final Rotation showTrustRotation = new Rotation(accelerationDirection, accelerationDirection.negate());
                final Rotation tempRotation = rotationFromXtoDirection.compose(currentRotation, RotationConvention.VECTOR_OPERATOR);
                final Rotation finalRotation = showTrustRotation.compose(tempRotation, RotationConvention.VECTOR_OPERATOR);
                final Attitude finalAttitude = new Attitude(state.getDate(), state.getFrame(), finalRotation, Vector3D.ZERO, Vector3D.ZERO);
                toReturn.add(finalAttitude);
            }
            else {
                final Rotation finalRotation = rotationFromXtoDirection.compose(currentRotation, RotationConvention.VECTOR_OPERATOR);
                final Attitude finalAttitude = new Attitude(state.getDate(), state.getFrame(), finalRotation, Vector3D.ZERO, Vector3D.ZERO);
                toReturn.add(finalAttitude);
            }
        }
        // Case : acceleration = PLUS_J
        else {

            final Attitude currentAttitude = sequence.getAttitude(state.getOrbit(), state.getDate(), state.getFrame());
            final Rotation currentRotation = currentAttitude.getRotation();
            // Rotation of PLUS_J of 180°
            if (showTrust) {
                final Rotation rotationFromXtoDirection = new Rotation(Vector3D.PLUS_J, Vector3D.MINUS_J);
                final Rotation finalRotation = rotationFromXtoDirection.compose(currentRotation, RotationConvention.VECTOR_OPERATOR);
                final Attitude finalAttitude = new Attitude(state.getDate(), state.getFrame(), finalRotation, Vector3D.ZERO, Vector3D.ZERO);
                toReturn.add(finalAttitude);
            }
            else {
                // No need to compute the direction rotation here
                final Attitude finalAttitude = new Attitude(state.getDate(), state.getFrame(), currentRotation, Vector3D.ZERO, Vector3D.ZERO);
                toReturn.add(finalAttitude);
            }
        }
    }
}
