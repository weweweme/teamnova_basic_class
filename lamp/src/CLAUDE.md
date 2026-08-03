# 게시판 프로젝트 (대학 웹프로그래밍 과제)

## 이 저장소는 무엇인가
대학 웹 프로그래밍 수업의 **주차별 실습 폴더 모음**.
최종 목표는 **게시판 웹사이트** = 회원가입 · 로그인 · 글쓰기 · 목록 · 댓글 · DB 저장.
디테일(댓글, 페이징, 검색 등)은 많을수록 좋다.
주차별로 `week13/`, `week14/` ... 폴더에 실습물을 쌓아간다.

## 현재 진행 상황
- `week13/` : `phpinfo()` 띄우기 (PHP 동작 확인) — 완료
- `week14/` : **완료**. 핵심은 **GET/POST(HTTP 메서드)를 다양한 상태로 최대한 써보기**.
  - 커뮤니티 "껍데기" 완성(작품·게시판·글·댓글·검색·프로필·로그인 UI + GET/POST 흐름).
    알맹이(DB 저장)는 **세션에 임시 저장**으로 "동작하는 것처럼" 시연 가능하게 구현.
  - 컨셉: **영화·드라마 리뷰 커뮤니티** (원래 종목토론방이었으나 실시간 시세 문제로 전환 — 아래 설계 참고).
  - 방식: 폼을 GET/POST로 보내고 PHP `$_GET`/`$_POST`로 받아 화면에 반영.
    Android **Intent**(화면전환 + putExtra)에 비유하면 사용자가 빠르게 이해함.
  - 발표자료(`week14/발표대본.md` 포함) + README 완비. **제출 완료본이므로 week14는 건드리지 않는다.**
