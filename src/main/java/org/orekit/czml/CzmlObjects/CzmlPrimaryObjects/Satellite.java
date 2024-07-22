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
import cesiumlanguagewriter.GregorianDate;
import cesiumlanguagewriter.OrientationCesiumWriter;
import cesiumlanguagewriter.PacketCesiumWriter;
import cesiumlanguagewriter.PathCesiumWriter;
import cesiumlanguagewriter.PolylineMaterialCesiumWriter;
import cesiumlanguagewriter.PositionCesiumWriter;
import cesiumlanguagewriter.SolidColorMaterialCesiumWriter;
import cesiumlanguagewriter.TimeInterval;
import org.hipparchus.geometry.euclidean.threed.Rotation;
import org.orekit.czml.CzmlObjects.CzmlAbstractObjects.CzmlModel;
import org.orekit.czml.CzmlObjects.CzmlSecondaryObjects.Billboard;
import org.orekit.czml.CzmlObjects.CzmlSecondaryObjects.Orientation;
import org.orekit.czml.CzmlObjects.CzmlSecondaryObjects.SatelliteObjects.SatelliteAttitude;
import org.orekit.czml.CzmlObjects.CzmlSecondaryObjects.SatelliteObjects.Path;
import org.orekit.czml.CzmlObjects.CzmlSecondaryObjects.SatelliteObjects.SatellitePosition;
import org.orekit.czml.CzmlEnum.ModelType;
import org.orekit.czml.CzmlEnum.PositionType;
import org.orekit.czml.CzmlObjects.Position;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.hipparchus.ode.nonstiff.AdaptiveStepsizeIntegrator;
import org.hipparchus.ode.nonstiff.DormandPrince853Integrator;
import org.orekit.attitudes.Attitude;
import org.orekit.files.ccsds.ndm.odm.oem.Oem;
import org.orekit.forces.ForceModel;
import org.orekit.forces.gravity.HolmesFeatherstoneAttractionModel;
import org.orekit.forces.gravity.potential.GravityFieldFactory;
import org.orekit.forces.gravity.potential.NormalizedSphericalHarmonicsProvider;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.orbits.KeplerianOrbit;
import org.orekit.orbits.Orbit;
import org.orekit.orbits.OrbitType;
import org.orekit.propagation.BoundedPropagator;
import org.orekit.propagation.EphemerisGenerator;
import org.orekit.propagation.Propagator;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.analytical.tle.TLE;
import org.orekit.propagation.analytical.tle.TLEPropagator;
import org.orekit.propagation.numerical.NumericalPropagator;
import org.orekit.propagation.sampling.OrekitFixedStepHandler;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScale;
import org.orekit.time.TimeScalesFactory;
import org.orekit.utils.Constants;
import org.orekit.utils.TimeStampedPVCoordinates;

import java.awt.Color;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/** Satellite class.
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
 * @since 1.0
 * @author Julien LEBLOND
 */

public class Satellite extends AbstractPrimaryObject implements CzmlPrimaryObject {

    /** .*/
    public static final String DEFAULT_MODEL_PATH = "";

    /** .*/
    public static final String DEFAULT_NAME = "Satellite";
    /** .*/
    public static final String DEFAULT_DESCRIPTION = "A satellite";
    /** .*/
    public static final String DEFAULT_FILE_SATELLITE_NAME = "Satellite created from : ";
    /** .*/
    public static final Color DEFAULT_COLOR = new Color(255, 255, 255);
    /** .*/
    private String description;
    /** .*/
    private List<TimeStampedPVCoordinates> Ephemeris = null;


    // Optional parameters
    /** .*/
    private boolean displayOnlyOnePeriod = false;
    /** .*/
    private boolean displayAttitude = false;
    /** .*/
    private boolean displayReferenceSystem = false;
    /** .*/
    private String modelPath = "";
    /** .*/
    private CzmlModel model;
    /** .*/
    private ModelType modelType;
    /** .*/
    private Rotation optionalRotation = Rotation.IDENTITY;

