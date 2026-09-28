package com.gymlogfx;

import com.gymlogfx.database.DatabaseManager;
import com.gymlogfx.service.ApiService;
import com.gymlogfx.util.SessionManager;
import com.gymlogfx.util.ThemeManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * MainApp - JavaFX Application entry point.
 *
 * Application launch sequence:
 * 1. JavaFX Application Thread is started by the JVM
 * 2. Database is initialized (background init on first access)
 * 3. Login screen is displayed
 * 4. After login → Main dashboard loads
 *
 * THREADING MODEL:
 * - JavaFX Application Thread: All UI operations
 * - HTTP Thread Pool (4 threads): API calls in ApiService
 * - Database Thread: Heavy DB queries run on Task threads
 * - Background Service Thread: Periodic quote refreshes
 */
public class MainApp extends Application {

    public static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;

        // Initialize database on startup (Singleton pattern)
        DatabaseManager.getInstance();

        // Load the login screen
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/fxml/LoginView.fxml")
        );
        Scene loginScene = new Scene(loader.load(), 900, 650);

        // Apply initial theme
        ThemeManager.getInstance().registerScene(loginScene);

        stage.setTitle("GymLogFX — Workout Logger");
        stage.setScene(loginScene);
        stage.setMinWidth(800);
        stage.setMinHeight(600);
        stage.show();

        // Handle app shutdown
        stage.setOnCloseRequest(e -> {
            DatabaseManager.getInstance().close();
            ApiService.shutdownThreadPool();
        });
    }

    /**
     * Navigate to the main dashboard after login
     */
    public static void showMainDashboard() throws IOException {
        FXMLLoader loader = new FXMLLoader(
            MainApp.class.getResource("/fxml/MainView.fxml")
        );
        Scene mainScene = new Scene(loader.load());

        // Make scene responsive - bind to screen bounds
        mainScene.getRoot().prefWidth(primaryStage.getWidth());
        mainScene.getRoot().prefHeight(primaryStage.getHeight());

        ThemeManager.getInstance().registerScene(mainScene);
        ThemeManager.getInstance().applyThemeToScene(mainScene);

        primaryStage.setScene(mainScene);
        primaryStage.setMaximized(true);
    }

    /**
     * Navigate back to login screen (logout)
     */
    public static void showLoginScreen() throws IOException {
        SessionManager.getInstance().logout();
        FXMLLoader loader = new FXMLLoader(
            MainApp.class.getResource("/fxml/LoginView.fxml")
        );
        Scene loginScene = new Scene(loader.load(), 900, 650);
        ThemeManager.getInstance().registerScene(loginScene);
        primaryStage.setScene(loginScene);
        primaryStage.setMaximized(false);
        primaryStage.setWidth(900);
        primaryStage.setHeight(650);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
