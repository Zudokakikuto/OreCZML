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

import org.hipparchus.ode.nonstiff.AdaptiveStepsizeIntegrator;
import org.hipparchus.ode.nonstiff.DormandPrince853Integrator;
import org.hipparchus.util.FastMath;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.orekit.czml.file.AbstractTest;
import org.orekit.czml.object.Polyline;
import org.orekit.czml.object.primary.Header;
import org.orekit.czml.object.primary.entities.Constellation;
import org.orekit.czml.object.primary.entities.Spacecraft;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The type Inter sat visu test.
 */
public class InterSatVisuTest
    extends
    AbstractTest {

    private final double data = initializeOrekitData();

    // Dates

    /** Start date. */
    final AbsoluteDate startDate =
        new AbsoluteDate(2024, 3, 15, 0, 0, 0.0, TimeScalesFactory.getUTC());

    /** Final date. */
    final AbsoluteDate finalDate = startDate.shiftedBy(32 * 3600);

    /** Clock. */
    final Clock clock = new Clock(startDate, finalDate, 10.0);

    /** Header. */
    final Header header = new Header("Test constructors inter sat", clock);

    // Orbits

    /** First orbit. */
    final KeplerianOrbit firstOrbit =
        new KeplerianOrbit(7878000, 0, FastMath.toRadians(0),
                           FastMath.toRadians(0), FastMath.toRadians(0),
                           FastMath.toRadians(0), PositionAngleType.MEAN,
                           FramesFactory.getEME2000(), startDate,
                           Constants.WGS84_EARTH_MU);

    /** Second orbit. */
    final KeplerianOrbit secondOrbit =
        new KeplerianOrbit(7078000, 0, FastMath.toRadians(20),
                           FastMath.toRadians(45), FastMath.toRadians(0),
                           FastMath.toRadians(0), PositionAngleType.MEAN,
                           FramesFactory.getEME2000(), startDate,
                           Constants.WGS84_EARTH_MU);

    /**
     * Inter sat visu constructor test.
     *
     * @throws IOException the io exception
     * @throws URISyntaxException the uri syntax exception
     */
    @Test
    @DisplayName("Inter sat visu constructor test")
    void InterSatVisuConstructorTest()
        throws IOException,
            URISyntaxException {

        final List<Spacecraft> satellites = new ArrayList<>();
        final Spacecraft firstSat =
            spacecraftFromOrbit(startDate, finalDate, firstOrbit);
        final Spacecraft secondSat =
            spacecraftFromOrbit(startDate, finalDate, secondOrbit);
        firstSat.setOrbitColor(new Color(255, 0, 0, 255));
        secondSat.setOrbitColor(new Color(255, 127, 0, 255));
        satellites.add(firstSat);
        satellites.add(secondSat);

        final InterSatVisu interSatVisu =
            InterSatVisu
                .builder(firstSat, secondSat, finalDate, header.getClock())
                .build();

        final String pathFile =
            loadResources("templateFile/object/primary/visu/InterSatVisuTemplate.txt");

        verifyFileOutput(pathFile, interSatVisu.toString(), 1e-8);

        Assertions.assertEquals(
                                secondSat.getSpaceCraftStates().get(0)
                                    .getPVCoordinates().toString(),
                                interSatVisu.getSatellite2()
                                    .getSpaceCraftStates().get(0)
                                    .getPVCoordinates().toString());
        Assertions.assertEquals(startDate, interSatVisu.getStartDate());
        Assertions.assertEquals(finalDate, interSatVisu.getFinalDate());
        Assertions.assertEquals(
                                firstSat.getSpaceCraftStates().get(0)
                                    .getPVCoordinates().toString(),
                                interSatVisu.getInitialState()
                                    .getPVCoordinates().toString());
        Assertions
            .assertEquals(Polyline.nonVectorBuilder(header.getClock()).build()
                .getClock().toString(),
                          interSatVisu.getPolyline().getClock().toString());
        Assertions.assertEquals(new ArrayList<>(Arrays
            .asList(true, false, true, false, true, false, true)),
                                interSatVisu.getBooleanList());

    }

    @Test
    @DisplayName("Inter sat visu with builder constructor test")
    public void InterSatVisuBuilderConstructorTets()
        throws URISyntaxException,
            IOException {

        // Bounded Propagators
        final Spacecraft firstSpacecraft =
            spacecraftFromOrbit(startDate, finalDate, firstOrbit);
        final Spacecraft secondSpacecraft =
            spacecraftFromOrbit(startDate, finalDate, secondOrbit);

        final String pathFile =
            loadResources("templateFile/object/primary/visu/InterSatVisuTemplate.txt");
        final InterSatVisu interSatVisuBuilder =
            InterSatVisu.builder(firstSpacecraft, secondSpacecraft, finalDate,
                                 header.getClock())
                .build();
        verifyFileOutput(pathFile, interSatVisuBuilder.toString(), 1e-8);
    }

    @Test
    @DisplayName("Inter Sat visu with propagators")
    public void InterSatVisuPropagatorConstructorTest()
        throws URISyntaxException,
            IOException {

        // Spacecrafts
        final Spacecraft spacecraft1 =
            spacecraftFromOrbit(startDate, finalDate, firstOrbit);
        final Spacecraft spacecraft2 =
            spacecraftFromOrbit(startDate, finalDate, secondOrbit);

        // Propagators
        final BoundedPropagator propagator1 =
            spacecraft1.getSpacecraftBoundedPropagator();
        final BoundedPropagator propagator2 =
            spacecraft2.getSpacecraftBoundedPropagator();
        final List<BoundedPropagator> propagators = new ArrayList<>();
        propagators.add(propagator1);
        propagators.add(propagator2);

        // Build inter sat visu with propagators
        final InterSatVisu interSatVisuPropagators =
            InterSatVisu.builder(propagators, finalDate, header.getClock())
                .build();

        // Reference files
        final String propagatorsInterSatPathFile =
            loadResources("templateFile/object/primary/visu/InterSatVisuPropagatorsTemplate.txt");

        verifyFileOutput(propagatorsInterSatPathFile,
                         interSatVisuPropagators.toString(), 1e-8);
        Assertions.assertEquals(propagators,
                                interSatVisuPropagators.getPropagators());
    }

    @Test
    @DisplayName("Inter Sat visu with builder and propagators")
    public void InterSatVisuBuilderPropagatorConstructorTest()
        throws URISyntaxException,
            IOException {

        // Spacecrafts
        final Spacecraft spacecraft1 =
            spacecraftFromOrbit(startDate, finalDate, firstOrbit);
        final Spacecraft spacecraft2 =
            spacecraftFromOrbit(startDate, finalDate, secondOrbit);

        // Propagators
        final BoundedPropagator propagator1 =
            spacecraft1.getSpacecraftBoundedPropagator();
        final BoundedPropagator propagator2 =
            spacecraft2.getSpacecraftBoundedPropagator();
        final List<BoundedPropagator> propagators = new ArrayList<>();
        propagators.add(propagator1);
        propagators.add(propagator2);

        // Build the inter sat visu with builder and propagators
        final InterSatVisu interSatVisuPropagatorsBuilder =
            InterSatVisu.builder(propagators, finalDate, header.getClock())
                .build();

        // Reference file
        final String propagatorsInterSatPathFile =
            loadResources("templateFile/object/primary/visu/InterSatVisuPropagatorsTemplate.txt");

        verifyFileOutput(propagatorsInterSatPathFile,
                         interSatVisuPropagatorsBuilder.toString(), 1e-8);
    }

    @Test
    @DisplayName("Inter sat visu with builder propagators and ID")
    public void InterSatVisuBuilderPropagatorsIdConstructorTest()
        throws URISyntaxException,
            IOException {

        // Spacecrafts
        final Spacecraft spacecraft1 =
            spacecraftFromOrbit(startDate, finalDate, firstOrbit);
        final Spacecraft spacecraft2 =
            spacecraftFromOrbit(startDate, finalDate, secondOrbit);

        // Propagators
        final BoundedPropagator propagator1 =
            spacecraft1.getSpacecraftBoundedPropagator();
        final BoundedPropagator propagator2 =
            spacecraft2.getSpacecraftBoundedPropagator();
        final List<BoundedPropagator> propagators = new ArrayList<>();
        propagators.add(propagator1);
        propagators.add(propagator2);

        // Build the inter sat visu with propagators and ID
        final InterSatVisu interSatVisuPropagatorsID =
            InterSatVisu.builder(propagators, finalDate, header.getClock())
                .withCustomId("CustomIDTest").build();

        // Reference file
        final String propagatorsIDInterSatPathFile =
            loadResources("templateFile/object/primary/visu/InterSatVisuPropagatorsIDTemplate.txt");

        verifyFileOutput(propagatorsIDInterSatPathFile,
                         interSatVisuPropagatorsID.toString(), 1e-8);
    }

    @Test
    @DisplayName("Inter sat visu constellation builder")
    public void InterSatVisuConstellationBuilder()
        throws URISyntaxException,
            IOException {

        // Spacecrafts
        final Spacecraft spacecraft1 =
            spacecraftFromOrbit(startDate, finalDate, firstOrbit);
        final Spacecraft spacecraft2 =
            spacecraftFromOrbit(startDate, finalDate, secondOrbit);

        // Propagators
        final BoundedPropagator propagator1 =
            spacecraft1.getSpacecraftBoundedPropagator();
        final BoundedPropagator propagator2 =
            spacecraft2.getSpacecraftBoundedPropagator();
        final List<BoundedPropagator> propagators = new ArrayList<>();
        propagators.add(propagator1);
        propagators.add(propagator2);

        final InterSatVisu interSatVisuFromConstellationBuilder =
            InterSatVisu.builder(Constellation
                .builder(propagators, finalDate, header.getClock()).build(),
                                 finalDate, header.getClock())
                .build();

        // Reference file
        final String constellationInterSatPathFile =
            loadResources("templateFile/object/primary/visu/InterSatVisuConstellationTemplate.txt");

        verifyFileOutput(constellationInterSatPathFile,
                         interSatVisuFromConstellationBuilder.toString(), 1e-8);
        Assertions.assertEquals(new ArrayList<>(Arrays
            .asList(spacecraft1.getId(), spacecraft2.getId())),
                                interSatVisuFromConstellationBuilder
                                    .getIdsSatellites());
    }

    @Test
    @DisplayName("Inter sat visu from constructor")
    public void InterSatVisuFromConstructor()
        throws URISyntaxException,
            IOException {

        // Spacecrafts
        final Spacecraft spacecraft1 =
            spacecraftFromOrbit(startDate, finalDate, firstOrbit);
        final Spacecraft spacecraft2 =
            spacecraftFromOrbit(startDate, finalDate, secondOrbit);

        // Builder the inter sat visu from the constructor
        final InterSatVisu interSatVisuFromConstructor =
            new InterSatVisu(spacecraft1, spacecraft2, finalDate);

        // Reference file
        final String constructorInterSatPathFile =
            loadResources("templateFile/object/primary/visu/InterSatVisuFromConstructorTemplate.txt");

        verifyFileOutput(constructorInterSatPathFile,
                         interSatVisuFromConstructor.toString(), 1e-8);
    }

    @Test
    @DisplayName("Inter Sat Visu from propagators constructors")
    public void InterSatVisuPropagatorsConstructorTest()
        throws URISyntaxException,
            IOException {

        // Spacecrafts
        final Spacecraft spacecraft1 =
            spacecraftFromOrbit(startDate, finalDate, firstOrbit);
        final Spacecraft spacecraft2 =
            spacecraftFromOrbit(startDate, finalDate, secondOrbit);

        // Propagators
        final BoundedPropagator propagator1 =
            spacecraft1.getSpacecraftBoundedPropagator();
        final BoundedPropagator propagator2 =
            spacecraft2.getSpacecraftBoundedPropagator();
        final List<BoundedPropagator> propagators = new ArrayList<>();
        propagators.add(propagator1);
        propagators.add(propagator2);

        // Build the inter sat visu from propagator
        final InterSatVisu interSatVisuFromPropagators =
            new InterSatVisu(propagators, finalDate, clock);

        // Reference file
        final String constructorPropagatorsInterSatPathFile =
            loadResources("templateFile/object/primary/visu/InterSatVisuFromPropagatorsConstructorTemplate.txt");

        verifyFileOutput(constructorPropagatorsInterSatPathFile,
                         interSatVisuFromPropagators.toString(), 1e-8);
    }

    @Test
    @DisplayName("Inter sat visu from constellation constructor")
    public void InterSatVisuConstellationConstructorTest()
        throws URISyntaxException,
            IOException {

        // Spacecrafts
        final Spacecraft spacecraft1 =
            spacecraftFromOrbit(startDate, finalDate, firstOrbit);
        final Spacecraft spacecraft2 =
            spacecraftFromOrbit(startDate, finalDate, secondOrbit);

        // Propagators
        final BoundedPropagator propagator1 =
            spacecraft1.getSpacecraftBoundedPropagator();
        final BoundedPropagator propagator2 =
            spacecraft2.getSpacecraftBoundedPropagator();
        final List<BoundedPropagator> propagators = new ArrayList<>();
        propagators.add(propagator1);
        propagators.add(propagator2);

        // Build the inter sat visu from constellation and constructor
        final Constellation constellation =
            Constellation.builder(propagators, finalDate, clock).build();
        final InterSatVisu interSatVisuFromConstellation =
            new InterSatVisu(constellation, finalDate, header.getClock());

        // Reference file
        final String constructorConstellationInterSatPathFile =
            loadResources("templateFile/object/primary/visu/InterSatVisuFromConstellationConstructorTemplate.txt");

        verifyFileOutput(constructorConstellationInterSatPathFile,
                         interSatVisuFromConstellation.toString(), 1e-8);
    }

    private Spacecraft spacecraftFromOrbit(final AbsoluteDate startDate,
                                           final AbsoluteDate finalDate,
                                           final KeplerianOrbit orbit)
        throws URISyntaxException,
            IOException {

        final SpacecraftState firstState = new SpacecraftState(orbit);

        final double[][] tolerances =
            NumericalPropagator.tolerances(10, orbit, OrbitType.CARTESIAN);

        final AdaptiveStepsizeIntegrator integrator =
            new DormandPrince853Integrator(0.001, 1000.0, tolerances[0],
                                           tolerances[1]);

        final NormalizedSphericalHarmonicsProvider provider =
            GravityFieldFactory.getNormalizedProvider(10, 10);
        final ForceModel holmesFeatherstone =
            new HolmesFeatherstoneAttractionModel(FramesFactory.getEME2000(),
                                                  provider);

        final NumericalPropagator propagator =
            new NumericalPropagator(integrator);

        propagator.setOrbitType(OrbitType.CARTESIAN);
        propagator.addForceModel(holmesFeatherstone);
        propagator.setInitialState(firstState);
        final EphemerisGenerator firstGenerator =
            propagator.getEphemerisGenerator();
        propagator.propagate(startDate, finalDate);
        final BoundedPropagator boundedPropagator =
            firstGenerator.getGeneratedEphemeris();
        return new Spacecraft(boundedPropagator, header.getClock());
    }
}
