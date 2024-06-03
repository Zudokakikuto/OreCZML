package org.example.CZMLAbstract;

import cesiumlanguagewriter.*;
import org.example.CZMLObjects.CZMLPrimaryObjects.CZMLPrimaryObject;
import org.example.CZMLObjects.CZMLPrimaryObjects.Header;
import org.orekit.time.TimeInterpolator;

import java.awt.image.RenderedImage;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

public class Model3D{

    private URI uri;
    private double scale;
    private double minimumPixelSize;
    private double maximumScale;
    private boolean show;
    private TimeInterval availability;

    public Model3D(URI uri, TimeInterval availability, double scale, double maximumScale, double minimumPixelSize, boolean show){
        this.uri = uri;
        this.scale = scale;
        this.maximumScale = maximumScale;
        this.minimumPixelSize = minimumPixelSize;
        this.show = show;
        this.availability = availability;
    }

    public Model3D(String path3DFile, Header header) throws URISyntaxException {
        this.availability = header.getClock().getInterval();
        this.uri= new URI(path3DFile);
        this.show = true;
        this.minimumPixelSize = 1000;
        this.maximumScale = 50;
    }

    public void generateCZML(PacketCesiumWriter packet, CesiumOutputStream output) {
        try(ModelCesiumWriter modelWriter = packet.getModelWriter()){
            modelWriter.open(output);
            CesiumResourceBehavior cesiumResourceBehavior = CesiumResourceBehavior.LINK_TO;
            CesiumResource cesiumResource = new CesiumResource(uri,cesiumResourceBehavior);
            modelWriter.writeGltfProperty(cesiumResource);
            modelWriter.writeMaximumScaleProperty(maximumScale);
            modelWriter.writeMinimumPixelSizeProperty(minimumPixelSize);
        }
    }
}
