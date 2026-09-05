# ---- Build stage ----
FROM eclipse-temurin:21-jdk-noble AS build
WORKDIR /app

COPY gradlew settings.gradle build.gradle ./
COPY gradle gradle
RUN chmod +x gradlew && ./gradlew --no-daemon dependencies

COPY src src
RUN ./gradlew --no-daemon build

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre-noble AS runtime
WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends curl \
	&& rm -rf /var/lib/apt/lists/*

RUN groupadd --system spring && useradd --system --gid spring --no-create-home spring
USER spring

COPY --from=build /app/build/libs/*.jar app.jar

# APP (8080) - HEALTHCHECK (8081)
EXPOSE 8080 8081

HEALTHCHECK --interval=30s --timeout=3s --start-period=20s --retries=3 \
	CMD curl -f http://localhost:8081/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
