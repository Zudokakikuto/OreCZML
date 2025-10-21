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

import org.hipparchus.geometry.euclidean.threed.Rotation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.orekit.annotation.DefaultDataContext;
import org.orekit.attitudes.Attitude;
import org.orekit.czml.file.AbstractTest;
import org.orekit.czml.object.utils.DateUtils;
import org.orekit.czml.object.primary.Header;
import org.orekit.czml.object.primary.entities.Spacecraft;
import org.orekit.frames.FramesFactory;
import org.orekit.propagation.BoundedPropagator;
import org.orekit.time.AbsoluteDate;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

/**
 * The type Orientation test.
 */
@DefaultDataContext
public class OrientationTest
    extends
    AbstractTest {

    /**
     * Orientation constructor test.
     *
     * @throws IOException the io exception
     * @throws URISyntaxException the uri syntax exception
     */
    @Test
    void OrientationConstructorTest()
        throws IOException,
            URISyntaxException {

        loadOrekitData();

        final Header header = dummyHeader();
        final AbsoluteDate startDate =
            DateUtils.toAbsoluteDate(header.getAvailability().getStart());
        final AbsoluteDate stopDate = startDate.shiftedBy(60.0);

        final BoundedPropagator propagator =
            dummyPropagator(startDate, stopDate);
        final Spacecraft satellite =
            new Spacecraft(propagator, header.getClock());

        final List<Attitude> attitudes = satellite.getAttitudes();

        final String pathFile =
            loadResources("templateFile/object/secondary/OrientationTemplate.txt");
        final String invertPathFile =
            loadResources("templateFile/object/secondary/OrientationInvertTemplate.txt");
        final String withBuilderPathFile =
            loadResources("templateFile/object/secondary/OrientationWithBuilderTemplate.txt");
        final String falseInvertPathFile =
            loadResources("templateFile/object/secondary/OrientationFalseInvertTemplate.txt");

        final Orientation orientation =
            new Orientation(attitudes, FramesFactory.getEME2000());
        final Orientation orientationWithBuilder =
            Orientation.builder(attitudes.get(0), FramesFactory.getEME2000())
                .build();
        final Orientation orientationInvert =
            new Orientation(attitudes, FramesFactory.getEME2000(), true, null);
        final Orientation orientationFalseInvert =
            new Orientation(attitudes, FramesFactory.getEME2000(), false,
                            new Rotation(1.0, 0.0, 0.0, 1.0, false));

        verifyFileOutput(pathFile, orientation.toString(), 1e-8);
        verifyFileOutput(invertPathFile, orientationInvert.toString(), 1e-8);
        verifyFileOutput(withBuilderPathFile, orientationWithBuilder.toString(),
                         1e-8);
        verifyFileOutput(falseInvertPathFile, orientationFalseInvert.toString(),
                         1e-8);

        Assertions.assertEquals(attitudes, orientation.getAttitudes());
    }
}
