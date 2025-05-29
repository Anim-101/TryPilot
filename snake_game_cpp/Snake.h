// Snake.h
#ifndef SNAKE_H
#define SNAKE_H

#include <vector>
#include <deque> // Using deque for efficient add/remove at both ends

/**
 * @struct Position
 * @brief Represents a 2D coordinate (x, y).
 * Used for snake segments and food position.
 */
struct Position {
    int x, y; ///< x and y coordinates.

    /**
     * @brief Equality operator for Position.
     * @param other The other Position to compare against.
     * @return True if both x and y coordinates are equal, false otherwise.
     */
    bool operator==(const Position& other) const {
        return x == other.x && y == other.y;
    }
};

/**
 * @enum Direction
 * @brief Represents the direction of the snake's movement.
 */
enum Direction {
    UP,      ///< Movement upwards.
    DOWN,    ///< Movement downwards.
    LEFT,    ///< Movement to the left.
    RIGHT,   ///< Movement to the right.
    STOPPED  ///< No movement (initial state or paused).
};

/**
 * @class Snake
 * @brief Represents the player-controlled snake in the game.
 * 
 * Handles the snake's movement, growth, and collision detection with itself.
 * The snake is represented as a deque of Position structs.
 */
class Snake {
public:
    /**
     * @brief Constructor for the Snake class.
     * @param startX The initial x-coordinate of the snake's head.
     * @param startY The initial y-coordinate of the snake's head.
     * @param segmentSize The size (width and height) of each snake segment in pixels. Defaults to 20.
     */
    Snake(int startX, int startY, int segmentSize = 20);

    /**
     * @brief Moves the snake one step in its current direction.
     * Adds a new head segment and removes the tail segment unless the snake is growing.
     */
    void Move();

    /**
     * @brief Makes the snake grow by one segment on its next move.
     * Sets a flag that prevents the tail from being removed in the next call to Move().
     */
    void Grow();

    /**
     * @brief Sets the snake's current direction of movement.
     * Prevents immediate 180-degree turns.
     * @param newDir The new direction for the snake.
     */
    void SetDirection(Direction newDir);

    /**
     * @brief Gets the snake's current direction of movement.
     * @return The current Direction.
     */
    Direction GetDirection() const;

    /**
     * @brief Gets the snake's body.
     * @return A constant reference to the deque of Position structs representing the snake's body.
     */
    const std::deque<Position>& GetBody() const;

    /**
     * @brief Gets the position of the snake's head.
     * @return The Position of the first segment in the body deque.
     */
    Position GetHeadPosition() const;

    /**
     * @brief Checks if the snake has collided with itself.
     * @return True if the head collides with any other body segment, false otherwise.
     */
    bool CheckSelfCollision() const;

    /**
     * @brief Gets the size of each snake segment.
     * @return The size (width/height) of a segment in pixels.
     */
    int GetSegmentSize() const;

private:
    std::deque<Position> body; ///< Deque storing the (x,y) positions of the snake's segments. Head is at the front.
    Direction currentDirection; ///< The current direction the snake is moving or set to move.
    Direction lastMovedDirection; ///< The direction the snake actually moved in the last call to Move(). Used to prevent 180-degree turns.
    int segmentSize;           ///< The size (width and height) of each segment in pixels.
    bool growNextMove;         ///< Flag indicating if the snake should grow in the next move.
};

#endif
