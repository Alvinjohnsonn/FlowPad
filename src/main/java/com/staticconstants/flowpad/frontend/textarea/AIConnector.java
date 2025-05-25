package com.staticconstants.flowpad.frontend.textarea;

import com.staticconstants.flowpad.frontend.MainEditorController;
import javafx.geometry.Bounds;
import javafx.scene.input.KeyCode;
import org.fxmisc.richtext.model.Paragraph;

import java.util.OptionalInt;

import static org.fxmisc.richtext.model.TwoDimensional.Bias.Forward;

public class AIConnector {
    private TextAreaController textAreaController;
    private CustomStyledArea<ParStyle, RichSegment, TextStyle> textArea;
    private String previousVersion;
    private String selectedText;


    public AIConnector(TextAreaController textAreaController){
        this.textAreaController = textAreaController;
        this.textArea = textAreaController.getTextArea();
        this.previousVersion = "";
        this.selectedText = "";
    }

    private Integer hoveredParagraphIndex = null;
    public void startHighlightParagraphOnHover() {
        textArea.setOnMouseMoved(event -> {
            OptionalInt charIdx = textArea.hit(event.getX(), event.getY()).getCharacterIndex();

            if (charIdx.isPresent()) {
                int pos = charIdx.getAsInt();
                int paragraphIndex = textArea.offsetToPosition(pos, Forward).getMajor();
                hoveredParagraphIndex = paragraphIndex;
                int paragraphLength = textArea.getParagraph(paragraphIndex).length();

                int paragraphStart = textArea.getAbsolutePosition(paragraphIndex, 0);
                int paragraphEnd = paragraphStart + paragraphLength;

                textArea.selectRange(paragraphStart, paragraphEnd);
            }
        });

        textArea.setOnMouseExited(event -> {
            textArea.deselect();
        });

        textArea.getScene().setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                cancelOperation();
            }
        });

        textArea.setOnMouseClicked(event -> {
            if (hoveredParagraphIndex!=null) {
                int start = textArea.getAbsolutePosition(hoveredParagraphIndex, 0);
                int end = start + textArea.getParagraph(hoveredParagraphIndex).length();
                selectedText = textArea.getText(start, end);

                showSelectConfirmation();
            }
        });
    }

    public void cancelOperation(){
        stopHighlightOnHover();
        stopTrackingSelection();
    }

    private void stopHighlightOnHover(){
        textArea.setOnMouseMoved(null);
        textArea.setOnMouseExited(null);
        textArea.setOnMouseClicked(null);
        textArea.getScene().setOnKeyPressed(null);

        textArea.deselect();
    }

    public void startTrackingSelection(){
        textArea.setOnMouseReleased(e->{
            if (!textArea.getSelectedText().isEmpty()){
                selectedText = textArea.getSelectedText();
                showSelectConfirmation();
            }
        });
    }

    private void stopTrackingSelection(){
        textArea.setOnMouseReleased(null);
        textArea.deselect();
    }

    public String getAllText() {
        selectedText = textArea.getText();
        return selectedText;
    }

    public String getSelectedText() {
        selectedText = textArea.getSelectedText();
        return selectedText;
    }

    public void modifyText(String newText){

    }

    private void showSelectConfirmation(){
        Bounds boundsInScene = textArea.localToScene(textArea.getBoundsInLocal());
        double x = boundsInScene.getMinX();
        double y = boundsInScene.getMinY();
        MainEditorController.showSelectConfirmationPopup(x,y,selectedText);
    }

}
