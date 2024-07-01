/* Copyright 2002-2024 CS GROUP
 * Licensed to CS GROUP (CS) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * CS licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.orekit.czml.CzmlObjects.CzmlSecondaryObjects.SatelliteObjects;

import cesiumlanguagewriter.Cartesian;
import cesiumlanguagewriter.JulianDate;
import cesiumlanguagewriter.PacketCesiumWriter;
import cesiumlanguagewriter.PositionCesiumWriter;
import cesiumlanguagewriter.Reference;
import cesiumlanguagewriter.TimeInterval;
import org.orekit.czml.CzmlEnum.OffsetType;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.AbstractPrimaryObject;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.CzmlPrimaryObject;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.Header;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.Satellite;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.SatelliteOffsetPath;
import org.orekit.czml.CzmlObjects.Polyline;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.orekit.attitudes.Attitude;
import org.orekit.frames.FramesFactory;
import org.orekit.frames.Transform;
import org.orekit.utils.IERSConventions;
import org.orekit.utils.PVCoordinates;


import java.awt.Color;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class SatelliteAttitude extends AbstractPrimaryObject implements CzmlPrimaryObject {

    /** .*/
    public static final String DEFAULT_H_POSITION = "#position";

    // Intrinsic parameters
    /** .*/
    private Reference reference;
    /** .*/
    private List<List<Polyline>> polylineList = new ArrayList<>();
    /** .*/
    private Satellite satellite;

    public SatelliteAttitude(final Satellite satellite) throws URISyntaxException, IOException {
        this.setId("REFERENCE SYSTEM/" + satellite.getId());
        this.setName("Reference system of : " + satellite.getName());
        this.setAvailability(satellite.getAvailability());
        this.reference = new Reference(satellite.getId() + DEFAULT_H_POSITION);
        this.satellite = satellite;
        final List<Attitude> attitudes = satellite.getAttitudes();

        final List<Cartesian> cartesianPositionOfSatellite = satellite.getCartesianArraylist();
        for (int i = 0; i < attitudes.size(); i++) {
            // SETUP
            final Attitude currentAttitude = attitudes.get(i);
            final JulianDate startDate = absoluteDateToJulianDate(currentAttitude.getDate());
            final double step = Header.MASTER_CLOCK.getMultiplier();
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

        // Offset objects

        final SatelliteOffsetPath satelliteOffsetPath = new SatelliteOffsetPath(satellite, 10000, OffsetType.VELOCITY_TANGENT);
        satelliteOffsetPath.writeCzmlBlock();
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
    public void writeCzmlBlock() {
        for (int i = 0; i < polylineList.size(); i++) {
            final List<Polyline> currentAttitudes = polylineList.get(i);
            for (int j = 0; j < currentAttitudes.size(); j++) {
                OUTPUT.setPrettyFormatting(true);
                try (PacketCesiumWriter packet = STREAM.openPacket(OUTPUT)) {
                    packet.writeId(getId() + polylineList.get(i).get(j).toString());
                    packet.writeName(getName());
                    packet.writeAvailability(getAvailability());

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
    public void cleanObject() {
        setId("");
        setName("");
        setAvailability(null);
        reference = null;
        polylineList = new ArrayList<>();
    }

}
