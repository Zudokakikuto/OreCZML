package org.example;

import org.example.CZMLObjects.CZMLPrimaryObjects.*;
import org.example.Inputs.InputFiles.OEMFile;
import org.example.Inputs.OrbitInput.OrbitInput;
import org.example.Outputs.OutputFiles.CZMLFile;
import org.hipparchus.analysis.function.Abs;
import org.hipparchus.ode.nonstiff.AdaptiveStepsizeIntegrator;
import org.hipparchus.ode.nonstiff.DormandPrince853Integrator;
import org.hipparchus.util.FastMath;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.data.DataContext;
import org.orekit.data.DataProvider;
import org.orekit.data.ZipJarCrawler;
import org.orekit.errors.OrekitException;
import org.orekit.forces.ForceModel;
import org.orekit.forces.gravity.HolmesFeatherstoneAttractionModel;
import org.orekit.forces.gravity.potential.GravityFieldFactory;
import org.orekit.forces.gravity.potential.NormalizedSphericalHarmonicsProvider;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.frames.TopocentricFrame;
import org.orekit.orbits.KeplerianOrbit;
import org.orekit.orbits.OrbitType;
import org.orekit.orbits.PositionAngleType;
import org.orekit.propagation.PropagationType;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.numerical.NumericalPropagator;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScale;
import org.orekit.time.TimeScalesFactory;
import org.orekit.utils.Constants;
import org.orekit.utils.IERSConventions;

import java.io.*;
import java.net.URISyntaxException;
import java.util.ArrayList;


