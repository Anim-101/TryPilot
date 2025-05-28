import java.awt.Color;
import java.util.Random;

/**
 * Represents the different Tetris pieces (Tetrominoes).
 * Each Tetromino has a defined shape, color, and can be rotated.
 * The shapes are defined as 4x4 integer arrays, where 1 represents a block
 * and 0 represents an empty space. Each Tetromino can have multiple such
 * arrays to represent its different rotation states.
 */
public enum Tetromino {
    /**
     * The I-shape Tetromino. Looks like a straight line of four blocks.
     * Rotations:
     * 0: Horizontal line
     * 1: Vertical line
     * 2: Horizontal line
     * 3: Vertical line
     */
    I_SHAPE(new int[][][] {
            { {0,0,0,0}, {1,1,1,1}, {0,0,0,0}, {0,0,0,0} }, // Rotation 0
            { {0,1,0,0}, {0,1,0,0}, {0,1,0,0}, {0,1,0,0} }, // Rotation 1
            { {0,0,0,0}, {0,0,0,0}, {1,1,1,1}, {0,0,0,0} }, // Rotation 2 (same as 0 for simplicity, could be optimized)
            { {0,0,1,0}, {0,0,1,0}, {0,0,1,0}, {0,0,1,0} }  // Rotation 3 (same as 1 for simplicity)
    }, new Color(0, 240, 240)), // Cyan color

    /**
     * The L-shape Tetromino.
     */
    L_SHAPE(new int[][][] {
            { {0,0,1,0}, {1,1,1,0}, {0,0,0,0}, {0,0,0,0} },
            { {0,1,0,0}, {0,1,0,0}, {0,1,1,0}, {0,0,0,0} },
            { {0,0,0,0}, {1,1,1,0}, {1,0,0,0}, {0,0,0,0} },
            { {1,1,0,0}, {0,1,0,0}, {0,1,0,0}, {0,0,0,0} }
    }, new Color(240, 160, 0)), // Orange color

    /**
     * The J-shape Tetromino (mirror image of L-shape).
     */
    J_SHAPE(new int[][][] {
            { {1,0,0,0}, {1,1,1,0}, {0,0,0,0}, {0,0,0,0} },
            { {0,1,1,0}, {0,1,0,0}, {0,1,0,0}, {0,0,0,0} },
            { {0,0,0,0}, {1,1,1,0}, {0,0,1,0}, {0,0,0,0} },
            { {0,1,0,0}, {0,1,0,0}, {1,1,0,0}, {0,0,0,0} }
    }, new Color(0, 0, 240)),   // Blue color

    /**
     * The S-shape Tetromino.
     */
    S_SHAPE(new int[][][] {
            { {0,1,1,0}, {1,1,0,0}, {0,0,0,0}, {0,0,0,0} },
            { {0,1,0,0}, {0,1,1,0}, {0,0,1,0}, {0,0,0,0} },
            { {0,0,0,0}, {0,1,1,0}, {1,1,0,0}, {0,0,0,0} }, // Same as rotation 0
            { {1,0,0,0}, {1,1,0,0}, {0,1,0,0}, {0,0,0,0} }  // Same as rotation 1
    }, new Color(0, 240, 0)),   // Green color

    /**
     * The Z-shape Tetromino (mirror image of S-shape).
     */
    Z_SHAPE(new int[][][] {
            { {1,1,0,0}, {0,1,1,0}, {0,0,0,0}, {0,0,0,0} },
            { {0,0,1,0}, {0,1,1,0}, {0,1,0,0}, {0,0,0,0} },
            { {0,0,0,0}, {1,1,0,0}, {0,1,1,0}, {0,0,0,0} }, // Same as rotation 0
            { {0,1,0,0}, {1,1,0,0}, {1,0,0,0}, {0,0,0,0} }  // Same as rotation 1
    }, new Color(240, 0, 0)),   // Red color

    /**
     * The T-shape Tetromino.
     */
    T_SHAPE(new int[][][] {
            { {0,1,0,0}, {1,1,1,0}, {0,0,0,0}, {0,0,0,0} },
            { {0,1,0,0}, {0,1,1,0}, {0,1,0,0}, {0,0,0,0} },
            { {0,0,0,0}, {1,1,1,0}, {0,1,0,0}, {0,0,0,0} },
            { {0,1,0,0}, {1,1,0,0}, {0,1,0,0}, {0,0,0,0} }
    }, new Color(160, 0, 240)), // Purple color

