package org.example.OLDOrbitGenerator;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Locale;

import cesiumlanguagewriter.*;
import org.orekit.data.DataContext;
import org.orekit.data.DataProvidersManager;
import org.orekit.data.DirectoryCrawler;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.orbits.KeplerianOrbit;
import org.orekit.orbits.Orbit;
import org.orekit.orbits.PositionAngleType;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.analytical.KeplerianPropagator;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.DateComponents;
import org.orekit.time.DateTimeComponents;
import org.orekit.time.TimeComponents;
import org.orekit.time.TimeScale;
import org.orekit.time.TimeScalesFactory;



import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.hipparchus.util.FastMath; 

public class OrbitGenerator {

    public static void main(String[] args) {
     // configure Orekit
        final File home       = new File(System.getProperty("user.home"));
        final File orekitData = new File(home, "orekit-data");
        if (!orekitData.exists()) {
            System.err.format(Locale.US, "Failed to find %s folder%n",
                              orekitData.getAbsolutePath());
            System.err.format(Locale.US, "You need to download %s from %s, unzip it in %s and rename it 'orekit-data' for this tutorial to work%n",
                              "orekit-data-master.zip", "https://gitlab.orekit.org/orekit/orekit-data/-/archive/master/orekit-data-master.zip",
                              home.getAbsolutePath());
            System.exit(1);
        }
        
        final DataProvidersManager manager = DataContext.getDefault().getDataProvidersManager();
        manager.addProvider(new DirectoryCrawler(orekitData));

        // Générer l'orbite et la stocker dans un objet
        OrbitData orbit = new OrbitData();
        System.out.println("Orbit generated");
        // Générer le fichier CZML à partir des données de positions et temps stockées dans l'objet OrbitData
        generateCZML(orbit);
    }

