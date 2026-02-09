package org.orekit.czml.object.secondary;

import org.hipparchus.ode.nonstiff.AdaptiveStepsizeIntegrator;
import org.hipparchus.ode.nonstiff.DormandPrince853Integrator;
import org.hipparchus.util.FastMath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.orekit.annotation.DefaultDataContext;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.czml.file.AbstractTest;
import org.orekit.czml.file.CzmlFile;
import org.orekit.czml.object.primary.Header;
import org.orekit.czml.object.primary.entities.CzmlGroundStation;
import org.orekit.czml.object.primary.entities.Spacecraft;
import org.orekit.czml.object.primary.visu.LineOfVisibility;
import org.orekit.forces.ForceModel;
import org.orekit.forces.gravity.HolmesFeatherstoneAttractionModel;
import org.orekit.forces.gravity.potential.GravityFieldFactory;
import org.orekit.forces.gravity.potential.NormalizedSphericalHarmonicsProvider;
import org.orekit.frames.FramesFactory;
import org.orekit.frames.TopocentricFrame;
import org.orekit.orbits.KeplerianOrbit;
import org.orekit.orbits.OrbitType;
import org.orekit.orbits.PositionAngleType;
import org.orekit.propagation.BoundedPropagator;
import org.orekit.propagation.EphemerisGenerator;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.numerical.NumericalPropagator;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScalesFactory;
import org.orekit.utils.Constants;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Test class for the visibility triangle object.
 *
 * @author Julien Leblond
 * @since 1.1
 */
public class VisibilityTriangleTest
    extends
    AbstractTest {

    /** Initialise orekit data. */
    private final double data = initializeOrekitData();

    // Dates

    /** Start date. */
    final AbsoluteDate startDate =
        new AbsoluteDate(2024, 3, 15, 0, 0, 0.0, TimeScalesFactory.getUTC());

    /** Final date. */
    final AbsoluteDate finalDate = startDate.shiftedBy(10.0 * 3600.0);

    /** Clock. */
    final Clock clock =
        new Clock(startDate, finalDate, AbstractTest.STEP_BETWEEN_EACH_INSTANT);

    /** Header. */
    final Header header = new Header("Dummy Header", clock);

    /** Toulouse point. */
    final GeodeticPoint toulousePoint =
        new GeodeticPoint(FastMath.toRadians(43.6047),
                          FastMath.toRadians(1.4442), 10);

    /** Toulouse frame. */
    final TopocentricFrame topocentricToulouse =
        new TopocentricFrame(getEarth(), toulousePoint, "Toulouse Frame");

    // Orbit
    final KeplerianOrbit initialOrbit =
        new KeplerianOrbit(7878000, 0, FastMath.toRadians(80), 0,
                           FastMath.toRadians(90), FastMath.toRadians(0),
                           PositionAngleType.MEAN, FramesFactory.getEME2000(),
                           startDate, Constants.WGS84_EARTH_MU);

    /** Constructor test. */
    @Test
    @DefaultDataContext
    @DisplayName("Visibility triangle constructor test")
    void VisibilityTriangleConstructorTest()
        throws URISyntaxException,
            IOException {

        final BoundedPropagator boundedPropagator =
            propagatorFromOrbit(startDate, finalDate, initialOrbit);

        final Spacecraft spacecraft =
            Spacecraft.builder(boundedPropagator, header.getClock()).build();

        // Line of visibility with a visibility triangle
        final LineOfVisibility coverageLine =
            LineOfVisibility
                .builder(topocentricToulouse, spacecraft, header.getClock())
                .withVisibilityTriangle().withCustomID("CustomID")
                .withAngleOfAperture(90.0).build();

        final String linePathFile =
            loadResources("templateFile/object/secondary/visibilitytriangle/VisibilityTriangleConstructorTemplate.txt");

        verifyFileOutput(linePathFile, coverageLine.toString(), 1e-8);
    }

    @Test
    @DisplayName("Visibility triangle coverage Czml File test")
    public void VisibilityTriangleCzmlFileTest()
        throws URISyntaxException,
            IOException {

        final String output = generateOutput();

        // Ground Station
        final CzmlGroundStation station =
            CzmlGroundStation.builder(topocentricToulouse, clock).build();

        // Spacecraft
        final BoundedPropagator boundedPropagator =
            propagatorFromOrbit(startDate, finalDate, initialOrbit);
        final Spacecraft spacecraft =
            Spacecraft.builder(boundedPropagator, header.getClock()).build();

        // Line of visibility with a visibility triangle
        final LineOfVisibility lineOfVisibility =
            LineOfVisibility
                .builder(topocentricToulouse, spacecraft, header.getClock())
                .withVisibilityTriangle().withCustomID("CustomID")
                .withAngleOfAperture(90.0).build();

        // Czml File
        final CzmlFile file =
            CzmlFile.builder(header).withSpacecraft(spacecraft)
                .withCzmlGroundStation(station)
                .withLineOfVisibility(lineOfVisibility).build();

        // Reference file
        final String entireFilePathFile =
            loadResources("templateFile/object/secondary/visibilitytriangle/VisibilityTriangleTestFileTemplate.txt");

        verifyFileOutput(entireFilePathFile, file.toString(), 1e-8);

        file.write(output);
    }

    private BoundedPropagator propagatorFromOrbit(final AbsoluteDate startDate,
                                                  final AbsoluteDate finalDate,
                                                  final KeplerianOrbit orbit) {
        final SpacecraftState initialState = new SpacecraftState(orbit);
        final NormalizedSphericalHarmonicsProvider provider =
            GravityFieldFactory.getNormalizedProvider(10, 10);
        final ForceModel holmesFeatherstone =
            new HolmesFeatherstoneAttractionModel(FramesFactory.getEME2000(),
                                                  provider);

        final double[][] tolerances =
            NumericalPropagator.tolerances(10, orbit, OrbitType.CARTESIAN);
        final AdaptiveStepsizeIntegrator integrator =
            new DormandPrince853Integrator(0.001, 1000.0, tolerances[0],
                                           tolerances[1]);

        final NumericalPropagator propagator =
            new NumericalPropagator(integrator);

        propagator.setOrbitType(OrbitType.CARTESIAN);
        propagator.addForceModel(holmesFeatherstone);
        propagator.setInitialState(initialState);

        final EphemerisGenerator generator = propagator.getEphemerisGenerator();

        propagator.propagate(startDate, finalDate);
        return generator.getGeneratedEphemeris();
    }
}
