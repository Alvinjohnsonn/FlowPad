package com.staticconstants.flowpad.frontend.textarea;

import javafx.scene.paint.Color;

import java.util.Objects;

/**
 * A custom style class for {@link TextSegment}.
 */
public class TextStyle {

    private final boolean bold;
    private final boolean italic;
    private final boolean underline;
    private final int fontSize;
    private final String fontFamily;
    private final Color textColor;
    private final Color backgroundColor;
    private final int headingLevel;

    /**
     * Default style of text segment.
     */
    public static final TextStyle EMPTY = new TextStyle(false, false, false,12, "Arial", Color.BLACK, Color.TRANSPARENT, 0);


    /**
     * Construct a {@link TextStyle} object
     * @param bold Sets the font weight.
     * @param italic Sets font style italic.
     * @param underline Sets text decoration underline.
     * @param fontSize Sets the size of the text font.
     * @param fontFamily Sets the font family.
     * @param textColor Sets the text color.
     * @param backgroundColor Sets the background color (highlight).
     * @param headingLevel Sets the type of heading (0-5), 0 = normal text.
     */
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

    /**
     * Reverse the boolean value of the bold property.
     * @return {@link TextStyle} object with previous attributes except the bold property is reversed.
     */
    public TextStyle toggleBold() {
        return new TextStyle(!this.bold, this.italic, this.underline, this.fontSize, this.fontFamily, this.textColor, this.backgroundColor, this.headingLevel);
    }
    public TextStyle setItalic(boolean italic) {
        return new TextStyle(this.bold, italic, this.underline, this.fontSize, this.fontFamily, this.textColor, this.backgroundColor, this.headingLevel);
    }
    /**
     * Reverse the boolean value of the italic property.
     * @return {@link TextStyle} object with previous attributes except the italic property is reversed.
     */
    public TextStyle toggleItalic() {
        return new TextStyle(this.bold, !this.italic, this.underline, this.fontSize, this.fontFamily, this.textColor, this.backgroundColor, this.headingLevel);
    }
    public TextStyle setUnderline(boolean underline) {
        return new TextStyle(this.bold, this.italic, underline, this.fontSize, this.fontFamily, this.textColor, this.backgroundColor, this.headingLevel);
    }
    /**
     * Reverse the boolean value of the underline property.
     * @return {@link TextStyle} object with previous attributes except the underline property is reversed.
     */
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

    /**
     * Toggle a particular style selected from attribute which will be applied to area.
     * @param area {@link CustomStyledArea} object which style to modify.
     * @param attribute The {@link TextAttribute} to modify.
     * @param desiredStyle The desired {@link TextStyle} to switch into (will only take the style specified from {@link TextAttribute}).
     */
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

    /**
     * Highlight a regular text and un-highlight already highlighted text
     * @return {@link TextStyle} object with previous attributes except the highlight property is reversed.
     */
    public TextStyle toggleHighlight() {
        Color newColor = Color.YELLOW;
//        TODO: Change to match default color highlight from settings

        return new TextStyle(this.bold, this.italic, this.underline, this.fontSize, this.fontFamily, this.textColor,
                newColor.equals(this.backgroundColor) ? Color.TRANSPARENT : newColor, this.headingLevel);
    }

    /**
     * Check whether a style is applied fully from a selection of characters.
     * @param area {@link CustomStyledArea} object to run the check.
     * @param start The starting index of the character.
     * @param end The ending index of the character.
     * @param attribute The {@link TextAttribute} of the style that is going to be checked.
     * @param desiredStyle The {@link TextStyle} to be compared to.
     * @return True if style is fully applied, and False otherwise.
     */
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

    /**
     * Get the {@link TextStyle} from a particular text segment of {@link CustomStyledArea}. Styles will be overridden by truth-ty and bigger values if different styles are found.
     * @param area {@link CustomStyledArea} which the {@link TextStyle} will be extracted from.
     * @param start The starting index of the TextStyle.
     * @param end The ending index of the TextStyle
     * @return {@link TextStyle} of the selection from a {@link CustomStyledArea}.
     */
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


    /**
     * Apply a particular style from the object {@link TextAttribute} to the {@link TextStyle}.
     * @param attr {@link TextAttribute}, the type of style to apply.
     * @param desiredStyle The {@link TextStyle} object which particular style will be used to be applied.
     * @return A new {@link TextStyle} object with new {@link TextAttribute} applied.
     */
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

    /**
     * Remove a particular style based on the {@link TextAttribute} given
     * @param attr {@link TextAttribute} to remove.
     * @return {@link TextStyle} which {@link TextAttribute} has been removed/disabled.
     */
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

    /**
     * Checks whether this {@link TextStyle} has the same {@link TextAttribute} with the given desired {@link TextStyle}.
     * @param attr {@link TextAttribute} to compare.
     * @param desiredStyle {@link TextStyle} which will be used for comparison.
     * @return True if the {@link TextAttribute} value matches and false otherwise.
     */
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
}