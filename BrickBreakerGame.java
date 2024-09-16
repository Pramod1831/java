import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.Random;

public class BrickBreakerGame extends JPanel implements java.awt.event.KeyListener{
    public static final int WIDTH=800;
    public static final int HEIGHT=600;
    public static final int PADDLE_WIDTH=200;
    public static final int PADDLE_HEIGHT=50;
    public static final int BALL_SIZE=30;

    private static final String HIGHSCORE_FILE = "highscore.txt";
    

    //position of the ball and rectangle box.

    private int paddleX = WIDTH / 2 - PADDLE_WIDTH / 2;
    private int ballX = WIDTH / 2 - BALL_SIZE / 2;
    private int ballY = HEIGHT / 2 - BALL_SIZE / 2;
    private int ballDX = 2;
    private int ballDY = -2;
    private int score = 0;
    private int lives=3;
    private int highScore;

    private boolean isRunning = true;

    public BrickBreakerGame(){
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.ORANGE);
        setFocusable(true);
        addKeyListener(this);
        
        // Start the game loop
        Thread gameLoop = new Thread(this::runGame);
        
        gameLoop.start();
        randomizeBallDirection();
    }

    @Override
    public void keyTyped(KeyEvent e) {
        
    }

    @Override
    public void keyPressed(KeyEvent e) {
        
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_LEFT) {
            paddleX -= 40;
            if(paddleX<0)
                paddleX=0;
        } else if (key == KeyEvent.VK_RIGHT) {
            paddleX += 40;
            if (paddleX > WIDTH - PADDLE_WIDTH) // Adjust paddle position to stay within screen bounds
                paddleX = WIDTH - PADDLE_WIDTH;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        
    }

    public void runGame(){
        while (isRunning) {
            // Update game state
            update();

            // Render game
            repaint();

            // Pause briefly to control frame rate
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void update() {
        ballX += ballDX;
        ballY += ballDY;
    
        if (ballX <= 0 || ballX >= WIDTH - BALL_SIZE) {
            ballDX = -ballDX;
        }
        if (ballY <= 0) {
            ballDY = -ballDY;
        } else if (ballY >= HEIGHT - BALL_SIZE) {
            // Player lost a life
            lives--;
            if (lives <= 0) {
                // Game over
                isRunning = false;
                if (score > highScore) {
                    highScore = score;
                    saveHighScore();
                }
                // Reset the game
                resetGame();
            } else {
                // Reset ball position and direction
                ballX = WIDTH / 2 - BALL_SIZE / 2;
                ballY = HEIGHT / 2 - BALL_SIZE / 2;
                randomizeBallDirection();
            }
        }
    
        // Collision detection with paddle
        if (ballY + BALL_SIZE >= HEIGHT - PADDLE_HEIGHT &&
                ballX + BALL_SIZE >= paddleX &&
                ballX <= paddleX + PADDLE_WIDTH) {
            ballDY = -ballDY;
            score++;
            ballDX *= 1.50;
            ballDY *= 1.50;
        }
    }
    
    private void resetGame() {
        score = 0;
        lives = 3;
        ballX = WIDTH / 2 - BALL_SIZE / 2;
        ballY = HEIGHT / 2 - BALL_SIZE / 2;
        randomizeBallDirection();
    }

    private void randomizeBallDirection() {
        // Randomly set ball direction (either left or right)
        Random random=new Random();
        ballDX = random.nextBoolean() ? 2 : -2;
        // Randomly set ball direction (either up or down)
        ballDY = random.nextBoolean() ? 2 : -2;
       
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw paddle
        g.setColor(Color.BLACK);
        g.fillRect(paddleX, HEIGHT - PADDLE_HEIGHT, PADDLE_WIDTH, PADDLE_HEIGHT);

        // Draw ball
        g.setColor(Color.RED);
        g.fillOval(ballX, ballY, BALL_SIZE, BALL_SIZE);

        g.setColor(Color.BLACK);
        g.setFont(new Font("Garamond", Font.BOLD, 30));
        g.drawString("Score: " + score, 20, 30);

        // Draw lives
        g.drawString("Lives: " + lives, WIDTH - 120, 30);

        //Draw HighScore
        g.drawString("High Score: "+highScore, 20,60);

        // Draw game over message if the game is over
        if (!isRunning) {
            g.setColor(Color.RED);
            g.setFont(new Font("Garamond", Font.BOLD, 50));
            g.drawString("Game Over", WIDTH / 2 - 120, HEIGHT / 2);
        }
    }

    @SuppressWarnings("unused")
    private void loadHighScore() {
        try (BufferedReader reader = new BufferedReader(new FileReader(HIGHSCORE_FILE))) {
            String line = reader.readLine();
            if (line != null && !line.isEmpty()) {
                highScore = Integer.parseInt(line);
                System.out.println("High score loaded: " + highScore); // Add this line to print the loaded high score
            } else {
                System.out.println("No high score found in the file.");
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error loading high score: " + e.getMessage());
        }
    }

    @SuppressWarnings("unused")
    private void saveHighScore() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(HIGHSCORE_FILE))) {
            writer.write(String.valueOf(highScore));
            System.out.println("High score saved: " + highScore);
        } catch (IOException e) {
            System.err.println("Error saving high score: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        
        BrickBreakerGame game = new BrickBreakerGame();
        game.loadHighScore(); // Load high score before the game starts
    
        JFrame frame = new JFrame("Brick Breaker");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.add(game); // Use the same instance of BrickBreakerGame
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    
}
