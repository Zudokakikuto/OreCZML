package org.example.CZMLObjects.CZMLSecondaryObects.HeaderObjects;

import cesiumlanguagewriter.*;
import org.example.CZMLObjects.CZMLSecondaryObects.CZMLSecondaryObject;
import org.example.Inputs.InputFiles.OEMFile;
import org.hipparchus.util.FastMath;
import org.orekit.time.*;
import org.orekit.utils.TimeStampedPVCoordinates;

import java.util.List;

public class Clock implements CZMLSecondaryObject {

    private TimeInterval interval;
    private JulianDate currentTime;
    private double multiplier;
    private ClockRange range;
    private ClockStep step;

    public Clock(TimeInterval interval, JulianDate currentTime, double multiplier, ClockRange range, ClockStep step){
        this.interval=interval;
        this.currentTime = currentTime;
        this.multiplier = multiplier;
        this.range = range;
        this.step = step;
    }

    public Clock(OEMFile oemFile) {

        List<TimeStampedPVCoordinates> Ephemeris = oemFile.getEphemeris();
        int length = Ephemeris.size();
        AbsoluteDate startTime = oemFile.getStartTime();
        AbsoluteDate stopTime = oemFile.getStopTime();
        int step = (int) FastMath.round(stopTime.durationFrom(startTime) / length);

        TimeScale timeScale = oemFile.getTimeScale();
        JulianDate startJulianDate = this.getJulianDate(startTime, timeScale);
        JulianDate stopJulianDate = this.getJulianDate(stopTime, timeScale);

        ClockStep clockStep = ClockStep.getFromValue(step);
        double multiplier = 60.0;

        this.step = clockStep;
        this.interval = new TimeInterval(startJulianDate, stopJulianDate);
        this.range = ClockRange.LOOP_STOP;
        this.multiplier = multiplier;
        this.currentTime = startJulianDate;
    }

    private JulianDate getJulianDate(AbsoluteDate AbsDate, TimeScale timeScale){
        DateTimeComponents components = AbsDate.getComponents(timeScale);
        DateComponents dateComponents = components.getDate();
        TimeComponents timeComponents = components.getTime();
        int year = dateComponents.getYear();
        int month = dateComponents.getMonth();
        int day = dateComponents.getDay();
        int hours = timeComponents.getHour();
        int mins = timeComponents.getMinute();
        double seconds = timeComponents.getSecond();
        GregorianDate gregorianDate =  new GregorianDate(year,month,day,hours,mins,seconds);
        return new JulianDate(gregorianDate);
    }

    public JulianDate getCurrentTime() {
        return currentTime;
    }

    public TimeInterval getInterval() {
        return interval;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public ClockRange getRange() {
        return range;
    }

    public ClockStep getStep() {
        return step;
    }


    @Override
    public void write(PacketCesiumWriter packetWriter, CesiumOutputStream output) {

        ClockCesiumWriter writer = packetWriter.getClockWriter();
        writer.open(output);
        writer.writeInterval(interval);
        writer.writeCurrentTime(interval.getStart());
        writer.writeMultiplier(multiplier);
        writer.writeRange(range);
        writer.writeStep(step);
    }

}
