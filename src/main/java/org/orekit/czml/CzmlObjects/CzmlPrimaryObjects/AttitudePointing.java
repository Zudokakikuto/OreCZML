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
import org.hipparchus.util.FastMath;
import org.orekit.attitudes.Attitude;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.czml.CzmlObjects.CzmlSecondaryObjects.Orientation;
import org.orekit.czml.CzmlObjects.Polyline;
import org.orekit.frames.Frame;
import org.orekit.propagation.SpacecraftState;
import org.orekit.time.AbsoluteDate;
import org.orekit.utils.Constants;

import java.awt.Color;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class AttitudePointing extends AbstractPrimaryObject implements CzmlPrimaryObject {

    /** .*/
    public static final String DEFAULT_NAME = "Attitude pointing of : ";
    /** .*/
    public static final String DEFAULT_ID = "ATTITUDE_POINTING/";
    /** .*/
    public static final String DEFAULT_H_POSITION = "#position";
    /** .*/
    public static final Color DEFAULT_COLOR = Color.GREEN;

    /** .*/
    private Orientation satelliteOrientation;
    /** .*/
    private Satellite satellite;
    /** .*/
    private boolean displayPeriodPointingPath = false;
    /** .*/
    private Polyline attitudePointingPolyline;
    /** .*/
    private OneAxisEllipsoid body;
    /** .*/
    private List<Line> lines = new ArrayList<>();
    /** .*/
    private List<GeodeticPoint> projectedAttitudes = new ArrayList<>();
    /** .*/
    private List<JulianDate> julianDates = new ArrayList<>();
    /** .*/
    private AbstractPointOnBody pointOnBody;
    /** .*/
    private boolean displayPointingPath = false;
    /** .*/
    private List<Attitude> satelliteAttitudes = new ArrayList<>();
    /** .*/
    private List<Cartesian> satelliteCartesians = new ArrayList<>();

    public AttitudePointing(final Satellite satellite, final OneAxisEllipsoid body, final Vector3D direction) {
        this(satellite, body, direction, Header.MASTER_CLOCK.getAvailability());
    }

    public AttitudePointing(final Satellite satellite, final OneAxisEllipsoid body, final Vector3D direction, final TimeInterval availability) {
        this(satellite, body, direction, availability, DEFAULT_COLOR);
    }

    public AttitudePointing(final Satellite satellite, final OneAxisEllipsoid body, final Vector3D direction, final Color color) {
        this(satellite, body, direction, Header.MASTER_CLOCK.getAvailability(), color);
    }

    public AttitudePointing(final Satellite satellite, final OneAxisEllipsoid body, final Vector3D direction, final TimeInterval availability, final Color color) {
        this(satellite, body, direction, availability, color, false);
    }

    public AttitudePointing(final Satellite satellite, final OneAxisEllipsoid body, final Vector3D direction, final TimeInterval availability, final Color color, final boolean alwaysDisplayOnGround) {
        this.setId(DEFAULT_ID + satellite.getId());
        this.satellite = satellite;
        this.setName(DEFAULT_NAME + satellite.getName());
        this.setAvailability(availability);
        this.satelliteOrientation = satellite.getOrientation();
        this.satelliteAttitudes = satellite.getAttitudes();
        this.satelliteCartesians = satellite.getCartesianArraylist();
        this.julianDates = satelliteOrientation.getJulianDates();
        this.body = body;
        final Frame currentFrame = satellite.getFrame();
        final List<SpacecraftState> states = satellite.getAllSpaceCraftStates();

        for (int i = 0; i < satelliteAttitudes.size(); i++) {
            final SpacecraftState state = states.get(i);
            final Cartesian currentCartesian = satelliteCartesians.get(i);
            final Vector3D currentSatellitePosition = state.getPosition();
            final Attitude currentAttitude = state.getAttitude();
            final Rotation currentRotation = currentAttitude.getRotation();
            final AbsoluteDate currentDate = state.getDate();
            final Vector3D origin = new Vector3D(currentCartesian.getX(), currentCartesian.getY(), currentCartesian.getZ());
            final Vector3D inputDirection = currentRotation.applyInverseTo(direction);
            final Vector3D closestToGround = body.projectToGround(origin, currentDate, currentFrame);
            final Line currentLine = Line.fromDirection(origin, inputDirection, 1.0);
            lines.add(currentLine);
            final GeodeticPoint intersectionGeodetic = body.getIntersectionPoint(currentLine, closestToGround, currentFrame, currentDate);
            if (alwaysDisplayOnGround && intersectionGeodetic == null) {
                final Vector3D projectedVector3D = body.projectToGround(currentSatellitePosition, currentDate, satellite.getFrame());
                final GeodeticPoint substitutePoint = body.transform(projectedVector3D, state.getFrame(), currentDate);
                projectedAttitudes.add(substitutePoint);
            }
            else {
                projectedAttitudes.add(intersectionGeodetic);
            }
        }
        this.pointOnBody = new AbstractPointOnBody(julianDates, projectedAttitudes, body);
        final Reference satelliteReference = new Reference(satellite.getId() + DEFAULT_H_POSITION);
        final Reference groundReference = new Reference(pointOnBody.getId() + DEFAULT_H_POSITION);
        this.attitudePointingPolyline = new Polyline(satelliteReference, groundReference, color);
    }

    public void displayPointingPath() {
        this.displayPointingPath = true;
    }

    public void displayPeriodPointingPath() {
        this.displayPeriodPointingPath = true;
        if (!displayPointingPath) {
            throw new RuntimeException("The pointing path is not displayed yet, use displayPointingPath first");
        }
    }

    @Override
    public void writeCzmlBlock() throws URISyntaxException, IOException {
        if (displayPointingPath) {
            this.pointOnBody.setDisplayPath(true);
            if (displayPeriodPointingPath) {
                this.pointOnBody.setDisplayPeriodPointingPath(true, satellite.getPeriod());
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
        satelliteOrientation = null;
        attitudePointingPolyline = null;
        body = null;
        lines = new ArrayList<>();
        projectedAttitudes = new ArrayList<>();
        julianDates = new ArrayList<>();
        pointOnBody = null;
    }

    public Satellite getSatellite() {
        return satellite;
    }

    public Orientation getSatelliteOrientation() {
        return satelliteOrientation;
    }

    public List<GeodeticPoint> getProjectedAttitudes() {
        return projectedAttitudes;
    }

    public List<JulianDate> getJulianDates() {
        return julianDates;
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
        return satelliteCartesians;
    }

    public List<Attitude> getSatelliteAttitudes() {
        return satelliteAttitudes;
    }
}