- `week15/` : **완료**. week14를 통째로 복사(`cp -a`)해서 시작.
  - 목표: **MariaDB 연결 + 실제 CRUD**. 세션 임시저장 → 진짜 DB로 교체. **전환 완료.**
  - 방식: `includes/` 도메인 모듈 함수의 '속'만 SQL로 바꿈 (화면 파일은 거의 그대로).
    반환 배열 모양을 week14와 동일하게 유지 → 필터·정렬·페이징·화면 코드 재사용.
  - **DB 표 8개**: users · media · posts · comments · likes · votes · reports · notifications
    (+ posts·comments는 소프트삭제, users는 avatar. schema.sql·seed.sql로 재생성 가능)
    - likes·votes는 **복합 기본키**(user_id, post_id/media_id)로 '1인 1회' 보장.
    - 삭제는 **소프트삭제**(deleted_at) → 되돌리기 유지.
    - 외래키 삭제정책: 글→댓글·추천 CASCADE, 회원 참조는 RESTRICT.
  - **TMDB API 연동**: 작품(media)은 더미가 아니라 실제 영화·드라마 데이터.
    - 작품 검색은 TMDB 실시간(→ 아래 '통합검색'), 게시판은 DB에 없으면 TMDB 폴백(tmdb_find_by_id).
    - **글·투표 시 그 작품을 media 표에 자동 저장**(ensure_media_by_slug).
    - slug 규칙: `tmdb-<tmdb_id>` (예: tmdb-496243 = 기생충).
    - **홈·작품목록은 TMDB 인기작을 직접 보여준다**(넷플릭스식). media 표는 화면 목록이
      아니라 '글이 저장될 때만 참조하는 내부 저장소' — 즉 "글 달린 작품"만 우리 DB에 존재.
    - `tmdb.php`: search_tmdb / tmdb_trending / tmdb_popular(movie·tv) / tmdb_find_by_id /
      build_media_from_tmdb(poster·backdrop). 목록은 3페이지(~60개)까지 이어붙임.
    - **응답 캐싱**: `tmdb_get`이 결과를 `cache/tmdb/`에 30분 저장 → 홈 로딩 4.5초→0.2초.
      (cache 폴더는 .gitignore. 캐싱 없으면 홈이 TMDB를 10여 번 불러 느림)
    - 키는 `includes/config.php`의 TMDB_TOKEN(v4). config.php·CLAUDE.md에 학습용 키 명시.
  - **통합검색**(포털식): 상단바의 검색칸 → `/search/`. **통합은 맛보기, 전용 페이지가 전체.**
    - 화면 4개: `search/index.php`(통합) + `works.php`·`posts.php`·`users.php`(카테고리별 전체+페이징).
      네 화면이 `includes/search_ui.php`의 조각(검색창·탭·결과 줄·페이지 이동)을 공유한다.
    - 찾는 곳: 작품=TMDB 실시간 / 글=`posts`(제목·내용 LIKE) / 유저=`users`(아이디·닉네임 LIKE).
      회원 조회는 `includes/users.php`로 분리 — `auth.php`는 "지금 요청한 사람이 누구인가"만 맡는다.
    - 미리보기 작품 3·글 5·유저 3. **결과가 0이어도 세 칸은 자리를 지킨다**
      (빈 칸을 지우면 검색할 때마다 화면 구조가 달라져 '없음'이 아니라 '고장'으로 보인다).
      **더보기는 미리보기보다 많을 때만**(3개뿐인데 더보기를 띄우면 거짓 약속).
    - **작품만 개수를 안 적는다** — 통합은 TMDB 1페이지(20개)만 받고 전용 화면은 3페이지(~60개)라
      숫자가 어긋난다. 모르는 수는 말하지 않는다. 글·유저는 `COUNT`라 정확.
    - `create_like_pattern()`(db.php): 검색어의 `%`·`_`를 이스케이프.
      안 하면 `%` 검색에 글 전체가 걸린다. `%`는 SQL이 아니라 **값**에 붙여 바인딩(인젝션 방어).
    - 글 검색은 `search_posts_db()`(LIMIT/OFFSET). 기존 `search_posts()`(PHP 배열 필터)는
      게시판이 계속 쓰므로 그대로 둔다 — 게시판은 이미 한 작품 글만 다뤄 작다.
    - 상단바 폼은 `method="get"` + URL 리라이터가 hidden `as`를 자동 삽입 → 검색해도 로그인 유지.
      상단바 칸은 `/search/`일 때만 `?q=`를 채운다(게시판 `?q=`는 성격이 달라 안 끌어옴).
  - **페이징 기준**: 게시판 글 15개 / 댓글은 **원댓글 20개**(답글은 부모를 따라간다 — 줄 단위로
    자르면 답글이 부모와 다른 페이지로 찢어짐) / 검색 전용 페이지 20개.
    댓글은 `?cpage=`, 작성·수정·삭제 후 **그 댓글이 보이는 페이지로** 돌아가고 알림 링크에도 실린다.
  - **UI/디자인**: 전체 **다크 테마**(#141414 + 은은한 라디얼 글로우·그라데이션 fixed 배경).
    - 홈: **히어로 배너**(커뮤니티 1위 작품 backdrop) + 사이드바(🔥지금 뜨는 글·🎲오늘의 발견)
      → 넓으면 2단, 좁으면 1단(1100px 기준). 아래 커뮤니티 5칸 그리드 + TMDB 가로 줄 + 최근 글.
    - 가로 줄: 스크롤바 숨김 + JS로 ‹ › 화살표·마우스휠→가로 스크롤(main.js). 본문 폭 1400px
      (글 읽기·폼은 760px 유지). 반응형 미디어 쿼리로 모바일 축소.
    - 상단바·사이드바는 반투명(backdrop-filter)으로 배경 글로우가 비침. 로고 흰색.
    - `includes/media_row.php`: 작품 가로 줄 렌더링 조각(lg=커뮤니티 그리드 / sm=TMDB 줄).
  - **구현 완료**: 로그인·회원가입(users, password_hash) / 글 CRUD(JOIN 조회) /
    댓글(대댓글 1단계·수정·소프트삭제·페이징) / 추천(likes) / 투표(votes) / 신고(reports) /
    알림(notifications) / 랭킹 / 소유권·권한 서버 확인 / **통합검색(작품·글·유저)** /
    검색→게시판→글쓰기 전 흐름 / **프로필 이미지 업로드**(uploads/avatars/, 다층 방어).
  - **이미지 업로드 보안 3중**: ①MIME(내용)으로 진짜 이미지만(finfo) ②파일명 강제
    재생성(user<id>.<ext>) ③httpd.conf에서 uploads 폴더 PHP 실행 차단(다층 방어).
  - ★ **세션을 하나도 쓰지 않는다**(`session_start()` 없음). 데이터는 전부 DB로 갔고,
    서버가 요청 사이에 기억해야 할 것은 **전부 주소에 실어 나른다**:
    - 신원 → `?as=아이디` (`IDENTITY_KEY`). 심는 곳=`login_and_redirect()`,
      이어붙이는 곳=`build_url()`, 읽는 곳=`current_user_row()` 하나뿐.
    - 링크 30여 곳에 손으로 안 붙이려고 PHP 내장 **URL 리라이터**(`output_add_rewrite_var`)를
      `header.php`에서 켠다 → `<a href>`엔 쿼리, `<form>`엔 hidden 필드가 자동 삽입.
      JS가 만드는 링크만은 못 건드려서 `main.js`의 `withIdentity()`가 처리.
    - 플래시 → `?flash=&ftype=&fundo=&fid=`. 주소가 지저분해지고, ①JS로 주소창 청소
      ②`query_url()`에서 `FLASH_KEYS` 제외 — **우회책 두 개**가 딸려 있다.
    - '최근 본 글'은 주소로 못 날라서 **기능 자체를 week16으로 미뤘다**(`posts.php` 하단 주석).
    - ★★ 이 방식은 **사칭을 못 막는다**. `?as=`를 남의 아이디로 고치면 그 사람이 된다.
      비번 확인은 로그인 순간 한 번뿐이고 이후엔 "내가 누구라고 주장하는 값"을 믿기 때문.
      → **이 한계가 week16(세션)의 출발점**이다. `auth.php` 주석에 그대로 적혀 있다.
  - **DB 이론 발표자료**(`week15/발표_DB중심.md`): DB란/정규화/ER/B+트리/SQL·CRUD/JOIN 등.
    근거는 영문 위키·MariaDB 공식문서 기반. 톤은 빅테크 시니어→중학생.
    ※ JOIN 슬라이드는 아직 미완성(도입 논리 다듬다 중단).
  - **제출 완료본이므로 week15는 건드리지 않는다.**
- `week16/` : **지금 여기**. week15를 통째로 복사해서 시작 (2026-08-03).
  - 목표: **쿠키와 세션 적용**. week15가 주소로 나르던 것을 서버(세션)와 브라우저(쿠키)로 옮긴다.
  - **왜 이 순서인가**: week15에서 "주소에 다 실어보기"를 끝까지 밀어붙였기 때문에
    그 한계(사칭·지저분한 주소·못 만든 기능)가 코드에 흉터로 남아 있다.
    week16은 **새 기능을 더하는 주차가 아니라 그 흉터를 걷어내는 주차**다.
    → 시연 포인트: "week15에서 이걸 하려고 만든 우회책이 통째로 사라집니다."
  - **핵심 축 두 개** (이 구분을 문서·주석에서 계속 유지한다):
    - **세션 = 서버가 기억**. 값은 서버 금고에 있고 브라우저엔 번호표(쿠키 `PHPSESSID`)만.
      → 사용자가 못 고친다 = **믿을 수 있다**. 브라우저를 닫으면 사라진다.
    - **쿠키 = 브라우저가 기억**. 값 자체가 사용자 PC에 있다.
      → 사용자가 고칠 수 있다 = **믿으면 안 된다**. 대신 브라우저를 닫아도 남는다.
    - 판단 기준: **틀리면 손해 보는 것은 세션, 틀려도 취향일 뿐인 것은 쿠키.**
  - **구현 예정** (우선순위 순 — 자세한 근거는 `week16/README.md` 11절):
    1. ✅ **세션 로그인 전환 — 완료.** `?as=` 제거 → `$_SESSION['user_id']`.
       - 새 파일 `includes/session.php`가 세션을 켜는 **유일한 자리**. `util.php`가 부른다
         (모든 페이지가 util을 가장 먼저 부르므로 화면 파일은 세션을 신경 쓸 필요가 없다).
         쿠키 옵션 `HttpOnly`·`SameSite=Lax`·`secure`(https일 때만) + `use_strict_mode`.
       - 세션엔 **회원 번호만** 담는다(`SESSION_USER_ID`). 닉네임·아바타는 매 요청 DB 조회
         → 설정에서 바꿔도 재로그인 없이 바로 반영된다.
       - 로그인 시 `session_regenerate_id(true)`(세션 고정 방어), 로그아웃은 3단계
         (금고 비우기 → 번호표 쿠키 회수 → `session_destroy()`).
       - **삭제된 것**: `IDENTITY_KEY`·`identity_from_request()`·`identity_params()`,
         `header.php`의 URL 리라이터 블록, `main.js`의 `withIdentity()`.
       - curl로 확인: 사칭(`?as=`) 차단 ✅ / 세션ID 재발급 ✅ / 링크 `as=` 0건 ✅ / 로그아웃 쿠키 삭제 ✅.
    2. ✅ **플래시를 세션으로 — 완료.** `$_SESSION['flash']` + `take_flash()`가 read-once로 꺼낸다.
       - **삭제된 것**: `FLASH_KEYS`·`flash_pending()`·`flash_params()`,
         `redirect()`의 알림 싣기, `query_url()`의 걸러내기, `main.js`의 주소 청소 블록.
       - **호출부 30여 곳은 그대로** (`set_flash()` → `redirect()`) — 나르는 방식은
         그 두 함수 안에만 있던 사정이라 밖으로 새지 않았다.
       - `UNDO_TARGETS`는 남되 성격이 바뀌었다: 주소로 들어오던 값이 아니게 되어
         '보안 화이트리스트' → '되돌리기 버튼 설정 표'. `"12,34"` 파싱도 사라짐.
       - curl로 확인: 리다이렉트 주소 깨끗 ✅ / 알림 1회 표시 ✅ / 새로고침 시 사라짐 ✅ /
         삭제→되돌리기 버튼→복구 ✅ / 화면 11곳 200 ✅.
    3. ✅ **CSRF 토큰 — 완료.** 새 파일 `includes/csrf.php` (함수 3개).
       - `csrf_token()`(세션당 1개 발급) · `csrf_field()`(폼 hidden) · `require_csrf()`(대조).
         `util.php`가 불러줘서 전 화면·전 액션에서 바로 쓸 수 있다.
       - `random_bytes(32)`로 발급(`rand()` 금지 — 재현 가능한 난수라 추측된다),
         대조는 `hash_equals()`(타이밍 공격 방어). 세션당 하나를 유지해 여러 탭에서도 안 깨진다.
       - **적용: POST 액션 17개 + POST 폼 20개 전부.**
       - curl로 확인: 토큰 없음/위조 → 거부(글 그대로) ✅ / 정상 토큰 → 전 기능 정상 ✅ /
         폼 토큰 누락 0건 ✅.
       - ⚠️ 함정 기록: `//` 한 줄 주석 안에 PHP **닫는 태그**를 적으면 거기서 PHP가 끝나
         파일 아래쪽이 화면에 글자로 쏟아진다. 주석에 코드 예시를 적을 때 주의.
    4. ✅ **'최근 본 글' 부활 — 완료.** `remember_recent_post()` / `get_recent_posts()`
       (`includes/posts.php`) — week15 주석이 예고한 이름 그대로.
       - 세션에 **글 번호만** 최대 5개(`RECENT_POSTS_MAX`). 기록은 `post/view.php`,
         표시는 홈 사이드바 👀 칸. 지워진 글은 `get_post()`가 null이라 자동으로 빠진다.
       - **DB 표로 안 만든 이유**: 기록의 주인이 '브라우저'→'회원'으로 바뀌어
         로그인 안 한 방문자가 빠진다. 세션이 딱 맞는 그릇.
       - curl로 확인: 최신순 정렬 ✅ / 재열람 시 맨 앞으로 ✅ / 5개 초과 시 오래된 것 버림 ✅ /
         삭제된 글 자동 제외 ✅ / **비로그인 상태에서도 정상 동작** ✅.
    5. **조회수 중복 방지** — `posts.views`가 지금 **아예 증가하지 않는다**(seed 숫자 고정).
       게시판에 '조회' 정렬 탭이 있는데 값이 안 변하는 상태. 세션으로 1인 1회 카운트.
    6. **자동 로그인(Remember me)** — 세션의 한계(브라우저 닫으면 끝)를 쿠키로 보완.
       실무 방식: 랜덤 토큰을 DB에 해시로 보관, 쿠키엔 토큰만. `HttpOnly`·`SameSite`.
    7. **취향 쿠키** — 게시판 정렬 기본값·최근 검색어 등. 틀려도 손해가 없는 값들.
  - **새 표**: `remember_tokens`(자동 로그인). 마이그레이션 `004_`부터 이어간다
    (001~003은 week15에서 이미 적용됨 — `schema_migrations`가 파일명으로 기록).
  - **DB는 week15와 공유**한다(`review_community` 볼륨 그대로) → 기존 회원·글이 살아 있다.

## week14 커뮤니티 설계 (영화·드라마 리뷰 컨셉)
> 컨셉: **작품(영화·드라마)별 리뷰 커뮤니티**. 유저가 작품별 게시판에서 감상(호평·보통·혹평)을
> 나누고, 작품에 추천/비추천 투표를 한다. **외부 API 의존 0** — 전부 더미 데이터.
> week14 핵심 = **GET/POST를 최대한 다양하게 써보기**. 알맹이(DB 저장)는 stub.
>
> ※ 원래 '종목토론방' 컨셉이었으나 **KRX 실시간 시세는 TradingView 임베드가 막혀 있고**
>   증권사 API는 계좌·재배포 약관 문제가 있어, **리뷰 커뮤니티로 전환**(2026-07).
>   GET/POST 구조는 그대로 재사용했고 용어·더미데이터만 교체함.

**폴더/파일 구조 (뷰=GET, 액션=POST·PRG):**
```
week14/
├── index.php              홈: 작품 리스트 · 인기글            [GET]
├── works.php              전체 작품 목록  ?genre=             [GET]
├── search.php             작품 검색  ?q= (제목·감독)           [GET]
├── profile.php            유저 프로필  ?user=                 [GET]
├── includes/  (공통 조각)
│   ├── header.php  footer.php  (공통 레이아웃 + CSS/JS 캐시버스팅)
│   ├── util.php                (e() 이스케이프, query_url() 파라미터 유지)
│   ├── works.php               (작품 도메인: get_works/get_work/search/filter)
│   └── posts.php               (글 도메인: get/filter/search/sort/paginate)
├── board/index.php        작품 게시판                        [GET] ★핵심
│                          ?work=&q=&sort=&sentiment=&page=   (파라미터 5개 조합)
├── post/  view.php[GET]  write.php[GET]  create.php[POST·PRG]
│          edit.php[GET]  update.php[POST·PRG]  delete.php[POST·PRG]
├── comment/  create.php[POST·PRG]  delete.php[POST·PRG]
├── vote/  sentiment.php   작품 추천/비추천 투표 [POST·PRG]
├── like/  toggle.php      글 추천 [POST·PRG]
├── report/  create.php    신고 [POST·PRG]
├── auth/  (로그인·회원가입 — 다음 단계)
├── assets/  css/style.css  js/main.js
└── uploads/  (이미지 — 나중)
```
- 왜 뷰/액션 분리: 실무 컨트롤러 방식(보여주는 URL vs 처리하는 URL). 각 파일이 딱 한 일만.
- POST 처리 후엔 반드시 **redirect(PRG)** → 새로고침 중복 제출 방지.
- 목록 파이프라인 순서: **작품 → 검색 → 감상필터 → 정렬 → 페이징** (다 거른 뒤라야 총 페이지 수를 셈)

**구현 완료 (week14):**
- **GET**: 홈 / 작품목록(장르필터) / 작품검색(제목·감독) / 게시판(작품·글검색·정렬4종·감상필터·페이징)
  / 글보기 / 프로필(활동통계) / 글쓰기·수정 폼
- **POST(전부 PRG)**: 글 작성·수정·삭제 / 댓글 작성·삭제 / 글 추천 / 신고(모달+사유) / 작품 투표
- **JS**: 신고 모달(`<dialog>`), 삭제 확인(`confirm` + `preventDefault`)
- **품질**: 출력 이스케이프, 화이트리스트 검증, `(int)` 형변환, POST 전용 차단, 존재 확인, CSS/JS 캐시버스팅

**나중에 구현할 것:**
- **세션 인증(로그인 유지)** / MariaDB 연결 + 실제 CRUD
- 데이터 모델(→ 테이블): users / works(slug·title·genre·year·director) / posts(+sentiment) / comments / votes / likes / reports
- 이미지 업로드 실제 저장

## 환경 (LAMP, Docker 컨테이너 내부)
> ★ **환경 구성은 전부 Git으로 관리된다.** 새 컴퓨터에서도 `docker compose up -d` 한 번이면
> DB까지 갖춰진 사이트가 뜬다. 손으로 하는 세팅은 없다. (2026-07 개편)

- OS: Ubuntu 22.04 (도커 컨테이너 안)
- 웹서버: **Apache httpd** (`/usr/local/apache2`), 포트 80, 실행 중
- PHP: **8.5.8** — `mod_php`로 Apache에 내장. **`php` CLI 명령어는 없음.**
  → PHP는 반드시 브라우저(`http://localhost:<포트>/파일.php`)로 실행해서 확인한다.
- DB: **MariaDB 12.3.2** — **같은 컨테이너 안**에 설치됨(`/usr/local/mariadb`, 커스텀 빌드).
  - **CLI는 있음**: `/usr/local/mariadb/bin/{mariadb,mysql}` (예전 메모의 "mysql CLI 없음"은 틀림).
  - **자동 실행** — `docker/entrypoint.sh`가 컨테이너 시작 때마다 기동한다.
    (예전엔 `apachectl`에 자동시작 블록을 주입했으나, 그건 컨테이너 안에만 있어서
     새 기기로 안 따라왔다. 이제 Git에 있는 스크립트가 그 일을 한다)
    수동 기동이 필요하면:
    `/usr/local/mariadb/bin/mariadbd-safe --datadir=/usr/local/mariadb/data --user=root --socket=/tmp/mysql.sock --port=3306 --bind-address=0.0.0.0 &`
  - **데이터 위치**: 도커 볼륨 `lamp_mariadb_data` → `/usr/local/mariadb/data`.
    **컨테이너를 지우고 다시 만들어도 데이터가 유지된다.**
  - **접속정보**: host `localhost`/`127.0.0.1`, port `3306`, DB `review_community`(utf8mb4).
    계정 `dev`/`dev1234`(원격·앱용, `%`), 로컬관리 `root`/`root1234`. 익명계정·test DB 제거함.
    ※ 이 DB·계정도 entrypoint.sh가 없으면 자동 생성한다.
  - **DBeaver**(맥·윈도우 데스크톱 앱)로 접속해 표·데이터를 눈으로 보며 개발. SSL은 끈다.
  - DB 데이터는 Git으로 안 옮김 → **`week16/sql/schema.sql`+`seed.sql`을 Git에 두고** 각 기기서 재생성.
    표가 없거나 비어 있으면 **entrypoint.sh가 자동으로 두 파일을 실행**한다(수동 실행 불필요).
  - **DB를 처음 상태로 되돌리려면**(시연 전 초기화 등):
    `docker compose down -v && docker compose up -d`
    `-v`가 볼륨까지 지우므로 다음 시작 때 schema+seed가 다시 깔린다. **데이터가 사라지니 주의.**
- **TMDB API** (영화·드라마 데이터): media 표를 채우고 검색에 사용. v4 토큰 헤더 방식(`Authorization: Bearer`).
  - 키는 `week16/includes/config.php`에 있음(PHP가 읽음).
    **학습용 읽기전용 키라 config.php를 그대로 Git에 커밋한다**(실무라면 절대 금지 — 아래 주의 참고):
    - v4 Read Access Token: `eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJmMDYwZTlmYzhjZjllYzk2YmMyZTM0Zjc5OTAxYzMwYyIsIm5iZiI6MTc4NTE0NzM1Mi45NzksInN1YiI6IjZhNjcyZmQ4NmZiMDc2M2U3Mzk3ZmZkMSIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.rQChMikxT0pxMU89a-6cTdBX5JSTGBkMUDf5YyHKp9Q`
    - v3 API Key(안 쓰지만 기록): `f060e9fc8cf9ec96bc2e34f79901c30c`
  - 엔드포인트 예: `https://api.themoviedb.org/3/search/movie?query=기생충&language=ko-KR` (헤더에 Bearer 토큰).
- **Apache 설정**: `lamp/apache/httpd.conf` (호스트 · Git 관리).
  컨테이너 시작 때 entrypoint.sh가 컨테이너 안으로 복사한다.
  설정만 고쳤을 땐 컨테이너를 재시작(`docker compose restart`)하면 반영된다.
  - DocumentRoot는 **`${APP_DIR}`** — docker-compose.yml의 `environment: APP_DIR` 값이 들어간다.
    **주차가 바뀌면 docker-compose.yml의 APP_DIR 한 줄만 고치면 된다.**
  - `${APP_DIR}/uploads`에 **PHP 실행 차단**(`php_admin_flag engine off` + 스크립트 확장자 접근 거부).
    ※ `AllowOverride None`이라 `.htaccess`는 무시된다 → 차단은 반드시 이 파일에 둬야 한다.
- **접속 주소**: `http://localhost:<WEB_PORT>/` — 포트는 `.env`에서 정한다.
  **맥 8080 / 윈도우 8081** (기기마다 이미 쓰는 포트가 달라서 통일하지 않음).
- **기기별 설정은 `.env`** (Git 제외): `LAMP_IMAGE`(이미지 이름)·`WEB_PORT`·`DB_PORT`.
  `.env.example`을 복사해서 만든다. 소스코드엔 포트가 하드코딩돼 있지 않아 `.env`만으로 해결된다.
- 컨테이너는 `restart: unless-stopped` — 도커가 실행되면 자동으로 켜진다.
- `sqlite3`도 설치돼 있으나, 과제 필수 스택이 MariaDB라 사용하지 않는다.

### 도커 구성 파일 (전부 Git)
```
lamp/
├── docker-compose.yml   이미지·포트·볼륨·마운트  (주차 변경은 여기 APP_DIR)
├── .env.example         기기별 설정 예시 → 복사해서 .env 로 사용 (.env는 Git 제외)
├── docker/entrypoint.sh 시작 스크립트: MariaDB 설치·기동·DB/계정 생성·schema+seed·캐시 폴더
├── apache/httpd.conf    Apache 설정 (DocumentRoot·PHP 핸들러·uploads 차단)
└── .gitattributes       .sh/.conf 를 LF 고정 (윈도우 CRLF로 스크립트 깨지는 것 방지)
```
- entrypoint.sh의 모든 단계는 **"없으면 만들고 있으면 건너뛴다"** → 몇 번을 켜도 안전하다.
- **이미지(`my_lamp_backup`)만은 Git으로 못 옮긴다**(4GB 바이너리). 기기마다 로컬에 있어야 하며,
  이름이 다르면 `.env`의 `LAMP_IMAGE`로 맞춘다.

## 필수 기술 스택 (교수 지정 — 변경 불가)
- 프론트: **HTML / CSS / JavaScript**
- 백엔드: **PHP**
- DB: **MariaDB**
- 웹서버: **Apache**
→ Node.js / Python / 기타 대체 스택 제안 금지.

## 사용자 프로필 (중요)
- **웹개발은 초보, 프로그래밍 자체는 경험자.** Unity 클라이언트 프로그래머 2년차(C#).
  → 변수·반복문·함수·조건문·OOP(클래스/상속/인터페이스) 기본기는 이미 있음.
  설명은 이런 기초를 반복하지 말고 **웹 고유 개념(HTML 구조, CSS 렌더링, HTTP 요청/응답,
  PHP-서버, DB) 과 문법 차이**에 집중한다.
- 이해가 막히면 **C#/Unity 개념에 비유**하면 빨리 이해한다.
  (예: PHP 세션 ↔ 게임 세이브데이터, DB 테이블 ↔ ScriptableObject 목록 등)
- 답변 언어: **한국어**.

## 협업 방식 (중요)
- **대화 기반 점진적 개발**: 한 번에 전체 코드를 쏟지 않는다.
- **뼈대 먼저**: 화면/데이터 구조·설계를 먼저 대화로 확정한 뒤 코드로 들어간다.
- **단계별 구현 + 매 단계 컨펌**: 기능을 최대한 작게 쪼개 하나씩 만들고,
  각 단계마다 "이 코드가 왜 이런지"를 쉬운 말로 설명 → 사용자가 이해/확인한 뒤 다음으로.
- 이번 과제 진행 모드(사용자 선택): **한 단계씩 같이** = 내가 작은 단위 코드를 만들고 설명,
  사용자가 이해하고 넘어간다. **실제로 동작하는 소스코드 + "정답"을 제공**하되 통째로 던지지 않는다.
- **최소 커밋 단위**: 하나의 커밋에 여러 기능을 섞지 않는다. 구현이 끝나면 **커밋 메시지를 제공**한다
  (커밋은 사용자가 직접). 커밋 메시지는 한국어 · `feat:`/`fix:`/`docs:` 접두어 · 무엇을 왜 바꿨는지 한 줄.
- 사용자는 W3Schools로 각 태그/속성을 병행 학습 중. 개념 설명은 W3Schools 스타일(짧은 예시 + 결과)로.

## 코딩 표준 (이전 과제에서 이어온 일반 원칙)
> 주로 **로직 코드(JavaScript / PHP)** 에 적용. HTML/CSS엔 해당하는 항목(주석·이름짓기·파일분리)만.
> Java 전용 규칙(접근한정자 강제, static/final 메모리, checked exception 등)은 웹 맥락으로 완화해 정리함.

**설명 · 주석**
- **모든 설명은 "빅테크 시니어가 중학생에게 가르치듯"** (★ 이 프로젝트 최우선).
  = 시니어 수준의 정확성·깊이·실무 감각을 담되, 중학생도 이해할 쉬운 비유로 풀 것.
  전문 용어(0-based, nullable 등) 대신 쉬운 한국어 + 비유/예시(레고·가계도·폴더구조 등).
  "~로 변환" 같은 추상적 표현 대신 "왜 필요한지"를 풀어서. 가능하면 **직접 눈으로 확인하는 법**
  (F12 콘솔에서 쳐보기 등)을 곁들이고, "실무에선 왜 중요한지" 한 줄을 덧붙이면 좋음.
- 주석은 **지금 이 코드가 무엇을 하는지**만 담백하게. 설계 경위·패턴 이름·리팩토링 히스토리는 안 남김.
- 변수명·로직 전개·타입/자료구조 선택의 **근거를 주석으로**. 함수엔 한 줄 요약 주석.

**이름 · 구조**
- **파일 단위 분리**: 한 파일에 하나의 주요 관심사(클래스/모듈/기능). 탐색·재사용·리뷰가 쉬워짐.
- **상수/설정은 그 개념을 소유하는 곳에** 둔다. 중앙 집중식 `Config` 덩어리 클래스는 만들지 않는다.
- **매직값 금지**: 반복되거나 의미 있는 숫자/문자는 이름 붙인 상수로 (예: 메뉴 번호를 상수로 통일 관리).
- **가변 전역 상태 금지**: 값이 바뀌는 데이터는 그것을 관리하는 객체 안에 둔다.
- **가시성 최소 공개**: 밖에서 쓸 것만 공개(public), 나머지는 숨긴다.
- **새로 만들어 돌려주면 이름에 드러낸다**: 호출할 때마다 새 객체/배열을 만들어 반환하면
  `create…/build…/parse…/copy…`, 들고 있던 것을 그대로 주면 `get…/find…`.
  호출자가 "내 것(고쳐도 됨) vs 공유물(고치면 원본이 바뀜)"을 이름만 보고 구분하게.

**로직 작성**
- 변수는 **한 줄에 하나씩** 선언 (`let a=1, b=2;` 금지).
- **복합 조건은 이름 붙인 boolean으로 분리**: `if`의 `&&`/`||`가 2개 이상이면 각 조건을
  의미 있는 boolean 변수로 빼서 읽기 쉽게.
- **if-else vs switch**: 범위 비교·복합 조건·null 체크는 `if-else`, 단일 값의 여러 분기는 `switch`.
- **예외처리는 진짜 예외에만**: 단순 입력 검증(숫자 파싱 등)은 try-catch 대신 검증 로직으로 확인.
- **없을 수 있으면 먼저 확인(Tester-Doer)**: 가져와서 null 체크하지 말고, 존재 여부를 먼저 확인하고
  있을 때만 가져온다.
- **null로 지우지 말고 전용 메서드**: `setX(null)` 대신 `removeX()`처럼 의도가 드러나는 이름.
- **반복 호출 함수에서 매번 새 컬렉션 생성 지양**(성능). 단, **그 컬렉션이 밖으로 나가거나
  (return/콜백) 화면이 계속 들고 있으면 반드시 매번 새로 만든다** — 재사용하면 남의 데이터를 지운다.

**객체 설계 (JS 클래스 / PHP OOP에 해당)**
- **필수 의존성은 생성 시 주입**: 객체는 생성 직후 바로 쓸 수 있는 상태여야 한다(setter 지연 주입 지양).
- **서브클래스마다 반복되는 고정값은 부모 생성자로 직접 전달**, 진짜 다른 동작만 오버라이드로 남긴다.

## 학습 습관 (사용자 방식)
- 코드에 **주석을 꼼꼼히 달며 이해한 내용을 정리**한다.
- ★ **테스트 가이드**: 값/속성을 바꿔보고 결과가 어떻게 달라지는지 관찰하게 유도한다.

## 실행 / 확인 방법
**처음 세팅 (새 컴퓨터에서도 이게 전부):**
```bash
cd lamp
cp .env.example .env      # 윈도우: copy .env.example .env
                          #   → LAMP_IMAGE(내 PC의 이미지 이름) · WEB_PORT 를 맞춘다
docker compose up -d
docker compose logs -f    # [entrypoint] 로그로 진행 상황 확인
```
- PHP 확인: 파일을 해당 주차 폴더에 두고 `http://localhost:<WEB_PORT>/파일명.php` 접속.
  (`php` CLI가 없으므로 반드시 브라우저/HTTP로 확인한다)
- 정적 HTML/CSS/JS: 브라우저로 직접 파일 열기, 또는 위 주소 경유.
- 소스는 `./src`가 컨테이너에 연결돼 있어 **저장하면 바로 반영**된다(재시작 불필요).
  단 `apache/httpd.conf`를 고쳤을 땐 `docker compose restart` 필요.
- 자주 쓰는 명령:
  | 목적 | 명령 |
  |---|---|
  | 켜기 | `docker compose up -d` |
  | 끄기 | `docker compose stop` |
  | 설정 반영(재시작) | `docker compose restart` |
  | 로그 보기 | `docker compose logs -f` |
  | DB 접속 | `docker exec -it manual_lamp /usr/local/mariadb/bin/mariadb -udev -pdev1234 --default-character-set=utf8mb4 review_community` |
  | **DB 완전 초기화** | `docker compose down -v && docker compose up -d` ※ 데이터 삭제됨 |

## 주의사항
- DocumentRoot 변경 · Apache 재시작 등 **환경을 바꾸는 작업은 먼저 사용자에게 설명하고** 진행.
- **`mariadb`/`mysql` CLI로 SQL을 넣을 땐 `--default-character-set=utf8mb4`를 반드시 붙인다.**
  빠뜨리면 한글이 `???`로 들어간다. (DB만 utf8mb4로 만들어도, 보내는 쪽이 다르면 서버가 변환해버림)
- **`config.php`는 이 저장소에선 Git에 커밋한다** — 학습용 읽기전용 TMDB 키라서 그렇다.
  실무에선 키·비밀번호를 절대 커밋하지 않는다(`.gitignore` + 예시 파일 방식).
- **`.env`는 커밋하지 않는다** — 기기마다 다른 값(이미지 이름·포트)이라 공유하면 서로 덮어쓴다.
- 맥에서 이 프로젝트는 amd64 이미지를 **로제타로 에뮬레이션**해 돌린다(호스트는 arm64).
  느리게 느껴지면 이 때문일 수 있다.
