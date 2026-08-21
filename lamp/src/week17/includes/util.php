<?php
// ============================================================
// util.php — 공통 도구 함수
//   (모든 페이지가 '가장 먼저' require 하는 파일)
//
// ★ week16에서 바뀐 것: 신원을 주소가 아니라 '세션'이 들고 있다.
//
//   [week15까지의 방식]
//     세션을 하나도 쓰지 않고, 서버가 기억해야 할 것을 전부 주소에 실어 날랐다.
//       · 누구인지 → ?as=영화광     · 알림 → ?flash=…&ftype=…
//     그런데 주소는 사용자가 고칠 수 있다 → as= 를 남의 아이디로 바꾸면 그 사람이 됐다(사칭).
//     '최근 본 글'처럼 계속 쌓이는 것은 주소가 무한정 길어져서 아예 못 만들었다.
//
//   [지금]
//     신원은 서버 금고(세션)에 있다 → 사용자가 손댈 수 없다. session.php에서 켠다.
//     그래서 이 파일에 있던 '신원을 주소에 이어붙이는' 장치가 통째로 사라졌다:
//       IDENTITY_KEY · identity_from_request() · identity_params()
//       (+ header.php의 URL 리라이터, main.js의 withIdentity)
//     build_url()은 이제 '주소 만들기'만 한다. 원래 함수 이름이 뜻하던 그 일만.
//
//   알림(flash)도 주소를 떠났다. 다만 최종 목적지는 세션이 아니라 쿠키다 (handoff.php).
//   → 주소는 이제 '무엇을 보여줄지'(작품·검색어·정렬·페이지)만 담는다. 원래 주소가 할 일이다.
// ============================================================

// 세션을 켠다. 모든 페이지가 이 파일을 가장 먼저 부르므로, 여기 한 줄이면 전체에 적용된다.
//   ★ 출력이 시작되기 전에 켜져야 해서 파일 맨 위에 둔다.
require_once __DIR__ . '/session.php';

// CSRF(위조 요청) 방어. 모든 폼(csrf_field)과 모든 POST 처리(require_csrf)가 쓰므로
//   여기서 한 번 불러 전체에서 쓸 수 있게 한다. 세션이 있어야 성립하는 방어라 session.php 다음에 둔다.
require_once __DIR__ . '/csrf.php';

// '다음 한 요청'에만 건네는 값들(알림·가려던 곳·폼 입력값)을 쿠키로 나르는 장치.
require_once __DIR__ . '/handoff.php';

// ★ 반드시 화면이 나가기 전에 불러야 한다 — 쿠키를 지우는 것도 응답 헤더이기 때문이다.
//   여기서 '읽고 나면 지운다'를 예약해 두면, 알림을 그리는 header.php 한복판에서는
//   지울 걱정 없이 꺼내 쓰기만 하면 된다.
handoff_boot();

// ── 시간대 ───────────────────────────────────────────────────
//   [문제] 컨테이너(리눅스)와 DB는 UTC로 돌아간다. 한국보다 9시간 느리다.
//     그대로 화면에 찍으면 아침 11시에 쓴 글이 '새벽 2시'로 보인다.
//
//   [해결] 저장은 UTC 그대로 두고, '보여줄 때만' 한국 시각으로 바꾼다.
//     이게 실무 표준이다 — 시차가 다른 곳에서 접속해도 각자의 시간으로 보여줄 수 있고,
//     서머타임·시간대 정책이 바뀌어도 저장된 값은 손댈 필요가 없기 때문이다.
//     (반대로 DB에 한국 시각을 저장해두면, 나중에 다른 나라 사용자가 생겼을 때
//      저장된 값 자체가 '어느 나라 시각인지' 알 수 없는 애물단지가 된다)
//
//   ★ UNIX_TIMESTAMP()로 꺼낸 값은 '시간대가 없는 절대 시각'(1970년부터의 초)이라,
//     여기서 시간대만 정해주면 date()가 알아서 한국 시각으로 그려준다.
date_default_timezone_set('Asia/Seoul');

