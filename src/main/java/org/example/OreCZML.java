package org.example;

import org.example.Inputs.OrbitInput.OrbitInput;
import org.example.Inputs.SpacecraftStateInput.SpacecraftStateListInput;
import org.example.Outputs.OutputFiles.CZMLFile;
import org.example.Inputs.InputFiles.OEMFile;
import org.example.CZMLObjects.CZMLPrimaryObjects.Header;
import org.example.CZMLObjects.CZMLPrimaryObjects.Satellite;
import org.hipparchus.analysis.function.Constant;
import org.orekit.data.DataContext;
import org.orekit.data.DataProvider;
import org.orekit.data.ZipJarCrawler;
import org.orekit.errors.OrekitException;
import org.orekit.files.ccsds.definitions.FrameFacade;
import org.orekit.forces.ForceModel;
import org.orekit.forces.gravity.HolmesFeatherstoneAttractionModel;
import org.orekit.forces.gravity.potential.GravityFieldFactory;
import org.orekit.forces.gravity.potential.NormalizedSphericalHarmonicsProvider;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.orbits.KeplerianOrbit;
import org.orekit.orbits.Orbit;
import org.orekit.orbits.OrbitType;
import org.orekit.orbits.PositionAngleType;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.analytical.tle.TLE;
import org.orekit.propagation.analytical.tle.TLEPropagator;
import org.orekit.propagation.sampling.OrekitFixedStepHandler;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScale;
import org.orekit.time.TimeScalesFactory;
import org.orekit.utils.Constants;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class OreCZML
{
    public static void main( String[] args ) throws IOException {

        try {
            final File home = new File(System.getProperty("user.home"));
            final File orekitDir = new File(home, "orekit-data.zip");
            final DataProvider provider = new ZipJarCrawler(orekitDir);
            DataContext.getDefault().getDataProvidersManager().addProvider(provider);
        } catch (OrekitException oe) {
            System.err.println(oe.getLocalizedMessage());
        }

        /* OEM FILE TEST :

        String pathName = "C:/Users/jleblond/Documents/git/Benchmark/Outputs";
        String inputName = "ephemeris01.xml";
        String outputName = "Output_CZML.txt";
        String inputPath = pathName + "/" + inputName;
        String outputPath = pathName + "/" + outputName;

        OEMFile oemFile = new OEMFile(inputPath);

        // Header
        Header headerOEM = oemFile.getHeader();
        headerOEM.write();

        // Satellite
        Satellite satelliteCZML = new Satellite(oemFile);
        satelliteCZML.write();
        satelliteCZML.endFile();

        //Output File
        CZMLFile CZMLfile = new CZMLFile(pathName,outputPath);
        CZMLFile.write(headerOEM);
        CZMLFile.write(satelliteCZML);

        */


        /*
        //KEPLERIAN ORBIT TEST

        String pathName = "C:/Users/jleblond/Documents/git/Benchmark/Outputs";
        String outputName = "Output_CZML.txt";
        String outputPath = pathName + "/" + outputName;

        // Orbit
        Frame EME2000 = FramesFactory.getEME2000();
        TimeScale UTC = TimeScalesFactory.getUTC();
        AbsoluteDate date = new AbsoluteDate(2022,1,17,12,0,0.0,UTC);
        KeplerianOrbit orbit = new KeplerianOrbit(42378000,0,0,0,90,180, PositionAngleType.MEAN,EME2000,date, Constants.WGS84_EARTH_MU);
        OrbitInput orbitInput = new OrbitInput(orbit, OrbitType.KEPLERIAN,UTC);

        // Header
        Header header = orbitInput.getHeader();
        header.write();

        // Satellite
        Satellite satelliteOrbit = new Satellite(orbitInput);
        satelliteOrbit.write();
        satelliteOrbit.endFile();

        // Output File
        CZMLFile CZMLfile = new CZMLFile(pathName,outputPath);
        CZMLFile.write(header);
        CZMLFile.write(satelliteOrbit);

        */


        // SpaceCraftStateList
        String pathName = "C:/Users/jleblond/Documents/git/Benchmark/Outputs";
        String outputName = "Output_CZML.txt";
        String outputPath = pathName + "/" + outputName;

        // Build of a SpacecraftStateList
        TLE tle = new TLE( "1 25544U 98067A   24145.18425404  .00021821  00000+0  36530-3 0  9997"
                ,"2 25544  51.6393  74.2911 0003297 199.8507 300.9902 15.51793224454828");

        TLEPropagator propagator = TLEPropagator.selectExtrapolator(tle);

        List<SpacecraftState> spacecraftStateList = new ArrayList<SpacecraftState>();
        AbsoluteDate startTime = propagator.getInitialState().getDate();
        Double period = propagator.getInitialState().getKeplerianPeriod();

        final NormalizedSphericalHarmonicsProvider provider = GravityFieldFactory.getNormalizedProvider(10, 10);
        final ForceModel holmesFeatherstone = new HolmesFeatherstoneAttractionModel(propagator.getFrame(), provider);

        propagator.getMultiplexer().add(60.0, new OrekitFixedStepHandler() {
            @Override
            public void handleStep(SpacecraftState currentState) {
                spacecraftStateList.add(currentState);
            }
        });

        final SpacecraftState finalState = propagator.propagate(startTime.shiftedBy(period));
        final KeplerianOrbit finalOrbit = (KeplerianOrbit) OrbitType.KEPLERIAN.convertType(finalState.getOrbit());

        SpacecraftStateListInput input = new SpacecraftStateListInput(spacecraftStateList);

        // Header
        Header header = input.getHeader();
        header.write();

        // Satellite
        Satellite satelliteSpacecraftStates = new Satellite(input);
        satelliteSpacecraftStates.write();
        satelliteSpacecraftStates.endFile();

        // Output File
        CZMLFile CZMLfile = new CZMLFile(pathName,outputPath);
        CZMLFile.write(header);
        CZMLFile.write(satelliteSpacecraftStates);


    }
}
