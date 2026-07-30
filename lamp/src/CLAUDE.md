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
- `week15/` : **지금 여기**. week14를 통째로 복사(`cp -a`)해서 시작.
  - 목표: **MariaDB 연결 + 실제 CRUD**. 세션 임시저장 → 진짜 DB로 교체. **핵심 전환 완료.**
  - 방식: `includes/` 도메인 모듈 함수의 '속'만 SQL로 바꿈 (화면 파일은 거의 그대로).
    반환 배열 모양을 week14와 동일하게 유지 → 필터·정렬·페이징·화면 코드 재사용.
  - **DB 표 7개**: users · media · posts · comments · likes · votes · reports
    (+ posts·comments는 소프트삭제, users는 avatar. schema.sql·seed.sql로 재생성 가능)
    - likes·votes는 **복합 기본키**(user_id, post_id/media_id)로 '1인 1회' 보장.
    - 삭제는 **소프트삭제**(deleted_at) → 되돌리기 유지.
    - 외래키 삭제정책: 글→댓글·추천 CASCADE, 회원 참조는 RESTRICT.
  - **TMDB API 연동**: 작품(media)은 더미가 아니라 실제 영화·드라마 데이터.
    - 검색(search.php)은 TMDB 실시간, 게시판은 DB에 없으면 TMDB 폴백(tmdb_find_by_id).
    - **글·투표 시 그 작품을 media 표에 자동 저장**(ensure_media_by_slug).
    - slug 규칙: `tmdb-<tmdb_id>` (예: tmdb-496243 = 기생충).
    - **홈·작품목록은 TMDB 인기작을 직접 보여준다**(넷플릭스식). media 표는 화면 목록이
      아니라 '글이 저장될 때만 참조하는 내부 저장소' — 즉 "글 달린 작품"만 우리 DB에 존재.
    - `tmdb.php`: search_tmdb / tmdb_trending / tmdb_popular(movie·tv) / tmdb_find_by_id /
      build_media_from_tmdb(poster·backdrop). 목록은 3페이지(~60개)까지 이어붙임.
    - **응답 캐싱**: `tmdb_get`이 결과를 `cache/tmdb/`에 30분 저장 → 홈 로딩 4.5초→0.2초.
      (cache 폴더는 .gitignore. 캐싱 없으면 홈이 TMDB를 10여 번 불러 느림)
    - 키는 `includes/config.php`의 TMDB_TOKEN(v4). config.php·CLAUDE.md에 학습용 키 명시.
  - **UI/디자인**: 전체 **다크 테마**(#141414 + 은은한 라디얼 글로우·그라데이션 fixed 배경).
    - 홈: **히어로 배너**(커뮤니티 1위 작품 backdrop) + 사이드바(🔥지금 뜨는 글·🎲오늘의 발견)
      → 넓으면 2단, 좁으면 1단(1100px 기준). 아래 커뮤니티 5칸 그리드 + TMDB 가로 줄 + 최근 글.
    - 가로 줄: 스크롤바 숨김 + JS로 ‹ › 화살표·마우스휠→가로 스크롤(main.js). 본문 폭 1400px
      (글 읽기·폼은 760px 유지). 반응형 미디어 쿼리로 모바일 축소.
    - 상단바·사이드바는 반투명(backdrop-filter)으로 배경 글로우가 비침. 로고 흰색.
    - `includes/media_row.php`: 작품 가로 줄 렌더링 조각(lg=커뮤니티 그리드 / sm=TMDB 줄).
  - **구현 완료**: 로그인·회원가입(users, password_hash) / 글 CRUD(JOIN 조회) /
    댓글 / 추천(likes) / 투표(votes) / 신고(reports) / 소유권·권한 서버 확인 /
    검색→게시판→글쓰기 전 흐름 / **프로필 이미지 업로드**(uploads/avatars/, 다층 방어).
  - **이미지 업로드 보안 3중**: ①MIME(내용)으로 진짜 이미지만(finfo) ②파일명 강제
    재생성(user<id>.<ext>) ③httpd.conf에서 uploads 폴더 PHP 실행 차단(다층 방어).
  - **DB 전환 완료** — 세션에 남은 건 로그인 상태·플래시·최근 본 글뿐.
  - **DB 이론 발표자료 병행 제작 중**(DB란/정규화/ER/B+트리/SQL·CRUD/JOIN 등).
    근거는 영문 위키·MariaDB 공식문서 기반. 톤은 빅테크 시니어→중학생.
    ※ JOIN 슬라이드는 아직 미완성(도입 논리 다듬다 중단).

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
  - DB 데이터는 Git으로 안 옮김 → **`week15/sql/schema.sql`+`seed.sql`을 Git에 두고** 각 기기서 재생성.
    표가 없거나 비어 있으면 **entrypoint.sh가 자동으로 두 파일을 실행**한다(수동 실행 불필요).
  - **DB를 처음 상태로 되돌리려면**(시연 전 초기화 등):
    `docker compose down -v && docker compose up -d`
    `-v`가 볼륨까지 지우므로 다음 시작 때 schema+seed가 다시 깔린다. **데이터가 사라지니 주의.**
- **TMDB API** (영화·드라마 데이터): media 표를 채우고 검색에 사용. v4 토큰 헤더 방식(`Authorization: Bearer`).
  - 키는 `week15/includes/config.php`에 있음(PHP가 읽음).
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
  (커밋은 사용자가 직접). ※ 현재는 git 저장소가 아님 — git 도입 후 적용.
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
