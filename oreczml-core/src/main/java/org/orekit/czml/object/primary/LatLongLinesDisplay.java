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

import cesiumlanguagewriter.Cartographic;
import cesiumlanguagewriter.LabelCesiumWriter;
import cesiumlanguagewriter.PacketCesiumWriter;
import cesiumlanguagewriter.PolylineCesiumWriter;
import cesiumlanguagewriter.PolylineMaterialCesiumWriter;
import cesiumlanguagewriter.PositionCesiumWriter;
import cesiumlanguagewriter.SolidColorMaterialCesiumWriter;
import cesiumlanguagewriter.TimeInterval;
import org.orekit.czml.errors.OreCzmlException;
import org.orekit.czml.errors.OreCzmlMessages;
import org.orekit.czml.object.secondary.Label;
import org.orekit.czml.object.Polyline;

import java.awt.Color;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * Latitude longitude display class
 *
 * <p> Allows the user to display the longitude and the latitudes lines of the body considered.
 * Labels representing the degree of the line can be displayed for clarification. </p>
 *
 * @author Julien LEBLOND
 * @since 1.0.0
 */
public class LatLongLinesDisplay extends AbstractPrimaryObject implements CzmlPrimaryObject {

    /** The default id of the lat long display object. */
    public static final String DEFAULT_ID = "LAT_LONG_DISP";

    /** The default name of the latitude longitude display object. */
    public static final String DEFAULT_NAME = "Longitudes and latitudes lines display.";

    /** The default availability of the latitude longitude display object. */
    public static final TimeInterval DEFAULT_AVAILABILITY = Header.getMasterClock()
                                                                  .getAvailability();

    /** The default angular step for longitude and latitude. */
    public static final int DEFAULT_ANGULAR_STEP = 15;

    /** The body around which to apply the object. */
    private List<Polyline> polylines;

    /** The number of lines of latitude. */
    private int numberOfLatitudeLines;

    /** The number of lines of longitude. */
    private int numberOfLongitudeLines;

    /** The list that contains all the cartographic coordinates for the latitude lines. */
    private List<List<Cartographic>> cartographicsLatitudeByLine = new ArrayList<>();

    /** The list that contains all the cartographic coordinates for the longitude lines. */
    private List<List<Cartographic>> cartographicsLongitudeByLine = new ArrayList<>();

    /** To display or not the labels. */
    private boolean displayLabels;


    // Constructors

    /** The default constructor using the default angular step while not displaying the labels. */
    public LatLongLinesDisplay() {
        this(DEFAULT_ANGULAR_STEP, DEFAULT_ANGULAR_STEP, false);
    }

    /**
     * Constructor using an angular step for the latitude and the longitude.
     *
     * @param latitudeAngularStep  : The angular step between each line of latitude.
     * @param longitudeAngularStep : The angular step between each line of longitude.
     * @param displayLabelsInput   : To display the labels of the lines or not (° of the parallels or of the meridians)
     */
    public LatLongLinesDisplay(final int latitudeAngularStep, final int longitudeAngularStep,
                               final boolean displayLabelsInput) {

        this.setId(DEFAULT_ID);
        this.setName(DEFAULT_NAME);
        this.setAvailability(DEFAULT_AVAILABILITY);

        this.displayLabels = displayLabelsInput;
        final List<Integer> divisorsLatitude  = findAllDivisors(360);
        final List<Integer> divisorsLongitude = findAllDivisors(360);

        if (latitudeAngularStep > 180) {
            throw new OreCzmlException(OreCzmlMessages.GREATER_ANGULAR_LATITUDE_STEP);
        }
        if (longitudeAngularStep > 360) {
            throw new OreCzmlException(OreCzmlMessages.GREATER_ANGULAR_LONGITUDE_STEP);
        }

        int divisorLatitudeToUse  = latitudeAngularStep;
        int divisorLongitudeToUse = longitudeAngularStep;

        if (!(divisorsLatitude.contains(latitudeAngularStep))) {
            divisorLatitudeToUse = findNearestLowerDivisor(360, latitudeAngularStep);
        }
        if (!divisorsLongitude.contains(longitudeAngularStep)) {
            divisorLongitudeToUse = findNearestLowerDivisor(360, longitudeAngularStep);
        }

        this.numberOfLatitudeLines  = 360 / divisorLatitudeToUse;
        this.numberOfLongitudeLines = 360 / divisorLongitudeToUse;

        for (int i = 0; i < numberOfLatitudeLines + 1; i++) {
            final List<Cartographic> currentCartographics = new ArrayList<>();
            for (int j = 0; j < 101; j++) {
                currentCartographics.add(new Cartographic(i * divisorLatitudeToUse, (360.0 / 100) * j, 0));
            }
            cartographicsLatitudeByLine.add(currentCartographics);
        }

        for (int i = 0; i < numberOfLongitudeLines + 1; i++) {
            final List<Cartographic> currentCartographics = new ArrayList<>();
            for (int j = 0; j < 101; j++) {
                currentCartographics.add(new Cartographic((360.0 / 100) * j, i * divisorLongitudeToUse, 0));
            }
            cartographicsLongitudeByLine.add(currentCartographics);
        }
    }


