package com.staticconstants.flowpad.frontend;

import com.staticconstants.flowpad.FlowPadApplication;
import com.staticconstants.flowpad.backend.db.notes.Note;
import com.staticconstants.flowpad.backend.db.notes.NoteDAO;
import com.staticconstants.flowpad.backend.notes.StyledTextCodecs;
import com.staticconstants.flowpad.frontend.textareaclasses.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.*;
import javafx.stage.Stage;
import org.fxmisc.richtext.GenericStyledArea;
import org.fxmisc.richtext.InlineCssTextArea;
import org.fxmisc.richtext.TextExt;
import org.fxmisc.richtext.model.SegmentOps;
import org.fxmisc.richtext.model.StyledSegment;
import org.fxmisc.richtext.model.TextOps;

import java.io.IOException;
import java.sql.Array;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
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

    private GenericStyledArea<ParStyle, RichSegment, TextStyle> richTextArea;
    private boolean isProgrammaticFontUpdate = false;
    private TextStyle desiredStyle;

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


        ParStyle initialParStyle = ParStyle.EMPTY;
        desiredStyle = TextStyle.EMPTY;
        TextStyle initialTextStyle = TextStyle.EMPTY;

        textFieldFontSize.setText(initialTextStyle.getFontSize()+"");
        // Apply paragraph style (optional for now)
        BiConsumer<TextFlow, ParStyle> paragraphStyler = (tf, p) -> {};

        // Render each segment with its style
        Function<StyledSegment<RichSegment, TextStyle>, Node> nodeFactory = seg -> {
            RichSegment s = seg.getSegment();
            TextStyle style = seg.getStyle();

            if (s instanceof TextSegment textSeg) {
                TextExt text = new TextExt(textSeg.getText());
                FontWeight weight = style.isBold() ? FontWeight.BOLD : FontWeight.NORMAL;
                FontPosture posture = style.isItalic() ? FontPosture.ITALIC : FontPosture.REGULAR;
                text.setFont(Font.font(style.getFontFamily(), weight, posture, style.getFontSize()));
                text.setUnderline(style.isUnderline());

                // Add later
//                style.getTextColor().ifPresent(text::setFill); // assuming getTextColor() returns Optional<Paint>

                return text;

            } else if (s instanceof ImageSegment imgSeg) {
                Image img = new Image(imgSeg.getImage().getUrl());
                ImageView view = new ImageView(img);
                view.setFitWidth(100);
                view.setPreserveRatio(true);
                return view;
            }

            return new Text("?");
        };

        // Create the GenericStyledArea

        RichTextOps segmentOps = new RichTextOps();

        richTextArea =  new GenericStyledArea<ParStyle, RichSegment, TextStyle>(
                initialParStyle,
                paragraphStyler,
                initialTextStyle,
                segmentOps,
                nodeFactory
        );
        richTextArea.setWrapText(true);

        richTextArea.caretPositionProperty().addListener((obs, oldSel, newSel) -> {
            updateFontSizeFieldFromSelection();
            updateFormattingFieldFromSelection();
            updateFontFamilyFromSelection();
//            TODO: Do more testing, doesn't always work
        });

