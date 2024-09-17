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
package org.orekit.czml.object.primary;

import cesiumlanguagewriter.ClockCesiumWriter;
import cesiumlanguagewriter.PacketCesiumWriter;
import org.orekit.czml.object.secondary.Clock;
import org.orekit.time.TimeScale;

import java.io.StringWriter;

/**
 * Header class
 *
 * <p>
 * The header object is the base to all the CZML file. It contains all the primary information needed for the scene to display.
 * The header must be the FIRST object to be created and written before any other object, else way the CZML will be corrupted.
 * </p>
 *
 * @author Julien LEBLOND
 * @since 1.0.0
 */

public class Header extends AbstractPrimaryObject implements CzmlPrimaryObject {

    /** The default id of a CZML file. */
    public static final String DEFAULT_ID = "document";

    /** The default version of a CZML file. */
    public static final String DEFAULT_VERSION = "1.0";

    /** The default path to the root folder. */
    public static final String DEFAULT_ROOT = System.getProperty("user.dir")
                                                    .replace("\\", "/");

    /** The default path to the resources' folder. */
    public static final String    DEFAULT_RESOURCES = DEFAULT_ROOT + "/src/main/resources";
    /** The master clock of the header. */
    private static      Clock     MASTER_CLOCK;
    /** The timescale of the header. */
    private static      TimeScale TIME_SCALE;
    /** JavaScript path for the public folder. */
    private static      String    pathToPublicJSFolder;
    /** The minimum step in time between each instant. */
    private             double    stepSimulation;
    /** The version of Cesium. */
    private             String    version;


    // Constructors

    /**
     * The classic builder, a name and a clock.
     *
     * @param name  : the name of the header.
     * @param clock : the clock of the header
     */
    public Header(final String name, final Clock clock, final String pathToPublicJSFolder) {
        this.setId(DEFAULT_ID);
        this.setName(name);
        this.stepSimulation       = clock.getStep()
                                         .getValue();
        this.pathToPublicJSFolder = pathToPublicJSFolder;
        MASTER_CLOCK              = clock;
        this.version              = DEFAULT_VERSION;
        TIME_SCALE                = clock.getTimeScale();
    }

    /**
     * The versioned constructor, if you don't know which version to use, does not use this builder.
     *
     * @param name    : the name of the header.
     * @param version : the version of the header.
     * @param clock   : the clock of the header.
     */
    public Header(final String name, final String version, final Clock clock) {
        this.setId(DEFAULT_ID);
        this.setName(name);
        MASTER_CLOCK        = clock;
        this.version        = version;
        this.stepSimulation = clock.getStep()
                                   .getValue();
    }


    // Overrides

    public static Clock getMasterClock() {
        return MASTER_CLOCK;
    }

    public static TimeScale getTimeScale() {
        return TIME_SCALE;
    }

    public static String getPathToPublicJSFolder() {
        return pathToPublicJSFolder;
    }


    @Override
    public void writeCzmlBlock() {
        OUTPUT.setPrettyFormatting(true);
        OUTPUT.writeStartSequence();

        try (PacketCesiumWriter packet = STREAM.openPacket(OUTPUT)) {
            packet.writeId(this.getId());
            packet.writeVersion(DEFAULT_VERSION);
            packet.writeName(this.getName());
            packet.writeVersion(version);
            try (ClockCesiumWriter clockWriter = packet.getClockWriter()) {
                MASTER_CLOCK.write(packet, OUTPUT);
            }
        }
    }


    // Getters

    @Override
    public StringWriter getStringWriter() {
        return STRING_WRITER;
    }

    @Override
    public void cleanObject() {
        this.setId("");
        this.setName("");
        this.stepSimulation = 0.0;
    }

    public Clock getClock() {
        return MASTER_CLOCK;
    }

    public double getStepSimulation() {
        return stepSimulation;
    }


    // Setters

    public void setStepSimulation(final double stepSimulation) {
        this.stepSimulation = stepSimulation;
    }
}
