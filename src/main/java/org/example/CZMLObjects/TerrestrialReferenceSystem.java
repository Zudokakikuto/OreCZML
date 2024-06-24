package org.example.CZMLObjects;

import cesiumlanguagewriter.Cartesian;
import cesiumlanguagewriter.PacketCesiumWriter;
import cesiumlanguagewriter.PositionCesiumWriter;
import cesiumlanguagewriter.TimeInterval;
import org.example.CZMLObjects.CZMLPrimaryObjects.CZMLPrimaryObject;
import org.example.CZMLObjects.CZMLPrimaryObjects.Header;
import org.orekit.bodies.BodyShape;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.frames.FramesFactory;
import org.orekit.utils.Constants;
import org.orekit.utils.IERSConventions;

import java.awt.*;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class TerrestrialReferenceSystem implements CZMLPrimaryObject {
    /** .*/
    public static final String DEFAULT_ID = "TerrestrialReferenceSystem";
    /** .*/
    public static final String DEFAULT_NAME = "Reference system of the central body";

    /** .*/
    private String id;
    /** .*/
    private String name;
    /** .*/
    private List<Polyline> polylines = new ArrayList<>();
    /** .*/
    private BodyShape body;
    /** .*/
    private TimeInterval availability;

    public TerrestrialReferenceSystem(final Header header, final OneAxisEllipsoid body, final String id, final String name) {
        this.id = id;
        this.body = body;
        this.name = name;
        this.availability = header.getClock().getAvailability();
        final Color colorRed = new Color(255, 10, 10);
        final Color colorGreen = new Color(10, 255, 10);
        final Color colorBlue = new Color(10, 10, 255);

        final Cartesian centralCartesian = new Cartesian(0.1, 0.1, 0.1);
        final double depth = body.getEquatorialRadius() * 3;

        final Cartesian plusXCartesian = new Cartesian(depth, 0, 0);
        final Cartesian plusYCartesian = new Cartesian(0, depth, 0);
        final Cartesian plusZCartesian = new Cartesian(0, 0, depth);

        final List<Cartesian> vectorToX = new ArrayList<>();
        final List<Cartesian> vectorToY = new ArrayList<>();
        final List<Cartesian> vectorToZ = new ArrayList<>();
        vectorToX.add(centralCartesian);
        vectorToX.add(plusXCartesian);

        vectorToY.add(centralCartesian);
        vectorToY.add(plusYCartesian);

        vectorToZ.add(centralCartesian);
        vectorToZ.add(plusZCartesian);

        final Polyline XPolyline = new Polyline(vectorToX, availability, colorRed, 1, 1e9);
        final Polyline YPolyline = new Polyline(vectorToY, availability, colorGreen, 1, 1e9);
        final Polyline ZPolyline = new Polyline(vectorToZ, availability, colorBlue, 1, 1e9);
        this.polylines.add(XPolyline);
        this.polylines.add(YPolyline);
        this.polylines.add(ZPolyline);
    }

    public TerrestrialReferenceSystem(final Header header) {
        this(   header,
                new OneAxisEllipsoid(Constants.WGS84_EARTH_EQUATORIAL_RADIUS, Constants.WGS84_EARTH_FLATTENING, FramesFactory.getITRF(IERSConventions.IERS_2010, true)),
                DEFAULT_ID,
                DEFAULT_NAME);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void generateCZML() {
        OUTPUT.setPrettyFormatting(true);
        for (int i = 0; i < 3; i++) {
            try (PacketCesiumWriter packet = STREAM.openPacket(OUTPUT)) {
                packet.writeId(id + " " + i);
                packet.writeName(name);
                packet.writeAvailability(availability);

                try (PositionCesiumWriter positionWriter = packet.getPositionWriter()) {
                    positionWriter.open(OUTPUT);
                    positionWriter.writeCartesian(new Cartesian(0, 0, 0));
                }

                polylines.get(i).writePolylineVectorFixed(packet, OUTPUT);
            }
        }
        cleanObject();
    }

    @Override
    public StringWriter getStringWriter() {
        return null;
    }

    @Override
    public void endFile() {
        OUTPUT.writeEndSequence();
    }

    @Override
    public void cleanObject() {
        id = "";
        name = "";
        polylines = new ArrayList<>();
        body = null;
    }
}
