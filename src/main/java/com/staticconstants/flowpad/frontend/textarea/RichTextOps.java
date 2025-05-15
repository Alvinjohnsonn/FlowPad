package com.staticconstants.flowpad.frontend.textarea;

import org.fxmisc.richtext.model.*;

import java.util.Optional;

public class RichTextOps<R, T> implements TextOps<RichSegment, TextStyle> {

    private final TextOps<String, TextStyle> styledTextOps = SegmentOps.styledTextOps();

    @Override
    public int length(RichSegment seg) {
        if (seg instanceof TextSegment textSegment) {
            return styledTextOps.length(textSegment.getText());
        }
        else if (seg instanceof HyperlinkSegment hyperlinkSegment) {
            return hyperlinkSegment.length();
        }
        else if (seg instanceof ImageSegment) {
            return 1;
        }
        return 1;
    }

    @Override
    public char charAt(RichSegment seg, int index) {
        if (seg instanceof TextSegment textSegment) {
            return styledTextOps.charAt(textSegment.getText(), index);
        }
        else if (seg instanceof  HyperlinkSegment hyperlinkSegment){
            return hyperlinkSegment.getText().charAt(index);
        }
        return '\ufffc';
    }

    @Override
    public String getText(RichSegment seg) {
        if (seg instanceof TextSegment textSegment) {
            return textSegment.getText();
        }
        else if (seg instanceof  HyperlinkSegment hyperlinkSegment){
            return hyperlinkSegment.getText();
        }
        return "\ufffc";
    }

    @Override
    public RichSegment subSequence(RichSegment seg, int start) {
        if (seg instanceof TextSegment t)
            return new TextSegment(styledTextOps.subSequence(t.getText(), start));
        else if (seg instanceof  HyperlinkSegment t)
            return t.subSequence(start);
        return seg;
    }

    @Override
    public RichSegment subSequence(RichSegment seg, int start, int end) {
        if (seg instanceof TextSegment t)
            return new TextSegment(styledTextOps.subSequence(t.getText(), start, end));
        if (seg instanceof HyperlinkSegment t)
            return t.subSequence(start, end);
        return seg;
    }

    @Override
    public Optional<RichSegment> joinSeg(RichSegment currentSeg, RichSegment nextSeg) {
        if (currentSeg instanceof TextSegment t1 && nextSeg instanceof TextSegment t2) {
            return styledTextOps.joinSeg(t1.getText(), t2.getText())
                    .map(merged -> new TextSegment(merged));
        }
        else if (currentSeg instanceof HyperlinkSegment h1 && nextSeg instanceof HyperlinkSegment h2 &&
                h1.getUrl().equals(h2.getUrl())) {
            return Optional.of(new HyperlinkSegment(h1.getText() + h2.getText(), h1.getUrl()));
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
