# week15 — 영화·드라마 리뷰 커뮤니티 (MariaDB 연동)

**주제: 실제 데이터베이스(MariaDB) 연결 + CRUD + 외부 API(TMDB) 연동**

week14의 "세션 임시 저장" 껍데기를, **진짜 MariaDB**로 바꾼 버전입니다.
작품 데이터는 더미가 아니라 **TMDB(영화·드라마 API)** 에서 실시간으로 가져옵니다.
글·댓글·추천·투표가 전부 DB에 저장되어, 브라우저를 닫거나 로그아웃해도 남습니다.

---

## 1. 실행 방법

| 항목 | 값 |
|---|---|
| 주소 | `http://localhost:8081/` |
| 웹서버 | Apache (mod_php) · PHP 8.5.8 |
| DB | **MariaDB 12.3.2** (같은 컨테이너, `/usr/local/mariadb`) |
| DocumentRoot | `/var/www/html/week15` |

**DB 서버**: 컨테이너 시작 시 `apachectl`이 **자동으로 MariaDB를 켠다**.
혹시 수동으로 켜야 하면:
```bash
/usr/local/mariadb/bin/mariadbd-safe --datadir=/usr/local/mariadb/data \
  --user=root --socket=/tmp/mysql.sock --port=3306 --bind-address=0.0.0.0 &
```

**DB 접속 정보** (DBeaver 등 GUI):
| 항목 | 값 |
|---|---|
| Host / Port | `localhost` / `3306` |
| Database | `review_community` (utf8mb4) |
| 계정 | `dev` / `dev1234` |

**테스트 계정** (전부 비번 `1234`): `영화광` · `해석러` · `심야극장`

---

## 2. DB 처음 세팅 (다른 기기에서 재현)

코드는 Git, **데이터는 SQL로 재생성**한다. DBeaver에서 순서대로 실행:

```
① sql/schema.sql  — 표 8개 + 외래키 생성
② sql/seed.sql    — 시연용 데이터 (회원 3 · 작품 5 · 글 10 …)
```

seed.sql은 맨 위에서 싹 비우고 다시 넣으므로 **여러 번 실행해도 같은 상태**가 된다.

---

## 3. 표 설계 (ER)

```
users ──┬─< posts >──┬── media
        │     │       │
        ├─< comments >┤
        ├─< likes >───┘ (post)
        └─< votes >──── media
```

| 표 | 내용 | 특징 |
|---|---|---|
| **users** | 회원 | 비밀번호 `password_hash` 저장 · `username`(로그인 신원)과 `nickname`(표시 이름) 분리 |
| **media** | 작품 | TMDB 데이터 (`tmdb_id`·poster·overview) |
| **posts** | 글 | `author_id`→users, `media_id`→media (외래키) |
| **comments** | 댓글 | `post_id`·`author_id` 외래키 |
| **likes** | 글 추천 | **복합키**(user_id, post_id) = 1인 1회 |
| **votes** | 작품 투표 | **복합키**(user_id, media_id) + choice |
| **reports** | 글 신고 | (reporter_id, post_id) UNIQUE = 1인 1회 |
| **notifications** | 알림 | 내 글에 댓글 시 생성 · `is_read`로 안읽음/읽음 |

**삭제 정책**: 글을 지우면 그 댓글·추천도 함께(`ON DELETE CASCADE`).
회원은 함부로 못 지우게(`RESTRICT`). 글·댓글 자체는 **소프트삭제**(`deleted_at`)라 되돌리기 가능.

---

## 4. TMDB 연동 흐름

작품은 우리가 다 저장하지 않는다. **필요할 때만** 우리 DB로 들여온다.

```
[검색]  search.php → TMDB 실시간 검색 (우리 DB 안 거침)
   │ 결과 클릭
[게시판] board/?work=tmdb-496243
   │  → media 표에 있나? 있으면 그 정보 / 없으면 TMDB에서 가져와 표시(폴백)
   │ 글쓰기
[저장]  create.php → ensure_media_by_slug (작품을 media 표에 자동 저장) → posts INSERT
```

- slug 규칙: `tmdb-<tmdb_id>` (예: `tmdb-496243`)
- TMDB 키는 `includes/config.php` (v4 토큰, 헤더 `Authorization: Bearer`)
- 영화(`title`)와 드라마(`name`)의 필드 차이는 `build_media_from_tmdb`가 흡수

**홈·작품목록도 TMDB로** (넷플릭스식):
- 홈 = 히어로 배너 + 사이드바(지금 뜨는 글·오늘의 발견) + 커뮤니티 줄 + TMDB 인기작 줄들 + 최근 글
- 작품목록 = TMDB 인기 영화/드라마 그리드
- `media` 표는 화면 목록이 **아니고**, "글 달린 작품"만 담는 내부 저장소

