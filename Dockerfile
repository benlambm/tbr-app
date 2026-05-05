# --- Build stage ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# --- Runtime stage ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /workspace/target/*.jar /app/app.jar

# Cold-start optimization, step 1: unpack Spring Boot's executable jar into
# its layered layout. Without this, CDS can only cache JDK classes, not
# Spring/application classes — much smaller gain.
RUN java -Djarmode=tools -jar /app/app.jar extract --destination /app/extracted

# Cold-start optimization, step 2: training run to generate a CDS (Class
# Data Sharing) archive. Boots Spring just far enough to load all beans,
# then exits via spring.context.exit=onRefresh. The resulting .jsa file
# lets the runtime JVM skip class-loading work on every cold start.
# Uses the default H2 profile (no external DB needed during build).
RUN java -XX:ArchiveClassesAtExit=/app/extracted/app.jsa \
         -Dspring.context.exit=onRefresh \
         -jar /app/extracted/app.jar

EXPOSE 8080
# -XX:SharedArchiveFile uses the CDS archive built above.
# -XX:TieredStopAtLevel=1 skips the C2 optimizing JIT compiler for faster
# boot (lower peak throughput, fine for a low-traffic class demo).
ENTRYPOINT ["java", \
            "-XX:SharedArchiveFile=/app/extracted/app.jsa", \
            "-XX:TieredStopAtLevel=1", \
            "-jar", "/app/extracted/app.jar"]
