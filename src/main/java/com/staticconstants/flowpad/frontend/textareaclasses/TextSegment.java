package com.staticconstants.flowpad.frontend.textareaclasses;

import javafx.scene.Node;

public final class TextSegment implements RichSegment{

    private final String text;

    public String getText(){
        return text;
    }

    public TextSegment(String text) {
        this.text = text;
    }

    public TextSegment subSequence(int start, int end) {
        return new TextSegment(text.substring(start, end));
    }

    @Override
    public String toString() {
        return text;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof TextSegment other && text.equals(other.text);
    }

    @Override
    public int hashCode() {
        return text.hashCode();
    }
}
