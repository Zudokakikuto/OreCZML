package org.orekit.czml.object.secondary;

import cesiumlanguagewriter.Cartographic;
import cesiumlanguagewriter.CesiumInterpolationAlgorithm;
import cesiumlanguagewriter.JulianDate;
import org.hipparchus.util.FastMath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.orekit.czml.errors.OresiumException;
import org.orekit.czml.file.AbstractTest;
import org.junit.jupiter.api.Assertions;

import java.util.ArrayList;
import java.util.List;

/**
 * Earth-based time position test class.
 *
 * @author Leblond Julien
 * @since 1.1
 */
public class CartographicTimePositionTest
    extends
    AbstractTest {

    /** Orekit data. */
    final double data = initializeOrekitData();

    /** Cartographics. */
    final List<Cartographic> cartographics = new ArrayList<>();

    /** Julian dates. */
    final List<JulianDate> julianDates = new ArrayList<>();

    /** The clock. */
    final Clock clock = dummyHeader().getClock();

    /** Start date in julian day. */
    final JulianDate startDateJul = clock.getAvailability().getStart();

    /** End Date in julian day. */
    final JulianDate finalDateJul = clock.getAvailability().getStop();

    /** Cartographic toulouse. */
    final Cartographic cartographicToulouse =
        new Cartographic(FastMath.toRadians(43.6047),
                         FastMath.toRadians(1.4442), 10);

    /** Cartographic Quito. */
    final Cartographic cartographicQuito =
        new Cartographic(0.1807, 11.5382, 2850);

    @BeforeEach
    public void setup() {
        cartographics.clear();
        julianDates.clear();

        julianDates.add(startDateJul);
        julianDates.add(finalDateJul);

        cartographics.add(cartographicToulouse);
        cartographics.add(cartographicQuito);
    }

    @Test
    public void testConstructor() {
        final CartographicTimePosition cartographicTimePosition =
            new CartographicTimePosition(cartographics, julianDates);

        // Reference file
        final String templateFile =
            loadResources("templateFile/object/secondary/cartographictimeposition/CartographicTimePositionConstructorTemplate.txt");

        verifyFileOutput(templateFile, cartographicTimePosition.toString(),
                         1e-8);
    }

    @Test
    public void testCloneObject_Success() {
        final CartographicTimePosition original =
            new CartographicTimePosition(cartographics, julianDates);
        final CartographicTimePosition cloned = original.cloneObject();

        // Verify cloned object is not the same instance
        Assertions.assertNotSame(cloned, original);

        // Verify cloned object has same data
        Assertions.assertEquals(original.getPositions(), cloned.getPositions());
        Assertions.assertEquals(original.getDates(), cloned.getDates());
        Assertions.assertEquals(original.getInterpolationDegree(),
                                cloned.getInterpolationDegree());
        Assertions.assertEquals(original.getReferenceFrame(),
                                cloned.getReferenceFrame());
        Assertions.assertEquals(original.getCesiumInterpolationAlgorithm(),
                                cloned.getCesiumInterpolationAlgorithm());
    }

    @Test
    public void testCloneObject_EmptyPositions() {
        julianDates.clear();
        cartographics.clear();
        julianDates.add(startDateJul); // Add date but no position

        final CartographicTimePosition original =
            new CartographicTimePosition(cartographics, julianDates);

        Assertions.assertThrows(OresiumException.class, original::cloneObject);
    }

    @Test
    public void testCloneObject_EmptyDates() {
        julianDates.clear();
        cartographics.clear();
        cartographics.add(cartographicToulouse); // Add position but no date

        final CartographicTimePosition original =
            new CartographicTimePosition(cartographics, julianDates);

        Assertions.assertThrows(OresiumException.class, original::cloneObject);
    }

    @Test
    public void testCloneObject_BothEmpty() {
        julianDates.clear();
        cartographics.clear();

        final CartographicTimePosition original =
            new CartographicTimePosition(cartographics, julianDates);

        Assertions.assertThrows(OresiumException.class, original::cloneObject);
    }

    @Test
    public void testGetPositions() {
        final CartographicTimePosition position =
            new CartographicTimePosition(cartographics, julianDates);

        // Verify correct positions are returned
        Assertions.assertEquals(cartographics, position.getPositions());

        // Verify returned list is unmodifiable
        Assertions.assertThrows(UnsupportedOperationException.class,
                                () -> position.getPositions()
                                    .add(cartographicToulouse));
    }

    @Test
    public void testGetPositions_Empty() {
        cartographics.clear();
        final CartographicTimePosition position =
            new CartographicTimePosition(cartographics, julianDates);

        // Verify empty list is returned
        Assertions.assertTrue(position.getPositions().isEmpty());
    }

    @Test
    public void testGetDates() {
        final CartographicTimePosition position =
            new CartographicTimePosition(cartographics, julianDates);

        // Verify correct dates are returned
        Assertions.assertEquals(julianDates, position.getDates());

        // Verify returned list is unmodifiable
        Assertions.assertThrows(UnsupportedOperationException.class,
                                () -> position.getDates().add(startDateJul));
    }

    @Test
    public void testGetDates_Empty() {
        julianDates.clear();
        final CartographicTimePosition position =
            new CartographicTimePosition(cartographics, julianDates);

        // Verify empty list is returned
        Assertions.assertTrue(position.getDates().isEmpty());
    }

    @Test
    public void testGetInterpolationDegree() {
        final CartographicTimePosition position =
            new CartographicTimePosition(cartographics, julianDates);

        // Verify fixed value
        Assertions.assertEquals(5, position.getInterpolationDegree());
    }

    @Test
    public void testGetReferenceFrame() {
        final CartographicTimePosition position =
            new CartographicTimePosition(cartographics, julianDates);

        // Verify fixed value
        Assertions.assertEquals("EARTH_FRAME", position.getReferenceFrame());
    }

    @Test
    public void testGetCesiumInterpolationAlgorithm() {
        final CartographicTimePosition position =
            new CartographicTimePosition(cartographics, julianDates);

        // Verify fixed value
        Assertions.assertEquals(CesiumInterpolationAlgorithm.LAGRANGE,
                                position.getCesiumInterpolationAlgorithm());
    }
}
