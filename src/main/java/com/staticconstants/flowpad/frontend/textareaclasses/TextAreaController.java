package com.staticconstants.flowpad.frontend.textareaclasses;

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
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.*;
import org.fxmisc.richtext.TextExt;
import org.fxmisc.richtext.model.StyledSegment;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class TextAreaController {
    private MainEditorController scene;
    private boolean programmaticFontUpdate;
    private boolean desiredStyleChanged;
    private CustomStyledArea<ParStyle, RichSegment, TextStyle> textArea;
    private String userData;
    private TextStyle desiredStyle;
    private int lastStartCaretPosition;
    private int lastEndCaretPosition;

    public TextAreaController(VBox editorContainer, String userData) {
        programmaticFontUpdate =false;
        desiredStyleChanged =false;
        this.userData = userData;

        ParStyle initialParStyle = ParStyle.EMPTY;
        desiredStyle = TextStyle.EMPTY;
        TextStyle initialTextStyle = TextStyle.EMPTY;

        BiConsumer<TextFlow, ParStyle> paragraphStyler = (tf, p) -> {};

        Function<StyledSegment<RichSegment, TextStyle>, Node> nodeFactory = seg -> {
            RichSegment s = seg.getSegment();
            TextStyle style = seg.getStyle();

            if (s instanceof TextSegment textSeg) {
                TextExt text = new TextExt(textSeg.getText());
                String fontFamily = style.getFontFamily();

                FontWeight weight = style.isBold() ? FontWeight.BOLD : FontWeight.NORMAL;
                FontPosture posture = style.isItalic() ? FontPosture.ITALIC : FontPosture.REGULAR;

                Font font = Font.font(fontFamily, weight, posture, style.getFontSize());
                text.setFont(font);

                text.setUnderline(style.isUnderline());

                // Add later
//                style.getTextColor().ifPresent(text::setFill); // assuming getTextColor() returns Optional<Paint>

                return text;

            } else if (s instanceof ImageSegment imgSeg) {
                ImageView view = new ImageView(imgSeg.getImage());
                view.setFitWidth(200);
                view.setPreserveRatio(true);
                return view;
            }

            return new Text("?");
        };

        RichTextOps<RichSegment, TextStyle> segmentOps = new RichTextOps<RichSegment, TextStyle>();


        textArea =  new CustomStyledArea<ParStyle, RichSegment, TextStyle>(
                initialParStyle,
                paragraphStyler,
                initialTextStyle,
                segmentOps,
                nodeFactory
        );
        textArea.setWrapText(true);
        textArea.setUserData(userData);

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
            updateFontSizeFieldFromSelection(scene.textFieldFontSize);
            updateFormattingFieldFromSelection(scene);
            updateFontFamilyFromSelection(scene.fontComboBox);
            desiredStyleChanged = false;
//            TODO: Do more testing, doesn't always work
        });
        this.scene=scene;
    }

    public void reload(){
        desiredStyleChanged = false;
        programmaticFontUpdate = false;

        Platform.runLater(() -> {
            textArea.requestFocus();

            if (lastEndCaretPosition==-1)
                textArea.moveTo(lastStartCaretPosition);
            else{
                textArea.selectRange(lastStartCaretPosition,lastEndCaretPosition);
            }

            updateFontSizeFieldFromSelection(scene.textFieldFontSize);
            updateFormattingFieldFromSelection(scene);
            updateFontFamilyFromSelection(scene.fontComboBox);
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

    public void setDesiredStyle(TextStyle newStyle){
        desiredStyle = newStyle;
    }

    public boolean isProgrammaticFontUpdate(){
        return programmaticFontUpdate;
    }

    private void updateFontSizeFieldFromSelection(TextField textFieldFontSize) {
        IndexRange selection = textArea.getSelection();
        int fontSize = 0;

        if (selection.getLength() == 0) {
            int caretPosition = textArea.getCaretPosition();
            TextStyle style = textArea.getStyleAtPosition(caretPosition > 0 ? caretPosition : caretPosition + 1);
            fontSize = style.getFontSize();
        } else {
            List<Integer> sizes = new ArrayList<>();
            for (int i = selection.getStart(); i < selection.getEnd(); i++) {
                sizes.add(textArea.getStyleAtPosition(i).getFontSize());
            }
            fontSize = Collections.max(sizes);
        }

        programmaticFontUpdate = true;
        textFieldFontSize.setText(String.valueOf(fontSize));
        if (!desiredStyleChanged) {
            desiredStyle = desiredStyle.setFontSize(fontSize);
        }
        programmaticFontUpdate = false;
    }

    private void updateFormattingFieldFromSelection(MainEditorController scene){
        IndexRange selection = textArea.getSelection();
        int start = selection.getStart();
        int end = selection.getEnd();

        int posToCheck = (start > 0) ? start : start+1;

        TextStyle referenceStyle;
        if (start == end) {
            referenceStyle = textArea.getStyleAtPosition(posToCheck);
        } else {
            referenceStyle = TextStyle.getStyleSelection(textArea, start, end);
        }

        programmaticFontUpdate = true;
        if (!desiredStyleChanged) {
            desiredStyle = new TextStyle(referenceStyle.isBold(), referenceStyle.isItalic(), referenceStyle.isUnderline(), desiredStyle.getFontSize(), desiredStyle.getFontFamily(), desiredStyle.getBackgroundColor() );
        }
        programmaticFontUpdate = false;

        scene.setSelectedButton(TextAttribute.BOLD, referenceStyle.isBold());
        scene.setSelectedButton(TextAttribute.ITALIC, referenceStyle.isItalic());
        scene.setSelectedButton(TextAttribute.UNDERLINE, referenceStyle.isUnderline());
    }


    private void updateFontFamilyFromSelection(ComboBox fontComboBox){
        int caretPosition = textArea.getCaretPosition();
        TextStyle style = textArea.getStyleAtPosition(caretPosition > 0 ? caretPosition : caretPosition+1);
        String fontFamily = style.getFontFamily();

        programmaticFontUpdate = true;
        fontComboBox.getSelectionModel().select(fontFamily);
        programmaticFontUpdate = false;

        if (!desiredStyleChanged) {
            desiredStyle = desiredStyle.setFontFamily(fontFamily);
        }
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
}
