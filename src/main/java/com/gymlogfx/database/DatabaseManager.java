package com.gymlogfx.database;

import com.gymlogfx.model.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * DatabaseManager - Singleton class managing all SQLite database operations.
 *
 * DATABASE SCHEMA:
 * ┌─────────────────────────────────────────────────────────────────┐
 * │  users          │ id, username, email, password_hash,           │
 * │                 │ fitness_level, join_date                      │
 * ├─────────────────┼───────────────────────────────────────────────┤
 * │  exercises      │ id, name, muscle_group, equipment, description│
 * ├─────────────────┼───────────────────────────────────────────────┤
 * │  workout_logs   │ id, user_id(FK), exercise_id(FK),             │
 * │                 │ workout_date, weight, sets, reps, notes       │
 * ├─────────────────┼───────────────────────────────────────────────┤
 * │  routines       │ id, user_id(FK), name, description,           │
 * │                 │ created_date                                  │
 * ├─────────────────┼───────────────────────────────────────────────┤
 * │  routine_exs    │ id, routine_id(FK), exercise_id(FK),          │
 * │                 │ sets, reps, sort_order                        │
 * └─────────────────────────────────────────────────────────────────┘
 *
 * RELATIONSHIPS:
 * - workout_logs.user_id → users.id  (many-to-one)
 * - workout_logs.exercise_id → exercises.id  (many-to-one)
 * - routines.user_id → users.id  (many-to-one)
 * - routine_exercises.routine_id → routines.id  (many-to-one)
 * - routine_exercises.exercise_id → exercises.id  (many-to-one)
 */
public class DatabaseManager {

    private static DatabaseManager instance;
    private Connection connection;
    private static final String DB_URL = "jdbc:sqlite:gymlogfx.db";

    private DatabaseManager() {
        initializeDatabase();
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    private void initializeDatabase() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(DB_URL);
            connection.createStatement().execute("PRAGMA foreign_keys = ON");
            createTables();
            seedDefaultData();
        } catch (Exception e) {
            System.err.println("Database initialization error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createTables() throws SQLException {
        Statement stmt = connection.createStatement();

        // Users table
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL UNIQUE,
                email TEXT NOT NULL UNIQUE,
                password_hash TEXT NOT NULL,
                fitness_level TEXT DEFAULT 'Beginner',
                join_date TEXT NOT NULL
            )
        """);

        // Exercises table
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS exercises (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL UNIQUE,
                muscle_group TEXT NOT NULL,
                equipment TEXT,
                description TEXT
            )
        """);

