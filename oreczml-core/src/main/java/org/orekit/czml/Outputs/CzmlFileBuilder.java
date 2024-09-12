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
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.BodyDisplay;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.CentralBodyReferenceSystem;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.CollisionDisplay;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.Constellation;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.CovarianceDisplay;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.CoveredSurfaceOnBody;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.CzmlGroundStation;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.FieldOfObservation;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.GroundTrack;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.Header;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.InterSatVisu;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.LatLongLinesDisplay;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.LineOfVisibility;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.ManeuverSequence;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.Satellite;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.SatelliteReferenceSystem;
import org.orekit.frames.TopocentricFrame;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Czml File Builder
 *
 * <p>
 * Builder and architect for the {@link CzmlFile} class. This builder organise and manage all the objects that needs to
 * be added to a Czml file.
 * </p>
 *
 * @author Julien LEBLOND.
 * @since 1.0.0
 */

public class CzmlFileBuilder {

    /** The default angle of aperture of a ground station. */
    public static final double DEFAULT_ANGLE_OF_APERTURE = 80.0;

    /** The path of the output. */
    private final String                         outputPath;
    /** List of all the satellites to write. */
    private final List<Satellite>                satellites        = new ArrayList<>();
    /** List of all the constellations to write. */
    private final List<Constellation>            constellations    = new ArrayList<>();
    /** List of all the czml ground stations to write. */
    private final List<CzmlGroundStation>        groundStations    = new ArrayList<>();
    /** List of all the inter-sat visu to write. */
    private final List<InterSatVisu>             visus             = new ArrayList<>();
    /** List of all the collision display to write. */
    private final List<CollisionDisplay>         collisions        = new ArrayList<>();
    /** List of all the lines of visibility to write. */
    private final List<LineOfVisibility>         lines             = new ArrayList<>();
    /** List of all the ground tracks to write. */
    private final List<GroundTrack>              groundTracks      = new ArrayList<>();
    /** List of all the attitude pointings to write. */
    private final List<AttitudePointing>         attitudePointings = new ArrayList<>();
    /** List of all the covariance display to write. */
    private final List<CovarianceDisplay>        covariances       = new ArrayList<>();
    /** List of all the fields of observation to write. */
    private final List<FieldOfObservation>       fields            = new ArrayList<>();
    /** List of all the maneuver sequences to write. */
    private final List<ManeuverSequence>         maneuverSequences = new ArrayList<>();
    /** List of all the satellite reference systems to write. */
    private final List<SatelliteReferenceSystem> satelliteSystems  = new ArrayList<>();
    /** List of all the covered surfaces on body to write. */
    private final List<CoveredSurfaceOnBody>     surfaces          = new ArrayList<>();
    /** List of all the latitude longitude lines display to write. */
    private final List<LatLongLinesDisplay>      latLongs          = new ArrayList<>();
    /** List of all the body display to write. */
    private final List<BodyDisplay>              bodyDisplays      = new ArrayList<>();
    /** The header of the czml file. */
    private       Header                         header;
    /** The central body reference system to write. */
    private       CentralBodyReferenceSystem     system;


    // Constructor

    /**
     * The constructor of the czml file builder object.
     *
     * @param pathFile : The path of the output.
     */
    public CzmlFileBuilder(final String pathFile) {
        this.outputPath = pathFile;
    }


    /**
     * Function to set up the header.
     *
     * @param headerInput : The header to set up.
     * @return : The czml file builder with the given header.
     */
    public CzmlFileBuilder withHeader(final Header headerInput) {
        this.header = headerInput;
        return this;
    }


    // Satellite

    /**
     * Function to set up a satellite.
     *
     * @param satelliteInput : The satellite to set up.
     * @return : The czml file builder with the given satellite.
     */
    public CzmlFileBuilder withSatellite(final Satellite satelliteInput) {
        this.satellites.add(satelliteInput);
        return this;
    }

