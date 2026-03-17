# Career Tracker (Portfolio + Self-Intro)

## How We Use This File
- Goal: Track implementation work and immediately extract portfolio/self-intro points.
- Rule: Every meaningful change adds one row to the log table below.
- Evidence first: Always record file paths, PR/commit id, and measurable KPI fields.

## Session Continuity
- Same session: I can keep full context and continue naturally.
- New session: I do not reliably retain full prior context unless it is written in this repository.
- Therefore: This file is the source of truth for cross-session continuity.

## Priority Implementation Backlog (Current)
1. Observability baseline (Actuator, Micrometer, structured logs, traceId)
2. Distributed scheduler lock (ShedLock + job run history)
3. Test automation expansion (Testcontainers + MockMvc + CI gate)
4. Load testing with report (k6/Gatling + bottleneck analysis)
5. External integration resilience (retry/backoff/circuit breaker)
6. Incremental data pipeline for click aggregation
7. Redis cache/rate-limit for hot paths
8. Grafana dashboarding (Prometheus metrics visualization and alert panels)
9. Test expansion phase 2 (critical scenarios and regression hardening)
10. Outbox-based reliable notification delivery
11. Backend-only ops auth flow (dev/reviewer token issuance + curl/Swagger runbook)

## Additional Options (Portfolio Boost)
- P0. OpenTelemetry tracing demo (request-to-DB/outbound trace path, local reproducible)
- P1. Sentry integration (error capture + release/env tags + triage-ready context)
- P1. Incident-readiness artifacts (Alert rules + Runbook + Postmortem template)
- P2. Graceful shutdown and deployment safety proof
- P3. Kafka demo (recommended after Outbox work to avoid isolated "toy" usage)

## Work Log Template
| Date | Task | What Changed | Portfolio Point | Self-Intro Point | Evidence (Path/PR) | KPI/Measurement |
|---|---|---|---|---|---|---|
| YYYY-MM-DD | ex) ShedLock 도입 | ex) scheduler lock table + @SchedulerLock 적용 | 분산 환경 중복 실행 제어 설계/운영 경험 | 오토스케일 중복 실행 리스크를 락으로 해결 | ex) src/.../Scheduler.java, PR #123 | duplicate_run_count, job_success_rate |

## STAR Snippet Template
- Situation: 어떤 운영/성능/안정성 문제가 있었는지
- Task: 내가 해결해야 했던 목표와 제약
- Action: 설계 선택, 구현 포인트, 트레이드오프
- Result: 지표 기반 결과 (숫자는 측정 후 입력)

## Writing Rules (Important)
- Portfolio: technical depth first (problem, architecture, trade-off, metrics, failure case).
- Self-intro: impact first in 1-2 lines (action + result).
- Never claim fake numbers. Use placeholders until measured.

## Incident Integrity Policy (Must Follow)
- We never write fiction as if it happened.
- Each story must start with one label: `[Incident]` (actually happened) or `[Preventive]` (did not happen, plausible risk).
- If no measured result exists, write `Validation Plan` and `Target KPI` instead of fake numbers.
- Safe verbs for preventive stories: `identified`, `anticipated`, `mitigated`, `added guardrail`, `validated in test`.
- Avoid misleading verbs unless true evidence exists: `resolved outage`, `eliminated`, `improved by N%`.

## Preventive Scenario Template (for future writing)
- Label: `[Preventive]`
- Risk Trigger: 어떤 조건에서 문제가 발생 가능한지
- Failure Mode: 실제로 깨질 수 있는 방식
- Guardrail Added: 내가 코드/설정/테스트로 추가한 장치
- Validation: 재현 테스트/로그/모니터링으로 어떻게 검증했는지
- KPI Plan: 추적할 지표 (값은 측정 후 입력)

## Preventive Scenario Bank (Code-grounded, Not Incidents)
| Scenario | Risk Trigger | Failure Mode | Guardrail Idea | Evidence |
|---|---|---|---|---|
| Scheduler duplicate run | 오토스케일 다중 인스턴스 + `@Scheduled` | 동일 잡 중복 실행으로 중복 알림/중복 정리 | ShedLock + job run history table | docs/architecture.md, src/main/java/com/jeonlog/exhibition_recommender/notification/scheduler/ExhibitionEndingSoonScheduler.java |
| Notification dedup race | 동시 요청/멀티 인스턴스에서 dedup 확인-저장 사이 경쟁 | 동일 dedupKey 알림 중복 생성 가능성 | DB unique 제약 보강 + insert 충돌 처리 | src/main/java/com/jeonlog/exhibition_recommender/notification/service/NotificationService.java, src/main/java/com/jeonlog/exhibition_recommender/notification/domain/Notification.java |
| FCM silent failure | 유효하지 않은 토큰/FCM 오류 | 알림 누락되지만 원인 추적 어려움 | 실패 사유 로깅 + invalid token 비활성화/정리 배치 | src/main/java/com/jeonlog/exhibition_recommender/notification/service/FcmPushClient.java |
| External webhook instability | Discord API 지연/오류 | 운영 알림 누락 또는 지연 | retry/backoff + circuit breaker + 실패 큐 | src/main/java/com/jeonlog/exhibition_recommender/report/service/DiscordReportWebhookService.java, src/main/java/com/jeonlog/exhibition_recommender/user/service/DiscordUserBlockWebhookService.java |
| Auth exception opacity | JWT/OAuth 관련 broad catch | 사용자 인증 실패 원인 파악 어려움 | 에러 코드 분류 로깅 + 공통 에러 매핑 | src/main/java/com/jeonlog/exhibition_recommender/auth/config/JwtAuthenticationFilter.java, src/main/java/com/jeonlog/exhibition_recommender/auth/controller/MobileOAuthController.java |
| Low test safety net | 통합/회귀 테스트 부족 | 변경 시 숨은 회귀 버그 유입 | Testcontainers + MockMvc + CI quality gate | src/test/java/com/jeonlog/exhibition_recommender/ExhibitionRecommenderApplicationTests.java, .github/workflows/ci.yml |