    // Overrides

    @Override
    public void writeCzmlBlock() throws URISyntaxException, IOException {
        writeLatitudeAndLongitude(cartographicsLatitudeByLine, numberOfLatitudeLines, true);

        writeLatitudeAndLongitude(cartographicsLongitudeByLine, numberOfLongitudeLines, false);
    }

    @Override
    public StringWriter getStringWriter() {
        return STRING_WRITER;
    }

    @Override
    public void cleanObject() {

    }


    // Getters

    public List<Polyline> getPolylines() {
        return new ArrayList<>(polylines);
    }

    public int getNumberOfLatitudeLines() {
        return numberOfLatitudeLines;
    }

    public int getNumberOfLongitudeLines() {
        return numberOfLongitudeLines;
    }

    public List<List<Cartographic>> getCartographicsLatitudeByLine() {
        return new ArrayList<>(cartographicsLatitudeByLine);
    }

    public List<List<Cartographic>> getCartographicsLongitudeByLine() {
        return new ArrayList<>(cartographicsLongitudeByLine);
    }

    public boolean isDisplayLabels() {
        return displayLabels;
    }


    // Private functions

    /**
     * This function writes the latitude and the longitude lines.
     *
     * @param latOrLongList : A list of list of cartographic points, this object is either representing the latitude lines or the longitude lines.
     * @param numberOfLines : The number of lines to display.
     * @param isLongitude   : A boolean to describe if the latOrLongList is either a latitude list or a longitude list of cartographic objects.
     */
    private void writeLatitudeAndLongitude(final List<List<Cartographic>> latOrLongList, final int numberOfLines,
                                           final boolean isLongitude) {
        for (int i = 0; i < numberOfLines; i++) {
            final List<Cartographic> cartographicGivenLine = latOrLongList.get(i);
            OUTPUT.setPrettyFormatting(true);
            try (PacketCesiumWriter packet = STREAM.openPacket(OUTPUT)) {
                packet.writeId(getId() + cartographicGivenLine.subList(0, 5));
                packet.writeName(getName());
                packet.writeAvailability(getAvailability());

                writePosition(packet, cartographicGivenLine.get(0));

                if (displayLabels) {
                    writeLabel(packet, cartographicGivenLine, isLongitude);
                }

                try (PolylineCesiumWriter polylineWriter = packet.getPolylineWriter()) {
                    polylineWriter.open(OUTPUT);
                    polylineWriter.writePositionsPropertyCartographicDegrees(cartographicGivenLine);

                    writeRedColorEquatorZeroLongitude(cartographicGivenLine, polylineWriter);
                }
            }
        }
    }

