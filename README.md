# GymLogFX

A JavaFX desktop workout logger: log sets, view progressive-overload
charts per exercise, and build reusable workout routines. Built to be a
working example of core JavaFX GUI concepts **and** core Java
concurrency concepts (`Thread`/`Runnable`, thread lifecycle,
synchronization over a shared resource, and the
`java.util.concurrent` executor framework).

---

## 1. Prerequisites

- **JDK 17 or newer** (JDK 21 recommended). Check with:
  ```
  java -version
  ```
- **Apache Maven 3.8+**. Check with:
  ```
  mvn -version
  ```
- No IDE is required, but IntelliJ IDEA, Eclipse or VS Code (with the
  "Extension Pack for Java") all import this project fine as a plain
  Maven project — no extra JavaFX plugin needed in the IDE, since the
  JavaFX jars are pulled in as normal Maven dependencies.

You do **not** need to install JavaFX separately — the `pom.xml`
declares `javafx-controls`, `javafx-graphics` and `javafx-base` as
regular Maven dependencies (version 21.0.2), and Maven downloads the
correct native binaries for your OS (Windows/macOS/Linux) automatically.

---

## 2. Running the app

From the project root (the folder containing `pom.xml`):

```bash
mvn clean javafx:run
```

The first run will download dependencies (including the JavaFX
runtime for your platform) and then open the GymLogFX window.

### Running from an IDE instead

1. Import the project as a Maven project.
2. Let Maven resolve dependencies.
3. Run the `com.gymlog.Main` class directly. (`Main` extends
   `javafx.application.Application` and has a normal `main` method
   that calls `launch(args)`, so most IDEs can run it like any other
   Java program — no special JavaFX run configuration is required as
   long as the JavaFX jars are on the module/classpath, which Maven
   already arranged.)

### Building a runnable jar (optional)

```bash
mvn clean package
```

This produces `target/gymlogfx-1.0.0.jar`. Because JavaFX has
platform-specific native libraries, the simplest way to launch that
jar directly is still through `mvn javafx:run`, or by adding the
`javafx-maven-plugin`'s `jlink` goal / a shade plugin if you want a
fully self-contained distributable — that's an optional next step,
not required for local development.

---

## 3. Where your data is stored

GymLogFX persists everything as plain CSV files in your home
directory, under:

```
~/.gymlogfx/workouts.csv
~/.gymlogfx/routines.csv
```

These are created automatically the first time the app saves. Delete
them (or the whole `.gymlogfx` folder) to reset the app to a blank
slate.

---

## 4. Using the app

### Login

The app opens on a login screen. The **first time** you run it, there's
no account yet, so it starts in "Create Account" mode — pick a username
and password and click **Create Account**. Your credentials are stored
(salted and hashed, not in plain text) in `~/.gymlogfx/user.properties`,
and every time after that you'll log in with them instead. Click
**Log Out** in the main app's status bar at any time to return to the
login screen without closing the app.

This is a real local login gate, not a demo stub — but it's still a
single-user desktop app with no server behind it, so treat it as a
convenience layer, not production-grade security.

### Dark mode

