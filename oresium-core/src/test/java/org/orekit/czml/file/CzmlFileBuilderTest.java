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
package org.orekit.czml.file;

import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.hipparchus.ode.nonstiff.AdaptiveStepsizeIntegrator;
import org.hipparchus.ode.nonstiff.DormandPrince853Integrator;
import org.hipparchus.util.FastMath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.czml.archi.factory.BodyFactory;
import org.orekit.czml.object.primary.GroundTrack;
import org.orekit.czml.object.primary.Header;
import org.orekit.czml.object.primary.ManeuverSequence;
import org.orekit.czml.object.primary.covariance.Collision;
import org.orekit.czml.object.primary.entities.Constellation;
import org.orekit.czml.object.primary.entities.CzmlGroundStation;
import org.orekit.czml.object.primary.entities.GroundVehicle;
import org.orekit.czml.object.primary.entities.InfluenceSphere;
import org.orekit.czml.object.primary.entities.Spacecraft;
import org.orekit.czml.object.primary.pointing.AttitudePointing;
import org.orekit.czml.object.primary.pointing.CoveredSurfaceOnBody;
import org.orekit.czml.object.primary.systems.LatLongLines;
import org.orekit.czml.object.primary.systems.SpacecraftReferenceSystem;
import org.orekit.czml.object.primary.visu.FieldOfObservation;
import org.orekit.czml.object.primary.visu.InterSatVisu;
import org.orekit.czml.object.primary.visu.LineOfVisibility;
import org.orekit.czml.object.primary.visu.MultipleLineOfVisibility;
import org.orekit.czml.object.primary.visu.StationVisibilityCircle;
import org.orekit.czml.object.secondary.Clock;
import org.orekit.czml.object.utils.DateUtils;
import org.orekit.forces.ForceModel;
import org.orekit.forces.gravity.HolmesFeatherstoneAttractionModel;
import org.orekit.forces.gravity.potential.GravityFieldFactory;
import org.orekit.forces.gravity.potential.NormalizedSphericalHarmonicsProvider;
import org.orekit.frames.FramesFactory;
import org.orekit.frames.LOFType;
import org.orekit.frames.TopocentricFrame;
import org.orekit.frames.Transform;
import org.orekit.geometry.fov.DoubleDihedraFieldOfView;
import org.orekit.geometry.fov.FieldOfView;
import org.orekit.orbits.KeplerianOrbit;
import org.orekit.orbits.OrbitType;
import org.orekit.orbits.PositionAngleType;
import org.orekit.propagation.BoundedPropagator;
import org.orekit.propagation.EphemerisGenerator;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.StateCovariance;
import org.orekit.propagation.numerical.NumericalPropagator;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScalesFactory;
import org.orekit.utils.Constants;
import org.orekit.utils.IERSConventions;
import org.orekit.utils.PVCoordinatesProvider;
import org.orekit.utils.TimeStampedPVCoordinates;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test class for the {@link CzmlFileBuilder} class.
 *
 * @author Leblond Julien
 * @since 2.0
 */
