package com.staticconstants.flowpad.frontend.textarea;

import org.fxmisc.richtext.model.Codec;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * A codec for serializing and deserializing {@link HyperlinkSegment} instances.
 * <p>
 * This implementation encodes the display text and the URL of the hyperlink
 * into a binary format and decodes them back into a {@link HyperlinkSegment}.
 */
public class HyperlinkSegmentCodec implements Codec<HyperlinkSegment> {

    /**
     * Returns the unique name for this codec. This name is used by
     * RichTextFX for segment serialization identification.
     *
     * @return the codec name, "hyperlink-segment"
     */
    @Override
    public String getName() {
        return "hyperlink-segment";
    }

    /**
     * Encodes a {@link HyperlinkSegment} to the provided {@link DataOutputStream}.
     * <p>
     * The encoding writes the display text and URL in UTF format.
     *
     * @param out     the output stream to write to
     * @param segment the segment to encode
     * @throws IOException if an I/O error occurs during writing
     */
    @Override
    public void encode(DataOutputStream out, HyperlinkSegment segment) throws IOException {
        out.writeUTF(segment.getDisplayText());
        out.writeUTF(segment.getUrl());
    }

    /**
     * Decodes a {@link HyperlinkSegment} from the provided {@link DataInputStream}.
     * <p>
     * The method expects the input to contain the display text followed by the URL in UTF format.
     *
     * @param in the input stream to read from
     * @return the decoded {@link HyperlinkSegment}
     * @throws IOException if an I/O error occurs during reading
     */
    @Override
    public HyperlinkSegment decode(DataInputStream in) throws IOException {
        String displayText = in.readUTF();
        String url = in.readUTF();
        return new HyperlinkSegment(displayText, url);
    }
}
