# Project Name

## Overview
ICB Backend

## Table of Contents
- [Installation](#installation)
- [Usage](#usage)
- [Configuration](#configuration)
- [Running the Application](#running-the-application)
- [Testing](#testing)

## Installation
1. **Clone the repository:**
   ```bash
   git clone https://github.com/JosephSe/icb-back.git
   ```
2. **Navigate to the project directory:**
   ```bash
   cd icb-back
   ```
3. **Build the project using Gradle:**
   ```bash
   ./gradlew build
   ```

## Usage
Provide instructions on how to use the application. Include any necessary information about the API endpoints, user interface, or command-line options.

## Configuration
Explain any configuration settings that need to be adjusted. This might include database settings, environment variables, or application properties.

## Running the Application
1. **Start the application:**
   ```bash
   ./gradlew bootRun
   ```
2. **Access the application:**
   - Open your browser and go to `http://localhost:8080`.

## Testing
Explain how to run the tests for your application. Include any specific commands or tools required.

```bash
./gradlew test
```

# Build the Docker image
```bash
docker build -t uk-go-hm:latest .
```

# Run the container
```bash
docker run -p 8080:8080 uk-go-hm:latest
```