public class OreCZML
{
    public static void main( String[] args ) throws IOException, URISyntaxException {

        try {
            final File home = new File(System.getProperty("user.home"));
            final File orekitDir = new File(home, "orekit-data.zip");
            final DataProvider provider = new ZipJarCrawler(orekitDir);
            DataContext.getDefault().getDataProvidersManager().addProvider(provider);
        } catch (OrekitException oe) {
            System.err.println(oe.getLocalizedMessage());
        }

        //OEM FILE TEST :
        String root = System.getProperty("user.dir").replace("\\","/");
        String resources = root + "/" + "src/main/resources";
        String outputPath = root + "/" + "Output";

        String inputOemMission = "oemMission.kvn";
        String inputOemChaser = "oemChaser.kvn";
        String inputOemMissionPath = resources + "/" + inputOemMission;
        String inputOemChaserPath = resources + "/" +inputOemChaser;

        String outputName = "Output_CZML.txt";
        String output = outputPath + "/" + outputName;

        OEMFile oemFileMission = new OEMFile(inputOemMissionPath);
        OEMFile oemFileChaser = new OEMFile(inputOemChaserPath);

        // Propagator
        Frame EME2000 = FramesFactory.getEME2000();
        TimeScale UTC = TimeScalesFactory.getUTC();
        AbsoluteDate startDate = new AbsoluteDate(2022,1,17,12,0,0.0,UTC);
        AbsoluteDate finalDate = new AbsoluteDate(2022,1,18,12,0,0.0,UTC);
        KeplerianOrbit initialOrbit = new KeplerianOrbit(7378000,0,FastMath.toRadians(60),0,FastMath.toRadians(90),FastMath.toRadians(90),PositionAngleType.MEAN,EME2000,startDate,Constants.WGS84_EARTH_MU);
        //KeplerianOrbit initialOrbit = new KeplerianOrbit(42164000,0,0,0,FastMath.toRadians(90),FastMath.toRadians(0), PositionAngleType.MEAN,EME2000,startDate, Constants.WGS84_EARTH_MU);
        final SpacecraftState initialState = new SpacecraftState(initialOrbit);

        final double positionTolerance = 10.0;
        final double minStep = 0.001;
        final double maxStep = 1000.0;

        final double[][] tolerances = NumericalPropagator.tolerances(positionTolerance, initialOrbit, OrbitType.CARTESIAN);
        final AdaptiveStepsizeIntegrator integrator = new DormandPrince853Integrator(minStep, maxStep, tolerances[0], tolerances[1]);

        final NumericalPropagator propagator = new NumericalPropagator(integrator);

        final NormalizedSphericalHarmonicsProvider provider = GravityFieldFactory.getNormalizedProvider(10, 10);
        final ForceModel holmesFeatherstone = new HolmesFeatherstoneAttractionModel(EME2000, provider);

        propagator.setOrbitType(OrbitType.CARTESIAN);
        propagator.addForceModel(holmesFeatherstone);
        propagator.setInitialState(initialState);

        // Topocentric frame
        IERSConventions IERS = IERSConventions.IERS_2010;
        Frame ITRF = FramesFactory.getITRF(IERS,true);
        OneAxisEllipsoid earth = new OneAxisEllipsoid(Constants.WGS84_EARTH_EQUATORIAL_RADIUS,Constants.WGS84_EARTH_FLATTENING,ITRF);
        //Position of Toulouse
        GeodeticPoint geodeticPoint = new GeodeticPoint(FastMath.toRadians(43.6047),FastMath.toRadians(1.4442),118);
        TopocentricFrame topocentricFrame = new TopocentricFrame(earth,geodeticPoint,"Toulouse Frame");

        // Header
        Header headerPropagator = new Header(propagator,finalDate);
        headerPropagator.generateCZML();

        // Satellite
        Satellite satellitePropagator = new Satellite(propagator,finalDate,headerPropagator);
        satellitePropagator.generateCZML();

        //Ground Station
        GroundStation groundStation = new GroundStation(topocentricFrame,headerPropagator);
        groundStation.generateCZML();

        // Line of visibility
        LineOfVisibility lineOfVisibility = new LineOfVisibility(topocentricFrame,satellitePropagator,headerPropagator,85);
        lineOfVisibility.generateCZML();
        lineOfVisibility.endFile();

        CZMLFile CZMLfile = new CZMLFile(outputPath,output);
        CZMLFile.write(headerPropagator);
        CZMLFile.write(satellitePropagator);
        CZMLFile.write(lineOfVisibility);

        /*
        //KEPLERIAN ORBIT TEST

        String pathName = System.getProperty("user.dir").replace("\\","/");
        String pathNameOutput = pathName + "/Output";
        String outputName = "Output_CZML.txt";
        String outputPath = pathNameOutput + "/" + outputName;
        String ressourcesPath = pathName + "/src/main/resources";
        String modele3D = "CesiumMilkTruck.glb";
        String Ingenuity3D = "Ingenuity/GLB/Ingenuity.glb";
        String modele3DPath = ressourcesPath + "/" + modele3D;
        String IngenuityPath = ressourcesPath + "/" + Ingenuity3D;

        // Orbit
        Frame EME2000 = FramesFactory.getEME2000();
        TimeScale UTC = TimeScalesFactory.getUTC();
        AbsoluteDate date = new AbsoluteDate(2022,1,17,12,0,0.0,UTC);
        KeplerianOrbit orbit1 = new KeplerianOrbit(42378000,0,0,0,FastMath.toRadians(90),FastMath.toRadians(180), PositionAngleType.MEAN,EME2000,date, Constants.WGS84_EARTH_MU);
        KeplerianOrbit orbit2 = new KeplerianOrbit(7378000,0,FastMath.toRadians(60),0,FastMath.toRadians(90),FastMath.toRadians(90),PositionAngleType.MEAN,EME2000,date,Constants.WGS84_EARTH_MU);
        OrbitInput orbitInput1 = new OrbitInput(orbit1, OrbitType.KEPLERIAN,UTC);
        OrbitInput orbitInput2 = new OrbitInput(orbit2, OrbitType.KEPLERIAN,UTC);

        // Topocentric Frame
        IERSConventions IERS = IERSConventions.IERS_2010;
        Frame ITRF = FramesFactory.getITRF(IERS,true);
        OneAxisEllipsoid earth = new OneAxisEllipsoid(Constants.WGS84_EARTH_EQUATORIAL_RADIUS,Constants.WGS84_EARTH_FLATTENING,ITRF);
        //Position of Toulouse
        GeodeticPoint geodeticPoint = new GeodeticPoint(FastMath.toRadians(43.6047),FastMath.toRadians(1.4442),118);
        TopocentricFrame topocentricFrame = new TopocentricFrame(earth,geodeticPoint,"Toulouse Frame");

        // Header
        Header header = orbitInput1.getHeader();
        header.generateCZML();

        // Ground station
       GroundStation OrekitGroundStation = new org.orekit.estimation.measurements.GroundStation(topocentricFrame);
       org.example.CZMLObjects.CZMLPrimaryObjects.GroundStation groundStation =
               new org.example.CZMLObjects.CZMLPrimaryObjects.GroundStation(OrekitGroundStation,header);
       groundStation.generateCZML();

        // Satellites
        Satellite satelliteOrbit1 = new Satellite(orbitInput1,header);
        Satellite satelliteOrbit2 = new Satellite(orbitInput2,header,IngenuityPath);
        satelliteOrbit1.generateCZML();
        satelliteOrbit2.generateCZML();

        //Visibility cone
        VisibilityCone visibilityCone = new VisibilityCone(groundStation,satelliteOrbit2,header);
        visibilityCone.noDisplay();
        visibilityCone.generateCZML();

        //Line of visibility (the visibility cone needs to be builds with a satellite and written for the line of visibility to works)
        LineOfVisibility lineOfVisibility = new LineOfVisibility(visibilityCone);
        lineOfVisibility.generateCZML();
        lineOfVisibility.endFile();

        // Output File
        CZMLFile CZMLfile = new CZMLFile(pathNameOutput,outputPath);
        CZMLFile.write(header);
        CZMLFile.write(satelliteOrbit1);
        CZMLFile.write(satelliteOrbit2);
        CZMLFile.write(groundStation);
        CZMLFile.write(visibilityCone);
        CZMLFile.write(lineOfVisibility);

        */


        /*
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
        Satellite satelliteSpacecraftStates = new Satellite(input,header);
        satelliteSpacecraftStates.write();
        satelliteSpacecraftStates.endFile();

        // Output File
        CZMLFile CZMLfile = new CZMLFile(pathName,outputPath);
        CZMLFile.write(header);
        CZMLFile.write(satelliteSpacecraftStates);
         */

    }
}
