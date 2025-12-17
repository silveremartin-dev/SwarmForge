# Build Stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy parent pom first for caching
COPY pom.xml .
COPY swarmforge-core/pom.xml swarmforge-core/
COPY swarmforge-server/pom.xml swarmforge-server/
COPY swarmforge-compute/pom.xml swarmforge-compute/
COPY swarmforge-editor/pom.xml swarmforge-editor/

# Download dependencies (fail-safe if child modules need parent)
# We might need to copy all poms up front to allow reactor build
# For simplicity, we copy the whole source, but efficient caching would be better.
COPY . .

# Build Server using reactor to ensure core is built
RUN mvn clean package -pl swarmforge-server -am -DskipTests

# Runtime Stage
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/swarmforge-server/target/swarmforge-server-*.jar app.jar

EXPOSE 8080 50051

ENTRYPOINT ["java", "-jar", "app.jar"]
