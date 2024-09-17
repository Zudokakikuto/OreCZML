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
package org.orekit.czml.archi.builder;

import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.orekit.attitudes.AttitudesSequence;
import org.orekit.czml.object.primary.Header;
import org.orekit.czml.object.primary.ManeuverSequence;
import org.orekit.czml.object.primary.Satellite;
import org.orekit.forces.maneuvers.Maneuver;
import org.orekit.frames.LOF;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Maneuver sequence builder class
 * <p>
 * Builder for the {@link ManeuverSequence} class.
 * <p>
 *
 * @author Julien LEBLOND
 * @since 1.0.0
 */

public class ManeuverSequenceBuilder {

    /** The default path to the 3D model used to display an arrow that represents the thrust or the acceleration. */
    public static final String DEFAULT_PATH_MODEL = Header.DEFAULT_RESOURCES + "/Default3DModels/maneuver_model.glb";

    /** The satellite which performs maneuvers. */
    private Satellite satellite;

    /** The attitude sequence used during the mission. */
    private AttitudesSequence sequence;

    /** The local orbital frame of the satellite. */
    private LOF lof;

    /** The list of maneuvers that have to be performed. */
    private List<Maneuver> maneuvers;

    /** If only one single maneuver is used, this object will be used instead of the list of maneuvers. */
    private Maneuver singleManeuver;

    /** The direction of the maneuver(s). */
    private Vector3D direction;

    /** The list of the direction of the maneuvers if several direction are inputted. */
    private List<Vector3D> directions = new ArrayList<>();

    // Optional parameters

    /** The model path of the arrow. */
    private String pathModel = DEFAULT_PATH_MODEL;

    /** To either show the acceleration or the thrust. By default, it shows the acceleration. */
    private boolean showTrust = false;

    // Constructors

    /**
     * The constructor with multiple maneuvers for the maneuver sequence builder object.
     *
     * @param sequenceInput  : The sequence of attitude used during the mission.
     * @param maneuversInput : The list of maneuvers to be done for the mission.
     * @param satelliteInput : The satellite which performs maneuvers.
     * @param directionInput : Direction of the maneuvers. (multiple directions will soon be added)
     * @param lofInput       : The local orbital frame of the satellite.
     */
    public ManeuverSequenceBuilder(final AttitudesSequence sequenceInput, final List<Maneuver> maneuversInput,
                                   final Satellite satelliteInput, final Vector3D directionInput, final LOF lofInput) {
        this.sequence  = sequenceInput;
        this.satellite = satelliteInput;
        this.maneuvers = maneuversInput;
        this.direction = directionInput;
        this.lof       = lofInput;
    }

    /**
     * The constructor with a single maneuver for the maneuver sequence builder object.
     *
     * @param sequenceInput  : The sequence of attitude used during the mission.
     * @param maneuverInput  : The maneuver to be done for the mission.
     * @param satelliteInput : The satellite which performs the maneuver.
     * @param directionInput : Direction of the maneuver.
     * @param lofInput       : The local orbital frame iof the satellite.
     */
    public ManeuverSequenceBuilder(final AttitudesSequence sequenceInput, final Maneuver maneuverInput,
                                   final Satellite satelliteInput, final Vector3D directionInput, final LOF lofInput) {
        this.sequence       = sequenceInput;
        this.singleManeuver = maneuverInput;
        this.satellite      = satelliteInput;
        this.direction      = directionInput;
        this.lof            = lofInput;
    }

    public ManeuverSequenceBuilder(final AttitudesSequence sequenceInput, final List<Maneuver> maneuversInput,
                                   final Satellite satelliteInput, final List<Vector3D> directionsInput,
                                   final LOF lofInput) {
        this.sequence   = sequenceInput;
        this.maneuvers  = maneuversInput;
        this.satellite  = satelliteInput;
        this.directions = directionsInput;
        this.lof        = lofInput;
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
     * @return : The maneuver sequence builder with a given direction for the arrow.
     */
    public ManeuverSequenceBuilder withShowTrust(final boolean showTrustInput) {
        this.showTrust = showTrustInput;
        return this;
    }

    /**
     * The build function that generates a maneuver sequence object.
     *
     * @return : A maneuver sequence object with the given parameters of the builder.
     */
    public ManeuverSequence build() throws URISyntaxException, IOException {
        if (!(maneuvers == null)) {
            if (direction == null) {
                return new ManeuverSequence(sequence, maneuvers, satellite, directions, lof, showTrust, pathModel);
            } else {
                return new ManeuverSequence(sequence, maneuvers, satellite, direction, lof, showTrust, pathModel);
            }
        } else {
            return new ManeuverSequence(sequence, singleManeuver, satellite, direction, lof, showTrust, pathModel);
        }
    }
}
