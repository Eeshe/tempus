FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

COPY gradle/ gradle/
COPY gradlew build.gradle.kts ./
RUN ./gradlew dependencies --no-daemon

COPY src/ src/

RUN ./gradlew bootJar -x test --no-daemon

FROM eclipse-temurin:25-jre
WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
