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

package org.orekit.czml.errors;

import org.hipparchus.exception.Localizable;

import java.util.Locale;

/**
 * OreCzmlMessages.
 * <p>
 * This class contains all the error messages that can happens when the
 * execution of the code is not nominal.=
 *
 * @author : Julien LEBLOND
 * @since V1.0
 */
public enum OreCzmlMessages
    implements
    Localizable {

                 // collision
                 /**
                  * Trigger when satellites are not close enough to build a
                  * collision object.
                  */
                 NOT_CLOSE_ENOUGH("Satellite are not close enough to build a collision object."),

                 // Constellation
                 /**
                  * Trigger when the number of models loaded is different from
                  * the name of satellite when several models are loaded.
                  */
                 NOT_SAME_NUMBER_SAT_MODELS("The number of models loaded is not the same as the number of satellites of the constellation."),

                 // Czml ground station builder
                 /**
                  * Triggers when multiple models are loaded into a single
                  * station.
                  */
                 MULTIPLE_MODEL_SINGLE_STATION("Can't apply none or several models to one ground station, use the .withModel(String) instead."),

                 // Czml ground station
                 /**
                  * Triggers when a single station is defined but the user tried
                  * to get multiple stations.
                  */
                 SINGLE_STATION_GET_MULTIPLE_GROUND_STATION("The ground station was not build with several topocentric frames, only one."),

                 /**
                  * Triggers when a get returning a single parameter on ground
                  * station are used when several ground stations are defined.
                  */
                 SEVERAL_STATION_UNIQUE_GET("Several ground stations were build, please use the appropriate getter (usually ends with an \"s\" for plural)."),

                 // Body
                 /**
                  * Triggers when the orbit of the body is not displayed, but
                  * the user tried to set up the period of the orbit.
                  */
                 CANT_DISPLAY_PERIOD_NO_ORBIT("The orbit is not displayed, do not use noOrbitDisplay() then setup the period display of the orbit."),

                 /**
                  * Triggers when the displayInfluenceSphere method is not
                  * called.
                  */
                 INFLUENCE_SPHERE_NOT_DISPLAYED("This displayInfluenceSphere() method has not been called, so the getInfluenceSphere() cannot be used."),

                 // Czml Model
                 /**
                  * Triggers when the extension of the file of the model used is
                  * not recognized as one of the extension file accepted.
                  */
                 MODEL_EXTENSION_UNKNOWN("The extension of the file for the model is not supported."),

                 /**
                  * Triggers when the file given is not a 2D or a 3D model.
                  */
                 MODEL_TYPE_UNKNOWN("Model Type not known."),

                 // Attitude Pointing
                 /**
                  * Trigger when the pointing path is not displayed, but the
                  * user tried to display the period of the pointing path.
                  */
                 POINTING_PATH_NOT_SHOWN("The pointing path is not displayed yet, use displayPointingPath first."),

                 /**
                  * Triggers when the period of the pointing path is not
                  * displayed and the user tried to setup the period pointing
                  * path.
                  */
                 PERIOD_POINTING_PATH_NOT_SHOWN("First use the .displayPeriodPointingPath() to set the period pointing path."),

                 // Czml File
                 /**
                  * Triggers when no object (except the header) has been defined
                  * in the czml file.
                  */
                 HEADER_ALONE("No objects have been written."),

                 /**
                  * Triggers when no header is defined in the czml file.
                  */
                 NO_HEADER("No header was defined in the primary objects."),

                 /**
                  * Triggers when the Czml file can't generate the output string
                  * asked.
                  */
                 STRING_NOT_GENERATED("String generation error."),

                 // Ground track
                 /**
                  * Trigger when the getAllGroundTracks() method is call and the
                  * ground tracks are empty, it can happen when the czml file
                  * has already been written or the ground track object is not
                  * built with a constellation.
                  */
                 EMPTY_GROUND_TRACKS("The ground tracks are empty, either the file is already written or the ground track is not build with a constellation."),

                 // Spacecraft
                 /**
                  * Triggers when the satellite does not display the
                  * orientation, but the method getOrientation() has been
                  * called.
                  */
                 NO_ORIENTATION_DISPLAYED("The spacecraft did not display the orientation, maybe you tried to use an AttitudePointing, please use the withDisplayAttitude() method first."),

                 /**
                  * Trigger when the initial state of the satellite object is
                  * not built with a keplerian period gettable.
                  */
                 NO_ORBIT_FOR_KEPLERIAN_PERIOD("The initial state of the spacecraft does not have a keplerian period."),

                 /**
                  * Triggers when the spacecraft is not inside an influence
                  * sphere.
                  */
                 NOT_INSIDE_AN_INFLUENCE_SPHERE("The spacecraft is not inside an influence sphere, or the influence sphere was not defined/inputted in the displayInfluenceSphereChanges() method."),

                 /**
                  * Triggers when changes are made in the spacecraft orbit
                  * without first building an influence sphere for the
                  * spacecraft body.
                  */
                 TRY_INFLUENCE_SPHERE_WITHOUT_SPHERES("You tried to displayed changes in the orbit of the spacecraft without building the influence sphere of the bodies. Try using .displayInfluenceSphere() on the bodies first."),

                 // Visibility cone
                 /**
                  * Triggers when the visibility cone was not defined for a
                  * given satellite.
                  */
                 NO_SAT_VISIBILITY_CONE("The Visibility cone was not defined with a given spacecraft."),

                 // Orientation
                 /**
                  * Triggers when several attitudes are defined, but the user
                  * uses the getSingleAttitude() method.
                  */
                 MULTIPLE_ATTITUDES_SINGLE_GET("Attitudes were created in multiples, can't return a single attitude, use getAttitudes instead."),

                 /**
                  * Triggers when a single attitude is defined, but the user
                  * uses the getAttitudes() method.
                  */
                 SINGLE_ATTITUDE_MULTIPLE_GET("Only one Attitude is built, can't return multiple attitudes, use getSingleAttitude instead."),

                 // Polyline
                 /**
                  * Triggers when the user calls a vector function into a
                  * non-vector polyline.
                  */
                 DEFAULT_CANT_CALL("Can't call a vector function on a non-vector polyline."),

                 /**
                  * Triggers when the user input more than 2 cartesians to build
                  * a vector polyline that takes two cartesian coordinates as an
                  * input. (each coordinate represents the extremity of the
                  * line).
                  */
                 MORE_THAN_2_CARTESIAN_POLYLINE("The size of the cartesian positions inputted in the Polyline must be 2."),

                 /**
                  * Triggers when the non-vector polyline is not defined with
                  * references.
                  */
                 NO_REFERENCES_POLYLINE("The polyline was not defined with references, so it cannot be written that way."),

                 // Position
                 /**
                  * Triggers when the height is not defined and the user uses
                  * the getHeight() method.
                  */
                 EMPTY_POSITION_HEIGHT("Height is not defined."),

                 /**
                  * Triggers when the 'x' parameter is not defined and the user
                  * uses the getX() method.
                  */
                 EMPTY_X("x is not defined."),

                 /**
                  * Triggers when the 'y' parameter is not defined and the user
                  * uses the getY() method.
                  */
                 EMPTY_Y("y is not defined."),

                 /**
                  * Triggers when the 'z' parameter is not defined and the user
                  * uses the getZ() method.
                  */
                 EMPTY_Z("z is not defined."),

                 /**
                  * Triggers when the position type given is not in the position
                  * type defined. (CARTESIAN_POSITION, CARTESIAN_VELOCITY,
                  * CARTOGRAPHIC_RADIANS, CARTOGRAPHIC_DEGREES)
                  */
                 POSITION_TYPE_UNKNOWN("Position Type is not defined."),

                 // Long lat lines
                 /**
                  * Triggers when the latitude is not defined and the user uses
                  * the getLatitudeRad() method.
                  */
                 DEFAULT_ERROR_LATITUDE("Latitude is not defined."),

                 /**
                  * Triggers when the longitude is not defined and the user uses
                  * the getLongitudeRad() method.
                  */
                 DEFAULT_ERROR_LONGITUDE("Longitude is not defined."),

                 /**
                  * Triggers when the angular step for the latitude is greater
                  * than 180°.
                  */
                 GREATER_ANGULAR_LATITUDE_STEP("The angular step for the latitude can't be greater than 180."),

                 /**
                  * Triggers when the angular step for the longitude is greater
                  * than 360°.
                  */
                 GREATER_ANGULAR_LONGITUDE_STEP("The angular step for the longitude can't be greater than 360."),

                 // Line of visibility

                 /**
                  * Triggers when the .getShowList() method is applied on a line
                  * of visibility containing several shows.
                  */
                 NOT_A_SINGLE_SAT_OR_STATION("The line of visibility used has several station or several spacecraft, don't use the .getShowList() method instead use the .getSingleShow(int) method."),

                 /**
                  * Triggers when the .getSingleShow(int) method is used while
                  * only one line of visibility is defined.
                  */
                 NOT_A_MULTIPLE_SAT_OR_STATION("The line of visibility used has only one station and one spacecraft defined, don't use the .getSingleShow(int) method, instead use the .getShowList() method."),

                 /**
                  * Triggers when the .displayTriangle() method is called while
                  * the line of visibility has several stations or spacecrafts.
                  */
                 NOT_A_SINGLE_TRIANGLE_LINE("The line of visibility used has several station or several spacecrafts, don't use the .displayTriangle() method, instead use the .displaySingleTriangle(int) method"),

                 /**
                  * Triggers when the .displaySingleTriangle(int) method is used
                  * while only one line of visibility is defined.
                  */
                 NOT_A_MULTIPLE_TRIANGLE_LINE("The line of visibility used has only one station and one spacecraft defined, don't use the .displaySingleTriangle(int) method, instead use the .displayTriangle() method"),

                 // Influence Sphere Utils

                 /**
                  * Trigger when the list of julian dates and the list of
                  * cartesian is not the same while trying to build segregated
                  * time and position for each influence sphere crossing.
                  */
                 NOT_SAME_SIZE_TIME_POSITION("The list of julian dates and the list of cartesian is not the same, thus the influence sphere crossing segregation cannot happen"),

                 // MultipleLinesVisibility

                 /**
                  * Trigger when the list of lines of visibility is tried to be
                  * built, but no spacecraft or no constellation are defined.
                  */
                 NO_SPACECRAFT_OR_CONSTELLATION("No spacecraft or constellation are defined to build the lines of visibility."),

                 /**
                  * Trigger when the size of the custom availabilities, custom
                  * name or custom ids are not the same as the number of
                  * topocentric frame when building the line of visibilities.
                  */
                 NOT_SAME_SIZE_TOPOCENTRIC_FRAMES("The custom ids, names or clocks size are not the same as the number of topocentric frame inputted in the multiple line of visibility."),
                 // unit testing

                 /**
                  * Trigger when satellites are not close enough to build a
                  * collision object.
                  */
                 NOT_A_NUMBER("The unit test file verification function falsely selected a text string as a number.");

    /**
     * The string containing the message.
     */
    private final String sourceFormat;

    OreCzmlMessages(final String s) {
        this.sourceFormat = s;
    }

    @Override
    public String getSourceString() {
        return sourceFormat;
    }

    @Override
    public String getLocalizedString(final Locale locale) {
        return sourceFormat;
    }

}
