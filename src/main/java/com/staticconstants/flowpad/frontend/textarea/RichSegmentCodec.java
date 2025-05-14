package com.staticconstants.flowpad.frontend.textarea;

import org.fxmisc.richtext.model.Codec;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class RichSegmentCodec implements Codec<RichSegment> {

    private final TextSegmentCodec textSegmentCodec = new TextSegmentCodec();

    @Override
    public String getName() {
        return "rich-segment";
    }

    @Override
    public void encode(DataOutputStream out, RichSegment segment) throws IOException {
        if (segment instanceof TextSegment) {
            out.writeUTF("text");
            textSegmentCodec.encode(out, (TextSegment) segment);
        } else {
            throw new IOException("Unsupported RichSegment type");
        }
    }

    @Override
    public RichSegment decode(DataInputStream in) throws IOException {
        if (in.available() == 0) {
            throw new IOException("No data available to decode RichSegment");
        }

        String type = in.readUTF();
        //System.out.println("RichSegmentCodec.decode() – type: " + type);

        if (type.equals("text")) {
            return textSegmentCodec.decode(in);
        } else {
            throw new IOException("Unknown segment type: " + type);
        }
    }
}

