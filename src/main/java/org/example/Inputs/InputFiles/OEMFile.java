package org.example.Inputs.InputFiles;

import cesiumlanguagewriter.*;
import org.example.CZMLObjects.CZMLPrimaryObjects.Header;
import org.example.CZMLObjects.CZMLSecondaryObects.HeaderObjects.Clock;
import org.example.Inputs.InputObjet;
import org.orekit.data.DataSource;
import org.orekit.files.ccsds.ndm.ParserBuilder;
import org.orekit.files.ccsds.ndm.odm.CartesianCovariance;
import org.orekit.files.ccsds.ndm.odm.oem.Oem;
import org.orekit.files.ccsds.ndm.odm.oem.OemParser;
import org.orekit.frames.Frame;
import org.orekit.time.*;
import org.orekit.utils.TimeStampedPVCoordinates;

import java.io.*;
import java.util.List;
import java.util.Set;

public class OEMFile implements InputObjet, InputFileBuilder {

    /** .*/
    public static final String DEFAULT_DOCUMENT_NAME = "document";
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

    public OEMFile(final String path) {

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

    public OEMFile(final Oem oem) {
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

    public OEMFile(final double version, final String ObjectID, final TimeScale timeScale, final Frame frame, final AbsoluteDate startTime, final AbsoluteDate stopTime, final List<TimeStampedPVCoordinates> Ephemeris) {
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

        return new Header(DEFAULT_DOCUMENT_NAME, this.name, Header.DEFAULT_VERSION, clock);
    }
}
