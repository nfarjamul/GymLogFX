# GymLogFX

GymLogFX is a JavaFX desktop application for logging workouts, tracking progressive overload, and building custom training routines — with exercise history visualized through charts.

## Features

- **Workout Logging** — record exercises, sets, reps, and weights for each session
- **Exercise History Charts** — visualize performance trends over time
- **Progressive-Overload Tracking** — monitor strength gains and identify plateaus
- **Custom Routine Builder** — create and manage personalized workout routines
- **Responsive GUI** — built with JavaFX controls and layouts for a smooth desktop experience

## Tech Stack

- **Language:** Java
- **GUI Framework:** JavaFX
  - Application structure (stages, scenes)
  - Common controls, layouts, and event handling
- **Concurrency:**
  - `Thread` and `Runnable`
  - Thread lifecycle and synchronization
  - Shared resource management
  - `java.util.concurrent` (executors, concurrent task management)

## Getting Started

### Prerequisites

- [JDK](https://adoptium.net/) 17 or later
- [JavaFX SDK](https://openjfx.io/) (if not bundled with your JDK)
- Maven or Gradle (depending on the build setup used)

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/<your-username>/GymLogFX.git
   cd GymLogFX
   ```

2. Build the project:
   ```bash
   mvn clean install
   ```

3. Run the application:
   ```bash
   mvn javafx:run
   ```

> Update the steps above to match your actual build tool (Maven/Gradle) and entry-point class.

## Usage

1. Launch GymLogFX.
2. Create or select a workout routine.
3. Log exercises, sets, reps, and weights during your session.
4. View your progress on the exercise history charts to track progressive overload over time.

## Project Structure

```
GymLogFX/
├── src/
│   ├── main/
│   │   ├── java/        # Application source code
│   │   └── resources/   # FXML files, stylesheets, assets
├── pom.xml               # Build configuration (Maven)
└── README.md
```

## Concurrency Design

GymLogFX uses Java's concurrency utilities to keep the UI responsive while handling background tasks such as data processing and chart updates:

- Background work is offloaded using `Thread`/`Runnable` and the `java.util.concurrent` executor framework
- Shared data (e.g., workout logs) is synchronized to prevent race conditions
- UI updates are dispatched back to the JavaFX Application Thread to avoid blocking the interface

## Contributing

Contributions are welcome. Please open an issue to discuss proposed changes before submitting a pull request.

## License

Add your chosen license here (e.g., MIT, Apache 2.0).
