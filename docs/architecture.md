# 전:록 (Jeonlog) - Server Architecture

> Last updated: 2026-03-13 | Auto-generated from codebase analysis + AWS EB console

---

## 1. System Overview

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

## 2. Compute & Scaling (Elastic Beanstalk)

| 항목 | 값 |
|---|---|
| **Environment** | `Jeonlog-prod-env` |
| **Application** | `jeonlog-prod` |
| **Platform** | Corretto 25 on 64bit Amazon Linux 2023 |
| **Instance Type** | `t3.small` (2 vCPU, 2 GiB RAM) |
| **AMI** | `ami-0a0d6c6b98f3d32cd` |
| **Processor** | x86_64 |
| **Availability Zone** | Any |
| **Procfile** | `web: java -jar build/libs/app.jar` |

### Auto Scaling Policy

| 항목 | 값 |
|---|---|
| **환경 유형** | 밸런싱된 로드 (Load Balanced) |
| **최소 인스턴스** | 1 |
| **최대 인스턴스** | 4 |
| **플릿 구성** | 온디맨드 인스턴스 |
| **스케일링 쿨다운** | 360초 |
| **지표** | NetworkOut (Average, Bytes) |
| **측정 기간** | 5분 |
| **위반 기간** | 5분 |
| **스케일 업** | 상위 임계값 6,000,000 bytes 초과 시 +1 |
| **스케일 다운** | 하위 임계값 2,000,000 bytes 미만 시 -1 |

### Load Balancer

| 항목 | 값 |
|---|---|
| **유형** | Application Load Balancer (ALB) |
| **가시성** | public |
| **서브넷** | `subnet-072242fa0fbb9b7b0`, `subnet-0a83d0a2cd958c207` |
| **IP 주소 유형** | IPv4 |
| **공유** | false |
| **로그 저장** | 비활성화 |

### Security Groups

- `sg-01c45dcc2eedb7fe5`
- `sg-0e72630115eca1443`

> **주의사항**: `@Scheduled` 잡(아래 참고)이 앱 인스턴스 내부에서 실행됩니다.
> 현재 최소 1 ~ 최대 4 오토스케일 구성이므로, **스케일 아웃 시 스케줄러가 인스턴스 수만큼 중복 실행**될 수 있습니다.
> 현재 코드에 분산 락(ShedLock 등)이 없으므로, 멀티 인스턴스 운영 시 중복 방지 대책이 필요합니다.

---

## 3. Application Layer

### 3.1 Framework & Runtime

| 항목 | 값 |
|---|---|
| **Framework** | Spring Boot 3.3.5 |
| **Language** | Java 21 |
| **Build Tool** | Gradle |
| **ORM** | Spring Data JPA + Hibernate + QueryDSL 5.0 |
| **DB** | MySQL (로컬: localhost, 프로덕션: RDS) |
| **Auth** | Spring Security + OAuth2 + JWT (jjwt 0.11.5) |
| **API Docs** | Swagger (springdoc-openapi 2.5.0) |
| **HTTP Client** | Spring WebFlux WebClient (outbound calls) |
| **File Upload** | AWS S3 Presigned URL (서버 프록시 없음) |
| **CDN** | CloudFront (`d1qinuzqu6uuo0.cloudfront.net`) |
| **Push** | Firebase Admin SDK 9.2.0 (FCM) |
| **Cache 라이브러리** | Caffeine (의존성 있음, 코드 미사용) |
| **Batch 라이브러리** | Spring Batch (의존성 있음, 코드 미사용) |

### 3.2 Layered Architecture

```
┌─────────────────────────────────────────────────────┐
│                  Security Filter Chain               │
│  JwtAuthenticationFilter → OAuth2 Login Flow         │
├─────────────────────────────────────────────────────┤
│                                                      │
│  @RestController  (29 controllers, /api/** prefix)   │
│    ├── auth/          (4 controllers)                │
│    ├── user/          (4 controllers)                │
│    ├── exhibition/    (2 controllers)                │
│    ├── search/        (1 controller)                 │
│    ├── record/        (1 controller)                 │
│    ├── bookmark/      (1 controller)                 │
│    ├── scrap/         (1 controller)                 │
│    ├── like/          (1 controller)                 │
│    ├── comment/       (1 controller)                 │
│    ├── report/        (2 controllers)                │
│    ├── notification/  (3 controllers)                │
│    ├── recommendation/(5 controllers)                │
│    └── common/        (3 controllers)                │
│                                                      │
├─────────────────────────────────────────────────────┤
│  @Service  (비즈니스 로직, @Transactional)             │
│    ├── 추천 엔진 (장르/분위기 기반 + 인기/나이/성별)      │
│    ├── 알림 서비스 (좋아요/댓글/팔로우/전시종료/공지)      │
│    ├── Discord Webhook (신고 접수 / 사용자 차단)        │
│    └── S3 Presigned URL 생성                          │
│                                                      │
├─────────────────────────────────────────────────────┤
│  @Repository  (Spring Data JPA + QueryDSL)           │
│    └── 25 Entity 클래스 → MySQL                       │
│                                                      │
└─────────────────────────────────────────────────────┘
```

