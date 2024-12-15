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

package org.orekit.czml.object.primary.entities;

import cesiumlanguagewriter.BooleanCesiumWriter;
import cesiumlanguagewriter.Cartesian;
import cesiumlanguagewriter.CesiumOutputStream;
import cesiumlanguagewriter.CesiumStreamWriter;
import cesiumlanguagewriter.JulianDate;
import cesiumlanguagewriter.OrientationCesiumWriter;
import cesiumlanguagewriter.PacketCesiumWriter;
import cesiumlanguagewriter.PathCesiumWriter;
import cesiumlanguagewriter.PolylineMaterialCesiumWriter;
import cesiumlanguagewriter.PositionCesiumWriter;
import cesiumlanguagewriter.SolidColorMaterialCesiumWriter;
import cesiumlanguagewriter.TimeInterval;
import org.hipparchus.geometry.euclidean.threed.Rotation;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.orekit.attitudes.Attitude;
import org.orekit.czml.errors.OreCzmlException;
import org.orekit.czml.errors.OreCzmlMessages;
import org.orekit.czml.object.ModelType;
import org.orekit.czml.object.Path;
import org.orekit.czml.object.Utils.DateUtils;
import org.orekit.czml.object.nonvisual.CzmlModel;
import org.orekit.czml.object.primary.AbstractPrimaryObject;
import org.orekit.czml.object.primary.Header;
import org.orekit.czml.object.primary.systems.SatelliteReferenceSystem;
import org.orekit.czml.object.secondary.Orientation;
import org.orekit.czml.object.secondary.TimePosition;
import org.orekit.errors.OrekitException;
import org.orekit.frames.Frame;
import org.orekit.orbits.Orbit;
import org.orekit.propagation.BoundedPropagator;
import org.orekit.propagation.Propagator;
import org.orekit.propagation.SpacecraftState;
import org.orekit.time.AbsoluteDate;

import java.awt.Color;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Satellite class.
 *
 * <p> This class represents the satellite object to be displayed. It can be build from many orekit outputs and admits
 * several functions to display or not intrinsic parameters.</p>
 *
 * <p> Each satellite object created will imply a propagation with default parameters, if you want you own parameters
 * in the propagator, you can still build a satellite with a propagator to do so.</p>
 *
 * <p> The satellite object admits also 2D and 3D models. By default builders will load a 2D image to represent the satellite,
 * but each builder is overloaded with a path to the 3D model to charge your own 2D or 3D model.</p>
 *
 * @author Julien LEBLOND
 * @since 1.0.0
 */
public class Satellite extends AbstractPrimaryObject {

    /**
     * The default model path, empty.
     */
    public static final String DEFAULT_MODEL_PATH = "";

    /**
     * The default id of the satellite.
     */
    public static final String DEFAULT_ID = "SAT/";

    /**
     * The default name of the satellite.
     */
    public static final String DEFAULT_NAME = "Satellite";

    /** The default format for the formatted ID. */
    public static final String DEFAULT_FORMAT = DEFAULT_ID + "{P(%1.8e, %2.8e, %3.8e), V(%4.8e, %5.8e, %6.8e)}";

    /** The intertial frame used in the position. */
    public static final String DEFAULT_INERTIAL = "INERTIAL";

    /**
     * The default orbit color of the satellite.
     */
    public static final Color DEFAULT_COLOR = new Color(255, 255, 255);

    /**
     * If the satellite already has an attitude defined or not.
     */
    private boolean oriented = false;

    // Optional parameters
    /**
     * To display or not only one period. By default, display all the paths.
     */
    private boolean displayOnlyOnePeriod = false;

    /**
     * To display or not the attitude of the satellite. By default, the satellite is oriented in TNW in the local orbital frame.
     */
    private boolean displayAttitude = false;

    /**
     * To display or not the satellite reference system.
     */
    private boolean displayReferenceSystem = false;

    // Orekit arguments
    /**
     * The list of the attitudes of the satellite.
     */
    private List<Attitude> attitudes = new ArrayList<>();

    /**
     * The list of the spacecraft states of the satellite.
     */
    private final List<SpacecraftState> spaceCraftStates = new ArrayList<>();

