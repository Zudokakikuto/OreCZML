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

import cesiumlanguagewriter.BooleanCesiumWriter;
import cesiumlanguagewriter.Cartesian;
import cesiumlanguagewriter.CesiumOutputStream;
import cesiumlanguagewriter.CesiumStreamWriter;
import cesiumlanguagewriter.JulianDate;
import cesiumlanguagewriter.OrientationCesiumWriter;
import cesiumlanguagewriter.PacketCesiumWriter;
import cesiumlanguagewriter.PathCesiumWriter;
import cesiumlanguagewriter.PolylineMaterialCesiumWriter;
import cesiumlanguagewriter.SolidColorMaterialCesiumWriter;
import cesiumlanguagewriter.TimeInterval;
import org.hipparchus.geometry.euclidean.threed.Rotation;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.orekit.annotation.DefaultDataContext;
import org.orekit.attitudes.Attitude;
import org.orekit.czml.errors.OreCzmlException;
import org.orekit.czml.errors.OreCzmlMessages;
import org.orekit.czml.object.ModelType;
import org.orekit.czml.object.Path;
import org.orekit.czml.object.nonvisual.CzmlModel;
import org.orekit.czml.object.primary.AbstractPrimaryObject;
import org.orekit.czml.object.primary.systems.SpacecraftReferenceSystem;
import org.orekit.czml.object.secondary.Clock;
import org.orekit.czml.object.secondary.Orientation;
import org.orekit.czml.object.secondary.TimePosition;
import org.orekit.czml.object.utils.DateUtils;
import org.orekit.czml.object.utils.InfluenceSphereUtils;
import org.orekit.errors.OrekitException;
import org.orekit.frames.Frame;
import org.orekit.orbits.Orbit;
import org.orekit.propagation.BoundedPropagator;
import org.orekit.propagation.Propagator;
import org.orekit.propagation.SpacecraftState;
import org.orekit.time.AbsoluteDate;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Spacecraft class.
 * <p>
 * This class represents the Spacecraft object to be displayed. It can be build
 * from many orekit outputs and admits several functions to display or not
 * intrinsic parameters.
 * </p>
 * <p>
 * Each Spacecraft object created will imply a propagation with default
 * parameters, if you want you own parameters in the propagator, you can still
 * build a Spacecraft with a propagator to do so.
 * </p>
 * <p>
 * The Spacecraft object admits also 2D and 3D models. By default builders will
 * load a 2D image to represent the Spacecraft, but each builder is overloaded
 * with a path to the 3D model to charge your own 2D or 3D model.
 * </p>
 *
 * @author Julien LEBLOND
 * @since 1.0.0
 */