public class CzmlFileBuilderTest
    extends
    AbstractTest {

    private Header header;

    private CzmlFileBuilder builder;

    private final double data = initializeOrekitData();

    final Clock clock = dummyHeader().getClock();

    final AbsoluteDate startDate =
        DateUtils.toAbsoluteDate(clock.getAvailability().getStart());

    final AbsoluteDate finalDate =
        DateUtils.toAbsoluteDate(clock.getAvailability().getStop());

    final BoundedPropagator propagator =
        dummyPropagator(startDate, finalDate, dummyOrbit(startDate));

    final List<BoundedPropagator> propagators = new ArrayList<>();

    final GeodeticPoint toulouseFrame =
        new GeodeticPoint(FastMath.toRadians(43.6047),
                          FastMath.toRadians(1.4442), 0);

    /** Toulouse frame. */
    final TopocentricFrame topocentricToulouse =
        new TopocentricFrame(getEarth(), toulouseFrame, "Toulouse Frame");

    final GeodeticPoint quitoFrame =
        new GeodeticPoint(FastMath.toRadians(0.1807),
                          FastMath.toRadians(11.5382), 2850);

    final TopocentricFrame topocentricQuito =
        new TopocentricFrame(getEarth(), quitoFrame, "Quito");

    @BeforeEach
    public void setUp() {
        header = dummyHeader();
        builder = new CzmlFileBuilder(header);
        propagators.clear();
        propagators.add(propagator);
    }

    @Test
    public void testWithSpacecraft() {
        final AbsoluteDate startDate =
            new AbsoluteDate(2024, 1, 1, 0, 0, 0.0, TimeScalesFactory.getUTC());
        final AbsoluteDate finalDate = startDate.shiftedBy(3600.0);
        final BoundedPropagator propagator =
            dummyPropagator(startDate, finalDate, dummyOrbit(startDate));
        final Spacecraft spacecraft =
            Spacecraft.builder(propagator, header.getClock()).build();
        builder.withSpacecraft(spacecraft);
        final String czml = builder.build().toString();

        assertTrue(czml.contains("\"id\":\"" + spacecraft.getId() + "\""));
    }

    @Test
    public void testWithSpacecraftList() {
        final AbsoluteDate startDate =
            new AbsoluteDate(2024, 1, 1, 0, 0, 0.0, TimeScalesFactory.getUTC());
        final AbsoluteDate finalDate = startDate.shiftedBy(3600.0);
        final BoundedPropagator propagator1 =
            dummyPropagator(startDate, finalDate, dummyOrbit(startDate));
        final BoundedPropagator propagator2 =
            dummyPropagator(startDate, finalDate, dummyOrbit(startDate));

        final Spacecraft spacecraft1 =
            Spacecraft.builder(propagator1, header.getClock()).build();
        final Spacecraft spacecraft2 =
            Spacecraft.builder(propagator2, header.getClock()).build();
        final List<Spacecraft> spacecrafts =
            Arrays.asList(spacecraft1, spacecraft2);

        builder.withSpacecraft(spacecrafts);
        final String czml = builder.build().toString();

        assertTrue(czml.contains("\"id\":\"" + spacecraft1.getId() + "\""));
        assertTrue(czml.contains("\"id\":\"" + spacecraft2.getId() + "\""));
    }

    @Test
    public void testWithGroundVehicle() {
        final AbsoluteDate startDate =
            new AbsoluteDate(2024, 1, 1, 0, 0, 0.0, TimeScalesFactory.getUTC());
        final AbsoluteDate finalDate = startDate.shiftedBy(3600.0);
        final OneAxisEllipsoid earth = getEarth();

        // Create a simple PVCoordinatesProvider for testing
        final PVCoordinatesProvider coordsProvider = (date, frame) -> {
            final Vector3D position = new Vector3D(6378000 + 1000, 0, 0); // 1000m
                                                                          // above
                                                                          // surface
            final Vector3D velocity = new Vector3D(0, 7000, 0); // 7km/s
                                                                // tangential
                                                                // velocity
            return new TimeStampedPVCoordinates(date, position, velocity);
        };

        final GroundVehicle groundVehicle =
            GroundVehicle
                .builder(coordsProvider, startDate, finalDate, earth, 60.0)
                .build();
        builder.withGroundVehicle(groundVehicle);
        final String czml = builder.build().toString();

        assertTrue(czml.contains("\"id\":\"" + groundVehicle.getId() + "\""));
    }

    @Test
    public void testWithGroundVehicleList() {
        final AbsoluteDate startDate =
            new AbsoluteDate(2024, 1, 1, 0, 0, 0.0, TimeScalesFactory.getUTC());
        final AbsoluteDate finalDate = startDate.shiftedBy(3600.0);
        final OneAxisEllipsoid earth = getEarth();

        // Create a simple PVCoordinatesProvider for testing
        final PVCoordinatesProvider coordsProvider1 = (date, frame) -> {
            final Vector3D position = new Vector3D(6378000 + 1000, 0, 0); // 1000m
                                                                          // above
                                                                          // surface
            final Vector3D velocity = new Vector3D(0, 7000, 0); // 7km/s
                                                                // tangential
                                                                // velocity
            return new TimeStampedPVCoordinates(date, position, velocity);
        };

        final PVCoordinatesProvider coordsProvider2 = (date, frame) -> {
            Vector3D position = new Vector3D(6378000 + 2000, 0, 0); // 2000m
                                                                    // above
                                                                    // surface
            Vector3D velocity = new Vector3D(0, 7500, 0); // 7.5km/s tangential
                                                          // velocity
            return new TimeStampedPVCoordinates(date, position, velocity);
        };

        final GroundVehicle vehicle1 =
            GroundVehicle
                .builder(coordsProvider1, startDate, finalDate, earth, 60.0)
                .build();
        final GroundVehicle vehicle2 =
            GroundVehicle
                .builder(coordsProvider2, startDate, finalDate, earth, 60.0)
                .build();
        final List<GroundVehicle> vehicles = Arrays.asList(vehicle1, vehicle2);

        builder.withGroundVehicle(vehicles);
        final String czml = builder.build().toString();

        assertTrue(czml.contains("\"id\":\"" + vehicle1.getId() + "\""));
        assertTrue(czml.contains("\"id\":\"" + vehicle2.getId() + "\""));
    }

    @Test
    public void testWithConstellation() {
        final Constellation constellation =
            Constellation.builder(propagators, finalDate, clock).build();
        builder.withConstellation(constellation);
        final String czml = builder.build().toString();

        assertTrue(czml.contains("SPACECRAFT"));
    }

    @Test
    public void testWithConstellationList() {
        final Constellation const1 =
            Constellation.builder(propagators, finalDate, clock).build();
        final Constellation const2 =
            Constellation.builder(propagators, finalDate, clock).build();
        final List<Constellation> constellations =
            Arrays.asList(const1, const2);

        builder.withConstellation(constellations);
        final String czml = builder.build().toString();

        final Matcher matcher = Pattern.compile("SPACECRAFT").matcher(czml);
        assertEquals(2, matcher.results().count());
    }

    @Test
    public void testWithCzmlGroundStation() {
        final CzmlGroundStation station =
            CzmlGroundStation.builder(topocentricQuito, clock).build();
        builder.withCzmlGroundStation(station);
        final String czml = builder.build().toString();

        assertTrue(czml.contains("\"id\":\"" + station.getId() + "\""));
    }

    @Test
    public void testWithCzmlGroundStationList() {
        final CzmlGroundStation station1 =
            CzmlGroundStation.builder(topocentricToulouse, clock).build();
        final CzmlGroundStation station2 =
            CzmlGroundStation.builder(topocentricQuito, clock).build();
        final List<CzmlGroundStation> stations =
            Arrays.asList(station1, station2);

        builder.withCzmlGroundStation(stations);
        final String czml = builder.build().toString();

        assertTrue(czml.contains("\"id\":\"" + station1.getId() + "\""));
        assertTrue(czml.contains("\"id\":\"" + station2.getId() + "\""));
    }

    @Test
    public void testWithInfluenceSphere() {
        final InfluenceSphere sphere =
            InfluenceSphere.builder(BodyFactory.getEarth(clock), clock).build();
        builder.withInfluenceSphere(sphere);
        final String czml = builder.build().toString();

        assertTrue(czml.contains("\"id\":\"" + sphere.getId() + "\""));
    }

    @Test
    public void testWithInfluenceSphereList() {
        final InfluenceSphere sphere1 =
            InfluenceSphere.builder(BodyFactory.getEarth(clock), clock).build();
        final InfluenceSphere sphere2 =
            InfluenceSphere.builder(BodyFactory.getMars(clock), clock).build();
        final List<InfluenceSphere> spheres = Arrays.asList(sphere1, sphere2);

        builder.withInfluenceSphere(spheres);
        final String czml = builder.build().toString();

        assertTrue(czml.contains("\"id\":\"" + sphere1.getId() + "\""));
        assertTrue(czml.contains("\"id\":\"" + sphere2.getId() + "\""));
    }

    @Test
    public void testWithAttitudePointing() {
        final AbsoluteDate startDate =
            new AbsoluteDate(2024, 1, 1, 0, 0, 0.0, TimeScalesFactory.getUTC());
        final AbsoluteDate finalDate = startDate.shiftedBy(3600.0);
        final BoundedPropagator propagator =
            dummyPropagator(startDate, finalDate, dummyOrbit(startDate));
        final Spacecraft spacecraft =
            Spacecraft.builder(propagator, header.getClock())
                .withDisplayAttitude().build();

        final AttitudePointing pointing =
            AttitudePointing.builder(spacecraft, getEarth(),
                                     new Vector3D(1.0, 1.0, 0.0), clock)
                .build();
        builder.withAttitudePointing(pointing);
        final String czml = builder.build().toString();

        assertTrue(czml.contains("\"id\":\"" + pointing.getId() + "\""));
    }

    @Test
    public void testWithAttitudePointingList() {
        final AbsoluteDate startDate =
            new AbsoluteDate(2024, 1, 1, 0, 0, 0.0, TimeScalesFactory.getUTC());
        final AbsoluteDate finalDate = startDate.shiftedBy(3600.0);
        final BoundedPropagator propagator1 =
            dummyPropagator(startDate, finalDate, dummyOrbit(startDate));
        final BoundedPropagator propagator2 =
            dummyPropagator(startDate, finalDate, dummyOrbit(startDate));
        final Spacecraft spacecraft1 =
            Spacecraft.builder(propagator1, header.getClock())
                .withDisplayAttitude().build();
        final Spacecraft spacecraft2 =
            Spacecraft.builder(propagator2, header.getClock())
                .withDisplayAttitude().build();

        final AttitudePointing pointing1 =
            AttitudePointing.builder(spacecraft1, getEarth(),
                                     new Vector3D(1.0, 2.0, 3.0), clock)
                .build();
        final AttitudePointing pointing2 =
            AttitudePointing.builder(spacecraft2, getEarth(),
                                     new Vector3D(3.0, 2.0, 1.0), clock)
                .build();
        final List<AttitudePointing> pointings =
            Arrays.asList(pointing1, pointing2);

        builder.withAttitudePointing(pointings);
        final String czml = builder.build().toString();

        assertTrue(czml.contains("\"id\":\"" + pointing1.getId() + "\""));
        assertTrue(czml.contains("\"id\":\"" + pointing2.getId() + "\""));
    }

    @Test
    public void testWithLineOfVisibility() {
        final AbsoluteDate startDate =
            new AbsoluteDate(2024, 1, 1, 0, 0, 0.0, TimeScalesFactory.getUTC());
        final AbsoluteDate finalDate = startDate.shiftedBy(3600.0);
        final BoundedPropagator propagator =
            dummyPropagator(startDate, finalDate, dummyOrbit(startDate));
        final Spacecraft spacecraft =
            Spacecraft.builder(propagator, header.getClock()).build();

        final LineOfVisibility line =
            LineOfVisibility.builder(topocentricToulouse, spacecraft, clock)
                .build();
        builder.withLineOfVisibility(line);
        final String czml = builder.build().toString();

        assertTrue(czml.contains("\"id\":\"" + line.getId() + "\""));
    }

    @Test
    public void testWithLineOfVisibilityList() {
        final AbsoluteDate startDate =
            new AbsoluteDate(2024, 1, 1, 0, 0, 0.0, TimeScalesFactory.getUTC());
        final AbsoluteDate finalDate = startDate.shiftedBy(3600.0);
        final BoundedPropagator propagator1 =
            dummyPropagator(startDate, finalDate, dummyOrbit(startDate));
        final BoundedPropagator propagator2 =
            dummyPropagator(startDate, finalDate, dummyOrbit(startDate));

        final Spacecraft spacecraft1 =
            Spacecraft.builder(propagator1, header.getClock()).build();
        final Spacecraft spacecraft2 =
            Spacecraft.builder(propagator2, header.getClock()).build();

        final LineOfVisibility line1 =
            LineOfVisibility.builder(topocentricToulouse, spacecraft1, clock)
                .build();
        final LineOfVisibility line2 =
            LineOfVisibility.builder(topocentricQuito, spacecraft2, clock)
                .build();
        final List<LineOfVisibility> lines = Arrays.asList(line1, line2);

        builder.withLineOfVisibility(lines);
        final String czml = builder.build().toString();

        assertTrue(czml.contains("\"id\":\"" + line1.getId() + "\""));
        assertTrue(czml.contains("\"id\":\"" + line2.getId() + "\""));
    }

    @Test
    public void testWithMultipleLineOfVisibility() {
        final AbsoluteDate startDate =
            new AbsoluteDate(2024, 1, 1, 0, 0, 0.0, TimeScalesFactory.getUTC());
        final AbsoluteDate finalDate = startDate.shiftedBy(3600.0);
        final BoundedPropagator propagator =
            dummyPropagator(startDate, finalDate, dummyOrbit(startDate));
        final Spacecraft spacecraft =
            Spacecraft.builder(propagator, header.getClock()).build();

        final List<TopocentricFrame> frames = new ArrayList<>();
        frames.add(topocentricToulouse);
        frames.add(topocentricQuito);

        final MultipleLineOfVisibility multiLine =
            MultipleLineOfVisibility.builder(frames, spacecraft).build();
        builder.withMultipleLineOfVisibility(multiLine);
        final String czml = builder.build().toString();

        final Matcher matcher = Pattern.compile("LINE_VISU").matcher(czml);
        assertEquals(2, matcher.results().count());
    }

    @Test
    public void testWithMultipleLineOfVisibilityList() {
        final AbsoluteDate startDate =
            new AbsoluteDate(2024, 1, 1, 0, 0, 0.0, TimeScalesFactory.getUTC());
        final AbsoluteDate finalDate = startDate.shiftedBy(3600.0);

        final BoundedPropagator propagator1 =
            dummyPropagator(startDate, finalDate, dummyOrbit(startDate));
        final BoundedPropagator propagator2 =
            dummyPropagator(startDate, finalDate, dummyOrbit(startDate));

        final Spacecraft spacecraft1 =
            Spacecraft.builder(propagator1, header.getClock()).build();
        final Spacecraft spacecraft2 =
            Spacecraft.builder(propagator2, header.getClock()).build();

        final List<TopocentricFrame> frames = new ArrayList<>();
        frames.add(topocentricToulouse);
        frames.add(topocentricQuito);

        final MultipleLineOfVisibility multi1 =
            MultipleLineOfVisibility.builder(frames, spacecraft1).build();
        final MultipleLineOfVisibility multi2 =
            MultipleLineOfVisibility.builder(frames, spacecraft2).build();
        final List<MultipleLineOfVisibility> multis =
            Arrays.asList(multi1, multi2);

        builder.withMultipleLineOfVisibility(multis);
        final String czml = builder.build().toString();

        final Matcher matcher = Pattern.compile("LINE_VISU").matcher(czml);
        assertEquals(2, matcher.results().count());
    }

    @Test
    public void testWithVisibilityCircle() {
        final Spacecraft spacecraft1 =
            Spacecraft.builder(propagator, header.getClock()).build();

        final StationVisibilityCircle circle =
            StationVisibilityCircle
                .builder(topocentricToulouse, spacecraft1, clock).build();
        builder.withVisibilityCircle(circle);
        final String czml = builder.build().toString();

        assertTrue(czml.contains("\"id\":\"" + circle.getId() + "\""));
    }

    @Test
    public void testWithVisibilityCircleList() {
        final Spacecraft spacecraft1 =
            Spacecraft.builder(propagator, header.getClock()).build();
        final Spacecraft spacecraft2 =
            Spacecraft.builder(propagator, header.getClock()).build();

        final StationVisibilityCircle circle1 =
            StationVisibilityCircle
                .builder(topocentricToulouse, spacecraft1, clock).build();
        final StationVisibilityCircle circle2 =
            StationVisibilityCircle
                .builder(topocentricQuito, spacecraft2, clock).build();
        final List<StationVisibilityCircle> circles =
            Arrays.asList(circle1, circle2);

        builder.withVisibilityCircle(circles);
        final String czml = builder.build().toString();

        assertTrue(czml.contains("\"id\":\"" + circle1.getId() + "\""));
        assertTrue(czml.contains("\"id\":\"" + circle2.getId() + "\""));
    }

    @Test
    public void testWithInterSatVisu() {
        final AbsoluteDate startDate =
            new AbsoluteDate(2024, 1, 1, 0, 0, 0.0, TimeScalesFactory.getUTC());
        final AbsoluteDate finalDate = startDate.shiftedBy(3600.0);
        final BoundedPropagator propagator1 =
            dummyPropagator(startDate, finalDate, dummyOrbit(startDate));
        final BoundedPropagator propagator2 =
            dummyPropagator(startDate, finalDate, dummyOrbit(startDate));
        final Spacecraft spacecraft1 =
            Spacecraft.builder(propagator1, header.getClock()).build();
        final Spacecraft spacecraft2 =
            Spacecraft.builder(propagator2, header.getClock()).build();

        final InterSatVisu visu =
            InterSatVisu.builder(spacecraft1, spacecraft2, finalDate, clock)
                .build();
        builder.withInterSatVisu(visu);
        final String czml = builder.build().toString();

        assertTrue(czml.contains("\"id\":\"" + visu.getId() + "\""));
    }

    @Test
    public void testWithInterSatVisuList() {
        final AbsoluteDate startDate =
            new AbsoluteDate(2024, 1, 1, 0, 0, 0.0, TimeScalesFactory.getUTC());
        final AbsoluteDate finalDate = startDate.shiftedBy(3600.0);
        final BoundedPropagator propagator1 =
            dummyPropagator(startDate, finalDate, dummyOrbit(startDate));
        final BoundedPropagator propagator2 =
            dummyPropagator(startDate, finalDate, dummyOrbit(startDate));
        final BoundedPropagator propagator3 =
            dummyPropagator(startDate, finalDate, dummyOrbit(startDate));
        final Spacecraft spacecraft1 =
            Spacecraft.builder(propagator1, header.getClock()).build();
        final Spacecraft spacecraft2 =
            Spacecraft.builder(propagator2, header.getClock()).build();
        final Spacecraft spacecraft3 =
            Spacecraft.builder(propagator3, header.getClock()).build();

        final InterSatVisu visu1 =
            InterSatVisu.builder(spacecraft1, spacecraft2, finalDate, clock)
                .build();
        final InterSatVisu visu2 =
            InterSatVisu.builder(spacecraft1, spacecraft3, finalDate, clock)
                .build();
        final List<InterSatVisu> visus = Arrays.asList(visu1, visu2);

        builder.withInterSatVisu(visus);
        final String czml = builder.build().toString();

        assertTrue(czml.contains("\"id\":\"" + visu1.getId() + "\""));
        assertTrue(czml.contains("\"id\":\"" + visu2.getId() + "\""));
    }

    @Test
    public void testWithManeuverSequence() {
        final AbsoluteDate startDate =
            new AbsoluteDate(2024, 1, 1, 0, 0, 0.0, TimeScalesFactory.getUTC());
        final AbsoluteDate finalDate = startDate.shiftedBy(3600.0);
        final BoundedPropagator propagator =
            dummyPropagator(startDate, finalDate, dummyOrbit(startDate));
        final Spacecraft spacecraft =
            Spacecraft.builder(propagator, header.getClock()).build();

        final ManeuverSequence sequence =
            dummyManeuverSequence(startDate, finalDate, spacecraft);
        builder.withManeuverSequence(sequence);
        final String czml = builder.build().toString();

        final Matcher matcher = Pattern.compile("MANEUVER").matcher(czml);
        assertEquals(2, matcher.results().count());
    }

    @Test
    public void testWithManeuverSequenceList() {
        final AbsoluteDate startDate =
            new AbsoluteDate(2024, 1, 1, 0, 0, 0.0, TimeScalesFactory.getUTC());
        final AbsoluteDate finalDate = startDate.shiftedBy(3600.0);
        final BoundedPropagator propagator1 =
            dummyPropagator(startDate, finalDate, dummyOrbit(startDate));
        final BoundedPropagator propagator2 =
            dummyPropagator(startDate, finalDate, dummyOrbit(startDate));
        final Spacecraft spacecraft1 =
            Spacecraft.builder(propagator1, header.getClock()).build();
        final Spacecraft spacecraft2 =
            Spacecraft.builder(propagator2, header.getClock()).build();

        final ManeuverSequence sequence1 =
            dummyManeuverSequence(startDate, finalDate, spacecraft1);
        final ManeuverSequence sequence2 =
            dummyManeuverSequence(startDate, finalDate, spacecraft2);
        final List<ManeuverSequence> sequences =
            Arrays.asList(sequence1, sequence2);

        builder.withManeuverSequence(sequences);
        final String czml = builder.build().toString();

        final Matcher matcher = Pattern.compile("MANEUVER").matcher(czml);
        assertEquals(4, matcher.results().count());
    }

    @Test
    public void testWithGroundTrack() {
        final AbsoluteDate startDate =
            new AbsoluteDate(2024, 1, 1, 0, 0, 0.0, TimeScalesFactory.getUTC());
        final AbsoluteDate finalDate = startDate.shiftedBy(3600.0);
        final BoundedPropagator propagator =
            dummyPropagator(startDate, finalDate, dummyOrbit(startDate));
        final Spacecraft spacecraft =
            Spacecraft.builder(propagator, header.getClock()).build();

        final GroundTrack track =
            GroundTrack.builder(spacecraft, getEarth(), clock).build();
        builder.withGroundTrack(track);
        final String czml = builder.build().toString();

        assertTrue(czml.contains("\"id\":\"" + track.getId() + "\""));
    }

    @Test
    public void testWithGroundTrackList() {
        final AbsoluteDate startDate =
            new AbsoluteDate(2024, 1, 1, 0, 0, 0.0, TimeScalesFactory.getUTC());
        final AbsoluteDate finalDate = startDate.shiftedBy(3600.0);
        final BoundedPropagator propagator1 =
            dummyPropagator(startDate, finalDate, dummyOrbit(startDate));
        final BoundedPropagator propagator2 =
            dummyPropagator(startDate, finalDate, dummyOrbit(startDate));
        final Spacecraft spacecraft1 =
            Spacecraft.builder(propagator1, header.getClock()).build();
        final Spacecraft spacecraft2 =
            Spacecraft.builder(propagator2, header.getClock()).build();

        final GroundTrack track1 =
            GroundTrack.builder(spacecraft1, getEarth(), clock).build();
        final GroundTrack track2 =
            GroundTrack.builder(spacecraft2, getEarth(), clock).build();
        final List<GroundTrack> tracks = Arrays.asList(track1, track2);

        builder.withGroundTrack(tracks);
        final String czml = builder.build().toString();

        assertTrue(czml.contains("\"id\":\"" + track1.getId() + "\""));
        assertTrue(czml.contains("\"id\":\"" + track2.getId() + "\""));
    }

    @Test
    public void testWithSatelliteReferenceSystem() {
        final AbsoluteDate startDate =
            new AbsoluteDate(2024, 1, 1, 0, 0, 0.0, TimeScalesFactory.getUTC());
        final AbsoluteDate finalDate = startDate.shiftedBy(3600.0);
        final BoundedPropagator propagator =
            dummyPropagator(startDate, finalDate, dummyOrbit(startDate));
        final Spacecraft spacecraft =
            Spacecraft.builder(propagator, header.getClock()).build();

        final SpacecraftReferenceSystem system =
            new SpacecraftReferenceSystem(spacecraft);
        builder.withSatelliteReferenceSystem(system);
        final String czml = builder.build().toString();

        assertTrue(czml.contains("\"id\":\"" + system.getId() + "\""));
    }

    @Test
    public void testWithSatelliteReferenceSystemList() {
        final AbsoluteDate startDate =
            new AbsoluteDate(2024, 1, 1, 0, 0, 0.0, TimeScalesFactory.getUTC());
        final AbsoluteDate finalDate = startDate.shiftedBy(3600.0);
        final BoundedPropagator propagator1 =
            dummyPropagator(startDate, finalDate, dummyOrbit(startDate));
        final BoundedPropagator propagator2 =
            dummyPropagator(startDate, finalDate, dummyOrbit(startDate));
        final Spacecraft spacecraft1 =
            Spacecraft.builder(propagator1, header.getClock()).build();
        final Spacecraft spacecraft2 =
            Spacecraft.builder(propagator2, header.getClock()).build();

        final SpacecraftReferenceSystem system1 =
            new SpacecraftReferenceSystem(spacecraft1);
        final SpacecraftReferenceSystem system2 =
            new SpacecraftReferenceSystem(spacecraft2);
        final List<SpacecraftReferenceSystem> systems =
            Arrays.asList(system1, system2);

        builder.withSatelliteReferenceSystem(systems);
        final String czml = builder.build().toString();

        assertTrue(czml.contains("\"id\":\"" + system1.getId() + "\""));
        assertTrue(czml.contains("\"id\":\"" + system2.getId() + "\""));
    }

    @Test
    public void testWithCoveredSurfaceOnBody() {
        // Creation of the clock.
        // Duration of the simulation in seconds
        final double durationOfSimulation = 1800.0;
        final AbsoluteDate startDate =
            new AbsoluteDate(2024, 3, 15, 0, 0, 0.0,
                             TimeScalesFactory.getUTC());
        final AbsoluteDate finalDate =
            startDate.shiftedBy(durationOfSimulation);
        final Clock clock = new Clock(startDate, finalDate, 60.0);

        final Header header =
            new Header("Example of the usage of the covered surface", clock);

        // Build of a LEO orbit

        final KeplerianOrbit initialOrbit =
            new KeplerianOrbit(7878000, 0, FastMath.toRadians(20), 0,
                               FastMath.toRadians(0), FastMath.toRadians(0),
                               PositionAngleType.MEAN,
                               FramesFactory.getEME2000(), startDate,
                               Constants.WGS84_EARTH_MU);

        final SpacecraftState initialState = new SpacecraftState(initialOrbit);

        // Build of the propagator

        final double[][] tolerances =
            NumericalPropagator.tolerances(10.0, initialOrbit,
                                           OrbitType.CARTESIAN);
        final AdaptiveStepsizeIntegrator integrator =
            new DormandPrince853Integrator(0.001, 1000.0, tolerances[0],
                                           tolerances[1]);

        final NumericalPropagator propagator =
            new NumericalPropagator(integrator);

        final NormalizedSphericalHarmonicsProvider provider =
            GravityFieldFactory.getNormalizedProvider(10, 10);
        final ForceModel holmesFeatherstone =
            new HolmesFeatherstoneAttractionModel(FramesFactory
                .getITRF(IERSConventions.IERS_2010, true), provider);

        propagator.setOrbitType(OrbitType.CARTESIAN);
        propagator.addForceModel(holmesFeatherstone);
        propagator.setInitialState(initialState);

        final EphemerisGenerator generator = propagator.getEphemerisGenerator();

        final SinusoidalLof sinusoidalLof =
            new SinusoidalLof(FramesFactory.getEME2000(), LOFType.VNC,
                              Vector3D.PLUS_I, 3600, FastMath.toRadians(45.0),
                              initialState.getDate());
        propagator.setAttitudeProvider(sinusoidalLof);

        propagator.propagate(startDate, finalDate);
        final BoundedPropagator boundedPropagator =
            generator.getGeneratedEphemeris();

        // Creation of the satellite
        final Spacecraft spacecraft =
            Spacecraft.builder(boundedPropagator, clock).withColor(Color.RED)
                .withOnlyOnePeriod().withDisplayAttitude().withReferenceSystem()
                .build();

        final AttitudePointing pointing =
            AttitudePointing
                .builder(spacecraft, getEarth(), Vector3D.MINUS_K, clock)
                .withColor(Color.ORANGE).displayPointingPath()
                .displayPeriodPointingPath().build();

        // Creation of the field of observation of the satellite, it describes
        // the area the satellite see
        final Transform initialInertToBody =
            initialState.getFrame().getTransformTo(getEarth().getBodyFrame(),
                                                   initialState.getDate());
        final Transform initialFovBody =
            new Transform(initialState.getDate(),
                          initialState.toTransform().getInverse(),
                          initialInertToBody);
        final FieldOfView fov =
            new DoubleDihedraFieldOfView(Vector3D.MINUS_K, Vector3D.PLUS_I,
                                         FastMath.toRadians(20),
                                         Vector3D.PLUS_J,
                                         FastMath.toRadians(20), 2);

        final FieldOfObservation fieldOfObservation =
            FieldOfObservation.builder(spacecraft, fov, initialFovBody).build();

        // Creation of the surface covered
        final CoveredSurfaceOnBody surface =
            CoveredSurfaceOnBody.builder(spacecraft, fieldOfObservation)
                .withColor(Color.RED).withFill(false).withOutline(true).build();

        builder.withSpacecraft(spacecraft);
        builder.withAttitudePointing(pointing);
        builder.withFieldOfObservation(fieldOfObservation);
        builder.withCoveredSurfaceOnBody(surface);
        String czml = builder.build().toString();

        final Matcher matcher =
            Pattern.compile("COVERED_SURFACE").matcher(czml);
        assertEquals(30, matcher.results().count());
    }

    @Test
    public void testWithLatLong() {
        final LatLongLines latLong = LatLongLines.builder(clock).build();
        builder.withLatLong(latLong);
        final String czml = builder.build().toString();

        final Matcher matcher = Pattern.compile("LAT_LONG").matcher(czml);
        assertEquals(48, matcher.results().count());
    }

    @Test
    public void testWithLatLongList() {
        final OneAxisEllipsoid earth = getEarth();
        final LatLongLines latLong1 = LatLongLines.builder(clock).build();
        final LatLongLines latLong2 = LatLongLines.builder(clock).build();
        final List<LatLongLines> latLongs = Arrays.asList(latLong1, latLong2);

        builder.withLatLong(latLongs);
        String czml = builder.build().toString();

        final Matcher matcher = Pattern.compile("LAT_LONG").matcher(czml);
        assertEquals(48, matcher.results().count());
    }
}
