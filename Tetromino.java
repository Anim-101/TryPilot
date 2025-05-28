import java.awt.Color;
import java.util.Random;

public enum Tetromino {
    I_SHAPE(new int[][][] {
            { {0,0,0,0}, {1,1,1,1}, {0,0,0,0}, {0,0,0,0} }, // Rotation 0
            { {0,1,0,0}, {0,1,0,0}, {0,1,0,0}, {0,1,0,0} }, // Rotation 1
            { {0,0,0,0}, {0,0,0,0}, {1,1,1,1}, {0,0,0,0} }, // Rotation 2
            { {0,0,1,0}, {0,0,1,0}, {0,0,1,0}, {0,0,1,0} }  // Rotation 3
    }, new Color(0, 240, 240)), // Cyan

    L_SHAPE(new int[][][] {
            { {0,0,1,0}, {1,1,1,0}, {0,0,0,0}, {0,0,0,0} },
            { {0,1,0,0}, {0,1,0,0}, {0,1,1,0}, {0,0,0,0} },
            { {0,0,0,0}, {1,1,1,0}, {1,0,0,0}, {0,0,0,0} },
            { {1,1,0,0}, {0,1,0,0}, {0,1,0,0}, {0,0,0,0} }
    }, new Color(240, 160, 0)), // Orange

    J_SHAPE(new int[][][] {
            { {1,0,0,0}, {1,1,1,0}, {0,0,0,0}, {0,0,0,0} },
            { {0,1,1,0}, {0,1,0,0}, {0,1,0,0}, {0,0,0,0} },
            { {0,0,0,0}, {1,1,1,0}, {0,0,1,0}, {0,0,0,0} },
            { {0,1,0,0}, {0,1,0,0}, {1,1,0,0}, {0,0,0,0} }
    }, new Color(0, 0, 240)),   // Blue

    S_SHAPE(new int[][][] {
            { {0,1,1,0}, {1,1,0,0}, {0,0,0,0}, {0,0,0,0} },
            { {0,1,0,0}, {0,1,1,0}, {0,0,1,0}, {0,0,0,0} },
            { {0,0,0,0}, {0,1,1,0}, {1,1,0,0}, {0,0,0,0} },
            { {1,0,0,0}, {1,1,0,0}, {0,1,0,0}, {0,0,0,0} }
    }, new Color(0, 240, 0)),   // Green

    Z_SHAPE(new int[][][] {
            { {1,1,0,0}, {0,1,1,0}, {0,0,0,0}, {0,0,0,0} },
            { {0,0,1,0}, {0,1,1,0}, {0,1,0,0}, {0,0,0,0} },
            { {0,0,0,0}, {1,1,0,0}, {0,1,1,0}, {0,0,0,0} },
            { {0,1,0,0}, {1,1,0,0}, {1,0,0,0}, {0,0,0,0} }
    }, new Color(240, 0, 0)),   // Red

    T_SHAPE(new int[][][] {
            { {0,1,0,0}, {1,1,1,0}, {0,0,0,0}, {0,0,0,0} },
            { {0,1,0,0}, {0,1,1,0}, {0,1,0,0}, {0,0,0,0} },
            { {0,0,0,0}, {1,1,1,0}, {0,1,0,0}, {0,0,0,0} },
            { {0,1,0,0}, {1,1,0,0}, {0,1,0,0}, {0,0,0,0} }
    }, new Color(160, 0, 240)), // Purple

    O_SHAPE(new int[][][] {
            { {1,1,0,0}, {1,1,0,0}, {0,0,0,0}, {0,0,0,0} }, // O-shape has only one rotation state
            { {1,1,0,0}, {1,1,0,0}, {0,0,0,0}, {0,0,0,0} },
            { {1,1,0,0}, {1,1,0,0}, {0,0,0,0}, {0,0,0,0} },
            { {1,1,0,0}, {1,1,0,0}, {0,0,0,0}, {0,0,0,0} }
    }, new Color(240, 240, 0)); // Yellow

    private final int[][][] shapes; // [rotation][row][col]
    private final Color color;
    private int currentRotation;

    Tetromino(int[][][] shapes, Color color) {
        this.shapes = shapes;
        this.color = color;
        this.currentRotation = 0;
    }

    public int[][] getCurrentShape() {
        return shapes[currentRotation];
    }

    public Color getColor() {
        return color;
    }

    public void rotate() {
        currentRotation = (currentRotation + 1) % shapes.length;
    }
    
    public void rotateBack() {
        currentRotation = (currentRotation - 1 + shapes.length) % shapes.length;
    }

    public int getWidth() {
        return getCurrentShape()[0].length; // Assumes all rows have same length
    }

    public int getHeight() {
        return getCurrentShape().length;
    }
    
    // Get the specific shape for a given rotation
    public int[][] getShape(int rotation) {
        return shapes[rotation % shapes.length];
    }
    
    public int getCurrentRotation() {
        return currentRotation;
    }

    public static Tetromino getRandomTetromino() {
        Random random = new Random();
        return values()[random.nextInt(values().length)];
    }
}
