package org.example.Inputs.OrbitInput;

import cesiumlanguagewriter.ClockRange;
import cesiumlanguagewriter.ClockStep;
import cesiumlanguagewriter.JulianDate;
import cesiumlanguagewriter.TimeInterval;
import org.example.CZMLObjects.CZMLPrimaryObjects.Header;
import org.example.CZMLObjects.CZMLSecondaryObects.HeaderObjects.Clock;
import org.example.Inputs.InputObjet;
import org.orekit.frames.Frame;
import org.orekit.orbits.KeplerianOrbit;
import org.orekit.orbits.Orbit;
import org.orekit.orbits.OrbitType;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScale;

public class OrbitInput implements InputObjet {

    // Keplerian parameters
    private double semiMajorAxis;
    private double eccentricity;
    private double inclination;
    private double perigeeArgument;
    private double rightAscensionOfAscendingNode;
    private double meanAnomaly;

    // Equinoctial parameters
    private double ex;
    private double ey;
    private double hx;
    private double hy;
    private double lv;

    // Cartesian parameters
    private double posX;
    private double posY;
    private double posZ;
    private double velX;
    private double velY;
    private double velZ;

    private OrbitType orbitType;
    private Orbit orbit;
    private TimeScale timeScale;
    private TimeInterval timeInterval;
    private AbsoluteDate startTime;
    private AbsoluteDate stopTime;
    private double period;
    private Frame frame;

    public OrbitInput(Orbit orbit, OrbitType orbitType, TimeScale timeScale){
        if(orbitType == OrbitType.KEPLERIAN) {
            this.semiMajorAxis = orbit.getA();
            this.eccentricity = orbit.getE();
            this.inclination = orbit.getI();
            this.perigeeArgument = ((KeplerianOrbit) orbit).getPerigeeArgument();
            this.rightAscensionOfAscendingNode = ((KeplerianOrbit) orbit).getRightAscensionOfAscendingNode();
            this.meanAnomaly = ((KeplerianOrbit) orbit).getMeanAnomaly();
        }

        if(orbitType == OrbitType.EQUINOCTIAL){
            this.semiMajorAxis = orbit.getA();
            this.ex = orbit.getEquinoctialEx();
            this.ey = orbit.getEquinoctialEy();
            this.hx = orbit.getHx();
            this.hy = orbit.getHy();
            this.lv = orbit.getLv();
        }

        if(orbitType == OrbitType.CARTESIAN){
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
        this.timeInterval = new TimeInterval(getJulianDate(startTime,timeScale),getJulianDate(stopTime,timeScale));
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
