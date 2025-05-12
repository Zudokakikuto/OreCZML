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
package org.orekit.czml.object.primary.entities;

import org.orekit.czml.archi.factory.BodyFactory;
import org.orekit.czml.object.primary.Header;
import org.orekit.frames.Frame;

public class InfluenceSphereBuilder {

    /** The gravitational constant. */
    public static final double GRAVITATIONAL_CONSTANT = 6.67430 * 10e-11;

    /** The default ID. */
    public static final String DEFAULT_ID = "INFLUENCE_SPHERE/";

    /** The default name. */
    public static final String DEFAULT_NAME = "Sphere of influence of :";

    /** The body considered for the sphere of influence. */
    private Body body;

    /** The header considered. */
    private Header header;

    /** The customID that can be set up. */
    private String customID;

    /** The path to the 3D model. */
    private String pathTo3DModel;

    /** The inertial frame of the body. */
    private Frame inertialFrame;

    /** The frame oriented of the body. */
    private Frame bodyOrientedFrame;

    /** The name of the body. */
    private String bodyName = "Nameless body";

    /** The mu of the body. */
    private double bodyMu;

    /** The mass of the central body. */
    private Body centralBody;

    public InfluenceSphereBuilder(final Body bodyInput,
                                  final Header headerInput) {
        this.body = bodyInput;
        this.header = headerInput;
        this.customID = DEFAULT_ID + bodyInput.getName();
        this.centralBody = BodyFactory.getSun(headerInput);
    }

    public InfluenceSphereBuilder(final Body bodyInput,
                                  final Body centralBodyInput,
                                  final Header headerInput) {
        this.body = bodyInput;
        this.header = headerInput;
        this.centralBody = centralBodyInput;
        this.customID = DEFAULT_ID + bodyInput.getName();
    }

    /**
     * This function set up a custom ID for the sphere of influence.
     *
     * @param customIDInput : The ID to set up
     * @return : The influence sphere builder with a custom ID.
     */
    public InfluenceSphereBuilder withCustomID(final String customIDInput) {
        this.customID = customIDInput;
        return this;
    }

    /**
     * This function set up a custom name for the body of the sphere of
     * influence.
     *
     * @param customBodyNameInput : The name to set up
     * @return : The influence sphere builder with a custom body name.
     */
    public InfluenceSphereBuilder
        withCustomBodyName(final String customBodyNameInput) {
        this.bodyName = customBodyNameInput;
        return this;
    }

    /**
     * This function set up an inertial frame for the sphere of influence.
     *
     * @param inertialFrameInput : The inertial frame to set up
     * @return : The influence sphere builder with a custom inertial frame.
     */
    public InfluenceSphereBuilder
        withInertialFrame(final Frame inertialFrameInput) {
        this.inertialFrame = inertialFrameInput;
        return this;
    }

    /**
     * This function set up a body oriented frame for the sphere of influence.
     *
     * @param bodyOrientedFrameInput : The body oriented frame to set up
     * @return : The influence sphere builder with a custom body oriented frame.
     */
    public InfluenceSphereBuilder
        withBodyOrientedFrame(final Frame bodyOrientedFrameInput) {
        this.bodyOrientedFrame = bodyOrientedFrameInput;
        return this;
    }

    /**
     * This function set up a custom mu for the sphere of influence.
     *
     * @param muInput : The mu to set up.
     * @return : The influence sphere builder with a custom mu.
     */
    public InfluenceSphereBuilder withCustomMu(final double muInput) {
        this.bodyMu = muInput;
        return this;
    }

    /**
     * This function set up a custom header for the sphere of influence.
     *
     * @param headerInput : The header to set up.
     * @return : The influence sphere builder with a custom header.
     */
    public InfluenceSphereBuilder withHeader(final Header headerInput) {
        this.header = headerInput;
        return this;
    }

    /**
     * This function set up a custom mass for the central body. Using this
     * method will make the sphere of influence not assume the body is orbiting
     * around the sun.
     *
     * @param centralBodyInput : The mass to set up.
     * @return : The influence sphere builder with a custom mass for the central
     *         body.
     */
    public InfluenceSphereBuilder withCentralBody(final Body centralBodyInput) {
        this.centralBody = centralBodyInput;
        return this;
    }

    /**
     * This function builds the sphere of influence with all the parameters
     * given.
     *
     * @return : The influence sphere object with the given parameters.
     */
    public InfluenceSphere build() {
        return new InfluenceSphere(body, centralBody, header);
    }
}
