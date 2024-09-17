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
package org.orekit.czml.file;

import org.orekit.czml.errors.OreCzmlException;
import org.orekit.czml.errors.OreCzmlMessages;
import org.orekit.czml.object.primary.AbstractPrimaryObject;
import org.orekit.czml.object.primary.Header;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Czml File
 *
 * <p>
 * The Czml file is the center of all the project. It carries all the information that will are written after each object as been declared.
 * It contains only packets and objects and interface with the Czml File Writer that write everything inside the file.
 * </p>
 *
 * @author Julien LEBLOND.
 * @since 1.0.0
 */
public class CzmlFile {

    /** The header of the file. */
    private static Header header;

    /** The complete list of all the primary object to write. */
    private final List<AbstractPrimaryObject> abstractPrimaryObjects;

    /** The path to the file. */
    private String pathFile = "";

    /** The directory of the file. */
    private String pathDirectory = "";


    // Constructor

    /**
     * The basic constructor of the czml file object, built from a path.
     *
     * @param pathFile ; The path where the czml file is written.
     */
    public CzmlFile(final String pathFile) {
        final String[]      splittedPath  = pathFile.split("/");
        final StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < splittedPath.length - 1; i++) {
            stringBuilder.append(splittedPath[i])
                         .append("\\");
        }
        this.pathDirectory     = String.valueOf(stringBuilder);
        this.pathFile          = pathFile;
        abstractPrimaryObjects = new ArrayList<>();
    }


    // Display functions

    /** This function writes all the primary objects into the czml file. */
    public void write() throws URISyntaxException, IOException {
        if (abstractPrimaryObjects.isEmpty()) {
            throw new OreCzmlException(OreCzmlMessages.HEADER_ALONE);
        }
        if (!containsInstance(abstractPrimaryObjects, Header.class)) {
            throw new OreCzmlException(OreCzmlMessages.NO_HEADER);
        } else {
            header = (Header) abstractPrimaryObjects.get(indexOfInstance(abstractPrimaryObjects, Header.class));
            final StringWriter                writer       = header.getStringWriter();
            final List<AbstractPrimaryObject> noDuplicates = new ArrayList<>();
            noDuplicates.add(abstractPrimaryObjects.get(0));
            for (int i = 1; i < abstractPrimaryObjects.size(); i++) {
                final AbstractPrimaryObject currentAbstractPrimaryObject = abstractPrimaryObjects.get(i);
                if (!currentAbstractPrimaryObject.getId()
                                                 .equals(abstractPrimaryObjects.get(i - 1)
                                                                               .getId())) {
                    noDuplicates.add(currentAbstractPrimaryObject);
                }
            }
            Files.createDirectories(Paths.get(getPathDirectory()));

            for (final AbstractPrimaryObject currentPrimaryObject : noDuplicates) {
                currentPrimaryObject.writeCzmlBlock();
            }

            try (FileWriter FileWriter = new FileWriter(getPathFile())) {
                FileWriter.write(writer.toString() + "\n]");
            }
        }
    }


    // Getters

    /**
     * This function allows the addition of a primary object into the czml file object.
     *
     * @param object : Primary object to add
     */
    public void addObject(final AbstractPrimaryObject object) {
        abstractPrimaryObjects.add(object);
    }

    public String getPathDirectory() {
        return pathDirectory;
    }

    public String getPathFile() {
        return pathFile;
    }


    // Setters

    public void setHeader(final Header headerInput) {
        header = headerInput;
    }


    // Usable functions

    /** This method clears the path and the directory of the czml file, use this to write two czml file after another. */
    public void clear() {
        this.pathDirectory = "";
        this.pathFile      = "";
    }


    // Private functions

    private <E> boolean containsInstance(List<E> list, Class<? extends E> clazz) {
        for (E e : list) {
            if (clazz.isInstance(e)) {
                return true;
            }
        }
        return false;
    }

    private <E> int indexOfInstance(List<E> list, Class<? extends E> clazz) {
        for (int i = 0; i < list.size(); i++) {
            final E e = list.get(i);
            if (clazz.isInstance(e)) {
                return i;
            }
        }
        return 0;
    }
}
