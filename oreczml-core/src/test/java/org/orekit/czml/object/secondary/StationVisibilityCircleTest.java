package org.orekit.czml.object.secondary;

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
import org.orekit.czml.object.primary.visu.StationVisibilityCircle;
import org.orekit.czml.object.utils.DateUtils;
import org.orekit.frames.TopocentricFrame;
import org.orekit.propagation.BoundedPropagator;
import org.orekit.time.AbsoluteDate;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Test class for the station visibility circle object.
 *
 * @author Julien Leblond
 * @since 1.1
 */
@DefaultDataContext
public class StationVisibilityCircleTest
    extends
    AbstractTest {

    /** Initialise orekit data. */
    private final double data = initializeOrekitData();

    // Header and clock
    final Header header = dummyHeader();

    final Clock clock = header.getClock();

    // Start and end date
    final AbsoluteDate startDate =
        DateUtils.toAbsoluteDate(clock.getAvailability().getStart());

    final AbsoluteDate endDate =
        DateUtils.toAbsoluteDate(clock.getAvailability().getStop());

    // Creation of the spacecraft
    final BoundedPropagator boundedPropagator =
        dummyPropagator(startDate, endDate, dummyOrbit(startDate));

    final Spacecraft spacecraft =
        Spacecraft.builder(boundedPropagator, clock).build();

    // Creation of the ground station
    final String frameName = "Toulouse Frame";

    final GeodeticPoint toulouseFrame =
        new GeodeticPoint(FastMath.toRadians(43.6047),
                          FastMath.toRadians(1.4442), 10);

    final TopocentricFrame topocentricToulouse =
        new TopocentricFrame(getEarth(), toulouseFrame, frameName);

    public StationVisibilityCircleTest()
        throws URISyntaxException,
            IOException {
    }

    /** Tests for the constructor of the station visibility circle object. */
    @Test
    @DisplayName("Station visibility circle cosntructor test")
    void stationVisibilityCircleConstructorTest()
        throws URISyntaxException,
            IOException {

        final CzmlGroundStation groundStation =
            CzmlGroundStation.builder(topocentricToulouse, clock).build();

        groundStation.displayCircle(spacecraft, 90.0);
        // Creation of the circle of visibility
        final StationVisibilityCircle circle =
            StationVisibilityCircle
                .builder(topocentricToulouse, spacecraft, clock).build();

        final String circlePathFile =
            loadResources("templateFile/object/secondary/StationVisibilityCircleConstructorTemplate.txt");

        verifyFileOutput(circlePathFile, circle.toString(), 1e-8);

    }

    @Test
    @DisplayName("Station visibility circle czml file test")
    public void StationVisibilityCircleCzmlFileTest()
        throws URISyntaxException,
            IOException {

        // output of the writing
        final String output = generateOutput();

        final CzmlGroundStation groundStation =
            CzmlGroundStation.builder(topocentricToulouse, clock).build();

        final Spacecraft spacecraft =
            Spacecraft.builder(boundedPropagator, clock).build();

        groundStation.displayCircle(spacecraft, 90.0);

        // Czml File
        final CzmlFile file =
            CzmlFile.builder(header).withSpacecraft(spacecraft)
                .withCzmlGroundStation(groundStation).build();

        // Reference file
        final String entirePathFile =
            loadResources("templateFile/object/secondary/StationVisibilityCircleTestFileTemplate.txt");

        verifyFileOutput(entirePathFile, file.toString(), 1e-8);

        file.write(output);
    }

}