    // Intrinsic parameters
    /** .*/
    private double period;
    /** .*/
    private List<Vector3D> vector3DS;
    /** .*/
    private List<Double> timeList;
    /** .*/
    private List<AbsoluteDate> absoluteDateList;
    /** .*/
    private Orientation orientation;
    /** .*/
    private List<Cartesian> cartesianArraylist;
    /** .*/
    private List<Orbit> orbits;
    /** .*/
    private Frame frame = null;
    /** .*/
    private Billboard billboard = null;
    /** .*/
    private List<Position> positionsList;
    /** .*/
    private BoundedPropagator boundedPropagator;
    /** .*/
    private Propagator satellitePropagator;
    /** .*/
    private List<Attitude> attitudes = new ArrayList<>();
    /** .*/
    private SatelliteAttitude satelliteAttitude = null;
    /** .*/
    private SatelliteReferenceSystem satelliteReferenceSystem = null;
    /** .*/
    private List<SpacecraftState> allSpaceCraftStates = new ArrayList<>();
    /** .*/
    private Color pathColor;
    /** .*/
    private boolean oriented = false;

    //// BUILDERS

    public Satellite(final Oem file) throws URISyntaxException, IOException {
        this(file, DEFAULT_MODEL_PATH, DEFAULT_COLOR);
    }

    public Satellite(final Oem file, final Color color) throws URISyntaxException, IOException {
        this(file, DEFAULT_MODEL_PATH, color);
    }

    public Satellite(final Oem file, final String modelPath) throws URISyntaxException, IOException {
        this(file, modelPath, DEFAULT_COLOR);
    }

    public Satellite(final Oem file, final String modelPath, final Color color) throws URISyntaxException, IOException {
        this.setId(DEFAULT_FILE_SATELLITE_NAME + file.getHeader().getMessageId());
        this.setName(DEFAULT_NAME);
        this.setAvailability(Header.MASTER_CLOCK.getAvailability());
        this.description = DEFAULT_DESCRIPTION;
        this.frame = file.getSegments().get(0).getFrame();
        this.Ephemeris = file.getSegments().get(0).getData().getEphemeridesDataLines();
        this.pathColor = color;

        final List<Vector3D> vector3DS1 = new ArrayList<>();
        this.timeList = new ArrayList<>();
        final List<Orbit> orbitList = new ArrayList<>();
        final List<Position> positions = new ArrayList<>();

        for (int i = 0; i < Ephemeris.size(); i++) {
            timeList.add(absoluteDateToJulianDateDelta(Ephemeris.get(i).getDate()));
            absoluteDateList.add(Ephemeris.get(i).getDate());
            vector3DS1.add(Ephemeris.get(i).getPosition());
            final double x = vector3DS1.get(i).getX();
            final double y = vector3DS1.get(i).getY();
            final double z = vector3DS1.get(i).getZ();
            final PositionType positionType = PositionType.CARTESIAN_POSITION;
            final Position tempPosition = new Position(x, y, z, positionType);
            positions.add(tempPosition);
            final KeplerianOrbit tempOrbit = new KeplerianOrbit(Ephemeris.get(i), frame, Constants.WGS84_EARTH_MU);
            orbitList.add(tempOrbit);
        }

        this.positionsList = positions;
        this.vector3DS = vector3DS1;
        this.orbits = orbitList;
        final List<Vector3D> toCartesians = propagationSat();
        this.cartesianArraylist = vectorToCartesian(toCartesians);
        this.modelPath = modelPath;
        this.model = new CzmlModel(modelPath);
        this.modelType = model.getModelType();
        this.period = orbits.get(0).getKeplerianPeriod();
    }

    public Satellite(final TLE tleFile) throws URISyntaxException, IOException {
        this(tleFile, DEFAULT_MODEL_PATH, DEFAULT_COLOR);
    }

    public Satellite(final TLE tleFile, final Color color) throws URISyntaxException, IOException {
        this(tleFile, DEFAULT_MODEL_PATH, color);
    }

