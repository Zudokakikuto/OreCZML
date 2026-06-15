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

import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.orekit.attitudes.AttitudesSequence;
import org.orekit.czml.object.primary.entities.Spacecraft;
import org.orekit.czml.object.secondary.Clock;
import org.orekit.forces.maneuvers.Maneuver;
import org.orekit.frames.LOF;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Maneuver sequence builder class
 * <p>
 * Builder for the {@link ManeuverSequence} class.
 *
 * @author Julien LEBLOND
 * @since 1.0.0
 */
public class ManeuverSequenceBuilder {

    /**
     * The default path to the 3D model used to display an arrow that represents
     * the thrust or the acceleration.
     */
    public static final String DEFAULT_PATH_MODEL =
        Objects.requireNonNull(ManeuverSequence.class.getClassLoader()
            .getResource("maneuver_model.glb")).getPath();

    /**
     * The satellite which performs maneuvers.
     */
    private final Spacecraft spacecraft;

    /**
     * The attitude sequence used during the mission.
     */
    private final AttitudesSequence sequence;

    /**
     * The local orbital frame of the satellite.
     */
    private final LOF lof;

    /**
     * The list of maneuvers that have to be performed.
     */
    private List<Maneuver> maneuvers;

    /**
     * If only one single maneuver is used, this object will be used instead of
     * the list of maneuvers.
     */
    private Maneuver singleManeuver;

    /**
     * The direction of the maneuver(s).
     */
    private Vector3D direction;

    /**
     * The list of the direction of the maneuvers if several directions are
     * inputted.
     */
    private List<Vector3D> directions = new ArrayList<>();

    // Optional parameters

    /**
     * The custom ID of the maneuver sequence.
     */
    private String customID;

    /**
     * The model path of the arrow.
     */
    private String pathModel = DEFAULT_PATH_MODEL;

    /**
     * To either show the acceleration or the thrust. By default, it shows the
     * acceleration.
     */
    private boolean showTrust = false;

    /** The clock to consider. */
    private Clock clock;

    // Constructors

    /**
     * The constructor with multiple maneuvers for the maneuver sequence builder
     * object.
     *
     * @param sequenceInput : The sequence of attitude used during the mission.
     * @param maneuversInput : The list of maneuvers to be done for the mission.
     * @param spacecraftInput : The spacecraft which performs maneuvers.
     * @param directionInput : Direction of the maneuvers. (multiple directions
     *        will soon be added)
     * @param lofInput : The local orbital frame of the satellite.
     * @param clock : The clock considered.
     */
    public ManeuverSequenceBuilder(final AttitudesSequence sequenceInput,
                                   final List<Maneuver> maneuversInput,
                                   final Spacecraft spacecraftInput,
                                   final Vector3D directionInput,
                                   final LOF lofInput, final Clock clock) {
        this.sequence = sequenceInput;
        this.spacecraft = spacecraftInput;
        this.maneuvers = new ArrayList<>(maneuversInput);
        this.direction = directionInput;
        this.lof = lofInput;
        this.customID =
            ManeuverSequence.DEFAULT_ID +
                        maneuvers.subList(0, maneuvers.size() - 1);
        this.clock = clock;
    }

    /**
     * The constructor with a single maneuver for the maneuver sequence builder
     * object.
     *
     * @param sequenceInput : The sequence of attitude used during the mission.
     * @param maneuverInput : The maneuver to be done for the mission.
     * @param spacecraftInput : The spacecraft which performs the maneuver.
     * @param directionInput : Direction of the maneuver.
     * @param lofInput : The local orbital frame iof the satellite.
     * @param clock : The clock considered.
     */
    public ManeuverSequenceBuilder(final AttitudesSequence sequenceInput,
                                   final Maneuver maneuverInput,
                                   final Spacecraft spacecraftInput,
                                   final Vector3D directionInput,
                                   final LOF lofInput, final Clock clock) {
        this.sequence = sequenceInput;
        this.singleManeuver = maneuverInput;
        this.spacecraft = spacecraftInput;
        this.direction = directionInput;
        this.lof = lofInput;
        this.customID = ManeuverSequence.DEFAULT_ID + singleManeuver.getName();
        this.clock = clock;
    }

    /**
     * The constructor with a single maneuver for the maneuver sequence builder
     * object.
     *
     * @param sequenceInput : The sequence of attitude used during the mission.
     * @param maneuversInput : The list of maneuver to be done for the mission.
     * @param spacecraftInput : The spacecraft which performs the maneuver.
     * @param directionsInput : The list of directions of the maneuvers.
     * @param lofInput : The local orbital frame iof the satellite.
     * @param clock : The clock considered.
     */
    public ManeuverSequenceBuilder(final AttitudesSequence sequenceInput,
                                   final List<Maneuver> maneuversInput,
                                   final Spacecraft spacecraftInput,
                                   final List<Vector3D> directionsInput,
                                   final LOF lofInput, final Clock clock) {
        this.sequence = sequenceInput;
        this.maneuvers = new ArrayList<>(maneuversInput);
        this.spacecraft = spacecraftInput;
        this.directions = new ArrayList<>(directionsInput);
        this.lof = lofInput;
        if (!maneuversInput.isEmpty()) {
            this.customID =
                ManeuverSequence.DEFAULT_ID +
                            maneuvers.subList(0, maneuvers.size() - 1);
        } else {
            this.customID = ManeuverSequence.DEFAULT_ID;
        }
        this.clock = clock;
    }

    /**
     * Function to set up a model.
     *
     * @param pathModelInput : The model to set up.
     * @return : The maneuver sequence builder with the given model loaded.
     */
    public ManeuverSequenceBuilder withPathModel(final String pathModelInput) {
        this.pathModel = pathModelInput;
        return this;
    }

    /**
     * Function to set up the arrow to show the acceleration of the thrust.
     *
     * @param showTrustInput : To display the thrust or not.
     * @return : The maneuver sequence builder with a given direction for the
     *         arrow.
     */
    public ManeuverSequenceBuilder withShowTrust(final boolean showTrustInput) {
        this.showTrust = showTrustInput;
        return this;
    }

    /**
     * Function to set up a custom ID.
     *
     * @param customIDInput : The custom ID to set up.
     * @return : The maneuver sequence object with a custom ID.
     */
    public ManeuverSequenceBuilder withCustomID(final String customIDInput) {
        this.customID = customIDInput;
        return this;
    }

    /**
     * Function to set up the clock.
     *
     * @param clockInput : The clock to set up.
     * @return : The maneuver sequence builder with a given clock.
     */
    public ManeuverSequenceBuilder withClock(final Clock clockInput) {
        this.clock = clockInput;
        return this;
    }

    /**
     * The build function that generates a maneuver sequence object.
     *
     * @return : A maneuver sequence object with the given parameters of the
     *         builder.
     */
    public ManeuverSequence build() {
        if (!(maneuvers == null)) {
            if (direction == null) {
                return new ManeuverSequence(sequence, maneuvers, spacecraft,
                                            directions, lof, showTrust,
                                            pathModel, customID, clock);
            } else {
                return new ManeuverSequence(sequence, maneuvers, spacecraft,
                                            direction, lof, showTrust,
                                            pathModel, customID, clock);
            }
        } else {
            return new ManeuverSequence(sequence, singleManeuver, spacecraft,
                                        direction, lof, showTrust, pathModel,
                                        customID, clock);
        }
    }
}
