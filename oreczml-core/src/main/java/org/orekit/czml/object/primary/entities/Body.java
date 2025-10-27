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

package org.orekit.czml.object.primary.entities;

import cesiumlanguagewriter.Cartesian;
import cesiumlanguagewriter.CesiumOutputStream;
import cesiumlanguagewriter.CesiumStreamWriter;
import cesiumlanguagewriter.JulianDate;
import cesiumlanguagewriter.PacketCesiumWriter;
import cesiumlanguagewriter.PathCesiumWriter;
import cesiumlanguagewriter.PositionCesiumWriter;
import org.hipparchus.geometry.euclidean.threed.Rotation;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.orekit.attitudes.Attitude;
import org.orekit.bodies.CelestialBody;
import org.orekit.czml.errors.OreCzmlException;
import org.orekit.czml.errors.OreCzmlMessages;
import org.orekit.czml.object.nonvisual.CzmlModel;
import org.orekit.czml.object.primary.AbstractPrimaryObject;
import org.orekit.czml.object.secondary.Clock;
import org.orekit.czml.object.secondary.Orientation;
import org.orekit.czml.object.utils.DateUtils;
import org.orekit.frames.Frame;
import org.orekit.frames.Transform;
import org.orekit.time.AbsoluteDate;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Body class
 * <p>
 * This class aims at displaying bodies except the earth. These bodies cannot be
 * seen if too far away.
 * </p>
 *
 * @author Julien LEBLOND
 * @since 1.0.0
 */
