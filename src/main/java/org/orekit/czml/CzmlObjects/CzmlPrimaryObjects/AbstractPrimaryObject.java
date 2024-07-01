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
package org.orekit.czml.CzmlObjects.CzmlPrimaryObjects;

import cesiumlanguagewriter.TimeInterval;

/** Abstract Primary Object

 * <p>
 * This class aims at giving a common abstract base where all primary objects will refer to.
 * </p>
 *
 * @since 2.0
 * @author Julien LEBLOND.
 */

public abstract class AbstractPrimaryObject implements CzmlPrimaryObject {

    /** THe id of the object.*/
    private String id;
    /** The name of the object.*/
    private String name;
    /** .*/
    private TimeInterval availability;

    public String getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public TimeInterval getAvailability() {
        return availability;
    }

    protected void setId(final String s) {
        this.id = s;
    }

    protected void setName(final String n) {
        this.name = n;
    }

    protected void setAvailability(final TimeInterval a) {
        this.availability = a;
    }
}
