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

import cesiumlanguagewriter.BillboardCesiumWriter;
import cesiumlanguagewriter.Cartesian;
import cesiumlanguagewriter.CesiumOutputStream;
import cesiumlanguagewriter.CesiumStreamWriter;
import cesiumlanguagewriter.PacketCesiumWriter;
import cesiumlanguagewriter.PositionCesiumWriter;
import cesiumlanguagewriter.TimeInterval;
import cesiumlanguagewriter.UriCesiumWriter;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.orekit.czml.archi.builder.CzmlGroundStationBuilder;
import org.orekit.czml.errors.OreCzmlException;
import org.orekit.czml.errors.OreCzmlMessages;
import org.orekit.czml.object.Position;
import org.orekit.czml.object.PositionType;
import org.orekit.czml.object.nonvisual.CzmlModel;
import org.orekit.czml.object.secondary.Billboard;
import org.orekit.czml.object.secondary.Label;
import org.orekit.frames.TopocentricFrame;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * CZML Ground Station
 * <p>
 * This class groups all the characteristics of a ground station. The ground station will be represented at the surface
 * of the central body with precise cartographic parameters. It can be build from the orekit ground station
 * {@link org.orekit.estimation.measurements.GroundStation}, or from the orekit topocentric frame {@link org.orekit.frames.TopocentricFrame}.
 * </p>
 *
 * @author Julien LEBLOND
 * @since 1.0.0
 */
public class CzmlGroundStation extends AbstractPrimaryObject {

    /**
     * The default image used when no image/model is used for the station.
     */
    public static final String DEFAULT_IMAGE = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAADsMAAA7DAcdvqGQAAACvSURBVDhPrZDRDcMgDAU9GqN0lIzijw6SUbJJygUeNQgSqepJTyHG91LVVpwDdfxM3T9TSl1EXZvDwii471fivK73cBFFQNTT/d2KoGpfGOpSIkhUpgUMxq9DFEsWv4IXhlyCnhBFnZcFEEuYqbiUlNwWgMTdrZ3JbQFoEVG53rd8ztG9aPJMnBUQf/VFraBJeWnLS0RfjbKyLJA8FkT5seDYS1Qwyv8t0B/5C2ZmH2/eTGNNBgMmAAAAAElFTkSuQmCC";

    /**
     * The default id for ground stations.
     */
    public static final String DEFAULT_ID = "GROUND_STATION/";

    /**
     * The default name for ground stations.
     */
    public static final String DEFAULT_NAME = "Ground station : ";

    /**
     * The default 3d model: empty.
     */
    public static final String DEFAULT_3D_MODEL = "";

    /**
     * The list containing all the names of all the stations.
     */
    private final List<String> names = new ArrayList<>();

    /**
     * The list of all the ids of all the stations.
     */
    private final List<String> ids = new ArrayList<>();

    /**
     * The list of all the availabilities of the ground stations.
     */
    private final List<TimeInterval> availabilities = new ArrayList<>();

    /**
     * The position on earth of a single station, it is the cartesian vector from the geodetic point.
     */
    private Vector3D positionOnEarth;

    /**
     * The list of all the position on earth of all the stations.
     */
    private final List<Vector3D> positionsOnEarth = new ArrayList<>();

    /**
     * The billboard that will display the image of the station.
     */
    private Billboard billboard;

    /**
     * The billboards of the stations.
     */
    private final List<Billboard> billboards = new ArrayList<>();

    /**
     * The list of the topocentric frames used to build the ground stations when multiple stations are computed.
     */
    private final List<TopocentricFrame> topocentricFrames;

    /**
     * The model used to define the station if only one model is used.
     */
    private CzmlModel model;

    /**
     * The list of the models used to build ground stations models when multiple stations are computed.
     */
    private List<CzmlModel> models = new ArrayList<>();

    // Intrinsic parameters

    /**
     * The list of the position object of the stations when several stations are computed.
     */
    private final List<Position> positionsObjects = new ArrayList<>();

    /**
     * The list of all the paths of the models if several are used.
     */
    private final List<String> modelPaths;

    /** The descriptions to write for each station. */
    private List<String> descriptions = new ArrayList<>();

    //// Constructors
    // Single Station Constructors

