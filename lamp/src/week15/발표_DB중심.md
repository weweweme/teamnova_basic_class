# 발표 대본 — DB 중심 (week15 리뷰 커뮤니티)

> 컨셉: **"홈페이지에서 동작 → DBeaver에서 표 새로고침(⟳) → DB가 바뀌는 걸 눈으로"**
> 각 장면은 **[홈페이지] → [DBeaver] → [코드] → [원리]** 네 박자.

---

## 0. 사전 준비 (발표 시작 전)

- [ ] **DBeaver**: `review_community` 접속 → 아래 표들 **Data 탭 미리 열기**
  `users · posts · likes · votes · comments · reports · notifications · media`
  - 각 표를 **id 내림차순** 정렬 → 새 행이 항상 맨 위
  - **자동커밋 ON** 확인 (아니면 새로고침해도 반영 안 됨)
  - 새로고침 = **⟳ 버튼** 또는 `Ctrl+R`
- [ ] **브라우저**: `http://localhost:8081/` — 홈·랭킹 한 번 열어 **TMDB 캐시 워밍**(콜드 4.8초 방지)
- [ ] **계정 2개 로그인 준비**: `영화광 / 1234`, `해석러 / 1234` (알림 시연용)
  - 이 프로젝트는 **세션을 쓰지 않는다**(신원을 주소 `?as=`로 나름) → **같은 브라우저 탭 두 개로
    동시에 다른 사람**이 될 수 있다. 탭A `?as=영화광` · 탭B `?as=해석러`. **시크릿창 불필요.**
    (세션 방식이면 쿠키가 브라우저당 하나라 이게 안 된다 — 알림 시연이 훨씬 편해진 셈)
- [ ] **주소창을 화면에 보이게** 둘 것. 시연 내내 `?as=영화광`, 동작 뒤엔 `?flash=…`가 주소에 뜬다.
  청중이 반드시 묻는 부분이라 **1번 구간에서 먼저 짚고 시작**한다 (아래).

---

## 1. 발표 얼개 (약 10~12분)

| 구간 | 내용 | 표 |
|---|---|---|
| 0 | 소개 | — |
| 1 | 기반: 연결 & 안전 | users |
| 2 | C·R: 번호로 저장, JOIN으로 이름 | posts·users·media |
| 3 | U·D: 소프트삭제 & 휴지통 | posts |
| 4 | 집계: DB가 세어준다 (랭킹·등급) | users·posts·likes |
| 5 | 관계: 복합키·투표·신고·역방향 | likes·votes·reports |
| 6 | 알림 & 외부데이터(TMDB) | notifications·media |
| 7 | 마무리: 재현성 | schema/seed |

---

## 2. 구간별 대본

### 0. 소개 (30초)
> "영화·드라마 리뷰 커뮤니티입니다. week14는 세션 임시저장, **week15는 진짜 MariaDB**. 글·댓글·추천·투표·신고·알림·휴지통까지 전부 DB에 남습니다. **표 8개**를 어떻게 잇고 썼는지, 화면과 DB를 나란히 보여드릴게요."

---

### 1. 기반 — 연결 & 안전 (로그인)
- **[홈페이지]** 로그인 (`영화광 / 1234`)
- **[DBeaver]** `users` ⟳ → `password`가 `$2y$12$...` **해시**, `username ≠ nickname`
- **[코드]** `includes/db.php`, `includes/auth.php`
  ```php
  $stmt = db()->prepare('SELECT * FROM users WHERE username = ?');
  $stmt->execute([$username]);              // 값은 ?로 분리 → SQL 주입 방어
  password_verify($password, $user['password']);  // 해시 대조
  ```
- **[원리]** 모든 쿼리 **Prepared Statement**, 비번은 `password_hash`로만 저장(평문 없음), PDO는 `static`으로 한 번만 연결.

#### ★ 여기서 주소창을 한 번 짚고 간다 (안 짚으면 반드시 질문 들어옴)

로그인하는 순간 주소가 `http://localhost:8081/?as=영화광` 으로 바뀐다.

> "이번 프로젝트는 **세션을 일부러 하나도 안 썼습니다.**
> 서버는 요청 하나 처리하고 바로 잊어버리거든요. 그래서 '나 영화광이야'를
> **매 요청마다 주소에 실어** 다시 알려주는 방식으로 만들었습니다.
> 링크 수십 개에 손으로 붙인 게 아니라, PHP 내장 URL 리라이터
> (`output_add_rewrite_var`)가 출력되는 모든 링크에 자동으로 붙이고,
> 폼에는 hidden 필드를 심어줍니다."

