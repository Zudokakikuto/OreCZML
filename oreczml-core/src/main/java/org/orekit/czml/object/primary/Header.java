/* Copyright 2002-2025 CS GROUP
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

import cesiumlanguagewriter.CesiumOutputStream;
import cesiumlanguagewriter.CesiumStreamWriter;
import cesiumlanguagewriter.ClockCesiumWriter;
import cesiumlanguagewriter.PacketCesiumWriter;
import cesiumlanguagewriter.TimeInterval;
import org.orekit.czml.errors.OreCzmlException;
import org.orekit.czml.errors.OreCzmlMessages;
import org.orekit.czml.file.CzmlFile;
import org.orekit.czml.object.secondary.Clock;

import java.io.StringWriter;
import java.util.Objects;

/**
 * Header class
 * <p>
 * The header object is the base to all the CZML file. It contains all the
 * primary information needed for the scene to display. The header must be the
 * FIRST object to be created and written before any other object, else way the
 * CZML will be corrupted.
 *
 * @author Julien LEBLOND
 * @since 1.0.0
 */
public class Header
    extends
    AbstractPrimaryObject<Header> {

    /**
     * The default id of a CZML file.
     */
    public static final String DEFAULT_ID = "document";

    /**
     * The default version of a CZML file.
     */
    public static final String DEFAULT_VERSION = "1.0";

    /**
     * The default path to the resources' folder.
     */
    private static final String DEFAULT_RESOURCES =
        CzmlFile.getDefaultRoot() + "/oreczml-core/src/main/resources";

    /**
     * The path to the external resources that the user may want to use. If
     * cesium JS is used, the resource folder must be the 'public' folder. If
     * the user does not mention a path for external resources, personalized 3D
     * or 2D models can't be used.
     */
    private static String pathToExternalResourceFolder;

    /** The private clock of the header. */
    private final Clock clock;

    /**
     * The version of Cesium.
     */
    private final String version;

    /** The path ot the external resources. */
    private String pathToExternalResources = DEFAULT_RESOURCES;

    // Constructors

    /**
     * The classic builder, a name and a clock.
     *
     * @param name : the name of the header.
     * @param masterClock : the clock of the header
     */
    public Header(final String name, final Clock masterClock) {
        this.setId(DEFAULT_ID);
        this.setName(name);
        pathToExternalResourceFolder = "";
        this.clock = masterClock;
        this.version = DEFAULT_VERSION;
    }

    /**
     * The classic builder, a name and a clock.
     *
     * @param name : the name of the header.
     * @param masterClock : the clock of the header
     * @param pathToExternalResourceFolder : The path to the JavaScript folder
     *        if one is used.
     */
    public Header(final String name, final Clock masterClock,
                  final String pathToExternalResourceFolder) {
        this.setId(DEFAULT_ID);
        this.setName(name);
        this.pathToExternalResources = pathToExternalResourceFolder;
        Header.pathToExternalResourceFolder = pathToExternalResourceFolder;
        this.clock = masterClock;
        this.version = DEFAULT_VERSION;
    }

    /**
     * The versioned constructor, if you don't know which version to use, does
     * not use this builder.
     *
     * @param name : the name of the header.
     * @param version : the version of the header.
     * @param clock : the clock of the header.
     */
    public Header(final String name, final String version, final Clock clock) {
        this.setId(DEFAULT_ID);
        this.setName(name);
        this.version = version;
        this.clock = clock.cloneObject();
    }

    /**
     * The versioned constructor, if you don't know which version to use, does
     * not use this builder.
     *
     * @param name : the name of the header.
     * @param version : the version of the header.
     * @param clock : the clock of the header.
     * @param pathToExternalResourceFolder : The path to the JavaScript folder
     *        if one is used.
     */
    public Header(final String name, final String version, final Clock clock,
                  final String pathToExternalResourceFolder) {
        this.setId(DEFAULT_ID);
        this.setName(name);
        this.version = version;
        this.clock = clock.cloneObject();
        Header.pathToExternalResourceFolder = pathToExternalResourceFolder;
        this.pathToExternalResources = pathToExternalResourceFolder;
    }

    // Overrides

    @Override
    public void writeCzmlBlock(final CesiumStreamWriter stream,
                               final CesiumOutputStream output) {
        output.setPrettyFormatting(true);
        output.writeStartSequence();

        try (PacketCesiumWriter packet = stream.openPacket(output)) {
            packet.writeId(this.getId());
            packet.writeName(this.getName());
            packet.writeVersion(version);
            try (ClockCesiumWriter ignored = packet.getClockWriter()) {
                clock.write(packet, output);
            }
        }
    }

    @Override
    public String toString() {
        final StringWriter writer = new StringWriter();
        final CesiumOutputStream output = new CesiumOutputStream(writer);
        final CesiumStreamWriter streamWriter = new CesiumStreamWriter();
        this.writeCzmlBlock(streamWriter, output);
        final String tempString = writer.toString();
        final String[] splittedString = tempString.split("\\[");
        return splittedString[1];
    }

    @Override
    public Header cloneObject() {
        final Header toReturn;
        if (this.getName() != null && this.getAvailability() != null) {
            if (this.version != null) {
                if (!Objects.equals(this.pathToExternalResources,
                                    DEFAULT_RESOURCES)) {
                    final Header copy =
                        new Header(this.getName(), this.version, this.clock,
                                   this.pathToExternalResources);
                    copy.setId(getId());
                    copy.setAvailability(getAvailability());
                    toReturn = copy;
                } else {
                    final Header copy =
                        new Header(this.getName(), this.version, this.clock);
                    copy.setId(getId());
                    copy.setAvailability(getAvailability());
                    toReturn = copy;
                }
            } else if (!Objects.equals(this.pathToExternalResources,
                                       DEFAULT_RESOURCES)) {
                final Header copy =
                    new Header(this.getName(), this.clock,
                               this.pathToExternalResources);
                copy.setId(getId());
                copy.setAvailability(getAvailability());
                toReturn = copy;
            } else {
                final Header copy = new Header(this.getName(), this.clock);
                copy.setId(getId());
                copy.setAvailability(getAvailability());
                toReturn = copy;
            }
            return toReturn;
        } else {
            throw new OreCzmlException(OreCzmlMessages.NOT_VALID_PRIMARY_OBJECT_FOR_CLONE);
        }
    }

    @Override
    public TimeInterval getAvailability() {
        return getClock().getAvailability();
    }

    // Getters

    /**
     * Gets path to external resource folder.
     *
     * @return the path to external resource folder
     */
    public static String getPathToExternalResourceFolder() {
        return pathToExternalResourceFolder;
    }

    /**
     * Gets clock.
     *
     * @return the clock
     */
    public Clock getClock() {
        return clock;
    }

    /**
     * Gets default resources.
     *
     * @return the default resources
     */
    public static String getDefaultResources() {
        return DEFAULT_RESOURCES;
    }
}
