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
import cesiumlanguagewriter.TimeInterval;
import org.orekit.czml.object.secondary.Clock;
import org.orekit.czml.object.utils.DateUtils;

import java.awt.Color;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract Primary Object class
 * <p>
 * This class aims at giving a common abstract base where all primary objects
 * will refer to.
 *
 * @author LEBLOND Julien.
 * @since 1.0.0
 */
public abstract class AbstractPrimaryObject<T extends CzmlPrimaryObject<T>>
    implements
    CzmlPrimaryObject<T> {

    /** The default interval between steps for the clock. */
    public static final Double DEFAULT_INTERVAL_BETWEEN_STEPS = 60.0;

    /**
     * THe id of the object.
     */
    private String id;

    /**
     * The name of the object.
     */
    private String name;

    /**
     * The availability of the object.
     */
    private TimeInterval availability;

    /**
     * The time intervals (if they are fractionated) of availability.
     */
    private final List<TimeInterval> availabilities = new ArrayList<>();

    // Overrides

    @Override
    public String toString() {
        final StringWriter writer = new StringWriter();
        final CesiumOutputStream output = new CesiumOutputStream(writer);
        final CesiumStreamWriter streamWriter = new CesiumStreamWriter();
        this.writeCzmlBlock(streamWriter, output);
        return writer.toString();
    }

    // Getters
    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public TimeInterval getAvailability() {
        return availability;
    }

    public Clock getClock() {
        return new Clock(DateUtils.toAbsoluteDate(getAvailability().getStart()),
                         DateUtils.toAbsoluteDate(getAvailability().getStop()),
                         DEFAULT_INTERVAL_BETWEEN_STEPS);
    }

    // Setters

    @Override
    public void setId(final String s) {
        this.id = s;
    }

    @Override
    public void setName(final String n) {
        this.name = n;
    }

    @Override
    public void setAvailability(final TimeInterval a) {
        this.availability = a;
    }

    /**
     * Sets availabilities.
     *
     * @param a the a
     */
    public void setAvailabilities(final List<TimeInterval> a) {
        this.availabilities.clear();
        this.availabilities.addAll(a);
    }

    // Public functions

    // Protected methods

    /**
     * Pre made color list java . util . list.
     *
     * @return the java . util . list
     */
    protected java.util.List<Color> preMadeColorList() {
        final List<Color> preMadeColorList = new ArrayList<>();
        final Color red = new Color(255, 0, 0);
        final Color orange = new Color(255, 127, 0);
        final Color yellow = new Color(255, 255, 0);
        final Color lightGreen = new Color(127, 255, 0);
        final Color green = new Color(0, 255, 0);
        final Color lightCyan = new Color(0, 255, 127);
        final Color cyan = new Color(0, 255, 255);
        final Color lightBlue = new Color(0, 127, 255);
        final Color blue = new Color(0, 0, 255);
        final Color violet = new Color(127, 0, 255);
        final Color magenta = new Color(255, 0, 255);
        final Color pink = new Color(255, 0, 127);
        preMadeColorList.add(red);
        preMadeColorList.add(orange);
        preMadeColorList.add(yellow);
        preMadeColorList.add(lightGreen);
        preMadeColorList.add(green);
        preMadeColorList.add(lightCyan);
        preMadeColorList.add(cyan);
        preMadeColorList.add(lightBlue);
        preMadeColorList.add(blue);
        preMadeColorList.add(violet);
        preMadeColorList.add(magenta);
        preMadeColorList.add(pink);
        return preMadeColorList;
    }

    /**
     * Color wheel list.
     *
     * @param numberOfEntities the total of sat
     * @return the list
     */
    protected List<Color> colorWheel(final int numberOfEntities) {
        // Check if the number of entities is bigger than 12 (number of primal
        // colors made with r,g,b) :
        final List<Color> toReturn = new ArrayList<>();
        if (numberOfEntities / 12.0 > 1) {
            final int totalOfColorBySection = numberOfEntities / 6;
            final int rest = numberOfEntities % 6;
            final int shiftOfColor = 255 / totalOfColorBySection;
            // To yellow
            for (int i = 0; i < totalOfColorBySection; i++) {
                final Color currentColor = new Color(255, shiftOfColor * i, 0);
                toReturn.add(currentColor);
            }
            // To green
            for (int i = 0; i < totalOfColorBySection; i++) {
                final Color currentColor =
                    new Color(255 - (shiftOfColor * i), 255, 0);
                toReturn.add(currentColor);
            }
            // To cyan
            for (int i = 0; i < totalOfColorBySection; i++) {
                final Color currentColor = new Color(0, 255, shiftOfColor * i);
                toReturn.add(currentColor);
            }
            // To blue
            for (int i = 0; i < totalOfColorBySection; i++) {
                final Color currentColor =
                    new Color(0, 255 - (shiftOfColor * i), 255);
                toReturn.add(currentColor);
            }
            // To magenta
            for (int i = 0; i < totalOfColorBySection; i++) {
                final Color currentColor = new Color(shiftOfColor * i, 0, 255);
                toReturn.add(currentColor);
            }
            // To red
            for (int i = 0; i < totalOfColorBySection + rest; i++) {
                final int totalColorOfLastSection =
                    totalOfColorBySection + rest;
                final int shiftOfColorLastSection =
                    255 / totalColorOfLastSection;
                final Color currentColor =
                    new Color(255, 0, 255 - (shiftOfColorLastSection * i));
                toReturn.add(currentColor);
            }
        } else {
            final List<Color> preMadeColors = preMadeColorList();
            for (int i = 0; i < numberOfEntities; i++) {
                toReturn.add(preMadeColors.get(i));
            }
        }
        return toReturn;
    }

    /**
     * Aims at building the list of object clone from a specific primary object.
     *
     * @param primaryObjects : The list of objects to clone
     * @param <T> : Primary object to clone
     * @return : The list of objects cloned.
     */
    public static <T extends CzmlPrimaryObject<T>> List<T>
        cloneList(final List<T> primaryObjects) {
        final List<T> listToReturn = new ArrayList<>();
        for (final T currentObject : primaryObjects) {
            listToReturn.add(currentObject);
        }
        return listToReturn;
    }
}
