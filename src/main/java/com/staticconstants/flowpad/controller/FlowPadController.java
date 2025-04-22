package com.staticconstants.flowpad.controller;

import com.staticconstants.flowpad.FlowPadApplication;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class FlowPadController {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick(ActionEvent event) throws IOException {

//        TODO: Delete the code below, previously used to launch and view the main editor page

        FXMLLoader fxmlLoader = new FXMLLoader(FlowPadApplication.class.getResource("maineditor-view.fxml"));
//        String stylesheet =  FlowPadApplication.class.getResource("editor-style.css").toExternalForm();

        Scene scene = new Scene(fxmlLoader.load());
//        scene.getStylesheets().add(stylesheet);

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setTitle("Note Editor");
        stage.setScene(scene);
        stage.show();
    }
}