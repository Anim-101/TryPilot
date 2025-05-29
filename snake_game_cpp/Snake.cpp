// Snake.cpp
#include "Snake.h"
#include <iostream> // For debugging (can be removed in final version)

/**
 * @brief Constructs a new Snake object.
 * Initializes the snake with a few segments starting at the given coordinates.
 * The snake initially consists of three segments, with the head at (startX, startY).
 * @param startX The initial x-coordinate for the head of the snake.
 * @param startY The initial y-coordinate for the head of the snake.
 * @param segSize The size (width and height) of each segment of the snake.
 */
Snake::Snake(int startX, int startY, int segSize)
    : currentDirection(STOPPED), lastMovedDirection(STOPPED), segmentSize(segSize), growNextMove(false) {
    // Initialize snake with a head and two body segments.
    // The head is at (startX, startY).
    body.push_front({startX, startY}); 
    // The following segments are added behind the head, extending to the left by default.
    // This assumes a horizontal starting orientation.
    body.push_front({startX - segmentSize, startY}); 
    body.push_front({startX - 2 * segmentSize, startY});
}

/**
 * @brief Moves the snake one step in its current direction.
 * If the snake is set to grow (growNextMove is true), a new head is added, and the tail is not removed.
 * Otherwise, a new head is added, and the tail segment is removed.
 * The lastMovedDirection is updated after the move.
 */
void Snake::Move() {
    if (currentDirection == STOPPED) return; // Do not move if direction is stopped.

    Position newHead = body.front(); // Start with the current head's position.

    // Update newHead coordinates based on the current direction.
    switch (currentDirection) {
        case UP:    newHead.y -= segmentSize; break;
        case DOWN:  newHead.y += segmentSize; break;
        case LEFT:  newHead.x -= segmentSize; break;
        case RIGHT: newHead.x += segmentSize; break;
        default: break; // Should not happen if currentDirection is valid.
    }

    body.push_front(newHead); // Add the new head to the front of the deque.

    if (growNextMove) {
        growNextMove = false; // Reset the grow flag, snake has grown.
    } else {
        body.pop_back(); // Remove the tail segment if not growing.
    }
    lastMovedDirection = currentDirection; // Update the direction the snake last moved.
}

/**
 * @brief Sets a flag to make the snake grow by one segment during its next move.
 */
void Snake::Grow() {
    growNextMove = true;
}

/**
 * @brief Sets the snake's intended direction of movement.
 * This method includes logic to prevent the snake from immediately reversing its direction
 * (e.g., moving left if it's currently moving right).
 * @param newDir The desired new direction.
 */
void Snake::SetDirection(Direction newDir) {
    // Prevent immediate 180-degree turns if the snake has more than one segment and was moving.
    if ((newDir == UP && lastMovedDirection == DOWN) ||
        (newDir == DOWN && lastMovedDirection == UP) ||
        (newDir == LEFT && lastMovedDirection == RIGHT) ||
        (newDir == RIGHT && lastMovedDirection == LEFT)) {
        
        // Allow direction change if snake is very short (1 segment) or was stopped.
        if (body.size() > 1 && lastMovedDirection != STOPPED) {
             return; // Ignore input that would cause immediate reversal.
        }
    }
    
    // If the snake is starting from a single block or was stopped,
    // reset lastMovedDirection to allow any initial move.
    if (currentDirection == STOPPED && body.size() == 1) {
         lastMovedDirection = STOPPED; 
    }

    currentDirection = newDir; // Set the new intended direction.
    // If this is the first movement input (snake was stopped),
    // also set lastMovedDirection to allow the first move.
    if (lastMovedDirection == STOPPED) { 
        lastMovedDirection = newDir;
    }
}

/**
 * @brief Gets the current intended direction of the snake.
 * @return The snake's current Direction.
 */
Direction Snake::GetDirection() const {
    return currentDirection;
}

/**
 * @brief Gets the snake's body segments.
 * @return A constant reference to the deque of Position structs representing the snake's body.
 * The head of the snake is at the front of the deque.
 */
const std::deque<Position>& Snake::GetBody() const {
    return body;
}

/**
 * @brief Gets the position of the snake's head.
 * @return The Position of the head segment. Returns {-1, -1} if the body is empty (should not happen).
 */
Position Snake::GetHeadPosition() const {
    if (body.empty()) {
        // This case should ideally not be reached in normal gameplay.
        return {-1, -1}; 
    }
    return body.front(); // The head is the first element in the deque.
}

/**
 * @brief Checks if the snake's head has collided with any part of its body.
 * @return True if a self-collision is detected, false otherwise.
 * A collision cannot occur if the snake is shorter than 2 segments.
 */
bool Snake::CheckSelfCollision() const {
    if (body.size() < 2) return false; // Not long enough to collide with self.

    Position head = body.front(); // Get the head's position.
    // Check if the head's position matches any other segment's position.
    // Start checking from the second segment (index 1).
    for (size_t i = 1; i < body.size(); ++i) {
        if (body[i].x == head.x && body[i].y == head.y) {
            return true; // Collision detected.
        }
    }
    return false; // No self-collision.
}

/**
 * @brief Gets the size (width and height) of each snake segment.
 * @return The segment size in pixels.
 */
int Snake::GetSegmentSize() const {
    return segmentSize;
}

