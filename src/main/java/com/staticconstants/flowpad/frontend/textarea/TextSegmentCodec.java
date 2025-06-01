package com.staticconstants.flowpad.frontend.textarea;

import org.fxmisc.richtext.model.Codec;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * A codec for serializing and deserializing {@link TextSegment} objects.
 * This implementation is used by RichTextFX to support saving and loading
 * styled text content.
 */
public class TextSegmentCodec implements Codec<TextSegment> {

    /**
     * Returns the unique name of this codec.
     *
     * @return the name "text-segment"
     */
    @Override
    public String getName() {
        return "text-segment";
    }

    /**
     * Encodes the {@link TextSegment} by writing its text content
     * to the given {@link DataOutputStream}.
     *
     * @param out     the output stream to write to
     * @param segment the text segment to encode
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void encode(DataOutputStream out, TextSegment segment) throws IOException {
        out.writeUTF(segment.getText());
    }

    /**
     * Decodes a {@link TextSegment} by reading its text content
     * from the given {@link DataInputStream}.
     *
     * @param in the input stream to read from
     * @return a new {@link TextSegment} containing the read text
     * @throws IOException if an I/O error occurs
     */
    @Override
    public TextSegment decode(DataInputStream in) throws IOException {
        String text = in.readUTF();
        return new TextSegment(text);
    }
}
