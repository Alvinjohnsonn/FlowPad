package com.staticconstants.flowpad;

import com.staticconstants.flowpad.backend.db.notes.NoteDAO;
import com.staticconstants.flowpad.backend.db.users.UserDAO;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class FlowPadApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {


//        FXMLLoader fxmlLoader = new FXMLLoader(FlowPadApplication.class.getResource("login-view.fxml"));
        FXMLLoader fxmlLoader = new FXMLLoader(FlowPadApplication.class.getResource("maineditor-view.fxml"));

        Scene scene = new Scene(fxmlLoader.load());



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
}
