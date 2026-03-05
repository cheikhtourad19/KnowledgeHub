# Multi-stage build with dependency caching
FROM maven:3.9-eclipse-temurin-21-alpine AS dependencies

WORKDIR /build

# Copy only POM files first for dependency resolution
COPY pom.xml .
COPY api-gateway/pom.xml api-gateway/
COPY auth-service/pom.xml auth-service/
COPY user-service/pom.xml user-service/
COPY resource-service/pom.xml resource-service/
COPY notification-service/pom.xml notification-service/
COPY notification-client/pom.xml notification-client/

# Download dependencies (cached layer)
RUN mvn dependency:go-offline -pl user-service -am || true

# Build stage
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /build

# Copy dependencies from cache stage
COPY --from=dependencies /root/.m2 /root/.m2

# Copy POM files
COPY pom.xml .
COPY api-gateway/pom.xml api-gateway/
COPY auth-service/pom.xml auth-service/
COPY user-service/pom.xml user-service/
COPY resource-service/pom.xml resource-service/
COPY notification-service/pom.xml notification-service/
COPY notification-client/pom.xml notification-client/

# Copy source code
COPY user-service/src user-service/src
COPY notification-client/src notification-client/src

# Build (dependencies already cached)
RUN mvn -pl user-service -am clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=build /build/user-service/target/*.jar app.jar

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8082/actuator/health || exit 1

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
