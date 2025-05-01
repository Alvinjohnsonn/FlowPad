package com.staticconstants.flowpad.frontend;

import com.staticconstants.flowpad.FlowPadApplication;
import com.staticconstants.flowpad.backend.db.notes.Note;
import com.staticconstants.flowpad.backend.db.notes.NoteDAO;
import com.staticconstants.flowpad.backend.notes.StyledTextCodecs;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.fxmisc.richtext.InlineCssTextArea;

import java.io.IOException;
import java.sql.Array;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    @FXML private Button btnBold;
    @FXML private Button btnItalic;
    @FXML private Button btnUnderline;
    @FXML private Button btnBack;

    private InlineCssTextArea richTextArea;
    private boolean isProgrammaticFontUpdate = false;
    private HashMap<String, String> desiredStyle = new HashMap<>();


    private int defaultFontSize = 12;

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

        desiredStyle.put("-fx-font-size", defaultFontSize+"px");
        desiredStyle.put("-fx-font-family", "Arial");

        String fontSize = desiredStyle.get("-fx-font-size");
        textFieldFontSize.setText(defaultFontSize+"");

        richTextArea = new InlineCssTextArea();
        richTextArea.setWrapText(true);
        richTextArea.setStyle(hashMapStyleToString(desiredStyle));

        richTextArea.caretPositionProperty().addListener((obs, oldSel, newSel) -> {
            updateFontSizeFieldFromSelection();
            updateFormattingFieldFromSelection();
            updateFontFamilyFromSelection();
//            TODO: Do more testing, doesn't always work
        });

        richTextArea.plainTextChanges().subscribe(change -> {
            int from = change.getPosition();
            int length = change.getInserted().length();
            richTextArea.setStyle(from, from+length, hashMapStyleToString(desiredStyle));
        });

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

