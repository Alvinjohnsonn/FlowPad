package com.staticconstants.flowpad.frontend.textarea;

import org.fxmisc.richtext.model.Codec;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class RichSegmentCodec implements Codec<RichSegment> {

    private final TextSegmentCodec textSegmentCodec = new TextSegmentCodec();
    private final HyperlinkSegmentCodec hyperlinkSegmentCodec = new HyperlinkSegmentCodec();


    @Override
    public String getName() {
        return "rich-segment";
    }

    @Override
    public void encode(DataOutputStream out, RichSegment segment) throws IOException {
        if (segment instanceof TextSegment) {
            out.writeUTF("text");
            textSegmentCodec.encode(out, (TextSegment) segment);
        }
        else if (segment instanceof HyperlinkSegment){
            out.writeUTF("hyperlink");
            hyperlinkSegmentCodec.encode(out, (HyperlinkSegment) segment);
        }
        else {
            throw new IOException("Unsupported RichSegment type");
        }
    }

    @Override
    public RichSegment decode(DataInputStream in) throws IOException {
        if (in.available() == 0) {
            throw new IOException("No data available to decode RichSegment");
        }

        String type = in.readUTF();
        if (type.equals("text")) {
            return textSegmentCodec.decode(in);
        }
        else if (type.equals("hyperlink")) {
            return hyperlinkSegmentCodec.decode(in);
        }else {
            throw new IOException("Unknown segment type: " + type);
        }
    }
}

