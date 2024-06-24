package org.example.CZMLObjects.CZMLSecondaryObects.SatelliteObjects;

import cesiumlanguagewriter.*;
import org.example.CZMLObjects.CZMLPrimaryObjects.CZMLPrimaryObject;
import org.example.CZMLObjects.CZMLPrimaryObjects.Satellite;
import org.example.CZMLObjects.Polyline;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.hipparchus.util.FastMath;
import org.orekit.frames.FramesFactory;
import org.orekit.frames.Transform;
import org.orekit.time.AbsoluteDate;
import org.orekit.utils.IERSConventions;
import org.orekit.utils.PVCoordinates;

import java.awt.*;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class SatelliteReferenceSystem implements CZMLPrimaryObject {

    /** .*/
    public static final String DEFAULT_ID = "REFERENCE SYSTEM/";
    /** .*/
    public static final String DEFAULT_NAME = "Reference system of : ";
    /** .*/
    public static final String DEFAULT_H_PATH = "#path";
    /** .*/
    public static final String DEFAULT_H_ORIENTATION = "#orientation";

    /** .*/
    private String id;
    /** .*/
    private String name;

    // Intrinsic parameters
    /** .*/
    private Reference referencePosition;
    /** .*/
    private Reference referenceOrientation;
    /** .*/
    private TimeInterval availability;
    /** .*/
    private Satellite satellite;
    /** .*/
    private List<List<Polyline>> polylineList = new ArrayList<>();

    // Builders

    public SatelliteReferenceSystem(final Satellite satellite) {
        this.id = DEFAULT_ID + satellite.getId();
        this.name = DEFAULT_NAME + satellite.getName();
        this.referencePosition = new Reference(satellite.getId() + DEFAULT_H_PATH);
        this.referenceOrientation = new Reference(satellite.getId() + DEFAULT_H_ORIENTATION);
        this.satellite = satellite;
        this.availability = satellite.getAvailability();

        final List<AbsoluteDate> absoluteDates = satellite.getAbsoluteDateList();
        final List<JulianDate> julianDates = absoluteDatelistToJulianDateList(absoluteDates);
        final List<TimeInterval> timeIntervals = julienDateListToTimeIntervals(julianDates);

        final List<Cartesian> inertialCartesian = satellite.getCartesianArraylist();
        for (int i = 0; i < inertialCartesian.size(); i++) {

            final List<Polyline> system = new ArrayList<>();
            final List<Cartesian> toXList = new ArrayList<>();
            final List<Cartesian> toYList = new ArrayList<>();
            final List<Cartesian> toZList = new ArrayList<>();

            final Cartesian currentCartesian = inertialCartesian.get(i);

            final Vector3D vectorInertial = new Vector3D(currentCartesian.getX(), currentCartesian.getY(), currentCartesian.getZ());
            final PVCoordinates toTransformIntoITRF = new PVCoordinates(vectorInertial);
            final Transform transformFromInertialToIRTF = FramesFactory.getEME2000().getTransformTo(FramesFactory.getITRF(IERSConventions.IERS_2010, true), absoluteDates.get(i));
            final PVCoordinates transformedPVCoordinates = transformFromInertialToIRTF.transformPVCoordinates(toTransformIntoITRF);

            final double plus_X_component = transformedPVCoordinates.getPosition().getX();
            final double plus_Y_component = transformedPVCoordinates.getPosition().getY();
            final double plus_Z_component = transformedPVCoordinates.getPosition().getZ();

            // Rotating parameters
            final Cartesian transformedCartesian = new Cartesian(plus_X_component, plus_Y_component, plus_Z_component);

            // Non-rotating parameters
            final double satellitePeriod = satellite.getPeriod();
            final double rotationSpeed = (2 * FastMath.PI) / satellitePeriod; // rad.s-1


            final Cartesian plusXCartesian = transformedCartesian.add(new Cartesian(100000, 0, 0));
            final Cartesian plusYCartesian = transformedCartesian.add(new Cartesian(0, 100000, 0));
            final Cartesian plusZCartesian = transformedCartesian.add(new Cartesian(0, 0, 100000));

            toXList.add(transformedCartesian);
            toYList.add(transformedCartesian);
            toZList.add(transformedCartesian);

            toXList.add(plusXCartesian);
            toYList.add(plusYCartesian);
            toZList.add(plusZCartesian);

            final TimeInterval currentTimeInterval = timeIntervals.get(i);

            final Color red = Color.red;
            final Color green = Color.green;
            final Color blue = Color.blue;

            final Polyline XPolyline = new Polyline(toXList, currentTimeInterval, red);
            final Polyline YPolyline = new Polyline(toYList, currentTimeInterval, green);
            final Polyline ZPolyline = new Polyline(toZList, currentTimeInterval, blue);

            system.add(XPolyline);
            system.add(YPolyline);
            system.add(ZPolyline);

            polylineList.add(system);
        }
    }

    // Overrides
    @Override
    public void generateCZML() {
        for (int i = 0; i < polylineList.size(); i++) {
            final List<Polyline> currentSystem = polylineList.get(i);
            for (int j = 0; j < currentSystem.size(); j++) {

                final Polyline currentPolyline = currentSystem.get(j);

                OUTPUT.setPrettyFormatting(true);
                try (PacketCesiumWriter packet = STREAM.openPacket(OUTPUT)) {
                    packet.writeId(id + " system : " + i + " line : " + j);
                    packet.writeName(name);
                    packet.writeOrientationPropertyVelocityReference(referenceOrientation);
                    packet.writePositionPropertyReference(referencePosition);
                    packet.writeAvailability(currentPolyline.getAvailability());

                    currentPolyline.writePolylineVectorInertial(packet, OUTPUT);
                }
            }
        }
    }

    @Override
    public StringWriter getStringWriter() {
        return STRING_WRITER;
    }

    @Override
    public void endFile() {
        OUTPUT.writeEndSequence();
    }

    @Override
    public void cleanObject() {

    }


    // GETS
    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public Satellite getSatellite() {
        return satellite;
    }

    public Reference getReferenceOrientation() {
        return referenceOrientation;
    }

    public Reference getReferencePosition() {
        return referencePosition;
    }

    public TimeInterval getAvailability() {
        return availability;
    }
}
