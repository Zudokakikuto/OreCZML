package org.example.Outputs.OutputFiles;

import org.example.CZMLObjects.CZMLPrimaryObjects.CZMLPrimaryObject;
import org.example.Outputs.OutputFileBuilder;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

public class CZMLFile implements OutputFileBuilder{

    private static String pathFile;
    private static String pathDirectory;

    public CZMLFile(String pathDirectory,String pathFile){
        this.pathDirectory = pathDirectory;
        this.pathFile=pathFile;
    }

    public static void write(CZMLPrimaryObject CZMLobject) throws IOException {
        StringWriter writer = CZMLobject.getStringWriter();
        Files.createDirectories(Paths.get(pathDirectory));
        FileWriter FileWriter = new FileWriter(pathFile);
        FileWriter.write(writer.toString());
        FileWriter.close();
    }
}
