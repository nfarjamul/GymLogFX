package com.gymlog;

import com.gymlog.concurrency.AutoSaveWorker;
import com.gymlog.concurrency.StatsCalculator;
import com.gymlog.data.UserStore;
import com.gymlog.data.WorkoutRepository;
import com.gymlog.ui.HistoryPane;
import com.gymlog.ui.LoginController;
import com.gymlog.ui.LogWorkoutPane;
import com.gymlog.ui.RoutineBuilderPane;
import com.gymlog.ui.ThemeManager;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

/**
 * Application entry point.
 *
 * <p>The app now opens on an FXML-defined login screen
 * ({@code login.fxml} / {@link LoginController}) instead of going
 * straight to the workout logger. Only after a successful login (or
 * account creation) does this class build the repository-backed main
 * UI and start the background threads described below - see
 * {@link #showMainApp(String)}.
 *
 * <p>Two concurrency mechanisms are owned here, same as before:
 * <ol>
 *   <li>A raw {@link Thread} running an {@link AutoSaveWorker}
 *       ({@link Runnable}) that periodically persists the shared
 *       {@link WorkoutRepository}. Its lifecycle - create, start,
 *       cooperative stop via a flag + interrupt(), and join() - is
 *       managed in {@link #stopBackgroundWork()}, which runs both on
 *       logout and on window close.</li>
 *   <li>A {@link StatsCalculator}, wrapping a
 *       {@code java.util.concurrent.ExecutorService} used by the
 *       History tab to compute chart data off the JavaFX Application
 *       Thread.</li>
 * </ol>
 *
 * <p>Dark mode is a single shared {@link ThemeManager}: both the login
 * screen and the main screen build their toggle button from it, and a
 * listener on its property re-applies the "dark" style class to
 * whichever Parent is currently the Scene's root.
 */
public class Main extends Application {

    private final UserStore userStore = new UserStore();
    private final ThemeManager themeManager = new ThemeManager();

    private Scene scene;

    private WorkoutRepository repository;
    private StatsCalculator statsCalculator;
    private Thread autoSaveThread;
    private AutoSaveWorker autoSaveWorker;

    @Override
    public void start(Stage primaryStage) throws IOException {
        Parent loginRoot = loadLoginScreen();

        scene = new Scene(loginRoot, 920, 640);
        scene.getStylesheets().add(getClass().getResource("/com/gymlog/styles.css").toExternalForm());
        themeManager.applyTo(loginRoot);
        themeManager.darkModeProperty().addListener((obs, wasDark, isDark) -> themeManager.applyTo(scene.getRoot()));

        primaryStage.setTitle("GymLogFX \u2014 Workout Logger");
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(event -> shutdown());
        primaryStage.show();
    }

    /** Loads login.fxml and wires its controller to the shared account store and theme manager. */
    private Parent loadLoginScreen() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/gymlog/ui/login.fxml"));
        Parent root = loader.load();
        LoginController controller = loader.getController();
        controller.setUserStore(userStore);
        controller.installThemeToggle(themeManager);
        controller.setOnLoginSuccess(this::showMainApp);
        return root;
    }

    /**
     * Builds the repository-backed main UI, starts the background
     * threads, and swaps it into the scene with a short fade-in. Called
     * once per login (including re-logins after a logout).
     */
    private void showMainApp(String username) {
        repository = new WorkoutRepository();
        repository.load();

        statsCalculator = new StatsCalculator();

        Label statusLabel = new Label("Ready");
        statusLabel.getStyleClass().add("status-label");

        autoSaveWorker = new AutoSaveWorker(repository, message ->
                Platform.runLater(() -> statusLabel.setText(message)));
        autoSaveThread = new Thread(autoSaveWorker, "AutoSave-Thread");
        autoSaveThread.setDaemon(true);
        autoSaveThread.start();

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        LogWorkoutPane logPane = new LogWorkoutPane(repository);
        HistoryPane historyPane = new HistoryPane(repository, statsCalculator);
        RoutineBuilderPane routinePane = new RoutineBuilderPane(repository);

        logPane.setOnEntryAdded(() -> {
            historyPane.refreshExerciseList();
            routinePane.refresh();
        });

        Tab logTab = new Tab("\uD83C\uDFCB Log Workout", logPane.getView());
        Tab historyTab = new Tab("\uD83D\uDCC8 History & Charts", historyPane.getView());
        Tab routineTab = new Tab("\uD83D\uDCCB Routines", routinePane.getView());
        tabPane.getTabs().addAll(logTab, historyTab, routineTab);

        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab == historyTab) {
                historyPane.refreshExerciseList();
            } else if (newTab == routineTab) {
                routinePane.refresh();
            }
        });

        Label welcomeLabel = new Label("Welcome, " + username);
        welcomeLabel.getStyleClass().add("welcome-label");

        Button logoutButton = new Button("Log Out");
        logoutButton.getStyleClass().add("logout-button");
        logoutButton.setOnAction(e -> logout());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox statusBar = new HBox(10, statusLabel, spacer, welcomeLabel, themeManager.buildToggleButton(), logoutButton);
        statusBar.getStyleClass().add("status-bar");
        statusBar.setPadding(new Insets(6, 12, 6, 12));
        statusBar.setAlignment(Pos.CENTER_LEFT);

        BorderPane root = new BorderPane();
        root.setCenter(tabPane);
        root.setBottom(statusBar);
        root.setOpacity(0);

        themeManager.applyTo(root);
        scene.setRoot(root);

        FadeTransition fade = new FadeTransition(Duration.millis(350), root);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    /** Stops the current session's background work and returns to a fresh login screen. */
    private void logout() {
        stopBackgroundWork();
        try {
            Parent loginRoot = loadLoginScreen();
            themeManager.applyTo(loginRoot);
            scene.setRoot(loginRoot);
        } catch (IOException e) {
            System.err.println("[Main] Failed to reload login screen: " + e.getMessage());
        }
    }

    /**
     * Graceful shutdown sequence for the current session, if one is
     * active: signal the autosave thread to stop, interrupt it (it is
     * likely sleeping), wait for it to terminate with join(), do one
     * last synchronous save, then shut down the stats executor.
     */
    private void stopBackgroundWork() {
        if (autoSaveWorker != null) {
            autoSaveWorker.stop();
            autoSaveThread.interrupt();
            try {
                autoSaveThread.join(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        if (repository != null) {
            repository.save();
        }
        if (statsCalculator != null) {
            statsCalculator.shutdown();
        }
        autoSaveWorker = null;
        autoSaveThread = null;
        repository = null;
        statsCalculator = null;
    }

    private void shutdown() {
        stopBackgroundWork();
        Platform.exit();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
