// Food.cpp
#include "Food.h"
#include <algorithm> // For std::find_if

/**
 * @brief Constructs a new Food object.
 * Initializes the board dimensions, segment size, and seeds the random number generator.
 * The initial position of the food is typically set by the first call to GenerateNewPosition.
 * @param bWidth The width of the game board.
 * @param bHeight The height of the game board.
 * @param segSize The size of each snake/food segment, used for grid alignment.
 */
Food::Food(int bWidth, int bHeight, int segSize)
    : boardWidth(bWidth), boardHeight(bHeight), segmentSize(segSize) {
    // Seed the random number generator using std::random_device for a non-deterministic seed.
    std::random_device rd;
    rng.seed(rd());
    // Note: The food's initial position is not set here;
    // it's expected to be set by an initial call to GenerateNewPosition() in Game::Init().
}

/**
 * @brief Generates a new random position for the food on the game board.
 * The position is aligned to a grid based on segmentSize.
 * It ensures that the new food position does not overlap with any part of the snake's body.
 * @param snakeBody A constant reference to the deque of Positions representing the snake's current body.
 */
void Food::GenerateNewPosition(const std::deque<Position>& snakeBody) {
    // Define distributions for x and y coordinates based on the number of grid cells.
    // The board is divided into a grid of segmentSize x segmentSize cells.
    std::uniform_int_distribution<int> distX(0, (boardWidth / segmentSize) - 1);
    std::uniform_int_distribution<int> distY(0, (boardHeight / segmentSize) - 1);

    bool positionOK = false;
    while(!positionOK) {
        // Generate a candidate position in terms of grid cells, then scale by segmentSize.
        pos.x = distX(rng) * segmentSize;
        pos.y = distY(rng) * segmentSize;

        // Check if the new food position overlaps with any segment of the snake's body.
        // std::find_if iterates through snakeBody and returns an iterator to the first element
        // for which the lambda function returns true.
        auto it = std::find_if(snakeBody.begin(), snakeBody.end(), 
            [&](const Position& segment) {
            // Lambda captures 'this' (or specific members like 'pos') to compare segment with candidate food position.
            return segment.x == pos.x && segment.y == pos.y;
        });

        // If find_if reaches the end of snakeBody, it means no overlap was found.
        if (it == snakeBody.end()) {
            positionOK = true; // Position is valid (not on the snake).
        }
        // If an overlap is found, the loop continues to find a new position.
    }
}

/**
 * @brief Gets the current position of the food.
 * @return The Position struct containing the (x,y) coordinates of the food.
 */
Position Food::GetPosition() const {
    return pos;
}

