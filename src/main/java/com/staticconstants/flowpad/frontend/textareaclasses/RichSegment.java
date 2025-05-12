package com.staticconstants.flowpad.frontend.textareaclasses;

import javafx.scene.Node;
import javafx.scene.image.Image;


public sealed interface RichSegment permits TextSegment, ImageSegment {

    int length();

    Node createNode(TextStyle style);
}
