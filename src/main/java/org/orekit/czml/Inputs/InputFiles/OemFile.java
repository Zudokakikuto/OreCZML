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
package org.orekit.czml.Inputs.InputFiles;

import cesiumlanguagewriter.ClockRange;
import cesiumlanguagewriter.ClockStep;
import cesiumlanguagewriter.JulianDate;
import cesiumlanguagewriter.TimeInterval;
import org.orekit.czml.Inputs.InputObjet;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.Header;
import org.orekit.czml.CzmlObjects.CzmlSecondaryObjects.HeaderObjects.Clock;
import org.orekit.data.DataSource;
import org.orekit.files.ccsds.ndm.ParserBuilder;
import org.orekit.files.ccsds.ndm.odm.CartesianCovariance;
import org.orekit.files.ccsds.ndm.odm.oem.Oem;
import org.orekit.files.ccsds.ndm.odm.oem.OemParser;
import org.orekit.frames.Frame;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScale;
import org.orekit.utils.TimeStampedPVCoordinates;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/** Oem File

 * <p>
 * The Oem file is an object that aims at interfacing the oem object from orekit with objects of OreCzml.
 * </p>
 *
 * @since 1.0
 * @author Julien LEBLOND.
 */

public class OemFile implements InputObjet, InputFileBuilder {

    /** .*/
    public static final String DEFAULT_VERSION = "1.0";
    // Attributes
    /** .*/
    private String path;
    /** .*/
    private String ID;
    /** .*/
    private String name;
    /** .*/
    private String objectID;

    /** .*/
    private final double version;
    /** .*/
    private TimeScale timeScale;
    /** .*/
    private final Frame frame;
    /** .*/
    private final AbsoluteDate startTime;
    /** .*/
    private final AbsoluteDate stopTime;
    /** .*/
    private final List<TimeStampedPVCoordinates> Ephemeris;

    // Optional attributes
    /** .*/
    private int interpolationDegree;
    /** .*/
    private List<List<CartesianCovariance>> covariances;
    /** .*/
    private String covarianceFrame;
    /** .*/
    private int numberOfSatellites;
    /** .*/
    private List<String> interpolations;
    /** .*/
    private AbsoluteDate covarianceStartTime;
    /** .*/
    private AbsoluteDate covarianceStopTime;

    public OemFile(final String path) {

        final ParserBuilder parserBuilder = new ParserBuilder();
        final OemParser oemParser = parserBuilder.buildOemParser();
        final Oem oem = oemParser.parse(new DataSource(path));

        final Set<String> set = oem.getSatellites().keySet();
        this.numberOfSatellites = set.size();
        this.ID = oem.getHeader().getMessageId();
        this.version = oem.getHeader().getFormatVersion();
        this.frame = oem.getSegments().get(0).getFrame();
        this.startTime = oem.getSegments().get(0).getStart();
        this.stopTime = oem.getSegments().get(0).getStop();
        this.Ephemeris = oem.getSegments().get(0).getData().getEphemeridesDataLines();
        this.timeScale = oem.getDataContext().getTimeScales().getUTC();
        for (int i = 0; i < oem.getSegments().size(); i++) {
            this.covariances.add(oem.getSegments().get(i).getCovarianceMatrices());
        }
    }

    public OemFile(final Oem oem) {
        final Set<String> set = oem.getSatellites().keySet();
        this.numberOfSatellites = set.size();
        this.ID = oem.getHeader().getMessageId();
        this.version = oem.getHeader().getFormatVersion();
        this.frame = oem.getSegments().get(0).getFrame();
        this.startTime = oem.getSegments().get(0).getStart();
        this.stopTime = oem.getSegments().get(0).getStop();
        this.Ephemeris = oem.getSegments().get(0).getData().getEphemeridesDataLines();
        this.timeScale = oem.getDataContext().getTimeScales().getUTC();
        for (int i = 0; i < oem.getSegments().size(); i++) {
            this.covariances.add(oem.getSegments().get(i).getCovarianceMatrices());
        }
    }

    public OemFile(final double version, final String ObjectID, final TimeScale timeScale, final Frame frame, final AbsoluteDate startTime, final AbsoluteDate stopTime, final List<TimeStampedPVCoordinates> Ephemeris) {
        this.objectID = ObjectID;
        this.version = version;
        this.timeScale = timeScale;
        this.frame = frame;
        this.startTime = startTime;
        this.stopTime = stopTime;
        this.Ephemeris = Ephemeris;
    }

    @Override
    public String read(final String inputPath) throws IOException {
        this.path = inputPath;
        final FileReader reader = new FileReader(path);
        return reader.toString();
    }

    @Override
    public void close() throws IOException {
        final FileWriter writer = new FileWriter(path);
        writer.close();
    }

    @Override
    public String getPath() {
        return this.path;
    }

    public String getID() {
        return this.ID;
    }

    public double getVersion() {
        return this.version;
    }

    public List<List<CartesianCovariance>> getCovariances() {
        return covariances;
    }

    public String getObjectID() { return this.objectID; }

    public TimeScale getTimeScale() {
        return this.timeScale;
    }

    public Frame getFrame() {
        return this.frame;
    }

    public AbsoluteDate getStartTime() {
        return this.startTime;
    }

    public AbsoluteDate getStopTime() {
        return this.stopTime;
    }

    public List<TimeStampedPVCoordinates> getEphemeris() {
        return this.Ephemeris;
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
        final ClockStep clockStep = ClockStep.SYSTEM_CLOCK_MULTIPLIER;
        final Clock clock = new Clock(interval, startJD, step, range, clockStep);

        return new Header(this.name, Header.DEFAULT_VERSION, clock);
    }
}
