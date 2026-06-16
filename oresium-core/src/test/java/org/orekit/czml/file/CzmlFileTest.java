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
package org.orekit.czml.file;

import cesiumlanguagewriter.CesiumOutputStream;
import cesiumlanguagewriter.CesiumStreamWriter;
import cesiumlanguagewriter.TimeInterval;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.orekit.annotation.DefaultDataContext;
import org.orekit.czml.errors.OresiumException;
import org.orekit.czml.errors.OresiumMessages;
import org.orekit.czml.object.primary.AbstractPrimaryObject;
import org.orekit.czml.object.primary.CzmlPrimaryObject;
import org.orekit.czml.object.primary.Header;
import org.orekit.czml.object.primary.entities.Spacecraft;
import org.orekit.propagation.BoundedPropagator;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScalesFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the {@link CzmlFile} class.
 *
 * @author Leblond Julien
 * @since 2.0
 */
@DefaultDataContext
class CzmlFileTest
    extends
    AbstractTest {

    /** Dummy header used across tests. */
    private Header testHeader;

    /** The initialize orekit data field. */
    private final double data = initializeOrekitData();

    @BeforeEach
    void setUp() {
        testHeader = dummyHeader();
    }

    // ========================================================================
    // Constructor
    // ========================================================================

    @Nested
    @DisplayName("Constructor tests")
    class ConstructorTests {

        @Test
        @DisplayName("Default constructor creates empty CzmlFile")
        void testDefaultConstructor() {
            final CzmlFile file = new CzmlFile();
            assertNotNull(file,
                          "CzmlFile should not be null after construction");
        }

        @Test
        @DisplayName("toString on empty file throws HEADER_ALONE exception")
        void testToStringEmptyFileThrowsException() {
            final CzmlFile file = new CzmlFile();
            final OresiumException exception =
                assertThrows(OresiumException.class, file::toString,
                             "toString() on an empty file should throw");
            assertTrue(exception.getMessage()
                .contains("No objects have been written"),
                       "Exception message should indicate header alone");
        }

        @Test
        @DisplayName("write(File) on empty file throws HEADER_ALONE exception")
        void testWriteFileEmptyFileThrowsException(@TempDir final Path tempDir) {
            final CzmlFile file = new CzmlFile();
            final File outputFile = tempDir.resolve("empty.czml").toFile();
            final OresiumException exception =
                assertThrows(OresiumException.class,
                             () -> file.write(outputFile),
                             "write() on an empty file should throw");
            assertTrue(exception.getMessage()
                .contains("No objects have been written"),
                       "Exception message should indicate header alone");
        }

        @Test
        @DisplayName("write(String) on empty file throws HEADER_ALONE exception")
        void testWriteStringEmptyFileThrowsException(@TempDir final Path tempDir) {
            final CzmlFile file = new CzmlFile();
            final String outputPath = tempDir.resolve("empty.czml").toString();
            final OresiumException exception =
                assertThrows(OresiumException.class,
                             () -> file.write(outputPath),
                             "write(String) on an empty file should throw");
            assertTrue(exception.getMessage()
                .contains("No objects have been written"),
                       "Exception message should indicate header alone");
        }
    }

    @ParameterizedTest
    @MethodSource("provideStreamOresiumException")
    public void writeTestThrow(final String path,
                               final OresiumMessages oresiumMessages) {

        final AbsoluteDate startDate =
            new AbsoluteDate(2026, 5, 1, 12, 0, 0.0,
                             TimeScalesFactory.getUTC());
        final AbsoluteDate finalDate = startDate.shiftedBy(10 * 60.0);
        final BoundedPropagator dummyPropagator =
            dummyPropagator(startDate, finalDate, dummyOrbit(startDate));
        final Spacecraft dummySpacecraft =
            Spacecraft.builder(dummyPropagator, dummyHeader().getClock())
                .build();

        final CzmlFile file =
            CzmlFile.builder(dummyHeader()).withSpacecraft(dummySpacecraft)
                .build();

        Assertions.assertThrows(OresiumException.class, () -> file.write(path));
        try {
            file.write(path);
        } catch (final OresiumException e) {
            Assertions.assertEquals(e.getMessage(),
                                    oresiumMessages.getSourceString());
        }
    }

    // ========================================================================
    // addObject / getDefaultRoot / clear
    // ========================================================================

    @Nested
    @DisplayName("addObject, getDefaultRoot, clear tests")
    class CoreMethodTests {

        @Test
        @DisplayName("addObject adds an object to the CzmlFile")
        void testAddObject() {
            // Given: create a file and a test object
            final CzmlFile file = new CzmlFile();
            final TestPrimaryObject testObj =
                new TestPrimaryObject("test-id", "test-name");

            // When: add the object
            file.addObject(testObj);

            // Then: toString should now throw NO_HEADER (not HEADER_ALONE)
            // because objects list is not empty but no Header is present
            final OresiumException exception =
                assertThrows(OresiumException.class, file::toString,
                             "Should throw NO_HEADER, not HEADER_ALONE");
            assertTrue(exception.getMessage().contains("No header was defined"),
                       "Exception should indicate missing header");
        }

        @Test
        @DisplayName("getDefaultRoot returns a non-empty string")
        void testGetDefaultRoot() {
            final String root = CzmlFile.getDefaultRoot();
            assertNotNull(root, "Default root should not be null");
            assertFalse(root.isEmpty(), "Default root should not be empty");
        }

        @Test
        @DisplayName("clear removes all objects from CzmlFile")
        void testClear() {
            // Given: create a file with objects
            final CzmlFile file = new CzmlFile();
            file.addObject(testHeader);
            file.addObject(new TestPrimaryObject("obj-1", "Object 1"));

            // When: clear the file
            file.clear();

            // Then: toString should now throw HEADER_ALONE (empty objects)
            final OresiumException exception =
                assertThrows(OresiumException.class, file::toString,
                             "After clear, toString should throw HEADER_ALONE");
            assertTrue(exception.getMessage()
                .contains("No objects have been written"),
                       "After clear, exception should indicate header alone");
        }
    }

    // ========================================================================
    // builder
    // ========================================================================

    @Nested
    @DisplayName("Builder tests")
    class BuilderTests {

        @Test
        @DisplayName("builder creates a CzmlFileBuilder with the given header")
        void testBuilder() {
            final CzmlFileBuilder builder = CzmlFile.builder(testHeader);
            assertNotNull(builder, "Builder should not be null");

            // Build a CzmlFile from the builder
            final CzmlFile file = builder.build();

            // The built file should have a header and be valid
            assertNotNull(file, "Built CzmlFile should not be null");

            // toString should produce valid output (no exception)
            final String result = file.toString();
            assertNotNull(result, "toString should produce a result");
            assertTrue(result.endsWith("]"),
                       "CZML output should end with closing bracket");
            assertTrue(result.contains("\"id\":\"document\""),
                       "CZML output should contain the document id");
        }

        @Test
        @DisplayName("builder with multiple objects creates valid CZML")
        void testBuilderWithObjects() {
            final CzmlFile file = CzmlFile.builder(testHeader).build();
            file.addObject(new TestPrimaryObject("sat-1", "Satellite 1"));
            file.addObject(new TestPrimaryObject("sat-2", "Satellite 2"));

            final String result = file.toString();
            assertNotNull(result, "toString should produce a result");
            assertTrue(result.endsWith("]"),
                       "CZML output should end with closing bracket");
            assertTrue(result.contains("\"id\":\"document\""),
                       "CZML output should contain the document id");
        }
    }

    // ========================================================================
    // toString tests
    // ========================================================================

    @Nested
    @DisplayName("toString tests")
    class ToStringTests {

        @Test
        @DisplayName("toString with only a header works")
        void testToStringWithHeader() {
            final CzmlFile file = new CzmlFile();
            file.addObject(testHeader);

            final String result = file.toString();
            assertNotNull(result, "toString should produce a result");
            assertTrue(result.endsWith("]"),
                       "CZML output should end with closing bracket");
            assertTrue(result.contains("\"id\":\"document\""),
                       "CZML output should contain document id");
            assertTrue(result.contains("\"version\":\"1.0\""),
                       "CZML output should contain version 1.0");
        }

        @Test
        @DisplayName("toString without a header throws NO_HEADER exception")
        void testToStringWithoutHeader() {
            final CzmlFile file = new CzmlFile();
            file.addObject(new TestPrimaryObject("obj-1", "No Header Object"));

            final OresiumException exception =
                assertThrows(OresiumException.class, file::toString,
                             "toString without header should throw NO_HEADER");
            assertTrue(exception.getMessage().contains("No header was defined"),
                       "Exception message should indicate no header");
        }

        @Test
        @DisplayName("toString removes consecutive duplicate object IDs")
        void testToStringRemovesConsecutiveDuplicateIds() {
            final CzmlFile file = new CzmlFile();
            file.addObject(testHeader);
            // Add two objects with the same ID consecutively
            file.addObject(new TestPrimaryObject("dup-id", "First Duplicate"));
            file.addObject(new TestPrimaryObject("dup-id", "Second Duplicate"));
            file.addObject(new TestPrimaryObject("unique-id", "Unique Object"));

            // Should not throw despite duplicates
            final String result = file.toString();
            assertNotNull(result, "toString should produce a result");
            assertTrue(result.endsWith("]"),
                       "CZML output should end with closing bracket");
        }

        @Test
        @DisplayName("toString preserves objects with different IDs")
        void testToStringPreservesDifferentIds() {
            final CzmlFile file = new CzmlFile();
            file.addObject(testHeader);
            file.addObject(new TestPrimaryObject("obj-a", "Object A"));
            file.addObject(new TestPrimaryObject("obj-b", "Object B"));
            file.addObject(new TestPrimaryObject("obj-c", "Object C"));

            final String result = file.toString();
            assertNotNull(result, "toString should produce a result");
            assertTrue(result.endsWith("]"),
                       "CZML output should end with closing bracket");
        }
    }

    // ========================================================================
    // write(File) tests
    // ========================================================================

    @Nested
    @DisplayName("write(File) tests")
    class WriteFileTests {

        @Test
        @DisplayName("write valid file succeeds")
        void testWriteValidFile(@TempDir final Path tempDir)
            throws IOException {
            // Given: create a CzmlFile with a header
            final CzmlFile file = new CzmlFile();
            file.addObject(testHeader);

            final File outputFile = tempDir.resolve("output.czml").toFile();

            // When: write to file
            file.write(outputFile);

            // Then: the file should exist and contain valid CZML content
            assertTrue(outputFile.exists(), "Output file should exist");
            final String content = Files.readString(outputFile.toPath());
            assertTrue(content.contains("\"id\":\"document\""),
                       "File content should contain document id");
            assertTrue(content.contains("\"version\":\"1.0\""),
                       "File content should contain version 1.0");
        }

        @Test
        @DisplayName("write throws IOException when file cannot be created")
        void testWriteThrowsIOException(@TempDir final Path tempDir) {
            // Given: create a CzmlFile with content
            final CzmlFile file = new CzmlFile();
            file.addObject(testHeader);
            file.addObject(new TestPrimaryObject("test-obj", "Test Object"));

            // Create a directory with the same name as the intended file
            // This will cause IOException when trying to write to it
            final File outputDir = tempDir.resolve("unwritable.czml").toFile();
            assertTrue(outputDir.mkdir(), "Should be able to create directory");

            // When/Then: write should throw OresiumException wrapping
            // IOException
            final OresiumException exception =
                Assertions
                    .assertThrows(OresiumException.class,
                                  () -> file.write(outputDir),
                                  "write() should throw OresiumException when file cannot be created");

            // Verify the exception message matches expected
            assertEquals(OresiumMessages.CZML_FILE_NOT_CREATED
                .getSourceString(), exception.getMessage(),
                         "Exception message should indicate file not created");
        }

        @Test
        @DisplayName("write creates parent directories automatically")
        void testWriteCreatesDirectories(@TempDir final Path tempDir) {
            final CzmlFile file = new CzmlFile();
            file.addObject(testHeader);

            final File nestedFile =
                tempDir.resolve("subdir").resolve("nested")
                    .resolve("output.czml").toFile();

            file.write(nestedFile);

            assertTrue(nestedFile.exists(),
                       "Nested output file should exist after write");
            assertTrue(nestedFile.getParentFile().exists(),
                       "Parent directories should be created");
        }

        @Test
        @DisplayName("write without header throws NO_HEADER exception")
        void testWriteWithoutHeader(@TempDir final Path tempDir) {
            final CzmlFile file = new CzmlFile();
            file.addObject(new TestPrimaryObject("obj-1", "No Header"));

            final File outputFile = tempDir.resolve("no-header.czml").toFile();

            final OresiumException exception =
                assertThrows(OresiumException.class,
                             () -> file.write(outputFile),
                             "write without header should throw NO_HEADER");
            assertTrue(exception.getMessage().contains("No header was defined"),
                       "Exception should indicate missing header");
        }

        @Test
        @DisplayName("write with header and objects succeeds")
        void testWriteWithHeaderAndObjects(@TempDir final Path tempDir)
            throws IOException {
            final CzmlFile file = new CzmlFile();
            file.addObject(testHeader);
            file.addObject(new TestPrimaryObject("sat-1", "Satellite 1"));
            file.addObject(new TestPrimaryObject("sat-2", "Satellite 2"));

            final File outputFile =
                tempDir.resolve("multi-object.czml").toFile();
            file.write(outputFile);

            assertTrue(outputFile.exists(), "Output file should exist");
            final String content = Files.readString(outputFile.toPath());
            assertTrue(content.contains("\"id\":\"document\""),
                       "File content should contain document id");
        }

        @Test
        @DisplayName("write clears the file after successful write")
        void testWriteClearsFile(@TempDir final Path tempDir) {
            final CzmlFile file = new CzmlFile();
            file.addObject(testHeader);

            final File outputFile = tempDir.resolve("output.czml").toFile();
            file.write(outputFile);

            // After write, file should be cleared. Adding no header should
            // result in HEADER_ALONE
            final OresiumException exception =
                assertThrows(OresiumException.class, file::toString,
                             "After write, file should be cleared");
            assertTrue(exception.getMessage()
                .contains("No objects have been written"),
                       "After write, exception should indicate header alone");
        }

        @Test
        @DisplayName("write removes consecutive duplicate IDs in written file")
        void testWriteRemovesDuplicateIds(@TempDir final Path tempDir)
            throws IOException {
            final CzmlFile file = new CzmlFile();
            file.addObject(testHeader);
            file.addObject(new TestPrimaryObject("dup-id", "Duplicate 1"));
            file.addObject(new TestPrimaryObject("dup-id", "Duplicate 2"));
            file.addObject(new TestPrimaryObject("unique-id", "Unique"));

            final File outputFile = tempDir.resolve("dedup.czml").toFile();
            // Should not throw despite duplicates
            file.write(outputFile);

            assertTrue(outputFile.exists(), "Output file should exist");
        }
    }

    // ========================================================================
    // write(String) tests
    // ========================================================================

    @Nested
    @DisplayName("write(String) tests")
    class WriteStringTests {

        @Test
        @DisplayName("write(String) delegates to write(File) successfully")
        void testWriteStringValid(@TempDir final Path tempDir)
            throws IOException {
            final CzmlFile file = new CzmlFile();
            file.addObject(testHeader);

            final String outputPath =
                tempDir.resolve("string-output.czml").toString();
            file.write(outputPath);

            final File outputFile = new File(outputPath);
            assertTrue(outputFile.exists(), "Output file should exist");
            final String content = Files.readString(outputFile.toPath());
            assertTrue(content.contains("\"id\":\"document\""),
                       "File content should contain document id");
        }
    }

    // ========================================================================
    // Test helper: minimal CzmlPrimaryObject stub
    // ========================================================================

    /**
     * Minimal stub implementation of {@link CzmlPrimaryObject} for testing the
     * {@link CzmlFile} class without the overhead of building a real object
     * like {@link org.orekit.czml.object.primary.entities.Spacecraft}.
     */
    static class TestPrimaryObject
        extends
        AbstractPrimaryObject<TestPrimaryObject> {

        /** The id of this test object. */
        private final String id;

        /** The name of this test object. */
        private final String name;

        /**
         * Constructor.
         *
         * @param id the object ID
         * @param name the object name
         */
        TestPrimaryObject(final String id, final String name) {
            this.id = id;
            this.name = name;
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
        public void writeCzmlBlock(final CesiumStreamWriter stream,
                                   final CesiumOutputStream output) {
            // No-op stub: does nothing for testing orchestration.
        }

        @Override
        public TimeInterval getAvailability() {
            return null;
        }

        @Override
        public void setAvailability(final TimeInterval interval) {
            // No-op stub
        }

        @Override
        public TestPrimaryObject cloneObject() {
            return new TestPrimaryObject(this.id, this.name);
        }
    }

    private static Stream<Arguments> provideStreamOresiumException() {
        return Stream
            .of(Arguments.of(" A name that ù^$*ù@-  doesn't work",
                             OresiumMessages.PATH_PROBLEM_WITH_CZML_FILE));
    }
}
