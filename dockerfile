# ----------------------------
# 1. Build stage
# ----------------------------
FROM maven:3.9.4-eclipse-temurin-17 AS builder
WORKDIR /app

# Copy pom.xml and download dependencies first (to use Docker cache)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the source and build the application
COPY src ./src
RUN mvn clean package -DskipTests

# ----------------------------
# 2. Runtime stage
# ----------------------------
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

# Copy the built JAR from the builder image
COPY --from=builder /app/target/*.jar app.jar

# Expose the Spring Boot default port
EXPOSE 8080

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]

