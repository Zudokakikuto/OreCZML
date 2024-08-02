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
import org.hipparchus.geometry.euclidean.threed.Rotation;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.orekit.attitudes.Attitude;
import org.orekit.czml.ArchiObjects.SatelliteBuilder;
import org.orekit.czml.CzmlEnum.ModelType;
import org.orekit.czml.CzmlObjects.CzmlAbstractObjects.CzmlModel;
import org.orekit.czml.CzmlObjects.CzmlSecondaryObjects.Billboard;
import org.orekit.czml.CzmlObjects.CzmlSecondaryObjects.Orientation;
import org.orekit.czml.CzmlObjects.CzmlSecondaryObjects.TimePosition;
import org.orekit.czml.CzmlObjects.Path;
import org.orekit.frames.Frame;
import org.orekit.orbits.KeplerianOrbit;
import org.orekit.orbits.Orbit;
import org.orekit.orbits.OrbitType;
import org.orekit.propagation.BoundedPropagator;
import org.orekit.propagation.Propagator;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.sampling.OrekitFixedStepHandler;
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
 * @since 1.0
 */

public class Satellite extends AbstractPrimaryObject implements CzmlPrimaryObject {

    /**
     * .
     */
    public static final String DEFAULT_MODEL_PATH = "";

    /**
     * .
     */
    public static final String DEFAULT_NAME = "Satellite";
    /**
     * .
     */
    public static final String DEFAULT_DESCRIPTION = "A satellite";
    /**
     * .
     */
    public static final Color DEFAULT_COLOR = new Color(255, 255, 255);

    // Optional parameters
    /**
     * .
     */
    private boolean displayOnlyOnePeriod = false;
    /**
     * .
     */
    private boolean displayAttitude = false;
    /**
     * .
     */
    private boolean displayReferenceSystem = false;

    // Orekit arguments
    /**
     * .
     */
    private List<Vector3D> vector3DS;
    /**
     * .
     */
    private List<Attitude> attitudes = new ArrayList<>();
    /**
     * .
     */
    private List<SpacecraftState> allSpaceCraftStates = new ArrayList<>();
    /**
     * .
     */
    private AbsoluteDate finalDate;
    /**
     * .
     */
    private Rotation optionalRotation;
    /**
     * .
     */
    private Frame frame;

    // Writing arguments
    /**
     * .
     */
    private double period;
    /**
     * .
     */
    private String description;
    /**
     * .
     */
    private Orientation orientation;
    /**
     * .
     */
    private Billboard billboard = null;
    /**
     * .
     */
    private Propagator satellitePropagator;
    /**
     * .
     */
    private BoundedPropagator boundedPropagator;
    /**
     * .
     */
    private SatelliteReferenceSystem satelliteReferenceSystem = null;
    /**
     * .
     */
    private Color color;
    /**
     * .
     */
    private boolean oriented = false;
    /**
     * .
     */
    private OrbitType orbitType;
    /**
     * .
     */
    private String modelPath;
    /**
     * .
     */
    private CzmlModel model;
    /**
     * .
     */
    private ModelType modelType;

