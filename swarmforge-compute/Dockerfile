# Build Stage
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy only modules needed for compute build
COPY pom.xml .
COPY swarmforge-core/pom.xml swarmforge-core/
COPY swarmforge-server/pom.xml swarmforge-server/
COPY swarmforge-compute/pom.xml swarmforge-compute/
COPY swarmforge-benchmarks/pom.xml swarmforge-benchmarks/
COPY swarmforge-plugins/pom.xml swarmforge-plugins/

# Strip GUI-only modules (editor, client) from reactor — not needed for compute build
RUN sed -i '/<module>swarmforge-editor<\/module>/d' pom.xml && \
    sed -i '/<module>swarmforge-client<\/module>/d' pom.xml

# Prefetch dependencies (cache layer)
RUN mvn dependency:go-offline -pl swarmforge-compute -am -DskipTests --no-transfer-progress || true

# Copy sources and build
COPY swarmforge-core/src swarmforge-core/src
COPY swarmforge-server/src swarmforge-server/src
COPY swarmforge-compute/src swarmforge-compute/src
COPY swarmforge-benchmarks/src swarmforge-benchmarks/src
COPY swarmforge-plugins/ swarmforge-plugins/

# Build Compute Node
RUN mvn clean package -pl swarmforge-compute -am -DskipTests --no-transfer-progress

# Runtime Stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/swarmforge-compute/target/swarmforge-compute-*.jar app.jar

ENV JAVA_OPTS="-Xms2g -Xmx6g -XX:+UseZGC -XX:+ZGenerational -XX:+UnlockDiagnosticVMOptions -XX:+UseSuperWord -XX:LoopUnrollLimit=1000 --add-modules jdk.incubator.vector -XX:+UseCompressedOops"

# Default Compute Port
EXPOSE 50052

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar \"$@\"", "--"]
