package org.example.CZMLObjects.CZMLSecondaryObects.SatelliteObjects;

import cesiumlanguagewriter.*;
import org.example.CZMLObjects.CZMLSecondaryObects.CZMLSecondaryObject;

import java.util.ArrayList;
import java.util.List;

public class SatellitePosition implements CZMLSecondaryObject {

    private List<JulianDate> dates;
    private List<Cartesian> positions;
    private CesiumInterpolationAlgorithm cesiumInterpolationAlgorithm;
    private int interpolationDegree;
    private String ReferenceFrame;

    public SatellitePosition(List<Cartesian> cartesians, List<Double> timeList){
        int cpt = 0;
        this.dates = new ArrayList<JulianDate>();
        this.positions = new ArrayList<Cartesian>();

        for (Cartesian position : cartesians) {
            assert false;
            this.dates.add(new JulianDate(timeList.get(cpt)));
            this.positions.add(position);
            cpt++;
        }

        this.cesiumInterpolationAlgorithm = CesiumInterpolationAlgorithm.LAGRANGE;
        this.interpolationDegree = 5;
        this.ReferenceFrame = "INERTIAL";
    }

    @Override
    public void write(PacketCesiumWriter packetWriter, CesiumOutputStream output) {
    }

    public List<Cartesian> getPositions() {
        return positions;
    }

    public List<JulianDate> getDates() {
        return dates;
    }

    public int getInterpolationDegree() {
        return interpolationDegree;
    }

    public String getReferenceFrame() {
        return ReferenceFrame;
    }

    public CesiumInterpolationAlgorithm getCesiumInterpolationAlgorithm() {
        return cesiumInterpolationAlgorithm;
    }
}

