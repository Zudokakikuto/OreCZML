package org.example.Inputs.InputFiles;

import cesiumlanguagewriter.*;
import org.example.CZMLObjects.CZMLPrimaryObjects.Header;
import org.example.CZMLObjects.CZMLSecondaryObects.HeaderObjects.Clock;
import org.example.Inputs.InputObjet;
import org.orekit.data.DataSource;
import org.orekit.files.ccsds.ndm.ParserBuilder;
import org.orekit.files.ccsds.ndm.odm.oem.Oem;
import org.orekit.files.ccsds.ndm.odm.oem.OemSegment;
import org.orekit.files.ccsds.ndm.odm.oem.OemParser;
import org.orekit.frames.Frame;
import org.orekit.time.*;
import org.orekit.utils.TimeStampedPVCoordinates;

import java.io.*;
import java.util.List;
import java.util.Set;

public class OEMFile implements InputObjet,InputFileBuilder {

    // Attributes
    private String pathName;
    private String ID;
    private String name;
    private String objectID;

    private final double version;

    private TimeScale timeScale;

    private final Frame frame;

    private final AbsoluteDate startTime;
    private final AbsoluteDate stopTime;

    private final List<TimeStampedPVCoordinates> Ephemeris;

    // Optional attributes
    private int interpolationDegree;
    private String covariances;
    private String covarianceFrame;

    private int numberOfSatellites;

    private List<String> interpolations;

    private AbsoluteDate covarianceStartTime;
    private AbsoluteDate covarianceStopTime;

    public OEMFile(String path){

        final ParserBuilder parserBuilder = new ParserBuilder();
        final OemParser oemParser = parserBuilder.buildOemParser();
        final Oem oem = oemParser.parse(new DataSource(path));

        Set<String> set = oem.getSatellites().keySet();
        this.numberOfSatellites = set.size();
        this.ID = oem.getHeader().getMessageId();
        this.version = oem.getHeader().getFormatVersion();
        this.frame = oem.getSegments().get(0).getFrame();
        this.startTime = oem.getSegments().get(0).getStart();
        this.stopTime = oem.getSegments().get(0).getStop();
        this.Ephemeris = oem.getSegments().get(0).getData().getEphemeridesDataLines();
        this.timeScale = oem.getDataContext().getTimeScales().getUTC();
    }

    public OEMFile(double version, String ObjectID, TimeScale timeScale, Frame frame, AbsoluteDate startTime, AbsoluteDate stopTime, List<TimeStampedPVCoordinates> Ephemeris){
        this.objectID = ObjectID;
        this.version = version;
        this.timeScale = timeScale;
        this.frame = frame;
        this.startTime = startTime;
        this.stopTime = stopTime;
        this.Ephemeris = Ephemeris;
    }

    @Override
    public String read() throws IOException {
        String path = this.getPath();
        FileReader reader = new FileReader(path);
        return reader.toString();
    }

    @Override
    public void close() throws IOException {
        String path = this.getPath();
        FileWriter writer = new FileWriter(path);
        writer.close();
    }

    @Override
    public String getPath() {
        return this.pathName;
    }

    public String getID(){
        return this.ID;
    }

    public double getVersion(){
        return this.version;
    }

    public String getObjectID() { return this.objectID; }

    public TimeScale getTimeScale(){
        return this.timeScale;
    }

    public Frame getFrame(){
        return this.frame;
    }

    public AbsoluteDate getStartTime(){
        return this.startTime;
    }

    public AbsoluteDate getStopTime(){
        return this.stopTime;
    }

    public List<TimeStampedPVCoordinates> getEphemeris(){
        return this.Ephemeris;
    }

    @Override
    public Header getHeader() {

        JulianDate startJD = this.getJulianDate(startTime,this.timeScale);
        JulianDate stopJD = this.getJulianDate(stopTime,this.timeScale);
        TimeInterval interval = new TimeInterval(startJD,stopJD);
        double multiplier = 60.0;
        ClockRange range = ClockRange.LOOP_STOP;
        ClockStep step = ClockStep.SYSTEM_CLOCK_MULTIPLIER;
        Clock clock = new Clock(interval,startJD,multiplier,range,step);

        return new Header("document", this.name,1.0,clock);
    }
}
