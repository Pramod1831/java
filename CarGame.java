package build;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Random;

public class CarGame extends JPanel implements ActionListener, KeyListener {
    private Timer timer;
    private Car playerCar;
    private ArrayList<Car> otherCars;
    private int score;
    private int speed;
    private long lastSpeedIncreaseTime;
    private long lastCarSpawnTime;
    private int spawnInterval;
    private boolean isPaused;
    
    private JButton pauseButton;
    private JButton resumeButton;

    private static final int[] LANE_X_COORDINATES = {80, 215, 350};
    private static final int LANE_WIDTH = 110;

    public CarGame() {
        setPreferredSize(new Dimension(480, 800));
        setBackground(Color.BLACK);
        setFocusable(true);
        setLayout(null); // Use null layout for absolute positioning
        addKeyListener(this);

        playerCar = new Car(LANE_X_COORDINATES[1], 600, Color.RED);
        otherCars = new ArrayList<>();
        timer = new Timer(30, this);
        score = 0;
        speed = 5;
        spawnInterval = 2000;
        lastSpeedIncreaseTime = System.currentTimeMillis();
        lastCarSpawnTime = System.currentTimeMillis();
        isPaused = false;

        // Add pause and resume buttons
        pauseButton = new JButton("Pause");
        resumeButton = new JButton("Resume");
        pauseButton.setBounds(380, 10, 80, 30);
        resumeButton.setBounds(380, 50, 80, 30);
        resumeButton.setEnabled(false); // Disable resume button initially

        pauseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                isPaused = true;
                pauseButton.setEnabled(false);
                resumeButton.setEnabled(true);
                repaint();
            }
        });

        resumeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                isPaused = false;
                pauseButton.setEnabled(true);
                resumeButton.setEnabled(false);
                repaint();
            }
        });

        add(pauseButton);
        add(resumeButton);

        timer.start();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawLanes(g);
        playerCar.draw(g);
        for (Car car : otherCars) {
            car.draw(g);
        }
        g.setColor(Color.WHITE);
        g.drawString("Score: " + score, 50, 20);
        if (isPaused) {
            g.drawString("Paused", getWidth() / 2 - 30, getHeight() / 2);
        }
    }

    private void drawLanes(Graphics g) {
        g.setColor(Color.WHITE);
        
        // Draw side strips
        g.fillRect(10, 0, 10, getHeight());
        g.fillRect(400, 0, 10, getHeight());

        // Draw center lane dividers
        for (int laneCenter : LANE_X_COORDINATES) {
            for (int y = 0; y < getHeight(); y += 40) {
                g.fillRect(laneCenter + LANE_WIDTH / 2, y, 5, 25);
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (isPaused) {
            return;
        }

        playerCar.move();
        if (System.currentTimeMillis() - lastSpeedIncreaseTime >= 30000) {
            speed += 2;
            spawnInterval = Math.max(spawnInterval - 200, 500); // Decrease spawn interval
            lastSpeedIncreaseTime = System.currentTimeMillis();
        }
        if (System.currentTimeMillis() - lastCarSpawnTime >= spawnInterval) {
            spawnCars();
            lastCarSpawnTime = System.currentTimeMillis();
        }
        for (Car car : otherCars) {
            car.y += speed;
        }
        otherCars.removeIf(car -> car.y > 800);
        checkCollisions();
        score++;
        repaint();
    }

    private void spawnCars() {
        Random rand = new Random();
        int lane1 = LANE_X_COORDINATES[rand.nextInt(LANE_X_COORDINATES.length)];
        otherCars.add(new Car(lane1, 0, Color.ORANGE));
    }

    private void checkCollisions() {
        for (Car car : otherCars) {
            if (playerCar.getBounds().intersects(car.getBounds())) {
                timer.stop();
                JOptionPane.showMessageDialog(this, "Game Over! Final Score: " + score);
                System.exit(0);
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            movePlayerToLeftLane();
        }
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            movePlayerToRightLane();
        }
        if (e.getKeyCode() == KeyEvent.VK_P) {
            isPaused = !isPaused;
            pauseButton.setEnabled(!isPaused);
            resumeButton.setEnabled(isPaused);
            repaint();
        }
    }

    private void movePlayerToLeftLane() {
        for (int i = 1; i < LANE_X_COORDINATES.length; i++) {
            if (playerCar.x == LANE_X_COORDINATES[i]) {
                playerCar.x = LANE_X_COORDINATES[i - 1];
                break;
            }
        }
    }

    private void movePlayerToRightLane() {
        for (int i = 0; i < LANE_X_COORDINATES.length - 1; i++) {
            if (playerCar.x == LANE_X_COORDINATES[i]) {
                playerCar.x = LANE_X_COORDINATES[i + 1];
                break;
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame frame = new JFrame("Pramod World");
        CarGame game = new CarGame();
        frame.add(game);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}

class Car {
    int x, y, dx;
    Color color;

    public Car(int x, int y, Color color) {
        this.x = x;
        this.y = y;
        this.color = color;
        this.dx = 0;
    }

    public void move() {
        x += dx;
        if (x < 0) x = 0;
        if (x > 430) x = 430;
    }

    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(color);

        // Draw car body
        g2d.fillRoundRect(x, y, 40, 100, 10, 10);

        // Draw car details
        g2d.setColor(Color.BLACK);
        g2d.fillRect(x + 5, y + 10, 30, 20); // Windshield
        g2d.fillRect(x + 5, y + 70, 30, 20); // Rear window

        g2d.setColor(Color.GRAY);
        g2d.fillRect(x + 10, y + 35, 20, 30); // Roof

        // Draw wheels
        g2d.setColor(Color.BLACK);
        g2d.fillOval(x - 5, y + 10, 10, 20);
        g2d.fillOval(x + 35, y + 10, 10, 20);
        g2d.fillOval(x - 5, y + 70, 10, 20);
        g2d.fillOval(x + 35, y + 70, 10, 20);
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, 40, 100);
    }
}