// ── 입력을 '안전하게' 받는 헬퍼 ──────────────────────────────
//   [문제] $_GET·$_POST의 값이 항상 문자열일 거라 믿으면 안 된다.
//     주소를 이렇게 보내면 값이 '배열'이 되어버린다:  /search/?q[]=x
//     그 상태로 trim($_GET['q']) 를 하면 → 치명적 오류(Fatal error)로 페이지가 통째로 깨진다.
//     (실제로 우리 사이트도 이걸로 터졌었다)
//
//   [해결] 값을 꺼낼 때 '문자열이 맞는지' 먼저 확인하고, 아니면 기본값을 쓴다.
//     is_string() = 이 값이 문자열이냐?  is_scalar() = 숫자·문자열·불린 같은 '단일 값'이냐?
//     → 배열이 오면 조용히 기본값으로 처리하고 넘어간다. (Tester-Doer)
//
//   앞으로 입력은 반드시 이 함수들로 받는다. ($_GET / $_POST 직접 접근 금지)

// 주소(?key=값)에서 문자열 하나 꺼내기
function get_str(string $key, string $default = ''): string {
    $value = $_GET[$key] ?? $default;
    return is_string($value) ? $value : $default;
}

// 주소에서 정수 하나 꺼내기
function get_int(string $key, int $default = 0): int {
    $value = $_GET[$key] ?? null;
    return is_scalar($value) ? (int)$value : $default;
}

// 폼(POST)에서 문자열 하나 꺼내기
function post_str(string $key, string $default = ''): string {
    $value = $_POST[$key] ?? $default;
    return is_string($value) ? $value : $default;
}

// 폼(POST)에서 정수 하나 꺼내기
function post_int(string $key, int $default = 0): int {
    $value = $_POST[$key] ?? null;
    return is_scalar($value) ? (int)$value : $default;
}

// ── 시각 표기 ────────────────────────────────────────────────
//   목록에서는 짧게, 마우스를 올리면 정확하게 — 두 함수를 짝으로 쓴다.

// 목록·글머리에 쓰는 '짧은' 시각.
//   오늘 쓴 글  → 14:32       (오늘 것은 '몇 시에'가 궁금하다)
//   올해 쓴 글  → 07-29       (며칠 전인지가 궁금하다)
//   작년 이전   → 2025-07-29  (연도가 있어야 한다)
//   ★ 이렇게 나누는 이유: 목록은 훑어보는 화면이라 글자가 짧을수록 눈에 잘 들어온다.
function format_time_short(int $timestamp): string {
    $now = time();

    $isToday = date('Y-m-d', $timestamp) === date('Y-m-d', $now);
    if ($isToday) {
        return date('H:i', $timestamp);
    }

    $isThisYear = date('Y', $timestamp) === date('Y', $now);
    return $isThisYear ? date('m-d', $timestamp) : date('Y-m-d', $timestamp);
}

// 정확한 시각. 글 보기 화면과, 목록에서 마우스를 올렸을 때(title) 쓴다.
function format_time_full(int $timestamp): string {
    return date('Y-m-d H:i', $timestamp);
}

// <time datetime="..."> 에 넣을 기계용 표기 (ISO 8601).
//   사람이 읽는 글자와 별개로, 검색엔진·보조기기가 '이게 시각이다'라고 알아보는 형식이다.
function format_time_machine(int $timestamp): string {
    return date('c', $timestamp);
}

// e() : 사용자 입력을 화면에 '안전하게' 출력하는 함수.
//   왜 필요? 사용자가 글에 <script>나쁜코드</script> 를 적으면,
//   그대로 화면에 꽂을 때 브라우저가 진짜 실행해버림(= XSS 공격).
//   htmlspecialchars()가 < > " & 같은 특수문자를 '무해한 글자'로 바꿔줌.
//   → 앞으로 사용자 데이터를 화면에 찍을 땐 무조건 e()로 감싼다.
function e(string $text): string {
    return htmlspecialchars($text, ENT_QUOTES, 'UTF-8');
}

