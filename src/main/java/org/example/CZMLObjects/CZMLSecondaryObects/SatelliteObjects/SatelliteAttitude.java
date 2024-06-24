package org.example.CZMLObjects.CZMLSecondaryObects.SatelliteObjects;

import cesiumlanguagewriter.*;
import org.example.CZMLObjects.CZMLPrimaryObjects.CZMLPrimaryObject;
import org.example.CZMLObjects.CZMLPrimaryObjects.Satellite;
import org.example.CZMLObjects.Polyline;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.orekit.attitudes.Attitude;
import org.orekit.frames.FramesFactory;
import org.orekit.frames.Transform;
import org.orekit.propagation.BoundedPropagator;
import org.orekit.utils.IERSConventions;
import org.orekit.utils.PVCoordinates;


import java.awt.*;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class SatelliteAttitude implements CZMLPrimaryObject {

    /** .*/
    public static final String DEFAULT_H_POSITION = "#position";

    /** .*/
    private String id;
    /** .*/
    private String name;

    // Intrinsic parameters
    /** .*/
    private Reference reference;
    /** .*/
    private TimeInterval availability;
    /** .*/
    private List<List<Polyline>> polylineList = new ArrayList<>();
    /** .*/
    private Satellite satellite;

    public SatelliteAttitude(final Satellite satellite) {
        this.id = "REFERENCE SYSTEM/" + satellite.getId();
        this.name = "Reference system of : " + satellite.getName();
        this.reference = new Reference(satellite.getId() + DEFAULT_H_POSITION);
        this.availability = satellite.getAvailability();
        this.satellite = satellite;
        final List<Attitude> attitudes = satellite.getAttitudes();
        final BoundedPropagator boundedPropagator = satellite.getBoundedPropagator();
        System.out.println("Finir boundedPropagator satellite attitude");

        final List<Cartesian> cartesianPositionOfSatellite = satellite.getCartesianArraylist();
        for (int i = 0; i < attitudes.size(); i++) {
            // SETUP
            final Attitude currentAttitude = attitudes.get(i);
            final JulianDate startDate = absoluteDateToJulianDate(currentAttitude.getDate());
            final double step =  satellite.getHeader().getStepSimulation();
            final TimeInterval currentTimeInterval = new TimeInterval(startDate, startDate.addSeconds(step));
            final List<Polyline> polylinesTemp = new ArrayList<>();
            // Colors
            final Color colorRed = new Color(255, 10, 10);
            final Color colorGreen = new Color(10, 255, 10);
            final Color colorBlue = new Color(10, 10, 255);
            // List of cartesians
            final List<Cartesian> vectorICartesian = new ArrayList<>();
            final List<Cartesian> vectorJCartesian = new ArrayList<>();
            final List<Cartesian> vectorKCartesian = new ArrayList<>();
            // Cartesian of satellite and relative vector 3D
            final Cartesian currentSatelliteCartesian = cartesianPositionOfSatellite.get(i);

            final double XToTransform = currentSatelliteCartesian.getX();
            final double YToTransform = currentSatelliteCartesian.getY();
            final double ZToTransform = currentSatelliteCartesian.getZ();

            // Transform from Inertial to ITRF frame
            final Vector3D vectorInertial = new Vector3D(XToTransform, YToTransform, ZToTransform);
            final PVCoordinates toTransformIntoITRF = new PVCoordinates(vectorInertial);
            final Transform transformFromInertialToIRTF = FramesFactory.getEME2000().getTransformTo(FramesFactory.getITRF(IERSConventions.IERS_2010, true), attitudes.get(i).getDate());
            final PVCoordinates transformedPVCoordinates = transformFromInertialToIRTF.transformPVCoordinates(toTransformIntoITRF);

            final double plus_X_component = transformedPVCoordinates.getPosition().getX();
            final double plus_Y_component = transformedPVCoordinates.getPosition().getY();
            final double plus_Z_component = transformedPVCoordinates.getPosition().getZ();

            final Cartesian transformedCartesian = new Cartesian(plus_X_component, plus_Y_component, plus_Z_component);

            final Vector3D plus_I = currentAttitude.getRotation().applyTo(Vector3D.PLUS_I);
            final Vector3D plus_J = currentAttitude.getRotation().applyTo(Vector3D.PLUS_J);
            final Vector3D plus_K = currentAttitude.getRotation().applyTo(Vector3D.PLUS_K);
            // Creation of cartesians from vector 3D not relative
            final Cartesian transformedICartesian = new Cartesian(plus_X_component + plus_I.getX() * 20000, plus_Y_component + plus_I.getY() * 20000, plus_Z_component + plus_I.getZ() * 20000);
            final Cartesian transformedJCartesian = new Cartesian(plus_X_component + plus_J.getX() * 20000, plus_Y_component + plus_J.getY() * 20000, plus_Z_component + plus_J.getZ() * 20000);
            final Cartesian transformedKCartesian = new Cartesian(plus_X_component + plus_K.getX() * 20000, plus_Y_component + plus_K.getY() * 20000, plus_Z_component + plus_K.getZ() * 20000);
            // Build of list of cartesians
            vectorICartesian.add(transformedCartesian);
            vectorICartesian.add(transformedICartesian);

            vectorJCartesian.add(transformedCartesian);
            vectorJCartesian.add(transformedJCartesian);

            vectorKCartesian.add(transformedCartesian);
            vectorKCartesian.add(transformedKCartesian);
            // Build of Polylines
            final Polyline plusIPolyline = new Polyline(vectorICartesian, currentTimeInterval, colorRed);
            final Polyline plusJPolyline = new Polyline(vectorJCartesian, currentTimeInterval, colorGreen);
            final Polyline plusKPolyline = new Polyline(vectorKCartesian, currentTimeInterval, colorBlue);
            // Adding polylines to polylineList
            polylinesTemp.add(plusIPolyline);
            polylinesTemp.add(plusJPolyline);
            polylinesTemp.add(plusKPolyline);
            polylineList.add(polylinesTemp);
        }
    }


    @Override
    public String getId() {
        return "";
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public void generateCZML() {
        for (int i = 0; i < polylineList.size(); i++) {
            final List<Polyline> currentAttitudes = polylineList.get(i);
            for (int j = 0; j < currentAttitudes.size(); j++) {
                OUTPUT.setPrettyFormatting(true);
                try (PacketCesiumWriter packet = STREAM.openPacket(OUTPUT)) {
                    packet.writeId(id + polylineList.get(i).get(j).toString());
                    packet.writeName(name);
                    packet.writeAvailability(availability);

                    try (PositionCesiumWriter positionWriter = packet.getPositionWriter()) {
                        positionWriter.open(OUTPUT);
                        positionWriter.writeReference(satellite.getId() + DEFAULT_H_POSITION);
                    }

                    final Polyline currentPolyline = currentAttitudes.get(j);
                    currentPolyline.writePolylineVectorInertial(packet, OUTPUT);
                }
            }
        }
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
        reference = null;
        availability = null;
        polylineList = new ArrayList<>();
    }

}