package com.staticconstants.flowpad.frontend.textarea;

import com.staticconstants.flowpad.FlowPadApplication;
import com.staticconstants.flowpad.frontend.MainEditorController;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.IndexRange;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import org.fxmisc.richtext.model.*;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.fxmisc.richtext.model.TwoDimensional.Bias.Forward;

public class TextAreaController {
    private MainEditorController scene;
    private boolean programmaticUpdate;
    private boolean desiredStyleChanged;
    private CustomStyledArea<ParStyle, RichSegment, TextStyle> textArea;
    private String userData;
    private TextStyle desiredStyle;
    private ParStyle desiredParStyle;
    private int lastStartCaretPosition;
    private int lastEndCaretPosition;
    private boolean suppressHyperlinkMonitoring;

    public TextAreaController(VBox editorContainer, String userData) {
        programmaticUpdate = false;
        desiredStyleChanged = false;
        suppressHyperlinkMonitoring = false;
        this.userData = userData;

        ParStyle initialParStyle = ParStyle.EMPTY;
        TextStyle initialTextStyle = TextStyle.EMPTY;
        desiredStyle = initialTextStyle;
        desiredParStyle = initialParStyle;

        BiConsumer<TextFlow, ParStyle> paragraphStyler = ParStyle::apply;

        Function<StyledSegment<RichSegment, TextStyle>, Node> nodeFactory = seg -> {
            RichSegment s = seg.getSegment();
            TextStyle style = seg.getStyle();

            if (s instanceof TextSegment textSeg) {
                return textSeg.createNode(style);
            }
            else if (s instanceof HyperlinkSegment linkSeg){
                return linkSeg.createNode(style);
            }
            else if (s instanceof ImageSegment imgSeg) {
                return imgSeg.createNode(style);
            }

            return new Text("?");
        };

        RichTextOps<RichSegment, TextStyle> segmentOps = new RichTextOps<RichSegment, TextStyle>();
        RichSegmentCodec richSegmentCodec = new RichSegmentCodec();
        TextStyleCodec textStyleCodec = new TextStyleCodec();
        ParStyleCodec parStyleCodec = new ParStyleCodec();

        textArea =  new CustomStyledArea<ParStyle, RichSegment, TextStyle>(
                initialParStyle,
                paragraphStyler,
                initialTextStyle,
                segmentOps,
                nodeFactory
        );
        textArea.setWrapText(true);
        textArea.setUserData(userData);
        textArea.setStyleCodecs(
                parStyleCodec,
                Codec.styledSegmentCodec(richSegmentCodec,textStyleCodec)
        );

        // For testing purposes
//        textArea.caretPositionProperty().addListener((obs, oldPos, newPos) -> {
//            System.out.println("Caret moved: " + newPos);
//            System.out.println("Current segment: " + getSegmentAt(newPos-1));
//        });

        CustomStyledArea<ParStyle, RichSegment, TextStyle> finaltextArea = textArea;
        textArea.setOnKeyTyped(event -> {
            if (event.isControlDown() && event.getCode() == KeyCode.V) {
                return;
            }

            int caretPosition = finaltextArea.getCaretPosition();
            if (caretPosition > 0) {
                finaltextArea.setStyle(caretPosition - 1, caretPosition, desiredStyle);
            }
        });

        textArea.setOnKeyPressed(event -> {
            if (event.isControlDown() && event.getCode() == KeyCode.V) {
                Clipboard clipboard = Clipboard.getSystemClipboard();
                if (clipboard.hasImage()) {
                    insertImage(clipboard.getImage());
                    event.consume();
                }
            }

            if (event.getCode() == KeyCode.BACK_SPACE) {
                int caretPosition = textArea.getCaretPosition();
                int paragraphIndex = textArea.offsetToPosition(caretPosition, TwoDimensional.Bias.Backward).getMajor();
                int paragraphStart = textArea.getAbsolutePosition(paragraphIndex, 0);

                if (caretPosition == paragraphStart) {
                    ParStyle style = textArea.getParagraph(paragraphIndex).getParagraphStyle();
                    ParStyle newStyle = style.decreaseListLevel(style.getListLevel());
                    textArea.setParagraphStyle(paragraphIndex, newStyle);
                    if (newStyle.getListType() != style.getListType()) {
                        updateListType(scene, newStyle);
                    }
                    textArea.refreshParagraphGraphics();
                    event.consume();
                }
            }
        });

        textArea.addEventFilter(KeyEvent.KEY_TYPED, event -> {
            if ("\t".equals(event.getCharacter())) {
                event.consume();
            }

        });

        textArea.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.TAB) {
                IndexRange selection = textArea.getSelection();
                int startPar = textArea.offsetToPosition(selection.getStart(), TwoDimensional.Bias.Backward).getMajor();
                int endPar = textArea.offsetToPosition(selection.getEnd(), TwoDimensional.Bias.Forward).getMajor();

                boolean isShift = event.isShiftDown();

                if (selection.getLength()==0){
                    ParStyle style = textArea.getParagraph(textArea.getCurrentParagraph()).getParagraphStyle();
                    ParStyle newStyle;
                    if (isShift) newStyle = style.decreaseListLevel(style.getListLevel());
                    else newStyle = style.increaseListLevel(style.getListLevel());

                    textArea.setParagraphStyle(textArea.getCurrentParagraph(), newStyle);
                    if (newStyle.getListType() != style.getListType()) updateListType(scene, newStyle);
                    textArea.refreshParagraphGraphics();
                    event.consume();
                    return;
                }

                boolean anyChanged = false;

                for (int i = startPar; i <= endPar; i++) {
                    ParStyle style = textArea.getParagraph(i).getParagraphStyle();
                    if (style.getListType() != ParStyle.ListType.NONE) {
                        ParStyle updated = isShift
                                ? style.decreaseListLevel(style.getListLevel())
                                : style.increaseListLevel(style.getListLevel());
                        textArea.setParagraphStyle(i, updated);
                        anyChanged = true;
                    }
                }

                if (anyChanged) {
                    updateListType(scene, textArea.getParagraph(textArea.getCurrentParagraph()).getParagraphStyle());
                    textArea.refreshParagraphGraphics();
                    event.consume();
                }
            }

