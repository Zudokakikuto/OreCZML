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

package org.orekit.czml.adaptors;

import org.orekit.czml.TutorialUtils;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.czml.archi.adaptor.AemAdaptor;
import org.orekit.czml.archi.adaptor.OemAdaptor;
import org.orekit.czml.object.primary.entities.SpacecraftBuilder;
import org.orekit.czml.file.CzmlFile;
import org.orekit.czml.object.primary.entities.CzmlGroundStation;
import org.orekit.czml.object.primary.Header;
import org.orekit.czml.object.primary.entities.Spacecraft;
import org.orekit.czml.object.secondary.Clock;
import org.orekit.czml.object.secondary.Orientation;
import org.orekit.data.DataSource;
import org.orekit.files.ccsds.ndm.ParserBuilder;
import org.orekit.files.ccsds.ndm.adm.aem.Aem;
import org.orekit.files.ccsds.ndm.adm.aem.AemParser;
import org.orekit.files.ccsds.ndm.odm.oem.Oem;
import org.orekit.files.ccsds.ndm.odm.oem.OemParser;
import org.orekit.frames.TopocentricFrame;
import org.orekit.propagation.BoundedPropagator;
import org.orekit.propagation.EphemerisGenerator;
import org.orekit.propagation.Propagator;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScalesFactory;

/**
 * This tutorial provides an example of how an Aem object ban be used to build
 * an orientation from it.
 */
public class AemAdaptorExample {

    private AemAdaptorExample() {
        // empty
    }

    /**
     * Main of the Aem tutorial.
     *
     * @param args arguments of the main function
     * @throws Exception exception to throw
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

        final String OemPath = TutorialUtils.loadResources("oemForAemTuto.xml");
        final String AemPath = TutorialUtils.loadResources("aemForAemTuto.xml");
        final String IssModel =
            TutorialUtils.loadResources("Default3DModels/ISSModel.glb");

        // Creation of the Oem
        final DataSource dataSourceOem = new DataSource(OemPath);
        final ParserBuilder parserBuilderOem = new ParserBuilder();
        final OemParser oemParser = parserBuilderOem.buildOemParser();
        final Oem oem = oemParser.parse(dataSourceOem);

        // Creation of the Aem
        final DataSource dataSourceAem = new DataSource(AemPath);
        final ParserBuilder parserBuilderAem = new ParserBuilder();
        final AemParser aemParser = parserBuilderAem.buildAemParser();
        final Aem aem = aemParser.parse(dataSourceAem);

        // Adaptor for oem
        final OemAdaptor oemAdaptor = new OemAdaptor(oem);
        final Propagator oemPropagator = oemAdaptor.buildPropagator();
        final AbsoluteDate startDate = oemAdaptor.buildStartDate();
        final AbsoluteDate finalDate = oemAdaptor.buildFinalDate();
        final EphemerisGenerator generator =
            oemPropagator.getEphemerisGenerator();
        oemPropagator.propagate(startDate, finalDate);
        final BoundedPropagator oemBoundedPropagator =
            generator.getGeneratedEphemeris();

        // Creation of the clock
        final Clock clock =
            new Clock(startDate, finalDate,
                      TutorialUtils.STEP_BETWEEN_EACH_INSTANT);

        // Creation of the header
        final Header header =
            new Header("Aem Adaptor Example", clock, pathToJSFolder);

        // Creation of the orientation for the satellite with the aem adaptor
        // Careful here, the header must be set before else way the bounded
        // propagator does not have a reference for the timescale.
        final AemAdaptor aemAdaptor = new AemAdaptor(aem);
        final Orientation orientation =
            aemAdaptor.buildOrientation(oemBoundedPropagator, header);

        // Creation of the satellite
        final Spacecraft satellite =
            new SpacecraftBuilder(oemBoundedPropagator, header)
                .withModelPath(IssModel).withOrientation(orientation).build();

        final CzmlGroundStation groundStation =
            new CzmlGroundStation(new TopocentricFrame(TutorialUtils.getEarth(),
                                                       new GeodeticPoint(0, 0,
                                                                         0),
                                                       "Station"),
                                  "", header);

        final CzmlFile file =
            CzmlFile.builder().withHeader(header).withSpacecraft(satellite)
                .withCzmlGroundStation(groundStation).build();

        // Writing the file
        file.write(output);
    }

}
