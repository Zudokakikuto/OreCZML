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
package org.orekit.czml.object.primary.pointing;

import cesiumlanguagewriter.CesiumOutputStream;
import cesiumlanguagewriter.CesiumStreamWriter;
import cesiumlanguagewriter.JulianDate;
import cesiumlanguagewriter.PacketCesiumWriter;
import cesiumlanguagewriter.Reference;
import cesiumlanguagewriter.TimeInterval;
import org.hipparchus.geometry.euclidean.threed.Line;
import org.hipparchus.geometry.euclidean.threed.Rotation;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.orekit.attitudes.Attitude;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.czml.errors.OreCzmlException;
import org.orekit.czml.errors.OreCzmlMessages;
import org.orekit.czml.object.Polyline;
import org.orekit.czml.object.nonvisual.PointOnBody;
import org.orekit.czml.object.primary.AbstractPrimaryObject;
import org.orekit.czml.object.primary.entities.Spacecraft;
import org.orekit.czml.object.secondary.Orientation;
import org.orekit.frames.Frame;
import org.orekit.propagation.SpacecraftState;
import org.orekit.time.AbsoluteDate;

import java.awt.Color;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Attitude pointing class
 * <p>
 * The attitude pointing represents a line that will go towards the central body
 * to project the attitude of the satellite at the surface. A given direction
 * will be needed to know which axis the object will project.
 *
 * @author Julien LEBLOND.
 * @since 1.0.0
 */