    /**
     * Function to set up a list of satellites.
     *
     * @param satellitesInput : The list of satellites to set up.
     * @return : The czml file builder with the given list of satellites.
     */
    public CzmlFileBuilder withSatellite(final List<Satellite> satellitesInput) {
        this.satellites.addAll(satellitesInput);
        return this;
    }


    // Constellation

    /**
     * Function to set up a constellation.
     *
     * @param constellationInput : The constellation to set up.
     * @return : The czml file builder with the given constellation.
     */
    public CzmlFileBuilder withConstellation(final Constellation constellationInput) {
        this.constellations.add(constellationInput);
        return this;
    }

    /**
     * Function to set up a list of constellations.
     *
     * @param constellationsInput : The list of constellations to set up.
     * @return : The czml file builder with the given list of constellations.
     */
    public CzmlFileBuilder withConstellation(final List<Constellation> constellationsInput) {
        this.constellations.addAll(constellationsInput);
        return this;
    }


    // Ground stations

    /**
     * Function to set up a ground station.
     *
     * @param station : The ground station to set up.
     * @return : The czml file builder with the given ground station.
     */
    public CzmlFileBuilder withCzmlGroundStation(final CzmlGroundStation station) {
        this.groundStations.add(station);
        return this;
    }

    /**
     * Function to set up a list of ground stations.
     *
     * @param stations : The list of ground stations to set up.
     * @return : The czml file builder with the given list of ground stations.
     */
    public CzmlFileBuilder withCzmlGroundStation(final List<CzmlGroundStation> stations) {
        this.groundStations.addAll(stations);
        return this;
    }


    // Attitude pointing

    /**
     * Function to set up an attitude pointing.
     *
     * @param attitudePointingInput : The attitude pointing to set up.
     * @return : The czml file builder with the given attitude pointing.
     */
    public CzmlFileBuilder withAttitudePointing(final AttitudePointing attitudePointingInput) {
        attitudePointings.add(attitudePointingInput);
        return this;
    }

    /**
     * Function to set up a list of attitude pointings.
     *
     * @param attitudePointingsInput : The list of attitude pointings to set up.
     * @return : The czml file builder with the given list of attitude pointings.
     */
    public CzmlFileBuilder withAttitudePointing(final List<AttitudePointing> attitudePointingsInput) {
        attitudePointings.addAll(attitudePointingsInput);
        return this;
    }


    // Covariance Display

    /**
     * Function to set up a covariance display.
     *
     * @param covarianceDisplayInput : The covariance display to set up.
     * @return : The czml file builder with the given covariance display.
     */
    public CzmlFileBuilder withCovarianceDisplay(final CovarianceDisplay covarianceDisplayInput) {
        this.covariances.add(covarianceDisplayInput);
        return this;
    }

    /**
     * Function to set up a list of covariances display.
     *
     * @param covariancesInput : The list of covariances display to set up.
     * @return : The czml file builder with the given list of covariances displays.
     */
    public CzmlFileBuilder withCovarianceDisplay(final List<CovarianceDisplay> covariancesInput) {
        this.covariances.addAll(covariancesInput);
        return this;
    }


    // Field Of Observation

    /**
     * Function to set up a field of observation.
     *
     * @param fieldOfObservationInput : The field of observation to set up.
     * @return : The czml file builder with the given field of observation.
     */
    public CzmlFileBuilder withFieldOfObservation(final FieldOfObservation fieldOfObservationInput) {
        this.fields.add(fieldOfObservationInput);
        return this;
    }

    /**
     * Function to set up a list of fields of observation.
     *
     * @param fieldOfObservationsInput : The list of fields of observation to set up.
     * @return : The czml file builder with the given list of fields of observation.
     */
    public CzmlFileBuilder withFieldOfObservation(final List<FieldOfObservation> fieldOfObservationsInput) {
        this.fields.addAll(fieldOfObservationsInput);
        return this;
    }


    // Line of visibility

    // Single Satellites/Single Station

