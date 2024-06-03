package org.example.Inputs.SpacecraftStateInput;

import cesiumlanguagewriter.ClockRange;
import cesiumlanguagewriter.ClockStep;
import cesiumlanguagewriter.JulianDate;
import cesiumlanguagewriter.TimeInterval;
import org.example.CZMLObjects.CZMLPrimaryObjects.Header;
import org.example.CZMLObjects.CZMLSecondaryObects.HeaderObjects.Clock;
import org.example.Inputs.InputObjet;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.orekit.frames.Frame;
import org.orekit.orbits.Orbit;
import org.orekit.propagation.SpacecraftState;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScale;
import org.orekit.time.TimeScalesFactory;

import java.util.ArrayList;
import java.util.List;

public class SpacecraftStateListInput implements InputObjet {

    private double period;
    private Frame frame;
    private TimeInterval timeInterval;

    // Intrinsic parameters
    private AbsoluteDate startTime;
    private AbsoluteDate stopTime;
    private TimeScale timeScale;
    private List<Vector3D> positions;
    private List<Double> timeList;
    private List<Orbit> orbits;

    public SpacecraftStateListInput(List<SpacecraftState> list){

        positions = new ArrayList<Vector3D>();
        timeList = new ArrayList<Double>();
        orbits = new ArrayList<Orbit>();

        startTime = list.get(0).getDate();
        stopTime = list.get(list.size()-1).getDate();
        frame = list.get(0).getFrame();
        period = list.get(0).getKeplerianPeriod();
        timeScale = TimeScalesFactory.getUTC();
        this.timeInterval = new TimeInterval(getJulianDate(startTime,timeScale),getJulianDate(stopTime,timeScale));

        for (int i = 0; i < list.size(); i++) {
            positions.add(list.get(i).getPosition());
            timeList.add(dateToDouble(list.get(i).getDate()));
            orbits.add(list.get(i).getOrbit());
        }
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
        return new Header("document", "No_title",1.0,clock);
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
