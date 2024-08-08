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

import cesiumlanguagewriter.TimeInterval;
import org.orekit.orbits.Orbit;
import org.orekit.propagation.BoundedPropagator;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScale;

import java.awt.Color;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Constellation
 *
 * <p>
 * This class builds several Satellites objects {@link Satellite} at once, with several propagators.
 * </p>
 *
 * @author Julien LEBLOND.
 * @since 1.0
 */

public class Constellation extends AbstractPrimaryObject implements CzmlPrimaryObject {

    /** The default empty string to represents the model, if this is used satellites are displayed with a 2D image from OreCZML/src/main/resources/. */
    public static final String DEFAULT_STRING_3D_MODEL = "";

    /** The default ID for the constellation. */
    public static final String DEFAULT_ID = "Constellation/";

    /** The default name for the constellation. */
    public static final String DEFAULT_NAME = "Constellation of : ";

    /** A default string to give the number of satellite in the constellation. */
    public static final String DEFAULT_NUMBER_OF_SAT = " satellites ";

    /** The list of all the initial orbits of the constellation. */
    private final List<Orbit> allInitialOrbits = new ArrayList<>();

    /**
     * A model can be used for a specific display, it will be applied to all the satellite of the constellation.
     * (under development for specified models for each satellite)
     */
    private final String modelPath;

    /** The list containing all the ids of all the satellites. */
    private final List<String> allIds = new ArrayList<>();


    // intrinsic parameters
    /** The list of all the propagators defining all the satellites of the constellation. */
    private final List<BoundedPropagator> allPropagators;

    /** The total number of satellites of the constellation. */
    private int totalOfSatellite;

    /** The timescale from {@link org.orekit.time.TimeScale}. */
    private TimeScale timeScale;

    /**
     * A list of lists referencing all orbits, the first indent of list correspond to all the different instants when the orbits are computed,
     * the second indent of list contains all the orbits of all the satellite of the constellation for a given time.
     */
    private List<List<Orbit>> allOrbitsByPlane = new ArrayList<>();

    /** The orbit used as a reference to compute all the others. */
    private Orbit referenceOrbit;

    /** The list referencing all the satellites. */
    private List<Satellite> allSatellites = new ArrayList<>();

    /** This boolean allows the constellation to display only the last period and not the entire path. */
    private boolean displayOnlyLastPeriod = false;


    public Constellation(final List<BoundedPropagator> allPropagators,
                         final AbsoluteDate finalDate) throws URISyntaxException, IOException {
        this(allPropagators, finalDate, DEFAULT_STRING_3D_MODEL);
    }

    public Constellation(final List<BoundedPropagator> allPropagators, final AbsoluteDate finalDate,
                         final String model3DPath) throws URISyntaxException, IOException {

        final List<Color> colorList = colorWheel(allPropagators.size());
        final TimeInterval intervalOfStudy = new TimeInterval(Header.getMasterClock()
                                                                    .getAvailability()
                                                                    .getStart(), Header.getMasterClock()
                                                                                       .getAvailability()
                                                                                       .getStop());

        this.totalOfSatellite = allPropagators.size();
        this.setId(DEFAULT_ID + totalOfSatellite);
        this.setName(DEFAULT_NAME + totalOfSatellite + DEFAULT_NUMBER_OF_SAT);
        this.setAvailability(intervalOfStudy);
        this.allPropagators = allPropagators;
        this.timeScale      = Header.getTimeScale();
        this.modelPath      = model3DPath;
        this.defineMultipleArgument(finalDate, model3DPath, colorList);
    }

    /** The generation function for the CZML file for the constellation. */
    @Override
    public void writeCzmlBlock() throws URISyntaxException, IOException {

        if (modelPath.isEmpty()) {
            this.iterateOnSatelliteWriting();
        } else {
            this.iterateOnSatelliteWriting();
            displayOnlyLastPeriod = false;
        }
    }

    /** This function returns the string writer of the constellation. */
    @Override
    public StringWriter getStringWriter() {
        return STRING_WRITER;
    }

    /** This function cleans all the private attributes to be used for another object. */
    @Override
    public void cleanObject() {
        this.allSatellites    = null;
        this.timeScale        = null;
        this.referenceOrbit   = null;
        this.allOrbitsByPlane = null;
        this.totalOfSatellite = 0;
    }

    /** This function allows the constellation to display only one period at a time. */
    public void displayOnlyOnePeriod() {
        displayOnlyLastPeriod = true;
    }

    /**
     * This getter return the timescale.
     *
     * @return : The timescale used.
     */
    public TimeScale getTimeScale() {
        return timeScale;
    }

    /**
     * This getter return the reference orbit.
     *
     * @return : The reference orbit used.
     */
    public Orbit getReferenceOrbit() {
        return referenceOrbit;
    }

    /**
     * This getter return all the satellites of the constellation.
     *
     * @return : All the satellites of the constellation.
     */
    public List<Satellite> getAllSatellites() {
        return allSatellites;
    }

    public List<BoundedPropagator> getAllPropagators() {
        return allPropagators;
    }

    public List<Orbit> getAllInitialOrbits() {
        return allInitialOrbits;
    }

    public int getTotalOfSatellite() {
        return totalOfSatellite;
    }

    public List<String> getAllIds() {
        return allIds;
    }

    private void defineMultipleArgument(final AbsoluteDate finalDate, final String model3DPath,
                                        final List<Color> colorList) throws URISyntaxException, IOException {
        for (int i = 0; i < allPropagators.size(); i++) {
            final BoundedPropagator propagator = allPropagators.get(i);
            final Satellite currentSatellite = Satellite.builder(propagator)
                                                        .withFinalDate(finalDate)
                                                        .withModelPath(model3DPath)
                                                        .withColor(colorList.get(i))
                                                        .build();
            allSatellites.add(currentSatellite);
            allOrbitsByPlane.add(currentSatellite.getOrbits());
            allIds.add(currentSatellite.getId());
            allInitialOrbits.add(currentSatellite.getOrbits()
                                                 .get(0));
        }
    }

    private void iterateOnSatelliteWriting() throws URISyntaxException, IOException {
        for (final Satellite satelliteToOutput : allSatellites) {
            if (!displayOnlyLastPeriod) {
                satelliteToOutput.writeCzmlBlock();
            } else {
                satelliteToOutput.displayOnlyOnePeriod();
                satelliteToOutput.writeCzmlBlock();
            }
        }
    }
}