    /**
     * The constructor of the czml ground station object with a default model.
     *
     * @param topocentricFrame : The topocentric frame where the ground station must be located.
     * @param header           : The header considered.
     * @throws URISyntaxException the uri syntax exception
     * @throws IOException        the io exception
     */
    public CzmlGroundStation(final TopocentricFrame topocentricFrame,
                             final Header header) throws URISyntaxException, IOException {
        this(topocentricFrame, DEFAULT_3D_MODEL, header);
    }

    /**
     * The constructor of the czml ground station object with no default parameters.
     *
     * @param topocentricFrame : The topocentric frame where the ground station must be located.
     * @param modelPath        : The path of the model to load.
     * @param header           : The header considered.
     * @throws URISyntaxException the uri syntax exception
     * @throws IOException        the io exception
     */
    public CzmlGroundStation(final TopocentricFrame topocentricFrame, final String modelPath,
                             final Header header) throws URISyntaxException, IOException {

        this(Collections.singletonList(topocentricFrame), modelPath, header);
    }

    // Multiple Stations Constructors

    /**
     * The constructor of several ground stations with a default model.
     *
     * @param topocentricFrames : The list of topocentric frame where the station must be located.
     * @param header            : The header considered.
     * @throws URISyntaxException the uri syntax exception
     * @throws IOException        the io exception
     */
    public CzmlGroundStation(final List<TopocentricFrame> topocentricFrames,
                             final Header header) throws URISyntaxException, IOException {
        this(topocentricFrames, DEFAULT_3D_MODEL, header);
    }

    /**
     * The constructor of several ground stations with no default parameters.
     *
     * @param topocentricFrames : The list of topocentric frame where the station must be located.
     * @param modelPath         : The path of the model to load (same for all stations, a feature when each station will be able                          to have its own model it under development)
     * @param header            : The header considered.
     * @throws URISyntaxException the uri syntax exception
     * @throws IOException        the io exception
     */
    public CzmlGroundStation(final List<TopocentricFrame> topocentricFrames, final String modelPath,
                             final Header header) throws URISyntaxException, IOException {
        this(topocentricFrames, Collections.singletonList(modelPath),
                topocentricFrames.size() + " " + topocentricFrames.get(0)
                                                                  .getName() + DEFAULT_ID, header);
    }

    /**
     * Instantiates a new Czml ground station.
     *
     * @param topocentricFrames the topocentric frames
     * @param modelPathsInput   the model paths input
     * @param customID          the custom id
     * @param header            the header
     * @throws URISyntaxException the uri syntax exception
     * @throws IOException        the io exception
     */
    public CzmlGroundStation(final List<TopocentricFrame> topocentricFrames, final List<String> modelPathsInput,
                             final String customID, final Header header) throws URISyntaxException, IOException {

        this.topocentricFrames = new ArrayList<>(topocentricFrames);
        this.setName("Packet containing " + topocentricFrames.size() + " ground stations");
        this.setId(customID);
        this.modelPaths = new ArrayList<>(modelPathsInput);
        for (final TopocentricFrame currentTopocentricFrame : topocentricFrames) {
            final double latitude = currentTopocentricFrame.getPoint()
                                                           .getLatitude();
            final double longitude = currentTopocentricFrame.getPoint()
                                                            .getLongitude();
            final double altitude = currentTopocentricFrame.getPoint()
                                                           .getAltitude();
            final PositionType positionType = PositionType.CARTOGRAPHIC_RADIANS;
            this.availabilities.add(header.getAvailability());
            this.ids.add(DEFAULT_ID + currentTopocentricFrame.getName());
            this.descriptions.add(
                    "<!--HTML-->\r\n<p>Id : " + DEFAULT_ID + currentTopocentricFrame.getName() + "</p\r\n<p>Longitude : " + longitude + "</p>\r\n<p>Latitude : " + latitude + "</p>\r\n<p>Simulated from : " + header.getAvailability()
                                                                                                                                                                                                                     .getStart() + " to " + header.getAvailability()
                                                                                                                                                                                                                                                  .getStop() + "</p>");
            this.billboards.add(new Billboard(DEFAULT_IMAGE));
            this.names.add(DEFAULT_NAME + currentTopocentricFrame.getName());
            this.positionsOnEarth.add(currentTopocentricFrame.getCartesianPoint());
            this.positionsObjects.add(new Position(longitude, latitude, altitude, positionType, header));
        }

        if (modelPathsInput.isEmpty()) {
            this.models = null;
        } else {
            if (modelPathsInput.size() == 1 && modelPathsInput.get(0)
                                                              .isEmpty()) {
                this.model = null;
            } else if (modelPathsInput.size() == 1) {
                this.model = new CzmlModel(modelPathsInput.get(0), 50, 300, 2, false, header);
            } else {
                for (final String currentPathModel : modelPathsInput) {
                    this.models.add(new CzmlModel(currentPathModel, 50, 300, 2, false, header));
                }
            }
        }
    }

