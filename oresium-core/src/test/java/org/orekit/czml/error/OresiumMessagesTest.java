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
package org.orekit.czml.error;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.orekit.annotation.DefaultDataContext;
import org.orekit.czml.errors.OresiumMessages;
import org.orekit.czml.file.AbstractTest;

/**
 * Oresium Messages tests.
 */
public class OresiumMessagesTest
    extends
    AbstractTest {

    /** Initialise orekit data. */
    private final double data = initializeOrekitData();

    @Nested
    class OresiumMessagesTests {

        /**
         * Collision Oresium messages test.
         */
        @Test
        @DefaultDataContext
        @DisplayName("Collision messages test")
        void CollisionOresiumMessagesTests() {
            Assertions
                .assertEquals("Satellite are not close enough to build a collision object.",
                              OresiumMessages.NOT_CLOSE_ENOUGH
                                  .getSourceString());

        }

        /**
         * Oresium messages test.
         */
        @DisplayName("Constellation messages test")
        @Test
        @DefaultDataContext
        void ConstellationOresiumMessagesTests() {
            Assertions
                .assertEquals("The number of models loaded is not the same as the number of satellites of the constellation.",
                              OresiumMessages.NOT_SAME_NUMBER_SAT_MODELS
                                  .getSourceString());
        }

        @DisplayName("Ground station messages test")
        @Test
        @DefaultDataContext
        void GroundStationOresiumMessagesTests() {
            Assertions
                .assertEquals("Can't apply none or several models to one ground station, use the .withModel(String) instead.",
                              OresiumMessages.MULTIPLE_MODEL_SINGLE_STATION
                                  .getSourceString());
            Assertions
                .assertEquals("The ground station was not build with several topocentric frames, only one.",
                              OresiumMessages.SINGLE_STATION_GET_MULTIPLE_GROUND_STATION
                                  .getSourceString());
            Assertions
                .assertEquals("Several ground stations were build, please use the appropriate getter (usually ends with an \"s\" for plural).",
                              OresiumMessages.SEVERAL_STATION_UNIQUE_GET
                                  .getSourceString());

        }

        @DisplayName("Body messages test")
        @Test
        @DefaultDataContext
        void BodyOresiumMessagesTests() {
            Assertions
                .assertEquals("The orbit is not displayed, do not use noOrbitDisplay() then setup the period display of the orbit.",
                              OresiumMessages.CANT_DISPLAY_PERIOD_NO_ORBIT
                                  .getSourceString());
        }

        @Test
        @DefaultDataContext
        void ModelOresiumMessagesTests() {
            Assertions
                .assertEquals("The extension of the file for the model is not supported.",
                              OresiumMessages.MODEL_EXTENSION_UNKNOWN
                                  .getSourceString());
            Assertions.assertEquals("Model Type not known.",
                                    OresiumMessages.MODEL_TYPE_UNKNOWN
                                        .getSourceString());

        }

        @DisplayName("Attitude pointing messages test")
        @Test
        @DefaultDataContext
        void AttitudePointingOresiumMessagesTests() {
            Assertions
                .assertEquals("The pointing path is not displayed yet, use displayPointingPath first.",
                              OresiumMessages.POINTING_PATH_NOT_SHOWN
                                  .getSourceString());
            Assertions
                .assertEquals("First use the .displayPeriodPointingPath() to set the period pointing path.",
                              OresiumMessages.PERIOD_POINTING_PATH_NOT_SHOWN
                                  .getSourceString());

        }

        @DisplayName("Czml File messages test")
        @Test
        @DefaultDataContext
        void CzmlFileOresiumMessagesTests() {
            Assertions
                .assertEquals("No objects have been written.",
                              OresiumMessages.HEADER_ALONE.getSourceString());
            Assertions
                .assertEquals("No header was defined in the primary objects.",
                              OresiumMessages.NO_HEADER.getSourceString());
            Assertions.assertEquals("String generation error.",
                                    OresiumMessages.STRING_NOT_GENERATED
                                        .getSourceString());

        }

        @DisplayName("Ground Track messages test")
        @Test
        @DefaultDataContext
        void GroundTrackOresiumMessagesTests() {
            Assertions
                .assertEquals("The ground tracks are empty, either the file is already written or the ground track is not build with a constellation.",
                              OresiumMessages.EMPTY_GROUND_TRACKS
                                  .getSourceString());

        }

        @DisplayName("Spacecraft messages test")
        @Test
        @DefaultDataContext
        void SpacecraftOresiumMessagesTests() {
            Assertions
                .assertEquals("The spacecraft did not display the orientation, maybe you tried to use an AttitudePointing, please use the withDisplayAttitude() method first.",
                              OresiumMessages.NO_ORIENTATION_DISPLAYED
                                  .getSourceString());
            Assertions
                .assertEquals("The initial state of the spacecraft does not have a keplerian period.",
                              OresiumMessages.NO_ORBIT_FOR_KEPLERIAN_PERIOD
                                  .getSourceString());

        }

        @DisplayName("Visibility cone messages test")
        @Test
        @DefaultDataContext
        void VisibilityConeOresiumMessagesTests() {
            Assertions
                .assertEquals("The Visibility cone was not defined with a given spacecraft.",
                              OresiumMessages.NO_SAT_VISIBILITY_CONE
                                  .getSourceString());

        }

        @DisplayName("Orientation messages test")
        @Test
        @DefaultDataContext
        void OrientationOresiumMessagesTests() {
            Assertions
                .assertEquals("Attitudes were created in multiples, can't return a single attitude, use getAttitudes instead.",
                              OresiumMessages.MULTIPLE_ATTITUDES_SINGLE_GET
                                  .getSourceString());
            Assertions
                .assertEquals("Only one Attitude is built, can't return multiple attitudes, use getSingleAttitude instead.",
                              OresiumMessages.SINGLE_ATTITUDE_MULTIPLE_GET
                                  .getSourceString());

        }

        @DisplayName("Polyline messages test")
        @Test
        @DefaultDataContext
        void PolylineOresiumMessagesTests() {
            Assertions
                .assertEquals("Can't call a vector function on a non-vector polyline.",
                              OresiumMessages.DEFAULT_CANT_CALL
                                  .getSourceString());
            Assertions
                .assertEquals("The size of the cartesian positions inputted in the Polyline must be 2.",
                              OresiumMessages.MORE_THAN_2_CARTESIAN_POLYLINE
                                  .getSourceString());
            Assertions
                .assertEquals("The polyline was not defined with references, so it cannot be written that way.",
                              OresiumMessages.NO_REFERENCES_POLYLINE
                                  .getSourceString());

        }

        @DisplayName("Position messages test")
        @Test
        @DefaultDataContext
        void PositionOresiumMessagesTests() {
            Assertions.assertEquals("Height is not defined.",
                                    OresiumMessages.EMPTY_POSITION_HEIGHT
                                        .getSourceString());
            Assertions.assertEquals("x is not defined.",
                                    OresiumMessages.EMPTY_X.getSourceString());
            Assertions.assertEquals("y is not defined.",
                                    OresiumMessages.EMPTY_Y.getSourceString());
            Assertions.assertEquals("z is not defined.",
                                    OresiumMessages.EMPTY_Z.getSourceString());
            Assertions.assertEquals("Position Type is not defined.",
                                    OresiumMessages.POSITION_TYPE_UNKNOWN
                                        .getSourceString());

        }

        @DisplayName("Long Lat Lines messages test")
        @Test
        @DefaultDataContext
        void LongLatLinesOresiumMessagesTests() {
            Assertions.assertEquals("Latitude is not defined.",
                                    OresiumMessages.DEFAULT_ERROR_LATITUDE
                                        .getSourceString());
            Assertions.assertEquals("Longitude is not defined.",
                                    OresiumMessages.DEFAULT_ERROR_LONGITUDE
                                        .getSourceString());
            Assertions
                .assertEquals("The angular step for the latitude can't be greater than 180.",
                              OresiumMessages.GREATER_ANGULAR_LATITUDE_STEP
                                  .getSourceString());
            Assertions
                .assertEquals("The angular step for the longitude can't be greater than 360.",
                              OresiumMessages.GREATER_ANGULAR_LONGITUDE_STEP
                                  .getSourceString());
        }
    }
}
