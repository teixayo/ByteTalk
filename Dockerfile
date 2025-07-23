FROM gradle:8.4-jdk21-alpine AS builder
WORKDIR /home/gradle/project

COPY settings.gradle.kts settings.gradle.kts
COPY gradle gradle
COPY backend/build.gradle.kts build.gradle.kts
COPY backend/src src

RUN gradle shadowJar -x test --parallel

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=builder /home/gradle/project/build/libs/ByteTalk-ByteTalk.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
