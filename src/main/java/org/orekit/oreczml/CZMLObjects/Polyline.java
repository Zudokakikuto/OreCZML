/** .*/

package org.example.CZMLObjects;

import cesiumlanguagewriter.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Polyline {

    /** .*/
    private TimeInterval availability = null;
    /** .*/
    private boolean show = false;
    /** .*/
    private final double width;
    /** .*/
    private final Color color;
    /** .*/
    private CesiumArcType arcType = null;
    /** .*/
    private Boolean arrow = false;
    /** .*/
    private Cartesian firstPosition;
    /** .*/
    private Cartesian secondPosition;
    /** .*/
    private double nearDistance;
    /** .*/
    private double farDistance;

    // BUILDERS
    // CLASSIC BUILDERS without position
    /**  This builder allows the construction of a polyline to be used as a non-vector.
     * @param availability : A time interval to represents when the packet will need to be displayed
     * @param width : The width of the polyline
     * @param color : The color of the polyline in r,g,b or r,g,b,a
     * @param show : A boolean to know if the polyline must be displayed */
    public Polyline(final TimeInterval availability, final double width, final Color color, final boolean show) {
        this.availability = availability;
        this.width = width;
        this.color = color;
        this.show = show;
        this.arcType = CesiumArcType.NONE;
    }

    /**  This builder allows the construction of a polyline to be used as a non-vector. It has no parameters. */
    public Polyline() {
        this.color = new Color(0, 255, 255, 255);
        this.width = 1;
        this.arcType = CesiumArcType.NONE;
    }

    // Vector Polyline builder
    /** This builder allows the construction of a polyline to be used as a vector.
     * @param cartesians : A list of cartesians with size 2, containing the first and the second position of the polyline
     * @param availability : A timeInterval representing the time interval when the polyline must be displayed, usually, each time step
     * @param color : the color of the polyline*/
    public Polyline(final List<Cartesian> cartesians, final TimeInterval availability, final Color color) {
        this(cartesians, availability, color, 1, 100000000);
    }

    /** This builder allows the construction of a polyline to be used as a vector.
     * @param cartesians : A list of cartesians with size 2, containing the first and the second position of the polyline
     * @param availability : A timeInterval representing the time interval when the polyline must be displayed, usually, each time step
     * @param color : the color of the polyline
     * @param nearDistance : The nearest distance where the polyline is displayed
     * @param farDistance : The fairest distance where the polyline is displayed*/
    public Polyline(final List<Cartesian> cartesians, final TimeInterval availability, final Color color, final double nearDistance, final double farDistance) {
        if (cartesians.size() != 2) {
            throw new RuntimeException("The size of the cartesian positions inputted in the Polyline must be 2");
        } else {
            this.width = 2;
            this.color = color;
            this.show = true;
            this.arcType = CesiumArcType.NONE;
            this.arrow = true;
            this.availability = availability;
            firstPosition = cartesians.get(0);
            secondPosition = cartesians.get(1);
            this.nearDistance = nearDistance;
            this.farDistance = farDistance;
        }
    }

    //// FUNCTIONS
    /** This functions aims at writing a polyline built as a vector in the Inertial frame.
     * @param packet : The packet where the polyline is written
     * @param output : The cesium output stream that write the strings in the file */
    public void writePolylineVectorInertial(final PacketCesiumWriter packet, final CesiumOutputStream output) {
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
                writePositionOfVectorInertial(polylineWriter, this, output);
            }
            try (BooleanCesiumWriter showWriter = polylineWriter.getShowWriter()) {
                showWriter.open(output);
                showWriter.writeInterval(availability);
                showWriter.writeBoolean(true);
            }
        }
    }

    /** This functions aims at writing a polyline built as a vector in the Fixed frame.
     * @param packet : The packet where the polyline is written
     * @param output : The cesium output stream that write the strings in the file */
    public void writePolylineVectorFixed(final PacketCesiumWriter packet, final CesiumOutputStream output) {
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
                writePositionOfVectorFixed(polylineWriter, this, output);
            }
            try (BooleanCesiumWriter showWriter = polylineWriter.getShowWriter()) {
                showWriter.open(output);
                showWriter.writeInterval(availability);
                showWriter.writeBoolean(true);
            }
        }
    }

    /** This function aims at writing a polyline built as a non-vector to be displayed as a line of visibility.
     * @param packet : The packet where the polyline is written
     * @param output : The cesium output stream that write the strings in the file
     * @param references : The references (example : object_ID#position the reference of the position of the object) that will be used to position each extremity of the polyline that will be displayed
     * @param showList : A list of Show objects, these objects will be useful to know when to display or not the polyline in time */
    public void writePolylineOfVisibility(final PacketCesiumWriter packet, final CesiumOutputStream output, final Iterable<Reference> references, final List<CZMLShow> showList) {
        try (PolylineCesiumWriter polylineWriter = packet.getPolylineWriter()) {
            polylineWriter.open(output);
            polylineWriter.writeWidthProperty(this.getWidth());
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

    /** This function aims at writing the position of the polyline built as a non-vector to display an attitude in the INERTIAL frame.
     * @param polylineWriter : the writer extracted from a packet cesium writer to write parameters of the polyline
     * @param polylineInput : the polyline object inputted
     * @param output : The cesium output stream that write the strings in the file */
    private void writePositionOfVectorInertial(final PolylineCesiumWriter polylineWriter, final Polyline polylineInput, final CesiumOutputStream output) {
        try (PositionListCesiumWriter positionWriter = polylineWriter.getPositionsWriter()) {
            positionWriter.open(output);
            final List<Cartesian> tempCartesians = new ArrayList<>();
            tempCartesians.add(firstPosition);
            tempCartesians.add(secondPosition);
            positionWriter.writeCartesian(tempCartesians);
            positionWriter.writeReferenceFrame("INERTIAL");
        }
    }

    /** This function aims at writing the position of the polyline built as a non-vector to display an attitude in the FIXED frame.
     * @param polylineWriter : the writer extracted from a packet cesium writer to write parameters of the polyline
     * @param polylineInput : the polyline object inputted
     * @param output : The cesium output stream that write the strings in the file */
    private void writePositionOfVectorFixed(final PolylineCesiumWriter polylineWriter, final Polyline polylineInput, final CesiumOutputStream output) {
        try (PositionListCesiumWriter positionWriter = polylineWriter.getPositionsWriter()) {
            positionWriter.open(output);
            positionWriter.writeReferenceFrame("FIXED");
            final List<Cartesian> tempCartesians = new ArrayList<>();
            tempCartesians.add(firstPosition);
            tempCartesians.add(secondPosition);
            positionWriter.writeCartesian(tempCartesians);
        }
    }

    /** This function aims at writing the position of the polyline built as a non-vector to display a line of visibility.
     * @param polylineWriter : the writer extracted from a packet cesium writer to write parameters of the polyline
     * @param polylineInput : the polyline object inputted
     * @param output : The cesium output stream that write the strings in the file
     * @param references : The references (example : object_ID#position the reference of the position of the object) that will be used to position each extremity of the polyline that will be displayed*/
    private void writePositionOfVisibility(final PolylineCesiumWriter polylineWriter, final Polyline polylineInput, final CesiumOutputStream output, final Iterable<Reference> references) {
        polylineWriter.writeArcTypeProperty(polylineInput.getArcType());

        try (PositionListCesiumWriter positionWriter = polylineWriter.getPositionsWriter()) {
            positionWriter.open(output);
            positionWriter.writeReferences(references);
        }
    }

    /** This functions aims at writing the list of Show objects of a polyline built as a non-vector to display a line of visibility.
     * @param polylineWriter : the writer extracted from a packet cesium writer to write parameters of the polyline
     * @param output : The cesium output stream that write the strings in the file
     * @param showList : A list of Show objects, these objects will be useful to know when to display or not the polyline in time */
    private void writeShowOfVisibility(final PolylineCesiumWriter polylineWriter, final CesiumOutputStream output, final List<CZMLShow> showList) {
        try (BooleanCesiumWriter showWriter = polylineWriter.getShowWriter()) {
            showWriter.open(output);
            output.writeStartSequence();
            if (showList.size() == 1) {
                final CZMLShow showTemp = showList.get(0);
                showWriter.writeInterval(showTemp.getAvailability());
                showWriter.writeBoolean(showTemp.getShow());
            } else {
                for (int i = 0; i < showList.size(); i++) {
                    final CZMLShow showTemp = showList.get(i);
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

    // GETS
    public TimeInterval getAvailability() {
        return availability;
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

    public void setAvailability(final TimeInterval availability) {
        this.availability = availability;
    }

    public Boolean getArrow() {
        return arrow;
    }
}