public class Body
    extends
    AbstractPrimaryObject<Body> {

    /**
     * The default id of the body object.
     */
    public static final String DEFAULT_ID = "BODY/";

    /**
     * The default name of the body object.
     */
    public static final String DEFAULT_NAME = "Celestial body : ";

    /**
     * The list of cartesian that represents the position of the object.
     */
    private final List<Cartesian> cartesianList;

    /**
     * The body represented.
     */
    private final CelestialBody body;

    /** The central body associated to the body. */
    private final Body centralBody;

    /** The 3D model loaded to display the body. */
    private CzmlModel model;

    /**
     * The path to the model.
     */
    private final String pathToModel;

    /**
     * The list of julian dates when the body must be displayed.
     */
    private final List<JulianDate> julianDatesSimulation;

    /**
     * To display or not the orbit of the body.
     */
    private boolean displayOrbit = true;

    /**
     * To display or not one period of the orbit of the body.
     */
    private boolean displayOnlyOnePeriod = false;

    /**
     * The period of the path of the orbit.
     */
    private double periodForPath;

    /**
     * The scale of the model loaded.
     */
    private double modelScale = 0.0;

    /**
     * The minimum pixel size of the model.
     */
    private double modelMinimumPixelSize = 0.0;

    /**
     * The maximum scale to which the model can be displayed.
     */
    private double modelMaximumScale = 0.0;

    /**
     * The orientation of the model.
     */
    private Orientation orientation;

    /** The description of the body selected. */
    private String description;

    /** The influence sphere of the body if one is asked. */
    private InfluenceSphere influenceSphere;

    /** To display the influence sphere or not. */
    private boolean displayInfluenceSphere = false;

    /** The clock considered. */
    private final Clock clock;

    /** The frame to use for the body. */
    private final Frame frame;

    // Constructors

    /**
     * The body constructor.
     *
     * @param body : The body to display.
     * @param pathToModel : The path to the model to load.
     * @param frameToExpress : The frame to use.
     * @param clock : The clock
     * @param centralBody : The central Body associated to the body
     */
    Body(final CelestialBody body, final String pathToModel,
         final Frame frameToExpress, final Clock clock,
         final Body centralBody) {
        this(body, pathToModel, frameToExpress, DEFAULT_ID + body.getName(),
             clock, centralBody);
    }

    /**
     * The body with custom ID argument.
     *
     * @param body : The body to display.
     * @param pathToModel : The path to the model to load.
     * @param frameToExpressInput : The frame to use
     * @param customID : The custom ID for the body.
     * @param clock : The clock
     * @param centralBody : The central body associated to the body
     */
    Body(final CelestialBody body, final String pathToModel,
         final Frame frameToExpressInput, final String customID,
         final Clock clock, final Body centralBody) {

        this.setId(customID);
        this.setName(DEFAULT_NAME + body.getName());
        this.setAvailability(clock.getAvailability());
        this.body = body;
        this.frame = frameToExpressInput;
        this.description =
            "<!--HTML-->\r\n<p>Id : " +
                           customID + "</p>\r\n<p>Name : " + body.getName() +
                           "</p>\r\n<p>Simulated from : " +
                           getAvailability().getStart() + " to " +
                           getAvailability().getStop() + "</p>";
        this.pathToModel = pathToModel;
        this.model = new CzmlModel(pathToModel, false, clock);
        this.julianDatesSimulation = clock.getJulianDatesSimulation();
        this.clock = clock;
        this.centralBody = centralBody;

        this.cartesianList =
            fillCartesian(body, julianDatesSimulation, frameToExpressInput);
        this.orientation = generateOrientation(body, julianDatesSimulation);
    }

    // Builders

    /**
     * Builder body builder.
     *
     * @param body the body
     * @param pathToModel the path to model
     * @param frameToExpressInput the path to model
     * @param clock the clock
     * @param centralBody the central body
     * @return the body builder
     */
    public static BodyBuilder
        builder(final CelestialBody body, final String pathToModel,
                final Frame frameToExpressInput, final Clock clock,
                final Body centralBody) {
        return new BodyBuilder(body, pathToModel, frameToExpressInput, clock,
                               centralBody);
    }

    // Overrides

    @Override
    public void writeCzmlBlock(final CesiumStreamWriter stream,
                               final CesiumOutputStream output)
        throws URISyntaxException,
            IOException {
        output.setPrettyFormatting(true);
        try (PacketCesiumWriter packet = stream.openPacket(output)) {
            packet.writeId(getId());
            packet.writeName(getName());
            packet.writeAvailability(getAvailability());

            writePosition(packet, output);
            writeModel(packet, output);

            if (displayOrbit) {
                writePath(packet, output);
            }
            this.orientation.write(packet, output);
        }
        if (displayInfluenceSphere) {
            this.influenceSphere.writeCzmlBlock(stream, output);
        }
    }

    @Override
    public Body cloneObject() {
        final Body copy =
            Body.builder(this.body, this.pathToModel, this.frame, this.clock,
                         this.centralBody)
                .withDescription(this.description)
                .withOrientation(this.orientation)
                .withModelScale(this.modelScale)
                .withModelMaximumScale(this.modelMaximumScale)
                .withCustomID(getId())
                .withModelMinimumPixelSize(this.modelMinimumPixelSize).build();
        copy.setName(getName());
        copy.setAvailability(getAvailability());
        return copy;
    }

    // Users' methods

    /**
     * Function to display the influence sphere of the body.
     */
    public void displayInfluenceSphere() {
        this.influenceSphere =
            InfluenceSphere.builder(this, this.clock).build();
        this.displayInfluenceSphere = true;
    }

    // GETTERS

    /**
     * Gets cartesian list.
     *
     * @return the cartesian list
     */
    public List<Cartesian> getCartesianPositionList() {
        return Collections.unmodifiableList(cartesianList);
    }

    /**
     * Gets body.
     *
     * @return the body
     */
    public CelestialBody getCelestialBody() {
        return body;
    }

    public InfluenceSphere getInfluenceSphere() {
        if (influenceSphere == null) {
            throw new OreCzmlException(OreCzmlMessages.INFLUENCE_SPHERE_NOT_DISPLAYED);
        } else {
            return influenceSphere.cloneObject();
        }
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
     * Gets the central body.
     *
     * @return The central body
     */
    public Body getCentralBody() {
        return centralBody.cloneObject();
    }

    /**
     * Is display orbit boolean.
     *
     * @return the boolean
     */
    public boolean isDisplayOrbit() {
        return displayOrbit;
    }

    /**
     * Is display only one period boolean.
     *
     * @return the boolean
     */
    public boolean isDisplayOnlyOnePeriod() {
        return displayOnlyOnePeriod;
    }

    /**
     * No orbit display.
     */
    public void noOrbitDisplay() {
        displayOrbit = false;
    }

    /**
     * Display only one period.
     *
     * @param periodInput the period input
     */
    public void displayOnlyOnePeriod(final double periodInput) {
        if (displayOrbit) {
            displayOnlyOnePeriod = true;
            this.periodForPath = periodInput;
        } else {
            throw new OreCzmlException(OreCzmlMessages.CANT_DISPLAY_PERIOD_NO_ORBIT);
        }
    }

    // Setters (looks like a builder to make it easier for the BodyFactory).

    /**
     * With model maximum scale body.
     *
     * @param modelMaximumScaleInput the model maximum scale input
     * @return the body
     */
    public Body withModelMaximumScale(final double modelMaximumScaleInput) {
        this.modelMaximumScale = modelMaximumScaleInput;
        return this;
    }

    /**
     * With model minimum pixel size body.
     *
     * @param modelMinimumPixelSizeInput the model minimum pixel size input
     * @return the body
     */
    public Body
        withModelMinimumPixelSize(final double modelMinimumPixelSizeInput) {
        this.modelMinimumPixelSize = modelMinimumPixelSizeInput;
        return this;
    }

    /**
     * With model scale body.
     *
     * @param modelScaleInput the model scale input
     * @return the body
     */
    public Body withModelScale(final double modelScaleInput) {
        this.modelScale = modelScaleInput;
        return this;
    }

    /**
     * With orientation body.
     *
     * @param orientationInput the orientation input
     * @return the body
     */
    public Body withOrientation(final Orientation orientationInput) {
        this.orientation = orientationInput;
        return this;
    }

    /**
     * With description body.
     *
     * @param descriptionInput : The description input
     * @return the body
     */
    public Body withDescription(final String descriptionInput) {
        this.description = descriptionInput;
        return this;
    }

    // Private functions

    /**
     * This functions aims at writing the position of the body in a given
     * packet.
     *
     * @param packet : The packet to write into the czml file.
     * @param output : The output stream of cesium that will contain the strings
     *        to write into the CzmLFile.
     */
    private void writePosition(final PacketCesiumWriter packet,
                               final CesiumOutputStream output) {
        try (PositionCesiumWriter positionWriter = packet.getPositionWriter()) {
            positionWriter.open(output);
            positionWriter.writeInterval(this.getAvailability());
            positionWriter.writeCartesian(julianDatesSimulation, cartesianList);
        }
    }

    /**
     * This functions aims at writing the model of the body in a given packet.
     *
     * @param packet : The packet to write into the czml file.
     * @param output : The output stream of cesium that will contain the strings
     *        to write into the CzmLFile.
     */
    private void writeModel(final PacketCesiumWriter packet,
                            final CesiumOutputStream output)
        throws URISyntaxException,
            IOException {
        if (modelScale != 0.0 &&
            modelMaximumScale != 0.0 && modelMinimumPixelSize != 0.0) {
            this.model =
                new CzmlModel(pathToModel, modelMaximumScale,
                              modelMinimumPixelSize, modelScale, false, clock);
        }
        model.generateCZML(packet, output);
    }

    /**
     * This functions aims at writing the path of the body in a given packet.
     *
     * @param packet : The packet to write into the czml file.
     * @param output : The output stream of cesium that will contain the strings
     *        to write into the CzmLFile.
     */
    private void writePath(final PacketCesiumWriter packet,
                           final CesiumOutputStream output) {
        try (PathCesiumWriter pathWriter = packet.getPathWriter()) {
            pathWriter.open(output);
            pathWriter.writeShowProperty(true);
            pathWriter.writeInterval(getAvailability());
            if (displayOnlyOnePeriod) {
                pathWriter.writeTrailTimeProperty(0.0);
                pathWriter.writeLeadTimeProperty(this.periodForPath);
            }
        }
    }

    /**
     * This function aims at getting the cartesian position of a body at
     * specific julian dates.
     *
     * @param bodyInput : The body to which the cartesian are computed.
     * @param julianDates : The julian dates when the cartesian must be
     *        computed.
     * @param frameToExpress : The frame the body is expressed in.
     * @return : The list of cartesian position of the body.
     */
    private List<Cartesian> fillCartesian(final CelestialBody bodyInput,
                                          final List<JulianDate> julianDates,
                                          final Frame frameToExpress) {
        final List<Cartesian> toReturn = new ArrayList<>();
        for (JulianDate julianDate : julianDates) {
            final AbsoluteDate date = DateUtils.toAbsoluteDate(julianDate);
            final Vector3D currentPosition =
                bodyInput.getPosition(date, frameToExpress);
            final Cartesian currentCartesian =
                new Cartesian(currentPosition.getX(), currentPosition.getY(),
                              currentPosition.getZ());
            toReturn.add(currentCartesian);
        }
        return toReturn;
    }

    /**
     * This functions aims at computing the orientation in time of the body,
     * because the major part of bodies computed rotate around themselves, we
     * need to take this into account when computing the orientation.
     *
     * @param bodyInput : The body to which the orientation must be computed.
     * @param julianDates : The julian dates when the orientation must be
     *        computed.
     * @return : The orientation of the body in time.
     */
    private Orientation
        generateOrientation(final CelestialBody bodyInput,
                            final List<JulianDate> julianDates) {
        final List<Attitude> attitudes = new ArrayList<>();
        final Frame bodyInertialFrame = bodyInput.getInertiallyOrientedFrame();
        final Frame bodyRotatingFrame = bodyInput.getBodyOrientedFrame();

        for (JulianDate julianDate : julianDates) {
            final AbsoluteDate date = DateUtils.toAbsoluteDate(julianDate);
            final Transform currentTransform =
                bodyRotatingFrame.getTransformTo(bodyInertialFrame, date);
            final Rotation currentRotation = currentTransform.getRotation();
            final Attitude currentAttitudeBody =
                new Attitude(date, bodyRotatingFrame, currentRotation,
                             Vector3D.ZERO, Vector3D.ZERO);
            attitudes.add(currentAttitudeBody);
        }
        return Orientation.builder(attitudes, bodyRotatingFrame)
            .withInvertToITRF(false).build();
    }
}
