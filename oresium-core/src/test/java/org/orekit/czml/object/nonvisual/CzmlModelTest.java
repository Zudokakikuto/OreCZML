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
package org.orekit.czml.object.nonvisual;

import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.jupiter.api.Test;
import org.orekit.annotation.DefaultDataContext;
import org.orekit.czml.file.AbstractTest;
import org.orekit.czml.object.primary.Header;

/**
 * The type Czml model test.
 */
public class CzmlModelTest
    extends
    AbstractTest {

    /** Initialise orekit data. */
    private final double data = initializeOrekitData();

    private final Header header = dummyHeader();

    /**
     * Czml model constructor test.
     *
     * @throws IOException the io exception
     */
    @Test
    @DefaultDataContext
    void CzmlModelConstructorTest()
        throws IOException,
            URISyntaxException {

        final CzmlModel modelToTest =
            new CzmlModel(loadResources("Default3DModels/ISSModel.glb"), false,
                          header.getClock());

        final String pathFile =
            loadResources("templateFile/nonvisual/czmlmodel/CzmlModelTemplate.txt");

        verifyFileOutput(pathFile, modelToTest.toString(), 1e-8);
    }
}