    // Builders

    /**
     * Builder czml ground station builder.
     *
     * @param topocentricFrameInput the topocentric frame input
     * @param header                the header
     * @return the czml ground station builder
     */
    public static CzmlGroundStationBuilder builder(final TopocentricFrame topocentricFrameInput, final Header header) {
        return new CzmlGroundStationBuilder(topocentricFrameInput, header);
    }

    /**
     * Builder czml ground station builder.
     *
     * @param topocentricFramesInput the topocentric frames input
     * @param header                 the header
     * @return the czml ground station builder
     */
    public static CzmlGroundStationBuilder builder(final List<TopocentricFrame> topocentricFramesInput,
                                                   final Header header) {
        return new CzmlGroundStationBuilder(topocentricFramesInput, header);
    }

    // Overrides

    @Override
    public void writeCzmlBlock(final CesiumStreamWriter stream,
                               final CesiumOutputStream output) throws IOException, URISyntaxException {
        for (int i = 0; i < ids.size(); i++) {

            this.setId(ids.get(i));
            this.setName(names.get(i));
            this.setAvailability(availabilities.get(i));
            this.positionOnEarth = positionsOnEarth.get(i);
            this.billboard       = billboards.get(i);

            output.setPrettyFormatting(true);
            try (PacketCesiumWriter packet = stream.openPacket(output)) {
                packet.writeId(ids.get(i));
                packet.writeName(names.get(i));
                packet.writeAvailability(availabilities.get(i));

                writeMultipleModels(packet, i, output);

                writeLabel(packet, i, output);

                writePosition(packet, output);
            }
        }
    }


    // Getters

    /**
     * Gets billboard.
     *
     * @return the billboard
     */
    public Billboard getBillboard() {
        return billboard;
    }

    /**
     * Gets topocentric frame.
     *
     * @return the topocentric frame
     */
    public TopocentricFrame getTopocentricFrame() {
        if (topocentricFrames.size() == 1) {
            return topocentricFrames.get(0);
        } else {
            throw new OreCzmlException(OreCzmlMessages.SEVERAL_STATION_UNIQUE_GET);
        }
    }

    /**
     * Gets topocentric frames.
     *
     * @return the topocentric frames
     */
    public List<TopocentricFrame> getTopocentricFrames() {
        if (topocentricFrames.isEmpty()) {
            throw new OreCzmlException(OreCzmlMessages.SINGLE_STATION_GET_MULTIPLE_GROUND_STATION);
        } else {
            return Collections.unmodifiableList(topocentricFrames);
        }
    }

    /**
     * Gets positions.
     *
     * @return the positions
     */
    public Vector3D getPositions() {
        if (topocentricFrames.size() == 1) {
            return positionsOnEarth.get(0);
        } else {
            throw new OreCzmlException(OreCzmlMessages.SEVERAL_STATION_UNIQUE_GET);
        }
    }

    /**
     * Gets position object.
     *
     * @return the position object
     */
    public Position getPositionObject() {
        if (topocentricFrames.size() == 1) {
            return positionsObjects.get(0);
        } else {
            throw new OreCzmlException(OreCzmlMessages.SEVERAL_STATION_UNIQUE_GET);
        }
    }

    /**
     * Gets billboards.
     *
     * @return the billboards
     */
    public List<Billboard> getBillboards() {
        return Collections.unmodifiableList(billboards);
    }

    /**
     * Gets positions objects.
     *
     * @return the positions objects
     */
    public List<Position> getPositionsObjects() {
        return Collections.unmodifiableList(positionsObjects);
    }

