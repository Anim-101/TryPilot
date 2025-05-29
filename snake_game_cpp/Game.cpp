// Game.cpp
#include "Game.h"
#include "Snake.h"
#include "Food.h"

// Basic Vertex Shader
const char* vertexShaderSource = R"glsl(
#version 330 core
layout (location = 0) in vec2 aPos; // Vertex position attribute

uniform mat4 projection;   // Projection matrix to map coordinates to screen space
uniform vec2 model_pos;    // Position of the rectangle (top-left)
uniform vec2 model_size;   // Size of the rectangle (width, height)

void main() {
    // Scale the unit quad (0,0 to 1,1) by model_size and translate by model_pos
    vec2 pos = aPos * model_size + model_pos;
    gl_Position = projection * vec4(pos.x, pos.y, 0.0, 1.0);
}
)"glsl";

// Basic Fragment Shader
const char* fragmentShaderSource = R"glsl(
#version 330 core
out vec4 FragColor;     // Output color of the fragment

uniform vec3 objectColor; // Color of the object passed from the application

void main() {
    FragColor = vec4(objectColor, 1.0); // Set fragment color
}
)"glsl";


/**
 * @brief Creates an orthographic projection matrix.
 * This matrix transforms coordinates from world space to clip space.
 * @param mat Pointer to a 16-element float array to store the resulting matrix.
 * @param left The coordinate for the left vertical clipping plane.
 * @param right The coordinate for the right vertical clipping plane.
 * @param bottom The coordinate for the bottom horizontal clipping plane.
 * @param top The coordinate for the top horizontal clipping plane.
 * @param nearVal The distance to the near depth clipping plane.
 * @param farVal The distance to the far depth clipping plane.
 */
void ortho(float* mat, float left, float right, float bottom, float top, float nearVal, float farVal) {
    mat[0] = 2.0f / (right - left); mat[4] = 0.0f; mat[8] = 0.0f; mat[12] = -(right + left) / (right - left);
    mat[1] = 0.0f; mat[5] = 2.0f / (top - bottom); mat[9] = 0.0f; mat[13] = -(top + bottom) / (top - bottom);
    mat[2] = 0.0f; mat[6] = 0.0f; mat[10] = -2.0f / (farVal - nearVal); mat[14] = -(farVal + nearVal) / (farVal - nearVal);
    mat[3] = 0.0f; mat[7] = 0.0f; mat[11] = 0.0f; mat[15] = 1.0f;
}


Game::Game(GLuint width, GLuint height)
    : State(GAME_ACTIVE), Keys(), Width(width), Height(height), PlayerSnake(nullptr), GameFood(nullptr), VAO(0), VBO(0), shaderProgram(0) {
    // Constructor initializes game state, dimensions, and nullifies pointers.
    // Keys array is implicitly zero-initialized for GLboolean.
}

Game::~Game() {
    // Destructor cleans up dynamically allocated memory.
    delete PlayerSnake;
    delete GameFood;
    // Clean up OpenGL resources.
    glDeleteVertexArrays(1, &VAO);
    glDeleteBuffers(1, &VBO);
    glDeleteProgram(shaderProgram);
}

void Game::Init() {
    // Initialize rendering data (shaders, VAO/VBO for simple shapes)
    compileShaders();
    initRenderData();

    // Initialize game objects
    // Snake starts in the middle of the screen. Segment size is passed from Snake's constructor default.
    PlayerSnake = new Snake(Width / 2, Height / 2); 
    GameFood = new Food(Width, Height, PlayerSnake->GetSegmentSize());
    // Ensure food doesn't spawn on the snake initially.
    GameFood->GenerateNewPosition(PlayerSnake->GetBody()); 

    this->State = GAME_ACTIVE; // Set the initial game state.
}

/**
 * @brief Compiles vertex and fragment shaders and links them into a shader program.
 * Shader source code is embedded as strings. Error checking for compilation and linking is included.
 */