    //// Constructors

//    public Satellite(final Oem file) throws URISyntaxException, IOException {
//        this(file, DEFAULT_MODEL_PATH, DEFAULT_COLOR);
//    }
//
//    public Satellite(final Oem file, final Color color) throws URISyntaxException, IOException {
//        this(file, DEFAULT_MODEL_PATH, color);
//    }
//
//    public Satellite(final Oem file, final String modelPath) throws URISyntaxException, IOException {
//        this(file, modelPath, DEFAULT_COLOR);
//    }
//
//    public Satellite(final Oem file, final String modelPath, final Color color) throws URISyntaxException, IOException {
//        this.setId(DEFAULT_FILE_SATELLITE_NAME + file.getHeader().getMessageId());
//        this.setName(DEFAULT_NAME);
//        this.setAvailability(Header.MASTER_CLOCK.getAvailability());
//        this.description = DEFAULT_DESCRIPTION;
//        this.frame = file.getSegments().get(0).getFrame();
//        this.Ephemeris = file.getSegments().get(0).getData().getEphemeridesDataLines();
//        this.color = color;
//
//        final List<Vector3D> vector3DS1 = new ArrayList<>();
//        this.timeList = new ArrayList<>();
//        final List<Orbit> orbitList = new ArrayList<>();
//        final List<Position> positions = new ArrayList<>();
//
//        for (int i = 0; i < Ephemeris.size(); i++) {
//            timeList.add(absoluteDateToJulianDateDelta(Ephemeris.get(i).getDate()));
//            absoluteDateList.add(Ephemeris.get(i).getDate());
//            vector3DS1.add(Ephemeris.get(i).getPosition());
//            final double x = vector3DS1.get(i).getX();
//            final double y = vector3DS1.get(i).getY();
//            final double z = vector3DS1.get(i).getZ();
//            final PositionType positionType = PositionType.CARTESIAN_POSITION;
//            final Position tempPosition = new Position(x, y, z, positionType);
//            positions.add(tempPosition);
//            final KeplerianOrbit tempOrbit = new KeplerianOrbit(Ephemeris.get(i), frame, Constants.WGS84_EARTH_MU);
//            orbitList.add(tempOrbit);
//        }
//
//        this.positionsList = positions;
//        this.vector3DS = vector3DS1;
//        this.orbits = orbitList;
//        this.orbitType = orbits.get(0).getType();
//        final List<Vector3D> toCartesians = propagationSat();
//        this.cartesianArraylist = vectorToCartesian(toCartesians);
//        this.modelPath = modelPath;
//        this.model = new CzmlModel(modelPath);
//        this.modelType = model.getModelType();
//        this.period = orbits.get(0).getKeplerianPeriod();
//    }
//
//    public Satellite(final TLE tleFile) throws URISyntaxException, IOException {
//        this(tleFile, DEFAULT_MODEL_PATH, DEFAULT_COLOR);
//    }
//
//    public Satellite(final TLE tleFile, final Color color) throws URISyntaxException, IOException {
//        this(tleFile, DEFAULT_MODEL_PATH, color);
//    }
//
//    public Satellite(final TLE tleFile, final String modelPath) throws URISyntaxException, IOException {
//        this(tleFile, modelPath, DEFAULT_COLOR);
//    }
//
//    public Satellite(final TLE file, final String modelPath, final Color color) throws URISyntaxException, IOException {
//        this.setId(DEFAULT_FILE_SATELLITE_NAME + "#ID :" + file.toString());
//        this.setName(DEFAULT_NAME);
//        this.description = DEFAULT_DESCRIPTION;
//        this.color = color;
//        this.frame = FramesFactory.getTEME();
//        this.setAvailability(Header.MASTER_CLOCK.getAvailability());
//        this.description = DEFAULT_DESCRIPTION;
//        timeList.add(absoluteDateToJulianDateDelta(file.getDate()));
//        timeList.add(absoluteDateToJulianDateDelta(julianDateToAbsoluteDate(Header.MASTER_CLOCK.getAvailability().getStop(), file.getUtc())));
//        this.positionsList = new ArrayList<>();
//        this.orbits = new ArrayList<>();
//        final Propagator propagator = TLEPropagator.selectExtrapolator(file);
//        final Orbit initialOrbit = propagator.getInitialState().getOrbit();
//        orbits.add(initialOrbit);
//        this.orbitType = orbits.get(0).getType();
//        this.cartesianArraylist = propagationOrbit(initialOrbit);
//        this.modelPath = modelPath;
//        this.model = new CzmlModel(modelPath);
//        this.modelType = model.getModelType();
//        this.period = orbits.get(0).getKeplerianPeriod();
//    }
//
//    public Satellite(final TimeInterval availability, final List<TimeStampedPVCoordinates> Ephemeris, final Frame frame) throws URISyntaxException, IOException {
//        this(DEFAULT_NAME, availability, DEFAULT_DESCRIPTION, Ephemeris, frame);
//    }
//
//    public Satellite(final String name, final TimeInterval availability, final String description, final List<TimeStampedPVCoordinates> Ephemeris, final Frame frame) throws URISyntaxException, IOException {
//        this(name, availability, description, Ephemeris, frame, DEFAULT_MODEL_PATH, DEFAULT_COLOR);
//    }
//
//    public Satellite(final String name, final TimeInterval availability, final String description, final List<TimeStampedPVCoordinates> Ephemeris, final Frame frame, final Color color) throws URISyntaxException, IOException {
//        this(name, availability, description, Ephemeris, frame, DEFAULT_MODEL_PATH, color);
//    }
//
//    public Satellite(final String name, final TimeInterval availability, final String description, final List<TimeStampedPVCoordinates> Ephemeris, final Frame frame, final String modelPath) throws URISyntaxException, IOException {
//        this(name, availability, description, Ephemeris, frame, modelPath, DEFAULT_COLOR);
//    }
//
//    public Satellite(final String name, final TimeInterval availability, final String description, final List<TimeStampedPVCoordinates> Ephemeris, final Frame frame, final String modelPath, final Color color) throws URISyntaxException, IOException {
//        this.setId(DEFAULT_NAME);
//        this.setName(name);
//        this.setAvailability(availability);
//        this.description = description;
//        this.Ephemeris = Ephemeris;
//        this.color = color;
//
//        final List<Orbit> orbitList = new ArrayList<>();
//        final List<Vector3D> vector3DS1 = new ArrayList<>();
//        this.timeList = new ArrayList<>();
//        final List<Position> positions = new ArrayList<>();
//
//        for (int i = 0; i < Ephemeris.size(); i++) {
//            timeList.add(absoluteDateToJulianDateDelta(Ephemeris.get(i).getDate()));
//            absoluteDateList.add(Ephemeris.get(i).getDate());
//            vector3DS1.add(Ephemeris.get(i).getPosition());
//            final double x = vector3DS1.get(i).getX();
//            final double y = vector3DS1.get(i).getY();
//            final double z = vector3DS1.get(i).getZ();
//            final PositionType positionType = PositionType.CARTESIAN_POSITION;
//            final Position tempPosition = new Position(x, y, z, positionType);
//            positions.add(tempPosition);
//            final KeplerianOrbit tempOrbit = new KeplerianOrbit(Ephemeris.get(i), frame, Constants.WGS84_EARTH_MU);
//            orbitList.add(tempOrbit);
//        }
//
//        this.positionsList = positions;
//        this.vector3DS = vector3DS1;
//        this.orbits = orbitList;
//        this.orbitType = orbits.get(0).getType();
//        this.frame = frame;
//        final List<Vector3D> toCartesians = propagationSat();
//        this.cartesianArraylist = vectorToCartesian(toCartesians);
//
//        this.modelPath = modelPath;
//        this.model = new CzmlModel(modelPath);
//        this.modelType = model.getModelType();
//        this.period = orbits.get(0).getKeplerianPeriod();
//    }
//
//    public Satellite(final Orbit orbit) throws URISyntaxException, IOException {
//        this(orbit, DEFAULT_MODEL_PATH, DEFAULT_COLOR);
//    }
//
//    public Satellite(final Orbit orbit, final Color color) throws URISyntaxException, IOException {
//        this(orbit, DEFAULT_MODEL_PATH, color);
//    }
//
//    public Satellite(final Orbit orbit, final String modelPath) throws URISyntaxException, IOException {
//        this(orbit, modelPath, DEFAULT_COLOR);
//    }
//
//    public Satellite(final Orbit orbit, final String modelPath, final Color color) throws URISyntaxException, IOException {
//        this.setId(orbit.toString());
//        this.setName(orbit.toString());
//        this.setAvailability(Header.MASTER_CLOCK.getAvailability());
//        final AbsoluteDate startTime = orbit.getDate();
//        this.orbitType = orbit.getType();
//        final AbsoluteDate stopTime = julianDateToAbsoluteDate(Header.MASTER_CLOCK.getAvailability().getStop(), TimeScalesFactory.getUTC());
//        this.timeList = new ArrayList<>();
//        this.orbits = new ArrayList<>();
//        this.positionsList = new ArrayList<>();
//        this.color = color;
//        this.description = DEFAULT_DESCRIPTION;
//        timeList.add(absoluteDateToJulianDateDelta(startTime));
//        timeList.add(absoluteDateToJulianDateDelta(stopTime));
//        orbits.add(orbit);
//        this.frame = orbit.getFrame();
//        this.cartesianArraylist = propagationOrbit(orbit);
//
//        this.modelPath = modelPath;
//        this.model = new CzmlModel(modelPath);
//        this.modelType = model.getModelType();
//        this.period = orbits.get(0).getKeplerianPeriod();
//    }
//
//    public Satellite(final List<SpacecraftState> input) throws URISyntaxException, IOException {
//        this(input, DEFAULT_MODEL_PATH, DEFAULT_COLOR);
//    }
//
//    public Satellite(final List<SpacecraftState> input, final Color color) throws URISyntaxException, IOException {
//        this(input, DEFAULT_MODEL_PATH, color);
//    }
//
//    public Satellite(final List<SpacecraftState> input, final String modelPath) throws URISyntaxException, IOException {
//        this(input, modelPath, DEFAULT_COLOR);
//    }
//
//    public Satellite(final List<SpacecraftState> input, final String modelPath, final Color color) throws URISyntaxException, IOException {
//        final List<Double> doubleListTemp = new ArrayList<>();
//        final List<Orbit> orbitTemp = new ArrayList<>();
//        final List<Vector3D> vector3DTemp = new ArrayList<>();
//        this.positionsList = new ArrayList<>();
//        this.color = color;
//        this.orbitType = input.get(0).getOrbit().getType();
//
//        this.setId(input.get(0).getOrbit().toString());
//        this.setName(DEFAULT_NAME);
//        this.setAvailability(Header.MASTER_CLOCK.getAvailability());
//
//        for (SpacecraftState spacecraftState : input) {
//            orbitTemp.add(spacecraftState.getOrbit());
//            vector3DTemp.add(spacecraftState.getPosition());
//            doubleListTemp.add(absoluteDateToJulianDateDelta(spacecraftState.getDate()));
//        }
//
//        this.description = DEFAULT_DESCRIPTION;
//        this.orbits = orbitTemp;
//        this.vector3DS = vector3DTemp;
//        this.timeList = doubleListTemp;
//        final List<Vector3D> toCartesians = propagationSat();
//        this.cartesianArraylist = vectorToCartesian(toCartesians);
//
//        this.modelPath = modelPath;
//        this.model = new CzmlModel(modelPath);
//        this.modelType = model.getModelType();
//        this.period = orbitTemp.get(0).getKeplerianPeriod();
//    }

