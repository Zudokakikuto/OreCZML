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

import cesiumlanguagewriter.TimeInterval;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract Primary Object class
 *
 * <p>
 * This class aims at giving a common abstract base where all primary objects will refer to.
 * </p>
 *
 * @author Julien LEBLOND.
 * @since 1.0.0
 */

public abstract class AbstractPrimaryObject implements CzmlPrimaryObject {

    /** THe id of the object. */
    private String id;

    /** The name of the object. */
    private String name;

    /** The availability of the object. */
    private TimeInterval availability;

    /** The time intervals (if they are fractionated) of availability. */
    private List<TimeInterval> availabilities;


    // Getters

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public TimeInterval getAvailability() {
        return availability;
    }

    public List<TimeInterval> getAvailabilities() {
        return new ArrayList<>(availabilities);
    }

    protected void setId(final String s) {
        this.id = s;
    }

    // Setters

    protected void setName(final String n) {
        this.name = n;
    }

    protected void setAvailability(final TimeInterval a) {
        this.availability = a;
    }

    protected void setAvailabilities(final List<TimeInterval> a) {
        this.availabilities = a;
    }


    // Protected methods

    protected java.util.List<Color> preMadeColorList() {
        final List<Color> preMadeColorList = new ArrayList<>();
        final Color       red              = new Color(255, 0, 0);
        final Color       orange           = new Color(255, 127, 0);
        final Color       yellow           = new Color(255, 255, 0);
        final Color       light_green      = new Color(127, 255, 0);
        final Color       green            = new Color(0, 255, 0);
        final Color       light_cyan       = new Color(0, 255, 127);
        final Color       cyan             = new Color(0, 255, 255);
        final Color       light_blue       = new Color(0, 127, 255);
        final Color       blue             = new Color(0, 0, 255);
        final Color       violet           = new Color(127, 0, 255);
        final Color       magenta          = new Color(255, 0, 255);
        final Color       pink             = new Color(255, 0, 127);
        preMadeColorList.add(red);
        preMadeColorList.add(orange);
        preMadeColorList.add(yellow);
        preMadeColorList.add(light_green);
        preMadeColorList.add(green);
        preMadeColorList.add(light_cyan);
        preMadeColorList.add(cyan);
        preMadeColorList.add(light_blue);
        preMadeColorList.add(blue);
        preMadeColorList.add(violet);
        preMadeColorList.add(magenta);
        preMadeColorList.add(pink);
        return preMadeColorList;
    }

    protected List<Color> colorWheel(final int totalOfSat) {
        // Check if the number of sat is bigger than 12 (number of primal colors made with r,g,b) :
        final List<Color> toReturn = new ArrayList<>();
        if (totalOfSat / 12.0 > 1) {
            final int totalOfColorBySection = totalOfSat / 6;
            final int rest                  = totalOfSat % 6;
            final int shiftOfColor          = 255 / totalOfColorBySection;
            // To yellow
            for (int i = 0; i < totalOfColorBySection; i++) {
                final Color currentColor = new Color(255, shiftOfColor * i, 0);
                toReturn.add(currentColor);
            }
            // To green
            for (int i = 0; i < totalOfColorBySection; i++) {
                final Color currentColor = new Color(255 - (shiftOfColor * i), 255, 0);
                toReturn.add(currentColor);
            }
            // To cyan
            for (int i = 0; i < totalOfColorBySection; i++) {
                final Color currentColor = new Color(0, 255, shiftOfColor * i);
                toReturn.add(currentColor);
            }
            // To blue
            for (int i = 0; i < totalOfColorBySection; i++) {
                final Color currentColor = new Color(0, 255 - (shiftOfColor * i), 255);
                toReturn.add(currentColor);
            }
            // To magenta
            for (int i = 0; i < totalOfColorBySection; i++) {
                final Color currentColor = new Color(shiftOfColor * i, 0, 255);
                toReturn.add(currentColor);
            }
            // To red
            for (int i = 0; i < totalOfColorBySection + rest; i++) {
                final int   totalColorOfLastSection = totalOfColorBySection + rest;
                final int   shiftOfColorLastSection = 255 / totalColorOfLastSection;
                final Color currentColor            = new Color(255, 0, 255 - (shiftOfColorLastSection * i));
                toReturn.add(currentColor);
            }
        } else {
            final List<Color> preMadeColors = preMadeColorList();
            for (int i = 0; i < totalOfSat; i++) {
                toReturn.add(preMadeColors.get(i));
            }
        }
        return toReturn;
    }
}