    public Satellite(final TLE tleFile, final String modelPath) throws URISyntaxException, IOException {
        this(tleFile, modelPath, DEFAULT_COLOR);
    }

    public Satellite(final TLE file, final String modelPath, final Color color) throws URISyntaxException, IOException {
        this.setId(DEFAULT_FILE_SATELLITE_NAME + "#ID :" + file.toString());
        this.setName(DEFAULT_NAME);
        this.description = DEFAULT_DESCRIPTION;
        this.pathColor = color;
        this.frame = FramesFactory.getTEME();
        this.setAvailability(Header.MASTER_CLOCK.getAvailability());
        this.description = DEFAULT_DESCRIPTION;
        timeList.add(absoluteDateToJulianDateDelta(file.getDate()));
        timeList.add(absoluteDateToJulianDateDelta(julianDateToAbsoluteDate(Header.MASTER_CLOCK.getAvailability().getStop(), file.getUtc())));
        this.positionsList = new ArrayList<>();
        this.orbits = new ArrayList<>();
        final Propagator propagator = TLEPropagator.selectExtrapolator(file);
        final Orbit initialOrbit = propagator.getInitialState().getOrbit();
        orbits.add(initialOrbit);
        this.cartesianArraylist = propagationOrbit(initialOrbit);
        this.modelPath = modelPath;
        this.model = new CzmlModel(modelPath);
        this.modelType = model.getModelType();
        this.period = orbits.get(0).getKeplerianPeriod();
    }

    public Satellite(final TimeInterval availability, final List<TimeStampedPVCoordinates> Ephemeris, final Frame frame) throws URISyntaxException, IOException {
        this(DEFAULT_NAME, availability, DEFAULT_DESCRIPTION, Ephemeris, frame);
    }

    public Satellite(final String name, final TimeInterval availability, final String description, final List<TimeStampedPVCoordinates> Ephemeris, final Frame frame) throws URISyntaxException, IOException {
        this(name, availability, description, Ephemeris, frame, DEFAULT_MODEL_PATH, DEFAULT_COLOR);
    }

    public Satellite(final String name, final TimeInterval availability, final String description, final List<TimeStampedPVCoordinates> Ephemeris, final Frame frame, final Color color) throws URISyntaxException, IOException {
        this(name, availability, description, Ephemeris, frame, DEFAULT_MODEL_PATH, color);
    }

    public Satellite(final String name, final TimeInterval availability, final String description, final List<TimeStampedPVCoordinates> Ephemeris, final Frame frame, final String modelPath) throws URISyntaxException, IOException {
        this(name, availability, description, Ephemeris, frame, modelPath, DEFAULT_COLOR);
    }

    public Satellite(final String name, final TimeInterval availability, final String description, final List<TimeStampedPVCoordinates> Ephemeris, final Frame frame, final String modelPath, final Color color) throws URISyntaxException, IOException {
        this.setId(DEFAULT_NAME);
        this.setName(name);
        this.setAvailability(availability);
        this.description = description;
        this.Ephemeris = Ephemeris;
        this.pathColor = color;

        final List<Orbit> orbitList = new ArrayList<Orbit>();
        final List<Vector3D> vector3DS1 = new ArrayList<>();
        this.timeList = new ArrayList<>();
        final List<Position> positions = new ArrayList<Position>();

        for (int i = 0; i < Ephemeris.size(); i++) {
            timeList.add(absoluteDateToJulianDateDelta(Ephemeris.get(i).getDate()));
            absoluteDateList.add(Ephemeris.get(i).getDate());
            vector3DS1.add(Ephemeris.get(i).getPosition());
            final double x = vector3DS1.get(i).getX();
            final double y = vector3DS1.get(i).getY();
            final double z = vector3DS1.get(i).getZ();
            final PositionType positionType = PositionType.CARTESIAN_POSITION;
            final Position tempPosition = new Position(x, y, z, positionType);
            positions.add(tempPosition);
            final KeplerianOrbit tempOrbit = new KeplerianOrbit(Ephemeris.get(i), frame, Constants.WGS84_EARTH_MU);
            orbitList.add(tempOrbit);
        }

        this.positionsList = positions;
        this.vector3DS = vector3DS1;
        this.orbits = orbitList;
        this.frame = frame;
        final List<Vector3D> toCartesians = propagationSat();
        this.cartesianArraylist = vectorToCartesian(toCartesians);

        this.modelPath = modelPath;
        this.model = new CzmlModel(modelPath);
        this.modelType = model.getModelType();
        this.period = orbits.get(0).getKeplerianPeriod();
    }

