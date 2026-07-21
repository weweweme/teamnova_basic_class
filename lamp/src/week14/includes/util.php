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
//     주소를 이렇게 보내면 값이 '배열'이 되어버린다:  /search.php?q[]=x
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
