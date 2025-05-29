// main.cpp
#define GLEW_STATIC // Define GLEW_STATIC if linking against the GLEW static library.
#include <GL/glew.h>
#include <GLFW/glfw3.h>
#include <iostream>
#include "Game.h" // Main game class header

// Window dimensions - Constants for the game window size.
const GLuint WIDTH = 800, HEIGHT = 600;

// Global Game object.
Game snakeGame(WIDTH, HEIGHT);

// GLFW callback function declarations
void key_callback(GLFWwindow* window, int key, int scancode, int action, int mode);
void framebuffer_size_callback(GLFWwindow* window, int width, int height);

/**
 * @brief Main entry point of the application.
 * Initializes GLFW, GLEW, creates the game window, and runs the game loop.
 * @return 0 if successful, -1 on failure.
 */
int main() {
    // Initialize GLFW library
    if (!glfwInit()) {
        std::cerr << "Failed to initialize GLFW" << std::endl;
        return -1;
    }
    // Set GLFW window hints for OpenGL version (3.3) and profile (Core).
    glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
    glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
    glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
    glfwWindowHint(GLFW_RESIZABLE, GL_FALSE); // Window is not resizable.

    // Create a GLFW window
    GLFWwindow* window = glfwCreateWindow(WIDTH, HEIGHT, "Snake Game C++ OpenGL", nullptr, nullptr);
    if (window == nullptr) {
        std::cerr << "Failed to create GLFW window" << std::endl;
        glfwTerminate(); // Terminate GLFW before exiting.
        return -1;
    }
    glfwMakeContextCurrent(window); // Make the window's context current.

    // Set GLFW callback functions
    glfwSetKeyCallback(window, key_callback); // For keyboard input.
    glfwSetFramebufferSizeCallback(window, framebuffer_size_callback); // For window resize events.

    // Initialize GLEW library (OpenGL Extension Wrangler)
    glewExperimental = GL_TRUE; // Needed for core profile.
    if (glewInit() != GLEW_OK) {
        std::cerr << "Failed to initialize GLEW" << std::endl;
        glfwDestroyWindow(window); // Destroy window before terminating GLFW.
        glfwTerminate();
        return -1;
    }

    // Define the viewport dimensions for OpenGL rendering.
    glViewport(0, 0, WIDTH, HEIGHT);

    // Configure global OpenGL state
    glEnable(GL_BLEND); // Enable blending for transparency (e.g., if using alpha values).
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA); // Standard alpha blending function.

    // Initialize the game (loads resources, sets up game objects).
    snakeGame.Init();

    // Variables for calculating delta time (time between frames).
    float deltaTime = 0.0f;
    float lastFrame = 0.0f;

    // Main game loop: continues until the window is closed.
    while (!glfwWindowShouldClose(window)) {
        // Calculate delta time for consistent game speed across different hardware.
        float currentFrame = static_cast<float>(glfwGetTime());
        deltaTime = currentFrame - lastFrame;
        lastFrame = currentFrame;

        glfwPollEvents(); // Poll for and process events (keyboard, mouse, window).

        // Process user input for the current frame.
        snakeGame.ProcessInput(deltaTime);

        // Update game state (e.g., snake movement, collisions).
        snakeGame.Update(deltaTime);

        // Render the game
        // Set background color (dark grey).
        glClearColor(0.1f, 0.1f, 0.1f, 1.0f); 
        // Clear the color buffer.
        glClear(GL_COLOR_BUFFER_BIT); 
        // Render game objects.
        snakeGame.Render();

        glfwSwapBuffers(window); // Swap the front and back buffers to display the rendered frame.
    }

    // Terminate GLFW, cleaning up all GLFW resources.
    glfwTerminate();
    return 0; // Successful execution.
}

/**
 * @brief GLFW callback function for keyboard input.
 * This function is called whenever a key is pressed, released, or repeated.
 * @param window The window that received the event.
 * @param key The keyboard key that was pressed or released.
 * @param scancode The system-specific scancode of the key.
 * @param action GLFW_PRESS, GLFW_RELEASE or GLFW_REPEAT.
 * @param mode Bit field describing which modifier keys were held down.
 */
void key_callback(GLFWwindow* window, int key, int scancode, int action, int mode) {
    // Close window if ESCAPE key is pressed.
    if (key == GLFW_KEY_ESCAPE && action == GLFW_PRESS)
        glfwSetWindowShouldClose(window, GL_TRUE);
    
    // Update the game's key state array.
    if (key >= 0 && key < 1024) { // Ensure key is within bounds of the Keys array.
        if (action == GLFW_PRESS)
            snakeGame.Keys[key] = GL_TRUE; // Mark key as pressed.
        else if (action == GLFW_RELEASE)
            snakeGame.Keys[key] = GL_FALSE; // Mark key as released.
    }
}

/**
 * @brief GLFW callback function for window framebuffer resize events.
 * This function is called when the window's framebuffer is resized.
 * @param window The window that was resized.
 * @param width The new width, in pixels, of the framebuffer.
 * @param height The new height, in pixels, of the framebuffer.
 */
void framebuffer_size_callback(GLFWwindow* window, int width, int height) {
    // Update OpenGL viewport to match the new window size.
    glViewport(0, 0, width, height);
    // If dynamic projection updates are needed (e.g., for a resizable window),
    // the game's projection matrix would be updated here.
    // snakeGame.UpdateProjection(width, height); 
}