    public Satellite(final BoundedPropagator propagator) throws URISyntaxException, IOException {
        this(propagator, propagator.getMinDate(), propagator.getMaxDate(), DEFAULT_MODEL_PATH, DEFAULT_COLOR);
    }

    public Satellite(final BoundedPropagator propagator, final AbsoluteDate startDateInput, final AbsoluteDate finalDateInput, final String modelPath, final Color color) throws URISyntaxException, IOException {

        if (propagator.getInitialState()
                      .getOrbit() == null) {
            throw new RuntimeException("The propagator has no initial state, please setup the propagator before building" +
                                               "the satellite");
        } else {
            this.setId(propagator.getInitialState()
                                 .getOrbit()
                                 .toString());
            this.setName(DEFAULT_NAME);
            this.setAvailability(Header.MASTER_CLOCK.getAvailability());
            this.satellitePropagator = propagator;
            this.description = DEFAULT_DESCRIPTION;
            this.frame = propagator.getFrame();
            this.color = color;
            this.finalDate = finalDateInput;
            this.orbitType = propagator.getInitialState()
                                       .getOrbit()
                                       .getType();


            // Setup propagator
            this.vector3DS = multiplexerSetup(propagator);

            // Propagation
            propagator.propagate(startDateInput, this.finalDate);
            this.boundedPropagator = propagator;
        }
        this.modelPath = modelPath;
        this.model = new CzmlModel(modelPath);
        this.modelType = model.getModelType();
        this.period = allSpaceCraftStates.get(0)
                                         .getOrbit()
                                         .getKeplerianPeriod();
    }