**예상 질문: "그럼 주소만 `?as=해석러`로 바꾸면 남이 될 수 있는 거 아닌가요?"**
→ **"맞습니다. 됩니다."** 이게 이 방식의 한계고, 숨기지 말고 그대로 인정하는 게 낫다.

| | |
|---|---|
| 지금 확인하는 것 | `users` 표에 **있는 아이디인지**만 (없는 아이디면 유령 사용자가 외래키를 깨뜨림) |
| 확인 못 하는 것 | **정말 본인인지** — 브라우저가 보내는 값은 GET·POST·hidden 뭐든 사용자가 고칠 수 있다 |
| 진짜 해결책 | **정답을 서버가 자기 쪽에 들고 있는 것 = 세션** → **week16 주제** |

> **킬러 멘트**: "비밀번호를 해시로 지키는 것과, **로그인 상태를 지키는 것**은 다른 문제입니다.
> 앞은 이번 주에 했고, 뒤는 다음 주에 세션으로 합니다."

- **[코드]** `includes/auth.php` — 신원을 읽는 곳은 프로젝트 전체에서 `current_user_row()` 하나뿐
  ```php
  $username = identity_from_request();          // 주소(?as=) 또는 폼의 hidden
  $cached   = $username === '' ? null : find_user($username);   // users 표에서 실존 확인
  ```
- **[덤]** 세션이 없어서 생긴 이점 하나 — **탭 두 개로 서로 다른 사람이 될 수 있다.**
  6번 알림 시연에서 시크릿창 없이 바로 보여준다.

---

### 2. C·R — "화면은 이름, DB는 번호" ⭐
- **[홈페이지]** 게시판 → 글쓰기 → 등록
- **[DBeaver]** `posts` ⟳ → 새 행 (**`author_id=1`, `media_id=3`처럼 번호**)
  → `users` ⟳ → `id=1`이 nickname '영화광' (두 표 번갈아 가리키며 **1 ↔ 영화광** 추적)
- **[코드]** `includes/posts.php` → `get_posts()`
  ```sql
  SELECT p.title, u.nickname AS authorNick, m.title AS workTitle
  FROM posts p
  JOIN users u ON p.author_id = u.id      -- 번호 → 이름
  JOIN media m ON p.media_id  = m.id      -- 번호 → 작품명
  WHERE p.deleted_at IS NULL              -- 지운 글 제외
  ```
- **[원리]** 저장은 **번호(FK)로**(중복 방지), 화면은 **3표 JOIN 한 번**으로 이름을 붙여 조회.

---

### 3. U·D — 소프트삭제 & 휴지통 ⭐⭐
- **[홈페이지]** 글 삭제
- **[DBeaver]** `posts` ⟳ → **행이 안 사라짐!** `deleted_at`에 시각만 찍힘
- **[홈페이지]** 설정 → 🗑 휴지통 → 되돌리기
- **[DBeaver]** `posts` ⟳ → `deleted_at`이 다시 **`[NULL]`**
- **[코드]** `includes/posts.php`
  ```sql
  UPDATE posts SET deleted_at = NOW() WHERE id = ?;   -- 삭제(표식만)
  UPDATE posts SET deleted_at = NULL WHERE id = ?;    -- 되돌리기
  DELETE FROM posts WHERE deleted_at < NOW() - INTERVAL 30 DAY;  -- 30일 후 영구삭제
  ```
- **[원리]** 진짜 안 지우고 **`deleted_at`(타임스탬프)** 만 → 되돌리기 + 30일 보관. cron이 없어 **휴지통 열 때 만료분 정리**(lazy purge).

---

### 4. 집계 — "DB가 세어준다" ⭐⭐
- **[홈페이지]** 랭킹 → 명예의 전당 / 작성자 옆 등급 배지 / 프로필 통계
- **[코드]** `includes/ranking.php` → `rank_users()`
  ```sql
  SELECT u.nickname,
         COUNT(DISTINCT p.id) AS 글수,      -- 뻥튀기 방지
         COUNT(l.post_id)     AS 받은추천
  FROM users u
  JOIN posts p      ON p.author_id = u.id
  LEFT JOIN likes l ON l.post_id   = p.id   -- 추천 0도 유지
  GROUP BY u.id
  ORDER BY 받은추천 DESC LIMIT 10;
  ```
