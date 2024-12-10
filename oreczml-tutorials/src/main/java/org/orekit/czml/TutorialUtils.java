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

package org.orekit.czml;

import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.data.DataContext;
import org.orekit.data.DataProvider;
import org.orekit.data.DirectoryCrawler;
import org.orekit.errors.OrekitException;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.utils.Constants;
import org.orekit.utils.IERSConventions;

import java.io.File;


/**
 * The type Tutorial utils.
 */
public class TutorialUtils {

    /**
     * .
     */
    public static final double STEP_BETWEEN_EACH_INSTANT = 60.0; // in seconds

    /**
     * .
     */
    public static final double POSITION_TOLERANCE = 10.0;

    /**
     * .
     */
    public static final double MIN_STEP = 0.001;

    /**
     * .
     */
    public static final double MAX_STEP = 1000.0;

    /**
     * .
     */
    public static final double CLASSIC_DURATION_OF_SIMULATION = 10 * 3600; // in seconds;

    /** user home. */
    private static final String USER_HOME = "user.home";

    /** orekit data. */
    private static final String OREKIT_DATA = "orekit-data";

    /**
     * The root of the project.
     */
    private static String ROOT = System.getProperty("user.dir");

    private TutorialUtils() {
    }

    /**
     * Load orekit data.
     */
    public static void loadOrekitData() {
        try {
            final File         home      = new File(System.getProperty(USER_HOME));
            final File         orekitDir = new File(home, OREKIT_DATA);
            final DataProvider provider  = new DirectoryCrawler(orekitDir);
            DataContext.getDefault()
                       .getDataProvidersManager()
                       .addProvider(provider);
        } catch (OrekitException oe) {
            System.err.println(oe.getLocalizedMessage());
        }
    }

    /**
     * Generate output string.
     *
     * @return the string
     */
    public static String generateOutput() {
        final String osName       = System.getProperty("os.name");
        final String outputName   = "Output.czml";
        final String outputFolder = "/Output";
        if (osName.contains("Windows")) {
            ROOT = ROOT.replace("\\", "/");
            final String outputPath = ROOT + outputFolder;
            return outputPath + "/" + outputName;
        } else if (osName.contains("Linux")) {
            final String outputPath = ROOT + outputFolder;
            return outputPath + "\\" + outputName;
        } else {
            ROOT = ROOT.replace("\\", "/");
            final String outputPath = ROOT + outputFolder;
            return outputPath + "/" + outputName;
        }
    }

    /**
     * Generate js path string.
     *
     * @param JsPath the js path
     * @return the string
     */
    public static String generateJSPath(final String JsPath) {
        final File javascriptFolder = new File(JsPath);
        javascriptFolder.mkdir();
        return JsPath;
    }

    /**
     * Load resources string.
     *
     * @param resourcePath the resource path
     * @return the string
     */
    public static String loadResources(final String resourcePath) {
        return new File(TutorialUtils.class.getClassLoader()
                                           .getResource(resourcePath)
                                           .getFile()).toPath()
                                                      .toString();
    }

    /**
     * Gets earth.
     *
     * @return the earth
     */
    public static OneAxisEllipsoid getEarth() {
        final Frame ITRF = FramesFactory.getITRF(IERSConventions.IERS_2010, true);
        return new OneAxisEllipsoid(Constants.WGS84_EARTH_EQUATORIAL_RADIUS, Constants.WGS84_EARTH_FLATTENING, ITRF);
    }

}
