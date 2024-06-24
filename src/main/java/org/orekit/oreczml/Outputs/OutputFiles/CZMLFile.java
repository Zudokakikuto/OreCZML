/** .*/

package org.example.Outputs.OutputFiles;

import org.example.CZMLObjects.CZMLPrimaryObjects.CZMLPrimaryObject;
import org.example.Outputs.OutputFileBuilder;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

public class CZMLFile implements OutputFileBuilder {

    /** .*/
    private static String pathFile = "";
    /** .*/
    private static String pathDirectory = "";

    public CZMLFile(final String pathDirectory, final String pathFile) {
        CZMLFile.pathDirectory = pathDirectory;
        CZMLFile.pathFile = pathFile;
    }

    public void write(final CZMLPrimaryObject CZMLObject) throws IOException {
        final StringWriter writer = CZMLObject.getStringWriter();
        Files.createDirectories(Paths.get(getPathDirectory()));
        final FileWriter FileWriter = new FileWriter(getPathFile());
        FileWriter.write(writer.toString());
        FileWriter.close();
    }

    public static String getPathDirectory() {
        return pathDirectory;
    }

    public static String getPathFile() {
        return pathFile;
    }

    public void clear() {
        CZMLFile.pathDirectory = null;
        CZMLFile.pathFile = null;
    }
}
