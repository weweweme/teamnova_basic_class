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
//   ※ 알림(flash)은 아직 주소로 나른다 — 다음 단계에서 세션으로 옮긴다 (아래 '플래시' 절).
// ============================================================

// 세션을 켠다. 모든 페이지가 이 파일을 가장 먼저 부르므로, 여기 한 줄이면 전체에 적용된다.
//   ★ 출력이 시작되기 전에 켜져야 해서 파일 맨 위에 둔다.
require_once __DIR__ . '/session.php';

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

    // ★ 유지하면 안 되는 것을 여기서 뺀다.
    //   "유지할 것(작품·검색·정렬·필터)"과 "빼야 할 것"의 구분이 이 함수의 핵심이다.
    //
    //   알림(FLASH_KEYS) — 한 번 쓰고 버릴 값이다. (FLASH_KEYS는 이 파일 아래 '플래시' 절)
    //     안 빼면 알림이 뜬 상태에서 정렬 탭을 누를 때마다 같은 알림이 다시 뜬다.
    //
    //   ※ week15에는 여기서 빼야 할 것이 하나 더 있었다 — 신원(as).
    //     리라이터가 모든 링크에 다시 붙여줘서, 안 빼면 ?as=영화광&sort=new&as=영화광 처럼
    //     두 번 붙었다. 신원이 세션으로 가면서 그 처리가 통째로 필요 없어졌다.
    foreach (FLASH_KEYS as $key) {
        unset($params[$key]);
    }

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
function redirect(string $path, array $overrides = []): never {
    // set_flash()가 남겨둔 알림이 있으면 주소에 함께 실어 보낸다.
    //   ★ 알림을 '어디로 보낼지' 아는 건 set_flash()가 아니라 여기다.
    //     그래서 30곳의 호출부(set_flash 다음 줄에 redirect)는 한 글자도 안 바뀐다.
    //   $overrides를 뒤에 둔 이유: 같은 이름이면 호출자가 지정한 값이 이겨야 하니까.
    $params = array_merge(flash_params(), $overrides);

    // ⚠️ header()는 화면(HTML)이 한 글자라도 출력되기 전에 불러야 한다.
    header('Location: ' . build_url($path, $params));
    exit;
}

// ── 플래시 메시지 ────────────────────────────────────────────
//   '한 번만 보여주고 사라지는 알림' (등록됨 / 삭제됨 / 권한없음 …)
//
//   [예전 방식 — 서버 세션]
//     서버 금고에 포스트잇을 붙여두고 다음 화면이 읽으면서 떼어갔다.
//     주소가 깨끗하고 딱 한 번만 뜨는, 실무 표준 방식이다(Rails·Laravel 등).
//
//   [지금 방식 — 주소에 싣기]  ※ 다음 단계에서 세션으로 되돌린다
//     week15에는 포스트잇을 붙일 금고(세션)가 아예 없어서 주소에 실어 날랐다.
//     이제 금고가 생겼으므로 이 절은 통째로 걷어낼 예정이다 — 아래 '대책'들과 함께.
//     지금은 이렇게 나간다:
//       /board/?work=tmdb-496243&flash=🗑 글이 삭제되었습니다.
//
//   [주소로 나르면 생기는 문제와 대책]
//     ① 주소가 지저분해진다        → 대책 없음. 이 방식의 대가다.
//     ② 새로고침하면 또 뜬다       → 그린 직후 JS가 주소에서 지운다 (main.js).
//     ③ ★ 제일 나쁨: query_url()이 지금 주소의 파라미터를 유지하므로,
//        알림이 뜬 채로 정렬·필터를 누르면 알림이 계속 따라다닌다.
//        → query_url()에서 FLASH_KEYS를 빼서 막는다.
//        ※ ②의 JS로는 ③을 못 막는다. 링크의 href는 서버가 이미 다 그려 보낸 뒤라,
//          주소창만 청소해도 링크 안에 박힌 flash는 그대로 남아 있기 때문이다.

// 알림을 실어 나르는 파라미터 이름들.
//   ★ query_url()이 이 목록을 보고 '링크에는 따라붙지 않게' 걸러낸다.
//     main.js에도 같은 목록이 있다(주소창 청소용) — 한쪽을 고치면 다른 쪽도 함께 고친다.
const FLASH_KEYS = ['flash', 'ftype', 'fundo', 'fid'];

