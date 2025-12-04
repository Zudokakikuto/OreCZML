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

package org.orekit.czml.object.nonvisual;

import cesiumlanguagewriter.CesiumOutputStream;
import cesiumlanguagewriter.CesiumResource;
import cesiumlanguagewriter.CesiumResourceBehavior;
import cesiumlanguagewriter.CesiumStreamWriter;
import cesiumlanguagewriter.ModelCesiumWriter;
import cesiumlanguagewriter.NearFarScalar;
import cesiumlanguagewriter.PacketCesiumWriter;
import org.orekit.czml.errors.OreCzmlException;
import org.orekit.czml.errors.OreCzmlMessages;
import org.orekit.czml.object.ModelType;
import org.orekit.czml.object.primary.Header;
import org.orekit.czml.object.secondary.Billboard;
import org.orekit.czml.object.secondary.Clock;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

/**
 * 3D/2D Models
 * <p>
 * This class describes the 3D and 2D models that will be loaded into the CZML
 * file.
 * </p>
 * <p>
 * 3D and 2D models can only be loaded with a local reference/path so the
 * program need to copy the file inputted. This way it will be able to reference
 * a local path.
 * </p>
 *
 * @author Julien LEBLOND
 * @since 1.0.0
 */
public class CzmlModel {

    /**
     * The default string to reference in local.
     */
    public static final String DEFAULT_SLASH_LOCAL = "./";

    /**
     * .
     */
    public static final String DEFAULT_MODEL_NAME =
        "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAADsMAAA7DAcdvqGQAAADJSURBVDhPnZHRDcMgEEMZjVEYpaNklIzSEfLfD4qNnXAJSFWfhO7w2Zc0Tf9QG2rXrEzSUeZLOGm47WoH95x3Hl3jEgilvDgsOQUTqsNl68ezEwn1vae6lceSEEYvvWNT/Rxc4CXQNGadho1NXoJ+9iaqc2xi2xbt23PJCDIB6TQjOC6Bho/sDy3fBQT8PrVhibU7yBFcEPaRxOoeTwbwByCOYf9VGp1BYI1BA+EeHhmfzKbBoJEQwn1yzUZtyspIQUha85MpkNIXB7GizqDEECsAAAAASUVORK5CYII=";

    /**
     * A boolean to show or not the model.
     */
    private boolean show;

    /**
     * The URI of the model.
     */
    private URI uri;

    /**
     * The scale of the model, change the width/height for 2D or all the
     * geometrical parameters for 3D.
     */
    private double scale;

    /**
     * The minimum pixel size of the display.
     */
    private double minimumPixelSize;

    /**
     * The maximum pixel size of the display.
     */
    private double maximumScale;

    /**
     * The time interval when the model is displayed.
     */
    private final Clock clock;

    /**
     * The absolute path of the file for 2D or 3D model.
     */
    private final String absolutePath;

    /**
     * The name of the object loaded.
     */
    private String nameOfObject;

    /**
     * The type of the mode, either 2D or 3D.
     */
    private final ModelType modelType;

    /**
     * Used only for 2D models, a billboard to display the 2D image.
     */
    private Billboard billboard;

    /**
     * The duplicated file in local at the relative path.
     */
    private File duplicatedLocalFile;

    /** Check if the model is for a satellite. */
    private final boolean isSatellite;

    /**
     * Builder for the model of the satellite, default parameters entered.
     *
     * @param absolutePathToModel : The string leading to the absolute path of
     *        the object
     * @param isSatelliteInput : Is the mode loaded for a satellite?
     * @param clock : The availability of the model
     */
    public CzmlModel(final String absolutePathToModel,
                     final boolean isSatelliteInput, final Clock clock) {
        this(absolutePathToModel, 5000000, 400, 1, isSatelliteInput, clock);
    }

    /**
     * This builder builds the model object with the absolute path of the file
     * given by the user.
     *
     * @param absolutePathToModel : The string leading to the absolute path of
     *        the object
     * @param maximumScale : The minimum scale for the object
     * @param minimumPixelSizeInput : The minimum of pixel displayed for the
     *        object
     * @param scale : The scale of the 3D model
     * @param isSatelliteInput : Is the model loaded for a satellite
     * @param clockInput : The clock for the model
     */
    public CzmlModel(final String absolutePathToModel,
                     final double maximumScale,
                     final double minimumPixelSizeInput, final double scale,
                     final boolean isSatelliteInput, final Clock clockInput) {

        this.isSatellite = isSatelliteInput;
        this.modelType = getModelTypeFromString(absolutePathToModel);

        if (this.modelType == ModelType.MODEL_3D) {
            this.absolutePath = absolutePathToModel;
            this.clock = clockInput;
            this.show = true;
            this.minimumPixelSize = minimumPixelSizeInput;
            this.maximumScale = maximumScale;
            this.scale = scale;
        } else if (this.modelType == ModelType.MODEL_2D) {
            this.absolutePath = absolutePathToModel;
            this.clock = clockInput;
            this.show = true;
        } else {
            if (isSatellite) {
                this.absolutePath = getSatelliteResourcePath();
                this.clock = clockInput;
                this.show = true;
            } else {
                this.absolutePath = "";
                this.clock = clockInput;
                this.show = false;
            }
        }
    }

