package org.example.adaptors;

import org.example.TutorialUtils;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.czml.archi.adaptor.AemAdaptor;
import org.orekit.czml.archi.adaptor.OemAdaptor;
import org.orekit.czml.archi.builder.SatelliteBuilder;
import org.orekit.czml.object.primary.CzmlGroundStation;
import org.orekit.czml.object.primary.Header;
import org.orekit.czml.object.primary.Satellite;
import org.orekit.czml.object.secondary.Clock;
import org.orekit.czml.object.secondary.Orientation;
import org.orekit.czml.file.CzmlFile;
import org.orekit.czml.file.CzmlFileBuilder;
import org.orekit.data.DataSource;
import org.orekit.files.ccsds.ndm.ParserBuilder;
import org.orekit.files.ccsds.ndm.adm.aem.Aem;
import org.orekit.files.ccsds.ndm.adm.aem.AemParser;
import org.orekit.files.ccsds.ndm.odm.oem.Oem;
import org.orekit.files.ccsds.ndm.odm.oem.OemParser;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.frames.TopocentricFrame;
import org.orekit.propagation.BoundedPropagator;
import org.orekit.propagation.EphemerisGenerator;
import org.orekit.propagation.Propagator;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScale;
import org.orekit.time.TimeScalesFactory;
import org.orekit.utils.Constants;
import org.orekit.utils.IERSConventions;

import static org.example.TutorialUtils.loadResources;

public class AemAdaptorExample {

    private AemAdaptorExample() {
        // empty
    }

    public static void main(final String[] args) throws Exception {

        // Load orekit data
        TutorialUtils.loadOrekitData();

        // Paths
        final String root = System.getProperty("user.dir")
                                  .replace("\\", "/");
        final String outputPath = root + "/Output";
        final String outputName = "Output.czml";
        final String output     = outputPath + "/" + outputName;
        // Change the path here to your JavaScript>public folder.
        final String pathToJSFolder = root + "/Javascript/public/";

        final String OemPath  = loadResources("oemForAemTuto.xml");
        final String AemPath  = loadResources("aemForAemTuto.xml");
        final String IssModel = loadResources("Default3DModels/ISSModel.glb");

        // Creation of the Oem
        final DataSource    dataSourceOem    = new DataSource(OemPath);
        final ParserBuilder parserBuilderOem = new ParserBuilder();
        final OemParser     oemParser        = parserBuilderOem.buildOemParser();
        final Oem           oem              = oemParser.parse(dataSourceOem);

        // Creation of the Aem
        final DataSource    dataSourceAem    = new DataSource(AemPath);
        final ParserBuilder parserBuilderAem = new ParserBuilder();
        final AemParser     aemParser        = parserBuilderAem.buildAemParser();
        final Aem           aem              = aemParser.parse(dataSourceAem);

        // Adaptor for oem
        final OemAdaptor         oemAdaptor    = new OemAdaptor(oem);
        final Propagator         oemPropagator = oemAdaptor.buildPropagator();
        final AbsoluteDate       startDate     = oemAdaptor.buildStartDate();
        final AbsoluteDate       finalDate     = oemAdaptor.buildFinalDate();
        final EphemerisGenerator generator     = oemPropagator.getEphemerisGenerator();
        oemPropagator.propagate(startDate, finalDate);
        final BoundedPropagator oemBoundedPropagator = generator.getGeneratedEphemeris();

        // Creation of the clock
        final TimeScale UTC                    = TimeScalesFactory.getUTC();
        final double    stepBetweenEachInstant = 60.0; // in seconds
        final Clock     clock                  = new Clock(startDate, finalDate, UTC, stepBetweenEachInstant);

        // Creation of the header
        final Header header = new Header("Aem Adaptor Example", clock, pathToJSFolder);

        // Creation of the model of the earth.
        final IERSConventions IERS = IERSConventions.IERS_2010;
        final Frame           ITRF = FramesFactory.getITRF(IERS, true);
        final OneAxisEllipsoid earth = new OneAxisEllipsoid(Constants.WGS84_EARTH_EQUATORIAL_RADIUS,
                Constants.WGS84_EARTH_FLATTENING, ITRF);

        // Creation of the orientation for the satellite with the aem adaptor
        // Careful here, the header must be set before else way the bounded propagator does not have a reference for the timescale.
        final AemAdaptor  aemAdaptor  = new AemAdaptor(aem);
        final Orientation orientation = aemAdaptor.buildOrientation(oemBoundedPropagator);

        // Creation of the satellite
        final Satellite satellite = new SatelliteBuilder(oemBoundedPropagator).withModelPath(IssModel)
                                                                              .withOrientation(orientation)
                                                                              .build();


        final CzmlGroundStation groundStation = new CzmlGroundStation(
                new TopocentricFrame(earth, new GeodeticPoint(0, 0, 0), "Station"), "");

        final CzmlFile file = CzmlFile.builder(output).withHeader(header)
                                                         .withSatellite(satellite)
                                                         .withCzmlGroundStation(groundStation)
                                                         .build();

        // Writing the file
        file.write();
    }

}
