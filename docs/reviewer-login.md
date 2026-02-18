# App Review Dummy Login (Easter Egg)

## 목적
- 소셜 로그인 없이도 앱 심사자가 기능을 검증할 수 있도록 임시 로그인 경로를 제공합니다.
- 이스터에그는 진입 UI일 뿐이며, 실제 인증은 서버 코드 검증으로 처리합니다.

## 동작 방식
1. 앱에서 이스터에그 동작으로 코드 입력 화면 진입
2. 프론트가 `POST /api/auth/reviewer-login` 호출₩
3. 서버가 설정값과 입력 코드를 비교
4. 일치 시 더미 사용자 JWT(access/refresh) 반환

## API
- Endpoint: `POST /api/auth/reviewer-login`
- Request:
```json
{
  "reviewCode": "심사용-코드"
}
```
- Success Response(`data`):
```json
{
  "accessToken": "...",
  "refreshToken": "...",
  "newUser": false,
  "reviewer": true
}
```

## 환경 변수
- `REVIEW_LOGIN_ENABLED` (`true`/`false`)
- `REVIEW_LOGIN_CODE` (심사 코드)
- `REVIEW_LOGIN_OAUTH_ID` (기본값: `app-reviewer`)
- `REVIEW_LOGIN_NAME` (기본값: `App Reviewer`)

## 운영 가이드
- 기본값은 `enabled=false`로 유지합니다.
- 심사 기간에만 활성화하고, 종료 즉시 `enabled=false`로 비활성화합니다.
- 앱 심사 제출 문구에는 "이스터에그 진입 후 코드 입력으로 로그인 가능"을 명시합니다.

## 심사 제출용 설명 예시
- "앱은 소셜 로그인을 기본으로 사용합니다. 심사 계정을 위해 앱 내부 이스터에그로 진입 가능한 Review Login을 제공합니다."
- "경로: 로그인 화면에서 로고 5회 탭 -> Review Login 화면"
- "입력 코드: [심사 전용 코드]"
- "해당 로그인은 심사 기간 동안만 활성화되며, 종료 후 비활성화됩니다."
