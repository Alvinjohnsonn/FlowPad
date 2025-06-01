package com.staticconstants.flowpad.frontend.textarea;

import javafx.scene.Node;

/**
 * Represents a rich content segment that can be rendered within a custom styled text area.
 * <p>
 * A {@code RichSegment} defines the basic contract for various types of segments, such as
 * {@link TextSegment}, {@link ImageSegment}, and {@link HyperlinkSegment}, which are permitted
 * to implement this sealed interface.
 * </p>
 */
public sealed interface RichSegment permits TextSegment, ImageSegment, HyperlinkSegment {

    /**
     * Returns the length of this segment in terms of content units.
     * <p>
     * For text-based segments, this typically corresponds to the number of characters.
     * For non-text segments such as images, this may return 1 to indicate a single unit.
     *
     * @return the logical length of the segment
     */
    int length();

    /**
     * Creates a JavaFX {@link Node} to visually represent this segment using the given text style.
     *
     * @param style the {@link TextStyle} to apply when rendering the node
     * @return a JavaFX {@link Node} representing this segment
     */
    Node createNode(TextStyle style);
}
