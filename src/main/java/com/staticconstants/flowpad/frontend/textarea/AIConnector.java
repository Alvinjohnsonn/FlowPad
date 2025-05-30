package com.staticconstants.flowpad.frontend.textarea;

import com.staticconstants.flowpad.backend.AI.AISavedMemory;
import com.staticconstants.flowpad.backend.AI.GeneratePrompt;
import com.staticconstants.flowpad.backend.AI.Prompt;
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
    private Prompt activePromptType;
    private int startIndex;
    private int endIndex;
    private AISavedMemory memory;


    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }

    public void setEndIndex(int endIndex) {
        this.endIndex = endIndex;
    }

    public AIConnector(TextAreaController textAreaController){
        this.textAreaController = textAreaController;
        this.textArea = textAreaController.getTextArea();
        this.previousVersion = "";
        this.selectedText = "";
        this.startIndex = 0;
        this.endIndex = 0;
    }

    public void setAdvancedResponse(boolean state){
        if (memory!=null)memory.setAdvancedResponse(state);
    }
    public void addPreviousAnswer(String answer){
        if (memory!=null)memory.addAnswer(answer);
    }
    public void sendQuery(CustomStyledArea<ParStyle, RichSegment, TextStyle> outputArea, String content){
        String prompt = GeneratePrompt.send(outputArea, getActivePromptType(), content);
        memory = new AISavedMemory(prompt);
        memory.setAdvancedResponse(getActivePromptType() == Prompt.FORMAT_WRITING || getActivePromptType() ==  Prompt.AI_HIGHLIGHT || getActivePromptType() == Prompt.GENERATE_OUTLINE);
    }
    public void sendOptionalRequest(CustomStyledArea<ParStyle, RichSegment, TextStyle> outputArea, String content, String request){
        GeneratePrompt.sendOptionalRequest(outputArea,request,memory, memory.isAdvancedResponse());
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

                startIndex = start;
                endIndex = end;

                showSelectConfirmation(false);
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

                startIndex = textArea.getSelection().getStart();
                endIndex = textArea.getSelection().getEnd();

                showSelectConfirmation(false);
            }
        });
    }

    private void stopTrackingSelection(){
        textArea.setOnMouseReleased(null);
        textArea.deselect();
    }

    public String getAllText() {
        selectedText = textArea.getText();
        startIndex = -1;
        endIndex = -1;
        return selectedText;
    }

    public String getSelectedText() {
        selectedText = textArea.getSelectedText();
        return selectedText;
    }

    public void modifyText(String newText){

    }

    public Prompt getActivePromptType(){
        return activePromptType;
    }
    public void setActivePromptType(Prompt type){
        activePromptType = type;
    }

    public void showSelectConfirmation(boolean isSelectAll){
        Bounds boundsInScene = textArea.localToScene(textArea.getBoundsInLocal());
        double x = boundsInScene.getMinX();
        double y = boundsInScene.getMinY();
        MainEditorController.showSelectConfirmationPopup(x,y,selectedText, isSelectAll);
    }

}
