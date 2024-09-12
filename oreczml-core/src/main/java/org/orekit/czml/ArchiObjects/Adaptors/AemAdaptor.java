package org.orekit.czml.ArchiObjects.Adaptors;

import org.hipparchus.geometry.euclidean.threed.Rotation;
import org.orekit.czml.CzmlObjects.CzmlSecondaryObjects.Orientation;
import org.orekit.files.ccsds.ndm.adm.aem.Aem;
import org.orekit.propagation.BoundedPropagator;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Aem adaptor
 * <p>
 * Adaptor for the {@link Aem} class, this helps build orientation objects from those files.
 * <p>
 *
 * @author Julien LEBLOND
 * @since 1.0.0
 */
public class AemAdaptor {

    /** The aem to be used to create an orientation. */
    private Aem aem;

    /** The orientation generated from the aem. */
    private Orientation orientation;


    /**
     * The constructor of the adaptor.
     *
     * @param aemInput : The aem to input.
     */
    public AemAdaptor(final Aem aemInput) {
        this.aem = aemInput;
    }

    public Orientation buildOrientation(final BoundedPropagator propagator) throws URISyntaxException, IOException {
        return new Orientation(aem.getSegments()
                                  .get(0)
                                  .getAttitudeProvider(), propagator, Rotation.IDENTITY, false);
    }
}
