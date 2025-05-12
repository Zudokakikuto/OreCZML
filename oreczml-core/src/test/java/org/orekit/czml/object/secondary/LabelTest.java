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
package org.orekit.czml.object.secondary;

import java.awt.Color;
import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.jupiter.api.Test;
import org.orekit.czml.file.AbstractTest;

import cesiumlanguagewriter.CesiumHorizontalOrigin;
import cesiumlanguagewriter.CesiumLabelStyle;
import cesiumlanguagewriter.CesiumVerticalOrigin;

/**
 * The type Label test.
 */
public class LabelTest
    extends
    AbstractTest {

    /**
     * Label constructor test.
     *
     * @throws IOException the io exception
     */
    @Test
    void LabelConstructorTest()
        throws IOException,
            URISyntaxException {

        loadOrekitData();

        final Label label = new Label("header");

        final Label coverageLabel = new Label("text 1", Color.RED);

        final Label completeLabel =
            new Label("text 2", Color.RED, CesiumHorizontalOrigin.CENTER,
                      CesiumVerticalOrigin.BASELINE, CesiumLabelStyle.FILL,
                      true);

        final String pathFile =
            loadResources("templateFile/secondary/LabelTemplate.txt");
        final String coveragePathFile =
            loadResources("templateFile/secondary/LabelCoverageTemplate.txt");
        final String completePathFile =
            loadResources("templateFile/secondary/LabelCompleteTemplate.txt");

        verifyFileOutput(pathFile, label.toString(), 1e-8);
        verifyFileOutput(coveragePathFile, coverageLabel.toString(), 1e-8);
        verifyFileOutput(completePathFile, completeLabel.toString(), 1e-8);
    }
}