    // Static Builders

    /**
     * The builder of the Czml Model.
     *
     * @param pathToModelInput : The path to the czml model
     * @param isSpacecraftInput : Is the model representing a spacecraft
     * @param clockInput : The clock considered
     * @return : The Czml model builder
     */
    public static CzmlModelBuilder builder(final String pathToModelInput,
                                           final boolean isSpacecraftInput,
                                           final Clock clockInput) {
        return new CzmlModelBuilder(pathToModelInput, isSpacecraftInput,
                                    clockInput);
    }

    /**
     * The generation function for the CZML file for models.
     *
     * @param packet : The packet where the model will be written.
     * @param output : The output that will write the strings.
     * @throws IOException the io exception
     * @throws URISyntaxException the uri syntax exception
     */
    public void generateCZML(final PacketCesiumWriter packet,
                             final CesiumOutputStream output)
        throws IOException,
            URISyntaxException {

        this.duplicateFile(absolutePath);

        if (modelType == ModelType.MODEL_3D) {

            generate3DModel(packet, output);

        } else if (modelType == ModelType.MODEL_2D) {

            write2D(packet, output);

        } else if (modelType == ModelType.EMPTY_MODEL) {

            if (isSatellite) {
                writeEmpty(packet, output);
            }

        } else {
            throw new OreCzmlException(OreCzmlMessages.MODEL_TYPE_UNKNOWN);
        }
    }

    @Override
    public String toString() {
        final StringWriter writer = new StringWriter();
        final CesiumOutputStream output = new CesiumOutputStream(writer);
        final CesiumStreamWriter streamWriter = new CesiumStreamWriter();
        output.setPrettyFormatting(true);

        try (PacketCesiumWriter packet = streamWriter.openPacket(output)) {
            this.generateCZML(packet, output);
        } catch (URISyntaxException | IOException e) {
            throw new OreCzmlException(OreCzmlMessages.STRING_NOT_GENERATED);
        }
        return writer.toString();
    }

    // Getters

