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
package org.orekit.czml.object.primary.entities;

import cesiumlanguagewriter.Cartesian;
import cesiumlanguagewriter.CesiumOutputStream;
import cesiumlanguagewriter.CesiumStreamWriter;
import cesiumlanguagewriter.PacketCesiumWriter;
import cesiumlanguagewriter.Reference;
import org.hipparchus.util.FastMath;
import org.orekit.czml.object.primary.AbstractPrimaryObject;
import org.orekit.czml.object.secondary.Clock;
import org.orekit.czml.object.secondary.CzmlEllipsoid;
import org.orekit.czml.object.utils.DateUtils;
import org.orekit.frames.Frame;
import org.orekit.orbits.KeplerianOrbit;
import org.orekit.time.AbsoluteDate;
import org.orekit.utils.PVCoordinates;

import java.io.IOException;
import java.net.URISyntaxException;

public class InfluenceSphere
    extends
    AbstractPrimaryObject {

    /** The gravitational constant. */
    public static final double GRAVITATIONAL_CONSTANT = 6.67430 * 1e-11;

    /** The default string for position references. */
    public static final String DEFAULT_H_POSITION = "#position";

    /** The default id of the influence sphere. */
    public static final String DEFAULT_ID = "INFLUENCE_SPHERE/";

    /** The default name of the influence sphere. */
    public static final String DEFAULT_NAME = "Sphere of influence of :";

    /** The "ellipsoid" that will be created as a sphere. */
    private final CzmlEllipsoid ellipsoid;

    /** The body considered. */
    private final Body body;

    /** The clock considered. */
    private Clock clock;

    /** The radisu of the sphere of influence. */
    private final double radius;

    /** The reference position of the sphere of influence. */
    private final Reference positionReference;

    /**
     * The default constructor of the influence sphere. The body object MUST BE
     * WRITTEN in the Czml file for the sphere of influence to work.
     *
     * @param bodyInput : The body considered for the sphere of influence.*
     *        around.
     * @param clock : The clock considered.
     */
    InfluenceSphere(final Body bodyInput, final Clock clock) {
        final AbsoluteDate startDate =
            DateUtils.toAbsoluteDate(clock.getAvailability().getStart());

        this.body = bodyInput;
        final Body centralBody = body.getCentralBody();
        final double centralBodyMass =
            centralBody.getCelestialBody().getGM() / GRAVITATIONAL_CONSTANT;

        final Frame centralFrame;
        centralFrame =
            centralBody.getCelestialBody().getInertiallyOrientedFrame();

        this.clock = clock;
        // The mass of the central body
        final double bodyMass =
            bodyInput.getCelestialBody().getGM() / GRAVITATIONAL_CONSTANT;

        this.setId(DEFAULT_ID + bodyInput.getName());
        this.setName(DEFAULT_NAME + body.getName());
        this.setAvailability(clock.getAvailability());

        final PVCoordinates initialPVCBody =
            body.getCelestialBody().getPVCoordinates(startDate, centralFrame);

        final KeplerianOrbit orbit =
            new KeplerianOrbit(initialPVCBody, centralFrame, startDate, body
                .getCentralBody().getCelestialBody().getGM());

        final double semiMajorAxis = orbit.getA();
        this.radius =
            semiMajorAxis * FastMath.pow(bodyMass / centralBodyMass, 0.4);
        final Cartesian cartesianForSphericalEllipsoid =
            new Cartesian(radius, radius, radius);

        // The position of the sphere of visibility.
        this.ellipsoid =
            CzmlEllipsoid.builder(cartesianForSphericalEllipsoid, clock)
                .withFill(false).withSliceStackPartition(10, 10)
                .withOutline(true).build();

        this.positionReference =
            new Reference(bodyInput.getId() + DEFAULT_H_POSITION);
    }

    /**
     * This builder assumes the body is orbiting around the sun.
     *
     * @param clock : The clock of the influence sphere
     * @param bodyInput : The body considered
     * @return An influence sphere builder with the given inputs
     */
    public static InfluenceSphereBuilder builder(final Body bodyInput,
                                                 final Clock clock) {
        return new InfluenceSphereBuilder(bodyInput, clock);
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
     * Gets the ellipsoid of the influence sphere.
     *
     * @return The ellipsoid.
     */
    public CzmlEllipsoid getEllipsoid() {
        return ellipsoid;
    }

    /**
     * Gets the body of the influence sphere.
     *
     * @return The body
     */
    public Body getBody() {
        return body;
    }

    /**
     * Gets the clock of the influence sphere.
     *
     * @return The clock
     */
    public Clock getClock() {
        return clock;
    }

    /**
     * Gets the radius of the influence sphere.
     *
     * @return The radius
     */
    public double getRadius() {
        return radius;
    }

    /**
     * Set the clock.
     *
     * @param clockInput The clock to set
     */
    public void setClock(final Clock clockInput) {
        this.clock = clockInput;
    }
}
