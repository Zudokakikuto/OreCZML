/** .*/

package org.example.CZMLObjects.CZMLPrimaryObjects;

import org.example.CZMLObjects.CZMLPrimaryObjects.CZMLPrimaryObject;
import org.example.Inputs.OrbitInput.OrbitInput;
import org.orekit.orbits.Orbit;
import org.orekit.orbits.OrbitType;
import org.orekit.orbits.WalkerConstellation;
import org.orekit.orbits.WalkerConstellationSlot;
import org.orekit.time.TimeScale;
import org.orekit.time.TimeScalesFactory;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Constellation extends WalkerConstellation implements CZMLPrimaryObject {

    /** .*/
    public static final String DEFAULT_STRING_3D_MODEL = "";

    /** .*/
    private int totalOfSatellite;
    /** .*/
    private int numberOfOrbitalPlanes;
    /** .*/
    private int phasingParameters;
    /** .*/
    private OrbitType orbitType;
    /** .*/
    private TimeScale timeScale;
    /** .*/
    private Header header;

    // intrinsic parameters
    /** .*/
    private List<List<Orbit>> allOrbits;
    /** .*/
    private Orbit referenceOrbit;
    /** .*/
    private List<Satellite> allSatellites;
    /** .*/
    private boolean displayOnlyLastPeriod = false;
    /** .*/
    private String model3DPath;

    public Constellation(final int t, final int p, final int f, final Orbit referenceOrbit, final Header header) {
        this(t, p, f, referenceOrbit, DEFAULT_STRING_3D_MODEL, header);
    }

    public Constellation(final int t, final int p, final int f, final Orbit referenceOrbit, final String model3DPath, final Header header) {
        super(t, p, f);
        this.totalOfSatellite = t;
        this.numberOfOrbitalPlanes = p;
        this.phasingParameters = f;
        this.referenceOrbit = referenceOrbit;
        this.header = header;

        this.getOrbits(referenceOrbit);
        this.orbitType = referenceOrbit.getType();

        // If not referenced, the timeScale used is UTC
        this.timeScale = TimeScalesFactory.getUTC();
        this.allSatellites = new ArrayList<Satellite>();
        this.model3DPath = model3DPath;
    }

    public List<List<Orbit>> getOrbits(final Orbit ReferenceOrbit) {
        final List<List<WalkerConstellationSlot<Orbit>>> constellationSlots =  this.buildRegularSlots(referenceOrbit);
        final List<List<Orbit>> listOfAllOrbits = new ArrayList<>(Collections.singletonList(new ArrayList<>(new ArrayList<>())));

        for (int i = 0; i < constellationSlots.size(); i++) {
            final List<WalkerConstellationSlot<Orbit>> currentPlane = constellationSlots.get(i);
            final List<Orbit> orbitsToAdd = new ArrayList<>();
            for (int j = 0; j < currentPlane.size(); j++) {
                final WalkerConstellationSlot<Orbit> currentSlot = currentPlane.get(j);
                orbitsToAdd.add(currentSlot.getOrbit());
            }
            listOfAllOrbits.add(orbitsToAdd);
        }

        this.allOrbits = listOfAllOrbits;
        return listOfAllOrbits;
    }

    @Override
    public String getId() {
        throw new RuntimeException("The constellation object don't have an ID, it is a group of satellite with different ids");
    }

    @Override
    public String getName() {
        throw new RuntimeException("The constellation object don't have a name, it is a group of satellite with different ids");
    }

    @Override
    public void generateCZML() {

        if (model3DPath.isEmpty()) {
            for (int i = 0; i < allOrbits.size(); i++) {
                final List<Orbit> currentPlane = allOrbits.get(i);
                for (int j = 0; j < currentPlane.size(); j++) {
                    final Orbit currentOrbit = currentPlane.get(j);
                    final OrbitInput orbitToInput = new OrbitInput(currentOrbit, orbitType, timeScale);
                    final Satellite satelliteToOutput = new Satellite(orbitToInput);
                    allSatellites.add(satelliteToOutput);
                    if (!displayOnlyLastPeriod) {
                        satelliteToOutput.generateCZML();
                    } else {
                        satelliteToOutput.displayOnlyOnePeriod();
                        satelliteToOutput.generateCZML();
                    }
                }
            }
            displayOnlyLastPeriod = false;
        }
        else {
            for (int i = 0; i < allOrbits.size(); i++) {
                final List<Orbit> currentPlane = allOrbits.get(i);
                for (int j = 0; j < currentPlane.size(); j++) {
                    final Orbit currentOrbit = currentPlane.get(j);
                    final OrbitInput orbitToInput = new OrbitInput(currentOrbit, orbitType, timeScale);
                    final Satellite satelliteToOutput = new Satellite(orbitToInput, model3DPath);
                    allSatellites.add(satelliteToOutput);
                    if (!displayOnlyLastPeriod) {
                        satelliteToOutput.generateCZML();
                    } else {
                        satelliteToOutput.displayOnlyOnePeriod();
                        satelliteToOutput.generateCZML();
                    }
                }
            }
            displayOnlyLastPeriod = false;
        }
    }


    public StringWriter getStringWriter() {
        return STRING_WRITER;
    }

    public void endFile() {
        OUTPUT.writeEndSequence();
    }

    @Override
    public void cleanObject() {
        this.allSatellites = null;
        this.orbitType = null;
        this.timeScale = null;
        this.referenceOrbit = null;
        this.allOrbits = null;
        this.phasingParameters = 0;
        this.numberOfOrbitalPlanes = 0;
        this.totalOfSatellite = 0;
    }

    public void displayOnlyOnePeriod() {
        displayOnlyLastPeriod = true;
    }

    public int getNumberOfOrbitalPlanes() {
        return numberOfOrbitalPlanes;
    }

    public TimeScale getTimeScale() {
        return timeScale;
    }

    public Orbit getReferenceOrbit() {
        return referenceOrbit;
    }

    public List<Satellite> getAllSatellites() {
        return allSatellites;
    }
}