### 3.3 Request Flow (End-to-End)

```
Client Request
    │
    ▼
ALB (public, 2 subnets)
    │
    ▼
SecurityFilterChain
    ├── JwtAuthenticationFilter
    │     ├── Authorization: Bearer {token} 파싱
    │     ├── JwtTokenProvider.validateToken()
    │     ├── subject에서 OauthProvider:oauthId 추출
    │     └── UserRepository로 User 조회 → SecurityContext 세팅
    │
    ▼
DispatcherServlet
    │
    ▼
@RestController (요청 매핑)
    │
    ▼
@Service (비즈니스 로직 + @Transactional)
    │
    ▼
@Repository (JPA → Hibernate → MySQL)
    │
    ▼
ApiResponse<T> (공통 응답 포맷: { success, data, code, message, timestamp })
```

---

## 4. Authentication & Authorization

### 4.1 OAuth2 Providers

| Provider | Web (OAuth2 Login) | Mobile (Token 검증) |
|---|---|---|
| **Google** | Spring Security OAuth2 Client | `MobileOAuthProfileService` → `oauth2.googleapis.com/tokeninfo` |
| **Naver** | Spring Security OAuth2 Client | `MobileOAuthProfileService` → `openapi.naver.com/v1/nid/me` |
| **Apple** | - | `AppleTokenService` → `appleid.apple.com/auth/token` |

### 4.2 JWT Token Lifecycle

| Token | 유효기간 | Subject 형식 |
|---|---|---|
| **Access Token** | 15분 | `{OauthProvider}:{oauthId}` |
| **Refresh Token** | 14일 | `{OauthProvider}:{oauthId}` |
| **Temp Token** | 가변 | `TEMP` (온보딩용) |

### 4.3 Public Endpoints (인증 불필요)

```
/, /favicon.ico, /error
/oauth2/**, /api/oauth/**, /api/auth/**
/swagger-ui/**, /v3/api-docs/**
/api/health
/api/users/check-nickname, /api/users/search
OPTIONS **
```

---

## 5. External Service Integrations

```
┌──────────────────────────┬──────────────────────────────────────────────────────┐
│ Service                  │ Usage                                                │
├──────────────────────────┼──────────────────────────────────────────────────────┤
│ Google OAuth2 API        │ ID token 검증 (모바일), OAuth2 로그인 (웹)             │
│ Naver OpenAPI            │ 프로필 조회 (모바일), OAuth2 로그인 (웹)                │
│ Apple Auth               │ authorization_code → token 교환 (모바일)              │
│ Firebase FCM             │ 푸시 알림 발송 (좋아요/댓글/팔로우/전시종료/공지)         │
│ AWS S3                   │ Presigned PUT URL 발급 (이미지/동영상 업로드)           │
│ AWS CloudFront           │ 미디어 파일 서빙 (CDN)                                │
│ AWS Secrets Manager      │ Firebase credential 파일 주입 (EB predeploy hook)     │
│ Discord Webhook (신고)   │ 신규 UGC 신고 접수 시 운영팀 알림                       │
│ Discord Webhook (차단)   │ 사용자 차단 발생 시 운영팀 알림                         │
└──────────────────────────┴──────────────────────────────────────────────────────┘
```

---

## 6. Background Jobs (Scheduled Tasks)

| Job | Cron | Timezone | 기능 |
|---|---|---|---|
| `ExhibitionEndingSoonScheduler` | `0 0 11 * * *` | Asia/Seoul | 종료 14일 전 전시에 대해 북마크(알림ON) 유저에게 FCM 푸시 발송 |
| `BookmarkCleanupService` | `0 0 1 * * *` | Server default | 종료일 지난 전시의 북마크 자동 삭제 |

---

## 7. Data Model (25 Entities)

### Core Domain

```
┌──────────┐     M:N      ┌──────────┐     1:N     ┌────────────────┐
│  Artist  │◄────────────▶│Exhibition │────────────▶│ExhibitionGenre │
└──────────┘              │          │              │  ├── exhibition │
                          │          │──M:1──┐      │  └── genre     │
                          └──────────┘       │      └────────────────┘
                               │ 1:N         ▼
                               │       ┌──────────┐  1:N  ┌────────────┐
                               │       │  Venue   │──────▶│ VenuePhoto │
                               │       └──────────┘       └────────────┘
                               ▼
                     ┌───────────────────┐
                     │ ExhibitionRecord  │
                     │  ├── user (M:1)   │
                     │  ├── exhibition   │
                     │  ├── mediaList    │──1:N──▶ RecordMedia
                     │  └── hashtags     │──M:N──▶ Hashtag
                     └───────────────────┘
                          │           │
                     1:N  ▼      1:N  ▼
                 ┌────────────┐ ┌───────────┐
                 │RecordLike  │ │RecordScrap│
                 │ ├── user   │ │ ├── user  │
                 │ └── record │ │ └── record│
                 └────────────┘ └───────────┘
```

