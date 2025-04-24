package com.staticconstants.flowpad.frontend;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
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
    @FXML private TabPane tabPane;
    @FXML private ComboBox fontComboBox;




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

        richTextArea.caretPositionProperty().addListener((obs, oldSel, newSel) -> {
            updateFontSizeFieldFromSelection();

//            TODO: Do more testing, doesn't always work
        });

//        TODO: Change background color and text color when richtextarea is selected


        fontComboBox.getItems().addAll(Font.getFamilies());
        fontComboBox.setOnAction(event -> {
            changeFontFamily();
        });
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
    private void changeFontFamily(){
        fontComboBox.getItems().addAll(Font.getFamilies());
        fontComboBox.setOnAction(event -> {
            String selectedFont = (String)fontComboBox.getValue();
            if (selectedFont != null) {
                addStyle(richTextArea, "-fx-font-family","'"+ selectedFont + "';");
            }
        });
    }
    @FXML
    private void handleFontSizeChange(String size){
        addStyle(richTextArea, "-fx-font-size", size+"px");
        applyStyleAtCaret("-fx-font-size: "+size+"px;");
    }

    private void applyStyleAtCaret(String style) {
        int caretPos = richTextArea.getCaretPosition();
        if (caretPos != richTextArea.getLength()) return;

        Platform.runLater(() -> {
            // Restore focus just in case
            richTextArea.requestFocus();
            // Apply style at the caret
            richTextArea.setStyle(caretPos, caretPos, style);

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


    private void addStyle(InlineCssTextArea area, String styleKey, String valueToToggle) {
        int start = area.getSelection().getStart();
        int end = area.getSelection().getEnd();

        boolean styleFullyApplied = true;

        // First pass: check if the style is fully applied
        for (int i = start; i < end; i++) {
            String currentStyle = area.getStyleOfChar(i);
            String value = getStyleValue(currentStyle, styleKey);
            if (!value.equals(valueToToggle)) {
                styleFullyApplied = false;
                break;
            }
        }

        // Second pass: apply or remove the style while preserving others
        for (int i = start; i < end; i++) {
            String currentStyle = area.getStyleOfChar(i);
            Map<String, String> styles = parseStyle(currentStyle);

            if (styleFullyApplied) {
                // Remove the styleKey if value matches
                styles.remove(styleKey);
            } else {
                styles.put(styleKey, valueToToggle);
            }

            // Rebuild and apply the new style
            StringBuilder newStyle = new StringBuilder();
            for (Map.Entry<String, String> entry : styles.entrySet()) {
                newStyle.append(entry.getKey()).append(": ").append(entry.getValue()).append("; ");
            }

            area.setStyle(i, i + 1, newStyle.toString().trim());
        }
    }

    private Map<String, String> parseStyle(String styleString) {
        Map<String, String> styles = new HashMap<>();
        if (styleString == null || styleString.isEmpty()) return styles;

        String[] declarations = styleString.split(";");
        for (String decl : declarations) {
            String[] parts = decl.trim().split(":", 2);
            if (parts.length == 2) {
                styles.put(parts[0].trim(), parts[1].trim());
            }
        }
        return styles;
    }

    private String getStyleValue(String styleString, String key) {
        Map<String, String> styles = parseStyle(styleString);
        return styles.getOrDefault(key, "");
    }


    @FXML
    private void closeTab(ActionEvent event) {
        Node source = (Node) event.getSource();

        for (Tab tab : tabPane.getTabs()) {
            if (tab.getGraphic() instanceof HBox hbox) {
                if (hbox.getChildren().contains(source)) {
                    tabPane.getTabs().remove(tab);
                    break;
                }
            }
        }
    }

}