    /**
     * This getter returns the billboard of the model if one exists.
     *
     * @return : The billboard used.
     */
    public Billboard getBillboard() {
        return billboard == null ? null : billboard;
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
     * This getter returns the availability of the packet.
     *
     * @return : The availability used.
     */
    public Clock getClock() {
        return clock;
    }

    /**
     * Gets maximum scale.
     *
     * @return the maximum scale
     */
    public double getMaximumScale() {
        return maximumScale;
    }

    /**
     * Get the absolute path of the model.
     *
     * @return The absolute path of the model.
     */
    public String getAbsolutePath() {
        return absolutePath;
    }

    /**
     * Gets scale.
     *
     * @return the scale
     */
    public double getScale() {
        return scale;
    }

    /**
     * Gets minimum pixel size.
     *
     * @return the minimum pixel size
     */
    public double getMinimumPixelSize() {
        return minimumPixelSize;
    }

    /**
     * Gets uri.
     *
     * @return the uri
     */
    public URI getUri() {
        return uri;
    }

    /**
     * Is show boolean.
     *
     * @return the boolean
     */
    public boolean isShow() {
        return show;
    }

    // Setters

    /**
     * Sets the show.
     *
     * @param show To show or not the model
     */
    public void setShow(final boolean show) {
        this.show = show;
    }

    /**
     * Sets the scale.
     *
     * @param scale : The scale to set.
     */
    public void setScale(final double scale) {
        this.scale = scale;
    }

    /**
     * Sets the minimum pixel size.
     *
     * @param minimumPixelSize : The minimum pixel size to set.
     */
    public void setMinimumPixelSize(final double minimumPixelSize) {
        this.minimumPixelSize = minimumPixelSize;
    }

    /**
     * Sets the maximum scale.
     *
     * @param maximumScale : The maximum scale to set.
     */
    public void setMaximumScale(final double maximumScale) {
        this.maximumScale = maximumScale;
    }

    /**
     * Sets the name of the object.
     *
     * @param nameOfObject : The name of the object.
     */
    public void setNameOfObject(final String nameOfObject) {
        this.nameOfObject = nameOfObject;
    }

    /**
     * CZML writer only understands a relative path, so we will need to
     * duplicate the file to use it to write and then delete it.
     *
     * @param absolutePathInputted : The absolute Path of the object
     */
    private void duplicateFile(final String absolutePathInputted)
        throws IOException {

        if (!(absolutePathInputted == null)) {
            final File inputtedFile = new File(absolutePathInputted);
            this.nameOfObject = inputtedFile.getName();

            // If you use cesiumJS, the file needs to be in the public folder of
            // the JavaScript file to be read by the local cesiumJS.
            // Else way, put the path to the resource folder that you are using.
            final String Javascript = Header.getPathToExternalResourceFolder();
            if (Javascript.isEmpty()) {
                this.duplicatedLocalFile = new File(absolutePathInputted);
                return;
            }
            final String relativePath = Javascript + "/" + nameOfObject;

            final File absoluteFile = new File(absolutePath);
            final File relativeFile = new File(relativePath);
            this.duplicatedLocalFile = relativeFile;
            final Path absolutePathOfFile =
                absoluteFile.getAbsoluteFile().toPath();
            final Path relativePathOfFile = relativeFile.toPath();
            Files.copy(absolutePathOfFile, relativePathOfFile,
                       StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * This function returns the type of the model from an absolutePath of the
     * file.
     *
     * @param absolutePathOfObject : The absolute path of the model.
     * @return : The model type of the file.
     */
    private ModelType
        getModelTypeFromString(final String absolutePathOfObject) {
        if (absolutePathOfObject.isEmpty()) {
            return ModelType.EMPTY_MODEL;
        }
        final File inputtedFile = new File(absolutePathOfObject);
        final String name = inputtedFile.getName();
        final String[] nameSplitted = name.split("\\.");
        final String extension = nameSplitted[1];
        return checkFromExtension(extension);
    }

    /**
     * This function returns the type of model from the extension of the file.
     *
     * @param extensionInput : The string representing the extension of the file
     * @return : The model type of the file.
     */
    private ModelType checkFromExtension(final String extensionInput) {
        // 3D Models supported
        if (extensionInput.equals("3ds") ||
            extensionInput.equals("3mf") || extensionInput.equals("dae") ||
            extensionInput.equals("fbx") || extensionInput.equals("glb") ||
            extensionInput.equals("max") || extensionInput.equals("obj") ||
            extensionInput.equals("skp") || extensionInput.equals("stl") ||
            extensionInput.equals("stp") || extensionInput.equals("vrml") ||
            extensionInput.equals("x3d")) {
            return ModelType.MODEL_3D;
        } else if (extensionInput.equals("avif") ||
                   extensionInput.equals("bpm") ||
                   extensionInput.equals("cgm") ||
                   extensionInput.equals("gif") ||
                   extensionInput.equals("heif") ||
                   extensionInput.equals("jpeg") ||
                   extensionInput.equals("png") ||
                   extensionInput.equals("svg") ||
                   extensionInput.equals("tiff") ||
                   extensionInput.equals("webp")) {
            return ModelType.MODEL_2D;
        } else {
            throw new OreCzmlException(OreCzmlMessages.MODEL_EXTENSION_UNKNOWN);
        }
    }

    /**
     * Gets default satellite resource path.
     *
     * @return default satellite resource path
     */
    private static String getSatelliteResourcePath() {
        if (!(CzmlModel.class.getClassLoader()
            .getResource(DEFAULT_MODEL_NAME) == null)) {
            return Objects.requireNonNull(CzmlModel.class.getClassLoader()
                .getResource(DEFAULT_MODEL_NAME)).getPath();
        }
        return null;
    }

    /**
     * Write a 3D model into a packet.
     *
     * @param packet : The packet where the model will be written.
     * @param output : The output that will write the strings.
     */
    private void generate3DModel(final PacketCesiumWriter packet,
                                 final CesiumOutputStream output)
        throws URISyntaxException {
        this.uri = new URI(DEFAULT_SLASH_LOCAL + nameOfObject);
        try (ModelCesiumWriter modelWriter = packet.getModelWriter()) {
            modelWriter.open(output);
            final CesiumResourceBehavior cesiumResourceBehavior =
                CesiumResourceBehavior.LINK_TO;
            final CesiumResource cesiumResource =
                new CesiumResource(getUri(), cesiumResourceBehavior);
            modelWriter.writeGltfProperty(cesiumResource);
            modelWriter.writeScaleProperty(getScale());
            modelWriter.writeMaximumScaleProperty(getMaximumScale());
            modelWriter.writeMinimumPixelSizeProperty(getMinimumPixelSize());
            modelWriter.writeIncrementallyLoadTexturesProperty(true);
            modelWriter.writeShowProperty(true);
        }
    }

    /**
     * This function aims at writing a billboard using a 2D model.
     *
     * @param packet : The packet where the model will be written.
     * @param output : The output that will write the strings.
     */
    private void write2D(final PacketCesiumWriter packet,
                         final CesiumOutputStream output)
        throws IOException,
            URISyntaxException {
        if (show) {
            final BufferedImage image = ImageIO.read(duplicatedLocalFile);
            final int height = image.getHeight();
            final NearFarScalar nearFarScalar =
                new NearFarScalar(1, (double) 80 / height, 1e9,
                                  (double) 80 / height);
            this.uri = new URI(DEFAULT_SLASH_LOCAL + nameOfObject);
            this.billboard = new Billboard(uri.toString(), nearFarScalar);
            this.billboard.write(packet, output);
        }
    }

    /**
     * This function aims at writing a billboard with an empty model.
     *
     * @param packet : The packet where the model will be written.
     * @param output : The output that will write the strings.
     */
    private void writeEmpty(final PacketCesiumWriter packet,
                            final CesiumOutputStream output) {
        this.billboard = new Billboard(DEFAULT_MODEL_NAME);
        this.billboard.write(packet, output);
    }
}
