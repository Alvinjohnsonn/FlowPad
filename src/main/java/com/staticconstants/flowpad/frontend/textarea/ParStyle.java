package com.staticconstants.flowpad.frontend.textarea;

import javafx.geometry.Insets;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import java.util.Objects;

public class ParStyle {
    public enum ListType {
        NONE,
        BULLET,
        NUMBERED
    }
    private final TextAlignment alignment;
    private final double lineSpacing;
    private final double leftMargin, rightMargin, topMargin, bottomMargin;
    private final Color backgroundColor;
    private final ListType listType;
    private final int listLevel;

    public static final ParStyle EMPTY = new ParStyle(TextAlignment.LEFT, 0, 0, 0, 0, 0, Color.WHITE, ListType.NONE, 0);

    public ParStyle(TextAlignment alignment, double lineSpacing,
                    double leftMargin, double rightMargin, double topMargin, double bottomMargin, Color backgroundColor, ListType listType, int listLevel) {
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
    public ParStyle setAlignment(TextAlignment alignment){
        return new ParStyle(alignment,lineSpacing,leftMargin,rightMargin,topMargin,bottomMargin,backgroundColor,listType,listLevel);
    }

    public double getLineSpacing() {
        return lineSpacing;
    }
    public ParStyle setLineSpacing(int lineSpacing){
        return new ParStyle(alignment,lineSpacing,leftMargin,rightMargin,topMargin,bottomMargin,backgroundColor,listType,listLevel);
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
//    TODO: Add setters

    public Color getBackgroundColor() {
        return backgroundColor;
    }
    public ParStyle setBackgroundColor(Color backgroundColor){
        return new ParStyle(alignment,lineSpacing,leftMargin,rightMargin,topMargin,bottomMargin,backgroundColor,listType,listLevel);
    }
    public ListType getListType(){
        return listType;
    }
    public ParStyle setListType(ListType listType){
        int listLevel = getListLevel();
        if (listType == ListType.NONE) listLevel = 0;
        else listLevel=1;
        return new ParStyle(alignment,lineSpacing,leftMargin,rightMargin,topMargin,bottomMargin,backgroundColor,listType,listLevel);
    }
    public int getListLevel(){
        return listLevel;
    }
    public ParStyle increaseListLevel(int currentListLevel){
        return new ParStyle(alignment,lineSpacing,leftMargin,rightMargin,topMargin,bottomMargin,backgroundColor,listType,currentListLevel<5?currentListLevel+1:currentListLevel);
    }
    public ParStyle decreaseListLevel(int currentListLevel){
        if (currentListLevel==1) return new ParStyle(alignment,lineSpacing,leftMargin,rightMargin,topMargin,bottomMargin,backgroundColor,ListType.NONE,0);
        else {
            return new ParStyle(alignment,lineSpacing,leftMargin,rightMargin,topMargin,bottomMargin,backgroundColor,listType,currentListLevel-1);
        }
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

//    public ParStyle merge(ParStyle other) {
//        return new ParStyle(
//                other.listType != ListType.NONE ? other.listType : this.listType,
//                other.getTopMargin() != 0 ? other.getTopMargin() : this.getTopMargin(),
//                other.getBottomMargin() != 0 ? other.getBottomMargin() : this.getBottomMargin()
//        );
//    }

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