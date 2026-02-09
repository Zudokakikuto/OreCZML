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
package org.orekit.czml.object.primary.visu;

import org.orekit.czml.errors.OresiumException;
import org.orekit.czml.errors.OresiumMessages;
import org.orekit.czml.object.primary.entities.Constellation;
import org.orekit.czml.object.primary.entities.Spacecraft;
import org.orekit.czml.object.secondary.Clock;
import org.orekit.errors.OrekitIOException;
import org.orekit.frames.TopocentricFrame;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Builder for the multiple line of visibility object.
 *
 * @author Julien Leblond
 * @since 1.1
 */
public class MultipleLineOfVisibilityBuilder {

    // Arguments

    /** The topocentric frame considered. */
    private final List<TopocentricFrame> topocentricFrames;

    /** The spacecraft if one is defined. */
    private Spacecraft spacecraft = null;

    /** The constellation if one is defined. */
    private Constellation constellation = null;

    /** The line of visibilities built. */
    private List<LineOfVisibility> lines = new ArrayList<>();

    /** The custom id of the lines. */
    private List<String> customIds = new ArrayList<>();

    // Constructors

    /** The custom clock of the lines. */
    private List<Clock> clocks = new ArrayList<>();

    /** Display triangles of all the line of visibility. */
    private boolean displayTriangle;

    /**
     * The default constructor with a spacecraft.
     *
     * @param topocentricFramesInput : The topocentric frames considered
     * @param spacecraftInput : The spacecraft
     */
    public MultipleLineOfVisibilityBuilder(final List<TopocentricFrame> topocentricFramesInput,
                                           final Spacecraft spacecraftInput) {
        this.topocentricFrames = topocentricFramesInput;
        this.spacecraft = spacecraftInput;
    }

    /**
     * The default constructor with a cosntellation.
     *
     * @param topocentricFramesInput : The topocentic frame considered
     * @param constellationInput : The constellation
     */
    public MultipleLineOfVisibilityBuilder(final List<TopocentricFrame> topocentricFramesInput,
                                           final Constellation constellationInput) {
        this.topocentricFrames = topocentricFramesInput;
        this.constellation = constellationInput;
    }

    /**
     * Function to set up a custom id for each line of visibility.
     *
     * @param ids : The id to set up
     * @return The multiple line of visibility builder with custom names
     */
    public MultipleLineOfVisibilityBuilder
        withCustomIds(final List<String> ids) {
        this.customIds = ids;
        return this;
    }

    /**
     * Function to set up a custom clock for each line of visibility.
     *
     * @param clocksInput : The clocks to set up
     * @return The multiple line of visibility builder with custom names
     */
    public MultipleLineOfVisibilityBuilder
        withCustomClocks(final List<Clock> clocksInput) {
        this.clocks = clocksInput;
        return this;
    }

    /**
     * Function to display the visibility triangle of the line of visibility.
     *
     * @return : The line of visibility builder with visibility triangles
     *         displayed
     */
    public MultipleLineOfVisibilityBuilder withVisibilityTriangle() {
        this.displayTriangle = true;
        return this;
    }

    public MultipleLineOfVisibility build()
        throws URISyntaxException,
            IOException {

        final MultipleLineOfVisibility multipleLineOfVisibility;
        this.lines =
            buildLines(topocentricFrames, spacecraft, constellation, clocks,
                       displayTriangle, customIds);
        if (spacecraft != null) {
            multipleLineOfVisibility =
                new MultipleLineOfVisibility(topocentricFrames, spacecraft);
            multipleLineOfVisibility.clear();
            multipleLineOfVisibility.addAll(lines);
        } else if (constellation != null) {
            multipleLineOfVisibility =
                new MultipleLineOfVisibility(topocentricFrames, constellation);
            multipleLineOfVisibility.clear();
            multipleLineOfVisibility.addAll(lines);
        } else {
            throw new OrekitIOException(OresiumMessages.NO_SPACECRAFT_OR_CONSTELLATION);
        }
        return multipleLineOfVisibility;
    }

    /**
     * The function build the list of the line of visibility .
     *
     * @param topocentricFramesInput : List of the topocentric frames considered
     * @param spacecraftInput : The spacecraft if one defined
     * @param constellationInput : The constellation if one defined
     * @param clocksInput : The availabilities considered
     * @param displayTriangleInput : To display the visibility triangle or not
     * @param customIdsInput : The custom ids if there is some
     * @return A list of line of visibility with the given parameters
     */
    private List<LineOfVisibility>
        buildLines(final List<TopocentricFrame> topocentricFramesInput,
                   final Spacecraft spacecraftInput,
                   final Constellation constellationInput,
                   final List<Clock> clocksInput,
                   final boolean displayTriangleInput,
                   final List<String> customIdsInput)
            throws URISyntaxException,
                IOException {

        checkSizeArguments(topocentricFramesInput, clocksInput, customIdsInput);

        final boolean areAvailabilites = !clocksInput.isEmpty();
        final boolean areCustomIds = !customIdsInput.isEmpty();

        final List<LineOfVisibility> linesBuilt = new ArrayList<>();
        if (spacecraftInput != null) {
            for (int i = 0; i < topocentricFramesInput.size(); i++) {
                final TopocentricFrame topocentricFrame =
                    topocentricFramesInput.get(i);
                final LineOfVisibilityBuilder lineBuilder =
                    LineOfVisibility.builder(topocentricFrame, spacecraftInput,
                                             spacecraftInput.getClock());
                if (areAvailabilites) {
                    lineBuilder.withClock(clocksInput.get(i));
                }
                if (areCustomIds) {
                    lineBuilder.withCustomID(customIdsInput.get(i));
                }
                if (displayTriangleInput) {
                    lineBuilder.withVisibilityTriangle();
                }
                linesBuilt.add(lineBuilder.build());
            }
        } else if (constellationInput != null) {
            for (int i = 0; i < topocentricFramesInput.size(); i++) {
                final TopocentricFrame topocentricFrame =
                    topocentricFramesInput.get(i);
                final LineOfVisibilityBuilder lineBuilder =
                    LineOfVisibility.builder(topocentricFrame,
                                             constellationInput,
                                             constellationInput.getClock());
                if (areAvailabilites) {
                    lineBuilder.withClock(clocksInput.get(i));
                }
                if (areCustomIds) {
                    lineBuilder.withCustomID(customIdsInput.get(i));
                }
                if (displayTriangleInput) {
                    lineBuilder.withVisibilityTriangle();
                }
                linesBuilt.add(lineBuilder.build());
            }
        } else {
            throw new OresiumException(OresiumMessages.NO_SPACECRAFT_OR_CONSTELLATION);
        }
        return linesBuilt;
    }

    /**
     * Ths function checks the size of each list if it is not empty.
     *
     * @param topocentricFramesInput : The topocentric frames, reference for the
     *        size of the lists
     * @param clocksInput : The clocks
     * @param customIdsInput : The custom names
     */
    private void
        checkSizeArguments(final List<TopocentricFrame> topocentricFramesInput,
                           final List<Clock> clocksInput,
                           final List<String> customIdsInput) {
        final int sizeNeeded = topocentricFramesInput.size();
        if (!(clocksInput.isEmpty()) && clocksInput.size() != sizeNeeded) {
            throw new OresiumException(OresiumMessages.NOT_SAME_SIZE_TOPOCENTRIC_FRAMES);
        }
        if (!(customIdsInput.isEmpty()) &&
            customIdsInput.size() != sizeNeeded) {
            throw new OresiumException(OresiumMessages.NOT_SAME_SIZE_TOPOCENTRIC_FRAMES);
        }
    }
}
