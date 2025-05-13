package com.staticconstants.flowpad.frontend;

import com.staticconstants.flowpad.FlowPadApplication;
import com.staticconstants.flowpad.frontend.textareaclasses.*;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.fxmisc.richtext.model.*;


import java.io.IOException;
import java.util.*;

import static javafx.collections.FXCollections.observableArrayList;
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
    @FXML public ComboBox headingComboBox;
    @FXML private Button btnBold;
    @FXML private Button btnItalic;
    @FXML private Button btnUnderline;
    @FXML private Button btnBack;
    @FXML private Button btnMarker;
    @FXML private ToolBar toolBar;
    @FXML private Button btnAlign;
    @FXML private Button btnClearFormatting;
    @FXML public ImageView imgActiveAlignment;
    @FXML public Button btnNumberedList;
    @FXML public Button btnBulletList;

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
            if (textAreas.get(activeNote).isProgrammaticUpdate()) return;
            handleFontSizeChange(newText);
        });

        textFieldFontSize.setOnAction(e -> {
            handleFontSizeChange(textFieldFontSize.getText());
        });

        fontComboBox.getItems().addAll(Font.getFamilies());
        fontComboBox.setOnAction(event -> {
            if (textAreas.get(activeNote).isProgrammaticUpdate()) return;
            String selectedFont = (String)fontComboBox.getValue();
            if (selectedFont != null) {
                TextAreaController active = textAreas.get(activeNote);
                active.setStyle(TextAttribute.FONT_FAMILY, selectedFont);
            }
        });

        fontComboBox.showingProperty().addListener((obs, wasShowing, isNowShowing) -> {
            if (!isNowShowing) {
                Platform.runLater(() -> textAreas.get(activeNote).getTextArea().requestFocus());
            }
        });

        headingComboBox.setItems(observableArrayList(
                new HeadingOption("Heading 1", 1),
                new HeadingOption("Heading 2", 2),
                new HeadingOption("Heading 3", 3),
                new HeadingOption("Heading 4", 4),
                new HeadingOption("Heading 5", 5),
                new HeadingOption("Normal text", 0)
        ));
        headingComboBox.setOnAction( event -> {
            TextAreaController active = textAreas.get(activeNote);

            if (active.isProgrammaticUpdate()) return;
            int headingLevel = ((HeadingOption)headingComboBox.getValue()).getLevel();
            active.setStyle(TextAttribute.HEADING_LEVEL, headingLevel);
        });
        headingComboBox.showingProperty().addListener((obs, wasShowing, isNowShowing) -> {
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

    public void showAlignStage(Node anchorNode) {
        if (alignStage != null && alignStage.isShowing()) {
            return;
        }

        alignStage = new Stage(StageStyle.TRANSPARENT);

        HBox itemBox = new HBox(4);
        itemBox.setPadding(new Insets(4));
        itemBox.setBackground(new Background(
                new BackgroundFill(Color.web("#E0EDEC"), new CornerRadii(8), Insets.EMPTY)
        ));

        itemBox.setStyle("""
    -fx-background-color: #E0EDEC;
    -fx-background-radius: 8;
""");
        Rectangle clip = new Rectangle();
        clip.setArcWidth(8);
        clip.setArcHeight(8);
        clip.widthProperty().bind(itemBox.widthProperty());
        clip.heightProperty().bind(itemBox.heightProperty());
        itemBox.setClip(clip);

        for (String icon : List.of(
                "icons/text-align-left.png", "icons/text-align-center.png",
                "icons/text-align-right.png", "icons/text-align-justify.png"
        )) {
            Image img = new Image(FlowPadApplication.class.getResource(icon).toExternalForm());
            ImageView imgView = new ImageView(img);
            imgView.setFitHeight(18);
            imgView.setFitWidth(18);
            imgView.setPreserveRatio(true);

            Button item = new Button();

            item.setGraphic(imgView);
            item.setOnAction(e -> {
                TextAlignment alignment = TextAlignment.LEFT;
                if (icon.contains("center")) alignment = TextAlignment.CENTER;
                else if (icon.contains("right")) alignment = TextAlignment.RIGHT;
                else if (icon.contains("justify")) alignment = TextAlignment.JUSTIFY;

                TextAreaController active = textAreas.get(activeNote);
                active.getTextArea().applyParStyleToSelection(active.getDesiredParStyle().setAlignment(alignment));
                imgActiveAlignment.setImage(img);

                FadeTransition fadeOut = new FadeTransition(Duration.millis(150), itemBox);
                fadeOut.setFromValue(1);
                fadeOut.setToValue(0);
                fadeOut.setOnFinished(ae -> alignStage.close());
                fadeOut.play();
            });
            itemBox.getChildren().add(item);
            item.getStyleClass().add("align-button");
            if (imgActiveAlignment.getImage().getUrl().equals(img.getUrl())) item.getStyleClass().add("active");
        }

        Scene scene = new Scene(itemBox);
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add(FlowPadApplication.class.getResource("css/editor-style.css").toExternalForm());

        alignStage.setAlwaysOnTop(true);;
        alignStage.initOwner(btnAlign.getScene().getWindow());
        alignStage.initModality(Modality.NONE);
        alignStage.setScene(scene);



        alignStage.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                FadeTransition fadeOut = new FadeTransition(Duration.millis(150), itemBox);
                fadeOut.setFromValue(1);
                fadeOut.setToValue(0);
                fadeOut.setOnFinished(e -> alignStage.close());
                fadeOut.play();
            }
        });

        Bounds bounds = anchorNode.localToScreen(anchorNode.getBoundsInLocal());
        alignStage.setX(bounds.getMinX());
        alignStage.setY(bounds.getMaxY());

        itemBox.setOpacity(0);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(150), itemBox);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        alignStage.show();
