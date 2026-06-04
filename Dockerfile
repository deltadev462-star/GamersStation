# Multi-stage build for optimized image size
FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests -B

# Production image
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -g 1000 -S spring && \
    adduser -u 1000 -S spring -G spring

RUN mkdir -p /app/uploads && chown -R spring:spring /app

USER spring:spring

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]