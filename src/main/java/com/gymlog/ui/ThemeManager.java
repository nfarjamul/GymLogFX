package com.gymlog.ui;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Parent;
import javafx.scene.control.Button;

/**
 * Small piece of UI state shared across the whole app: whether dark mode
 * is on. Both the login screen and the main app screen build their dark
 * mode toggle button through {@link #buildToggleButton()}, so flipping
 * the icon on one screen is really just flipping this one property -
 * whichever Parent is the Scene's current root gets re-themed via
 * {@link #applyTo(Parent)}.
 *
 * <p>The actual re-theming trick is CSS: a "dark" style class is added
 * to (or removed from) the root node, and {@code styles.css} uses that
 * class to override {@code -fx-base}/{@code -fx-background} (which most
 * built-in JavaFX controls derive their colors from) plus a handful of
 * explicit overrides for things that don't pick those variables up
 * automatically, such as labels and charts.
 */
public class ThemeManager {

    private static final String DARK_STYLE_CLASS = "dark";

    private final BooleanProperty darkMode = new SimpleBooleanProperty(false);

    public BooleanProperty darkModeProperty() {
        return darkMode;
    }

    public boolean isDarkMode() {
        return darkMode.get();
    }

    /** Adds or removes the "dark" style class on the given root to match the current state. */
    public void applyTo(Parent root) {
        if (root == null) {
            return;
        }
        boolean on = darkMode.get();
        boolean present = root.getStyleClass().contains(DARK_STYLE_CLASS);
        if (on && !present) {
            root.getStyleClass().add(DARK_STYLE_CLASS);
        } else if (!on && present) {
            root.getStyleClass().remove(DARK_STYLE_CLASS);
        }
    }

    /** Builds a small text/icon toggle button wired to this shared dark-mode state. */
    public Button buildToggleButton() {
        Button button = new Button();
        button.getStyleClass().add("dark-toggle");
        button.textProperty().bind(Bindings.when(darkMode)
                .then("\u2600  Light Mode")
                .otherwise("\uD83C\uDF19  Dark Mode"));
        button.setOnAction(e -> darkMode.set(!darkMode.get()));
        return button;
    }
}
