# Build Stage
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy parent pom and only the modules needed for server build
COPY pom.xml .
COPY swarmforge-core/pom.xml swarmforge-core/
COPY swarmforge-server/pom.xml swarmforge-server/
COPY swarmforge-compute/pom.xml swarmforge-compute/
COPY swarmforge-benchmarks/pom.xml swarmforge-benchmarks/
COPY swarmforge-plugins/pom.xml swarmforge-plugins/

# Strip GUI-only modules (editor, client) from reactor — they are not needed for cloud build
RUN sed -i '/<module>swarmforge-editor<\/module>/d' pom.xml && \
    sed -i '/<module>swarmforge-client<\/module>/d' pom.xml

# Prefetch dependencies (cache layer)
RUN mvn dependency:go-offline -pl swarmforge-server -am -DskipTests --no-transfer-progress || true

# Copy sources and build
COPY swarmforge-core/src swarmforge-core/src
COPY swarmforge-server/src swarmforge-server/src
COPY swarmforge-compute/src swarmforge-compute/src
COPY swarmforge-benchmarks/src swarmforge-benchmarks/src
COPY swarmforge-plugins/ swarmforge-plugins/
RUN mvn clean package -pl swarmforge-server -am -DskipTests --no-transfer-progress

# Runtime Stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/swarmforge-server/target/swarmforge-server-*.jar app.jar

EXPOSE 8080 50051

ENTRYPOINT ["java", "-jar", "app.jar"]
