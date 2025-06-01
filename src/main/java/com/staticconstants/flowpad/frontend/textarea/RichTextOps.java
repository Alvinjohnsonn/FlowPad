package com.staticconstants.flowpad.frontend.textarea;

import org.fxmisc.richtext.model.*;

import java.util.Optional;

/**
 * A {@link TextOps} implementation that supports multiple segment types including
 * {@link TextSegment}, {@link HyperlinkSegment}, and {@link ImageSegment}, enabling
 * rich content manipulation in a {@code GenericStyledArea}.
 *
 * @param <R> the type of paragraph style (unused in this class but required by TextOps)
 * @param <T> the type of text style (typically {@link TextStyle})
 */
public class RichTextOps<R, T> implements TextOps<RichSegment, TextStyle> {

    private final TextOps<String, TextStyle> styledTextOps = SegmentOps.styledTextOps();

    /**
     * Returns the length of the given segment.
     * Text and hyperlink segments return their character count,
     * while image segments return 1 to represent a single embedded object.
     *
     * @param seg the segment
     * @return the length of the segment
     */
    @Override
    public int length(RichSegment seg) {
        if (seg instanceof TextSegment textSegment) {
            return styledTextOps.length(textSegment.getText());
        } else if (seg instanceof HyperlinkSegment hyperlinkSegment) {
            return hyperlinkSegment.length();
        } else if (seg instanceof ImageSegment) {
            return 1;
        }
        return 1;
    }

    /**
     * Returns the character at the specified index in the segment.
     * For image segments, a placeholder object replacement character is returned.
     *
     * @param seg the segment
     * @param index the character index
     * @return the character at the given index
     */
    @Override
    public char charAt(RichSegment seg, int index) {
        if (seg instanceof TextSegment textSegment) {
            return styledTextOps.charAt(textSegment.getText(), index);
        } else if (seg instanceof HyperlinkSegment hyperlinkSegment) {
            return hyperlinkSegment.getText().charAt(index);
        }
        return '\ufffc'; // Object replacement character for non-text
    }

    /**
     * Returns the textual content of the segment.
     * For image segments, returns a placeholder string.
     *
     * @param seg the segment
     * @return the string content of the segment
     */
    @Override
    public String getText(RichSegment seg) {
        if (seg instanceof TextSegment textSegment) {
            return textSegment.getText();
        } else if (seg instanceof HyperlinkSegment hyperlinkSegment) {
            return hyperlinkSegment.getText();
        }
        return "\ufffc"; // Placeholder for non-text segments
    }

    /**
     * Returns a subsequence of the segment starting from {@code start} to the end.
     *
     * @param seg the original segment
     * @param start the starting index
     * @return the resulting segment
     */
    @Override
    public RichSegment subSequence(RichSegment seg, int start) {
        if (seg instanceof TextSegment t) {
            return new TextSegment(styledTextOps.subSequence(t.getText(), start));
        } else if (seg instanceof HyperlinkSegment t) {
            return t.subSequence(start);
        }
        return seg;
    }

    /**
     * Returns a subsequence of the segment from {@code start} to {@code end}.
     *
     * @param seg the original segment
     * @param start the start index (inclusive)
     * @param end the end index (exclusive)
     * @return the resulting segment
     */
    @Override
    public RichSegment subSequence(RichSegment seg, int start, int end) {
        if (seg instanceof TextSegment t) {
            return new TextSegment(styledTextOps.subSequence(t.getText(), start, end));
        }
        if (seg instanceof HyperlinkSegment t) {
            return t.subSequence(start, end);
        }
        return seg;
    }

    /**
     * Attempts to join two adjacent segments of the same type.
     * - Text segments are merged using standard text joining.
     * - Hyperlink segments are merged if their URLs are equal.
     *
     * @param currentSeg the first segment
     * @param nextSeg the second segment
     * @return an {@code Optional} of the merged segment, or empty if not joinable
     */
    @Override
    public Optional<RichSegment> joinSeg(RichSegment currentSeg, RichSegment nextSeg) {
        if (currentSeg instanceof TextSegment t1 && nextSeg instanceof TextSegment t2) {
            return styledTextOps.joinSeg(t1.getText(), t2.getText())
                    .map(TextSegment::new);
        } else if (currentSeg instanceof HyperlinkSegment h1 && nextSeg instanceof HyperlinkSegment h2 &&
                h1.getUrl().equals(h2.getUrl())) {
            return Optional.of(new HyperlinkSegment(h1.getText() + h2.getText(), h1.getUrl()));
        }
        return Optional.empty();
    }

    /**
     * Creates an empty text segment.
     *
     * @return an empty {@link TextSegment}
     */
    @Override
    public RichSegment createEmptySeg() {
        return new TextSegment("");
    }

    /**
     * Creates a new text segment with the given string.
     *
     * @param text the text to create the segment with
     * @return a new {@link TextSegment}
     */
    @Override
    public RichSegment create(String text) {
        return new TextSegment(text);
    }
}
