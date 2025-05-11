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
    @FXML public TextField textFieldFontSize;
    @FXML private VBox editorContainer;
    @FXML private TabPane tabPane;
    @FXML public ComboBox fontComboBox;
    @FXML private Button btnBold;
    @FXML private Button btnItalic;
    @FXML private Button btnUnderline;
    @FXML private Button btnBack;
    @FXML private ToolBar toolBar;

    private HashMap<String, TextAreaController> textAreas;
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

        textFieldFontSize.setText(TextStyle.EMPTY.getFontSize()+"");

        TextAreaController tac = new TextAreaController(editorContainer, "note1");
        tac.initializeUpdateToolbar(this);
        textAreas.put("note1", tac);

        textFieldFontSize.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getText();
            return newText.matches("[0-9]*") ? change : null;
        }));

        textFieldFontSize.textProperty().addListener((obs, oldText, newText) -> {
            if (textAreas.get(activeNote).isProgrammaticFontUpdate()) return;
            handleFontSizeChange(newText);
        });

        textFieldFontSize.setOnAction(e -> {
            handleFontSizeChange(textFieldFontSize.getText());
        });

        fontComboBox.getItems().addAll(Font.getFamilies());
        fontComboBox.setOnAction(event -> {
            if (textAreas.get(activeNote).isProgrammaticFontUpdate()) return;
            String selectedFont = (String)fontComboBox.getValue();
            if (selectedFont != null) {
                TextAreaController active = textAreas.get(activeNote);
                TextStyle newStyle = active.getDesiredStyle().setFontFamily(selectedFont);
                active.setDesiredStyle(newStyle);
                TextStyle.toggleStyle(active.getTextArea(), TextAttribute.FONT_FAMILY, active.getDesiredStyle());

                textAreas.get(activeNote).setDesiredStyleChanged(true);
            }
        });

        fontComboBox.showingProperty().addListener((obs, wasShowing, isNowShowing) -> {
            if (!isNowShowing) {
                Platform.runLater(() -> textAreas.get(activeNote).getTextArea().requestFocus());
            }
        });

        tabPane.getSelectionModel().selectedItemProperty().addListener(
                (obs,oldTab, newTab) -> {
                    if (oldTab == newTab || newTab==null) return;
                    SplitPane sp = (SplitPane)newTab.getContent();
                    VBox vb = (VBox)sp.getItems().getFirst();

                    if (!vb.getChildren().contains(toolBar)) vb.getChildren().addFirst(toolBar);
                    
                    activeNote = (String)newTab.getUserData();

                    textAreas.get(activeNote).reload();
                });
        tabPane.getTabs().removeFirst(); // delete the existing tab used for visual design purposes

        newNote();
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

    public void setSelectedButton(TextAttribute att, boolean isSelected) {
        Button btn = null;
        switch(att){
            case BOLD -> btn = btnBold;
            case ITALIC -> btn = btnItalic;
            case UNDERLINE -> btn = btnUnderline;
        }

        if (btn==null)return;

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
        TextStyle newStyle = textAreas.get(activeNote).getDesiredStyle().setFontSize(fontSize);
        textAreas.get(activeNote).setDesiredStyle(newStyle);

        textAreas.get(activeNote).setDesiredStyleChanged(true);
    }

    @FXML
    private void decreaseFontSize() {
        int fontSize = Integer.parseInt(textFieldFontSize.getText());
        if (fontSize > 1) {
            fontSize--;
            textFieldFontSize.setText(fontSize + "");
            TextStyle newStyle = textAreas.get(activeNote).getDesiredStyle().setFontSize(fontSize);
            textAreas.get(activeNote).setDesiredStyle(newStyle);

            textAreas.get(activeNote).setDesiredStyleChanged(true);
        }
    }

    @FXML
    private void bold(){
        TextAreaController active = textAreas.get(activeNote);
        TextStyle newStyle = active.getDesiredStyle().toggleBold();
        active.setDesiredStyle(newStyle);

        TextStyle.toggleStyle(active.getTextArea(), TextAttribute.BOLD, active.getDesiredStyle());
        toggleSelectedButton(btnBold);

        active.setDesiredStyleChanged(true);
    }
    @FXML
    private void italic(){
        TextAreaController active = textAreas.get(activeNote);
        TextStyle newStyle = active.getDesiredStyle().toggleItalic();
        active.setDesiredStyle(newStyle);

        TextStyle.toggleStyle(active.getTextArea(), TextAttribute.ITALIC, active.getDesiredStyle());
        toggleSelectedButton(btnItalic);

        active.setDesiredStyleChanged(true);
    }
    @FXML
    private void underline(){
        TextAreaController active = textAreas.get(activeNote);
        TextStyle newStyle = active.getDesiredStyle().toggleUnderline();
        active.setDesiredStyle(newStyle);

        TextStyle.toggleStyle(active.getTextArea(), TextAttribute.UNDERLINE, active.getDesiredStyle());
        toggleSelectedButton(btnUnderline);

        active.setDesiredStyleChanged(true);
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
        TextAreaController active = textAreas.get(activeNote);
        TextStyle newStyle = active.getDesiredStyle().setFontSize(Integer.parseInt(size));
        active.setDesiredStyle(newStyle);

        TextStyle.toggleStyle(active.getTextArea(), TextAttribute.FONT_SIZE, active.getDesiredStyle());

        active.setDesiredStyleChanged(true);
    }


    @FXML
    private void closeTab(ActionEvent event) {
        Node source = (Node) event.getSource();

        for (Tab tab : tabPane.getTabs()) {
            if (tab.getGraphic() instanceof HBox hbox) {
                if (hbox.getChildren().contains(source)) {
//                    TODO: Check if code is safe, prone to errors
                    textAreas.remove(tab.getUserData());
                    tabPane.getTabs().remove(tab);


                    if (!tabPane.getTabs().isEmpty())
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
        hbox.setPadding(new Insets(2 ,8,2, 8));

//        Initialize Label Title
        TextField title = new TextField(fileName);
        title.setPrefWidth(80);
        title.setMaxWidth(80);
        title.setMinWidth(80);
        title.setEditable(false);
        title.setPadding(new Insets(0,0,0,0));
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
                title.deselect();
                title.setEditable(false);
                renameFile(title);

            }
        });

        title.focusedProperty().addListener((obs,old,current) -> {
            if (!current){
                title.deselect();
                title.setEditable(false);
                renameFile(title);
            }

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

        TextAreaController newTextArea = new TextAreaController(editor,fileName);
        newTextArea.initializeUpdateToolbar(this);
        textAreas.put(fileName, newTextArea);

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
    private void renameFile(TextField tf){
        Object tag = tf.getUserData()==null ? "" : tf.getUserData();

        if (tag.equals(tf.getText())) return;

        Tab tab = null;

        for (Tab t : tabPane.getTabs()){
            if (t.getUserData()!=null && t.getUserData().equals(tag)){
                tab = t;
                break;
            }
        }

        if (tab==null) return;

        TextAreaController temp = textAreas.get(tab.getUserData());
        textAreas.remove(tab.getUserData());
        tf.setUserData(tf.getText());
        tab.setUserData(tf.getText());
        textAreas.put(tf.getText(), temp);

//        TODO: Rename Note object
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


    private RichSegment getSegmentAt(int position) {
        if (position < 0 || position >= textAreas.get(activeNote).getTextArea().getLength()) {
            return null;
        }

        TwoDimensional.Position twoDimPos = textAreas.get(activeNote).getTextArea().offsetToPosition(position, Forward);
        int paragraphIndex = twoDimPos.getMajor();
        int column = twoDimPos.getMinor();

        Paragraph<ParStyle, RichSegment, TextStyle> paragraph = textAreas.get(activeNote).getTextArea().getParagraph(paragraphIndex);
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
