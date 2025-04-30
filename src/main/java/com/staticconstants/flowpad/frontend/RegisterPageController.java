package com.staticconstants.flowpad.frontend;

import com.staticconstants.flowpad.FlowPadApplication;
import com.staticconstants.flowpad.backend.db.users.User;
import com.staticconstants.flowpad.backend.db.users.UserDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
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


    @FXML
    protected void onBackButtonClick() throws IOException {
        Stage stage = (Stage) btnBack.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(FlowPadApplication.class.getResource("flowpad-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    protected void onRegisterButtonClick() throws IOException {
        try {
            String username = txtUsername.getText();
            char[] password = txtPassword.getText().toCharArray();
            String firstName = txtFirstName.getText();
            String lastName = txtLastName.getText();

            UserDAO userDAO = new UserDAO();
            User user = new User(firstName, lastName, username, password);
            userDAO.insert(user);


            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Registration successful!");
            alert.show();

            Stage stage = (Stage) btnRegister.getScene().getWindow();
            FXMLLoader fxmlLoader = new FXMLLoader(FlowPadApplication.class.getResource("Submit-Page.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Registration failed: " + e.getMessage());
            alert.show();
        }


    }

}
