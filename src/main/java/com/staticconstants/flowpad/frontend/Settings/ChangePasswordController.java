package com.staticconstants.flowpad.frontend.Settings;

import com.staticconstants.flowpad.FlowPadApplication;
import com.staticconstants.flowpad.backend.db.users.UserDAO;
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
    private void handleChangePassword() {
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

        userDAO.checkLoginAsync(username, oldPassword.toCharArray()).thenAccept(valid -> {

            if (!valid) {
                showAlertLater("Old password is incorrect.", Alert.AlertType.ERROR);
            } else {
                userDAO.updatePassword(username, newPassword.toCharArray()).thenAccept(success -> {
                    if (success) {
                        showAlertLater("Password changed successfully!", Alert.AlertType.INFORMATION);
                        clearFields();
                    } else {
                        showAlertLater("Failed to update password.", Alert.AlertType.ERROR);
                    }
                });
            }
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
