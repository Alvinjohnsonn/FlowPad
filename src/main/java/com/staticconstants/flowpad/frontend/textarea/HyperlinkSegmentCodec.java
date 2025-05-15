package com.staticconstants.flowpad.frontend.textarea;

import org.fxmisc.richtext.model.Codec;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class HyperlinkSegmentCodec implements Codec<HyperlinkSegment> {
    @Override
    public String getName() {
        return "hyperlink-segment";
    }

    @Override
    public void encode(DataOutputStream out, HyperlinkSegment segment) throws IOException {
        out.writeUTF(segment.getDisplayText());
        out.writeUTF(segment.getUrl());
    }

    @Override
    public HyperlinkSegment decode(DataInputStream in) throws IOException {
        String displayText = in.readUTF();
        String url = in.readUTF();
        return new HyperlinkSegment(displayText,url);
    }
}
