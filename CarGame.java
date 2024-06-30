package build;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
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
    private int highScore;

    private JButton pauseButton;
    private JButton resumeButton;
    private JButton settingsButton;
    private JButton restartButton;
    private JButton quitButton;

    private Clip backgroundClip;
    private Clip collisionClip;
    private Clip sideClip;
    private Clip machineClip;
    private Clip hornClip;

    @SuppressWarnings("unused")
    private FloatControl backgroundVolumeControl;
    @SuppressWarnings("unused")
    private FloatControl collisionVolumeControl;
    @SuppressWarnings("unused")
    private FloatControl sideVolumeControl;
    @SuppressWarnings("unused")
    private FloatControl machineVolumeControl;
    @SuppressWarnings("unused")
    private FloatControl hornVolumeControl;

    private static final int[] LANE_X_COORDINATES = {80, 215, 350};
    private static final int LANE_WIDTH = 110;

    private static final String HIGHSCORE_FILE = "CarGameHighScore.txt";
    private static final String SAVE_FILE = "CarGameSave.txt";

    public CarGame() {
        setPreferredSize(new Dimension(410, 800));
        setBackground(Color.BLACK);
        setFocusable(true);
        setLayout(null); // Use null layout for absolute positioning
        addKeyListener(this);

        playerCar = new Car(LANE_X_COORDINATES[1], 600, Color.RED);
        otherCars = new ArrayList<>();
        timer = new Timer(30, this);
        score = 0;
        speed = 5;
        spawnInterval = 1900;
        lastSpeedIncreaseTime = System.currentTimeMillis();
        lastCarSpawnTime = System.currentTimeMillis();
        isPaused = false;

        // Add buttons
        pauseButton = new JButton("||");
        pauseButton.setBounds(340, 10, 40, 30);
        pauseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pauseGame();
            }
        });

        resumeButton = new JButton("Resume");
        resumeButton.setBounds(150, 200, 100, 30);
        resumeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resumeGame();
            }
        });

        restartButton = new JButton("Restart");
        restartButton.setBounds(150, 240, 100, 30);
        restartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                restartGame();
            }
        });

        quitButton = new JButton("Quit");
        quitButton.setBounds(150, 280, 100, 30);
        quitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                quitGame();
            }
        });

        settingsButton = new JButton("Settings");
        settingsButton.setBounds(150, 320, 100, 30);
        settingsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showSettings();
            }
        });

        add(pauseButton);
        add(resumeButton);
        add(restartButton);
        add(quitButton);
        add(settingsButton);

        resumeButton.setVisible(false);
        restartButton.setVisible(false);
        quitButton.setVisible(false);
        settingsButton.setVisible(false);

        loadHighScore();
        prepareSounds();
        playBackgroundMusic();
        playMachineSound();
        timer.start();
    }

    private void prepareSounds() {
        backgroundClip = loadClip("/build/sounds/background.wav");
        setVolume(backgroundClip, -5.0f); // Reduce background volume
        collisionClip = loadClip("/build/sounds/collision.wav");
        sideClip = loadClip("/build/sounds/side.wav");
        machineClip = loadClip("/build/sounds/machine.wav");
        setVolume(machineClip, -10.0f); //  machine volume
        hornClip = loadClip("/build/sounds/carhorn.wav");
    }

    private Clip loadClip(String path) {
        try {
            InputStream audioSrc = getClass().getResourceAsStream(path);
            InputStream bufferedIn = new BufferedInputStream(audioSrc);
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(bufferedIn);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            return clip;
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void setVolume(Clip clip, float volume) {
        if (clip != null && clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl control = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            control.setValue(volume);
        }
    }

    private void playBackgroundMusic() {
        if (backgroundClip != null) {
            backgroundClip.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    private void playCollisionSound() {
        if (collisionClip != null) {
            collisionClip.stop(); // Stop any previous play
            collisionClip.setFramePosition(0); // Rewind to the beginning
            collisionClip.start();
        }
    }

    private void playSideSound() {
        if (sideClip != null) {
            sideClip.stop(); // Stop any previous play
            sideClip.setFramePosition(0); // Rewind to the beginning
            sideClip.start();
        }
    }

    private void playMachineSound() {
        if (machineClip != null) {
            machineClip.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    private void playHornSound() {
        if (hornClip != null) {
            hornClip.stop(); // Stop any previous play
            hornClip.setFramePosition(0); // Rewind to the beginning
            hornClip.start();
        }
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
        g.drawString("Score: " + score, 40, 20);
        g.drawString("High Score: " + highScore, 40, 35);
        if (isPaused) {
            g.drawString("Paused", getWidth() / 2 - 30, getHeight() / 2);
            drawCrashEffect(g); // Draw crash effect if paused due to collision
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

    private void drawCrashEffect(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.RED);
        g2d.setStroke(new BasicStroke(5));
        for (Car car : otherCars) {
            if (playerCar.getBounds().intersects(car.getBounds())) {
                int centerX = (playerCar.x + car.x) / 2 + 20;
                int centerY = (playerCar.y + car.y) / 2 + 50;
                g2d.drawLine(centerX - 20, centerY - 20, centerX + 20, centerY + 20);
                g2d.drawLine(centerX + 20, centerY - 20, centerX - 20, centerY + 20);
            }
        }
    }

    private void loadHighScore() {
        try (BufferedReader reader = new BufferedReader(new FileReader(HIGHSCORE_FILE))) {
            String line = reader.readLine();
            if (line != null && !line.isEmpty()) {
                highScore = Integer.parseInt(line);
                System.out.println("High score loaded: " + highScore);
            } else {
                System.out.println("No high score found in the file.");
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error loading high score: " + e.getMessage());
        }
    }

    private void saveHighScore() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(HIGHSCORE_FILE))) {
            writer.write(String.valueOf(highScore));
            System.out.println("High score saved: " + highScore);
        } catch (IOException e) {
            System.err.println("Error saving high score: " + e.getMessage());
        }
    }

    private void saveGameState() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(SAVE_FILE))) {
            writer.write(score + "\n");
            writer.write(speed + "\n");
            writer.write(playerCar.x + "\n");
            writer.write(playerCar.y + "\n");
            for (Car car : otherCars) {
                writer.write(car.x + " " + car.y + "\n");
            }
            System.out.println("Game state saved.");
        } catch (IOException e) {
            System.err.println("Error saving game state: " + e.getMessage());
        }
    }

    private void loadGameState() {
        try (BufferedReader reader = new BufferedReader(new FileReader(SAVE_FILE))) {
            score = Integer.parseInt(reader.readLine());
            speed = Integer.parseInt(reader.readLine());
            playerCar.x = Integer.parseInt(reader.readLine());
            playerCar.y = Integer.parseInt(reader.readLine());
            otherCars.clear();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" ");
                int x = Integer.parseInt(parts[0]);
                int y = Integer.parseInt(parts[1]);
                otherCars.add(new Car(x, y, Color.ORANGE));
            }
            System.out.println("Game state loaded.");
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error loading game state: " + e.getMessage());
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
                playCollisionSound();
                if (score > highScore) {
                    highScore = score;
                    saveHighScore();
                }
                isPaused = true; // Pause the game on collision
                repaint(); // Trigger repaint to show crash effect

                int response = JOptionPane.showOptionDialog(this, "Game Over! Final Score: " + score, "Game Over",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null,
                        new String[]{"Restart", "Quit"}, "Restart");
                if (response == 0) {
                    restartGame();
                } else {
                    quitGame();
                }
                break; // Exit loop after collision is detected
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            movePlayerToLeftLane();
            playSideSound();
        }
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            movePlayerToRightLane();
            playSideSound();
        }
        if (e.getKeyCode() == KeyEvent.VK_P) {
            pauseGame();
        }
        if (e.getKeyCode() == KeyEvent.VK_S) {
            saveGameState();
        }
        if (e.getKeyCode() == KeyEvent.VK_L) {
            loadGameState();
            repaint();
        }
        if (e.getKeyCode() == KeyEvent.VK_H) {
            playHornSound();
        }
    }

    private void pauseGame() {
        isPaused = true;
        pauseButton.setEnabled(false);
        resumeButton.setVisible(true);
        restartButton.setVisible(true);
        quitButton.setVisible(true);
        settingsButton.setVisible(true);
        repaint();
    }

    private void resumeGame() {
        isPaused = false;
        pauseButton.setEnabled(true);
        resumeButton.setVisible(false);
        restartButton.setVisible(false);
        quitButton.setVisible(false);
        settingsButton.setVisible(false);
        repaint();
    }

    private void restartGame() {
        timer.stop();
        score = 0;
        speed = 5;
        otherCars.clear();
        playerCar.x = LANE_X_COORDINATES[1];
        playerCar.y = 600;
        lastSpeedIncreaseTime = System.currentTimeMillis();
        lastCarSpawnTime = System.currentTimeMillis();
        isPaused = false;
        resumeButton.setVisible(false);
        restartButton.setVisible(false);
        quitButton.setVisible(false);
        settingsButton.setVisible(false);
        playBackgroundMusic();
        playMachineSound();
        timer.start();
    }

    private void quitGame() {
        System.exit(0);
    }

    private void showSettings() {
        JPanel panel = new JPanel(new GridLayout(5, 1));

        JSlider backgroundSlider = createVolumeSlider(backgroundClip);
        JSlider collisionSlider = createVolumeSlider(collisionClip);
        JSlider sideSlider = createVolumeSlider(sideClip);
        JSlider machineSlider = createVolumeSlider(machineClip);
        JSlider hornSlider = createVolumeSlider(hornClip);

        panel.add(new JLabel("Background Volume"));
        panel.add(backgroundSlider);
        panel.add(new JLabel("Collision Volume"));
        panel.add(collisionSlider);
        panel.add(new JLabel("Side Volume"));
        panel.add(sideSlider);
        panel.add(new JLabel("Machine Volume"));
        panel.add(machineSlider);
        panel.add(new JLabel("Horn Volume"));
        panel.add(hornSlider);

        JOptionPane.showMessageDialog(this, panel, "Adjust Volume", JOptionPane.QUESTION_MESSAGE);
    }

    private JSlider createVolumeSlider(Clip clip) {
        JSlider slider = new JSlider(-80, 6);
        if (clip != null && clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl control = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            slider.setValue((int) control.getValue());
            slider.addChangeListener(e -> control.setValue(slider.getValue()));
        }
        return slider;
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
        g2d.setColor(Color.GRAY);
        g2d.fillRect(x + 5, y + 10, 30, 20); // Windshield
        g2d.fillRect(x + 5, y + 70, 30, 20); // Rear window

        g2d.setColor(Color.ORANGE);
        g2d.fillRect(x + 10, y + 35, 20, 30); // Roof

        // Draw wheels
        g2d.setColor(Color.WHITE);
        g2d.fillOval(x - 5, y + 10, 10, 20);
        g2d.fillOval(x + 35, y + 10, 10, 20);
        g2d.fillOval(x - 5, y + 70, 10, 20);
        g2d.fillOval(x + 35, y + 70, 10, 20);
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, 40, 100);
    }
}