    /**
     * The optional rotation applied to the attitude of the satellite.
     */
    private Rotation optionalRotation;

    /**
     * The frame in which the satellite is computed.
     */
    private final Frame frame;

    // Writing arguments
    /**
     * The period of the orbit.
     */
    private double period;

    /**
     * The orientation in the local orbital frame of the satellite.
     */
    private Orientation orientation;

    /**
     * The propagator of the satellite.
     */
    private Propagator satellitePropagator;

    /**
     * The satellite reference system object.
     */
    private SatelliteReferenceSystem satelliteReferenceSystem = null;

    /**
     * The color of the orbit.
     */
    private Color color;

    /**
     * The CzmlModel for the display of the model.
     */
    private final CzmlModel model;

    /**
     * The type of the model.
     */
    private final ModelType modelType;

    /** The start date of the propagation. */
    private final AbsoluteDate startDate;

    /** The final date of the propagation. */
    private final AbsoluteDate finalDate;

    /** The header used for the satellite. */
    private final Header header;

    /** The description of the satellite. */
    private String description;


    // Constructor

    /**
     * The basic satellite constructor, it only needs one bounded propagator. It uses default parameters.
     *
     * @param propagator : A bounded propagator resulting from an already done propagation.
     * @param header     : The header considered.
     * @throws URISyntaxException the uri syntax exception
     * @throws IOException        the io exception
     */
    public Satellite(final BoundedPropagator propagator, final Header header) throws URISyntaxException, IOException {
        this(propagator, propagator.getMinDate(), propagator.getMaxDate(), DEFAULT_MODEL_PATH, DEFAULT_COLOR,
                String.format(DEFAULT_FORMAT,
                        propagator.getInitialState()
                                  .getPosition()
                                  .getX(),
                        propagator.getInitialState()
                                  .getPosition()
                                  .getY(),
                        propagator.getInitialState()
                                  .getPosition()
                                  .getZ(),
                        propagator.getInitialState()
                                  .getPVCoordinates()
                                  .getVelocity()
                                  .getX(),
                        propagator.getInitialState()
                                  .getPVCoordinates()
                                  .getVelocity()
                                  .getY(),
                        propagator.getInitialState()
                                  .getPVCoordinates()
                                  .getVelocity()
                                  .getZ()), header);
    }

    /**
     * The satellite constructor with no default parameters.
     *
     * @param propagator     : A bounded propagator resulting from an already done propagation.
     * @param startDateInput : The start date to consider for the start of the propagation and the availability of the satellite.
     * @param finalDateInput : The stop date to consider for the stop the propagation and the availability of the satellite.
     * @param modelPath      : The path to the model to load.
     * @param color          : The color of the orbit.
     * @param customID       : The custom ID of the satellite.
     * @param header         : The header considered when several are used.
     * @throws URISyntaxException the uri syntax exception
     * @throws IOException        the io exception
     */
    public Satellite(final BoundedPropagator propagator, final AbsoluteDate startDateInput,
                     final AbsoluteDate finalDateInput, final String modelPath, final Color color,
                     final String customID, final Header header) throws URISyntaxException, IOException {

        this.setId(customID);
        this.setName(DEFAULT_NAME);
        this.setAvailability(new TimeInterval(DateUtils.toJulianDate(startDateInput, header.getTimeScale()),
                DateUtils.toJulianDate(finalDateInput, header.getTimeScale())));
        this.satellitePropagator = propagator;
        this.description         = "<!--HTML-->\r\n<p>Id : " + customID + "</p>\r\n<p>" + "Simulated from : " + startDateInput + " to " + finalDateInput + "</p>";
        this.frame               = propagator.getFrame();
        this.color               = color;
        this.model               = new CzmlModel(modelPath, true, header);
        this.modelType           = model.getModelType();
        this.startDate           = startDateInput;
        this.finalDate           = finalDateInput;
        this.header              = header;
        // Setup propagator
        multiplexerSetup(propagator);
        // Propagation
        satellitePropagator.propagate(startDate, finalDate);
        propagator.clearStepHandlers();
    }


    // Builder

    /**
     * Builder satellite builder.
     *
     * @param propagator the propagator
     * @param header     the header
     * @return the satellite builder
     */
    public static SatelliteBuilder builder(final BoundedPropagator propagator, final Header header) {
        return new SatelliteBuilder(propagator, header);
    }