        // Workout logs table - FK relationships to users and exercises
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS workout_logs (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                exercise_id INTEGER NOT NULL,
                exercise_name TEXT NOT NULL,
                workout_date TEXT NOT NULL,
                weight REAL DEFAULT 0.0,
                sets INTEGER DEFAULT 1,
                reps INTEGER DEFAULT 10,
                notes TEXT,
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                FOREIGN KEY (exercise_id) REFERENCES exercises(id) ON DELETE RESTRICT
            )
        """);

        // Routines table - FK to users
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS routines (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                name TEXT NOT NULL,
                description TEXT,
                created_date TEXT NOT NULL,
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
            )
        """);

        // Routine exercises junction table - FK to routines and exercises
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS routine_exercises (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                routine_id INTEGER NOT NULL,
                exercise_id INTEGER NOT NULL,
                sets INTEGER DEFAULT 3,
                reps INTEGER DEFAULT 10,
                sort_order INTEGER DEFAULT 0,
                FOREIGN KEY (routine_id) REFERENCES routines(id) ON DELETE CASCADE,
                FOREIGN KEY (exercise_id) REFERENCES exercises(id) ON DELETE RESTRICT
            )
        """);

        stmt.close();
    }

    private void seedDefaultData() throws SQLException {
        // Check if already seeded
        ResultSet rs = connection.createStatement().executeQuery("SELECT COUNT(*) FROM exercises");
        if (rs.next() && rs.getInt(1) > 0) return;

        // Seed exercises
        String insertExercise = "INSERT OR IGNORE INTO exercises (name, muscle_group, equipment, description) VALUES (?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(insertExercise);

        Object[][] exercises = {
            {"Bench Press", "Chest", "Barbell", "Classic chest exercise targeting the pectoral muscles"},
            {"Squat", "Legs", "Barbell", "King of all exercises - targets quads, glutes, hamstrings"},
            {"Deadlift", "Back", "Barbell", "Full body compound movement - posterior chain focus"},
            {"Pull-Up", "Back", "Bodyweight", "Upper body pulling exercise for lats and biceps"},
            {"Overhead Press", "Shoulders", "Barbell", "Vertical pressing movement for shoulder development"},
            {"Dumbbell Curl", "Arms", "Dumbbell", "Isolation exercise for bicep development"},
            {"Tricep Pushdown", "Arms", "Cable", "Cable exercise for tricep isolation"},
            {"Leg Press", "Legs", "Machine", "Machine-based quad dominant leg exercise"},
            {"Lat Pulldown", "Back", "Cable", "Cable pulldown targeting the latissimus dorsi"},
            {"Romanian Deadlift", "Legs", "Barbell", "Hip hinge movement for hamstring and glute development"},
            {"Dumbbell Row", "Back", "Dumbbell", "Unilateral row for back thickness and width"},
            {"Incline Bench Press", "Chest", "Barbell", "Upper chest focused pressing movement"},
            {"Leg Curl", "Legs", "Machine", "Machine-based hamstring isolation exercise"},
            {"Calf Raise", "Legs", "Machine", "Isolation exercise for calf muscles"},
            {"Face Pull", "Shoulders", "Cable", "Rear delt and external rotation exercise"}
        };

        for (Object[] ex : exercises) {
            ps.setString(1, (String) ex[0]);
            ps.setString(2, (String) ex[1]);
            ps.setString(3, (String) ex[2]);
            ps.setString(4, (String) ex[3]);
            ps.executeUpdate();
        }
        ps.close();

        // Seed a default user: demo / password123
        String now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        String insertUser = "INSERT OR IGNORE INTO users (username, email, password_hash, fitness_level, join_date) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement ups = connection.prepareStatement(insertUser);
        ups.setString(1, "demo");
        ups.setString(2, "demo@gymlogfx.com");
        ups.setString(3, hashPassword("password123"));
        ups.setString(4, "Intermediate");
        ups.setString(5, now);
        ups.executeUpdate();
        ups.close();
    }

    /** Simple hash - in production use BCrypt */
    public String hashPassword(String password) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            return password;
        }
    }

    // ==================== USER CRUD ====================

    /** CREATE: Insert a new user */
    public boolean createUser(String username, String email, String password, String fitnessLevel) {
        String now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        String sql = "INSERT INTO users (username, email, password_hash, fitness_level, join_date) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, email);
            ps.setString(3, hashPassword(password));
            ps.setString(4, fitnessLevel);
            ps.setString(5, now);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error creating user: " + e.getMessage());
            return false;
        }
    }

    /** READ: Authenticate a user */
    public User authenticateUser(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password_hash = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, hashPassword(password));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("email"),
                    rs.getString("password_hash"),
                    rs.getString("fitness_level"),
                    rs.getString("join_date")
                );
            }
        } catch (SQLException e) {
            System.err.println("Auth error: " + e.getMessage());
        }
        return null;
    }

    /** UPDATE: Update user fitness level */
    public boolean updateUserFitnessLevel(int userId, String fitnessLevel) {
        String sql = "UPDATE users SET fitness_level = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, fitnessLevel);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Update error: " + e.getMessage());
            return false;
        }
    }

    /** DELETE: Delete a user */
    public boolean deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Delete error: " + e.getMessage());
            return false;
        }
    }

    // ==================== EXERCISE CRUD ====================

    /** READ: Get all exercises */
    public ObservableList<Exercise> getAllExercises() {
        ObservableList<Exercise> list = FXCollections.observableArrayList();
        String sql = "SELECT * FROM exercises ORDER BY muscle_group, name";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Exercise(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("muscle_group"),
                    rs.getString("equipment"),
                    rs.getString("description")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching exercises: " + e.getMessage());
        }
        return list;
    }

    /** CREATE: Add a new exercise */
    public boolean createExercise(String name, String muscleGroup, String equipment, String description) {
        String sql = "INSERT INTO exercises (name, muscle_group, equipment, description) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, muscleGroup);
            ps.setString(3, equipment);
            ps.setString(4, description);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error creating exercise: " + e.getMessage());
            return false;
        }
    }

    /** UPDATE: Update exercise */
    public boolean updateExercise(int id, String name, String muscleGroup, String equipment, String desc) {
        String sql = "UPDATE exercises SET name=?, muscle_group=?, equipment=?, description=? WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, muscleGroup);
            ps.setString(3, equipment);
            ps.setString(4, desc);
            ps.setInt(5, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating exercise: " + e.getMessage());
            return false;
        }
    }

    /** DELETE: Remove exercise */
    public boolean deleteExercise(int id) {
        String sql = "DELETE FROM exercises WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting exercise: " + e.getMessage());
            return false;
        }
    }

    // ==================== WORKOUT LOG CRUD ====================

    /** CREATE: Log a workout */
    public boolean logWorkout(int userId, int exerciseId, String exerciseName,
                               String date, double weight, int sets, int reps, String notes) {
        String sql = "INSERT INTO workout_logs (user_id, exercise_id, exercise_name, workout_date, weight, sets, reps, notes) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, exerciseId);
            ps.setString(3, exerciseName);
            ps.setString(4, date);
            ps.setDouble(5, weight);
            ps.setInt(6, sets);
            ps.setInt(7, reps);
            ps.setString(8, notes);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error logging workout: " + e.getMessage());
            return false;
        }
    }

    /** READ: Get workout logs for a user */
    public ObservableList<WorkoutLog> getWorkoutLogs(int userId) {
        ObservableList<WorkoutLog> list = FXCollections.observableArrayList();
        String sql = "SELECT * FROM workout_logs WHERE user_id = ? ORDER BY workout_date DESC, id DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new WorkoutLog(
                    rs.getInt("id"),
                    rs.getInt("user_id"),
                    rs.getInt("exercise_id"),
                    rs.getString("exercise_name"),
                    rs.getString("workout_date"),
                    rs.getDouble("weight"),
                    rs.getInt("sets"),
                    rs.getInt("reps"),
                    rs.getString("notes")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching logs: " + e.getMessage());
        }
        return list;
    }

    /** READ: Get workout logs for progressive overload chart (specific exercise) */
    public List<WorkoutLog> getProgressionData(int userId, String exerciseName) {
        List<WorkoutLog> list = new ArrayList<>();
        String sql = "SELECT * FROM workout_logs WHERE user_id = ? AND exercise_name = ? ORDER BY workout_date ASC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, exerciseName);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new WorkoutLog(
                    rs.getInt("id"),
                    rs.getInt("user_id"),
                    rs.getInt("exercise_id"),
                    rs.getString("exercise_name"),
                    rs.getString("workout_date"),
                    rs.getDouble("weight"),
                    rs.getInt("sets"),
                    rs.getInt("reps"),
                    rs.getString("notes")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching progression: " + e.getMessage());
        }
        return list;
    }

    /** UPDATE: Update a workout log entry */
    public boolean updateWorkoutLog(int logId, double weight, int sets, int reps, String notes) {
        String sql = "UPDATE workout_logs SET weight=?, sets=?, reps=?, notes=? WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setDouble(1, weight);
            ps.setInt(2, sets);
            ps.setInt(3, reps);
            ps.setString(4, notes);
            ps.setInt(5, logId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating log: " + e.getMessage());
            return false;
        }
    }

    /** DELETE: Remove a workout log entry */
    public boolean deleteWorkoutLog(int logId) {
        String sql = "DELETE FROM workout_logs WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, logId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting log: " + e.getMessage());
            return false;
        }
    }

    // ==================== ROUTINE CRUD ====================

    /** CREATE: Create a new routine */
    public int createRoutine(int userId, String name, String description) {
        String now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        String sql = "INSERT INTO routines (user_id, name, description, created_date) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            ps.setString(2, name);
            ps.setString(3, description);
            ps.setString(4, now);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
        } catch (SQLException e) {
            System.err.println("Error creating routine: " + e.getMessage());
        }
        return -1;
    }

    /** READ: Get routines for a user */
    public ObservableList<Routine> getRoutines(int userId) {
        ObservableList<Routine> list = FXCollections.observableArrayList();
        String sql = "SELECT * FROM routines WHERE user_id = ? ORDER BY created_date DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Routine(
                    rs.getInt("id"),
                    rs.getInt("user_id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getString("created_date")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching routines: " + e.getMessage());
        }
        return list;
    }

    /** UPDATE: Update routine */
    public boolean updateRoutine(int routineId, String name, String description) {
        String sql = "UPDATE routines SET name=?, description=? WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, description);
            ps.setInt(3, routineId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating routine: " + e.getMessage());
            return false;
        }
    }

    /** DELETE: Remove routine (cascades to routine_exercises) */
    public boolean deleteRoutine(int routineId) {
        String sql = "DELETE FROM routines WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, routineId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting routine: " + e.getMessage());
            return false;
        }
    }

    /** Add exercise to routine */
    public boolean addExerciseToRoutine(int routineId, int exerciseId, int sets, int reps, int sortOrder) {
        String sql = "INSERT INTO routine_exercises (routine_id, exercise_id, sets, reps, sort_order) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, routineId);
            ps.setInt(2, exerciseId);
            ps.setInt(3, sets);
            ps.setInt(4, reps);
            ps.setInt(5, sortOrder);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error adding exercise to routine: " + e.getMessage());
            return false;
        }
    }

    /** Get exercises for a routine */
    public ObservableList<Exercise> getRoutineExercises(int routineId) {
        ObservableList<Exercise> list = FXCollections.observableArrayList();
        String sql = "SELECT e.* FROM exercises e " +
                     "JOIN routine_exercises re ON e.id = re.exercise_id " +
                     "WHERE re.routine_id = ? ORDER BY re.sort_order";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, routineId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Exercise(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("muscle_group"),
                    rs.getString("equipment"),
                    rs.getString("description")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error getting routine exercises: " + e.getMessage());
        }
        return list;
    }

    public Connection getConnection() { return connection; }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing DB: " + e.getMessage());
        }
    }
}
