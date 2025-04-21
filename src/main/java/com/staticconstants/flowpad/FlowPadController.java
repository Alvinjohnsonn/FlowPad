package com.staticconstants.flowpad;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class FlowPadController {

    @FXML
    private Button RegisterAccountButton;
    @FXML
    private Label welcomeText;

    @FXML
    private Button SubmitButton;



    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("");
    }


    // When the Submit Button is clicked, could lead to the Home Page.
    @FXML
    protected void onSubmitButtonClick() throws IOException {
        Stage stage = (Stage) SubmitButton.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(FlowPadApplication.class.getResource("Submit-Page.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Home Page");
        stage.setScene(scene);
    }


    @FXML
    protected void onRegisterAccountButtonClick() throws IOException {
        Stage stage = (Stage) RegisterAccountButton.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(FlowPadApplication.class.getResource("Register-Page.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Register Page");
        stage.setScene(scene);
    }

}