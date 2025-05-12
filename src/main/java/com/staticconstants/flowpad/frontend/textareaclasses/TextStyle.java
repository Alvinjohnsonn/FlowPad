package com.staticconstants.flowpad.frontend.textareaclasses;

import javafx.css.converter.FontConverter;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import org.fxmisc.richtext.GenericStyledArea;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TextStyle {

    private boolean bold;
    private boolean italic;
    private final boolean underline;
    private final int fontSize;
    private final String fontFamily;
    private final String backgroundColor;
    private int headingLevel;

    public static final TextStyle EMPTY = new TextStyle(false, false, false,12, "Arial", "transparent", 0);


    public TextStyle(boolean bold, boolean italic, boolean underline, int fontSize, String fontFamily, String backgroundColor, int headingLevel) {
        this.bold = bold;
        this.italic = italic;
        this.underline = underline;
        this.fontSize = fontSize;
        this.fontFamily = fontFamily;
        this.backgroundColor = backgroundColor;
        this.headingLevel = headingLevel;
    }
    public TextStyle setBold(boolean bold) {
        return new TextStyle(bold, this.italic, this.underline, this.fontSize, this.fontFamily, this.backgroundColor, this.headingLevel);
    }
    public TextStyle toggleBold() {
        return new TextStyle(!this.bold, this.italic, this.underline, this.fontSize, this.fontFamily, this.backgroundColor, this.headingLevel);
    }
    public TextStyle setItalic(boolean italic) {
        return new TextStyle(this.bold, italic, this.underline, this.fontSize, this.fontFamily, this.backgroundColor, this.headingLevel);
    }
    public TextStyle toggleItalic() {
        return new TextStyle(this.bold, !this.italic, this.underline, this.fontSize, this.fontFamily, this.backgroundColor, this.headingLevel);
    }
    public TextStyle setUnderline(boolean underline) {
        return new TextStyle(this.bold, this.italic, underline, this.fontSize, this.fontFamily, this.backgroundColor, this.headingLevel);
    }
    public TextStyle toggleUnderline() {
        return new TextStyle(this.bold, this.italic, !this.underline, this.fontSize, this.fontFamily, this.backgroundColor, this.headingLevel);
    }
    public TextStyle setFontSize(int size) {
        return new TextStyle(this.bold, this.italic, this.underline, size, fontFamily, this.backgroundColor, this.headingLevel);
    }
    public TextStyle setFontFamily(String fontFamily) {
        return new TextStyle(this.bold, this.italic, this.underline, this.fontSize, fontFamily, this.backgroundColor, this.headingLevel);
    }
    public TextStyle setBackgroundColor(String backgroundColor) {
        return new TextStyle(this.bold, this.italic, this.underline, this.fontSize, this.fontFamily, backgroundColor, this.headingLevel);
    }
    public TextStyle setHeadingLevel(int headingLevel) {
        return new TextStyle(this.bold, this.italic, this.underline, this.fontSize, this.fontFamily, this.backgroundColor, headingLevel);
    }


    public boolean isBold() {
        return bold;
    }

    public boolean isItalic() {
        return italic;
    }

    public boolean isUnderline() {
        return underline;
    }

    public int getFontSize() {
        return fontSize;
    }

    public String getFontFamily() {
        return fontFamily;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public int getHeadingLevel() { return headingLevel; }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof TextStyle)) return false;
        TextStyle that = (TextStyle) o;
        return bold == that.bold &&
                italic == that.italic &&
                underline == that.underline &&
                fontSize == that.fontSize &&
                fontFamily.equals(that.fontFamily) &&
                backgroundColor.equals(that.backgroundColor) &&
                headingLevel == that.headingLevel;
    }

    @Override
    public int hashCode() {
        return Objects.hash(bold, italic, underline, fontSize, fontFamily, backgroundColor, headingLevel);
    }


    public static void toggleStyle(CustomStyledArea<ParStyle, RichSegment, TextStyle> area,
                                   TextAttribute attribute,
                                   TextStyle desiredStyle) {
        if (area.getSelection().getLength() == 0) return;

        int start = area.getSelection().getStart();
        int end = area.getSelection().getEnd();

        boolean applied = isStyleFullyApplied(area, start, end, attribute, desiredStyle);

        for (int i = start+1; i <= end; i++) {
            TextStyle current = area.getStyleAtPosition(i);
            TextStyle updated;

            if (applied) {
                updated = current.remove(attribute);
            } else {
                updated = current.apply(attribute, desiredStyle);
            }
            area.setStyle(i-1, i, updated);
        }
    }

    public TextStyle toggleHighlight() {
        String newColor = "yellow";
        return new TextStyle(this.bold, this.italic, this.underline, this.fontSize, this.fontFamily,
                "yellow".equals(this.backgroundColor) ? "transparent" : newColor, this.headingLevel);
    }


    public static boolean isStyleFullyApplied(CustomStyledArea<ParStyle, RichSegment, TextStyle> area,
                                              int start, int end,
                                              TextAttribute attribute,
                                              TextStyle desiredStyle) {
        if (area.getSelection().getLength() == 0){
            TextStyle currentStyle;
            if (area.getCaretPosition()>0) {
                currentStyle = area.getStyleAtPosition(area.getCaretPosition());
            }
            else{
                currentStyle = area.getStyleAtPosition(area.getCaretPosition()+1);
            }

            return currentStyle.matches(attribute, desiredStyle);
        }

        for (int i = start; i < end; i++) {
            TextStyle current = area.getStyleAtPosition(i);
            if (!current.matches(attribute, desiredStyle)) {
                return false;
            }
        }
        return true;
    }

    public static TextStyle getStyleSelection(CustomStyledArea<ParStyle, RichSegment, TextStyle> area,
                                              int start, int end) {
        TextStyle result = area.getStyleAtPosition(start+1);
        for (int i = start+2; i <= end; i++) {
            TextStyle currentStyle = area.getStyleAtPosition(i);

            if (!result.isBold()) result = result.setBold(result.bold || currentStyle.isBold());
            if (!result.isItalic()) result = result.setItalic(result.italic || currentStyle.isItalic());
            if (!result.isUnderline()) result = result.setUnderline(result.underline || currentStyle.isUnderline());
            if (result.getFontSize()<currentStyle.getFontSize()) result = result.setFontSize(currentStyle.getFontSize());
            if (result.getBackgroundColor().equals(currentStyle.getBackgroundColor())) result = result.setBackgroundColor(currentStyle.getBackgroundColor());
            if (result.getHeadingLevel() == currentStyle.getHeadingLevel()) result = result.setHeadingLevel(currentStyle.getHeadingLevel());
        }
        return result;
    }


    public TextStyle apply(TextAttribute attr, TextStyle desiredStyle) {
        return switch (attr) {
            case BOLD -> new TextStyle(desiredStyle.bold, isItalic(), underline, fontSize, fontFamily, backgroundColor, headingLevel);
            case ITALIC -> new TextStyle(bold, desiredStyle.italic, underline, fontSize, fontFamily, backgroundColor, headingLevel);
            case UNDERLINE -> new TextStyle(bold, italic, desiredStyle.underline, fontSize, fontFamily, backgroundColor, headingLevel);
            case FONT_SIZE -> new TextStyle(bold, italic, underline, desiredStyle.fontSize, fontFamily, backgroundColor, headingLevel);
            case FONT_FAMILY -> new TextStyle(bold, italic, underline, fontSize, desiredStyle.fontFamily, backgroundColor, headingLevel);
            case HIGHLIGHT -> new TextStyle(bold, italic, underline, fontSize, fontFamily, desiredStyle.backgroundColor, headingLevel);
            case HEADING_LEVEL -> new TextStyle(bold, italic, underline, fontSize, fontFamily, backgroundColor, desiredStyle.headingLevel);
        };
    }


    public TextStyle remove(TextAttribute attr) {
        return switch (attr) {
            case BOLD -> new TextStyle(false, italic, underline, fontSize, fontFamily, backgroundColor, headingLevel);
            case ITALIC -> new TextStyle(bold, false, underline, fontSize, fontFamily, backgroundColor, headingLevel);
            case UNDERLINE -> new TextStyle(bold, italic, false, fontSize, fontFamily, backgroundColor, headingLevel);
            case FONT_SIZE -> this;
            case FONT_FAMILY -> this;
            case HIGHLIGHT -> new TextStyle(bold, italic, underline, fontSize, fontFamily, "transparent", headingLevel);
            case HEADING_LEVEL -> new TextStyle(bold, italic, underline, fontSize, fontFamily, backgroundColor, 0);
        };
    }

    public boolean matches(TextAttribute attr, TextStyle desiredStyle) {
        return switch (attr) {
            case BOLD -> bold == desiredStyle.bold;
            case ITALIC -> italic == desiredStyle.italic;
            case UNDERLINE -> underline == desiredStyle.underline;
            case FONT_SIZE -> fontSize == desiredStyle.fontSize;
            case FONT_FAMILY -> fontFamily.equals(desiredStyle.fontFamily);
            case HIGHLIGHT -> Objects.equals(backgroundColor, desiredStyle.backgroundColor);
            case HEADING_LEVEL -> headingLevel == desiredStyle.headingLevel;
        };
    }

    @Override
    public String toString() {
        return "Bold: " + (isBold() ? "true" : "false") +
                "\nItalic: " + (isItalic() ? "true" : "false") +
                "\nUnderline: " + (isUnderline() ? "true" : "false") +
                "\nBackground Color: " + (getBackgroundColor()) + "\n";
    }
}