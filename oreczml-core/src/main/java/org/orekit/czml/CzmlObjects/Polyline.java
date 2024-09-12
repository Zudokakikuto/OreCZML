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
package org.orekit.czml.CzmlObjects;

import cesiumlanguagewriter.BooleanCesiumWriter;
import cesiumlanguagewriter.Cartesian;
import cesiumlanguagewriter.CesiumArcType;
import cesiumlanguagewriter.CesiumOutputStream;
import cesiumlanguagewriter.PacketCesiumWriter;
import cesiumlanguagewriter.PolylineCesiumWriter;
import cesiumlanguagewriter.PolylineMaterialCesiumWriter;
import cesiumlanguagewriter.PositionListCesiumWriter;
import cesiumlanguagewriter.Reference;
import cesiumlanguagewriter.SolidColorMaterialCesiumWriter;
import cesiumlanguagewriter.TimeInterval;
import org.orekit.czml.ArchiObjects.Builders.NonVectorPolylineBuilder;
import org.orekit.czml.ArchiObjects.Builders.VectorPolylineBuilder;
import org.orekit.czml.ArchiObjects.Exceptions.OreCzmlExceptions;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.Header;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Polyline class
 *
 * <p>
 * Polyline objects are line that can be drawn from one position to another in a given amount of time. They can follow
 * references of objects to update their position in real time during the simulation. They can change color, or can even represents
 * vectors. They are in fact used in the line of visibility of the terrestrial reference system. That is why there are two ways to build
 * a polyline :
 * </p>
 * <p>
 * As a vector.
 * As a non-vector fixed line.
 * </p>
 *
 * @author Julien LEBLOND.
 * @since 1.0.0
 */

public class Polyline {

    /** The default exception error if a vector function is used in a non-vector polyline. */
    public static final String DEFAULT_CANT_CALL = "Can't call a vector function on a non-vector polyline";

    /** The default color of the polyline. */
    public static final Color DEFAULT_COLOR = new Color(0, 255, 255, 255);

    /** The default width of the polyline. */
    public static final double DEFAULT_WIDTH = 1;

    /** The default arc-type of the polyline. */
    public static final CesiumArcType DEFAULT_ARC_TYPE = CesiumArcType.NONE;

    /** The default show of the polyline. */
    public static final boolean DEFAULT_SHOW = true;

    /** The default availability of the polyline. */
    public static final TimeInterval DEFAULT_AVAILABILITY = Header.getMasterClock()
                                                                  .getAvailability();

    /** The default reference of the polyline. */
    public static final Reference DEFAULT_REFERENCE = null;

    /** The default near distance. */
    public static final double DEFAULT_NEAR_DISTANCE = 1;

    /** The default far distance. */
    public static final double DEFAULT_FAR_DISTANCE = 100000000;

    /** The width of the polyline. */
    private final double width;

    /** The color of the polyline. */
    private final Color color;

    /** The availability of the polyline. */
    private TimeInterval availability;

    /** To show or not the polyline. */
    private boolean show;

    /** The arc type of the polyline. (a straight line by default). The available parameters are: NONE, GEODESIC, RHUMB; */
    private CesiumArcType arcType;

    /** If the polyline is a vector or not. */
    private Boolean arrow;

    /** The cartesian position of the first extremity of the polyline (for vector polyline). */
    private Cartesian firstPosition;

    /** The cartesian position of the first extremity of the polyline (for vector polyline). */
    private Cartesian secondPosition;

    /** The reference for the first extremity of the polyline (for non-vector polyline). */
    private Reference firstReference;

    /** The reference for the second extremity of the polyline (for non-vector polyline). */
    private Reference secondReference;

    /** The near distance where the polyline must be displayed. */
    private double nearDistance;

    /** The far distance where the polyline must not be displayed anymore. */
    private double farDistance;


    // Constructors
    // Non-vector constructors

    /** This constructor generates a polyline to be used as a non-vector. It uses default parameters. */
    public Polyline() {
        this(DEFAULT_REFERENCE, DEFAULT_REFERENCE, DEFAULT_AVAILABILITY, DEFAULT_COLOR, DEFAULT_WIDTH, DEFAULT_SHOW,
                DEFAULT_ARC_TYPE, DEFAULT_NEAR_DISTANCE, DEFAULT_FAR_DISTANCE);
    }

