/* Copyright 2002-2026 CS GROUP
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

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hipparchus.geometry.euclidean.threed.Rotation;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.hipparchus.util.FastMath;
import org.orekit.attitudes.Attitude;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.czml.object.ModelType;
import org.orekit.czml.object.nonvisual.CzmlModel;
import org.orekit.czml.object.primary.AbstractPrimaryObject;
import org.orekit.czml.object.secondary.Clock;
import org.orekit.czml.object.secondary.CartographicTimePosition;
import org.orekit.czml.object.secondary.Label;
import org.orekit.czml.object.secondary.Orientation;
import org.orekit.czml.object.secondary.Path;
import org.orekit.czml.object.utils.DateUtils;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.time.AbsoluteDate;
import org.orekit.utils.PVCoordinatesProvider;
import org.orekit.utils.TimeStampedAngularCoordinates;
import org.orekit.utils.TimeStampedPVCoordinates;

import cesiumlanguagewriter.BooleanCesiumWriter;
import cesiumlanguagewriter.Cartographic;
import cesiumlanguagewriter.CesiumOutputStream;
import cesiumlanguagewriter.CesiumStreamWriter;
import cesiumlanguagewriter.JulianDate;
import cesiumlanguagewriter.PacketCesiumWriter;
import cesiumlanguagewriter.PathCesiumWriter;
import cesiumlanguagewriter.PolylineMaterialCesiumWriter;
import cesiumlanguagewriter.SolidColorMaterialCesiumWriter;
import cesiumlanguagewriter.TimeInterval;

/**
 * GroundVehicle class.
 * <p>
 * This class represents the GroundVehicle object to be displayed. It will move
 * along a coordinate path defined w.r.t. the Earth body frame.
 * </p>
 * <p>
 * Each GroundVehicle object will move along the trajectory specified by the
 * PVCoordinatesProvider. Ensuring that the start and stop times given by the
 * user are congruent with the bounds of the PVCoordinatesProvider is left to
 * the user.
 * </p>
 *
 * @author Brianna Aubin
 * @since 1.1
 */
