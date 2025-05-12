package com.staticconstants.flowpad.frontend.textareaclasses;

import javafx.scene.Node;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import org.fxmisc.richtext.TextExt;

public final class TextSegment implements RichSegment{

    private final String text;

    public String getText(){
        return text;
    }

    public TextSegment(String text) {
        this.text = text;
    }

    public TextSegment subSequence(int start, int end) {
        return new TextSegment(text.substring(start, end));
    }

    @Override
    public String toString() {
        return text;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof TextSegment other && text.equals(other.text);
    }

    @Override
    public int hashCode() {
        return text.hashCode();
    }
    @Override
    public int length() {
        return text.length();
    }


    @Override
    public Node createNode(TextStyle style) {
        TextExt text = new TextExt(getText());
        String fontFamily = style.getFontFamily();

        int fontSize = switch (style.getHeadingLevel()) {
            case 1 -> 28;
            case 2 -> 24;
            case 3 -> 20;
            case 4 -> 16;
            case 5 -> 14;
            default -> style.getFontSize();
        };

        FontWeight weight = (style.isBold() || style.getHeadingLevel() > 0) ? FontWeight.BOLD : FontWeight.NORMAL;
        FontPosture posture = style.isItalic() ? FontPosture.ITALIC : FontPosture.REGULAR;
        Font font = Font.font(fontFamily, weight, posture, fontSize);
        text.setFont(font);
        text.setUnderline(style.isUnderline());
        text.setBackgroundColor(Paint.valueOf(style.getBackgroundColor() == null ? "transparent" : style.getBackgroundColor()));
        // Add later
//                style.getTextColor().ifPresent(text::setFill); // assuming getTextColor() returns Optional<Paint>
//                Optional<Paint> color = Optional.of(Paint.valueOf(style.getBackgroundColor() == null ? "transparent" : style.getBackgroundColor()));
//                color.ifPresent(text::setBackgroundColor);

        return text;
    }
}
