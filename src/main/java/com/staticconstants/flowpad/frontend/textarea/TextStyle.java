package com.staticconstants.flowpad.frontend.textarea;

import javafx.scene.paint.Color;

import java.util.Objects;

public class TextStyle {

    private final boolean bold;
    private final boolean italic;
    private final boolean underline;
    private final int fontSize;
    private final String fontFamily;
    private final Color textColor;
    private final Color backgroundColor;
    private final int headingLevel;

    public static final TextStyle EMPTY = new TextStyle(false, false, false,12, "Arial", Color.BLACK, Color.TRANSPARENT, 0);


    public TextStyle(boolean bold, boolean italic, boolean underline, int fontSize, String fontFamily, Color textColor, Color backgroundColor, int headingLevel) {
        this.bold = bold;
        this.italic = italic;
        this.underline = underline;
        this.fontSize = fontSize;
        this.fontFamily = fontFamily;
        this.textColor = textColor;
        this.backgroundColor = backgroundColor;
        this.headingLevel = headingLevel;
    }
    public TextStyle setBold(boolean bold) {
        return new TextStyle(bold, this.italic, this.underline, this.fontSize, this.fontFamily, this.textColor, this.backgroundColor, this.headingLevel);
    }
    public TextStyle toggleBold() {
        return new TextStyle(!this.bold, this.italic, this.underline, this.fontSize, this.fontFamily, this.textColor, this.backgroundColor, this.headingLevel);
    }
    public TextStyle setItalic(boolean italic) {
        return new TextStyle(this.bold, italic, this.underline, this.fontSize, this.fontFamily, this.textColor, this.backgroundColor, this.headingLevel);
    }
    public TextStyle toggleItalic() {
        return new TextStyle(this.bold, !this.italic, this.underline, this.fontSize, this.fontFamily, this.textColor, this.backgroundColor, this.headingLevel);
    }
    public TextStyle setUnderline(boolean underline) {
        return new TextStyle(this.bold, this.italic, underline, this.fontSize, this.fontFamily, this.textColor, this.backgroundColor, this.headingLevel);
    }
    public TextStyle toggleUnderline() {
        return new TextStyle(this.bold, this.italic, !this.underline, this.fontSize, this.fontFamily, this.textColor, this.backgroundColor, this.headingLevel);
    }
    public TextStyle setFontSize(int size) {
        return new TextStyle(this.bold, this.italic, this.underline, size, fontFamily, this.textColor, this.backgroundColor, this.headingLevel);
    }
    public TextStyle setFontFamily(String fontFamily) {
        return new TextStyle(this.bold, this.italic, this.underline, this.fontSize, fontFamily, this.textColor, this.backgroundColor, this.headingLevel);
    }
    public TextStyle setTextColor(Color textColor) {
        return new TextStyle(this.bold, this.italic, this.underline, this.fontSize, this.fontFamily, textColor, this.backgroundColor, this.headingLevel);
    }
    public TextStyle setBackgroundColor(Color backgroundColor) {
        return new TextStyle(this.bold, this.italic, this.underline, this.fontSize, this.fontFamily, this.textColor, backgroundColor, this.headingLevel);
    }
    public TextStyle setHeadingLevel(int headingLevel) {
        return new TextStyle(this.bold, this.italic, this.underline, this.fontSize, this.fontFamily, this.textColor, this.backgroundColor, headingLevel);
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

    public Color getTextColor() { return textColor; }

    public Color getBackgroundColor() {
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
                textColor.equals(that.textColor) &&
                backgroundColor.equals(that.backgroundColor) &&
                headingLevel == that.headingLevel;
    }

    @Override
    public int hashCode() {
        return Objects.hash(bold, italic, underline, fontSize, fontFamily,textColor, backgroundColor, headingLevel);
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
        Color newColor = Color.YELLOW;
//        TODO: Change to match default color highlight from settings

        return new TextStyle(this.bold, this.italic, this.underline, this.fontSize, this.fontFamily, this.textColor,
                newColor.equals(this.backgroundColor) ? Color.TRANSPARENT : newColor, this.headingLevel);
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
            if (result.getTextColor().equals(currentStyle.getTextColor())) result = result.setTextColor(currentStyle.getTextColor());
            if (result.getBackgroundColor().equals(currentStyle.getBackgroundColor())) result = result.setBackgroundColor(currentStyle.getBackgroundColor());
            if (result.getHeadingLevel() == currentStyle.getHeadingLevel()) result = result.setHeadingLevel(currentStyle.getHeadingLevel());
        }
        return result;
    }


    public TextStyle apply(TextAttribute attr, TextStyle desiredStyle) {
        return switch (attr) {
            case BOLD -> new TextStyle(desiredStyle.bold, isItalic(), underline, fontSize, fontFamily, textColor, backgroundColor, headingLevel);
            case ITALIC -> new TextStyle(bold, desiredStyle.italic, underline, fontSize, fontFamily, textColor,backgroundColor, headingLevel);
            case UNDERLINE -> new TextStyle(bold, italic, desiredStyle.underline, fontSize, fontFamily,textColor, backgroundColor, headingLevel);
            case FONT_SIZE -> new TextStyle(bold, italic, underline, desiredStyle.fontSize, fontFamily,textColor, backgroundColor, headingLevel);
            case FONT_FAMILY -> new TextStyle(bold, italic, underline, fontSize, desiredStyle.fontFamily,textColor, backgroundColor, headingLevel);
            case TEXT_COLOR -> new TextStyle(bold, italic, underline, fontSize, fontFamily, desiredStyle.textColor, backgroundColor, headingLevel);
            case HIGHLIGHT -> new TextStyle(bold, italic, underline, fontSize, fontFamily, textColor,desiredStyle.backgroundColor, headingLevel);
            case HEADING_LEVEL -> new TextStyle(bold, italic, underline, fontSize, fontFamily, textColor, backgroundColor, desiredStyle.headingLevel);
        };
    }


    public TextStyle remove(TextAttribute attr) {
        return switch (attr) {
            case BOLD -> new TextStyle(false, italic, underline, fontSize, fontFamily, textColor, backgroundColor, headingLevel);
            case ITALIC -> new TextStyle(bold, false, underline, fontSize, fontFamily,textColor, backgroundColor, headingLevel);
            case UNDERLINE -> new TextStyle(bold, italic, false, fontSize, fontFamily, textColor,backgroundColor, headingLevel);
            case FONT_SIZE -> this;
            case FONT_FAMILY -> this;
            case TEXT_COLOR -> new TextStyle(bold, italic, underline, fontSize, fontFamily, Color.BLACK, backgroundColor, headingLevel);
            case HIGHLIGHT -> new TextStyle(bold, italic, underline, fontSize, fontFamily,textColor, Color.TRANSPARENT, headingLevel);
            case HEADING_LEVEL -> new TextStyle(bold, italic, underline, fontSize, fontFamily,textColor, backgroundColor, 0);
        };
    }

    public boolean matches(TextAttribute attr, TextStyle desiredStyle) {
        return switch (attr) {
            case BOLD -> bold == desiredStyle.bold;
            case ITALIC -> italic == desiredStyle.italic;
            case UNDERLINE -> underline == desiredStyle.underline;
            case FONT_SIZE -> fontSize == desiredStyle.fontSize;
            case FONT_FAMILY -> fontFamily.equals(desiredStyle.fontFamily);
            case TEXT_COLOR -> textColor.equals(desiredStyle.textColor);
            case HIGHLIGHT -> backgroundColor.equals(desiredStyle.backgroundColor);
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
//    TODO: Complete this
}