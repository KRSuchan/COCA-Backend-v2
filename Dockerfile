# 1단계: Gradle 빌드
FROM gradle:8.3-jdk17 AS builder
WORKDIR /cocaBack
COPY ./coca-app.jar coca-app.jar
RUN gradle build -x test

# 2단계: 실행용 경량 이미지
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "coca-app.jar"]
