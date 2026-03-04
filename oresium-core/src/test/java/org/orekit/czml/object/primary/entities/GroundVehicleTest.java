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

import org.junit.jupiter.api.Test;
import org.orekit.annotation.DefaultDataContext;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.czml.file.AbstractTest;
import org.orekit.czml.object.nonvisual.CzmlModel;
import org.orekit.czml.object.primary.Header;
import org.orekit.czml.object.utils.DateUtils;
import org.orekit.time.AbsoluteDate;
import org.orekit.utils.PVCoordinatesProvider;
import org.orekit.utils.WaypointPVBuilder;

import java.io.IOException;
import java.net.URISyntaxException;

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

    /**
     * Satellite constructor test.
     *
     * @throws IOException the io exception
     * @throws URISyntaxException the uri syntax exception
     */
    @Test
    void GroundVehicleConstructorTest()
        throws IOException,
            URISyntaxException {

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
     * Satellite constructor test.
     *
     * @throws IOException the io exception
     * @throws URISyntaxException the uri syntax exception
     */
    @Test
    void GroundVehicleLabelTest()
        throws IOException,
            URISyntaxException {

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

    private final PVCoordinatesProvider getTrajectory() {

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
}