    public Satellite(final Orbit orbit) throws URISyntaxException, IOException {
        this(orbit, DEFAULT_MODEL_PATH, DEFAULT_COLOR);
    }

    public Satellite(final Orbit orbit, final Color color) throws URISyntaxException, IOException {
        this(orbit, DEFAULT_MODEL_PATH, color);
    }

    public Satellite(final Orbit orbit, final String modelPath) throws URISyntaxException, IOException {
        this(orbit, modelPath, DEFAULT_COLOR);
    }

    public Satellite(final Orbit orbit, final String modelPath, final Color color) throws URISyntaxException, IOException {
        this.setId(orbit.toString());
        this.setName(orbit.toString());
        this.setAvailability(Header.MASTER_CLOCK.getAvailability());
        final AbsoluteDate startTime = orbit.getDate();
        final AbsoluteDate stopTime = julianDateToAbsoluteDate(Header.MASTER_CLOCK.getAvailability().getStop(), TimeScalesFactory.getUTC());
        this.timeList = new ArrayList<>();
        this.orbits = new ArrayList<>();
        this.positionsList = new ArrayList<>();
        this.pathColor = color;
        this.description = DEFAULT_DESCRIPTION;
        timeList.add(absoluteDateToJulianDateDelta(startTime));
        timeList.add(absoluteDateToJulianDateDelta(stopTime));
        orbits.add(orbit);
        this.frame = orbit.getFrame();
        this.cartesianArraylist = propagationOrbit(orbit);

        this.modelPath = modelPath;
        this.model = new CzmlModel(modelPath);
        this.modelType = model.getModelType();
        this.period = orbits.get(0).getKeplerianPeriod();
    }

    public Satellite(final List<SpacecraftState> input) throws URISyntaxException, IOException {
        this(input, DEFAULT_MODEL_PATH, DEFAULT_COLOR);
    }

    public Satellite(final List<SpacecraftState> input, final Color color) throws URISyntaxException, IOException {
        this(input, DEFAULT_MODEL_PATH, color);
    }

    public Satellite(final List<SpacecraftState> input, final String modelPath) throws URISyntaxException, IOException {
        this(input, modelPath, DEFAULT_COLOR);
    }

    public Satellite(final List<SpacecraftState> input, final String modelPath, final Color color) throws URISyntaxException, IOException {
        final List<Double> doubleListTemp = new ArrayList<>();
        final List<Orbit> orbitTemp = new ArrayList<>();
        final List<Vector3D> vector3DTemp = new ArrayList<>();
        this.positionsList = new ArrayList<>();
        this.pathColor = color;

        this.setId(input.get(0).getOrbit().toString());
        this.setName(DEFAULT_NAME);
        this.setAvailability(Header.MASTER_CLOCK.getAvailability());

        for (SpacecraftState spacecraftState : input) {
            orbitTemp.add(spacecraftState.getOrbit());
            vector3DTemp.add(spacecraftState.getPosition());
            doubleListTemp.add(absoluteDateToJulianDateDelta(spacecraftState.getDate()));
        }

        this.description = DEFAULT_DESCRIPTION;
        this.orbits = orbitTemp;
        this.vector3DS = vector3DTemp;
        this.timeList = doubleListTemp;
        final List<Vector3D> toCartesians = propagationSat();
        this.cartesianArraylist = vectorToCartesian(toCartesians);

        this.modelPath = modelPath;
        this.model = new CzmlModel(modelPath);
        this.modelType = model.getModelType();
        this.period = orbitTemp.get(0).getKeplerianPeriod();
    }

