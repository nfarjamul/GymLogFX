package com.gymlogfx.controller;

import com.gymlogfx.MainApp;
import com.gymlogfx.util.SessionManager;
import com.gymlogfx.util.ThemeManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Pane;

import java.io.IOException;

public class MainController {
    @FXML private Pane contentArea;
    @FXML private Label userGreeting;
    @FXML private ToggleButton themeToggle;

    @FXML public void initialize() {
        if (SessionManager.getInstance().getCurrentUser() != null) {
            userGreeting.setText("Hello, " + SessionManager.getInstance().getCurrentUser().getUsername());
        }
        themeToggle.setSelected(SessionManager.getInstance().isDarkMode());
        showDashboard(null);
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node view = loader.load();
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML public void showDashboard(ActionEvent event) { loadView("/fxml/DashboardView.fxml"); }
    @FXML public void showWorkoutLog(ActionEvent event) { loadView("/fxml/WorkoutLogView.fxml"); }
    @FXML public void showHistory(ActionEvent event) { loadView("/fxml/HistoryView.fxml"); }
    @FXML public void showRoutines(ActionEvent event) { loadView("/fxml/RoutinesView.fxml"); }
    @FXML public void showCharts(ActionEvent event) { loadView("/fxml/ChartsView.fxml"); }
    @FXML public void showEvaluatorDemo(ActionEvent event) { loadView("/fxml/EvaluatorDemoView.fxml"); }

    @FXML public void handleLogout(ActionEvent event) throws IOException {
        MainApp.showLoginScreen();
    }

    @FXML public void toggleTheme(ActionEvent event) {
        ThemeManager.getInstance().toggleTheme();
    }
}
