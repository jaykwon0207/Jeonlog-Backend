# Exhibition Recommender Backend

전시 정보 조회, 검색, 기록, 북마크, 알림 기능을 제공하는 Spring Boot 기반 백엔드입니다.
사용자의 전시 활동 데이터를 기반으로 선호 장르/분위기를 누적하고, 이를 활용해 개인화 추천과 인기 추천을 제공합니다.

## 주요 기능

- 개인화 전시 추천
- 인기 전시 추천
- 전시/공간 탐색 및 검색
- 사용자 활동 기능(북마크, 전시 기록, 좋아요/스크랩)
- 운영 기능(푸시 알림, 신고 처리, 웹훅 재시도)

## 빠른 실행

```bash
./gradlew bootRun
```

## 테스트

```bash
./gradlew --no-daemon --stacktrace clean test
```
