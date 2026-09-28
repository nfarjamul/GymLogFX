package com.gymlogfx.util;

import javafx.scene.Scene;

import java.util.ArrayList;
import java.util.List;

/**
 * ThemeManager - Manages dark/light theme switching across all scenes.
 * Demonstrates Observer-like pattern for theme propagation.
 */
public class ThemeManager {
    private static ThemeManager instance;
    private final List<Scene> registeredScenes = new ArrayList<>();
    private static final String DARK_CSS = "/styles/dark-theme.css";
    private static final String LIGHT_CSS = "/styles/light-theme.css";

    private ThemeManager() {}

    public static ThemeManager getInstance() {
        if (instance == null) instance = new ThemeManager();
        return instance;
    }

    public void registerScene(Scene scene) {
        registeredScenes.add(scene);
        applyTheme(scene, SessionManager.getInstance().isDarkMode());
    }

    public void applyTheme(Scene scene, boolean darkMode) {
        scene.getStylesheets().clear();
        String css = getClass().getResource(darkMode ? DARK_CSS : LIGHT_CSS).toExternalForm();
        scene.getStylesheets().add(css);
    }

    public void toggleTheme() {
        SessionManager.getInstance().toggleDarkMode();
        boolean dark = SessionManager.getInstance().isDarkMode();
        for (Scene scene : registeredScenes) {
            applyTheme(scene, dark);
        }
    }

    public void applyThemeToScene(Scene scene) {
        applyTheme(scene, SessionManager.getInstance().isDarkMode());
    }

    public void unregisterScene(Scene scene) {
        registeredScenes.remove(scene);
    }
}
