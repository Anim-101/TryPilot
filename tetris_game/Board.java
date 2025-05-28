import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * The Board class represents the main playing area for the Tetris game.
 * It handles game logic, drawing the board and pieces, and user input.
 * It extends JPanel, so it can be easily integrated into a Swing GUI.
 */
public class Board extends JPanel {
    // Constants defining the board dimensions and game speed
    private static final int BOARD_WIDTH = 10;  // Number of columns on the board
    private static final int BOARD_HEIGHT = 20; // Number of rows on the board
    private static final int BLOCK_SIZE = 30;   // Size of each Tetris block in pixels
    private static final int INITIAL_DELAY = 600; // Initial speed of falling pieces (milliseconds)
    private static final int FAST_DELAY = 50;    // Speed of falling pieces when 'Down' key is pressed

    private Timer timer; // Timer to control the game loop (piece falling)
    private boolean isFallingFinished = false; // True if the current piece has landed and game might be over
    private boolean isPaused = false;          // True if the game is currently paused
    private int score = 0;                   // Player's current score
    private int linesCleared = 0;            // Number of lines cleared

    private Tetromino currentPiece;  // The Tetromino piece currently falling
    private Point currentPosition; // Top-left (x,y) position of the currentPiece on the boardGrid

    /**
     * The main game grid. Stores the Color of each settled block.
     * A null value in a cell means it's empty.
     * Dimensions: boardGrid[row][column]
     */
    private Color[][] boardGrid;

    /**
     * Constructor for the Board.
     * Initializes the game board and starts the game.
     */
    public Board() {
        initBoard();
    }

    /**
     * Initializes the board panel properties, sets up the game grid,
     * adds a key listener for user input, and starts the game.
     */
    private void initBoard() {
        setFocusable(true); // Allows the panel to receive keyboard focus
        // Set the preferred size of the panel based on board dimensions and block size
        setPreferredSize(new Dimension(BOARD_WIDTH * BLOCK_SIZE, BOARD_HEIGHT * BLOCK_SIZE));
        setBackground(Color.BLACK); // Set the background color of the board

        // Initialize the grid with nulls (empty cells)
        boardGrid = new Color[BOARD_HEIGHT][BOARD_WIDTH];
        addKeyListener(new TAdapter()); // Add the keyboard input handler
        startGame(); // Start the actual game logic
    }

    /**
     * Resets the game state and starts a new game.
     * Clears the board, resets flags, score, spawns a new piece, and starts the timer.
     */
    private void startGame() {
        clearBoard(); // Clear any existing blocks from the grid
        isFallingFinished = false; // Reset game over flag
        isPaused = false;          // Reset pause flag
        score = 0;                 // Reset score
        linesCleared = 0;          // Reset lines cleared
        spawnNewPiece();           // Create the first piece
        // Initialize the timer to call gameLoop() at regular intervals
        timer = new Timer(INITIAL_DELAY, e -> gameLoop());
        timer.start(); // Start the timer
        repaint(); // Request an initial paint of the board
    }

    /**
     * The main game loop, called by the Timer.
     * If the game is not paused or over, it attempts to move the current piece down.
     * If the piece cannot move down, it means it has landed.
     */
    private void gameLoop() {
        if (isPaused || isFallingFinished) {
            return; // Do nothing if paused or game is over
        }
        // Try to move the piece one step down
        if (!movePiece(0, 1)) {
            pieceDropped(); // If it can't move down, the piece has landed
        }
        repaint(); // Redraw the board after any change
    }

