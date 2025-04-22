package com.staticconstants.flowpad.controller;

import com.staticconstants.flowpad.FlowPadApplication;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import javax.swing.*;
import javax.swing.text.Highlighter;
import java.io.IOException;
import java.util.Objects;

public class SettingsController {
    @FXML
    public Button Privacy;

    @FXML
    public Button backButton;

    @FXML
    public Button Highlight;

    @FXML
    private Label welcomeText;

    @FXML
    private ImageView profileImage;

    public void initialize() {
        if (profileImage != null) {
            Image image = new Image(getClass().getResource("/com/staticconstants/flowpad/profiles.jpg").toExternalForm());
            profileImage.setImage(image);
            Circle clip = new Circle(50, 50, 50);
            profileImage.setClip(clip);
        }
    }



    @FXML
    protected void onPrivacyButtonClick() throws IOException {
        Stage stage = (Stage) Privacy.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(FlowPadApplication.class.getResource("Privacy.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), stage.getWidth(), stage.getHeight());
        stage.setScene(scene);
    }
    @FXML
    protected void onHighlight() throws IOException {
        Stage stage = (Stage) Highlight.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(FlowPadApplication.class.getResource("HighlightOptions.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), stage.getWidth(), stage.getHeight());
        stage.setScene(scene);
    }



    @FXML
    private void onBackButton() throws IOException {
        Stage stage = (Stage) backButton.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("flowpad-view.fxml")); // Your main view FXML
        Scene scene = new Scene(fxmlLoader.load(), stage.getWidth(), stage.getHeight());
        stage.setScene(scene);
        String css = Objects.requireNonNull(getClass().getResource("flowpad-stylesheet.css")).toExternalForm();
        scene.getStylesheets().add(css);

        stage.setScene(scene);
    }


}