    /**
     * This constructor generates a polyline to be used as a non-vector. It uses no default parameters.
     *
     * @param firstReference  : The reference for the first extremity of the polyline.
     * @param secondReference : The reference for the second extremity of the polyline.
     * @param availability    : The time interval when the polyline is displayed.
     * @param color           : The color of the polyline.
     * @param width           : The width of the polyline.
     * @param show            : To show or not the polyline.
     * @param arcType         : The arc-type of the polyline.
     * @param nearDistance    : The near distance where the polyline must be displayed.
     * @param farDistance     : The far distance where the polyline must not be displayed anymore.
     */
    public Polyline(final Reference firstReference, final Reference secondReference, final TimeInterval availability,
                    final Color color, final double width, final boolean show, final CesiumArcType arcType,
                    final double nearDistance, final double farDistance) {
        this.firstReference  = firstReference;
        this.secondReference = secondReference;
        this.availability    = availability;
        this.color           = color;
        this.width           = width;
        this.show            = show;
        this.arrow           = true;
        this.arcType         = arcType;
        this.nearDistance    = nearDistance;
        this.farDistance     = farDistance;
    }

    // Vector constructors

    /**
     * This constructor generates a polyline to be used as a vector.
     *
     * @param cartesians : A list of cartesians with size 2, containing the first and the second position of the polyline
     */
    public Polyline(final List<Cartesian> cartesians) {
        this(cartesians, DEFAULT_AVAILABILITY, DEFAULT_COLOR, DEFAULT_NEAR_DISTANCE, DEFAULT_FAR_DISTANCE);
    }

    /**
     * This constructor generates a polyline to be used as a vector.
     *
     * @param cartesians   : A list of cartesians with size 2, containing the first and the second position of the polyline
     * @param availability : A timeInterval representing the time interval when the polyline must be displayed, usually, each time step
     * @param color        : the color of the polyline
     * @param nearDistance : The nearest distance where the polyline is displayed
     * @param farDistance  : The fairest distance where the polyline is displayed
     */
    public Polyline(final List<Cartesian> cartesians, final TimeInterval availability, final Color color,
                    final double nearDistance, final double farDistance) {
        if (cartesians.size() != 2) {
            throw new RuntimeException(OreCzmlExceptions.MORE_THAN_2_CARTESIAN_POLYLINE);
        } else {
            this.width        = 2;
            this.color        = color;
            this.show         = true;
            this.arcType      = CesiumArcType.NONE;
            this.arrow        = true;
            this.availability = availability;
            firstPosition     = cartesians.get(0);
            secondPosition    = cartesians.get(1);
            this.nearDistance = nearDistance;
            this.farDistance  = farDistance;
        }
    }


    // Builders

    public static NonVectorPolylineBuilder nonVectorBuilder() {
        return new NonVectorPolylineBuilder();
    }

    public static VectorPolylineBuilder vectorBuilder(final List<Cartesian> cartesians) {
        return new VectorPolylineBuilder(cartesians);
    }


    // Getters

    public TimeInterval getAvailability() {
        return availability;
    }

    public void setAvailability(final TimeInterval availability) {
        this.availability = availability;
    }

    public Color getColor() {
        return color;
    }

    public boolean getShow() {
        return show;
    }

    public double getWidth() {
        return width;
    }

    public CesiumArcType getArcType() {
        return arcType;
    }

    public void setArcType(final CesiumArcType arcType) {
        this.arcType = arcType;
    }


    // Setters

    public Boolean getArrow() {
        return arrow;
    }


    // Private functions

    /**
     * This functions aims at writing a polyline built as a vector in the Inertial frame.
     *
     * @param packet : The packet where the polyline is written
     * @param output : The cesium output stream that writes the strings in the file
     */
    public void writePolylineVectorInertial(final PacketCesiumWriter packet, final CesiumOutputStream output) {
        if (arrow) {
            try (PolylineCesiumWriter polylineWriter = packet.getPolylineWriter()) {
                polylineWriter.open(output);
                polylineWriter.writeWidthProperty(width);
                polylineWriter.writeArcTypeProperty(arcType);
                polylineWriter.writeShowProperty(show);
                polylineWriter.writeDistanceDisplayConditionProperty(nearDistance, farDistance);
                try (PolylineMaterialCesiumWriter materialWriter = polylineWriter.getMaterialWriter()) {
                    materialWriter.open(output);
                    output.writeStartObject();
                    try (SolidColorMaterialCesiumWriter solidColorWriter = materialWriter.getSolidColorWriter()) {
                        solidColorWriter.open(output);
                        solidColorWriter.writeColorProperty(color);
                    }
                    output.writeEndObject();
                    writePositionOfVectorInertial(polylineWriter, output);
                }
                try (BooleanCesiumWriter showWriter = polylineWriter.getShowWriter()) {
                    showWriter.open(output);
                    showWriter.writeInterval(availability);
                    showWriter.writeBoolean(true);
                }
            }
        } else {
            throw new RuntimeException(DEFAULT_CANT_CALL);
        }
    }