//        richTextArea.plainTextChanges().subscribe(change -> {
//            int from = change.getPosition();
//            int length = change.getInserted().length();
//            richTextArea.setStyle(from, from+length, desiredStyle);
//        });

        richTextArea.setOnKeyTyped(event -> {
            int caretPosition = richTextArea.getCaretPosition();
            if (caretPosition > 0) {
                // Apply desiredStyle to the most recently typed character
                richTextArea.setStyle(caretPosition - 1, caretPosition, desiredStyle);
            }
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
                desiredStyle = desiredStyle.setFontFamily(selectedFont);
                TextStyle.toggleStyle(richTextArea, TextAttribute.FONT_FAMILY, desiredStyle);
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

//        desiredStyle.put("-fx-font-size", fontSize+"px");

        desiredStyle = desiredStyle.setFontSize(fontSize);
    }

    @FXML
    private void decreaseFontSize() {
        int fontSize = Integer.parseInt(textFieldFontSize.getText());
        if (fontSize > 1) {
            fontSize--;
            textFieldFontSize.setText(fontSize + "");

//            desiredStyle.put("-fx-font-size", fontSize+"px");
            desiredStyle = desiredStyle.setFontSize(fontSize);
        }
    }

    @FXML
    private void bold(){
//        addOrRemoveStyle(richTextArea, "-fx-font-weight", "bold");
//        switchOnOffDesiredStyle("-fx-font-weight", "bold");

        desiredStyle = desiredStyle.toggleBold();
        TextStyle.toggleStyle(richTextArea, TextAttribute.BOLD, desiredStyle);
        toggleSelectedButton(btnBold);
    }
    @FXML
    private void italic(){
//        addOrRemoveStyle(richTextArea, "-fx-font-style", "italic");
//        switchOnOffDesiredStyle("-fx-font-style", "italic");

        desiredStyle = desiredStyle.toggleItalic();
        TextStyle.toggleStyle(richTextArea, TextAttribute.ITALIC, desiredStyle);
        toggleSelectedButton(btnItalic);
    }
    @FXML
    private void underline(){
//        addOrRemoveStyle(richTextArea, "-fx-underline", "true");
//        switchOnOffDesiredStyle("-fx-underline", "true");

        desiredStyle = desiredStyle.toggleUnderline();
        TextStyle.toggleStyle(richTextArea, TextAttribute.UNDERLINE, desiredStyle);
        toggleSelectedButton(btnUnderline);
    }

    @FXML
    private void save(){

//        try {
//            byte[] serializedText = StyledTextCodecs.serializeStyledText(richTextArea);
//
//            Note note = new Note("test", serializedText, new String[]{});
//            NoteDAO dao = new NoteDAO();
//            dao.insert(note);
//
//        } catch (IOException ex) {
//            // TODO: Add better exception handling
//            System.err.println("Failed to serialize text");
//        }

    }



    @FXML
    private void handleFontSizeChange(String size){
//        addOrRemoveStyle(richTextArea, "-fx-font-size", size+"px");
//        desiredStyle.put("-fx-font-size", size+"px");

        desiredStyle = desiredStyle.setFontSize(Integer.parseInt(size));
        TextStyle.toggleStyle(richTextArea, TextAttribute.FONT_SIZE, desiredStyle);
    }


    private void updateFontSizeFieldFromSelection() {
        IndexRange selection = richTextArea.getSelection();

        if (selection.getLength() == 0) {
            if (richTextArea.getCaretPosition()>0) {
                int size = richTextArea.getStyleAtPosition(richTextArea.getCaretPosition() - 1).getFontSize();
                desiredStyle = desiredStyle.setFontSize(size);
                textFieldFontSize.setText(String.valueOf(size));
            }
            else{
                int size = richTextArea.getStyleAtPosition(richTextArea.getCaretPosition()).getFontSize();
                desiredStyle = desiredStyle.setFontSize(size);
                textFieldFontSize.setText(String.valueOf(size));
            }

            return;
        }

        List<Integer> sizes = new ArrayList<>(List.of());

        for (int i = selection.getStart(); i < selection.getEnd(); i++) {
            int size = richTextArea.getStyleAtPosition(i).getFontSize();
            sizes.add(size);
        }
        int maxFontSize= Collections.max(sizes);

        isProgrammaticFontUpdate = true;

        textFieldFontSize.setText(String.valueOf(maxFontSize));
        desiredStyle = desiredStyle.setFontSize(maxFontSize);

        isProgrammaticFontUpdate = false;
    }

    private void updateFormattingFieldFromSelection(){
        int start = richTextArea.getSelection().getStart();
        int end = richTextArea.getSelection().getEnd();

        if (TextStyle.isStyleFullyApplied(richTextArea, start, end, TextAttribute.BOLD, desiredStyle) && desiredStyle.isBold()){
            setSelectedButton(btnBold, true);
        }
        else setSelectedButton(btnBold, false);

        if (TextStyle.isStyleFullyApplied(richTextArea,start, end, TextAttribute.ITALIC, desiredStyle) && desiredStyle.isItalic()){
            setSelectedButton(btnItalic, true);
        }
        else setSelectedButton(btnItalic, false);

        if (TextStyle.isStyleFullyApplied(richTextArea,start, end, TextAttribute.UNDERLINE, desiredStyle) && desiredStyle.isUnderline()){
            setSelectedButton(btnUnderline, true);
        }
        else setSelectedButton(btnUnderline, false);
    }


    private void updateFontFamilyFromSelection(){
        String currentFontFamily = "";
        if (richTextArea.getCaretPosition()>0) {
            currentFontFamily = richTextArea.getStyleAtPosition(richTextArea.getCaretPosition() - 1).getFontFamily();
        }
        else{
            currentFontFamily = richTextArea.getStyleAtPosition(richTextArea.getCaretPosition()).getFontFamily();
        }
//        String value = getStyleValue(currentStyle, "-fx-font-family");
//        desiredStyle.put("-fx-font-family", value);

        desiredStyle = desiredStyle.setFontFamily(currentFontFamily);
//        if (value != null && value.length() >= 2 && value.startsWith("'") && value.endsWith("'")) {
//            value = value.substring(1, value.length() - 1);
//        }
        isProgrammaticFontUpdate = true;
        fontComboBox.getSelectionModel().select(currentFontFamily);
        isProgrammaticFontUpdate = false;
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
        String stylesheet =  FlowPadApplication.class.getResource("flowpad-stylesheet.css").toExternalForm();

        Scene scene = new Scene(fxmlLoader.load());
        scene.getStylesheets().add(stylesheet);
        stage.setTitle("Home Page");

        stage.setScene(scene);
        stage.setMaximized(true);
    }
}
