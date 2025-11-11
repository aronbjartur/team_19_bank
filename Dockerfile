# Use an official OpenJDK image as the base image
FROM eclipse-temurin:21-jdk-alpine

# Set the working directory inside the container
WORKDIR /app
# Copy the Maven wrapper and project files into the container
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Download dependencies without running tests
RUN ./mvnw dependency:go-offline -B

# Copy the source code into the container
COPY src/ src/
# Copy the Maven wrapper and project files into the container
RUN ./mvnw clean package -DskipTests

# Copy the JAR file from the target directory into the container
COPY target/team_19_bank-0.0.1-SNAPSHOT.jar app.jar

# Expose the port your Spring Boot application runs on
EXPOSE 8080

# Define the command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
