package com.staticconstants.flowpad.frontend.textarea;
import javafx.scene.paint.Color;
import org.fxmisc.richtext.model.Codec;
import java.io.*;

public class TextStyleCodec implements Codec<TextStyle> {

    @Override
    public String getName() {
        return "text-style";
    }

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
