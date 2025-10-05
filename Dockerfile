# Build stage
FROM maven:3.9.5-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/gas-tracker-1.0.0.jar app.jar
EXPOSE $PORT
CMD java -Dserver.port=$PORT -jar app.jar