    /**
     * Spawns a new random Tetromino at the top-center of the board.
     * If the new piece cannot be placed (collides immediately), the game is over.
     */
    private void spawnNewPiece() {
        currentPiece = Tetromino.getRandomTetromino(); // Get a random new piece
        // Calculate starting position: horizontally centered, at the very top (row 0)
        // Note: currentPiece.getWidth() might refer to the 4x4 grid, not the actual visual width.
        // For pieces like 'I' in vertical state, this might need adjustment or a more precise width calculation.
        currentPosition = new Point(BOARD_WIDTH / 2 - 2, 0); // A common starting point for 4x4 based pieces
        // Adjust for specific piece actual width if needed, e.g. currentPiece.getActualWidth() / 2

        // Check if the game is over (new piece collides immediately)
        if (!canMove(currentPiece, currentPosition, currentPiece.getCurrentRotation())) {
            isFallingFinished = true; // Set game over flag
            timer.stop(); // Stop the game timer
            System.out.println("Game Over! Score: " + score); // Print game over message
            // A graphical game over message is drawn in paintComponent
        }
    }

    /**
     * Attempts to move the current piece by deltaX and deltaY.
     * @param deltaX The change in the x-coordinate (columns).
     * @param deltaY The change in the y-coordinate (rows).
     * @return True if the piece was successfully moved, false otherwise (e.g., collision).
     */
    private boolean movePiece(int deltaX, int deltaY) {
        if (currentPiece == null) return false; // No piece to move

        Point newPosition = new Point(currentPosition.x + deltaX, currentPosition.y + deltaY);
        // Check if the piece *can* move to the new position
        if (canMove(currentPiece, newPosition, currentPiece.getCurrentRotation())) {
            currentPosition = newPosition; // Update current position
            repaint(); // Redraw the board
            return true;
        }
        return false;
    }

    /**
     * Attempts to rotate the current piece clockwise.
     * If the rotation causes a collision, the rotation is reverted.
     */
    private void rotatePiece() {
        if (currentPiece == null) return; // No piece to rotate

        currentPiece.rotate(); // Perform the rotation

        // Check if the new rotation is valid
        if (!canMove(currentPiece, currentPosition, currentPiece.getCurrentRotation())) {
            currentPiece.rotateBack(); // If not, revert to the original rotation
        }
        repaint(); // Redraw the board
    }

    /**
     * Called when the current piece can no longer move down (it has landed).
     * Adds the piece to the boardGrid, checks for and removes full lines, and spawns a new piece.
     */
    private void pieceDropped() {
        if (currentPiece == null) return;

        int[][] shape = currentPiece.getCurrentShape();
        Color color = currentPiece.getColor();

        // Add the blocks of the landed piece to the boardGrid
        for (int i = 0; i < shape.length; i++) { // Iterate over the piece's 4x4 grid rows
            for (int j = 0; j < shape[i].length; j++) { // Iterate over the piece's 4x4 grid columns
                if (shape[i][j] == 1) { // If this part of the Tetromino is a solid block
                    // Calculate the actual board coordinates
                    int boardX = currentPosition.x + j;
                    int boardY = currentPosition.y + i;
                    // Ensure the block is within board boundaries before adding
                    // This check is mostly a safeguard; canMove should prevent out-of-bounds drops.
                    if (boardY >= 0 && boardY < BOARD_HEIGHT && boardX >= 0 && boardX < BOARD_WIDTH) {
                        boardGrid[boardY][boardX] = color;
                    }
                }
            }
        }
        removeFullLines(); // Check for and remove any completed lines
        spawnNewPiece();   // Spawn the next piece
    }

