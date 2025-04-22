package com.staticconstants.flowpad.controller;

import javafx.application.Platform;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.fxmisc.richtext.InlineCssTextArea;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MainEditorController {
    @FXML private TreeView<String> folderTree;
    @FXML private VBox aiOptions;
    @FXML private Button btnDocuments;
    @FXML private Button btnAi;
    @FXML private Slider zoomSlider;
    @FXML private Label zoomLabel;
    @FXML private Button btnMinus;
    @FXML private Button btnPlus;
    @FXML private TextField textFieldFontSize;
    @FXML private VBox editorContainer;
    private InlineCssTextArea richTextArea;
    private boolean isProgrammaticFontUpdate = false;
    private int fontSize = 12;


    @FXML
    private void showDocuments() {
        setActiveButton(btnDocuments, btnAi);

        folderTree.setVisible(true);
        folderTree.setManaged(true);

        aiOptions.setVisible(false);
        aiOptions.setManaged(false);
    }

    @FXML
    private void showAIOptions() {
        setActiveButton(btnAi, btnDocuments);

        folderTree.setVisible(false);
        folderTree.setManaged(false);

        aiOptions.setVisible(true);
        aiOptions.setManaged(true);
    }

    @FXML
    public void initialize() {
        TreeItem<String> rootItem = new TreeItem<>("Notes");
        rootItem.setExpanded(true);

        TreeItem<String> personalNotes = new TreeItem<>("CAB302");
        personalNotes.getChildren().addAll(
                new TreeItem<>("Week 1"),
                new TreeItem<>("Week 2")
        );

        TreeItem<String> workNotes = new TreeItem<>("CAB103");
        workNotes.getChildren().addAll(
                new TreeItem<>("Week 1"),
                new TreeItem<>("Week 2")
        );

        rootItem.getChildren().addAll(personalNotes, workNotes);

        folderTree.setRoot(rootItem);

        zoomSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int zoomPercent = newVal.intValue();
            zoomLabel.setText(zoomPercent + "%");
        });


        richTextArea = new InlineCssTextArea();
        richTextArea.setWrapText(true);
        richTextArea.setStyle("-fx-font-size: 12px; -fx-font-family: Arial");

        VBox.setVgrow(richTextArea, Priority.ALWAYS);
        editorContainer.getChildren().add(richTextArea);

        textFieldFontSize.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getText();
            return newText.matches("[0-9]*") ? change : null;
        }));

        textFieldFontSize.textProperty().addListener((obs, oldText, newText) -> {
            if (isProgrammaticFontUpdate) return;
            handleFontSizeChange(newText);
        });

        textFieldFontSize.setOnAction(e -> {
            handleFontSizeChange(textFieldFontSize.getText());
        });

        richTextArea.selectionProperty().addListener((obs, oldSel, newSel) -> {
            updateFontSizeFieldFromSelection();

//            TODO: Do more testing, doesn't always work
        });

//        TODO: Change background color and text color when richtextarea is selected
    }


//    TODO: Add active and inactive formatting button states like bold, italic, etc

    private void setActiveButton(Button active, Button inactive) {
        if (!active.getStyleClass().contains("selected")) active.getStyleClass().add("selected");

        inactive.getStyleClass().remove("selected");
    }

    @FXML
    private void increaseFontSize() {
        fontSize++;
        textFieldFontSize.setText(fontSize + "");
    }

    @FXML
    private void decreaseFontSize() {
        if (fontSize > 1) {
            fontSize--;
            textFieldFontSize.setText(fontSize + "");
        }
    }

    @FXML
    private void bold(){
        addStyle(richTextArea, "-fx-font-weight", "bold");
        applyStyleAtCaret("-fx-font-weight: bold");
    }
    @FXML
    private void italic(){
        addStyle(richTextArea, "-fx-font-style", "italic");
        applyStyleAtCaret("-fx-font-style: italic");
    }
    @FXML
    private void underline(){
        addStyle(richTextArea, "-fx-underline", "true");
       applyStyleAtCaret ("-fx-underline: true");
    }

    @FXML
    private void handleFontSizeChange(String size){
        addStyle(richTextArea, "-fx-font-size", size+"px");
        applyStyleAtCaret("-fx-font-size: "+size+"px;");
    }

    private void applyStyleAtCaret(String style) {
        int caretPos = richTextArea.getCaretPosition();

        Platform.runLater(() -> {
            // Restore focus just in case
            richTextArea.requestFocus();
            // Apply style at the caret
            richTextArea.setStyle(caretPos, caretPos+1, style);

//            TODO: Fix last caret position formatting, doesn't apply to the next text written
        });
    }

    private void updateFontSizeFieldFromSelection() {
        IndexRange selection = richTextArea.getSelection();

        if (selection.getLength() == 0) {
            return;
        }

        List<String> styles = new ArrayList<>();

        for (int i = selection.getStart(); i < selection.getEnd(); i++) {
            String style = richTextArea.getStyleOfChar(i);
            styles.add(style);
        }

        OptionalInt maxFontSize = styles.stream()
                .map(this::extractFontSize)
                .filter(OptionalInt::isPresent)
                .mapToInt(OptionalInt::getAsInt)
                .max();

        isProgrammaticFontUpdate = true;
        maxFontSize.ifPresent(size ->
                textFieldFontSize.setText(String.valueOf(size)));
        isProgrammaticFontUpdate = false;
    }

    private OptionalInt extractFontSize(String style) {
        try {
            Pattern pattern = Pattern.compile("-fx-font-size\\s*:\\s*(\\d+)px");
            Matcher matcher = pattern.matcher(style);
            if (matcher.find()) {
                return OptionalInt.of(Integer.parseInt(matcher.group(1)));
            }
        } catch (Exception e) {
            // ignore
        }
        return OptionalInt.empty();
    }

    private String getStyleValue(InlineCssTextArea area, String styleKey){
        String styleValue = "";
        String currentStyle = area.getStyleOfChar(area.getSelection().getStart());

        String[] styles = currentStyle.split(";");
        for (String style : styles){
            if (style.contains(styleKey)){
                styleValue = style.split(":")[1];
                break;
            }
        }
        return styleValue;  // if return empty then style is not set
    }


    private void addStyle(InlineCssTextArea area, String styleKey, String valueToToggle) {
        int start = area.getSelection().getStart();
        int end = area.getSelection().getEnd();

        for (int i = start; i < end; i++) {
            String currentStyle = area.getStyleOfChar(i);
            String updatedStyle = updateStyle(currentStyle, styleKey, valueToToggle);
            area.setStyle(i, i + 1, updatedStyle);
        }
    }

    private String updateStyle(String currentStyle, String key, String value) {
        Map<String, String> styleMap = new HashMap<>();

        for (String style : currentStyle.split(";")) {
            if (style.contains(":")) {
                String[] parts = style.trim().split(":");
                styleMap.put(parts[0].trim(), parts[1].trim());
            }
        }

        if (value.equals(styleMap.get(key))) {
            styleMap.remove(key);
        } else {
            styleMap.remove(key);
            styleMap.put(key, value);
        }

        return styleMap.entrySet()
                .stream()
                .map(e -> e.getKey() + ": " + e.getValue())
                .collect(Collectors.joining("; "));
    }


}
