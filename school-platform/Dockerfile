# syntax=docker/dockerfile:1

# ---- Build stage: Maven + JDK 21 (no host toolchain required) ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Cache dependencies: copy every module POM first, then resolve offline.
COPY pom.xml .
COPY common-library/pom.xml common-library/
COPY module-auth/pom.xml module-auth/
COPY module-student/pom.xml module-student/
COPY module-staff/pom.xml module-staff/
COPY module-academics/pom.xml module-academics/
COPY module-finance/pom.xml module-finance/
COPY module-communication/pom.xml module-communication/
COPY module-reporting/pom.xml module-reporting/
COPY module-ai-client/pom.xml module-ai-client/
COPY app/pom.xml app/
RUN mvn -q -B -e dependency:go-offline || true

# Copy sources and build (runs unit + ArchUnit tests; no DB/Keycloak needed).
COPY . .
RUN mvn -q -B -e clean package

# ---- Runtime stage: slim JRE ----
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app
RUN groupadd --system spring && useradd --system --gid spring spring
COPY --from=build /workspace/app/target/school-platform-app.jar app.jar
USER spring
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
