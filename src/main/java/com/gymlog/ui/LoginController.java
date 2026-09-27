package com.gymlog.ui;

import com.gymlog.data.UserStore;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.function.Consumer;

/**
 * Controller for {@code login.fxml}. Handles both "log in" and
 * "create account" in a single small screen, backed by {@link UserStore}.
 * On success, hands the logged-in username off to whoever wired this
 * controller via {@link #setOnLoginSuccess(Consumer)}.
 */
public class LoginController {

    @FXML private VBox card;
    @FXML private HBox themeToggleBox;
    @FXML private Label subtitleLabel;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label errorLabel;
    @FXML private Button actionButton;
    @FXML private Hyperlink modeToggleLink;

    private UserStore userStore;
    private Consumer<String> onLoginSuccess = username -> { };
    private boolean createMode;

    @FXML
    private void initialize() {
        actionButton.setOnAction(this::handleAction);
        modeToggleLink.setOnAction(e -> toggleMode());
    }

    /** Wires the account store and picks the initial mode (create vs. log in) based on whether an account exists. */
    public void setUserStore(UserStore userStore) {
        this.userStore = userStore;
        createMode = !userStore.hasAccount();
        applyMode();
        if (!createMode) {
            usernameField.setText(userStore.getUsername());
        }
    }

    public void setOnLoginSuccess(Consumer<String> callback) {
        this.onLoginSuccess = callback;
    }

    /** Injects the shared dark-mode toggle button (built by ThemeManager) into this screen. */
    public void installThemeToggle(ThemeManager themeManager) {
        themeToggleBox.getChildren().setAll(themeManager.buildToggleButton());
    }

    private void toggleMode() {
        createMode = !createMode;
        errorLabel.setText("");
        applyMode();
    }

    private void applyMode() {
        confirmPasswordField.setVisible(createMode);
        confirmPasswordField.setManaged(createMode);

        if (createMode) {
            subtitleLabel.setText("Create your local account");
            actionButton.setText("Create Account");
            modeToggleLink.setText("Already have an account? Log in");
        } else {
            subtitleLabel.setText("Log in to your workout log");
            actionButton.setText("Log In");
            modeToggleLink.setText("New here? Create an account");
        }
    }

    private void handleAction(ActionEvent event) {
        errorLabel.setText("");
        String username = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Enter a username and password.");
            return;
        }

        if (createMode) {
            if (userStore.hasAccount()) {
                showError("An account already exists on this computer. Log in instead.");
                return;
            }
            String confirm = confirmPasswordField.getText() == null ? "" : confirmPasswordField.getText();
            if (!password.equals(confirm)) {
                showError("Passwords don't match.");
                return;
            }
            userStore.createAccount(username, password);
            onLoginSuccess.accept(username);
        } else {
            if (userStore.verify(username, password)) {
                onLoginSuccess.accept(username);
            } else {
                showError("Incorrect username or password.");
            }
        }
    }

    /** Shows an error message and gives the card a short "shake" for feedback. */
    private void showError(String message) {
        errorLabel.setText(message);
        TranslateTransition shake = new TranslateTransition(Duration.millis(60), card);
        shake.setFromX(0);
        shake.setByX(10);
        shake.setCycleCount(4);
        shake.setAutoReverse(true);
        shake.setOnFinished(e -> card.setTranslateX(0));
        shake.play();
    }
}
