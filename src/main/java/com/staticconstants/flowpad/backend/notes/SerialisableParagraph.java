package com.staticconstants.flowpad.backend.notes;

import com.staticconstants.flowpad.frontend.textarea.ParStyle;

import java.util.List;

public class SerialisableParagraph {
    public ParStyle parStyle;
    public List<SerialisableSegment> segments;

    public SerialisableParagraph(ParStyle parStyle, List<SerialisableSegment> segments) {
        this.parStyle = parStyle;
        this.segments = segments;
    }
}
