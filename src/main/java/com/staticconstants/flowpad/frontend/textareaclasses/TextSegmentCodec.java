package com.staticconstants.flowpad.frontend.textareaclasses;

import org.fxmisc.richtext.model.Codec;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TextSegmentCodec implements Codec<TextSegment> {

    @Override
    public String getName() {
        return "text-segment";
    }

    @Override
    public void encode(DataOutputStream out, TextSegment segment) throws IOException {
        out.writeUTF(segment.getText());
    }

    @Override
    public TextSegment decode(DataInputStream in) throws IOException {
        String text = in.readUTF();
        return new TextSegment(text);
    }
}
