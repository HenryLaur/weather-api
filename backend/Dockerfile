# Build
FROM gradle:8.7-jdk17-alpine AS build
WORKDIR /app
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle ./gradle
RUN gradle dependencies --no-daemon
COPY src/main/resources/static/city.list.json ./static/city.list.json
COPY src ./src
RUN gradle build --no-daemon -x test

# Run
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]