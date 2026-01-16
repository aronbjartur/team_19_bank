# Stage 1: Build the application
# We use JDK 21 as seen in your earlier logs
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Copy the maven wrapper and pom first to cache dependencies
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline -B

# Copy source and build the JAR
COPY src/ src/
RUN ./mvnw clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Crucial fix: COPY --from=build tells Docker to grab the file
# from the 'build' stage above, NOT from your GitHub files.
COPY --from=build /app/target/team_19_bank-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