    /**
     * The O-shape Tetromino (a 2x2 square).
     * It only has one effective rotation state.
     */
    O_SHAPE(new int[][][] {
            { {1,1,0,0}, {1,1,0,0}, {0,0,0,0}, {0,0,0,0} }, // Rotation 0
            { {1,1,0,0}, {1,1,0,0}, {0,0,0,0}, {0,0,0,0} }, // Rotation 1 (same)
            { {1,1,0,0}, {1,1,0,0}, {0,0,0,0}, {0,0,0,0} }, // Rotation 2 (same)
            { {1,1,0,0}, {1,1,0,0}, {0,0,0,0}, {0,0,0,0} }  // Rotation 3 (same)
    }, new Color(240, 240, 0)); // Yellow color

    /**
     * Stores all rotation states for this Tetromino.
     * The first dimension is the rotation index (0-3).
     * The second dimension is the row within the 4x4 grid.
     * The third dimension is the column within the 4x4 grid.
     */
    private final int[][][] shapes;
    /**
     * The color of this Tetromino.
     */
    private final Color color;
    /**
     * The current rotation state of this Tetromino (0-3).
     */
    private int currentRotation;

    /**
     * Constructor for a Tetromino.
     * @param shapes An array of 4x4 integer matrices representing the different rotation states.
     * @param color The color of this Tetromino.
     */
    Tetromino(int[][][] shapes, Color color) {
        this.shapes = shapes;
        this.color = color;
        this.currentRotation = 0; // Default to the first rotation state
    }

    /**
     * Gets the 4x4 integer array representing the current rotation state of the Tetromino.
     * @return A 2D integer array (4x4) for the current shape.
     */
    public int[][] getCurrentShape() {
        return shapes[currentRotation];
    }

    /**
     * Gets the color of this Tetromino.
     * @return The {@link Color} object.
     */
    public Color getColor() {
        return color;
    }

    /**
     * Rotates the Tetromino clockwise to its next rotation state.
     * If it's at the last rotation state, it cycles back to the first.
     */
    public void rotate() {
        currentRotation = (currentRotation + 1) % shapes.length;
    }
    
    /**
     * Rotates the Tetromino counter-clockwise to its previous rotation state.
     * If it's at the first rotation state, it cycles back to the last.
     */
    public void rotateBack() {
        currentRotation = (currentRotation - 1 + shapes.length) % shapes.length;
    }

    /**
     * Gets the effective width of the Tetromino in its current rotation.
     * This is based on the 4x4 grid definition. For actual bounding box,
     * further calculation might be needed if shapes aren't dense in the 4x4.
     * However, for simplicity, we use the grid width.
     * @return The width of the shape's 4x4 grid (typically 4).
     */
    public int getWidth() {
        // Assumes all rows in a shape definition have the same length (which they do, 4)
        return getCurrentShape()[0].length;
    }

    /**
     * Gets the effective height of the Tetromino in its current rotation.
     * Similar to getWidth, this is based on the 4x4 grid definition.
     * @return The height of the shape's 4x4 grid (typically 4).
     */
    public int getHeight() {
        return getCurrentShape().length;
    }
    
    /**
     * Gets the specific 4x4 integer array for a given rotation index.
     * @param rotation The desired rotation index (0-3).
     * @return A 2D integer array (4x4) for the specified rotation.
     */
    public int[][] getShape(int rotation) {
        return shapes[rotation % shapes.length]; // Use modulo for safety
    }
    
    /**
     * Gets the current rotation index of the Tetromino.
     * @return The current rotation index (an integer from 0 to shapes.length - 1).
     */
    public int getCurrentRotation() {
        return currentRotation;
    }

    /**
     * A utility method to get a randomly selected Tetromino.
     * @return A random {@link Tetromino} enum constant.
     */
    public static Tetromino getRandomTetromino() {
        Random random = new Random();
        // values() returns an array of all enum constants
        return values()[random.nextInt(values().length)];
    }
}
