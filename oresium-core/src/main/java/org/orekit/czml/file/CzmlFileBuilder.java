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
package org.orekit.czml.file;

import org.orekit.czml.object.primary.GroundTrack;
import org.orekit.czml.object.primary.Header;
import org.orekit.czml.object.primary.ManeuverSequence;
import org.orekit.czml.object.primary.covariance.Collision;
import org.orekit.czml.object.primary.covariance.Covariance;
import org.orekit.czml.object.primary.entities.Body;
import org.orekit.czml.object.primary.entities.Constellation;
import org.orekit.czml.object.primary.entities.CzmlGroundStation;
import org.orekit.czml.object.primary.entities.InfluenceSphere;
import org.orekit.czml.object.primary.entities.Spacecraft;
import org.orekit.czml.object.primary.pointing.AttitudePointing;
import org.orekit.czml.object.primary.pointing.CoveredSurfaceOnBody;
import org.orekit.czml.object.primary.systems.CentralBodyReferenceSystem;
import org.orekit.czml.object.primary.systems.LatLongLines;
import org.orekit.czml.object.primary.systems.SpacecraftReferenceSystem;
import org.orekit.czml.object.primary.visu.FieldOfObservation;
import org.orekit.czml.object.primary.visu.InterSatVisu;
import org.orekit.czml.object.primary.visu.LineOfVisibility;
import org.orekit.czml.object.primary.visu.MultipleLineOfVisibility;
import org.orekit.czml.object.primary.visu.StationVisibilityCircle;
import org.orekit.czml.object.primary.visu.VisibilityTriangle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Czml File Builder
 * <p>
 * Builder and architect for the {@link CzmlFile} class. This builder organise
 * and manage all the objects that needs to be added to a Czml file.
 *
 * @author Julien LEBLOND.
 * @since 1.0.0
 */
public class CzmlFileBuilder {

    /** The default angle of aperture of a ground station. */
    public static final double DEFAULT_ANGLE_OF_APERTURE = 80.0;

    /** List of all the satellites to write. */
    private List<Spacecraft> satellites = new ArrayList<>();

    /** List of all the constellations to write. */
    private List<Constellation> constellations = new ArrayList<>();

    /** List of all the czml ground stations to write. */
    private List<CzmlGroundStation> groundStations = new ArrayList<>();

    /** List of all the influence sphere. */
    private List<InfluenceSphere> influenceSpheres = new ArrayList<>();

    /** List of all the inter-sat visu to write. */
    private List<InterSatVisu> interSatVisus = new ArrayList<>();

    /** List of all the collision to write. */
    private List<Collision> collisions = new ArrayList<>();

    /** List of all the lines of visibility to write. */
    private List<LineOfVisibility> lines = new ArrayList<>();

    /** List of all the multiple line of visibility object to write. */
    private List<MultipleLineOfVisibility> multipleLineVisibilities =
        new ArrayList<>();

    /** List of all the Station visibility circle. */
    private List<StationVisibilityCircle> visibilityCircles = new ArrayList<>();

    /** List of all the visibility triangles. */
    private List<VisibilityTriangle> visibilityTriangles = new ArrayList<>();

    /** List of all the ground tracks to write. */
    private List<GroundTrack> groundTracks = new ArrayList<>();

    /** List of all the attitude pointings to write. */
    private List<AttitudePointing> attitudePointings = new ArrayList<>();

    /** List of all the covariance display to write. */
    private List<Covariance> covariances = new ArrayList<>();

    /** List of all the fields of observation to write. */
    private List<FieldOfObservation> fieldOfObservations = new ArrayList<>();

    /** List of all the maneuver sequences to write. */
    private List<ManeuverSequence> maneuverSequences = new ArrayList<>();

    /** List of all the satellite reference systems to write. */
    private List<SpacecraftReferenceSystem> satelliteSystems =
        new ArrayList<>();

    /** List of all the covered surfaces on body to write. */
    private List<CoveredSurfaceOnBody> surfaces = new ArrayList<>();

    /** List of all the latitude longitude lines display to write. */
    private List<LatLongLines> latLongs = new ArrayList<>();

    /** List of all the body to write. */
    private List<Body> bodies = new ArrayList<>();

    /** The header of the czml file. */
    private Header header;

    /** The central body reference system to write. */
    private CentralBodyReferenceSystem system;

    // Constructor