            if (event.getCode() == KeyCode.ENTER) {
                int caretPosition = textArea.getCaretPosition();
                if (caretPosition < 8) return;

                RichSegment currentSegment = MainEditorController.getSegmentAt(this, caretPosition-1);
                if (currentSegment instanceof HyperlinkSegment){
                    return;
                }

                int currentParagraphIndex = textArea.getCurrentParagraph();
                if (currentParagraphIndex < 0) return;

                String paragraphText = textArea.getParagraph(currentParagraphIndex).getText();


                Pattern urlPattern = Pattern.compile("https://[\\w\\-./?%&=]+\\.(com|org|net|edu|gov|io|co|id|au)(/\\S*)?");
                Matcher matcher = urlPattern.matcher(paragraphText);

                int lastMatchStart = -1;
                int lastMatchEnd = -1;
                String lastUrl = null;

                while (matcher.find()) {
                    lastMatchStart = matcher.start();
                    lastMatchEnd = matcher.end();
                    lastUrl = matcher.group();
                }

                if (lastUrl != null && caretPosition == textArea.getAbsolutePosition(currentParagraphIndex, lastMatchEnd)) {
                    int startPos = textArea.getAbsolutePosition(currentParagraphIndex, lastMatchStart);
                    int endPos = textArea.getAbsolutePosition(currentParagraphIndex, lastMatchEnd);

                    textArea.replace(startPos, endPos,
                            new HyperlinkSegment(lastUrl, lastUrl), desiredStyle);
                    event.consume();
                }
            }
        });

        textArea.caretPositionProperty().addListener((obs, oldVal, newVal) -> {
            if (textArea.isFocused()) {
                if (textArea.getSelection().getLength()>0) {
                    lastStartCaretPosition = textArea.getSelection().getStart();
                    lastEndCaretPosition = textArea.getSelection().getEnd();
                }
                else {
                    lastStartCaretPosition = newVal;
                    lastEndCaretPosition = -1;
                }
            }
        });


