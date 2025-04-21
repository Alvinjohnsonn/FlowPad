package com.staticconstants.flowpad;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class FlowPadApplication extends Application {

    public static final int WIDTH = 1450;
    public static final int HEIGHT = 750;


    @Override
    public void start(Stage stage) throws IOException {


        FXMLLoader fxmlLoader = new FXMLLoader(FlowPadApplication.class.getResource("flowpad-view.fxml"));
        String flowpadStylesheet =  FlowPadApplication.class.getResource("flowpad-stylesheet.css").toExternalForm();

        Scene scene = new Scene(fxmlLoader.load(), WIDTH, HEIGHT);
        scene.getStylesheets().add(flowpadStylesheet);
        stage.setTitle("FlowPad!");
        stage.setScene(scene);
        stage.show();
        //test

    }

    public static void main(String[] args) {
        launch();
    }
}