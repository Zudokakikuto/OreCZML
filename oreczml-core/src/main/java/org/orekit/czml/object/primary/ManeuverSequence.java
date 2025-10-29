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
package org.orekit.czml.object.primary;

import cesiumlanguagewriter.CesiumOutputStream;
import cesiumlanguagewriter.CesiumStreamWriter;
import cesiumlanguagewriter.JulianDate;
import cesiumlanguagewriter.PacketCesiumWriter;
import cesiumlanguagewriter.Reference;
import cesiumlanguagewriter.TimeInterval;
import org.hipparchus.geometry.euclidean.threed.Rotation;
import org.hipparchus.geometry.euclidean.threed.RotationConvention;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.orekit.attitudes.Attitude;
import org.orekit.attitudes.AttitudesSequence;
import org.orekit.czml.errors.OreCzmlException;
import org.orekit.czml.errors.OreCzmlMessages;
import org.orekit.czml.object.nonvisual.CzmlModel;
import org.orekit.czml.object.primary.entities.Spacecraft;
import org.orekit.czml.object.secondary.Clock;
import org.orekit.czml.object.secondary.Orientation;
import org.orekit.czml.object.utils.DateUtils;
import org.orekit.forces.maneuvers.Maneuver;
import org.orekit.forces.maneuvers.trigger.AbstractManeuverTriggers;
import org.orekit.forces.maneuvers.trigger.ManeuverTriggers;
import org.orekit.frames.LOF;
import org.orekit.propagation.BoundedPropagator;
import org.orekit.propagation.SpacecraftState;
import org.orekit.time.AbsoluteDate;
import org.orekit.utils.TimeSpanMap;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Maneuver sequence class
 * <p>
 * The maneuver sequence class depicts the maneuvers done by a single satellite.
 * It can only manage one direction at a time for the maneuvers for the moment.
 * </p>
 *
 * @author Julien LEBLOND
 * @since 1.0.0
 */
