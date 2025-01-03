package org.orekit.czml.error;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.orekit.czml.errors.OreCzmlMessages;
import org.orekit.czml.file.AbstractTest;

/**
 * OreCzml Messages tests.
 */
public class OreCzmlMessagesTest extends AbstractTest {

    /**
     * OreCzml messages test.
     */
    @Test
    void OreCzmlMessagesTests() {

        loadOrekitData();

        // Collision
        Assertions.assertEquals("Satellite are not close enough to build a collision object.",
                OreCzmlMessages.NOT_CLOSE_ENOUGH.getSourceString());

        // Constellation
        Assertions.assertEquals(
                "The number of models loaded is not the same as the number of satellites of the constellation.",
                OreCzmlMessages.NOT_SAME_NUMBER_SAT_MODELS.getSourceString());

        // Czml Ground Station Builder
        Assertions.assertEquals(
                "Can't apply none or several models to one ground station, use the .withModel(String) instead.",
                OreCzmlMessages.MULTIPLE_MODEL_SINGLE_STATION.getSourceString());

        // Czml Ground Station
        Assertions.assertEquals("The ground station was not build with several topocentric frames, only one.",
                OreCzmlMessages.SINGLE_STATION_GET_MULTIPLE_GROUND_STATION.getSourceString());
        Assertions.assertEquals(
                "Several ground stations were build, please use the appropriate getter (usually ends with an \"s\" for plural).",
                OreCzmlMessages.SEVERAL_STATION_UNIQUE_GET.getSourceString());

        // Body
        Assertions.assertEquals(
                "The orbit is not displayed, do not use noOrbitDisplay() then setup the period display of the orbit.",
                OreCzmlMessages.CANT_DISPLAY_PERIOD_NO_ORBIT.getSourceString());

        // Czml Model
        Assertions.assertEquals("The extension of the file for the model is not supported.",
                OreCzmlMessages.MODEL_EXTENSION_UNKNOWN.getSourceString());
        Assertions.assertEquals("Model Type not known.", OreCzmlMessages.MODEL_TYPE_UNKNOWN.getSourceString());

        // Attitude Pointing
        Assertions.assertEquals("The pointing path is not displayed yet, use displayPointingPath first.",
                OreCzmlMessages.POINTING_PATH_NOT_SHOWN.getSourceString());
        Assertions.assertEquals("First use the .displayPeriodPointingPath() to set the period pointing path.",
                OreCzmlMessages.PERIOD_POINTING_PATH_NOT_SHOWN.getSourceString());

        // Czml File
        Assertions.assertEquals("No objects have been written.", OreCzmlMessages.HEADER_ALONE.getSourceString());
        Assertions.assertEquals("No header was defined in the primary objects.",
                OreCzmlMessages.NO_HEADER.getSourceString());
        Assertions.assertEquals("String generation error.", OreCzmlMessages.STRING_NOT_GENERATED.getSourceString());

        // Ground Track
        Assertions.assertEquals(
                "The ground tracks are empty, either the file is already written or the ground track is not build with a constellation.",
                OreCzmlMessages.EMPTY_GROUND_TRACKS.getSourceString());

        // Satellite
        Assertions.assertEquals(
                "The spacecraft did not display the orientation, maybe you tried to use an AttitudePointing, please use the withDisplayAttitude() method first.",
                OreCzmlMessages.NO_ORIENTATION_DISPLAYED.getSourceString());
        Assertions.assertEquals("The initial state of the spacecraft does not have a keplerian period.",
                OreCzmlMessages.NO_ORBIT_FOR_KEPLERIAN_PERIOD.getSourceString());

        // Visibility cone
        Assertions.assertEquals("The Visibility cone was not defined with a given spacecraft.",
                OreCzmlMessages.NO_SAT_VISIBILITY_CONE.getSourceString());

        // Orientation
        Assertions.assertEquals(
                "Attitudes were created in multiples, can't return a single attitude, use getAttitudes instead.",
                OreCzmlMessages.MULTIPLE_ATTITUDES_SINGLE_GET.getSourceString());
        Assertions.assertEquals(
                "Only one Attitude is built, can't return multiple attitudes, use getSingleAttitude instead.",
                OreCzmlMessages.SINGLE_ATTITUDE_MULTIPLE_GET.getSourceString());

        // Polyline
        Assertions.assertEquals("Can't call a vector function on a non-vector polyline.",
                OreCzmlMessages.DEFAULT_CANT_CALL.getSourceString());
        Assertions.assertEquals("The size of the cartesian positions inputted in the Polyline must be 2.",
                OreCzmlMessages.MORE_THAN_2_CARTESIAN_POLYLINE.getSourceString());
        Assertions.assertEquals("The polyline was not defined with references, so it cannot be written that way.",
                OreCzmlMessages.NO_REFERENCES_POLYLINE.getSourceString());

        // Position
        Assertions.assertEquals("Height is not defined.", OreCzmlMessages.EMPTY_POSITION_HEIGHT.getSourceString());
        Assertions.assertEquals("x is not defined.", OreCzmlMessages.EMPTY_X.getSourceString());
        Assertions.assertEquals("y is not defined.", OreCzmlMessages.EMPTY_Y.getSourceString());
        Assertions.assertEquals("z is not defined.", OreCzmlMessages.EMPTY_Z.getSourceString());
        Assertions.assertEquals("Position Type is not defined.",
                OreCzmlMessages.POSITION_TYPE_UNKNOWN.getSourceString());

        // Long lat lines
        Assertions.assertEquals("Latitude is not defined.", OreCzmlMessages.DEFAULT_ERROR_LATITUDE.getSourceString());
        Assertions.assertEquals("Longitude is not defined.", OreCzmlMessages.DEFAULT_ERROR_LONGITUDE.getSourceString());
        Assertions.assertEquals("The angular step for the latitude can't be greater than 180.",
                OreCzmlMessages.GREATER_ANGULAR_LATITUDE_STEP.getSourceString());
        Assertions.assertEquals("The angular step for the longitude can't be greater than 360.",
                OreCzmlMessages.GREATER_ANGULAR_LONGITUDE_STEP.getSourceString());
    }
}

