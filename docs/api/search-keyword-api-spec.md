# 추천검색어 API 명세 (Frontend 연동용)

이 문서는 추천검색어 기능의 프론트 연동 기준을 정리한다.

## 기능 개요
- 검색 시점에 검색 결과 조회와 검색 로그 저장을 분리 호출한다.
- 추천검색어 랭킹은 전체 사용자 검색 로그를 집계해 반환한다.
- 랭킹에는 전시/전시관/작가 데이터와 매칭되는 키워드만 노출된다.

## 1) 전시 검색
### Request
- Method: `GET`
- Path: `/api/exhibitions/search`
- Query Params:
  - `query` (string, required): 사용자 입력 검색어
  - `filter` (string[], optional): `title`, `artist`, `genre`, `location`

### Example
```http
GET /api/exhibitions/search?query=%EB%AA%A8%EB%84%A4%20%EC%A0%84%EC%8B%9C
```

### Response
- `200 OK`
- `ApiResponse<List<ExhibitionSearchResponseDto>>`

## 2) 검색 로그 저장 (추천검색어 집계용)
### Request
- Method: `POST`
- Path: `/api/exhibitions/search/log`
- Auth: `Authorization: Bearer <access_token>` 필요
- Query Params:
  - `query` (string, required): 사용자가 검색한 원문 텍스트

### Example
```http
POST /api/exhibitions/search/log?query=%EB%AA%A8%EB%84%A4-%EC%A0%84%EC%8B%9C
Authorization: Bearer eyJ...
```

### Server 처리 규칙
- 입력 `query`는 정규화 후 저장된다.
  - 소문자 변환
  - 특수문자는 공백으로 치환
  - 다중 공백은 1칸으로 축약
  - 양끝 공백 제거
- 정규화 결과가 빈 문자열이면 저장하지 않는다.
- 사용자 인증 실패 시 에러 응답:
  - `success=false`
  - `code=UNAUTHORIZED`

### Response
- 성공: `200 OK`
```json
{
  "success": true,
  "data": "검색 로그가 저장되었습니다."
}
```

## 3) 추천검색어(인기 검색어) 조회
### Request
- Method: `GET`
- Path: `/api/exhibitions/search/rank`
- Query Params:
  - `from` (string, optional): ISO-8601 LocalDateTime
  - `to` (string, optional): ISO-8601 LocalDateTime
  - `limit` (int, optional, default `10`): 서버에서 `1~10`으로 보정

### Example
```http
GET /api/exhibitions/search/rank?from=2026-04-01T00:00:00&to=2026-04-30T23:59:59&limit=10
```

### Response
- 성공: `200 OK`
- 정렬 기준: `count DESC`, 동률 시 `keyword ASC`
```json
{
  "success": true,
  "data": [
    { "keyword": "모네 전시", "count": 12 },
    { "keyword": "서울시립미술관", "count": 10 },
    { "keyword": "teamlab", "count": 10 }
  ]
}
```

### Error
- `from`/`to` 형식 오류: `400 BAD_REQUEST`
```json
{
  "success": false,
  "code": "INVALID_ARGUMENT",
  "message": "from는 ISO-8601 LocalDateTime 형식이어야 합니다. 예: 2026-04-27T10:30:00"
}
```

## 프론트 호출 순서(권장)
1. 사용자가 엔터/검색 버튼으로 검색 확정
2. `GET /api/exhibitions/search` 호출로 검색 결과 표시
3. 로그인 상태면 `POST /api/exhibitions/search/log`를 비동기 호출

## 프론트 구현 가이드
- `search/log`는 타이핑마다 호출하지 말고 검색 확정 시점에만 호출
- `search/log` 실패는 UX를 막지 않도록 non-blocking 처리
- query 파라미터는 반드시 URL 인코딩(`encodeURIComponent`) 후 전송