    /**
     * Function to set up a line of visibility from a topocentric frame and a satellite with default parameters.
     *
     * @param topocentricFrameInput : The topocentric frame where the ground station must be.
     * @param satelliteInput        : The satellite that will be observed by the station.
     * @return : The czml file builder with the given line of visibility.
     */
    public CzmlFileBuilder withLineOfVisibility(final TopocentricFrame topocentricFrameInput,
                                                final Satellite satelliteInput) throws URISyntaxException, IOException {
        return this.withLineOfVisibility(topocentricFrameInput, satelliteInput, DEFAULT_ANGLE_OF_APERTURE);
    }

    /**
     * Function to set up a line of visibility from a topocentric frame and a satellite with a given angle of aperture.
     *
     * @param topocentricFrameInput : The topocentric frame where the ground station must be.
     * @param satelliteInput        : The satellite that will be observed by the station.
     * @param angleOfAperture       : The angle of aperture of the visibility of the station.
     * @return : The czml file builder with the given line of visibility.
     */
    public CzmlFileBuilder withLineOfVisibility(final TopocentricFrame topocentricFrameInput,
                                                final Satellite satelliteInput,
                                                final double angleOfAperture) throws URISyntaxException, IOException {
        final LineOfVisibility line = LineOfVisibility.builder(topocentricFrameInput, satelliteInput)
                                                      .withAngleOfAperture(angleOfAperture)
                                                      .build();
        lines.add(line);
        return this;
    }


    // Multiple Satellites/Single Station

    /**
     * Function to set up a line of visibility from a topocentric frame and a constellation with default parameters.
     *
     * @param topocentricFrameInput : The topocentric frame where the ground station must be.
     * @param constellationInput    : The constellation that will be observed by the station.
     * @return : The czml file builder with the given line of visibility.
     */
    public CzmlFileBuilder withLineOfVisibility(final TopocentricFrame topocentricFrameInput,
                                                final Constellation constellationInput) throws URISyntaxException, IOException {
        return this.withLineOfVisibility(topocentricFrameInput, constellationInput, DEFAULT_ANGLE_OF_APERTURE);
    }

    /**
     * Function to set up a line of visibility from a topocentric frame and a constellation with a given angle of aperture.
     *
     * @param topocentricFrameInput : The topocentric frame where the ground station must be.
     * @param constellationInput    : The constellation that will be observed by the station.
     * @param angleOfAperture       : The angle of aperture of the visibility of the station.
     * @return : The czml file builder with the given line of visibility.
     */
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


    // Single Satellites/Multiple Stations

    /**
     * Function to set up a line of visibility from a list of topocentric frames and a satellite with a given angle of aperture.
     *
     * @param topocentricFramesInput : The list of topocentric frames where the ground stations must be.
     * @param satelliteInput         : The satellite that will be observed by the stations.
     * @return : The czml file builder with the given line of visibility.
     */
    public CzmlFileBuilder withLineOfVisibility(final List<TopocentricFrame> topocentricFramesInput,
                                                final Satellite satelliteInput) throws URISyntaxException, IOException {
        return this.withLineOfVisibility(topocentricFramesInput, satelliteInput, DEFAULT_ANGLE_OF_APERTURE);
    }

    /**
     * Function to set up a line of visibility from a list of topocentric frames and a satellite with a given angle of aperture.
     *
     * @param topocentricFramesInput : The list of topocentric frames where the ground stations must be.
     * @param satelliteInput         : The satellite that will be observed by the stations.
     * @param angleOfAperture        : The angle of aperture of the visibility of the stations.
     * @return : The czml file builder with the given line of visibility.
     */
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


    // Multiple Satellites/Multiple Stations

    /**
     * Function to set up a line of visibility from a list of topocentric frames and a satellite with a given angle of aperture.
     *
     * @param topocentricFramesInput : The list of topocentric frames where the ground stations must be.
     * @param constellationInput     : The constellation that will be observed by the stations.
     * @return : The czml file builder with the given line of visibility.
     */
    public CzmlFileBuilder withLineOfVisibility(final List<TopocentricFrame> topocentricFramesInput,
                                                final Constellation constellationInput) throws URISyntaxException, IOException {
        return this.withLineOfVisibility(topocentricFramesInput, constellationInput, DEFAULT_ANGLE_OF_APERTURE);
    }

