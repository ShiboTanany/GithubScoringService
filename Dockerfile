# Use a valid Maven image with JDK 21
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

# Copy pom.xml and pre-download dependencies
COPY pom.xml .

# Copy source and build
COPY src ./src
RUN mvn clean package

# Use minimal runtime JDK
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

COPY --from=build /app/target/GitHubScoreService-*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