    public static SatelliteBuilder builder(final BoundedPropagator propagator) {
        return new SatelliteBuilder(propagator);
    }

    // Overrides
    @Override
    public StringWriter getStringWriter() {
        return STRING_WRITER;
    }

    @Override
    public void cleanObject() {
        this.setId("");
        this.setName("");
        this.setAvailability(null);
        this.description = "";
        this.modelPath = "";
        this.model = null;
        this.vector3DS = new ArrayList<>();
        this.frame = null;
        this.billboard = null;
        this.attitudes = new ArrayList<>();
        this.allSpaceCraftStates = new ArrayList<>();
        this.oriented = false;
        this.displayAttitude = false;
        this.orientation = null;
        this.optionalRotation = null;
        this.displayOnlyOnePeriod = false;
        this.color = null;
        this.displayReferenceSystem = false;
        this.satelliteReferenceSystem = null;
        this.satellitePropagator = null;
        this.modelType = null;
        this.period = 0.0;
        this.orbitType = null;
    }

    // GETS

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
            final Double tempDouble = absoluteDateToJulianDateDelta(currentDate);
            toReturn.add(tempDouble);
        }
        return toReturn;
    }

    public List<Attitude> getAttitudes() {
        return attitudes;
    }

    // SETS
    public void setAttitudes(final List<Attitude> attitudes) {
        this.attitudes = attitudes;
    }

    public List<Vector3D> getVector3DS() {
        return vector3DS;
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
            throw new RuntimeException("The satellite did not display the orientation, please use the displaySatelliteAttitude() method first");
        }
    }

    protected void setOrientation(final Orientation orientation) {
        oriented = true;
        this.orientation = orientation;
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

    public BoundedPropagator getBoundedPropagator() {
        return boundedPropagator;
    }

    // User functions

    public void setBoundedPropagator(final BoundedPropagator boundedPropagator) {
        this.boundedPropagator = boundedPropagator;
    }

    public void setOptionalRotation(final Rotation inputRotation) {
        optionalRotation = inputRotation;
    }

    public void resetSpacecraftStates() {
        this.allSpaceCraftStates = new ArrayList<>();
    }

    public List<SpacecraftState> getAllSpaceCraftStates() {
        return allSpaceCraftStates;
    }

    public void setAllSpaceCraftStates(final List<SpacecraftState> allSpaceCraftStatesInput) {
        this.allSpaceCraftStates = allSpaceCraftStatesInput;
    }

    public void displayOnlyOnePeriod() {
        displayOnlyOnePeriod = true;
    }

    public void displaySatelliteAttitude() {
        this.displayAttitude = true;
        orientationSetup();
    }

    public void displaySatelliteReferenceSystem() throws URISyntaxException, IOException {
        this.displayReferenceSystem = true;
        this.satelliteReferenceSystem = new SatelliteReferenceSystem(this);
    }


    // Functions
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
                pathProperty.writeLeadTimeProperty(this.getOrbits()
                                                       .get(0)
                                                       .getKeplerianPeriod());
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

    private void czmlPosition(final PacketCesiumWriter packet) {

        final TimePosition Pos = new TimePosition(this.getCartesianArraylist(), getTimeList());

        try (PositionCesiumWriter writer = packet.openPositionProperty()) {
            writer.writeReferenceFrame(Pos.getReferenceFrame());
            writer.writeInterpolationAlgorithm(Pos.getCesiumInterpolationAlgorithm());
            writer.writeInterpolationDegree(Pos.getInterpolationDegree());
            writer.writeCartesian(Pos.getDates(), Pos.getPositions());
        }
    }

    private List<Vector3D> multiplexerSetup(final Propagator propagator) {
        final List<Vector3D> toReturn = new ArrayList<>();

        propagator.getMultiplexer()
                  .add(Header.MASTER_CLOCK.getMultiplier(), new OrekitFixedStepHandler() {
                      @Override
                      public void handleStep(final SpacecraftState currentState) {
                          final KeplerianOrbit o = new KeplerianOrbit(currentState.getOrbit());
                          toReturn.add(o.getPosition());
                          allSpaceCraftStates.add(currentState);
                          final Attitude currentSpaceCraftAttitude = currentState.getAttitude();
                          attitudes.add(currentSpaceCraftAttitude);
                      }
                  });
        return toReturn;
    }

    private void orientationSetup() {
        if (!oriented) {
            if (getModelType() == ModelType.MODEL_2D || getModelType() == ModelType.EMPTY_MODEL) {
                if (displayAttitude) {
                    this.orientation = new Orientation(getAttitudes(), getFrame(), optionalRotation);
                }
            } else if (getModelType() == ModelType.MODEL_3D) {
                if (!displayAttitude) {
                    this.orientation = null;
                } else {
                    this.orientation = new Orientation(getAttitudes(), getFrame(), optionalRotation);
                }
            }
        }
    }
}
