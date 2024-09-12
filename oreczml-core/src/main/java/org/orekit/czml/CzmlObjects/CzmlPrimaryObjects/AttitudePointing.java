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

import cesiumlanguagewriter.Cartesian;
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
import org.orekit.czml.ArchiObjects.Builders.AttitudePointingBuilder;
import org.orekit.czml.ArchiObjects.Exceptions.OreCzmlExceptions;
import org.orekit.czml.CzmlObjects.CzmlAbstractObjects.AbstractPointOnBody;
import org.orekit.czml.CzmlObjects.CzmlSecondaryObjects.Orientation;
import org.orekit.czml.CzmlObjects.Polyline;
import org.orekit.frames.Frame;
import org.orekit.propagation.SpacecraftState;
import org.orekit.time.AbsoluteDate;

import java.awt.Color;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Attitude pointing class
 *
 * <p>
 * The attitude pointing represents a line that will go towards the central body to project the attitude of the satellite at the surface.
 * A given direction will be needed to know which axis the object will project.
 * </p>
 *
 * @author Julien LEBLOND.
 * @since 1.0.0
 */

public class AttitudePointing extends AbstractPrimaryObject implements CzmlPrimaryObject {

    /** The default ID of the attitude-pointing object. */
    public static final String DEFAULT_ID = "ATTITUDE_POINTING/";

    /** The default name of the attitude-pointing object. */
    public static final String DEFAULT_NAME = "Attitude pointing of : ";

    /** This can be used to create a reference in the position of an object. */
    public static final String DEFAULT_H_POSITION = "#position";

    /** The default color of the line. */
    public static final Color DEFAULT_COLOR = Color.GREEN;

    /** The satellite which attitude will be pointed. */
    private final Satellite satellite;
    /** The list of the attitude of the satellite. */
    private final List<Attitude> satelliteAttitudes;
    /** The list of the position in cartesian of the satellite. */
    private final List<Cartesian> satelliteCartesians;
    /** The central body around which the satellite is orbiting. */
    private OneAxisEllipsoid body;
    /** The orientation of the satellite. */
    private Orientation satelliteOrientation;
    /** The line to be displayed. */
    private Polyline attitudePointingPolyline;
    /** The list of the projected attitude that gives points on the body. */
    private List<GeodeticPoint> projectedAttitudes = new ArrayList<>();
    /** The list of the julian dates when the line should be displayed. */
    private List<JulianDate> julianDates;
    /** The abstract point on body builds from the projected attitudes. */
    private AbstractPointOnBody pointOnBody;
    /** To display or not the path of the attitude pointing. */
    private boolean displayPointingPath = false;

    /** To display or not the path with a period. */
    private boolean displayPeriodPointingPath = false;

    /** The period in seconds of the path if displayed. */
    private double periodPointingPath = 0.0;


    // Constructors

    /**
     * The constructor of the attitude-pointing object with default parameters.
     *
     * @param satellite : The satellite that will point to the body.
     * @param body      : The body to point to.
     * @param direction : The direction to point to.
     */
    public AttitudePointing(final Satellite satellite, final OneAxisEllipsoid body, final Vector3D direction) {
        this(satellite, body, direction, Header.getMasterClock()
                                               .getAvailability(), DEFAULT_COLOR, false);
    }

    /**
     * The constructor with no default parameters.
     *
     * @param satellite             : The satellite that will point to the body.
     * @param body                  : The body to point to.
     * @param direction             : The direction to point to.
     * @param availability          : The time interval when the attitude pointing object will be displayed.
     * @param color                 : The color of the pointing (polyline).
     * @param alwaysDisplayOnGround : Director that manages the pointing or not at objects during the orbit. Put this
     *                              parameter on if the satellite is pointing at objects during the orbit. This boolean will project the attitude on
     *                              the ground when it is not pointing at objects. When the attitude is pointing at objects, it will put the
     *                              projection on the pointed object
     * @see AttitudeTuto.AttitudePathAlongOrbit
     */

