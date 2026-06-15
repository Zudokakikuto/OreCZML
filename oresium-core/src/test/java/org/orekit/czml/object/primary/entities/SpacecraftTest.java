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
import cesiumlanguagewriter.JulianDate;
import cesiumlanguagewriter.TimeInterval;
import org.hipparchus.geometry.euclidean.threed.Rotation;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.orekit.annotation.DefaultDataContext;
import org.orekit.czml.archi.factory.BodyFactory;
import org.orekit.czml.file.AbstractTest;
import org.orekit.czml.file.CzmlFile;
import org.orekit.czml.object.nonvisual.CzmlModel;
import org.orekit.czml.object.primary.Header;
import org.orekit.czml.object.secondary.Clock;
import org.orekit.czml.object.utils.DateUtils;
import org.orekit.propagation.BoundedPropagator;
import org.orekit.time.AbsoluteDate;

import java.util.ArrayList;
import java.util.List;

/**
 * The type Satellite test.
 */
@DefaultDataContext
public class SpacecraftTest
    extends
    AbstractTest {

    /** Initialise orekit data. */
    private final double data = initializeOrekitData();

    /** Header. */
    final Header header = dummyHeader();

    /** Start Date. */
    final AbsoluteDate startDate =
        DateUtils.toAbsoluteDate(header.getAvailability().getStart());

    final AbsoluteDate finalDate = startDate.shiftedBy(60.0);

    // Build of the spacecraft
    final BoundedPropagator dummyPropagator =
        dummyPropagator(startDate, finalDate, dummyOrbit(startDate));

    final Spacecraft dummySpacecraft =
        Spacecraft.builder(dummyPropagator, header.getClock().getMultiplier())
            .withName("Spacecraft").withDisplayName().build();

    public SpacecraftTest() {
    }

    /**
     * Satellite constructor test. *
     */
    @Test
    void SatelliteConstructorTest() {

        final BoundedPropagator propagator =
            dummyPropagator(startDate, finalDate, dummyOrbit(startDate));
        final Spacecraft spacecraft =
            new Spacecraft(propagator, header.getClock());

        final String IssModel = loadResources("Default3DModels/ISSModel.glb");
        final CzmlModel model =
            CzmlModel.builder(IssModel, true, header.getClock()).build();

        final Spacecraft spacecraftWithModel =
            Spacecraft.builder(propagator, header.getClock()).withModel(model)
                .build();

        final String pathFile =
            loadResources("templateFile/object/primary/entities/spacecraft/SpacecraftTemplate.txt");
        final String pathFileWithModel =
            loadResources("templateFile/object/primary/entities/spacecraft/SpacecraftBuilderWithModelTemplate.txt");

        verifyFileOutput(pathFileWithModel, spacecraftWithModel.toString(),
                         1e-8);
        verifyFileOutput(pathFile, spacecraft.toString(), 1e-8);
    }

    /**
     * Satellite constructor test. *
     */
    @Test
    void SatelliteLabelTest() {

        final BoundedPropagator propagator =
            dummyPropagator(startDate, finalDate, dummyOrbit(startDate));

        final Spacecraft spacecraft =
            Spacecraft.builder(propagator, header.getClock().getMultiplier())
                .withName("Spacecraft").withDisplayName().build();

        final String pathFile =
            loadResources("templateFile/object/primary/entities/spacecraft/SpacecraftLabelTemplate.txt");

        verifyFileOutput(pathFile, spacecraft.toString(), 1e-8);
    }

    /**
     * Attitude constructor test. *
     */
    @Test
    @DefaultDataContext
    void attitudeConstructorTest() {

        final BoundedPropagator propagator =
            dummyPropagator(startDate, finalDate, dummyOrbit(startDate));

        final Spacecraft spacecraft =
            Spacecraft.builder(propagator, header.getClock())
                .withDisplayAttitude().build();

        final String pathFile =
            loadResources("templateFile/object/primary/entities/spacecraft/SpacecraftAttitudeTemplate.txt");

        verifyFileOutput(pathFile, spacecraft.toString(), 1e-8);
    }

    /** Test for the display of the influence sphere. */
    @Test
    @DefaultDataContext
    void influenceSphereDisplayTest() {

        final AbsoluteDate finalDate = startDate.shiftedBy(60.0);

        final Clock clock = new Clock(startDate, finalDate, 1.0);

        // Build of the spacecraft
        final BoundedPropagator propagator =
            dummyPropagator(startDate, finalDate, dummyOrbit(startDate));

        final Spacecraft spacecraft =
            Spacecraft.builder(propagator, header.getClock())
                .withDisplayAttitude().build();

        // Build of two bodies : Earth and Moon
        final Body earth = BodyFactory.getEarth(clock);
        final Body moon = BodyFactory.getMoon(clock);
        final List<Body> bodyList = new ArrayList<>();
        bodyList.add(earth);
        bodyList.add(moon);

        spacecraft.displayInfluenceSphereChanges(bodyList, earth);

        final String spacecraftWithInfluenceSphereTemplate =
            loadResources("templateFile/object/primary/entities/spacecraft/SpacecraftInfluenceSphereTemplate.txt");

        verifyFileOutput(spacecraftWithInfluenceSphereTemplate,
                         spacecraft.toString(), 1e-8);
    }

    /** Test for the getDisplayReferenceSytem. */
    @Test
    @DefaultDataContext
    void displayReferenceSystemTest() {

        final AbsoluteDate finalDate = startDate.shiftedBy(60.0);

        // Build of the spacecraft
        final BoundedPropagator propagator =
            dummyPropagator(startDate, finalDate, dummyOrbit(startDate));

        final Spacecraft spacecraft =
            Spacecraft.builder(propagator, header.getClock())
                .withDisplayAttitude().build();

        spacecraft.displaySpacecraftReferenceSystem();

        final CzmlFile file =
            CzmlFile.builder(header).withSpacecraft(spacecraft).build();

        final String spacecraftTemplateFile =
            loadResources("templateFile/object/primary/entities/spacecraft/SpacecraftCzmlFileDisplayReferenceSystemTemplate.txt");

        verifyFileOutput(spacecraftTemplateFile, file.toString(), 1e-8);
    }

    /** Test for the clone object. */
    @Test
    @DefaultDataContext
    void cloneObjectTests() {

        final AbsoluteDate finalDate = startDate.shiftedBy(60.0);

        // Build of the spacecraft
        final BoundedPropagator propagator =
            dummyPropagator(startDate, finalDate, dummyOrbit(startDate));

        final Spacecraft spacecraft =
            Spacecraft.builder(propagator, header.getClock())
                .withDisplayAttitude().build();

        spacecraft.displaySpacecraftAttitude();

        final Spacecraft clonedSpacecraft = spacecraft.cloneObject();

        final String spacecraftTemplateFile =
            loadResources("templateFile/object/primary/entities/spacecraft/SpacecraftClonedTemplate.txt");

        verifyFileOutput(spacecraftTemplateFile, clonedSpacecraft.toString(),
                         1e-8);

    }

    @Nested
    public class GetterSetterTests {

        @Test
        void orientedTest() {
            Assertions.assertFalse(dummySpacecraft.isOriented());
        }

        @Test
        void orientedTestTrue() {
            dummySpacecraft.setOriented(true);
            Assertions.assertTrue(dummySpacecraft.isOriented());
            dummySpacecraft.setOriented(false);
        }

        @Test
        void displayOnlyOnePeriodTest() {
            Assertions.assertFalse(dummySpacecraft.isDisplayOnlyOnePeriod());
        }

        @Test
        void displayOnlyOnePeriodTrue() {
            dummySpacecraft.setDisplayOnlyOnePeriod(true);
            Assertions.assertTrue(dummySpacecraft.isDisplayOnlyOnePeriod());
            dummySpacecraft.setDisplayOnlyOnePeriod(false);
        }

        @Test
        void displayAttitudeTest() {
            Assertions.assertFalse(dummySpacecraft.isDisplayAttitude());
        }

        @Test
        void displayAttitudeTrue() {
            dummySpacecraft.setDisplayAttitude(true);
            Assertions.assertTrue(dummySpacecraft.isDisplayAttitude());
            dummySpacecraft.setDisplayAttitude(false);
        }

        @Test
        void displayReferenceSystemTest() {
            Assertions.assertFalse(dummySpacecraft.isDisplayReferenceSystem());
        }

        @Test
        void displayReferenceSystemTrue() {
            dummySpacecraft.setDisplayReferenceSystem(true);
            Assertions.assertTrue(dummySpacecraft.isDisplayReferenceSystem());
            dummySpacecraft.setDisplayReferenceSystem(false);
        }

        @Test
        void displayInfluenceSphereChangesTest() {
            Assertions
                .assertFalse(dummySpacecraft.isDisplayInfluenceSphereChanges());
        }

        @Test
        void displayInfluenceSphereChangesTrue() {
            dummySpacecraft.setDisplayInfluenceSphereChanges(true);
            Assertions
                .assertTrue(dummySpacecraft.isDisplayInfluenceSphereChanges());
            dummySpacecraft.setDisplayInfluenceSphereChanges(false);
        }

        @Test
        void intervalInfluenceSheres() {
            Assertions
                .assertNull(dummySpacecraft.getIntervalInfluenceSpheres());
        }

        @Test
        void intervalInfluenceSphereChangesFill() {
            final List<TimeInterval> timeIntervals = new ArrayList<>();
            final TimeInterval interval =
                new TimeInterval(DateUtils.toJulianDate(startDate),
                                 DateUtils.toJulianDate(finalDate));
            timeIntervals.add(interval);
            dummySpacecraft.setIntervalInfluenceSpheres(timeIntervals);
            Assertions.assertEquals(interval, dummySpacecraft
                .getIntervalInfluenceSpheres().get(0));
            dummySpacecraft.setIntervalInfluenceSpheres(null);
        }

        @Test
        void positionInsideInfluenceSheres() {
            Assertions
                .assertNull(dummySpacecraft.getPositionInsideInfluenceSphere());
        }

        @Test
        void positionInsideInfluenceSphereFill() {
            final List<List<Cartesian>> cartesiansListList = new ArrayList<>();
            final List<Cartesian> cartesiansList = new ArrayList<>();
            final Cartesian cartesian = new Cartesian(40, 1, 4);
            cartesiansList.add(cartesian);
            cartesiansListList.add(cartesiansList);
            dummySpacecraft
                .setPositionInsideInfluenceSphere(cartesiansListList);
            Assertions.assertEquals(cartesian, dummySpacecraft
                .getPositionInsideInfluenceSphere().get(0).get(0));
            dummySpacecraft.setPositionInsideInfluenceSphere(null);
        }

        @Test
        void timeInsideInfluenceSphereTest() {
            Assertions
                .assertNull(dummySpacecraft.getTimeInsideInfluenceSphere());
        }

        @Test
        void timeInsideInfluenceSphereFill() {
            final List<List<JulianDate>> julianDateListList = new ArrayList<>();
            final List<JulianDate> julianDateList = new ArrayList<>();
            final JulianDate julianDate = DateUtils.toJulianDate(startDate);
            julianDateList.add(julianDate);
            julianDateListList.add(julianDateList);
            dummySpacecraft.setTimeInsideInfluenceSphere(julianDateListList);
            Assertions.assertEquals(julianDate, dummySpacecraft
                .getTimeInsideInfluenceSphere().get(0).get(0));
            dummySpacecraft.setTimeInsideInfluenceSphere(null);
        }

        @Test
        void optionalRotationTest() {
            Assertions.assertNull(dummySpacecraft.getOptionalRotation());
        }

        @Test
        void optionalRotationNotNull() {
            final Vector3D vector = new Vector3D(2, 1, 1);
            final Vector3D vector2 = new Vector3D(2, 2, 10);
            final Rotation rotation = new Rotation(vector, vector2);
            dummySpacecraft.setOptionalRotation(rotation);
            Assertions.assertEquals(rotation,
                                    dummySpacecraft.getOptionalRotation());
            dummySpacecraft.setOptionalRotation(null);
        }

        @Test
        void startDateTest() {
            Assertions.assertEquals(startDate, dummySpacecraft.getStartDate());
        }

        @Test
        void finalDateTest() {
            Assertions.assertEquals(finalDate, dummySpacecraft.getFinalDate());
        }

        @Test
        void clockMultiplierTest() {
            Assertions.assertEquals(10.0, dummySpacecraft.getClockMultiplier());
        }
    }

}
