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
package org.orekit.czml.CzmlEnum;

public enum PositionType {

    /** The position in radians in the (longitude, latitude, altitude) reference system. */
    CARTOGRAPHIC_RADIANS,
    /** The position in degrees in the (longitude, latitude, altitude) reference system. */
    CARTOGRAPHIC_DEGREES,
    /** The position in the cartesian position (x,y,z) reference system. */
    CARTESIAN_POSITION,
    /** The position in the cartesian velocity (vx,vy,vz) reference system. */
    CARTESIAN_VELOCITY;

    PositionType() {
    }
}
