package com.staticconstants.flowpad.frontend.textarea;

import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import org.fxmisc.richtext.model.Codec;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * A {@link Codec} implementation for serializing and deserializing {@link ParStyle} objects.
 * <p>
 * The paragraph style includes properties like alignment, line spacing, margins,
 * background color, list type, and indentation level. These properties are serialized
 * in a specific order and must be read in the same order to reconstruct the style correctly.
 * </p>
 */
public class ParStyleCodec implements Codec<ParStyle> {

    /**
     * Returns the name of this codec.
     *
     * @return the string "par-style"
     */
    @Override
    public String getName() {
        return "par-style";
    }

    /**
     * Encodes a {@link ParStyle} instance to a {@link DataOutputStream}.
     * <p>
     * The encoded data includes:
     * alignment (as string), line spacing, all four margins,
     * background color (as string), list type (as string), and list level.
     * </p>
     *
     * @param out   the output stream to write to
     * @param style the paragraph style to encode
     * @throws IOException if writing to the stream fails
     */
    @Override
    public void encode(DataOutputStream out, ParStyle style) throws IOException {
        out.writeUTF(style.getAlignment().toString());
        out.writeDouble(style.getLineSpacing());
        out.writeDouble(style.getLeftMargin());
        out.writeDouble(style.getRightMargin());
        out.writeDouble(style.getTopMargin());
        out.writeDouble(style.getBottomMargin());
        out.writeUTF(style.getBackgroundColor().toString());
        out.writeUTF(style.getListType().toString());
        out.writeInt(style.getListLevel());
    }

    /**
     * Decodes a {@link ParStyle} instance from a {@link DataInputStream}.
     * <p>
     * Expects values in the same order as encoded by {@link #encode}.
     * </p>
     *
     * @param in the input stream to read from
     * @return the decoded {@link ParStyle} object
     * @throws IOException if reading fails or input values are invalid
     */
    @Override
    public ParStyle decode(DataInputStream in) throws IOException {
        String alignmentStr = in.readUTF();
        double lineSpacing = in.readDouble();
        double leftMargin = in.readDouble();
        double rightMargin = in.readDouble();
        double topMargin = in.readDouble();
        double bottomMargin = in.readDouble();
        String bgColorStr = in.readUTF();
        String listType = in.readUTF();
        int listLevel = in.readInt();

        TextAlignment alignment = TextAlignment.valueOf(alignmentStr);
        Color backgroundColor = Color.valueOf(bgColorStr);
        ParStyle.ListType listTypeEnum = ParStyle.ListType.valueOf(listType);

        return new ParStyle(
                alignment,
                lineSpacing,
                leftMargin,
                rightMargin,
                topMargin,
                bottomMargin,
                backgroundColor,
                listTypeEnum,
                listLevel
        );
    }
}
