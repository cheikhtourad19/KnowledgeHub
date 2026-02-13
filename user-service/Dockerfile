# Multi-stage build for Spring Boot application
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /build
# Copy entire project for build
COPY . .
# Build the specific module
RUN mvn -pl user-service -am clean package -DskipTests
# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Copy the built jar from build stage
COPY --from=build /build/user-service/target/*.jar app.jar
# Create non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8082/actuator/health || exit 1
# Expose port
EXPOSE 8082
# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
