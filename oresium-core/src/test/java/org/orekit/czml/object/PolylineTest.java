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
package org.orekit.czml.object;

import cesiumlanguagewriter.CesiumArcType;
import cesiumlanguagewriter.Reference;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.orekit.annotation.DefaultDataContext;
import org.orekit.czml.file.AbstractTest;
import org.orekit.czml.object.primary.Header;

import java.awt.Color;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * The type Polyline test.
 */
public class PolylineTest
    extends
    AbstractTest {

    /** Initialise orekit data. */
    private final double data = initializeOrekitData();

    /** Header. */
    private final Header header = dummyHeader();

    /**
     * Polyline constructor test.
     *
     * @throws IOException the io exception
     */
    @Test
    @DefaultDataContext
    void PolylineConstructorTest()
        throws IOException,
            URISyntaxException {

        final Polyline polyline =
            Polyline.nonVectorBuilder(header.getClock()).build();

        final String pathFile =
            loadResources("templateFile/object/unclassed/polyline/PolylineTemplate.txt");

        verifyFileOutput(pathFile, polyline.toString(), 1e-8);
    }

    @Test
    @DisplayName("Polyline non vector constructor test")
    public void PolylineNonVectorConstructorTest()
        throws URISyntaxException,
            IOException {

        final Polyline polylineNonVector =
            Polyline.nonVectorBuilder(header.getClock()).withColor(Color.ORANGE)
                .withArcType(CesiumArcType.NONE).withShow(true).withWidth(10.0)
                .withFarDistance(10.0).withNearDistance(1.0)
                .withFirstReference(new Reference("sat#position"))
                .withSecondReference(new Reference("groundstation#position"))
                .build();

        // Reference file
        final String nonVectorPathFile =
            loadResources("templateFile/object/unclassed/polyline/PolylineNonVectorTemplate.txt");

        verifyFileOutput(nonVectorPathFile, polylineNonVector.toString(), 1e-8);
    }
}
