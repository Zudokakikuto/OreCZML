package org.orekit.czml.object.utils;

import cesiumlanguagewriter.Cartesian;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.hipparchus.linear.MatrixUtils;
import org.hipparchus.linear.RealMatrix;
import org.hipparchus.util.FastMath;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.czml.archi.factory.BodyFactory;
import org.orekit.czml.file.AbstractTest;
import org.orekit.czml.object.primary.GroundTrack;
import org.orekit.czml.object.primary.Header;
import org.orekit.czml.object.primary.ManeuverSequence;
import org.orekit.czml.object.primary.covariance.Collision;
import org.orekit.czml.object.primary.covariance.Covariance;
import org.orekit.czml.object.primary.entities.Body;
import org.orekit.czml.object.primary.entities.Constellation;
import org.orekit.czml.object.primary.entities.CzmlGroundStation;
import org.orekit.czml.object.primary.entities.InfluenceSphere;
import org.orekit.czml.object.primary.entities.Spacecraft;
import org.orekit.czml.object.primary.pointing.AttitudePointing;
import org.orekit.czml.object.primary.pointing.CoveredSurfaceOnBody;
import org.orekit.czml.object.primary.systems.CentralBodyReferenceSystem;
import org.orekit.czml.object.primary.systems.LatLongLines;
import org.orekit.czml.object.primary.systems.SpacecraftReferenceSystem;
import org.orekit.czml.object.primary.visu.FieldOfObservation;
import org.orekit.czml.object.primary.visu.InterSatVisu;
import org.orekit.czml.object.primary.visu.LineOfVisibility;
import org.orekit.czml.object.primary.visu.MultipleLineOfVisibility;
import org.orekit.czml.object.primary.visu.StationVisibilityCircle;
import org.orekit.czml.object.primary.visu.VisibilityCone;
import org.orekit.czml.object.secondary.Billboard;
import org.orekit.czml.object.secondary.Clock;
import org.orekit.czml.object.secondary.Cylinder;
import org.orekit.czml.object.secondary.CzmlEllipsoid;
import org.orekit.czml.object.secondary.Label;
import org.orekit.czml.object.secondary.Orientation;
import org.orekit.czml.object.secondary.Path;
import org.orekit.czml.object.secondary.Polygon;
import org.orekit.czml.object.secondary.TimePosition;
import org.orekit.frames.FramesFactory;
import org.orekit.frames.LOFType;
import org.orekit.frames.TopocentricFrame;
import org.orekit.frames.Transform;
import org.orekit.geometry.fov.DoubleDihedraFieldOfView;
import org.orekit.geometry.fov.FieldOfView;
import org.orekit.orbits.KeplerianOrbit;
import org.orekit.orbits.Orbit;
import org.orekit.orbits.OrbitType;
import org.orekit.orbits.PositionAngleType;
import org.orekit.propagation.BoundedPropagator;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.StateCovariance;
import org.orekit.propagation.numerical.NumericalPropagator;
import org.orekit.time.AbsoluteDate;
import org.orekit.utils.Constants;

