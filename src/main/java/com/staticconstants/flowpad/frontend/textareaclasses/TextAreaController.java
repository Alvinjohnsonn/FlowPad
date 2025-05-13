package com.staticconstants.flowpad.frontend.textareaclasses;

import com.staticconstants.flowpad.FlowPadApplication;
import com.staticconstants.flowpad.frontend.MainEditorController;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.IndexRange;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.*;
import org.fxmisc.richtext.model.Codec;
import org.fxmisc.richtext.model.StyledSegment;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

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

    public TextAreaController(VBox editorContainer, String userData) {
        programmaticUpdate =false;
        desiredStyleChanged =false;
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

            } else if (s instanceof ImageSegment imgSeg) {
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
                updateListType(scene, getParStyleOnSelection());
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
            desiredStyle = new TextStyle(referenceStyle.isBold(), referenceStyle.isItalic(), referenceStyle.isUnderline(), desiredStyle.getFontSize(), desiredStyle.getFontFamily(), desiredStyle.getBackgroundColor(), desiredStyle.getHeadingLevel());
        }
        programmaticUpdate = false;

        scene.setSelectedButton(TextAttribute.BOLD, referenceStyle.isBold());
        scene.setSelectedButton(TextAttribute.ITALIC, referenceStyle.isItalic());
        scene.setSelectedButton(TextAttribute.UNDERLINE, referenceStyle.isUnderline());
        scene.setSelectedButton(TextAttribute.HIGHLIGHT, referenceStyle.getBackgroundColor().equals("yellow"));
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
            case HIGHLIGHT -> newStyle = getDesiredStyle().setBackgroundColor((String)value);
            case HEADING_LEVEL -> newStyle = getDesiredStyle().setHeadingLevel((int)value);
        }

        setDesiredStyle(newStyle);
        TextStyle.toggleStyle(getTextArea(), att, getDesiredStyle());
        setDesiredStyleChanged(true);
    }
}
