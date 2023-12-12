import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.io.File;
import java.io.IOException;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

class Meal {
    String name;
    double price;

    public Meal(String name, double price) {
        this.name = name;
        this.price = price;
    }

    public double calculateTotal(double quantity) {
        return quantity * price;
    }

    @Override
    public String toString() {
        return name;
    }
}

class BackgroundPanel extends JPanel {
    private Image backgroundImage;

    public BackgroundPanel(String imagePath) {
        this.backgroundImage = new ImageIcon(imagePath).getImage();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
    }
}

class TextElementStyler {
    public static JLabel createStyledLabel(String text, Font font, Color gradientColor) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(gradientColor);
        return label;
    }

    public static JLabel createStyledLabelWithOutline(String text, Font font, Color textColor, Color outlineColor) {
    	JLabel label = new JLabel(text);
    	label.setFont(font);

    	label.setForeground(textColor);

    	label.setBorder(BorderFactory.createLineBorder(outlineColor, 2)); // Add border for the outline

    	return label;
    }

    public static JButton createStyledButton(String text, Font font, Color gradientColor) {
        JButton button = new JButton(text);
        button.setFont(font);
        button.setForeground(gradientColor);
        button.setFocusPainted(false);
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        return button;
    }
}

class LoadingScreen extends JWindow {
    private Timer loadingTimer;
    private JProgressBar progressBar;

    public LoadingScreen() {
        setLayout(new BorderLayout());
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        add(progressBar, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);

        loadingTimer = new Timer(3000, new ActionListener() { // Adjust the duration as needed
            @Override
            public void actionPerformed(ActionEvent e) {
                LoadingScreen.this.setVisible(false);
                LoadingScreen.this.dispose();
            }
        });
    }

    public void startLoading() {
        loadingTimer.start();
        setVisible(true);
    }
}

public class POSApp extends JFrame {
	private Clip clip;
    private JComboBox<Meal> mealComboBox;
    private JTextField quantityField;
    private JComboBox<String> customerComboBox;
    private JButton calculateButton;
    private JTextField totalField;
    private JTextField paymentField;
    private JTextField changeField;

    private JPanel startMenuPanel;
    private JButton startButton;

    private static final DecimalFormat df = new DecimalFormat("0.00");
    private Timer flashingTimer;

    private boolean transitionOccurred = false;
    private LoadingScreen loadingScreen;  // Add LoadingScreen reference