## KPI Map (Implementation-ready)
| Module | KPI | Collection Method | Evidence |
|---|---|---|---|
| Notifications | dedup collision count, send success rate, send latency p95 | app log fields + DB query + (later) Micrometer timer | src/main/java/com/jeonlog/exhibition_recommender/notification/service/NotificationService.java |
| Scheduler | duplicate_run_count, job_success_rate, job_duration | job_run_history table + scheduler logs | src/main/java/com/jeonlog/exhibition_recommender/bookmark/service/BookmarkCleanupService.java |
| External webhooks | timeout rate, retry recovery rate | structured warn/error logs + retry counters | src/main/java/com/jeonlog/exhibition_recommender/report/service/DiscordReportWebhookService.java |
| Authentication | token validation error rate by reason | filter/controller structured logs by error code | src/main/java/com/jeonlog/exhibition_recommender/auth/config/JwtAuthenticationFilter.java |
| API reliability | 5xx rate, endpoint latency p95 | Actuator/Micrometer + GlobalExceptionHandler logs | src/main/java/com/jeonlog/exhibition_recommender/common/api/GlobalExceptionHandler.java |

## Collaboration Rule (from now on)
- During implementation, each completed task is immediately tagged in this file as:
  - `Portfolio Candidate`
  - `Self-Intro Candidate`
  - `Preventive Candidate`
- I will keep these tags updated every time we ship meaningful changes.

## First Entries (Current Codebase Strengths)
| Date | Task | What Changed | Portfolio Point | Self-Intro Point | Evidence (Path/PR) | KPI/Measurement |
|---|---|---|---|---|---|---|
| 2026-03-14 | Notification dedup/batch basis 확인 | Existing dedup key + slice pagination + batch save pattern identified | 대량 알림 처리 안정화 패턴 설계 근거 | 중복 알림 방지와 대량 처리 안정성에 기여 | src/main/java/com/jeonlog/exhibition_recommender/notification/service/NotificationService.java | dedup hit count, push success rate, send latency |
| 2026-03-14 | Scheduler distributed risk 확인 | Autoscaling 환경 스케줄러 중복 실행 리스크 문서화 확인 | 분산 환경에서 배치 중복 실행 리스크 식별 능력 | 운영 리스크를 선제적으로 식별하고 개선 우선순위화 | docs/architecture.md, docs/architecture-summary.md | duplicate_run_count (after lock) |
| 2026-03-15 | Observability baseline 도입 | Actuator + Prometheus metrics registry + traceId(MDC) filter + 구조화 로그 패턴 추가 | 운영 이슈를 지표/로그 기반으로 추적 가능한 관측성 베이스라인 구축 | 기능 개발보다 먼저 계측 기반을 마련해 이후 성능/안정성 개선의 검증 가능성 확보 | build.gradle, src/main/resources/application.properties, src/main/java/com/jeonlog/exhibition_recommender/common/logging/TraceIdFilter.java, src/main/java/com/jeonlog/exhibition_recommender/config/SecurityConfig.java | http.server.requests p95, 5xx rate, traceId 기반 오류 추적률 |
| 2026-03-16 | Test automation expansion 1차 | Testcontainers(MySQL, Auth 통합) + MockMvc 시나리오(Auth/Report security) + CI build gate 추가 | 운영 DB와 유사한 테스트 환경 및 API 시나리오 회귀 방어선 구축 경험 | 기능 구현 전 테스트 안전망을 먼저 확장해 변경 리스크를 줄였음 | build.gradle, src/test/java/com/jeonlog/exhibition_recommender/support/MySqlContainerTestSupport.java, src/test/java/com/jeonlog/exhibition_recommender/auth/controller/AuthControllerIntegrationTest.java, src/test/java/com/jeonlog/exhibition_recommender/report/controller/ReportSecurityIntegrationTest.java, .github/workflows/ci.yml | integration test pass rate, skipped test count(local no-docker), CI gate pass rate |
| 2026-03-17 | Load testing with report (k6/Gatling + bottleneck analysis) | Added performance-test directory with k6 script for API load testing, README, and Gradle task for running tests | 성능 테스트 자동화 및 병목 분석을 위한 기반 구축 경험 | "성능 테스트를 먼저 구현하여 병목을 사전에 식별하고 안정성을 확보했다"는 성능 중심 실행 사례 | performance-test/api-load-test.js, performance-test/README.md, build.gradle (loadTest task) | API latency p95, error rate (to be measured) |

## Candidate Tags (Latest)
- Portfolio Candidate: Observability baseline 도입으로 장애/성능 분석을 위한 계측 기반 선구축
- Portfolio Candidate: Testcontainers + MockMvc + CI gate로 회귀 탐지 신뢰도를 높인 테스트 자동화 확장
- Self-Intro Candidate: "운영 리스크를 먼저 계측 가능 상태로 전환한 뒤 기능 개선을 진행했다"는 실행 우선순위 판단 사례
- Self-Intro Candidate: "기능 구현 전에 통합 테스트 게이트를 먼저 세워 배포 리스크를 낮췄다"는 품질 우선 실행 사례
- Preventive Candidate: [Preventive] traceId 전파 + 표준 로그 포맷으로 원인 추적 불가 리스크 완화
- Preventive Candidate: [Preventive] Docker 미지원 환경에서는 통합 테스트를 skip 처리하고, CI(Docker 지원)에서 검증해 로컬/CI 환경 차이 리스크 완화
