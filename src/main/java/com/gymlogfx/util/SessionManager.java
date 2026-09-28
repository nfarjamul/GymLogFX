package com.gymlogfx.util;

import com.gymlogfx.model.User;

/**
 * SessionManager - Singleton managing the currently logged-in user.
 * Demonstrates Singleton design pattern.
 */
public class SessionManager {
    private static SessionManager instance;
    private User currentUser;
    private boolean darkMode = true;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public User getCurrentUser() { return currentUser; }
    public void setCurrentUser(User user) { this.currentUser = user; }
    public boolean isLoggedIn() { return currentUser != null; }
    public void logout() { currentUser = null; }

    public boolean isDarkMode() { return darkMode; }
    public void setDarkMode(boolean darkMode) { this.darkMode = darkMode; }

    public void toggleDarkMode() { this.darkMode = !this.darkMode; }
}
