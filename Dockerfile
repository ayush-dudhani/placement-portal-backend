# ---------- Build Stage ----------
FROM maven:3.9.9-eclipse-temurin-17 AS builder

WORKDIR /app

COPY pom.xml .
COPY src ./src

# Cache dependencies and build
RUN mvn clean package

# ---------- Runtime Stage ----------
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=builder /app/target/*.jar placement-portal.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "placement-portal.jar"]