import java.awt.Color;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/** Class to test all the clone object functions. */
public class CloningTest
    extends
    AbstractTest {

    /** The test of all the cloning function. */
    @Test
    void cloningTest()
        throws URISyntaxException,
            IOException {

        // Initialisation of all needed objects to begin cloning tests
        loadOrekitData();
        final Header dummyHeader = dummyHeader();
        final Clock clock = dummyHeader.getClock();
        final AbsoluteDate startDate =
            DateUtils.toAbsoluteDate(dummyHeader.getAvailability().getStart());
        final AbsoluteDate finalDate =
            DateUtils.toAbsoluteDate(dummyHeader.getAvailability().getStop());
        final Orbit firstOrbit = dummyOrbit(startDate, 7900000, 0, 10, 0, 0, 0);
        final Orbit secondOrbit =
            dummyOrbit(startDate, 7900000, 0, -10, 0, 0.1, 0);
        final PropagatorWithEphemeris propagatorWithEphemeris1 =
            dummyNumericalPropagatorWithEphemeris(startDate, finalDate,
                                                  firstOrbit);
        final PropagatorWithEphemeris propagatorWithEphemeris2 =
            dummyNumericalPropagatorWithEphemeris(startDate, finalDate,
                                                  secondOrbit);
        final NumericalPropagator mockNumericalPropagator1 =
            propagatorWithEphemeris1.getPropagator();
        final NumericalPropagator mockNumericalPropagator2 =
            propagatorWithEphemeris2.getPropagator();
        final BoundedPropagator mockBoundedPropagator1 =
            propagatorWithEphemeris1.getEphemeris();
        final BoundedPropagator mockBoundedPropagator2 =
            propagatorWithEphemeris2.getEphemeris();
        final Spacecraft mockSpacecraft1 =
            Spacecraft.builder(mockBoundedPropagator1, clock).build();
        final Spacecraft mockSpacecraft2 =
            Spacecraft.builder(mockBoundedPropagator2, clock).build();
        final List<BoundedPropagator> boundedPropagators = new ArrayList<>();
        boundedPropagators.add(mockBoundedPropagator1);
        boundedPropagators.add(mockBoundedPropagator2);
        final Body mars = BodyFactory.getMars(clock);

        // Build of the covariance
        final RealMatrix realMatrix =
            MatrixUtils.createRealDiagonalMatrix(new double[] {
                20000 *
                                                                20000,
                1e-6, 1e-6, 1e-6, 1e-6, (36 * 4.848e-6) * (36 * 4.848e-6)
            });
        final StateCovariance stateCovariance =
            new StateCovariance(realMatrix, startDate,
                                FramesFactory.getEME2000(),
                                OrbitType.EQUINOCTIAL, PositionAngleType.MEAN);
        final List<StateCovariance> covariances1 =
            covariancePropagation(mockSpacecraft1, mockNumericalPropagator1,
                                  stateCovariance, clock);
        final List<StateCovariance> covariances2 =
            covariancePropagation(mockSpacecraft2, mockNumericalPropagator2,
                                  stateCovariance, clock);

        // Creation of a topocentric frame around Toulouse.
        final GeodeticPoint toulouseFrame =
            new GeodeticPoint(FastMath.toRadians(43.6047),
                              FastMath.toRadians(1.4442), 10);
        final TopocentricFrame topocentricToulouse =
            new TopocentricFrame(AbstractTest.getEarth(), toulouseFrame,
                                 "Toulouse");

        // Creation of a topocentric frame around Quito
        final GeodeticPoint quitoFrame =
            new GeodeticPoint(FastMath.toRadians(0.1807),
                              FastMath.toRadians(11.5382), 2850);
        final TopocentricFrame topocentricQuito =
            new TopocentricFrame(getEarth(), quitoFrame, "Quito");

        // Creation of a list of topocentric frame
        final List<TopocentricFrame> topocentricFrames = new ArrayList<>();
        topocentricFrames.add(topocentricToulouse);
        topocentricFrames.add(topocentricQuito);

        // Field of observation build
        final Orbit orbitCovering =
            new KeplerianOrbit(7878000, 0, FastMath.toRadians(20), 0,
                               FastMath.toRadians(0), FastMath.toRadians(0),
                               PositionAngleType.MEAN,
                               FramesFactory.getEME2000(), startDate,
                               Constants.WGS84_EARTH_MU);
        final SpacecraftState initialStateCovering =
            new SpacecraftState(orbitCovering);
        final BoundedPropagator boundedPropagatorCovering =
            dummyPropagator(startDate, finalDate, orbitCovering);
        final SinusoidalLof sinusoidalLof =
            new SinusoidalLof(FramesFactory.getEME2000(), LOFType.VNC,
                              Vector3D.PLUS_I, 3600, FastMath.toRadians(45.0),
                              initialStateCovering.getDate());
        boundedPropagatorCovering.setAttitudeProvider(sinusoidalLof);
        boundedPropagatorCovering.propagate(startDate, finalDate);
        final Spacecraft coveringSpacecraft =
            Spacecraft.builder(boundedPropagatorCovering, clock)
                .withColor(Color.RED).withOnlyOnePeriod().withDisplayAttitude()
                .withReferenceSystem().build();
        // Creation of the field of observation of the satellite, it describes
        // the area the satellite see
        final Transform initialInertToBody =
            initialStateCovering.getFrame()
                .getTransformTo(getEarth().getBodyFrame(),
                                initialStateCovering.getDate());
        final Transform initialFovBody =
            new Transform(initialStateCovering.getDate(),
                          initialStateCovering.toTransform().getInverse(),
                          initialInertToBody);
        final FieldOfView fov =
            new DoubleDihedraFieldOfView(Vector3D.MINUS_K, Vector3D.PLUS_I,
                                         FastMath.toRadians(20),
                                         Vector3D.PLUS_J,
                                         FastMath.toRadians(20), 2);
        final FieldOfObservation fieldOfObservation =
            FieldOfObservation.builder(coveringSpacecraft, fov, initialFovBody)
                .build();

        // CLONING TESTS

        // Collision
        final Collision collision =
            Collision.builder(mockSpacecraft1, mockSpacecraft2, covariances1,
                              covariances2, LOFType.TNW, LOFType.TNW)
                .build();
        final Collision collisionCloned = collision.cloneObject();
        Assertions.assertEquals(collision.toString(),
                                collisionCloned.toString());

        // Covariance
        final Covariance covariance =
            Covariance.builder(mockSpacecraft1, covariances1, LOFType.TNW)
                .build();
        final Covariance covarianceCloned = covariance.cloneObject();
        Assertions.assertEquals(covariance.toString(),
                                covarianceCloned.toString());

        // Body
        final Body body = BodyFactory.getMoon(clock);
        final Body bodyCloned = body.cloneObject();
        Assertions.assertEquals(body.toString(), bodyCloned.toString());

        // Constellation
        final Constellation constellation =
            Constellation.builder(boundedPropagators, finalDate, clock).build();
        final Constellation constellationCloned = constellation.cloneObject();
        Assertions.assertEquals(constellation.toString(),
                                constellationCloned.toString());

        // Ground Station
        final CzmlGroundStation groundStation =
            CzmlGroundStation.builder(topocentricToulouse, clock).build();
        final CzmlGroundStation groundStationCloned =
            groundStation.cloneObject();
        Assertions.assertEquals(groundStation.toString(),
                                groundStationCloned.toString());

        // Influence sphere
        final InfluenceSphere influenceSphere =
            InfluenceSphere.builder(mars, clock).build();
        final InfluenceSphere influenceSphereCloned =
            influenceSphere.cloneObject();
        Assertions.assertEquals(influenceSphere.toString(),
                                influenceSphereCloned.toString());

        // Spacecraft
        final Spacecraft spacecraftCloned = mockSpacecraft1.cloneObject();
        Assertions.assertEquals(mockSpacecraft1.toString(),
                                spacecraftCloned.toString());

        // Attitude Pointing
        mockSpacecraft1.displaySpacecraftAttitude();
        final AttitudePointing attitudePointing =
            AttitudePointing
                .builder(mockSpacecraft1, getEarth(), Vector3D.MINUS_J, clock)
                .build();
        final AttitudePointing attitudePointingCloned =
            attitudePointing.cloneObject();
        Assertions.assertEquals(attitudePointing.toString(),
                                attitudePointingCloned.toString());

        // Covered Surface On Body
        final CoveredSurfaceOnBody coveredSurfaceOnBody =
            CoveredSurfaceOnBody.builder(coveringSpacecraft, fieldOfObservation)
                .withColor(Color.RED).withFill(false).withOutline(true).build();
        final CoveredSurfaceOnBody coveredSurfaceOnBodyCloned =
            coveredSurfaceOnBody.cloneObject();
        Assertions.assertEquals(coveredSurfaceOnBody.toString(),
                                coveredSurfaceOnBodyCloned.toString());

        // Central Body Reference System
        final CentralBodyReferenceSystem system =
            CentralBodyReferenceSystem.builder(clock).build();
        final CentralBodyReferenceSystem systemCloned = system.cloneObject();
        Assertions.assertEquals(system.toString(), systemCloned.toString());

        // Lat long lines
        final LatLongLines lines = LatLongLines.builder(clock).build();
        final LatLongLines linesCloned = lines.cloneObject();
        Assertions.assertEquals(lines.toString(), linesCloned.toString());

        // Spacecraft Reference system
        final SpacecraftReferenceSystem spacecraftReferenceSystem =
            new SpacecraftReferenceSystem(mockSpacecraft1);
        final SpacecraftReferenceSystem spacecraftReferenceSystemCloned =
            spacecraftReferenceSystem.cloneObject();
        Assertions.assertEquals(spacecraftReferenceSystem.toString(),
                                spacecraftReferenceSystemCloned.toString());

        // Field of Observation
        final FieldOfObservation fieldOfObservationCloned =
            fieldOfObservation.cloneObject();
        Assertions.assertEquals(fieldOfObservation.toString(),
                                fieldOfObservationCloned.toString());

        // Inter Sat Visu
        // With Spacecraft
        final InterSatVisu interSatVisuSpacecraft =
            InterSatVisu
                .builder(mockSpacecraft1, mockSpacecraft2, finalDate, clock)
                .build();
        final InterSatVisu interSatVisuSpacecraftCloned =
            interSatVisuSpacecraft.cloneObject();
        Assertions.assertEquals(interSatVisuSpacecraft.toString(),
                                interSatVisuSpacecraftCloned.toString());
        // With propagator list
        final InterSatVisu interSatVisuList =
            InterSatVisu.builder(boundedPropagators, finalDate, clock).build();
        final InterSatVisu interSatVisuListCloned =
            interSatVisuList.cloneObject();
        Assertions.assertEquals(interSatVisuList.toString(),
                                interSatVisuListCloned.toString());
        // With Constellation
        final InterSatVisu interSatVisuConstellation =
            InterSatVisu.builder(constellation, finalDate, clock).build();
        final InterSatVisu interSatVisuConstellationCloned =
            interSatVisuConstellation.cloneObject();
        Assertions.assertEquals(interSatVisuConstellation.toString(),
                                interSatVisuConstellationCloned.toString());

        // Line of visibility
        // With spacecraft
        final LineOfVisibility lineOfVisibilitySpacecraft =
            LineOfVisibility
                .builder(topocentricToulouse, mockSpacecraft1, clock).build();
        final LineOfVisibility lineOfVisibilitySpacecraftCloned =
            lineOfVisibilitySpacecraft.cloneObject();
        Assertions.assertEquals(lineOfVisibilitySpacecraft.toString(),
                                lineOfVisibilitySpacecraftCloned.toString());
        // With Constellation
        final LineOfVisibility lineOfVisibilityConstellation =
            LineOfVisibility.builder(topocentricToulouse, constellation, clock)
                .build();
        final LineOfVisibility lineOfVisibilityConstellationCloned =
            lineOfVisibilityConstellation.cloneObject();
        Assertions.assertEquals(lineOfVisibilityConstellation.toString(),
                                lineOfVisibilityConstellationCloned.toString());

        // Multiple line of visibility
        // With Spacecraft
        final MultipleLineOfVisibility multipleLineOfVisibilitySpacecraft =
            MultipleLineOfVisibility.builder(topocentricFrames, mockSpacecraft1)
                .build();
        final MultipleLineOfVisibility multipleLineOfVisibilitySpacecraftCloned =
            multipleLineOfVisibilitySpacecraft.cloneObject();
        Assertions
            .assertEquals(multipleLineOfVisibilitySpacecraft.toString(),
                          multipleLineOfVisibilitySpacecraftCloned.toString());
        // With Constellation
        final MultipleLineOfVisibility multipleLineOfVisibilityConstellation =
            MultipleLineOfVisibility.builder(topocentricFrames, constellation)
                .build();
        final MultipleLineOfVisibility multipleLineOfVisibilityConstellationCloned =
            multipleLineOfVisibilityConstellation.cloneObject();
        Assertions
            .assertEquals(multipleLineOfVisibilityConstellation.toString(),
                          multipleLineOfVisibilityConstellationCloned
                              .toString());

        // Station visibility circle
        final StationVisibilityCircle circle =
            StationVisibilityCircle
                .builder(topocentricToulouse, mockSpacecraft1, clock).build();
        final StationVisibilityCircle circleCloned = circle.cloneObject();
        Assertions.assertEquals(circle.toString(), circleCloned.toString());

        // Visibility Cone
        final VisibilityCone cone =
            new VisibilityCone(topocentricToulouse, mockSpacecraft1, 90.0,
                               clock);
        final VisibilityCone coneCloned = cone.cloneObject();
        Assertions.assertEquals(cone.toString(), coneCloned.toString());

        // Ground Track
        // With Spacecraft
        final GroundTrack groundTrack =
            GroundTrack.builder(mockSpacecraft1, getEarth(), clock).build();
        final GroundTrack groundTrackCloned = groundTrack.cloneObject();
        Assertions.assertEquals(groundTrack.toString(),
                                groundTrackCloned.toString());

        // Header
        final Header headerCloned = dummyHeader.cloneObject();
        Assertions.assertEquals(dummyHeader.toString(),
                                headerCloned.toString());

        // Maneuver Sequence
        final ManeuverSequence sequence =
            dummyManeuverSequence(startDate, finalDate, mockSpacecraft1);
        final ManeuverSequence sequenceCloned = sequence.cloneObject();
        Assertions.assertEquals(sequence.toString(), sequenceCloned.toString());

        // Billboard
        final Billboard billboard =
            new Billboard(Spacecraft.DEFAULT_MODEL_PATH);
        final Billboard billboardCloned = billboard.cloneObject();
        Assertions.assertEquals(billboard.toString(),
                                billboardCloned.toString());

        // Clock
        final Clock clockCloned = clock.cloneObject();
        Assertions.assertEquals(clock.toString(), clockCloned.toString());

        // Cylinder
        final Cylinder cylinder =
            new Cylinder(topocentricToulouse, mockSpacecraft1, 90.0, clock);
        final Cylinder cylinderCloned = cylinder.cloneObject();
        Assertions.assertEquals(cylinder.toString(), cylinderCloned.toString());

        // CzmlEllipsoid
        // With cartesians
        final Cartesian dummyCartesian = new Cartesian(40, 10, 5);
        final CzmlEllipsoid ellipsoidCartesian =
            CzmlEllipsoid.builder(dummyCartesian, clock).build();
        final CzmlEllipsoid ellipsoidCartesianCloned =
            ellipsoidCartesian.cloneObject();
        Assertions.assertEquals(ellipsoidCartesian.toString(),
                                ellipsoidCartesianCloned.toString());
        // With julian dates
        final CzmlEllipsoid ellipsoidJD =
            CzmlEllipsoid
                .builder(mockSpacecraft1.getJulianDates(),
                         mockSpacecraft1.getCartesianArraylist(), clock)
                .build();
        final CzmlEllipsoid ellipsoidJDCloned = ellipsoidJD.cloneObject();
        Assertions.assertEquals(ellipsoidJD.toString(),
                                ellipsoidJDCloned.toString());

        // Label
        final Label label = new Label("Text", Color.RED);
        final Label labelCloned = label.cloneObject();
        Assertions.assertEquals(label.toString(), labelCloned.toString());

        // Orientation
        // Single attitude
        final Orientation orientationSingleAttitude =
            Orientation.builder(mockSpacecraft1.getAttitudes().get(0),
                                FramesFactory.getEME2000())
                .build();
        final Orientation orientationSingleAttitudeCloned =
            orientationSingleAttitude.cloneObject();
        Assertions.assertEquals(orientationSingleAttitude.toString(),
                                orientationSingleAttitudeCloned.toString());
        // Multiple Attitudes
        final Orientation orientationMultipleAttitudes =
            Orientation.builder(mockSpacecraft1.getAttitudes(),
                                FramesFactory.getEME2000())
                .build();
        final Orientation orientationMultipleAttitudesCloned =
            orientationMultipleAttitudes.cloneObject();
        Assertions.assertEquals(orientationMultipleAttitudes.toString(),
                                orientationMultipleAttitudesCloned.toString());

        // Path
        final Path path = new Path(clock.getAvailability(), true);
        final Path pathCloned = path.cloneObject();
        Assertions.assertEquals(path.toString(), pathCloned.toString());

        // Polygon
        final Polygon polygon =
            Polygon.builder(mockSpacecraft1.getCartesianArraylist(), clock)
                .build();
        final Polygon polygonCloned = polygon.cloneObject();
        Assertions.assertEquals(polygon.toString(), polygonCloned.toString());

        // Time position
        final TimePosition timePosition =
            new TimePosition(mockSpacecraft1.getCartesianArraylist(),
                             mockSpacecraft1.getJulianDates());
        final TimePosition timePositionCloned = timePosition.cloneObject();
        Assertions.assertEquals(timePosition.toString(),
                                timePositionCloned.toString());
    }
}
