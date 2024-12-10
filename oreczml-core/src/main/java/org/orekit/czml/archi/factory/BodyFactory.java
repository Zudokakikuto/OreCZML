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
package org.orekit.czml.archi.factory;

import org.orekit.bodies.CelestialBody;
import org.orekit.bodies.CelestialBodyFactory;
import org.orekit.czml.object.primary.Body;
import org.orekit.czml.object.primary.Header;

/**
 * Body Factory class
 * <p>
 * Factory for the {@link Body} class.
 *
 * @author Julien LEBLOND
 * @since 1.0
 */
public class BodyFactory {

    /**
     * The default folder where the 3D models of bodies are loaded from.
     */
    public static final String BODIES_SOURCES = Body.class.getClassLoader()
                                                          .getResource("Bodies")
                                                          .getPath();

    /**
     * The default 3D model of The Moon.
     */
    public static final String MOON_MODEL = BODIES_SOURCES + "/moon.glb";

    /**
     * The default 3D model of Mercury.
     */
    public static final String MERCURY_MODEL = BODIES_SOURCES + "/mercury.glb";

    /**
     * The default 3D model of Venus.
     */
    public static final String VENUS_MODEL = BODIES_SOURCES + "/venus.glb";

    /** The default 3D model of The Earth for offline sessions. */
    public static final String EARTH_MODEL = BODIES_SOURCES + "/earth.glb";

    /**
     * The default 3D model of Mars.
     */
    public static final String MARS_MODEL = BODIES_SOURCES + "/mars.glb";

    /**
     * The default 3D model of Jupiter.
     */
    public static final String JUPITER_MODEL = BODIES_SOURCES + "/jupiter.glb";

    /**
     * The default 3D model of Saturn.
     */
    public static final String SATURN_MODEL = BODIES_SOURCES + "/saturn.glb";

    /**
     * The default 3D model of Uranus.
     */
    public static final String URANUS_MODEL = BODIES_SOURCES + "/uranus.glb";

    /**
     * The default 3D model of Neptune.
     */
    public static final String NEPTUNE_MODEL = BODIES_SOURCES + "/neptune.glb";

    /**
     * The default 3D model of Pluto.
     */
    public static final String PLUTO_MODEL = BODIES_SOURCES + "/pluto.glb";

    /**
     * The default 3D model of The Sun.
     */
    public static final String SUN_MODEL = BODIES_SOURCES + "/sun.glb";


    // Constructor

    /**
     * An empty constructor.
     */
    private BodyFactory() {
    }

    /**
     * Function to get The Moon.
     *
     * @param header : The header considered.
     * @return : A body object with the 3D models and the default parameters of The Moon loaded.
     */
    public static Body getMoon(final Header header) {
        final CelestialBody moon = CelestialBodyFactory.getMoon();
        return new Body(moon, MOON_MODEL, header).withModelScale(1e120)
                                                 .withModelMinimumPixelSize(400)
                                                 .withModelMaximumScale(5e6);
    }

    /**
     * Function to get Mercury.
     *
     * @param header : The header considered.
     * @return : A body object with the 3D models and the default parameters of Mercury loaded.
     */
    public static Body getMercury(final Header header) {
        final CelestialBody mercury = CelestialBodyFactory.getMercury();
        return new Body(mercury, MERCURY_MODEL, header).withModelScale(1)
                                                       .withModelMinimumPixelSize(400)
                                                       .withModelMaximumScale(1e50);
    }

    /**
     * Function to get Venus.
     *
     * @param header : The header considered.
     * @return : A body object with the 3D models and the default parameters of Venus loaded.
     */
    public static Body getVenus(final Header header) {
        final CelestialBody venus = CelestialBodyFactory.getVenus();
        return new Body(venus, VENUS_MODEL, header).withModelScale(1)
                                                   .withModelMinimumPixelSize(400)
                                                   .withModelMaximumScale(1e80);
    }

    /**
     * Function to get The Earth.
     *
     * @param header : The header considered.
     * @return : A body object with the 3D models and the default parameters of The Earth loaded.
     */
    public static Body getEarth(final Header header) {
        final CelestialBody earth = CelestialBodyFactory.getEarth();
        return new Body(earth, EARTH_MODEL, header).withModelScale(1)
                                                   .withModelMinimumPixelSize(1180)
                                                   .withModelMaximumScale(1.02e6);
    }

    /**
     * Function to get Mars.
     *
     * @param header : The header considered.
     * @return : A body object with the 3D models and the default parameters of Mars loaded.
     */
    public static Body getMars(final Header header) {
        final CelestialBody mars = CelestialBodyFactory.getMars();
        return new Body(mars, MARS_MODEL, header).withModelScale(1)
                                                 .withModelMinimumPixelSize(400)
                                                 .withModelMaximumScale(1e200);
    }

    /**
     * Function to get Jupiter.
     *
     * @param header : The header considered.
     * @return : A body object with the 3D models and the default parameters of Jupiter loaded.
     */
    public static Body getJupiter(final Header header) {
        final CelestialBody jupiter = CelestialBodyFactory.getJupiter();
        return new Body(jupiter, JUPITER_MODEL, header).withModelScale(1)
                                                       .withModelMinimumPixelSize(400)
                                                       .withModelMaximumScale(1e100);
    }

    /**
     * Function to get Saturn.
     *
     * @param header : The header considered.
     * @return : A body object with the 3D models and the default parameters of Saturn loaded.
     */
    public static Body getSaturn(final Header header) {
        final CelestialBody saturn = CelestialBodyFactory.getSaturn();
        return new Body(saturn, SATURN_MODEL, header).withModelScale(1)
                                                     .withModelMinimumPixelSize(400)
                                                     .withModelMaximumScale(1e100);
    }

    /**
     * Function to get Uranus.
     *
     * @param header : The header considered.
     * @return : A body object with the 3D models and the default parameters of Uranus loaded.
     */
    public static Body getUranus(final Header header) {
        final CelestialBody uranus = CelestialBodyFactory.getUranus();
        return new Body(uranus, URANUS_MODEL, header).withModelScale(1)
                                                     .withModelMinimumPixelSize(400)
                                                     .withModelMaximumScale(1e100);
    }

    /**
     * Function to get Neptune.
     *
     * @param header : The header considered.
     * @return : A body object with the 3D models and the default parameters of Neptune loaded.
     */
    public static Body getNeptune(final Header header) {
        final CelestialBody neptune = CelestialBodyFactory.getNeptune();
        return new Body(neptune, NEPTUNE_MODEL, header).withModelScale(1)
                                                       .withModelMinimumPixelSize(400)
                                                       .withModelMaximumScale(1e100);
    }

    /**
     * Function to get Pluto.
     *
     * @param header : The header considered.
     * @return : A body object with the 3D models and the default parameters of Pluto loaded.
     */
    public static Body getPluto(final Header header) {
        final CelestialBody pluto = CelestialBodyFactory.getPluto();
        return new Body(pluto, PLUTO_MODEL, header).withModelScale(1)
                                                   .withModelMinimumPixelSize(400)
                                                   .withModelMaximumScale(1e100);
    }

    /**
     * Function to get The Sun.
     *
     * @param header : The header considered.
     * @return : A body object with the 3D models and the default parameters of The Sun loaded.
     */
    public static Body getSun(final Header header) {
        final CelestialBody sun = CelestialBodyFactory.getSun();
        return new Body(sun, SUN_MODEL, header).withModelScale(1)
                                               .withModelMinimumPixelSize(400)
                                               .withModelMaximumScale(1e100);
    }
}