    public Satellite(final Propagator propagator, final AbsoluteDate finalDate) throws URISyntaxException, IOException {
        this(propagator, finalDate, DEFAULT_MODEL_PATH);
    }

    public Satellite(final Propagator propagator, final AbsoluteDate finalDate, final Color color) throws URISyntaxException, IOException {
        this(propagator, finalDate, DEFAULT_MODEL_PATH, color);
    }

    public Satellite(final Propagator propagator, final AbsoluteDate finalDate, final String modelPath) throws URISyntaxException, IOException {
        this(propagator, finalDate, modelPath, DEFAULT_COLOR);
    }

    public Satellite(final Propagator propagator, final AbsoluteDate finalDate, final String modelPath, final Color color) throws URISyntaxException, IOException {

        if (propagator.getInitialState().getOrbit() == null) {
            throw new RuntimeException("The propagator has no initial state, please setup the propagator before building" +
                    "the satellite");
        }
        else {
            this.setId(propagator.getInitialState().getOrbit().toString());
            this.setName(DEFAULT_NAME);
            this.setAvailability(Header.MASTER_CLOCK.getAvailability());
            this.satellitePropagator = propagator;
            this.description = DEFAULT_DESCRIPTION;
            this.frame = propagator.getFrame();
            this.pathColor = color;

            // Creation of empty list to be filled with multiplexerSetup
            this.timeList = new ArrayList<>();
            this.absoluteDateList = new ArrayList<>();
            this.orbits = new ArrayList<>();
            this.positionsList = new ArrayList<>();

            orbits.add(propagator.getInitialState().getOrbit());
            final AbsoluteDate startDate = julianDateToAbsoluteDate(Header.MASTER_CLOCK.getAvailability().getStart(), Header.TIME_SCALE);

            // Setup propagator
            final EphemerisGenerator generator = propagator.getEphemerisGenerator();
            this.vector3DS = multiplexerSetup(propagator);

            // Propagation
            propagator.propagate(startDate, finalDate);

            // Retrieve parameters from propagation
            this.boundedPropagator = generator.getGeneratedEphemeris();
            this.cartesianArraylist = vectorToCartesian(vector3DS);
        }

        this.modelPath = modelPath;
        this.model = new CzmlModel(modelPath);
        this.modelType = model.getModelType();
        this.period = orbits.get(0).getKeplerianPeriod();
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
        this.Ephemeris = new ArrayList<>();
        this.modelPath = "";
        this.model = null;
        this.vector3DS = new ArrayList<>();
        this.timeList = new ArrayList<>();
        this.cartesianArraylist = new ArrayList<>();
        this.orbits = new ArrayList<>();
        this.frame = null;
        this.billboard = null;
        this.positionsList = new ArrayList<>();
        this.boundedPropagator = null;
        this.attitudes = new ArrayList<>();
        this.allSpaceCraftStates = new ArrayList<>();
        this.oriented = false;
        this.displayAttitude = false;
        this.orientation = null;
        this.optionalRotation = null;
        this.displayOnlyOnePeriod = false;
        this.pathColor = null;
        this.absoluteDateList = new ArrayList<>();
        this.displayReferenceSystem = false;
        this.satelliteReferenceSystem = null;
        this.satellitePropagator = null;
        this.modelType = null;
        this.satelliteAttitude = null;
        this.period = 0.0;
    }