    public POSApp() {
        setTitle("Dine-In Delight | Point of Sale System");
        setSize(640, 368);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLayout(null);

     // Initialize the loading screen
        loadingScreen = new LoadingScreen();
        loadingScreen.startLoading();  // Show loading screen initially

        initializeStartMenu();

        // Show the start menu initially
        showStartMenu();

        // Handle key press to switch from start menu to POS menu
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (!transitionOccurred) {
                	loadingScreen.startLoading(); // Show loading screen during transition

                	// Switch to the POS menu when any key is pressed
                    startButton.setVisible(false);  // Hide the start button
                    remove(startMenuPanel);
                    initializeComponents(); // Initialize components
                    addComponents();        // Add POS components
                    revalidate();
                    repaint();

                    // Set focus on the POS menu for key events to work
                    mealComboBox.requestFocusInWindow();

                    // Set the transition flag
                    transitionOccurred = true;

                    // Remove the key listener
                    POSApp.this.removeKeyListener(this);
                }
            }
        });
    }

    private void initializeStartMenu() {
        startMenuPanel = new BackgroundPanel("Restaurant.gif");

        // Use BorderLayout for centering components
        startMenuPanel.setLayout(new BorderLayout());

        // Create a custom font
        Font cooperFont = new Font("Cooper", Font.BOLD, 36);

        JLabel titleLabel = TextElementStyler.createStyledLabelWithOutline("Dine-In Delight", cooperFont, Color.WHITE, new Color(128, 0, 128));
        titleLabel.setHorizontalAlignment(JLabel.CENTER);

        startButton = TextElementStyler.createStyledButton("Do you want to play the game?", new Font("Cooper", Font.PLAIN, 18), Color.WHITE);
        startButton.setBorder(BorderFactory.createLineBorder(new Color(128, 0, 128), 2));
        startButton.addActionListener(e -> handleStartButtonClick());

        // Add the title and button to the start menu panel
        startMenuPanel.add(titleLabel, BorderLayout.CENTER);
        startMenuPanel.add(startButton, BorderLayout.SOUTH);

        // Initialize flashing timer
        flashingTimer = new Timer(500, e -> startButton.setVisible(!startButton.isVisible()));
        flashingTimer.start();
    }

    private void showStartMenu() {
        setContentPane(startMenuPanel);
        revalidate();
        repaint();

        loadingScreen.setVisible(false); // Hide loading screen when showing start menu
    }

    private void initializeComponents() {
        Meal mealA = new Meal("Meal A", 100.00);
        Meal mealB = new Meal("Meal B", 150.00);

        mealComboBox = new JComboBox<>(new Meal[]{mealA, mealB});

        quantityField = new JTextField(10);

        customerComboBox = new JComboBox<>(new String[]{"Regular", "Senior Citizen"});

        calculateButton = TextElementStyler.createStyledButton("Calculate", new Font("Cooper", Font.PLAIN, 18), Color.WHITE);
        calculateButton.addActionListener(e -> {
        	calculateTotal();
        });

        totalField = new JTextField(15);

        paymentField = new JTextField(15);

        changeField = new JTextField(15);
        changeField.setEditable(false);
    }

    private void addComponents() {
        setLayout(new FlowLayout(FlowLayout.CENTER));

        // Create a JPanel to hold the components
        JPanel componentsPanel = new BackgroundPanel("Restaurant.gif"); // Make the panel transparent
        componentsPanel.setLayout(new GridLayout(2, 2, 10, 10)); // Adjust spacing as needed

        // First Row
        JPanel firstRowPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        firstRowPanel.setBackground(new Color(139, 69, 19));
        JLabel menuLabel = TextElementStyler.createStyledLabel("Menu:", new Font("Cooper", Font.PLAIN, 18), Color.WHITE);
        firstRowPanel.add(menuLabel);
        firstRowPanel.add(mealComboBox);

        JLabel quantityLabel = TextElementStyler.createStyledLabel("Enter Quantity:", new Font("Cooper", Font.PLAIN, 18), Color.WHITE);
        firstRowPanel.add(quantityLabel);
        firstRowPanel.add(quantityField);

        // Second Row
        JPanel secondRowPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        secondRowPanel.setBackground(new Color(139, 69, 19));
        secondRowPanel.add(customerComboBox);

        JLabel totalLabel = TextElementStyler.createStyledLabel("Total Order:", new Font("Cooper", Font.PLAIN, 18), Color.WHITE);
        secondRowPanel.add(totalLabel);
        secondRowPanel.add(totalField);

        // Add rows to the main panel
        componentsPanel.add(firstRowPanel);
        componentsPanel.add(secondRowPanel);

        // Column under it
        JPanel columnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        columnPanel.setBackground(new Color(139, 69, 19));
        JLabel paymentLabel = TextElementStyler.createStyledLabel("Enter Payment:", new Font("Cooper", Font.PLAIN, 18), Color.WHITE);
        columnPanel.add(paymentLabel);
        columnPanel.add(paymentField);

        // Calculate Button (Purple Box)
        JPanel calculateButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        calculateButtonPanel.setBackground(new Color(255, 165, 0)); // Purple background
        calculateButtonPanel.add(calculateButton);

        // Change Field
        JPanel changePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        changePanel.setBackground(new Color(139, 69, 19));
        JLabel changeLabel = TextElementStyler.createStyledLabel("Change:", new Font("Cooper", Font.PLAIN, 18), Color.WHITE);
        changePanel.add(changeLabel);
        changePanel.add(changeField);

        // Add column components
        componentsPanel.add(columnPanel);
        componentsPanel.add(calculateButtonPanel);
        componentsPanel.add(changePanel);

        // Add the componentsPanel to the main frame
        add(componentsPanel);
    }

    private void calculateTotal() {
        try {
            Meal selectedMeal = (Meal) mealComboBox.getSelectedItem();
            double quantity = Double.parseDouble(quantityField.getText());
            String customerType = (String) customerComboBox.getSelectedItem();

            double totalOrder;
            if (customerType.equals("Senior Citizen")) {
                totalOrder = selectedMeal.calculateTotal(quantity) - (selectedMeal.calculateTotal(quantity) * 0.20);
            } else {
                totalOrder = selectedMeal.calculateTotal(quantity);
            }

            totalField.setText(df.format(totalOrder));

            double payment = Double.parseDouble(paymentField.getText());
            double change = payment - totalOrder;
            changeField.setText(df.format(change));
         // Ask the player if they want to play again
            int playAgainChoice = JOptionPane.showOptionDialog(
                    this,
                    "Do you want to play again?",
                    "Play Again",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    null,
                    null);

            if (playAgainChoice == JOptionPane.YES_OPTION) {
                // Reset fields and go back to the POS screen
                quantityField.setText("");
                paymentField.setText("");
                changeField.setText("");
                mealComboBox.setSelectedIndex(0);
                customerComboBox.setSelectedIndex(0);
                mealComboBox.requestFocusInWindow();
            } else {
                // Game over, exit the program
                gameOver();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid input. Please enter valid numbers.");
        }
    }

    private void gameOver() {
    	stopSound();
        JOptionPane.showMessageDialog(this, "Game Over. Thank you for playing!");
        System.exit(0);
    }

    private void handleStartButtonClick() {
        flashingTimer.stop();

        int choice = JOptionPane.showOptionDialog(
                this,
                "Do you want to play the game?",
                "Game Start Confirmation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                null,
                null);

        if (choice == JOptionPane.YES_OPTION) {
            // Continue with POS screen setup
            startButton.setVisible(false);
            remove(startMenuPanel);
            initializeComponents();
            addComponents();
            revalidate();
            repaint();
            mealComboBox.requestFocusInWindow();
            playCaffeineSound("Blue Wednesday & Felty - Caffeine.wav");

            loadingScreen.startLoading(); // Show loading screen before asking the user to play the game

        } else {
            // Exit the game when the user selects "No"
            System.exit(0);
        }

        startButton.setForeground(Color.WHITE);
        startButton.setBorder(BorderFactory.createLineBorder(new Color(128, 0, 128), 2));
    }

    private void setCustomMousePointer( ) {
    	Toolkit toolkit = Toolkit.getDefaultToolkit();
    	Image cursorImage = new ImageIcon("Mouse Pointer.png").getImage();
    	Point hotSpot = new Point(0, 0);

    	Cursor customCursor = toolkit.createCustomCursor(cursorImage, hotSpot, "CustomCursor");

    	addMouseListener(new MouseAdapter() {
    		@Override
    		public void mouseClicked(MouseEvent e) {
    			playMouseClickSound("Mouse Click.wav");
    		}
    	});

    	setCursor(customCursor);
    }

    private void playCaffeineSound(String filePath) {
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(filePath).getAbsoluteFile());
            Clip clip = AudioSystem.getClip();

            clip.open(audioInputStream);

            // Loop the clip
            clip.loop(Clip.LOOP_CONTINUOUSLY);

            // Start playing
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    private void playMouseClickSound(String filePath) {
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(filePath).getAbsoluteFile());
            Clip clip = AudioSystem.getClip();

            clip.open(audioInputStream);

            // Start playing (no looping)
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    private void stopSound() {
        // Stop the currently playing sound
        if (clip != null && clip.isRunning()) {
            clip.stop();
            clip.close();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
        POSApp posApp = new POSApp();
        posApp.setCustomMousePointer();
        posApp.setVisible(true);
        posApp.loadingScreen.startLoading(); // Show loading screen initially
    	});
    }
}