// query_url() : 지금 주소의 GET 파라미터는 '유지'하고, 일부만 바꾼 새 주소를 만든다.
//   왜 필요? 정렬 탭을 눌러도 '작품·필터'가 살아있어야 하고,
//            필터를 눌러도 '정렬'이 살아있어야 하니까.
//            (안 그러면 탭 누를 때마다 다른 조건이 날아감)
//   예) 지금 ?work=parasite&sort=views 인 상태에서
//       query_url('/board/', ['sentiment'=>'호평'])
//       → /board/?work=parasite&sort=views&sentiment=호평
function query_url(string $path, array $overrides = []): string {
    // array_merge : 현재 $_GET 위에 $overrides를 덮어쓴다(같은 키면 새 값이 이김).
    $params = array_merge($_GET, $overrides);

    // ★ week16: 여기 있던 '빼내기' 처리가 통째로 사라졌다.
    //   week15에는 지금 주소에 섞여 있어서 반드시 걸러야 할 것이 둘 있었다:
    //     · 알림(flash·ftype·fundo·fid) — 안 빼면 알림이 뜬 상태에서 정렬 탭을 누를 때마다
    //       같은 알림이 다시 떴다. (알림이 링크를 타고 계속 따라다녔다)
    //     · 신원(as) — 리라이터가 모든 링크에 다시 붙여줘서, 안 빼면
    //       ?as=영화광&sort=new&as=영화광 처럼 두 번 붙었다.
    //   둘 다 세션으로 옮겨가 주소에서 사라졌으므로, 이제 걸러낼 것이 없다.
    //   → 이 함수는 "지금 조건은 유지하고 일부만 바꾼다"는 제 일만 한다.

    // 값이 빈 것('')은 주소에서 아예 빼버린다.
    //   왜 이렇게 하나?
    //   ① 지저분한 빈 파라미터 방지 — '전체' 필터를 고르면 sentiment=''가 되는데,
    //      그대로 두면 /board/?work=parasite&sort=new&sentiment=  처럼 꼬리가 남는다.
    //   ② 상태가 주소에 정직하게 드러남 — "sentiment 항목 자체가 없음" = "필터 안 걸림".
    //      (빈 값으로 남겨두면 '필터를 건 건가 만 건가' 헷갈림)
    //   ③ 같은 화면인데 주소가 여러 개가 되는 걸 막음 —
    //      '?sentiment=' 와 '아무것도 없음'은 결과가 똑같은데 URL은 서로 달라진다.
    //      URL이 갈라지면 공유·북마크·캐시 입장에서 같은 페이지를 다른 것으로 취급해 낭비.
    //   fn($v) => ... 는 '짧은 익명 함수' (Java 람다와 같은 것).
    $params = array_filter($params, fn($v) => $v !== '' && $v !== null);

    // http_build_query : 배열을 'a=1&b=2' 형태 쿼리문자열로 (한글·특수문자 자동 인코딩).
    return $params ? $path . '?' . http_build_query($params) : $path;
}

// ── 주소 만들기 + 리다이렉트 ─────────────────────────────────
//   ★ week15에서 이 자리에 있던 '신원 이어붙이기' 장치가 전부 사라졌다.
//     IDENTITY_KEY · identity_from_request() · identity_params() — 세 개 모두.
//     "지금 누구인가"는 이제 세션이 답하므로(auth.php), 주소는 그 일에서 손을 뗐다.
//
//   [사라진 것이 왜 성과인가]
//     그 장치들은 기능이 아니라 '세션이 없어서 어쩔 수 없이 만든 배관'이었다.
//     링크 30여 곳에 신원을 빠짐없이 붙이려고 URL 리라이터를 켜야 했고,
//     JS가 만든 링크는 리라이터가 못 건드려서 main.js에 같은 일을 하는 함수를
//     하나 더 둬야 했다. 하나라도 빠뜨리면 그 링크를 누르는 순간 로그아웃됐다.
//     → 세션 하나로 이 전부가 필요 없어졌다.

// 경로 + 파라미터로 완성된 주소를 만든다.
//   query_url()과 다른 점: query_url은 '지금 주소의 $_GET을 유지'하고,
//   이 함수는 '내가 지정한 것만' 담는다 (리다이렉트는 화면이 바뀌므로 기존 조건을 끌고 갈 이유가 없다).
//
//   예) build_url('/board/', ['work' => 'tmdb-496243'])
//       → /board/?work=tmdb-496243
function build_url(string $path, array $overrides = []): string {
    // 경로에 이미 ?쿼리가 붙어 있으면 떼어내 배열로 바꾼다.
    //   ('/board/?work=x' 처럼 손으로 붙여둔 기존 코드도 그대로 받아주기 위해)
    $ownParams = [];
    $questionMark = strpos($path, '?');
    if ($questionMark !== false) {
        parse_str(substr($path, $questionMark + 1), $ownParams);
        $path = substr($path, 0, $questionMark);
    }

    // 우선순위: 경로에 붙어있던 값  <  호출자가 지정한 값 (뒤가 이긴다)
    $params = array_merge($ownParams, $overrides);

    // 빈 값·null은 주소에서 뺀다 (query_url()과 같은 규칙)
    $params = array_filter($params, fn($v) => $v !== '' && $v !== null);

    // http_build_query가 한글·특수문자를 알아서 인코딩한다 → urlencode() 직접 호출 불필요.
    return $params ? $path . '?' . http_build_query($params) : $path;
}

