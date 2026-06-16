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
package org.orekit.czml.other;

import org.orekit.bodies.GeodeticPoint;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.czml.TutorialUtils;
import org.orekit.czml.file.CzmlFile;
import org.orekit.czml.object.nonvisual.CzmlModel;
import org.orekit.czml.object.nonvisual.CzmlModelBuilder;
import org.orekit.czml.object.primary.Header;
import org.orekit.czml.object.primary.entities.CzmlGroundStation;
import org.orekit.czml.object.primary.entities.GroundVehicle;
import org.orekit.czml.object.primary.entities.GroundVehicleBuilder;
import org.orekit.czml.object.secondary.Clock;
import org.orekit.frames.TopocentricFrame;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScalesFactory;
import org.orekit.utils.PVCoordinatesProvider;
import org.orekit.utils.WaypointPVBuilder;

/**
 * An example of a LEO mission.
 */
public class GroundVehicleExample {

    private GroundVehicleExample() {
        // empty
    }

    /**
     * Main of the LEO satellite tutorial.
     *
     * @param args the args
     * @throws Exception the exception
     */
    public static void main(final String[] args)
        throws Exception {
        // Load orekit data
        TutorialUtils.loadOrekitData();

        // Paths
        final String output = TutorialUtils.generateOutput();
        // !!! Here you need to change the path inside 'generateJsPath' to the
        // path you are using for images or Model.
        // This folder can also be the public folder of your cesium javascript
        // interface.
        final String pathToJSFolder =
            TutorialUtils.generateJSPath(System.getProperty("user.dir") +
                                         "/Javascript/public");

        final String groundStationModel =
            TutorialUtils.loadResources("Default3DModels/ground_Station.glb");

        // Creation of the clock.

        final AbsoluteDate startDate =
            new AbsoluteDate("2025-12-13T02:00:00.000Z",
                             TimeScalesFactory.getUTC());
        final AbsoluteDate finalDate =
            new AbsoluteDate("2025-12-13T22:00:00.000Z",
                             TimeScalesFactory.getUTC());

        final Clock clock =
            new Clock(startDate, finalDate,
                      TutorialUtils.STEP_BETWEEN_EACH_INSTANT);

        final Header header =
            new Header("LEO-Airplane-Intersatvisu-Tutorial", clock,
                       pathToJSFolder);

        final GeodeticPoint planeStartPoint =
            new GeodeticPoint(Math.toRadians(23.13), Math.toRadians(-82.34),
                              10000.0);
        final GeodeticPoint planeStopPoint =
            new GeodeticPoint(Math.toRadians(55.75), Math.toRadians(37.4),
                              10000.0);
        final PVCoordinatesProvider airplanePVCoordProvider =
            getGreatCircleCoordsProvider(TutorialUtils.getEarth(),
                                         planeStartPoint, planeStopPoint,
                                         startDate, finalDate);

        final TopocentricFrame havanaFrame =
            new TopocentricFrame(TutorialUtils.getEarth(), planeStartPoint,
                                 "Havana");
        final TopocentricFrame moscowFrame =
            new TopocentricFrame(TutorialUtils.getEarth(), planeStopPoint,
                                 "Moscow");

        // Creation of a ground Station at Toulouse
        final CzmlGroundStation havanaStation =
            new CzmlGroundStation(havanaFrame, groundStationModel,
                                  header.getClock());
        final CzmlGroundStation moscowStation =
            new CzmlGroundStation(moscowFrame, groundStationModel,
                                  header.getClock());

        final CzmlModel planeModel =
            new CzmlModelBuilder("Javascript\\public\\airplane.glb", false,
                                 header.getClock())
                .withMinimumPixelSize(100).build();

        final GroundVehicleBuilder planeBuilder =
            new GroundVehicleBuilder(airplanePVCoordProvider, startDate,
                                     finalDate, TutorialUtils.getEarth(),
                                     clock.getMultiplier());
        final GroundVehicle plane =
            planeBuilder.withModel(planeModel).withName("RedWings").build();
        plane.displayName();

        // Creation of the file
        final CzmlFile file =
            CzmlFile.builder(header).withCzmlGroundStation(havanaStation)
                .withCzmlGroundStation(moscowStation).withGroundVehicle(plane)
                .build();

        // Writing in the file
        file.write(output);
    }

    private static PVCoordinatesProvider
        getGreatCircleCoordsProvider(final OneAxisEllipsoid earthModel,
                                     final GeodeticPoint startPoint,
                                     final GeodeticPoint stopPoint,
                                     final AbsoluteDate startDate,
                                     final AbsoluteDate stopDate) {

        return WaypointPVBuilder.greatCircleBuilder(earthModel)
            .addWaypoint(startPoint, startDate).addWaypoint(stopPoint, stopDate)
            .build();
    }
}