- **[원리]** 개수를 **DB에게 시킴**(COUNT+GROUP BY). JOIN으로 줄이 불어나 **COUNT(DISTINCT)**, 추천 0인 사람도 살리려 **LEFT JOIN**. (등급은 `includes/level.php`가 글 수 → 5단계)
- **[말]** *"이건 여러 표를 합친 계산이라 표 하나엔 안 보여요 — 화면이 그 결과."*

---

### 5. 관계 — 복합키·투표·신고 ⭐
**추천 (likes 토글)**
- **[홈페이지]** 👍 추천 → **[DBeaver]** `likes` ⟳ → `(user_id, post_id)` 행 추가(**id 없는 표**)
- **[홈페이지]** 다시 클릭(취소) → `likes` ⟳ → 행 사라짐(토글)

**투표 (votes — INSERT·UPDATE·DELETE 다 나옴)**
- 👍 추천 → `votes` ⟳ 행 추가 / 👎 비추천 → **`choice`만 바뀜(갈아타기)** / 👎 재클릭 → 행 사라짐(취소)

**신고 (reports + UNIQUE)**
- 🚩 신고 → `reports` ⟳ 행 추가 / 같은 글 또 신고 → **행 안 늘어남**(도배 차단)

- **[코드]** `sql/schema.sql`
  ```sql
  PRIMARY KEY (user_id, post_id)               -- likes: 짝이 곧 기본키 = 1인 1회
  UNIQUE KEY uq_report (reporter_id, post_id)  -- reports: 1인 1글 1회
  ```
- **[원리]** "1인 1회"를 코드 아닌 **DB 구조(복합키·UNIQUE)** 가 보장.

**좋아요한 글 (역방향 조회)**
- **[홈페이지]** 프로필 → 좋아요한 글 탭
- **[코드]** `includes/posts.php` `get_liked_posts()`
  ```sql
  FROM likes lk JOIN posts p ON lk.post_id = p.id WHERE lk.user_id = ?
  ```
- **[원리]** 같은 `likes`를 "이 글 몇 명?" ↔ "이 사람 뭐 눌렀?" **양방향** 활용.

---

### 6. 알림 & 외부데이터 ⭐
**알림 (연쇄 INSERT + 읽음 UPDATE)**
- 준비: **탭 두 개** — 탭A `?as=영화광`, 탭B `?as=해석러` (세션이 없어 시크릿창이 필요 없다)
- **[홈페이지]** (해석러) 영화광 글에 댓글
- **[DBeaver]** `comments` ⟳ 새 행 / `notifications` ⟳ 알림 행(`is_read=0`)
- **[홈페이지]** (영화광) 🔔 열기 → `notifications` ⟳ → **`is_read` 0→1**
- **[코드]** `comment/create.php` → 댓글 저장 직후 `create_notification()` (자기 댓글 제외)

**TMDB media 자동저장**
- **[홈페이지]** 검색 → **아직 글 없던 새 작품** → 글쓰기
- **[DBeaver]** `media` ⟳ → **그 작품 행 새로 등장**
- **[코드]** `includes/media.php` `ensure_media()`, `includes/tmdb.php` `tmdb_get()`(30분 캐시)
- **[원리]** 작품은 **글이 처음 달릴 때만** `media`에 저장(UNIQUE `tmdb_id`로 중복 방지). TMDB 응답은 파일 캐시로 홈 4.8초→0.6초.

---

### 7. 킬러 시연 — 영구삭제 CASCADE ⭐⭐
- 댓글·추천이 달린 글 준비 → **[DBeaver]** `comments`·`likes`에 그 글 행 확인
- **[홈페이지]** 휴지통 → 🔥 영구삭제
- **[DBeaver]** `posts` ⟳ 행 진짜 사라짐 / `comments`·`likes` ⟳ → **딸린 행도 같이 사라짐!**
- **[원리]** 글을 진짜 지우면 댓글·추천·알림도 **외래키 CASCADE**로 자동 정리.

---

### 8. 마무리 — 재현성 (30초)
> "데이터는 Git으로 안 옮기고 **`sql/schema.sql` + `sql/seed.sql`** 로 어디서든 재생성. 표 8개·외래키·복합키·소프트삭제가 이 두 파일에 다 들어있습니다."

---

## 3. DBeaver 실시간 시연 12장면 (요약)

