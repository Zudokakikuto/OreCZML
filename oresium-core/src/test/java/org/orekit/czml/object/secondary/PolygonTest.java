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

import cesiumlanguagewriter.Cartesian;
import org.junit.jupiter.api.Test;
import org.orekit.annotation.DefaultDataContext;
import org.orekit.czml.file.AbstractTest;
import org.orekit.czml.object.primary.Header;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * The type Polygon test.
 */
public class PolygonTest
    extends
    AbstractTest {

    /** Initialise orekit data. */
    private final double data = initializeOrekitData();

    /** Header. */
    final Header header = dummyHeader();

    /**
     * Polygon constructor test.
     *
     * @throws IOException the io exception
     */
    @Test
    @DefaultDataContext
    void PolygonConstructorTest()
        throws IOException,
            URISyntaxException {

        final List<Cartesian> cartesians = randomCartesian();

        final Polygon polygon = new Polygon(cartesians, header.getClock());

        final String pathFile =
            loadResources("templateFile/object/secondary/PolygonTemplate.txt");

        verifyFileOutput(pathFile, polygon.toString(), 1e-8);
    }

    private List<Cartesian> randomCartesian() {
        final List<Cartesian> toReturn = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            toReturn.add(new Cartesian(i, 2 * i, i));
        }
        return toReturn;
    }
}
