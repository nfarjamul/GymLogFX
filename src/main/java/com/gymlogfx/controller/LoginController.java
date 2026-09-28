package com.gymlogfx.controller;

import com.gymlogfx.MainApp;
import com.gymlogfx.database.DatabaseManager;
import com.gymlogfx.model.User;
import com.gymlogfx.util.SessionManager;
import com.gymlogfx.util.ThemeManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private ToggleButton themeToggle;
    @FXML private Button loginButton;

    @FXML public void initialize() {
        themeToggle.setSelected(SessionManager.getInstance().isDarkMode());
    }

    @FXML public void handleLogin(ActionEvent event) throws IOException {
        String username = usernameField.getText();
        String password = passwordField.getText();
        User user = DatabaseManager.getInstance().authenticateUser(username, password);
        if (user != null) {
            SessionManager.getInstance().setCurrentUser(user);
            MainApp.showMainDashboard();
        } else {
            errorLabel.setText("Invalid username or password");
        }
    }

    @FXML public void showRegister(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/RegisterView.fxml"));
        Scene scene = new Scene(loader.load(), 900, 650);
        ThemeManager.getInstance().registerScene(scene);
        MainApp.primaryStage.setScene(scene);
    }

    @FXML public void toggleTheme(ActionEvent event) {
        ThemeManager.getInstance().toggleTheme();
    }
}
