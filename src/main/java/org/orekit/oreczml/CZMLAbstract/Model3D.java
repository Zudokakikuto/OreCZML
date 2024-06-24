package org.example.CZMLAbstract;

import cesiumlanguagewriter.*;
import org.example.CZMLObjects.CZMLPrimaryObjects.Header;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class Model3D {
    /** .*/
    private URI uri;
    /** .*/
    private double scale;
    /** .*/
    private double minimumPixelSize;
    /** .*/
    private double maximumScale;
    /** .*/
    private boolean show;
    /** .*/
    private TimeInterval availability;
    /** .*/
    private String absolutePath;
    /** .*/
    private String relativePath;
    /** .*/
    private String nameOfObject;

    public Model3D(final URI uri, final TimeInterval availability, final double scale, final double maximumScale, final double minimumPixelSize, final boolean show) {
        this.uri = uri;
        this.scale = scale;
        this.maximumScale = maximumScale;
        this.minimumPixelSize = minimumPixelSize;
        this.show = show;
        this.availability = availability;
    }

    /** This builder builds the model object with the absolute path of the file given by the user.
     * @param absolutePathToObject : The string leading to the absolute path of the object
     * @param header : The header of the CZML file.*/
    public Model3D(final String absolutePathToObject, final Header header) throws URISyntaxException, IOException {
        this.absolutePath = absolutePathToObject;
        this.duplicateFile();
        this.availability = header.getClock().getAvailability();
        this.uri = new URI("./" + nameOfObject);
        this.show = true;
        this.minimumPixelSize = 400;
        this.maximumScale = 5000000;
    }

    public void generateCZML(final PacketCesiumWriter packet, final CesiumOutputStream output) {
        try (ModelCesiumWriter modelWriter = packet.getModelWriter()) {
            modelWriter.open(output);
            final CesiumResourceBehavior cesiumResourceBehavior = CesiumResourceBehavior.LINK_TO;
            final CesiumResource cesiumResource = new CesiumResource(uri, cesiumResourceBehavior);
            modelWriter.writeGltfProperty(cesiumResource);
            modelWriter.writeMaximumScaleProperty(maximumScale);
            modelWriter.writeMinimumPixelSizeProperty(minimumPixelSize);
        }
    }

    /** CZML writer only understands relative path, so we will need to duplicate the file to use it to write and then delete it.*/
    private void duplicateFile() throws IOException {

        final String replacedPath = absolutePath.replace("\\", "/");
        final String[] absolutePathSplitted = replacedPath.split("/");
        this.nameOfObject = absolutePathSplitted[absolutePathSplitted.length - 1];

        final String root = System.getProperty("user.dir").replace("\\", "/");
        final String Javascript = root + "/JavaScript";
        final String JavaScriptOutput = Javascript + "/public";
        this.relativePath = JavaScriptOutput + "/" + nameOfObject;

        final File absoluteFile = new File(absolutePath);
        final File relativeFile = new File(relativePath);
        final Path absolutePathOfFile = absoluteFile.getAbsoluteFile().toPath();
        final Path relativePathOfFile = relativeFile.toPath();
        Files.copy(absolutePathOfFile, relativePathOfFile, StandardCopyOption.REPLACE_EXISTING);
    }

}
