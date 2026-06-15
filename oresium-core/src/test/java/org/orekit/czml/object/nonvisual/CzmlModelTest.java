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
package org.orekit.czml.object.nonvisual;

import cesiumlanguagewriter.CesiumOutputStream;
import cesiumlanguagewriter.CesiumStreamWriter;
import cesiumlanguagewriter.PacketCesiumWriter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.orekit.annotation.DefaultDataContext;
import org.orekit.czml.errors.OresiumException;
import org.orekit.czml.errors.OresiumMessages;
import org.orekit.czml.file.AbstractTest;
import org.orekit.czml.object.ModelType;
import org.orekit.czml.object.primary.Header;
import org.orekit.czml.object.secondary.Billboard;
import org.orekit.czml.object.secondary.Clock;

import java.io.File;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * The type Czml model test.
 */
public class CzmlModelTest
    extends
    AbstractTest {

    /** Initialise orekit data. */
    private final double data = initializeOrekitData();

    private final Header header = dummyHeader();

    final CzmlModel modelCoverage =
        CzmlModel.builder(loadResources("Default3DModels/satellite.png"), false,
                          header.getClock())
            .build();

    /**
     * Czml model constructor test.
     */
    @Test
    @DefaultDataContext
    void CzmlModelConstructorTest() {

        final CzmlModel modelToTest =
            new CzmlModel(loadResources("Default3DModels/ISSModel.glb"), false,
                          header.getClock());

        final CzmlModel modelCoverage =
            new CzmlModel(loadResources("Default3DModels/satellite.png"), false,
                          header.getClock());

        final String pathFile =
            loadResources("templateFile/nonvisual/czmlmodel/CzmlModelTemplate.txt");

        final String coverageFile =
            loadResources("templateFile/nonvisual/czmlmodel/CzmlModelCoverageTemplate.txt");

        verifyFileOutput(pathFile, modelToTest.toString(), 1e-8);
        verifyFileOutput(coverageFile, modelCoverage.toString(), 1e-8);
    }

    /** Czml Model ocnstructor test. */
    @Test
    @DisplayName("Czml Model not satellite constructor")
    void CzmlModelNotSatelliteConstructorTest() {
        final CzmlModel model =
            new CzmlModel("", 10.0, 20.0, 2.0, false, header.getClock());

        final String outputExcepted = "{" + System.lineSeparator() + "}";

        Assertions.assertEquals(outputExcepted, model.toString());
    }

    @Test
    @DisplayName("Test for duplicate file function")
    void DuplicateFileTest() {
        // Récupérer le clock avant d'activer le mock pour éviter la
        // réinitialisation
        final Clock testClock = header.getClock();
        final String sourcePath =
            loadResources("Default3DModels/ground_Station.glb");
        final String targetFolder = loadResources("Default3DModels");

        try (final MockedStatic<Header> mocked = mockStatic(Header.class)) {
            // Configurer le mock AVANT de créer le CzmlModel
            mocked.when(Header::getPathToExternalResourceFolder)
                .thenReturn(targetFolder);

            final CzmlModel modelToTest =
                new CzmlModel(sourcePath, false, testClock);

            // Exécuter la méthode à tester
            modelToTest.duplicateFile(sourcePath);

            // Vérifier que le fichier a bien été copié
            final File expectedFile =
                new File(targetFolder, "ground_Station.glb");
            Assertions.assertTrue(expectedFile
                .exists(), "Le fichier dupliqué devrait exister dans le dossier de destination");
        }
    }

    @Test
    @DisplayName("Test degraded for duplicate file function")
    void DuplicateFileTestDegraded() {
        final Clock testClock = header.getClock();
        final String sourcePath = "Default3DModels/ground_Station.glb";
        final String targetFolder = loadResources("");
        try (final MockedStatic<Header> mocked = mockStatic(Header.class)) {
            // Configurer le mock AVANT de créer le CzmlModel
            mocked.when(Header::getPathToExternalResourceFolder)
                .thenReturn(targetFolder);

            final CzmlModel modelToTest =
                new CzmlModel(sourcePath, false, testClock);

            // Vérifier que le fichier a bien été copié
            Assertions
                .assertThrows(OresiumException.class,
                              () -> modelToTest.duplicateFile(sourcePath));
        }
    }

    @Nested
    @DisplayName("generateCZML method tests")
    public class GenerateCZMLTests {

        @Test
        @DisplayName("Test EMPTY_MODEL case for satellite - should write empty billboard")
        public void testGenerateCZMLEmptyModelSatellite() {
            // Create an empty model for satellite
            final CzmlModel emptyModel =
                new CzmlModel("", true, header.getClock());

            // Verify model type is EMPTY_MODEL
            Assertions.assertEquals(ModelType.EMPTY_MODEL,
                                    emptyModel.getModelType());

            // This should write an empty billboard
            final String result = emptyModel.toString();

            // For satellite empty model, the writeEmpty method should be called
            // which creates a billboard with DEFAULT_MODEL_NAME
            // The default model name contains base64 encoded image data
            Assertions
                .assertTrue(result.contains("data:image/png;base64,"),
                            "Satellite empty model should use default model name with base64 image");
        }

        @Test
        @DisplayName("Test unknown model type - should throw OresiumException")
        public void testGenerateCZMLUnknownModelType() {
            // Create a model and then use reflection to force an unknown model
            // type
            final CzmlModel model =
                new CzmlModel(loadResources("Default3DModels/satellite.png"),
                              false, header.getClock());

            // Use reflection to set the modelType to null, which will trigger
            // the unknown type exception
            try {
                java.lang.reflect.Field field =
                    CzmlModel.class.getDeclaredField("modelType");
                field.setAccessible(true);
                field.set(model, null);
            } catch (Exception e) {
                Assertions
                    .fail("Failed to set modelType field via reflection: " +
                          e.getMessage());
            }

            // Verify that generateCZML throws OresiumException for unknown
            // model type
            final StringWriter writer = new StringWriter();
            final CesiumOutputStream output = new CesiumOutputStream(writer);
            final CesiumStreamWriter streamWriter = new CesiumStreamWriter();

            try (PacketCesiumWriter packet = streamWriter.openPacket(output)) {
                OresiumException exception =
                    assertThrows(OresiumException.class,
                                 () -> model.generateCZML(packet, output));
                Assertions.assertEquals(OresiumMessages.MODEL_TYPE_UNKNOWN,
                                        exception.getSpecifier());
            }
        }
    }

    @Test
    public void testGenerate3DModelTestThrow() {

        final CzmlModel model = new CzmlModel("", true, header.getClock());

        model.setNameOfObject(" A name that ù^$*ù@-  doesn't work");

        final StringWriter writer = new StringWriter();
        final CesiumOutputStream output = new CesiumOutputStream(writer);
        final CesiumStreamWriter streamWriter = new CesiumStreamWriter();
        output.setPrettyFormatting(true);

        try (PacketCesiumWriter packet = streamWriter.openPacket(output)) {
            Assertions
                .assertThrows(OresiumException.class,
                              () -> model.generate3DModel(packet, output));
        }
    }

    @ParameterizedTest
    @MethodSource("provideStreamOresiumException")
    public void testWrite2DTestThrow(final String nameOfObject,
                                     final OresiumMessages messages) {
        final CzmlModel model = new CzmlModel("", false, header.getClock());

        model.setShow(true);
        model.setNameOfObject(nameOfObject);
        model.duplicateFile(nameOfObject);

        final StringWriter writer = new StringWriter();
        final CesiumOutputStream output = new CesiumOutputStream(writer);
        final CesiumStreamWriter streamWriter = new CesiumStreamWriter();
        output.setPrettyFormatting(true);

        try (PacketCesiumWriter packet = streamWriter.openPacket(output)) {
            Assertions.assertThrows(OresiumException.class,
                                    () -> model.write2D(packet, output));
            model.write2D(packet, output);
        } catch (OresiumException oresiumException) {
            Assertions.assertEquals(messages.getSourceString(),
                                    oresiumException.getMessage());
        }
    }

    @Nested
    public class GetterSetterTests {

        @Test
        public void minimumPixelSizeTest() {
            final CzmlModel modelCoverage =
                CzmlModel
                    .builder(loadResources("Default3DModels/satellite.png"),
                             false, header.getClock())
                    .build();
            modelCoverage.setMinimumPixelSize(60.0);
            Assertions.assertEquals(60.0, modelCoverage.getMinimumPixelSize());
        }

        @Test
        public void BillboardTest() {
            final CzmlModel modelCoverage =
                CzmlModel
                    .builder(loadResources("Default3DModels/satellite.png"),
                             false, header.getClock())
                    .build();
            final Billboard billboard =
                new Billboard(loadResources("Default3DModels/satellite.png"));
            modelCoverage.setBillboard(billboard);
            Assertions.assertEquals(billboard.toString(),
                                    modelCoverage.getBillboard().toString());
        }

        @Test
        public void ClockTest() {
            final Clock clock = header.getClock();
            Assertions.assertEquals(clock.toString(),
                                    modelCoverage.getClock().toString());
        }

        @Test
        public void ShowTest() {
            Assertions.assertTrue(modelCoverage.isShow());
        }

        @Test
        public void ScaleTest() {
            Assertions.assertEquals(0, modelCoverage.getScale());
            modelCoverage.setScale(60.0);
            Assertions.assertEquals(60.0, modelCoverage.getScale());
            modelCoverage.setScale(0.0);
        }

        @Test
        public void MaximumScaleTest() {
            Assertions.assertEquals(0, modelCoverage.getMaximumScale());
            modelCoverage.setMaximumScale(60.0);
            Assertions.assertEquals(60.0, modelCoverage.getMaximumScale());
            modelCoverage.setMaximumScale(0.0);
        }

        @Test
        public void NameOfObjectTest() {
            Assertions.assertNull(modelCoverage.getNameOfObject());
            modelCoverage.setNameOfObject("test");
            Assertions.assertEquals("test", modelCoverage.getNameOfObject());
            modelCoverage.setNameOfObject(null);
        }

        @Test
        public void CheckFromExtensionNominalTest() {
            final List<String> threeDExtensionList =
                Arrays.asList("3ds", "3mf", "dae", "fbx", "glb", "max", "obj",
                              "skp", "stl", "stp", "vrml", "x3d");
            final List<String> twoDExtensionList =
                Arrays.asList("avif", "bpm", "cgm", "gif", "heif", "jpeg",
                              "png", "svg", "tiff", "webp");

            for (final String extension : threeDExtensionList) {
                final ModelType threeDType =
                    CzmlModel.checkFromExtension(extension);
                Assertions.assertEquals(ModelType.MODEL_3D, threeDType);
            }

            for (final String extension : twoDExtensionList) {
                final ModelType twoDType =
                    CzmlModel.checkFromExtension(extension);
                Assertions.assertEquals(ModelType.MODEL_2D, twoDType);
            }
        }

        @Test
        public void CheckFromExtensionDegradedTest() {
            final String incorrectExtension = "wrong";
            Assertions.assertThrows(OresiumException.class, () -> CzmlModel
                .checkFromExtension(incorrectExtension));
        }
    }

    private static Stream<Arguments> provideStreamOresiumException() {
        return Stream
            .of(Arguments.of(" A name that ù^$*ù@-  doesn't work",
                             OresiumMessages.MODEL_2D_IMAGE_IS_NULL),
                Arguments.of("", OresiumMessages.MODEL_2D_IMAGE_IS_NULL));
    }
}
