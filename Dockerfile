# Use a Java runtime as the base image
FROM openjdk:11

# Copy the JAR file to the container
COPY build/libs/doodle_king.jar /app.jar

# Set the entry point to run the JAR file
ENTRYPOINT ["java", "-jar", "/app.jar"]