| # | 홈페이지 동작 | DBeaver ⟳ | 볼 것 |
|---|---|---|---|
| 1 | 글쓰기 | posts, users | author_id 번호 ↔ users 이름 |
| 2 | 글 삭제 | posts | 행 남고 deleted_at만 |
| 3 | 되돌리기 | posts | deleted_at → NULL |
| 4 | 추천/취소 | likes | 짝 행 추가/삭제(토글) |
| 5 | 작품 투표 | votes | INSERT→choice UPDATE→DELETE |
| 6 | 신고 ×2 | reports | 1건만(도배 차단) |
| 7 | 댓글 | comments, notifications | 두 표에 행 |
| 8 | 알림 열람 | notifications | is_read 0→1 |
| 9 | 새 작품에 글 | media | 작품 행 등장 |
| 10 | 회원가입 | users | 새 회원 행 |
| 11 | 글 수정 / 댓글삭제 | posts / comments | UPDATE / deleted_at |
| 12 | 영구삭제 | posts, comments, likes | CASCADE 동시 삭제 |
| + | 닉네임/비번 변경 | users | nickname / password만 변경 |

---

## 4. 표 8개 커버리지 체크

| 표 | 등장 장면 |
|---|---|
| users | 회원가입·닉네임/아바타·비번·로그인 |
| media | TMDB 자동저장 |
| posts | 작성·수정·소프트삭제·되돌리기·영구삭제 |
| comments | 작성·삭제 |
| likes | 추천 토글 |
| votes | 투표 갈아타기/취소 |
| reports | 신고 + 도배 차단 |
| notifications | 생성 + 읽음 |

→ **8개 전부** 시연에 등장.

---

## 5. 하이라이트 3개 (이것만은 꼭)

1. **삭제해도 행이 안 사라짐** (소프트삭제, 장면 2)
2. **영구삭제 시 CASCADE로 댓글·추천도 동시 삭제** (장면 12)
3. **추천·투표 토글 = 복합키로 1인 1회** (장면 4·5)

→ "DB를 구조적으로 이해했다"를 가장 잘 보여주는 3가지.

## 시연 팁
- 미리 실행해두지 말고 **동작 → 그때 ⟳** 해야 "실시간" 느낌.
- 표는 **id 내림차순**으로 정렬 → 새 행이 맨 위.
- 조회수(`posts.views`)는 자동 증가 안 함 → 그 얘긴 하지 않기.
- **주소에 한글 알림이 잠깐 뜬다** — 동작 직후 주소가
  `/board/?…&flash=🗑 글이 삭제되었습니다.` 가 됐다가, 알림을 그린 뒤 JS가 지워서 곧 깨끗해진다.
  1번 구간에서 `?as=`를 이미 설명했으면 "알림도 같은 방식"으로 한마디면 끝난다.
- 되돌리기 버튼이 달린 알림은 **8초**, 보통 알림은 3초 뒤 사라진다 — 삭제 시연 때 서두르지 말 것.

---

# 6. 보안 슬라이드 (DB) — 2장

## 보안 1/2 — DB 보안 (1): "입력을 그대로 믿지 않는다"
> DB를 쓰면 '사용자 입력'과 '비밀번호'라는 위험이 들어온다. 둘 다 그대로 두지 않았다.

**① SQL 주입 (injection)**
- **문제**: 입력을 SQL 문장에 그대로 이어 붙이면, 입력이 '값'이 아니라 **'명령'으로 실행**된다.
- **공격 예**: 로그인 아이디 칸에 `' OR '1'='1` →
  ```sql
  SELECT * FROM users WHERE username = '' OR '1'='1'
  -- 조건이 항상 참 → 비밀번호 없이 로그인 통과
  ```
- **대응**: SQL 뼈대와 값을 분리, 값은 `?`에 데이터로만 전달 (Prepared Statement)
  ```php
  $stmt = db()->prepare('SELECT * FROM users WHERE username = ?');
  $stmt->execute([$username]);   // 값은 절대 SQL로 해석 안 됨
  ```
  → 프로젝트의 **모든 쿼리**가 이 방식.

**② 비밀번호 해싱**
- **문제**: 평문 저장 시 DB 한 번 유출 = 전 회원 비번 노출 (재사용 탓에 다른 사이트까지 위험).
- **대응**: `password_hash()`로 되돌릴 수 없게 저장, 로그인은 `password_verify()`로 대조.
  ```php
  $hash = password_hash($pw, PASSWORD_DEFAULT);  // 저장
  password_verify($input, $hash);                // 로그인 대조
  ```

> **핵심**: 사용자 입력·비밀번호는 '위험물' — 그대로 쓰지 않고 **안전하게 감싸서** 다룬다.

## 보안 2/2 — DB 보안 (2): "규칙은 DB·서버가 강제한다"
> 화면에서 막아도 사용자는 요청을 조작할 수 있다. 그래서 진짜 방어는 DB 구조와 서버에 둔다.