public class AttitudePointing
    extends
    AbstractPrimaryObject {

    /**
     * The default ID of the attitude-pointing object.
     */
    public static final String DEFAULT_ID = "ATTITUDE_POINTING/";

    /**
     * The default name of the attitude-pointing object.
     */
    public static final String DEFAULT_NAME = "Attitude pointing of : ";

    /**
     * This can be used to create a reference in the position of an object.
     */
    public static final String DEFAULT_H_POSITION = "#position";

    /**
     * The default color of the line.
     */
    public static final Color DEFAULT_COLOR = Color.GREEN;

    /**
     * The satellite which attitude will be pointed.
     */
    private final Spacecraft satellite;

    /**
     * The list of the attitude of the satellite.
     */
    private final List<Attitude> satelliteAttitudes;

    /**
     * The list of the spacecraft states of the satellite.
     */
    private final List<SpacecraftState> states;

    /**
     * The central body around which the satellite is orbiting.
     */
    private final OneAxisEllipsoid body;

    /**
     * The orientation of the satellite.
     */
    private final Orientation satelliteOrientation;

    /**
     * The line to be displayed.
     */
    private final Polyline attitudePointingPolyline;

    /**
     * The list of the julian dates when the line should be displayed.
     */
    private final List<JulianDate> julianDates;

    /**
     * The point on body builds from the projected attitudes.
     */
    private final PointOnBody pointOnBody;

    /**
     * To display or not the path of the attitude pointing.
     */
    private boolean displayPointingPath = false;

    /**
     * To display or not the path with a period.
     */
    private boolean displayPeriodPointingPath = false;

    /**
     * The period in seconds of the path if displayed.
     */
    private double periodPointingPath = 0.0;

    // Constructors

    /**
     * The constructor of the attitude-pointing object with default parameters.
     *
     * @param satellite : The satellite that will point to the body.
     * @param body : The body to point to.
     * @param direction : The direction to point to.
     * @param availability : The availability of the attitude pointing.
     */
    AttitudePointing(final Spacecraft satellite, final OneAxisEllipsoid body,
                     final Vector3D direction,
                     final TimeInterval availability) {
        this(satellite, body, direction, DEFAULT_COLOR, false,
             DEFAULT_ID + satellite.getId(), availability);
    }

    /**
     * The constructor with no default parameters.
     *
     * @param satellite : The satellite that will point to the body.
     * @param body : The body to point to.
     * @param direction : The line of sight in the spacecraft frame.
     * @param color : The color of the pointing (polyline).
     * @param alwaysDisplayOnGround : Director that manages the pointing or not
     *        at objects during the orbit. Put this parameter on if the
     *        satellite is pointing at objects during the orbit. This boolean
     *        will project the attitude on the ground when it is not pointing at
     *        objects. When the attitude is pointing at objects, it will put the
     *        projection on the pointed object
     *        AttitudeTuto.AttitudePathAlongOrbit
     * @param ID : The ID of the attitude pointing object
     * @param availability : The availability of the attitude pointing
     */
    AttitudePointing(final Spacecraft satellite, final OneAxisEllipsoid body,
                     final Vector3D direction, final Color color,
                     final boolean alwaysDisplayOnGround, final String ID,
                     final TimeInterval availability) {
        this.setId(ID);
        this.satellite = satellite;
        this.setName(DEFAULT_NAME + satellite.getName());
        this.setAvailability(availability);
        this.satelliteOrientation = satellite.getOrientation();
        this.satelliteAttitudes = satellite.getAttitudes();
        this.states = satellite.getSpaceCraftStates();
        this.julianDates = satelliteOrientation.getJulianDates();
        this.body = body;
        final Frame frame = satellite.getFrame();

        final List<GeodeticPoint> projectedGeodeticPoints =
            this.generateProjectedGeodeticPoint(direction, frame,
                                                alwaysDisplayOnGround,
                                                satelliteAttitudes, states,
                                                satellite, body);

        this.pointOnBody =
            new PointOnBody(julianDates, projectedGeodeticPoints, body);
        final Reference satelliteReference =
            new Reference(satellite.getId() + DEFAULT_H_POSITION);
        final Reference groundReference =
            new Reference(pointOnBody.getId() + DEFAULT_H_POSITION);
        this.attitudePointingPolyline =
            Polyline.nonVectorBuilder(availability)
                .withFirstReference(satelliteReference)
                .withSecondReference(groundReference).withColor(color).build();
    }

    // Builder

    /**
     * Builder attitude pointing builder.
     *
     * @param satelliteInput the satellite input
     * @param bodyInput the body input
     * @param directionInput the direction input
     * @param availability the time interval for which the attitude pointing
     *        line is available
     * @return the attitude pointing builder
     */
    public static AttitudePointingBuilder
        builder(final Spacecraft satelliteInput,
                final OneAxisEllipsoid bodyInput, final Vector3D directionInput,
                final TimeInterval availability) {
        return new AttitudePointingBuilder(satelliteInput, bodyInput,
                                           directionInput, availability);
    }

    // Overrides

    @Override
    public void writeCzmlBlock(final CesiumStreamWriter stream,
                               final CesiumOutputStream output)
        throws URISyntaxException,
            IOException {
        if (displayPointingPath) {
            this.pointOnBody.setDisplayPath(true);
            if (displayPeriodPointingPath) {
                this.pointOnBody
                    .setDisplayPeriodPointingPath(true, satellite.getPeriod());
                if (periodPointingPath != 0.0) {
                    this.pointOnBody.setPeriodForPath(periodPointingPath);
                }
            }
        }
        output.setPrettyFormatting(true);
        pointOnBody.writeCzmlBlock(stream, output);
        try (PacketCesiumWriter packet = stream.openPacket(output)) {
            packet.writeId(getId());
            packet.writeName(getName());
            packet.writeAvailability(getAvailability());
            attitudePointingPolyline.writeReferencesPolyline(packet, output);
        }
    }

    // Display methods

    /**
     * This method allows displaying the path of the attitude pointing.
     */
    public void displayPointingPath() {
        this.displayPointingPath = true;
    }

    /**
     * This method allows displaying the path with a given period.
     */
    public void displayPeriodPointingPath() {
        this.displayPeriodPointingPath = true;
        if (!displayPointingPath) {
            throw new OreCzmlException(OreCzmlMessages.POINTING_PATH_NOT_SHOWN);
        }
    }

    // Getters

    /**
     * Gets satellite.
     *
     * @return the satellite
     */
    public Spacecraft getSatellite() {
        return satellite;
    }

    /**
     * Gets julian dates.
     *
     * @return the julian dates
     */
    public List<JulianDate> getJulianDates() {
        return Collections.unmodifiableList(julianDates);
    }

    /**
     * Gets body.
     *
     * @return the body
     */
    public OneAxisEllipsoid getBody() {
        return body;
    }

    /**
     * Is display period pointing path boolean.
     *
     * @return the boolean
     */
    public boolean isDisplayPeriodPointingPath() {
        return displayPeriodPointingPath;
    }

    /**
     * Sets display period pointing path.
     *
     * @param periodPointingPathInput the period pointing path input
     */
    public void
        setDisplayPeriodPointingPath(final double periodPointingPathInput) {
        if (!displayPeriodPointingPath) {
            throw new OreCzmlException(OreCzmlMessages.PERIOD_POINTING_PATH_NOT_SHOWN);
        }
        this.periodPointingPath = periodPointingPathInput;
    }

    // Setters

    /**
     * Gets period pointing path.
     *
     * @return the period pointing path
     */
    public double getPeriodPointingPath() {
        return periodPointingPath;
    }

    // Private functions

    private List<GeodeticPoint>
        generateProjectedGeodeticPoint(final Vector3D directionInput,
                                       final Frame frameInput,
                                       final boolean alwaysDisplayOnGroundInput,
                                       final List<Attitude> satelliteAttitudesInput,
                                       final List<SpacecraftState> statesInput,
                                       final Spacecraft satelliteInput,
                                       final OneAxisEllipsoid bodyInput) {

        final List<GeodeticPoint> toReturn = new ArrayList<>();
        for (int i = 0; i < satelliteAttitudesInput.size(); i++) {
            final SpacecraftState state = statesInput.get(i);
            final Attitude currentAttitude = state.getAttitude();
            final Rotation currentRotation = currentAttitude.getRotation();
            final AbsoluteDate currentDate = state.getDate();
            final Vector3D origin = state.getPosition();
            final Vector3D inputDirection =
                currentRotation.applyInverseTo(directionInput);
            final Vector3D closestToGround =
                bodyInput.projectToGround(origin, currentDate, frameInput);
            final Line currentLine =
                Line.fromDirection(origin, inputDirection, 1.0);
            final GeodeticPoint intersectionGeodetic =
                bodyInput.getIntersectionPoint(currentLine, closestToGround,
                                               frameInput, currentDate);
            if (alwaysDisplayOnGroundInput && intersectionGeodetic == null) {
                final Vector3D projectedVector3D =
                    bodyInput.projectToGround(origin, currentDate,
                                              satelliteInput.getFrame());
                final GeodeticPoint substitutePoint =
                    bodyInput.transform(projectedVector3D, state.getFrame(),
                                        currentDate);
                toReturn.add(substitutePoint);
            } else {
                toReturn.add(intersectionGeodetic);
            }
        }
        return toReturn;
    }
}
