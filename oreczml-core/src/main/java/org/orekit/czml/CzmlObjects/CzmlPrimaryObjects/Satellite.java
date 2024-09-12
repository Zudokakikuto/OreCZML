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

import cesiumlanguagewriter.BooleanCesiumWriter;
import cesiumlanguagewriter.Cartesian;
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
import org.orekit.czml.ArchiObjects.Builders.SatelliteBuilder;
import org.orekit.czml.ArchiObjects.Exceptions.OreCzmlExceptions;
import org.orekit.czml.CzmlEnum.ModelType;
import org.orekit.czml.CzmlObjects.CzmlAbstractObjects.CzmlModel;
import org.orekit.czml.CzmlObjects.CzmlSecondaryObjects.Billboard;
import org.orekit.czml.CzmlObjects.CzmlSecondaryObjects.Orientation;
import org.orekit.czml.CzmlObjects.CzmlSecondaryObjects.TimePosition;
import org.orekit.czml.CzmlObjects.Path;
import org.orekit.errors.OrekitIllegalStateException;
import org.orekit.frames.Frame;
import org.orekit.orbits.Orbit;
import org.orekit.propagation.BoundedPropagator;
import org.orekit.propagation.Propagator;
import org.orekit.propagation.SpacecraftState;
import org.orekit.time.AbsoluteDate;

import java.awt.Color;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
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

public class Satellite extends AbstractPrimaryObject implements CzmlPrimaryObject {

    /** The default model path, empty. */
    public static final String DEFAULT_MODEL_PATH = "";

    /** The default id of the satellite. */
    public static final String DEFAULT_ID = "SAT/";

    /** The default name of the satellite. */
    public static final String DEFAULT_NAME = "Satellite";

    /** The default description of the satellite. */
    public static final String DEFAULT_DESCRIPTION = "A satellite";

    /** The default orbit color of the satellite. */
    public static final Color DEFAULT_COLOR = new Color(255, 255, 255);

    // Optional parameters
    /** To display or not only one period. By default, display all the path. */
    private boolean displayOnlyOnePeriod = false;

    /** To display or not the attitude of the satellite. By default, the satellite is oriented in TNW in the local orbital frame. */
    private boolean displayAttitude = false;

    /** To display or not the satellite reference system. */
    private boolean displayReferenceSystem = false;

    // Orekit arguments
    /** The list of the attitudes of the satellite. */
    private List<Attitude> attitudes = new ArrayList<>();

    /** The list of the spacecraft states of the satellite. */
    private List<SpacecraftState> allSpaceCraftStates = new ArrayList<>();

    /** The final date of propagation. */
    private AbsoluteDate finalDate;

    /** The optional rotation applied to the attitude of the satellite. */
    private Rotation optionalRotation;

    /** The frame in which the satellite is computed. */
    private Frame frame;

    // Writing arguments
    /** The period of the orbit. */
    private double period;

    /** The description of the satellite. */
    private String description;

    /** The orientation in the local orbital frame of the satellite. */
    private Orientation orientation;

    /** The image, if one is charged, of the satellite. By default, a basic image of a satellite will be used. */
    private Billboard billboard = null;

    /** The propagator of the satellite. */
    private Propagator satellitePropagator;

    /** The satellite reference system object. */
    private SatelliteReferenceSystem satelliteReferenceSystem = null;

    /** The color of the orbit. */
    private Color color;

    /** If the satellite already has an attitude defined or not. */
    private boolean oriented = false;

    /** The path ot the model to be used. */
    private String modelPath;

    /** The CzmlModel for the display of the model. */
    private CzmlModel model;

    /** The type of the model. */
    private ModelType modelType;


    // Constructor

    /**
     * The basic satellite constructor, it only needs one bounded propagator. It uses default parameters.
     *
     * @param propagator : A bounded propagator resulting from an already done propagation.
     */
    public Satellite(final BoundedPropagator propagator) throws URISyntaxException, IOException {
        this(propagator, propagator.getMinDate(), propagator.getMaxDate(), DEFAULT_MODEL_PATH, DEFAULT_COLOR);
    }

