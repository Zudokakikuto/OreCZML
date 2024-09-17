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
package org.orekit.czml.object.nonvisual;

import cesiumlanguagewriter.CesiumOutputStream;
import cesiumlanguagewriter.CesiumResource;
import cesiumlanguagewriter.CesiumResourceBehavior;
import cesiumlanguagewriter.ModelCesiumWriter;
import cesiumlanguagewriter.NearFarScalar;
import cesiumlanguagewriter.PacketCesiumWriter;
import cesiumlanguagewriter.TimeInterval;
import org.orekit.czml.errors.OreCzmlException;
import org.orekit.czml.errors.OreCzmlMessages;
import org.orekit.czml.object.ModelType;
import org.orekit.czml.object.primary.Header;
import org.orekit.czml.object.secondary.Billboard;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * 3D/2D Models
 *
 * <p> This class describes the 3D and 2D models that will be loaded into the CZML file. </p>
 *
 * <p> 3D and 2D models can only be loaded with a local reference/path so the program need to copy the file inputted. This way
 * it will be able to reference a local path.</p>
 *
 * @author Julien LEBLOND
 * @since 1.0.0
 */

public class CzmlModel {
    /** The default string to reference in local. */
    public static final String DEFAULT_SLASH_LOCAL = "./";

    /** . */
    public static final String DEFAULT_MODEL_NAME = "Default3DModels/satellite.png";

    /** A boolean to show or not the model. */
    private final boolean show;

    /** The URI of the model. */
    private URI uri;

    /** The scale of the model, change the width/height for 2D or all the geometrical parameters for 3D. */
    private double scale;

    /** The minimum pixel size of the display. */
    private double minimumPixelSize;

    /** The maximum pixel size of the display. */
    private double maximumScale;

    /** The time interval when the model is displayed. */
    private TimeInterval availability;

    /** The absolute path of the file for 2D or 3D model. */
    private String absolutePath;

    /** The relative path of the file created in : OreCZML/JavaScript/public/. */
    private String relativePath;

    /** The name of the object loaded. */
    private String nameOfObject;

    /** The type of the mode, either 2D or 3D. */
    private ModelType modelType;

    /** Used only for 2D models, a billboard to display the 2D image. */
    private Billboard billboard;

    /** The string representing the extension of the file used. This is used to determine the type of the file. */
    private String extension;

    /** The duplicated file in local at the relative path. */
    private File duplicatedLocalFile;

    /**
     * Builder for the model of the satellite, default parameters entered.
     *
     * @param absolutePathToObject : The string leading to the absolute path of the object
     */
    public CzmlModel(final String absolutePathToObject) throws URISyntaxException, IOException {
        this(absolutePathToObject, 5000000, 400, 1);
    }

    /**
     * This builder builds the model object with the absolute path of the file given by the user.
     *
     * @param absolutePathToObject  : The string leading to the absolute path of the object
     * @param maximumScale          : The minimum scale for the object
     * @param minimumPixelSizeInput : The minimum of pixels display for the object
     * @param scale                 : The scale of the 3D model
     */
    public CzmlModel(final String absolutePathToObject, final double maximumScale, final double minimumPixelSizeInput,
                     final double scale) throws URISyntaxException, IOException {
        this.modelType = getModelTypeFromString(absolutePathToObject);

        if (this.modelType == ModelType.MODEL_3D) {
            this.absolutePath = absolutePathToObject;
            this.duplicateFile(absolutePathToObject);
            this.availability     = Header.getMasterClock()
                                          .getAvailability();
            this.uri              = new URI(DEFAULT_SLASH_LOCAL + nameOfObject);
            this.show             = true;
            this.minimumPixelSize = minimumPixelSizeInput;
            this.maximumScale     = maximumScale;
            this.scale            = scale;
        } else if (this.modelType == ModelType.MODEL_2D) {
            this.absolutePath = absolutePathToObject;
            duplicateFile(absolutePathToObject);
            final BufferedImage image         = ImageIO.read(duplicatedLocalFile);
            final int           height        = image.getHeight();
            final NearFarScalar nearFarScalar = new NearFarScalar(1, (double) 80 / height, 1e9, (double) 80 / height);
            this.uri          = new URI(DEFAULT_SLASH_LOCAL + nameOfObject);
            this.billboard    = new Billboard(uri.toString(), nearFarScalar);
            this.availability = Header.getMasterClock()
                                      .getAvailability();
            this.show         = true;
        } else {
            this.absolutePath = getSatelliteResourcePath();
            this.availability = Header.getMasterClock()
                                      .getAvailability();
            duplicateFile(absolutePath);
            final BufferedImage image         = ImageIO.read(duplicatedLocalFile);
            final int           height        = image.getHeight();
            final NearFarScalar nearFarScalar = new NearFarScalar(1, (double) 80 / height, 1e9, (double) 80 / height);
            this.uri       = new URI(DEFAULT_SLASH_LOCAL + nameOfObject);
            this.billboard = new Billboard(uri.toString(), nearFarScalar);
            this.show      = true;
        }
    }