**캐싱**: `tmdb_get`이 응답을 `cache/tmdb/`에 30분 저장 → 홈 로딩 4.5초 → 0.2초.
(TMDB를 홈 한 번에 10여 회 호출하므로 캐시가 필수. `cache/`는 Git 제외)

---

## 5. 코드 구조

### 폴더 규칙 — "자원 하나 = 폴더 하나, 홈만 루트"
```
index.php              홈 (유일한 루트 화면)
works/  search/  rank/  profile/  settings/  notifications/  board/
   └ index.php = 그 자원의 '화면'(GET)   (+ 처리 파일이 있으면 같은 폴더에)
post/   ├ view·write·edit (화면)  └ create·update·delete·restore (처리 POST)
comment/  vote/  like/  report/  auth/    그 자원의 처리(POST) 파일들
profile/avatar.php · settings/{nickname,password}.php   내 정보 편집 처리
api/    row.php · browse.php   화면(HTML) 대신 데이터(JSON) — 지연 로딩용
includes/   모든 화면이 공유하는 로직 모듈 + 레이아웃
assets/  sql/  uploads/  cache/   정적파일 · DB재생성 · 업로드물 · TMDB캐시
```
→ **보여주는 URL(뷰)과 처리하는 URL(액션)을 분리**하고, POST 처리 뒤엔 **리다이렉트(PRG)**로
새로고침 중복 제출을 막는다. 자원마다 폴더라 "어느 파일이 어디 있나"가 한 규칙으로 설명된다.

### includes 모듈 — "함수 속만 SQL로"
week14 대비 **화면 파일은 거의 그대로**다. `includes/` 모듈 함수의 내부만 세션→SQL로 바꿨다.

```
includes/
├── config.php     비밀 설정 (TMDB 토큰 · DB 접속정보, Git 제외 대상)
├── db.php         PDO 연결(static 재사용) · db_scalar() 헬퍼
├── tmdb.php       TMDB 호출 (검색·인기작·상세·형식변환) + 30분 캐싱
├── media.php      작품 저장/조회 (ensure_media)
├── auth.php       로그인·회원가입·아바타 · 닉네임/비밀번호 변경 (username≠nickname)
├── posts.php      글 CRUD (조회는 JOIN, 삭제는 소프트삭제)
├── comments.php   댓글 CRUD
├── works.php      작품 조회·투표 (DB+TMDB 폴백) · 커뮤니티 작품
├── ranking.php    랭킹 집계 (유저 랭킹 = users↔posts↔likes JOIN)
├── level.php      유저 등급 배지 (글 수 → 5단계)
├── notifications.php  알림 생성·조회·읽음 처리
├── reports.php    신고 저장 (1인 1회)
├── upload.php     프로필 이미지 업로드 (MIME 검증·파일명 재생성)
├── media_row.php  작품 가로 줄 렌더링 조각 (홈에서 재사용)
└── util.php · header.php · footer.php
```

**핵심 원리:** `get_posts()`가 `posts + media + users`를 **JOIN**해서
week14와 **똑같은 모양의 배열**을 돌려준다 → 그 위의 필터·정렬·페이징·화면이 그대로 재사용됨.

---

## 6. UI / 디자인 (넷플릭스식 다크)

- **다크 테마**: 배경 `#141414` + 은은한 라디얼 글로우·그라데이션(`fixed`).
  사이드바·상단바는 반투명(`backdrop-filter`)이라 배경 글로우가 비침.
- **홈 레이아웃**:
  - 히어로 배너 (커뮤니티 1위 작품의 TMDB backdrop) + 사이드바(🔥지금 뜨는 글·🎲오늘의 발견)
  - → 넓으면 2단, 좁으면 1단 (1100px 기준)
  - 아래: 커뮤니티 5칸 그리드(글 많은 순 상위 5) → TMDB 인기작 가로 줄들 → [최근 글 | 댓글 많은 글] 2단 게시판
- **가로 줄**: 스크롤바 숨김 + JS로 `‹ ›` 화살표 · 마우스휠→가로 스크롤 (`main.js`).
- **폭**: 본문 1400px (홈·게시판·목록), 글 읽기·폼은 760px 유지.
- **반응형**: 미디어 쿼리로 태블릿·모바일에서 포스터·여백·칸 수 축소.

---

## 7. 안전장치

