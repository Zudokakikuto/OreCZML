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

import cesiumlanguagewriter.CesiumHorizontalOrigin;
import cesiumlanguagewriter.CesiumResourceBehavior;
import cesiumlanguagewriter.NearFarScalar;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.orekit.annotation.DefaultDataContext;
import org.orekit.czml.file.AbstractTest;

import java.awt.Color;

/**
 * The type Billboard test.
 */
class BillboardTest
    extends
    AbstractTest {

    /** Initialise orekit data. */
    private final double data = initializeOrekitData();

    /** The default image string to use. */
    final String imageStr =
        "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAADsMAAA7DAcdvqGQAAACvSURBVDhPrZDRDcMgDAU9GqN0lIzijw6SUbJJygUeNQgSqepJTyHG91LVVpwDdfxM3T9TSl1EXZvDwii471fivK73cBFFQNTT/d2KoGpfGOpSIkhUpgUMxq9DFEsWv4IXhlyCnhBFnZcFEEuYqbiUlNwWgMTdrZ3JbQFoEVG53rd8ztG9aPJMnBUQf/VFraBJeWnLS0RfjbKyLJA8FkT5seDYS1Qwyv8t0B/5C2ZmH2/eTGNNBgMmAAAAAElFTkSuQmCC";

    /**
     * Billboard constructor test.
     */
    @Test
    @DefaultDataContext
    @DisplayName("Billboard constructor test")
    void BillboardConstructorTest() {

        final Billboard billboard = new Billboard(imageStr);

        final String pathFile =
            loadResources("templateFile/object/secondary/billboard/BillboardTemplate.txt");

        verifyFileOutput(pathFile, billboard.toString(), 1e-8);
    }

    @Test
    @DisplayName("Billboard coverage constructor test")
    void BillboardCoverageConstructorTest() {

        final Billboard billboardCoverage = new Billboard(imageStr, 10);

        // Reference file
        final String coveragePathFile =
            loadResources("templateFile/object/secondary/billboard/BillboardCoverageTemplate.txt");

        verifyFileOutput(coveragePathFile, billboardCoverage.toString(), 1e-8);
    }

    @Test
    @DisplayName("Billboard near far constructor test")
    void BillboardNearFarConstructorTest() {
        final NearFarScalar nearFarScalar =
            new NearFarScalar(10.0, 20.0, 100.0, 50.0);

        final Billboard billboardNearFar =
            new Billboard(imageStr, nearFarScalar);

        // Reference file
        final String nearFarPathFile =
            loadResources("templateFile/object/secondary/billboard/BillboardNearFarTemplate.txt");

        verifyFileOutput(nearFarPathFile, billboardNearFar.toString(), 1e-8);
    }

    @Test
    @DisplayName("Complex billboard constructor test")
    void ComplexBillboardConstructorTest() {

        final NearFarScalar nearFarScalar =
            new NearFarScalar(10.0, 20.0, 100.0, 50.0);

        final Billboard complexConstructor =
            new Billboard(CesiumResourceBehavior.LINK_TO,
                          CesiumHorizontalOrigin.CENTER, imageStr, true, 10,
                          Color.RED, nearFarScalar);

        // Reference file
        final String complexConstructorPathFile =
            loadResources("templateFile/object/secondary/billboard/BillboardComplexConstructorTemplate.txt");

        verifyFileOutput(complexConstructorPathFile,
                         complexConstructor.toString(), 1e-8);
    }
}
