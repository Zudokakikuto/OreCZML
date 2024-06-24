package org.example.Inputs.InputFiles;

import org.example.Inputs.InputObjet;

import java.io.IOException;

public interface InputFileBuilder<T> extends InputObjet {
    T read(String path) throws IOException;
    void close() throws IOException;
    String getPath();
}
