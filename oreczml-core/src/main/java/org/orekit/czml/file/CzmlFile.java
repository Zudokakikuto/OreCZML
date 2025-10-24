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
import org.orekit.czml.errors.OreCzmlException;
import org.orekit.czml.errors.OreCzmlMessages;
import org.orekit.czml.object.primary.AbstractPrimaryObject;
import org.orekit.czml.object.primary.CzmlPrimaryObject;
import org.orekit.czml.object.primary.Header;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Czml File
 * <p>
 * The Czml file is the center of all the project. It carries all the
 * information that will are written after each object as been declared. It
 * contains only packets and objects and interface with the Czml File Writer
 * that write everything inside the file.
 *
 * @author Julien LEBLOND.
 * @since 1.0.0
 */
public class CzmlFile {

    /**
     * The default path to the root folder.
     */
    private static final String DEFAULT_ROOT =
        System.getProperty("user.dir").replace("\\", "/");

    /**
     * The complete list of all the primary object to write.
     */
    private List<CzmlPrimaryObject> objects;

    // Constructor

    /**
     * The basic constructor of the czml file object, built from a path.
     */
    public CzmlFile() {
        objects = new ArrayList<>();
    }

    // Display functions

    /**
     * Returns CZML file as a String.
     *
     * @return CZML file as String
     */
    @Override
    public String toString() {
        if (objects.isEmpty()) {
            throw new OreCzmlException(OreCzmlMessages.HEADER_ALONE);
        }
        // Get the header
        final Header header = getHeader();
        if (header == null) {
            throw new OreCzmlException(OreCzmlMessages.NO_HEADER);
        } else {
            // String writer used by Cesium
            final StringWriter writer = new StringWriter();

            // The output stream of cesium that will contain the strings to
            // write into the CzmLFile
            final CesiumOutputStream output = new CesiumOutputStream(writer);

            // The stream that converts all the strings into understandable
            // string for the CzmlFile.
            final CesiumStreamWriter streamWriter = new CesiumStreamWriter();

            // Remove duplicates
            final List<CzmlPrimaryObject> noDuplicates = new ArrayList<>();
            noDuplicates.add(objects.get(0));
            for (int i = 1; i < objects.size(); i++) {
                final CzmlPrimaryObject object = objects.get(i);
                if (!object.getId().equals(objects.get(i - 1).getId())) {
                    noDuplicates.add(object);
                }
            }

            // Write every object in output
            for (final CzmlPrimaryObject object : noDuplicates) {
                try {
                    object.writeCzmlBlock(streamWriter, output);
                } catch (URISyntaxException | IOException e) {
                    throw new OreCzmlException(OreCzmlMessages.STRING_NOT_GENERATED);
                }
            }

            return writer + System.lineSeparator() + "]";
        }
    }

    /**
     * This function writes all the primary objects into the czml file from a
     * path.
     *
     * @param outputFilePath file to write in
     * @throws URISyntaxException the uri syntax exception
     * @throws IOException the io exception
     */
    public void write(final String outputFilePath)
        throws URISyntaxException,
            IOException {
        write(new File(outputFilePath));
    }

    /**
     * This function writes all the primary objects into the czml file from a
     * File object.
     *
     * @param outputFile file to write in
     * @throws URISyntaxException the uri syntax exception
     * @throws IOException the io exception
     */
    public void write(final File outputFile)
        throws URISyntaxException,
            IOException {
        if (objects.isEmpty()) {
            throw new OreCzmlException(OreCzmlMessages.HEADER_ALONE);
        }
        // Get the header
        final Header header = getHeader();
        if (header == null) {
            throw new OreCzmlException(OreCzmlMessages.NO_HEADER);
        } else {
            // String writer used by Cesium
            final StringWriter writer = new StringWriter();

            // The output stream of cesium that will contain the strings to
            // write into the CzmLFile
            final CesiumOutputStream output = new CesiumOutputStream(writer);

            // The stream that converts all the strings into understandable
            // string for the CzmlFile.
            final CesiumStreamWriter streamWriter = new CesiumStreamWriter();

            // Remove duplicates
            final List<CzmlPrimaryObject> noDuplicates = new ArrayList<>();
            noDuplicates.add(objects.get(0));
            for (int i = 1; i < objects.size(); i++) {
                final CzmlPrimaryObject object = objects.get(i);
                if (!object.getId().equals(objects.get(i - 1).getId())) {
                    noDuplicates.add(object);
                }
            }

            // Write every object in output
            for (final CzmlPrimaryObject object : noDuplicates) {
                object.writeCzmlBlock(streamWriter, output);
            }

            // Write in file
            final boolean out = outputFile.getParentFile().mkdirs(); // Create
                                                                     // output
                                                                     // directory
                                                                     // if
            // needed
            try (BufferedWriter FileWriter =
                Files.newBufferedWriter(outputFile.toPath(),
                                        StandardCharsets.UTF_8)) {
                FileWriter.write(content + System.lineSeparator() + "]");
            }
        }
        clear();
    }

    /**
     * Builder czml file builder.
     *
     * @param headerInput : The header considered.
     * @return the czml file builder
     */
    public static CzmlFileBuilder builder(final Header headerInput) {
        return new CzmlFileBuilder(headerInput);
    }

    // Getters

    /**
     * This function allows the addition of a primary object into the czml file
     * object.
     *
     * @param object : Primary object to add
     */
    public void addObject(final AbstractPrimaryObject object) {
        objects.add(object);
    }

    /**
     * Gets default root.
     *
     * @return the default root
     */
    public static String getDefaultRoot() {
        return DEFAULT_ROOT;
    }

    /**
     * Get the header.
     *
     * @return header or null of no header
     */
    private Header getHeader() {
        for (final CzmlPrimaryObject object : objects) {
            if (object instanceof Header) {
                return (Header) object;
            }
        }
        return null;
    }

    // Usable functions

    /**
     * This method clears the path and the directory of the czml file, use this
     * to write two czml file after another.
     */
    public void clear() {
        this.objects = new ArrayList<>();
    }
}
