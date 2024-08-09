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
import cesiumlanguagewriter.PathCesiumWriter;
import cesiumlanguagewriter.PositionCesiumWriter;
import org.hipparchus.geometry.euclidean.threed.Rotation;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.orekit.attitudes.Attitude;
import org.orekit.bodies.CelestialBody;
import org.orekit.czml.CzmlObjects.CzmlAbstractObjects.CzmlModel;
import org.orekit.czml.CzmlObjects.CzmlSecondaryObjects.Orientation;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.frames.Transform;
import org.orekit.time.AbsoluteDate;
import org.orekit.utils.IERSConventions;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Body display class
 *
 * <p> This class aims at displaying bodies except the earth. These bodies cannot be seen if too far away.</p>
 *
 * @author Julien LEBLOND
 * @since 1.0.0
 */

public class BodyDisplay extends AbstractPrimaryObject implements CzmlPrimaryObject {

    /** . */
    public static final String DEFAULT_ID   = "BODY/";
    /** . */
    public static final String DEFAULT_NAME = "Celestial body : ";

    /** . */
    private List<Cartesian> cartesianList;

    /** . */
    private CelestialBody body;

    /** . */
    private CzmlModel model;

    /** . */
    private String pathToModel;

    /** . */
    private List<JulianDate> allJulianDatesSimulation;

    /** . */
    private boolean displayOrbit = true;

    /** . */
    private boolean displayOnlyOnePeriod = false;

    /** . */
    private double periodForPath;

    /** . */
    private double modelScale = 0.0;

    /** . */
    private double modelMinimumPixelSize = 0.0;

    /** . */
    private double modelMaximumScale = 0.0;

    /** . */
    private Orientation orientation;

    public BodyDisplay(final CelestialBody body, final String pathToModel) throws URISyntaxException, IOException {

        this.setId(DEFAULT_ID + body.getName());
        this.setName(DEFAULT_NAME + body.getName());
        this.setAvailability(Header.getMasterClock()
                                   .getAvailability());

        this.pathToModel              = pathToModel;
        this.model                    = new CzmlModel(pathToModel);
        this.allJulianDatesSimulation = Header.getMasterClock()
                                              .getAllJulianDatesSimulation();

        this.cartesianList = fillCartesian(body, allJulianDatesSimulation);
        this.orientation = generateOrientation(body, allJulianDatesSimulation);
    }

    @Override
    public void writeCzmlBlock() throws URISyntaxException, IOException {
        OUTPUT.setPrettyFormatting(true);
        try (PacketCesiumWriter packet = STREAM.openPacket(OUTPUT)) {
            packet.writeId(getId());
            packet.writeName(getName());
            packet.writeAvailability(getAvailability());

            writePosition(packet);
            writeModel(packet);

            if (displayOrbit) {
                writePath(packet);
            }
            this.orientation.write(packet, OUTPUT);
        }
    }

    @Override
    public StringWriter getStringWriter() {
        return STRING_WRITER;
    }

    @Override
    public void cleanObject() {
        cartesianList            = new ArrayList<>();
        body                     = null;
        model                    = null;
        pathToModel              = "";
        allJulianDatesSimulation = new ArrayList<>();
        displayOrbit             = true;
    }

    // GETTERS

    public List<Cartesian> getCartesianList() {
        return cartesianList;
    }

    public CelestialBody getBody() {
        return body;
    }

    public CzmlModel getModel() {
        return model;
    }

    public String getPathToModel() {
        return pathToModel;
    }

    public List<JulianDate> getAllJulianDatesSimulation() {
        return allJulianDatesSimulation;
    }

    public boolean isDisplayOrbit() {
        return displayOrbit;
    }

    public boolean isDisplayOnlyOnePeriod() {
        return displayOnlyOnePeriod;
    }

    public double getPeriodForPath() {
        return periodForPath;
    }

    public void noOrbitDisplay() {
        displayOrbit = false;
    }