void Game::compileShaders() {
    // Vertex shader
    GLuint vertexShader = glCreateShader(GL_VERTEX_SHADER);
    glShaderSource(vertexShader, 1, &vertexShaderSource, NULL);
    glCompileShader(vertexShader);
    // Check for shader compile errors
    int success;
    char infoLog[512];
    glGetShaderiv(vertexShader, GL_COMPILE_STATUS, &success);
    if (!success) {
        glGetShaderInfoLog(vertexShader, 512, NULL, infoLog);
        std::cerr << "ERROR::SHADER::VERTEX::COMPILATION_FAILED\n" << infoLog << std::endl;
    }

    // Fragment shader
    GLuint fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
    glShaderSource(fragmentShader, 1, &fragmentShaderSource, NULL);
    glCompileShader(fragmentShader);
    // Check for shader compile errors
    glGetShaderiv(fragmentShader, GL_COMPILE_STATUS, &success);
    if (!success) {
        glGetShaderInfoLog(fragmentShader, 512, NULL, infoLog);
        std::cerr << "ERROR::SHADER::FRAGMENT::COMPILATION_FAILED\n" << infoLog << std::endl;
    }

    // Link shaders
    shaderProgram = glCreateProgram();
    glAttachShader(shaderProgram, vertexShader);
    glAttachShader(shaderProgram, fragmentShader);
    glLinkProgram(shaderProgram);
    // Check for linking errors
    glGetProgramiv(shaderProgram, GL_LINK_STATUS, &success);
    if (!success) {
        glGetProgramInfoLog(shaderProgram, 512, NULL, infoLog);
        std::cerr << "ERROR::SHADER::PROGRAM::LINKING_FAILED\n" << infoLog << std::endl;
    }
    glDeleteShader(vertexShader);
    glDeleteShader(fragmentShader);
}

/**
 * @brief Initializes Vertex Array Object (VAO) and Vertex Buffer Object (VBO) for rendering quads.
 * A simple quad is defined, and its vertex data is buffered to the GPU.
 */
void Game::initRenderData() {
    // A simple quad (two triangles)
    float vertices[] = {
        // pos      
        0.0f, 1.0f, 
        1.0f, 0.0f, 
        0.0f, 0.0f, 

        0.0f, 1.0f, 
        1.0f, 1.0f, 
        1.0f, 0.0f
    };

    glGenVertexArrays(1, &VAO);
    glGenBuffers(1, &VBO);

    glBindBuffer(GL_ARRAY_BUFFER, VBO);
    glBufferData(GL_ARRAY_BUFFER, sizeof(vertices), vertices, GL_STATIC_DRAW);

    glBindVertexArray(VAO);
    glEnableVertexAttribArray(0);
    glVertexAttribPointer(0, 2, GL_FLOAT, GL_FALSE, 2 * sizeof(float), (void*)0);
    glBindBuffer(GL_ARRAY_BUFFER, 0);
    glBindVertexArray(0);
}

/**
 * @brief Draws a rectangle on the screen using the compiled shader program.
 * Sets up the projection matrix, model position, size, and color uniforms.
 * @param x The x-coordinate of the rectangle's top-left corner.
 * @param y The y-coordinate of the rectangle's top-left corner.
 * @param rectWidth The width of the rectangle.
 * @param rectHeight The height of the rectangle.
 * @param r Red color component (0.0f - 1.0f).
 * @param g Green color component (0.0f - 1.0f).
 * @param b Blue color component (0.0f - 1.0f).
 */
void Game::DrawRectangle(float x, float y, float rectWidth, float rectHeight, float r, float g, float b) {
    glUseProgram(shaderProgram);
    
    float projection[16];
    ortho(projection, 0.0f, static_cast<float>(this->Width), static_cast<float>(this->Height), 0.0f, -1.0f, 1.0f);
    
    glUniformMatrix4fv(glGetUniformLocation(shaderProgram, "projection"), 1, GL_FALSE, projection);
    glUniform2f(glGetUniformLocation(shaderProgram, "model_pos"), x, y);
    glUniform2f(glGetUniformLocation(shaderProgram, "model_size"), rectWidth, rectHeight);
    glUniform3f(glGetUniformLocation(shaderProgram, "objectColor"), r, g, b);

    glBindVertexArray(VAO);
    glDrawArrays(GL_TRIANGLES, 0, 6);
    glBindVertexArray(0);
}