    /**
     * The constructor of the czml file builder object.
     *
     * @param headerInput : The header considered.
     */
    @SuppressWarnings("squid:S2384")
    public CzmlFileBuilder(final Header headerInput) {
        this.header = headerInput;
    }

    // Satellite

    /**
     * Function to set up a satellite.
     *
     * @param satelliteInput : The satellite to set up.
     * @return : The czml file builder with the given satellite.
     */
    public CzmlFileBuilder withSpacecraft(final Spacecraft... satelliteInput) {
        this.satellites.addAll(Arrays.asList(satelliteInput));
        return this;
    }

    /**
     * Function to set up a list of satellites.
     *
     * @param satellitesInput : The list of satellites to set up.
     * @return : The czml file builder with the given list of satellites.
     */
    public CzmlFileBuilder
        withSpacecraft(final List<Spacecraft> satellitesInput) {
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
    public CzmlFileBuilder
        withConstellation(final Constellation... constellationInput) {
        this.constellations.addAll(Arrays.asList(constellationInput));
        return this;
    }

    /**
     * Function to set up a list of constellations.
     *
     * @param constellationsInput : The list of constellations to set up.
     * @return : The czml file builder with the given list of constellations.
     */
    public CzmlFileBuilder
        withConstellation(final List<Constellation> constellationsInput) {
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
    public CzmlFileBuilder
        withCzmlGroundStation(final CzmlGroundStation... station) {
        this.groundStations.addAll(Arrays.asList(station));
        return this;
    }

    /**
     * Function to set up a list of ground stations.
     *
     * @param stations : The list of ground stations to set up.
     * @return : The czml file builder with the given list of ground stations.
     */
    public CzmlFileBuilder
        withCzmlGroundStation(final List<CzmlGroundStation> stations) {
        this.groundStations.addAll(stations);
        return this;
    }

    // Influence sphere

    /**
     * Function to set up an influence sphere.
     *
     * @param influenceSphere : The influence sphere to set up.
     * @return : The czml file builder with the given influence sphere.
     */
    public CzmlFileBuilder
        withInfluenceSphere(final InfluenceSphere... influenceSphere) {
        this.influenceSpheres.addAll(Arrays.asList(influenceSphere));
        return this;
    }

    /**
     * Function to set up a list of influence sphere.
     *
     * @param influenceSpheresInput : The list of influence sphere to set up.
     * @return : The czml file builder with the given list of influence spheres.
     */
    public CzmlFileBuilder
        withInfluenceSphere(final List<InfluenceSphere> influenceSpheresInput) {
        this.influenceSpheres.addAll(influenceSpheresInput);
        return this;
    }

    // Attitude pointing

    /**
     * Function to set up an attitude pointing.
     *
     * @param attitudePointingInput : The attitude pointing to set up.
     * @return : The czml file builder with the given attitude pointing.
     */
    public CzmlFileBuilder
        withAttitudePointing(final AttitudePointing... attitudePointingInput) {
        attitudePointings.addAll(Arrays.asList(attitudePointingInput));
        return this;
    }

    /**
     * Function to set up a list of attitude pointings.
     *
     * @param attitudePointingsInput : The list of attitude pointings to set up.
     * @return : The czml file builder with the given list of attitude
     *         pointings.
     */
    public CzmlFileBuilder
        withAttitudePointing(final List<AttitudePointing> attitudePointingsInput) {
        attitudePointings.addAll(attitudePointingsInput);
        return this;
    }

    // Covariance Display

    /**
     * Function to set up a covariance display.
     *
     * @param covarianceInput : The covariance display to set up.
     * @return : The czml file builder with the given covariance display.
     */
    public CzmlFileBuilder withCovariance(final Covariance... covarianceInput) {
        this.covariances.addAll(Arrays.asList(covarianceInput));
        return this;
    }

    /**
     * Function to set up a list of covariances display.
     *
     * @param covariancesInput : The list of covariances display to set up.
     * @return : The czml file builder with the given list of covariances
     *         displays.
     */
    public CzmlFileBuilder
        withCovariance(final List<Covariance> covariancesInput) {
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
    public CzmlFileBuilder
        withFieldOfObservation(final FieldOfObservation... fieldOfObservationInput) {
        this.fieldOfObservations.addAll(Arrays.asList(fieldOfObservationInput));
        return this;
    }

    /**
     * Function to set up a list of fields of observation.
     *
     * @param fieldOfObservationsInput : The list of fields of observation to
     *        set up.
     * @return : The czml file builder with the given list of fields of
     *         observation.
     */
    public CzmlFileBuilder
        withFieldOfObservation(final List<FieldOfObservation> fieldOfObservationsInput) {
        this.fieldOfObservations.addAll(fieldOfObservationsInput);
        return this;
    }

    // Line of visibility

    /**
     * Function to set up a line of visibility.
     *
     * @param lineOfVisibility : The line of visibility to set up.
     * @return : The czml file builder with the given line of visibility.
     */
    public CzmlFileBuilder
        withLineOfVisibility(final LineOfVisibility... lineOfVisibility) {
        this.lines.addAll(Arrays.asList(lineOfVisibility));
        return this;
    }

    /**
     * Function to set up a list of lines of visibility.
     *
     * @param linesOfVisibility : The list of line of visibility to set up.
     * @return : The czml file builder with the given list of lines of
     *         visibility.
     */
    public CzmlFileBuilder
        withLineOfVisibility(final List<LineOfVisibility> linesOfVisibility) {
        this.lines.addAll(linesOfVisibility);
        return this;
    }

    // Multiple line of visibility

    /**
     * Function to set up a multiple line of visibility object.
     *
     * @param multipleLineOfVisibilityInput : The list of multiple line of
     *        visibility objects to set up.
     * @return : The czml file builder with the given multiple lines of
     *         visibility object
     */
    public CzmlFileBuilder
        withMultipleLineOfVisibility(final MultipleLineOfVisibility... multipleLineOfVisibilityInput) {
        this.multipleLineVisibilities
            .addAll(Arrays.asList(multipleLineOfVisibilityInput));
        return this;
    }

    /**
     * Function to set up a list of multiple lines of visibility objects.
     *
     * @param multipleLineOfVisibilityInput : The list of line of visibility to
     *        set up.
     * @return : The czml file builder with the given list of multiple lines of
     *         visibility objects.
     */
    public CzmlFileBuilder
        withMultipleLineOfVisibility(final List<MultipleLineOfVisibility> multipleLineOfVisibilityInput) {
        this.multipleLineVisibilities.addAll(multipleLineOfVisibilityInput);
        return this;
    }

    // Visibility circles

    /**
     * Function to set up a visibility circle.
     *
     * @param stationVisibilityCircle : The visibility circle to set up.
     * @return : The czml file builder with the given visibility circle.
     */
    public CzmlFileBuilder
        withVisibilityCircle(final StationVisibilityCircle... stationVisibilityCircle) {
        this.visibilityCircles.addAll(Arrays.asList(stationVisibilityCircle));
        return this;
    }

    /**
     * Function to set up a list of visibility circles.
     *
     * @param stationVisibilityCircles : The list of visibility circle to set
     *        up.
     * @return : The czml file builder with the given list of visibility
     *         circles.
     */
    public CzmlFileBuilder
        withVisibilityCircle(final List<StationVisibilityCircle> stationVisibilityCircles) {
        this.visibilityCircles.addAll(stationVisibilityCircles);
        return this;
    }

    // Visibility triangle

    /**
     * Function to set up a visibility triangle.
     *
     * @param visibilityTriangle : The visibility triangle to set up.
     * @return : The czml file builder with the given visibility triangle.
     */
    public CzmlFileBuilder
        withVisibilityTriangle(final VisibilityTriangle... visibilityTriangle) {
        this.visibilityTriangles.addAll(Arrays.asList(visibilityTriangle));
        return this;
    }

    /**
     * Function to set up a list of visibility triangles.
     *
     * @param visibilityTrianglesInput : The list of visibility triangles to set
     *        up.
     * @return : The czml file builder with the given list of visibility
     *         triangles.
     */
    public CzmlFileBuilder
        withVisibilityTriangle(final List<VisibilityTriangle> visibilityTrianglesInput) {
        this.visibilityTriangles.addAll(visibilityTrianglesInput);
        return this;
    }

    // Inter-sat Visu

    /**
     * Function to set up an inter-sat visu.
     *
     * @param visuInput : The inter-sat visu to set up.
     * @return : The czml file builder with the given inter-sat visu.
     */
    public CzmlFileBuilder withInterSatVisu(final InterSatVisu... visuInput) {
        this.interSatVisus.addAll(Arrays.asList(visuInput));
        return this;
    }

    /**
     * Function to set up a list of inter-sat visus.
     *
     * @param visuInputs : The list of inter-sat visus to set up.
     * @return : The czml file builder with the given list of inter-sat visus.
     */
    public CzmlFileBuilder
        withInterSatVisu(final List<InterSatVisu> visuInputs) {
        this.interSatVisus.addAll(visuInputs);
        return this;
    }

    // collision

    /**
     * Function to set up a collision.
     *
     * @param collisionInput : The collision to set up.
     * @return : The czml file builder with the given collision.
     */
    public CzmlFileBuilder withCollision(final Collision... collisionInput) {
        this.collisions.addAll(Arrays.asList(collisionInput));
        return this;
    }

    /**
     * Function to set up a list of collisions.
     *
     * @param collisionDisplaysInput : The list of collisions to set up.
     * @return : The czml file builder with the given list of collisions.
     */
    public CzmlFileBuilder
        withCollision(final List<Collision> collisionDisplaysInput) {
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
    public CzmlFileBuilder
        withManeuverSequence(final ManeuverSequence... maneuverSequenceInput) {
        this.maneuverSequences.addAll(Arrays.asList(maneuverSequenceInput));
        return this;
    }

    /**
     * Function to set up a list of maneuver sequences.
     *
     * @param maneuverSequencesInput : The list of maneuver sequences to set up.
     * @return : The czml file builder with the given list of maneuver
     *         sequences.
     */
    public CzmlFileBuilder
        withManeuverSequence(final List<ManeuverSequence> maneuverSequencesInput) {
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
    public CzmlFileBuilder
        withGroundTrack(final GroundTrack... groundTrackInput) {
        this.groundTracks.addAll(Arrays.asList(groundTrackInput));
        return this;
    }

    /**
     * Function to set up a list of ground tracks.
     *
     * @param groundTracksInput : The list of ground tracks to set up.
     * @return : The czml file builder with the given list of ground tracks.
     */
    public CzmlFileBuilder
        withGroundTrack(final List<GroundTrack> groundTracksInput) {
        this.groundTracks.addAll(groundTracksInput);
        return this;
    }

    // Satellite Reference System

    /**
     * Function to set up a satellite reference system.
     *
     * @param systemInput : The satellite reference system to set up.
     * @return : The czml file builder with the given satellite reference
     *         system.
     */
    public CzmlFileBuilder
        withSatelliteReferenceSystem(final SpacecraftReferenceSystem... systemInput) {
        this.satelliteSystems.addAll(Arrays.asList(systemInput));
        return this;
    }

    /**
     * Function to set up a list of satellite reference systems.
     *
     * @param systemsInput : The list of satellite reference systems to set up.
     * @return : The czml file builder with the given list of satellite
     *         reference systems.
     */
    public CzmlFileBuilder
        withSatelliteReferenceSystem(final List<SpacecraftReferenceSystem> systemsInput) {
        this.satelliteSystems.addAll(systemsInput);
        return this;
    }

    // Covered Surface On Body

    /**
     * Function to set up a covered surface on body.
     *
     * @param surfaceInput : The covered surface on the body to set up.
     * @return : The czml file builder with the given covered surface on the
     *         body.
     */
    public CzmlFileBuilder
        withCoveredSurfaceOnBody(final CoveredSurfaceOnBody... surfaceInput) {
        this.surfaces.addAll(Arrays.asList(surfaceInput));
        return this;
    }

    /**
     * Function to set up a list of covered surfaces on the body.
     *
     * @param surfacesInput : The list of covered surfaces on the body to set
     *        up.
     * @return : The czml file builder with the given list of covered surfaces
     *         on body.
     */
    public CzmlFileBuilder
        withCoveredSurfaceOnBody(final List<CoveredSurfaceOnBody> surfacesInput) {
        this.surfaces.addAll(surfacesInput);
        return this;
    }

    // Lat Long Display

    /**
     * Function to set up a latitude longitude display.
     *
     * @param latLongInput : The latitude longitude display to set up.
     * @return : The czml file builder with the given latitude longitude
     *         display.
     */
    public CzmlFileBuilder withLatLong(final LatLongLines... latLongInput) {
        this.latLongs.addAll(Arrays.asList(latLongInput));
        return this;
    }

    /**
     * Function to set up a list of latitude longitude displays.
     *
     * @param latLongsInput : The list of latitude longitude displays to set up.
     * @return : The czml file builder with the given list of latitude longitude
     *         displays.
     */
    public CzmlFileBuilder withLatLong(final List<LatLongLines> latLongsInput) {
        this.latLongs.addAll(latLongsInput);
        return this;
    }

    // body

    /**
     * Function to set up a body.
     *
     * @param body : The body to set up.
     * @return : The czml file builder with the given body.
     */
    public CzmlFileBuilder withBody(final Body... body) {
        this.bodies.addAll(Arrays.asList(body));
        return this;
    }

    /**
     * Function to set up a list of bodys.
     *
     * @param body : The list of body to set up.
     * @return : The czml file builder with the given list of body.
     */
    public CzmlFileBuilder withBody(final List<Body> body) {
        this.bodies.addAll(body);
        return this;
    }

    // Central Body Reference System

    /**
     * Function to set up a central body reference system.
     *
     * @param systemInput : The central body reference system to set up.
     * @return : The czml file builder with the given central body reference
     *         system.
     */
    public CzmlFileBuilder
        withCentralBodyReferenceSystem(final CentralBodyReferenceSystem systemInput) {
        this.system = systemInput;
        return this;
    }

    /**
     * The build function that generates a czml file object.
     *
     * @return : A czml file object with the given parameters of the builder.
     */
    public CzmlFile build() {
        final CzmlFile toReturn = new CzmlFile();
        // Add the header
        toReturn.addObject(header);

        addSatellites(toReturn);
        addConstellations(toReturn);
        addGroundStations(toReturn);
        addInfluenceSpheres(toReturn);
        addAttitudePointings(toReturn);
        addCovarianceDisplays(toReturn);
        addFieldOfObservation(toReturn);
        addLineOfVisibility(toReturn);
        addMultipleLineOfVisibility(toReturn);
        addVisibilityCircles(toReturn);
        addVisibilityTriangles(toReturn);
        addInterSatVisu(toReturn);
        addCollisionDisplay(toReturn);
        addManeuverSequence(toReturn);
        addGroundTrack(toReturn);
        addSatelliteReferenceSystem(toReturn);
        addCoveredSurfaceOnBody(toReturn);
        addLatLongDisplay(toReturn);
        addBodyDisplay(toReturn);
        addCentralBodyReferenceSystem(toReturn);

        clear();

        return toReturn;
    }

    // Private functions

    /**
     * This function adds the satellites to the czml file.
     *
     * @param file : The czml file that will be written.
     */
    private void addSatellites(final CzmlFile file) {
        for (final Spacecraft satellite : satellites) {
            file.addObject(satellite);
        }
    }

    /**
     * This function adds the constellation to the czml file.
     *
     * @param file : The czml file that will be written.
     */
    private void addConstellations(final CzmlFile file) {
        for (final Constellation constellation : constellations) {
            file.addObject(constellation);
        }
    }

    /**
     * This function adds the ground stations to the czml file.
     *
     * @param file : The czml file that will be written.
     */
    private void addGroundStations(final CzmlFile file) {
        for (final CzmlGroundStation station : groundStations) {
            file.addObject(station);
        }
    }

    /**
     * This function adds the influence sphere to the czml file.
     *
     * @param file : The czml file that will be written
     */
    private void addInfluenceSpheres(final CzmlFile file) {
        for (final InfluenceSphere sphere : influenceSpheres) {
            file.addObject(sphere);
        }
    }

    /**
     * This function adds the attitude pointings to the czml file.
     *
     * @param file : The czml file that will be written.
     */
    private void addAttitudePointings(final CzmlFile file) {
        for (final AttitudePointing pointing : attitudePointings) {
            file.addObject(pointing);
        }
    }

    /**
     * This function adds the covariance displays to the czml file.
     *
     * @param file : The czml file that will be written.
     */
    private void addCovarianceDisplays(final CzmlFile file) {
        for (final Covariance covariance : covariances) {
            file.addObject(covariance);
        }
    }

    /**
     * This function adds the fields of observation to the czml file.
     *
     * @param file : The czml file that will be written.
     */
    private void addFieldOfObservation(final CzmlFile file) {
        for (final FieldOfObservation field : fieldOfObservations) {
            file.addObject(field);
        }
    }

    /**
     * This function adds the lines of visibility to the czml file.
     *
     * @param file : The czml file that will be written.
     */
    private void addLineOfVisibility(final CzmlFile file) {
        for (final LineOfVisibility line : lines) {
            file.addObject(line);
        }
    }

    /**
     * This function adds the multiple lines of visibility to the czml file.
     *
     * @param file : The czml file that will be written.
     */
    private void addMultipleLineOfVisibility(final CzmlFile file) {
        for (final MultipleLineOfVisibility multipleLineOfVisibility : multipleLineVisibilities) {
            file.addObject(multipleLineOfVisibility);
        }
    }

    /**
     * This function adds the visibility circles to the czml file.
     *
     * @param file : The czml file that will be written.
     */
    private void addVisibilityCircles(final CzmlFile file) {
        for (final StationVisibilityCircle circle : visibilityCircles) {
            file.addObject(circle);
        }
    }

    /**
     * This function adds the visibility triangles to the czml file.
     *
     * @param file : The czml file that will be written.
     */
    private void addVisibilityTriangles(final CzmlFile file) {
        for (final VisibilityTriangle triangle : visibilityTriangles) {
            file.addObject(triangle);
        }
    }

    /**
     * This function adds the inter-sat visus to the czml file.
     *
     * @param file : The czml file that will be written.
     */
    private void addInterSatVisu(final CzmlFile file) {
        for (final InterSatVisu visu : interSatVisus) {
            file.addObject(visu);
        }
    }

    /**
     * This function adds the collisions to the czml file.
     *
     * @param file : The czml file that will be written.
     */
    private void addCollisionDisplay(final CzmlFile file) {
        for (final Collision collision : collisions) {
            file.addObject(collision);
        }
    }

    /**
     * This function adds the maneuver sequences to the czml file.
     *
     * @param file : The czml file that will be written.
     */
    private void addManeuverSequence(final CzmlFile file) {
        for (final ManeuverSequence maneuverSequence : maneuverSequences) {
            file.addObject(maneuverSequence);
        }
    }

    /**
     * This function adds the ground tracks to the czml file.
     *
     * @param file : The czml file that will be written.
     */
    private void addGroundTrack(final CzmlFile file) {
        for (final GroundTrack groundTrack : groundTracks) {
            file.addObject(groundTrack);
        }
    }

    /**
     * This function adds the satellite reference systems to the czml file.
     *
     * @param file : The czml file that will be written.
     */
    private void addSatelliteReferenceSystem(final CzmlFile file) {
        for (final SpacecraftReferenceSystem systemInput : satelliteSystems) {
            file.addObject(systemInput);
        }
    }

    /**
     * This function adds the covered surface on body to the czml file.
     *
     * @param file : The czml file that will be written.
     */
    private void addCoveredSurfaceOnBody(final CzmlFile file) {
        for (final CoveredSurfaceOnBody surface : surfaces) {
            file.addObject(surface);
        }
    }

    /**
     * This function adds the latitude longitude displays to the czml file.
     *
     * @param file : The czml file that will be written.
     */
    private void addLatLongDisplay(final CzmlFile file) {
        for (final LatLongLines latLong : latLongs) {
            file.addObject(latLong);
        }
    }

    /**
     * This function adds the bodys to the czml file.
     *
     * @param file : The czml file that will be written.
     */
    private void addBodyDisplay(final CzmlFile file) {
        for (final Body body : bodies) {
            file.addObject(body);
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

    private void clear() {
        this.header = null;
        this.satellites = new ArrayList<>();
        this.constellations = new ArrayList<>();
        this.groundStations = new ArrayList<>();
        this.interSatVisus = new ArrayList<>();
        this.collisions = new ArrayList<>();
        this.lines = new ArrayList<>();
        this.multipleLineVisibilities = new ArrayList<>();
        this.groundTracks = new ArrayList<>();
        this.attitudePointings = new ArrayList<>();
        this.covariances = new ArrayList<>();
        this.fieldOfObservations = new ArrayList<>();
        this.maneuverSequences = new ArrayList<>();
        this.satelliteSystems = new ArrayList<>();
        this.surfaces = new ArrayList<>();
        this.latLongs = new ArrayList<>();
        this.bodies = new ArrayList<>();
        this.visibilityTriangles = new ArrayList<>();
        this.visibilityCircles = new ArrayList<>();
        this.influenceSpheres = new ArrayList<>();
        this.system = null;
    }
}
