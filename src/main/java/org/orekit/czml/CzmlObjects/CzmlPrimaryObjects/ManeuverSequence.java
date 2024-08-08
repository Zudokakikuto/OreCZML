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

    /** The direction of the arrow. (By default it is the acceleration). */
    private Vector3D arrowDirection;

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


    public ManeuverSequence(final AttitudesSequence sequence, final List<Maneuver> maneuversInput,
                            final Satellite satellite, final Vector3D accelerationDirection,
                            final LOF lofInput) throws URISyntaxException, IOException {
        this(sequence, maneuversInput, satellite, accelerationDirection, lofInput, false, Header.getMasterClock()
                                                                                                .getMultiplier(),
                DEFAULT_PATH_MODEL);
    }

    public ManeuverSequence(final AttitudesSequence sequenceInput, final List<Maneuver> maneuversInput,
                            final Satellite satellite, final Vector3D accelerationDirection, final LOF lofInput,
                            final boolean showTrustInput, final double timeShiftInput,
                            final String pathModel) throws URISyntaxException, IOException {
        this.maneuvers      = maneuversInput;
        this.propagator     = (BoundedPropagator) satellite.getSatellitePropagator();
        this.states         = satellite.getAllSpaceCraftStates();
        this.arrowDirection = accelerationDirection;
        this.lof            = lofInput;
        this.sequence       = sequenceInput;
        this.showTrust      = showTrustInput;
        int length = maneuversInput.size();
        if (length > 10) {
            length = 10;
        }
        this.setId(DEFAULT_ID + maneuversInput.subList(0, length));
        this.setName(DEFAULT_NAME + length + ", applied to :" + propagator.toString());
        final JulianDate startDate = absoluteDateToJulianDate(propagator.getMinDate(), Header.getTimeScale());
        final JulianDate stopDate  = absoluteDateToJulianDate(propagator.getMaxDate(), Header.getTimeScale());
        this.setAvailability(new TimeInterval(startDate, stopDate));
        this.satellitePositionReference = new Reference(satellite.getId() + DEFAULT_H_POSITION);

        this.attitudesWithManeuver   = generateAllAttitudesManeuvers(states, maneuversInput);
        this.model                   = new CzmlModel(pathModel, 500000, 40, 5E-05);
        this.availabilitiesManeuvers = generateAvailabilitiesManeuvers(maneuvers);
        this.orientations            = generateOrientationManeuvers(attitudesWithManeuver);
    }

    // Builder

    public static ManeuverSequenceBuilder builder(final AttitudesSequence sequenceInput,
                                                  final List<Maneuver> maneuversInput, final Satellite satellite,
                                                  final Vector3D accelerationDirection, final LOF lofInput) {
        return new ManeuverSequenceBuilder(sequenceInput, maneuversInput, satellite, accelerationDirection, lofInput);
    }

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


    // GETTERS

    public List<Maneuver> getManeuvers() {
        return maneuvers;
    }

    public BoundedPropagator getPropagator() {
        return propagator;
    }

    public List<SpacecraftState> getStates() {
        return states;
    }

    public List<TimeInterval> getAvailabilitiesManeuvers() {
        return availabilitiesManeuvers;
    }

    public CzmlModel getModel() {
        return model;
    }

    public Reference getSatellitePositionReference() {
        return satellitePositionReference;
    }

    public Vector3D getArrowDirection() {
        return arrowDirection;
    }

    public List<Orientation> getOrientations() {
        return orientations;
    }

    public List<List<Attitude>> getAttitudesWithManeuver() {
        return attitudesWithManeuver;
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

    private List<List<Attitude>> generateAllAttitudesManeuvers(final List<SpacecraftState> statesInput,
                                                               final List<Maneuver> maneuversInput) {
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

    private List<Orientation> generateOrientationManeuvers(final List<List<Attitude>> allAttitudeByManeuver) {
        final List<Orientation> toReturn = new ArrayList<>();
        for (final List<Attitude> attitudesGivenManeuver : allAttitudeByManeuver) {
            if (!attitudesGivenManeuver.isEmpty()) {
                toReturn.add(new Orientation(attitudesGivenManeuver, propagator.getFrame(), false));
            }
        }
        return toReturn;
    }

    private List<Attitude> generateAttitudeForOneManeuver(final List<SpacecraftState> statesInput,
                                                          final Maneuver currentManeuver) {
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
                // If this is the first time the firing occurs for the maneuver then we will save it
                if (!firstFiringDateFound) {
                    firstFiringDateFound = true;
                    firstFiringDate      = state.getDate();
                    dateFinalTime        = firstFiringDate.shiftedBy(finalLocalTime);
                }
                definitionOfAttitudes(state, toReturn);
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
        if (arrowDirection != Vector3D.PLUS_J) {

            final Attitude currentAttitude = sequence.getAttitude(state.getOrbit(), state.getDate(), state.getFrame());
            final Rotation currentRotation = currentAttitude.getRotation();
            // We need the acceleration direction to NOT be PLUS_J else way we can't compute the following rotation :
            final Rotation rotationFromXtoDirection = new Rotation(Vector3D.PLUS_J, arrowDirection.negate());
            // If we want to display the thrust and not the acceleration, we need to rotate the vector by 180°
            if (showTrust) {
                final Rotation showTrustRotation = new Rotation(arrowDirection, arrowDirection.negate());
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
