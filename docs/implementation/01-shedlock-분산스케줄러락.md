# 분산 스케줄러 락 (ShedLock)

## 한 줄 요약

여러 대의 서버에서 같은 예약 작업이 중복으로 실행되는 것을 막아주는 기능

---

## 왜 필요해?

### 상황

AWS 오토스케일로 서버가 1대에서 3대로 늘었을 때

```
현재 우리 서버:
- Instance 1, Instance 2, Instance 3 (최대 3대)

예약 작업 (매일 11시 실행):
1. 전시 종료 알림 발송
2. 끝난 전시 북마크 삭제
```

### 문제

@Scheduled는 "시간 되면 무조건 실행"한다!

```
Instance 1: 11시 -> 알림 발송
Instance 2: 11시 -> 알림 발송 (중복!)
Instance 3: 11시 -> 알림 발송 (중복!)

결과: 같은 알림이 3번 감 (사용자: "스팸이다!")
```

---

## 어떻게 해결해?

ShedLock이 "먼저 온 사람만 실행"하게 한다

```
Instance 1: 11시 -> Lock 획득 -> 알림 발송 -> Lock 해제
Instance 2: 11시 -> Lock 있음 -> 대기 -> 실행 안 함
Instance 3: 11시 -> Lock 있음 -> 대기 -> 실행 안 함

결과: 알림 1번만 감 (정상!)
```

### Lock이 뭔데?

현실 비유:
- 오피스 건물 (서버 여러 대)
- 전자카드키 (Lock) - "카드키 있는 사람만 회의실 사용 가능"

---

## 구현 방법

### 1. 라이브러리 추가

```gradle
// build.gradle
dependencies {
    implementation 'net.javacrumbs.shedlock:shedlock-spring:5.10.0'
    implementation 'net.javacrumbs.shedlock:shedlock-provider-jdbc-template:5.10.0'
}
```

### 2. DB 테이블 추가

```sql
CREATE TABLE shedlock (
    name        VARCHAR(64) NOT NULL PRIMARY KEY,
    lock_until  TIMESTAMP(3) NOT NULL,
    locked_at   TIMESTAMP(3) NOT NULL,
    locked_by   VARCHAR(255) NOT NULL
);
```

### 3. 설정 클래스

```java
@Configuration
@EnableSchedulerLock(defaultLockAtMostFor = "PT30M")
public class ShedLockConfig {

    @Bean
    public LockProvider lockProvider(DataSource dataSource) {
        return new JdbcTemplateLockProvider(
            JdbcTemplateLockProvider.Configuration.builder()
                .withJdbcTemplate(new JdbcTemplate(dataSource))
                .usingDbTime()
                .build()
        );
    }
}
```

### 4. 예약 작업에 적용

```java
@Scheduled(cron = "0 0 11 * * *", zone = "Asia/Seoul")
@SchedulerLock(
    name = "exhibitionEndingSoon",
    lockAtMostFor = "PT30M",
    lockAtLeastFor = "PT1M"
)
public void run() {
    // 실제 작업
}
```

---

## 설정값 설명

| 설정 | 값 | 의미 |
|---|---|---|
| name | "exhibitionEndingSoon" | Lock의 별명 (다른 작업이랑 겹치면 안 됨) |
| lockAtMostFor | PT30M | 잡았는데 서버가 죽으면 30분 뒤에 자동으로 풀림 |
| lockAtLeastFor | PT1M | 작업이 금방 끝나도 1분은 기다려야 다시 실행함 |

### 왜 이렇게 설정했는가?

- 30분: 우리 작업(알림 발송)이 아무리 오래 걸려도 30분은 걸리지 않을 것 같아서
- 1분: 같은 서버에서 너무 자주 실행되는 것 방지

---

## 포폴에서 어필하는 방법

```
Situation: 오토스케일 환경에서 예약 작업이 중복 실행되는 문제 발견
Task: 분산 환경에서도 작업이 한 번만 실행되도록 설계
Action: ShedLock 도입 -> DB Lock으로 한 서버만 실행되도록 제어
Result: 중복 실행 0건 달성
```

---

## 참고하면 좋은 것

- [ShedLock 공식 문서](https://github.com/lukas-krecan/ShedLock)
- 현재 적용된 파일: src/main/java/com/jeonlog/exhibition_recommender/notification/scheduler/ExhibitionEndingSoonScheduler.java
