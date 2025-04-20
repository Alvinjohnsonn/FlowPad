package com.staticconstants.flowpad;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;

public class FlowPadController {
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
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }
}
