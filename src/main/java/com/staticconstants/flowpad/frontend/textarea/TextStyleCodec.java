package com.staticconstants.flowpad.frontend.textarea;

import javafx.scene.paint.Color;
import org.fxmisc.richtext.model.Codec;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * A codec for serializing and deserializing {@link TextStyle} objects.
 * This class is used by RichTextFX to persist text formatting information
 * such as font styling, colors, and heading level.
 */
public class TextStyleCodec implements Codec<TextStyle> {

    /**
     * Returns the unique name of this codec.
     *
     * @return the name "text-style"
     */
    @Override
    public String getName() {
        return "text-style";
    }

    /**
     * Encodes a {@link TextStyle} by writing its properties to a {@link DataOutputStream}.
     *
     * @param out   the output stream to write to
     * @param style the text style to encode
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void encode(DataOutputStream out, TextStyle style) throws IOException {
        out.writeBoolean(style.isBold());
        out.writeBoolean(style.isItalic());
        out.writeBoolean(style.isUnderline());
        out.writeInt(style.getFontSize());
        out.writeUTF(style.getFontFamily());
        out.writeUTF(style.getTextColor().toString());
        out.writeUTF(style.getBackgroundColor().toString());
        out.writeInt(style.getHeadingLevel());
    }

    /**
     * Decodes a {@link TextStyle} by reading its properties from a {@link DataInputStream}.
     *
     * @param in the input stream to read from
     * @return a new {@link TextStyle} object with the decoded styling properties
     * @throws IOException if an I/O error occurs
     */
    @Override
    public TextStyle decode(DataInputStream in) throws IOException {
        boolean bold = in.readBoolean();
        boolean italic = in.readBoolean();
        boolean underline = in.readBoolean();
        int fontSize = in.readInt();
        String fontFamily = in.readUTF();
        String textColor = in.readUTF();
        String backgroundColor = in.readUTF();
        int headingLevel = in.readInt();

        Color txtColor = Color.valueOf(textColor);
        Color bgColor = Color.valueOf(backgroundColor);
        return new TextStyle(bold, italic, underline, fontSize, fontFamily, txtColor, bgColor, headingLevel);
    }
}
