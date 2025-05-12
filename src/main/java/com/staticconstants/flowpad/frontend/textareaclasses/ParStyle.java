package com.staticconstants.flowpad.frontend.textareaclasses;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import java.util.Objects;

public class ParStyle {

    private final TextAlignment alignment;
    private final double lineSpacing;
    private final double leftMargin;
    private final double rightMargin;
    private final double topMargin;
    private final double bottomMargin;
    private final Color backgroundColor;

    public static final ParStyle EMPTY = new ParStyle(TextAlignment.LEFT, 0, 0, 0, 0, 0, Color.WHITE);

    public ParStyle(TextAlignment alignment, double lineSpacing,
                    double leftMargin, double rightMargin, double topMargin, double bottomMargin, Color backgroundColor) {
        this.alignment = alignment;
        this.lineSpacing = lineSpacing;
        this.leftMargin = leftMargin;
        this.rightMargin = rightMargin;
        this.topMargin = topMargin;
        this.bottomMargin = bottomMargin;
        this.backgroundColor = backgroundColor;
    }

    public TextAlignment getAlignment() {
        return alignment;
    }

    public double getLineSpacing() {
        return lineSpacing;
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

    private static String toHexString(Color color) {
        return String.format("#%02X%02X%02X", (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
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
                '}';
    }
}