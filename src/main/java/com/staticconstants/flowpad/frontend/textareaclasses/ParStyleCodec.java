package com.staticconstants.flowpad.frontend.textareaclasses;

import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import org.fxmisc.richtext.model.Codec;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ParStyleCodec implements Codec<ParStyle> {

    @Override
    public String getName() {
        return "par-style";
    }


    @Override
    public void encode(DataOutputStream out, ParStyle style) throws IOException {
        out.writeUTF(style.getAlignment().toString());
        out.writeDouble(style.getLineSpacing());
        out.writeDouble(style.getLeftMargin());
        out.writeDouble(style.getRightMargin());
        out.writeDouble(style.getTopMargin());
        out.writeDouble(style.getBottomMargin());
        out.writeUTF(style.getBackgroundColor().toString());
    }

    @Override
    public ParStyle decode(DataInputStream in) throws IOException {
        String alignmentStr = in.readUTF();
        double lineSpacing = in.readDouble();
        double leftMargin = in.readDouble();
        double rightMargin = in.readDouble();
        double topMargin = in.readDouble();
        double bottomMargin = in.readDouble();
        String bgColorStr = in.readUTF();

        TextAlignment alignment = TextAlignment.valueOf(alignmentStr);
        Color backgroundColor = Color.valueOf(bgColorStr);

        return new ParStyle(alignment, lineSpacing, leftMargin, rightMargin, topMargin, bottomMargin, backgroundColor);
    }
}