**③ 무결성 — DB 구조가 잘못된 데이터를 거부**
- **외래키(FK)**: 글의 `author_id`는 실제 회원이어야 함 → 없는 번호로 글? **DB가 거부**
- **복합키**: 같은 사람이 같은 글 두 번 추천 불가 → **1인 1회**
- **UNIQUE**: 같은 글 도배 신고 차단
  ```sql
  PRIMARY KEY (user_id, post_id)       -- likes: 1인 1회
  UNIQUE KEY  (reporter_id, post_id)   -- reports: 도배 방지
  ```
  → "1인 1회 / 중복 금지"를 코드가 아니라 **DB 구조가 보장**.

**④ 권한 재확인 — 서버에서 소유권 확인**
- **문제**: '삭제' 버튼을 숨겨도, 주소를 직접 치거나 요청을 조작해 **남의 글 번호**를 `/post/delete.php`로 보낼 수 있다.
- **대응**: 처리 서버에서 **"이 글 주인 = 지금 로그인한 사람?"** 을 다시 확인.
  ```php
  if (!is_owner($post['author'])) { /* 거부 */ }
  // 수정·삭제·복원·영구삭제 전부 서버에서 재확인
  ```

> **킬러 멘트**: 화면에서 버튼 숨김은 **편의**일 뿐, 진짜 보안은 **DB 구조 + 서버 확인**.

**⑤ 아직 못 막은 것 — 신원 위조 (숨기지 말고 먼저 말할 것)**
- 지금은 로그인 상태를 주소(`?as=아이디`)로 나른다 → **주소를 고치면 남이 될 수 있다.**
  `is_owner()`는 "이 글 주인 = 지금 신원?"을 성실히 확인하지만, **그 '지금 신원' 자체를 못 믿는다.**
- 못 막는 이유: 브라우저가 보내는 값은 GET·POST·hidden 무엇이든 사용자가 고칠 수 있다.
- 해결책은 **정답을 서버가 자기 쪽에 들고 있는 것 = 세션** → **week16**.
- 이번 주에 한 것은 **비밀번호를 지키는 일**(해시), 다음 주에 할 것은 **로그인 상태를 지키는 일**.

> Q&A에서 나오기 전에 우리가 먼저 말하는 게 낫다. "알고 남겨둔 것"과 "모르고 뚫린 것"은 다르다.

---

# 7. 이미지 처리 — 1장

## 이미지는 DB에 어떻게 저장했나? — "파일은 저장소, DB엔 경로"

**실무 표준 아키텍처** — 이미지 '파일'은 저장소에, DB엔 그 파일의 '위치'만:
```
[파일]  uploads/avatars/user1.webp              ← 실제 이미지 (대규모면 S3/CDN)
[DB]    users.avatar = '/uploads/avatars/user1.webp'  ← '위치'만 저장
[화면]  <img src="그 경로">  → 브라우저가 저장소에서 직접 받아감
```
> 확장자가 `.webp`인 이유: 서버에 GD가 없어서, **올리기 전에 브라우저 Canvas로
> 256px 정사각형 WebP로 압축**해 보낸다(2MB → 약 20KB). 서버 검증은 그대로 다 한다.

- **왜 DB에 통째로(BLOB) 안 넣나**
  - DB 용량 폭증 → 백업·복제 느려짐 / 이미지마다 DB를 거쳐야 함(파일은 웹서버가 바로 서빙) / 브라우저·CDN 캐싱 어려움
  - 그래서 Rails·Django 같은 실무 프레임워크도 기본이 "파일은 외부 저장, DB엔 참조"
- **우리 구현**: `save_avatar()`(검증 → uploads 저장 → 경로 반환) + `set_avatar()`(`UPDATE users SET avatar = ?`)
  - 대규모라면 그 경로가 **S3·CDN URL**로 바뀔 뿐 **구조는 동일**
- **안전하게 받기 (3중 방어)**: 진짜 이미지인지 MIME 검사(finfo) · 파일명 강제 재생성 · uploads 폴더 PHP 실행 차단
- 📚 비유: 도서관이 책 원본은 서가(폴더)에, 목록카드(DB)엔 '몇 번 서가'만.

**DBeaver 시연**: 프로필 이미지 변경 → `users` ⟳ → `avatar` 칸에 **경로 문자열**이 채워짐(이미지가 아니라 경로!).

> 한 줄: **"파일은 파일시스템(또는 S3), DB엔 경로."** — 실무 표준 그대로.