// PRG의 'R' — 처리 끝나고 GET 화면으로 돌려보낸다.
//   반환형 never = '이 함수는 절대 되돌아오지 않는다'(exit로 끝나므로).
//     PHP가 이걸 알면, 호출한 뒤에 exit를 또 쓰지 않아도 뒷줄이 실행될 걱정이 없다.
//   ★ week16: 알림을 주소에 실어 보내던 처리가 사라졌다.
//     알림은 이제 쿠키가 나르므로 리다이렉트는 '주소만' 만들면 된다.
//     그런데도 30여 곳의 호출부(set_flash 다음 줄에 redirect)는 한 글자도 안 바뀌었다 —
//     알림을 어떻게 나르는지는 원래 이 두 함수 안에만 있던 사정이기 때문이다.
function redirect(string $path, array $overrides = []): never {
    // ⚠️ header()는 화면(HTML)이 한 글자라도 출력되기 전에 불러야 한다.
    header('Location: ' . build_url($path, $overrides));
    exit;
}

// ── 플래시 메시지 ────────────────────────────────────────────
//   '한 번만 보여주고 사라지는 알림' (등록됨 / 삭제됨 / 권한없음 …)
//
//   [week15 — 주소에 싣기]
//     포스트잇을 붙일 금고(세션)가 없어서 알림을 주소에 실어 날랐다:
//       /board/?work=tmdb-496243&flash=🗑 글이 삭제되었습니다.&ftype=error
//     그 대가로 문제 셋이 생겼고, 그중 둘은 '대책'을 따로 만들어 막아야 했다:
//       ① 주소가 지저분해진다   → 대책 없음. 이 방식의 대가였다.
//       ② 새로고침하면 또 뜬다   → 그린 직후 JS가 주소에서 지우게 했다 (main.js).
//       ③ 제일 나빴던 것: query_url()이 지금 주소의 파라미터를 유지하므로,
//          알림이 뜬 채로 정렬·필터를 누르면 알림이 계속 따라다녔다.
//          → query_url()에서 FLASH_KEYS를 빼서 막았다.
//          (②의 JS로는 ③을 못 막는다. 링크의 href는 서버가 이미 그려 보낸 뒤라
//           주소창만 청소해도 링크 안에 박힌 flash는 그대로 남기 때문)
//
//   [week16 — 세션에 넣기]
//     서버 금고에 포스트잇을 붙여두고, 다음 화면이 **읽으면서 떼어간다**(read-once).
//     ★ 세 문제가 한꺼번에 사라진다:
//       ① 주소에 아무것도 안 붙으니 지저분해질 일이 없다.
//       ② 읽는 순간 지워지니 새로고침해도 다시 안 뜬다 — JS 주소청소가 필요 없다.
//       ③ 애초에 링크에 실리지 않으니 따라다닐 수가 없다 — FLASH_KEYS가 필요 없다.
//     → 파라미터 4개(flash·ftype·fundo·fid)와 대책 2개가 전부 지워졌다.
//     Rails·Laravel 같은 실무 프레임워크가 쓰는 방식이 바로 이것이다.

//   [week16 후반 — 세션에서 쿠키로 다시 옮기기]
//     세션으로 옮기고 나서 기준을 다시 대봤다: "사용자가 고쳐서 이득을 보나?"
//     알림은 **아니다.** 문구를 고쳐봐야 자기 화면에 자기가 쓴 글자가 뜰 뿐이다.
//     주소로 나르던 시절이 위험했던 건 **링크를 남에게 뿌릴 수 있어서**였는데,
//     쿠키는 남이 못 심는다 → 그 위험이 통째로 없다.
//     → 세션은 '고치면 진짜 손해가 나는 값'만 담는 금고로 남긴다. (handoff.php)
//     ★ 대신 '읽을 때도 검사'가 새로 필요해졌다. 아래 take_flash가 그 일을 한다.

// 알림 문구 길이 한도. 쿠키는 4KB뿐이라 담기 전에 우리가 먼저 자른다.
const FLASH_MAX_LEN = 200;

