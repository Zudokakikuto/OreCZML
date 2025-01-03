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
import org.orekit.bodies.CelestialBodyFactory;
import org.orekit.czml.errors.OreCzmlException;
import org.orekit.czml.errors.OreCzmlMessages;
import org.orekit.czml.object.ModelType;
import org.orekit.czml.object.Path;
import org.orekit.czml.object.Utils.DateUtils;
import org.orekit.czml.object.nonvisual.CzmlModel;
import org.orekit.czml.object.primary.AbstractPrimaryObject;
import org.orekit.czml.object.primary.Header;
import org.orekit.czml.object.primary.systems.SpacecraftReferenceSystem;
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
 * Spacecraft class.
 *
 * <p> This class represents the Spacecraft object to be displayed. It can be build from many orekit outputs and admits
 * several functions to display or not intrinsic parameters.</p>
 *
 * <p> Each Spacecraft object created will imply a propagation with default parameters, if you want you own parameters
 * in the propagator, you can still build a Spacecraft with a propagator to do so.</p>
 *
 * <p> The Spacecraft object admits also 2D and 3D models. By default builders will load a 2D image to represent the Spacecraft,
 * but each builder is overloaded with a path to the 3D model to charge your own 2D or 3D model.</p>
 *
 * @author Julien LEBLOND
 * @since 1.0.0
 */
public class Spacecraft extends AbstractPrimaryObject {

    /**
     * The default model path, empty.
     */
    public static final String DEFAULT_MODEL_PATH = "";

    /**
     * The default id of the Spacecraft.
     */
    public static final String DEFAULT_ID = "SPACECRAFT/";

    /**
     * The default name of the Spacecraft.
     */
    public static final String DEFAULT_NAME = "Spacecraft";

    /** The default format for the formatted ID. */
    public static final String DEFAULT_FORMAT = DEFAULT_ID + "{P(%1.8e, %2.8e, %3.8e), V(%4.8e, %5.8e, %6.8e)}";

    /** The intertial frame used in the position. */
    public static final String DEFAULT_INERTIAL = "INERTIAL";

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
     * To display or not the attitude of the Spacecraft. By default, the Spacecraft is oriented in TNW in the local orbital frame.
     */
    private boolean displayAttitude = false;

    /**
     * To display or not the Spacecraft reference system.
     */
    private boolean displayReferenceSystem = false;

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
     * The period of the orbit.
     */
    private double period;

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

    /**
     * The type of the model.
     */
    private final ModelType modelType;

    /** The start date of the propagation. */
    private final AbsoluteDate startDate;

    /** The final date of the propagation. */
    private final AbsoluteDate finalDate;

    /** The header used for the Spacecraft. */
    private final Header header;

    /** The description of the Spacecraft. */
    private String description;


    // Constructor

    /**
     * The basic Spacecraft constructor, it only needs one bounded propagator. It uses default parameters.
     *
     * @param propagator : A bounded propagator resulting from an already done propagation.
     * @param header     : The header considered.
     * @throws URISyntaxException the uri syntax exception
     * @throws IOException        the io exception
     */
    public Spacecraft(final BoundedPropagator propagator, final Header header) throws URISyntaxException, IOException {
        this(propagator, propagator.getMinDate(), propagator.getMaxDate(), DEFAULT_MODEL_PATH, DEFAULT_COLOR,
                String.format(DEFAULT_FORMAT, propagator.getInitialState()
                                                        .getPosition()
                                                        .getX(), propagator.getInitialState()
                                                                           .getPosition()
                                                                           .getY(), propagator.getInitialState()
                                                                                              .getPosition()
                                                                                              .getZ(),
                        propagator.getInitialState()
                                  .getPVCoordinates()
                                  .getVelocity()
                                  .getX(), propagator.getInitialState()
                                                     .getPVCoordinates()
                                                     .getVelocity()
                                                     .getY(), propagator.getInitialState()
                                                                        .getPVCoordinates()
                                                                        .getVelocity()
                                                                        .getZ()), header);
    }

