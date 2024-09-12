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
package org.orekit.czml.ArchiObjects.Factories;

import org.orekit.bodies.CelestialBody;
import org.orekit.bodies.CelestialBodyFactory;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.BodyDisplay;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.Header;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Body display factory class
 * <p>
 * Factory for the {@link BodyDisplay} class.
 * <p>
 *
 * @author Julien LEBLOND
 * @since 1.0.0
 */

public class BodyDisplayFactory {

    /** The default folder where the bodies 3D models are loaded from. */
    public static final String BODIES_SOURCES = Header.DEFAULT_RESOURCES + "/Bodies";

    /** The default 3D model of The Moon. */
    public static final String MOON_MODEL = BODIES_SOURCES + "/moon.glb";

    /** The default 3D model of Mercury. */
    public static final String MERCURY_MODEL = BODIES_SOURCES + "/mercury.glb";

    /** The default 3D model of Venus. */
    public static final String VENUS_MODEL = BODIES_SOURCES + "/venus.glb";

    /** The default 3D model of Mars. */
    public static final String MARS_MODEL = BODIES_SOURCES + "/mars.glb";

    /** The default 3D model of Jupiter. */
    public static final String JUPITER_MODEL = BODIES_SOURCES + "/jupiter.glb";

    /** The default 3D model of Saturn. */
    public static final String SATURN_MODEL = BODIES_SOURCES + "/saturn.glb";

    /** The default 3D model of Uranus. */
    public static final String URANUS_MODEL = BODIES_SOURCES + "/uranus.glb";

    /** The default 3D model of Neptune. */
    public static final String NEPTUNE_MODEL = BODIES_SOURCES + "/neptune.glb";

    /** The default 3D model of Pluto. */
    public static final String PLUTO_MODEL = BODIES_SOURCES + "/pluto.glb";

    /** The default 3D model of The Sun. */
    public static final String SUN_MODEL = BODIES_SOURCES + "/sun.glb";


    // Constructor

    /** An empty constructor. */
    private BodyDisplayFactory() {
    }

    /**
     * Function to get The Moon.
     *
     * @return : A body display object with the 3D models and the default parameters of The Moon loaded.
     */
    public static BodyDisplay getMoon() throws URISyntaxException, IOException {
        final CelestialBody moon = CelestialBodyFactory.getMoon();
        return new BodyDisplay(moon, MOON_MODEL).withModelScale(1e120)
                                                .withModelMinimumPixelSize(400)
                                                .withModelMaximumScale(5e6);
    }

    /**
     * Function to get Mercury.
     *
     * @return : A body display object with the 3D models and the default parameters of Mercury loaded.
     */
    public static BodyDisplay getMercury() throws URISyntaxException, IOException {
        final CelestialBody mercury = CelestialBodyFactory.getMercury();
        return new BodyDisplay(mercury, MERCURY_MODEL).withModelScale(1)
                                                      .withModelMinimumPixelSize(400)
                                                      .withModelMaximumScale(1e50);
    }

    /**
     * Function to get Venus.
     *
     * @return : A body display object with the 3D models and the default parameters of Venus loaded.
     */
    public static BodyDisplay getVenus() throws URISyntaxException, IOException {
        final CelestialBody venus = CelestialBodyFactory.getVenus();
        return new BodyDisplay(venus, VENUS_MODEL).withModelScale(1)
                                                  .withModelMinimumPixelSize(400)
                                                  .withModelMaximumScale(1e80);
    }

    /**
     * Function to get Mars.
     *
     * @return : A body display object with the 3D models and the default parameters of Mars loaded.
     */
    public static BodyDisplay getMars() throws URISyntaxException, IOException {
        final CelestialBody mars = CelestialBodyFactory.getMars();
        return new BodyDisplay(mars, MARS_MODEL).withModelScale(1)
                                                .withModelMinimumPixelSize(400)
                                                .withModelMaximumScale(1e200);
    }

    /**
     * Function to get Jupiter.
     *
     * @return : A body display object with the 3D models and the default parameters of Jupiter loaded.
     */
    public static BodyDisplay getJupiter() throws URISyntaxException, IOException {
        final CelestialBody jupiter = CelestialBodyFactory.getJupiter();
        return new BodyDisplay(jupiter, JUPITER_MODEL).withModelScale(1)
                                                      .withModelMinimumPixelSize(400)
                                                      .withModelMaximumScale(1e100);
    }

    /**
     * Function to get Saturn.
     *
     * @return : A body display object with the 3D models and the default parameters of Saturn loaded.
     */
    public static BodyDisplay getSaturn() throws URISyntaxException, IOException {
        final CelestialBody saturn = CelestialBodyFactory.getSaturn();
        return new BodyDisplay(saturn, SATURN_MODEL).withModelScale(1)
                                                    .withModelMinimumPixelSize(400)
                                                    .withModelMaximumScale(1e100);
    }

    /**
     * Function to get Uranus.
     *
     * @return : A body display object with the 3D models and the default parameters of Uranus loaded.
     */
    public static BodyDisplay getUranus() throws URISyntaxException, IOException {
        final CelestialBody uranus = CelestialBodyFactory.getUranus();
        return new BodyDisplay(uranus, URANUS_MODEL).withModelScale(1)
                                                    .withModelMinimumPixelSize(400)
                                                    .withModelMaximumScale(1e100);
    }

    /**
     * Function to get Neptune.
     *
     * @return : A body display object with the 3D models and the default parameters of Neptune loaded.
     */
    public static BodyDisplay getNeptune() throws URISyntaxException, IOException {
        final CelestialBody neptune = CelestialBodyFactory.getNeptune();
        return new BodyDisplay(neptune, NEPTUNE_MODEL).withModelScale(1)
                                                      .withModelMinimumPixelSize(400)
                                                      .withModelMaximumScale(1e100);
    }

    /**
     * Function to get Pluto.
     *
     * @return : A body display object with the 3D models and the default parameters of Pluto loaded.
     */
    public static BodyDisplay getPluto() throws URISyntaxException, IOException {
        final CelestialBody pluto = CelestialBodyFactory.getPluto();
        return new BodyDisplay(pluto, PLUTO_MODEL).withModelScale(1)
                                                  .withModelMinimumPixelSize(400)
                                                  .withModelMaximumScale(1e100);
    }

    /**
     * Function to get The Sun.
     *
     * @return : A body display object with the 3D models and the default parameters of The Sun loaded.
     */
    public static BodyDisplay getSun() throws URISyntaxException, IOException {
        final CelestialBody sun = CelestialBodyFactory.getSun();
        return new BodyDisplay(sun, SUN_MODEL).withModelScale(1)
                                              .withModelMinimumPixelSize(400)
                                              .withModelMaximumScale(1e100);
    }
}
