package com.staticconstants.flowpad;

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
import java.io.IOException;
import java.util.Objects;

public class FlowPadController {
    @FXML
    public Button Privacy;

    @FXML
    private Label welcomeText;

    @FXML
    private ImageView profileImage;

    public void initialize() {
        // Load image from resources
        Image image = new Image(getClass().getResource("/com/staticconstants/flowpad/profiles.jpg").toExternalForm());



        // Set image into the ImageView
        profileImage.setImage(image);

        // Clip the ImageView with a circular mask
        Circle clip = new Circle(50, 50, 50); // x, y, radius (adjust as needed)
        profileImage.setClip(clip);
    }


    @FXML
    protected void onPrivacyButtonClick() throws IOException {
        Stage stage = (Stage) Privacy.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(FlowPadApplication.class.getResource("Privacy.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), stage.getWidth(), stage.getHeight());
        stage.setScene(scene);
    }

}
