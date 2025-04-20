package com.staticconstants.flowpad;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Objects;

public class FlowPadApplication extends Application {
    public static final int WIDTH = 640;
    public static final int HEIGHT = 360;
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(FlowPadApplication.class.getResource("flowpad-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(),WIDTH, HEIGHT);


        String flowpadStylesheet = Objects.requireNonNull(FlowPadApplication.class
                        .getResource("flowpad-stylesheet.css"))
                .toExternalForm();
        scene.getStylesheets().add(flowpadStylesheet);



        stage.setTitle("FlowPad");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
