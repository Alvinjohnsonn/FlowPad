package com.staticconstants.flowpad.frontend.textarea;

import javafx.scene.Node;


public sealed interface RichSegment permits TextSegment, ImageSegment {

    int length();

    Node createNode(TextStyle style);
}
