# ---- Stage 1: Build ----
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder
WORKDIR /build

# Cache dependency layer separately from source code
COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn package -B -DskipTests

# ---- Stage 2: Runtime ----
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

# Non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Upload directory (overridden by volume mount in production)
RUN mkdir -p /app/uploads && chown appuser:appgroup /app/uploads

COPY --from=builder /build/target/ai-platform-backend-0.0.1-SNAPSHOT.jar app.jar

USER appuser
EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
