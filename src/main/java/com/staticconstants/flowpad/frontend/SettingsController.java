package com.staticconstants.flowpad.frontend;

import com.staticconstants.flowpad.FlowPadApplication;
import com.staticconstants.flowpad.backend.db.DbHandler;
import com.staticconstants.flowpad.backend.security.HashedPassword;
import com.staticconstants.flowpad.backend.security.PasswordHasher;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.CompletionStage;

/**
 * Controller class for the Settings view in the FlowPad application.
 * Handles user interactions related to settings, privacy, highlights, navigation, and logout.
 */
public class SettingsController {
    @FXML
    public Button Privacy;

    @FXML
    public Button backButton;

    @FXML
    public Button Highlight;

    @FXML
    public Button btnGoBack;

    @FXML
    private Label welcomeText;

    @FXML
    private ImageView profileImage;

    @FXML
    public Button logoutbtn;

    /**
     * Initializes the controller by setting the profile image with a circular clip.
     * Called automatically after the FXML is loaded.
     */
    public void initialize() {
        if (profileImage != null) {
            Image image = new Image(getClass().getResource("/com/staticconstants/flowpad/icons/profiles.jpg").toExternalForm());
            profileImage.setImage(image);
            Circle clip = new Circle(50, 50, 50);
            profileImage.setClip(clip);
        }
    }

    /**
     * Handles the Privacy button click event and loads the Privacy.fxml scene.
     *
     * @throws IOException if the FXML file cannot be loaded
     */
    @FXML
    protected void onPrivacyButtonClick() throws IOException {
        Stage stage = (Stage) Privacy.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(FlowPadApplication.class.getResource("Privacy.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), stage.getWidth(), stage.getHeight());
        stage.setScene(scene);
    }

    /**
     * Handles the Highlight button click event and loads the HighlightOptions.fxml scene.
     *
     * @throws IOException if the FXML file cannot be loaded
     */
    @FXML
    protected void onHighlight() throws IOException {
        Stage stage = (Stage) Highlight.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(FlowPadApplication.class.getResource("HighlightOptions.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), stage.getWidth(), stage.getHeight());
        stage.setScene(scene);
    }

    /**
     * Handles the Back button click event and returns to the settings-view scene.
     *
     * @throws IOException if the FXML file cannot be loaded
     */
    @FXML
    private void onBackButton() throws IOException {
        Stage stage = (Stage) backButton.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/staticconstants/flowpad/settings-view.fxml"));
        Scene scene = new Scene(loader.load(), stage.getWidth(), stage.getHeight());
        stage.setScene(scene);
    }

    /**
     * Handles the Go Back button click event and navigates back to the main editor view.
     *
     * @throws IOException if the FXML file cannot be loaded
     */
    @FXML
    private void goBack() throws IOException {
        Stage stage = (Stage) btnGoBack.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(FlowPadApplication.class.getResource("maineditor-view.fxml"));
        String stylesheet = FlowPadApplication.class.getResource("css/editor-style.css").toExternalForm();

        Scene scene = new Scene(fxmlLoader.load());
        scene.getStylesheets().add(stylesheet);
        stage.setTitle("Home Page");

        stage.setScene(scene);
        stage.setMaximized(true);
    }

    /**
     * Handles the Logout button click event and navigates to the login view.
     *
     * @throws IOException if the FXML file cannot be loaded
     */
    @FXML
    protected void onLogOutButtonClick() throws IOException {
        Stage stage = (Stage) logoutbtn.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(FlowPadApplication.class.getResource("login-view.fxml"));
        String stylesheet = FlowPadApplication.class.getResource("flowpad-stylesheet.css").toExternalForm();

        Scene scene = new Scene(fxmlLoader.load());
        scene.getStylesheets().add(stylesheet);
        stage.setTitle("Settings Page");

        stage.setScene(scene);
        stage.setMaximized(true);
    }

    /**
     * Updates the password of the specified user in the database.
     *
     * @param currentUsername the username of the user whose password is to be updated
     * @param charArray       the new password as a character array
     * @return a CompletionStage representing the success or failure of the update operation
     */
    public CompletionStage<Boolean> updatePassword(String currentUsername, char[] charArray) {
        return DbHandler.getInstance().dbOperation(conn -> {
            HashedPassword hashed = PasswordHasher.hashPassword(new String(charArray).toCharArray());
            Arrays.fill(charArray, ' '); // clear memory

            String sql = "UPDATE Users SET hashedPassword = ?, encodedSalt = ? WHERE username = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, hashed.hashBase64);
                stmt.setString(2, hashed.saltBase64);
                stmt.setString(3, currentUsername);
                int affected = stmt.executeUpdate();
                return affected > 0;
            }
        });
    }
}
