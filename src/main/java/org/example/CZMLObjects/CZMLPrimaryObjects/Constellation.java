package org.example.CZMLObjects.CZMLPrimaryObjects;

import org.orekit.orbits.Orbit;

import java.io.StringWriter;

public class Constellation implements CZMLPrimaryObject{

    private int totalOfSatellite;
    private int numberOfOrbitalPlanes;
    private int phasingParameters;

    // intrinsic parameters
    private Orbit referenceOrbit;



    @Override
    public void generateCZML() {

    }

    @Override
    public StringWriter getStringWriter() {
        return stringWriter;
    }

    @Override
    public void endFile() {
        output.writeEndSequence();
    }
}
