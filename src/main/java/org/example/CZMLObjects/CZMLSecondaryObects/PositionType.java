package org.example.CZMLObjects.CZMLSecondaryObects;

import org.orekit.orbits.OrbitType;
import org.orekit.orbits.PositionAngleType;
import org.orekit.utils.IERSConventions;

public enum PositionType {

    CARTOGRAPHIC_RADIANS,
    CARTOGRAPHIC_DEGREES,
    CARTESIAN_POSITION,
    CARTESIAN_VELOCITY;

    private PositionType(){
    }
}
