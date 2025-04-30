package com.staticconstants.flowpad.frontend;

import com.staticconstants.flowpad.FlowPadApplication;
import com.staticconstants.flowpad.backend.db.users.User;
import com.staticconstants.flowpad.backend.db.users.UserDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.scene.image.Image;

import java.io.IOException;

public class RegisterController {

    @FXML
    private ImageView Logo;
    @FXML
    private Button RegisterAccountButton;
    @FXML
    private Label welcomeText;

    @FXML
    private Button SubmitButton;

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("");
    }

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
            String firstName = "";
            String lastName = "";

            User user = new User(firstName, lastName, username, password);
            UserDAO userDAO = new UserDAO();

            //create code to check db if login is correct
            //check if the username and password is correct
            if (userDAO.checklogin(username, password)){
                // Get user details

                // Success
                Stage stage = (Stage) SubmitButton.getScene().getWindow();
                FXMLLoader fxmlLoader = new FXMLLoader(FlowPadApplication.class.getResource("maineditor-view.fxml"));
                Scene scene = new Scene(fxmlLoader.load());
                stage.setTitle("Home Page");
                stage.setScene(scene);
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
    }

}