    /**
     * Function to set up a line of visibility from a list of topocentric frames and a satellite with a given angle of aperture.
     *
     * @param topocentricFramesInput : The list of topocentric frames where the ground stations must be.
     * @param constellationInput     : The constellation that will be observed by the stations.
     * @param angleOfAperture        : The angle of aperture of the visibility of the stations.
     * @return : The czml file builder with the given line of visibility.
     */
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


    // Inter-sat Visu

    /**
     * Function to set up an inter-sat visu.
     *
     * @param visuInput : The inter-sat visu to set up.
     * @return : The czml file builder with the given inter-sat visu.
     */
    public CzmlFileBuilder withInterSatVisu(final InterSatVisu visuInput) {
        this.visus.add(visuInput);
        return this;
    }

    /**
     * Function to set up a list of inter-sat visus.
     *
     * @param visuInputs : The list of inter-sat visus to set up.
     * @return : The czml file builder with the given list of inter-sat visus.
     */
    public CzmlFileBuilder withInterSatVisu(final List<InterSatVisu> visuInputs) {
        this.visus.addAll(visuInputs);
        return this;
    }


    // Collision display

    /**
     * Function to set up a collision display.
     *
     * @param collisionDisplayInput : The collision display to set up.
     * @return : The czml file builder with the given collision display.
     */
    public CzmlFileBuilder withCollisionDisplay(final CollisionDisplay collisionDisplayInput) {
        this.collisions.add(collisionDisplayInput);
        return this;
    }

    /**
     * Function to set up a list of collision displays.
     *
     * @param collisionDisplaysInput : The list of collision displays to set up.
     * @return : The czml file builder with the given list of collision displays.
     */
    public CzmlFileBuilder withCollisionDisplay(final List<CollisionDisplay> collisionDisplaysInput) {
        this.collisions.addAll(collisionDisplaysInput);
        return this;
    }


    // Maneuver sequence

    /**
     * Function to set up a maneuver sequence.
     *
     * @param maneuverSequenceInput : The maneuver sequence to set up.
     * @return : The czml file builder with the given maneuver sequence.
     */
    public CzmlFileBuilder withManeuverSequence(final ManeuverSequence maneuverSequenceInput) {
        this.maneuverSequences.add(maneuverSequenceInput);
        return this;
    }

    /**
     * Function to set up a list of maneuver sequences.
     *
     * @param maneuverSequencesInput : The list of maneuver sequences to set up.
     * @return : The czml file builder with the given list of maneuver sequences.
     */
    public CzmlFileBuilder withManeuverSequence(final List<ManeuverSequence> maneuverSequencesInput) {
        this.maneuverSequences.addAll(maneuverSequencesInput);
        return this;
    }


    // Ground Track

    /**
     * Function to set up a ground track.
     *
     * @param groundTrackInput : The ground track to set up.
     * @return : The czml file builder with the given ground track.
     */
    public CzmlFileBuilder withGroundTrack(final GroundTrack groundTrackInput) {
        this.groundTracks.add(groundTrackInput);
        return this;
    }

    /**
     * Function to set up a list of ground tracks.
     *
     * @param groundTracksInput : The list of ground tracks to set up.
     * @return : The czml file builder with the given list of ground tracks.
     */
    public CzmlFileBuilder withGroundTrack(final List<GroundTrack> groundTracksInput) {
        this.groundTracks.addAll(groundTracksInput);
        return this;
    }


    // Satellite Reference System

    /**
     * Function to set up a satellite reference system.
     *
     * @param systemInput : The satellite reference system to set up.
     * @return : The czml file builder with the given satellite reference system.
     */
    public CzmlFileBuilder withSatelliteReferenceSystem(final SatelliteReferenceSystem systemInput) {
        this.satelliteSystems.add(systemInput);
        return this;
    }

