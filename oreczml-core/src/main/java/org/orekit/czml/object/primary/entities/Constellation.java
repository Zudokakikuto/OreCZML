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

import cesiumlanguagewriter.CesiumOutputStream;
import cesiumlanguagewriter.CesiumStreamWriter;
import cesiumlanguagewriter.TimeInterval;
import org.orekit.czml.errors.OreCzmlException;
import org.orekit.czml.errors.OreCzmlMessages;
import org.orekit.czml.object.primary.AbstractPrimaryObject;
import org.orekit.czml.object.primary.Header;
import org.orekit.orbits.Orbit;
import org.orekit.propagation.BoundedPropagator;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScale;

import java.awt.Color;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Constellation
 * <p>
 * This class builds several Satellites objects {@link Spacecraft} at once, with
 * several propagators.
 *
 * @author Julien LEBLOND.
 * @since 1.0.0
 */
public class Constellation
    extends
    AbstractPrimaryObject {

    /**
     * The default empty string to represent the model, if this is used,
     * satellites are displayed with a 2D image from
     * OreCZML/src/main/resources/.
     */
    public static final String DEFAULT_STRING_MODEL = "";

    /**
     * The default ID for the constellation.
     */
    public static final String DEFAULT_ID = "Constellation/";

    /**
     * The default name for the constellation.
     */
    public static final String DEFAULT_NAME = "Constellation of : ";

    /**
     * A default string to give the number of satellites in the constellation.
     */
    public static final String DEFAULT_NUMBER_OF_SAT = " satellites ";

    /**
     * The list of all the initial orbits of the constellation.
     */
    private final List<Orbit> InitialOrbits = new ArrayList<>();

    /**
     * The list containing all the ids of all the satellites.
     */
    private final List<String> Ids = new ArrayList<>();

    // intrinsic parameters
    /**
     * The list of all the propagators defining all the satellites of the
     * constellation.
     */
    private final List<BoundedPropagator> propagators;

    /**
     * The total number of satellites of the constellation.
     */
    private final int totalOfSatellite;

    /**
     * The timescale from {@link org.orekit.time.TimeScale}.
     */
    private final TimeScale timeScale;

    /**
     * The list referencing all the satellites.
     */
    private final List<Spacecraft> satellites = new ArrayList<>();

    /**
     * This boolean allows the constellation to display only the last period and
     * not the entire path.
     */
    private boolean displayOnlyLastPeriod = false;

    /** The header considered. */
    private final Header header;

    /** Boolean to enable multi models for satellites or not. */
    private boolean multipleModels;

    /** Boolean to display the attitude or not of satellites. */
    private boolean displayAttitude;

    // Constructors

    /**
     * The constructor with a default model.
     *
     * @param Propagators : A list of bounded propagator that represents each a
     *        propagator for a given satellite.
     * @param finalDate : The final date when the propagation must stop.
     * @param header : The header considered.
     * @throws URISyntaxException the uri syntax exception
     * @throws IOException the io exception
     */
    Constellation(final List<BoundedPropagator> Propagators,
                  final AbsoluteDate finalDate, final Header header)
        throws URISyntaxException,
            IOException {
        this(Propagators, finalDate, DEFAULT_STRING_MODEL,
             DEFAULT_ID + Propagators.size() + " " + DEFAULT_NUMBER_OF_SAT,
             header);
    }

    /**
     * The constructor of the constellation with no default parameters.
     *
     * @param propagatorsInput : A list of bounded propagator that represents
     *        each a propagator for a given satellite.
     * @param finalDate : The final date when the propagation must stop.
     * @param modelPath : The path of the model used.
     * @param customID : The custom ID of the constellation.
     * @param header : The header to use when several are used.
     * @throws URISyntaxException the uri syntax exception
     * @throws IOException the io exception
     */
    Constellation(final List<BoundedPropagator> propagatorsInput,
                  final AbsoluteDate finalDate, final String modelPath,
                  final String customID, final Header header)
        throws URISyntaxException,
            IOException {

        this(propagatorsInput, finalDate, Collections.singletonList(modelPath),
             customID, header);
    }

    Constellation(final List<BoundedPropagator> propagatorsInput,
                  final AbsoluteDate finalDate, final List<String> modelsInput,
                  final String customID, final Header header)
        throws URISyntaxException,
            IOException {

        final List<Color> colorList = colorWheel(propagatorsInput.size());
        final TimeInterval intervalOfStudy =
            new TimeInterval(header.getAvailability().getStart(),
                             header.getAvailability().getStop());
        this.header = header;
        this.multipleModels = modelsInput.size() > 1;
        this.totalOfSatellite = propagatorsInput.size();
        this.setName(DEFAULT_NAME + totalOfSatellite + DEFAULT_NUMBER_OF_SAT);
        this.setId(customID);
        this.setAvailability(intervalOfStudy);
        this.propagators = new ArrayList<>(propagatorsInput);
        this.timeScale = header.getTimeScale();
        this.defineMultipleArgument(finalDate, colorList, multipleModels,
                                    modelsInput);
    }

    /**
     * Builder constellation builder.
     *
     * @param propagatorsInput the propagators input
     * @param finalDateInput the final date input
     * @param header the header
     * @return the constellation builder
     */
    public static ConstellationBuilder
        builder(final List<BoundedPropagator> propagatorsInput,
                final AbsoluteDate finalDateInput, final Header header) {
        return new ConstellationBuilder(propagatorsInput, finalDateInput,
                                        header);
    }

    // Overrides

    /**
     * The generation function for the CZML file for the constellation.
     */
    @Override
    public void writeCzmlBlock(final CesiumStreamWriter stream,
                               final CesiumOutputStream output)
        throws URISyntaxException,
            IOException {
        iterateOnSatelliteWriting(stream, output);
    }

    // Display methods

    /**
     * This function allows the constellation to display only one period at a
     * time.
     */
    public void displayOnlyOnePeriod() {
        displayOnlyLastPeriod = true;
    }

    public void displayAttitude() {
        displayAttitude = true;
    }

    // Getters

    /**
     * This getter returns the timescale.
     *
     * @return : The timescale used.
     */
    public TimeScale getTimeScale() {
        return timeScale;
    }

    /**
     * This getter returns all the satellites of the constellation.
     *
     * @return : All the satellites of the constellation.
     */
    public List<Spacecraft> getSatellites() {
        return Collections.unmodifiableList(satellites);
    }

    /**
     * Gets propagators.
     *
     * @return the propagators
     */
    public List<BoundedPropagator> getPropagators() {
        return Collections.unmodifiableList(propagators);
    }

    /**
     * Gets initial orbits.
     *
     * @return the initial orbits
     */
    public List<Orbit> getInitialOrbits() {
        return Collections.unmodifiableList(InitialOrbits);
    }

    /**
     * Gets total of satellite.
     *
     * @return the total of satellite
     */
    public int getTotalOfSatellite() {
        return totalOfSatellite;
    }

    /**
     * Gets ids.
     *
     * @return the ids
     */
    public List<String> getIds() {
        return Collections.unmodifiableList(Ids);
    }

    // Private functions

    /**
     * This function aims at defining arguments that represent multiple
     * satellites.
     *
     * @param finalDate : The final date of the propagation.
     * @param models : The model of the satellites.
     * @param colorList : The color list of all the color to use for each
     *        satellite.
     * @param multipleModelsInput : The boolean to use or not several models for
     *        satellites.
     */
    private void defineMultipleArgument(final AbsoluteDate finalDate,
                                        final List<Color> colorList,
                                        final boolean multipleModelsInput,
                                        final List<String> models)
        throws URISyntaxException,
            IOException {
        for (int i = 0; i < propagators.size(); i++) {
            final BoundedPropagator propagator = propagators.get(i);
            if (!multipleModelsInput) {
                final Spacecraft currentSatellite =
                    Spacecraft.builder(propagator, header)
                        .withFinalDate(finalDate).withModelPath(models.get(0))
                        .withColor(colorList.get(i)).build();
                satellites.add(currentSatellite);
                Ids.add(currentSatellite.getId());
                InitialOrbits.add(currentSatellite.getOrbits().get(0));
            } else {
                if (models.size() != propagators.size()) {
                    throw new OreCzmlException(OreCzmlMessages.NOT_SAME_NUMBER_SAT_MODELS);
                }
                final String currentModel = models.get(i);
                final Spacecraft currentSatellite =
                    Spacecraft.builder(propagator, header)
                        .withFinalDate(finalDate).withModelPath(currentModel)
                        .withColor(colorList.get(i)).build();
                satellites.add(currentSatellite);
                Ids.add(currentSatellite.getId());
                InitialOrbits.add(currentSatellite.getOrbits().get(0));
            }
        }
    }

    /**
     * This function aims at writing each satellite given the argument
     * allSatellites contains all of them.
     *
     * @param stream : The stream that converts all the strings into
     *        understandable string for the CzmlFile.
     * @param output : The output stream of cesium that will contain the strings
     *        to write into the CzmLFile.
     */
    private void iterateOnSatelliteWriting(final CesiumStreamWriter stream,
                                           final CesiumOutputStream output)
        throws URISyntaxException,
            IOException {
        for (final Spacecraft satelliteToOutput : satellites) {
            if (displayAttitude) {
                satelliteToOutput.displaySpacecraftAttitude();
            }
            if (displayOnlyLastPeriod) {
                satelliteToOutput.displayOnlyOnePeriod();
            }
            satelliteToOutput.writeCzmlBlock(stream, output);
        }
    }
}
