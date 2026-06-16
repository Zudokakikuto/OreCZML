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

package org.orekit.czml.object.primary.systems;

import cesiumlanguagewriter.Cartographic;
import cesiumlanguagewriter.CesiumOutputStream;
import cesiumlanguagewriter.CesiumStreamWriter;
import cesiumlanguagewriter.LabelCesiumWriter;
import cesiumlanguagewriter.PacketCesiumWriter;
import cesiumlanguagewriter.PolylineCesiumWriter;
import cesiumlanguagewriter.PolylineMaterialCesiumWriter;
import cesiumlanguagewriter.PositionCesiumWriter;
import cesiumlanguagewriter.SolidColorMaterialCesiumWriter;
import org.orekit.czml.errors.OresiumException;
import org.orekit.czml.errors.OresiumMessages;
import org.orekit.czml.object.primary.AbstractPrimaryObject;
import org.orekit.czml.object.secondary.Clock;
import org.orekit.czml.object.secondary.Label;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Latitude longitude display class
 * <p>
 * Allows the user to display the longitude and the latitudes lines of the body
 * considered. Labels representing the degree of the line can be displayed for
 * clarification.
 *
 * @author LEBLOND Julien
 * @since 1.0.0
 */
public class LatLongLines
    extends
    AbstractPrimaryObject<LatLongLines> {

    /**
     * The default id of the lat long display object.
     */
    public static final String DEFAULT_ID = "LAT_LONG";

    /**
     * The default name of the latitude longitude display object.
     */
    public static final String DEFAULT_NAME = "Longitudes and latitudes lines.";

    /**
     * The default angular step for longitude and latitude.
     */
    public static final int DEFAULT_ANGULAR_STEP = 15;

    /**
     * The number of lines of latitude.
     */
    private final int numberOfLatitudeLines;

    /**
     * The number of lines of longitude.
     */
    private final int numberOfLongitudeLines;

    /**
     * The list that contains all the cartographic coordinates for the latitude
     * lines.
     */
    private final List<List<Cartographic>> cartographicLatitudeByLine =
        new ArrayList<>();

    /**
     * The list that contains all the cartographic coordinates for the longitude
     * lines.
     */
    private final List<List<Cartographic>> cartographicLongitudeByLine =
        new ArrayList<>();

    /**
     * To display or not the labels.
     */
    private final boolean displayLabels;

    /** The latitude angular step. */
    private int latitudeAngularStep;

    /** The longitude angular step. */
    private int longitudeAngularStep;

    /** The clock of the lat long lines. */
    private Clock clock;

    // Constructors

    /**
     * The default constructor using the default angular step while not
     * displaying the labels.
     *
     * @param clock : The clock considered.
     */
    LatLongLines(final Clock clock) {
        this(DEFAULT_ANGULAR_STEP, DEFAULT_ANGULAR_STEP, false, DEFAULT_ID,
             clock);
    }

    /**
     * Constructor using an angular step for the latitude and the longitude.
     *
     * @param latitudeAngularStepInput : The angular step between each line of
     *        latitude.
     * @param longitudeAngularStepInput : The angular step between each line of
     *        longitude.
     * @param displayLabelsInput : To display the labels of the lines or not (°
     *        of the parallels or of the meridians)
     * @param customID : The custom ID of the lat long lines object.
     * @param clock : The availability considered when several are used.
     */
    LatLongLines(final int latitudeAngularStepInput,
                 final int longitudeAngularStepInput,
                 final boolean displayLabelsInput, final String customID,
                 final Clock clock) {

        this.setId(customID);
        this.setName(DEFAULT_NAME);
        this.setAvailability(clock.getAvailability());
        this.clock = clock;
        this.longitudeAngularStep = longitudeAngularStepInput;
        this.latitudeAngularStep = latitudeAngularStepInput;

        this.displayLabels = displayLabelsInput;
        final List<Integer> divisorsLatitude = findAllDivisors(360);
        final List<Integer> divisorsLongitude = findAllDivisors(360);

        if (latitudeAngularStepInput > 180) {
            throw new OresiumException(OresiumMessages.GREATER_ANGULAR_LATITUDE_STEP);
        }
        if (longitudeAngularStepInput > 360) {
            throw new OresiumException(OresiumMessages.GREATER_ANGULAR_LONGITUDE_STEP);
        }

        int divisorLatitudeToUse = latitudeAngularStepInput;
        int divisorLongitudeToUse = longitudeAngularStepInput;

        if (!(divisorsLatitude.contains(latitudeAngularStepInput))) {
            divisorLatitudeToUse =
                findNearestLowerDivisor(360, latitudeAngularStepInput);
        }
        if (!divisorsLongitude.contains(longitudeAngularStepInput)) {
            divisorLongitudeToUse =
                findNearestLowerDivisor(360, longitudeAngularStepInput);
        }

        this.numberOfLatitudeLines = 360 / divisorLatitudeToUse;
        this.numberOfLongitudeLines = 360 / divisorLongitudeToUse;

        for (int i = 0; i < numberOfLatitudeLines + 1; i++) {
            final List<Cartographic> currentCartographic = new ArrayList<>();
            for (int j = 0; j < 101; j++) {
                currentCartographic
                    .add(new Cartographic(i * divisorLatitudeToUse,
                                          (360.0 / 100) * j, 0));
            }
            cartographicLatitudeByLine.add(currentCartographic);
        }

        for (int i = 0; i < numberOfLongitudeLines + 1; i++) {
            final List<Cartographic> currentCartographic = new ArrayList<>();
            for (int j = 0; j < 101; j++) {
                currentCartographic
                    .add(new Cartographic((360.0 / 100) * j,
                                          i * divisorLongitudeToUse, 0));
            }
            cartographicLongitudeByLine.add(currentCartographic);
        }
    }

    // Builder

    /**
     * Builder lat long lines builder.
     *
     * @param clock the clock
     * @return the lat long lines builder
     */
    public static LatLongLinesBuilder builder(final Clock clock) {
        return new LatLongLinesBuilder(clock);
    }

    // Overrides

    @Override
    public void writeCzmlBlock(final CesiumStreamWriter stream,
                               final CesiumOutputStream output) {
        writeLatitudeAndLongitude(cartographicLatitudeByLine,
                                  numberOfLatitudeLines, true, output, stream);

        writeLatitudeAndLongitude(cartographicLongitudeByLine,
                                  numberOfLongitudeLines, false, output,
                                  stream);
    }

    @Override
    public LatLongLines cloneObject() {
        final LatLongLines copy =
            LatLongLines.builder(this.clock).withCustomID(getId())
                .withDisplay(this.displayLabels)
                .withLatitudeAngularStep(this.latitudeAngularStep)
                .withLongitudeAngularStep(longitudeAngularStep).build();
        copy.setName(getName());
        return copy;
    }

    // Private functions

    /**
     * This function writes the latitude and the longitude lines.
     *
     * @param latOrLongList : A list of list of cartographic points, this object
     *        is either representing the latitude lines or the longitude lines.
     * @param numberOfLines : The number of lines to display.
     * @param isLongitude : A boolean to describe if the latOrLongList is either
     *        a latitude list or a longitude list of cartographic objects.
     * @param output : The output stream of cesium that will contain the strings
     *        to write into the CzmLFile.
     * @param stream : The stream that converts all the strings into
     *        understandable string for the CzmlFile.
     */
    private void
        writeLatitudeAndLongitude(final List<List<Cartographic>> latOrLongList,
                                  final int numberOfLines,
                                  final boolean isLongitude,
                                  final CesiumOutputStream output,
                                  final CesiumStreamWriter stream) {
        for (int i = 0; i < numberOfLines; i++) {
            final List<Cartographic> cartographicGivenLine =
                latOrLongList.get(i);
            output.setPrettyFormatting(true);
            try (PacketCesiumWriter packet = stream.openPacket(output)) {
                final String toWrite =
                    cartographicGivenLine.subList(0, 5).toString();
                packet.writeId(getId() + toWrite);
                packet.writeName(getName());
                packet.writeAvailability(getAvailability());

                writePosition(packet, cartographicGivenLine.get(0), output);

                if (displayLabels) {
                    writeLabel(packet, cartographicGivenLine, isLongitude,
                               output);
                }

                try (PolylineCesiumWriter polylineWriter =
                    packet.getPolylineWriter()) {
                    polylineWriter.open(output);
                    polylineWriter
                        .writePositionsPropertyCartographicDegrees(cartographicGivenLine);

                    writeRedColorEquatorZeroLongitude(cartographicGivenLine,
                                                      polylineWriter, output);
                }
            }
        }
    }

    /**
     * This function writes a red line at the equator and a red line at the 0°
     * longitude line.
     *
     * @param cartographicGivenLine : A list of cartographic objects
     *        representing one line.
     * @param polylineWriter : The writer to write a polyline
     * @param output : The output stream of cesium that will contain the strings
     *        to write into the CzmLFile.
     */
    private void
        writeRedColorEquatorZeroLongitude(final List<Cartographic> cartographicGivenLine,
                                          final PolylineCesiumWriter polylineWriter,
                                          final CesiumOutputStream output) {
        for (final Cartographic currentCartographic : cartographicGivenLine) {
            if (Objects.equals(currentCartographic,
                               new Cartographic(0, 0, 0))) {
                try (PolylineMaterialCesiumWriter materialWriter =
                    polylineWriter.getMaterialWriter()) {
                    materialWriter.open(output);
                    output.writeStartObject();
                    try (SolidColorMaterialCesiumWriter colorWriter =
                        materialWriter.getSolidColorWriter()) {
                        colorWriter.open(output);
                        colorWriter.writeColorProperty(new Color(255, 0, 0));
                    }
                    output.writeEndObject();
                }
            }
        }
    }

    /**
     * This function writes a label for a given list of cartographic objects
     * (representing a line).
     *
     * @param packet : The packet that will write in the czml file.
     * @param cartographicGivenLine : The list of cartographic representing the
     *        line which label should represent.
     * @param isLatitude : If the line represented is a line of latitude or not.
     * @param output : The output stream of cesium that will contain the strings
     *        to write into the CzmLFile.
     */
    private void writeLabel(final PacketCesiumWriter packet,
                            final List<Cartographic> cartographicGivenLine,
                            final boolean isLatitude,
                            final CesiumOutputStream output) {

        final String labelString;
        if (isLatitude) {
            labelString =
                cartographicGivenLine.get(0).getLongitude() + "° long";
        } else {
            labelString = cartographicGivenLine.get(0).getLatitude() + "° lat";
        }

        try (LabelCesiumWriter labelWriter = packet.getLabelWriter()) {
            labelWriter.open(output);
            final Label label = new Label(labelString);
            labelWriter.writeFillColorProperty(new Color(102, 255, 0));
            labelWriter.writeFontProperty("11pt Lucida Console");
            labelWriter
                .writeHorizontalOriginProperty(label.getHorizontalOrigin());
            labelWriter.writeVerticalOriginProperty(label.getVerticalOrigin());
            labelWriter.writeTextProperty(labelString);
            labelWriter.writeShowProperty(label.getShow());
        }
    }

    /**
     * This function writes the position of the packet to give the label a
     * position to refer to.
     *
     * @param packet : The packet that will write in the czml file.
     * @param currentCartographic : The cartographic used to refer to the label
     *        at this position.
     * @param output : The output stream of cesium that will contain the strings
     *        to write into the CzmLFile.
     */
    private void writePosition(final PacketCesiumWriter packet,
                               final Cartographic currentCartographic,
                               final CesiumOutputStream output) {

        try (PositionCesiumWriter positionWriter = packet.getPositionWriter()) {
            positionWriter.open(output);
            positionWriter.writeInterval(this.getAvailability());
            positionWriter.writeCartographicDegrees(currentCartographic);
        }
    }

    /**
     * This function finds the nearest lower divisor of a given number. This
     * allows dividing the number with a divisor not too far from the number
     * inputted that is not one.
     *
     * @param number : The number which divisor need to be found.
     * @param notADivisor : The reference number used to find the nearest lower
     *        divisor.
     * @return : The nearest divisor of number lower than notADivisor.
     */
    private int findNearestLowerDivisor(final int number,
                                        final int notADivisor) {
        final List<Integer> divisors = findAllDivisors(number);
        int toReturn = 0;

        for (int i = 0; i < divisors.size() - 1; i++) {
            if (divisors.get(i) < notADivisor &&
                divisors.get(i + 1) > notADivisor) {
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
            final double divide = (double) number / i;
            final int divideCrop = number / i;
            if (divide == divideCrop) {
                divisors.add(i);
            }
        }
        return divisors;
    }
}
