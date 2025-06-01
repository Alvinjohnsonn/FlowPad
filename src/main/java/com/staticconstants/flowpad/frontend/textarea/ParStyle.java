package com.staticconstants.flowpad.frontend.textarea;

import javafx.geometry.Insets;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;

import java.util.Objects;

/**
 * Represents paragraph-level styling attributes for a rich text editor.
 * <p>
 * This includes alignment, spacing, margins, background color, and list information
 * such as type and indentation level.
 */
public class ParStyle {

    /**
     * Enum for specifying list formatting type.
     */
    public enum ListType {
        NONE,      ///< No list formatting
        BULLET,    ///< Unordered list
        NUMBERED   ///< Ordered list
    }

    private final TextAlignment alignment;
    private final double lineSpacing;
    private final double leftMargin, rightMargin, topMargin, bottomMargin;
    private final Color backgroundColor;
    private final ListType listType;
    private final int listLevel;

    /**
     * A predefined empty/default paragraph style.
     */
    public static final ParStyle EMPTY = new ParStyle(TextAlignment.LEFT, 0, 0, 0, 0, 0, Color.WHITE, ListType.NONE, 0);

    /**
     * Constructs a new {@code ParStyle} with all properties specified.
     *
     * @param alignment       the text alignment
     * @param lineSpacing     the line spacing multiplier
     * @param leftMargin      left margin in pixels
     * @param rightMargin     right margin in pixels
     * @param topMargin       top margin in pixels
     * @param bottomMargin    bottom margin in pixels
     * @param backgroundColor the background color
     * @param listType        the type of list formatting
     * @param listLevel       the level of list nesting
     */
    public ParStyle(TextAlignment alignment, double lineSpacing,
                    double leftMargin, double rightMargin, double topMargin, double bottomMargin,
                    Color backgroundColor, ListType listType, int listLevel) {
        this.alignment = alignment;
        this.lineSpacing = lineSpacing;
        this.leftMargin = leftMargin;
        this.rightMargin = rightMargin;
        this.topMargin = topMargin;
        this.bottomMargin = bottomMargin;
        this.backgroundColor = backgroundColor;
        this.listType = listType;
        this.listLevel = listLevel;
    }

    public TextAlignment getAlignment() {
        return alignment;
    }

    /**
     * Returns a copy of this style with updated text alignment.
     */
    public ParStyle setAlignment(TextAlignment alignment) {
        return new ParStyle(alignment, lineSpacing, leftMargin, rightMargin, topMargin, bottomMargin, backgroundColor, listType, listLevel);
    }

    public double getLineSpacing() {
        return lineSpacing;
    }

    /**
     * Returns a copy of this style with updated line spacing.
     */
    public ParStyle setLineSpacing(int lineSpacing) {
        return new ParStyle(alignment, lineSpacing, leftMargin, rightMargin, topMargin, bottomMargin, backgroundColor, listType, listLevel);
    }

    public double getLeftMargin() {
        return leftMargin;
    }

    public double getRightMargin() {
        return rightMargin;
    }

    public double getTopMargin() {
        return topMargin;
    }

    public double getBottomMargin() {
        return bottomMargin;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Returns a copy of this style with updated background color.
     */
    public ParStyle setBackgroundColor(Color backgroundColor) {
        return new ParStyle(alignment, lineSpacing, leftMargin, rightMargin, topMargin, bottomMargin, backgroundColor, listType, listLevel);
    }

    public ListType getListType() {
        return listType;
    }

    /**
     * Returns a copy of this style with updated list type.
     * Automatically adjusts list level: 1 for BULLET/NUMBERED, 0 for NONE.
     */
    public ParStyle setListType(ListType listType) {
        int listLevel = (listType == ListType.NONE) ? 0 : 1;
        return new ParStyle(alignment, lineSpacing, leftMargin, rightMargin, topMargin, bottomMargin, backgroundColor, listType, listLevel);
    }

    public int getListLevel() {
        return listLevel;
    }

    /**
     * Returns a copy of this style with an explicitly set list level.
     */
    public ParStyle setListLevel(int currentListLevel) {
        return new ParStyle(alignment, lineSpacing, leftMargin, rightMargin, topMargin, bottomMargin, backgroundColor, listType, currentListLevel);
    }

    /**
     * Returns a copy of this style with an increased list level, up to a maximum of 5.
     */
    public ParStyle increaseListLevel(int currentListLevel) {
        return new ParStyle(alignment, lineSpacing, leftMargin, rightMargin, topMargin, bottomMargin, backgroundColor, listType,
                currentListLevel < 5 ? currentListLevel + 1 : currentListLevel);
    }

    /**
     * Returns a copy of this style with a decreased list level.
     * If the current level is 1, resets list type to NONE and level to 0.
     */
    public ParStyle decreaseListLevel(int currentListLevel) {
        if (currentListLevel == 1) {
            return new ParStyle(alignment, lineSpacing, leftMargin, rightMargin, topMargin, bottomMargin, backgroundColor, ListType.NONE, 0);
        } else {
            return new ParStyle(alignment, lineSpacing, leftMargin, rightMargin, topMargin, bottomMargin, backgroundColor, listType, currentListLevel - 1);
        }
    }

    /**
     * Applies the given {@code ParStyle} properties to a {@link TextFlow} node.
     *
     * @param textFlow the TextFlow to apply styles to
     * @param style    the ParStyle object containing paragraph formatting
     */
    public static void apply(TextFlow textFlow, ParStyle style) {
        textFlow.setTextAlignment(style.getAlignment());
        textFlow.setLineSpacing(style.getLineSpacing());
        textFlow.setPadding(new Insets(
                style.topMargin,
                style.rightMargin,
                style.bottomMargin,
                style.leftMargin
        ));
        if (style.getBackgroundColor() != null) {
            textFlow.setStyle("-fx-background-color: " + toHexString(style.getBackgroundColor()));
        }
    }

    /**
     * Converts a {@link Color} to a hexadecimal string.
     *
     * @param color the color to convert
     * @return a hex string like {@code "#RRGGBB"}
     */
    private static String toHexString(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ParStyle)) return false;
        ParStyle parStyle = (ParStyle) o;
        return alignment == parStyle.alignment &&
                lineSpacing == parStyle.lineSpacing &&
                leftMargin == parStyle.leftMargin &&
                rightMargin == parStyle.rightMargin &&
                topMargin == parStyle.topMargin &&
                bottomMargin == parStyle.bottomMargin &&
                backgroundColor == parStyle.backgroundColor;
    }

    @Override
    public int hashCode() {
        return Objects.hash(alignment, lineSpacing, leftMargin, rightMargin, topMargin, bottomMargin, backgroundColor);
    }

    @Override
    public String toString() {
        return "ParStyle{" +
                "alignment=" + alignment +
                ", lineSpacing=" + lineSpacing +
                ", leftMargin=" + leftMargin +
                ", rightMargin=" + rightMargin +
                ", topMargin=" + topMargin +
                ", bottomMargin=" + bottomMargin +
                ", backgroundColor=" + backgroundColor +
                ", listType=" + listType +
                ", listLevel=" + listLevel +
                '}';
    }
}
