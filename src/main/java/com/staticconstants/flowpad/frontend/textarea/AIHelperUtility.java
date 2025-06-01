package com.staticconstants.flowpad.frontend.textarea;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import org.fxmisc.richtext.model.TwoDimensional;

import java.io.IOException;

/**
 * A utility class that assists with inserting rich content (text, images, hyperlinks) into a {@link CustomStyledArea}.
 * It also provides functionality to parse structured JSON input (e.g. from AI) and apply content and styles accordingly.
 */
public class AIHelperUtility {
    private CustomStyledArea<ParStyle, RichSegment, TextStyle> textArea;

    /**
     * Constructs a new AIHelperUtility bound to the given {@link CustomStyledArea}.
     *
     * @param textArea the styled area where content will be inserted
     */
    public AIHelperUtility(CustomStyledArea<ParStyle, RichSegment, TextStyle> textArea) {
        this.textArea = textArea;
    }

    /**
     * Appends a plain text segment to the editor with the specified style.
     *
     * @param text  the text content to insert
     * @param style the {@link TextStyle} to apply to the text
     */
    public void insertText(String text, TextStyle style) {
        RichSegment segment = new TextSegment(text+" ");
        textArea.append(segment, style);
    }

    /**
     * Appends a paragraph with both text and paragraph style to the editor.
     *
     * @param text     the text content of the paragraph
     * @param style    the {@link TextStyle} for text formatting
     * @param parStyle the {@link ParStyle} for paragraph formatting (alignment, list type, etc.)
     */
    public void insertParagraph(String text, TextStyle style, ParStyle parStyle) {
        int insertPos = textArea.getLength();

        RichSegment segment = new TextSegment(text);
        textArea.insert(insertPos, segment, style);

        textArea.insertText(insertPos + text.length(), "\n");

        int paragraphIndex = textArea.offsetToPosition(insertPos, TwoDimensional.Bias.Forward).getMajor();
        textArea.setParagraphStyle(paragraphIndex, parStyle);
    }

    /**
     * Inserts an image into the editor.
     *
     * @param image the JavaFX {@link Image} to insert
     */
    public void insertImage(Image image) {
        RichSegment imageSegment = new ImageSegment(image);
        textArea.append(imageSegment, TextStyle.EMPTY);
    }

    /**
     * Inserts a hyperlink segment with the given display text and URL.
     *
     * @param text the visible text
     * @param url  the hyperlink URL
     */
    public void insertHyperlink(String text, String url) {
        RichSegment hyperlinkSegment = new HyperlinkSegment(text, url);
        textArea.append(hyperlinkSegment, TextStyle.EMPTY);
    }

    /**
     * Applies a sequence of JSON instructions to the editor.
     * Each node in the array should specify a type ("text", "image", or "hyperlink") and relevant content and style.
     *
     * @param nodes the root array node containing content instructions
     * @throws IOException if a node is malformed
     */
    public void applyJsonInstructions(JsonNode nodes) throws IOException {
        for (JsonNode node : nodes) {
            applyJsonInstruction(node);
        }
    }

    /**
     * Applies a single JSON instruction to the editor.
     * Supports types: "text", "image", "hyperlink".
     *
     * @param node the JSON node representing one content block
     * @throws IOException if the node is invalid
     */
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

    /**
     * Parses a JSON node into a {@link TextStyle} object.
     *
     * @param styleNode the JSON node representing style attributes
     * @return the resulting {@link TextStyle}, or {@link TextStyle#EMPTY} if null or invalid
     */
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

    /**
     * Parses a JSON node into a {@link ParStyle} object.
     *
     * @param parStyleNode the JSON node representing paragraph style
     * @return the resulting {@link ParStyle}, or {@link ParStyle#EMPTY} if null or invalid
     */
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

        // TODO: Add margin parser if needed in the future

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
