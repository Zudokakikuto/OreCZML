/* Copyright 2002-2025 CS GROUP
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
package org.orekit.czml.object.utils;

import cesiumlanguagewriter.Cartesian;
import cesiumlanguagewriter.JulianDate;
import cesiumlanguagewriter.TimeInterval;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.orekit.czml.errors.OresiumException;
import org.orekit.czml.errors.OresiumMessages;
import org.orekit.czml.object.primary.entities.Body;
import org.orekit.czml.object.primary.entities.InfluenceSphere;
import org.orekit.frames.Frame;
import org.orekit.frames.Transform;
import org.orekit.propagation.SpacecraftState;
import org.orekit.time.AbsoluteDate;

import java.util.ArrayList;
import java.util.List;

/**
 * Class that groups all the functions that allows to compute the influence
 * sphere and all computation related.
 *
 * @author LEBLOND Julien
 * @since 1.1
 */
public class InfluenceSphereUtils {

    private void InfluenceSphere() {
    }

    /**
     * Function to find the dates of crossing the influence spheres.
     *
     * @param bodies The bodies considered
     * @param centralFrame The frame of the central body
     * @param spacecraftStates The list of states of the spacecraft
     * @param spacecraftFrame The frame of the spacecraft
     * @param finalDate The final date of the propagation
     * @return A list of absolute date containing all the dates when the sphere
     *         of influence changes
     */
    public static List<AbsoluteDate>
        findCrossingSphereDates(final List<Body> bodies,
                                final Frame centralFrame,
                                final List<SpacecraftState> spacecraftStates,
                                final Frame spacecraftFrame,
                                final AbsoluteDate finalDate) {
        // Determine initial influence sphere
        InfluenceSphere lastKnownInfluenceSphere = null;
        final List<AbsoluteDate> datesToReturn = new ArrayList<>();

        for (final SpacecraftState state : spacecraftStates) {

            final AbsoluteDate date = state.getDate();

            // Transform spacecraft position into central frame
            final Transform satToCentralFrame =
                spacecraftFrame.getTransformTo(centralFrame, date);
            final Vector3D satPosCentral =
                satToCentralFrame.transformPosition(state.getPosition());

            InfluenceSphere currentSphere = null;
            double minDistance = Double.POSITIVE_INFINITY;

            // Identify which body's sphere the spacecraft is in (if any)
            for (final Body body : bodies) {

                body.displayInfluenceSphere();
                final InfluenceSphere sphere = body.getInfluenceSphere();

                final Frame bodyFrame =
                    body.getCelestialBody().getInertiallyOrientedFrame();
                final Transform bodyToCentralFrame =
                    bodyFrame.getTransformTo(centralFrame, date);

                final Vector3D bodyPosCentral =
                    bodyToCentralFrame.transformPosition(body.getCelestialBody()
                        .getPosition(date, bodyFrame));

                final double distance = satPosCentral.distance(bodyPosCentral);

                // If inside the sphere and closer than others
                if (distance <= sphere.getRadius() && distance < minDistance) {
                    currentSphere = sphere;
                    minDistance = distance;
                }
            }

            // Detect sphere change
            if (lastKnownInfluenceSphere == null && currentSphere != null) {
                lastKnownInfluenceSphere = currentSphere;
                datesToReturn.add(date);
            } else if (currentSphere != null &&
                       !currentSphere.getBody().getName()
                           .equals(lastKnownInfluenceSphere.getBody()
                               .getName())) {
                lastKnownInfluenceSphere = currentSphere;
                datesToReturn.add(date);
            }
        }
        // Add final date
        datesToReturn.add(finalDate);
        return datesToReturn;
    }

    /**
     * This function finds and segregate the interval of time (and the
     * corresponding positions) of a spacecraft to into part corresponding of
     * crossing an influence sphere.
     *
     * @param initialCartesians : The cartesian list of the position of the
     *        spacecraft
     * @param initialJulianDates : The julian dates list of the spacecraft
     * @param dateChanges : The list of absolute date when the spacecraft is
     *        crossing a new influence sphere
     * @return A map where each occurrence of timeList or positionList belongs
     *         to a single influence sphere crossing
     */
    public static List<List<Cartesian>>
        findPositionInsideInfluenceSpheres(final List<Cartesian> initialCartesians,
                                           final List<JulianDate> initialJulianDates,
                                           final List<TimeInterval> dateChanges) {
        final List<List<Cartesian>> listToReturn = new ArrayList<>();
        // initialCartesians and initialJulianDates are supposed to have the
        // same size
        if (initialCartesians.size() != initialJulianDates.size()) {
            throw new OresiumException(OresiumMessages.NOT_SAME_SIZE_TIME_POSITION);
        }

        int startingIndex = 0;
        int listIndex = 0;
        for (final TimeInterval interval : dateChanges) {
            for (int i = 0; i < initialJulianDates.size(); i++) {
                final JulianDate currentJulianDate = initialJulianDates.get(i);
                // Returns 0 if they are identical, <0 if currentJulianDate is
                // before the stop of the interval, and > 0 if it's after
                if (currentJulianDate.compareTo(interval.getStop()) == 0 ||
                    currentJulianDate.compareTo(interval.getStop()) > 0) {
                    final List<Cartesian> subCartesian =
                        initialCartesians.subList(startingIndex, i);
                    listToReturn.add(subCartesian);
                    startingIndex = i + 1;
                    listIndex = listIndex + 1;
                    break;
                }
            }
        }
        return listToReturn;
    }

    /**
     * This function finds and segregate the interval of time (and the
     * corresponding positions) of a spacecraft to into part corresponding of
     * crossing an influence sphere.
     *
     * @param initialJulianDates : The julian dates list of the spacecraft
     * @param dateChanges : The list of absolute date when the spacecraft is
     *        crossing a new influence sphere
     * @return A map where each occurrence of timeList or positionList belongs
     *         to a single influence sphere crossing
     */
    public static List<List<JulianDate>>
        findTimeInsideInfluenceSpheres(final List<JulianDate> initialJulianDates,
                                       final List<TimeInterval> dateChanges) {
        final List<List<JulianDate>> listToReturn = new ArrayList<>();

        int startingIndex = 0;
        int listIndex = 0;
        for (final TimeInterval interval : dateChanges) {
            for (int i = 0; i < initialJulianDates.size(); i++) {
                final JulianDate currentJulianDate = initialJulianDates.get(i);
                // Returns 0 if they are identical, <0 if currentJulianDate is
                // before the stop of the interval, and > 0 if it's after
                if (currentJulianDate.compareTo(interval.getStop()) == 0 ||
                    currentJulianDate.compareTo(interval.getStop()) > 0) {
                    final List<JulianDate> subJulianDate =
                        initialJulianDates.subList(startingIndex, i);
                    listToReturn.add(subJulianDate);
                    startingIndex = i + 1;
                    listIndex = listIndex + 1;
                    break;
                }
            }
        }
        return listToReturn;
    }
}