    /**
     * The Spacecraft constructor with no default parameters.
     *
     * @param propagator     : A bounded propagator resulting from an already done propagation.
     * @param startDateInput : The start date to consider for the start of the propagation and the availability of the Spacecraft.
     * @param finalDateInput : The stop date to consider for the stop the propagation and the availability of the Spacecraft.
     * @param modelPath      : The path to the model to load.
     * @param color          : The color of the orbit.
     * @param customID       : The custom ID of the Spacecraft.
     * @param header         : The header considered when several are used.
     * @throws URISyntaxException the uri syntax exception
     * @throws IOException        the io exception
     */
    public Spacecraft(final BoundedPropagator propagator, final AbsoluteDate startDateInput,
                      final AbsoluteDate finalDateInput, final String modelPath, final Color color,
                      final String customID, final Header header) throws URISyntaxException, IOException {

        this.setId(customID);
        this.setName(DEFAULT_NAME);
        this.setAvailability(new TimeInterval(DateUtils.toJulianDate(startDateInput, header.getTimeScale()),
                DateUtils.toJulianDate(finalDateInput, header.getTimeScale())));
        this.spacecraftPropagator = propagator;
        this.description          = "<!--HTML-->\r\n<p>Id : " + customID + "</p>\r\n<p>" + "Simulated from : " + startDateInput + " to " + finalDateInput + "</p>";
        this.frame                = propagator.getFrame();
        this.color                = color;
        this.model                = new CzmlModel(modelPath, true, header);
        this.modelType            = model.getModelType();
        this.startDate            = startDateInput;
        this.finalDate            = finalDateInput;
        this.header               = header;
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
     * @param header     the header
     * @return the Spacecraft builder
     */
    public static SpacecraftBuilder builder(final BoundedPropagator propagator, final Header header) {
        return new SpacecraftBuilder(propagator, header);
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
            getSpacecraftReferenceSystem().writeCzmlBlock(stream, output);
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
        this.displayReferenceSystem    = true;
        this.spacecraftReferenceSystem = new SpacecraftReferenceSystem(this, header);
    }

    /**
     * TODO : The distance between the body and the spacecraft is not matching what is displayed on screen for no reasons.
     *  To fix or delete in the future.
     */
    public void displayInfluenceSphereChanges(final List<Body> bodies) {
        // We will take all the position and coordinates in the same frame: the frame of the sun
        // So that all will be referenced to this system, and we will be able to measure distances.
        final Frame sunFrame = CelestialBodyFactory.getSun()
                                                   .getInertiallyOrientedFrame();

        final List<InfluenceSphere> spheres       = new ArrayList<>();
        final List<Double>          spheresRadius = new ArrayList<>();
        final List<Color>           colorList     = colorWheel(bodies.size());
        final List<AbsoluteDate>    datesChanges  = new ArrayList<>();


        // Build the influence spheres
        buildInfluenceSphere(bodies, spheres, spheresRadius);

        // Sort the radiuses of the bodies considered.
        Collections.sort(spheresRadius);
        // Sorting the influence sphere and the bodies in accordance to the radiuses.
        final List<Body>            bodiesSorted  = sortedBodies(spheresRadius, bodies);
        final List<InfluenceSphere> spheresSorted = sortedSpheres(spheresRadius, spheres);

        // Determine the influence sphere in which the spacecraft is
        // Get the position of the spacecraft
        final Vector3D initialPosition = this.getSpaceCraftStates()
                                             .get(0)
                                             .getPVCoordinates(sunFrame)
                                             .getPosition();

        // Compute the initial influence sphere where the spacecraft is
        InfluenceSphere lastKnownInfluenceSphere = computeInitialInfluenceSphere(spheresSorted, spheresRadius,
                bodiesSorted, initialPosition, sunFrame);

        // If the initial influence sphere is not referenced in the inputs, throw this error.
        if (lastKnownInfluenceSphere == null) {
            throw new OreCzmlException(OreCzmlMessages.NOT_INSIDE_AN_INFLUENCE_SPHERE);
        }

        System.out.println(lastKnownInfluenceSphere.getName());

        // Add the beginning date of the simulation to this list. We will add the end date of the simulation too.
        datesChanges.add(this.startDate);

        // Let's iterate on the states then in the body.
        // We will check if the spacecraft at each state in inside a new influence sphere or not.
        for (int i = 0; i < this.getSpaceCraftStates()
                                .size(); i++) {
            final SpacecraftState state = this.getSpaceCraftStates()
                                              .get(i);
            for (final Body currentBody : bodies) {
                final InfluenceSphere currentInfluenceSphere = currentBody.getInfluenceSphere();
                final AbsoluteDate    currentDate            = state.getDate();

                // Compute the position of the spacecraft in the frame of the sun.
                final Vector3D currentPosition = state.getPVCoordinates(currentBody.getCelestialBody()
                                                                                   .getBodyOrientedFrame())
                                                      .getPosition();

                // Compute the position of the body in the frame of the sun.
                final Vector3D currentPositionCurrentBody = currentBody.getCelestialBody()
                                                                       .getPosition(currentDate,
                                                                               currentBody.getCelestialBody()
                                                                                          .getBodyOrientedFrame());

                // Compute the distance between those two entities.
                final double distance = currentPosition.distance(currentPositionCurrentBody);

                // If the distance in less than the radius of the given body.
                // And if the body is not the last one registered as the main one.
                // Then save the date as a date when the main influence sphere attracting the spacecraft changed.
                if (distance <= currentInfluenceSphere.getRadius() && (!(lastKnownInfluenceSphere.getBody()
                                                                                                 .getName()
                                                                                                 .equals(currentInfluenceSphere.getBody()
                                                                                                                               .getName())))) {

                    System.out.println("Radius influence sphere : " + currentBody.getInfluenceSphere()
                                                                                 .getRadius());
                    System.out.println("Distance : " + distance);

                    System.out.println("Name current influence sphere : " + lastKnownInfluenceSphere.getBody()
                                                                                                    .getName());
                    System.out.println("Name replacing influence sphere : " + spheres.get(i)
                                                                                     .getName());

                    lastKnownInfluenceSphere = spheres.get(i);
                    datesChanges.add(state.getDate());
                }
//                if (currentPosition.distance(currentPositionCurrentBody) > currentInfluenceSphere.getRadius()) {
//                    for (int j = 0; j < spheresRadius.size(); j++) {
//                        // The radiuses are sorted by size. We will stop on the first that we find being the closest.
//                        final double currentRadius     = spheresRadius.get(j);
//                        final Body   sortedCurrentBody = bodiesSorted.get(j);
//                        final Frame inertialFrameSortedBody = sortedCurrentBody.getCelestialBody()
//                                                                               .getInertiallyOrientedFrame();
//                        final Vector3D currentPositionSortedBody = sortedCurrentBody.getCelestialBody()
//                                                                                    .getPosition(currentDate,
//                                                                                            inertialFrameSortedBody);
//                        if (currentPosition.distance(currentPositionSortedBody) < currentRadius) {
//                            lastKnownInfluenceSphere = spheresSorted.get(j);
//                            datesChanges.add(state.getDate());
//                            break;
//                        }
//                    }
//                }
            }
        }
        // Add the last date of the simulation.
        datesChanges.add(this.finalDate);
        System.out.println(datesChanges);
    }

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
        try {
            this.period = spacecraftPropagator.getInitialState()
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
        this.spacecraftPropagator = boundedPropagator;
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
     * This function set a multiplexer for the propagator of the Spacecraft. It will handle the retrieving of the
     * spacecraft states and of the attitudes.
     *
     * @param propagator : The propagator of the Spacecraft.
     */
    private void multiplexerSetup(final Propagator propagator) {
        propagator.getMultiplexer()
                  .add(header.getClock()
                             .getMultiplier(), currentState -> {
                      spaceCraftStates.add(currentState);
                      final Attitude currentSpaceCraftAttitude = currentState.getAttitude();
                      attitudes.add(currentSpaceCraftAttitude);
                  });
    }

    /**
     * This function aims at writing the global display of the Spacecraft, it ensures that the model loaded and the orientation are correct.
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
     * This function writes the path of the Spacecraft to display the orbit.
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
     * This function writes the position of the Spacecraft in time.
     *
     * @param packet : The packet that will write in the czml file.
     * @param output : The output stream of cesium that will contain the strings to write into the CzmLFile.
     */
    private void czmlPosition(final PacketCesiumWriter packet, final CesiumOutputStream output) {

        final TimePosition timePosition = new TimePosition(getCartesianArraylist(), getJulianDates());
        timePosition.write(packet, output);
    }

    /**
     * This function is used when the attitude of the Spacecraft must be displayed. To do so, this function defines an
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

    private InfluenceSphere computeInitialInfluenceSphere(final List<InfluenceSphere> spheres,
                                                          final List<Double> spheresRadius, final List<Body> bodies,
                                                          final Vector3D initialPosition, final Frame sunFrame) {
        for (int i = 0; i < bodies.size(); i++) {
            final Body currentBody = bodies.get(i);
            final Vector3D positionOfBodyAtInitialState = currentBody.getCelestialBody()
                                                                     .getPosition(this.spaceCraftStates.get(0)
                                                                                                       .getDate(),
                                                                             sunFrame);
            if (initialPosition.distance(positionOfBodyAtInitialState) < spheresRadius.get(i)) {
                return spheres.get(i);
            }
        }
        return null;
    }

    private List<Body> sortedBodies(final List<Double> radiusesSorted, final List<Body> bodiesNotSorted) {
        final List<Body> toReturn = new ArrayList<>();

        for (final double currentRadius : radiusesSorted) {
            for (final Body currentBody : bodiesNotSorted) {
                if (currentBody.getInfluenceSphere()
                               .getRadius() == currentRadius) {
                    toReturn.add(currentBody);
                }
            }
        }
        return toReturn;
    }

    private List<InfluenceSphere> sortedSpheres(final List<Double> radiusesSorted,
                                                final List<InfluenceSphere> spheres) {
        final List<InfluenceSphere> toReturn = new ArrayList<>();
        for (final double currentRadius : radiusesSorted) {
            for (final InfluenceSphere currentSphere : spheres) {
                if (currentSphere.getRadius() == currentRadius) {
                    toReturn.add(currentSphere);
                }
            }
        }
        return toReturn;
    }

    private void buildInfluenceSphere(final List<Body> bodies, final List<InfluenceSphere> emptyListSphere,
                                      final List<Double> emptyListRadius) {
        for (final Body currentBody : bodies) {
            try {
                final InfluenceSphere currentSphereBody = currentBody.getInfluenceSphere();
                emptyListSphere.add(currentSphereBody);
                emptyListRadius.add(currentSphereBody.getRadius());
            } catch (OreCzmlException exception) {
                throw new OreCzmlException(OreCzmlMessages.TRY_INFLUENCE_SPHERE_WITHOUT_SPHERES);
            }
        }
    }
}
