package com.staticconstants.flowpad.frontend;

import com.staticconstants.flowpad.FlowPadApplication;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
/**
 * Controller class for the FlowPad landing screen.
 * Handles navigation to various sections of the application such as main editor,
 * settings, registration, and handles application exit.
 */
public class FlowPadController {
    
    @FXML
    private Label welcomeText;

    /**
     * Handles the event to open the main editor page.
     * Loads the maineditor-view.fxml and applies the editor stylesheet.
     *
     * @param event the ActionEvent triggered by the UI
     * @throws IOException if the FXML file cannot be loaded
     */
    @FXML
    protected void OpenMainPageButton(ActionEvent event) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(FlowPadApplication.class.getResource("maineditor-view.fxml"));
        String stylesheet =  FlowPadApplication.class.getResource("css/editor-style.css").toExternalForm();

        Scene scene = new Scene(fxmlLoader.load());
        scene.getStylesheets().add(stylesheet);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setTitle("Note Editor");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Handles the event to open the settings page.
     * Loads the settings-view.fxml file and sets the stage title to "Settings".
     *
     * @param event the ActionEvent triggered by the UI
     * @throws IOException if the FXML file cannot be loaded
     */
    @FXML
    protected void OpenSettingsButton(ActionEvent event) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(FlowPadApplication.class.getResource("settings-view.fxml"));

        Scene scene = new Scene(fxmlLoader.load());

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setTitle("Settings");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Handles the event to open the registration page.
     * Loads the register-view.fxml file and sets the stage title to "Register".
     *
     * @param event the ActionEvent triggered by the UI
     * @throws IOException if the FXML file cannot be loaded
     */
    @FXML
    protected void OpenRegisterButton(ActionEvent event) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(FlowPadApplication.class.getResource("register-view.fxml"));

        Scene scene = new Scene(fxmlLoader.load());

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setTitle("Register");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Exits the application.
     *
     * @throws IOException not used in this context, but declared for consistency
     */
    @FXML
    protected void Exit() throws IOException {
        Platform.exit();
    }
}