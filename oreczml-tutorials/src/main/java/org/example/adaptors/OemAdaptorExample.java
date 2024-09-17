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
package org.example.adaptors;

import org.example.TutorialUtils;
import org.orekit.czml.archi.adaptor.OemAdaptor;
import org.orekit.czml.object.primary.Header;
import org.orekit.czml.object.primary.Satellite;
import org.orekit.czml.object.secondary.Clock;
import org.orekit.czml.file.CzmlFile;
import org.orekit.czml.file.CzmlFileBuilder;
import org.orekit.data.DataSource;
import org.orekit.files.ccsds.ndm.ParserBuilder;
import org.orekit.files.ccsds.ndm.odm.oem.Oem;
import org.orekit.files.ccsds.ndm.odm.oem.OemParser;
import org.orekit.propagation.BoundedPropagator;
import org.orekit.propagation.EphemerisGenerator;
import org.orekit.propagation.Propagator;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScale;
import org.orekit.time.TimeScalesFactory;

import static org.example.TutorialUtils.loadResources;

public class OemAdaptorExample {

    private OemAdaptorExample() {
        // empty
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

        final String OemPath  = loadResources("oemForOemTuto.xml");
        final String IssModel = loadResources("Default3DModels/ISSModel.glb");

        // Creation of the Oem
        final DataSource    dataSource    = new DataSource(OemPath);
        final ParserBuilder parserBuilder = new ParserBuilder();
        final OemParser     oemParser     = parserBuilder.buildOemParser();
        final Oem           oem           = oemParser.parse(dataSource);

        // Creation of oem
        final OemAdaptor         adaptor       = new OemAdaptor(oem);
        final Propagator         oemPropagator = adaptor.buildPropagator();
        final AbsoluteDate       startDate     = adaptor.buildStartDate();
        final AbsoluteDate       finalDate     = adaptor.buildFinalDate();
        final EphemerisGenerator generator     = oemPropagator.getEphemerisGenerator();
        oemPropagator.propagate(startDate, finalDate);
        final BoundedPropagator oemBoundedPropagator = generator.getGeneratedEphemeris();

        // Creation of the clock
        final TimeScale UTC                    = TimeScalesFactory.getUTC();
        final double    stepBetweenEachInstant = 60.0; // in seconds
        final Clock     clock                  = new Clock(startDate, finalDate, UTC, stepBetweenEachInstant);

        // Creation of the header
        final Header header = new Header("Oem Adaptor Example", clock, pathToJSFolder);

        // Creation of the satellite
        final Satellite satellite = Satellite.builder(oemBoundedPropagator)
                                             .withModelPath(IssModel)
                                             .withOnlyOnePeriod()
                                             .build();

        final CzmlFile file = CzmlFile.builder(output).withHeader(header)
                                                         .withSatellite(satellite)
                                                         .build();

        file.write();
    }
}
