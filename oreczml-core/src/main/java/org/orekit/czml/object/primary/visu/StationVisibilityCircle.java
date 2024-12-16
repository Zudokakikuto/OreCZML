package org.orekit.czml.object.primary.visu;

import cesiumlanguagewriter.BooleanCesiumWriter;
import cesiumlanguagewriter.Cartesian;
import cesiumlanguagewriter.CesiumOutputStream;
import cesiumlanguagewriter.CesiumStreamWriter;
import cesiumlanguagewriter.MaterialCesiumWriter;
import cesiumlanguagewriter.PacketCesiumWriter;
import cesiumlanguagewriter.PolygonCesiumWriter;
import cesiumlanguagewriter.SolidColorMaterialCesiumWriter;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.hipparchus.util.FastMath;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.czml.object.primary.AbstractPrimaryObject;
import org.orekit.czml.object.primary.Header;
import org.orekit.czml.object.primary.entities.Spacecraft;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.frames.TopocentricFrame;
import org.orekit.utils.Constants;
import org.orekit.utils.IERSConventions;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Station visibility circle.
 *
 * <p>
 * This clas aims at displaying the visibility circle of a station. It has no constructor and is called with the
 * .displayCircle() method of the CzmlGroundStation object.
 * </p>
 */

public class StationVisibilityCircle extends AbstractPrimaryObject {


    public static final String DEFAULT_ID = "STATION_CIRCLE/";

    public static final String DEFAULT_NAME = "Circle of visibility of the station : ";

    public static final double DEFAULT_ANGLE_OF_APERTURE = 90.0;

    private Header header;

    private TopocentricFrame topocentricFrame;

    private double angleOfAperture = DEFAULT_ANGLE_OF_APERTURE;

    private Spacecraft satellite;

    private VisibilityCone cone;

    private double topRadius;

    private List<GeodeticPoint> circleGeodetic = new ArrayList<>();

    private List<Cartesian> circleCartesian = new ArrayList<>();

    /**
     * Default constructor of the station visibility circle.
     *
     * @param topocentricFrame : The topocentric frame representing a ground station.
     * @param satellite        : The satellite observed.
     * @param angleOfAperture  : The angle of aperture of the visibility of the station.
     * @param header           : The header considered.
     */
    StationVisibilityCircle(final TopocentricFrame topocentricFrame, final Spacecraft satellite,
                            final double angleOfAperture, final Header header) {
        this.setId(DEFAULT_ID + topocentricFrame.getName() + "/" + satellite.getId());
        this.setName(DEFAULT_NAME + topocentricFrame.getName());
        this.header = header;
        this.setAvailability(header.getAvailability());
        this.topocentricFrame = topocentricFrame;
        this.angleOfAperture  = angleOfAperture;
        this.satellite        = satellite;
        this.cone             = new VisibilityCone(topocentricFrame, satellite, angleOfAperture, header);
        this.topRadius        = cone.getCylinder()
                                    .getTopRadius();
        this.circleGeodetic   = computePointPositions(topocentricFrame, satellite, angleOfAperture);
        this.circleCartesian  = cartesianGround(circleGeodetic);
    }

    // Builder

    public static StationVisibilityCircleBuilder builder(final TopocentricFrame topocentricFrameInput, final Spacecraft satelliteInput, final Header headerInput) {
        return new StationVisibilityCircleBuilder(topocentricFrameInput, satelliteInput, headerInput);
    }

    @Override
    public void writeCzmlBlock(final CesiumStreamWriter stream,
                               final CesiumOutputStream output) throws URISyntaxException, IOException {
        output.setPrettyFormatting(true);
        try (PacketCesiumWriter packet = stream.openPacket(output)) {
            packet.writeId(getId());
            packet.writeName(getName());
            packet.writeAvailability(getAvailability());
            try (PolygonCesiumWriter polygonCesiumWriter = packet.getPolygonWriter()) {
                polygonCesiumWriter.open(output);
                polygonCesiumWriter.writePositionsProperty(circleCartesian);
                polygonCesiumWriter.writeShowProperty(true);
                polygonCesiumWriter.writePerPositionHeightProperty(false);
                polygonCesiumWriter.writeFillProperty(false);
                try (BooleanCesiumWriter outlineWriter = polygonCesiumWriter.openOutlineProperty()) {
                    outlineWriter.writeBoolean(true);
                }
                try (MaterialCesiumWriter materialCesiumWriter = polygonCesiumWriter.openMaterialProperty()) {
                    try (SolidColorMaterialCesiumWriter solidColorMaterialCesiumWriter = materialCesiumWriter.openSolidColorProperty()) {
                        solidColorMaterialCesiumWriter.writeColorProperty(satellite.getColor());
                    }
                }
            }
        }
    }

    // Private functions

    final List<GeodeticPoint> computePointPositions(final TopocentricFrame topocentricFrameInput,
                                                    final Spacecraft satelliteInput, final double angleOfAperture) {
        final List<GeodeticPoint> toReturn         = new ArrayList<>();
        final double              fixedElevation   = 90.0 - angleOfAperture;
        final double              radiansElevation = FastMath.toRadians(fixedElevation);
        for (int i = 0; i < 360; i = i + 5) {
            final double radiansI = FastMath.toRadians(i);
            final GeodeticPoint currentGeodetic = topocentricFrameInput.computeLimitVisibilityPoint(
                    satelliteInput.getOrbits()
                                  .get(0)
                                  .getA(), radiansI, radiansElevation);
            toReturn.add(currentGeodetic);
        }
        return toReturn;
    }

    final List<Cartesian> cartesianGround(final List<GeodeticPoint> geodetics) {
        final List<Cartesian> toReturn = new ArrayList<>();
        final Frame           ITRF     = FramesFactory.getITRF(IERSConventions.IERS_2010, true);
        final OneAxisEllipsoid earth = new OneAxisEllipsoid(Constants.EGM96_EARTH_EQUATORIAL_RADIUS,
                Constants.IERS2010_EARTH_FLATTENING, ITRF);
        for (GeodeticPoint geodetic : geodetics) {
            final TopocentricFrame currentTopocentric = new TopocentricFrame(earth, geodetic, "currentTopocentric");
            final Vector3D         currentVector      = currentTopocentric.getCartesianPoint();
            toReturn.add(new Cartesian(currentVector.getX(), currentVector.getY(), currentVector.getZ()));
        }
        return toReturn;
    }
}