//        TODO: Fix error if align button is hidden
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
            case HIGHLIGHT -> btn = btnMarker;
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
        active.setStyle(TextAttribute.BOLD, active.getDesiredStyle().toggleBold().isBold());
        toggleSelectedButton(btnBold);
    }
    @FXML
    private void italic(){
        TextAreaController active = textAreas.get(activeNote);
        active.setStyle(TextAttribute.ITALIC, active.getDesiredStyle().toggleItalic().isItalic());
        toggleSelectedButton(btnItalic);
    }
    @FXML
    private void underline(){
        TextAreaController active = textAreas.get(activeNote);
        active.setStyle(TextAttribute.UNDERLINE, active.getDesiredStyle().toggleUnderline().isUnderline());
        toggleSelectedButton(btnUnderline);
    }

    @FXML
    private void highlight() {
        TextAreaController active = textAreas.get(activeNote);
        active.setStyle(TextAttribute.HIGHLIGHT, active.getDesiredStyle().toggleHighlight().getBackgroundColor());
        toggleSelectedButton(btnMarker);
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
        active.setStyle(TextAttribute.FONT_SIZE, Integer.parseInt(size));
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
        TextAreaController active = textAreas.get(activeNote);
        if (active != null && active.getTextArea().isUndoAvailable()) {
            active.getTextArea().undo();
        }
    }

    @FXML
    private void redo(){
        TextAreaController active = textAreas.get(activeNote);
        if (active != null && active.getTextArea().isRedoAvailable()) {
            active.getTextArea().redo();
        }
    }
    @FXML
    private void cut(){
        TextAreaController active = textAreas.get(activeNote);
        if (active != null) {
            active.getTextArea().cut();
        }
    }
    @FXML
    private void copy(){
        TextAreaController active = textAreas.get(activeNote);
        if (active != null) {
            active.getTextArea().copy();
        }
    }

    @FXML
    private void paste(){
        TextAreaController active = textAreas.get(activeNote);
        if (active != null) {
            active.getTextArea().paste();
        }
    }

    @FXML
    private void selectAll(){
        TextAreaController active = textAreas.get(activeNote);
        if (active != null) {
            active.getTextArea().selectAll();
        }
    }
    @FXML
    private void find(){

    }
    Stage alignStage;
    @FXML
    private void align(){
        if (alignStage != null && alignStage.isShowing()) {
            alignStage.close();
        } else {
            showAlignStage(btnAlign);
        }
    }
    @FXML
    private void setLineSpacing(){

    }
    @FXML
    private void setBulletList() {
        TextAreaController active = textAreas.get(activeNote);
        ParStyle parStyle = active.getParStyleOnSelection();

        ParStyle newParStyle=null;
        if (parStyle.getListType() == ParStyle.ListType.BULLET) {
            newParStyle=parStyle.setListType(ParStyle.ListType.NONE);
            active.getTextArea().applyParStyleToSelection(newParStyle);
            btnBulletList.getStyleClass().removeAll("active");
        }
        else{
            newParStyle=parStyle.setListType(ParStyle.ListType.BULLET);
            active.getTextArea().applyParStyleToSelection(newParStyle);
            btnBulletList.getStyleClass().add("active");
        }
        if (newParStyle!=null) active.setDesiredParStyle(newParStyle);
        btnNumberedList.getStyleClass().removeAll("active");
    }
    @FXML
    private void setNumberedList(){
        TextAreaController active = textAreas.get(activeNote);
        ParStyle parStyle = active.getParStyleOnSelection();

        ParStyle newParStyle=null;
        if (parStyle.getListType() == ParStyle.ListType.NUMBERED) {
            newParStyle=parStyle.setListType(ParStyle.ListType.NONE);
            active.getTextArea().applyParStyleToSelection(newParStyle);
            btnNumberedList.getStyleClass().removeAll("active");
        }
        else{
            newParStyle=parStyle.setListType(ParStyle.ListType.NUMBERED);
            active.getTextArea().applyParStyleToSelection(newParStyle);
            btnNumberedList.getStyleClass().add("active");
        }
        if (newParStyle!=null) active.setDesiredParStyle(newParStyle);
        btnBulletList.getStyleClass().removeAll("active");
    }
    @FXML
    private void clearFormatting(){

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
