# Build Stage
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

COPY . .

# Build Compute Node
RUN mvn clean package -pl swarmforge-compute -am -DskipTests

# Runtime Stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/swarmforge-compute/target/swarmforge-compute-*.jar app.jar

# Default Compute Port
EXPOSE 50052

ENTRYPOINT ["java", "-jar", "app.jar"]
