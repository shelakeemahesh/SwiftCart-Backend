# Build stage
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /build

# Cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Compile and package application
COPY src ./src
RUN mvn clean package -DskipTests -B

# Production runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create unprivileged system group and user
RUN addgroup -S swiftcart && adduser -S swiftcart -G swiftcart

# Copy jar from build stage with correct ownership
COPY --from=build --chown=swiftcart:swiftcart /build/target/*.jar app.jar

# Run container as non-root user
USER swiftcart:swiftcart

EXPOSE 8080

# Health check using Actuator
HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health | grep UP || exit 1

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
