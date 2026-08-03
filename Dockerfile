# ── Stage 1: Build ───────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app

# eclipse-temurin images install Java under /opt/java/openjdk
ENV JAVA_HOME=/opt/java/openjdk
ENV PATH="${JAVA_HOME}/bin:${PATH}"

# Copy Maven wrapper and pom first (layer cache: only re-download deps if pom changes)
COPY NewNovaLearn/.mvn/ .mvn/
COPY NewNovaLearn/mvnw NewNovaLearn/pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:resolve -q

# Copy source and build the fat JAR
COPY NewNovaLearn/src ./src
RUN ./mvnw clean package -DskipTests -q

# ── Stage 2: Runtime ──────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

ENV JAVA_HOME=/opt/java/openjdk
ENV PATH="${JAVA_HOME}/bin:${PATH}"

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