public class Spacecraft
    extends
    AbstractPrimaryObject {

    /**
     * The default model path, empty.
     */
    public static final String DEFAULT_MODEL_PATH =
        new File(Spacecraft.class.getClassLoader().getResource("satellite.png")
            .getFile()).toPath().toString();

    /**
     * The default id of the Spacecraft.
     */
    public static final String DEFAULT_ID = "SPACECRAFT/";

    /**
     * The default name of the Spacecraft.
     */
    public static final String DEFAULT_NAME = "Spacecraft";

    /** The default id when a path if created when influence sphere are used. */
    public static final String DEFAULT_ID_PATH_INFLUENCE_SPHERE =
        "PATH INSIDE INFLUENCE SPHERE:";

    /**
     * The default name when a path if created when influence sphere are used.
     */
    public static final String DEFAULT_NAME_PATH_INFLUENCE_SPHERE =
        "Path inside the sphere of influence of :";

    /** The default reference position tool. */
    public static final String H_REFERENCE_POSITION = "#position";

    /** The default format for the formatted ID. */
    public static final String DEFAULT_FORMAT =
        DEFAULT_ID + "{P(%1.8e, %2.8e, %3.8e), V(%4.8e, %5.8e, %6.8e)}";

    /**
     * The default orbit color of the Spacecraft.
     */
    public static final Color DEFAULT_COLOR = new Color(255, 255, 255);

    /**
     * If the Spacecraft already has an attitude defined or not.
     */
    private boolean oriented = false;

    // Optional parameters

    /**
     * To display or not only one period. By default, display all the paths.
     */
    private boolean displayOnlyOnePeriod = false;

    /**
     * To display or not the attitude of the Spacecraft. By default, the
     * Spacecraft is oriented in TNW in the local orbital frame.
     */
    private boolean displayAttitude = false;

    /**
     * To display or not the Spacecraft reference system.
     */
    private boolean displayReferenceSystem = false;

    /** To display or not the influence sphere changes. */
    private boolean displayInfluenceSphereChanges = false;

    /** The dates of changes in the sphere of influence. */
    private List<TimeInterval> intervalInfluenceSpheres;

    /**
     * The list containing the list of position of the satellite splitted for
     * each influence sphere.
     */
    private List<List<Cartesian>> positionInsideInfluenceSphere;

    /**
     * The list containing the list of time of the satellite splitted for each
     * influence sphere.
     */
    private List<List<JulianDate>> timeInsideInfluenceSphere;

    // Orekit arguments

    /**
     * The list of the attitudes of the Spacecraft.
     */
    private List<Attitude> attitudes = new ArrayList<>();

    /**
     * The list of the spacecraft states of the Spacecraft.
     */
    private final List<SpacecraftState> spaceCraftStates = new ArrayList<>();

    /**
     * The optional rotation applied to the attitude of the Spacecraft.
     */
    private Rotation optionalRotation;

    /**
     * The frame in which the Spacecraft is computed.
     */
    private final Frame frame;

    // Writing arguments

    /**
     * The orientation in the local orbital frame of the Spacecraft.
     */
    private Orientation orientation;

    /**
     * The propagator of the Spacecraft.
     */
    private Propagator spacecraftPropagator;

    /**
     * The Spacecraft reference system object.
     */
    private SpacecraftReferenceSystem spacecraftReferenceSystem = null;

    /**
     * The color of the orbit.
     */
    private Color color;

    /**
     * The CzmlModel for the display of the model.
     */
    private final CzmlModel model;

    /** The type of the model. */
    private final ModelType modelType;

    /** The start date of the propagation. */
    private final AbsoluteDate startDate;

    /** The final date of the propagation. */
    private final AbsoluteDate finalDate;

    /** The time step size for the propagation. */
    private final double clockMultiplier;

    /** The description of the Spacecraft. */
    private String description;

    /** The clock of the spacecraft. */
    private final Clock clock;

    // Constructor

    /**
     * The basic Spacecraft constructor, it only needs one bounded propagator.
     * It uses default parameters.
     *
     * @param propagator : A bounded propagator resulting from an already done
     *        propagation.
     * @param clock : The clock used for the spacecraft
     * @throws URISyntaxException the uri syntax exception
     * @throws IOException the io exception
     */
    public Spacecraft(final BoundedPropagator propagator, final Clock clock)
        throws URISyntaxException,
            IOException {
        this(propagator, propagator.getMinDate(), propagator.getMaxDate(),
             clock.getMultiplier(), DEFAULT_MODEL_PATH, DEFAULT_COLOR,
             String.format(DEFAULT_FORMAT,
                           propagator.getInitialState().getPosition().getX(),
                           propagator.getInitialState().getPosition().getY(),
                           propagator.getInitialState().getPosition().getZ(),
                           propagator.getInitialState().getPVCoordinates()
                               .getVelocity().getX(),
                           propagator.getInitialState().getPVCoordinates()
                               .getVelocity().getY(),
                           propagator.getInitialState().getPVCoordinates()
                               .getVelocity().getZ()));
    }

    /**
     * The Spacecraft constructor with no default parameters.
     *
     * @param propagator : A bounded propagator resulting from an already done
     *        propagation.
     * @param startDateInput : The start date to consider for the start of the
     *        propagation and the availability of the Spacecraft.
     * @param finalDateInput : The stop date to consider for the stop the
     *        propagation and the availability of the Spacecraft.
     * @param modelPath : The path to the model to load.
     * @param color : The color of the orbit.
     * @param customID : The custom ID of the Spacecraft.
     * @param clockMultiplier : The clock multiplier
     * @throws URISyntaxException the uri syntax exception
     * @throws IOException the io exception
     */
    @DefaultDataContext
    public Spacecraft(final BoundedPropagator propagator,
                      final AbsoluteDate startDateInput,
                      final AbsoluteDate finalDateInput,
                      final double clockMultiplier, final String modelPath,
                      final Color color, final String customID)
        throws URISyntaxException,
            IOException {

        this.setId(customID);
        this.setName(DEFAULT_NAME);
        this.setAvailability(new TimeInterval(DateUtils
            .toJulianDate(startDateInput), DateUtils.toJulianDate(finalDateInput)));
        this.spacecraftPropagator = propagator;
        this.description =
            "<!--HTML-->\r\n<p>Id : " +
                           customID + "</p>\r\n<p>" + "Simulated from : " +
                           startDateInput + " to " + finalDateInput + "</p>";
        this.frame = propagator.getFrame();
        this.color = color;
        this.clock = new Clock(startDateInput, finalDateInput, clockMultiplier);
        this.model = new CzmlModel(modelPath, true, clock);
        this.modelType = model.getModelType();
        this.startDate = startDateInput;
        this.finalDate = finalDateInput;
        this.clockMultiplier = clockMultiplier;

        // Setup propagator
        multiplexerSetup(propagator);
        // Propagation
        spacecraftPropagator.propagate(startDate, finalDate);
        propagator.clearStepHandlers();
    }

    // Builder

    /**
     * Builder Spacecraft builder.
     *
     * @param propagator the propagator
     * @param clock the clock
     * @return the Spacecraft builder
     */
    public static SpacecraftBuilder builder(final BoundedPropagator propagator,
                                            final Clock clock) {
        return new SpacecraftBuilder(propagator, clock);
    }

    /**
     * Builder Spacecraft builder.
     *
     * @param propagator the propagator
     * @param clockMultiplierInput the clock multiplier
     * @return the Spacecraft builder
     */
    public static SpacecraftBuilder builder(final BoundedPropagator propagator,
                                            final double clockMultiplierInput) {
        return new SpacecraftBuilder(propagator, clockMultiplierInput);
    }

    // Overrides

    @Override
    public void writeCzmlBlock(final CesiumStreamWriter stream,
                               final CesiumOutputStream output)
        throws URISyntaxException,
            IOException {

        output.setPrettyFormatting(true);
        // If the influence sphere are to be displayed, the path must be written
        // in several objects
        if (!displayInfluenceSphereChanges) {
            try (PacketCesiumWriter packet = stream.openPacket(output)) {
                packet.writeId(this.getId());
                packet.writeName(getName());
                packet.writeAvailability(getAvailability());
                packet.writeDescriptionProperty(description);

                czmlDisplay(packet, output);

                czmlPath(packet, output, getColor());

                czmlPosition(packet, output, getCartesianArraylist(),
                             getJulianDates());

            } catch (URISyntaxException | IOException e) {
                throw new RuntimeException(e);
            }
            if (getDisplayReferenceSystem()) {
                getSpacecraftReferenceSystem().writeCzmlBlock(stream, output);
            }
        } else {
            final List<Color> colors =
                this.colorWheel(intervalInfluenceSpheres.size());
            for (int i = 0; i < intervalInfluenceSpheres.size(); i++) {
                final List<Cartesian> cartesians =
                    positionInsideInfluenceSphere.get(i);
                final List<JulianDate> julianDates =
                    timeInsideInfluenceSphere.get(i);
                try (PacketCesiumWriter packet = stream.openPacket(output)) {
                    packet.writeId(this.getId() +
                                   " " + DEFAULT_ID_PATH_INFLUENCE_SPHERE +
                                   " " + i);
                    packet.writeName(DEFAULT_NAME_PATH_INFLUENCE_SPHERE +
                                     " " + i);
                    packet.writeAvailability(getAvailability());
                    packet
                        .writeDescriptionProperty("Path representing the " +
                                                  i +
                                                  "the path of the spacecraft " +
                                                  getId() +
                                                  " going through influence spheres");

                    czmlPath(packet, output, colors.get(i));
                    czmlDisplay(packet, output);
                    czmlPosition(packet, output, cartesians, julianDates);
                }
            }
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
     * Display Spacecraft attitude.
     */
    public void displaySpacecraftAttitude() {
        this.displayAttitude = true;
        orientationSetup();
    }

    /**
     * Display Spacecraft reference system.
     */
    public void displaySpacecraftReferenceSystem() {
        this.displayReferenceSystem = true;
        this.spacecraftReferenceSystem = new SpacecraftReferenceSystem(this);
    }

    /**
     * Detects and prints all dates when the spacecraft changes the sphere of
     * influence.
     *
     * @param bodies list of celestial bodies with defined influence spheres
     * @param centralBody the main reference body (e.g., the Sun)
     */
    public void displayInfluenceSphereChanges(final List<Body> bodies,
                                              final Body centralBody) {

        final Frame centralFrame =
            centralBody.getCelestialBody().getInertiallyOrientedFrame();

        final List<AbsoluteDate> datesChanges =
            InfluenceSphereUtils
                .findCrossingSphereDates(bodies, centralFrame,
                                         this.getSpaceCraftStates(), this.frame,
                                         this.finalDate);

        if (datesChanges.size() > 2) {
            displayInfluenceSphereChanges = true;
            final List<JulianDate> datesChangesJulian =
                DateUtils.toJulianDates(datesChanges);
            this.intervalInfluenceSpheres =
                DateUtils.createTimeIntervals(datesChangesJulian);
            // We remove the first interval because it starts at the 01/01/1900
            // to the first date of the simulation, which is useless here.
            this.intervalInfluenceSpheres.remove(0);
            this.positionInsideInfluenceSphere =
                InfluenceSphereUtils
                    .findPositionInsideInfluenceSpheres(getCartesianArraylist(),
                                                        getJulianDates(),
                                                        intervalInfluenceSpheres);
            this.timeInsideInfluenceSphere =
                InfluenceSphereUtils
                    .findTimeInsideInfluenceSpheres(getJulianDates(),
                                                    intervalInfluenceSpheres);
        }
    }

    // Sphere of influence

    // Getters

    /**
     * Gets spacecraft states.
     *
     * @return the spacecraft states
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
            toReturn.add(new Cartesian(currentVector3D.getX(),
                                       currentVector3D.getY(),
                                       currentVector3D.getZ()));
        }
        return Collections.unmodifiableList(toReturn);
    }

    /**
     * Gets julian dates.
     *
     * @return the julian dates
     */
    public List<JulianDate> getJulianDates() {
        return DateUtils.toJulianDates(getAbsoluteDateList());
    }

    /**
     * Gets Spacecraft propagator.
     *
     * @return the Spacecraft propagator
     */
    public Propagator getSpacecraftPropagator() {
        return spacecraftPropagator;
    }

    /**
     * Gets Spacecraft bounded propagator.
     *
     * @return the Spacecraft bounded propagator
     */
    public BoundedPropagator getSpacecraftBoundedPropagator() {
        return (BoundedPropagator) spacecraftPropagator;
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
     * Gets the clock.
     *
     * @return the clock
     */
    public Clock getClock() {
        return clock;
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
     * Gets Spacecraft reference system.
     *
     * @return the Spacecraft reference system
     */
    public SpacecraftReferenceSystem getSpacecraftReferenceSystem() {
        return spacecraftReferenceSystem;
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
        final double period;
        try {
            period =
                spacecraftPropagator.getInitialState().getKeplerianPeriod();
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
        this.spacecraftPropagator = boundedPropagator;
    }

    /**
     * Sets attitudes.
     *
     * @param attitudes the attitudes
     */
    public void setAttitudes(final List<Attitude> attitudes) {
        this.attitudes = new ArrayList<>(attitudes);
        this.orientation = new Orientation(attitudes, getFrame());
        oriented = true;
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
        this.attitudes = new ArrayList<>();
        this.orientation = null;
        oriented = false;
    }

    // Private functions

    /**
     * This function set a multiplexer for the propagator of the Spacecraft. It
     * will handle the retrieving of the spacecraft states and of the attitudes.
     *
     * @param propagator : The propagator of the Spacecraft.
     */
    private void multiplexerSetup(final Propagator propagator) {

        propagator.getMultiplexer().add(clockMultiplier, currentState -> {
            spaceCraftStates.add(currentState);
            final Attitude currentSpaceCraftAttitude =
                currentState.getAttitude();
            attitudes.add(currentSpaceCraftAttitude);
        });
    }

    /**
     * This function aims at writing the global display of the Spacecraft, it
     * ensures that the model loaded and the orientation are correct.
     *
     * @param packet : The packet that will write in the czml file.
     * @param output : The output stream of cesium that will contain the strings
     *        to write into the CzmLFile.
     */
    private void czmlDisplay(final PacketCesiumWriter packet,
                             final CesiumOutputStream output)
        throws URISyntaxException,
            IOException {

        if (getModelType() == ModelType.MODEL_2D ||
            getModelType() == ModelType.EMPTY_MODEL) {
            getModel().generateCZML(packet, output);
            if (getDisplayAttitude()) {
                orientation.write(packet, output);
            }
        } else if (getModelType() == ModelType.MODEL_3D) {
            if (!getDisplayAttitude()) {

                try (OrientationCesiumWriter orientationWriter =
                    packet.getOrientationWriter()) {
                    orientationWriter.open(output);
                    orientationWriter
                        .writeVelocityReference(this.getId() +
                                                H_REFERENCE_POSITION);
                    this.orientation = null;
                }
            } else {
                if (!oriented) {
                    this.orientation =
                        new Orientation(attitudes, getFrame(), false,
                                        optionalRotation);
                }
                this.orientation.write(packet, output);
            }
            this.getModel().generateCZML(packet, output);
        }
    }

    /**
     * This function writes the path of the Spacecraft to display the orbit.
     *
     * @param packet : The packet that will write in the czml file.
     * @param output : The output stream of cesium that will contain the strings
     *        to write into the CzmLFile.
     * @param colorInput : The color of the path
     */
    private void czmlPath(final PacketCesiumWriter packet,
                          final CesiumOutputStream output,
                          final Color colorInput) {
        try (PathCesiumWriter pathProperty = packet.openPathProperty()) {
            // If only one period of the orbit is displayed (and that the period
            // is less long than the time of simulation, elseway this will not
            // do anything visible)
            // Then the path must have an availability only of the duration of
            // the period
            if (!getDisplayOnlyOnePeriod()) {
                final Path path = new Path(getAvailability());
                try (BooleanCesiumWriter showPath =
                    pathProperty.openShowProperty()) {
                    showPath.writeInterval(getAvailability().getStart(),
                                           getAvailability().getStop());
                    showPath.writeBoolean(path.getShow());
                }
            }
            // If not, then we display all the path of the orbit during the
            // entire simulation
            else {
                final Path path = new Path(getAvailability());
                // If the keplerian period is incredibly big, we fix the display
                // of the path to 1 h
                if ((this.getOrbits().get(0).getKeplerianPeriod() + "")
                    .equals("Infinity")) {
                    pathProperty.writeLeadTimeProperty(3600);
                } else {
                    pathProperty.writeLeadTimeProperty(this.getOrbits().get(0)
                        .getKeplerianPeriod());
                }
                pathProperty.writeTrailTimeProperty(0.0);
                try (BooleanCesiumWriter showPath =
                    pathProperty.openShowProperty()) {
                    showPath.writeInterval(getAvailability().getStart(),
                                           getAvailability().getStop());
                    showPath.writeBoolean(path.getShow());
                }
            }
            // Writing of the color of the path
            try (PolylineMaterialCesiumWriter materialWriter =
                pathProperty.getMaterialWriter()) {
                materialWriter.open(output);
                output.writeStartObject();
                try (SolidColorMaterialCesiumWriter solidColorWriter =
                    materialWriter.getSolidColorWriter()) {
                    solidColorWriter.open(output);
                    solidColorWriter.writeColorProperty(colorInput);
                }
                output.writeEndObject();
            }
        }
    }

    /**
     * This function writes the position of the Spacecraft in time.
     *
     * @param packet : The packet that will write in the czml file.
     * @param output : The output stream of cesium that will contain the strings
     *        to write into the CzmLFile.
     * @param cartesians : The cartesians coordinates to be written
     * @param julianDates : The JulianDate corresponding the cartesian
     *        coordinates
     */
    private void czmlPosition(final PacketCesiumWriter packet,
                              final CesiumOutputStream output,
                              final List<Cartesian> cartesians,
                              final List<JulianDate> julianDates) {

        final TimePosition timePosition =
            new TimePosition(cartesians, julianDates);
        timePosition.write(packet, output);
    }

    /**
     * This function is used when the attitude of the Spacecraft must be
     * displayed. To do so, this function defines an orientation object
     * depending on the type of the model loaded.s
     */
    private void orientationSetup() {
        if (!oriented) {
            if (getModelType() == ModelType.MODEL_2D ||
                getModelType() == ModelType.EMPTY_MODEL) {
                if (displayAttitude) {
                    this.orientation =
                        Orientation.builder(getAttitudes(), getFrame())
                            .withOptionalRotation(optionalRotation)
                            .withInvertToITRF(false).build();
                    oriented = true;
                }
            } else if (getModelType() == ModelType.MODEL_3D) {
                if (!displayAttitude) {
                    this.orientation = null;
                } else {
                    this.orientation =
                        Orientation.builder(getAttitudes(), getFrame())
                            .withOptionalRotation(optionalRotation)
                            .withInvertToITRF(false).build();
                    oriented = true;
                }
            }
        }
    }

}
