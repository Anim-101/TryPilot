import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * The main class for the Tetris game.
 * It creates the game window (JFrame) and adds the game board (Board panel) to it.
 */
public class TetrisGame extends JFrame {

    /**
     * Constructor for TetrisGame.
     * Calls the method to initialize the user interface.
     */
    public TetrisGame() {
        initUI();
    }

    /**
     * Initializes the User Interface (UI) of the game.
     * Creates a new Board panel and adds it to this JFrame.
     * Sets JFrame properties like title, default close operation, and size.
     */
    private void initUI() {
        Board board = new Board(); // Create an instance of our game board
        add(board); // Add the Board panel to the JFrame's content pane

        setTitle("Tetris"); // Set the title of the game window
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Ensure the application exits when the window is closed
        setResizable(false); // Prevent the user from resizing the game window
        pack(); // Sizes the JFrame so that all its contents (the Board panel) are at or above their preferred sizes
        setLocationRelativeTo(null); // Center the game window on the screen
    }

    /**
     * The main entry point of the application.
     * It schedules the creation and display of the game GUI on the Event Dispatch Thread (EDT).
     * @param args Command line arguments (not used in this application).
     */
    public static void main(String[] args) {
        // SwingUtilities.invokeLater ensures that GUI updates happen on the Event Dispatch Thread,
        // which is crucial for thread safety in Swing applications.
        SwingUtilities.invokeLater(() -> {
            TetrisGame game = new TetrisGame(); // Create an instance of the game
            game.setVisible(true); // Make the game window visible
        });
    }
}