    // Overrides

    @Override
    public void writeCzmlBlock(final CesiumStreamWriter stream,
                               final CesiumOutputStream output) throws URISyntaxException, IOException {

        output.setPrettyFormatting(true);
        try (PacketCesiumWriter packet = stream.openPacket(output)) {
            packet.writeId(this.getId());
            packet.writeName(getName());
            packet.writeAvailability(getAvailability());
            packet.writeDescriptionProperty(description);

            czmlDisplay(packet, stream, output);

            czmlPath(packet, output);

            czmlPosition(packet, output);

        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
        if (getDisplayReferenceSystem()) {
            getSatelliteReferenceSystem().writeCzmlBlock(stream, output);
        }
    }


    // Display functions

    /**
     * Display only one period.
     */
    public void displayOnlyOnePeriod() {
        displayOnlyOnePeriod = true;
    }

    /**
     * Display satellite attitude.
     */
    public void displaySatelliteAttitude() {
        this.displayAttitude = true;
        orientationSetup();
    }

    /**
     * Display satellite reference system.
     */
    public void displaySatelliteReferenceSystem() {
        this.displayReferenceSystem   = true;
        this.satelliteReferenceSystem = new SatelliteReferenceSystem(this, header);
    }


    // Getters

    /**
     * Gets space craft states.
     *
     * @return the space craft states
     */
    public List<SpacecraftState> getSpaceCraftStates() {
        return Collections.unmodifiableList(spaceCraftStates);
    }

    /**
     * Gets absolute date list.
     *
     * @return the absolute date list
     */
    public List<AbsoluteDate> getAbsoluteDateList() {
        final List<AbsoluteDate> toReturn = new ArrayList<>();
        for (SpacecraftState spacecraftState : spaceCraftStates) {
            toReturn.add(spacecraftState.getDate());
        }
        return Collections.unmodifiableList(toReturn);
    }

    /**
     * Gets attitudes.
     *
     * @return the attitudes
     */
    public List<Attitude> getAttitudes() {
        List<Attitude> toReturn = new ArrayList<>();
        if (attitudes.isEmpty()) {
            for (SpacecraftState spacecraftState : spaceCraftStates) {
                toReturn.add(spacecraftState.getAttitude());
            }
        } else {
            toReturn = attitudes;
        }
        return Collections.unmodifiableList(toReturn);
    }

    /**
     * Gets orbits.
     *
     * @return the orbits
     */
    public List<Orbit> getOrbits() {
        final List<Orbit> toReturn = new ArrayList<>();
        for (SpacecraftState spacecraftState : spaceCraftStates) {
            toReturn.add(spacecraftState.getOrbit());
        }
        return Collections.unmodifiableList(toReturn);
    }

    /**
     * Gets cartesian arraylist.
     *
     * @return the cartesian arraylist
     */
    public List<Cartesian> getCartesianArraylist() {
        final List<Cartesian> toReturn = new ArrayList<>();
        for (SpacecraftState spacecraftState : spaceCraftStates) {
            final Vector3D currentVector3D = spacecraftState.getPosition();
            toReturn.add(new Cartesian(currentVector3D.getX(), currentVector3D.getY(), currentVector3D.getZ()));
        }
        return Collections.unmodifiableList(toReturn);
    }

    /**
     * Gets julian dates.
     *
     * @return the julian dates
     */
    public List<JulianDate> getJulianDates() {
        return DateUtils.toJulianDates(getAbsoluteDateList(), header.getTimeScale());
    }

    /**
     * Gets satellite propagator.
     *
     * @return the satellite propagator
     */
    public Propagator getSatellitePropagator() {
        return satellitePropagator;
    }

    /**
     * Gets satellite bounded propagator.
     *
     * @return the satellite bounded propagator
     */
    public BoundedPropagator getSatelliteBoundedPropagator() {
        return (BoundedPropagator) satellitePropagator;
    }

    /**
     * Gets frame.
     *
     * @return the frame
     */
    public Frame getFrame() {
        return frame;
    }

    /**
     * Gets color.
     *
     * @return the color
     */
    public Color getColor() {
        return color;
    }

    /**
     * Gets model type.
     *
     * @return the model type
     */
    public ModelType getModelType() {
        return modelType;
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
     * Gets orientation.
     *
     * @return the orientation
     */
    public Orientation getOrientation() {
        if (orientation != null) {
            return orientation;
        } else {
            throw new OreCzmlException(OreCzmlMessages.NO_ORIENTATION_DISPLAYED);
        }
    }

    /**
     * Gets display attitude.
     *
     * @return the display attitude
     */
    public boolean getDisplayAttitude() {
        return displayAttitude;
    }

    /**
     * Gets satellite reference system.
     *
     * @return the satellite reference system
     */
    public SatelliteReferenceSystem getSatelliteReferenceSystem() {
        return satelliteReferenceSystem;
    }

    /**
     * Gets display reference system.
     *
     * @return the display reference system
     */
    public boolean getDisplayReferenceSystem() {
        return displayReferenceSystem;
    }

    /**
     * Gets display only one period.
     *
     * @return the display only one period
     */
    public boolean getDisplayOnlyOnePeriod() {
        return displayOnlyOnePeriod;
    }

    /**
     * Gets period.
     *
     * @return the period
     */
    public double getPeriod() {
        try {
            this.period = satellitePropagator.getInitialState()
                                             .getKeplerianPeriod();
        } catch (OrekitException e) {
            throw new OreCzmlException(OreCzmlMessages.NO_ORBIT_FOR_KEPLERIAN_PERIOD);
        }
        return period;
    }

    // SETTERS

    /**
     * Sets propagator.
     *
     * @param boundedPropagator the bounded propagator
     */
    public void setPropagator(final BoundedPropagator boundedPropagator) {
        this.satellitePropagator = boundedPropagator;
    }

    /**
     * Sets attitudes.
     *
     * @param attitudes the attitudes
     */
    public void setAttitudes(final List<Attitude> attitudes) {
        this.attitudes   = new ArrayList<>(attitudes);
        this.orientation = new Orientation(attitudes, getFrame(), header);
        oriented         = true;
    }

    /**
     * Sets optional rotation.
     *
     * @param optionalRotationInput the optional rotation input
     */
    public void setOptionalRotation(final Rotation optionalRotationInput) {
        this.optionalRotation = optionalRotationInput;
    }

    /**
     * Sets orbit color.
     *
     * @param orbitColor the orbit color
     */
    public void setOrbitColor(final Color orbitColor) {
        this.color = orbitColor;
    }

    /**
     * Sets the description.
     *
     * @param descriptionInput : The description
     */
    public void setDescription(final String descriptionInput) {
        this.description = descriptionInput;
    }

    /**
     * Reset attitudes.
     */
    public void resetAttitudes() {
        this.attitudes   = new ArrayList<>();
        this.orientation = null;
        oriented         = false;
    }

    // Private functions

    /**
     * This function set a multiplexer for the propagator of the satellite. It will handle the retrieving of the
     * spacecraft states and of the attitudes.
     *
     * @param propagator : The propagator of the satellite.
     */
    private void multiplexerSetup(final Propagator propagator) {
        propagator.getMultiplexer()
                  .add(header.getClock()
                             .getMultiplier(),
                          currentState -> {
                              spaceCraftStates.add(currentState);
                              final Attitude currentSpaceCraftAttitude = currentState.getAttitude();
                              attitudes.add(currentSpaceCraftAttitude);
                          });
    }

    /**
     * This function aims at writing the global display of the satellite, it ensures that the model loaded and the orientation are correct.
     *
     * @param packet : The packet that will write in the czml file.
     * @param output : The output stream of cesium that will contain the strings to write into the CzmLFile.
     * @param stream : The stream that converts all the strings into understandable string for the CzmlFile.
     */
    private void czmlDisplay(final PacketCesiumWriter packet, final CesiumStreamWriter stream,
                             final CesiumOutputStream output) throws URISyntaxException, IOException {

        if (getModelType() == ModelType.MODEL_2D || getModelType() == ModelType.EMPTY_MODEL) {
            try (PositionCesiumWriter positionWriter = packet.getPositionWriter()) {
                positionWriter.open(output);
                positionWriter.writeReferenceFrame(DEFAULT_INERTIAL);
            }
            getModel().generateCZML(packet, output);
            if (getDisplayAttitude()) {
                orientation.write(packet, output);
            }
        } else if (getModelType() == ModelType.MODEL_3D) {
            try (PositionCesiumWriter positionWriter = packet.getPositionWriter()) {
                positionWriter.open(output);
                positionWriter.writeReferenceFrame(DEFAULT_INERTIAL);
            }
            if (!getDisplayAttitude()) {

                try (OrientationCesiumWriter orientationWriter = packet.getOrientationWriter()) {
                    orientationWriter.open(output);
                    orientationWriter.writeVelocityReference(this.getId() + "#position");
                    this.orientation = null;
                }
            } else {
                if (!oriented) {
                    this.orientation = new Orientation(attitudes, getFrame(), false, optionalRotation, header);
                }
                this.orientation.write(packet, output);
            }
            this.getModel()
                .generateCZML(packet, output);
        }
    }

    /**
     * This function writes the path of the satellite to display the orbit.
     *
     * @param packet : The packet that will write in the czml file.
     * @param output : The output stream of cesium that will contain the strings to write into the CzmLFile.
     */
    private void czmlPath(final PacketCesiumWriter packet, final CesiumOutputStream output) {
        try (PathCesiumWriter pathProperty = packet.openPathProperty()) {
            if (!getDisplayOnlyOnePeriod()) {
                final Path path = new Path(getAvailability());
                try (BooleanCesiumWriter showPath = pathProperty.openShowProperty()) {
                    showPath.writeInterval(getAvailability().getStart(), getAvailability().getStop());
                    showPath.writeBoolean(path.getShow());
                }
            } else {
                final Path path = new Path(getAvailability());
                if ((this.getOrbits()
                         .get(0)
                         .getKeplerianPeriod() + "").equals("Infinity")) {
                    pathProperty.writeLeadTimeProperty(3600);
                } else {
                    pathProperty.writeLeadTimeProperty(this.getOrbits()
                                                           .get(0)
                                                           .getKeplerianPeriod());
                }
                pathProperty.writeTrailTimeProperty(0.0);
                try (BooleanCesiumWriter showPath = pathProperty.openShowProperty()) {
                    showPath.writeInterval(getAvailability().getStart(), getAvailability().getStop());
                    showPath.writeBoolean(path.getShow());
                }
            }
            try (PolylineMaterialCesiumWriter materialWriter = pathProperty.getMaterialWriter()) {
                materialWriter.open(output);
                output.writeStartObject();
                try (SolidColorMaterialCesiumWriter solidColorWriter = materialWriter.getSolidColorWriter()) {
                    solidColorWriter.open(output);
                    solidColorWriter.writeColorProperty(getColor());
                }
                output.writeEndObject();
            }
        }
    }

    /**
     * This function writes the position of the satellite in time.
     *
     * @param packet : The packet that will write in the czml file.
     * @param output : The output stream of cesium that will contain the strings to write into the CzmLFile.
     */
    private void czmlPosition(final PacketCesiumWriter packet, final CesiumOutputStream output) {

        final TimePosition timePosition = new TimePosition(getCartesianArraylist(), getJulianDates());
        timePosition.write(packet, output);
    }

    /**
     * This function is used when the attitude of the satellite must be displayed. To do so, this function defines an
     * orientation object depending on the type of the model loaded.s
     */
    private void orientationSetup() {
        if (!oriented) {
            if (getModelType() == ModelType.MODEL_2D || getModelType() == ModelType.EMPTY_MODEL) {
                if (displayAttitude) {
                    this.orientation = Orientation.builder(getAttitudes(), getFrame(), header)
                                                  .withOptionalRotation(optionalRotation)
                                                  .withInvertToITRF(false)
                                                  .build();
                    oriented         = true;
                }
            } else if (getModelType() == ModelType.MODEL_3D) {
                if (!displayAttitude) {
                    this.orientation = null;
                } else {
                    this.orientation = Orientation.builder(getAttitudes(), getFrame(), header)
                                                  .withOptionalRotation(optionalRotation)
                                                  .withInvertToITRF(false)
                                                  .build();
                    oriented         = true;
                }
            }
        }
    }
}