//        TODO: Fix unwanted conversion from deleting char just before or after hyperlink
        textArea.plainTextChanges()
                .subscribe(change -> {
                    if (suppressHyperlinkMonitoring) return;

                    int index = change.getPosition();
                    int removed = change.getRemoved().length();

                    if (removed > 0) {
                        int deletionStart = index;
                        int deletionEnd = index + removed;

                        int paragraphIdx = textArea.offsetToPosition(deletionStart, Forward).getMajor();
                        Paragraph<ParStyle, RichSegment, TextStyle> paragraph = textArea.getParagraph(paragraphIdx);
                        int paragraphStart = textArea.getAbsolutePosition(paragraphIdx, 0);
                        int runningOffset = paragraphStart;

                        for (RichSegment segment : paragraph.getSegments()) {
                            int segLen = segment.length();
                            int segStart = runningOffset;
                            int segEnd = segStart + segLen;


                            if (segment instanceof HyperlinkSegment hyperlink &&
                                    deletionStart <= segEnd && deletionEnd > segStart) {

                                TextSegment replacement = new TextSegment(hyperlink.getText());
                                textArea.replace(segStart, segEnd, replacement, textArea.getStyleAtPosition(segStart));
                                break;
                            }

                            runningOffset += segLen;
                        }
                    }
                });

        VBox.setVgrow(textArea, Priority.ALWAYS);
        editorContainer.getChildren().add(textArea);
    }


    public void initializeUpdateToolbar(MainEditorController scene){
        textArea.caretPositionProperty().addListener((obs, oldSel, newSel) -> {
            updateToolbar(scene);
            desiredStyleChanged = false;
//            TODO: Do more testing, doesn't always work
        });
        this.scene=scene;
    }

    public void reload(){
        desiredStyleChanged = false;
        programmaticUpdate = false;
        suppressHyperlinkMonitoring = false;
        Platform.runLater(() -> {
            textArea.requestFocus();

            if (lastEndCaretPosition==-1)
                textArea.moveTo(lastStartCaretPosition);
            else{
                textArea.selectRange(lastStartCaretPosition,lastEndCaretPosition);
            }

            updateToolbar(scene);
            desiredStyleChanged = false;
        });
    }

    public CustomStyledArea<ParStyle, RichSegment, TextStyle> getTextArea(){
        return textArea;
    }

    public boolean isDesiredStyleChanged(){
        return desiredStyleChanged;
    }

    public void setDesiredStyleChanged(boolean changed){
        desiredStyleChanged = changed;
    }

    public TextStyle getDesiredStyle(){
        return desiredStyle;
    }

    public ParStyle getDesiredParStyle(){
        return desiredParStyle;
    }

    public void setDesiredStyle(TextStyle newStyle){
        desiredStyle = newStyle;
    }

    public void setDesiredParStyle(ParStyle newStyle){
        desiredParStyle = newStyle;
    }

    public boolean isProgrammaticUpdate(){
        return programmaticUpdate;
    }

    public boolean getSuppressHyperlinkMonitoring(){
        return suppressHyperlinkMonitoring;
    }

    public void setSuppressHyperlinkMonitoring(boolean state){
        suppressHyperlinkMonitoring = state;
    }

    private void updateFontSizeField(TextField textFieldFontSize, TextStyle style) {
        IndexRange selection = textArea.getSelection();
        int fontSize = 0;

        if (selection.getLength() == 0) {
            fontSize = style.getFontSize();
        } else {
            List<Integer> sizes = new ArrayList<>();
            for (int i = selection.getStart(); i < selection.getEnd(); i++) {
                sizes.add(textArea.getStyleAtPosition(i).getFontSize());
            }
            fontSize = Collections.max(sizes);
        }

        programmaticUpdate = true;
        textFieldFontSize.setText(String.valueOf(fontSize));
        if (!desiredStyleChanged) {
            desiredStyle = desiredStyle.setFontSize(fontSize);
        }
        programmaticUpdate = false;
    }

    private void updateFormattingField(MainEditorController scene, TextStyle style){
        IndexRange selection = textArea.getSelection();
        int start = selection.getStart();
        int end = selection.getEnd();

        TextStyle referenceStyle;
        if (start == end) {
            referenceStyle = style;
        } else {
            referenceStyle = TextStyle.getStyleSelection(textArea, start, end);
        }

        programmaticUpdate = true;
        if (!desiredStyleChanged) {
            desiredStyle = new TextStyle(referenceStyle.isBold(), referenceStyle.isItalic(), referenceStyle.isUnderline(), desiredStyle.getFontSize(), desiredStyle.getFontFamily(), desiredStyle.getTextColor(), desiredStyle.getBackgroundColor(), desiredStyle.getHeadingLevel());
        }
        programmaticUpdate = false;

        scene.setSelectedButton(TextAttribute.BOLD, referenceStyle.isBold());
        scene.setSelectedButton(TextAttribute.ITALIC, referenceStyle.isItalic());
        scene.setSelectedButton(TextAttribute.UNDERLINE, referenceStyle.isUnderline());
        scene.setSelectedButton(TextAttribute.HIGHLIGHT, referenceStyle.getBackgroundColor().equals(Color.YELLOW));
//        TODO: Change "yellow" to the highlight color settings from LoggedIn user property
    }


    private void updateFontFamily(ComboBox fontComboBox, TextStyle style){
        String fontFamily = style.getFontFamily();

        programmaticUpdate = true;
        fontComboBox.getSelectionModel().select(fontFamily);
        programmaticUpdate = false;

        if (!desiredStyleChanged) {
            desiredStyle = desiredStyle.setFontFamily(fontFamily);
        }
    }

    private void updateHeadingLevel(ComboBox headingComboBox, TextStyle style){
        int headingLevel = style.getHeadingLevel();

        programmaticUpdate = true;
        for (Object opt : headingComboBox.getItems()) {
            HeadingOption option = (HeadingOption)opt;
            if (option.getLevel() == headingLevel) {
                headingComboBox.setValue(option);
                break;
            }
        }
        programmaticUpdate = false;

        if (!desiredStyleChanged) {
            desiredStyle = desiredStyle.setHeadingLevel(headingLevel);
        }
    }

    private void updateTextAlignment(MainEditorController scene, ParStyle parStyle){
        programmaticUpdate = true;
        if (!desiredStyleChanged) {
            desiredParStyle = parStyle;
        }

        String icon = switch (parStyle.getAlignment()){
            case LEFT -> "icons/text-align-left.png";
            case CENTER -> "icons/text-align-center.png";
            case RIGHT -> "icons/text-align-right.png";
            case JUSTIFY -> "icons/text-align-justify.png";
        };

        Image img = new Image(FlowPadApplication.class.getResource(icon).toExternalForm());
        scene.imgActiveAlignment.setImage(img);
        programmaticUpdate = false;
    }

    private void updateListType(MainEditorController scene, ParStyle parStyle){
        programmaticUpdate = true;
        if (!desiredStyleChanged) {
            desiredParStyle = parStyle;
        }

        switch (parStyle.getListType()){
            case NONE -> {
                scene.btnBulletList.getStyleClass().removeAll("active");
                scene.btnNumberedList.getStyleClass().removeAll("active");

            }
            case BULLET -> scene.btnBulletList.getStyleClass().add("active");
            case NUMBERED -> scene.btnNumberedList.getStyleClass().add("active");
        };

        programmaticUpdate = false;
    }

    public ParStyle getParStyleOnSelection(){
        int startPar = textArea.offsetToPosition(textArea.getSelection().getStart(), Forward).getMajor();
        return textArea.getParagraph(startPar).getParagraphStyle();
    }


    private void updateToolbar(MainEditorController scene){
        int caretPosition = textArea.getCaretPosition();
        TextStyle style = textArea.getStyleAtPosition(caretPosition > 0 ? caretPosition : caretPosition+1);

        ParStyle parStyle = getParStyleOnSelection();

        updateFontFamily(scene.fontComboBox, style);
        updateFontSizeField(scene.textFieldFontSize, style);
        updateFormattingField(scene, style);
        updateHeadingLevel(scene.headingComboBox, style);
        updateTextAlignment(scene, parStyle);
        updateListType(scene, parStyle);
    }

    private void insertImage(Image image) {
        if (image == null) return;

        int pos = textArea.getCaretPosition();

        TextStyle style = Optional.ofNullable(textArea.getStyleAtPosition(pos))
                .orElse(TextStyle.EMPTY);

        textArea.insert(pos, new ImageSegment(image), style);
        textArea.insert(pos + 1, new TextSegment(""), style);
        textArea.moveTo(pos+1);
    }

    public void setStyle(TextAttribute att, Object value){
        TextStyle newStyle = getDesiredStyle();
        switch(att){
            case BOLD -> newStyle = getDesiredStyle().setBold((Boolean) value);
            case ITALIC -> newStyle = getDesiredStyle().setItalic((Boolean) value);
            case UNDERLINE -> newStyle = getDesiredStyle().setUnderline((boolean)value);
            case FONT_SIZE -> newStyle = getDesiredStyle().setFontSize((int)value);
            case FONT_FAMILY -> newStyle = getDesiredStyle().setFontFamily((String)value);
            case TEXT_COLOR -> newStyle = getDesiredStyle().setTextColor((Color)value);
            case HIGHLIGHT -> newStyle = getDesiredStyle().setBackgroundColor((Color)value);
            case HEADING_LEVEL -> newStyle = getDesiredStyle().setHeadingLevel((int)value);
        }

        setDesiredStyle(newStyle);
        TextStyle.toggleStyle(getTextArea(), att, getDesiredStyle());
        setDesiredStyleChanged(true);
    }
}