// '되돌리기' 버튼을 붙일 수 있는 곳 — 허용 목록(화이트리스트).
//   ★ 왜 목록으로 못 박나: 이 값이 주소로 들어오기 때문이다.
//     주소는 누구나 고칠 수 있으니, 받은 주소를 그대로 <form action>에 꽂으면
//     "아무 데로나 POST를 쏘는 버튼"이 달린 링크를 남이 만들어 뿌릴 수 있다.
//     → 우리가 미리 적어둔 곳 외에는 버튼을 아예 그리지 않는다.
//   fields = 그 파일이 받아야 하는 값의 '이름과 순서'. 주소엔 fid=12,34 처럼 순서대로 담긴다.
const UNDO_TARGETS = [
    'post' => ['label' => '되돌리기', 'url' => '/post/restore.php', 'fields' => ['id']],
];

// 알림 남기기 (액션 파일이 redirect() 하기 '직전'에 호출)
//   $type    : 'ok' = 성공(초록) / 'error' = 거부·실패(빨강)
//   $undo    : 되돌리기 버튼을 띄우려면 UNDO_TARGETS의 키 ('post' · 'comment')
//   $undoIds : 그 버튼이 보낼 번호들. UNDO_TARGETS의 fields와 '같은 순서'로 넣는다.
//
//   ★ 여기서 주소를 만들지 않는다는 점이 중요하다.
//     '어디로 갈지'는 다음 줄의 redirect()만 알고 있으니, 쪽지만 적어두고 넘긴다.
function set_flash(string $message, string $type = 'ok', string $undo = '', array $undoIds = []): void {
    flash_pending(['message' => $message, 'type' => $type, 'undo' => $undo, 'ids' => $undoIds]);
}

// set_flash()가 적은 쪽지를 redirect()에게 건네주는 작은 보관함.
//   전역변수를 만들지 않으려고 '함수 안의 static'에 담았다
//   (C#의 static 지역변수와 같다. 요청이 끝나면 통째로 사라진다).
//   인자를 주면 쓰고, 안 주면 읽는다.
function flash_pending(?array $flash = null): ?array {
    static $pending = null;
    if ($flash !== null) {
        $pending = $flash;
    }
    return $pending;
}

// 대기 중인 알림을 '주소에 붙일 파라미터'로 바꾼다. (redirect()가 부른다)
//   남긴 알림이 없으면 빈 배열 → 주소에 아무것도 안 붙는다.
function flash_params(): array {
    $flash = flash_pending();
    if ($flash === null) {
        return [];
    }

    $params = ['flash' => $flash['message']];

    // 'ok'는 기본값이라 주소에 안 적는다 (주소를 조금이라도 짧게).
    if ($flash['type'] !== 'ok') {
        $params['ftype'] = $flash['type'];
    }

    // 되돌리기 버튼이 필요할 때만 두 칸을 더 붙인다.
    if (isset(UNDO_TARGETS[$flash['undo']])) {
        $params['fundo'] = $flash['undo'];
        $params['fid']   = implode(',', $flash['ids']);   // [12, 34] → "12,34"
    }

    return $params;
}

// 알림 꺼내기 (header.php가 화면에 그릴 때 호출). 없으면 null.
//   ★ 세션 때와 달리 '꺼내면서 지우기'가 없다. 알림은 주소에 적혀 있고,
//     주소를 지우는 일은 화면을 그린 뒤 브라우저(JS)가 한다.
//   반환 모양은 예전과 똑같은 ['message','type','action'] → header.php는 손댈 게 없다.
function take_flash(): ?array {
    $message = get_str('flash');
    if ($message === '') {
        return null;
    }

    // 색깔은 우리가 아는 두 가지만 인정한다 (주소로 들어온 값이라 그대로 믿지 않는다).
    $type = get_str('ftype') === 'error' ? 'error' : 'ok';

    return [
        'message' => $message,
        'type'    => $type,
        'action'  => create_undo_action(get_str('fundo'), get_str('fid')),
    ];
}

// 주소의 fundo·fid를 '되돌리기 버튼 하나'로 만든다. 허용 목록에 없으면 null(버튼 없음).
//   이름이 create_…인 이유: 매번 새 배열을 만들어 돌려주기 때문.
function create_undo_action(string $undo, string $ids): ?array {
    if (!isset(UNDO_TARGETS[$undo])) {
        return null;
    }
    $target = UNDO_TARGETS[$undo];

    // "12,34" → ['12','34']. 개수가 안 맞으면 조작된 주소이므로 버튼을 그리지 않는다.
    $values = $ids === '' ? [] : explode(',', $ids);
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
