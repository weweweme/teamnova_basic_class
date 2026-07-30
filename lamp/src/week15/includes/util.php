<?php
// ============================================================
// util.php — 공통 도구 함수 + 세션 부트스트랩
//   (모든 페이지가 '가장 먼저' require 하는 파일)
// ============================================================

// ── 세션 시작 ────────────────────────────────────────────────
//   세션 = 서버가 '이 브라우저 = 이 사용자'를 기억하는 저장공간.
//     로그인하면 서버 금고에 "이 사람 로그인함"을 적어두고,
//     브라우저에는 그 금고의 '번호표'(쿠키 PHPSESSID)를 발급한다.
//     이후 모든 요청에서 브라우저가 번호표를 내밀면 서버가 알아본다.
//   ★ session_start()는 '어떤 화면 출력보다 먼저' 불러야 한다 (쿠키도 헤더로 나가므로).
//     그래서 모든 페이지가 맨 먼저 require하는 이 파일에 둔다.
//   session_status() 확인 = 이미 시작됐으면 다시 안 함(중복 경고 방지).
if (session_status() === PHP_SESSION_NONE) {
    session_start();
}

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

    // ★ 유지하면 안 되는 것 두 종류를 여기서 뺀다.
    //   "유지할 것(작품·검색·정렬·필터)"과 "빼야 할 것"의 구분이 이 함수의 핵심이다.
    //
    //   ① 알림(FLASH_KEYS) — 한 번 쓰고 버릴 값이다. (FLASH_KEYS는 이 파일 아래 '플래시' 절)
    //      안 빼면 알림이 뜬 상태에서 정렬 탭을 누를 때마다 같은 알림이 다시 뜬다.
    //   ② 신원(IDENTITY_KEY) — 뺀다고 사라지지 않는다. header.php의 URL 리라이터가
    //      출력 직전에 모든 링크에 다시 붙여주기 때문이다.
    //      여기서 안 빼면 ?as=영화광&sort=new&as=영화광 처럼 '두 번' 붙는다(우리 것 + 리라이터 것).
    //      덤: users 표에 없는 엉터리 ?as= 값이 들어와도 링크로 퍼지지 않는다
    //          (리라이터는 진짜 회원일 때만 켜지므로).
    foreach ([...FLASH_KEYS, IDENTITY_KEY] as $key) {
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

// ── 신원 이어붙이기 + 리다이렉트 ─────────────────────────────
//   [왜 필요한가]
//     HTTP는 요청 하나하나가 서로를 기억하지 못한다. PHP 스크립트는 요청 한 번을
//     처리하고 죽으므로, '방금 누가 왔었는지'를 서버가 스스로 기억할 방법이 없다.
//     그래서 "나 영화광이야"를 매 요청마다 다시 실어 보내야 한다.
//     우리는 그것을 '주소에 붙이는 방법'으로 통일한다 → ?as=영화광
//
//   [왜 주소 하나로 통일하나]
//     POST 폼도 action 주소에 ?as=영화광 을 달면 PHP가 $_GET으로 읽어준다.
//     그래서 링크·폼·리다이렉트 세 자리가 모두 같은 규칙 하나로 처리된다.
//     (hidden 필드와 섞으면 "여긴 어느 쪽이었지"를 매번 따져야 해서 실수가 난다)

// 신원을 싣는 파라미터 이름. 주소에 ?as=영화광 으로 나타난다.
//   ★ 읽는 쪽은 auth.php 의 current_user() — 이 이름을 양쪽이 함께 쓴다.
const IDENTITY_KEY = 'as';

// 지금 요청에 실려온 신원을 꺼낸다. 없으면 빈 문자열.
//   ★ 신원을 읽는 곳은 프로젝트 전체에서 이 함수 하나뿐이다.
//     그래서 나머지 코드는 "주소로 왔나 폼으로 왔나"를 신경 쓸 필요가 없다.
//
//   왜 두 군데를 보나:
//     · 주소($_GET)  — 링크를 누르거나 리다이렉트로 넘어올 때
//     · 폼($_POST)   — 폼을 제출할 때. PHP의 URL 리라이터가 폼에는 주소 대신
//                      hidden 필드를 심어주기 때문이다 (header.php에서 켠다).
//                      ※ method="get" 폼은 action 주소의 ?쿼리를 버리고 입력값으로
//                        새로 만들기 때문에, 폼에는 hidden 필드가 정답이다.
function identity_from_request(): string {
    $fromUrl = get_str(IDENTITY_KEY);
    return $fromUrl !== '' ? $fromUrl : post_str(IDENTITY_KEY);
}

// 지금 요청의 신원을, 다음 주소로 그대로 넘기기 위해 배열로 만든다.
//   ★ '로그인 상태'를 묻지 않는다는 점이 중요하다. 그냥 받은 값을 넘겨줄 뿐이라
//     이 파일이 auth.php를 몰라도 된다 (서로 얽히지 않게).
function identity_params(): array {
    $as = identity_from_request();
    return $as === '' ? [] : [IDENTITY_KEY => $as];
}

// 경로 + 파라미터로 완성된 주소를 만든다. 신원은 자동으로 얹힌다.
//   query_url()과 다른 점: query_url은 '지금 주소의 $_GET을 유지'하고,
//   이 함수는 '내가 지정한 것만' 담는다 (리다이렉트는 화면이 바뀌므로 기존 조건을 끌고 갈 이유가 없다).
//
//   예) build_url('/board/', ['work' => 'tmdb-496243'])
//       → /board/?work=tmdb-496243&as=%EC%98%81%ED%99%94%EA%B4%91
function build_url(string $path, array $overrides = []): string {
    // 경로에 이미 ?쿼리가 붙어 있으면 떼어내 배열로 바꾼다.
    //   ('/board/?work=x' 처럼 손으로 붙여둔 기존 코드도 그대로 받아주기 위해)
    $ownParams = [];
    $questionMark = strpos($path, '?');
    if ($questionMark !== false) {
        parse_str(substr($path, $questionMark + 1), $ownParams);
        $path = substr($path, 0, $questionMark);
    }

    // 우선순위: 경로에 붙어있던 값  <  지금 요청의 신원  <  호출자가 지정한 값
    //   → 마지막이 이기므로, 로그인은 ['as'=>$username] 으로 심고
    //     로그아웃은 ['as'=>null] 으로 지울 수 있다.
    $params = array_merge($ownParams, identity_params(), $overrides);

    // 빈 값·null은 주소에서 뺀다 (query_url()과 같은 규칙)
    $params = array_filter($params, fn($v) => $v !== '' && $v !== null);

    // http_build_query가 한글·특수문자를 알아서 인코딩한다 → urlencode() 직접 호출 불필요.
    return $params ? $path . '?' . http_build_query($params) : $path;
}

// PRG의 'R' — 처리 끝나고 GET 화면으로 돌려보낸다. 신원을 자동으로 이어붙인다.
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
//   [지금 방식 — 주소에 싣기]
//     세션을 안 쓰기로 했으니 포스트잇을 붙일 금고가 없다.
//     그래서 신원(?as=)과 똑같이 주소에 실어 다음 화면으로 넘긴다:
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
    'post'    => ['label' => '되돌리기', 'url' => '/post/restore.php',    'fields' => ['id']],
    'comment' => ['label' => '되돌리기', 'url' => '/comment/restore.php', 'fields' => ['comment_id', 'post_id']],
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