    /**
     * Checks if a given piece can be placed at a specific position and rotation
     * without colliding with boundaries or other settled pieces.
     * @param piece The Tetromino to check.
     * @param position The target top-left (x,y) position on the boardGrid.
     * @param rotation The target rotation state of the piece.
     * @return True if the move is valid, false otherwise.
     */
    private boolean canMove(Tetromino piece, Point position, int rotation) {
        int[][] shape = piece.getShape(rotation); // Get the specific rotated shape matrix

        for (int i = 0; i < shape.length; i++) { // Iterate piece rows
            for (int j = 0; j < shape[i].length; j++) { // Iterate piece columns
                if (shape[i][j] == 1) { // If this is a solid block of the Tetromino
                    int boardX = position.x + j; // Projected X position on the board
                    int boardY = position.y + i; // Projected Y position on the board

                    // Boundary checks:
                    // Check if the block is outside the left, right, or bottom edges of the board.
                    if (boardX < 0 || boardX >= BOARD_WIDTH || boardY >= BOARD_HEIGHT) {
                        return false; // Collision with boundary
                    }
                    // Note: Top boundary (boardY < 0) is also a collision, 
                    // useful if pieces could rotate into negative Y space temporarily.
                    if (boardY < 0) {
                         return false; // Collision with top boundary
                    }

                    // Collision check with existing settled pieces:
                    // If the cell on the boardGrid is already occupied.
                    if (boardGrid[boardY][boardX] != null) {
                        return false; // Collision with another piece
                    }
                }
            }
        }
        return true; // No collisions detected, the move is valid
    }

    /**
     * Checks for and removes any lines that have been completely filled with blocks.
     * Shifts down the lines above the removed ones and updates the score.
     */
    private void removeFullLines() {
        int linesRemovedThisTurn = 0;
        // Iterate from the bottom line upwards
        for (int i = BOARD_HEIGHT - 1; i >= 0; i--) {
            boolean lineIsFull = true;
            // Check if all cells in the current line are filled
            for (int j = 0; j < BOARD_WIDTH; j++) {
                if (boardGrid[i][j] == null) {
                    lineIsFull = false; // Found an empty cell, so line is not full
                    break;
                }
            }

            if (lineIsFull) {
                linesRemovedThisTurn++;
                // If line is full, remove it by shifting all lines above it down by one row
                for (int k = i; k > 0; k--) { // Start from the full line, move upwards
                    // Copy the line above (k-1) to the current line (k)
                    System.arraycopy(boardGrid[k - 1], 0, boardGrid[k], 0, BOARD_WIDTH);
                }
                // Clear the topmost line (row 0) as it has been shifted down
                for (int j = 0; j < BOARD_WIDTH; j++) {
                    boardGrid[0][j] = null;
                }
                // Since a line was removed and lines shifted, the current row index 'i'
                // now contains what was previously line 'i-1'. So, we need to re-check
                // the current row index 'i' in the next iteration of the outer loop.
                i++;
            }
        }

        if (linesRemovedThisTurn > 0) {
            score += calculateScore(linesRemovedThisTurn); // Add points for cleared lines
            linesCleared += linesRemovedThisTurn;
            // Optionally, increase game speed based on linesCleared
            // Example: if (linesCleared % 10 == 0) timer.setDelay(Math.max(100, timer.getDelay() - 50));

            System.out.println("Score: " + score + ", Lines: " + linesCleared); // Print new score and lines to console
        }
    }

    /**
     * Calculates the score awarded for clearing a certain number of lines simultaneously.
     * Standard Tetris scoring is often used (e.g., 1 line = 40, 2 lines = 100, etc.).
     * @param lines The number of lines cleared at once.
     * @return The score points to be added.
     */
    private int calculateScore(int lines) {
        switch (lines) {
            case 1: return 40;
            case 2: return 100;
            case 3: return 300;
            case 4: return 1200; // A "Tetris" (clearing 4 lines at once)
            default: return 0;
        }
    }

    /**
     * Clears the entire board grid by setting all cells to null (empty).
     * Used when starting a new game.
     */
    private void clearBoard() {
        for (int i = 0; i < BOARD_HEIGHT; i++) {
            for (int j = 0; j < BOARD_WIDTH; j++) {
                boardGrid[i][j] = null;
            }
        }
    }