    /**
     * Function to set up a list of satellite reference systems.
     *
     * @param systemsInput : The list of satellite reference systems to set up.
     * @return : The czml file builder with the given list of satellite reference systems.
     */
    public CzmlFileBuilder withSatelliteReferenceSystem(final List<SatelliteReferenceSystem> systemsInput) {
        this.satelliteSystems.addAll(systemsInput);
        return this;
    }


    // Covered Surface On Body

    /**
     * Function to set up a covered surface on body.
     *
     * @param surfaceInput : The covered surface on body to set up.
     * @return : The czml file builder with the given covered surface on body.
     */
    public CzmlFileBuilder withCoveredSurfaceOnBody(final CoveredSurfaceOnBody surfaceInput) {
        this.surfaces.add(surfaceInput);
        return this;
    }

    /**
     * Function to set up a list of covered surfaces on body.
     *
     * @param surfacesInput : The list of covered surfaces on body to set up.
     * @return : The czml file builder with the given list of covered surfaces on body.
     */
    public CzmlFileBuilder withCoveredSurfaceOnBody(final List<CoveredSurfaceOnBody> surfacesInput) {
        this.surfaces.addAll(surfacesInput);
        return this;
    }


    // Lat Long Display

    /**
     * Function to set up a latitude longitude display.
     *
     * @param latlongInput : The latitude longitude display to set up.
     * @return : The czml file builder with the given latitude longitude display.
     */
    public CzmlFileBuilder withLatLongDisplay(final LatLongLinesDisplay latlongInput) {
        this.latLongs.add(latlongInput);
        return this;
    }

    /**
     * Function to set up a list of latitude longitude displays.
     *
     * @param latlongsInput : The list of latitude longitude displays to set up.
     * @return : The czml file builder with the given list of latitude longitude displays.
     */
    public CzmlFileBuilder withLatLongDisplay(final List<LatLongLinesDisplay> latlongsInput) {
        this.latLongs.addAll(latlongsInput);
        return this;
    }


    // Body Display

    /**
     * Function to set up a body display.
     *
     * @param bodyDisplay : The body display to set up.
     * @return : The czml file builder with the given body display.
     */
    public CzmlFileBuilder withBodyDisplay(final BodyDisplay bodyDisplay) {
        this.bodyDisplays.add(bodyDisplay);
        return this;
    }

    /**
     * Function to set up a list of body displays.
     *
     * @param bodyDisplay : The list of body display to set up.
     * @return : The czml file builder with the given list of body display.
     */
    public CzmlFileBuilder withBodyDisplay(final List<BodyDisplay> bodyDisplay) {
        this.bodyDisplays.addAll(bodyDisplay);
        return this;
    }


    // Central Body Reference System

    /**
     * Function to set up a central body reference system.
     *
     * @param systemInput : The central body reference system to set up.
     * @return : The czml file builder with the given central body reference system.
     */
    public CzmlFileBuilder withCentralBodyReferenceSystem(final CentralBodyReferenceSystem systemInput) {
        this.system = systemInput;
        return this;
    }

    /**
     * The build function that generates a czml file object.
     *
     * @return : A czml file object with the given parameters of the builder.
     */
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
        this.addCollisionDisplay(toReturn);
        this.addManeuverSequence(toReturn);
        this.addGroundTrack(toReturn);
        this.addSatelliteReferenceSystem(toReturn);
        this.addCoveredSurfaceOnBody(toReturn);
        this.addLatLongDisplay(toReturn);
        this.addBodyDisplay(toReturn);
        this.addCentralBodyReferenceSystem(toReturn);

