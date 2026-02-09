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

import cesiumlanguagewriter.CesiumOutputStream;
import cesiumlanguagewriter.CesiumStreamWriter;
import org.orekit.czml.errors.OresiumException;
import org.orekit.czml.errors.OresiumMessages;
import org.orekit.czml.object.primary.AbstractPrimaryObject;
import org.orekit.czml.object.primary.entities.Constellation;
import org.orekit.czml.object.primary.entities.Spacecraft;
import org.orekit.frames.TopocentricFrame;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * <p>
 * Object made to represent multiple lines of visibility for a satellite or a
 * constellation. Avoiding building lines of visibility one by one. This object
 * is also a {@link List<LineOfVisibility>} implementing all methods related to
 * lists.
 * </p>
 *
 * @author Julien Leblond
 * @since 1.1
 */
public class MultipleLineOfVisibility
    extends
    AbstractPrimaryObject<MultipleLineOfVisibility>
    implements
    List<LineOfVisibility> {

    // Arguments

    /** The default id. */
    public static final String DEFAULT_ID = "MULTIPLE_LINES/";

    /** The topocentric frames considered. */
    private List<TopocentricFrame> topocentricFrames;

    /** The list of line of visibility considered. */
    private final List<LineOfVisibility> lines;

    /** The id of the multiple visibility line object. */
    private String id;

    /** The spacecraft if one is used. */
    private Spacecraft spacecraft;

    /** The constellation if one is used. */
    private Constellation constellation;

    // Constructor

    /**
     * The default constructor with a satellite.
     *
     * @param topocentricFrames : The topocentric frames considered
     * @param spacecraft : The spacecraft
     */
    MultipleLineOfVisibility(final List<TopocentricFrame> topocentricFrames,
                             final Spacecraft spacecraft)
        throws URISyntaxException,
            IOException {
        this.topocentricFrames = topocentricFrames;
        this.lines = buildLines(topocentricFrames, spacecraft, null);
        this.id =
            DEFAULT_ID +
                  " " + topocentricFrames.size() + " " + spacecraft.getId();
        this.spacecraft = spacecraft;
    }

    /**
     * The default constructor with a constellation.
     *
     * @param topocentricFrames : The topocentric frame considered
     * @param constellation : The constellation
     */
    MultipleLineOfVisibility(final List<TopocentricFrame> topocentricFrames,
                             final Constellation constellation)
        throws URISyntaxException,
            IOException {
        this.topocentricFrames = topocentricFrames;
        this.lines = buildLines(topocentricFrames, null, constellation);
        this.id =
            DEFAULT_ID +
                  " " + topocentricFrames.size() + " " + constellation.getId();
        this.constellation = constellation;
    }

    // Builders

    /**
     * Builder for the multiple line of visibility object with a spacecraft.
     *
     * @param topocentricFramesInput : The topocentric frames considered
     * @param spacecraftInput : The spacecraft
     * @return The builder for the mutliple line of visibility from a spacecraft
     */
    public static MultipleLineOfVisibilityBuilder
        builder(final List<TopocentricFrame> topocentricFramesInput,
                final Spacecraft spacecraftInput) {
        return new MultipleLineOfVisibilityBuilder(topocentricFramesInput,
                                                   spacecraftInput);
    }

    /**
     * Builder for the multiple line of visibility object with a constellation.
     *
     * @param topocentricFramesInput : The topocentric frames considered
     * @param constellationInput : The constellation
     * @return The builder for the multiple line of visibility from a
     *         constellation
     */
    public static MultipleLineOfVisibilityBuilder
        builder(final List<TopocentricFrame> topocentricFramesInput,
                final Constellation constellationInput) {
        return new MultipleLineOfVisibilityBuilder(topocentricFramesInput,
                                                   constellationInput);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public MultipleLineOfVisibility cloneObject() {
        final MultipleLineOfVisibility toReturn;
        try {
            if (this.spacecraft != null) {
                toReturn =
                    new MultipleLineOfVisibility(this.topocentricFrames,
                                                 this.spacecraft);
            } else if (this.constellation != null) {
                toReturn =
                    new MultipleLineOfVisibility(this.topocentricFrames,
                                                 this.constellation);
            } else {
                throw new OresiumException(OresiumMessages.NOT_VALID_PRIMARY_OBJECT_FOR_CLONE);
            }
        } catch (URISyntaxException | IOException e) {
            throw new OresiumException(OresiumMessages.NOT_VALID_PRIMARY_OBJECT_FOR_CLONE);
        }
        return toReturn;
    }

    /**
     * The function build the list of the line of visibility.
     *
     * @param topocentricFramesInput : The topocentric frame considered.
     * @param spacecraftInput : The spacecraft considered if defined.
     * @param constellationInput : The constellation considered if defined.
     * @return The list of line of visibility
     */
    private List<LineOfVisibility>
        buildLines(final List<TopocentricFrame> topocentricFramesInput,
                   final Spacecraft spacecraftInput,
                   final Constellation constellationInput)
            throws URISyntaxException,
                IOException {
        final List<LineOfVisibility> linesBuilt = new ArrayList<>();
        if (spacecraftInput != null) {
            for (final TopocentricFrame topocentricFrame : topocentricFramesInput) {
                final LineOfVisibility currentTopocentricLine =
                    LineOfVisibility.builder(topocentricFrame, spacecraftInput,
                                             spacecraftInput.getClock())
                        .build();
                linesBuilt.add(currentTopocentricLine);
            }
        } else if (constellationInput != null) {
            for (final TopocentricFrame topocentricFrame : topocentricFramesInput) {
                final LineOfVisibility currentTopocentricLine =
                    LineOfVisibility
                        .builder(topocentricFrame, constellationInput,
                                 constellationInput.getClock())
                        .build();
                linesBuilt.add(currentTopocentricLine);
            }
        } else {
            throw new OresiumException(OresiumMessages.NO_SPACECRAFT_OR_CONSTELLATION);
        }
        return linesBuilt;
    }

    @Override
    public void writeCzmlBlock(final CesiumStreamWriter stream,
                               final CesiumOutputStream output)
        throws URISyntaxException,
            IOException {
        for (final LineOfVisibility line : lines) {
            line.writeCzmlBlock(stream, output);
        }
    }

    @Override
    public int size() {
        return lines.size();
    }

    @Override
    public boolean isEmpty() {
        return lines.isEmpty();
    }

    @Override
    public boolean contains(final Object o) {
        return lines.contains(o);
    }

    @Override
    public Iterator<LineOfVisibility> iterator() {
        return lines.iterator();
    }

    @Override
    public Object[] toArray() {
        return lines.toArray();
    }

    @Override
    public <T> T[] toArray(final T[] a) {
        return lines.toArray(a);
    }

    @Override
    public boolean add(final LineOfVisibility lineOfVisibility) {
        return lines.add(lineOfVisibility);
    }

    @Override
    public boolean remove(final Object o) {
        return lines.remove(o);
    }

    @Override
    public boolean containsAll(final Collection<?> c) {
        return new HashSet<>(lines).containsAll(c);
    }

    @Override
    public boolean addAll(final Collection<? extends LineOfVisibility> c) {
        return lines.addAll(c);
    }

    @Override
    public boolean addAll(final int index,
                          final Collection<? extends LineOfVisibility> c) {
        return lines.addAll(index, c);
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
        return lines.removeAll(c);
    }

    @Override
    public boolean retainAll(final Collection<?> c) {
        return lines.retainAll(c);
    }

    @Override
    public void clear() {
        lines.clear();
    }

    @Override
    public LineOfVisibility get(final int index) {
        return lines.get(index);
    }

    @Override
    public LineOfVisibility set(final int index,
                                final LineOfVisibility element) {
        return lines.set(index, element);
    }

    @Override
    public void add(final int index, final LineOfVisibility element) {
        lines.add(index, element);
    }

    @Override
    public LineOfVisibility remove(final int index) {
        return lines.remove(index);
    }

    @Override
    public int indexOf(final Object o) {
        return lines.indexOf(o);
    }

    @Override
    public int lastIndexOf(final Object o) {
        return lines.lastIndexOf(o);
    }

    @Override
    public ListIterator<LineOfVisibility> listIterator() {
        return lines.listIterator();
    }

    @Override
    public ListIterator<LineOfVisibility> listIterator(final int index) {
        return lines.listIterator(index);
    }

    @Override
    public List<LineOfVisibility> subList(final int fromIndex,
                                          final int toIndex) {
        return lines.subList(fromIndex, toIndex);
    }

    public List<TopocentricFrame> getTopocentricFrames() {
        return topocentricFrames;
    }

    public void
        setTopocentricFrames(final List<TopocentricFrame> topocentricFrames) {
        this.topocentricFrames = topocentricFrames;
    }
}
