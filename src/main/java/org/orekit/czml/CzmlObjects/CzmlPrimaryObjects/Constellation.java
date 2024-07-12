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
import org.orekit.orbits.OrbitType;
import org.orekit.orbits.WalkerConstellation;
import org.orekit.orbits.WalkerConstellationSlot;
import org.orekit.time.TimeScale;
import org.orekit.time.TimeScalesFactory;

import java.awt.*;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Constellation

 * <p>
 * This class builds several Satellites objects {@link Satellite} at once.
 * Extending the Walker Constellation object {@link org.orekit.orbits.WalkerConstellation} this library works the same way
 * to build objects.
 * </p>
 *
 * @since 1.0
 * @author Julien LEBLOND.
 */
public class Constellation extends AbstractPrimaryObject implements CzmlPrimaryObject {

    /** The default empty string to represents the model, if this is used satellites are displayed with a 2D image from OreCZML/src/main/resources/.*/
    public static final String DEFAULT_STRING_3D_MODEL = "";
    /** .*/
    public static final String DEFAULT_ID = "Constellation/";
    /** .*/
    public static final String DEFAULT_NAME = "Constellation of : ";
    /** .*/
    public static final String DEFAULT_NUMBER_OF_SAT = " satellites ";
    /** .*/
    public static final String DEFAULT_INITIAL_ORBIT = "from the initial orbit : ";

    /** The total number of satellites of the constellation.*/
    private int totalOfSatellite;
    /** The total number of orbital planes of the constellation.*/
    private int numberOfOrbitalPlanes;
    /** The phasing number for the constellation.*/
    private int phasingParameters;
    /** The type of orbit from {@link org.orekit.orbits.OrbitType}. It can be CARTESIAN, CIRCULAR, EQUINOCTIAL, KEPLERIAN*/
    private OrbitType orbitType;
    /** The timescale from {@link org.orekit.time.TimeScale}.*/
    private TimeScale timeScale;
    /** .*/
    private WalkerConstellation walker;

    // intrinsic parameters
    /** A list of lists referencing all orbits, the first indent of list correspond to all the different instants when the orbits are computed,
     * the second indent of list contains all the orbits of all the satellite of the constellation for a given time.*/
    private List<List<Orbit>> allOrbits;
    /** The orbit used as a reference to compute all the others.*/
    private Orbit referenceOrbit;
    /** The list referencing all the satellites.*/
    private List<Satellite> allSatellites;
    /** This boolean allows the constellation to display only the last period and not the entire path.*/
    private boolean displayOnlyLastPeriod = false;
    /** A model can be used for a specific display, it will be applied to all the satellite of the constellation.
     * (under development for specified models for each satellite)*/
    private String modelPath;

    // Builders
    /** The classic builder of the constellation.
     * @param totalOfSatellites : The total number of satellites.
     * @param numberOfPlanes : The total number of orbital planes
     * @param phasingNumber : The phasing number of the constellation
     * @param referenceOrbit : The orbit used as a reference.
     */
    public Constellation(final int totalOfSatellites, final int numberOfPlanes, final int phasingNumber, final Orbit referenceOrbit) throws URISyntaxException, IOException {
        this(totalOfSatellites, numberOfPlanes, phasingNumber, referenceOrbit, DEFAULT_STRING_3D_MODEL);
    }

    /** This builder allows the used to specify a model 2D or 3D.
     * @param totalOfSatellites : The total number of satellites.
     * @param numberOfPlanes : The total number of orbital planes
     * @param phasingNumber : The phasing number of the constellation
     * @param referenceOrbit : The orbit used as a reference.
     * @param model3DPath : The absolute path of the model.*/
    public Constellation(final int totalOfSatellites, final int numberOfPlanes, final int phasingNumber, final Orbit referenceOrbit, final String model3DPath) throws URISyntaxException, IOException {
        this.setId(DEFAULT_ID + totalOfSatellites + " " + numberOfPlanes + " " + phasingNumber + " " + referenceOrbit.toString());
        this.setName(DEFAULT_NAME + totalOfSatellites + DEFAULT_NUMBER_OF_SAT + DEFAULT_INITIAL_ORBIT + referenceOrbit);
        final TimeInterval intervalOfStudy = new TimeInterval(absoluteDateToJulianDate(referenceOrbit.getDate()), Header.MASTER_CLOCK.getAvailability().getStop());
        this.setAvailability(intervalOfStudy);
        this.walker = new WalkerConstellation(totalOfSatellites, numberOfPlanes, phasingNumber);
        this.totalOfSatellite = totalOfSatellites;
        this.numberOfOrbitalPlanes = numberOfPlanes;
        this.phasingParameters = phasingNumber;
        this.referenceOrbit = referenceOrbit;

        allOrbits = this.allOrbits(referenceOrbit);
        this.allSatellites = new ArrayList<>();
        final List<Color> colorList = colorWheel(allOrbits.size());

        for (int i = 0; i < allOrbits.size(); i++) {
            final List<Orbit> currentPlane = allOrbits.get(i);
            for (int j = 0; j < currentPlane.size(); j++) {
                final Orbit currentOrbit = currentPlane.get(j);
                final Satellite satelliteToOutput = new Satellite(currentOrbit, colorList.get(i));
                allSatellites.add(satelliteToOutput);
            }
        }

        this.orbitType = referenceOrbit.getType();
        // If not referenced, the timeScale used is UTC
        this.timeScale = TimeScalesFactory.getUTC();
        this.modelPath = model3DPath;
    }

