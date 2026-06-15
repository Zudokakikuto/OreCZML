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

import cesiumlanguagewriter.CesiumHeightReference;
import org.hipparchus.util.FastMath;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.orekit.annotation.DefaultDataContext;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.czml.errors.OresiumException;
import org.orekit.czml.file.AbstractTest;
import org.orekit.czml.object.Position;
import org.orekit.czml.object.PositionType;
import org.orekit.czml.object.primary.Header;
import org.orekit.czml.object.primary.entities.CzmlGroundStation;
import org.orekit.czml.object.primary.entities.Spacecraft;
import org.orekit.czml.object.secondary.Clock;
import org.orekit.czml.object.secondary.Cylinder;
import org.orekit.czml.object.utils.DateUtils;
import org.orekit.frames.TopocentricFrame;
import org.orekit.propagation.BoundedPropagator;
import org.orekit.time.AbsoluteDate;

import java.awt.Color;

/**
 * The type Visibility cone test.
 */
@DefaultDataContext
public class VisibilityConeTest
    extends
    AbstractTest {

    private final double data = initializeOrekitData();

    /** Header. */
    final Header header = dummyHeader();

    // Dates

    /** Start Date. */
    final AbsoluteDate startDate =
        DateUtils.toAbsoluteDate(header.getAvailability().getStart());

    /** Final Date. */
    final AbsoluteDate finalDate = startDate.shiftedBy(60.0);

    /** Coverage cylinder. */
    final Cylinder coverageCylinder =
        new Cylinder(10, 20, 10, Color.RED,
                     new Position(1, 2, 1, PositionType.CARTESIAN_POSITION,
                                  header.getClock()),
                     CesiumHeightReference.CLAMP_TO_GROUND, header.getClock());

    /** Point of toulouse. */
    final GeodeticPoint toulouseFrame =
        new GeodeticPoint(FastMath.toRadians(43.6047),
                          FastMath.toRadians(1.4442), 10);

    /** Frame of toulouse. */
    final TopocentricFrame topocentricToulouse =
        new TopocentricFrame(getEarth(), toulouseFrame, "Toulouse Frame");

    /** Ground station from toulouse. */
    final CzmlGroundStation groundStation =
        new CzmlGroundStation(topocentricToulouse, header.getClock());

    public VisibilityConeTest() {
    }

    /**
     * Visibility cone constructor test. *
     */
    @Test
    @DisplayName("Visibility cone constructor test")
    void VisibilityConeConstructorTest() {

        final GeodeticPoint toulouseFrame =
            new GeodeticPoint(FastMath.toRadians(43.6047),
                              FastMath.toRadians(1.4442), 10);
        final TopocentricFrame topocentricToulouse =
            new TopocentricFrame(getEarth(), toulouseFrame, "Toulouse Frame");

        final BoundedPropagator propagator =
            dummyPropagator(startDate, finalDate, dummyOrbit(startDate));
        final Spacecraft satellite =
            new Spacecraft(propagator, header.getClock());

        final VisibilityCone cone =
            new VisibilityCone(topocentricToulouse, satellite,
                               header.getClock());

        final String pathFile =
            loadResources("templateFile/object/primary/visu/visibilitycone/VisibilityConeTemplate.txt");

        verifyFileOutput(pathFile, cone.toString(), 1e-8);
    }

    @Test
    @DisplayName("Visibility Cone coverage test")
    public void VisibilityConeCoverageConstructorTest() {

        // Build visibility cone for coverage
        final VisibilityCone coverageCone =
            new VisibilityCone("An id", "a name", coverageCylinder,
                               header.getClock());

        // Reference file
        final String coveragePathFile =
            loadResources("templateFile/object/primary/visu/visibilitycone/VisibilityConeCoverageTemplate.txt");

        verifyFileOutput(coveragePathFile, coverageCone.toString(), 1e-8);
    }

    @Test
    @DisplayName("Test constructor of Visibility cone from a cylinder and a spacecraft")
    public void VisibilityConeSpacecraftCylinderConstructorTest() {

        final BoundedPropagator propagator =
            dummyPropagator(startDate, finalDate, dummyOrbit(startDate));
        final Spacecraft satellite =
            new Spacecraft(propagator, header.getClock());

        // Build of the visibility cone from a spacecraft and a cylinder
        final VisibilityCone cylinderSatCone =
            new VisibilityCone("An id", "A name", coverageCylinder, satellite,
                               header.getClock());

        // Reference file
        final String cylinderSatPathFile =
            loadResources("templateFile/object/primary/visu/visibilitycone/VisibilityConeCylinderSatTemplate.txt");

        verifyFileOutput(cylinderSatPathFile, cylinderSatCone.toString(), 1e-8);
    }

    @Test
    @DisplayName("Test Visibility cone constructor from a ground station")
    public void VisibilityConeGroundStationConstructorTest() {

        // Build of the visibility cone from the ground station
        final VisibilityCone groundStationCone =
            new VisibilityCone(groundStation, header.getClock());

        // Reference file
        final String groundStationPathFile =
            loadResources("templateFile/object/primary/visu/visibilitycone/VisibilityConeGroundStationTemplate.txt");

        verifyFileOutput(groundStationPathFile, groundStationCone.toString(),
                         1e-8);
    }

    @Test
    @DisplayName("Test visibility cone constructor from ground station and a spacecraft")
    public void VisibilityConeGroundStationSpacecraftConstructorTest() {

        final BoundedPropagator propagator =
            dummyPropagator(startDate, finalDate, dummyOrbit(startDate));
        final Spacecraft satellite =
            new Spacecraft(propagator, header.getClock());

        // Build of the visibility cone from the ground station
        final VisibilityCone groundStationSatCone =
            new VisibilityCone(groundStation, satellite, header.getClock());

        // Reference file
        final String GroundStationSatPathFile =
            loadResources("templateFile/object/primary/visu/visibilitycone/VisibilityConeGroundStationSatTemplate.txt");

        verifyFileOutput(GroundStationSatPathFile,
                         groundStationSatCone.toString(), 1e-8);
    }

    @Test
    @DisplayName("Test for clone object for the visibility cone object")
    public void CloneObjectVisibilityConeTest() {

        final BoundedPropagator propagator =
            dummyPropagator(startDate, finalDate, dummyOrbit(startDate));

        final Clock clock = header.getClock();

        final Spacecraft spacecraft =
            Spacecraft.builder(propagator, clock).build();

        final VisibilityCone visibilityConeWithSpacecraft =
            new VisibilityCone("An id", "a name", coverageCylinder, spacecraft,
                               clock);
        final VisibilityCone visibilityConeWithoutSpacecraft =
            new VisibilityCone("An id", "a name", coverageCylinder, clock);
        final VisibilityCone visibilityConeWithGroundStation =
            new VisibilityCone(groundStation, clock);
        final VisibilityCone visibilityConeWithGroundStationSpacecraft =
            new VisibilityCone(groundStation, spacecraft, clock);

        // Clones
        final VisibilityCone visibilityConeClonedSpacecraft =
            visibilityConeWithSpacecraft.cloneObject();
        final VisibilityCone visibilityConeClonedWithoutSpacecraft =
            visibilityConeWithoutSpacecraft.cloneObject();
        final VisibilityCone visibilityConeGroundStation =
            visibilityConeWithGroundStation.cloneObject();
        final VisibilityCone visibilityConeClonedWithGroundStationSpacecraft =
            visibilityConeWithGroundStationSpacecraft.cloneObject();

        // Assertions
        Assertions.assertEquals(visibilityConeWithSpacecraft.toString(),
                                visibilityConeClonedSpacecraft.toString());
        Assertions
            .assertEquals(visibilityConeWithoutSpacecraft.toString(),
                          visibilityConeClonedWithoutSpacecraft.toString());
        Assertions.assertEquals(visibilityConeWithGroundStation.toString(),
                                visibilityConeGroundStation.toString());
        Assertions
            .assertEquals(visibilityConeWithGroundStationSpacecraft.toString(),
                          visibilityConeClonedWithGroundStationSpacecraft
                              .toString());
    }

    @Nested
    public class GetterSetterTests {

        @Test
        public void PositionTest() {
            final VisibilityCone visibilityCone =
                new VisibilityCone(groundStation, header.getClock());
            final Position position =
                new Position(4624415.12018318, 116587.90809901741,
                             4376399.285187051, PositionType.CARTESIAN_POSITION,
                             header.getClock());
            Assertions.assertEquals(visibilityCone.getPosition().toString(),
                                    position.toString());
        }

        @Test
        public void SpacecraftTest() {
            final BoundedPropagator propagator =
                dummyPropagator(startDate, finalDate, dummyOrbit(startDate));
            final Spacecraft spacecraft =
                Spacecraft.builder(propagator, header.getClock()).build();

            final VisibilityCone visibilityConeNominal =
                new VisibilityCone(groundStation, spacecraft,
                                   header.getClock());
            final VisibilityCone visibilityConeDegraded =
                new VisibilityCone("An id", "a name", coverageCylinder,
                                   header.getClock());

            // Assertions
            Assertions
                .assertEquals(visibilityConeNominal.getSpacecraft().toString(),
                              spacecraft.toString());
            Assertions.assertThrows(OresiumException.class,
                                    visibilityConeDegraded::getSpacecraft);
        }
    }
}