    @Override
    public void writeCzmlBlock() throws URISyntaxException, IOException {
        OUTPUT.setPrettyFormatting(true);
        try (PacketCesiumWriter packet = STREAM.openPacket(OUTPUT)) {
            packet.writeId(getId());
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

    // GETS

    public String getDescription() {
        return description;
    }

    public List<AbsoluteDate> getAbsoluteDateList() {
        return absoluteDateList;
    }

    public List<Double> getTimeList() {
        return timeList;
    }

    public List<Attitude> getAttitudes() {
        return attitudes;
    }

    public List<Vector3D> getVector3DS() {
        return vector3DS;
    }

    public List<TimeStampedPVCoordinates> getEphemeris() {
        if (Ephemeris == null) {
            throw new RuntimeException("The satellite was not build with an OEM, so the Ephemeris is empty, try using the getOrbits method to access the time and positions");
        } else {
            return Ephemeris;
        }
    }

    public List<Orbit> getOrbits() {
        return orbits;
    }

    public Propagator getSatellitePropagator() {
        return satellitePropagator;
    }

    public List<Cartesian> getCartesianArraylist() {
        return cartesianArraylist;
    }

    public Frame getFrame() {
        return frame;
    }

    public Color getPathColor() {
        return pathColor;
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

    public SatelliteAttitude getSatelliteAttitude() {
        return satelliteAttitude;
    }

    public List<Position> getPositionsList() {
        return positionsList;
    }

    public Orientation getOrientation() {
        if (orientation != null) {
            return orientation;
        }
        else {
            throw new RuntimeException("The satellite did not display the orientation, please use the displaySatelliteAttitude() method first");
        }
    }

    public BoundedPropagator getBoundedPropagator() {
        return boundedPropagator;
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

    public List<SpacecraftState> getAllSpaceCraftStates() {
        return allSpaceCraftStates;
    }

    // SETS
    public void setAttitudes(final List<Attitude> attitudes) {
        this.attitudes = attitudes;
    }

    public void setBoundedPropagator(final BoundedPropagator boundedPropagator) {
        this.boundedPropagator = boundedPropagator;
    }

    public void setOrientation(final Orientation orientation) {
        oriented = true;
        this.orientation = orientation;
    }

    // User functions

    public void displayOnlyOnePeriod() {
        displayOnlyOnePeriod = true;
    }

    public void displaySatelliteAttitude() throws URISyntaxException, IOException {
        this.displayAttitude = true;
        orientationSetup();
    }

    public void displaySatelliteReferenceSystem() throws URISyntaxException, IOException {
        this.displayReferenceSystem = true;
        this.satelliteReferenceSystem = new SatelliteReferenceSystem(this);
    }

    public void setOptionalRotation(final Rotation inputRotation) {
        optionalRotation = inputRotation;
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
        }
        else if (getModelType() == ModelType.MODEL_3D)
        {
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
            }
            else {
                if (!oriented) {
                    this.orientation = new Orientation(getAttitudes(), getFrame(), false, optionalRotation);
                }
                this.orientation.write(packet, OUTPUT);
            }
            this.getModel().generateCZML(packet, OUTPUT);
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
                pathProperty.writeLeadTimeProperty(this.getOrbits().get(0).getKeplerianPeriod());
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
                    solidColorWriter.writeColorProperty(getPathColor());
                }
                OUTPUT.writeEndObject();
            }
        }
    }

    private void czmlPosition(final PacketCesiumWriter packet) {

        final SatellitePosition Pos = new SatellitePosition(this.getCartesianArraylist(), getTimeList());

        try (PositionCesiumWriter writer = packet.openPositionProperty()) {
            writer.writeReferenceFrame(Pos.getReferenceFrame());
            writer.writeInterpolationAlgorithm(Pos.getCesiumInterpolationAlgorithm());
            writer.writeInterpolationDegree(Pos.getInterpolationDegree());
            writer.writeCartesian(Pos.getDates(), Pos.getPositions());
        }
    }

    private double getStepBetweenDates() {
        final List<TimeStampedPVCoordinates> Ephemeris_temp = this.getEphemeris();

        final AbsoluteDate Date0 = Ephemeris_temp.get(0).getDate();
        final AbsoluteDate Date1 = Ephemeris_temp.get(1).getDate();

        return Date1.durationFrom(Date0);
    }

