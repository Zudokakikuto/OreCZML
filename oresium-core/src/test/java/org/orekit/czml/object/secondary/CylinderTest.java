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
package org.orekit.czml.object.secondary;

import cesiumlanguagewriter.CesiumHeightReference;
import org.hipparchus.util.FastMath;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.czml.file.AbstractTest;
import org.orekit.czml.object.Position;
import org.orekit.czml.object.PositionType;
import org.orekit.czml.object.primary.Header;
import org.orekit.czml.object.primary.entities.CzmlGroundStation;
import org.orekit.czml.object.primary.entities.Spacecraft;
import org.orekit.czml.object.utils.DateUtils;
import org.orekit.frames.TopocentricFrame;
import org.orekit.propagation.BoundedPropagator;
import org.orekit.time.AbsoluteDate;

import java.awt.Color;

/**
 * The type Cylinder test.
 */
class CylinderTest
    extends
    AbstractTest {

    /** Initialise orekit data. */
    private final double data = initializeOrekitData();

    /** Header. */
    final Header header = dummyHeader();

    // Dates

    /** First Date. */
    final AbsoluteDate startDate =
        DateUtils.toAbsoluteDate(header.getAvailability().getStart());

    /** Final date. */
    final AbsoluteDate finalDate = startDate.shiftedBy(60.0);

    // Topocentric

    /** Toulouse point. */
    final GeodeticPoint toulouseFrame =
        new GeodeticPoint(FastMath.toRadians(43.6047),
                          FastMath.toRadians(1.4442), 10);

    /** Toulouse frame. */
    final TopocentricFrame topocentricToulouse =
        new TopocentricFrame(getEarth(), toulouseFrame, "Toulouse Frame");

    /** Dummy cylinder. */
    final Cylinder dummyCylinder =
        new Cylinder(100.0, 1.0, 2.0, new Color(255, 0, 0),
                     new Position(0.0, 1.0, 0.0,
                                  PositionType.CARTESIAN_POSITION,
                                  header.getClock()),
                     CesiumHeightReference.CLAMP_TO_GROUND, header.getClock());

    /**
     * Cylinder constructor test. *
     */
    @Test
    void CylinderConstructorTest() {

        final BoundedPropagator propagator =
            dummyPropagator(startDate, finalDate, dummyOrbit(startDate));
        final Spacecraft spacecraft =
            new Spacecraft(propagator, header.getClock());

        final Cylinder cylinder =
            new Cylinder(topocentricToulouse, spacecraft, 90.0,
                         header.getClock());

        final String pathFile =
            loadResources("templateFile/object/secondary/cylinder/CylinderTemplate.txt");

        verifyFileOutput(pathFile, cylinder.toString(), 1e-8);
    }

    @Test
    @DisplayName("Cylinder coverage constructor test")
    void CylinderCoverageConstructorTest() {
        final Cylinder coverageCylinder =
            new Cylinder(10.0, 20.0, 1.0, Color.BLUE,
                         new Position(1, 45, 20,
                                      PositionType.CARTESIAN_POSITION,
                                      header.getClock()),
                         CesiumHeightReference.CLAMP_TO_GROUND,
                         header.getClock());

        // Reference file
        final String coveragePathFile =
            loadResources("templateFile/object/secondary/cylinder/CylinderCoverageTemplate.txt");

        verifyFileOutput(coveragePathFile, coverageCylinder.toString(), 1e-8);
    }

    @Test
    @DisplayName("Cylinder from ground station constructor test")
    void CylinderGroundStationConstructorTest() {

        final CzmlGroundStation groundStation =
            new CzmlGroundStation(topocentricToulouse, header.getClock());

        final Cylinder groundStationCylinder =
            new Cylinder(groundStation, 80.0, header.getClock());

        // Reference file
        final String groundStationPathFile =
            loadResources("templateFile/object/secondary/cylinder/CylinderGroundStationTemplate.txt");

        verifyFileOutput(groundStationPathFile,
                         groundStationCylinder.toString(), 1e-8);
    }

    @Test
    @DisplayName("Cylinder from topocentric frame constructor test")
    void CylinderTopocentricConstructorTest() {

        final Cylinder topocentricCylinder =
            new Cylinder(topocentricToulouse, 90.0, header.getClock());

        // Reference file
        final String topocentricPathFile =
            loadResources("templateFile/object/secondary/cylinder/CylinderTopocentricTemplate.txt");

        verifyFileOutput(topocentricPathFile, topocentricCylinder.toString(),
                         1e-8);
    }

    @Test
    @DisplayName("Cylinder from topocentric, angle, clock and spacecraft")
    void CylinderTopocentricSpacecraftConstructorTest() {

        final BoundedPropagator dummyPropagator =
            dummyPropagator(startDate, finalDate, dummyOrbit(startDate));
        final Spacecraft dummySpacecraft =
            Spacecraft.builder(dummyPropagator, header.getClock()).build();

        final Cylinder topocentricSpacecraftCylinder =
            new Cylinder(topocentricToulouse, 90.0, header.getClock(),
                         dummySpacecraft);

        // Reference file
        final String templateFile =
            loadResources("templateFile/object/secondary/cylinder/CylinderTopocentricSpacecraftTemplate.txt");

        verifyFileOutput(templateFile, topocentricSpacecraftCylinder.toString(),
                         1e-8);
    }

    @Test
    @DisplayName("Clone function test")
    void CloneObjectTest() {

        final BoundedPropagator dummyPropagator =
            dummyPropagator(startDate, finalDate, dummyOrbit(startDate));
        final Spacecraft dummySpacecraft =
            Spacecraft.builder(dummyPropagator, header.getClock()).build();

        final Cylinder topocentricSpacecraftCylinder =
            new Cylinder(topocentricToulouse, 90.0, header.getClock(),
                         dummySpacecraft);

        final Cylinder clonedCylinder =
            topocentricSpacecraftCylinder.cloneObject();

        Assertions.assertEquals(topocentricSpacecraftCylinder.toString(),
                                clonedCylinder.toString());
    }

    @Test
    @DisplayName("Cloning test with a cylinder with a spacecraft")
    void CloneObjectWithSpacecraftTest() {

        final BoundedPropagator dummyPropagator =
            dummyPropagator(startDate, finalDate, dummyOrbit(startDate));
        final Spacecraft dummySpacecraft =
            Spacecraft.builder(dummyPropagator, header.getClock()).build();

        final Cylinder cylinder =
            new Cylinder(topocentricToulouse, dummySpacecraft, 90.0,
                         dummySpacecraft.getClock());

        final Cylinder clonedCylinder = cylinder.cloneObject();

        // Assertions
        Assertions.assertEquals(cylinder.toString(), clonedCylinder.toString());
    }

    @Test
    @DisplayName("Cloning test without a spacecraft")
    void CloneObjectWithoutSpacecraftTest() {

        final Cylinder cylinder =
            new Cylinder(topocentricToulouse, 90.0, header.getClock());

        final Cylinder clonedCylinder = cylinder.cloneObject();

        // Assertions
        Assertions.assertEquals(cylinder.toString(), clonedCylinder.toString());
    }

    @Test
    @DisplayName("Cloning test with a ground station")
    void CloneObjectWithGroundStationTest() {

        final Clock clock = dummyHeader().getClock();

        final Cylinder cylinder =
            new Cylinder(new CzmlGroundStation(topocentricToulouse, clock),
                         90.0, clock);

        final Cylinder clonedCylinder = cylinder.cloneObject();

        // Assertions
        Assertions.assertEquals(cylinder.toString(), clonedCylinder.toString());
    }

    @Nested
    @DisplayName("Tests for getters and setters")
    class GetterAndSetterTests {

        @Test
        void colorTest() {
            Assertions.assertEquals(new Color(255, 0, 0),
                                    dummyCylinder.getColor());
        }

        @Test
        void bottomRadiusTest() {
            Assertions.assertEquals(2.0, dummyCylinder.getBottomRadius());
        }

        @Test
        void showTest() {
            Assertions.assertTrue(dummyCylinder.getShow());
        }

        @Test
        void topRadiusTest() {
            Assertions.assertEquals(1.0, dummyCylinder.getTopRadius());
        }

        @Test
        void clockTest() {
            Assertions.assertEquals(header.getClock(),
                                    dummyCylinder.getClock());
        }

        @Test
        void lengthTest() {
            Assertions.assertEquals(100.0, dummyCylinder.getLength());
        }

        @Test
        void topocentricTest() {
            final GeodeticPoint point = new GeodeticPoint(50.0, 10.0, 100.0);
            final TopocentricFrame frame =
                new TopocentricFrame(getEarth(), point, "");
            dummyCylinder.setTopocentricFrame(frame);
            Assertions.assertEquals(dummyCylinder.getTopocentricFrame(), frame);
            dummyCylinder.setTopocentricFrame(topocentricToulouse);
        }

        @Test
        void groundStationTest() {
            final CzmlGroundStation station =
                CzmlGroundStation
                    .builder(topocentricToulouse, header.getClock()).build();
            dummyCylinder.setGroundStation(station);
            Assertions.assertEquals(dummyCylinder.getGroundStation().toString(),
                                    station.toString());
        }

        @Test
        void spacecraftTest() {
            final BoundedPropagator dummyPropagator =
                dummyPropagator(startDate, finalDate, dummyOrbit(startDate));
            final Spacecraft spacecraft =
                Spacecraft.builder(dummyPropagator, header.getClock()).build();
            dummyCylinder.setSpacecraft(spacecraft);
            Assertions.assertEquals(dummyCylinder.getSpacecraft().toString(),
                                    spacecraft.toString());
        }
    }
}
