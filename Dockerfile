FROM --platform=linux/amd64 eclipse-temurin:21-jre-jammy

WORKDIR /app

# 빌드된 jar 파일만 복사 (경로는 액션 설정과 맞춤)
COPY build/libs/*.jar app.jar

# 실행 권한 및 환경 설정
ENTRYPOINT ["java", "-jar", "app.jar"]