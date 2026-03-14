# [Stage 1: Build]
FROM gradle:8.5-jdk21 AS build
WORKDIR /app
COPY . .
# 권한 부여 및 빌드 (테스트 제외)
RUN chmod +x gradlew
RUN ./gradlew clean build -x test

# [Stage 2: Run]
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
# 빌드 스테이지에서 생성된 jar만 복사
COPY --from=build /app/build/libs/*[!plain].jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]