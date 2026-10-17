# ==============================================================================
# Stage 1: Build stage
# ==============================================================================
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /build

# Cache maven dependencies first
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build executable WAR
COPY src ./src
RUN mvn clean package -DskipTests -B

# ==============================================================================
# Stage 2: Runtime stage
# Packaging Choice: eclipse-temurin:21-jre with Spring Boot executable packaging
#
# Rationale:
# Spring Boot executable packaging (via spring-boot-maven-plugin repackage) bundles
# an embedded Tomcat web server directly within the WAR/JAR. Running on
# eclipse-temurin:21-jre provides:
#   1. Minimal footprint and attack surface compared to a full external Tomcat image.
#   2. Faster cold-start times and exact environment parity with 12-factor apps.
#   3. Direct control over JVM arguments, signals (SIGTERM/SIGINT), and process lifecycle.
#   4. Elimination of external Tomcat configuration, catalina lifecycle overhead,
#      and context path deployment mismatches.
# ==============================================================================
FROM eclipse-temurin:21-jre

# Metadata
LABEL maintainer="AMPT DevOps Team"
LABEL description="Agricultural Market Price Tracker (AMPT)"
LABEL version="1.0.0"

# Install curl for HEALTHCHECK
RUN apt-get update && \
    apt-get install -y --no-install-recommends curl && \
    rm -rf /var/lib/apt/lists/*

# Security: Create non-root user and group
RUN groupadd -r amptgroup && useradd -r -g amptgroup -m -d /home/amptuser amptuser

WORKDIR /app

# Copy built artifact from builder stage
COPY --from=builder /build/target/ampt.war /app/app.war

# Create data directory with proper ownership for non-root user
RUN mkdir -p /app/data && chown -R amptuser:amptgroup /app

# Switch to non-root user
USER amptuser:amptgroup

# Expose standard container port
EXPOSE 8080

# Environment variables
ENV SERVER_PORT=8080 \
    SPRING_PROFILES_ACTIVE=local \
    JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"

# Healthcheck monitoring Spring Boot Actuator endpoint
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Launch Spring Boot executable WAR using java -jar
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.war"]