        return toReturn;
    }


    // Private functions

    /**
     * This function adds the satellites to the czml file.
     *
     * @param file : The czml file that will be written.
     */
    private void addSatellites(final CzmlFile file) {
        for (Satellite satellite : satellites) {
            file.addObject(satellite);
        }
    }

    /**
     * This function adds the constellation to the czml file.
     *
     * @param file : The czml file that will be written.
     */
    private void addConstellations(final CzmlFile file) {
        for (Constellation constellation : constellations) {
            file.addObject(constellation);
        }
    }

    /**
     * This function adds the ground stations to the czml file.
     *
     * @param file : The czml file that will be written.
     */
    private void addGroundStations(final CzmlFile file) {
        for (CzmlGroundStation station : groundStations) {
            file.addObject(station);
        }
    }

    /**
     * This function adds the attitude pointings to the czml file.
     *
     * @param file : The czml file that will be written.
     */
    private void addAttitudePointings(final CzmlFile file) {
        for (AttitudePointing pointing : attitudePointings) {
            file.addObject(pointing);
        }
    }

    /**
     * This function adds the covariance displays to the czml file.
     *
     * @param file : The czml file that will be written.
     */
    private void addCovarianceDisplays(final CzmlFile file) {
        for (CovarianceDisplay covariance : covariances) {
            file.addObject(covariance);
        }
    }

    /**
     * This function adds the fields of observation to the czml file.
     *
     * @param file : The czml file that will be written.
     */
    private void addFieldOfObservation(final CzmlFile file) {
        for (FieldOfObservation field : fields) {
            file.addObject(field);
        }
    }

    /**
     * This function adds the lines of visibility to the czml file.
     *
     * @param file : The czml file that will be written.
     */
    private void addLineOfVisibility(final CzmlFile file) {
        for (LineOfVisibility line : lines) {
            file.addObject(line);
        }
    }

    /**
     * This function adds the inter-sat visus to the czml file.
     *
     * @param file : The czml file that will be written.
     */
    private void addInterSatVisu(final CzmlFile file) {
        for (InterSatVisu visu : visus) {
            file.addObject(visu);
        }
    }

    /**
     * This function adds the collision displays to the czml file.
     *
     * @param file : The czml file that will be written.
     */
    private void addCollisionDisplay(final CzmlFile file) {
        for (CollisionDisplay collision : collisions) {
            file.addObject(collision);
        }
    }

    /**
     * This function adds the maneuver sequences to the czml file.
     *
     * @param file : The czml file that will be written.
     */
    private void addManeuverSequence(final CzmlFile file) {
        for (ManeuverSequence maneuverSequence : maneuverSequences) {
            file.addObject(maneuverSequence);
        }
    }

    /**
     * This function adds the ground tracks to the czml file.
     *
     * @param file : The czml file that will be written.
     */
    private void addGroundTrack(final CzmlFile file) {
        for (GroundTrack groundTrack : groundTracks) {
            file.addObject(groundTrack);
        }
    }

    /**
     * This function adds the satellite reference systems to the czml file.
     *
     * @param file : The czml file that will be written.
     */
    private void addSatelliteReferenceSystem(final CzmlFile file) {
        for (SatelliteReferenceSystem systemInput : satelliteSystems) {
            file.addObject(systemInput);
        }
    }

    /**
     * This function adds the covered surface on body to the czml file.
     *
     * @param file : The czml file that will be written.
     */
    private void addCoveredSurfaceOnBody(final CzmlFile file) {
        for (CoveredSurfaceOnBody surface : surfaces) {
            file.addObject(surface);
        }
    }

    /**
     * This function adds the latitude longitude displays to the czml file.
     *
     * @param file : The czml file that will be written.
     */
    private void addLatLongDisplay(final CzmlFile file) {
        for (LatLongLinesDisplay latLong : latLongs) {
            file.addObject(latLong);
        }
    }

    /**
     * This function adds the body displays to the czml file.
     *
     * @param file : The czml file that will be written.
     */
    private void addBodyDisplay(final CzmlFile file) {
        for (BodyDisplay bodyDisplay : bodyDisplays) {
            file.addObject(bodyDisplay);
        }
    }

    /**
     * This function adds the central body reference system to the czml file.
     *
     * @param file : The czml file that will be written.
     */
    private void addCentralBodyReferenceSystem(final CzmlFile file) {
        if (system != null) {
            file.addObject(system);
        }
    }
}