    private List<Cartesian> vectorToCartesian(final List<Vector3D> initPositionList) {

        final List<Cartesian> cartesianList = new ArrayList<>();

        for (Vector3D position : getVector3DS()) {
            final Cartesian posCartesian = new Cartesian(position.getX(), position.getY(), position.getZ());
            cartesianList.add(posCartesian);
        }
        return cartesianList;
    }

    private List<Vector3D> propagationSat() {

        final Orbit initialOrbit = this.getOrbits().get(0);
        final OrbitType propagationType = OrbitType.CARTESIAN;
        final SpacecraftState initialState = new SpacecraftState(initialOrbit);
        final List<Vector3D> toReturn;

        final double positionTolerance = 10.0;
        final double minStep = 0.001;
        final double maxStep = 1000.0;
        timeList = new ArrayList<>();
        absoluteDateList = new ArrayList<>();

        final double[][] tolerances = NumericalPropagator.tolerances(positionTolerance, initialOrbit, propagationType);
        final AdaptiveStepsizeIntegrator integrator = new DormandPrince853Integrator(minStep, maxStep, tolerances[0], tolerances[1]);

        final NumericalPropagator propagator = new NumericalPropagator(integrator);

        final NormalizedSphericalHarmonicsProvider provider = GravityFieldFactory.getNormalizedProvider(10, 10);
        final Frame frame_temp = this.getFrame();
        final ForceModel holmesFeatherstone = new HolmesFeatherstoneAttractionModel(frame_temp, provider);

        propagator.setOrbitType(propagationType);

        propagator.addForceModel(holmesFeatherstone);

        final EphemerisGenerator generator = propagator.getEphemerisGenerator();

        propagator.setInitialState(initialState);

        toReturn = multiplexerSetup(propagator);

        final GregorianDate gregorianDate = new GregorianDate(getAvailability().getStop());
        final TimeScale UTC = TimeScalesFactory.getUTC();
        final AbsoluteDate stopDate = new AbsoluteDate(gregorianDate.getYear(), gregorianDate.getMonth(),
                gregorianDate.getDay(), gregorianDate.getHour(), gregorianDate.getMinute(), gregorianDate.getSecond(), UTC);

        propagator.propagate(stopDate);
        this.satellitePropagator = propagator;
        this.boundedPropagator = generator.getGeneratedEphemeris();

        return toReturn;
    }

    private List<Cartesian> propagationOrbit(final Orbit initialOrbit) {

        final List<Vector3D> propagatedParameters = propagationSat();

        this.vector3DS = propagatedParameters;

        final List<Cartesian> cartesians = vectorToCartesian(propagatedParameters);

        this.cartesianArraylist = cartesians;

        return cartesians;
    }

    private List<Vector3D> multiplexerSetup(final Propagator propagator) {

        final List<Vector3D> toReturn = new ArrayList<>();

        propagator.getMultiplexer().add(Header.MASTER_CLOCK.getMultiplier(), new OrekitFixedStepHandler() {
            @Override
            public void handleStep(final SpacecraftState currentState) {
                final KeplerianOrbit o = (KeplerianOrbit) OrbitType.KEPLERIAN.convertType(currentState.getOrbit());
                toReturn.add(o.getPosition());
                allSpaceCraftStates.add(currentState);
                final Attitude currentSpaceCraftAttitude = currentState.getAttitude();
                attitudes.add(currentSpaceCraftAttitude);
                final double x = toReturn.get(toReturn.size() - 1).getX();
                final double y = toReturn.get(toReturn.size() - 1).getY();
                final double z = toReturn.get(toReturn.size() - 1).getZ();
                final PositionType positionType = PositionType.CARTESIAN_POSITION;
                final Position tempPosition = new Position(x, y, z, positionType);
                positionsList.add(tempPosition);
                timeList.add(absoluteDateToJulianDateDelta(o.getDate()));
                absoluteDateList.add(o.getDate());
                orbits.add(o);
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
