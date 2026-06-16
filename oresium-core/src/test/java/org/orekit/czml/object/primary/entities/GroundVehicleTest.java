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

import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.orekit.annotation.DefaultDataContext;
import org.orekit.attitudes.Attitude;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.czml.file.AbstractTest;
import org.orekit.czml.object.nonvisual.CzmlModel;
import org.orekit.czml.object.primary.Header;
import org.orekit.czml.object.secondary.Clock;
import org.orekit.czml.object.secondary.Orientation;
import org.orekit.czml.object.utils.DateUtils;
import org.orekit.frames.FramesFactory;
import org.orekit.propagation.Propagator;
import org.orekit.time.AbsoluteDate;
import org.orekit.utils.AbsolutePVCoordinates;
import org.orekit.utils.PVCoordinates;
import org.orekit.utils.PVCoordinatesProvider;
import org.orekit.utils.TimeStampedAngularCoordinates;
import org.orekit.utils.TimeStampedPVCoordinates;
import org.orekit.utils.WaypointPVBuilder;

import java.awt.Color;

import java.util.ArrayList;
import java.util.List;

/**
 * The type Satellite test.
 */
@DefaultDataContext
public class GroundVehicleTest
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

    final Propagator dummyPropagator =
        dummyPropagator(startDate, finalDate, dummyOrbit(startDate));

    final GroundVehicle dummyVehicle =
        new GroundVehicle(dummyPropagator, startDate, finalDate, getEarth(),
                          60.0);

    /**
     * Satellite constructor test. *
     */
    @Test
    void GroundVehicleConstructorTest() {

        final String planeModel = loadResources("Default3DModels/airplane.glb");
        final CzmlModel model =
            CzmlModel.builder(planeModel, true, header.getClock()).build();

        final PVCoordinatesProvider airplanePVCoordsProvider = getTrajectory();

        GroundVehicleBuilder planeBuilder =
            new GroundVehicleBuilder(airplanePVCoordsProvider, startDate,
                                     finalDate, AbstractTest.getEarth(),
                                     header.getClock().getMultiplier());
        GroundVehicle plane = planeBuilder.withModel(model).build();

        final String pathFile =
            loadResources("templateFile/object/primary/entities/groundvehicle/GroundVehicleTemplate.txt");
        verifyFileOutput(pathFile, plane.toString(), 1e-8);
    }

    /**
     * Satellite constructor test. *
     */
    @Test
    void GroundVehicleLabelTest() {

        final PVCoordinatesProvider airplanePVCoordsProvider = getTrajectory();

        GroundVehicleBuilder planeBuilder =
            new GroundVehicleBuilder(airplanePVCoordsProvider, startDate,
                                     finalDate, AbstractTest.getEarth(),
                                     header.getClock().getMultiplier());
        GroundVehicle plane =
            planeBuilder.withName("Airplane").withDisplayName().build();

        final String pathFile =
            loadResources("templateFile/object/primary/entities/groundvehicle/GroundVehicleLabelTemplate.txt");

        verifyFileOutput(pathFile, plane.toString(), 1e-8);
    }

    private PVCoordinatesProvider getTrajectory() {

        final GeodeticPoint startPoint =
            new GeodeticPoint(Math.toRadians(23.13), Math.toRadians(22.34),
                              10000.0);
        final GeodeticPoint stopPoint =
            new GeodeticPoint(Math.toRadians(24.13), Math.toRadians(23.34),
                              10000.0);
        return WaypointPVBuilder.greatCircleBuilder(AbstractTest.getEarth())
            .addWaypoint(startPoint, startDate)
            .addWaypoint(stopPoint, finalDate).build();
    }

    /** Test for constructor of ground vehicle. */
    @Test
    void groundVehicleConstructorTest() {

        final GroundVehicle groundVehicle =
            new GroundVehicle(dummyPropagator, startDate, finalDate, getEarth(),
                              60.0);

        final String templateFile =
            loadResources("templateFile/object/primary/entities/groundvehicle/GroundVehicleConstructorTemplate.txt");

        verifyFileOutput(templateFile, groundVehicle.toString(), 1e-8);
    }

    /** Test for the builder of ground vehicle. */
    @Test
    void groundVehicleBuilderTest() {

        final GroundVehicle groundVehicle =
            GroundVehicle.builder(dummyPropagator, startDate, finalDate,
                                  getEarth(), 60.0)
                .build();

        final String templateFile =
            loadResources("templateFile/object/primary/entities/groundvehicle/GroundVehicleBuilderTemplate.txt");

        verifyFileOutput(templateFile, groundVehicle.toString(), 1e-8);
    }

    /** test for cloning object. */
    @Test
    void cloningVehicleTest() {
        dummyVehicle.setDisplayAttitude(false);
        final GroundVehicle vehicleCloned = dummyVehicle.cloneObject();
        final String templateFile =
            loadResources("templateFile/object/primary/entities/groundvehicle/GroundVehicleClonedTemplate.txt");

        verifyFileOutput(templateFile, vehicleCloned.toString(), 1e-8);
    }

    /** Test for cloning object with a display attitude. */
    @Test
    void cloningVehicleAttitudeShowTest() {
        dummyVehicle.setDisplayAttitude(true);
        final List<Attitude> attitudes = new ArrayList<>();
        final TimeStampedAngularCoordinates coordinates =
            new TimeStampedAngularCoordinates(startDate,
                                              new PVCoordinates(new Vector3D(1.0,
                                                                             0.0,
                                                                             0.0),
                                                                new Vector3D(0.01,
                                                                             0.02,
                                                                             0.0)),
                                              new PVCoordinates(new Vector3D(3.0,
                                                                             1.0,
                                                                             0.0),
                                                                new Vector3D(0.03,
                                                                             0.00,
                                                                             0.01)));
        attitudes.add(new Attitude(FramesFactory.getEME2000(), coordinates));
        dummyVehicle.setAttitudes(attitudes);

        final GroundVehicle vehicleCloned = dummyVehicle.cloneObject();

        final String templateFile =
            loadResources("templateFile/object/primary/entities/groundvehicle/GroundVehicleCloneAttitudeTemplate.txt");

        verifyFileOutput(templateFile, vehicleCloned.toString(), 1e-8);
    }

    @Nested
    public class GetterSetterTests {

        @Test
        void PVCoordinatesProviderTest() {
            Assertions.assertEquals(dummyPropagator,
                                    dummyVehicle.getPVCoordinatesProvider());
        }

        @Test
        void clockTest() {
            final Clock clock = new Clock(startDate, finalDate, 60.0);
            Assertions.assertEquals(clock.toString(),
                                    dummyVehicle.getClock().toString());
        }

        @Test
        void clockSetTest() {
            final Clock clock = new Clock(startDate, finalDate, 180.0);
            dummyVehicle.setClock(clock);
            Assertions.assertEquals(clock.toString(),
                                    dummyVehicle.getClock().toString());
            dummyVehicle.setClock(new Clock(startDate, finalDate, 60.0));
        }

        @Test
        void startDateTest() {
            Assertions.assertEquals(startDate, dummyVehicle.getStartDate());
        }

        @Test
        void finalDateTest() {
            Assertions.assertEquals(finalDate, dummyVehicle.getFinalDate());
        }

        @Test
        void descriptionTest() {
            final String description =
                "<!--HTML-->\r\n" +
                                       "<p>Id : GROUNDVEHICLE/</p>\r\n" +
                                       "<p>Simulated from : 2024-01-01T00:00:00.000Z to 2024-01-01T00:01:00.000Z</p>";
            Assertions.assertEquals(description, dummyVehicle.getDescription());
        }

        @Test
        void displayAttitudeTest() {
            Assertions.assertTrue(dummyVehicle.getDisplayAttitude());
        }

        @Test
        void orientationTest() {
            final Orientation orientation =
                new Orientation(dummyVehicle.getAttitudes(),
                                dummyVehicle.getFrame());
            Assertions.assertEquals(orientation.toString(),
                                    dummyVehicle.getOrientation().toString());
        }

        @Test
        void colorSetTest() {
            final Color color = new Color(255, 255, 0);
            dummyVehicle.setColor(color);
            Assertions.assertEquals(dummyVehicle.getColor(), color);
        }

        @Test
        void pvCoordinatesProviderSetTest() {
            final TimeStampedPVCoordinates coordinates =
                new TimeStampedPVCoordinates(startDate,
                                             new PVCoordinates(new Vector3D(1.0,
                                                                            0.0,
                                                                            0.0),
                                                               new Vector3D(0.01,
                                                                            0.02,
                                                                            0.0)),
                                             new PVCoordinates(new Vector3D(3.0,
                                                                            1.0,
                                                                            0.0),
                                                               new Vector3D(0.03,
                                                                            0.01,
                                                                            0.01)));
            final PVCoordinatesProvider provider =
                new AbsolutePVCoordinates(FramesFactory.getEME2000(),
                                          coordinates);
            dummyVehicle.setPVCoordinatesProvider(provider);
            Assertions.assertEquals(provider,
                                    dummyVehicle.getPVCoordinatesProvider());
            dummyVehicle.setPVCoordinatesProvider(dummyPropagator);
        }

        @Test
        void orientationSetTest() {
            final Orientation orientation =
                new Orientation(dummyVehicle.getAttitudes(),
                                dummyVehicle.getFrame());
            dummyVehicle.setOrientation(orientation);
            Assertions.assertEquals(orientation.toString(),
                                    dummyVehicle.getOrientation().toString());
        }
    }
}
