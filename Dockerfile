# 1. 실행 환경 설정 (자바 21 기반, ARM 아키텍처 지원 이미지)
FROM eclipse-temurin:21-jre-jammy

# 2. 컨테이너 내부 작업 디렉토리 설정
WORKDIR /app

# 3. 빌드된 jar 파일을 컨테이너 내부로 복사
# Gradle 빌드 시 생성되는 jar 파일의 이름을 확인해주세요 (보통 *-SNAPSHOT.jar)
COPY build/libs/*[!plain].jar app.jar

# 4. 앱 실행 명령어
ENTRYPOINT ["java", "-jar", "app.jar"]