    /**
     * This functions aims at writing a polyline built as a vector in the Fixed frame.
     *
     * @param packet : The packet where the polyline is written
     * @param output : The cesium output stream that writes the strings in the file
     */
    public void writePolylineVectorFixed(final PacketCesiumWriter packet, final CesiumOutputStream output) {
        if (arrow) {
            try (PolylineCesiumWriter polylineWriter = packet.getPolylineWriter()) {
                polylineWriter.open(output);
                polylineWriter.writeWidthProperty(width);
                polylineWriter.writeArcTypeProperty(arcType);
                polylineWriter.writeShowProperty(show);
                polylineWriter.writeDistanceDisplayConditionProperty(nearDistance, farDistance);
                try (PolylineMaterialCesiumWriter materialWriter = polylineWriter.getMaterialWriter()) {
                    materialWriter.open(output);
                    output.writeStartObject();
                    try (SolidColorMaterialCesiumWriter solidColorWriter = materialWriter.getSolidColorWriter()) {
                        solidColorWriter.open(output);
                        solidColorWriter.writeColorProperty(color);
                    }
                    output.writeEndObject();
                    writePositionOfVectorFixed(polylineWriter, output);
                }
                try (BooleanCesiumWriter showWriter = polylineWriter.getShowWriter()) {
                    showWriter.open(output);
                    showWriter.writeInterval(availability);
                    showWriter.writeBoolean(true);
                }
            }
        } else {
            throw new RuntimeException(DEFAULT_CANT_CALL);
        }
    }

    /**
     * This function aims at writing a polyline built as a non-vector to be displayed as a line of visibility.
     *
     * @param packet     : The packet where the polyline is written
     * @param output     : The cesium output stream that writes the strings in the file
     * @param references : The references (example: object_ID#position the reference of the position of the object) that will be used to position each extremity of the polyline that will be displayed
     * @param showList   : A list of Show objects, these objects will be useful to know when to display or not the polyline in time
     */
    public void writePolylineOfVisibility(final PacketCesiumWriter packet, final CesiumOutputStream output,
                                          final Iterable<Reference> references, final List<CzmlShow> showList) {
        try (PolylineCesiumWriter polylineWriter = packet.getPolylineWriter()) {
            polylineWriter.open(output);
            polylineWriter.writeWidthProperty(this.getWidth());
            polylineWriter.writeArcTypeProperty(CesiumArcType.NONE);
            try (PolylineMaterialCesiumWriter materialWriter = polylineWriter.getMaterialWriter()) {
                materialWriter.open(output);
                output.writeStartObject();
                try (SolidColorMaterialCesiumWriter solidColorWriter = materialWriter.getSolidColorWriter()) {
                    solidColorWriter.open(output);
                    solidColorWriter.writeColorProperty(this.getColor());
                }
                output.writeEndObject();
                writePositionOfVisibility(polylineWriter, this, output, references);
                writeShowOfVisibility(polylineWriter, output, showList);
                output.writeEndSequence();
            }
        }
    }

    /**
     * This function aims at writing a polyline defined with references.
     *
     * @param packet : The packet where the polyline is written.
     * @param output : The cesium output stream that writes in the czml file.
     */
    public void writeReferencesPolyline(final PacketCesiumWriter packet, final CesiumOutputStream output) {
        try (PolylineCesiumWriter polylineWriter = packet.getPolylineWriter()) {
            polylineWriter.open(output);
            polylineWriter.writeWidthProperty(this.getWidth());
            polylineWriter.writeArcTypeProperty(CesiumArcType.NONE);
            try (PolylineMaterialCesiumWriter materialWriter = polylineWriter.getMaterialWriter()) {
                materialWriter.open(output);
                output.writeStartObject();
                try (SolidColorMaterialCesiumWriter solidColorWriter = materialWriter.getSolidColorWriter()) {
                    solidColorWriter.open(output);
                    solidColorWriter.writeColorProperty(this.getColor());
                }
                output.writeEndObject();
                writeReferences(polylineWriter, output);
            }
        }
    }

    /**
     * This function aims at writing the position of the polyline built as a non-vector to display an attitude in the INERTIAL frame.
     *
     * @param polylineWriter : the writer extracted from a packet cesium writer to write parameters of the polyline
     * @param output         : The cesium output stream that writes the strings in the file
     */
    private void writePositionOfVectorInertial(final PolylineCesiumWriter polylineWriter,
                                               final CesiumOutputStream output) {
        try (PositionListCesiumWriter positionWriter = polylineWriter.getPositionsWriter()) {
            positionWriter.open(output);
            final List<Cartesian> tempCartesians = new ArrayList<>();
            tempCartesians.add(firstPosition);
            tempCartesians.add(secondPosition);
            positionWriter.writeCartesian(tempCartesians);
            positionWriter.writeReferenceFrame("INERTIAL");
        }
    }

