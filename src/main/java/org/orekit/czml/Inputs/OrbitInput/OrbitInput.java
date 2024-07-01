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
package org.orekit.czml.Inputs.OrbitInput;

import cesiumlanguagewriter.ClockRange;
import cesiumlanguagewriter.ClockStep;
import cesiumlanguagewriter.JulianDate;
import cesiumlanguagewriter.TimeInterval;
import org.orekit.czml.Inputs.InputObjet;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.Header;
import org.orekit.czml.CzmlObjects.CzmlSecondaryObjects.HeaderObjects.Clock;
import org.orekit.frames.Frame;
import org.orekit.orbits.KeplerianOrbit;
import org.orekit.orbits.Orbit;
import org.orekit.orbits.OrbitType;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScale;

/** Orbit Input

 * <p>
 * The orbit input aims at taking an orbit in input in order to interface it with all OreCzml objects.
 * </p>
 *
 * @since 1.0
 * @author Julien LEBLOND.
 */
public class OrbitInput implements InputObjet {

    // Keplerian parameters
    /** .*/
    private double semiMajorAxis;
    /** .*/
    private double eccentricity;
    /** .*/
    private double inclination;
    /** .*/
    private double perigeeArgument;
    /** .*/
    private double rightAscensionOfAscendingNode;
    /** .*/
    private double meanAnomaly;

    // Equinoctial parameters
    /** .*/
    private double ex;
    /** .*/
    private double ey;
    /** .*/
    private double hx;
    /** .*/
    private double hy;
    /** .*/
    private double lv;

    // Cartesian parameters
    /** .*/
    private double posX;
    /** .*/
    private double posY;
    /** .*/
    private double posZ;
    /** .*/
    private double velX;
    /** .*/
    private double velY;
    /** .*/
    private double velZ;

    /** .*/
    private OrbitType orbitType;
    /** .*/
    private Orbit orbit;
    /** .*/
    private TimeScale timeScale;
    /** .*/
    private TimeInterval timeInterval;
    /** .*/
    private AbsoluteDate startTime;
    /** .*/
    private AbsoluteDate stopTime;
    /** .*/
    private double period;
    /** .*/
    private Frame frame;

    public OrbitInput(final Orbit orbit, final OrbitType orbitType, final TimeScale timeScale) {
        if (orbitType == OrbitType.KEPLERIAN) {
            this.semiMajorAxis = orbit.getA();
            this.eccentricity = orbit.getE();
            this.inclination = orbit.getI();
            this.perigeeArgument = ((KeplerianOrbit) orbit).getPerigeeArgument();
            this.rightAscensionOfAscendingNode = ((KeplerianOrbit) orbit).getRightAscensionOfAscendingNode();
            this.meanAnomaly = ((KeplerianOrbit) orbit).getMeanAnomaly();
        }

        if (orbitType == OrbitType.EQUINOCTIAL) {
            this.semiMajorAxis = orbit.getA();
            this.ex = orbit.getEquinoctialEx();
            this.ey = orbit.getEquinoctialEy();
            this.hx = orbit.getHx();
            this.hy = orbit.getHy();
            this.lv = orbit.getLv();
        }

        if (orbitType == OrbitType.CARTESIAN) {
            this.posX = orbit.getPosition().getX();
            this.posY = orbit.getPosition().getY();
            this.posZ = orbit.getPosition().getZ();
            this.velX = orbit.getPVCoordinates().getVelocity().getX();
            this.velY = orbit.getPVCoordinates().getVelocity().getY();
            this.velZ = orbit.getPVCoordinates().getVelocity().getZ();
        }

        this.startTime = orbit.getDate();
        this.period = orbit.getKeplerianPeriod();
        this.stopTime = startTime.shiftedBy(period);
        this.orbitType = orbitType;
        this.orbit = orbit;
        this.timeScale = timeScale;
        this.frame = orbit.getFrame();
        this.timeInterval = new TimeInterval(getJulianDate(startTime, timeScale), getJulianDate(stopTime, timeScale));
    }

    /** This method sets the step of the simulation at 60.0 and gets the header with default parameters.*/
    @Override
    public Header getHeader() {
        return this.getheader(60.0);
    }

    public Header getheader(final double step) {
        final JulianDate startJD = this.getJulianDate(startTime, this.timeScale);
        final JulianDate stopJD = this.getJulianDate(stopTime, this.timeScale);
        final TimeInterval interval = new TimeInterval(startJD, stopJD);
        final ClockRange range = ClockRange.LOOP_STOP;
        final ClockStep multiplier = ClockStep.SYSTEM_CLOCK_MULTIPLIER;
        final Clock clock = new Clock(interval, startJD, step, range, multiplier);
        return new Header("No_title", "1.0", clock);
    }

    // GETS
    public double getSemiMajorAxis() {
        return semiMajorAxis;
    }

    public double getEccentricity() {
        return eccentricity;
    }

    public double getInclination() {
        return inclination;
    }

    public double getPerigeeArgument() {
        return perigeeArgument;
    }

    public double getRaan() {
        return rightAscensionOfAscendingNode;
    }

    public double getMeanAnomaly() {
        return meanAnomaly;
    }

    public double getEx() {
        return ex;
    }

    public double getEy() {
        return ey;
    }

    public double getHx() {
        return hx;
    }

    public double getHy() {
        return hy;
    }

    public double getLv() {
        return lv;
    }

    public double getPosX() {
        return posX;
    }

    public double getPosY() {
        return posY;
    }

    public double getPosZ() {
        return posZ;
    }

    public double getVelX() {
        return velX;
    }

    public double getVelY() {
        return velY;
    }

    public double getVelZ() {
        return velZ;
    }

    public Orbit getOrbit() {
        return orbit;
    }

    public OrbitType getOrbitType() {
        return orbitType;
    }

    public TimeScale getTimeScale() {
        return timeScale;
    }

    public TimeInterval getTimeInterval() {
        return timeInterval;
    }

    public double getPeriod() {
        return period;
    }

    public AbsoluteDate getStartTime() {
        return startTime;
    }

    public AbsoluteDate getStopTime() {
        return stopTime;
    }

    public Frame getFrame() {
        return frame;
    }
}
