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
import org.orekit.czml.ArchiObjects.Builders.ManeuverSequenceBuilder;
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
import java.util.Collections;
import java.util.List;

/**
 * Maneuver sequence class
 *
 * <p> The maneuver sequence class depicts the maneuvers done by a single satellite. It can only manage one direction at a time for the maneuvers for the moment. </p>
 *
 * @author Julien LEBLOND
 * @since 1.0.0
 */

public class ManeuverSequence extends AbstractPrimaryObject implements CzmlPrimaryObject {

    /** A basic default ID for maneuvers. */
    public static final String DEFAULT_ID = "MANEUVER/";

    /** A basic default name for maneuvers. */
    public static final String DEFAULT_NAME = "Maneuvers : ";

    /** The default 3D model representing an arrow, this can depict the acceleration or the thrust. */
    public static final String DEFAULT_PATH_MODEL = Header.DEFAULT_RESOURCES + "/Default3DModels/maneuver_model.glb";

    /** The default string for the name to which the maneuver sequence is applied. */
    public static final String DEFAULT_APPLIED = ", applied to :";

    /** This allows to reference the position of an object. */
    public static final String DEFAULT_H_POSITION = "#position";

    /** The list of maneuvers, can be a list of only one maneuver. */
    private List<Maneuver> maneuvers;

    /** The bounded propagator extracted from the satellite. */
    private BoundedPropagator propagator;

    /** The list of spacecraft state of the satellite. */
    private List<SpacecraftState> states;

    /** The availabilities of the maneuvers, when to display them. */
    private List<TimeInterval> availabilitiesManeuvers;

    /** The model to represents the acceleration or the thrust. */
    private CzmlModel model;

    /** The reference in position of the satellite. */
    private Reference satellitePositionReference;

    /** The direction of the arrows, by default, this is the acceleration direction. */
    private List<Vector3D> arrowsDirection = new ArrayList<>();

    /** The orientation objects that allows to write the attitude in the CzmlFile. */
    private List<Orientation> orientations;

    /** The attitudes of the maneuvers, each sublist is attributed at each maneuver. */
    private List<List<Attitude>> attitudesWithManeuver = new ArrayList<>();

    /** The sequence of attitudes if the satellite. */
    private AttitudesSequence sequence;

    /** The local orbital frame used for the satellite. */
    private LOF lof;

    /** To show the acceleration (false), or the thrust (true) with the arrow. */
    private boolean showTrust;

    // Constructors

    /**
     * This constructor allows creating a maneuver sequence object for a given maneuver on a satellite.
     *
     * @param sequenceInput         : The attitude sequence of the satellite taking into account the orientation that will be
     *                              necessary for the maneuver.
     * @param maneuverInput         : The maneuver to perform.
     * @param satelliteInput        : The satellite that will perform the maneuver.
     * @param accelerationDirection : The direction of the acceleration of the maneuver.
     * @param lofInput              : The local orbital frame of the satellite.
     * @param showTrustInput        : To show or not the arrow in the direction of the thrust (by default, it shows the direction of the acceleration)
     * @param pathModel             : The path to the model of the arrow to display.
     * @see OtherTutorials.ManeuverSequenceExample
     */
    public ManeuverSequence(final AttitudesSequence sequenceInput, final Maneuver maneuverInput,
                            final Satellite satelliteInput, final Vector3D accelerationDirection, final LOF lofInput,
                            final boolean showTrustInput,
                            final String pathModel) throws URISyntaxException, IOException {
        final List<Maneuver> maneuversTemp = new ArrayList<>();
        maneuversTemp.add(maneuverInput);

        this.maneuvers  = maneuversTemp;
        this.propagator = (BoundedPropagator) satelliteInput.getSatellitePropagator();
        this.states     = satelliteInput.getAllSpaceCraftStates();
        this.lof        = lofInput;
        this.sequence   = sequenceInput;
        this.showTrust  = showTrustInput;
        final int length = maneuversTemp.size();

        this.arrowsDirection = Collections.singletonList(accelerationDirection);

        this.setId(DEFAULT_ID + maneuversTemp.subList(0, length));
        this.setName(DEFAULT_NAME + length + DEFAULT_APPLIED + propagator.toString());
        final JulianDate startDate = absoluteDateToJulianDate(propagator.getMinDate(), Header.getTimeScale());
        final JulianDate stopDate  = absoluteDateToJulianDate(propagator.getMaxDate(), Header.getTimeScale());
        this.setAvailability(new TimeInterval(startDate, stopDate));
        this.satellitePositionReference = new Reference(satelliteInput.getId() + DEFAULT_H_POSITION);

        this.attitudesWithManeuver   = generateAllAttitudesManeuvers(states, maneuversTemp, arrowsDirection);
        this.model                   = new CzmlModel(pathModel, 500000, 40, 5E-05);
        this.availabilitiesManeuvers = generateAvailabilitiesManeuvers(maneuvers);
        this.orientations            = generateOrientationManeuvers(attitudesWithManeuver);
    }