    public static void generateCZML(OrbitData CZMLData) {

        // Obtenir les données de temps et position
        ArrayList<Double> timeList = CZMLData.getTimeList();
        ArrayList<Vector3D> positionList = CZMLData.getPosList();

        // Transformation de la liste de Vector3D vers Cartesian (objet issue de CZML-Writer)
        ArrayList<Cartesian> cartesianList = vectorToCartesian(positionList);

        try {
            // Création fichier
            FileWriter myWriter = new FileWriter("czml_test.czml");

            //
            final JulianDate initialJD = new JulianDate(timeList.get(0));
            final JulianDate endJD = new JulianDate(timeList.get(timeList.size() - 1));

            // Création des flux Java et Cesium
            StringWriter sw = new StringWriter();
            CesiumOutputStream output = new CesiumOutputStream(sw);
            output.setPrettyFormatting(true);
            CesiumStreamWriter writer = new CesiumStreamWriter();

            // Début de fichier
            output.writeStartSequence();

            // Création du paquet de début (paquet avec ID document)
            PacketCesiumWriter docPacket = writer.openPacket(output);

            docPacket.writeId("document");
            docPacket.writeVersion("1.0");
            docPacket.writeName("Keplerian Test");

            // Définit les paramètres pour l'horloge
            // Ouverture d'une propriété
            ClockCesiumWriter clockWriter = docPacket.openClockProperty();
            clockWriter.writeMultiplier(60);
            clockWriter.writeInterval(initialJD, endJD);
            clockWriter.writeCurrentTime(initialJD);
            // Fermeture de la propriété
            clockWriter.close();

            // Fermeture paquet document
            docPacket.close();

            // !!!!! Bien fermer tout les paquets et propriétés ouverts !!!!!!

            // Paquet principal : Il semblerait qu'il faille 1 paquet par orbite
            PacketCesiumWriter packet = writer.openPacket(output);

            packet.writeId("TestScenario");
            packet.writeAvailability(initialJD, endJD);

            // Gestion icone du satellite
            BillboardCesiumWriter first_bill = packet.openBillboardProperty();

            String imageStr = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAADsMAAA7DAcdvqGQAAADJSURBVDhPnZHRDcMgEEMZjVEYpaNklIzSEfLfD4qNnXAJSFWfhO7w2Zc0Tf9QG2rXrEzSUeZLOGm47WoH95x3Hl3jEgilvDgsOQUTqsNl68ezEwn1vae6lceSEEYvvWNT/Rxc4CXQNGadho1NXoJ+9iaqc2xi2xbt23PJCDIB6TQjOC6Bho/sDy3fBQT8PrVhibU7yBFcEPaRxOoeTwbwByCOYf9VGp1BYI1BA+EeHhmfzKbBoJEQwn1yzUZtyspIQUha85MpkNIXB7GizqDEECsAAAAASUVORK5CYII=";
            first_bill.writeColorProperty(0, 255, 255, 195);
            UriCesiumWriter imageBill = first_bill.openImageProperty();
            imageBill.writeUri(imageStr, CesiumResourceBehavior.LINK_TO);
            imageBill.close();

            first_bill.writeScaleProperty(1.5);
            first_bill.writeShowProperty(true);

            first_bill.close();

            // Création d'une trajectoire
            PathCesiumWriter path = packet.openPathProperty();
            // Attention à la propriété show de la propriété path à bien mettre en place
            BooleanCesiumWriter showPath = path.openShowProperty();
            showPath.writeInterval(initialJD, endJD);
            showPath.writeBoolean(true);
            showPath.close();

            path.close();


            // Les JulianDates sont en JD
            // Les positions sont en X,Y,Z
            // Le référentiel peut être changé normalement
            ArrayList<JulianDate> dates = new ArrayList<>();
            ArrayList<Cartesian> positions = new ArrayList<>();

            // Ajout des dates et positions
            int cpt = 0;
            for (Cartesian position : cartesianList) {
                dates.add(new JulianDate(timeList.get(cpt)));
                System.out.println(timeList.get(cpt));
                positions.add(position);
                cpt++;
            }

            // Définir la propriété de position qui contient des paramètres, les temps et positions.
            PositionCesiumWriter posProperties = packet.openPositionProperty();
            posProperties.writeReferenceFrame("INERTIAL");
            posProperties.writeInterpolationAlgorithm(CesiumInterpolationAlgorithm.LAGRANGE);
            posProperties.writeInterpolationDegree(5);
            posProperties.writeCartesian(dates, positions);
            posProperties.close();

            packet.close();
            sw.close();

            // Fin de fichier
            output.writeEndSequence();

            // Ecrire dans le fichier czml et le fermer
            myWriter.write(sw.toString());
            myWriter.close();
            
            System.out.println("Working Directory = " + System.getProperty("user.dir"));
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Convert a list of Vector3D as produced by Orekit to a list of Cartesian as read by czml-writer
     * @param positionList List of positions using Vector3D
     * @return List of positions using Cartesian
     */
    private static ArrayList<Cartesian> vectorToCartesian(ArrayList<Vector3D> positionList) {
        ArrayList<Cartesian> cartesianList = new ArrayList<>();
        for (Vector3D position: positionList) {
            Cartesian posCartesian = new Cartesian(position.getX(), position.getY(), position.getZ());
            cartesianList.add(posCartesian);
        }
        return cartesianList;
    }

}

/**
 * Classe permettant de générer une orbite à partir d'une liste de position et temps, ou bien par sa méthode generateOrbit
 */
class OrbitData {

    private static final TimeScale utc = TimeScalesFactory.getUTC();
    
    private ArrayList<Vector3D> positionList;
    private ArrayList<Double> timeList;

    public OrbitData() {
        GenerateOrbit();
    }
    
    public OrbitData(ArrayList<AbsoluteDate> timeListAbsDate, ArrayList<Vector3D> positionList) {
        this.positionList = positionList;
        ArrayList<Double> timeListDouble = new ArrayList<>();
        for (AbsoluteDate date: timeListAbsDate){
            timeListDouble.add(dateToDouble(date));
        }
        this.timeList = timeListDouble;
    }

    // Génére une orbite simple
    private void GenerateOrbit() {
        Frame inertialFrame = FramesFactory.getEME2000();

        TimeScale utc = TimeScalesFactory.getUTC();
        AbsoluteDate initialDate = new AbsoluteDate(2020, 4, 28, 15, 0, 00.000, utc);

        double mu =  3.986004415e+14;

        double a = 6378e3 + 10000e3;                     // semi major axis in meters
        double e = 0.5;                   // eccentricity
        double i = FastMath.toRadians(7);        // inclination
        double omega = FastMath.toRadians(180);  // perigee argument
        double raan = FastMath.toRadians(261);   // right ascension of ascending node
        double lM = 0;                           // mean anomaly

        ArrayList<Vector3D> positionList = new ArrayList<>();
        ArrayList<Double> timeList = new ArrayList<>();

        Orbit initialOrbit = new KeplerianOrbit(a, e, i, omega, raan, lM, PositionAngleType.MEAN,inertialFrame, initialDate, mu);

        KeplerianPropagator kepler = new KeplerianPropagator(initialOrbit);

        double duration = initialOrbit.getKeplerianPeriod();
        AbsoluteDate finalDate = initialDate.shiftedBy(duration);
        double stepT = 60.;
        for (AbsoluteDate extrapDate = initialDate;
                extrapDate.compareTo(finalDate) <= 0;
                extrapDate = extrapDate.shiftedBy(stepT))  {
            SpacecraftState currentState = kepler.propagate(extrapDate);
            timeList.add(dateToDouble(extrapDate));
            System.out.println(extrapDate);
            positionList.add(currentState.getPVCoordinates().getPosition());
        }

        this.positionList = positionList;
        this.timeList = timeList;
    }

    // Convertit une AbsoluteDate d'Orekit vers un Astronomical JD en double
    private static double dateToDouble(AbsoluteDate date) {
        DateTimeComponents dtc = date.getComponents(utc);
        DateComponents dc = dtc.getDate();
        TimeComponents tc = dtc.getTime();
        double jd = dc.getMJD();
        double fracDay = tc.getSecondsInUTCDay();
        double finalDay = jd + fracDay / 86400 + 2400000.5;
        return finalDay;
    }
    
    public ArrayList<Vector3D> getPosList(){
        return positionList;
    }

    public ArrayList<Double> getTimeList(){
        return timeList;
    }

}