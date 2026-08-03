# ── Stage 1: Build ───────────────────────────────────────────────────────────
# Maven image has mvn + JAVA_HOME pre-configured — no need for mvnw
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy pom first (layer cache: only re-download deps if pom changes)
COPY NewNovaLearn/pom.xml ./
RUN mvn dependency:resolve -q

# Copy source and build the fat JAR
COPY NewNovaLearn/src ./src
RUN mvn clean package -DskipTests -q

# ── Stage 2: Runtime ──────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Create a non-root user for security
RUN groupadd --system novalearn && useradd --system --gid novalearn novalearn

COPY --from=build /app/target/novalearn-0.0.1-SNAPSHOT.jar app.jar

# Uploads directory
RUN mkdir -p uploads && chown -R novalearn:novalearn /app

USER novalearn

EXPOSE 8080

ENTRYPOINT ["java", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
