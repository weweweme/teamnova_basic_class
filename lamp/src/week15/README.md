# week14 — 영화·드라마 리뷰 커뮤니티

**주제: HTTP 메서드(GET / POST)를 최대한 다양한 상황에서 써보기**

작품(영화·드라마)별로 리뷰를 나누고 투표하는 커뮤니티입니다.
DB 연결은 다음 주차 과제이므로, 데이터는 **더미 + 세션(임시 저장)** 으로 동작합니다.
브라우저를 닫으면 초기화되지만, **시연 중에는 실제로 글이 써지고 지워집니다.**

---

## 1. 실행 방법

| 항목 | 값 |
|---|---|
| 주소 | `http://localhost:8081/` |
| 웹서버 | Apache (mod_php) |
| PHP | 8.5.8 |
| DocumentRoot | `/var/www/html/week14` |

**테스트 계정**

| 아이디 | 비밀번호 | 비고 |
|---|---|---|
| `영화광` | `1234` | 글 8개 보유 — 수정·삭제 시연용 |
| `admin` | `admin1234` | 남의 글은 못 지우는 것 확인용 |

---

## 2. 핵심 설계 — 뷰와 액션을 나눈다

```
보여주는 URL (GET)   ←→   처리하는 URL (POST)
post/view.php             post/delete.php
post/write.php            post/create.php
post/edit.php             post/update.php
```

- **GET** = 조회. 주소만 보고 같은 화면을 다시 열 수 있어야 한다 (공유·북마크 가능).
- **POST** = 변경. 처리 후 **반드시 리다이렉트**해서 새로고침 중복 제출을 막는다.

### PRG (Post → Redirect → Get)

```
[POST] /post/create.php  →  저장  →  [302] /board/?work=parasite
                                          ↓
                                     [GET] 게시판 화면
```

리다이렉트를 안 하면 새로고침할 때마다 글이 또 등록됩니다.
주소창에 남는 건 **GET 주소**이므로, F5를 눌러도 조회만 다시 일어납니다.

---

## 3. 화면 구성

### GET — 조회 (9개)

| 파일 | 주소 | 하는 일 |
|---|---|---|
| `index.php` | `/` | 홈 — 작품 목록 · 인기글 |
| `works.php` | `/works.php?genre=` | 전체 작품 (장르 필터) |
| `search.php` | `/search.php?q=` | 작품 검색 (제목·감독) |
| `board/index.php` | `/board/?work=&q=&sort=&sentiment=&page=` | **★ 게시판 (파라미터 5개)** |
| `post/view.php` | `/post/view.php?id=` | 글 보기 + 댓글 |
| `post/write.php` | `/post/write.php?work=` | 글쓰기 폼 |
| `post/edit.php` | `/post/edit.php?id=` | 글 수정 폼 |
| `profile.php` | `/profile.php?user=` | 프로필 (활동 통계) |
| `auth/login.php`<br>`auth/signup.php` | | 로그인·회원가입 폼 |

### POST — 변경 (11개, 전부 PRG)

| 파일 | 하는 일 |
|---|---|
| `post/create.php` · `update.php` · `delete.php` | 글 작성 · 수정 · 삭제 |
| `comment/create.php` · `delete.php` | 댓글 작성 · 삭제 |
| `like/toggle.php` | 글 추천 (1인 1회, 다시 누르면 취소) |
| `vote/sentiment.php` | 작품 추천/비추천 투표 (1인 1표) |
| `report/create.php` | 신고 (모달 + 사유) |
| `auth/authenticate.php` · `register.php` · `logout.php` | 로그인 · 가입 · 로그아웃 |

---

## 4. 게시판 — GET 파라미터 5개가 겹치는 곳

```
/board/?work=parasite&q=음악&sort=hot&sentiment=호평&page=2
         └작품      └글검색  └정렬   └감상필터      └페이지
```

**처리 순서가 중요합니다.**

```
작품으로 추리기 → 검색어로 추리기 → 감상으로 추리기 → 정렬 → 페이지 자르기
```

다 거르고 난 뒤라야 **총 몇 페이지인지 셀 수 있기** 때문입니다.
(먼저 자르면 "3개 중 2페이지" 같은 엉뚱한 결과가 나옵니다)

### GET 폼의 함정

GET 폼을 제출하면 **주소의 기존 파라미터가 전부 사라지고** 폼 안의 입력칸만 새 주소가 됩니다.
그래서 게시판 검색창에는 작품을 `hidden`으로 같이 실어 보냅니다.

```html
<form method="get" action="/board/">
  <input type="hidden" name="work" value="parasite">  <!-- 이게 없으면 작품이 풀림 -->
  <input type="text" name="q">
</form>
```