    public void displayOnlyOnePeriod(final double periodInput) {
        if (displayOrbit) {
            displayOnlyOnePeriod = true;
            this.periodForPath   = periodInput;
        } else {
            throw new RuntimeException(
                    "The orbit is not displayed, do not use noOrbitDisplay() then setup the period display of the orbit.");
        }
    }


    // Setters

    public BodyDisplay withModelMaximumScale(final double modelMaximumScaleInput) {
        this.modelMaximumScale = modelMaximumScaleInput;
        return this;
    }

    public BodyDisplay withModelMinimumPixelSize(final double modelMinimumPixelSizeInput) {
        this.modelMinimumPixelSize = modelMinimumPixelSizeInput;
        return this;
    }

    public BodyDisplay withModelScale(final double modelScaleInput) {
        this.modelScale = modelScaleInput;
        return this;
    }

    public BodyDisplay withOrientation(final Orientation orientationInput) {
        this.orientation = orientationInput;
        return this;
    }

    // Private functions

    private void writePosition(final PacketCesiumWriter packet) {
        try (PositionCesiumWriter positionWriter = packet.getPositionWriter()) {
            positionWriter.open(OUTPUT);
            positionWriter.writeInterval(this.getAvailability());
            positionWriter.writeCartesian(allJulianDatesSimulation, cartesianList);
        }
    }

    private void writeModel(final PacketCesiumWriter packet) throws URISyntaxException, IOException {
        if (modelScale != 0.0 && modelMaximumScale != 0.0 && modelMinimumPixelSize != 0.0) {
            this.model = new CzmlModel(pathToModel, modelMaximumScale, modelMinimumPixelSize, modelScale);
        }
        model.generateCZML(packet, OUTPUT);
    }

    private void writePath(final PacketCesiumWriter packet) {
        try (PathCesiumWriter pathWriter = packet.getPathWriter()) {
            pathWriter.open(OUTPUT);
            pathWriter.writeShowProperty(true);
            pathWriter.writeInterval(Header.getMasterClock()
                                           .getAvailability());
            if (displayOnlyOnePeriod) {
                pathWriter.writeTrailTimeProperty(0.0);
                pathWriter.writeLeadTimeProperty(this.periodForPath);
            }
        }
    }

    private List<Cartesian> fillCartesian(final CelestialBody bodyInput, final List<JulianDate> julianDates) {
        final Frame           ITRF     = FramesFactory.getITRF(IERSConventions.IERS_2010, true);
        final List<Cartesian> toReturn = new ArrayList<>();
        for (JulianDate julianDate : julianDates) {
            final AbsoluteDate date            = julianDateToAbsoluteDate(julianDate, Header.getTimeScale());
            final Vector3D     currentPosition = bodyInput.getPosition(date, ITRF);
            final Cartesian currentCartesian = new Cartesian(currentPosition.getX(), currentPosition.getY(),
                    currentPosition.getZ());
            toReturn.add(currentCartesian);
        }
        return toReturn;
    }

    private Orientation generateOrientation(final CelestialBody bodyInput, final List<JulianDate> julianDates) {
        final List<Attitude> attitudes     = new ArrayList<>();
        final Frame          bodyInertialFrame = bodyInput.getInertiallyOrientedFrame();
        final Frame          bodyRotatingFrame   = bodyInput.getBodyOrientedFrame();

        for (JulianDate julianDate : julianDates) {
            final AbsoluteDate date             = julianDateToAbsoluteDate(julianDate, Header.getTimeScale());
            final Transform    currentTransform = bodyRotatingFrame.getTransformTo(bodyInertialFrame, date);
            final Rotation     currentRotation  = currentTransform.getRotation();
            final Attitude currentAttitudeBody = new Attitude(date, bodyRotatingFrame, currentRotation, Vector3D.ZERO,
                    Vector3D.ZERO);
            attitudes.add(currentAttitudeBody);
        }
        return new Orientation(attitudes, bodyRotatingFrame, false);
    }
}