    /**
     * Overrides JPanel's paintComponent method to custom-draw the game.
     * This method is called automatically whenever the board needs to be redrawn (e.g., after repaint()).
     * @param g The Graphics object used for drawing.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // Call superclass method (important for proper JPanel painting)

        Graphics2D g2d = (Graphics2D) g.create(); // Create a copy for local modifications
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        drawBoardBackground(g2d); // Draw a modern background
        drawBoardGridLines(g2d); // Draw grid lines for better visibility
        drawSettledPieces(g2d);  // Draw the pieces that have already landed
        drawGhostPiece(g2d);     // Draw the ghost piece
        drawCurrentPiece(g2d);   // Draw the currently falling piece
        drawScoreAndLines(g2d);  // Draw the current score and lines cleared

        // Draw game over or paused messages if applicable
        if (isFallingFinished) {
            drawGameOverMessage(g2d);
        } else if (isPaused) {
            drawPausedMessage(g2d);
        }
        g2d.dispose(); // Dispose of the graphics copy
    }

    /**
     * Draws the background of the game board.
     * @param g2d The Graphics2D object for drawing.
     */
    private void drawBoardBackground(Graphics2D g2d) {
        g2d.setColor(new Color(20, 20, 20)); // Dark gray background
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }

    /**
     * Draws subtle grid lines on the board.
     * @param g2d The Graphics2D object for drawing.
     */
    private void drawBoardGridLines(Graphics2D g2d) {
        g2d.setColor(new Color(40, 40, 40)); // Darker grid lines
        for (int i = 0; i <= BOARD_HEIGHT; i++) {
            g2d.drawLine(0, i * BLOCK_SIZE, BOARD_WIDTH * BLOCK_SIZE, i * BLOCK_SIZE);
        }
        for (int i = 0; i <= BOARD_WIDTH; i++) {
            g2d.drawLine(i * BLOCK_SIZE, 0, i * BLOCK_SIZE, BOARD_HEIGHT * BLOCK_SIZE);
        }
    }


    /**
     * Draws all the pieces that have settled onto the boardGrid.
     * Uses the modern drawSquare method.
     * @param g The Graphics object used for drawing.
     */
    private void drawSettledPieces(Graphics2D g) {
        for (int i = 0; i < BOARD_HEIGHT; i++) {
            for (int j = 0; j < BOARD_WIDTH; j++) {
                if (boardGrid[i][j] != null) {
                    drawSquare(g, j * BLOCK_SIZE, i * BLOCK_SIZE, boardGrid[i][j], false);
                }
            }
        }
    }