    /**
     * The generation function for the CZML file for models.
     *
     * @param packet : The packet where the model will be written.
     * @param output : The output that will write the strings.
     */
    public void generateCZML(final PacketCesiumWriter packet, final CesiumOutputStream output) {
        if (modelType == ModelType.MODEL_3D) {
            try (ModelCesiumWriter modelWriter = packet.getModelWriter()) {
                modelWriter.open(output);
                final CesiumResourceBehavior cesiumResourceBehavior = CesiumResourceBehavior.LINK_TO;
                final CesiumResource cesiumResource = new CesiumResource(getUri(),
                        cesiumResourceBehavior);
                modelWriter.writeGltfProperty(cesiumResource);
                modelWriter.writeScaleProperty(getScale());
                modelWriter.writeMaximumScaleProperty(getMaximumScale());
                modelWriter.writeMinimumPixelSizeProperty(getMinimumPixelSize());
                modelWriter.writeIncrementallyLoadTexturesProperty(true);
                modelWriter.writeShowProperty(true);
            }
        } else if (modelType == ModelType.MODEL_2D) {
            this.getBillboard()
                .write(packet, output);
        } else if (modelType == ModelType.EMPTY_MODEL) {
            this.getBillboard()
                .write(packet, output);
        } else {
            throw new OreCzmlException(OreCzmlMessages.MODEL_TYPE_UNKNOWN);
        }
    }

    /**
     * This getter returns the billboard of the model if one exists.
     *
     * @return : The billboard used.
     */
    public Billboard getBillboard() {
        return billboard;
    }

    /**
     * This getter returns the model type of the model.
     *
     * @return : The model type of the object.
     */
    public ModelType getModelType() {
        return modelType;
    }

    /**
     * This getter returns the billboard of the model.
     *
     * @return : The absolute path of the object.
     */
    public String getAbsolutePath() {
        return absolutePath;
    }

    /**
     * This getter returns the name of the object.
     *
     * @return : The name of the object.
     */
    public String getNameOfObject() {
        return nameOfObject;
    }

    /**
     * This getter returns the availability of the packet.
     *
     * @return : The availability used.
     */
    public TimeInterval getAvailability() {
        return availability;
    }

    /**
     * This getter returns the relative path of the object.
     *
     * @return : The relative path used.
     */
    public String getRelativePath() {
        return relativePath;
    }

    public double getMaximumScale() {
        return maximumScale;
    }

    public double getScale() {
        return scale;
    }

    public double getMinimumPixelSize() {
        return minimumPixelSize;
    }

    public String getExtension() {
        return extension;
    }

    public URI getUri() {
        return uri;
    }

    public boolean isShow() {
        return show;
    }

    public File getDuplicatedLocalFile() {
        return duplicatedLocalFile;
    }

    /**
     * CZML writer only understands a relative path, so we will need to duplicate the file to use it to write and then delete it.
     *
     * @param absolutePathInputted : The absolute Path of the object
     */
    private void duplicateFile(final String absolutePathInputted) throws IOException {

        this.nameOfObject = getNameOfObjectFromPath(absolutePathInputted);

        final String Javascript = Header.DEFAULT_ROOT + "/JavaScript";
        // The file needs to be in the public of the Javascript file to be read by the local cesiumJS
        final String JavaScriptOutput = Javascript + "/public";
        this.relativePath = JavaScriptOutput + "/" + nameOfObject;

        final File absoluteFile = new File(absolutePath);
        final File relativeFile = new File(relativePath);
        this.duplicatedLocalFile = relativeFile;
        final Path absolutePathOfFile = absoluteFile.getAbsoluteFile()
                                                    .toPath();
        final Path relativePathOfFile = relativeFile.toPath();
        Files.copy(absolutePathOfFile, relativePathOfFile, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * This function returns the type of the model from an absolutePath of the file.
     *
     * @param absolutePathOfObject : The absolute path of the model.
     * @return : The model type of the file.
     */
    private ModelType getModelTypeFromString(final String absolutePathOfObject) {
        if (absolutePathOfObject.isEmpty()) {
            return ModelType.EMPTY_MODEL;
        }
        final String   name         = getNameOfObjectFromPath(absolutePathOfObject);
        final String[] nameSplitted = name.split("\\.");
        this.extension = nameSplitted[1];
        return checkFromExtension(extension);
    }

    /**
     * This function returns the name of the object from the absolute path of the file.
     *
     * @param absolutePathInputted :
     * @return : The string of the name of the file.
     */
    private String getNameOfObjectFromPath(final String absolutePathInputted) {
        final String   replacedPath         = absolutePathInputted.replace("\\", "/");
        final String[] absolutePathSplitted = replacedPath.split("/");
        return absolutePathSplitted[absolutePathSplitted.length - 1];
    }

    /**
     * This function returns the type of model from the extension of the file.
     *
     * @param extensionInput : The string representing the extension of the file
     * @return : The model type of the file.
     */
    private ModelType checkFromExtension(final String extensionInput) {
        // 3D Models supported
        if (extensionInput.equals("3ds") || extensionInput.equals("3mf") || extensionInput.equals(
                "dae") || extensionInput.equals("fbx") ||
                extensionInput.equals("glb") || extensionInput.equals("max") || extensionInput.equals(
                "obj") || extensionInput.equals("skp") ||
                extensionInput.equals("stl") || extensionInput.equals("stp") || extensionInput.equals(
                "vrml") || extensionInput.equals("x3d")) {
            return ModelType.MODEL_3D;
        } else if (extensionInput.equals("avif") || extensionInput.equals("bpm") || extensionInput.equals(
                "cgm") || extensionInput.equals("gif") ||
                extensionInput.equals("heif") || extensionInput.equals("jpeg") || extensionInput.equals(
                "png") || extensionInput.equals("svg") ||
                extensionInput.equals("tiff") || extensionInput.equals("webp")) {
            return ModelType.MODEL_2D;
        } else {
            throw new OreCzmlException(OreCzmlMessages.MODEL_EXTENSION_UNKNOWN);
        }
    }

    /**
     * Gets default satellite resource path
     *
     * @return default satellite resource path
     */
    private static String getSatelliteResourcePath() {
        return CzmlModel.class.getClassLoader()
                              .getResource(DEFAULT_MODEL_NAME)
                              .getPath()
                              .toString();
    }
}
