package org.orekit.czml.ArchiObjects;

import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.orekit.attitudes.AttitudesSequence;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.Header;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.ManeuverSequence;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.Satellite;
import org.orekit.forces.maneuvers.Maneuver;
import org.orekit.frames.LOF;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public class ManeuverSequenceBuilder {

    /** . */
    public static final double DEFAULT_TIME_SHIFT = Header.getMasterClock()
                                                          .getMultiplier();
    /** . */
    public static final String DEFAULT_PATH_MODEL = Header.DEFAULT_RESOURCES + "/Default3DModels/maneuver_model.glb";

    /** . */
    private Satellite         satellite;
    /** . */
    private AttitudesSequence sequence;

    /** . */
    private LOF lof;

    /** . */
    private List<Maneuver> maneuvers;

    /** . */
    private Vector3D direction;

    // Optional parameters
    /** . */
    private double timeShift = DEFAULT_TIME_SHIFT;

    /** . */
    private String pathModel = DEFAULT_PATH_MODEL;

    /** . */
    private boolean showTrust = false;

    public ManeuverSequenceBuilder(final AttitudesSequence sequenceInput, final List<Maneuver> maneuversInput,
                                   final Satellite satelliteInput, final Vector3D directionInput, final LOF lofInput) {
        this.sequence  = sequenceInput;
        this.satellite = satelliteInput;
        this.maneuvers = maneuversInput;
        this.direction = directionInput;
        this.lof       = lofInput;
    }

    public ManeuverSequenceBuilder withTimeShift(final double timeShiftInput) {
        this.timeShift = timeShiftInput;
        return this;
    }

    public ManeuverSequenceBuilder withPathModel(final String pathModelInput) {
        this.pathModel = pathModelInput;
        return this;
    }

    public ManeuverSequenceBuilder withShowTrust(final boolean showTrustInput) {
        this.showTrust = showTrustInput;
        return this;
    }

    public ManeuverSequence build() throws URISyntaxException, IOException {
        return new ManeuverSequence(sequence, maneuvers, satellite, direction, lof, showTrust, timeShift, pathModel);
    }

}