public class GroundVehicle
    extends
    AbstractPrimaryObject<GroundVehicle> {

    /** The default model path, empty. */
    public static final String DEFAULT_MODEL_PATH =
        new File(GroundVehicle.class.getClassLoader()
            .getResource("airplane.glb").getFile()).toPath().toString();

    /** The default id of the GroundVehicle. */
    public static final String DEFAULT_ID = "GROUNDVEHICLE/";

    /** The default name of the GroundVehicle. */
    public static final String DEFAULT_NAME = "GroundVehicle";

    /** Beginning of the HTML. */
    public static final String BEGIN_HTML = "<!--HTML-->\r\n<p>Id : ";

    /** Simulated from HTML. */
    public static final String SIMULATED_FROM =
        "</p>\r\n<p>" + "Simulated from : ";

    /** String for 'to'. */
    public static final String TO = " to ";

    /** End HTML. */
    public static final String END_HTML = "</p>";

    /** The default orbit color of the GroundVehicle. */
    public static final Color DEFAULT_COLOR = new Color(255, 255, 255);

    // Optional parameters

    /** To display the name of the GroundVehicle or not. */
    private boolean displayName = false;

    /** Whether or not to add the attitude values to the display. */
    private boolean displayAttitude;

    // Orekit arguments

    /** The list of the time states of the GroundVehicle. */
    private final List<TimeStampedPVCoordinates> gvCoordinates =
        new ArrayList<>();

    /**
     * The list of the attitudes of the Spacecraft.
     */
    private List<Attitude> attitudes = new ArrayList<>();

    // Writing arguments

    /** The coordinates provider of the GroundVehicle. */
    private PVCoordinatesProvider coordsProvider;

    /** The orientation in the earth body frame of the GroundVehicle. */
    private Orientation orientation;

    /** The color of the ground trajectory. */
    private Color color;

    /** The CzmlModel for the display of the model. */
    private CzmlModel model;

    /** The type of the model. */
    private final ModelType modelType;

    /** The start date of the propagation. */
    private final AbsoluteDate startDate;

    /** The final date of the propagation. */
    private final AbsoluteDate finalDate;

    /** The time step size for the propagation. */
    private final double clockMultiplier;

    /** The description of the GroundVehicle. */
    private String description;

    /** The clock of the GroundVehicle. */
    private Clock clock;

    /** The earth ellipsoid. */
    private OneAxisEllipsoid earth;

    /**
     * The GroundVehicle constructor with no default parameters.
     *
     * @param coordsProvider : A PVCoordinatesProvider for a vehicle whose
     *        movement is expressed in terms of the Earth body frame.
     * @param startDate : The start date to consider for the start of the
     *        propagation and the availability of the GroundVehicle.
     * @param finalDate : The stop date to consider for the stop the propagation
     *        and the availability of the GroundVehicle.
     * @param earth : the earth body model
     * @param clockMultiplier : The clock multiplier *
     */
    public GroundVehicle(final PVCoordinatesProvider coordsProvider,
                         final AbsoluteDate startDate,
                         final AbsoluteDate finalDate,
                         final OneAxisEllipsoid earth,
                         final Double clockMultiplier) {
        this.setId(DEFAULT_ID);
        this.setName(DEFAULT_NAME);
        this.clock = new Clock(startDate, finalDate, clockMultiplier);
        this.setAvailability(new TimeInterval(DateUtils
            .toJulianDate(startDate), DateUtils.toJulianDate(finalDate)));
        this.coordsProvider = coordsProvider;
        this.description =
            BEGIN_HTML +
                           DEFAULT_ID + SIMULATED_FROM + startDate + TO +
                           finalDate + END_HTML;
        this.color = DEFAULT_COLOR;
        this.model = new CzmlModel(DEFAULT_MODEL_PATH, true, clock);
        this.modelType = model.getModelType();
        this.startDate = startDate;
        this.finalDate = finalDate;
        this.clockMultiplier = clockMultiplier;
        this.earth = earth;
        createGroundVehicleTrajectory();
        this.displayAttitude = orientationSetup();
    }

    /**
     * The GroundVehicle constructor with no default parameters.
     *
     * @param coordsProvider : A PVCoordinatesProvider whose values are
     *        expressed in the earth body frame.
     * @param startDateInput : The start date to consider for the start of the
     *        propagation and the availability of the GroundVehicle.
     * @param finalDateInput : The stop date to consider for the stop the
     *        propagation and the availability of the GroundVehicle.
     * @param earth : the earth body model
     * @param clockMultiplier : The clock multiplier
     * @param modelPath : The path to the model to load.
     * @param color : The color of the orbit.
     * @param customID : The custom ID of the GroundVehicle.
     * @param name : The name of the GroundVehicle. *
     */
    public GroundVehicle(final PVCoordinatesProvider coordsProvider,
                         final AbsoluteDate startDateInput,
                         final AbsoluteDate finalDateInput,
                         final OneAxisEllipsoid earth,
                         final double clockMultiplier, final String modelPath,
                         final Color color, final String customID,
                         final String name) {

        this.setId(customID);
        this.setName(name);
        this.clock = new Clock(startDateInput, finalDateInput, clockMultiplier);
        this.setAvailability(new TimeInterval(DateUtils
            .toJulianDate(startDateInput), DateUtils.toJulianDate(finalDateInput)));
        this.coordsProvider = coordsProvider;
        this.description =
            BEGIN_HTML +
                           customID + SIMULATED_FROM + startDateInput + TO +
                           finalDateInput + END_HTML;
        this.color = color;
        this.model = new CzmlModel(modelPath, true, clock);
        this.modelType = model.getModelType();
        this.startDate = startDateInput;
        this.finalDate = finalDateInput;
        this.earth = earth;
        this.clockMultiplier = clockMultiplier;
        createGroundVehicleTrajectory();
        this.displayAttitude = orientationSetup();
    }

    /**
     * GroundVehicle builder.
     *
     * @param coordsProvider : The propagator used to create the GroundVehicle
     *        trajectory.
     * @param startDate : The GroundVehicle takeoff date.
     * @param finalDate : The GroundVehicle landing date.
     * @param clockMultiplier : The clock multiplier value used to create the
     *        trajectory timestep.
     * @param earth : The body the GroundVehicle is flying around.
     * @return the GroundVehicle builder
     */
    public static GroundVehicleBuilder
        builder(final PVCoordinatesProvider coordsProvider,
                final AbsoluteDate startDate, final AbsoluteDate finalDate,
                final OneAxisEllipsoid earth, final Double clockMultiplier) {
        return new GroundVehicleBuilder(coordsProvider, startDate, finalDate,
                                        earth, clockMultiplier);
    }

    /**
     * Retrieves the PVCoordinatesProvider for functions that solve for
     * satellite view intervals.
     *
     * @return the PVCoordinatesProvider for the ground vehicle
     */
    public final PVCoordinatesProvider getPVCoordinatesProvider() {
        return coordsProvider;
    }

    /**
     * Calculates the ground points for the vehicle trajectory in the output.
     * CZML file
     */
    private void createGroundVehicleTrajectory() {

        final Double multiplier = getClockMultiplier();
        AbsoluteDate currentDate =
            DateUtils.toAbsoluteDate(getAvailability().getStart());
        final AbsoluteDate maxDate =
            DateUtils.toAbsoluteDate(getAvailability().getStop());

        while (currentDate.isBeforeOrEqualTo(maxDate)) {
            gvCoordinates.add(coordsProvider
                .getPVCoordinates(currentDate, earth.getBodyFrame()));
            currentDate = currentDate.shiftedBy(multiplier);
        }
    }

    @Override
    public void writeCzmlBlock(final CesiumStreamWriter stream,
                               final CesiumOutputStream output) {

        output.setPrettyFormatting(true);

        try (PacketCesiumWriter packet = stream.openPacket(output)) {
            packet.writeId(this.getId());
            packet.writeName(getName());
            packet.writeAvailability(getAvailability());
            packet.writeDescriptionProperty(description);

            if (getDisplayName()) {
                writeLabel(packet, output);
            }

            czmlDisplay(packet, stream, output);

            czmlPath(packet, output, getColor());

            czmlPosition(packet, output, getCartographicArraylist(),
                         getJulianDates());
        }
    }

    @Override
    public GroundVehicle cloneObject() {
        final GroundVehicle copy =
            new GroundVehicle(this.coordsProvider, this.startDate,
                              this.finalDate, this.earth, this.clockMultiplier,
                              this.getModel().getAbsolutePath(), this.color,
                              this.getId(), this.getName());

        copy.setId(this.getId());
        copy.setName(this.getName());
        copy.clock = this.getClock();
        copy.setAvailability(new TimeInterval(DateUtils
            .toJulianDate(this.startDate), DateUtils.toJulianDate(this.finalDate)));
        copy.coordsProvider = this.coordsProvider;
        copy.displayAttitude = this.displayAttitude;
        if (displayAttitude) {
            copy.setAttitudes(this.attitudes);
            copy.setOrientation(this.orientation);
        }
        copy.description =
            BEGIN_HTML +
                           this.getId() + SIMULATED_FROM + startDate + TO +
                           finalDate + END_HTML;
        copy.color = color;
        copy.model =
            new CzmlModel(this.getModel().getAbsolutePath(), true, clock);
        copy.displayName = this.displayName;
        copy.earth = this.earth;
        return copy;
    }

    // Display functions

    /**
     * Display name associated with GroundVehicle.
     */
    public void displayName() {
        displayName = true;
    }

    // Getters

    /**
     * Gets GroundVehicle states.
     *
     * @return the GroundVehicle states
     */
    public List<TimeStampedPVCoordinates> getGroundVehicleCoordinates() {
        return Collections.unmodifiableList(gvCoordinates);
    }

    /**
     * Gets absolute date list.
     *
     * @return the absolute date list
     */
    public List<AbsoluteDate> getAbsoluteDateList() {
        final List<AbsoluteDate> toReturn = new ArrayList<>();
        for (TimeStampedPVCoordinates coord : gvCoordinates) {
            toReturn.add(coord.getDate());
        }
        return Collections.unmodifiableList(toReturn);
    }

    /**
     * Gets the clock.
     *
     * @return The clock
     */
    public Clock getClock() {
        return clock.cloneObject();
    }

    /**
     * //List<TimeStampedPVCoordinates> gvCoordinates Gets attitudes.
     *
     * @return the attitudes
     */
    public List<Attitude> getAttitudes() {

        // x in direction of velocity, z in direction of earth normal
        List<Attitude> toReturn = new ArrayList<>();
        if (attitudes.isEmpty()) {
            for (TimeStampedPVCoordinates pv : gvCoordinates) {
                toReturn.add(getAttitude(pv));
            }
            // To account for fact that last attitude will be NaN with zero
            // velocity
            final Attitude temp1 = toReturn.get(toReturn.size() - 1);
            final Attitude temp2 = toReturn.get(toReturn.size() - 2);

            final TimeStampedAngularCoordinates timeStampedAngularCoords =
                new TimeStampedAngularCoordinates(temp1.getDate(),
                                                  temp2.getRotation(),
                                                  Vector3D.ZERO, Vector3D.ZERO);
            final Attitude output =
                new Attitude(FramesFactory.getEME2000(),
                             timeStampedAngularCoords);

            toReturn.set(toReturn.size() - 1, output);
        } else {
            toReturn = attitudes;
        }
        return Collections.unmodifiableList(toReturn);

    }

    /**
     * Calculates the ground vehicle attitude as a function of time. Points +X
     * in the velocity direction; +Z away from the Earth
     *
     * @param pv the pos/vel/time for the ground vehicle
     * @return the attitude
     */
    private Attitude getAttitude(final TimeStampedPVCoordinates pv) {
        final GeodeticPoint point =
            earth.transform(pv.getPosition(), earth.getBodyFrame(),
                            pv.getDate());
        final Vector3D xNew =
            pv.getVelocity().scalarMultiply(1.0 / pv.getVelocity().getNorm());
        final Vector3D zNew =
            new Vector3D(FastMath.cos(point.getLongitude()) *
                         FastMath.cos(point.getLatitude()),
                         FastMath.sin(point.getLongitude()) *
                                                            FastMath.cos(point
                                                                .getLatitude()),
                         FastMath.sin(point.getLatitude()));
        final Rotation rot =
            new Rotation(xNew, zNew, Vector3D.PLUS_I, Vector3D.PLUS_K);

        final TimeStampedAngularCoordinates timeStampedAngularCoords =
            new TimeStampedAngularCoordinates(pv.getDate(), rot, Vector3D.ZERO,
                                              Vector3D.ZERO);
        return new Attitude(FramesFactory.getEME2000(),
                            timeStampedAngularCoords);
    }

    /**
     * Gets cartographic arraylist.
     *
     * @return the cartographic arraylist
     */
    public List<Cartographic> getCartographicArraylist() {
        final List<Cartographic> toReturn = new ArrayList<>();
        for (TimeStampedPVCoordinates coord : gvCoordinates) {
            final Vector3D currentVector3D = coord.getPosition();
            final GeodeticPoint point =
                earth.transform(currentVector3D, earth.getBodyFrame(),
                                coord.getDate());
            toReturn
                .add(new Cartographic(point.getLongitude(), point.getLatitude(),
                                      point.getAltitude()));
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
     * Gets frame.
     *
     * @return the frame
     */
    public Frame getFrame() {
        return earth.getBodyFrame();
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
     * Gets color.
     *
     * @return the color
     */
    public Color getColor() {
        return color;
    }

    /**
     * Gets the startDate.
     *
     * @return the startDate
     */
    public AbsoluteDate getStartDate() {
        return startDate;
    }

    /**
     * Gets the finalDate.
     *
     * @return the finalDate
     */
    public AbsoluteDate getFinalDate() {
        return finalDate;
    }

    /**
     * Gets the clockMultiplier.
     *
     * @return the clockMultiplier
     */
    public double getClockMultiplier() {
        return clockMultiplier;
    }

    /**
     * Gets the description.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
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
        return model.cloneObject();
    }

    /**
     * Gets display name value.
     *
     * @return the display name boolean
     */
    public boolean getDisplayName() {
        return displayName;
    }

    /**
     * Gets orientation.
     *
     * @return the orientation
     */
    public Orientation getOrientation() {
        return orientation.cloneObject();
    }

    // SETTERS

    /**
     * Sets coordinates provider.
     *
     * @param coordsProviderInput the PVCoordinatesProvider
     */
    public void
        setPVCoordinatesProvider(final PVCoordinatesProvider coordsProviderInput) {
        this.coordsProvider = coordsProviderInput;
    }

    /**
     * Sets the clock.
     *
     * @param clock The clock
     */
    public void setClock(final Clock clock) {
        this.clock = clock.cloneObject();
    }

    /**
     * Sets the display of the attitude.
     *
     * @param displayAttitude : Boolean to show the attitude or not
     */
    public void setDisplayAttitude(final boolean displayAttitude) {
        this.displayAttitude = displayAttitude;
    }

    /**
     * Sets the color.
     *
     * @param color The color
     */
    public void setColor(final Color color) {
        this.color = color;
    }

    /**
     * Sets attitudes.
     *
     * @param attitudes the attitudes
     */
    public void setAttitudes(final List<Attitude> attitudes) {
        this.attitudes = new ArrayList<>(attitudes);
        this.orientation = new Orientation(attitudes, getFrame());
    }

    /**
     * Sets the orientation.
     *
     * @param orientation The orientation
     */
    public void setOrientation(final Orientation orientation) {
        this.orientation = orientation.cloneObject();
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
     * Sets the model.
     *
     * @param modelInput : The model to set
     */
    public void setModel(final CzmlModel modelInput) {
        this.model = modelInput.cloneObject();
    }

    // Private functions

    /**
     * This function aims at writing the global display of the GroundVehicle, it
     * ensures that the model loaded and the orientation are correct.
     *
     * @param packet : The packet that will write in the czml file.
     * @param output : The output stream of cesium that will contain the strings
     *        to write into the CzmLFile.
     * @param stream : The stream that converts all the strings into
     *        understandable string for the CzmlFile.
     */
    private void czmlDisplay(final PacketCesiumWriter packet,
                             final CesiumStreamWriter stream,
                             final CesiumOutputStream output) {

        if (getModelType() == ModelType.MODEL_2D ||
            getModelType() == ModelType.EMPTY_MODEL) {
            getModel().generateCZML(packet, output);
        } else if (getModelType() == ModelType.MODEL_3D) {
            this.orientation.write(packet, output);
            this.getModel().generateCZML(packet, output);
        }
    }

    /**
     * This function writes the path of the GroundVehicle to display the
     * trajectory.
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

            final Path path = new Path(getAvailability());
            try (BooleanCesiumWriter showPath =
                pathProperty.openShowProperty()) {
                showPath.writeInterval(getAvailability().getStart(),
                                       getAvailability().getStop());
                showPath.writeBoolean(path.getShow());
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
     * This function aims at writing multiple models when several are loaded.
     * The number of models should be the same as the number of ground stations
     * wanted.
     *
     * @param packet : The packet that will write in the czml file.
     * @param output : The output stream of cesium that will contain the strings
     *        to write into the CzmLFile.
     */
    private void writeLabel(final PacketCesiumWriter packet,
                            final CesiumOutputStream output) {
        final Label label = new Label(getName());
        label.write(packet, output);
    }

    /**
     * This function writes the position of the GroundVehicle in time.
     *
     * @param packet : The packet that will write in the czml file.
     * @param output : The output stream of cesium that will contain the strings
     *        to write into the CzmLFile.
     * @param cartograhpics : The cartographics coordinates to be written
     * @param julianDates : The JulianDate corresponding the cartesian
     *        coordinates
     */
    private void czmlPosition(final PacketCesiumWriter packet,
                              final CesiumOutputStream output,
                              final List<Cartographic> cartograhpics,
                              final List<JulianDate> julianDates) {

        final CartographicTimePosition timePosition =
            new CartographicTimePosition(cartograhpics, julianDates);
        timePosition.write(packet, output);
    }

    /**
     * This function is used when the attitude of the GroundVehicle must be
     * displayed. This is true whenever a 3D model is loaded, as the model
     * requires an orientation be displayed correctly.
     *
     * @return the attitude display status
     */
    private boolean orientationSetup() {
        if (getModelType() == ModelType.MODEL_3D) {
            this.orientation =
                Orientation.builder(getAttitudes(), getFrame()).build();
            return true;
        }
        return false;
    }

}
