package com.staticconstants.flowpad.frontend.textareaclasses;

import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

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
}
