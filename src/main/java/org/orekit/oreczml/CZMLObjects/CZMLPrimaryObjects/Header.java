package org.example.CZMLObjects.CZMLPrimaryObjects;

import cesiumlanguagewriter.*;
import org.example.CZMLObjects.CZMLSecondaryObects.HeaderObjects.Clock;
import org.orekit.propagation.Propagator;
import org.orekit.time.AbsoluteDate;

import java.io.StringWriter;

public class Header implements CZMLPrimaryObject {

    /** .*/
    public static final String DEFAULT_ID = "document";
    /** .*/
    public static final String DEFAULT_VERSION = "1.0";

    /** .*/
    private String id;
    /** .*/
    private String name;
    /** .*/
    private String version;
    /** .*/
    private Clock clock;
    /** .*/
    private double stepSimulation;

    public Header(final String ID, final String Name, final Clock clock) {
        this.id = DEFAULT_ID;
        this.name = Name;
        this.version = DEFAULT_VERSION;
        this.clock = clock;
        this.stepSimulation = this.clock.getStep().getValue();
    }

    public Header(final String ID, final String Name, final String version, final Clock clock) {
        this.id = DEFAULT_ID;
        this.name = Name;
        this.version = version;
        this.clock = clock;
        this.stepSimulation = this.clock.getStep().getValue();
    }

    public Header(final Propagator propagator, final AbsoluteDate finalDate) {
        this(propagator, finalDate, 60.0);
        this.stepSimulation = 60.0;
    }

    public Header(final Propagator propagator, final AbsoluteDate finalDate, final double stepSimulation) {
        this.id = DEFAULT_ID;
        this.name = propagator.toString();
        this.version = DEFAULT_VERSION;
        final JulianDate startDate = absoluteDateToJulianDate(propagator.getInitialState().getDate());
        final JulianDate stopDate = absoluteDateToJulianDate(finalDate);
        final TimeInterval timeInterval = new TimeInterval(startDate, stopDate);
        final ClockRange clockRange = ClockRange.LOOP_STOP;
        final ClockStep clockStep = ClockStep.SYSTEM_CLOCK_MULTIPLIER;
        this.clock = new Clock(timeInterval, startDate, stepSimulation, clockRange, clockStep);
        this.stepSimulation = stepSimulation;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void generateCZML() {
        OUTPUT.setPrettyFormatting(true);
        OUTPUT.writeStartSequence();

        try (PacketCesiumWriter packet = STREAM.openPacket(OUTPUT)) {
            packet.writeId(this.id);
            packet.writeVersion(DEFAULT_VERSION);
            packet.writeName(this.name);
            try (ClockCesiumWriter clockWriter = packet.getClockWriter()) {
                clock.write(packet, OUTPUT);
            }
        }
    }

    @Override
    public StringWriter getStringWriter() {
        return STRING_WRITER;
    }

    @Override
    public void endFile() {
        OUTPUT.writeEndSequence();
    }

    @Override
    public void cleanObject() {
        this.id = "";
        this.name = "";
        this.stepSimulation = 0.0;
        this.clock = null;
        this.version = "";
    }

    public Clock getClock() {
        return clock;
    }

    public double getStepSimulation() {
        return stepSimulation;
    }
}
