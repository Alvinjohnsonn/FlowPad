package com.staticconstants.flowpad.frontend;

import com.staticconstants.flowpad.FlowPadApplication;
import com.staticconstants.flowpad.backend.AI.Prompt;
import com.staticconstants.flowpad.frontend.textarea.*;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.fxmisc.richtext.CustomCssMetaData;
import org.fxmisc.richtext.TextExt;
import org.fxmisc.richtext.model.*;


import java.io.File;
import java.io.IOException;
import java.sql.Array;
import java.util.*;
import java.util.function.Consumer;

import static javafx.collections.FXCollections.observableArrayList;
import static org.controlsfx.tools.Utils.getWindow;
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
    @FXML private Button btnTextColor;
    @FXML private Button btnClearFormatting;
    @FXML public ImageView imgActiveAlignment;
    @FXML public Button btnNumberedList;
    @FXML public Button btnBulletList;
    @FXML private Button profilebtn;
    @FXML public Button btnGenerateSummary;
    @FXML public Button btnAIHighlight;
    @FXML public Button btnAutoCorrect;
    @FXML public Button btnRefactorContent;
    @FXML public Button btnGenerateOutline;
    @FXML public Button btnFormatWriting;
    @FXML public Button btnShortToFull;
    @FXML public Button btnCustomPrompt;
    private AnchorPane parentPane;

    private static HashMap<String, TextAreaController> textAreas;
    private static String activeNote;

    private static Stage popup;

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


        // Initialize Button AI On Click
        btnGenerateSummary.setOnAction(e -> {
            textAreas.get(activeNote).getAIConnector().setActivePromptType(Prompt.GENERATE_SUMMARY);
            showTextSelectOptionPopup(((Button)e.getSource()));
        });
        btnAIHighlight.setOnAction(e -> {
            textAreas.get(activeNote).getAIConnector().setActivePromptType(Prompt.AI_HIGHLIGHT);
            showTextSelectOptionPopup(((Button)e.getSource()));
        });
        btnAutoCorrect.setOnAction(e -> {
            textAreas.get(activeNote).getAIConnector().setActivePromptType(Prompt.AUTO_CORRECT);
            showTextSelectOptionPopup(((Button)e.getSource()));
        });
        btnRefactorContent.setOnAction(e -> {
            textAreas.get(activeNote).getAIConnector().setActivePromptType(Prompt.REFACTOR_CONTENT);
            showTextSelectOptionPopup(((Button)e.getSource()));
        });
        btnGenerateOutline.setOnAction(e -> {
            textAreas.get(activeNote).getAIConnector().setActivePromptType(Prompt.GENERATE_OUTLINE);
            showTextSelectOptionPopup(((Button)e.getSource()));
        });
        btnFormatWriting.setOnAction(e -> {
            textAreas.get(activeNote).getAIConnector().setActivePromptType(Prompt.FORMAT_WRITING);
            showTextSelectOptionPopup(((Button)e.getSource()));
        });
        btnShortToFull.setOnAction(e -> {
            textAreas.get(activeNote).getAIConnector().setActivePromptType(Prompt.SHORT_TO_FULL);
            showTextSelectOptionPopup(((Button)e.getSource()));
        });
        btnCustomPrompt.setOnAction(e -> {
            textAreas.get(activeNote).getAIConnector().setActivePromptType(Prompt.CUSTOM_PROMPT);
            showTextSelectOptionPopup(((Button)e.getSource()));
        });

        newNote();
    }
    private static void initPopupStage(String tag, Scene scene, Node container, double screenX, double screenY){
        popup = new Stage(StageStyle.TRANSPARENT);
        popup.setUserData(tag);
        popup.setAlwaysOnTop(true);;
        popup.initModality(Modality.NONE);
        popup.setScene(scene);

        popup.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                popup.close();
            }
        });

        popup.setX(screenX);
        popup.setY(screenY);

        //        TODO: Fix error if anchorNode is outside of bounds
    }

    private void showAlignStage(Node anchorNode) {
        String tag = "setAlignment";
        if (popup != null && popup.isShowing() && popup.getUserData().equals(tag)) {
            return;
        }

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
                fadeOut.setOnFinished(ae -> popup.close());
                fadeOut.play();
            });
            itemBox.getChildren().add(item);
            item.getStyleClass().add("align-button");
            if (imgActiveAlignment.getImage().getUrl().equals(img.getUrl())) item.getStyleClass().add("active");
        }

        Scene scene = new Scene(itemBox);
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add(FlowPadApplication.class.getResource("css/editor-style.css").toExternalForm());

        Bounds bounds = anchorNode.localToScreen(anchorNode.getBoundsInLocal());

        initPopupStage(tag, scene, itemBox, bounds.getMinX(), bounds.getMaxY());
        popup.initOwner(btnAlign.getScene().getWindow());

        itemBox.setOpacity(0);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(150), itemBox);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
        popup.show();
    }

    private void showColorPicker(Node anchorNode) {
        String tag = "setTextColor";
        if (popup != null && popup.isShowing() && popup.getUserData().equals(tag)) {
            return;
        }

        TextAreaController active = textAreas.get(activeNote);
        ColorPicker picker = new ColorPicker();

        picker.setValue(active.getTextArea().getStyleAtPosition(active.getTextArea().getCaretPosition()).getTextColor());
        picker.setOnAction(e->{
            active.setStyle(TextAttribute.TEXT_COLOR, picker.getValue());
            popup.close();
        });

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

        itemBox.getChildren().add(picker);

        Scene scene = new Scene(itemBox);
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add(FlowPadApplication.class.getResource("css/editor-style.css").toExternalForm());

        Bounds bounds = anchorNode.localToScreen(anchorNode.getBoundsInLocal());

        popup = new Stage(StageStyle.TRANSPARENT);
        popup.setUserData(tag);
        popup.setScene(scene);
        popup.initModality(Modality.NONE);
        popup.setX(bounds.getMinX());
        popup.setY(bounds.getMaxY());
        popup.initOwner(btnAlign.getScene().getWindow());

        itemBox.setOpacity(0);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(150), itemBox);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
        popup.show();
    }

    public static void showHyperlinkEditorPopup(TextExt textNode, HyperlinkSegment segment, double screenX, double screenY, Consumer<HyperlinkSegment> onConfirm) {
        String tag = "setHyperlink";
        if (popup != null && popup.isShowing() && popup.getUserData().equals(tag)) {
            return;
        }

        TextField displayField = new TextField(segment.getDisplayText());
        TextField urlField = new TextField(segment.getUrl());

        Button saveButton = new Button("Save");
        Button cancelButton = new Button("Cancel");
        saveButton.setStyle("-fx-background-color: -primary-color; -fx-background-radius: 10;");
        cancelButton.setStyle("-fx-background-color: -primary-color; -fx-background-radius: 10;");

        HBox buttons = new HBox(10, saveButton, cancelButton);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        VBox layout = new VBox(10, new Label("Display Text:"), displayField, new Label("URL:"), urlField, buttons);
        layout.setPadding(new Insets(15));
        layout.setStyle("-fx-background-color: -secondary-color; -fx-background-radius: 10;");
        layout.setEffect(new DropShadow(10, Color.rgb(0, 0, 0, 0.3)));

        StackPane root = new StackPane(layout);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: transparent; -fx-background-radius: 10;");
        root.setPickOnBounds(false);

        Scene scene = new Scene(root);
        scene.getStylesheets().add(FlowPadApplication.class.getResource("css/editor-style.css").toExternalForm());
        scene.setFill(Color.TRANSPARENT);

        initPopupStage(tag, scene, layout, screenX, screenY);
        if (textNode.getScene()!=null) popup.initOwner(textNode.getScene().getWindow());

        saveButton.setOnAction(e -> {
            String newDisplay = displayField.getText().trim();
            String newUrl = urlField.getText().trim();

            if (!newUrl.startsWith("https://")) newUrl = "https://"+newUrl;

            if (!newDisplay.isEmpty() && !newUrl.isEmpty()) {
                HyperlinkSegment newSegment = new HyperlinkSegment(newDisplay, newUrl);
                replaceHyperlinkSegment(textNode, segment, newSegment);

                if (onConfirm != null) {
                    onConfirm.accept(newSegment);
                }
            }

            popup.close();
        });

        cancelButton.setOnAction(e -> popup.close());
        popup.show();
    }

    public void showTextSelectOptionPopup(Node anchorNode){
        String tag = "setTextSelect";
        if (popup != null && popup.isShowing() && popup.getUserData().equals(tag)) {
            return;
        }

        Label title = new Label("Select text to modify");
        title.setStyle("-fx-font-weight: bold;");

        Button btnSelectAll = new Button("Select all");
        Button btnSelectPar = new Button("Select paragraph");
        Button btnSelectCursor = new Button("Select manually");

        btnSelectAll.getStyleClass().add("align-button");
        btnSelectPar.getStyleClass().add("align-button");
        btnSelectCursor.getStyleClass().add("align-button");

        VBox layout = new VBox(10, title, btnSelectAll, btnSelectPar, btnSelectCursor);
        layout.setPadding(new Insets(15));
        layout.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        layout.setEffect(new DropShadow(10, Color.rgb(0, 0, 0, 0.3)));

        StackPane root = new StackPane(layout);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: transparent; -fx-background-radius: 10;");
        root.setPickOnBounds(false);

        Scene scene = new Scene(root);
        scene.getStylesheets().add(FlowPadApplication.class.getResource("css/editor-style.css").toExternalForm());
        scene.setFill(Color.TRANSPARENT);

        Bounds bounds = anchorNode.localToScreen(anchorNode.getBoundsInLocal());

        initPopupStage(tag, scene, layout, bounds.getMinX(), bounds.getMaxY());
        popup.initOwner(anchorNode.getScene().getWindow());

        AIConnector aiCon = textAreas.get(activeNote).getAIConnector();
        btnSelectAll.setOnAction(e -> {
            popup.close();
            if (aiCon.getActivePromptType() == Prompt.CUSTOM_PROMPT)
                aiCon.showSelectConfirmation(true);
            else textAreas.get(activeNote).showAIOutput(aiCon.getAllText(), "");
        });
        btnSelectPar.setOnAction(e -> {
            popup.close();
            aiCon.startHighlightParagraphOnHover();
        });
        btnSelectCursor.setOnAction(e -> {
            popup.close();
            aiCon.startTrackingSelection();
        });

        popup.show();
    }

    public static void showSelectConfirmationPopup(double screenX, double screenY, String selectedText, boolean isSelectAll){
        String tag = "setSelectConfirm";
        if (popup != null && popup.isShowing() && popup.getUserData().equals(tag)) {
            return;
        }

        Label title = new Label("Confirm Selection");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px");

//        Label content = new Label(selectedText);
//        content.setWrapText(true);
//        content.setMaxWidth(400);

        TextArea content = new TextArea(selectedText);
        content.setWrapText(true);
        content.setEditable(false);
        content.setFocusTraversable(false);
        content.setStyle("-fx-font-size: 12px;");
        content.setMinWidth(24);
        VBox.setVgrow(content, Priority.ALWAYS);

        if (isSelectAll){
            content.setManaged(false);
            content.setVisible(false);
        }
        TextField prompt = new TextField();
        prompt.setPromptText("Enter your custom prompt...");
        prompt.requestFocus();

        Button btnCancel = new Button("Cancel");
        Button btnReselect = new Button("Reselect");
        Button btnConfirm = new Button("Confirm");
        btnCancel.setStyle("-fx-font-size: 12px; -fx-background-color: -primary-color;");
        btnReselect.setStyle("-fx-font-size: 12px; -fx-background-color: -primary-color;");
        btnConfirm.setStyle("-fx-font-size: 12px; -fx-background-color: -primary-color;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox buttons = new HBox(12);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(buttons, Priority.ALWAYS);

        buttons.getChildren().addAll(btnCancel, spacer, btnReselect, btnConfirm);

        VBox layout = new VBox(10, title, content, buttons);

        if (textAreas.get(activeNote).getAIConnector().getActivePromptType() == Prompt.CUSTOM_PROMPT){
            layout = new VBox(10, title, content, prompt, buttons);
        }

        layout.setPadding(new Insets(15));
        layout.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        layout.setEffect(new DropShadow(10, Color.rgb(0, 0, 0, 0.3)));

        StackPane root = new StackPane(layout);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: transparent; -fx-background-radius: 10;");
        root.setPickOnBounds(false);

        Scene scene = new Scene(root);
        scene.getStylesheets().add(FlowPadApplication.class.getResource("css/editor-style.css").toExternalForm());
        scene.setFill(Color.TRANSPARENT);

        initPopupStage(tag, scene, layout, screenX, screenY);
//        popup.initOwner(tabPane.getScene().getWindow());

        AIConnector aiCon = textAreas.get(activeNote).getAIConnector();
        btnCancel.setOnAction(e -> {
            aiCon.cancelOperation();
            popup.close();
        });
        btnReselect.setOnAction(e -> {
            popup.close();
        });
        btnConfirm.setOnAction(e -> {
            if (aiCon.getActivePromptType() == Prompt.CUSTOM_PROMPT && prompt.getText().isEmpty()){
                prompt.requestFocus();
                return;
            }

            popup.close();

            aiCon.cancelOperation();
            textAreas.get(activeNote).showAIOutput(selectedText, prompt.getText());
        });

        popup.show();
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

//        Initialize Editor Container
        VBox editor = new VBox();
        editor.prefWidth(500);
        VBox.setVgrow(editor,Priority.ALWAYS);
        editor.setPadding(new Insets(10,10,10,10));

        TextAreaController newTextArea = new TextAreaController(editor, fileName, splitPane);
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

    @FXML
    private void align(){
        if (popup != null && popup.isShowing()) {
            popup.close();
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
        //TODO: Add code
    }
    @FXML
    private void setTextColor(){
        if (popup != null && popup.isShowing()) {
            popup.close();
        } else {
            showColorPicker(btnTextColor);
        }
    }
    @FXML
    private void insertHyperlink() {
        CustomStyledArea<ParStyle, RichSegment, TextStyle> area = textAreas.get(activeNote).getTextArea();
        int caretPos = area.getCaretPosition();
        IndexRange selection = area.getSelection();

        String selectedText = selection.getLength() > 0
                ? area.getText(selection.getStart(), selection.getEnd())
                : "";

        TextExt dummy = new TextExt();
        Bounds caretBounds = area.getCaretBounds().orElse(null);

        Point2D screenPos = caretBounds != null
                ? new Point2D(caretBounds.getMinX(), caretBounds.getMaxY())
                : new Point2D(500, 500);

        showHyperlinkEditorPopup(dummy, new HyperlinkSegment(selectedText, ""), screenPos.getX(), screenPos.getY(), newSegment -> {
            if (selection.getLength() > 0) {
                area.replace(selection.getStart(), selection.getEnd(), newSegment, area.getStyleAtPosition(selection.getStart()));
            } else {
                area.insert(caretPos, newSegment, area.getStyleAtPosition(caretPos));
            }
        });
    }

    @FXML
    private void insertImage(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        File userHome = new File(System.getProperty("user.home"));
        File downloadsFolder = new File(userHome, "Downloads");

        if (downloadsFolder.exists() && downloadsFolder.isDirectory()) {
            fileChooser.setInitialDirectory(downloadsFolder);
        }
        File file = fileChooser.showOpenDialog(tabPane.getScene().getWindow());

        if (file != null) {
            insertImageAtCaret(file);
        }
    }

    private void insertImageAtCaret(File file) {
        CustomStyledArea<ParStyle, RichSegment, TextStyle> area = textAreas.get(activeNote).getTextArea();
        int caretPos = area.getCaretPosition();
        IndexRange selection = area.getSelection();

        Image image = new Image(file.toURI().toString());
        ImageSegment imageSegment = new ImageSegment(image);

        if (selection.getLength() > 0) {
            area.replace(selection.getStart(), selection.getEnd(), imageSegment, area.getStyleAtPosition(selection.getStart()));
        } else {
            area.insert(caretPos, imageSegment, area.getStyleAtPosition(caretPos));
        }
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
        if (activeNote == tab.getUserData()) activeNote = tf.getText();
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


    @FXML
    protected void onProfileButtonClick() throws IOException {
        Stage stage = (Stage) profilebtn.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(FlowPadApplication.class.getResource("settings-view.fxml"));
        String stylesheet =  FlowPadApplication.class.getResource("flowpad-stylesheet.css").toExternalForm();

        Scene scene = new Scene(fxmlLoader.load());
        scene.getStylesheets().add(stylesheet);
        stage.setTitle("Settings Page");

        stage.setScene(scene);
        stage.setMaximized(true);
    }



    private static void replaceHyperlinkSegment(Node node, HyperlinkSegment oldSegment, HyperlinkSegment newSegment) {
        textAreas.get(activeNote).setSuppressHyperlinkMonitoring(true);
        CustomStyledArea<ParStyle, RichSegment, TextStyle> area = textAreas.get(activeNote).getTextArea();


        int pos = area.getCaretPosition();
        for (int i = 0; i < area.getParagraphs().size(); i++) {
            Paragraph<ParStyle, RichSegment, TextStyle> paragraph = area.getParagraph(i);
            int abs = area.getAbsolutePosition(i, 0);
            for (RichSegment seg : paragraph.getSegments()) {
                if (seg == oldSegment) {
                    int segStart = abs;
                    int segEnd = abs + seg.length();

                    area.replace(segStart, segEnd, newSegment, area.getStyleAtPosition(segStart));
                    textAreas.get(activeNote).setSuppressHyperlinkMonitoring(false);
                    return;
                }
                abs += seg.length();
            }
        }
        textAreas.get(activeNote).setSuppressHyperlinkMonitoring(false);
    }


    public static RichSegment getSegmentAt(TextAreaController controller, int position) {
        if (position < 0 || position >= controller.getTextArea().getLength()) {
            return null;
        }

        TwoDimensional.Position twoDimPos = controller.getTextArea().offsetToPosition(position, Forward);
        int paragraphIndex = twoDimPos.getMajor();
        int column = twoDimPos.getMinor();

        Paragraph<ParStyle, RichSegment, TextStyle> paragraph = controller.getTextArea().getParagraph(paragraphIndex);
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

    public static String getStyleValue(String styleString, String key) {
        Map<String, String> styles = parseStyle(styleString);
        return styles.getOrDefault(key, "");
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



}