    /**
     * This constructor allows creating a maneuver sequence object for a given list of maneuvers on a satellite with default parameters.
     *
     * @param sequenceInput         : The attitude sequence of the satellite taking into account the orientation that will be
     *                              necessary for the maneuver.
     * @param maneuversInput        : The list of maneuvers to perform.
     * @param satelliteInput        : The satellite that will perform the maneuvers.
     * @param accelerationDirection : The direction of the acceleration of the maneuver.
     * @param lofInput              : The local orbital frame of the satellite.
     * @see OtherTutorials.ManeuverSequenceExample
     */
    public ManeuverSequence(final AttitudesSequence sequenceInput, final List<Maneuver> maneuversInput,
                            final Satellite satelliteInput, final Vector3D accelerationDirection,
                            final LOF lofInput) throws URISyntaxException, IOException {
        this(sequenceInput, maneuversInput, satelliteInput, accelerationDirection, lofInput, false,
                DEFAULT_PATH_MODEL);
    }


    /**
     * This constructor allows creating a maneuver sequence object for a given list of maneuvers on a satellite with no default parameters.
     * This constructor allows each maneuver to have the same direction of the propulsion.
     *
     * @param sequenceInput         : The attitude sequence of the satellite taking into account the orientation that will be
     *                              necessary for the maneuver.
     * @param maneuversInput        : The list of maneuvers to perform.
     * @param satelliteInput        : The satellite that will perform the maneuvers.
     * @param accelerationDirection : The direction of the acceleration of the maneuvers.
     * @param lofInput              : The local orbital frame of the satellite.
     * @param showTrustInput        : To show or not the arrow in the direction of the thrust (by default, it shows the direction of the acceleration)
     * @param pathModel             : The path to the model of the arrow to display.
     * @see OtherTutorials.ManeuverSequenceExample
     */
    public ManeuverSequence(final AttitudesSequence sequenceInput, final List<Maneuver> maneuversInput,
                            final Satellite satelliteInput, final Vector3D accelerationDirection, final LOF lofInput,
                            final boolean showTrustInput,
                            final String pathModel) throws URISyntaxException, IOException {
        this(sequenceInput, maneuversInput, satelliteInput, Collections.singletonList(accelerationDirection), lofInput,
                showTrustInput, pathModel);
    }