// '되돌리기' 버튼을 붙일 수 있는 곳 — 이름 → 버튼 정보 표.
//   fields = 그 파일이 받아야 하는 값의 '이름과 순서'. set_flash의 $undoIds와 순서를 맞춘다.
//   ※ week15에는 이 표가 '보안 장치'이기도 했다. undo 값이 주소(?fundo=)로 들어와서,
//     허용 목록 없이 <form action>에 꽂으면 "아무 데로나 POST를 쏘는 버튼"이 달린 링크를
//     남이 만들어 뿌릴 수 있었다. 이제 이 값은 우리 코드가 세션에 직접 넣으므로
//     그 위험은 사라졌고, 표는 '한곳에서 관리하는 설정'으로만 남는다.
const UNDO_TARGETS = [
    'post' => ['label' => '되돌리기', 'url' => '/post/restore.php', 'fields' => ['id']],
];

// 알림 남기기 (액션 파일이 redirect() 하기 '직전'에 호출)
//   $type    : 'ok' = 성공(초록) / 'error' = 거부·실패(빨강)
//   $undo    : 되돌리기 버튼을 띄우려면 UNDO_TARGETS의 키 ('post')
//   $undoIds : 그 버튼이 보낼 번호들. UNDO_TARGETS의 fields와 '같은 순서'로 넣는다.
//
//   ★ 쿠키에 바로 적는다. week15에서는 '요청이 끝나면 사라지는 static 보관함'에 적어두고
//     redirect()가 주소로 옮겨 실었는데, 그 중계가 통째로 필요 없어졌다.
//     (그래서 flash_pending()·flash_params() 두 함수가 사라졌다)
//   ★ 호출부 30여 곳은 세션→쿠키로 옮기면서도 한 글자도 안 바뀌었다.
//     어디에 담는지는 원래 이 두 함수 안에만 있던 사정이기 때문이다.
function set_flash(string $message, string $type = 'ok', string $undo = '', array $undoIds = []): void {
    handoff_put(HANDOFF_FLASH, [
        'message' => mb_substr($message, 0, FLASH_MAX_LEN),
        'type'    => $type,
        'undo'    => $undo,
        'ids'     => $undoIds,
    ]);
}

// 알림 꺼내기 (header.php가 화면에 그릴 때 호출). 없으면 null.
//   ★★ 이름이 take_(가져가다)인 이유가 이제야 맞아떨어진다 — 꺼내면서 지운다.
//     이 '읽으면 사라진다'가 플래시의 핵심이다. 지우지 않으면 다음 화면에도,
//     그 다음 화면에도 계속 따라다닌다.
//   반환 모양은 week15와 똑같은 ['message','type','action'] → header.php는 손댈 게 없다.
//
//   ★★ 여기가 '쿠키로 옮긴 대가'를 치르는 자리다.
//     세션에 있을 땐 우리가 넣은 값이 그대로 나왔으므로 그냥 써도 됐다.
//     쿠키는 사용자가 고칠 수 있으므로, 꺼낸 조각을 **하나씩 다시 검사**한다.
//     그래도 검사가 간단한 이유는, 이 값들이 원래부터 '아는 것 중 하나'이기 때문이다:
//       type은 둘 중 하나 · undo는 표에 있는 이름 · ids는 숫자.
//     → 아는 것과 대조해서 아니면 버린다. 이게 사용자 입력을 다루는 기본 자세다.
function take_flash(): ?array {
    $flash = handoff_take(HANDOFF_FLASH);
    if ($flash === null) {
        return null;
    }

    // 색깔은 우리가 아는 두 가지만 인정한다. (모르는 값이면 무조건 'ok')
    //   안 걸러내면 사용자가 넣은 글자가 그대로 CSS 클래스 자리에 박힌다.
    $type = ($flash['type'] ?? null) === 'error' ? 'error' : 'ok';

    // 되돌리기 번호는 숫자만 남긴다. 배열이 중첩돼 들어와도 여기서 걸러진다.
    //   ★ 고쳐봐야 소용없다 — restore.php가 '내 글인지'를 서버에서 다시 확인한다(is_owner).
    //     즉 이 검사는 보안이 아니라 '화면이 안 깨지게' 하는 것이다. 진짜 방어는 저쪽에 있다.
    //   array_values = 걸러내고 남은 것의 번호를 0,1,2…로 다시 매긴다.
    //     (안 하면 중간이 빠졌을 때 $values[0] 이 없어서 create_undo_action이 어긋난다)
    $ids = array_values(array_map('intval', array_filter((array) ($flash['ids'] ?? []), 'is_scalar')));

    return [
        'message' => handoff_str($flash['message'] ?? null, FLASH_MAX_LEN),
        'type'    => $type,
        'action'  => create_undo_action(handoff_str($flash['undo'] ?? null, 20), $ids),
    ];
}

