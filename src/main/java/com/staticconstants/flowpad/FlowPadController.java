package com.staticconstants.flowpad;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.scene.image.Image;

import java.io.IOException;

public class FlowPadController {

    @FXML
    private ImageView Logo;
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

    @FXML
    public void initialize() {

        Image image = new Image(getClass().getResource("/com/staticconstants/flowpad/Logo.jpg").toExternalForm());

        Logo.setImage(image);

        Circle clip = new Circle(150, 150, 150);
        Logo.setClip(clip);
    }

    // When the Submit Button is clicked, could lead to the Home Page.
    @FXML
    protected void onSubmitButtonClick() throws IOException {
        Stage stage = (Stage) SubmitButton.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(FlowPadApplication.class.getResource("Submit-Page.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), FlowPadApplication.WIDTH, FlowPadApplication.HEIGHT);
        stage.setTitle("Home Page");
        stage.setScene(scene);
    }


    @FXML
    protected void onRegisterAccountButtonClick() throws IOException {
        Stage stage = (Stage) RegisterAccountButton.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(FlowPadApplication.class.getResource("Register-Page.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), FlowPadApplication.WIDTH, FlowPadApplication.HEIGHT);
        stage.setTitle("Register Page");
        stage.setScene(scene);
    }

}