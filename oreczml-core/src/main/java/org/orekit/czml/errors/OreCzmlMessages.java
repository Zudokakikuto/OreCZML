package org.orekit.czml.errors;

import org.hipparchus.exception.Localizable;

import java.util.Locale;


public enum OreCzmlMessages implements Localizable {

    // Czml ground station builder
    MULTIPLE_MODEL_SINGLE_STATION(
            "Can't apply several models to one ground station, use the .withModel(String) instead"),

    // Czml ground station
    SINGLE_STATION_GET_MULTIPLE_GROUND_STATION(
            "The ground station was not build with several topocentric frames, only one."),

    // Body Display
    CANT_DISPLAY_PERIOD_NO_ORBIT(
            "The orbit is not displayed, do not use noOrbitDisplay() then setup the period display of the orbit."),

    // Czml Model
    MODEL_EXTENSION_UNKNOWN("The extension of the file for the model is not supported"),

    MODEL_TYPE_UNKNOWN("Model Type not known"),

    // Attitude Pointing
    POINTING_PATH_NOT_SHOWN("The pointing path is not displayed yet, use displayPointingPath first"),

    PERIOD_POINTING_PATH_NOT_SHOWN("First use the .displayPeriodPointingPath() to set the period pointing path."),

    // Czml File
    HEADER_ALONE("No objects other than the header have been written."),

    NO_HEADER("No header was defined in the primary objects."),

    // Ground track
    EMPTY_GROUND_TRACKS(
            "The ground tracks are empty, either the file is already written or the ground track is not build with a constellation"),

    // Satellite
    NO_ORIENTATION_DISPLAYED(
            "The satellite did not display the orientation, please use the displaySatelliteAttitude() method first"),

    // Visibility cone
    NO_SAT_VISIBILITY_CONE("The Visibility cone was not defined with a given satellite"),

    // Orientation
    MULTIPLE_ATTITUDES_SINGLE_GET(
            "Attitudes were created in multiples, can't return a single attitude, use getAttitudes instead."),

    SINGLE_ATTITUDE_MULTIPLE_GET(
            "Only one Attitude is built, can't return multiple attitudes, use getSingleAttitude instead."),

    // Polyline
    DEFAULT_CANT_CALL("Can't call a vector function on a non-vector polyline"),

    MORE_THAN_2_CARTESIAN_POLYLINE("The size of the cartesian positions inputted in the Polyline must be 2"),

    NO_REFERENCES_POLYLINE("The polyline was not defined with references, so it cannot be written that way."),

    // Position
    EMPTY_POSITION_HEIGHT("Height is not defined"),

    EMPTY_X("x is not defined"),

    EMPTY_Y("y is not defined"),

    EMPTY_Z("z is not defined"),

    EMPTY_VX("vx is not defined"),

    EMPTY_VY("vy is not defined"),

    EMPTY_VZ("vz is not defined"),

    POSITION_TYPE_UNKNOWN("Position Type is not defined"),

    EMPTY_FRAME("No reference frame defined"),

    // Long lat lines
    DEFAULT_ERROR_LATITUDE("Latitude is not defined"),

    DEFAULT_ERROR_LONGITUDE("Longitude is not defined"),

    GREATER_ANGULAR_LATITUDE_STEP("The angular step for the latitude can't be greater than 180"),

    GREATER_ANGULAR_LONGITUDE_STEP("The angular step for the longitude can't be greater than 360");

    private final String sourceFormat;

    OreCzmlMessages(final String s) {
        this.sourceFormat = s;
    }

    @Override
    public String getSourceString() {
        return "";
    }

    @Override
    public String getLocalizedString(final Locale locale) {
        return "";
    }
}
