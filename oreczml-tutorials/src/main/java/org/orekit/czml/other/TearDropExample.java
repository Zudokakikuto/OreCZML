package org.orekit.czml.other;

import org.orekit.czml.TutorialUtils;
import org.orekit.propagation.SpacecraftState;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScale;
import org.orekit.time.TimeScalesFactory;
import org.orekit.utils.AbsolutePVCoordinates;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TearDropExample {

    private TearDropExample() {
        // empty
    }

    /**
     * Main of the tear drop tutorial.
     *
     * @param args the args
     * @throws Exception the exception
     */
    public static void main(final String[] args) throws Exception {

        TutorialUtils.loadOrekitData();

        final String             pathToCsv      = TutorialUtils.loadResources("teardrop.csv");
        final BufferedReader     bufferedReader = new BufferedReader(new FileReader(pathToCsv));
        final List<AbsoluteDate> absoluteDates  = new ArrayList<>();
        final List<Double>       times          = new ArrayList<>();
        final List<Double>       xList          = new ArrayList<>();
        final List<Double>       yList          = new ArrayList<>();
        final List<Double>       zList          = new ArrayList<>();
        final List<Double>       vxList         = new ArrayList<>();
        final List<Double>       vyList         = new ArrayList<>();
        final List<Double>       vzList         = new ArrayList<>();
        final TimeScale          UTC            = TimeScalesFactory.getUTC();
        while (bufferedReader.readLine() != null) {
            final String       line             = bufferedReader.readLine();
            final String[]     splittedLines    = line.split(",");
            final List<String> splittedIntoList = Arrays.asList(splittedLines);
            absoluteDates.add(new AbsoluteDate(splittedIntoList.get(0), UTC));
            times.add(Double.parseDouble(splittedIntoList.get(1)));
            xList.add(Double.parseDouble(splittedIntoList.get(2)));
            yList.add(Double.parseDouble(splittedIntoList.get(3)));
            zList.add(Double.parseDouble(splittedIntoList.get(4)));
            vxList.add(Double.parseDouble(splittedIntoList.get(5)));
            vyList.add(Double.parseDouble(splittedIntoList.get(6)));
            vzList.add(Double.parseDouble(splittedIntoList.get(7)));
        }
//        final List<SpacecraftState> states = new ArrayList<>();
//        for (int i = 0; i < xList.size(); i++) {
//            final AbsolutePVCoordinates absolutePVCoordinates = new AbsolutePVCoordinates()
//            states.add(new SpacecraftState()
//        }
    }
}
