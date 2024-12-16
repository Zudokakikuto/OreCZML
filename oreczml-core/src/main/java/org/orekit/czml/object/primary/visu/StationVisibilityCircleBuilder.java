package org.orekit.czml.object.primary.visu;

import org.orekit.czml.object.primary.Header;
import org.orekit.czml.object.primary.entities.Spacecraft;
import org.orekit.frames.TopocentricFrame;

public class StationVisibilityCircleBuilder {

    /** The default angle of aperture of the station. */
    public static final double DEFAULT_ANGLE_OF_APERTURE = 90.0;

    /** The topocentric frame representing the station. */
    private TopocentricFrame topocentricFrame;

    /** The satellite observed. */
    private Spacecraft satellite;

    /** The angle of aperture of the station. */
    private double angleOfAperture = DEFAULT_ANGLE_OF_APERTURE;

    /** The header considered. */
    private Header header;

    /**
     * The default constructor for the station visibility circle builder.
     *
     * @param topocentricFrameInput : The topocentric frame representing the ground station.
     * @param satelliteInput        : The satellite observed.
     * @param headerInput           : The header considered.
     */
    public StationVisibilityCircleBuilder(final TopocentricFrame topocentricFrameInput, final Spacecraft satelliteInput,
                                          final Header headerInput) {
        this.topocentricFrame = topocentricFrameInput;
        this.satellite        = satelliteInput;
        this.header           = headerInput;
    }

    /**
     * This functions sets an angle of aperture.
     *
     * @param angleOfApertureInput : The angle of aperture to set.
     * @return : The station visibility circle builder with an angle of aperture set.
     */
    public StationVisibilityCircleBuilder withAngleOfAperture(final double angleOfApertureInput) {
        this.angleOfAperture = angleOfApertureInput;
        return this;
    }

    /** This function builds the visibility circle. */
    public StationVisibilityCircle build() {
        return new StationVisibilityCircle(topocentricFrame, satellite, angleOfAperture, header);
    }

}
