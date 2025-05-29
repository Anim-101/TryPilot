// Food.h
#ifndef FOOD_H
#define FOOD_H

#include "Snake.h" // For Position struct and Snake body
#include <vector>
#include <deque>
#include <random>   // For random number generation

class Food {
public:
    Food(int boardWidth, int boardHeight, int segmentSz);

    void GenerateNewPosition(const std::deque<Position>& snakeBody);
    Position GetPosition() const;

private:
    Position pos;
    int boardWidth;
    int boardHeight;
    int segmentSize; // To align food with the snake's grid

    std::mt19937 rng; // Mersenne Twister random number generator
};

#endif