    /**
     * The maneuver sequence constructor that allows different direction for each maneuver.
     *
     * @param sequenceInput         : The attitude sequence of the satellite taking into account the orientation that will be
     *                              necessary for the maneuver.
     * @param maneuversInput        : The list of maneuvers to perform.
     * @param satelliteInput        : The satellite that will perform the maneuvers.
     * @param accelerationDirection : The list of the directions of the accelerations of the maneuvers.
     * @param lofInput              : The local orbital frame of the satellite.
     * @param showTrustInput        : To show or not the arrow in the direction of the thrust (by default, it shows the direction of the acceleration)
     * @param pathModel             : The path to the model of the arrow to display.
     */
    public ManeuverSequence(final AttitudesSequence sequenceInput, final List<Maneuver> maneuversInput,
                            final Satellite satelliteInput, final List<Vector3D> accelerationDirection,
                            final LOF lofInput,
                            final boolean showTrustInput,
                            final String pathModel) throws URISyntaxException, IOException {
        this.maneuvers  = maneuversInput;
        this.propagator = (BoundedPropagator) satelliteInput.getSatellitePropagator();
        this.states     = satelliteInput.getAllSpaceCraftStates();

        if (accelerationDirection.size() == 1) {
            for (int i = 0; i < maneuversInput.size(); i++) {
                arrowsDirection.add(accelerationDirection.get(0));
            }
        } else {
            arrowsDirection = accelerationDirection;
        }

        this.lof       = lofInput;
        this.sequence  = sequenceInput;
        this.showTrust = showTrustInput;
        int length = maneuversInput.size();
        if (length > 10) {
            length = 10;
        }
        this.setId(DEFAULT_ID + maneuversInput.subList(0, length));
        this.setName(DEFAULT_NAME + length + DEFAULT_APPLIED + propagator.toString());
        final JulianDate startDate = absoluteDateToJulianDate(propagator.getMinDate(), Header.getTimeScale());
        final JulianDate stopDate  = absoluteDateToJulianDate(propagator.getMaxDate(), Header.getTimeScale());
        this.setAvailability(new TimeInterval(startDate, stopDate));
        this.satellitePositionReference = new Reference(satelliteInput.getId() + DEFAULT_H_POSITION);
        this.attitudesWithManeuver      = generateAllAttitudesManeuvers(states, maneuversInput, arrowsDirection);
        this.model                      = new CzmlModel(pathModel, 500000, 40, 5E-05);
        this.availabilitiesManeuvers    = generateAvailabilitiesManeuvers(maneuvers);
        this.orientations               = generateOrientationManeuvers(attitudesWithManeuver);
    }

    // Builders

    public static ManeuverSequenceBuilder builder(final AttitudesSequence sequenceInput,
                                                  final Maneuver maneuverInput, final Satellite satellite,
                                                  final Vector3D accelerationDirection, final LOF lofInput) {
        return new ManeuverSequenceBuilder(sequenceInput, maneuverInput, satellite, accelerationDirection, lofInput);
    }

    public static ManeuverSequenceBuilder builder(final AttitudesSequence sequenceInput,
                                                  final List<Maneuver> maneuversInput, final Satellite satellite,
                                                  final Vector3D accelerationDirection, final LOF lofInput) {
        return new ManeuverSequenceBuilder(sequenceInput, maneuversInput, satellite, accelerationDirection, lofInput);
    }

    public static ManeuverSequenceBuilder builder(final AttitudesSequence sequenceInput,
                                                  final List<Maneuver> maneuversInput, final Satellite satellite,
                                                  final List<Vector3D> accelerationDirections, final LOF lofInput) {
        return new ManeuverSequenceBuilder(sequenceInput, maneuversInput, satellite, accelerationDirections, lofInput);
    }

    // Overrides

