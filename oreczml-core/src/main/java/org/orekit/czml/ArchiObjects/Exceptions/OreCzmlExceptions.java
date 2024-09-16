package org.orekit.czml.ArchiObjects.Exceptions;


public class OreCzmlExceptions extends RuntimeException {

    // Czml ground station builder
    public static final String MULTIPLE_MODEL_SINGLE_STATION = "Can't apply several models to one ground station, use the .withModel(String) instead";

    // Czml ground station
    public static final String SINGLE_STATION_GET_MULTIPLE_GROUND_STATION = "The ground station was not build with several topocentric frames, only one.";

    // Body Display
    public static final String CANT_DISPLAY_PERIOD_NO_ORBIT = "The orbit is not displayed, do not use noOrbitDisplay() then setup the period display of the orbit.";

    // Czml Model
    public static final String MODEL_EXTENSION_UNKNOWN = "The extension of the file for the model is not supported";
    public static final String MODEL_TYPE_UNKNOWN      = "Model Type not known";

    // Attitude Pointing
    public static final String POINTING_PATH_NOT_SHOWN        = "The pointing path is not displayed yet, use displayPointingPath first";
    public static final String PERIOD_POINTING_PATH_NOT_SHOWN = "First use the .displayPeriodPointingPath() to set the period pointing path.";

    // Czml File
    public static final String HEADER_ALONE = "No objects other than the header have been written.";
    public static final String NO_HEADER    = "No header was defined in the primary objects.";

    // Ground track
    public static final String EMPTY_GROUND_TRACKS = "The ground tracks are empty, either the file is already written or the ground track is not build with a constellation";

    // Satellite
    public static final String NO_ORIENTATION_DISPLAYED = "The satellite did not display the orientation, please use the displaySatelliteAttitude() method first";

    // Visibility cone
    public static final String NO_SAT_VISIBILITY_CONE = "The Visibility cone was not defined with a given satellite";

    // Orientation
    public static final String MULTIPLE_ATTITUDES_SINGLE_GET = "Attitudes were created in multiples, can't return a single attitude, use getAttitudes instead.";
    public static final String SINGLE_ATTITUDE_MULTIPLE_GET  = "Only one Attitude is built, can't return multiple attitudes, use getSingleAttitude instead.";

    // Polyline
    public static final String MORE_THAN_2_CARTESIAN_POLYLINE = "The size of the cartesian positions inputted in the Polyline must be 2";
    public static final String NO_REFERENCES_POLYLINE         = "The polyline was not defined with references, so it cannot be written that way.";

    // Position
    public static final String EMPTY_POSITION_HEIGHT = "Height is not defined";
    public static final String EMPTY_X               = "x is not defined";
    public static final String EMPTY_Y               = "y is not defined";
    public static final String EMPTY_Z               = "z is not defined";
    public static final String EMPTY_VX              = "vx is not defined";
    public static final String EMPTY_VY              = "vy is not defined";
    public static final String EMPTY_VZ              = "vz is not defined";
    public static final String POSITION_TYPE_UNKNOWN = "Position Type is not defined";
    public static final String EMPTY_FRAME           = "No reference frame defined";

    // Long lat lines
    public static final String DEFAULT_ERROR_LATITUDE         = "Latitude is not defined";
    public static final String DEFAULT_ERROR_LONGITUDE        = "Longitude is not defined";
    public static final String GREATER_ANGULAR_LATITUDE_STEP  = "The angular step for the latitude can't be greater than 180";
    public static final String GREATER_ANGULAR_LONGITUDE_STEP = "The angular step for the longitude can't be greater than 360";

}