    public AttitudePointing(final Satellite satellite, final OneAxisEllipsoid body, final Vector3D direction,
                            final TimeInterval availability, final Color color, final boolean alwaysDisplayOnGround) {
        this.setId(DEFAULT_ID + satellite.getId());
        this.satellite = satellite;
        this.setName(DEFAULT_NAME + satellite.getName());
        this.setAvailability(availability);
        this.satelliteOrientation = satellite.getOrientation();
        this.satelliteAttitudes   = satellite.getAttitudes();
        this.satelliteCartesians  = satellite.getCartesianArraylist();
        this.julianDates          = satelliteOrientation.getJulianDates();
        this.body                 = body;
        final Frame                 currentFrame = satellite.getFrame();
        final List<SpacecraftState> states       = satellite.getAllSpaceCraftStates();

        for (int i = 0; i < satelliteAttitudes.size(); i++) {
            final SpacecraftState state                    = states.get(i);
            final Cartesian       currentCartesian         = satelliteCartesians.get(i);
            final Vector3D        currentSatellitePosition = state.getPosition();
            final Attitude        currentAttitude          = state.getAttitude();
            final Rotation        currentRotation          = currentAttitude.getRotation();
            final AbsoluteDate    currentDate              = state.getDate();
            final Vector3D origin = new Vector3D(currentCartesian.getX(),
                    currentCartesian.getY(), currentCartesian.getZ());
            final Vector3D inputDirection  = currentRotation.applyInverseTo(direction);
            final Vector3D closestToGround = body.projectToGround(origin, currentDate, currentFrame);
            final Line     currentLine     = Line.fromDirection(origin, inputDirection, 1.0);
            final GeodeticPoint intersectionGeodetic = body.getIntersectionPoint(currentLine, closestToGround,
                    currentFrame, currentDate);
            if (alwaysDisplayOnGround && intersectionGeodetic == null) {
                final Vector3D projectedVector3D = body.projectToGround(currentSatellitePosition, currentDate,
                        satellite.getFrame());
                final GeodeticPoint substitutePoint = body.transform(projectedVector3D, state.getFrame(),
                        currentDate);
                projectedAttitudes.add(substitutePoint);
            } else {
                projectedAttitudes.add(intersectionGeodetic);
            }
        }
        this.pointOnBody = new AbstractPointOnBody(julianDates, projectedAttitudes, body);
        final Reference satelliteReference = new Reference(satellite.getId() + DEFAULT_H_POSITION);
        final Reference groundReference    = new Reference(pointOnBody.getId() + DEFAULT_H_POSITION);
        this.attitudePointingPolyline = Polyline.nonVectorBuilder()
                                                .withFirstReference(satelliteReference)
                                                .withSecondReference(groundReference)
                                                .withColor(color)
                                                .build();
    }


    // Builder

    public static AttitudePointingBuilder builder(final Satellite satelliteInput, final OneAxisEllipsoid bodyInput,
                                                  final Vector3D directionInput) {
        return new AttitudePointingBuilder(satelliteInput, bodyInput, directionInput);
    }


    // Overrides

    @Override
    public void writeCzmlBlock() throws URISyntaxException, IOException {
        if (displayPointingPath) {
            this.pointOnBody.setDisplayPath(true);
            if (displayPeriodPointingPath) {
                this.pointOnBody.setDisplayPeriodPointingPath(true, satellite.getPeriod());
                if (periodPointingPath != 0.0) {
                    this.pointOnBody.setPeriodForPath(periodPointingPath);
                }
            }
        }
        OUTPUT.setPrettyFormatting(true);
        pointOnBody.writeCzmlBlock();
        try (PacketCesiumWriter packet = STREAM.openPacket(OUTPUT)) {
            packet.writeId(getId());
            packet.writeName(getName());
            packet.writeAvailability(getAvailability());
            attitudePointingPolyline.writeReferencesPolyline(packet, OUTPUT);
        }
    }

    @Override
    public StringWriter getStringWriter() {
        return STRING_WRITER;
    }

    @Override
    public void cleanObject() {
        satelliteOrientation     = null;
        attitudePointingPolyline = null;
        body                     = null;
        projectedAttitudes       = new ArrayList<>();
        julianDates              = new ArrayList<>();
        pointOnBody              = null;
    }


    // Display methods

    /** This method allows displaying the path of the attitude pointing. */
    public void displayPointingPath() {
        this.displayPointingPath = true;
    }

    /** This method allows displaying the path with a given period. */
    public void displayPeriodPointingPath() {
        this.displayPeriodPointingPath = true;
        if (!displayPointingPath) {
            throw new RuntimeException(OreCzmlExceptions.POINTING_PATH_NOT_SHOWN);
        }
    }


    // Getters

    public Satellite getSatellite() {
        return satellite;
    }

    public Orientation getSatelliteOrientation() {
        return satelliteOrientation;
    }

    public List<GeodeticPoint> getProjectedAttitudes() {
        return new ArrayList<>(projectedAttitudes);
    }

    public List<JulianDate> getJulianDates() {
        return new ArrayList<>(julianDates);
    }

    public AbstractPointOnBody getPointOnBody() {
        return pointOnBody;
    }

    public Polyline getAttitudePointingPolyline() {
        return attitudePointingPolyline;
    }

    public OneAxisEllipsoid getBody() {
        return body;
    }

    public List<Cartesian> getSatelliteCartesians() {
        return new ArrayList<>(satelliteCartesians);
    }

    public List<Attitude> getSatelliteAttitudes() {
        return new ArrayList<>(satelliteAttitudes);
    }

    public boolean isDisplayPointingPath() {
        return displayPointingPath;
    }

    public boolean isDisplayPeriodPointingPath() {
        return displayPeriodPointingPath;
    }

    public void setDisplayPeriodPointingPath(final double periodPointingPathInput) {
        if (!displayPeriodPointingPath) {
            throw new RuntimeException(OreCzmlExceptions.PERIOD_POINTING_PATH_NOT_SHOWN);
        }
        this.periodPointingPath = periodPointingPathInput;
    }


    // Setters

    public double getPeriodPointingPath() {
        return periodPointingPath;
    }

}

