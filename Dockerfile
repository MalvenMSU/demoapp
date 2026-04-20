# -------------------------------------------------------------
# Stage 1: Build the application securely
# -------------------------------------------------------------
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /build

# Copy the Gradle wrapper
COPY gradlew .
COPY gradle gradle

# Make gradle wrapper executable
RUN chmod +x gradlew

# Copy build configuration
COPY build.gradle settings.gradle gradle.properties ./
COPY config config
COPY src src

# Execute Gradle build (creates the executable jar)
# We skip tests and static analysis here as they are already handled by the CI pipeline
RUN ./gradlew bootJar -x test -x checkstyleMain -x spotbugsMain --no-daemon

# -------------------------------------------------------------
# Stage 2: Create a minimal, hardened production image
# -------------------------------------------------------------
# Hardening: Using a minimal alpine base image exclusively featuring the JRE (No JDK/compiler)
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Hardening: Create a non-root user and group
# Running as root inside a container is a major security risk.
RUN addgroup -S demogroup && adduser -S demouser -G demogroup

# Hardening: Switch immediately to the non-root user
USER demouser:demogroup

# Copy the compiled JAR artifact from the builder stage
# Hardening: Explicitly assign ownership to the non-root user
COPY --from=builder --chown=demouser:demogroup /build/build/libs/demoapp-0.0.1-SNAPSHOT.jar app.jar

# Hardening / Configuration:
# Secrets (DB passwords, API keys) must NEVER be hardcoded in the Dockerfile or source.
# Instead, they are passed as environment variables by the container orchestrator (e.g., Kubernetes, Docker Compose)
# leaving these as blank declarations to document the requirement.
ENV DATABASE_URL=""
ENV DATABASE_USERNAME=""
ENV DATABASE_PASSWORD=""

# Expose standard application port
EXPOSE 8080

# Execute the application
ENTRYPOINT ["java", "-jar", "app.jar"]
