# 전:록 서버 아키텍처

> 서버 담당자용

---

```
                          ┌─────────────────────────────────────────────────────────┐
                          │                      AWS Cloud (ap-northeast-2)         │
                          │                                                         │
    Mobile App ──────┐    │  ┌──────────────┐    ┌──────────────────────────────┐   │
    (iOS/Android)    │    │  │     ALB      │    │  Elastic Beanstalk           │   │
                     ├────┼─▶│  (public,    │───▶│  Jeonlog-prod-env            │   │
    Web Client ──────┘    │  │  2 subnets)  │    │                              │   │
    (jeonlog.com)         │  └──────────────┘    │  ┌────────────────────────┐  │   │
                          │                      │  │ EC2 (t3.small)         │  │   │
                          │                      │  │ Corretto 25 / AL2023   │  │   │
                          │                      │  │                        │  │   │
                          │                      │  │  Spring Boot 3.3.5     │  │   │
                          │                      │  │  Java 21               │  │   │
                          │                      │  │  (single JAR)          │  │   │
                          │                      │  └────────────────────────┘  │   │
                          │                      │  Auto Scaling: 1 ~ 4        │   │
                          │                      └──────────────────────────────┘   │
                          │                                    │                    │
                          │           ┌────────────────────────┼──────────┐         │
                          │           ▼                        ▼          ▼         │
                          │  ┌──────────────┐  ┌───────────────┐  ┌────────────┐   │
                          │  │  RDS MySQL   │  │   S3 Bucket   │  │ Secrets    │   │
                          │  │              │  │   (media)     │  │ Manager    │   │
                          │  └──────────────┘  └───────┬───────┘  │(Firebase)  │   │
                          │                            │          └────────────┘   │
                          │                            ▼                           │
                          │                    ┌───────────────┐                   │
                          │                    │  CloudFront   │                   │
                          │                    │  (CDN)        │                   │
                          │                    └───────────────┘                   │
                          └─────────────────────────────────────────────────────────┘
                                                      │
                              ┌────────────────────────┼──────────────────┐
                              ▼                        ▼                  ▼
                      ┌──────────────┐      ┌──────────────┐    ┌──────────────┐
                      │  Google      │      │  Naver       │    │  Apple       │
                      │  OAuth2      │      │  OAuth2      │    │  OAuth       │
                      └──────────────┘      └──────────────┘    └──────────────┘
                              │                                         │
                              ▼                                         ▼
                      ┌──────────────┐                          ┌──────────────┐
                      │  Firebase    │                          │  Discord     │
                      │  FCM (Push)  │                          │  Webhooks    │
                      └──────────────┘                          └──────────────┘
```

---

## EC2 / EB

| 항목 | 값 |
|---|---|
| 플랫폼 | Corretto 25 on AL2023 |
| 인스턴스 | t3.small (1~4대, 오토스케일) |
| 스케일링 | NetworkOut 기준 (5분) |
| 로드밸런서 | ALB (public) |

---

## 배포

- CI: GitHub Actions → Gradle test → EB deploy
- Predeploy: Firebase credential (Secrets Manager → 파일)

---

## 스택

- Spring Boot 3.3.5 / Java 21
- JPA + Hibernate + QueryDSL
- MySQL (RDS)

---

## 인증

- JWT (Access 15분, Refresh 14일)
- OAuth2: Google, Naver, Apple
- ⚠️ 토큰 블랙리스트 없음

---

## 외부 연동

| 서비스 | 용도 |
|---|---|
| Firebase FCM | 푸시 알림 |
| S3 + CloudFront | 미디어 |
| Discord Webhook | 신고/차단 알림 |

---

## 스케줄러 (⚠️ 주의)

| Job | Cron | 설명 |
|---|---|---|
| ExhibitionEndingSoonScheduler | `0 0 11 * * *` | 종료 14일 전 전시 알림 |
| BookmarkCleanupService | `0 0 1 * * *` | 종료된 전시 북마크 삭제 |

**오토스케일(1~4대)에서 인스턴스마다 중복 실행됨** → ShedLock 필요

---

## 확인 필요

1. 스케줄러 분산락 (ShedLock)
2. JWT revoke 메커니즘
3. FCM 실패시 토큰 정리
4. 미사용 의존성 제거 (spring-batch, caffeine)
