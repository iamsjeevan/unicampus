# backend/Dockerfile
# Simple Dockerfile assuming JAR and key are handled correctly

# Use an official OpenJDK runtime as a parent image (Java 17 JRE)
FROM eclipse-temurin:17-jre-jammy

# Set the working directory inside the container
WORKDIR /app

# Argument for the application JAR name/path (relative to context)
ARG JAR_FILE=backend/target/*.jar

# Copy the Firebase key from the resources directory within the context
# Assumes build context is repo root, key is in backend/src/main/resources
COPY backend/src/main/resources/firebase-service-account.json /app/firebase-key.json

# Copy the packaged application JAR file into the container
COPY ${JAR_FILE} app.jar

# Make port 8080 available
EXPOSE 8080

# Define the command to run your application
# The FirebaseConfig will look for the key at /app/firebase-key.json
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
