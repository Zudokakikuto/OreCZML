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

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.orekit.czml.TutorialUtils;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScale;
import org.orekit.time.TimeScalesFactory;

public class TearDropExample {

    private TearDropExample() {
        // empty
    }

    /**
     * Main of the tear drop tutorial.
     *
     * @param args the args
     * @throws Exception the exception
     */
    public static void main(final String[] args)
        throws Exception {

        TutorialUtils.loadOrekitData();

        final String pathToCsv = TutorialUtils.loadResources("teardrop.csv");
        final BufferedReader bufferedReader =
            new BufferedReader(new FileReader(pathToCsv));
        final List<AbsoluteDate> absoluteDates = new ArrayList<>();
        final List<Double> times = new ArrayList<>();
        final List<Double> xList = new ArrayList<>();
        final List<Double> yList = new ArrayList<>();
        final List<Double> zList = new ArrayList<>();
        final List<Double> vxList = new ArrayList<>();
        final List<Double> vyList = new ArrayList<>();
        final List<Double> vzList = new ArrayList<>();
        final TimeScale UTC = TimeScalesFactory.getUTC();
        while (bufferedReader.readLine() != null) {
            final String line = bufferedReader.readLine();
            final String[] splittedLines = line.split(",");
            final List<String> splittedIntoList = Arrays.asList(splittedLines);
            absoluteDates.add(new AbsoluteDate(splittedIntoList.get(0), UTC));
            times.add(Double.parseDouble(splittedIntoList.get(1)));
            xList.add(Double.parseDouble(splittedIntoList.get(2)));
            yList.add(Double.parseDouble(splittedIntoList.get(3)));
            zList.add(Double.parseDouble(splittedIntoList.get(4)));
            vxList.add(Double.parseDouble(splittedIntoList.get(5)));
            vyList.add(Double.parseDouble(splittedIntoList.get(6)));
            vzList.add(Double.parseDouble(splittedIntoList.get(7)));
        }
        // final List<SpacecraftState> states = new ArrayList<>();
        // for (int i = 0; i < xList.size(); i++) {
        // final AbsolutePVCoordinates absolutePVCoordinates = new
        // AbsolutePVCoordinates()
        // states.add(new SpacecraftState()
        // }
    }
}
