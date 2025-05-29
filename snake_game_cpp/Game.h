// Game.h
#ifndef GAME_H
#define GAME_H

#include <GL/glew.h>
#include <GLFW/glfw3.h>
#include <vector>
#include <iostream>

// Forward declarations
class Snake; 
class Food;  

/**
 * @brief Represents the current state of the game.
 */
enum GameState {
    GAME_ACTIVE, ///< The game is currently being played.
    GAME_MENU,   ///< The game is in a menu state (not fully implemented).
    GAME_WIN,    ///< The game has been won (not typically used in Snake, placeholder).
    GAME_OVER    ///< The game is over.
};

/**
 * @class Game
 * @brief Manages the overall game state, logic, rendering, and input for the Snake game.
 * 
 * This class initializes all game components (snake, food), handles the game loop,
 * processes user input, updates game objects, and renders them to the screen using OpenGL.
 */
class Game {
public:
    // Game state
    GameState State;      ///< Current state of the game (e.g., GAME_ACTIVE, GAME_OVER).
    GLboolean Keys[1024]; ///< Array to store the state of keyboard keys. True if pressed, false otherwise.
    GLuint Width, Height; ///< Width and height of the game window in pixels.
    
    // Game components
    Snake* PlayerSnake; ///< Pointer to the player-controlled Snake object.
    Food* GameFood;     ///< Pointer to the Food object.
    
    /**
     * @brief Constructor for the Game class.
     * @param width The width of the game window.
     * @param height The height of the game window.
     */
    Game(GLuint width, GLuint height);
    
    /**
     * @brief Destructor for the Game class. Cleans up dynamically allocated resources.
     */
    ~Game();
    
    /**
     * @brief Initializes the game state.
     * This includes setting up shaders, rendering data, and creating game objects like the snake and food.
     */
    void Init();
    
    /**
     * @brief Processes user input.
     * Called in each iteration of the game loop to handle keyboard events.
     * @param dt Delta time, the time elapsed since the last frame.
     */
    void ProcessInput(GLfloat dt);

    /**
     * @brief Updates the game state.
     * Called in each iteration of the game loop to update game logic, such as snake movement and collision detection.
     * @param dt Delta time, the time elapsed since the last frame.
     */
    void Update(GLfloat dt);

    /**
     * @brief Renders the game.
     * Called in each iteration of the game loop to draw all game objects to the screen.
     */
    void Render();

    /**
     * @brief Resets the game to its initial state.
     * Called when the game is over and the player chooses to restart.
     */
    void ResetGame();

private:
    /**
     * @brief Draws a simple colored rectangle.
     * Used for rendering snake segments and food.
     * @param x The x-coordinate of the top-left corner of the rectangle.
     * @param y The y-coordinate of the top-left corner of the rectangle.
     * @param rectWidth The width of the rectangle.
     * @param rectHeight The height of the rectangle.
     * @param r Red component of the color (0.0 - 1.0).
     * @param g Green component of the color (0.0 - 1.0).
     * @param b Blue component of the color (0.0 - 1.0).
     */
    void DrawRectangle(float x, float y, float rectWidth, float rectHeight, float r, float g, float b);
    
    GLuint VAO, VBO;        ///< Vertex Array Object and Vertex Buffer Object for rendering rectangles.
    GLuint shaderProgram;   ///< ID of the compiled shader program.

    /**
     * @brief Initializes the VAO and VBO for rendering simple quads.
     */
    void initRenderData();

    /**
     * @brief Compiles and links the vertex and fragment shaders.
     */
    void compileShaders();
};

#endif