| 항목 | 방법 |
|---|---|
| SQL 인젝션 | 모든 쿼리 **Prepared Statement** (`?` + `execute([...])`) |
| XSS | 출력은 `e()` = `htmlspecialchars()` |
| 비밀번호 | `password_hash` 저장 · `password_verify` 대조 (평문 없음) |
| 1인 1회 | likes·votes **복합 기본키**로 DB가 구조적으로 보장 |
| 데이터 무결성 | **외래키** — 없는 회원/작품의 글은 DB가 거부 |
| 권한 | 수정·삭제는 화면에서 숨기고 **서버에서 소유권 재확인**(`is_owner`) |
| GET으로 변경 차단 | 액션 파일은 POST 아니면 즉시 리다이렉트 |
| 세션 고정 공격 | 로그인 시 `session_regenerate_id(true)` |
| **이미지 업로드** | ①MIME 검사(finfo) ②파일명 강제 재생성 ③업로드 폴더 PHP 실행 차단 |

**세션은 로그인 상태·플래시 알림·최근 본 글에만** 남았다. 나머지 데이터는 전부 DB.

---

## 8. 커뮤니티 기능

- **랭킹** (`rank.php`, 상단바): 탭 3종 — 🎬 인기 작품 / 👑 명예의 전당(유저) / 🔥 화제의 글.
  1~3위 메달. 매 요청 DB 집계라 자동 갱신. 유저 랭킹은 **users↔posts↔likes JOIN + GROUP BY**
  (글 수 `COUNT(DISTINCT)`, 받은 추천 `COUNT`) — JOIN으로 줄이 불어나므로 DISTINCT가 필요한 실사례.
- **게시판 상단 작품 상세**: 감독·출연·분량 + ▶ 예고편 모달(유튜브).
  TMDB `append_to_response=credits,videos`로 한 번에 받음 (`build_tmdb_detail`).
- **마이페이지 · 설정 분리**:
  - 공개 프로필(`profile.php`)은 아바타·통계·작성 글만. 내 프로필이면 `⚙️ 설정` 링크.
  - 설정(`settings.php`, 로그인 전용): 프로필 이미지 · **닉네임 변경** · **비밀번호 변경**.
  - 비밀번호 변경은 **현재 비밀번호 확인 후**에만 (무단 변경 방지).
- **아이디 ↔ 닉네임 분리**: `username`=로그인·소유권·프로필URL(신원, 고정),
  `nickname`=화면 표시 이름(변경 가능). 표시만 닉네임으로 바꾸고 권한 로직은 username 유지.
- **유저 등급 배지** (`includes/level.php`): 작성 글 수로 5단계(🌱새싹→👑시네필).
  글·댓글·프로필·랭킹의 작성자 옆에 배지 표시. 규칙은 한 곳(등급표)에서 관리.
- **좋아요한 글** (프로필 탭): `likes`를 역방향 조회(likes→posts JOIN)해 유저가 추천한 글 모아보기.
- **댓글 알림** (`notifications` 표, 상단바 🔔): 내 글에 남이 댓글 달면 알림 생성(자기 댓글 제외).
  안 읽은 개수 뱃지 → 목록(`notifications.php`)을 열면 전체 읽음 처리.
- **휴지통** (설정 안, `trash/`): 삭제한 글은 `deleted_at`을 타임스탬프로 써서 30일 보관.
  되돌리기 / 지금 영구삭제 가능. **cron이 없어** 휴지통을 열 때 만료분을 함께 정리(lazy purge).
  영구삭제 시 댓글·추천·알림도 외래키 CASCADE로 함께 삭제.

## 9. 성능 (지연 로딩)

무거운 TMDB 호출을 페이지 로딩에서 떼어내 **껍데기 먼저, 데이터는 나중에**.

| 화면 | 방식 | 효과(콜드) |
|---|---|---|
| 홈 | 우리 DB(히어로·커뮤니티·글)는 서버 렌더, TMDB 가로줄은 JS가 `api/row.php`로 채움 | 4.85초 → **0.6초** |
| 작품 둘러보기 | 첫 페이지도 안 그리고 무한스크롤 JS가 `api/browse.php`로 1페이지부터 채움 | 0.88초 → **0.02초** |

- 로딩 중엔 회색 **스켈레톤** 표시 → 데이터 오면 실제 카드로 교체.
- **프로필 이미지 리사이즈**: 서버에 GD가 없어, 올리기 전에 **브라우저 Canvas로 256px 정사각형
  WebP** 압축(2MB→~20KB). 서버 3중 검증은 그대로 → 이중 방어.
- 홈 커뮤니티 줄은 글 많은 순 **상위 5개**(그리드 5칸)만, 매 방문 자동 갱신.

## 10. 남은 것 (선택)

- 글에 이미지 첨부 (지금은 프로필 이미지만)
- DB 이론 발표자료 마무리 (JOIN 슬라이드 등)

> DB 자동 시작은 `apachectl`에 설정 완료 — 컨테이너 재시작 시 MariaDB가 자동 기동.
