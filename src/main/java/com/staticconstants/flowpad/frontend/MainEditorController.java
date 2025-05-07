package com.staticconstants.flowpad.frontend;

import com.staticconstants.flowpad.FlowPadApplication;
import com.staticconstants.flowpad.frontend.textareaclasses.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.*;
import javafx.stage.Stage;
import org.fxmisc.richtext.GenericStyledArea;
import org.fxmisc.richtext.TextExt;
import org.fxmisc.richtext.model.*;

import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static org.fxmisc.richtext.model.TwoDimensional.Bias.Forward;

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

    private CustomStyledArea<ParStyle, RichSegment, TextStyle> richTextArea;
    private boolean isProgrammaticFontUpdate = false;
    private boolean isDesiredStyleChanged = false;
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

        richTextArea =  new CustomStyledArea<ParStyle, RichSegment, TextStyle>(
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
            isDesiredStyleChanged = false;
//            TODO: Do more testing, doesn't always work
        });
        richTextArea.caretPositionProperty().addListener((obs, oldPos, newPos) -> {
            System.out.println("Caret moved: " + newPos);
            System.out.println("Current segment: " + getSegmentAt(newPos-1));
        });

        richTextArea.setOnKeyTyped(event -> {
            if (event.isControlDown() && event.getCode() == KeyCode.V) {
                return;
            }

            int caretPosition = richTextArea.getCaretPosition();
            if (caretPosition > 0) {
                richTextArea.setStyle(caretPosition - 1, caretPosition, desiredStyle);
            }
        });

        richTextArea.setOnKeyPressed(event -> {
            if (event.isControlDown() && event.getCode() == KeyCode.V) {
                Clipboard clipboard = Clipboard.getSystemClipboard();
                if (clipboard.hasImage()) {
                    insertImage(clipboard.getImage());
                    event.consume();
                }
            }

            if (event.getCode() == KeyCode.BACK_SPACE) {
                event.consume();
                int caretPos = richTextArea.getCaretPosition();

                if (caretPos < 1) return;

                var segBefore = getSegmentAt(caretPos);

                if (segBefore instanceof ImageSegment) {
                    System.out.println("Its a image segment");
                    if (caretPos == 1) {
                        System.out.println("Replaced");
                        richTextArea.replace(0, caretPos, new TextSegment(""), richTextArea.getStyleAtPosition(caretPos));
                    } else {
                        richTextArea.deleteText(caretPos - 1, caretPos);
                    }

                }
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

                isDesiredStyleChanged = true;
            }
        });

        fontComboBox.showingProperty().addListener((obs, wasShowing, isNowShowing) -> {
            if (!isNowShowing) {
                Platform.runLater(() -> richTextArea.requestFocus());
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
        desiredStyle = desiredStyle.setFontSize(fontSize);

        isDesiredStyleChanged = true;
    }

    @FXML
    private void decreaseFontSize() {
        int fontSize = Integer.parseInt(textFieldFontSize.getText());
        if (fontSize > 1) {
            fontSize--;
            textFieldFontSize.setText(fontSize + "");
            desiredStyle = desiredStyle.setFontSize(fontSize);

            isDesiredStyleChanged = true;
        }
    }

    @FXML
    private void bold(){
        desiredStyle = desiredStyle.toggleBold();
        TextStyle.toggleStyle(richTextArea, TextAttribute.BOLD, desiredStyle);
        toggleSelectedButton(btnBold);

        isDesiredStyleChanged = true;
    }
    @FXML
    private void italic(){
        desiredStyle = desiredStyle.toggleItalic();
        TextStyle.toggleStyle(richTextArea, TextAttribute.ITALIC, desiredStyle);
        toggleSelectedButton(btnItalic);

        isDesiredStyleChanged = true;
    }
    @FXML
    private void underline(){
        desiredStyle = desiredStyle.toggleUnderline();
        TextStyle.toggleStyle(richTextArea, TextAttribute.UNDERLINE, desiredStyle);
        toggleSelectedButton(btnUnderline);

        isDesiredStyleChanged = true;
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
        desiredStyle = desiredStyle.setFontSize(Integer.parseInt(size));
        TextStyle.toggleStyle(richTextArea, TextAttribute.FONT_SIZE, desiredStyle);

        isDesiredStyleChanged = true;
    }


    private void updateFontSizeFieldFromSelection() {
        IndexRange selection = richTextArea.getSelection();
        int fontSize = 0;

        if (selection.getLength() == 0) {
            int caretPosition = richTextArea.getCaretPosition();
            TextStyle style = richTextArea.getStyleAtPosition(caretPosition > 0 ? caretPosition : caretPosition + 1);

//            RichSegment seg = getSegmentAt(caretPosition > 0 ? caretPosition : caretPosition + 1);
//            if (seg instanceof TextSegment) {
                fontSize = style.getFontSize();
//            } else {
//                return;
//            }
        } else {
            List<Integer> sizes = new ArrayList<>();
            for (int i = selection.getStart(); i < selection.getEnd(); i++) {
                sizes.add(richTextArea.getStyleAtPosition(i).getFontSize());
            }
            fontSize = Collections.max(sizes);
        }

        isProgrammaticFontUpdate = true;
        textFieldFontSize.setText(String.valueOf(fontSize));
        if (!isDesiredStyleChanged) desiredStyle = desiredStyle.setFontSize(fontSize);
        isProgrammaticFontUpdate = false;
    }

    private void updateFormattingFieldFromSelection(){
        IndexRange selection = richTextArea.getSelection();
        int start = selection.getStart();
        int end = selection.getEnd();

        int posToCheck = (start > 0) ? start : start+1;
//        RichSegment seg = getSegmentAt(posToCheck);
//        if (!(seg instanceof TextSegment)) return;

        TextStyle referenceStyle;
        if (start == end) {
            referenceStyle = richTextArea.getStyleAtPosition(posToCheck);
        } else {
            referenceStyle = TextStyle.getStyleSelection(richTextArea, start, end);
        }

        isProgrammaticFontUpdate = true;
        if (!isDesiredStyleChanged) desiredStyle = new TextStyle(referenceStyle.isBold(), referenceStyle.isItalic(), referenceStyle.isUnderline(), desiredStyle.getFontSize(), desiredStyle.getFontFamily());
        isProgrammaticFontUpdate = false;

        setSelectedButton(btnBold, referenceStyle.isBold());
        setSelectedButton(btnItalic, referenceStyle.isItalic());
        setSelectedButton(btnUnderline, referenceStyle.isUnderline());
    }


    private void updateFontFamilyFromSelection(){
        int caretPosition = richTextArea.getCaretPosition();
        TextStyle style = richTextArea.getStyleAtPosition(caretPosition > 0 ? caretPosition : caretPosition+1);
        String fontFamily = style.getFontFamily();

//        RichSegment seg = getSegmentAt(caretPosition > 0 ? caretPosition : caretPosition + 1);
//        if (!(seg instanceof TextSegment)) return;

        isProgrammaticFontUpdate = true;
        fontComboBox.getSelectionModel().select(fontFamily);
        isProgrammaticFontUpdate = false;

        if (!isDesiredStyleChanged) desiredStyle = desiredStyle.setFontFamily(fontFamily);
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

    private void insertImage(Image image) {
        if (image == null) return;

        int pos = richTextArea.getCaretPosition();

        // Ensure we get a non-null style
        TextStyle style = Optional.ofNullable(richTextArea.getStyleAtPosition(pos))
                .orElse(TextStyle.EMPTY);

        richTextArea.insert(pos, new ImageSegment(image), style);

        richTextArea.insert(pos + 1, new TextSegment(""), style);
        richTextArea.moveTo(pos + 1);
    }

    private RichSegment getSegmentAt(int position) {
        if (position < 0 || position >= richTextArea.getLength()) {
            return null;
        }

        TwoDimensional.Position twoDimPos = richTextArea.offsetToPosition(position, Forward);
        int paragraphIndex = twoDimPos.getMajor();
        int column = twoDimPos.getMinor();

        Paragraph<ParStyle, RichSegment, TextStyle> paragraph = richTextArea.getParagraph(paragraphIndex);
        int offset = 0;

        for (StyledSegment<RichSegment, TextStyle> seg : paragraph.getStyledSegments()) {
            int segLength = seg.getSegment().length();
            if (column >= offset && column < offset + segLength) {
                return seg.getSegment();
            }
            offset += segLength;
        }

        return null;
    }
}