//        TODO: Change background color and text color when richtextarea is selected

        fontComboBox.getItems().addAll(Font.getFamilies());
        fontComboBox.setOnAction(event -> {
            if (isProgrammaticFontUpdate) return;
            String selectedFont = (String)fontComboBox.getValue();
            if (selectedFont != null) {
                addOrRemoveStyle(richTextArea, "-fx-font-family","'"+ selectedFont + "';");
                desiredStyle.put("-fx-font-family", "'"+ selectedFont + "'");
            }
        });
    }

    public static String hashMapStyleToString(HashMap<String, String> styles){
        String styleString = "";
        for (String style : styles.keySet()){
            styleString += style + ":" + styles.get(style) + ";";
        }
        return styleString;
    }

    public static void setActiveButton(Button active, Button inactive) {
        if (!active.getStyleClass().contains("selected")) active.getStyleClass().add("selected");

        inactive.getStyleClass().removeAll("selected");
    }

    public static void setSelectedButton(Button btn, boolean isSelected) {
        if (isSelected && !btn.getStyleClass().contains("selected")) btn.getStyleClass().add("selected");

        else if (!isSelected) btn.getStyleClass().removeAll("selected");
    }

    public static void toggleSelectedButton(Button btn) {
        if (!btn.getStyleClass().contains("selected")) btn.getStyleClass().add("selected");
        else btn.getStyleClass().removeAll("selected");
    }

    @FXML
    private void increaseFontSize() {
        int fontSize = Integer.parseInt(textFieldFontSize.getText());
        fontSize++;
        textFieldFontSize.setText(fontSize + "");

        desiredStyle.put("-fx-font-size", fontSize+"px");
    }

    @FXML
    private void decreaseFontSize() {
        int fontSize = Integer.parseInt(textFieldFontSize.getText());
        if (fontSize > 1) {
            fontSize--;
            textFieldFontSize.setText(fontSize + "");

            desiredStyle.put("-fx-font-size", fontSize+"px");
        }
    }

    @FXML
    private void bold(){
        addOrRemoveStyle(richTextArea, "-fx-font-weight", "bold");
        switchOnOffDesiredStyle("-fx-font-weight", "bold");
        toggleSelectedButton(btnBold);
    }
    @FXML
    private void italic(){
        addOrRemoveStyle(richTextArea, "-fx-font-style", "italic");
        switchOnOffDesiredStyle("-fx-font-style", "italic");
        toggleSelectedButton(btnItalic);
    }
    @FXML
    private void underline(){
        addOrRemoveStyle(richTextArea, "-fx-underline", "true");
        switchOnOffDesiredStyle("-fx-underline", "true");
        toggleSelectedButton(btnUnderline);
    }

    @FXML
    private void save(){

        try {
            byte[] serializedText = StyledTextCodecs.serializeStyledText(richTextArea);

            Note note = new Note("test", serializedText, new String[]{});
            NoteDAO dao = new NoteDAO();
            dao.insert(note);

        } catch (IOException ex) {
            // TODO: Add better exception handling
            System.err.println("Failed to serialize text");
        }

    }

    private void switchOnOffDesiredStyle(String key, String value){
        if (desiredStyle.getOrDefault(key,"").equals(value)){
            desiredStyle.remove(key);
        }
        else desiredStyle.put(key, value);
    }


    @FXML
    private void handleFontSizeChange(String size){
        addOrRemoveStyle(richTextArea, "-fx-font-size", size+"px");
        desiredStyle.put("-fx-font-size", size+"px");
    }


    private void updateFontSizeFieldFromSelection() {
        IndexRange selection = richTextArea.getSelection();

        if (selection.getLength() == 0) {
            if (richTextArea.getCaretPosition()>0) {
                String sizeValue = getStyleValue(richTextArea.getStyleOfChar(richTextArea.getCaretPosition() - 1),"-fx-font-size");
                desiredStyle.put("-fx-font-size", sizeValue);
                textFieldFontSize.setText(sizeValue.substring(0, sizeValue.length()-2));
            }
            else{
                String sizeValue = getStyleValue(richTextArea.getStyleOfChar(richTextArea.getCaretPosition()),"-fx-font-size");
                desiredStyle.put("-fx-font-size", sizeValue);
                textFieldFontSize.setText(sizeValue);
                textFieldFontSize.setText(sizeValue.substring(0, sizeValue.length()-2));
            }

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
        maxFontSize.ifPresent(size -> {
            textFieldFontSize.setText(String.valueOf(size));
            desiredStyle.put("-fx-font-size", size + "px");
        });

        isProgrammaticFontUpdate = false;
    }

    private void updateFormattingFieldFromSelection(){
        if (isStyleFullyApplied(richTextArea,"-fx-font-weight", "bold")){
            setSelectedButton(btnBold, true);
        }
        else setSelectedButton(btnBold, false);

        if (isStyleFullyApplied(richTextArea,"-fx-font-style", "italic")){
            setSelectedButton(btnItalic, true);
        }
        else setSelectedButton(btnItalic, false);

        if (isStyleFullyApplied(richTextArea,"-fx-underline", "true")){
            setSelectedButton(btnUnderline, true);
        }
        else setSelectedButton(btnUnderline, false);
    }


    private void updateFontFamilyFromSelection(){
        String currentStyle = "";
        if (richTextArea.getCaretPosition()>0) {
            currentStyle = richTextArea.getStyleOfChar(richTextArea.getCaretPosition() - 1);
        }
        else{
            currentStyle = richTextArea.getStyleOfChar(richTextArea.getCaretPosition());
        }
        String value = getStyleValue(currentStyle, "-fx-font-family");
        desiredStyle.put("-fx-font-family", value);
        if (value != null && value.length() >= 2 && value.startsWith("'") && value.endsWith("'")) {
            value = value.substring(1, value.length() - 1);
        }
        isProgrammaticFontUpdate = true;
        fontComboBox.getSelectionModel().select(value);
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

    // Check if all selection has the same style applied
    // If there are no selection then it will check the style applied on the char before
    // the caret position, however, if the caret position is at 0 it will compare it with the char
    // after the caret position. The function will return true if the styles parameter given matches
    // with the conditions stated above.
    public static boolean isStyleFullyApplied(InlineCssTextArea area, String styleKey, String valueToToggle){
        if (area.getSelection().getLength() == 0){
            String currentStyle = "";
            if (area.getCaretPosition()>0) {
                currentStyle = area.getStyleOfChar(area.getCaretPosition() - 1);
            }
            else{
                currentStyle = area.getStyleOfChar(area.getCaretPosition());
            }
            String value = getStyleValue(currentStyle, styleKey);
            return value.equals(valueToToggle);
        }

        int start = area.getSelection().getStart();
        int end = area.getSelection().getEnd();
        boolean styleFullyApplied = true;

        for (int i = start; i < end; i++) {
            String currentStyle = area.getStyleOfChar(i);
            String value = getStyleValue(currentStyle, styleKey);
            if (!value.equals(valueToToggle)) {
                styleFullyApplied = false;
                break;
            }
        }
        return styleFullyApplied;
    }

    public static void addOrRemoveStyle(InlineCssTextArea area, String styleKey, String valueToToggle) {
        if (area.getSelection().getLength() == 0) return;
        int start = area.getSelection().getStart();
        int end = area.getSelection().getEnd();

        boolean styleFullyApplied = isStyleFullyApplied(area, styleKey, valueToToggle);

        for (int i = start; i < end; i++) {
            String currentStyle = area.getStyleOfChar(i);
            Map<String, String> styles = parseStyle(currentStyle);

            if (styleFullyApplied) {
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

    public static HashMap<String, String> parseStyle(String styleString) {
        HashMap<String, String> styles = new HashMap<>();
        if (styleString == null || styleString.isEmpty()) return styles;

        String[] styleArray = styleString.split(";");
        for (String style : styleArray) {
            String[] parts = style.trim().split(":", 2);
            if (parts.length == 2) {
                styles.put(parts[0].trim(), parts[1].trim());
            }
        }
        return styles;
    }

    public static String getStyleValue(String styleString, String key) {
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

    @FXML
    protected void onBackButtonClick() throws IOException {
        Stage stage = (Stage) btnBack.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(FlowPadApplication.class.getResource("flowpad-view.fxml"));

        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("FlowPad");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }
}