public class ManeuverSequence
    extends
    AbstractPrimaryObject<ManeuverSequence> {

    /**
     * A basic default ID for maneuvers.
     */
    public static final String DEFAULT_ID = "MANEUVER/";

    /**
     * A basic default name for maneuvers.
     */
    public static final String DEFAULT_NAME = "Maneuvers : ";

    /**
     * The default 3D model representing an arrow, this can depict the
     * acceleration or the thrust.
     */
    public static final String DEFAULT_PATH_MODEL =
        Header.getDefaultResources() + "/maneuver_model.glb";

    /**
     * The default string for the name to which the maneuver sequence is
     * applied.
     */
    public static final String DEFAULT_APPLIED = ", applied to :";

    /**
     * This allows referencing the position of an object.
     */
    public static final String DEFAULT_H_POSITION = "#position";

    /**
     * The list of maneuvers can be a list of only one maneuver.
     */
    private final List<Maneuver> maneuvers;

    /**
     * The bounded propagator extracted from the satellite.
     */
    private final BoundedPropagator propagator;

    /**
     * The list of spacecraft state of the satellite.
     */
    private final List<SpacecraftState> states;

    /**
     * The availabilities of the maneuvers, when to display them.
     */
    private final List<TimeInterval> availabilitiesManeuvers;

    /**
     * The model to represent the acceleration or the thrust.
     */
    private final CzmlModel model;

    /**
     * The reference in position of the satellite.
     */
    private final Reference satellitePositionReference;

    /**
     * The direction of the arrows, by default, this is the acceleration
     * direction.
     */
    private List<Vector3D> arrowsDirection = new ArrayList<>();

    /**
     * The orientation objects that allow to write the attitude in the CzmlFile.
     */
    private final List<Orientation> orientations;

    /**
     * The attitudes of the maneuvers, each sublist is attributed to each
     * maneuver.
     */
    private List<List<Attitude>> attitudesWithManeuver = new ArrayList<>();

    /**
     * The sequence of attitudes if the satellite.
     */
    private final AttitudesSequence sequence;

    /**
     * The local orbital frame used for the satellite.
     */
    private final LOF lof;

    /**
     * To show the acceleration (false), or the thrust (true) with the arrow.
     */
    private final boolean showTrust;

    /** The spacecraft concerned. */
    private Spacecraft spacecraft;

    /** The clock of the maneuver sequence. */
    private Clock clock;

    // Constructors

    /**
     * This constructor allows creating a maneuver sequence object for a given
     * maneuver on a satellite.
     *
     * @param sequenceInput : The attitude sequence of the satellite taking into
     *        account the orientation that will be necessary for the maneuver.
     * @param maneuverInput : The maneuver to perform.
     * @param spacecraftInput : The satellite that will perform the maneuver.
     * @param accelerationDirection : The direction of the acceleration of the
     *        maneuver.
     * @param lofInput : The local orbital frame of the satellite.
     * @param showTrustInput : To show or not the arrow in the direction of the
     *        thrust (by default, it shows the direction of the acceleration).
     * @param pathModelInput : The path to the model of the arrow to display.
     *        Check the 'ManeuverSequenceExample' tutorial to see the usage of
     *        this class.
     * @param customID : The custom ID of the Maneuver Sequence.
     * @param clock : The clock considered
     */
    ManeuverSequence(final AttitudesSequence sequenceInput,
                     final Maneuver maneuverInput,
                     final Spacecraft spacecraftInput,
                     final Vector3D accelerationDirection, final LOF lofInput,
                     final boolean showTrustInput, final String pathModelInput,
                     final String customID, final Clock clock) {

        final List<Maneuver> maneuversTemp = new ArrayList<>();
        maneuversTemp.add(maneuverInput);
        setAvailability(clock.getAvailability());

        this.maneuvers = maneuversTemp;
        this.clock = clock;
        this.propagator =
            (BoundedPropagator) spacecraftInput.getSpacecraftPropagator();
        this.spacecraft = spacecraftInput;
        this.states = spacecraftInput.getSpaceCraftStates();
        this.lof = lofInput;
        this.sequence = sequenceInput;
        this.showTrust = showTrustInput;
        final int length = maneuversTemp.size();

        this.arrowsDirection = Collections.singletonList(accelerationDirection);

        this.setId(customID);
        this.setName(DEFAULT_NAME +
                     length + DEFAULT_APPLIED + propagator.toString());
        final JulianDate startDate =
            DateUtils.toJulianDate(propagator.getMinDate());
        final JulianDate stopDate =
            DateUtils.toJulianDate(propagator.getMaxDate());
        this.setAvailability(new TimeInterval(startDate, stopDate));
        this.satellitePositionReference =
            new Reference(spacecraftInput.getId() + DEFAULT_H_POSITION);

        this.attitudesWithManeuver =
            generateAttitudesManeuvers(states, maneuversTemp, arrowsDirection,
                                       sequenceInput);
        this.model =
            new CzmlModel(pathModelInput, 500000, 40, 5E-05, false, clock);
        this.availabilitiesManeuvers =
            generateAvailabilitiesManeuvers(maneuvers, clock.getAvailability());
        this.orientations = generateOrientationManeuvers(attitudesWithManeuver);
    }

    /**
     * This constructor allows creating a maneuver sequence object for a given
     * list of maneuvers on a satellite with default parameters.
     *
     * @param sequenceInput : The attitude sequence of the satellite taking into
     *        account the orientation that will be necessary for the maneuver.
     * @param maneuversInput : The list of maneuvers to perform.
     * @param spacecraftInput : The satellite that will perform the maneuvers.
     * @param accelerationDirection : The direction of the acceleration of the
     *        maneuver.
     * @param lofInput : The local orbital frame of the satellite. Check the
     *        'ManeuverSequenceExample' tutorial.
     * @param clock : The clock considered.
     */
    ManeuverSequence(final AttitudesSequence sequenceInput,
                     final List<Maneuver> maneuversInput,
                     final Spacecraft spacecraftInput,
                     final Vector3D accelerationDirection, final LOF lofInput,
                     final Clock clock) {
        this(sequenceInput, maneuversInput, spacecraftInput,
             accelerationDirection, lofInput, false, DEFAULT_PATH_MODEL,
             ManeuverSequence.DEFAULT_ID +
                                                                         maneuversInput
                                                                             .subList(0,
                                                                                      maneuversInput
                                                                                          .size() -
                                                                                         1),
             clock);
    }

    /**
     * This constructor allows creating a maneuver sequence object for a given
     * list of maneuvers on a satellite with no default parameters. This
     * constructor allows each maneuver to have the same direction of the
     * propulsion.
     *
     * @param sequenceInput : The attitude sequence of the satellite taking into
     *        account the orientation that will be necessary for the maneuver.
     * @param maneuversInput : The list of maneuvers to perform.
     * @param spacecraftInput : The satellite that will perform the maneuvers.
     * @param accelerationDirection : The direction of the acceleration of the
     *        maneuvers.
     * @param lofInput : The local orbital frame of the satellite.
     * @param showTrustInput : To show or not the arrow in the direction of the
     *        thrust (by default, it shows the direction of the acceleration).
     * @param pathModel : The path to the model of the arrow to display. Check
     *        the 'ManeuverSequenceExample' tutorial.
     * @param customID : The custom ID of the maneuver sequence object.
     * @param clock : The clock considered.
     */
    ManeuverSequence(final AttitudesSequence sequenceInput,
                     final List<Maneuver> maneuversInput,
                     final Spacecraft spacecraftInput,
                     final Vector3D accelerationDirection, final LOF lofInput,
                     final boolean showTrustInput, final String pathModel,
                     final String customID, final Clock clock) {
        this(sequenceInput, maneuversInput, spacecraftInput,
             Collections.singletonList(accelerationDirection), lofInput,
             showTrustInput, pathModel, customID, clock);
    }

    /**
     * The maneuver sequence constructor that allows different direction for
     * each maneuver.
     *
     * @param sequenceInput : The attitude sequence of the satellite taking into
     *        account the orientation that will be necessary for the maneuver.
     * @param maneuversInput : The list of maneuvers to perform.
     * @param spacecraftInput : The satellite that will perform the maneuvers.
     * @param accelerationDirection : The list of the directions of the
     *        accelerations of the maneuvers.
     * @param lofInput : The local orbital frame of the satellite.
     * @param showTrustInput : To show or not the arrow in the direction of the
     *        thrust (by default, it shows the direction of the acceleration).
     * @param pathModel : The path to the model of the arrow to display.
     * @param customID : The custom id of the maneuver sequence object
     * @param clock : The clock considered
     */
    ManeuverSequence(final AttitudesSequence sequenceInput,
                     final List<Maneuver> maneuversInput,
                     final Spacecraft spacecraftInput,
                     final List<Vector3D> accelerationDirection,
                     final LOF lofInput, final boolean showTrustInput,
                     final String pathModel, final String customID,
                     final Clock clock) {

        this.clock = clock;
        this.maneuvers = maneuversInput;
        this.spacecraft = spacecraftInput;
        this.sequence = sequenceInput;
        this.propagator = spacecraftInput.getSpacecraftBoundedPropagator();
        this.states = spacecraftInput.getSpaceCraftStates();
        setAvailability(clock.getAvailability());

        if (accelerationDirection.size() == 1) {
            for (int i = 0; i < maneuversInput.size(); i++) {
                arrowsDirection.add(accelerationDirection.get(0));
            }
        } else {
            arrowsDirection.addAll(accelerationDirection);
        }

        this.lof = lofInput;
        this.showTrust = showTrustInput;
        int length = maneuversInput.size();
        if (length > 10) {
            length = 10;
        }
        this.setId(customID);
        this.setName(DEFAULT_NAME +
                     length + DEFAULT_APPLIED + propagator.toString());
        final JulianDate startDate =
            DateUtils.toJulianDate(propagator.getMinDate());
        final JulianDate stopDate =
            DateUtils.toJulianDate(propagator.getMaxDate());
        this.setAvailability(new TimeInterval(startDate, stopDate));
        this.satellitePositionReference =
            new Reference(spacecraftInput.getId() + DEFAULT_H_POSITION);

        this.attitudesWithManeuver =
            generateAttitudesManeuvers(states, maneuversInput, arrowsDirection,
                                       sequenceInput);

        this.model = new CzmlModel(pathModel, 500000, 40, 5E-05, false, clock);

        this.availabilitiesManeuvers =
            generateAvailabilitiesManeuvers(maneuvers, clock.getAvailability());
        generateAvailabilitiesManeuvers(maneuvers, clock.getAvailability());

        this.orientations = generateOrientationManeuvers(attitudesWithManeuver);
    }

    // Builders

    /**
     * Builder maneuver sequence builder.
     *
     * @param sequenceInput the sequence input
     * @param maneuverInput the maneuver input
     * @param spacecraftInput the satellite
     * @param accelerationDirection the acceleration direction
     * @param lofInput the lof input
     * @param clockInput the clock
     * @return the maneuver sequence builder
     */
    public static ManeuverSequenceBuilder
        builder(final AttitudesSequence sequenceInput,
                final Maneuver maneuverInput, final Spacecraft spacecraftInput,
                final Vector3D accelerationDirection, final LOF lofInput,
                final Clock clockInput) {
        return new ManeuverSequenceBuilder(sequenceInput, maneuverInput,
                                           spacecraftInput,
                                           accelerationDirection, lofInput,
                                           clockInput);
    }

    /**
     * Builder maneuver sequence builder.
     *
     * @param sequenceInput the sequence input
     * @param maneuversInput the maneuvers input
     * @param spacecraftInput the spacecraft
     * @param accelerationDirection the acceleration direction
     * @param lofInput the lof input
     * @param clockInput the clock
     * @return the maneuver sequence builder
     */
    public static ManeuverSequenceBuilder
        builder(final AttitudesSequence sequenceInput,
                final List<Maneuver> maneuversInput,
                final Spacecraft spacecraftInput,
                final Vector3D accelerationDirection, final LOF lofInput,
                final Clock clockInput) {
        return new ManeuverSequenceBuilder(sequenceInput, maneuversInput,
                                           spacecraftInput,
                                           accelerationDirection, lofInput,
                                           clockInput);
    }

    /**
     * Builder maneuver sequence builder.
     *
     * @param sequenceInput the sequence input
     * @param maneuversInput the maneuvers input
     * @param spacecraftInput the spacecraft
     * @param accelerationDirections the acceleration directions
     * @param lofInput the lof input
     * @param clockInput the clock
     * @return the maneuver sequence builder
     */
    public static ManeuverSequenceBuilder
        builder(final AttitudesSequence sequenceInput,
                final List<Maneuver> maneuversInput,
                final Spacecraft spacecraftInput,
                final List<Vector3D> accelerationDirections, final LOF lofInput,
                final Clock clockInput) {
        return new ManeuverSequenceBuilder(sequenceInput, maneuversInput,
                                           spacecraftInput,
                                           accelerationDirections, lofInput,
                                           clockInput);
    }

    // Overrides

    @Override
    public void writeCzmlBlock(final CesiumStreamWriter stream,
                               final CesiumOutputStream output)
        throws URISyntaxException,
            IOException {
        output.setPrettyFormatting(true);
        for (int i = 0; i < maneuvers.size(); i++) {
            final Orientation currentOrientation = orientations.get(i);
            final TimeInterval currentAvailability =
                availabilitiesManeuvers.get(i);
            try (PacketCesiumWriter packet = stream.openPacket(output)) {
                packet
                    .writeId(DEFAULT_ID + satellitePositionReference + " " + i);
                packet.writeName(DEFAULT_NAME + satellitePositionReference);
                packet.writeAvailability(currentAvailability);
                packet
                    .writePositionPropertyReference(satellitePositionReference);

                currentOrientation.write(packet, output);

                model.generateCZML(packet, output);
            }
        }
    }

    @Override
    public ManeuverSequence cloneObject() {
        final ManeuverSequence toReturn;
        try {
            if (maneuvers.size() == 1) {
                toReturn =
                    ManeuverSequence
                        .builder(this.sequence, this.maneuvers.get(0),
                                 this.spacecraft, this.arrowsDirection.get(0),
                                 this.lof, this.clock)
                        .withCustomID(getId())
                        .withPathModel(this.model.getAbsolutePath())
                        .withShowTrust(this.showTrust).build();
            } else if (!maneuvers.isEmpty()) {
                if (arrowsDirection.size() == 1) {
                    toReturn =
                        ManeuverSequence
                            .builder(this.sequence, this.maneuvers,
                                     this.spacecraft,
                                     this.arrowsDirection.get(0), this.lof,
                                     this.clock)
                            .withCustomID(getId())
                            .withPathModel(this.model.getAbsolutePath())
                            .withShowTrust(this.showTrust).build();
                } else {
                    toReturn =
                        ManeuverSequence
                            .builder(this.sequence, this.maneuvers,
                                     this.spacecraft, this.arrowsDirection,
                                     this.lof, this.clock)
                            .withCustomID(getId())
                            .withPathModel(this.model.getAbsolutePath())
                            .withShowTrust(this.showTrust).build();
                }
            } else {
                throw new OreCzmlException(OreCzmlMessages.NOT_VALID_PRIMARY_OBJECT_FOR_CLONE);
            }
            return toReturn;
        } catch (URISyntaxException | IOException e) {
            throw new OreCzmlException(OreCzmlMessages.NOT_VALID_PRIMARY_OBJECT_FOR_CLONE);
        }
    }

    // Getters

    /**
     * Gets maneuvers.
     *
     * @return the maneuvers
     */
    public List<Maneuver> getManeuvers() {
        return Collections.unmodifiableList(maneuvers);
    }

    /**
     * Gets propagator.
     *
     * @return the propagator
     */
    public BoundedPropagator getPropagator() {
        return propagator;
    }

    /**
     * Gets model.
     *
     * @return the model
     */
    public CzmlModel getModel() {
        return model;
    }

    /**
     * Gets arrows direction.
     *
     * @return the arrows direction
     */
    public List<Vector3D> getArrowsDirection() {
        return Collections.unmodifiableList(arrowsDirection);
    }

    /**
     * Gets sequence.
     *
     * @return the sequence
     */
    public AttitudesSequence getSequence() {
        return sequence;
    }

    /**
     * Gets lof.
     *
     * @return the lof
     */
    public LOF getLof() {
        return lof;
    }

    /**
     * Is show trust boolean.
     *
     * @return the boolean
     */
    public boolean isShowTrust() {
        return showTrust;
    }

    // Private functions

    /**
     * This function aims at generating a list of all the attitudes by maneuver.
     *
     * @param statesInput : The list of the spacecraft states of the satellite.
     * @param maneuversInput : The list of the maneuvers to perform.
     * @param directions : The direction of the propulsion of the maneuver.
     * @param sequenceInput : The sequence considered
     * @return : A list of the attitudes organized by maneuver.
     */
    private List<List<Attitude>>
        generateAttitudesManeuvers(final List<SpacecraftState> statesInput,
                                   final List<Maneuver> maneuversInput,
                                   final List<Vector3D> directions,
                                   final AttitudesSequence sequenceInput) {
        final List<List<Attitude>> toReturn = new ArrayList<>();

        // Iteration for each maneuver
        for (int i = 0; i < maneuversInput.size(); i++) {
            final Maneuver maneuver = maneuversInput.get(i);
            final Vector3D direction = directions.get(i);
            toReturn
                .add(generateAttitudeForOneManeuver(statesInput, maneuver,
                                                    direction, sequenceInput));
        }
        return toReturn;
    }

    /**
     * This function will generate a list of the time intervals representing
     * when the maneuvers take place.
     *
     * @param maneuverList : The list of the maneuvers to perform.
     * @param availability : The availability considered.
     * @return : A list of the time intervals chronologically ordered of when
     *         the maneuvers happen.
     */
    private List<TimeInterval>
        generateAvailabilitiesManeuvers(final List<Maneuver> maneuverList,
                                        final TimeInterval availability) {

        final List<TimeInterval> toReturn = new ArrayList<>();
        for (final Maneuver currentManeuver : maneuverList) {
            final AbstractManeuverTriggers currentTrigger =
                (AbstractManeuverTriggers) currentManeuver
                    .getManeuverTriggers();
            final TimeSpanMap<Boolean> map = currentTrigger.getFirings();
            for (TimeSpanMap.Span<Boolean> span = map.getFirstNonNullSpan();
                 span != null; span = span.next()) {
                if (span.getData()) {
                    if (span.getEnd().isAfter(DateUtils
                        .toAbsoluteDate(availability.getStop()))) {
                        toReturn.add(new TimeInterval(DateUtils
                            .toJulianDate(span.getStart()),
                                                      availability.getStop()));
                    } else if (span.getStart().isBefore(DateUtils
                        .toAbsoluteDate(availability.getStart()))) {
                        toReturn
                            .add(new TimeInterval(availability.getStart(),
                                                  DateUtils.toJulianDate(span
                                                      .getEnd())));
                    } else {
                        toReturn
                            .add(new TimeInterval(DateUtils
                                .toJulianDate(span.getStart()),
                                                  DateUtils.toJulianDate(span
                                                      .getEnd())));
                    }
                }
            }
        }
        return toReturn;
    }

    /**
     * This function generates a list of the attitudes of the satellite knowing
     * a maneuver is performing.
     *
     * @param direction : The list of the direction of the propulsion of the
     *        maneuvers.
     * @param statesInput : The list of the spacecraft states of the satellite.
     * @param currentManeuver : The maneuver to perform.
     * @param sequenceInput : The sequence to consider
     * @return : A list of the attitudes during the maneuver.
     */
    private List<Attitude>
        generateAttitudeForOneManeuver(final List<SpacecraftState> statesInput,
                                       final Maneuver currentManeuver,
                                       final Vector3D direction,
                                       final AttitudesSequence sequenceInput) {

        final ManeuverTriggers currentTrigger =
            currentManeuver.getManeuverTriggers();
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
            final double[] maneuversParameters =
                currentManeuver.getParameters();
            final double finalLocalTime =
                maneuversParameters[maneuversParameters.length - 1];

            // If the maneuver is firing
            if (currentTrigger.isFiring(state.getDate(),
                                        currentManeuver.getParameters())) {
                // If this is the first time the firing occurs for the maneuver,
                // then we will save it
                if (!firstFiringDateFound) {
                    firstFiringDateFound = true;
                    firstFiringDate = state.getDate();
                    dateFinalTime = firstFiringDate.shiftedBy(finalLocalTime);
                }
                definitionOfAttitudes(direction, state, toReturn,
                                      sequenceInput);
            }

            // If we already found the first date, and that the previous state
            // is firing and the next is not, but
            // the time indicates that we did not reach the end of the maneuver.
            // We will add the current state as if it
            // is firing. This way the maneuver is entirely covered in display.
            // Else way, the arrow maneuver stopped being
            // displayed before the end of the maneuver.
            if (firstFiringDateFound && previousState != null) {
                if (currentTrigger.isFiring(previousState.getDate(),
                                            currentManeuver.getParameters()) &&
                    !(currentTrigger
                        .isFiring(state.getDate(),
                                  currentManeuver.getParameters()))) {
                    assert dateFinalTime != null;
                    if (previousState.getDate().isBefore(dateFinalTime)) {

                        definitionOfAttitudes(direction, state, toReturn,
                                              sequenceInput);
                    }
                }
            }
        }
        return toReturn;
    }

    /**
     * This function will generate the orientations in time, knowing the list of
     * the attitudes ordered by maneuvers.
     *
     * @param allAttitudeByManeuver : The list of the attitudes ordered by
     *        maneuver.
     * @return : A list of orientation objects representing the orientation of
     *         the satellite in time to be written in the czml file.
     */
    private List<Orientation>
        generateOrientationManeuvers(final List<List<Attitude>> allAttitudeByManeuver) {
        final List<Orientation> toReturn = new ArrayList<>();
        for (final List<Attitude> attitudesGivenManeuver : allAttitudeByManeuver) {
            if (!attitudesGivenManeuver.isEmpty()) {
                toReturn.add(Orientation
                    .builder(attitudesGivenManeuver, propagator.getFrame())
                    .withInvertToITRF(false).build());
            }
        }
        return toReturn;
    }

    /**
     * This function aims at generating the attitudes for a given spacecraft
     * state.
     *
     * @param direction : The direction of the maneuver to consider.
     * @param state : The state to consider.
     * @param toReturn : The list to add attitudes into.
     * @param sequenceInput : The sequence to consider
     */
    private void definitionOfAttitudes(final Vector3D direction,
                                       final SpacecraftState state,
                                       final List<Attitude> toReturn,
                                       final AttitudesSequence sequenceInput) {

        // The default direction of thrust of the 3D model is PLUS_J, so we will
        // need to make sure when the
        // direction of thrust asked is PLUS_J in Local Orbital Frame, we will
        // need just to retrieve the
        // attitude from the sequence.

        final Attitude currentAttitude =
            sequenceInput.getAttitude(state.getOrbit(), state.getDate(),
                                      state.getFrame());
        final Rotation currentRotation = currentAttitude.getRotation();
        if (direction != Vector3D.PLUS_J) {

            // We need the acceleration direction to NOT be PLUS_J else way we
            // can't compute the following rotation :
            final Rotation rotationFromXtoDirection =
                new Rotation(Vector3D.PLUS_J, direction.negate());
            // If we want to display the thrust and not the acceleration, we
            // need to rotate the vector by 180°
            if (showTrust) {
                final Rotation showTrustRotation =
                    new Rotation(direction, direction.negate());
                final Rotation tempRotation =
                    rotationFromXtoDirection
                        .compose(currentRotation,
                                 RotationConvention.VECTOR_OPERATOR);
                final Rotation finalRotation =
                    showTrustRotation
                        .compose(tempRotation,
                                 RotationConvention.VECTOR_OPERATOR);
                final Attitude finalAttitude =
                    new Attitude(state.getDate(), state.getFrame(),
                                 finalRotation, Vector3D.ZERO, Vector3D.ZERO);
                toReturn.add(finalAttitude);
            } else {
                final Rotation finalRotation =
                    rotationFromXtoDirection
                        .compose(currentRotation,
                                 RotationConvention.VECTOR_OPERATOR);
                final Attitude finalAttitude =
                    new Attitude(state.getDate(), state.getFrame(),
                                 finalRotation, Vector3D.ZERO, Vector3D.ZERO);
                toReturn.add(finalAttitude);
            }
        }
        // Case: acceleration = PLUS_J
        else {

            // Rotation of PLUS_J of 180°
            if (showTrust) {
                final Rotation rotationFromXtoDirection =
                    new Rotation(Vector3D.PLUS_J, Vector3D.MINUS_J);
                final Rotation finalRotation =
                    rotationFromXtoDirection
                        .compose(currentRotation,
                                 RotationConvention.VECTOR_OPERATOR);
                final Attitude finalAttitude =
                    new Attitude(state.getDate(), state.getFrame(),
                                 finalRotation, Vector3D.ZERO, Vector3D.ZERO);
                toReturn.add(finalAttitude);
            } else {
                // No need to compute the direction rotation here
                final Attitude finalAttitude =
                    new Attitude(state.getDate(), state.getFrame(),
                                 currentRotation, Vector3D.ZERO, Vector3D.ZERO);
                toReturn.add(finalAttitude);
            }
        }
    }
}
