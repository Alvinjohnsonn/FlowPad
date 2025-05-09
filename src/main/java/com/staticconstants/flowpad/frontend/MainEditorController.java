package com.staticconstants.flowpad.frontend;

import com.staticconstants.flowpad.FlowPadApplication;
import com.staticconstants.flowpad.backend.db.notes.Note;
import com.staticconstants.flowpad.frontend.textareaclasses.*;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.*;
import javafx.stage.Stage;
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
    @FXML private TextField test;
    @FXML private ToolBar toolBar;
    
    private boolean isProgrammaticFontUpdate = false;
    private boolean isDesiredStyleChanged = false;
    
    private HashMap<String, CustomStyledArea<ParStyle, RichSegment, TextStyle>> textAreas;
    private HashMap<String, TextStyle> activeStyles;
    private String activeNote;

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
        textAreas = new HashMap<>();
        activeStyles = new HashMap<>();

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

        TextStyle desiredStyle = TextStyle.EMPTY;
        textFieldFontSize.setText(desiredStyle.getFontSize()+"");

        CustomStyledArea<ParStyle, RichSegment, TextStyle> textArea = null;
        initializeTextArea(textArea, editorContainer, "note1");

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

        fontComboBox.getItems().addAll(Font.getFamilies());
        fontComboBox.setOnAction(event -> {
            if (isProgrammaticFontUpdate) return;
            String selectedFont = (String)fontComboBox.getValue();
            if (selectedFont != null) {
                TextStyle newStyle = getActiveDesiredStyle().setFontFamily(selectedFont);
                setActiveDesiredStyle(newStyle);
                TextStyle.toggleStyle(getActiveTextArea(), TextAttribute.FONT_FAMILY, getActiveDesiredStyle());

                isDesiredStyleChanged = true;
            }
        });

        fontComboBox.showingProperty().addListener((obs, wasShowing, isNowShowing) -> {
            if (!isNowShowing) {
                Platform.runLater(() -> getActiveTextArea().requestFocus());
            }
        });


