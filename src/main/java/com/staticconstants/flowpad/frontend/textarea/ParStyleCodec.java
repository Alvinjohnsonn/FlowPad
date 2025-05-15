package com.staticconstants.flowpad.frontend.textarea;

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
        out.writeUTF(style.getListType().toString());
        out.writeInt(style.getListLevel());
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
        String listType = in.readUTF();
        int listLevel = in.readInt();

        TextAlignment alignment = TextAlignment.valueOf(alignmentStr);
        Color backgroundColor = Color.valueOf(bgColorStr);
        ParStyle.ListType listType1 = ParStyle.ListType.valueOf(listType);
        return new ParStyle(alignment, lineSpacing, leftMargin, rightMargin, topMargin, bottomMargin, backgroundColor, listType1, listLevel);
    }
}