    /**
     * The satellite constructor with no default parameters.
     *
     * @param propagator     : A bounded propagator resulting from an already done propagation.
     * @param startDateInput : The start date to consider for the start of the propagation and the availability of the satellite.
     * @param finalDateInput : The stop date to consider for the stop the propagation and the availability of the satellite.
     * @param modelPath      : The path to the model to load.
     * @param color          : The color of the orbit.
     */
    public Satellite(final BoundedPropagator propagator, final AbsoluteDate startDateInput,
                     final AbsoluteDate finalDateInput, final String modelPath,
                     final Color color) throws URISyntaxException, IOException {
        try {
            this.setId(DEFAULT_ID + propagator.getInitialState()
                                              .getOrbit()
                                              .toString());
        } catch (OrekitIllegalStateException e) {
            this.setId(DEFAULT_ID + propagator.getInitialState()
                                              .toString());
        }
        this.setName(DEFAULT_NAME);
        this.setAvailability(new TimeInterval(absoluteDateToJulianDate(startDateInput, Header.getTimeScale()),
                absoluteDateToJulianDate(finalDateInput, Header.getTimeScale())));
        this.satellitePropagator = propagator;
        this.description         = DEFAULT_DESCRIPTION;
        this.frame               = propagator.getFrame();
        this.color               = color;
        this.finalDate           = finalDateInput;
        // Setup propagator
        multiplexerSetup(propagator);
        // Propagation
        propagator.propagate(startDateInput, this.finalDate);
        this.modelPath = modelPath;
        this.model     = new CzmlModel(modelPath);
        this.modelType = model.getModelType();
        try {
            this.period = allSpaceCraftStates.get(0)
                                             .getOrbit()
                                             .getKeplerianPeriod();
        } catch (OrekitIllegalStateException e) {
            this.period = allSpaceCraftStates.get(0)
                                             .getKeplerianPeriod();
        }
    }


    // Builder

    public static SatelliteBuilder builder(final BoundedPropagator propagator) {
        return new SatelliteBuilder(propagator);
    }


    // Overrides

