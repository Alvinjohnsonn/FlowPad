package com.staticconstants.flowpad.backend.notes;

import com.staticconstants.flowpad.frontend.textarea.TextStyle;


public class SerialisableSegment {
    public Type type;
    public String content;
    public TextStyle style;

    public SerialisableSegment(Type type, String content, TextStyle style)
    {
        this.type = type;
        this.content = content;
        this.style = style;
    }


    public enum Type {
        TEXT,
        IMAGE
    }
}