    @Override
    public void writeCzmlBlock() throws URISyntaxException, IOException {
        OUTPUT.setPrettyFormatting(true);
        for (int i = 0; i < maneuvers.size(); i++) {
            final Maneuver     currentManeuver     = maneuvers.get(i);
            final Orientation  currentOrientation  = orientations.get(i);
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


    // Getters

    public List<Maneuver> getManeuvers() {
        return new ArrayList<>(maneuvers);
    }

    public BoundedPropagator getPropagator() {
        return propagator;
    }

    public List<SpacecraftState> getStates() {
        return new ArrayList<>(states);
    }

    public List<TimeInterval> getAvailabilitiesManeuvers() {
        return new ArrayList<>(availabilitiesManeuvers);
    }

    public CzmlModel getModel() {
        return model;
    }

    public Reference getSatellitePositionReference() {
        return satellitePositionReference;
    }

    public List<Vector3D> getArrowsDirection() {
        return new ArrayList<>(arrowsDirection);
    }

    public List<Orientation> getOrientations() {
        return new ArrayList<>(orientations);
    }

    public List<List<Attitude>> getAttitudesWithManeuver() {
        return new ArrayList<>(attitudesWithManeuver);
    }

    public AttitudesSequence getSequence() {
        return sequence;
    }

    public LOF getLof() {
        return lof;
    }

    public boolean isShowTrust() {
        return showTrust;
    }


    // Private functions

    /**
     * This function aims at generating a list of all the attitudes by maneuver.
     *
     * @param statesInput    : The list of the spacecraft states of the satellite.
     * @param maneuversInput : The list of the maneuvers to perform.
     * @param directions     : The direction of the propulsion of the maneuver.
     * @return : A list of the attitudes organized by maneuver.
     */
    private List<List<Attitude>> generateAllAttitudesManeuvers(final List<SpacecraftState> statesInput,
                                                               final List<Maneuver> maneuversInput,
                                                               final List<Vector3D> directions) {
        final List<List<Attitude>> toReturn = new ArrayList<>();

        // Iteration for each maneuver
        for (int i = 0; i < maneuversInput.size(); i++) {
            final Maneuver maneuver  = maneuversInput.get(i);
            final Vector3D direction = directions.get(i);
            toReturn.add(generateAttitudeForOneManeuver(statesInput, maneuver, direction));
        }
        return toReturn;
    }

    /**
     * This function will generate a list of the time intervals representing when the maneuvers take place.
     *
     * @param maneuverList : The list of the maneuvers to perform.
     * @return : A list of the time intervals chronologically ordered of when the maneuvers happen.
     */
    private List<TimeInterval> generateAvailabilitiesManeuvers(final List<Maneuver> maneuverList) {
        final List<TimeInterval> toReturn = new ArrayList<>();
        for (final Maneuver currentManeuver : maneuverList) {
            final AbstractManeuverTriggers currentTrigger = (AbstractManeuverTriggers) currentManeuver.getManeuverTriggers();
            final TimeSpanMap<Boolean>     map            = currentTrigger.getFirings();
            for (TimeSpanMap.Span<Boolean> span = map.getFirstNonNullSpan(); span != null; span = span.next()) {
                if (span.getData()) {
                    if (span.getEnd()
                            .isAfter(julianDateToAbsoluteDate(Header.getMasterClock()
                                                                    .getAvailability()
                                                                    .getStop(), Header.getTimeScale()))) {
                        toReturn.add(new TimeInterval(absoluteDateToJulianDate(span.getStart(), Header.getTimeScale()),
                                Header.getMasterClock()
                                      .getAvailability()
                                      .getStop()));
                    } else if (span.getStart()
                                   .isBefore(julianDateToAbsoluteDate(Header.getMasterClock()
                                                                            .getAvailability()
                                                                            .getStart(), Header.getTimeScale()))) {
                        toReturn.add(new TimeInterval(Header.getMasterClock()
                                                            .getAvailability()
                                                            .getStart(),
                                absoluteDateToJulianDate(span.getEnd(), Header.getTimeScale())));
                    } else {
                        toReturn.add(new TimeInterval(absoluteDateToJulianDate(span.getStart(), Header.getTimeScale()),
                                absoluteDateToJulianDate(span.getEnd(), Header.getTimeScale())));
                    }
                }
            }
        }
        return toReturn;
    }

    /**
     * This function generates a list of the attitudes of the satellite knowing a maneuver is performing.
     *
     * @param direction       : The list of the direction of the propulsion of the maneuvers.
     * @param statesInput     : The list of the spacecraft states of the satellite.
     * @param currentManeuver : The maneuver to perform.
     * @return : A list of the attitudes during the maneuver.
     */
    private List<Attitude> generateAttitudeForOneManeuver(final List<SpacecraftState> statesInput,
                                                          final Maneuver currentManeuver,
                                                          final Vector3D direction) {

        final ManeuverTriggers currentTrigger       = currentManeuver.getManeuverTriggers();
        boolean                firstFiringDateFound = false;
        AbsoluteDate           dateFinalTime        = null;
        SpacecraftState        previousState        = null;
        AbsoluteDate           firstFiringDate;

        final List<Attitude> toReturn = new ArrayList<>();

        for (int j = 0; j < statesInput.size(); j++) {

            final SpacecraftState state = statesInput.get(j);
            if (j != 0) {
                previousState = statesInput.get(j - 1);
            }
            final double[] maneuversParameters = currentManeuver.getParameters();
            final double   finalLocalTime      = maneuversParameters[maneuversParameters.length - 1];

            // If the maneuver is firing
            if (currentTrigger.isFiring(state.getDate(), currentManeuver.getParameters())) {
                // If this is the first time the firing occurs for the maneuver, then we will save it
                if (!firstFiringDateFound) {
                    firstFiringDateFound = true;
                    firstFiringDate      = state.getDate();
                    dateFinalTime        = firstFiringDate.shiftedBy(finalLocalTime);
                }
                definitionOfAttitudes(direction, state, toReturn);
            }

            // If we already found the first date, and that the previous state is firing and the next is not, but
            // the time indicates that we did not reach the end of the maneuver. We will add the current state as if it
            // is firing. This way the maneuver is entirely covered in display. Else way, the arrow maneuver stopped being
            // displayed before the end of the maneuver.
            if (firstFiringDateFound) {
                assert previousState != null;
                if (currentTrigger.isFiring(previousState.getDate(),
                        currentManeuver.getParameters()) && !(currentTrigger.isFiring(state.getDate(),
                        currentManeuver.getParameters()))) {
                    assert dateFinalTime != null;
                    if (previousState.getDate()
                                     .isBefore(dateFinalTime)) {

                        definitionOfAttitudes(direction, state, toReturn);
                    }
                }
            }
        }
        return toReturn;
    }

    /**
     * This function will generate the orientations in time, knowing the list of the attitudes ordered by maneuvers.
     *
     * @param allAttitudeByManeuver : The list of the attitudes ordered by maneuver.
     * @return : A list of orientation objects representing the orientation of the satellite in time to be written in the czml file.
     */
    private List<Orientation> generateOrientationManeuvers(final List<List<Attitude>> allAttitudeByManeuver) {
        final List<Orientation> toReturn = new ArrayList<>();
        for (final List<Attitude> attitudesGivenManeuver : allAttitudeByManeuver) {
            if (!attitudesGivenManeuver.isEmpty()) {
                toReturn.add(Orientation.builder(attitudesGivenManeuver, propagator.getFrame())
                                        .withInvertToITRF(false)
                                        .build());
            }
        }
        return toReturn;
    }


    /**
     * This function aims at generating the attitudes for a given spacecraft state.
     *
     * @param direction : The direction of the maneuver to consider.
     * @param state     : The state to consider.
     * @param toReturn  : The list to add attitudes into.
     */
    private void definitionOfAttitudes(final Vector3D direction, final SpacecraftState state,
                                       final List<Attitude> toReturn) {

        // The default direction of thrust of the 3D model is PLUS_J, so we will need to make sure when the
        // direction of thrust asked is PLUS_J in Local Orbital Frame, we will need just to retrieve the
        // attitude from the sequence.

        if (direction != Vector3D.PLUS_J) {

            final Attitude currentAttitude = sequence.getAttitude(state.getOrbit(), state.getDate(), state.getFrame());
            final Rotation currentRotation = currentAttitude.getRotation();
            // We need the acceleration direction to NOT be PLUS_J else way we can't compute the following rotation :
            final Rotation rotationFromXtoDirection = new Rotation(Vector3D.PLUS_J, direction.negate());
            // If we want to display the thrust and not the acceleration, we need to rotate the vector by 180°
            if (showTrust) {
                final Rotation showTrustRotation = new Rotation(direction, direction.negate());
                final Rotation tempRotation = rotationFromXtoDirection.compose(currentRotation,
                        RotationConvention.VECTOR_OPERATOR);
                final Rotation finalRotation = showTrustRotation.compose(tempRotation,
                        RotationConvention.VECTOR_OPERATOR);
                final Attitude finalAttitude = new Attitude(state.getDate(), state.getFrame(), finalRotation,
                        Vector3D.ZERO, Vector3D.ZERO);
                toReturn.add(finalAttitude);
            } else {
                final Rotation finalRotation = rotationFromXtoDirection.compose(currentRotation,
                        RotationConvention.VECTOR_OPERATOR);
                final Attitude finalAttitude = new Attitude(state.getDate(), state.getFrame(), finalRotation,
                        Vector3D.ZERO, Vector3D.ZERO);
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
                final Rotation finalRotation = rotationFromXtoDirection.compose(currentRotation,
                        RotationConvention.VECTOR_OPERATOR);
                final Attitude finalAttitude = new Attitude(state.getDate(), state.getFrame(), finalRotation,
                        Vector3D.ZERO, Vector3D.ZERO);
                toReturn.add(finalAttitude);
            } else {
                // No need to compute the direction rotation here
                final Attitude finalAttitude = new Attitude(state.getDate(), state.getFrame(), currentRotation,
                        Vector3D.ZERO, Vector3D.ZERO);
                toReturn.add(finalAttitude);
            }
        }
    }
}