    /**
     * This function aims at writing the position of the polyline built as a non-vector to display an attitude in the FIXED frame.
     *
     * @param polylineWriter : the writer extracted from a packet cesium writer to write parameters of the polyline
     * @param output         : The cesium output stream that writes the strings in the file
     */
    private void writePositionOfVectorFixed(final PolylineCesiumWriter polylineWriter,
                                            final CesiumOutputStream output) {
        try (PositionListCesiumWriter positionWriter = polylineWriter.getPositionsWriter()) {
            positionWriter.open(output);
            positionWriter.writeReferenceFrame("FIXED");
            final List<Cartesian> tempCartesians = new ArrayList<>();
            tempCartesians.add(firstPosition);
            tempCartesians.add(secondPosition);
            positionWriter.writeCartesian(tempCartesians);
        }
    }

    /**
     * This function aims at writing the position of the polyline built as a non-vector to display a line of visibility.
     *
     * @param polylineWriter : the writer extracted from a packet cesium writer to write parameters of the polyline
     * @param polylineInput  : the polyline object inputted
     * @param output         : The cesium output stream that writes the strings in the file
     * @param references     : The references (example: object_ID#position the reference of the position of the object) that will be used to position each extremity of the polyline that will be displayed
     */
    private void writePositionOfVisibility(final PolylineCesiumWriter polylineWriter, final Polyline polylineInput,
                                           final CesiumOutputStream output, final Iterable<Reference> references) {
        polylineWriter.writeArcTypeProperty(polylineInput.getArcType());

        try (PositionListCesiumWriter positionWriter = polylineWriter.getPositionsWriter()) {
            positionWriter.open(output);
            positionWriter.writeReferences(references);
        }
    }

    /**
     * This functions aims at writing the list of Show objects of a polyline built as a non-vector to display a line of visibility.
     *
     * @param polylineWriter : the writer extracted from a packet cesium writer to write parameters of the polyline
     * @param output         : The cesium output stream that write the strings in the file
     * @param showList       : A list of Show objects, these objects will be useful to know when to display or not the polyline in time
     */
    private void writeShowOfVisibility(final PolylineCesiumWriter polylineWriter, final CesiumOutputStream output,
                                       final List<CzmlShow> showList) {
        try (BooleanCesiumWriter showWriter = polylineWriter.getShowWriter()) {
            showWriter.open(output);
            output.writeStartSequence();
            if (showList.size() == 1) {
                final CzmlShow showTemp = showList.get(0);
                showWriter.writeInterval(showTemp.getAvailability());
                showWriter.writeBoolean(showTemp.getShow());
            } else {
                for (int i = 0; i < showList.size(); i++) {
                    final CzmlShow showTemp = showList.get(i);
                    if (i == 0) {
                        showWriter.writeInterval(showTemp.getAvailability());
                        showWriter.writeBoolean(showTemp.getShow());
                        output.writeEndObject();
                    } else if (i != showList.size() - 1) {
                        output.writeStartObject();
                        showWriter.writeInterval(showTemp.getAvailability());
                        showWriter.writeBoolean(showTemp.getShow());
                        output.writeEndObject();
                    } else {
                        output.writeStartObject();
                        showWriter.writeInterval(showTemp.getAvailability());
                        showWriter.writeBoolean(showTemp.getShow());
                    }
                }
            }
        }
    }

    /**
     * This function takes the first and second reference defined in the constructor and build a polyline with them.
     *
     * @param polylineCesiumWriter : The writer that can write inside a polyline.
     * @param output               : The cesium output stream that write inside the czml file.
     */
    private void writeReferences(final PolylineCesiumWriter polylineCesiumWriter, final CesiumOutputStream output) {
        if (firstReference == null || secondReference == null) {
            throw new RuntimeException(OreCzmlExceptions.NOT_REFERENCES_POLYLINE);
        } else {
            try (PositionListCesiumWriter positionWriter = polylineCesiumWriter.getPositionsWriter()) {
                positionWriter.open(output);
                final Reference[] references = Arrays.asList(firstReference, secondReference)
                                                     .toArray(new Reference[0]);
                final Iterable<Reference> listOfReferences = convertToIterable(references);
                positionWriter.writeReferences(listOfReferences);
            }
        }
    }

    /**
     * This function aims at converting an array of references into an iterable object.
     *
     * @param array : An array of references.
     * @return : An iterable object of references.
     */
    private static Iterable<Reference> convertToIterable(final Reference[] array) {
        return () -> Arrays.stream(array)
                           .iterator();
    }
}