// ── 입력값 되살리기 (old input) ──────────────────────────────
//   [무엇을 고치는 문제인가]
//     글을 길게 쓰다가 제목 길이 제한에 걸리면, 폼으로 되돌아왔을 때 **쓴 내용이 통째로 날아갔다.**
//     액션 파일은 처리만 하고 redirect로 화면을 바꾸는데(PRG), 그 사이에 값을 들고 갈
//     방법이 없었기 때문이다.
//
//   [왜 주소가 아닌가]
//     주소에 실으면 브라우저 기록·서버 로그에 사용자가 친 값이 통째로 남는다.
//     잠깐 맡아뒀다 다음 화면에서 꺼내 쓰고 지우는 것 — **플래시와 똑같은 구조**다.
//
//   [★ 왜 쿠키인가 — 그리고 왜 긴 글에는 안 쓰는가]
//     이 값은 **짧은 폼 전용**이다. 지금 쓰는 곳은 회원가입·로그인의 '아이디' 한 칸뿐이다.
//     글쓰기처럼 긴 본문은 여기 담지 않는다:
//       ① 쿠키는 4KB뿐인데 본문은 5000자까지 허용된다 → 넘치면 **조용히 잘린다.**
//       ② 긴 글은 애초에 '다음 한 요청'이 아니라 **며칠 뒤에도** 살아 있어야 한다.
//          → 그건 임시저장(초안)의 일이고, drafts 표에 따로 산다. (drafts.php)
//     ★ 같은 '입력값 되살리기'인데 **얼마나 오래 살아야 하는가**로 그릇이 갈린다.
//
//   ★ 비밀번호는 어느 쪽에도 절대 담지 않는다. 쿠키는 브라우저에 평문으로 남는다.
//
//   [Laravel의 old()와 같은 것]
//     실무 프레임워크에는 이 기능이 기본으로 들어 있다. 이름도 old()다.

// 한 칸의 길이 한도. 아이디 정도만 담으므로 넉넉히 이 정도면 충분하다.
const OLD_INPUT_MAX_LEN = 100;

// 방금 보낸 값을 다음 화면까지 맡아둔다. (액션 파일이 redirect 하기 직전에 호출)
//   ★ 비밀번호는 절대 넘기지 않는다 — 호출하는 쪽에서 빼고 넘긴다.
//     쿠키는 브라우저 안에 평문으로 남으므로, 담는 순간 F12에서 그대로 보인다.
//
//   ★★ 깨진 글자를 걸러낸다.
//     여기는 **사용자가 친 값이 우리 보관 장치에 처음 들어오는 자리**다.
//     (그 전까지 담긴 건 회원 번호·토큰 같은 '우리가 만든 값'뿐이었다)
//     정상 브라우저는 UTF-8로 보내지만, 요청은 직접 만들어 보낼 수도 있다.
//     성한 글자만 남겨 두면 다음 화면에서 그리다 깨질 일이 없다.
function keep_old_input(array $values): void {
    $clean = [];
    foreach ($values as $key => $value) {
        $clean[(string) $key] = handoff_str($value, OLD_INPUT_MAX_LEN);
    }
    handoff_put(HANDOFF_OLD_INPUT, $clean);
}

// 맡아둔 값을 버린다. (처리에 성공해서 폼으로 돌아갈 일이 없어졌을 때 호출)
//   ★ 이게 없으면 성공한 값이 쿠키에 남아 다음에 폼을 열 때 되살아난다.
//     '읽으면 지워진다'는 old()를 **부르는 화면**에만 해당한다 — 성공 후 가는 목록 화면은
//     old()를 부르지 않으므로 아무도 안 치운다. 그래서 성공한 쪽에서 직접 버려야 한다.
function forget_old_input(): void {
    handoff_drop(HANDOFF_OLD_INPUT);
}

