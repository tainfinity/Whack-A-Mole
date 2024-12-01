import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.AudioInputStream;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MoleGame extends JFrame implements ActionListener {
    private static final int GAME_DURATION = 10000; // Total game time in milliseconds
    private static final int TIMER_DELAY = 500; // Timer delay in milliseconds
    private static final int BOARD_SIZE = 9; // Number of buttons on the board
    private static final Color HOLE_COLOR = new Color(104, 69, 5); // Brown color for holes

    private JPanel topBoard, gameBoard;
    private JButton startBtn;
    private List<JButton> btnList;
    private Timer timer;
    private int[] moles = new int[BOARD_SIZE];
    private int gameTime = GAME_DURATION, whacked = 0, missed = 0;
    private boolean gameStarted = false;
    private JLabel statusLabel, scoreLabel;
    private Random random = new Random();
    private ImageIcon moleIcon;

    public MoleGame() {
        loadMoleImage();
        initializeUIComponents();
        configureMainFrame();
    }

    private void loadMoleImage() {
        try {
            ImageIcon originalIcon = new ImageIcon(getClass().getClassLoader().getResource("mole.png"));
            if (originalIcon == null || originalIcon.getImageLoadStatus() == MediaTracker.ERRORED) {
                throw new IllegalArgumentException("Mole image not found: mole.png");
            }
            Image scaledImage = originalIcon.getImage().getScaledInstance(125, 125, Image.SCALE_SMOOTH);
            moleIcon = new ImageIcon(scaledImage);
        } catch (Exception e) {
            System.err.println("Error loading mole image: mole.png");
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void initializeUIComponents() {
        // Top panel with Start button and status labels
        topBoard = new JPanel(new BorderLayout());
        startBtn = new JButton("Start");
        statusLabel = new JLabel("Ready to start!");
        scoreLabel = new JLabel("Score: 0");

        // Add padding to the labels for spacing
        statusLabel.setBorder(new EmptyBorder(10, 20, 10, 20));
        scoreLabel.setBorder(new EmptyBorder(10, 20, 10, 20));

        startBtn.addActionListener(e -> startGame());

        topBoard.add(startBtn, BorderLayout.WEST);
        topBoard.add(statusLabel, BorderLayout.CENTER);
        topBoard.add(scoreLabel, BorderLayout.EAST);

        // Game board panel with Grid layout
        gameBoard = new JPanel(new GridLayout(3, 3));
        btnList = IntStream.range(0, BOARD_SIZE).mapToObj(i -> {
            JButton button = new JButton();
            button.setFocusable(false);
            button.setBackground(HOLE_COLOR);
            button.addActionListener(this);
            gameBoard.add(button);
            return button;
        }).collect(Collectors.toList());

        // Timer for mole movement
        timer = new Timer(TIMER_DELAY, e -> {
            if (gameTime <= 0) {
                timer.stop();
                gameOver();
            } else {
                generateMole();
                gameTime -= TIMER_DELAY;
            }
        });
    }

    private void configureMainFrame() {
        setLayout(new BorderLayout());
        add(topBoard, BorderLayout.NORTH);
        add(gameBoard, BorderLayout.CENTER);

        setTitle("Whack-a-Mole");
        setSize(600, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void startGame() {
        // Reset game state
        whacked = missed = 0;
        gameTime = GAME_DURATION;
        gameStarted = true;
        scoreLabel.setText("Score: 0");
        statusLabel.setText("Game in progress...");
        startBtn.setEnabled(false);

        // Start the timer
        timer.start();
    }

    private void generateMole() {
        clearMoles();
        int moleIndex = random.nextInt(BOARD_SIZE);
        moles[moleIndex] = 1;
        btnList.get(moleIndex).setIcon(moleIcon);
        playSound("laugh.wav");
    }

    private void clearMoles() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            moles[i] = 0;
            JButton button = btnList.get(i);
            button.setBackground(HOLE_COLOR);
            button.setIcon(null);
        }
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        if (!gameStarted) return;

        JButton clickedButton = (JButton) evt.getSource();
        int index = btnList.indexOf(clickedButton);

        if (moles[index] == 1) {
            whacked++;
            scoreLabel.setText("Score: " + whacked);
            statusLabel.setText("Whacked a mole!");
            playSound("whack.wav");
            clearMoles();
            System.out.println("Whacked! Current score: " + whacked);
        } else {
            missed++;
            statusLabel.setText("Missed!");
        }
    }

    private void gameOver() {
        clearMoles();
        gameStarted = false;
        startBtn.setEnabled(true);
        statusLabel.setText("Game Over!");
        playSound("gameover.wav");

        String message = "Game Over!\nYou whacked " + whacked + " moles and missed " + missed + " times.";
        JOptionPane.showMessageDialog(this, message, "Game Over", JOptionPane.INFORMATION_MESSAGE);
        System.out.println(message);
    }

    private void playSound(String soundFile) {
        try {
            java.net.URL soundUrl = getClass().getClassLoader().getResource(soundFile);
            if (soundUrl == null) {
                throw new IllegalArgumentException("Sound file not found: " + soundFile);
            }
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundUrl);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    clip.close();
                }
            });
        } catch (Exception e) {
            System.err.println("Error playing sound: " + soundFile);
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MoleGame::new);
    }
}
