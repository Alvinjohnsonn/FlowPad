package com.staticconstants.flowpad.frontend;

import com.staticconstants.flowpad.FlowPadApplication;
import com.staticconstants.flowpad.backend.LoggedInUser;
import com.staticconstants.flowpad.backend.db.users.User;
import com.staticconstants.flowpad.backend.db.users.UserDAO;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
/**
 * Controller class for the registration page in the FlowPad application.
 * Handles user input for account creation, validation, and scene transitions.
 */
public class RegisterPageController {
    @FXML
    private Button btnBack;

    @FXML
    private Button btnRegister;

    @FXML
    private TextField txtFirstName;

    @FXML
    private TextField txtLastName;

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;
    /**
     * Initializes the registration page.
     * Ensures the user table is created in the database when the controller is loaded.
     */
    @FXML
    public void initialize(){
        UserDAO userDAO = new UserDAO();

        userDAO.createTable().thenRun(() -> {
            System.out.println("Users table created or already exists.");
        }).exceptionally(ex -> {
            System.err.println("Failed to create Users table:");
            ex.printStackTrace();
            return null;
        });
    }
    /**
     * Handles the back button click.
     * Navigates the user back to the login screen.
     *
     * @throws IOException if the login-view FXML cannot be loaded
     */
    @FXML
    protected void onBackButtonClick() throws IOException {
        Stage stage = (Stage) btnBack.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(FlowPadApplication.class.getResource("login-view.fxml"));

        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("FlowPad");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }
    /**
     * Handles the registration process when the register button is clicked.
     * Captures form data, inserts a new user into the database, and navigates to the main screen upon success.
     *
     * @throws IOException if the main view FXML cannot be loaded
     */
    @FXML
    protected void onRegisterButtonClick() throws IOException {
        try {
            String username = txtUsername.getText();
            char[] password = txtPassword.getText().toCharArray();
            String firstName = txtFirstName.getText();
            String lastName = txtLastName.getText();

            UserDAO userDAO = new UserDAO();
            LoggedInUser.user = new User(firstName, lastName, username, password);
            LoggedInUser.notes = new HashMap<>();

            userDAO.insert(LoggedInUser.user).thenAccept(success -> {
                Platform.runLater(() -> {  // Ensure this runs on the JavaFX Application Thread
                    if (success) {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setContentText("Registration successful!");
                        alert.show();

                        Stage stage = (Stage) btnRegister.getScene().getWindow();
                        FXMLLoader fxmlLoader = new FXMLLoader(FlowPadApplication.class.getResource("flowpad-view.fxml"));
                        String stylesheet =  FlowPadApplication.class.getResource("flowpad-stylesheet.css").toExternalForm();

                        Scene scene = null;
                        try {
                            scene = new Scene(fxmlLoader.load());
                            scene.getStylesheets().add(stylesheet);
                            stage.setScene(scene);
                            stage.setMaximized(true);
                            stage.show();

                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setContentText("Registration failed.");
                        alert.show();
                    }
                });


            }).exceptionally(ex -> {
                ex.printStackTrace(); // Log or show error
                Platform.runLater(() -> {  // Ensure this runs on the JavaFX Application Thread
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText("An error occurred: " + ex.getMessage());
                    alert.show();
                });


                return null;
            });



        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Registration failed: " + e.getMessage());
            alert.show();
        }


    }

}
