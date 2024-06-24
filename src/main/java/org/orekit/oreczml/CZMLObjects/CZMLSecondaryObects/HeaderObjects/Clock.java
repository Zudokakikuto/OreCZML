/** .*/

package org.example.CZMLObjects.CZMLSecondaryObects.HeaderObjects;

import cesiumlanguagewriter.*;
import org.example.CZMLObjects.CZMLSecondaryObects.CZMLSecondaryObject;
import org.example.Inputs.InputFiles.OEMFile;
import org.hipparchus.util.FastMath;
import org.orekit.time.*;
import org.orekit.utils.TimeStampedPVCoordinates;

import java.util.List;

public class Clock implements CZMLSecondaryObject {

    /** .*/
    private TimeInterval availability;
    /** .*/
    private JulianDate currentTime;
    /** .*/
    private double multiplier;
    /** .*/
    private ClockRange range;
    /** .*/
    private ClockStep step;

    public Clock(final TimeInterval interval, final JulianDate currentTime, final double multiplier, final ClockRange range, final ClockStep step) {
        this.availability = interval;
        this.currentTime = currentTime;
        this.multiplier = multiplier;
        this.range = range;
        this.step = step;
    }

    public Clock(final OEMFile oemFile) {
        this(oemFile, 60.0);
    }

    public Clock(final OEMFile file, final double step) {
        final List<TimeStampedPVCoordinates> Ephemeris = file.getEphemeris();
        final int length = Ephemeris.size();
        final AbsoluteDate startTime = file.getStartTime();
        final AbsoluteDate stopTime = file.getStopTime();
        final int step_rounded = (int) FastMath.round(stopTime.durationFrom(startTime) / length);

        final TimeScale timeScale = file.getTimeScale();
        final JulianDate startJulianDate = this.getJulianDate(startTime, timeScale);
        final JulianDate stopJulianDate = this.getJulianDate(stopTime, timeScale);

        final ClockStep clockStep = ClockStep.getFromValue(step_rounded);
        final double multiplier_temp = step;

        this.step = clockStep;
        this.availability = new TimeInterval(startJulianDate, stopJulianDate);
        this.range = ClockRange.LOOP_STOP;
        this.multiplier = multiplier_temp;
        this.currentTime = startJulianDate;
    }

    private JulianDate getJulianDate(final AbsoluteDate AbsDate, final TimeScale timeScale) {
        final DateTimeComponents components = AbsDate.getComponents(timeScale);
        final DateComponents dateComponents = components.getDate();
        final TimeComponents timeComponents = components.getTime();
        final int year = dateComponents.getYear();
        final int month = dateComponents.getMonth();
        final int day = dateComponents.getDay();
        final int hours = timeComponents.getHour();
        final int mins = timeComponents.getMinute();
        final double seconds = timeComponents.getSecond();
        final GregorianDate gregorianDate =  new GregorianDate(year, month, day, hours, mins, seconds);
        return new JulianDate(gregorianDate);
    }

    public JulianDate getCurrentTime() {
        return currentTime;
    }

    public TimeInterval getAvailability() {
        return availability;
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
    public void write(final PacketCesiumWriter packetWriter, final CesiumOutputStream output) {

        final ClockCesiumWriter writer = packetWriter.getClockWriter();
        writer.open(output);
        writer.writeInterval(availability);
        writer.writeCurrentTime(availability.getStart());
        writer.writeMultiplier(multiplier);
        writer.writeRange(range);
        writer.writeStep(step);
    }

}