Click the 🌙 **Dark Mode** button (on the login screen or in the main
app's status bar) to toggle a dark theme across the whole app — it
flips a single shared piece of state, so the two screens always agree
on which mode you're in even after you log out and back in.

### The three tabs

- **🏋 Log Workout** — pick or type an exercise name, enter weight, reps
  and sets, and click **Add Set**. Your entry appears immediately in
  the table below. Select a row and click **Delete Selected** to
  remove it.
- **📈 History & Charts** — pick an exercise from the dropdown to see a
  line chart of your top set weight for that exercise over time
  (classic progressive-overload tracking), plus a one-line summary of
  how much you've added since your first logged session.
- **📋 Routines** — build a named routine (e.g. "Push Day") by selecting
  exercises from the list on the right and clicking **Add →**. Click
  **Save Routine** to store it. Saved routines appear on the left;
  select one to edit it, or delete it.

A status bar at the bottom of the main window shows what the
background autosave thread is doing (see below), who's logged in, the
dark mode toggle, and the log-out button.

---

## 5. Project structure

```
GymLogFX/
├── pom.xml
└── src/main/
    ├── java/com/gymlog/
    │   ├── Main.java                    Entry point; login gate, theme wiring, thread lifecycle
    │   ├── model/
    │   │   ├── WorkoutEntry.java        One logged set (date, exercise, weight, reps, sets)
    │   │   └── Routine.java             A named list of exercises
    │   ├── data/
    │   │   ├── WorkoutRepository.java   The shared, synchronized in-memory store + CSV I/O
    │   │   └── UserStore.java           Local login/account store (salted SHA-256 hash)
    │   ├── concurrency/
    │   │   ├── AutoSaveWorker.java      Runnable run on a raw Thread (autosave loop)
    │   │   └── StatsCalculator.java     ExecutorService wrapper for background chart math
    │   └── ui/
    │       ├── LoginController.java     FXML controller for login.fxml
    │       ├── ThemeManager.java        Shared dark-mode state + toggle button
    │       ├── LogWorkoutPane.java      "Log Workout" tab
    │       ├── HistoryPane.java         "History & Charts" tab
    │       └── RoutineBuilderPane.java  "Routines" tab
    └── resources/com/gymlog/
        ├── styles.css                   Light theme (default) + ".dark" overrides
        └── ui/login.fxml                Login / create-account screen layout
```

---

## 6. How this app covers each topic

### JavaFX fundamentals

- **Application structure / stages & scenes**: `Main.start(Stage)`
  builds one `Scene` containing a `BorderPane`, and sets it on the
  primary `Stage`.
- **Common controls**: `TableView`, `ComboBox` (editable), `DatePicker`,
  `Spinner`, `TextField`, `Button`, `Label`, `ListView` (including
  multi-selection), and `LineChart`/`XYChart` for the chart.
- **Event handling**: button `setOnAction(...)` handlers, a
  `ComboBox` `setOnAction`, a `ListView` selection listener, and a
  `TabPane` selection listener that refreshes other tabs.
- **Layouts**: `BorderPane` (overall page structure in every pane),
  `VBox`/`HBox` (button rows, form rows), and `GridPane` (the
  Log Workout form).

### Concurrency

- **Thread and Runnable / thread lifecycle** — `AutoSaveWorker`
  (`concurrency/AutoSaveWorker.java`) implements `Runnable`. `Main`
  wraps it in `new Thread(worker, "AutoSave-Thread")`, calls
  `start()`, and on window close calls a cooperative `stop()` (a
  `volatile boolean` flag) followed by `Thread.interrupt()` (to wake it
  out of `Thread.sleep`) and `Thread.join()` (to wait for it to
  actually finish) before exiting. This walks through the full
  new → runnable → running → timed-waiting → terminated lifecycle.
- **Synchronization & shared resources** — `WorkoutRepository`
  (`data/WorkoutRepository.java`) is read and written from the JavaFX
  Application Thread, the autosave thread, and the stats worker
  threads. Every method that touches its internal lists is
  `synchronized`, and read methods return defensive copies so callers
  can safely iterate the result on any thread. The class's Javadoc
  explains the reasoning in detail.
- **`java.util.concurrent` / executors** — `StatsCalculator`
  (`concurrency/StatsCalculator.java`) wraps a
  `ExecutorService` (`Executors.newFixedThreadPool(2, ...)`, with a
  custom `ThreadFactory` for named daemon threads). `HistoryPane`
  submits a `Callable` via `computeProgressionAsync(...)`, gets back a
  `Future`, and a small helper thread blocks on `future.get()` before
  handing the result to the UI thread with `Platform.runLater(...)` —
  the only safe way to touch live JavaFX nodes from a background
  thread.

### Login, FXML and dark mode

- **Login page**: `Main` shows `login.fxml` first. `LoginController`
  handles both logging in and (on first run) creating an account,
  backed by `UserStore`, which stores a username and a salted SHA-256
  password hash in `~/.gymlogfx/user.properties`. On success it hands
  the username to `Main.showMainApp(String)`, which is also where the
  repository and background threads are (re)created — logging out
  cleanly tears them down via `stopBackgroundWork()` and returns to a
  fresh login screen, ready to log back in.
- **FXML**: `login.fxml` is loaded with `FXMLLoader` and defines the
  login screen's whole layout declaratively (`StackPane` > `VBox` card
  containing the title, fields, error label, action button, and a
  hyperlink to switch between "log in" and "create account"). Its
  `fx:controller` attribute wires it straight to `LoginController`,
  whose `@FXML`-annotated fields are injected by the loader and whose
  `initialize()` method runs automatically once loading finishes.
- **Interactivity touches**: a shake animation on the login card when
  credentials are rejected, tooltips on a few key buttons, emoji tab
  icons, and a fade-in transition when the main app appears after
  login.
- **Dark mode**: `ThemeManager` holds one shared `BooleanProperty`.
  Both the login screen and the main screen build their toggle button
  through `ThemeManager.buildToggleButton()`, so they always agree on
  the current mode. Toggling it adds/removes a `"dark"` style class on
  the Scene's current root; `styles.css` uses that class to override
  `-fx-base`/`-fx-background` (which most built-in Modena controls
  derive their colors from) plus explicit rules for labels, charts,
  and this app's own custom style classes.

---

## 7. Extending it further

Some natural next steps if you want to keep building on this:

- Swap the CSV persistence in `WorkoutRepository` for SQLite (via
  JDBC) — the `synchronized` boundary stays exactly where it is.
- Add a `ScheduledExecutorService` version of the autosave loop as an
  alternative to the raw `Thread`, to compare the two approaches.
- Add unit tests for `WorkoutEntry`/`Routine` CSV round-tripping and
  for `StatsCalculator`'s aggregation logic.
- Move more of the main screen (the tabs, the status bar) into its own
  FXML file alongside `login.fxml`, following the same pattern.
- Support more than one local account in `UserStore` if you want
  multiple people sharing the same machine to keep separate logs.
