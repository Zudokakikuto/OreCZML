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

    /** The default string for the description. */
    private static final String ID_BODY = "ID : BODY/";

    /** The default header for the description. */
    private static final String DESCRIPTION_HEADER = "<!--HTML-->\r\n<p>";

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
                                                 .withModelMaximumScale(5e6)
                                                 .withDescription(
                                                         DESCRIPTION_HEADER + ID_BODY + CelestialBodyFactory.getMoon()
                                                                                                            .getName() + "</p>\r\n<p>Sideral Orbital Period : 27.321661 days </p>\r\n<p>Mean Radius : 1737.4 km </p>\r\nMass : 7.364e22 kg</p>");
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
                                                       .withModelMaximumScale(1e50)
                                                       .withDescription(
                                                               DESCRIPTION_HEADER + ID_BODY + CelestialBodyFactory.getMercury()
                                                                                                                  .getName() + "</p>\r\n<p>Sideral Orbital Period : 87.9691 days </p>\r\n<p>Mean Radius : 2439.7 km </p>\r\n<p>Mass : 3.3011e22 kg</p>");
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
                                                   .withModelMaximumScale(1e80)
                                                   .withDescription(
                                                           DESCRIPTION_HEADER + ID_BODY + CelestialBodyFactory.getVenus()
                                                                                                              .getName() + "</p>\r\n<p>Sideral Orbital Period : 224.701 days </p>\r\n<p>Mean Radius : 6051.8 km </p>\r\n<p>Mass : 4.8675e24 kg </p>");
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
                                                   .withModelMaximumScale(1.02e6)
                                                   .withDescription(
                                                           DESCRIPTION_HEADER + ID_BODY + CelestialBodyFactory.getEarth()
                                                                                                              .getName() + "</p>\r\n<p>Sideral Orbital Period : 365.256 days </p>\r\n<p>Mean Radius : 6371.0 km </p>\r\n<p>Mass : 5.9722e24 kg</p>");
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
                                                 .withModelMaximumScale(1e200)
                                                 .withDescription(
                                                         DESCRIPTION_HEADER + ID_BODY + CelestialBodyFactory.getMars()
                                                                                                            .getName() + "</p>\r\n<p>Sideral Orbital Period : 686.980 days </p>\r\n<p>Mean Radius : 3389.5 km </p>\r\n<p>Mass : 6.4171e23 kg</p>");
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
                                                       .withModelMaximumScale(1e100)
                                                       .withDescription(
                                                               DESCRIPTION_HEADER + ID_BODY + CelestialBodyFactory.getJupiter()
                                                                                                                  .getName() + "</p>\r\n<p>Sideral Orbital Period : 11.862 years </p>\r\n<p>Mean Radius : 69911 km </p>\r\n<p>Mass : 1.8982e27 kg</p>");
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
                                                     .withModelMaximumScale(1e100)
                                                     .withDescription(
                                                             DESCRIPTION_HEADER + ID_BODY + CelestialBodyFactory.getSaturn()
                                                                                                                .getName() + "</p>\r\n<p>Sideral Orbital Period : 29.4475 years </p>\r\n<p>Mean Radius : 58232 km </p>\r\n<p>Mass : 5.6834e26 kg</p>");
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
                                                     .withModelMaximumScale(1e100)
                                                     .withDescription(
                                                             DESCRIPTION_HEADER + ID_BODY + CelestialBodyFactory.getUranus()
                                                                                                                .getName() + "</p>\r\n<p>Sideral Orbital Period : 84.0205 years </p>\r\n<p>Mean Radius : 25362 km </p>\r\n<p>Mass : 8.6810e25 kg</p>");
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
                                                       .withModelMaximumScale(1e100)
                                                       .withDescription(ID_BODY + CelestialBodyFactory.getNeptune()
                                                                                                      .getName() + "</p>\r\n<p>Sideral Orbital Period : 164.8 years </p>\r\n<p>Mean Radius : 24622 km </p>\r\n<p>Mass : 1.02409e26 kg </p>");
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
                                                   .withModelMaximumScale(1e100)
                                                   .withDescription(ID_BODY + CelestialBodyFactory.getPluto()
                                                                                                  .getName() + "</p>\r\n<p>Sideral Orbital Period : 247.94 years </p>\r\n<p>Mean Radius : 1188.3 km </p>\r\n<p>Mass : 1.3025e22 kg </p>");
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
                                               .withModelMaximumScale(1e100)
                                               .withDescription(ID_BODY + CelestialBodyFactory.getJupiter()
                                                                                              .getName() + "</p>\r\n<p>Equatorial Radius : 6.957e8 km </p>\r\n<p>Mass : 1.9885e30 kg</p>");
    }
}
