# Build Stage
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy parent pom first for caching
COPY pom.xml .
COPY swarmforge-core/pom.xml swarmforge-core/
COPY swarmforge-server/pom.xml swarmforge-server/
COPY swarmforge-compute/pom.xml swarmforge-compute/
COPY swarmforge-editor/pom.xml swarmforge-editor/

# Build Server using reactor to ensure core is built
COPY . .
RUN mvn clean package -pl swarmforge-server -am -DskipTests

# Runtime Stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/swarmforge-server/target/swarmforge-server-*.jar app.jar

EXPOSE 8080 50051

ENTRYPOINT ["java", "-jar", "app.jar"]