    @Override
    public void writeCzmlBlock() throws URISyntaxException, IOException {
        OUTPUT.setPrettyFormatting(true);
        try (PacketCesiumWriter packet = STREAM.openPacket(OUTPUT)) {
            packet.writeId(this.getId());
            packet.writeName(getName());
            packet.writeAvailability(getAvailability());

            czmlDisplay(packet);

            czmlPath(packet);

            czmlPosition(packet);

        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
        if (getDisplayReferenceSystem()) {
            getSatelliteReferenceSystem().writeCzmlBlock();
        }
    }

    @Override
    public StringWriter getStringWriter() {
        return STRING_WRITER;
    }

    @Override
    public void cleanObject() {
        this.setId("");
        this.setName("");
        this.setAvailability(null);
        this.description              = "";
        this.modelPath                = "";
        this.model                    = null;
        this.frame                    = null;
        this.billboard                = null;
        this.attitudes                = new ArrayList<>();
        this.allSpaceCraftStates      = new ArrayList<>();
        this.oriented                 = false;
        this.displayAttitude          = false;
        this.orientation              = null;
        this.optionalRotation         = null;
        this.displayOnlyOnePeriod     = false;
        this.color                    = null;
        this.displayReferenceSystem   = false;
        this.satelliteReferenceSystem = null;
        this.satellitePropagator      = null;
        this.modelType                = null;
        this.period                   = 0.0;
    }


    // Display functions

    public void displayOnlyOnePeriod() {
        displayOnlyOnePeriod = true;
    }

    public void displaySatelliteAttitude() {
        this.displayAttitude = true;
        orientationSetup();
    }

    public void displaySatelliteReferenceSystem() throws URISyntaxException, IOException {
        this.displayReferenceSystem   = true;
        this.satelliteReferenceSystem = new SatelliteReferenceSystem(this);
    }


    // Getters

    public String getDescription() {
        return description;
    }

    public List<AbsoluteDate> getAbsoluteDateList() {
        final List<AbsoluteDate> toReturn = new ArrayList<>();
        for (SpacecraftState allSpaceCraftState : allSpaceCraftStates) {
            toReturn.add(allSpaceCraftState.getDate());
        }
        return toReturn;
    }

    public List<Double> getTimeList() {
        final List<Double> toReturn = new ArrayList<>();
        for (SpacecraftState allSpaceCraftState : allSpaceCraftStates) {
            final AbsoluteDate currentDate = allSpaceCraftState.getDate();
            final Double       tempDouble  = absoluteDateToJulianDateOriginDelta(currentDate);
            toReturn.add(tempDouble);
        }
        return toReturn;
    }

    public List<Attitude> getAttitudes() {
        return new ArrayList<>(attitudes);
    }

    public void setAttitudes(final List<Attitude> attitudes) {
        this.attitudes = attitudes;
    }

    public List<Orbit> getOrbits() {
        final List<Orbit> toReturn = new ArrayList<>();
        for (SpacecraftState allSpaceCraftState : allSpaceCraftStates) {
            toReturn.add(allSpaceCraftState.getOrbit());
        }
        return toReturn;
    }

    public Propagator getSatellitePropagator() {
        return satellitePropagator;
    }

    public BoundedPropagator getSatelliteBoundedPropagator() {
        return (BoundedPropagator) satellitePropagator;
    }

    public List<Cartesian> getCartesianArraylist() {
        final List<Cartesian> toReturn = new ArrayList<>();
        for (SpacecraftState allSpaceCraftState : allSpaceCraftStates) {
            final Vector3D currentVector3D = allSpaceCraftState.getPosition();
            toReturn.add(new Cartesian(currentVector3D.getX(), currentVector3D.getY(), currentVector3D.getZ()));
        }
        return toReturn;
    }

    public Frame getFrame() {
        return frame;
    }

    public Color getColor() {
        return color;
    }

    public String getModelPath() {
        return modelPath;
    }

    public ModelType getModelType() {
        return modelType;
    }

    public CzmlModel getModel() {
        return model;
    }

    public Billboard getBillboard() {
        return billboard;
    }

    public Orientation getOrientation() {
        if (orientation != null) {
            return orientation;
        } else {
            throw new RuntimeException(OreCzmlExceptions.NO_ORIENTATION_DISPLAYED);
        }
    }

    public boolean getDisplayAttitude() {
        return displayAttitude;
    }

    public SatelliteReferenceSystem getSatelliteReferenceSystem() {
        return satelliteReferenceSystem;
    }

    public boolean getDisplayReferenceSystem() {
        return displayReferenceSystem;
    }

    public boolean getDisplayOnlyOnePeriod() {
        return displayOnlyOnePeriod;
    }

    public double getPeriod() {
        return period;
    }


    // SETTERS

    public List<SpacecraftState> getAllSpaceCraftStates() {
        return new ArrayList<>(allSpaceCraftStates);
    }

    public void setAllSpaceCraftStates(final List<SpacecraftState> allSpaceCraftStatesInput) {
        this.allSpaceCraftStates = allSpaceCraftStatesInput;
    }

    public void setPropagator(final BoundedPropagator boundedPropagator) {
        this.satellitePropagator = boundedPropagator;
    }

    public void setOptionalRotation(final Rotation inputRotation) {
        optionalRotation = inputRotation;
    }

    public void resetSpacecraftStates() {
        this.allSpaceCraftStates = new ArrayList<>();
    }

    protected void setOrientation(final Orientation orientation) {
        oriented         = true;
        this.orientation = orientation;
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
                  .add(Header.getMasterClock()
                             .getMultiplier(), currentState -> {
                      allSpaceCraftStates.add(currentState);
                      final Attitude currentSpaceCraftAttitude = currentState.getAttitude();
                      attitudes.add(currentSpaceCraftAttitude);
                  });
    }

    /**
     * This function aims at writing the global display of the satellite, it ensure that the model loaded and the orientation are correct.
     *
     * @param packet : The packet that will write in the czml file.
     */
    private void czmlDisplay(final PacketCesiumWriter packet) throws URISyntaxException, IOException {

        if (getModelType() == ModelType.MODEL_2D || getModelType() == ModelType.EMPTY_MODEL) {
            getModel().generateCZML(packet, OUTPUT);
            if (getDisplayAttitude()) {
                if (!oriented) {
                    this.orientation = new Orientation(getAttitudes(), getFrame(), false, optionalRotation);
                }
                orientation.write(packet, OUTPUT);
            }
        } else if (getModelType() == ModelType.MODEL_3D) {
            try (PositionCesiumWriter positionWriter = packet.getPositionWriter()) {
                positionWriter.open(OUTPUT);
                positionWriter.writeReferenceFrame("INERTIAL");
            }
            if (!getDisplayAttitude()) {

                try (OrientationCesiumWriter orientationWriter = packet.getOrientationWriter()) {
                    orientationWriter.open(OUTPUT);
                    orientationWriter.writeVelocityReference(this.getId() + "#position");
                    this.orientation = null;
                }
            } else {
                if (!oriented) {
                    this.orientation = new Orientation(getAttitudes(), getFrame(), false, optionalRotation);
                }
                this.orientation.write(packet, OUTPUT);
            }
            this.getModel()
                .generateCZML(packet, OUTPUT);
        }
    }

    /**
     * This function writes the path of the satellite to display the orbit.
     *
     * @param packet : The packet that will write in the czml file.
     */
    private void czmlPath(final PacketCesiumWriter packet) {
        try (PathCesiumWriter pathProperty = packet.openPathProperty()) {
            if (!getDisplayOnlyOnePeriod()) {
                final Path path = new Path(getAvailability(), packet);
                try (BooleanCesiumWriter showPath = pathProperty.openShowProperty()) {
                    showPath.writeInterval(getAvailability().getStart(), getAvailability().getStop());
                    showPath.writeBoolean(path.getShow());
                }
            } else {
                final Path path = new Path(getAvailability(), packet);
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
                displayOnlyOnePeriod = false;
            }
            try (PolylineMaterialCesiumWriter materialWriter = pathProperty.getMaterialWriter()) {
                materialWriter.open(OUTPUT);
                OUTPUT.writeStartObject();
                try (SolidColorMaterialCesiumWriter solidColorWriter = materialWriter.getSolidColorWriter()) {
                    solidColorWriter.open(OUTPUT);
                    solidColorWriter.writeColorProperty(getColor());
                }
                OUTPUT.writeEndObject();
            }
        }
    }

    /**
     * This function writes the position of the satellite in time.
     *
     * @param packet : The packet that will write in the czml file.
     */
    private void czmlPosition(final PacketCesiumWriter packet) {

        final TimePosition Pos = new TimePosition(this.getCartesianArraylist(), getTimeList());

        try (PositionCesiumWriter writer = packet.openPositionProperty()) {
            writer.writeReferenceFrame(Pos.getReferenceFrame());
            writer.writeInterpolationAlgorithm(Pos.getCesiumInterpolationAlgorithm());
            writer.writeInterpolationDegree(Pos.getInterpolationDegree());
            writer.writeCartesian(Pos.getDates(), Pos.getPositions());
        }
    }

    /**
     * This function is used when the attitude of the satellite must be displayed. To do so, this function defines an
     * orientation object depending of the type of the model loaded.s
     */
    private void orientationSetup() {
        if (!oriented) {
            if (getModelType() == ModelType.MODEL_2D || getModelType() == ModelType.EMPTY_MODEL) {
                if (displayAttitude) {
                    this.orientation = Orientation.builder(getAttitudes(), getFrame())
                                                  .withOptionalRotation(optionalRotation)
                                                  .build();
                }
            } else if (getModelType() == ModelType.MODEL_3D) {
                if (!displayAttitude) {
                    this.orientation = null;
                } else {
                    this.orientation = Orientation.builder(getAttitudes(), getFrame())
                                                  .withOptionalRotation(optionalRotation)
                                                  .build();
                }
            }
        }
    }
}