    /**
     * Draws the currently falling Tetromino piece.
     * Uses the modern drawSquare method.
     * @param g The Graphics object used for drawing.
     */
    private void drawCurrentPiece(Graphics2D g) {
        if (currentPiece == null) {
            return;
        }
        int[][] shape = currentPiece.getCurrentShape();
        Color color = currentPiece.getColor();
        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (shape[i][j] == 1) {
                    int x = (currentPosition.x + j) * BLOCK_SIZE;
                    int y = (currentPosition.y + i) * BLOCK_SIZE;
                    drawSquare(g, x, y, color, false);
                }
            }
        }
    }

    /**
     * Draws a "ghost" image of the current piece showing where it will land.
     * @param g2d The Graphics2D object for drawing.
     */
    private void drawGhostPiece(Graphics2D g2d) {
        if (currentPiece == null || isFallingFinished) {
            return;
        }

        Point ghostPosition = new Point(currentPosition.x, currentPosition.y);
        // Move ghost piece down until it hits something
        while (canMove(currentPiece, new Point(ghostPosition.x, ghostPosition.y + 1), currentPiece.getCurrentRotation())) {
            ghostPosition.y++;
        }

        int[][] shape = currentPiece.getCurrentShape();
        Color ghostColor = new Color(currentPiece.getColor().getRed(),
                                     currentPiece.getColor().getGreen(),
                                     currentPiece.getColor().getBlue(), 70); // Semi-transparent

        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (shape[i][j] == 1) {
                    int x = (ghostPosition.x + j) * BLOCK_SIZE;
                    int y = (ghostPosition.y + i) * BLOCK_SIZE;
                    // Only draw ghost if it's different from current piece's actual position
                    if (ghostPosition.y > currentPosition.y) {
                         drawSquare(g2d, x, y, ghostColor, true);
                    }
                }
            }
        }
    }


    /**
     * Draws a single Tetris block (square) with a modern look.
     * @param g The Graphics object used for drawing.
     * @param x The x-coordinate of the top-left corner of the square.
     * @param y The y-coordinate of the top-left corner of the square.
     * @param color The base color of the square.
     * @param isGhost True if this is a ghost piece block, false otherwise.
     */
    private void drawSquare(Graphics2D g, int x, int y, Color color, boolean isGhost) {
        if (isGhost) {
            g.setColor(color); // Use the semi-transparent color directly
            g.fillRect(x, y, BLOCK_SIZE, BLOCK_SIZE);
            // Optionally, draw a border for the ghost piece
            g.setColor(color.darker());
            g.drawRect(x, y, BLOCK_SIZE -1 , BLOCK_SIZE -1);
        } else {
            // Create a brighter color for the main block face
            Color mainColor = color;
            Color lightBevel = color.brighter().brighter();
            Color darkBevel = color.darker().darker();

            g.setColor(mainColor);
            g.fillRect(x + 1, y + 1, BLOCK_SIZE - 2, BLOCK_SIZE - 2);

            // Draw 3D bevel effect
            g.setColor(lightBevel);
            g.drawLine(x, y + BLOCK_SIZE - 1, x, y); // Left edge
            g.drawLine(x, y, x + BLOCK_SIZE - 1, y); // Top edge

            g.setColor(darkBevel);
            g.drawLine(x + 1, y + BLOCK_SIZE - 1, x + BLOCK_SIZE - 1, y + BLOCK_SIZE - 1); // Bottom edge
            g.drawLine(x + BLOCK_SIZE - 1, y + BLOCK_SIZE - 1, x + BLOCK_SIZE - 1, y + 1); // Right edge

            // Inner highlight/shadow for more depth (optional)
            g.setColor(mainColor.brighter());
            g.drawRect(x + 2, y + 2, BLOCK_SIZE - 5, BLOCK_SIZE - 5);
        }
    }

    /**
     * Draws the current score and lines cleared on the board.
     * @param g The Graphics2D object used for drawing.
     */
    private void drawScoreAndLines(Graphics2D g) {
        g.setFont(new Font("Consolas", Font.BOLD, 18));
        g.setColor(Color.WHITE);
        String scoreText = "Score: " + score;
        String linesText = "Lines: " + linesCleared;
        g.drawString(scoreText, 10, 20);
        g.drawString(linesText, 10, 40);
    }

    /**
     * Draws the "Game Over" message.
     * @param g The Graphics2D object used for drawing.
     */
    private void drawGameOverMessage(Graphics2D g) {
        String msg = "Game Over";
        String scoreMsg = "Final Score: " + score;
        String restartMsg = "Press 'R' to Restart";

        Font largeFont = new Font("Consolas", Font.BOLD, 30);
        Font mediumFont = new Font("Consolas", Font.BOLD, 20);
        Font smallFont = new Font("Consolas", Font.PLAIN, 16);

        g.setColor(new Color(0, 0, 0, 150)); // Semi-transparent black background for text
        g.fillRect(0, getHeight() / 2 - 60, getWidth(), 120);

        g.setColor(Color.RED);
        g.setFont(largeFont);
        int msgWidth = g.getFontMetrics().stringWidth(msg);
        g.drawString(msg, (getWidth() - msgWidth) / 2, getHeight() / 2 - 20);

        g.setColor(Color.WHITE);
        g.setFont(mediumFont);
        int scoreMsgWidth = g.getFontMetrics().stringWidth(scoreMsg);
        g.drawString(scoreMsg, (getWidth() - scoreMsgWidth) / 2, getHeight() / 2 + 10);

        g.setFont(smallFont);
        int restartMsgWidth = g.getFontMetrics().stringWidth(restartMsg);
        g.drawString(restartMsg, (getWidth() - restartMsgWidth) / 2, getHeight() / 2 + 40);
    }

    /**
     * Draws the "Paused" message.
     * @param g The Graphics2D object used for drawing.
     */
    private void drawPausedMessage(Graphics2D g) {
        String msg = "Paused";
        String resumeMsg = "Press 'P' to Resume";
        Font largeFont = new Font("Consolas", Font.BOLD, 30);
        Font smallFont = new Font("Consolas", Font.PLAIN, 16);

        g.setColor(new Color(0, 0, 0, 150)); // Semi-transparent background
        g.fillRect(0, getHeight() / 2 - 40, getWidth(), 80);

        g.setColor(Color.YELLOW);
        g.setFont(largeFont);
        int msgWidth = g.getFontMetrics().stringWidth(msg);
        g.drawString(msg, (getWidth() - msgWidth) / 2, getHeight() / 2);

        g.setColor(Color.WHITE);
        g.setFont(smallFont);
        int resumeMsgWidth = g.getFontMetrics().stringWidth(resumeMsg);
        g.drawString(resumeMsg, (getWidth() - resumeMsgWidth) / 2, getHeight() / 2 + 30);
    }


    /**
     * Inner class TAdapter handles keyboard input for the game.
     * It extends KeyAdapter to override only the necessary key event methods.
     */
    private class TAdapter extends KeyAdapter {
        /**
         * Called when a key is pressed.
         * Handles game controls like moving, rotating, pausing, and restarting.
         * @param e The KeyEvent object containing information about the key press.
         */
        @Override
        public void keyPressed(KeyEvent e) {
            // If game is over, only allow 'R' to restart
            if (isFallingFinished) {
                if (e.getKeyCode() == KeyEvent.VK_R) {
                    startGame(); // Restart the game
                }
                return; // Ignore other keys if game is over
            }

            // Handle Pause/Resume with 'P' key
            if (e.getKeyCode() == KeyEvent.VK_P) {
                isPaused = !isPaused; // Toggle pause state
                if (isPaused) {
                    timer.stop(); // Stop game timer if paused
                } else {
                    timer.start(); // Resume game timer if unpaused
                }
                repaint(); // Redraw to show/hide Paused message
                return;
            }

            // If game is paused (and not over), ignore other game control keys
            if (isPaused) {
                return;
            }

            // Handle game controls based on the key pressed
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT:
                    if (!isPaused) movePiece(-1, 0);
                    break;
                case KeyEvent.VK_RIGHT:
                    if (!isPaused) movePiece(1, 0);
                    break;
                case KeyEvent.VK_DOWN:
                    if (!isPaused) {
                        // Temporarily increase speed
                        timer.setDelay(FAST_DELAY);
                        movePiece(0, 1);
                    }
                    break;
                case KeyEvent.VK_UP:
                case KeyEvent.VK_SPACE: // Added SPACE for rotation
                    if (!isPaused) rotatePiece();
                    break;
                case KeyEvent.VK_P:
                    togglePause();
                    break;
                case KeyEvent.VK_R: // Added 'R' for restart
                    if (isFallingFinished || isPaused) { // Allow restart if game over or paused
                        startGame();
                    }
                    break;
            }
            repaint(); // Repaint after any action
        }

        @Override
        public void keyReleased(KeyEvent e) {
            if (isPaused || currentPiece == null) { // Don't revert speed if paused or no piece
                return;
            }

            int key = e.getKeyCode();
            // Revert to normal speed when 'Down' key is released
            if (key == KeyEvent.VK_DOWN) {
                timer.setDelay(INITIAL_DELAY); // Or whatever the current normal delay is
            }
        }
    }

    /**
     * Toggles the pause state of the game.
     */
    private void togglePause() {
        if (isFallingFinished) { // Cannot pause/unpause if game is over
            return;
        }
        isPaused = !isPaused;
        if (isPaused) {
            timer.stop();
            System.out.println("Game Paused");
        } else {
            timer.start();
            System.out.println("Game Resumed");
        }
        repaint(); // Redraw to show/hide pause message
    }
}
