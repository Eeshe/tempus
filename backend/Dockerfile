# Build Angular frontend
FROM node:24-alpine AS frontend
WORKDIR /frontend
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

# Build Spring Boot backend (with frontend baked into static)
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app
COPY backend/gradle/ gradle/
COPY backend/gradlew backend/build.gradle.kts ./
RUN ./gradlew dependencies --no-daemon
COPY backend/src/ src/
RUN rm -rf src/main/resources/static
COPY --from=frontend /frontend/dist/tempus/browser/ src/main/resources/static/
RUN ./gradlew bootJar -x test --no-daemon

# Runtime
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
