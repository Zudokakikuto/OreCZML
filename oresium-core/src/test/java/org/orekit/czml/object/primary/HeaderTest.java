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
package org.orekit.czml.object.primary;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.orekit.annotation.DefaultDataContext;
import org.orekit.czml.errors.OresiumException;
import org.orekit.czml.file.AbstractTest;
import org.orekit.czml.object.secondary.Clock;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScalesFactory;

/**
 * The type Header test.
 */
public class HeaderTest
    extends
    AbstractTest {

    /** Initialise orekit data. */
    private final double data = initializeOrekitData();

    /** Header. */
    final Header header = dummyHeader();

    /** Header value. */
    final String headerValue = "A header";

    /** Header version number. */
    final String headerVersionNumber = "1.0";

    /**
     * Header constructor test.
     */
    @Test
    @DefaultDataContext
    @DisplayName("Header dummy constructor test")
    void HeaderConstructorTest() {

        final String pathFile =
            loadResources("templateFile/object/primary/header/HeaderTemplate.txt");

        verifyFileOutput(pathFile, header.toString(), 1e-8);
    }

    @Test
    @DisplayName("Header constructor coverage test")
    public void HeaderCoverageConstructorTest() {

        final Header headerCoverage =
            new Header(headerValue, headerVersionNumber, header.getClock());

        // Reference file
        final String coveragePathFile =
            loadResources("templateFile/object/primary/header/HeaderCoverageTemplate.txt");

        verifyFileOutput(coveragePathFile, headerCoverage.toString(), 1e-8);
    }

    @Test
    @DisplayName("Header with a version constructor test")
    public void HeaderVersionConstructorTest() {

        final Header headerVersion =
            new Header(headerValue, headerVersionNumber, header.getClock(), "");

        // Reference file
        final String versionPathFile =
            loadResources("templateFile/object/primary/header/HeaderVersionTemplate.txt");

        verifyFileOutput(versionPathFile, headerVersion.toString(), 1e-8);
    }

    @Test
    @DisplayName("Basic constructor test")
    public void BasicConstructorHeaderTest() {
        final AbsoluteDate startDate =
            new AbsoluteDate(2020, 1, 1, 0, 0, 0.0, TimeScalesFactory.getUTC());
        final AbsoluteDate finalDate = startDate.shiftedBy(60.0);
        final Header basicHeader =
            new Header("Basic header", new Clock(startDate, finalDate, 10.0),
                       "");

        final String versionPathFile =
            loadResources("templateFile/object/primary/header/HeaderBasicConstructorTemplate.txt");

        verifyFileOutput(versionPathFile, basicHeader.toString(), 1e-8);
    }

    @Test
    @DisplayName("Cloning Test")
    public void CloningTest() {

        final Header headerWithExternalResources = dummyHeader();
        headerWithExternalResources
            .setPathToExternalResources(Header.getDefaultResources());
        final Header headerFilled = dummyHeader();
        final Header nullNameAndVersion =
            new Header(null, null, dummyHeader().getClock());
        final Header nullNameClockAndPathNotEqual =
            new Header("Test", "1.0", dummyHeader().getClock());
        nullNameClockAndPathNotEqual
            .setPathToExternalResources(Header.getDefaultResources());

        // Cloning
        final Header clonedHeaderWithExternalResources =
            headerWithExternalResources.cloneObject();
        final Header clonedHeaderFilled = headerFilled.cloneObject();
        final Header clonedNullNameClockAndPathNotEqual =
            nullNameClockAndPathNotEqual.cloneObject();

        // Assertions
        Assertions.assertEquals(headerWithExternalResources.toString(),
                                clonedHeaderWithExternalResources.toString());
        Assertions.assertEquals(headerFilled.toString(),
                                clonedHeaderFilled.toString());
        Assertions.assertEquals(nullNameClockAndPathNotEqual.toString(),
                                clonedNullNameClockAndPathNotEqual.toString());

        Assertions.assertThrows(OresiumException.class,
                                nullNameAndVersion::cloneObject);
    }

    @Test
    @DisplayName("Clone Object Test - Name and Clock not null, path equals DEFAULT_RESOURCES")
    public void
        cloneObjectTest_NameAndClockNotNull_PathEqualsDefaultResources() {
        // Create a header with non-null name and clock
        final AbsoluteDate startDate =
            new AbsoluteDate(2020, 1, 1, 0, 0, 0.0, TimeScalesFactory.getUTC());
        final AbsoluteDate finalDate = startDate.shiftedBy(60.0);
        final Clock clock = new Clock(startDate, finalDate, 10.0);
        final Header originalHeader = new Header("Test Header", "1.0", clock);

        // Set path to external resources to DEFAULT_RESOURCES
        originalHeader.setPathToExternalResources(Header.getDefaultResources());

        // Clone the header
        final Header clonedHeader = originalHeader.cloneObject();

        // Verify the cloned header is equivalent to the original
        Assertions.assertEquals(originalHeader.toString(),
                                clonedHeader.toString());
        Assertions.assertEquals(originalHeader.getName(),
                                clonedHeader.getName());
        Assertions.assertEquals(originalHeader.getClock(),
                                clonedHeader.getClock());
        Assertions.assertEquals(originalHeader.getPathToExternalResources(),
                                clonedHeader.getPathToExternalResources());
    }
}