//        Tab Setup
        test.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if(mouseEvent.getButton().equals(MouseButton.PRIMARY)){
                    if(mouseEvent.getClickCount() == 2){
                        TextField tf = (TextField)mouseEvent.getSource();
                        tf.setEditable(true);
                        tf.selectAll();

                    }
                }
            }
        });
        test.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
            Object tag = ((TextField)event.getSource()).getUserData();
            Tab tab = null;

            for (Tab t : tabPane.getTabs()){
                if (t.getUserData().equals(tag)){
                    tab = t;
                    break;
                }
            }

            if (tab!=null) {
                tabPane.getSelectionModel().select(tab);

            }
        });
        test.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.ESCAPE){
                ((TextField)event.getSource()).setEditable(false);
            }
        });
        test.focusedProperty().addListener((obs,old,current) -> {
            if (!current) test.setEditable(false);
        });
        test.textProperty().addListener((obs, oldS, newS) -> {
            test.setPrefWidth(newS.length() * 7);
        });

        tabPane.getSelectionModel().selectedItemProperty().addListener(
                (obs,oldTab, newTab) -> {
                    if (oldTab == newTab) return;
                    SplitPane sp = (SplitPane)newTab.getContent();
                    VBox vb = (VBox)sp.getItems().getFirst();

                    if (!vb.getChildren().contains(toolBar)) vb.getChildren().addFirst(toolBar);
                    
                    activeNote = (String)newTab.getUserData();
                });

        newNote();
    }

    private void initializeTextArea(CustomStyledArea<ParStyle, RichSegment, TextStyle> textArea, VBox editorContainer, String userData) {
        ParStyle initialParStyle = ParStyle.EMPTY;
        TextStyle desiredStyle = TextStyle.EMPTY;
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

        textArea.caretPositionProperty().addListener((obs, oldSel, newSel) -> {
            updateFontSizeFieldFromSelection();
            updateFormattingFieldFromSelection();
            updateFontFamilyFromSelection();
            isDesiredStyleChanged = false;
//            TODO: Do more testing, doesn't always work
        });
        textArea.caretPositionProperty().addListener((obs, oldPos, newPos) -> {
            System.out.println("Caret moved: " + newPos);
            System.out.println("Current segment: " + getSegmentAt(newPos-1));
        });

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

        VBox.setVgrow(textArea, Priority.ALWAYS);
        editorContainer.getChildren().add(textArea);

        activeStyles.put(userData, desiredStyle);
        textAreas.put(userData, textArea);
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
        TextStyle newStyle = getActiveDesiredStyle().setFontSize(fontSize);
        setActiveDesiredStyle(newStyle);

        isDesiredStyleChanged = true;
    }

    @FXML
    private void decreaseFontSize() {
        int fontSize = Integer.parseInt(textFieldFontSize.getText());
        if (fontSize > 1) {
            fontSize--;
            textFieldFontSize.setText(fontSize + "");
            TextStyle newStyle = getActiveDesiredStyle().setFontSize(fontSize);
            setActiveDesiredStyle(newStyle);

            isDesiredStyleChanged = true;
        }
    }

    @FXML
    private void bold(){
        TextStyle newStyle = getActiveDesiredStyle().toggleBold();
        setActiveDesiredStyle(newStyle);

        TextStyle.toggleStyle(getActiveTextArea(), TextAttribute.BOLD, getActiveDesiredStyle());
        toggleSelectedButton(btnBold);

        isDesiredStyleChanged = true;
    }
    @FXML
    private void italic(){
        TextStyle newStyle = getActiveDesiredStyle().toggleItalic();
        setActiveDesiredStyle(newStyle);
        TextStyle.toggleStyle(getActiveTextArea(), TextAttribute.ITALIC, getActiveDesiredStyle());
        toggleSelectedButton(btnItalic);

        isDesiredStyleChanged = true;
    }
    @FXML
    private void underline(){
        TextStyle newStyle = getActiveDesiredStyle().toggleUnderline();
        setActiveDesiredStyle(newStyle);
        TextStyle.toggleStyle(getActiveTextArea(), TextAttribute.UNDERLINE, getActiveDesiredStyle());
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
        TextStyle newStyle = getActiveDesiredStyle().setFontSize(Integer.parseInt(size));
        setActiveDesiredStyle(newStyle);

        TextStyle.toggleStyle(getActiveTextArea(), TextAttribute.FONT_SIZE, getActiveDesiredStyle());

        isDesiredStyleChanged = true;
    }


    private void updateFontSizeFieldFromSelection() {
        IndexRange selection = getActiveTextArea().getSelection();
        int fontSize = 0;

        if (selection.getLength() == 0) {
            int caretPosition = getActiveTextArea().getCaretPosition();
            TextStyle style = getActiveTextArea().getStyleAtPosition(caretPosition > 0 ? caretPosition : caretPosition + 1);

//            RichSegment seg = getSegmentAt(caretPosition > 0 ? caretPosition : caretPosition + 1);
//            if (seg instanceof TextSegment) {
                fontSize = style.getFontSize();
//            } else {
//                return;
//            }
        } else {
            List<Integer> sizes = new ArrayList<>();
            for (int i = selection.getStart(); i < selection.getEnd(); i++) {
                sizes.add(getActiveTextArea().getStyleAtPosition(i).getFontSize());
            }
            fontSize = Collections.max(sizes);
        }

        isProgrammaticFontUpdate = true;
        textFieldFontSize.setText(String.valueOf(fontSize));
        if (!isDesiredStyleChanged) {
            TextStyle newStyle = getActiveDesiredStyle().setFontSize(fontSize);
            setActiveDesiredStyle(newStyle);
        }
        isProgrammaticFontUpdate = false;
    }

    private void updateFormattingFieldFromSelection(){
        IndexRange selection = getActiveTextArea().getSelection();
        int start = selection.getStart();
        int end = selection.getEnd();

        int posToCheck = (start > 0) ? start : start+1;
//        RichSegment seg = getSegmentAt(posToCheck);
//        if (!(seg instanceof TextSegment)) return;

        TextStyle referenceStyle;
        if (start == end) {
            referenceStyle = getActiveTextArea().getStyleAtPosition(posToCheck);
        } else {
            referenceStyle = TextStyle.getStyleSelection(getActiveTextArea(), start, end);
        }

        isProgrammaticFontUpdate = true;
        if (!isDesiredStyleChanged) {
            TextStyle newStyle = new TextStyle(referenceStyle.isBold(), referenceStyle.isItalic(), referenceStyle.isUnderline(), getActiveDesiredStyle().getFontSize(), getActiveDesiredStyle().getFontFamily());
            setActiveDesiredStyle(newStyle);
        }
        isProgrammaticFontUpdate = false;

        setSelectedButton(btnBold, referenceStyle.isBold());
        setSelectedButton(btnItalic, referenceStyle.isItalic());
        setSelectedButton(btnUnderline, referenceStyle.isUnderline());
    }


    private void updateFontFamilyFromSelection(){
        int caretPosition = getActiveTextArea().getCaretPosition();
        TextStyle style = getActiveTextArea().getStyleAtPosition(caretPosition > 0 ? caretPosition : caretPosition+1);
        String fontFamily = style.getFontFamily();

//        RichSegment seg = getSegmentAt(caretPosition > 0 ? caretPosition : caretPosition + 1);
//        if (!(seg instanceof TextSegment)) return;

        isProgrammaticFontUpdate = true;
        fontComboBox.getSelectionModel().select(fontFamily);
        isProgrammaticFontUpdate = false;

        if (!isDesiredStyleChanged) {
            TextStyle newStyle = getActiveDesiredStyle().setFontFamily(fontFamily);
            setActiveDesiredStyle(newStyle);
        }
    }


    @FXML
    private void closeTab(ActionEvent event) {
        Node source = (Node) event.getSource();

        for (Tab tab : tabPane.getTabs()) {
            if (tab.getGraphic() instanceof HBox hbox) {
                if (hbox.getChildren().contains(source)) {
//                    TODO: Check if code is safe, prone to errors
                    textAreas.remove(tab.getUserData());
                    activeStyles.remove(tab.getUserData());
                    tabPane.getTabs().remove(tab);

                    activeNote = (String)tabPane.getTabs().getFirst().getUserData();
                    break;
                }
            }
        }
    }

    private static int numOfNewNote = 0;
    @FXML
    private void newNote(){
//        TODO: Create Note object

        String fileName = "New Note" + (numOfNewNote>0 ? " "+numOfNewNote : "") ;
        numOfNewNote++;

//        Initialize Tab
        Tab newTab = new Tab();
        newTab.setUserData(fileName);

//        Initialize HBox Tab Title
        HBox hbox = new HBox();
        hbox.setAlignment(Pos.CENTER_LEFT);
        hbox.setSpacing(12.0);
        hbox.setPadding(new Insets(0 ,8 ,0, 8));

//        Initialize Label Title
        TextField title = new TextField(fileName);
        title.setPrefWidth(80);
        title.setMaxWidth(90);
        title.setMinWidth(50);
        title.setEditable(false);
        title.setUserData(fileName);
        title.getStyleClass().add("bg-transparent");
        title.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if(mouseEvent.getButton().equals(MouseButton.PRIMARY)){
                    if(mouseEvent.getClickCount() == 2){
                        TextField tf = (TextField)mouseEvent.getSource();
                        tf.setEditable(true);
                        tf.selectAll();

                    }
                }
            }
        });

        title.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
            Object tag = ((TextField)event.getSource()).getUserData()==null ? "" : ((TextField)event.getSource()).getUserData();
            Tab tab = null;

            for (Tab t : tabPane.getTabs()){
                if (t.getUserData()!=null && t.getUserData().equals(tag)){
                    tab = t;
                    break;
                }
            }

            if (tab!=null) {
                tabPane.getSelectionModel().select(tab);

            }
        });

        title.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.ESCAPE){
                ((TextField)event.getSource()).setEditable(false);

//                TODO: Rename Note object
            }
        });

        title.focusedProperty().addListener((obs,old,current) -> {
            if (!current) title.setEditable(false);

//            TODO: Rename Note object
        });

        title.textProperty().addListener((obs, oldS, newS) -> {
            title.setPrefWidth(newS.length() * 7);
        });

