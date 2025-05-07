package com.staticconstants.flowpad.frontend.textareaclasses;

import org.fxmisc.richtext.model.*;

import java.util.Optional;

public class RichTextOps<R, T> implements TextOps<RichSegment, TextStyle> {

    private final TextOps<String, TextStyle> styledTextOps = SegmentOps.styledTextOps();

    @Override
    public int length(RichSegment seg) {
        if (seg instanceof TextSegment textSegment) {
            return styledTextOps.length(textSegment.getText());
        } else if (seg instanceof ImageSegment) {
            return 1;
        }
        return 1;
    }

    @Override
    public char charAt(RichSegment seg, int index) {
        if (seg instanceof TextSegment textSegment) {
            return styledTextOps.charAt(textSegment.getText(), index);
        }
        return '\ufffc';
    }

    @Override
    public String getText(RichSegment seg) {
        if (seg instanceof TextSegment textSegment) {
            return textSegment.getText();
        }
        return "\ufffc";
    }

    @Override
    public RichSegment subSequence(RichSegment seg, int start) {
        if (seg instanceof TextSegment t)
            return new TextSegment(styledTextOps.subSequence(t.getText(), start));
        return seg;
    }

    @Override
    public RichSegment subSequence(RichSegment seg, int start, int end) {
        if (seg instanceof TextSegment t)
            return new TextSegment(styledTextOps.subSequence(t.getText(), start, end));
        return seg;
    }

    @Override
    public Optional<RichSegment> joinSeg(RichSegment currentSeg, RichSegment nextSeg) {
        if (currentSeg instanceof TextSegment t1 && nextSeg instanceof TextSegment t2) {
            return styledTextOps.joinSeg(t1.getText(), t2.getText())
                    .map(merged -> new TextSegment(merged));
        }
        return Optional.empty();
    }

    @Override
    public RichSegment createEmptySeg() {
        return new TextSegment("");
    }

    @Override
    public RichSegment create(String text) {
        return new TextSegment(text);
    }


}