    public List<TimeInterval> getAvailabilities() {
        return Collections.unmodifiableList(availabilities);
    }

    /**
     * Gets ids.
     *
     * @return the ids
     */
    public List<String> getIds() {
        return Collections.unmodifiableList(ids);
    }

    /**
     * Gets names.
     *
     * @return the names
     */
    public List<String> getNames() {
        return Collections.unmodifiableList(names);
    }

    /**
     * Gets positions on earth.
     *
     * @return the positions on earth
     */
    public List<Vector3D> getPositionsOnEarth() {
        return Collections.unmodifiableList(positionsOnEarth);
    }

    /**
     * Gets position on earth.
     *
     * @return the position on earth
     */
    public Vector3D getPositionOnEarth() {
        return positionOnEarth;
    }

    /**
     * Gets model.
     *
     * @return the model
     */
    public CzmlModel getModel() {
        return model;
    }


    // Private functions

    /**
     * This function aims at writing the billboard of the ground station (if one is necessary).
     *
     * @param packet : The packet that will write in the czml file.
     * @param output : The output stream of cesium that will contain the strings to write into the CzmLFile.
     */
    private void writeBillBoard(final PacketCesiumWriter packet,
                                final CesiumOutputStream output) throws IOException, URISyntaxException {
        if (model == null) {
            try (BillboardCesiumWriter billboardWriter = packet.getBillboardWriter()) {
                billboardWriter.open(output);
                billboardWriter.writeHorizontalOriginProperty(billboard.getCesiumHorizontalOrigin());
                billboardWriter.writeColorProperty(billboard.getColor());
                try (UriCesiumWriter imageBillBoard = billboardWriter.openImageProperty()) {
                    imageBillBoard.writeUri(billboard.getImageStr(), billboard.getCesiumResourceBehavior());
                }
                billboardWriter.writeScaleProperty(billboard.getScale());
                billboardWriter.writeShowProperty(billboard.getShow());
            }
        } else {
            model.generateCZML(packet, output);
        }
    }

    /**
     * This function will write the specific model for the given station when multiple stations are used.
     *
     * @param packet          : The packet that will write in the czml file.
     * @param iterationNumber : The iteration number of the current station.
     * @param output          : The output stream of cesium that will contain the strings to write into the CzmLFile.
     */
    private void writeMultipleModels(final PacketCesiumWriter packet, final int iterationNumber,
                                     final CesiumOutputStream output) throws IOException, URISyntaxException {
        if (models.isEmpty()) {
            writeBillBoard(packet, output);
        } else if (modelPaths.size() == 1) {
            if (modelPaths.get(0)
                          .isEmpty()) {
                writeBillBoard(packet, output);
            } else {
                this.model.generateCZML(packet, output);
            }
        } else {
            final CzmlModel currentModel = models.get(iterationNumber);
            currentModel.generateCZML(packet, output);
        }
    }

    /**
     * This function aims at writing multiple models when several are loaded.
     * The number of models should be the same as the number of ground stations wanted.
     *
     * @param packet          : The packet that will write in the czml file.
     * @param iterationNumber : The number of the iteration in the loop.
     * @param output          : The output stream of cesium that will contain the strings to write into the CzmLFile.
     */
    private void writeLabel(final PacketCesiumWriter packet, final int iterationNumber,
                            final CesiumOutputStream output) {
        final Label label = new Label(topocentricFrames.get(iterationNumber)
                                                       .getName());
        label.write(packet, output);
    }


    /**
     * This function aims at writing the position of the ground station.
     *
     * @param packet : The packet that will write in the czml file.
     * @param output : The output stream of cesium that will contain the strings to write into the CzmLFile.
     */
    private void writePosition(final PacketCesiumWriter packet, final CesiumOutputStream output) {

        try (PositionCesiumWriter positionWriter = packet.getPositionWriter()) {
            positionWriter.open(output);
            positionWriter.writeInterval(this.getAvailability());
            final Cartesian cartesian = new Cartesian(positionOnEarth.getX(), positionOnEarth.getY(),
                    positionOnEarth.getZ());
            positionWriter.writeCartesian(cartesian);
        }
    }

}