    /** This function returns all the orbits of the constellation from a single reference orbit.
     * @param referenceOrbitInput : The reference orbit.
     * @return : All the orbits of the constellation in a list. */
    public List<List<Orbit>> allOrbits(final Orbit referenceOrbitInput) {
        final List<List<WalkerConstellationSlot<Orbit>>> constellationSlots =  this.walker.buildRegularSlots(referenceOrbitInput);
        final List<List<Orbit>> listOfAllOrbits = new ArrayList<>(Collections.singletonList(new ArrayList<>(new ArrayList<>())));

        for (int i = 0; i < constellationSlots.size(); i++) {
            final List<WalkerConstellationSlot<Orbit>> currentPlane = constellationSlots.get(i);
            final List<Orbit> orbitsToAdd = new ArrayList<>();
            for (int j = 0; j < currentPlane.size(); j++) {
                final WalkerConstellationSlot<Orbit> currentSlot = currentPlane.get(j);
                orbitsToAdd.add(currentSlot.getOrbit());
            }
            listOfAllOrbits.add(orbitsToAdd);
        }
        return listOfAllOrbits;
    }
    /** The generation function for the CZML file for the constellation.*/
    @Override
    public void writeCzmlBlock() throws URISyntaxException, IOException {

        if (modelPath.isEmpty()) {
            for (int i = 0; i < allSatellites.size(); i++) {
                final Satellite satelliteToOutput = allSatellites.get(i);
                if (!displayOnlyLastPeriod) {
                    satelliteToOutput.writeCzmlBlock();
                } else {
                    satelliteToOutput.displayOnlyOnePeriod();
                    satelliteToOutput.writeCzmlBlock();
                }
            }
        }
        else {
            for (int i = 0; i < allSatellites.size(); i++) {
                final Satellite satelliteToOutput = allSatellites.get(i);
                if (!displayOnlyLastPeriod) {
                    satelliteToOutput.writeCzmlBlock();
                } else {
                    satelliteToOutput.displayOnlyOnePeriod();
                    satelliteToOutput.writeCzmlBlock();
                }
            }
            displayOnlyLastPeriod = false;
        }
    }
    /** This function returns the string writer of the constellation.*/
    @Override
    public StringWriter getStringWriter() {
        return STRING_WRITER;
    }

    /** This function cleans all the private attributes to be used for another object.*/
    @Override
    public void cleanObject() {
        this.allSatellites = null;
        this.orbitType = null;
        this.timeScale = null;
        this.referenceOrbit = null;
        this.allOrbits = null;
        this.phasingParameters = 0;
        this.numberOfOrbitalPlanes = 0;
        this.totalOfSatellite = 0;
    }

    /** This function allows the constellation to display only one period at a time.*/
    public void displayOnlyOnePeriod() {
        displayOnlyLastPeriod = true;
    }

    /** This getter return the number of orbital planes.
     * @return : The number of orbital planes.*/
    public int getNumberOfOrbitalPlanes() {
        return numberOfOrbitalPlanes;
    }
    /** This getter return the timescale.
     * @return : The timescale used.*/
    public TimeScale getTimeScale() {
        return timeScale;
    }
    /** This getter return the reference orbit.
     * @return : The reference orbit used.*/
    public Orbit getReferenceOrbit() {
        return referenceOrbit;
    }
    /** This getter return all the satellites of the constellation.
     * @return : All the satellites of the constellation.*/
    public List<Satellite> getAllSatellites() {
        return allSatellites;
    }

    public WalkerConstellation getWalker() {
        return walker;
    }

    public List<List<Orbit>> getAllOrbits() {
        return allOrbits;
    }

    public int getPhasingParameters() {
        return phasingParameters;
    }

    public int getTotalOfSatellite() {
        return totalOfSatellite;
    }
}
