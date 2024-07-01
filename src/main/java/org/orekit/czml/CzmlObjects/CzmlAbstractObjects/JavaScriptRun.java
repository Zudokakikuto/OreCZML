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
package org.orekit.czml.CzmlObjects.CzmlAbstractObjects;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/** Javascript Run.

 * <p>
 *     This class allows the library to run a javascript run in java.
 * </p>
 *
 * <p>
 *     In order to launch a localhost server to run Cesium JS, this library allows the user to launch a javascript run
 * </p>
 *
 * <p> Still under development </p>
 *
 * @since 1.0
 * @author Julien LEBLOND*/

public class JavaScriptRun {

    /** .*/
    private final String pathToJavaScript;
    /** .*/
    private File navigatePath;
    /** .*/
    private final boolean is_Windows;

    // Builders

    public JavaScriptRun(final String pathToFile) throws Exception {

        this.is_Windows = System.getProperty("os.name").toLowerCase().startsWith("windows");
        this.pathToJavaScript = pathToFile;
        this.run();

    }

    public void run() throws Exception {
        // Where we want to execute
        final File location = new File(pathToJavaScript);

        runCommand(location, "ls");

        runCommand(location, "nvs");

        final Scanner scan = new Scanner(System.in);
        final String toInput = scan.nextLine();
        runCommand(location, "npm start");

    }

    private void runCommand(final File whereToRun, final String command) throws Exception {

        final ProcessBuilder builder = new ProcessBuilder();
        builder.directory(whereToRun);

        if (this.is_Windows) {
            builder.command("cmd.exe", "/c", command);
        }
        else {
            builder.command("sh", "-c", command);
        }

        final Process process = builder.start();

        final OutputStream outputStream = process.getOutputStream();
        final InputStream inputStream = process.getInputStream();
        final InputStream errorStream = process.getErrorStream();

        printStream(inputStream);
        printStream(errorStream);

        final boolean isFinished = process.waitFor(30, TimeUnit.SECONDS);
        outputStream.flush();
        outputStream.close();

        if (!isFinished) {
            process.destroyForcibly();
        }
    }

    private static void printStream(final InputStream inputStream) throws IOException {
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
            }
        }
    }
}
