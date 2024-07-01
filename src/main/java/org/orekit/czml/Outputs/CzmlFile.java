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
package org.orekit.czml.Outputs;

import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.AbstractPrimaryObject;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.CzmlPrimaryObject;
import org.orekit.czml.CzmlObjects.CzmlPrimaryObjects.Header;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/** Czml File

 * <p>
 * The Czml file is the center of all the project. It carries all the information that will are written after each object as been declared.
 * It contains only packets and objects and interface with the Czml File Writer that write everything inside the file.
 * </p>
 *
 * @since 1.0
 * @author Julien LEBLOND.
 */
public class CzmlFile {

    /** .*/
    private final List<AbstractPrimaryObject> abstractPrimaryObjects;
    /** .*/
    private final List<CzmlPrimaryObject> czmlObjects;
    /** .*/
    private static Header header;
    /** .*/
    private String pathFile = "";
    /** .*/
    private String pathDirectory = "";


    public CzmlFile(final String pathFile) {
        final String[] splittedPath = pathFile.split("/");
        final StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < splittedPath.length - 1; i++) {
            stringBuilder.append(splittedPath[i]).append("\\");
        }
        this.pathDirectory = String.valueOf(stringBuilder);
        this.pathFile = pathFile;
        abstractPrimaryObjects = new ArrayList<>();
        czmlObjects = new ArrayList<>();
    }

    public void write() throws URISyntaxException, IOException {
        if (abstractPrimaryObjects.isEmpty()) {
            throw new RuntimeException("No objects other than the header have been written.");
        }
        else {
            final StringWriter writer = header.getStringWriter();
            final List<AbstractPrimaryObject> noDuplicates = new ArrayList<>();
            noDuplicates.add(abstractPrimaryObjects.get(0));
            for (int i = 1; i < abstractPrimaryObjects.size(); i++) {
                final AbstractPrimaryObject currentAbstractPrimaryObject = abstractPrimaryObjects.get(i);
                if (!currentAbstractPrimaryObject.getName().equals(abstractPrimaryObjects.get(i - 1).getName())) {
                    noDuplicates.add(currentAbstractPrimaryObject);
                }
            }
            Files.createDirectories(Paths.get(getPathDirectory()));
            final FileWriter FileWriter = new FileWriter(getPathFile());
            for (final AbstractPrimaryObject currentPrimaryObject : noDuplicates) {
                currentPrimaryObject.writeCzmlBlock();
            }
            FileWriter.write(writer.toString() + "\n]");
            FileWriter.close();
        }
    }

    public String getPathDirectory() {
        return pathDirectory;
    }

    public String getPathFile() {
        return pathFile;
    }

    public void addObject(final AbstractPrimaryObject object) {
        abstractPrimaryObjects.add(object);
    }

//    public void addObject(final CzmlPrimaryObject object) {
//        czmlObjects.add(object);
//    }


    public static void setHeader(final Header header) {
        CzmlFile.header = header;
    }

    public void clear() {
        this.pathDirectory = null;
        this.pathFile = null;
    }
}