// 맡아둔 값을 꺼낸다. 없으면 $default. (폼 화면이 value= 자리에서 호출)
//   ★ static을 쓰는 이유: 폼에는 칸이 여러 개라 이 함수가 한 화면에서 여러 번 불린다.
//     handoff_take는 '꺼내면서 지우는' 함수라 두 번째 호출부터는 null이 온다.
//     → 요청 안에서 '처음 한 번'만 꺼내 두고, 나머지 칸은 그 사본에서 읽는다.
//     (static 지역변수는 요청이 끝나면 사라진다 — 딱 한 요청만 살면 되는 이 용도에 맞다)
//   ★ 값은 쿠키에서 왔으므로 여기서도 handoff_str로 한 번 더 거른다.
//     화면에 그리기 직전이라, 이상한 값이 통과하면 곧바로 눈에 보이는 자리다.
function old(string $key, string $default = ''): string {
    static $input = null;

    if ($input === null) {
        $input = handoff_take(HANDOFF_OLD_INPUT) ?? [];
    }
    if (!isset($input[$key])) {
        return $default;
    }

    return handoff_str($input[$key], OLD_INPUT_MAX_LEN);
}

// 세션에 담긴 undo·ids를 '되돌리기 버튼 하나'로 만든다. 표에 없는 이름이면 null(버튼 없음).
//   이름이 create_…인 이유: 매번 새 배열을 만들어 돌려주기 때문.
//   ★ week15에는 "12,34" 같은 문자열을 주소에서 받아 쪼개야 했다.
//     세션은 배열을 배열 그대로 보관하므로 그 변환이 통째로 없어졌다.
function create_undo_action(string $undo, array $values): ?array {
    if (!isset(UNDO_TARGETS[$undo])) {
        return null;
    }
    $target = UNDO_TARGETS[$undo];

    // 개수가 안 맞으면 set_flash 호출부와 UNDO_TARGETS의 정의가 어긋난 것이다 → 버튼을 그리지 않는다.
    if (count($values) !== count($target['fields'])) {
        return null;
    }

    // 이름(fields)과 값(values)을 순서대로 짝지어 hidden 필드용 배열을 만든다.
    //   (int) 로 바꾸는 이유: restore 파일이 받는 건 글·댓글 '번호'뿐이라,
    //   숫자가 아닌 값이 섞여 들어올 여지를 여기서 잘라낸다.
    $fields = [];
    foreach ($target['fields'] as $index => $name) {
        $fields[$name] = (int) $values[$index];
    }

    return ['label' => $target['label'], 'url' => $target['url'], 'fields' => $fields];
}

// ── 검색어 강조 ──────────────────────────────────────────────
//   검색 결과에서 찾은 글자에 <mark>(형광펜) 을 씌운 HTML을 만든다.
//
//   ★★ 순서가 생명이다 (여기서 실수하면 보안 구멍이 뚫린다)
//      ① 먼저 e() 로 안전하게 만들고  ② 그 '뒤에' <mark> 태그를 넣는다.
//
//      만약 반대로 하면?
//        · <mark>를 먼저 넣고 e() 하면 → <mark>까지 글자로 변해서 화면에 태그가 그대로 보인다.
//        · e()를 아예 안 하면 → 사용자가 넣은 <script>가 진짜 코드로 실행된다 (XSS).
//
//   ※ 이 함수의 결과는 '이미 안전 처리된 HTML'이므로,
//      화면에서는 e() 를 한 번 더 씌우지 않고 그대로 출력한다.
//      이름을 create_… 로 시작하는 이유: 새 문자열을 만들어 돌려주기 때문.
function create_highlighted(string $text, string $query): string {
    $safeText = e($text);

    if ($query === '') {
        return $safeText;               // 검색어가 없으면 강조할 것도 없다
    }
    $safeQuery = e($query);

    // preg_quote = 검색어에 정규식 기호(. * ? + 등)가 섞여 있어도
    //   '특별한 의미'가 아니라 '그냥 글자'로 찾도록 막아준다.
    //   (안 하면 검색어 '.' 하나가 '아무 글자나'로 해석돼 전부 강조된다)
    $pattern = '/' . preg_quote($safeQuery, '/') . '/iu';
    //           i = 대소문자 구분 안 함 / u = 한글 같은 여러 바이트 글자를 제대로 처리

    // $0 = 방금 찾아낸 그 글자 자체를 가리킨다.
    return preg_replace($pattern, '<mark>$0</mark>', $safeText);
}
