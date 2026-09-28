package com.gymlogfx.controller;

import com.gymlogfx.MainApp;
import com.gymlogfx.database.DatabaseManager;
import com.gymlogfx.util.ThemeManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;

import java.io.IOException;

public class RegisterController {
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private RadioButton beginnerRB;
    @FXML private RadioButton intermediateRB;
    @FXML private RadioButton expertRB;
    @FXML private Label errorLabel;
    @FXML private Label successLabel;

    @FXML public void handleRegister(ActionEvent event) {
        String username = usernameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String confirm = confirmPasswordField.getText();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please fill all fields.");
            return;
        }
        if (!password.equals(confirm)) {
            errorLabel.setText("Passwords do not match.");
            return;
        }
        String level = "Beginner";
        if (intermediateRB != null && intermediateRB.isSelected()) level = "Intermediate";
        else if (expertRB != null && expertRB.isSelected()) level = "Expert";

        boolean success = DatabaseManager.getInstance().createUser(username, email, password, level);
        if (success) {
            successLabel.setText("Registration successful!");
            errorLabel.setText("");
        } else {
            errorLabel.setText("Registration failed (username/email may exist).");
            successLabel.setText("");
        }
    }

    @FXML public void goToLogin(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
        Scene scene = new Scene(loader.load(), 900, 650);
        ThemeManager.getInstance().registerScene(scene);
        MainApp.primaryStage.setScene(scene);
    }
}
