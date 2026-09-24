# =============================================================================
# Royal Pearl Hyderabad – Multi-stage Dockerfile
# Stage 1: Build with Maven + JDK 21
# Stage 2: Minimal runtime with JRE 21
# =============================================================================

# ---- Stage 1: Build ---------------------------------------------------------
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /build

# Copy dependency manifests first for layer caching
COPY pom.xml .
COPY .mvn/ .mvn/

# Download dependencies (cached unless pom.xml changes)
RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline -q 2>/dev/null || true

# Copy source and build (skip tests – run separately in CI)
COPY src ./src

RUN --mount=type=cache,target=/root/.m2 \
    mvn package -DskipTests -q && \
    ls -la target/*.jar

# ---- Stage 2: Runtime -------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine AS runtime

# Non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copy fat JAR from builder
COPY --from=builder /build/target/hotel-*.jar app.jar

# Change ownership
RUN chown appuser:appgroup app.jar

USER appuser

# Actuator health port (same as app port)
EXPOSE 8080

# JVM tuning for containers: respect cgroup memory limits, G1GC
ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:+UseG1GC \
               -XX:+ExitOnOutOfMemoryError \
               -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
