# Build stage
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Copy gradle files first for better layer caching
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle* .
COPY gradle.properties* .

# Give execution permissions to gradlew
RUN chmod +x ./gradlew

# Download dependencies (will be cached if no changes)
RUN ./gradlew dependencies --no-daemon

# Copy the source code
COPY src src

# Build the application
RUN ./gradlew bootJar --no-daemon

# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app

# Add a non-root user to run the application
RUN addgroup --system --gid 1001 appuser && \
    adduser --system --uid 1001 --gid 1001 --no-create-home appuser

# Copy the JAR from the build stage
COPY --from=build /app/build/libs/uk-go-hm-0.0.1-SNAPSHOT.jar app.jar

# Set ownership
RUN chown -R appuser:appuser /app
USER appuser

# Expose the application port (adjust if your app uses a different port)
EXPOSE 8080

# Set the startup command
ENTRYPOINT ["java", "-jar", "/app/app.jar"]