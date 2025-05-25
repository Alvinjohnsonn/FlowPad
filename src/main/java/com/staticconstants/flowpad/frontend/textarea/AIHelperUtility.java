package com.staticconstants.flowpad.frontend.textarea;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.io.IOException;

public class AIHelperUtility {
    private CustomStyledArea<ParStyle, RichSegment, TextStyle> textArea;

    public AIHelperUtility(CustomStyledArea<ParStyle,RichSegment,TextStyle> textArea){
        this.textArea = textArea;
    }

//    public void insertParagraph(List<RichSegment> segments, ParStyle style) {
//        textArea.append(style, segments);
//    }
    public void insertText(String text, TextStyle style) {
        RichSegment segment = new TextSegment(text);
        textArea.append(segment, style);
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
            String type = node.get("type").asText();
            switch (type) {
                case "text" -> {
                    String content = node.get("content").asText();
                    JsonNode styleNode = node.get("style");
                    TextStyle style = parseStyle(styleNode);
                    insertText(content, style);
                }
                case "image" -> insertImage(new Image(node.get("url").asText(), true));
                case "hyperlink" -> insertHyperlink(node.get("text").asText(), node.get("url").asText());
            }
        }
    }

    public void applyJsonInstruction(JsonNode node) throws IOException {
        String type = node.get("type").asText();
        switch (type) {
            case "text" -> {
                String content = node.get("content").asText();
                JsonNode styleNode = node.get("style");
                TextStyle style = parseStyle(styleNode);
                insertText(content, style);
            }
            case "image" -> insertImage(new Image(node.get("url").asText(), true));
            case "hyperlink" -> insertHyperlink(node.get("text").asText(), node.get("url").asText());
        }
    }

    private TextStyle parseStyle(JsonNode styleNode) {
        TextStyle style = TextStyle.EMPTY;
        if (styleNode.has("bold")) style = style.setBold(styleNode.get("bold").asBoolean());
        if (styleNode.has("italic")) style = style.setItalic(styleNode.get("italic").asBoolean());
        if (styleNode.has("underline")) style = style.setItalic(styleNode.get("underline").asBoolean());
        if (styleNode.has("fontSize")) style = style.setFontSize(styleNode.get("fontSize").asInt());
        if (styleNode.has("textColor")) style = style.setTextColor(Color.valueOf(styleNode.get("textColor").asText()));
        if (styleNode.has("backgroundColor")) style = style.setBackgroundColor(Color.valueOf(styleNode.get("backgroundColor").asText()));
        if (styleNode.has("headingLevel")) style = style.setHeadingLevel(styleNode.get("headingLevel").asInt());
        return style;
    }
}
