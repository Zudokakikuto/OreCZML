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
package org.orekit.czml.object.primary;

import org.hipparchus.ode.nonstiff.AdaptiveStepsizeIntegrator;
import org.hipparchus.ode.nonstiff.DormandPrince853Integrator;
import org.hipparchus.util.FastMath;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.orekit.czml.file.AbstractTest;
import org.orekit.czml.object.Polyline;
import org.orekit.czml.object.primary.entities.Satellite;
import org.orekit.czml.object.primary.visu.InterSatVisu;
import org.orekit.czml.object.secondary.Clock;
import org.orekit.forces.ForceModel;
import org.orekit.forces.gravity.HolmesFeatherstoneAttractionModel;
import org.orekit.forces.gravity.potential.GravityFieldFactory;
import org.orekit.forces.gravity.potential.NormalizedSphericalHarmonicsProvider;
import org.orekit.frames.FramesFactory;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The type Inter sat visu test.
 */
public class InterSatVisuTest extends AbstractTest {

    /**
     * Inter sat visu constructor test.
     *
     * @throws IOException        the io exception
     * @throws URISyntaxException the uri syntax exception
     */
    @Test
    void InterSatVisuConstructorTest() throws IOException, URISyntaxException {

        loadOrekitData();

        final AbsoluteDate startDate = new AbsoluteDate(2024, 3, 15, 0, 0, 0.0, TimeScalesFactory.getUTC());
        final AbsoluteDate finalDate = startDate.shiftedBy(32 * 3600);

        final Clock clock = new Clock(startDate, finalDate, TimeScalesFactory.getUTC(),
                60.0);

        final Header header = new Header("Test constructors inter sat", clock);

        final KeplerianOrbit firstOrbit = new KeplerianOrbit(7878000, 0, FastMath.toRadians(0), FastMath.toRadians(0),
                FastMath.toRadians(0), FastMath.toRadians(0), PositionAngleType.MEAN, FramesFactory.getEME2000(),
                startDate,
                Constants.WGS84_EARTH_MU);
        final SpacecraftState firstState = new SpacecraftState(firstOrbit);

        //// Build of the first satellite with an orbit with 40 degree inclination and omega of 90 degrees
        // Build of a LEO orbit
        final KeplerianOrbit secondOrbit = new KeplerianOrbit(7078000, 0, FastMath.toRadians(20),
                FastMath.toRadians(45), FastMath.toRadians(0), FastMath.toRadians(0), PositionAngleType.MEAN,
                FramesFactory.getEME2000(),
                startDate, Constants.WGS84_EARTH_MU);
        final SpacecraftState secondState = new SpacecraftState(secondOrbit);

        // Build of the propagator


        final double[][] tolerances1 = NumericalPropagator.tolerances(10, firstOrbit,
                OrbitType.CARTESIAN);
        final double[][] tolerances2 = NumericalPropagator.tolerances(10, secondOrbit,
                OrbitType.CARTESIAN);
        final AdaptiveStepsizeIntegrator integrator1 = new DormandPrince853Integrator(0.001,
                1000.0, tolerances1[0],
                tolerances1[1]);
        final AdaptiveStepsizeIntegrator integrator2 = new DormandPrince853Integrator(0.001,
                1000.0, tolerances2[0],
                tolerances2[1]);

        final NormalizedSphericalHarmonicsProvider provider = GravityFieldFactory.getNormalizedProvider(10,
                10);
        final ForceModel holmesFeatherstone = new HolmesFeatherstoneAttractionModel(FramesFactory.getEME2000(),
                provider);

        final NumericalPropagator firstPropagator  = new NumericalPropagator(integrator1);
        final NumericalPropagator secondPropagator = new NumericalPropagator(integrator2);

        firstPropagator.setOrbitType(OrbitType.CARTESIAN);
        firstPropagator.addForceModel(holmesFeatherstone);
        firstPropagator.setInitialState(firstState);
        final EphemerisGenerator firstGenerator = firstPropagator.getEphemerisGenerator();
        firstPropagator.propagate(startDate, finalDate);
        final BoundedPropagator firstBoundedPropagator = firstGenerator.getGeneratedEphemeris();

        secondPropagator.setOrbitType(OrbitType.CARTESIAN);
        secondPropagator.addForceModel(holmesFeatherstone);
        secondPropagator.setInitialState(secondState);
        final EphemerisGenerator secondGenerator = secondPropagator.getEphemerisGenerator();
        secondPropagator.propagate(startDate, finalDate);
        final BoundedPropagator secondBoundedPropagator = secondGenerator.getGeneratedEphemeris();

        final List<BoundedPropagator> propagators = new ArrayList<>();
        propagators.add(firstBoundedPropagator);
        propagators.add(secondBoundedPropagator);

        final List<Satellite> satellites = new ArrayList<>();
        final Satellite       firstSat   = new Satellite(firstBoundedPropagator, header);
        final Satellite       secondSat  = new Satellite(secondBoundedPropagator, header);
        firstSat.setOrbitColor(new Color(255, 0, 0, 255));
        secondSat.setOrbitColor(new Color(255, 127, 0, 255));
        satellites.add(firstSat);
        satellites.add(secondSat);

        final InterSatVisu interSatVisu        = InterSatVisu.builder(firstSat, secondSat, finalDate, header)
                                                             .build();
        final InterSatVisu interSatVisuBuilder = InterSatVisu.builder(firstSat, secondSat, finalDate, header)
                                                             .build();

        final InterSatVisu interSatVisuPropagators        = InterSatVisu.builder(propagators, finalDate, header)
                                                                        .build();
        final InterSatVisu interSatVisuPropagatorsBuilder = InterSatVisu.builder(propagators, finalDate, header)
                                                                        .build();

        final InterSatVisu interSatVisuPropagatorsID = InterSatVisu.builder(propagators, finalDate, header)
                                                                   .withCustomId("CustomIDTest")
                                                                   .build();

        final InterSatVisu interSatVisuFromConstellationBuilder = InterSatVisu.builder(
                                                                                      new Constellation(propagators, finalDate, header), finalDate, header)
                                                                              .build();

        final String pathFile = loadResources("templateFile/primary/InterSatVisuTemplate.txt");
        final String propagatorsInterSatPathFile = loadResources(
                "templateFile/primary/InterSatVisuPropagatorsTemplate.txt");
        final String propagatorsIDInterSatPathFile = loadResources(
                "templateFile/primary/InterSatVisuPropagatorsIDTemplate.txt");
        final String constellationInterSatPathFile = loadResources(
                "templateFile/primary/InterSatVisuConstellationTemplate.txt");

        Path path  = Path.of(pathFile);
        Path path1 = Path.of(propagatorsInterSatPathFile);


        Assertions.assertEquals(Files.readString(path), interSatVisu.toString());
        Assertions.assertEquals(Files.readString(path), interSatVisuBuilder.toString());

        Assertions.assertEquals(Files.readString(path1),
                interSatVisuPropagators.toString());
        Assertions.assertEquals(Files.readString(path1),
                interSatVisuPropagatorsBuilder.toString());

        Assertions.assertEquals(Files.readString(Path.of(propagatorsIDInterSatPathFile)),
                interSatVisuPropagatorsID.toString());

        Assertions.assertEquals(Files.readString(Path.of(constellationInterSatPathFile)),
                interSatVisuFromConstellationBuilder.toString());

        // Getters coverage

        Assertions.assertEquals(secondSat.getSpaceCraftStates()
                                         .get(0)
                                         .getPVCoordinates(), interSatVisu.getSatellite2()
                                                                          .getSpaceCraftStates()
                                                                          .get(0)
                                                                          .getPVCoordinates());
        Assertions.assertEquals(startDate, interSatVisu.getStartDate());
        Assertions.assertEquals(finalDate, interSatVisu.getStopDate());
        Assertions.assertEquals(firstSat.getSpaceCraftStates()
                                        .get(0)
                                        .getPVCoordinates(), interSatVisu.getInitialState()
                                                                         .getPVCoordinates());
        Assertions.assertEquals(propagators, interSatVisuPropagators.getPropagators());
        Assertions.assertEquals(new ArrayList<>(Arrays.asList(firstSat.getId(), secondSat.getId())),
                interSatVisuFromConstellationBuilder.getIdsSatellites());
        Assertions.assertEquals(new Polyline(header).getAvailability(), interSatVisu.getPolyline()
                                                                                    .getAvailability());
        Assertions.assertEquals(new ArrayList<>(
                Arrays.asList(true, false, true, false, true, false, false, true, false, true, false, true, false, true,
                        false, true, false, true, false)), interSatVisu.getBooleanList());
    }
}
