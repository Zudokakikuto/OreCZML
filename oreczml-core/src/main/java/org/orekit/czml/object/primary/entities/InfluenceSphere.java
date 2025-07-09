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

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.hipparchus.util.FastMath;
import org.orekit.czml.archi.factory.BodyFactory;
import org.orekit.czml.object.Utils.DateUtils;
import org.orekit.czml.object.primary.AbstractPrimaryObject;
import org.orekit.czml.object.primary.Header;
import org.orekit.czml.object.secondary.CzmlEllipsoid;
import org.orekit.frames.Frame;
import org.orekit.orbits.KeplerianOrbit;
import org.orekit.time.AbsoluteDate;
import org.orekit.utils.PVCoordinates;

import cesiumlanguagewriter.Cartesian;
import cesiumlanguagewriter.CesiumOutputStream;
import cesiumlanguagewriter.CesiumStreamWriter;
import cesiumlanguagewriter.PacketCesiumWriter;
import cesiumlanguagewriter.Reference;

public class InfluenceSphere
    extends
    AbstractPrimaryObject {

    /** The gravitational constant. */
    public static final double GRAVITATIONAL_CONSTANT = 6.67430 * 10e-11;

    /** The default string for position references. */
    public static final String DEFAULT_H_POSITION = "#position";

    /** The default id of the influence sphere. */
    public static final String DEFAULT_ID = "INFLUENCE_SPHERE/";

    /** The default name of the influence sphere. */
    public static final String DEFAULT_NAME = "Sphere of influence of :";

    /** The "ellipsoid" that will be created as a sphere. */
    private CzmlEllipsoid ellipsoid;

    /** The body considered. */
    private Body body;

    /** The header considered. */
    private Header header;

    /** The radisu of the sphere of influence. */
    private double radius;

    /** The mass of the body. */
    private double bodyMass;

    /** The mass of the central body. */
    private double centralBodyMass;

    /** Is the body orbiting around the sun. */
    private boolean orbitingAroundTheSun = true;

    /** The position of the sphere of visibility. */
    private List<Cartesian> cartesianPosition;

    /** The reference position of the sphere of influence. */
    private Reference positionReference;

    /**
     * The default constructor of the influence sphere assuming the body is
     * orbiting around the sun. The body object MUST BE WRITTEN in the Czml file
     * for the sphere of influence to work.
     *
     * @param bodyInput : The body considered for the sphere of influence.
     * @param headerInput : The header considered.
     */
    InfluenceSphere(final Body bodyInput, final Header headerInput) {
        this(bodyInput, BodyFactory.getSun(headerInput), headerInput);
    }

    /**
     * This constructor does not assume the body is orbiting around the sun.
     *
     * @param bodyInput : The body considered for the sphere of influence.
     * @param centralBody : The central body around which the body is orbiting
     *        around.
     * @param headerInput : The header considered.
     */
    InfluenceSphere(final Body bodyInput, final Body centralBody,
                    final Header headerInput) {
        final AbsoluteDate startDate =
            DateUtils.toAbsoluteDate(headerInput.getAvailability().getStart());

        this.centralBodyMass =
            centralBody.getCelestialBody().getGM() / GRAVITATIONAL_CONSTANT;
        final Frame centralFrame =
            centralBody.getCelestialBody().getInertiallyOrientedFrame();

        this.header = headerInput;
        this.body = bodyInput;
        this.bodyMass =
            bodyInput.getCelestialBody().getGM() / GRAVITATIONAL_CONSTANT;

        this.setId(DEFAULT_ID + bodyInput.getName());
        this.setName(DEFAULT_NAME + body.getName());
        this.setAvailability(header.getAvailability());

        final PVCoordinates initialPVCBody =
            body.getCelestialBody().getPVCoordinates(startDate, centralFrame);
        final KeplerianOrbit orbit =
            new KeplerianOrbit(initialPVCBody, centralFrame, startDate,
                               centralBodyMass * GRAVITATIONAL_CONSTANT);

        final double semiMajorAxis = orbit.getA();
        this.radius =
            semiMajorAxis * FastMath.pow(bodyMass / centralBodyMass, 0.4);
        final Cartesian cartesianForSphericalEllipsoid =
            new Cartesian(radius, radius, radius);

        this.cartesianPosition = bodyInput.getCartesianPositionList();
        this.ellipsoid =
            CzmlEllipsoid.builder(cartesianForSphericalEllipsoid, header)
                .withFill(false).withSliceStackPartition(10, 10)
                .withOutline(true).build();
        this.positionReference =
            new Reference(bodyInput.getId() + DEFAULT_H_POSITION);
    }

    /**
     * This builder assumes the body is orbiting around the sun.
     *
     * @param bodyInput : The body considered for the sphere of influence.
     * @param headerInput : The header considered.
     * @return : The influence sphere builder.
     */
    public static InfluenceSphereBuilder builder(final Body bodyInput,
                                                 final Header headerInput) {
        return new InfluenceSphereBuilder(bodyInput, headerInput);
    }

    /**
     * This builder does not assume that the body is orbiting around the sun.
     *
     * @param bodyInput : The body considered for the sphere of influence.
     * @param centralBodyInput : The central body around which the body is
     *        orbiting around.
     * @param headerInput : The header considered.
     * @return : The influence sphere builder.
     */
    public static InfluenceSphereBuilder builder(final Body bodyInput,
                                                 final Body centralBodyInput,
                                                 final Header headerInput) {
        return new InfluenceSphereBuilder(bodyInput, centralBodyInput,
                                          headerInput);
    }

    @Override
    public void writeCzmlBlock(final CesiumStreamWriter stream,
                               final CesiumOutputStream output)
        throws URISyntaxException,
            IOException {
        output.setPrettyFormatting(true);
        try (PacketCesiumWriter packet = stream.openPacket(output)) {
            packet.writeId(getId());
            packet.writeName(getName());
            packet.writeAvailability(getAvailability());
            packet.writePositionPropertyReference(positionReference);
            ellipsoid.write(packet, output);
        }
    }

    /**
     * Gets the ellipsoid.
     *
     * @return the ellipsoid
     */
    public CzmlEllipsoid getEllipsoid() {
        return ellipsoid;
    }

    /**
     * Gets the body.
     *
     * @return the body
     */
    public Body getBody() {
        return body;
    }

    /**
     * Gets the header.
     *
     * @return the header
     */
    public Header getHeader() {
        return header;
    }

    /**
     * Gets the radius of the influence sphere.
     *
     * @return the radius of the influence sphere in metres
     */
    public double getRadius() {
        return radius;
    }

    /**
     * Gets the mass of the body.
     *
     * @return the mass of the body in kilograms
     */
    public double getBodyMass() {
        return bodyMass;
    }

    /**
     * Gets the mass of the central body.
     *
     * @return the mass of the central body in kilograms
     */
    public double getCentralBodyMass() {
        return centralBodyMass;
    }

    /**
     * Gets the orbiting around the sun flag.
     *
     * @return true if the body is orbiting the Sun, false otherwise
     */
    public boolean isOrbitingAroundTheSun() {
        return orbitingAroundTheSun;
    }

    /**
     * Gets the list of Cartesian positions.
     *
     * @return the list of Cartesian positions
     */
    public List<Cartesian> getCartesianPosition() {
        return cartesianPosition;
    }

    /**
     * Gets the position reference.
     *
     * @return the position reference
     */
    public Reference getPositionReference() {
        return positionReference;
    }
}
