# GymLogFX 🏋️

A comprehensive **JavaFX workout logger** desktop application for tracking gym sessions, visualizing progress, building routines, and evaluating fitness assessments - all backed by a local SQLite database.

---

## Features

- **Workout Logging** — Log exercises with sets, reps, and weight. Session history stored persistently in SQLite.
- **Progress Charts** — Interactive line/bar charts (JavaFX Charts) showing strength progression and volume over time.
- **Routine Builder** — Create, edit, and reorder custom workout routines. Assign exercises to days of the week.
- **Evaluator Demo** — Built-in fitness evaluator module for BMI, 1-rep max estimates, and readiness scoring.
- **Dark / Light Themes** — Toggle between a sleek dark theme and a clean light theme at runtime.
- **Multi-user Login** — Simple credential-based login screen with per-user data separation.
- **API Integration** — Background `HttpClient` calls to the ExerciseDB / Wger REST API for exercise library lookups.
- **Export** — Export workout history to CSV for use in spreadsheets.

---

## Prerequisites

| Tool | Minimum Version |
|------|----------------|
| Java JDK | 17 |
| Apache Maven | 3.8+ |
| Internet connection | Optional (for API features) |

> **Note**: JavaFX 21 modules are pulled automatically by Maven from Maven Central — no separate JavaFX SDK installation is required.

---

## Build & Run

### Clone the repository
```bash
git clone https://github.com/youruser/GymLogFX.git
cd GymLogFX
```

### Compile
```bash
mvn clean compile
```

### Run with JavaFX Maven plugin
```bash
mvn javafx:run
```

### Package as fat JAR
```bash
mvn clean package
java -jar target/GymLogFX-1.0.0.jar
```

---

## Project Structure

```
GymLogFXFinal/
├── pom.xml
├── README.md
├── .gitignore
└── src/
    └── main/
        ├── java/
        │   ├── module-info.java
        │   └── com/gymlogfx/
        │       ├── MainApp.java                  # JavaFX Application entry point
        │       ├── controller/
        │       │   ├── MainController.java        # Root layout + navigation
        │       │   ├── LoginController.java       # Login screen logic
        │       │   ├── DashboardController.java   # Dashboard & stats overview
        │       │   ├── WorkoutLogController.java  # Log a new workout session
        │       │   ├── HistoryController.java     # Browse past sessions
        │       │   ├── RoutineController.java     # Routine builder
        │       │   ├── ChartsController.java      # Progress charts
        │       │   └── SettingsController.java    # App settings & theme toggle
        │       ├── model/
        │       │   ├── User.java
        │       │   ├── Exercise.java
        │       │   ├── WorkoutSession.java
        │       │   ├── WorkoutSet.java
        │       │   └── Routine.java
        │       ├── database/
        │       │   ├── DatabaseManager.java       # JDBC connection & schema init
        │       │   ├── UserDAO.java
        │       │   ├── ExerciseDAO.java
        │       │   ├── WorkoutDAO.java
        │       │   └── RoutineDAO.java
        │       ├── service/
        │       │   ├── ExerciseApiService.java    # Async HTTP calls
        │       │   ├── WorkoutService.java
        │       │   └── ChartService.java
        │       ├── evaluator/
        │       │   ├── EvaluatorController.java
        │       │   └── FitnessEvaluator.java
        │       └── util/
        │           ├── SessionManager.java        # Singleton: current logged-in user
        │           ├── ThemeManager.java          # Dark/light CSS swapping
        │           ├── CsvExporter.java
        │           └── AlertHelper.java
        └── resources/
            ├── fxml/
            │   ├── Login.fxml
            │   ├── Main.fxml
            │   ├── Dashboard.fxml
            │   ├── WorkoutLog.fxml
            │   ├── History.fxml
            │   ├── Routine.fxml
            │   ├── Charts.fxml
            │   ├── Settings.fxml
            │   └── Evaluator.fxml
            ├── styles/
            │   ├── dark-theme.css
            │   └── light-theme.css
            └── images/
                └── logo.png
```

---

## Database Schema

All data is stored in a local SQLite file (`gymlog.db`) created on first launch.

```sql
CREATE TABLE users (
    id       INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT UNIQUE NOT NULL,
    password TEXT NOT NULL,          -- bcrypt hash in production
    created_at TEXT DEFAULT (datetime('now'))
);

CREATE TABLE exercises (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    name        TEXT NOT NULL,
    muscle_group TEXT,
    equipment   TEXT,
    description TEXT
);

CREATE TABLE workout_sessions (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id    INTEGER REFERENCES users(id),
    date       TEXT NOT NULL,
    notes      TEXT,
    duration_minutes INTEGER
);

CREATE TABLE workout_sets (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    session_id  INTEGER REFERENCES workout_sessions(id),
    exercise_id INTEGER REFERENCES exercises(id),
    set_number  INTEGER,
    reps        INTEGER,
    weight_kg   REAL
);

CREATE TABLE routines (
    id      INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER REFERENCES users(id),
    name    TEXT NOT NULL,
    day_of_week TEXT
);

CREATE TABLE routine_exercises (
    routine_id  INTEGER REFERENCES routines(id),
    exercise_id INTEGER REFERENCES exercises(id),
    order_index INTEGER
);
```

---

## Threading Model

| Layer | Thread |
|-------|--------|
| UI rendering & event handlers | JavaFX Application Thread |
| Database reads/writes | Background `Thread` / `Task<T>` |
| HTTP API calls | `CompletableFuture` via `HttpClient` (daemon threads) |
| Chart data aggregation | `Platform.runLater()` for UI updates after background compute |

All database operations use `javafx.concurrent.Task` to avoid blocking the UI thread. Results are posted back with `Platform.runLater()`.

---

## API Integration

The `ExerciseApiService` uses Java 11+ `HttpClient` to fetch exercise data from:

- **Wger REST API** — `https://wger.de/api/v2/exercise/`
- **ExerciseDB (RapidAPI)** — `https://exercisedb.p.rapidapi.com/exercises`

Responses are parsed with **Gson** into model objects and cached in the local SQLite database to minimise API calls.

Configure your RapidAPI key in `Settings` or via the `EXERCISEDB_API_KEY` environment variable.



