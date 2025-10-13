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
package org.orekit.czml.object.primary.visu;

import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.hipparchus.ode.nonstiff.AdaptiveStepsizeIntegrator;
import org.hipparchus.ode.nonstiff.DormandPrince853Integrator;
import org.hipparchus.util.FastMath;
import org.junit.jupiter.api.Test;
import org.orekit.attitudes.LofOffset;
import org.orekit.czml.file.AbstractTest;
import org.orekit.czml.object.primary.Header;
import org.orekit.czml.object.primary.entities.Spacecraft;
import org.orekit.forces.ForceModel;
import org.orekit.forces.gravity.HolmesFeatherstoneAttractionModel;
import org.orekit.forces.gravity.potential.GravityFieldFactory;
import org.orekit.forces.gravity.potential.NormalizedSphericalHarmonicsProvider;
import org.orekit.frames.FramesFactory;
import org.orekit.frames.LOFType;
import org.orekit.frames.Transform;
import org.orekit.geometry.fov.DoubleDihedraFieldOfView;
import org.orekit.geometry.fov.FieldOfView;
import org.orekit.orbits.KeplerianOrbit;
import org.orekit.orbits.OrbitType;
import org.orekit.orbits.PositionAngleType;
import org.orekit.propagation.BoundedPropagator;
import org.orekit.propagation.EphemerisGenerator;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.numerical.NumericalPropagator;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScalesFactory;
import org.orekit.utils.Constants;

import java.awt.Color;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * The type Field of observation test.
 */
public class FieldOfObservationTest
    extends
    AbstractTest {

    /**
     * Field of observation constructor test.
     *
     * @throws IOException the io exception
     * @throws URISyntaxException the uri syntax exception
     */
    @Test
    void FieldOfObservationConstructorTest()
        throws IOException,
            URISyntaxException {

        loadOrekitData();

        final Header header = dummyHeader();
        final AbsoluteDate startDate =
            new AbsoluteDate(2024, 3, 15, 0, 0, 0.0,
                             TimeScalesFactory.getUTC());
        final AbsoluteDate finalDate = startDate.shiftedBy(10 * 3600);

        final KeplerianOrbit initialOrbit =
            new KeplerianOrbit(7878000, 0, FastMath.toRadians(20), 0,
                               FastMath.toRadians(0), FastMath.toRadians(0),
                               PositionAngleType.MEAN,
                               FramesFactory.getEME2000(), startDate,
                               Constants.WGS84_EARTH_MU);

        final SpacecraftState initialState = new SpacecraftState(initialOrbit);

        // Build of the propagator

        final double[][] tolerances =
            NumericalPropagator.tolerances(10, initialOrbit,
                                           OrbitType.CARTESIAN);
        final AdaptiveStepsizeIntegrator integrator =
            new DormandPrince853Integrator(0.001, 1000.0, tolerances[0],
                                           tolerances[1]);

        final NumericalPropagator propagator =
            new NumericalPropagator(integrator);

        final NormalizedSphericalHarmonicsProvider provider =
            GravityFieldFactory.getNormalizedProvider(10, 10);
        final ForceModel holmesFeatherstone =
            new HolmesFeatherstoneAttractionModel(FramesFactory.getEME2000(),
                                                  provider);

        propagator.setOrbitType(OrbitType.CARTESIAN);
        propagator.addForceModel(holmesFeatherstone);
        propagator.setInitialState(initialState);

        final EphemerisGenerator generator = propagator.getEphemerisGenerator();

        final LofOffset lofOffset =
            new LofOffset(FramesFactory.getEME2000(), LOFType.TNW);
        propagator.setAttitudeProvider(lofOffset);

        propagator.propagate(startDate, finalDate);
        final BoundedPropagator boundedPropagator =
            generator.getGeneratedEphemeris();

        final Spacecraft satellite =
            new Spacecraft(boundedPropagator, header.getClock());

        final FieldOfView fov =
            new DoubleDihedraFieldOfView(Vector3D.PLUS_J, Vector3D.PLUS_I,
                                         FastMath.toRadians(20),
                                         Vector3D.PLUS_K,
                                         FastMath.toRadians(20), 2);

        final Transform initialInertToBody =
            propagator.getFrame().getTransformTo(getEarth().getBodyFrame(),
                                                 initialState.getDate());
        final Transform initialFovBody =
            new Transform(initialState.getDate(), propagator.getInitialState()
                .toTransform().getInverse(), initialInertToBody);

        final FieldOfObservation fieldOfObservation =
            FieldOfObservation.builder(satellite, fov, initialFovBody)
                .withColor(Color.ORANGE).withBody(getEarth())
                .withCustomID("CustomID").withAngularStep(20.0).build();

        final String pathFile =
            loadResources("templateFile/object/primary/visu/FieldOfObservationTemplate.txt");
        verifyFileOutput(pathFile, fieldOfObservation.toString(), 1e-3);
    }
}
