package org.example.CZMLAbstract;

import java.io.*;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

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

        System.out.println("Running in: " + whereToRun);
        System.out.println("Command: " + command);

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
                System.out.println(line);
            }
        }
    }
}
