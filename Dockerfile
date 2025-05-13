# 1단계: 빌드 전용 단계
FROM --platform=linux/amd64 openjdk:17-jdk-slim as builder

WORKDIR /app
COPY . .
RUN apt-get update && apt-get install -y dos2unix && \
    dos2unix gradlew && chmod +x gradlew && \
    ./gradlew bootJar --no-daemon

# 2단계: 실행 단계
FROM --platform=linux/amd64 openjdk:17-jdk-slim

WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-Dspring.profiles.active=docker", "-jar", "app.jar"]