정렬·필터 탭은 `query_url()`로 **다른 파라미터를 유지한 채** 하나만 바꿉니다.

---

## 5. 폴더 구조

```
week14/
├── index.php  works.php  search.php  profile.php     [GET]
├── includes/
│   ├── util.php       입력 헬퍼 · e() 이스케이프 · query_url() · 플래시
│   ├── auth.php       로그인·세션·소유권
│   ├── works.php      작품 도메인 (조회·검색·필터·투표)
│   ├── posts.php      글 도메인 (조회·검색·필터·정렬·페이징)
│   ├── comments.php   댓글 도메인
│   └── header.php  footer.php
├── board/index.php                                   [GET] ★핵심
├── post/     view · write · edit [GET] / create · update · delete [POST]
├── comment/  create · delete                         [POST]
├── like/     toggle                                  [POST]
├── vote/     sentiment                               [POST]
├── report/   create                                  [POST]
├── auth/     login · signup [GET] / authenticate · register · logout [POST]
└── assets/   css/style.css  js/main.js
```

**도메인별 파일 분리 이유:** 나중에 MariaDB를 붙일 때 `includes/` 안 함수들의 **속만** SQL로
바꾸면 되고, 화면 파일은 한 줄도 안 고쳐도 됩니다.

---

## 6. 안전장치

| 항목 | 방법 |
|---|---|
| XSS | 모든 출력을 `e()` = `htmlspecialchars()` 통과 |
| 배열 주입 (`?q[]=x`) | `get_str()` / `post_int()` 헬퍼가 타입 확인 후 반환 |
| 잘못된 값 | 정렬·감상·신고사유를 **화이트리스트**로만 허용 |
| 페이지 범위 | `?page=-99`, `?page=99999` → 1~마지막으로 보정 |
| 길이 초과 | 제목 100자 · 내용 5,000자 · 댓글 500자 (서버에서 재확인) |
| 비밀번호 | `password_hash()` 저장 · `password_verify()` 대조 (평문 저장 안 함) |
| 세션 탈취 | 로그인 시 `session_regenerate_id(true)` |
| 계정 열거 | 아이디 오류·비번 오류를 **같은 메시지**로 |
| 권한 | 수정·삭제는 화면에서 숨기고, **서버에서 다시 확인** |
| GET 주소로 삭제 | 액션 파일은 POST가 아니면 즉시 리다이렉트 |

**"화면에서 버튼을 숨기는 것"은 편의일 뿐이고, 진짜 방어는 서버 확인입니다.**
폼을 조작해 `POST id=2`를 직접 보내도 남의 글은 지워지지 않습니다.

---

## 7. 알림(플래시) — 주소를 더럽히지 않는 법

처음엔 `?deleted=1`로 알림을 넘겼는데 세 가지 문제가 있었습니다.

1. 주소가 지저분해짐
2. 새로고침하면 알림이 또 뜸
3. 정렬·필터를 눌러도 `&deleted=1`이 계속 따라다님

그래서 알림을 **세션에 잠깐 맡겼다가, 화면에 꺼내면서 즉시 지웁니다.**

```php
set_flash('🗑 글이 삭제되었습니다.');       // 액션 파일
header('Location: /board/?work=parasite');   // 주소는 깨끗

function take_flash(): ?array {              // header.php에서 호출
    $flash = $_SESSION['flash'];
    unset($_SESSION['flash']);               // ★ 꺼내면서 지운다
    return $flash;
}
```

JS가 3초 뒤 `.fade-out` 클래스를 붙이면 CSS가 부드럽게 사라지게 합니다.
**내용은 서버(PHP), 사라지는 연출은 CSS/JS** — 역할이 나뉘어 있습니다.

---

## 8. 데이터

작품 **10개** (영화 6 · 드라마 4) · 글 **44개** · 댓글 **99개**

같은 감독 작품을 2편씩 넣어 검색이 여러 건을 걸러내는 걸 볼 수 있습니다.

- 봉준호 → 기생충, 살인의 추억
- 크리스토퍼 놀란 → 인터스텔라, 인셉션
- 김원석 → 시그널, 나의 아저씨

**댓글 수는 저장하지 않고 실제 댓글을 세어서 표시합니다.**
숫자를 따로 저장하면 실제 댓글과 어긋나기 때문입니다.

---

## 9. 다음 주차 예정

- MariaDB 연결 + 실제 CRUD (`includes/` 함수 속만 SQL로 교체)
- 데이터 모델: `users` / `works` / `posts` / `comments` / `votes` / `likes` / `reports`
- 이미지 업로드
