package com.staticconstants.flowpad.frontend;

import com.staticconstants.flowpad.FlowPadApplication;
import com.staticconstants.flowpad.backend.LoggedInUser;
import com.staticconstants.flowpad.backend.db.notes.Note;
import com.staticconstants.flowpad.backend.db.notes.NoteDAO;
import com.staticconstants.flowpad.backend.db.users.LoginResult;
import com.staticconstants.flowpad.backend.db.users.UserDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.scene.image.Image;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

public class LoginController {

    @FXML
    private ImageView Logo;
    @FXML
    private Button RegisterAccountButton;

    @FXML
    private Button btnSubmit;

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;


    @FXML
    public void initialize() {

        Image image = new Image(getClass().getResource("/com/staticconstants/flowpad/icons/Logo.jpg").toExternalForm());

        Logo.setImage(image);

        Circle clip = new Circle(150, 150, 150);
        Logo.setClip(clip);
    }

    // When the Submit Button is clicked, could lead to the Home Page.
    @FXML
    protected void onSubmitButtonClick() throws IOException {
        try {
            String username = txtUsername.getText();
            char[] password = txtPassword.getText().toCharArray();

            UserDAO userDAO = new UserDAO();

            //create code to check db if login is correct
            //check if the username and password is correct
            if (userDAO.login(username, password).get() == LoginResult.SUCCESS){
                // Get user details
                new NoteDAO().getAll().whenComplete(((notes, ex) -> {

                    LoggedInUser.notes = new HashMap<>();
                    if (ex != null) {
                        System.err.println("Could not get notes");
                        return;
                    }

                    for(Note note : notes) {
                        LoggedInUser.notes.put(note.getFilename(), note);
                    }

                }));
                // Success
                Stage stage = (Stage) btnSubmit.getScene().getWindow();

                FXMLLoader fxmlLoader = new FXMLLoader(FlowPadApplication.class.getResource("maineditor-view.fxml"));
                String stylesheet =  FlowPadApplication.class.getResource("css/editor-style.css").toExternalForm();

                Scene scene = new Scene(fxmlLoader.load());
                scene.getStylesheets().add(stylesheet);
                stage.setTitle("Home Page");

                stage.setScene(scene);
                Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
                stage.setX(screenBounds.getMinX());
                stage.setY(screenBounds.getMinY());
                stage.setWidth(screenBounds.getWidth());
                stage.setHeight(screenBounds.getHeight());
                stage.show();
            }else{
                Alert loginalert = new Alert(Alert.AlertType.INFORMATION);
                loginalert.setContentText("Login failed!");
                loginalert.show();
            }


        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Login failed: " + e.getMessage());
            alert.show();
        }
    }


    @FXML
    protected void onRegisterAccountButtonClick() throws IOException {
        Stage stage = (Stage) RegisterAccountButton.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(FlowPadApplication.class.getResource("Register-Page.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Register Page");
        stage.setScene(scene);
        stage.show();
    }

}