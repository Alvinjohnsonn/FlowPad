package com.staticconstants.flowpad.frontend;

import com.staticconstants.flowpad.FlowPadApplication;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class FlowPadController {
    @FXML
    private Label welcomeText;

    @FXML
    protected void OpenMainPageButton(ActionEvent event) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(FlowPadApplication.class.getResource("maineditor-view.fxml"));

        Scene scene = new Scene(fxmlLoader.load());

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setTitle("Note Editor");
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    protected void OpenSettingsButton(ActionEvent event) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(FlowPadApplication.class.getResource("settings-view.fxml"));

        Scene scene = new Scene(fxmlLoader.load());

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setTitle("Settings");
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    protected void OpenRegisterButton(ActionEvent event) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(FlowPadApplication.class.getResource("register-view.fxml"));

        Scene scene = new Scene(fxmlLoader.load());

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setTitle("Register");
        stage.setScene(scene);
        stage.show();
    }
}