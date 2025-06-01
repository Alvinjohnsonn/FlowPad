package com.staticconstants.flowpad.frontend;

import com.staticconstants.flowpad.FlowPadApplication;
import com.staticconstants.flowpad.backend.AI.Prompt;
import com.staticconstants.flowpad.backend.LoggedInUser;
import com.staticconstants.flowpad.backend.db.notes.Note;
import com.staticconstants.flowpad.backend.db.notes.NoteDAO;
import com.staticconstants.flowpad.backend.notes.StyledTextCodec;
import com.staticconstants.flowpad.frontend.folders.FolderTreeBuilder;
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
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import static javafx.collections.FXCollections.observableArrayList;
import static org.controlsfx.tools.Utils.getWindow;
import static org.fxmisc.richtext.model.TwoDimensional.Bias.Forward;
/**
 * The main controller class for the FlowPad text editor application.
 * Handles all UI interactions, document management, and text formatting operations.
 */
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

    /** Map of filename to TextAreaController instances */
    private static HashMap<String, TextAreaController> textAreas;
    /** The currently active note being edited */
    private static Note activeNote;
    /** The popup stage used for various dialogs */
    private static Stage popup;

    /**
     * Shows the documents view and hides AI options.
     */
    @FXML
    private void showDocuments() {
        setActiveButton(btnDocuments, btnAi);

        folderTree.setVisible(true);
        folderTree.setManaged(true);

        aiOptions.setVisible(false);
        aiOptions.setManaged(false);
    }

    /**
     * Shows the AI options view and hides documents.
     */
    @FXML
    private void showAIOptions() {
        setActiveButton(btnAi, btnDocuments);

        folderTree.setVisible(false);
        folderTree.setManaged(false);

        aiOptions.setVisible(true);
        aiOptions.setManaged(true);
    }

    /**
     * Initializes the controller after FXML loading.
     * Sets up UI components, event handlers, and initial state.
     */
    @FXML
    public void initialize() {

        textAreas = new HashMap<>();

        if ( LoggedInUser.notes.isEmpty() ) {
            System.out.println("New note");
            newNote();
        }
        else {
            List<Note> notes = new ArrayList<>(LoggedInUser.notes.values().stream().toList());
            notes.sort(Comparator.comparingLong(Note::getLastModifiedTime)); // most recent note is last in the list now
            activeNote = notes.getLast();
        }

        TreeItem<String> rootItem = new TreeItem<>();
        rootItem.setExpanded(true);

        List<List<String>> folders = new ArrayList<>();
        // setup treeview
        for (Note note : LoggedInUser.notes.values())
        {
            List<String> fileTree = new ArrayList<>(note.getFolders().stream().toList());
            fileTree.add(note.getFilename());
            folders.add(fileTree);
        }

        List<TreeItem<String>> trees = FolderTreeBuilder.buildTreeItems(folders);
        rootItem.getChildren().addAll(trees);
        rootItem.getChildren().forEach(tree -> {
            if (activeNote.getFolders().contains(tree.getValue())) {
                tree.setExpanded(true);
            }
        });
        folderTree.setRoot(rootItem);

        folderTree.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {

            for (Tab tab : tabPane.getTabs()) {
                if (tab.getUserData().toString().equals(newValue.getValue())) {
                    tabPane.getSelectionModel().select(tab);
                    return;
                }
            }

            if (newValue != null && newValue.isLeaf()) {
                // Perform your action here
                String selectedValue = newValue.getValue();

                try {
                    openNote(selectedValue);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                textAreas.get(activeNote.getFilename()).reload();
            }
        });

        zoomSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int zoomPercent = newVal.intValue();
            zoomLabel.setText(zoomPercent + "%");
        });

        textFieldFontSize.setText(TextStyle.EMPTY.getFontSize()+"");
        SplitPane splitPane = new SplitPane();
        splitPane.getStyleClass().add("inner-split");

        TextAreaController tac = new TextAreaController(editorContainer, activeNote.getFilename(), splitPane);
        tac.initializeUpdateToolbar(this);
        textAreas.put(activeNote.getFilename(), tac);

        textFieldFontSize.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getText();
            return newText.matches("[0-9]*") ? change : null;
        }));

        textFieldFontSize.textProperty().addListener((obs, oldText, newText) -> {
            if (textAreas.get(activeNote.getFilename()).isProgrammaticUpdate()) return;
            handleFontSizeChange(newText);
        });

        textFieldFontSize.setOnAction(e -> {
            handleFontSizeChange(textFieldFontSize.getText());
        });

        fontComboBox.getItems().addAll(Font.getFamilies());
        fontComboBox.setOnAction(event -> {
            if (textAreas.get(activeNote.getFilename()).isProgrammaticUpdate()) return;
            String selectedFont = (String)fontComboBox.getValue();
            if (selectedFont != null) {
                TextAreaController active = textAreas.get(activeNote.getFilename());
                active.setStyle(TextAttribute.FONT_FAMILY, selectedFont);
            }
        });

        fontComboBox.showingProperty().addListener((obs, wasShowing, isNowShowing) -> {
            if (!isNowShowing) {
                Platform.runLater(() -> textAreas.get(activeNote.getFilename()).getTextArea().requestFocus());
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
            TextAreaController active = textAreas.get(activeNote.getFilename());

            if (active.isProgrammaticUpdate()) return;
            int headingLevel = ((HeadingOption)headingComboBox.getValue()).getLevel();
            active.setStyle(TextAttribute.HEADING_LEVEL, headingLevel);
        });
        headingComboBox.showingProperty().addListener((obs, wasShowing, isNowShowing) -> {
            if (!isNowShowing) {
                Platform.runLater(() -> textAreas.get(activeNote.getFilename()).getTextArea().requestFocus());
            }
        });

        tabPane.getSelectionModel().selectedItemProperty().addListener(
                (obs,oldTab, newTab) -> {
                    if (oldTab == newTab || newTab==null) return;
                    SplitPane sp = (SplitPane)newTab.getContent();
                    VBox vb = (VBox)sp.getItems().getFirst();

                    if (!vb.getChildren().contains(toolBar)) vb.getChildren().addFirst(toolBar);

                    activeNote = LoggedInUser.notes.get(newTab.getUserData().toString());
                    textAreas.get(activeNote.getFilename()).reload();
                });
        tabPane.getTabs().removeFirst(); // delete the existing tab used for visual design purposes


        // Initialize Button AI On Click
        btnGenerateSummary.setOnAction(e -> {
            textAreas.get(activeNote.getFilename()).getAIConnector().setActivePromptType(Prompt.GENERATE_SUMMARY);
            showTextSelectOptionPopup(((Button)e.getSource()));
        });
        btnAIHighlight.setOnAction(e -> {
            textAreas.get(activeNote.getFilename()).getAIConnector().setActivePromptType(Prompt.AI_HIGHLIGHT);
            showTextSelectOptionPopup(((Button)e.getSource()));
        });
        btnAutoCorrect.setOnAction(e -> {
            textAreas.get(activeNote.getFilename()).getAIConnector().setActivePromptType(Prompt.AUTO_CORRECT);
            showTextSelectOptionPopup(((Button)e.getSource()));
        });
        btnRefactorContent.setOnAction(e -> {
            textAreas.get(activeNote.getFilename()).getAIConnector().setActivePromptType(Prompt.REFACTOR_CONTENT);
            showTextSelectOptionPopup(((Button)e.getSource()));
        });
        btnGenerateOutline.setOnAction(e -> {
            textAreas.get(activeNote.getFilename()).getAIConnector().setActivePromptType(Prompt.GENERATE_OUTLINE);
            showTextSelectOptionPopup(((Button)e.getSource()));
        });
        btnFormatWriting.setOnAction(e -> {
            textAreas.get(activeNote.getFilename()).getAIConnector().setActivePromptType(Prompt.FORMAT_WRITING);
            showTextSelectOptionPopup(((Button)e.getSource()));
        });
        btnShortToFull.setOnAction(e -> {
            textAreas.get(activeNote.getFilename()).getAIConnector().setActivePromptType(Prompt.SHORT_TO_FULL);
            showTextSelectOptionPopup(((Button)e.getSource()));
        });
        btnCustomPrompt.setOnAction(e -> {
            textAreas.get(activeNote.getFilename()).getAIConnector().setActivePromptType(Prompt.CUSTOM_PROMPT);
            showTextSelectOptionPopup(((Button)e.getSource()));
        });
    }

    /**
     * Initializes a popup stage with common settings.
     *
     * @param tag The identifier for the popup
     * @param scene The scene to display in the popup
     * @param container The root container node
     * @param screenX The x position on screen
     * @param screenY The y position on screen
     */
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

    /**
     * Shows the text alignment options popup.
     *
     * @param anchorNode The node to anchor the popup to
     */
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

                TextAreaController active = textAreas.get(activeNote.getFilename());
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

    /**
     * Shows the color picker popup for text color selection.
     *
     * @param anchorNode The node to anchor the popup to
     */
    private void showColorPicker(Node anchorNode) {
        String tag = "setTextColor";
        if (popup != null && popup.isShowing() && popup.getUserData().equals(tag)) {
            return;
        }

        TextAreaController active = textAreas.get(activeNote.getFilename());
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

    /**
     * Shows the hyperlink editor popup.
     *
     * @param textNode The text node being edited
     * @param segment The current hyperlink segment
     * @param screenX The x position on screen
     * @param screenY The y position on screen
     * @param onConfirm Callback when changes are confirmed
     */
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

    /**
     * Shows the text selection options popup for AI operations.
     *
     * @param anchorNode The node to anchor the popup to
     */
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

        AIConnector aiCon = textAreas.get(activeNote.getFilename()).getAIConnector();
        btnSelectAll.setOnAction(e -> {
            popup.close();
            if (aiCon.getActivePromptType() == Prompt.CUSTOM_PROMPT)
                aiCon.showSelectConfirmation(true);
            else textAreas.get(activeNote.getFilename()).showAIOutput(aiCon.getAllText(), "");
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

    /**
     * Shows the selection confirmation popup for AI operations.
     *
     * @param screenX The x position on screen
     * @param screenY The y position on screen
     * @param selectedText The text that was selected
     * @param isSelectAll Whether all text was selected
     */
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

        if (textAreas.get(activeNote.getFilename()).getAIConnector().getActivePromptType() == Prompt.CUSTOM_PROMPT){
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

        AIConnector aiCon = textAreas.get(activeNote.getFilename()).getAIConnector();
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
            textAreas.get(activeNote.getFilename()).showAIOutput(selectedText, prompt.getText());
        });

        popup.show();
    }

    /**
     * Converts a style HashMap to a CSS style string.
     *
     * @param styles The style map to convert
     * @return The CSS style string
     */
    public static String hashMapStyleToString(HashMap<String, String> styles){
        String styleString = "";
        for (String style : styles.keySet()){
            styleString += style + ":" + styles.get(style) + ";";
        }
        return styleString;
    }

    /**
     * Sets the active state for a button pair (one active, one inactive).
     *
     * @param active The button to set as active
     * @param inactive The button to set as inactive
     */
    public static void setActiveButton(Button active, Button inactive) {
        if (!active.getStyleClass().contains("selected")) active.getStyleClass().add("selected");

        inactive.getStyleClass().removeAll("selected");
    }

    /**
     * Sets the selected state for a formatting button.
     *
     * @param att The text attribute being toggled
     * @param isSelected Whether the button should appear selected
     */
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

    /**
     * Toggles the selected state of a button.
     *
     * @param btn The button to toggle
     */
    public static void toggleSelectedButton(Button btn) {
        if (!btn.getStyleClass().contains("selected")) btn.getStyleClass().add("selected");
        else btn.getStyleClass().removeAll("selected");
    }

    /**
     * Increases the font size of the selected text.
     */
    @FXML
    private void increaseFontSize() {
        int fontSize = Integer.parseInt(textFieldFontSize.getText());
        fontSize++;
        textFieldFontSize.setText(fontSize + "");
        TextStyle newStyle = textAreas.get(activeNote.getFilename()).getDesiredStyle().setFontSize(fontSize);
        textAreas.get(activeNote.getFilename()).setDesiredStyle(newStyle);

        textAreas.get(activeNote.getFilename()).setDesiredStyleChanged(true);
    }

    /**
     * Decreases the font size of the selected text.
     */
    @FXML
    private void decreaseFontSize() {
        int fontSize = Integer.parseInt(textFieldFontSize.getText());
        if (fontSize > 1) {
            fontSize--;
            textFieldFontSize.setText(fontSize + "");
            TextStyle newStyle = textAreas.get(activeNote.getFilename()).getDesiredStyle().setFontSize(fontSize);
            textAreas.get(activeNote.getFilename()).setDesiredStyle(newStyle);

            textAreas.get(activeNote.getFilename()).setDesiredStyleChanged(true);
        }
    }

    /**
     * Toggles bold formatting for the selected text.
     */
    @FXML
    private void bold(){
        TextAreaController active = textAreas.get(activeNote.getFilename());
        active.setStyle(TextAttribute.BOLD, active.getDesiredStyle().toggleBold().isBold());
        toggleSelectedButton(btnBold);
    }

    /**
     * Toggles italic formatting for the selected text.
     */
    @FXML
    private void italic(){
        TextAreaController active = textAreas.get(activeNote.getFilename());
        active.setStyle(TextAttribute.ITALIC, active.getDesiredStyle().toggleItalic().isItalic());
        toggleSelectedButton(btnItalic);
    }

    /**
     * Toggles underline formatting for the selected text.
     */
    @FXML
    private void underline(){
        TextAreaController active = textAreas.get(activeNote.getFilename());
        active.setStyle(TextAttribute.UNDERLINE, active.getDesiredStyle().toggleUnderline().isUnderline());
        toggleSelectedButton(btnUnderline);
    }

    /**
     * Toggles highlight formatting for the selected text.
     */
    @FXML
    private void highlight() {
        TextAreaController active = textAreas.get(activeNote.getFilename());
        active.setStyle(TextAttribute.HIGHLIGHT, active.getDesiredStyle().toggleHighlight().getBackgroundColor());
        toggleSelectedButton(btnMarker);
    }

    /**
     * Saves the current note to the database.
     */
    @FXML
    private void save(){

        try {

            byte[] serializedText = StyledTextCodec.serializeStyledText(textAreas.get(activeNote.getFilename()).getTextArea());
            NoteDAO dao = new NoteDAO();
            activeNote.setSerializedText(serializedText);
            LoggedInUser.notes.put(activeNote.getFilename(), activeNote);

            if (activeNote.isNewNote()) {
                System.out.println("Inserting note");
                dao.insert(activeNote);
                activeNote.existingNote();
                folderTree.getRoot().getChildren().getLast().getChildren().add(new TreeItem<>(activeNote.getFilename()));
            }
            else {
                System.out.println("Updating note");
                dao.update(activeNote);
            }

        } catch (IOException ex) {
            // TODO: Add better exception handling
            System.err.println("Failed to serialize text");
        }

    }

    /**
     * Handles font size changes from the text field.
     *
     * @param size The new font size as a string
     */
    @FXML
    private void handleFontSizeChange(String size){
        TextAreaController active = textAreas.get(activeNote.getFilename());
        active.setStyle(TextAttribute.FONT_SIZE, Integer.parseInt(size));
    }

    /**
     * Closes the specified tab.
     *
     * @param event The action event that triggered the close
     */
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
                    {
                        activeNote = LoggedInUser.notes.get(tabPane.getTabs().getFirst().getUserData().toString());
                    }

                    break;
                }
            }
        }
    }

    /**
     * Opens a note in a new tab.
     *
     * @param fileName The name of the note file to open
     * @throws IOException If there's an error loading the note
     */
    private void openNote(String fileName) throws IOException {
        activeNote = LoggedInUser.notes.get(fileName);

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

        TextAreaController newTextArea = new TextAreaController(editor,fileName, splitPane);
        StyledTextCodec.deserializeStyledText(activeNote.getSerializedText(), newTextArea.getTextArea());
        newTextArea.initializeUpdateToolbar(this);
        textAreas.put(activeNote.getFilename(), newTextArea);

//        Complete All Setup
        mainContainer.getChildren().add(editor);
        splitPane.getItems().add(mainContainer);
        newTab.setContent(splitPane);

        tabPane.getTabs().add(newTab);
        tabPane.getSelectionModel().select(newTab);
    }

    /**
     * Creates a new note and opens it in a tab.
     */
    private static int numOfNewNote = 0;
    @FXML
    private void newNote(){
        String fileName = "New Note" + (numOfNewNote>0 ? " "+numOfNewNote : "") ;
        numOfNewNote++;

        activeNote = new Note(fileName, new byte[]{}, new String[]{});
        LoggedInUser.notes.put(fileName, activeNote);

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
        textAreas.put(activeNote.getFilename(), newTextArea);

//        Complete All Setup
        mainContainer.getChildren().add(editor);
        splitPane.getItems().add(mainContainer);
        newTab.setContent(splitPane);

        tabPane.getTabs().add(newTab);
        tabPane.getSelectionModel().select(newTab);
    }

    /**
     * Performs an undo operation on the text area.
     */
    @FXML
    private void undo(){
        TextAreaController active = textAreas.get(activeNote.getFilename());
        if (active != null && active.getTextArea().isUndoAvailable()) {
            active.getTextArea().undo();
        }
    }

    /**
     * Performs a redo operation on the text area.
     */
    @FXML
    private void redo(){
        TextAreaController active = textAreas.get(activeNote.getFilename());
        if (active != null && active.getTextArea().isRedoAvailable()) {
            active.getTextArea().redo();
        }
    }

    /**
     * Cuts the selected text to the clipboard.
     */
    @FXML
    private void cut(){
        TextAreaController active = textAreas.get(activeNote.getFilename());
        if (active != null) {
            active.getTextArea().cut();
        }
    }

    /**
     * Copies the selected text to the clipboard.
     */
    @FXML
    private void copy(){
        TextAreaController active = textAreas.get(activeNote.getFilename());
        if (active != null) {
            active.getTextArea().copy();
        }
    }

    /**
     * Pastes text from the clipboard.
     */
    @FXML
    private void paste(){
        TextAreaController active = textAreas.get(activeNote.getFilename());
        if (active != null) {
            active.getTextArea().paste();
        }
    }

    /**
     * Selects all text in the current text area.
     */
    @FXML
    private void selectAll(){
        TextAreaController active = textAreas.get(activeNote.getFilename());
        if (active != null) {
            active.getTextArea().selectAll();
        }
    }

    @FXML
    private void find() {
    }
    @FXML
    private void setLineSpacing() {
    }
    /**
     * Shows or hides the alignment options popup.
     */
    @FXML
    private void align(){
        if (popup != null && popup.isShowing()) {
            popup.close();
        } else {
            showAlignStage(btnAlign);
        }
    }

    /**
     * Toggles bullet list formatting for the selected paragraphs.
     */
    @FXML
    private void setBulletList() {
        TextAreaController active = textAreas.get(activeNote.getFilename());
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

    /**
     * Toggles numbered list formatting for the selected paragraphs.
     */
    @FXML
    private void setNumberedList(){
        TextAreaController active = textAreas.get(activeNote.getFilename());
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

    /**
     * Clears all formatting from the selected text (not yet implemented).
     */
    @FXML
    private void clearFormatting(){
        //TODO: Add code
    }

    /**
     * Shows the text color picker popup.
     */
    @FXML
    private void setTextColor(){
        if (popup != null && popup.isShowing()) {
            popup.close();
        } else {
            showColorPicker(btnTextColor);
        }
    }

    /**
     * Inserts a hyperlink at the current caret position or around selection.
     */
    @FXML
    private void insertHyperlink() {
        CustomStyledArea<ParStyle, RichSegment, TextStyle> area = textAreas.get(activeNote.getFilename()).getTextArea();
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

    /**
     * Opens a file chooser to insert an image at the caret position.
     */
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

    /**
     * Inserts an image at the current caret position.
     *
     * @param file The image file to insert
     */
    private void insertImageAtCaret(File file) {
        CustomStyledArea<ParStyle, RichSegment, TextStyle> area = textAreas.get(activeNote.getFilename()).getTextArea();
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

    /**
     * Renames a leaf node in the folder tree.
     *
     * @param root The root tree item to search from
     * @param targetName The current name to find
     * @param newName The new name to set
     * @return true if the node was found and renamed, false otherwise
     */
    private boolean renameLeaf(TreeItem<String> root, String targetName, String newName) {
        if (root == null) return false;

        // Check if it's a leaf and matches the target name
        if (root.isLeaf() && root.getValue().equals(targetName)) {
            root.setValue(newName);
            return true; // Found and renamed
        }

        // Recursively search children
        for (TreeItem<String> child : root.getChildren()) {
            boolean found = renameLeaf(child, targetName, newName);
            if (found) return true; // Stop on first match
        }

        return false; // Not found
    }

    /**
     * Renames the currently active note file.
     *
     * @param tf The text field containing the new name
     */
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

        String oldFilename = activeNote.getFilename();
        TextAreaController tac = textAreas.get(oldFilename);
        textAreas.remove(oldFilename);

        renameLeaf(folderTree.getRoot(), oldFilename, tf.getText());
        activeNote.setFilename(tf.getText());
        NoteDAO noteDAO = new NoteDAO();
        noteDAO.update(activeNote);

        tf.setUserData(tf.getText());
        tab.setUserData(tf.getText());

        textAreas.put(tf.getText(), tac);
        LoggedInUser.notes.remove(oldFilename);
        LoggedInUser.notes.put(tf.getText(), activeNote);
    }

    /**
     * Handles the back button click, returning to the home page.
     *
     * @throws IOException If there's an error loading the home page
     */
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

    /**
     * Handles the profile button click, opening the settings page.
     *
     * @throws IOException If there's an error loading the settings page
     */
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

    /**
     * Replaces a hyperlink segment in the text area.
     *
     * @param node The node containing the hyperlink
     * @param oldSegment The segment to replace
     * @param newSegment The new segment to insert
     */
    private static void replaceHyperlinkSegment(Node node, HyperlinkSegment oldSegment, HyperlinkSegment newSegment) {
        textAreas.get(activeNote.getFilename()).setSuppressHyperlinkMonitoring(true);
        CustomStyledArea<ParStyle, RichSegment, TextStyle> area = textAreas.get(activeNote.getFilename()).getTextArea();


        int pos = area.getCaretPosition();
        for (int i = 0; i < area.getParagraphs().size(); i++) {
            Paragraph<ParStyle, RichSegment, TextStyle> paragraph = area.getParagraph(i);
            int abs = area.getAbsolutePosition(i, 0);
            for (RichSegment seg : paragraph.getSegments()) {
                if (seg == oldSegment) {
                    int segStart = abs;
                    int segEnd = abs + seg.length();

                    area.replace(segStart, segEnd, newSegment, area.getStyleAtPosition(segStart));
                    textAreas.get(activeNote.getFilename()).setSuppressHyperlinkMonitoring(false);
                    return;
                }
                abs += seg.length();
            }
        }
        textAreas.get(activeNote.getFilename()).setSuppressHyperlinkMonitoring(false);
    }

    /**
     * Gets the segment at a specific position in the text area.
     *
     * @param controller The text area controller
     * @param position The position to check
     * @return The segment at the position, or null if not found
     */
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

    /**
     * Gets a specific style value from a CSS style string.
     *
     * @param styleString The CSS style string
     * @param key The style property to get
     * @return The style value, or empty string if not found
     */
    public static String getStyleValue(String styleString, String key) {
        Map<String, String> styles = parseStyle(styleString);
        return styles.getOrDefault(key, "");
    }

    /**
     * Parses a CSS style string into a map of properties.
     *
     * @param styleString The CSS style string to parse
     * @return A map of style properties to values
     */
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