//        Initialize X image
        Image img = new Image(FlowPadApplication.class.getResource("icons/close.png").toExternalForm());
        ImageView x = new ImageView();
        x.setImage(img);
        x.setFitHeight(8.0);
        x.setFitWidth(8.0);
        x.setPickOnBounds(true);
        x.setPreserveRatio(true);

//        Initialize Close Button
        Button btnClose = new Button("");
        btnClose.setOnAction(event -> closeTab(event));
        btnClose.getStyleClass().add("tab-close");
        btnClose.setGraphic(x);

//        Combine Title and Button
        hbox.getChildren().add(title);
        hbox.getChildren().add(btnClose);
        newTab.setGraphic(hbox);

//        Initialize Content, starting with SplitPane
        SplitPane splitPane = new SplitPane();
        splitPane.getStyleClass().add("inner-split");

//        Initialize Main Container
        VBox mainContainer = new VBox();
        mainContainer.setPrefWidth(501);
        mainContainer.setPrefHeight(379);
        mainContainer.setSpacing(5);
        VBox.setVgrow(mainContainer, Priority.ALWAYS);
        mainContainer.getChildren().add(toolBar);

//        Initialize GenericStyledArea



//        Initialize Editor Container
        VBox editor = new VBox();
        editor.prefWidth(500);
        VBox.setVgrow(editor,Priority.ALWAYS);
        editor.setPadding(new Insets(10,10,10,10));

        CustomStyledArea<ParStyle, RichSegment, TextStyle> textArea = null;
        initializeTextArea(textArea, editor, fileName);

