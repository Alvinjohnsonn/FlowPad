package com.staticconstants.flowpad;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class FlowPadController {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }
}