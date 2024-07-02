import org.hipparchus.util.FastMath;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.Header;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.Satellite;
import org.orekit.czml.CzmlObjects.CzmlSecondaryObjects.HeaderObjects.Clock;
import org.orekit.czml.Outputs.CzmlFile;
import org.orekit.data.DataContext;
import org.orekit.data.DataProvider;
import org.orekit.data.DirectoryCrawler;
import org.orekit.errors.OrekitException;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.orbits.KeplerianOrbit;
import org.orekit.orbits.PositionAngleType;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScale;
import org.orekit.time.TimeScalesFactory;
import org.orekit.utils.Constants;

import java.io.File;

public class LowEarthOrbitSatellite {

    private LowEarthOrbitSatellite() {
        // empty
    }

    public static void main(final String[] args ) throws Exception {
        try {
            final File home = new File(System.getProperty("user.home"));
            final File orekitDir = new File(home, "orekit-data");
            final DataProvider provider = new DirectoryCrawler(orekitDir);
            DataContext.getDefault().getDataProvidersManager().addProvider(provider);
        } catch (OrekitException oe) {
            System.err.println(oe.getLocalizedMessage());
        }

        // Paths
        final String root = System.getProperty("user.dir").replace("\\", "/");
        final String outputPath = root + "/Output";
        final String outputName = "Output.czml";
        final String output = outputPath + "/" + outputName;

        // File created
        final CzmlFile file = new CzmlFile(output);

        // Creation of the clock
        final TimeScale UTC = TimeScalesFactory.getUTC();
        final double durationOfSimulation = 5 * 3600; // in seconds;
        final double stepBetweenEachInstant = 60.0; // in seconds
        final AbsoluteDate startDate = new AbsoluteDate(2024, 3, 15, 0, 0, 0.0, UTC);
        final AbsoluteDate finalDate = startDate.shiftedBy(durationOfSimulation);
        final Clock clock = new Clock(startDate, finalDate, UTC, stepBetweenEachInstant);

        // Creation of the header
        final Header header = new Header("Low Earth Orbit Tutorial", clock);
        file.addObject(header);

        // Build of the LEO orbit
        final Frame EME2000 = FramesFactory.getEME2000();
        final KeplerianOrbit initialOrbit = new KeplerianOrbit(7878000, 0, FastMath.toRadians(10), 0, FastMath.toRadians(90), FastMath.toRadians(0), PositionAngleType.MEAN, EME2000, startDate, Constants.WGS84_EARTH_MU);

        // Build the LEO Satellite
        final Satellite leoSatellite = new Satellite(initialOrbit);
        file.addObject(leoSatellite);

        // Write inside the CzmlFile the objects
        file.write();
    }
}
