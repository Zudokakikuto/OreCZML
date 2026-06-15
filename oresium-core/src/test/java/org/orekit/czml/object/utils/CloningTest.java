package org.orekit.czml.object.utils;

import cesiumlanguagewriter.Cartesian;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.hipparchus.linear.MatrixUtils;
import org.hipparchus.linear.RealMatrix;
import org.hipparchus.util.FastMath;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import java.util.ArrayList;
import java.util.List;

/** Class to test all the clone object functions. */
public class CloningTest
    extends
    AbstractTest {

    /** Initialise orekit data. */
    private final double data = initializeOrekitData();

    /** Header. */
    private final Header dummyHeader = dummyHeader();

    /** Clock. */
    private final Clock clock = dummyHeader.getClock();

    // Dates

    /** Start date. */
    private final AbsoluteDate startDate =
        DateUtils.toAbsoluteDate(dummyHeader.getAvailability().getStart());

    /** Final date. */
    private final AbsoluteDate finalDate =
        DateUtils.toAbsoluteDate(dummyHeader.getAvailability().getStop());

    // Orbits

    /** First orbit. */
    private final Orbit firstOrbit =
        dummyOrbit(startDate, 7900000, 0, 10, 0, 0, 0);

    /** Second orbit. */
    private final Orbit secondOrbit =
        dummyOrbit(startDate, 7900000, 0, -10, 0, 0.1, 0);

    /** Orbit or coverage. */
    private final Orbit orbitCovering =
        new KeplerianOrbit(7878000, 0, FastMath.toRadians(20), 0,
                           FastMath.toRadians(0), FastMath.toRadians(0),
                           PositionAngleType.MEAN, FramesFactory.getEME2000(),
                           startDate, Constants.WGS84_EARTH_MU);

    // Propagators

    /** First propagator with ephemeris. */
    private final PropagatorWithEphemeris propagatorWithEphemeris1 =
        dummyNumericalPropagatorWithEphemeris(startDate, finalDate, firstOrbit);

    /** Second propagator with ephemeris. */
    private final PropagatorWithEphemeris propagatorWithEphemeris2 =
        dummyNumericalPropagatorWithEphemeris(startDate, finalDate,
                                              secondOrbit);

    /** First numerical propagator. */
    private final NumericalPropagator mockNumericalPropagator1 =
        propagatorWithEphemeris1.getPropagator();

    /** Second numerical propagator. */
    private final NumericalPropagator mockNumericalPropagator2 =
        propagatorWithEphemeris2.getPropagator();

    /** First mocked bounded propagator. */
    private final BoundedPropagator mockBoundedPropagator1 =
        propagatorWithEphemeris1.getEphemeris();

    /** Second mocked bounded propagator. */
    private final BoundedPropagator mockBoundedPropagator2 =
        propagatorWithEphemeris2.getEphemeris();

    /** Coverage bounded propagator. */
    private final BoundedPropagator boundedPropagatorCovering =
        dummyPropagator(startDate, finalDate, orbitCovering);

    /** List of bounded propagators. */
    private final List<BoundedPropagator> boundedPropagators =
        new ArrayList<>();

    // Spacecrafts

    /** First mocked spacecraft. */
    private final Spacecraft mockSpacecraft1 =
        Spacecraft.builder(mockBoundedPropagator1, clock).build();

    /** Second mocked spacecraft. */
    private final Spacecraft mockSpacecraft2 =
        Spacecraft.builder(mockBoundedPropagator2, clock).build();

    // Bodies

    /** Body of Mars. */
    private final Body mars = BodyFactory.getMars(clock);

    // Covariances

    /** Matrix of covariance. */
    private final RealMatrix realMatrix =
        MatrixUtils.createRealDiagonalMatrix(new double[] {
            20000 *
                                                            20000,
            1e-6, 1e-6, 1e-6, 1e-6, (36 * 4.848e-6) * (36 * 4.848e-6)
        });

    /** State covariance. */
    private final StateCovariance stateCovariance =
        new StateCovariance(realMatrix, startDate, FramesFactory.getEME2000(),
                            OrbitType.EQUINOCTIAL, PositionAngleType.MEAN);

    // Propagation of the covariances

    /** First covariance propagated. */
    final List<StateCovariance> covariances1 =
        covariancePropagation(mockSpacecraft1, mockNumericalPropagator1,
                              stateCovariance, clock);

    /** Second covariance propagated. */
    final List<StateCovariance> covariances2 =
        covariancePropagation(mockSpacecraft2, mockNumericalPropagator2,
                              stateCovariance, clock);

    // Topocentric frames
    // Creation of a topocentric frame around Toulouse.

    /** Toulouse point. */
    final GeodeticPoint toulousePoint =
        new GeodeticPoint(FastMath.toRadians(43.6047),
                          FastMath.toRadians(1.4442), 10);

    /** Toulouse frame. */
    final TopocentricFrame topocentricToulouse =
        new TopocentricFrame(AbstractTest.getEarth(), toulousePoint,
                             "Toulouse");

    // Creation of a topocentric frame around Quito

    /** Quito point. */
    final GeodeticPoint quitoPoint =
        new GeodeticPoint(FastMath.toRadians(0.1807),
                          FastMath.toRadians(11.5382), 2850);

    /** Quito frame. */
    final TopocentricFrame topocentricQuito =
        new TopocentricFrame(getEarth(), quitoPoint, "Quito");

    // Creation of a list of topocentric frame
    final List<TopocentricFrame> topocentricFrames = new ArrayList<>();

    /** Default constructor. */
    public CloningTest() {
    }

    @Nested
    class PrimaryObjectsCloningTest {

        @Test
        @DisplayName("Cloning of collision")
        void CollisionCloningTest() {

            // Collision
            final Collision collision =
                Collision
                    .builder(mockSpacecraft1, mockSpacecraft2, covariances1,
                             covariances2, LOFType.TNW, LOFType.TNW)
                    .build();
            final Collision collisionCloned = collision.cloneObject();
            Assertions.assertEquals(collision.toString(),
                                    collisionCloned.toString());

        }

        @Test
        @DisplayName("Cloning of covariance")
        void CovarianceCloningTest() {

            // Covariance
            final Covariance covariance =
                Covariance.builder(mockSpacecraft1, covariances1, LOFType.TNW)
                    .build();
            final Covariance covarianceCloned = covariance.cloneObject();
            Assertions.assertEquals(covariance.toString(),
                                    covarianceCloned.toString());

        }

        @Test
        @DisplayName("Cloning of body")
        void BodyCloningTest() {

            // Body
            final Body body = BodyFactory.getMoon(clock);
            final Body bodyCloned = body.cloneObject();
            Assertions.assertEquals(body.toString(), bodyCloned.toString());
        }

        @Test
        @DisplayName("Cloning of constellation")
        void ConstellationCloningTest() {

            boundedPropagators.add(mockBoundedPropagator1);
            boundedPropagators.add(mockBoundedPropagator2);
            // Constellation
            final Constellation constellation =
                Constellation.builder(boundedPropagators, finalDate, clock)
                    .build();
            final Constellation constellationCloned =
                constellation.cloneObject();
            Assertions.assertEquals(constellation.toString(),
                                    constellationCloned.toString());
            boundedPropagators.clear();
        }

        @Test
        @DisplayName("Cloning of ground station")
        void GroundStationCloningTest() {

            // Ground Station
            final CzmlGroundStation groundStation =
                CzmlGroundStation.builder(topocentricToulouse, clock).build();
            final CzmlGroundStation groundStationCloned =
                groundStation.cloneObject();
            Assertions.assertEquals(groundStation.toString(),
                                    groundStationCloned.toString());
        }

        @Test
        @DisplayName("Cloning of influence sphere")
        void InfluenceSphereCloningTest() {

            // Influence sphere
            final InfluenceSphere influenceSphere =
                InfluenceSphere.builder(mars, clock).build();
            final InfluenceSphere influenceSphereCloned =
                influenceSphere.cloneObject();
            Assertions.assertEquals(influenceSphere.toString(),
                                    influenceSphereCloned.toString());
        }

        @Test
        @DisplayName("Cloning of spacecraft")
        void SpacecraftCloningTest() {

            // Spacecraft
            final Spacecraft spacecraftCloned = mockSpacecraft1.cloneObject();
            Assertions.assertEquals(mockSpacecraft1.toString(),
                                    spacecraftCloned.toString());
        }

        @Test
        @DisplayName("Cloning of attitude pointing")
        void AttitudePointingCloningTest() {

            // Attitude Pointing
            mockSpacecraft1.displaySpacecraftAttitude();
            final AttitudePointing attitudePointing =
                AttitudePointing.builder(mockSpacecraft1, getEarth(),
                                         Vector3D.MINUS_J, clock)
                    .build();
            final AttitudePointing attitudePointingCloned =
                attitudePointing.cloneObject();
            Assertions.assertEquals(attitudePointing.toString(),
                                    attitudePointingCloned.toString());
        }

        @Test
        @DisplayName("Cloning of covered surface on body")
        void CoveredSurfaceOnBodyCloningTest() {

            // Initialisation of the field of view
            final SpacecraftState initialStateCovering =
                new SpacecraftState(orbitCovering);
            final SinusoidalLof sinusoidalLof =
                new SinusoidalLof(FramesFactory.getEME2000(), LOFType.VNC,
                                  Vector3D.PLUS_I, 3600,
                                  FastMath.toRadians(45.0),
                                  initialStateCovering.getDate());
            boundedPropagatorCovering.setAttitudeProvider(sinusoidalLof);
            boundedPropagatorCovering.propagate(startDate, finalDate);

            final Spacecraft spacecraft =
                Spacecraft.builder(boundedPropagatorCovering, clock)
                    .withColor(Color.RED).withOnlyOnePeriod()
                    .withDisplayAttitude().withReferenceSystem().build();

            // Creation of the field of observation of the satellite, it
            // describes
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
                FieldOfObservation.builder(spacecraft, fov, initialFovBody)
                    .build();

            // Covered Surface On Body
            final CoveredSurfaceOnBody coveredSurfaceOnBody =
                CoveredSurfaceOnBody.builder(spacecraft, fieldOfObservation)
                    .withColor(Color.RED).withFill(false).withOutline(true)
                    .build();
            final CoveredSurfaceOnBody coveredSurfaceOnBodyCloned =
                coveredSurfaceOnBody.cloneObject();
            Assertions.assertEquals(coveredSurfaceOnBody.toString(),
                                    coveredSurfaceOnBodyCloned.toString());
        }

        @Test
        @DisplayName("Cloning of central body reference system")
        void CentralBodyReferenceSystemCloningTest() {

            // Central Body Reference System
            final CentralBodyReferenceSystem system =
                CentralBodyReferenceSystem.builder(clock).build();

            final CentralBodyReferenceSystem systemCloned =
                system.cloneObject();
            Assertions.assertEquals(system.toString(), systemCloned.toString());
        }

        @Test
        @DisplayName("Cloning of latitude longitude lines")
        void LatitudeLongitudeLinesCloningTest() {

            // Lat long lines
            final LatLongLines lines = LatLongLines.builder(clock).build();

            final LatLongLines linesCloned = lines.cloneObject();
            Assertions.assertEquals(lines.toString(), linesCloned.toString());
        }

        @Test
        @DisplayName("Cloning of spacecraft reference system")
        void SpacecraftReferenceSystemCloningTest() {

            // Spacecraft Reference system
            final SpacecraftReferenceSystem spacecraftReferenceSystem =
                new SpacecraftReferenceSystem(mockSpacecraft1);

            final SpacecraftReferenceSystem spacecraftReferenceSystemCloned =
                spacecraftReferenceSystem.cloneObject();
            Assertions.assertEquals(spacecraftReferenceSystem.toString(),
                                    spacecraftReferenceSystemCloned.toString());

        }

        @Test
        @DisplayName("Cloning of field of observation")
        void FieldOfObservationCloningTest() {

            // Initialisation of the field of view
            final SpacecraftState initialStateCovering =
                new SpacecraftState(orbitCovering);
            final SinusoidalLof sinusoidalLof =
                new SinusoidalLof(FramesFactory.getEME2000(), LOFType.VNC,
                                  Vector3D.PLUS_I, 3600,
                                  FastMath.toRadians(45.0),
                                  initialStateCovering.getDate());
            boundedPropagatorCovering.setAttitudeProvider(sinusoidalLof);
            boundedPropagatorCovering.propagate(startDate, finalDate);

            final Spacecraft spacecraft =
                Spacecraft.builder(boundedPropagatorCovering, clock)
                    .withColor(Color.RED).withOnlyOnePeriod()
                    .withDisplayAttitude().withReferenceSystem().build();

            // Creation of the field of observation of the satellite, it
            // describes
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
                FieldOfObservation.builder(spacecraft, fov, initialFovBody)
                    .build();

            // Field of Observation
            final FieldOfObservation fieldOfObservationCloned =
                fieldOfObservation.cloneObject();
            Assertions.assertEquals(fieldOfObservation.toString(),
                                    fieldOfObservationCloned.toString());
        }

        @Test
        @DisplayName("Cloning of inter sat visu with Spacecraft")
        void InterSatVisuCloningTest() {

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
        }

        @Test
        @DisplayName("Cloning of inter sat visu with propagator list")
        void InterSatVisuPropagatorsCloningTest() {

            boundedPropagators.add(mockBoundedPropagator1);
            boundedPropagators.add(mockBoundedPropagator2);
            // With propagator list
            final InterSatVisu interSatVisuList =
                InterSatVisu.builder(boundedPropagators, finalDate, clock)
                    .build();

            final InterSatVisu interSatVisuListCloned =
                interSatVisuList.cloneObject();
            Assertions.assertEquals(interSatVisuList.toString(),
                                    interSatVisuListCloned.toString());
            boundedPropagators.clear();
        }

        @Test
        @DisplayName("Cloning of inter sat visu with constellation")
        void InterSatVisuConstellationCloningTest() {

            boundedPropagators.add(mockBoundedPropagator1);
            boundedPropagators.add(mockBoundedPropagator2);
            // Constellation
            final Constellation constellation =
                Constellation.builder(boundedPropagators, finalDate, clock)
                    .build();
            // With Constellation
            final InterSatVisu interSatVisuConstellation =
                InterSatVisu.builder(constellation, finalDate, clock).build();

            final InterSatVisu interSatVisuConstellationCloned =
                interSatVisuConstellation.cloneObject();
            Assertions.assertEquals(interSatVisuConstellation.toString(),
                                    interSatVisuConstellationCloned.toString());
            boundedPropagators.clear();
        }

        @Test
        @DisplayName("Cloning of line of visibility with spacecraft")
        void LineOfVisibilitySpacecraftCloningTest() {

            // Line of visibility
            // With spacecraft
            final LineOfVisibility lineOfVisibilitySpacecraft =
                LineOfVisibility
                    .builder(topocentricToulouse, mockSpacecraft1, clock)
                    .build();

            final LineOfVisibility lineOfVisibilitySpacecraftCloned =
                lineOfVisibilitySpacecraft.cloneObject();
            Assertions
                .assertEquals(lineOfVisibilitySpacecraft.toString(),
                              lineOfVisibilitySpacecraftCloned.toString());
        }

        @Test
        @DisplayName("Cloning of line of visibility with constellation")
        void LineOfVisibilityConstellationCloningTest() {

            boundedPropagators.add(mockBoundedPropagator1);
            boundedPropagators.add(mockBoundedPropagator2);
            // Constellation
            final Constellation constellation =
                Constellation.builder(boundedPropagators, finalDate, clock)
                    .build();
            // With Constellation
            final LineOfVisibility lineOfVisibilityConstellation =
                LineOfVisibility
                    .builder(topocentricToulouse, constellation, clock).build();

            final LineOfVisibility lineOfVisibilityConstellationCloned =
                lineOfVisibilityConstellation.cloneObject();
            Assertions
                .assertEquals(lineOfVisibilityConstellation.toString(),
                              lineOfVisibilityConstellationCloned.toString());
            boundedPropagators.clear();
        }

        @Test
        @DisplayName("Cloning of multiple line of visibility with spacecraft")
        void MultipleLineOfVisibilitySpacecraftCloningTest() {

            topocentricFrames.add(topocentricToulouse);
            topocentricFrames.add(topocentricQuito);
            // Multiple line of visibility
            // With Spacecraft
            final MultipleLineOfVisibility multipleLineOfVisibilitySpacecraft =
                MultipleLineOfVisibility
                    .builder(topocentricFrames, mockSpacecraft1).build();

            final MultipleLineOfVisibility multipleLineOfVisibilitySpacecraftCloned =
                multipleLineOfVisibilitySpacecraft.cloneObject();
            Assertions
                .assertEquals(multipleLineOfVisibilitySpacecraft.toString(),
                              multipleLineOfVisibilitySpacecraftCloned
                                  .toString());
            topocentricFrames.clear();
        }

        @Test
        @DisplayName("Cloning of multiple line of visibility with constellation")
        void MultipleLineOfVisibilityConstellationCloningTest() {

            topocentricFrames.add(topocentricToulouse);
            topocentricFrames.add(topocentricQuito);

            boundedPropagators.add(mockBoundedPropagator1);
            boundedPropagators.add(mockBoundedPropagator2);

            // Constellation
            final Constellation constellation =
                Constellation.builder(boundedPropagators, finalDate, clock)
                    .build();
            // With Constellation
            final MultipleLineOfVisibility multipleLineOfVisibilityConstellation =
                MultipleLineOfVisibility
                    .builder(topocentricFrames, constellation).build();

            final MultipleLineOfVisibility multipleLineOfVisibilityConstellationCloned =
                multipleLineOfVisibilityConstellation.cloneObject();
            Assertions
                .assertEquals(multipleLineOfVisibilityConstellation.toString(),
                              multipleLineOfVisibilityConstellationCloned
                                  .toString());

            boundedPropagators.clear();
            topocentricFrames.clear();
        }

        @Test
        @DisplayName("Cloning of station visibility circle")
        void StationVisibilityCircleCloningTest() {

            // Station visibility circle
            final StationVisibilityCircle circle =
                StationVisibilityCircle
                    .builder(topocentricToulouse, mockSpacecraft1, clock)
                    .build();

            final StationVisibilityCircle circleCloned = circle.cloneObject();
            Assertions.assertEquals(circle.toString(), circleCloned.toString());

        }

        @Test
        @DisplayName("Cloning of visibility cone")
        void VisibilityConeCloningTest() {

            // Visibility Cone
            final VisibilityCone cone =
                new VisibilityCone(topocentricToulouse, mockSpacecraft1, 90.0,
                                   clock);

            final VisibilityCone coneCloned = cone.cloneObject();
            Assertions.assertEquals(cone.toString(), coneCloned.toString());
        }

        @Test
        @DisplayName("Cloning of ground track")
        void GrounTrackCloningTest() {

            // Ground Track
            // With Spacecraft
            final GroundTrack groundTrack =
                GroundTrack.builder(mockSpacecraft1, getEarth(), clock).build();

            final GroundTrack groundTrackCloned = groundTrack.cloneObject();
            Assertions.assertEquals(groundTrack.toString(),
                                    groundTrackCloned.toString());
        }

        @Test
        @DisplayName("Cloning of header")
        void HeaderCloningTest() {

            // Header
            final Header headerCloned = dummyHeader.cloneObject();
            Assertions.assertEquals(dummyHeader.toString(),
                                    headerCloned.toString());
        }

        @Test
        @DisplayName("Cloning of maneuver sequence")
        void ManeuverSequenceCloningTest() {

            // Maneuver Sequence
            final ManeuverSequence sequence =
                dummyManeuverSequence(startDate, finalDate, mockSpacecraft1);

            final ManeuverSequence sequenceCloned = sequence.cloneObject();
            Assertions.assertEquals(sequence.toString(),
                                    sequenceCloned.toString());
        }
    }

    @Nested
    class SecondaryObjectsCloningTest {

        @Test
        @DisplayName("Cloning of billboard")
        void BillboardCloningTest() {

            // Billboard
            final Billboard billboard =
                new Billboard(Spacecraft.DEFAULT_MODEL_PATH);

            final Billboard billboardCloned = billboard.cloneObject();
            Assertions.assertEquals(billboard.toString(),
                                    billboardCloned.toString());

        }

        @Test
        @DisplayName("Cloning of clock")
        void ClockCloningTest() {

            // Clock
            final Clock clockCloned = clock.cloneObject();
            Assertions.assertEquals(clock.toString(), clockCloned.toString());

        }

        @Test
        @DisplayName("Cloning of cylinder")
        void CylinderCloningTest() {

            // Cylinder
            final Cylinder cylinder =
                new Cylinder(topocentricToulouse, mockSpacecraft1, 90.0, clock);

            final Cylinder cylinderCloned = cylinder.cloneObject();
            Assertions.assertEquals(cylinder.toString(),
                                    cylinderCloned.toString());
        }

        @Test
        @DisplayName("Cloning of czml ellipsoid with cartesians")
        void CzmlEllipsoidCartesiansCloningTest() {

            // CzmlEllipsoid
            // With cartesians
            final Cartesian dummyCartesian = new Cartesian(40, 10, 5);

            final CzmlEllipsoid ellipsoidCartesian =
                CzmlEllipsoid.builder(dummyCartesian, clock).build();

            final CzmlEllipsoid ellipsoidCartesianCloned =
                ellipsoidCartesian.cloneObject();
            Assertions.assertEquals(ellipsoidCartesian.toString(),
                                    ellipsoidCartesianCloned.toString());
        }

        @Test
        @DisplayName("Cloning of czml ellipsoid with julian dates")
        void CzmlEllipsoidJDCloningTest() {

            // With julian dates
            final CzmlEllipsoid ellipsoidJD =
                CzmlEllipsoid
                    .builder(mockSpacecraft1.getJulianDates(),
                             mockSpacecraft1.getCartesianArraylist(), clock)
                    .build();

            final CzmlEllipsoid ellipsoidJDCloned = ellipsoidJD.cloneObject();
            Assertions.assertEquals(ellipsoidJD.toString(),
                                    ellipsoidJDCloned.toString());
        }

        @Test
        @DisplayName("Cloning of label")
        void LabelCloningTest() {

            // Label
            final Label label = new Label("Text", Color.RED);

            final Label labelCloned = label.cloneObject();
            Assertions.assertEquals(label.toString(), labelCloned.toString());

        }

        @Test
        @DisplayName("Cloning of orientation with a single attitude")
        void OrientationSingleCloningTest() {

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
        }

        @Test
        @DisplayName("Cloning of orientation with multiple attitudes")
        void OrientationMultipleAttitudesCloningTest() {

            // Multiple Attitudes
            final Orientation orientationMultipleAttitudes =
                Orientation.builder(mockSpacecraft1.getAttitudes(),
                                    FramesFactory.getEME2000())
                    .build();

            final Orientation orientationMultipleAttitudesCloned =
                orientationMultipleAttitudes.cloneObject();
            Assertions
                .assertEquals(orientationMultipleAttitudes.toString(),
                              orientationMultipleAttitudesCloned.toString());

        }

        @Test
        @DisplayName("Cloning of path")
        void PathCloningTest() {

            // Path
            final Path path = new Path(clock.getAvailability(), true);

            final Path pathCloned = path.cloneObject();
            Assertions.assertEquals(path.toString(), pathCloned.toString());

        }

        @Test
        @DisplayName("Cloning of poygon")
        void PolygonCloningTest() {

            // Polygon
            final Polygon polygon =
                Polygon.builder(mockSpacecraft1.getCartesianArraylist(), clock)
                    .build();

            final Polygon polygonCloned = polygon.cloneObject();
            Assertions.assertEquals(polygon.toString(),
                                    polygonCloned.toString());

        }

        @Test
        @DisplayName("Cloning of time position")
        void TimePositionCloningTest() {

            // Time position
            final TimePosition timePosition =
                new TimePosition(mockSpacecraft1.getCartesianArraylist(),
                                 mockSpacecraft1.getJulianDates());

            final TimePosition timePositionCloned = timePosition.cloneObject();
            Assertions.assertEquals(timePosition.toString(),
                                    timePositionCloned.toString());
        }
    }
}
