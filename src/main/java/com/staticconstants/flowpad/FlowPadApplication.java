package com.staticconstants.flowpad;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class FlowPadApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Load the FXML layout
        FXMLLoader fxmlLoader = new FXMLLoader(FlowPadApplication.class.getResource("flowpad-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        // ✅ Load and apply the CSS
        String flowpadStylesheet = FlowPadApplication.class
                .getResource("flowpad-stylesheet.css")
                .toExternalForm();
        scene.getStylesheets().add(flowpadStylesheet);

        // ✅ Set stage properties
        stage.setTitle("FlowPad");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