void Game::ProcessInput(GLfloat dt) {
    // Process input only if the game is active or over (for restart).
    if (this->State == GAME_ACTIVE) {
        // Set snake direction based on W/A/S/D or Arrow keys.
        // The Snake class handles logic to prevent immediate 180-degree turns.
        if (this->Keys[GLFW_KEY_W] || this->Keys[GLFW_KEY_UP])
            PlayerSnake->SetDirection(UP);
        if (this->Keys[GLFW_KEY_S] || this->Keys[GLFW_KEY_DOWN])
            PlayerSnake->SetDirection(DOWN);
        if (this->Keys[GLFW_KEY_A] || this->Keys[GLFW_KEY_LEFT])
            PlayerSnake->SetDirection(LEFT);
        if (this->Keys[GLFW_KEY_D] || this->Keys[GLFW_KEY_RIGHT])
            PlayerSnake->SetDirection(RIGHT);
    } else if (this->State == GAME_OVER) {
        // If game is over, check for 'R' key press to reset the game.
        if (this->Keys[GLFW_KEY_R]) {
            ResetGame();
        }
    }
}

void Game::Update(GLfloat dt) {
    // Do not update game logic if not in active state.
    if (this->State != GAME_ACTIVE) return;

    PlayerSnake->Move(); // Move the snake according to its current direction.

    // Check collision with food
    // If snake's head is at the same position as food.
    if (PlayerSnake->GetHeadPosition().x == GameFood->GetPosition().x &&
        PlayerSnake->GetHeadPosition().y == GameFood->GetPosition().y) {
        PlayerSnake->Grow(); // Make the snake grow.
        GameFood->GenerateNewPosition(PlayerSnake->GetBody()); // Generate new food at a valid position.
        // TODO: Increase score, potentially increase speed.
    }

    // Check collision with self
    if (PlayerSnake->CheckSelfCollision()) {
        this->State = GAME_OVER; // Set game state to GAME_OVER.
        std::cout << "Game Over! Press R to restart." << std::endl; // Notify player.
    }

    // Check collision with walls
    Position head = PlayerSnake->GetHeadPosition();
    int segmentSize = PlayerSnake->GetSegmentSize();
    // Check if head goes out of bounds.
    if (head.x < 0 || head.x + segmentSize > this->Width || head.y < 0 || head.y + segmentSize > this->Height) {
         this->State = GAME_OVER; // Set game state to GAME_OVER.
         std::cout << "Game Over! Hit a wall. Press R to restart." << std::endl; // Notify player.
    }
}

void Game::Render() {
    // Render snake and food if the game is active or over (to show final state).
    if (this->State == GAME_ACTIVE || this->State == GAME_OVER) { 
        // Draw snake: iterate through each segment and draw a rectangle.
        for (const auto& segment : PlayerSnake->GetBody()) {
            DrawRectangle(static_cast<float>(segment.x), static_cast<float>(segment.y),
                          static_cast<float>(PlayerSnake->GetSegmentSize()), static_cast<float>(PlayerSnake->GetSegmentSize()),
                          0.0f, 1.0f, 0.0f); // Green snake
        }
        // Draw food: draw a rectangle at the food's position.
        DrawRectangle(static_cast<float>(GameFood->GetPosition().x), static_cast<float>(GameFood->GetPosition().y),
                      static_cast<float>(PlayerSnake->GetSegmentSize()), static_cast<float>(PlayerSnake->GetSegmentSize()),
                      1.0f, 0.0f, 0.0f); // Red food
    }
    
    if (this->State == GAME_OVER) {
        // Simple text rendering is complex with raw OpenGL.
        // For now, we rely on console output for "Game Over" message.
        // A proper game would use a text rendering library or implement text rendering with textures.
        // TODO: Implement on-screen "Game Over" message and score.
    }
}

void Game::ResetGame() {
    std::cout << "Resetting game..." << std::endl;
    // Clean up old game objects.
    delete PlayerSnake;
    delete GameFood;
    // Create new game objects.
    PlayerSnake = new Snake(Width / 2, Height / 2);
    GameFood = new Food(Width, Height, PlayerSnake->GetSegmentSize());
    GameFood->GenerateNewPosition(PlayerSnake->GetBody());
    this->State = GAME_ACTIVE; // Set state back to active.
    // TODO: Reset score, speed etc. if implemented.
}

