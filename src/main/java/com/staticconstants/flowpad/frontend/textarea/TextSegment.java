package com.staticconstants.flowpad.frontend.textarea;

import javafx.scene.Node;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import org.fxmisc.richtext.TextExt;

/**
 * A {@link RichSegment} implementation that represents a plain text segment.
 * <p>
 * This class stores a string of text and provides functionality to render it
 * using a JavaFX {@link TextExt} node, styled with a given {@link TextStyle}.
 * </p>
 */
public final class TextSegment implements RichSegment {

    private final String text;

    /**
     * Constructs a {@code TextSegment} with the specified text.
     *
     * @param text the text content of this segment
     */
    public TextSegment(String text) {
        this.text = text;
    }

    /**
     * Returns the raw text of this segment.
     *
     * @return the text content
     */
    public String getText() {
        return text;
    }

    /**
     * Returns a new {@code TextSegment} containing a subsequence of the current text.
     *
     * @param start the start index, inclusive
     * @param end   the end index, exclusive
     * @return a new {@code TextSegment} containing the specified range
     */
    public TextSegment subSequence(int start, int end) {
        return new TextSegment(text.substring(start, end));
    }

    /**
     * Returns the number of characters in this text segment.
     *
     * @return the length of the text
     */
    @Override
    public int length() {
        return text.length();
    }

    /**
     * Returns a {@link TextExt} node that visually represents this text segment
     * using the specified {@link TextStyle}.
     *
     * @param style the text style to apply
     * @return a styled JavaFX node
     */
    @Override
    public Node createNode(TextStyle style) {
        TextExt text = new TextExt(getText());
        String fontFamily = style.getFontFamily();

        int fontSize = switch (style.getHeadingLevel()) {
            case 1 -> 28;
            case 2 -> 24;
            case 3 -> 20;
            case 4 -> 16;
            case 5 -> 14;
            default -> style.getFontSize();
        };

        FontWeight weight = (style.isBold() || style.getHeadingLevel() > 0)
                ? FontWeight.BOLD : FontWeight.NORMAL;
        FontPosture posture = style.isItalic()
                ? FontPosture.ITALIC : FontPosture.REGULAR;
        Font font = Font.font(fontFamily, weight, posture, fontSize);
        text.setFont(font);
        text.setUnderline(style.isUnderline());
        text.setBackgroundColor(style.getBackgroundColor());
        text.setFill(style.getTextColor());

        return text;
    }

    /**
     * Returns a string representation of this segment.
     *
     * @return the text content
     */
    @Override
    public String toString() {
        return text;
    }

    /**
     * Compares this segment to another object for equality.
     *
     * @param obj the object to compare to
     * @return {@code true} if the other object is a {@code TextSegment} with equal text
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof TextSegment other && text.equals(other.text);
    }

    /**
     * Returns a hash code based on the text content.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return text.hashCode();
    }
}
