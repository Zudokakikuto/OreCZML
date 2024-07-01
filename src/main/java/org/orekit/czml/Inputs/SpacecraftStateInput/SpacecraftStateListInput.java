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
package org.orekit.czml.Inputs.SpacecraftStateInput;

import cesiumlanguagewriter.ClockRange;
import cesiumlanguagewriter.ClockStep;
import cesiumlanguagewriter.JulianDate;
import cesiumlanguagewriter.TimeInterval;
import org.orekit.czml.Inputs.InputObjet;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.Header;
import org.orekit.czml.CzmlObjects.CzmlSecondaryObjects.HeaderObjects.Clock;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.orekit.frames.Frame;
import org.orekit.orbits.Orbit;
import org.orekit.propagation.SpacecraftState;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScale;
import org.orekit.time.TimeScalesFactory;

import java.util.ArrayList;
import java.util.List;

/** SpaceCraft State List Input

 * <p>
 * The SpaceCraft State List Input aims at interfacing a list of SpaceCraftState with all OreCzml objects.
 * </p>
 *
 * @since 1.0
 * @author Julien LEBLOND.
 */

public class SpacecraftStateListInput implements InputObjet {

    /** .*/
    private double period;
    /** .*/
    private Frame frame;
    /** .*/
    private TimeInterval timeInterval;

    // Intrinsic parameters
    /** .*/
    private AbsoluteDate startTime;
    /** .*/
    private AbsoluteDate stopTime;
    /** .*/
    private TimeScale timeScale;
    /** .*/
    private List<Vector3D> positions;
    /** .*/
    private List<Double> timeList;
    /** .*/
    private List<Orbit> orbits;

    public SpacecraftStateListInput(final List<SpacecraftState> list) {

        positions = new ArrayList<Vector3D>();
        timeList = new ArrayList<Double>();
        orbits = new ArrayList<Orbit>();

        startTime = list.get(0).getDate();
        stopTime = list.get(list.size() - 1).getDate();
        frame = list.get(0).getFrame();
        period = list.get(0).getKeplerianPeriod();
        timeScale = TimeScalesFactory.getUTC();
        this.timeInterval = new TimeInterval(getJulianDate(startTime, timeScale), getJulianDate(stopTime, timeScale));

        for (int i = 0; i < list.size(); i++) {
            positions.add(list.get(i).getPosition());
            timeList.add(dateToDouble(list.get(i).getDate()));
            orbits.add(list.get(i).getOrbit());
        }
    }

    /** This method sets the step of the simulation at 60.0 and gets the header with default parameters.*/
    @Override
    public Header getHeader() {
        return this.getHeader(60.0);
    }

    /**@param step : The step in time used in the simulation
     * @return : A header with a given step for simulation and default parameters*/
    public Header getHeader(final double step) {
        final JulianDate startJD = this.getJulianDate(startTime, this.timeScale);
        final JulianDate stopJD = this.getJulianDate(stopTime, this.timeScale);
        final TimeInterval interval = new TimeInterval(startJD, stopJD);
        final ClockRange range = ClockRange.LOOP_STOP;
        final ClockStep multiplier = ClockStep.SYSTEM_CLOCK_MULTIPLIER;
        final Clock clock = new Clock(interval, startJD, step, range, multiplier);
        return new Header("No_title", "1.0", clock);
    }

    public AbsoluteDate getStopTime() {
        return stopTime;
    }

    public AbsoluteDate getStartTime() {
        return startTime;
    }

    public Frame getFrame() {
        return frame;
    }

    public TimeScale getTimeScale() {
        return timeScale;
    }

    public double getPeriod() {
        return period;
    }

    public TimeInterval getTimeInterval() {
        return timeInterval;
    }

    public List<Vector3D> getPositions() {
        return positions;
    }

    public List<Double> getTimeList() {
        return timeList;
    }

    public List<Orbit> getOrbits() {
        return orbits;
    }
}
