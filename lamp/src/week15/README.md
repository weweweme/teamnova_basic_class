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
① sql/schema.sql  — 표 6개 + 외래키 생성
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
| **users** | 회원 | 비밀번호 `password_hash` 저장 |
| **media** | 작품 | TMDB 데이터 (`tmdb_id`·poster·overview) |
| **posts** | 글 | `author_id`→users, `media_id`→media (외래키) |
| **comments** | 댓글 | `post_id`·`author_id` 외래키 |
| **likes** | 글 추천 | **복합키**(user_id, post_id) = 1인 1회 |
| **votes** | 작품 투표 | **복합키**(user_id, media_id) + choice |

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

---

## 5. 코드 구조 — "함수 속만 SQL로"

week14 대비 **화면 파일은 거의 그대로**다. `includes/` 모듈 함수의 내부만 세션→SQL로 바꿨다.

```
includes/
├── config.php    비밀 설정 (TMDB 토큰 · DB 접속정보, Git 제외 대상)
├── db.php        PDO 연결(static 재사용) · db_scalar() 헬퍼
├── tmdb.php      TMDB 호출 (검색 · id 조회 · 형식 변환)
├── media.php     작품 저장/조회 (ensure_media)
├── auth.php      로그인·회원가입 (users 표, password_hash)
├── posts.php     글 CRUD (조회는 JOIN, 삭제는 소프트삭제)
├── comments.php  댓글 CRUD
├── works.php     작품 조회 + 투표 (DB + TMDB 폴백)
└── util.php · header.php · footer.php
```

**핵심 원리:** `get_posts()`가 `posts + media + users`를 **JOIN**해서
week14와 **똑같은 모양의 배열**을 돌려준다 → 그 위의 필터·정렬·페이징·화면이 그대로 재사용됨.

---

## 6. 안전장치

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

**세션은 로그인 상태·플래시 알림·최근 본 글에만** 남았다. 나머지 데이터는 전부 DB.

---

## 7. 다음에 할 것

- `reports` 표 (신고를 실제 저장 — 지금은 접수 처리만)
- 이미지 업로드
- DB 자동 시작 (컨테이너 시작 시 mariadbd 자동 실행)