//        Complete All Setup
        mainContainer.getChildren().add(editor);
        splitPane.getItems().add(mainContainer);
        newTab.setContent(splitPane);

        tabPane.getTabs().add(newTab);
        tabPane.getSelectionModel().select(newTab);
    }

    @FXML
    private void undo(){

    }

    @FXML
    private void redo(){

    }
    @FXML
    private void cut(){

    }
    @FXML
    private void copy(){

    }

    @FXML
    private void paste(){

    }

    @FXML
    private void selectAll(){

    }
    @FXML
    private void find(){

    }

    @FXML
    private void renameFile(Label fileName){

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

        int pos = getActiveTextArea().getCaretPosition();

        TextStyle style = Optional.ofNullable(getActiveTextArea().getStyleAtPosition(pos))
                .orElse(TextStyle.EMPTY);

        getActiveTextArea().insert(pos, new ImageSegment(image), style);
        getActiveTextArea().insert(pos + 1, new TextSegment(""), style);
        getActiveTextArea().moveTo(pos+1);
    }

    private RichSegment getSegmentAt(int position) {
        if (position < 0 || position >= getActiveTextArea().getLength()) {
            return null;
        }

        TwoDimensional.Position twoDimPos = getActiveTextArea().offsetToPosition(position, Forward);
        int paragraphIndex = twoDimPos.getMajor();
        int column = twoDimPos.getMinor();

        Paragraph<ParStyle, RichSegment, TextStyle> paragraph = getActiveTextArea().getParagraph(paragraphIndex);
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
    
    private CustomStyledArea<ParStyle, RichSegment, TextStyle> getActiveTextArea(){
        return textAreas.get(activeNote);
    }
    private TextStyle getActiveDesiredStyle(){
        return activeStyles.get(activeNote);
    }
    private void setActiveDesiredStyle(TextStyle newStyle){
        activeStyles.put(activeNote, newStyle);
    }

}
