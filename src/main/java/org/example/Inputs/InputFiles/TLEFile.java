package org.example.Inputs.InputFiles;

import cesiumlanguagewriter.ClockRange;
import cesiumlanguagewriter.ClockStep;
import cesiumlanguagewriter.JulianDate;
import cesiumlanguagewriter.TimeInterval;
import org.example.CZMLObjects.CZMLPrimaryObjects.Header;
import org.example.CZMLObjects.CZMLSecondaryObects.HeaderObjects.Clock;
import org.example.Inputs.InputObjet;
import org.orekit.frames.FactoryManagedFrame;
import org.orekit.frames.FramesFactory;
import org.orekit.orbits.KeplerianOrbit;
import org.orekit.orbits.Orbit;
import org.orekit.propagation.Propagator;
import org.orekit.propagation.analytical.tle.TLE;
import org.orekit.propagation.analytical.tle.TLEPropagator;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScale;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class TLEFile implements InputObjet, InputFileBuilder {

    /** .*/
    public static final String DEFAULT_DOCUMENT_NAME = "document";
    /** .*/
    public static final String DEFAULT_VERSION = "1.0";

    // Attributes
    /** .*/
    private String id;
    /** .*/
    private String name;
    /** .*/
    private String path;
    /** .*/
    private AbsoluteDate startTime;
    /** .*/
    private AbsoluteDate stopTime;
    /** .*/
    private TLE tleInputted;
    /** .*/
    private FactoryManagedFrame frame;
    /** .*/
    private TimeScale timeScale;
    /** .*/
    private Orbit orbit;

    public TLEFile(final TLE tle, final AbsoluteDate finalDate) {
        this.id = "#ID :" + tle.toString();
        this.name = tle.toString();
        this.startTime = tle.getDate();
        this.stopTime = finalDate;
        this.tleInputted = tle;
        this.timeScale = tle.getUtc();
        this.frame = FramesFactory.getTEME();

        final Propagator propagator = TLEPropagator.selectExtrapolator(tle);
        this.orbit = propagator.getInitialState().getOrbit();
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

    @Override
    public Header getHeader() {
        return this.getHeader(60.0);
    }

    public Header getHeader(final double step) {
        final JulianDate startJD = this.getJulianDate(startTime, this.timeScale);
        final JulianDate stopJD = this.getJulianDate(stopTime, this.timeScale);
        final TimeInterval interval = new TimeInterval(startJD, stopJD);
        final ClockRange range = ClockRange.LOOP_STOP;
        final ClockStep clockStep = ClockStep.SYSTEM_CLOCK_MULTIPLIER;
        final Clock clock = new Clock(interval, startJD, step, range, clockStep);

        return new Header(DEFAULT_DOCUMENT_NAME, this.name, Header.DEFAULT_VERSION, clock);
    }

    // GETS
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public TLE getTLE() {
        return tleInputted;
    }

    public AbsoluteDate getStartTime() {
        return startTime;
    }

    public AbsoluteDate getStopTime() {
        return stopTime;
    }

    public TimeScale getTimeScale() {
        return timeScale;
    }

    public FactoryManagedFrame getFrame() {
        return frame;
    }

    public Orbit getOrbit() {
        return orbit;
    }

    public TLE getTleInputted() {
        return tleInputted;
    }
}