### User & Social

```
┌──────┐
│ User │──1:N──▶ Follow (follower, following)
│      │──1:N──▶ UserBlock (blocker, blocked)
│      │──1:N──▶ UserVisit (user, exhibition)
│      │──1:N──▶ Bookmark (user, exhibition)
│      │──1:N──▶ ExhibitionClickLog (user, exhibition)
│      │──1:N──▶ Search (user, exhibition?)
│      │──1:N──▶ Report (reporter, reportedUser)
└──────┘
```

### Notification & Recommendation

```
Notification          PushToken            ServiceAnnouncement
NotificationImage     UserGenre            InitialExhibition
RecordComment (→ ExhibitionRecord, User)
```

---

## 8. Configuration & Environment

### Spring Profiles

| Profile | 용도 | DB | Port |
|---|---|---|---|
| `local` (default) | 로컬 개발 | `localhost:3306/jeonlog` | 8080 |
| `prod` | 프로덕션 | RDS (env var 주입) | `${PORT:5000}` |
| `test` | CI 테스트 | H2 in-memory | - |

### Secret 관리

| Secret | 주입 방식 |
|---|---|
| DB credentials | EB 환경변수 → `application-prod.properties` |
| JWT_SECRET | EB 환경변수 |
| OAuth client secrets | EB 환경변수 |
| AWS access/secret key | EB 환경변수 |
| Firebase credential | AWS Secrets Manager → predeploy hook → 파일 |
| Discord webhook URLs | EB 환경변수 |

### Predeploy Hook

```bash
# .platform/hooks/predeploy/10_fetch_firebase_secret.sh
aws secretsmanager get-secret-value \
  --secret-id "$FIREBASE_SECRET_ID" \
  --query SecretString --output text \
  --region "$AWS_REGION" \
  > "$FIREBASE_CREDENTIALS_PATH"
```

---

## 9. CI/CD

### GitHub Actions (`.github/workflows/ci.yml`)

```
Trigger: push(main, develop) / pull_request / workflow_dispatch
    │
    ▼
JDK 21 (Temurin) + Gradle setup
    │
    ▼
./gradlew --no-daemon --stacktrace clean test
    │
    ▼
Upload test reports (artifact)
```

### 배포 파이프라인

```
Code push → GitHub Actions (test) → EB CLI deploy → Elastic Beanstalk
                                                        │
                                                        ▼
                                              predeploy hook 실행
                                              (Firebase secret fetch)
                                                        │
                                                        ▼
                                              java -jar build/libs/app.jar
```

---

## 10. API Surface (29 Controllers, ~70+ Endpoints)

| Module | Controllers | 주요 Base Path |
|---|---|---|
| **auth** | 4 | `/api/auth/*`, `/api/oauth/*` |
| **user** | 4 | `/api/users/*`, `/api/profile/*` |
| **exhibition** | 2 | `/api/venues/*`, `/api/exhibitions/*/click` |
| **search** | 1 | `/api/exhibitions/*` |
| **record** | 1 | `/api/records/*`, `/api/exhibitions/*/records/*` |
| **bookmark** | 1 | `/api/exhibitions/*/bookmarks` |
| **scrap** | 1 | `/api/records/*/scrap` |
| **like** | 1 | `/api/records/*/like` |
| **comment** | 1 | `/api/records/*/comments/*` |
| **report** | 2 | `/api/reports/*`, `/api/admin/reports/*` |
| **notification** | 3 | `/api/notifications/*`, `/api/push-tokens`, `/api/admin/announcements` |
| **recommendation** | 5 | `/api/recommendations/*`, `/api/initial/*` |
| **common** | 3 | `/api/health`, `/api/files/*` |

---

## 11. Known Risks & Improvement Points

| # | 항목 | 상세 |
|---|---|---|
| 1 | **스케줄러 중복 실행** | 오토스케일(1~4대) 환경에서 `@Scheduled` 잡이 인스턴스마다 실행됨. ShedLock 또는 리더 선출 필요 |
| 2 | **미사용 의존성** | `spring-boot-starter-batch`, `spring-boot-starter-cache` + Caffeine이 build.gradle에 있으나 코드에서 미사용 |
| 3 | **ALB 로그 비활성화** | 로드 밸런서 액세스 로그가 꺼져 있어 트래픽 분석/디버깅 제한 |
| 4 | **JWT Access Token 15분** | Refresh Token은 14일이나, 토큰 블랙리스트/revoke 메커니즘 없음 |
| 5 | **FCM 실패 무시** | `FcmPushClient.send()` 에서 예외를 catch 후 무시 (토큰 만료 시 정리 로직 없음) |
| 6 | **Presigned URL 보안** | S3 PUT presigned URL이 content-type 제한 없이 발급됨 |
