package com.staticconstants.flowpad;

import com.staticconstants.flowpad.backend.db.notes.NoteDAO;
import com.staticconstants.flowpad.backend.db.users.UserDAO;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FlowPadApplication extends Application {
    public static final ExecutorService aiExecutor = Executors.newFixedThreadPool(5);

    @Override
    public void start(Stage stage) throws IOException {


        FXMLLoader fxmlLoader = new FXMLLoader(FlowPadApplication.class.getResource("login-view.fxml"));

        Scene scene = new Scene(fxmlLoader.load());
        scene.getStylesheets().add(getClass().getResource("flowpad-stylesheet.css").toExternalForm());


        stage.setTitle("FlowPad");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    public static void main(String[] args) {
        UserDAO users = new UserDAO();
        users.createTable();

        NoteDAO notes = new NoteDAO();
        notes.createTable();


        launch();
    }

    @Override
    public void stop() {
        aiExecutor.shutdownNow();
    }

}
