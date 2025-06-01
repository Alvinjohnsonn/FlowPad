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

/**
 * Handles the connection between the rich text area and AI-based features such as prompt generation,
 * selection tracking, and intelligent content insertion.
 */
public class AIConnector {
    private TextAreaController textAreaController;
    private CustomStyledArea<ParStyle, RichSegment, TextStyle> textArea;
    private String previousVersion;
    private String selectedText;
    private Prompt activePromptType;
    private int startIndex;
    private int endIndex;
    private AISavedMemory memory;

    private Integer hoveredParagraphIndex = null;

    /**
     * Constructs an AIConnector for the given text area controller.
     *
     * @param textAreaController the controller associated with the editor text area
     */
    public AIConnector(TextAreaController textAreaController){
        this.textAreaController = textAreaController;
        this.textArea = textAreaController.getTextArea();
        this.previousVersion = "";
        this.selectedText = "";
        this.startIndex = 0;
        this.endIndex = 0;
    }

    /**
     * Returns the start index of the current selection or AI-tracked range.
     */
    public int getStartIndex() {
        return startIndex;
    }

    /**
     * Sets the start index of the AI-tracked selection range.
     */
    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    /**
     * Returns the end index of the current selection or AI-tracked range.
     */
    public int getEndIndex() {
        return endIndex;
    }

    /**
     * Sets the end index of the AI-tracked selection range.
     */
    public void setEndIndex(int endIndex) {
        this.endIndex = endIndex;
    }

    /**
     * Sets whether the AI should use advanced response logic when replying.
     */
    public void setAdvancedResponse(boolean state){
        if (memory != null) memory.setAdvancedResponse(state);
    }

    /**
     * Adds a previous answer to the AI memory context.
     */
    public void addPreviousAnswer(String answer){
        if (memory != null) memory.addAnswer(answer);
    }

    /**
     * Sends a predefined query to the AI based on the active prompt type and editor content.
     *
     * @param outputArea the area from which the query context is generated
     * @param content the selected text to send as context
     */
    public void sendQuery(CustomStyledArea<ParStyle, RichSegment, TextStyle> outputArea, String content){
        String prompt = GeneratePrompt.send(outputArea, getActivePromptType(), content);
        memory = new AISavedMemory(prompt);
        memory.setAdvancedResponse(getActivePromptType() == Prompt.FORMAT_WRITING ||
                getActivePromptType() == Prompt.AI_HIGHLIGHT ||
                getActivePromptType() == Prompt.GENERATE_OUTLINE);
    }

    /**
     * Sends a custom prompt string to the AI using the given text and prompt content.
     *
     * @param outputArea the editor context
     * @param content the user input text
     * @param customPrompt the user-defined AI prompt
     */
    public void sendCustomPrompt(CustomStyledArea<ParStyle, RichSegment, TextStyle> outputArea, String content, String customPrompt){
        memory = new AISavedMemory(customPrompt);
        memory.setInitialPrompt(GeneratePrompt.customPrompt(outputArea, content, customPrompt));
    }

    /**
     * Sends an optional user request to the AI using the current memory.
     *
     * @param outputArea the editor context
     * @param request the optional request to send
     */
    public void sendOptionalRequest(CustomStyledArea<ParStyle, RichSegment, TextStyle> outputArea, String request){
        GeneratePrompt.sendOptionalRequest(outputArea, request, memory, memory.isAdvancedResponse());
    }

    /**
     * Begins tracking hover behavior over paragraphs to allow paragraph-based selection.
     */
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

        textArea.setOnMouseExited(event -> textArea.deselect());

//        textArea.getScene().setOnKeyPressed(event -> {
//            if (event.getCode() == KeyCode.ESCAPE) {
//                cancelOperation();
//            }
//        });

        textArea.setOnMouseClicked(event -> {
            if (hoveredParagraphIndex != null) {
                int start = textArea.getAbsolutePosition(hoveredParagraphIndex, 0);
                int end = start + textArea.getParagraph(hoveredParagraphIndex).length();
                selectedText = textArea.getText(start, end);
                startIndex = start;
                endIndex = end;
                showSelectConfirmation(false);
            }
        });
    }

    /**
     * Cancels all selection and hover-based operations.
     */
    public void cancelOperation(){
        stopHighlightOnHover();
        stopTrackingSelection();
    }

    /**
     * Stops paragraph-hover highlighting.
     */
    private void stopHighlightOnHover(){
        textArea.setOnMouseMoved(null);
        textArea.setOnMouseExited(null);
        textArea.setOnMouseClicked(null);
//        textArea.getScene().setOnKeyPressed(null);
        textArea.deselect();
    }

    /**
     * Begins tracking user selections via mouse releases.
     */
    public void startTrackingSelection(){
        textArea.setOnMouseReleased(e -> {
            if (!textArea.getSelectedText().isEmpty()){
                selectedText = textArea.getSelectedText();
                startIndex = textArea.getSelection().getStart();
                endIndex = textArea.getSelection().getEnd();
                showSelectConfirmation(false);
            }
        });
    }

    /**
     * Stops tracking selection behavior.
     */
    private void stopTrackingSelection(){
        textArea.setOnMouseReleased(null);
        textArea.deselect();
    }

    /**
     * Returns the entire text content of the editor.
     *
     * @return all text in the editor
     */
    public String getAllText() {
        selectedText = textArea.getText();
        startIndex = -1;
        endIndex = -1;
        return selectedText;
    }

    /**
     * Returns the currently selected text in the editor.
     *
     * @return selected text
     */
    public String getSelectedText() {
        selectedText = textArea.getSelectedText();
        return selectedText;
    }

    /**
     * Replaces selected content or a portion of text with new AI-generated content.
     * (Implementation pending)
     *
     * @param newText the new text to insert
     */
    public void modifyText(String newText){
        // Implementation to be added
    }

    /**
     * Gets the currently active prompt type for the AI interaction.
     *
     * @return the active prompt type
     */
    public Prompt getActivePromptType(){
        return activePromptType;
    }

    /**
     * Sets the active prompt type used for AI generation.
     *
     * @param type the prompt type to activate
     */
    public void setActivePromptType(Prompt type){
        activePromptType = type;
    }

    /**
     * Displays a confirmation popup allowing the user to confirm selected text submission to the AI.
     *
     * @param isSelectAll whether the selection includes the entire document
     */
    public void showSelectConfirmation(boolean isSelectAll){
        Bounds boundsInScene = textArea.localToScene(textArea.getBoundsInLocal());
        double x = boundsInScene.getMinX();
        double y = boundsInScene.getMinY();
        MainEditorController.showSelectConfirmationPopup(x, y, selectedText, isSelectAll);
    }
}
