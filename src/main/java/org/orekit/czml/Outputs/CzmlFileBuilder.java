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
package org.orekit.czml.Outputs;

import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.AttitudePointing;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.CentralBodyReferenceSystem;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.Constellation;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.CovarianceDisplay;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.CzmlGroundStation;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.FieldOfObservation;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.GroundTrack;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.Header;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.InterSatVisu;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.LineOfVisibility;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.ManeuverSequence;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.Satellite;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.SatelliteReferenceSystem;
import org.orekit.frames.TopocentricFrame;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class CzmlFileBuilder {

    /** . */
    public static final double DEFAULT_ANGLE_OF_APERTURE = 80.0;

    /** . */
    public static final String DEFAULT_MODEL_PATH = "";

    /** . */
    private String outputPath;

    /** . */
    private Header header;

    /** . */
    private List<Satellite> satellites = new ArrayList<>();

    /** . */
    private List<Constellation> constellations = new ArrayList<>();

    /** . */
    private List<CzmlGroundStation> groundStations = new ArrayList<>();

    /** . */
    private List<InterSatVisu> visus = new ArrayList<>();

    /** . */
    private List<LineOfVisibility> lines = new ArrayList<>();

    /** . */
    private List<GroundTrack> groundTracks = new ArrayList<>();

    /** . */
    private List<AttitudePointing> attitudePointings = new ArrayList<>();

    /** . */
    private List<CovarianceDisplay> covariances = new ArrayList<>();

    /** . */
    private List<FieldOfObservation> fields = new ArrayList<>();

    /** . */
    private List<ManeuverSequence> maneuverSequences = new ArrayList<>();

    /** . */
    private List<SatelliteReferenceSystem> satelliteSystems = new ArrayList<>();

    /** . */
    private CentralBodyReferenceSystem system;


    public CzmlFileBuilder(final String pathFile) {
        this.outputPath = pathFile;
    }


    public CzmlFileBuilder withHeader(final Header headerInput) {
        this.header = headerInput;
        return this;
    }


    // Satellite
    public CzmlFileBuilder withSatellite(final Satellite satelliteInput) {
        this.satellites.add(satelliteInput);
        return this;
    }

    public CzmlFileBuilder withSatellite(final List<Satellite> satellitesInput) {
        this.satellites.addAll(satellitesInput);
        return this;
    }


    // Constellation
    public CzmlFileBuilder withConstellation(final Constellation constellationInput) {
        this.constellations.add(constellationInput);
        return this;
    }

    public CzmlFileBuilder withConstellation(final List<Constellation> constellationsInput) {
        this.constellations.addAll(constellationsInput);
        return this;
    }


    // Ground stations
    public CzmlFileBuilder withCzmlGroundStation(final CzmlGroundStation station) {
        this.groundStations.add(station);
        return this;
    }

    public CzmlFileBuilder withCzmlGroundStation(final List<CzmlGroundStation> stations) {
        this.groundStations.addAll(stations);
        return this;
    }


    // Attitude pointing
    public CzmlFileBuilder withAttitudePointing(final AttitudePointing attitudePointingInput) {
        attitudePointings.add(attitudePointingInput);
        return this;
    }

    public CzmlFileBuilder withAttitudePointing(final List<AttitudePointing> attitudePointingsInput) {
        attitudePointings.addAll(attitudePointingsInput);
        return this;
    }


    // Covariance Display
    public CzmlFileBuilder withCovarianceDisplay(final CovarianceDisplay covarianceDisplayInput) {
        this.covariances.add(covarianceDisplayInput);
        return this;
    }

    public CzmlFileBuilder withCovarianceDisplay(final List<CovarianceDisplay> covariancesInput) {
        this.covariances.addAll(covariancesInput);
        return this;
    }


    // Field Of Observation
    public CzmlFileBuilder withFieldOfObservation(final FieldOfObservation fieldOfObservationInput) {
        this.fields.add(fieldOfObservationInput);
        return this;
    }

    public CzmlFileBuilder withFieldOfObservation(final List<FieldOfObservation> fieldOfObservationsInput) {
        this.fields.addAll(fieldOfObservationsInput);
        return this;
    }


    // Line of visibility
    public CzmlFileBuilder withLineOfVisibility(final TopocentricFrame topocentricFrameInput,
                                                final Satellite satelliteInput,
                                                final double angleOfAperture) throws URISyntaxException, IOException {
        final LineOfVisibility line = LineOfVisibility.builder(topocentricFrameInput, satelliteInput)
                                                      .withAngleOfAperture(angleOfAperture)
                                                      .build();
        lines.add(line);
        return this;
    }

    public CzmlFileBuilder withLineOfVisibility(final TopocentricFrame topocentricFrameInput,
                                                final Satellite satelliteInput) throws URISyntaxException, IOException {
        return this.withLineOfVisibility(topocentricFrameInput, satelliteInput, DEFAULT_ANGLE_OF_APERTURE);
    }

    public CzmlFileBuilder withLineOfVisibility(final TopocentricFrame topocentricFrameInput,
                                                final Constellation constellationInput,
                                                final double angleOfAperture) throws URISyntaxException, IOException {
        final List<Satellite> satellitesOfConstellation = constellationInput.getAllSatellites();
        for (int i = 0; i < constellationInput.getTotalOfSatellite(); i++) {
            final Satellite currentSatellite = satellitesOfConstellation.get(i);
            lines.add(LineOfVisibility.builder(topocentricFrameInput, currentSatellite)
                                      .withAngleOfAperture(angleOfAperture)
                                      .build());
        }
        return this;
    }

    public CzmlFileBuilder withLineOfVisibility(final TopocentricFrame topocentricFrameInput,
                                                final Constellation constellationInput) throws URISyntaxException, IOException {
        return this.withLineOfVisibility(topocentricFrameInput, constellationInput, DEFAULT_ANGLE_OF_APERTURE);
    }

    public CzmlFileBuilder withLineOfVisibility(final List<TopocentricFrame> topocentricFramesInput,
                                                final Satellite satelliteInput,
                                                final double angleOfAperture) throws URISyntaxException, IOException {
        for (final TopocentricFrame currentFrame : topocentricFramesInput) {
            lines.add(LineOfVisibility.builder(currentFrame, satelliteInput)
                                      .withAngleOfAperture(angleOfAperture)
                                      .build());
        }
        return this;
    }

    public CzmlFileBuilder withLineOfVisibility(final List<TopocentricFrame> topocentricFramesInput,
                                                final Satellite satelliteInput) throws URISyntaxException, IOException {
        return this.withLineOfVisibility(topocentricFramesInput, satelliteInput, DEFAULT_ANGLE_OF_APERTURE);
    }

    public CzmlFileBuilder withLineOfVisibility(final List<TopocentricFrame> topocentricFramesInput,
                                                final Constellation constellationInput,
                                                final double angleOfAperture) throws URISyntaxException, IOException {
        for (final TopocentricFrame currentFrame : topocentricFramesInput) {
            for (int j = 0; j < constellationInput.getTotalOfSatellite(); j++) {
                final Satellite currentSatellite = constellationInput.getAllSatellites()
                                                                     .get(j);
                final LineOfVisibility currentLine = LineOfVisibility.builder(currentFrame, currentSatellite)
                                                                     .withAngleOfAperture(angleOfAperture)
                                                                     .build();
                lines.add(currentLine);
            }
        }
        return this;
    }

    public CzmlFileBuilder withLineOfVisibility(final List<TopocentricFrame> topocentricFramesInput,
                                                final Constellation constellationInput) throws URISyntaxException, IOException {
        return this.withLineOfVisibility(topocentricFramesInput, constellationInput, DEFAULT_ANGLE_OF_APERTURE);
    }


    // Inter-sat Visu
    public CzmlFileBuilder withInterSatVisu(final InterSatVisu visuInput) {
        this.visus.add(visuInput);
        return this;
    }

    public CzmlFileBuilder withInterSatVisu(final List<InterSatVisu> visuInputs) {
        this.visus.addAll(visuInputs);
        return this;
    }


    // Maneuver sequence
    public CzmlFileBuilder withManeuverSequence(final ManeuverSequence maneuverSequenceInput) {
        this.maneuverSequences.add(maneuverSequenceInput);
        return this;
    }

    public CzmlFileBuilder withManeuverSequence(final List<ManeuverSequence> maneuverSequencesInput) {
        this.maneuverSequences.addAll(maneuverSequencesInput);
        return this;
    }


    // Ground Track
    public CzmlFileBuilder withGroundTrack(final GroundTrack groundTrackInput) {
        this.groundTracks.add(groundTrackInput);
        return this;
    }

    public CzmlFileBuilder withGroundTrack(final List<GroundTrack> groundTracksInput) {
        this.groundTracks.addAll(groundTracksInput);
        return this;
    }


    // Satellite Reference System
    public CzmlFileBuilder withSatelliteReferenceSystem(final SatelliteReferenceSystem systemInput) {
        this.satelliteSystems.add(systemInput);
        return this;
    }

    public CzmlFileBuilder withSatelliteReferenceSystem(final List<SatelliteReferenceSystem> systemsInput) {
        this.satelliteSystems.addAll(systemsInput);
        return this;
    }


    // Central Body Reference System
    public CzmlFileBuilder withCentralBodyReferenceSystem(final CentralBodyReferenceSystem systemInput) {
        this.system = systemInput;
        return this;
    }

    public CzmlFile build() {
        final CzmlFile toReturn = new CzmlFile(outputPath);
        // Add the header
        toReturn.addObject(header);

        this.addSatellites(toReturn);
        this.addConstellations(toReturn);
        this.addGroundStations(toReturn);
        this.addAttitudePointings(toReturn);
        this.addCovarianceDisplays(toReturn);
        this.addFieldOfObservation(toReturn);
        this.addLineOfVisibility(toReturn);
        this.addInterSatVisu(toReturn);
        this.addManeuverSequence(toReturn);
        this.addGroundTrack(toReturn);
        this.addSatelliteReferenceSystem(toReturn);
        this.addCentralBodyReferenceSystem(toReturn);

        return toReturn;
    }

    private void addSatellites(final CzmlFile file) {
        for (Satellite satellite : satellites) {
            file.addObject(satellite);
        }
    }

    private void addConstellations(final CzmlFile file) {
        for (Constellation constellation : constellations) {
            file.addObject(constellation);
        }
    }

    private void addGroundStations(final CzmlFile file) {
        for (CzmlGroundStation station : groundStations) {
            file.addObject(station);
        }
    }

    private void addAttitudePointings(final CzmlFile file) {
        for (AttitudePointing pointing : attitudePointings) {
            file.addObject(pointing);
        }
    }

    private void addCovarianceDisplays(final CzmlFile file) {
        for (CovarianceDisplay covariance : covariances) {
            file.addObject(covariance);
        }
    }

    private void addFieldOfObservation(final CzmlFile file) {
        for (FieldOfObservation field : fields) {
            file.addObject(field);
        }
    }

    private void addLineOfVisibility(final CzmlFile file) {
        for (LineOfVisibility line : lines) {
            file.addObject(line);
        }
    }

    private void addInterSatVisu(final CzmlFile file) {
        for (InterSatVisu visu : visus) {
            file.addObject(visu);
        }
    }

    private void addManeuverSequence(final CzmlFile file) {
        for (ManeuverSequence maneuverSequence : maneuverSequences) {
            file.addObject(maneuverSequence);
        }
    }

    private void addGroundTrack(final CzmlFile file) {
        for (GroundTrack groundTrack : groundTracks) {
            file.addObject(groundTrack);
        }
    }

    private void addSatelliteReferenceSystem(final CzmlFile file) {
        for (SatelliteReferenceSystem systemInput : satelliteSystems) {
            file.addObject(systemInput);
        }
    }

    private void addCentralBodyReferenceSystem(final CzmlFile file) {
        if (system != null) {
            file.addObject(system);
        }
    }
}
