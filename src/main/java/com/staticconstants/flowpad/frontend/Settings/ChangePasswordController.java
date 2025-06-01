package com.staticconstants.flowpad.frontend.Settings;

import com.staticconstants.flowpad.FlowPadApplication;
import com.staticconstants.flowpad.backend.LoggedInUser;
import com.staticconstants.flowpad.backend.db.users.LoginResult;
import com.staticconstants.flowpad.backend.db.users.User;
import com.staticconstants.flowpad.backend.db.users.UserDAO;
import com.staticconstants.flowpad.backend.security.PasswordHasher;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class ChangePasswordController {
    @FXML TextField usernameField;
    @FXML private PasswordField oldPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button backButton;

    private final UserDAO userDAO = new UserDAO();
    private String currentUsername;

    public void setCurrentUsername(String username) {
        this.currentUsername = username;
    }

    @FXML
    private void handleChangePassword() throws Exception {
        String username = usernameField.getText();
        String oldPassword = oldPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (username.isEmpty() || oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showAlert("Please fill in all fields.", Alert.AlertType.ERROR);
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showAlert("New passwords do not match.", Alert.AlertType.ERROR);
            return;
        }

        userDAO.login(username, oldPassword.toCharArray()).thenAccept(loginResult -> {
            if (loginResult == LoginResult.PASSWORD_INCORRECT) {
                showAlertLater("Old password is incorrect.", Alert.AlertType.ERROR);
                return;
            }

            if (loginResult != LoginResult.SUCCESS) {
                showAlertLater("Error checking password", Alert.AlertType.ERROR);
                return;
            }


            LoggedInUser.user = User.fromExisting(
                    LoggedInUser.user.getId(),
                    LoggedInUser.user.getFirstName(),
                    LoggedInUser.user.getLastName(),
                    LoggedInUser.user.getUsername(),
                    PasswordHasher.hashPassword(newPassword.toCharArray())
            );

            userDAO.update(LoggedInUser.user).whenComplete((v, ex) -> {
                if (ex == null) {
                    showAlertLater("Password changed successfully!", Alert.AlertType.INFORMATION);
                    clearFields();
                } else {
                    showAlertLater("Failed to update password.", Alert.AlertType.ERROR);
                }
            });

        }).exceptionally(ex -> {
            ex.printStackTrace();
            showAlertLater("An error occurred.", Alert.AlertType.ERROR);
            return null;
        });
    }


    private void showAlert(String msg, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void showAlertLater(String msg, Alert.AlertType type) {
        Platform.runLater(() -> showAlert(msg, type));
    }

    private void clearFields() {
        Platform.runLater(() -> {
            oldPasswordField.clear();
            newPasswordField.clear();
            confirmPasswordField.clear();
        });
    }

    @FXML
    public void onBackButton() throws IOException {
        Stage stage = (Stage) backButton.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(FlowPadApplication.class.getResource("settings-view.fxml"));
        Scene scene = new Scene(loader.load(), stage.getWidth(), stage.getHeight());

        String css = Objects.requireNonNull(
                getClass().getResource("/com/staticconstants/flowpad/flowpad-stylesheet.css")
        ).toExternalForm();
        scene.getStylesheets().add(css);

        stage.setScene(scene);
    }
}
