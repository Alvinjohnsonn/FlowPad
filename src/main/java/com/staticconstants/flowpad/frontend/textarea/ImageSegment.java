package com.staticconstants.flowpad.frontend.textarea;

import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javax.imageio.ImageIO;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import javafx.embed.swing.SwingFXUtils;


public final class ImageSegment implements RichSegment {
    private final Image image;

    public Image getImage(){
        return image;
    }
    public ImageSegment(Image image) {
        this.image = image;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ImageSegment other && image.equals(other.image);
    }

    @Override
    public int hashCode() {
        return image.hashCode();
    }

    @Override
    public int length() {
        return 1;
    }

    @Override
    public Node createNode(TextStyle style) {
        ImageView view = new ImageView(getImage());
        view.setFitWidth(200);
        view.setPreserveRatio(true);
        return view;
    }

    public String toBase64() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        RenderedImage renderedImage = SwingFXUtils.fromFXImage(image, null);
        ImageIO.write(renderedImage, "png", baos); // PNG preserves transparency
        baos.flush();
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    public static ImageSegment fromBase64(String base64) {
        byte[] bytes = Base64.getDecoder().decode(base64);
        InputStream in = new ByteArrayInputStream(bytes);
        return new ImageSegment(new Image(in));
    }

}
