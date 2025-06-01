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

/**
 * A {@link RichSegment} implementation that represents an embedded image.
 * <p>
 * This segment wraps a JavaFX {@link Image} and can render it as an {@link ImageView}
 * node. It supports serialization to and from a Base64-encoded PNG string.
 * </p>
 */
public final class ImageSegment implements RichSegment {
    private final Image image;

    /**
     * Constructs a new {@code ImageSegment} using the given JavaFX {@link Image}.
     *
     * @param image the image to embed in this segment
     */
    public ImageSegment(Image image) {
        this.image = image;
    }

    /**
     * Returns the underlying JavaFX {@link Image} stored in this segment.
     *
     * @return the image
     */
    public Image getImage() {
        return image;
    }

    /**
     * Returns a constant length of 1, since an image is treated as a single unit.
     *
     * @return {@code 1}
     */
    @Override
    public int length() {
        return 1;
    }

    /**
     * Creates an {@link ImageView} node to visually render the image.
     * <p>
     * The image view is resized to a fixed width of 200 while preserving aspect ratio.
     *
     * @param style the text style (not used for images, but required by interface)
     * @return a JavaFX node displaying the image
     */
    @Override
    public Node createNode(TextStyle style) {
        ImageView view = new ImageView(getImage());
        view.setFitWidth(200);
        view.setPreserveRatio(true);
        return view;
    }

    /**
     * Serializes the image into a Base64-encoded PNG string.
     * <p>
     * This is useful for saving or transmitting image segments as plain text.
     *
     * @return a Base64-encoded string representing the PNG image
     * @throws IOException if an error occurs during image encoding
     */
    public String toBase64() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        RenderedImage renderedImage = SwingFXUtils.fromFXImage(image, null);
        ImageIO.write(renderedImage, "png", baos); // PNG preserves transparency
        baos.flush();
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    /**
     * Deserializes a Base64-encoded PNG string into a new {@code ImageSegment}.
     *
     * @param base64 the Base64 string representing an encoded PNG image
     * @return a new {@code ImageSegment} containing the decoded image
     */
    public static ImageSegment fromBase64(String base64) {
        byte[] bytes = Base64.getDecoder().decode(base64);
        InputStream in = new ByteArrayInputStream(bytes);
        return new ImageSegment(new Image(in));
    }

    /**
     * Compares this segment with another object for equality.
     *
     * @param obj the object to compare to
     * @return {@code true} if the object is an {@code ImageSegment} with the same image
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof ImageSegment other && image.equals(other.image);
    }

    /**
     * Returns a hash code based on the image content.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return image.hashCode();
    }
}
