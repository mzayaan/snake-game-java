import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class SnakeGame {
    public static final int WINDOW_SIZE = 650; // Window dimensions
    public static final int GRID_SIZE = 25;   // Size of the grid cells
    public static final int MOVE_STEP = 25;  // Movement step for the snake

    public static void main(String[] args) {
        JFrame frame = new JFrame("Snake Game with Styled Snake and Grid");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        GamePanel gamePanel = new GamePanel(frame);
        frame.add(gamePanel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class GamePanel extends JPanel {
    private final Snake snake = new Snake();
    private final Food food = new Food();
    private final Bomb bomb = new Bomb();
    private final Score score = new Score();
    private final JFrame frame;
    private Image background;
    private boolean isGameRestarted = false;

    public GamePanel(JFrame frame) {
        this.frame = frame;
        this.setPreferredSize(new Dimension(SnakeGame.WINDOW_SIZE, SnakeGame.WINDOW_SIZE));
        this.setFocusable(true);

        // Load the background image
        loadImage();

        // Get player name
        score.setPlayerName(JOptionPane.showInputDialog(this, "Enter your name:", "Player Name", JOptionPane.QUESTION_MESSAGE));
        if (score.getPlayerName() == null || score.getPlayerName().trim().isEmpty()) {
            score.setPlayerName("Player");
        }

        resetGame();
        setupKeyBindings();

        new Timer(150, e -> {
            snake.moveSnake(this, food, bomb, score);
            repaint();
        }).start();
    }

    private void loadImage() {
        background = new ImageIcon("SnakeGraphics.jpg").getImage();
    }

    private void resetGame() {
        snake.reset();
        score.reset();
        food.spawn(snake.getSnake(), bomb.getPosition());
        bomb.spawn(snake.getSnake(), food.getPosition());

        if (!isGameRestarted) {
            JOptionPane.showMessageDialog(this, "Welcome to the Snake Game, " + score.getPlayerName() + "!\nCollect food to score points and avoid bombs!");
        }
        isGameRestarted = true;
    }

    public void gameOver(String message) {
        saveScore(score.getPlayerName(), score.getScore());

        int option = JOptionPane.showOptionDialog(this, message + "\nYour score: " + score.getScore() + "\nWould you like to play again?",
                "Game Over", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, new String[]{"Play Again", "Exit"}, "Play Again");

        if (option == JOptionPane.YES_OPTION) {
            resetGame();
        } else {
            JOptionPane.showMessageDialog(this, "Thanks for playing, " + score.getPlayerName() + "!", "Goodbye", JOptionPane.INFORMATION_MESSAGE);
            System.exit(0);
        }
    }

    private void saveScore(String playerName, int score) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("leaderboard.txt", true))) {
            writer.write(playerName + ": " + score);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupKeyBindings() {
        InputMap inputMap = this.getInputMap(WHEN_IN_FOCUSED_WINDOW);
        inputMap.put(KeyStroke.getKeyStroke("UP"), "moveUp");
        inputMap.put(KeyStroke.getKeyStroke("DOWN"), "moveDown");
        inputMap.put(KeyStroke.getKeyStroke("LEFT"), "moveLeft");
        inputMap.put(KeyStroke.getKeyStroke("RIGHT"), "moveRight");

        this.getActionMap().put("moveUp", new MoveAction(0, -SnakeGame.MOVE_STEP));
        this.getActionMap().put("moveDown", new MoveAction(0, SnakeGame.MOVE_STEP));
        this.getActionMap().put("moveLeft", new MoveAction(-SnakeGame.MOVE_STEP, 0));
        this.getActionMap().put("moveRight", new MoveAction(SnakeGame.MOVE_STEP, 0));
    }

    private class MoveAction extends AbstractAction {
        private final int newDx, newDy;

        public MoveAction(int newDx, int newDy) {
            this.newDx = newDx;
            this.newDy = newDy;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            snake.changeDirection(newDx, newDy);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw background image
        if (background != null) {
            g.drawImage(background, 0, 0, SnakeGame.WINDOW_SIZE, SnakeGame.WINDOW_SIZE, this);
        }

        // Draw checkered grid
        for (int x = 0; x < SnakeGame.WINDOW_SIZE; x += SnakeGame.GRID_SIZE) {
            for (int y = 0; y < SnakeGame.WINDOW_SIZE; y += SnakeGame.GRID_SIZE) {
                if ((x / SnakeGame.GRID_SIZE + y / SnakeGame.GRID_SIZE) % 2 == 0) {
                    g.setColor(new Color(255, 255, 255, 50)); // Light color with transparency
                } else {
                    g.setColor(new Color(200, 200, 200, 50)); // Darker color with transparency
                }
                g.fillRect(x, y, SnakeGame.GRID_SIZE, SnakeGame.GRID_SIZE);
            }
        }

        // Draw snake, food, and bomb
        snake.draw(g);
        food.draw(g);
        bomb.draw(g);

        // Display score and player name
        g.setColor(Color.BLACK);
        g.drawString("Player: " + score.getPlayerName(), 10, 20);
        g.drawString("Score: " + score.getScore(), 10, 40);
    }
}

class Snake {
    private final ArrayList<Point> snake = new ArrayList<>();
    private int dx = 0, dy = 0;

    public void reset() {
        snake.clear();
        snake.add(new Point(SnakeGame.WINDOW_SIZE / 2, SnakeGame.WINDOW_SIZE / 2));
        dx = 0;
        dy = 0;
    }

    public ArrayList<Point> getSnake() {
        return snake;
    }

    public void changeDirection(int newDx, int newDy) {
        if ((dx != 0 && dx == -newDx) || (dy != 0 && dy == -newDy)) return;
        dx = newDx;
        dy = newDy;
    }

    public void moveSnake(GamePanel panel, Food food, Bomb bomb, Score score) {
        if (dx == 0 && dy == 0) return;

        Point head = snake.get(0);
        Point newHead = new Point(head.x + dx, head.y + dy);

        if (newHead.x < 0 || newHead.y < 0 || newHead.x >= SnakeGame.WINDOW_SIZE || newHead.y >= SnakeGame.WINDOW_SIZE) {
            panel.gameOver("The snake collided with a wall!");
            return;
        }

        if (snake.contains(newHead)) {
            panel.gameOver("The snake bit itself!");
            return;
        }

        if (newHead.equals(bomb.getPosition())) {
            panel.gameOver("The snake ate a bomb!");
            return;
        }

        snake.add(0, newHead);
        if (newHead.equals(food.getPosition())) {
            score.increment();
            food.spawn(snake, bomb.getPosition());
            bomb.spawn(snake, food.getPosition());
        } else {
            snake.remove(snake.size() - 1);
        }
    }

    public void draw(Graphics g) {
        for (int i = 0; i < snake.size(); i++) {
            Point segment = snake.get(i);

            if (i == 0) {
                g.setColor(new Color(0, 255, 255));
                g.fillOval(segment.x, segment.y, SnakeGame.GRID_SIZE, SnakeGame.GRID_SIZE);

                g.setColor(Color.BLACK);
                g.fillOval(segment.x + 6, segment.y + 6, 6, 6);
                g.fillOval(segment.x + 16, segment.y + 6, 6, 6);
            } else {
                g.setColor(new Color(0, 200, 200));
                g.fillRoundRect(segment.x, segment.y, SnakeGame.GRID_SIZE, SnakeGame.GRID_SIZE, 10, 10);
            }
        }
    }
}

class Food {
    private Point position;

    public Point getPosition() {
        return position;
    }

    public void spawn(ArrayList<Point> snake, Point bomb) {
        Random random = new Random();
        int x, y;
        do {
            x = random.nextInt(SnakeGame.WINDOW_SIZE / SnakeGame.GRID_SIZE) * SnakeGame.GRID_SIZE;
            y = random.nextInt(SnakeGame.WINDOW_SIZE / SnakeGame.GRID_SIZE) * SnakeGame.GRID_SIZE;
        } while (snake.contains(new Point(x, y)) || (bomb != null && bomb.equals(new Point(x, y))));
        position = new Point(x, y);
    }

    public void draw(Graphics g) {
        g.setColor(Color.RED);
        g.fillOval(position.x + 5, position.y + 5, SnakeGame.GRID_SIZE - 10, SnakeGame.GRID_SIZE - 10);
        g.setColor(Color.GREEN);
        g.fillRect(position.x + 12, position.y, 5, 10);
    }
}

class Bomb {
    private Point position;

    public Point getPosition() {
        return position;
    }

    public void spawn(ArrayList<Point> snake, Point food) {
        Random random = new Random();
        int x, y;
        do {
            x = random.nextInt(SnakeGame.WINDOW_SIZE / SnakeGame.GRID_SIZE) * SnakeGame.GRID_SIZE;
            y = random.nextInt(SnakeGame.WINDOW_SIZE / SnakeGame.GRID_SIZE) * SnakeGame.GRID_SIZE;
        } while (snake.contains(new Point(x, y)) || (food != null && food.equals(new Point(x, y))));
        position = new Point(x, y);
    }

    public void draw(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillOval(position.x + 5, position.y + 5, SnakeGame.GRID_SIZE - 10, SnakeGame.GRID_SIZE - 10);
        g.setColor(Color.ORANGE);
        g.drawLine(position.x + SnakeGame.GRID_SIZE / 2, position.y, position.x + SnakeGame.GRID_SIZE / 2, position.y - 10);
    }
}

class Score {
    private int score = 0;
    private String playerName;

    public int getScore() {
        return score;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public void reset() {
        score = 0;
    }

    public void increment() {
        score++;
    }
}
