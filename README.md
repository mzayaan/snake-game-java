# Snake

Classic Snake, written in Java with Swing. Built as a team project for the
BSc (Hons) Software Engineering programme at the University of Technology,
Mauritius, applying object-oriented design.

**▶ [Play it in your browser](https://mzayaan.github.io/snake-game-java/)** — no
install needed. The Java bytecode runs client-side through
[CheerpJ](https://cheerpj.com/), so the first load pulls down a JVM and takes a
few seconds.

## Gameplay

- Arrow keys to steer.
- Eat food to grow and score.
- Avoid bombs and your own tail.
- Scores append to a leaderboard file.

## How it's put together

One source file, `SnakeGame.java`, holding a handful of focused classes:

| Class | Responsibility |
| --- | --- |
| `SnakeGame` | Entry point, window setup, shared constants |
| `GamePanel` | Render loop, input handling, collision and game state |
| `Snake` | Body segments, movement, growth |
| `Food` | Spawn position and scoring value |
| `Bomb` | Hazard placement |
| `Score` | Score tracking and leaderboard writing |

Grid is 25 px cells in a 650 × 650 window.

## Run it on your machine

Needs a JDK (17 or newer). No build tool, no dependencies.

```bash
git clone https://github.com/mzayaan/snake-game-java
cd snake-game-java
javac SnakeGame.java
java SnakeGame
```

Run it from the project directory so `SnakeGraphics.jpg` and `leaderboard.txt`
resolve.

## Deployment

`.github/workflows/deploy.yml` compiles the source, packages a runnable
`snake-game.jar`, and publishes it to GitHub Pages alongside `web/index.html`,
which boots the JAR with CheerpJ.

The browser build rewrites two file paths at compile time, because relative
paths don't resolve inside CheerpJ's virtual filesystem:

| Source | Browser build | Why |
| --- | --- | --- |
| `SnakeGraphics.jpg` | `/app/snake-game-java/SnakeGraphics.jpg` | `/app/` is the read-only mount of the site |
| `leaderboard.txt` | `/files/leaderboard.txt` | `/files/` is writable, backed by IndexedDB |

The committed source is untouched, so the desktop version still runs unchanged.