    /**
     * This function writes a red line at the equator and a red line at the 0° longitude line.
     *
     * @param cartographicGivenLine : A list of cartographic objects representing one line.
     * @param polylineWriter        : The writer to write a polyline
     */
    private void writeRedColorEquatorZeroLongitude(final List<Cartographic> cartographicGivenLine,
                                                   final PolylineCesiumWriter polylineWriter) {
        for (final Cartographic currentCartographic : cartographicGivenLine) {
            if (Objects.equals(currentCartographic, new Cartographic(0, 0, 0))) {
                try (PolylineMaterialCesiumWriter materialWriter = polylineWriter.getMaterialWriter()) {
                    materialWriter.open(CzmlPrimaryObject.OUTPUT);
                    CzmlPrimaryObject.OUTPUT.writeStartObject();
                    try (SolidColorMaterialCesiumWriter colorWriter = materialWriter.getSolidColorWriter()) {
                        colorWriter.open(CzmlPrimaryObject.OUTPUT);
                        colorWriter.writeColorProperty(new Color(255, 0, 0));
                    }
                    CzmlPrimaryObject.OUTPUT.writeEndObject();
                }
            }
        }
    }

    /**
     * This function writes a label for a given list of cartographic objects (representing a line).
     *
     * @param packet                : The packet that will write in the czml file.
     * @param cartographicGivenLine : The list of cartographic representing the line which label should represent.
     * @param isLatitude            : If the line represented is a line of latitude or not.
     */
    private void writeLabel(final PacketCesiumWriter packet, final List<Cartographic> cartographicGivenLine,
                            final boolean isLatitude) {

        final String labelString;
        if (isLatitude) {
            labelString = cartographicGivenLine.get(0)
                                               .getLongitude() + "° long";
        } else {
            labelString = cartographicGivenLine.get(0)
                                               .getLatitude() + "° lat";
        }

        try (LabelCesiumWriter labelWriter = packet.getLabelWriter()) {
            labelWriter.open(OUTPUT);
            final Label label = new Label(labelString);
            labelWriter.writeFillColorProperty(new Color(102, 255, 0));
            labelWriter.writeFontProperty("11pt Lucida Console");
            labelWriter.writeHorizontalOriginProperty(label.getHorizontalOrigin());
            labelWriter.writeVerticalOriginProperty(label.getVerticalOrigin());
            labelWriter.writeTextProperty(labelString);
            labelWriter.writeShowProperty(label.getShow());
        }
    }

    /**
     * This function writes the position of the packet to give the label a position to refer to.
     *
     * @param packet              : The packet that will write in the czml file.
     * @param currentCartographic : The cartographic used to refer the label at this position.
     */
    private void writePosition(final PacketCesiumWriter packet, final Cartographic currentCartographic) {

        try (PositionCesiumWriter positionWriter = packet.getPositionWriter()) {
            positionWriter.open(OUTPUT);
            positionWriter.writeInterval(this.getAvailability());
            positionWriter.writeCartographicDegrees(currentCartographic);
        }
    }

    /**
     * This function finds the nearest lower divisor of a given number. This allows dividing the number with a divisor
     * not too far from the number inputted that is not one.
     *
     * @param number      : The number which divisor need to be found.
     * @param notADivisor : The reference number used to find the nearest lower divisor.
     * @return : The nearest divisor of number lower than notADivisor.
     */
    private int findNearestLowerDivisor(final int number, final int notADivisor) {
        final List<Integer> divisors = findAllDivisors(number);
        int                 toReturn = 0;

        for (int i = 0; i < divisors.size() - 1; i++) {
            if (divisors.get(i) < notADivisor && divisors.get(i + 1) > notADivisor) {
                toReturn = divisors.get(i);
                break;
            }
        }
        if (toReturn == 0) {
            toReturn = divisors.get(divisors.size() - 1);
        }
        return toReturn;
    }

    /**
     * This function finds all the divisors of a given number.
     *
     * @param number : The number which divisors need to be found.
     * @return : A list of divisors of number
     */
    private List<Integer> findAllDivisors(final int number) {
        final List<Integer> divisors = new ArrayList<>();
        for (int i = 1; i < number; i++) {
            final double divide     = (double) number / i;
            final int    divideCrop = number / i;
            if (divide == divideCrop) {
                divisors.add(i);
            }
        }
        return divisors;
    }
}
