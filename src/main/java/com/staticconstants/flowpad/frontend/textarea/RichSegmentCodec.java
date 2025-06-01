package com.staticconstants.flowpad.frontend.textarea;

import org.fxmisc.richtext.model.Codec;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * A {@link Codec} implementation for serializing and deserializing {@link RichSegment} objects.
 * <p>
 * Supports {@link TextSegment} and {@link HyperlinkSegment} types.
 * Each segment is encoded with a type identifier followed by its specific data.
 * </p>
 * <p>
 * {@link ImageSegment} is currently unsupported and will cause an exception.
 * </p>
 */
public class RichSegmentCodec implements Codec<RichSegment> {

    private final TextSegmentCodec textSegmentCodec = new TextSegmentCodec();
    private final HyperlinkSegmentCodec hyperlinkSegmentCodec = new HyperlinkSegmentCodec();

    /**
     * Returns the name of the codec.
     *
     * @return the string "rich-segment"
     */
    @Override
    public String getName() {
        return "rich-segment";
    }

    /**
     * Encodes a {@link RichSegment} to the provided {@link DataOutputStream}.
     * The segment is preceded by a string identifier ("text" or "hyperlink") to indicate its type.
     *
     * @param out     the output stream to write to
     * @param segment the segment to encode
     * @throws IOException if writing fails or the segment type is unsupported
     */
    @Override
    public void encode(DataOutputStream out, RichSegment segment) throws IOException {
        if (segment instanceof TextSegment) {
            out.writeUTF("text");
            textSegmentCodec.encode(out, (TextSegment) segment);
        } else if (segment instanceof HyperlinkSegment) {
            out.writeUTF("hyperlink");
            hyperlinkSegmentCodec.encode(out, (HyperlinkSegment) segment);
        } else {
            throw new IOException("Unsupported RichSegment type");
        }
    }

    /**
     * Decodes a {@link RichSegment} from the provided {@link DataInputStream}.
     * Expects a type string ("text" or "hyperlink") followed by the corresponding segment data.
     *
     * @param in the input stream to read from
     * @return the decoded {@link RichSegment}
     * @throws IOException if the stream is empty or contains an unknown type
     */
    @Override
    public RichSegment decode(DataInputStream in) throws IOException {
        if (in.available() == 0) {
            throw new IOException("No data available to decode RichSegment");
        }

        String type = in.readUTF();
        if (type.equals("text")) {
            return textSegmentCodec.decode(in);
        } else if (type.equals("hyperlink")) {
            return hyperlinkSegmentCodec.decode(in);
        } else {
            throw new IOException("Unknown segment type: " + type);
        }
    }
}
