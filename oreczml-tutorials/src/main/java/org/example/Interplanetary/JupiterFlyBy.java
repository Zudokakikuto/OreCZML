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
package org.example.Interplanetary;

import org.example.TutorialUtils;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.hipparchus.ode.nonstiff.AdaptiveStepsizeIntegrator;
import org.hipparchus.ode.nonstiff.DormandPrince853Integrator;
import org.orekit.bodies.CelestialBody;
import org.orekit.bodies.CelestialBodyFactory;
import org.orekit.czml.archi.factory.BodyDisplayFactory;
import org.orekit.czml.object.primary.BodyDisplay;
import org.orekit.czml.object.primary.Header;
import org.orekit.czml.object.primary.Satellite;
import org.orekit.czml.object.secondary.Clock;
import org.orekit.czml.file.CzmlFile;
import org.orekit.czml.file.CzmlFileBuilder;
import org.orekit.forces.gravity.SingleBodyAbsoluteAttraction;
import org.orekit.forces.inertia.InertialForces;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.frames.Transform;
import org.orekit.orbits.KeplerianOrbit;
import org.orekit.orbits.OrbitType;
import org.orekit.propagation.BoundedPropagator;
import org.orekit.propagation.EphemerisGenerator;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.numerical.NumericalPropagator;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScale;
import org.orekit.time.TimeScalesFactory;
import org.orekit.utils.AbsolutePVCoordinates;
import org.orekit.utils.Constants;
import org.orekit.utils.PVCoordinates;

import java.awt.Color;

import static org.example.TutorialUtils.loadResources;

public class JupiterFlyBy {

    private JupiterFlyBy() {
    }

    public static void main(final String[] args) throws Exception {
        // Load orekit data
        TutorialUtils.loadOrekitData();

        // Paths
        final String root = System.getProperty("user.dir")
                                  .replace("\\", "/");
        final String outputPath = root + "/Output";
        final String outputName = "Output.czml";
        final String output     = outputPath + "/" + outputName;
        // Change the path here to your JavaScript>public folder.
        final String pathToJSFolder = root + "/Javascript/public/";

        final String juiceModel = loadResources("Default3DModels/Juno.glb");

        // Creation of the clock.
        final TimeScale UTC                    = TimeScalesFactory.getUTC();
        final double    durationOfSimulation   = 100000; // in seconds;
        final double    stepBetweenEachInstant = 60.0; // in seconds
        final AbsoluteDate initialDate = new AbsoluteDate(2004, 1, 1, 0, 0, 00.000,
                TimeScalesFactory.getUTC());
        final AbsoluteDate finalDate = initialDate.shiftedBy(durationOfSimulation);
        final Clock        clock     = new Clock(initialDate, finalDate, UTC, stepBetweenEachInstant);

        final Header header = new Header("Jupiter fly by", clock, pathToJSFolder);

        // Solar system
        final CelestialBody jupiter = CelestialBodyFactory.getJupiter();
        final CelestialBody[] otherBodies = {
                CelestialBodyFactory.getSun(),
                CelestialBodyFactory.getMercury(),
                CelestialBodyFactory.getVenus(),
                CelestialBodyFactory.getEarthMoonBarycenter(),
                CelestialBodyFactory.getMars(),
                CelestialBodyFactory.getSaturn(),
                CelestialBodyFactory.getUranus(),
                CelestialBodyFactory.getNeptune()
        };

        // Frames
        final Frame EME2000     = FramesFactory.getEME2000();
        final Frame jovianFrame = jupiter.getInertiallyOrientedFrame();
        final Frame ICRF        = FramesFactory.getICRF();

        // Integration parameters
        final double minStep           = 1.0;
        final double maxstep           = 7200.0;
        final double positionTolerance = 1.0;
        final double initialStepSize   = 120.0;

        // Initial conditions
        final double x  = 69911000 + 100000000;
        final double y  = -2000000000;
        final double z  = 0;
        final double Vx = 0;
        final double Vy = 50000;
        final double Vz = 0;
        final PVCoordinates initialScWrtJupiter = new PVCoordinates(new Vector3D(x, y, z),
                new Vector3D(Vx, Vy, Vz));

        // Propagator

        final Transform     initialTransform1  = jovianFrame.getTransformTo(EME2000, initialDate);
        final PVCoordinates initialConditions1 = initialTransform1.transformPVCoordinates(initialScWrtJupiter);

        final AbsolutePVCoordinates initialAbsPva1 = new AbsolutePVCoordinates(EME2000, initialDate,
                initialConditions1);

        final KeplerianOrbit  initialOrbit = new KeplerianOrbit(initialAbsPva1, EME2000, Constants.WGS84_EARTH_MU);
        final SpacecraftState initialState = new SpacecraftState(initialOrbit);

        final double[][] tolerances1 = NumericalPropagator.tolerances(positionTolerance, initialOrbit,
                OrbitType.CARTESIAN);

        final AdaptiveStepsizeIntegrator integrator1 =
                new DormandPrince853Integrator(minStep, maxstep, tolerances1[0], tolerances1[1]);
        integrator1.setInitialStepSize(initialStepSize);

        final NumericalPropagator propagator = new NumericalPropagator(integrator1);

        //// Setup the propagation
        propagator.setOrbitType(OrbitType.CARTESIAN);
        propagator.addForceModel(new SingleBodyAbsoluteAttraction(jupiter));

        // Adding the other bodies to the propagation
        for (final CelestialBody body : otherBodies) {
            propagator.addForceModel(new SingleBodyAbsoluteAttraction(body));
        }
        propagator.addForceModel(new InertialForces(ICRF));
        propagator.setIgnoreCentralAttraction(true);
        propagator.setInitialState(initialState);

        final EphemerisGenerator generator = propagator.getEphemerisGenerator();

        propagator.propagate(initialDate, finalDate);

        final BoundedPropagator boundedPropagator = generator.getGeneratedEphemeris();

        final Satellite satellite = Satellite.builder(boundedPropagator)
                                             .withModelPath(juiceModel)
                                             .withColor(Color.MAGENTA)
                                             .withOnlyOnePeriod()
                                             .build();

        final BodyDisplay jupiterDisplay = BodyDisplayFactory.getJupiter();

        final CzmlFile file = new CzmlFileBuilder(output).withHeader(header)
                                                         .withSatellite(satellite)
                                                         .withBodyDisplay(jupiterDisplay)
                                                         .build();

        // Writing the file
        file.write();
    }
}
