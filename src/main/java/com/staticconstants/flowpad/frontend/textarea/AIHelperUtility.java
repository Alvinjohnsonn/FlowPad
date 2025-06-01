package com.staticconstants.flowpad.frontend.textarea;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

import java.io.IOException;

public class AIHelperUtility {
    private CustomStyledArea<ParStyle, RichSegment, TextStyle> textArea;

    public AIHelperUtility(CustomStyledArea<ParStyle,RichSegment,TextStyle> textArea){
        this.textArea = textArea;
    }

    public void insertText(String text, TextStyle style) {
        RichSegment segment = new TextSegment(text);
        textArea.append(segment, style);
    }

    public void insertParagraph(String text, TextStyle style, ParStyle parStyle) {
        RichSegment segment = new TextSegment(text);
        textArea.append(segment, style);

        int paragraphIndex = textArea.getParagraphs().size() - 1;
        textArea.applyParStyleToParagraph(paragraphIndex, parStyle);
        int pos = textArea.getLength();
        textArea.setStyle(pos, pos, TextStyle.EMPTY);
    }

    public void insertImage(Image image) {
        RichSegment imageSegment = new ImageSegment(image);
        textArea.append(imageSegment, TextStyle.EMPTY);
    }

    public void insertHyperlink(String text, String url) {
        RichSegment hyperlinkSegment = new HyperlinkSegment(text, url);
        textArea.append(hyperlinkSegment, TextStyle.EMPTY);
    }

    public void applyJsonInstructions(JsonNode nodes) throws IOException {
        for (JsonNode node : nodes) {
            applyJsonInstruction(node);
        }
    }

    public void applyJsonInstruction(JsonNode node) throws IOException {
        String type = node.get("type").asText();
        switch (type) {
            case "text" -> {
                String content = node.get("content").asText();
                JsonNode styleNode = node.get("style");
                JsonNode parStyleNode = node.get("paragraphStyle");
                TextStyle style = parseStyle(styleNode);
                ParStyle parStyle = parseParStyle(parStyleNode);

                if (parStyleNode == null) insertText(content, style);
                else insertParagraph(content, style, parStyle);
            }
            case "image" -> insertImage(new Image(node.get("url").asText(), true));
            case "hyperlink" -> insertHyperlink(node.get("text").asText(), node.get("url").asText());
        }
    }

    private TextStyle parseStyle(JsonNode styleNode) {
        if (styleNode == null || styleNode.isNull()) {
            return TextStyle.EMPTY;
        }
        TextStyle style = TextStyle.EMPTY;
        if (styleNode.has("bold")) style = style.setBold(styleNode.get("bold").asBoolean());
        if (styleNode.has("italic")) style = style.setItalic(styleNode.get("italic").asBoolean());
        if (styleNode.has("underline")) style = style.setItalic(styleNode.get("underline").asBoolean());
        if (styleNode.has("fontSize")) style = style.setFontSize(styleNode.get("fontSize").asInt());
        if (styleNode.has("textColor")) style = style.setTextColor(Color.valueOf(styleNode.get("textColor").asText()));
        if (styleNode.has("backgroundColor")) style = style.setBackgroundColor(styleNode.get("backgroundColor").asText().equals("transparent") ? Color.TRANSPARENT : Color.valueOf(styleNode.get("backgroundColor").asText()));
        if (styleNode.has("headingLevel")) style = style.setHeadingLevel(styleNode.get("headingLevel").asInt());
        return style;
    }

    public static ParStyle parseParStyle(JsonNode parStyleNode) {
        if (parStyleNode == null || parStyleNode.isNull()) {
            return ParStyle.EMPTY;
        }

        ParStyle style = ParStyle.EMPTY;

        if (parStyleNode.has("alignment")) {
            String alignment = parStyleNode.get("alignment").asText();
            switch (alignment.toLowerCase()) {
                case "center" -> style = style.setAlignment(TextAlignment.CENTER);
                case "right" -> style = style.setAlignment(TextAlignment.RIGHT);
                case "justify" -> style = style.setAlignment(TextAlignment.JUSTIFY);
                default -> style = style.setAlignment(TextAlignment.LEFT);
            }
        }

//        TODO: Add margin parser if needed (for now it's not implemented)
//        if (parStyleNode.has("marginTop")) {
//            int top = parStyleNode.get("marginTop").asInt();
//            style = style.set(top);
//        }
        if (parStyleNode.has("listType")) {
            String type = parStyleNode.get("listType").asText();
            ParStyle.ListType listType = switch (type){
                case "none" -> ParStyle.ListType.NONE;
                case "bullet" -> ParStyle.ListType.BULLET;
                case "numbered" -> ParStyle.ListType.NUMBERED;
                default -> ParStyle.ListType.NONE;
            };

            style = style.setListType(listType);
        }

        if (parStyleNode.has("listLevel")) {
            int level = parStyleNode.get("listLevel").asInt();
            style = style.setListLevel(level);
        }

        if (parStyleNode.has("lineSpacing")) {
            int lineSpacing = parStyleNode.get("lineSpacing").asInt();
            style = style.setLineSpacing(lineSpacing);
        }


        return style;
    }
}
