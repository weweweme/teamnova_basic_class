<?php
// ============================================================
// prefs.php — '취향' 쿠키
//
//   [무엇을 담나]
//     세션에 담은 것들(로그인·알림·최근 본 글·조회수)은 전부 **틀리면 손해 보는 값**이었다.
//     여기 담는 건 반대다 — 틀려도 사용자가 다시 한 번 누르면 그만인 값:
//       · 게시판 정렬 기본값 (최신/인기/조회/댓글 중 뭘 즐겨 보나)
//       · 최근 검색어
//
//   [★ 왜 이건 쿠키가 맞나]
//     ① 브라우저를 닫아도 남아야 한다. 세션은 닫으면 사라지므로 애초에 못 한다.
//     ② 로그인 안 한 사람도 써야 한다. DB에 넣으면 회원만 쓰는 기능이 된다.
//     ③ 조작당해도 손해가 없다. 남이 내 정렬 기본값을 바꿔봐야 "탭이 하나 다르게 열린다"뿐이다.
//     세 조건이 다 맞을 때가 쿠키 자리다.
//
//   [★★ 쿠키를 읽을 때의 철칙]
//     쿠키는 사용자 PC에 있다 = **사용자가 마음대로 고칠 수 있다.**
//     그래서 읽은 값을 절대 그대로 믿지 않는다 — 우리가 아는 값인지 확인하고 쓴다.
//     세션과 결정적으로 다른 점이고, week15의 ?as= 가 뚫린 이유와 정확히 같은 이야기다.
//     (여기서는 '허용 목록에 있는 값인가' · '글자 수가 정상인가'를 확인한다)
// ============================================================

require_once __DIR__ . '/util.php';    // e() · get_str
require_once __DIR__ . '/posts.php';   // SEARCH_QUERY_MAX — 검색어 길이 제한은 검색 기능이 정한 값이라
                                       //   여기서 따로 정하지 않고 그쪽을 따른다 (숫자가 둘로 갈라지지 않게)

// 취향은 오래 기억해도 손해가 없으므로 넉넉히 잡는다.
const PREF_DAYS = 90;

// 쿠키 이름들
const PREF_SORT_COOKIE   = 'pref_sort';       // 게시판 정렬 기본값
const RECENT_SEARCH_COOKIE = 'recent_search'; // 최근 검색어 (JSON 배열)

// 최근 검색어를 몇 개까지 기억할지
const RECENT_SEARCH_MAX = 5;

// 취향 쿠키에 공통으로 붙일 옵션.
//   ★ httponly를 켜지 '않는' 유일한 자리다 — 나중에 JS가 읽어 쓸 수도 있는 값이고,
//     훔쳐가 봐야 '이 사람은 조회순을 좋아한다' 정도라 가릴 이유가 없기 때문이다.
//     (로그인 토큰은 반대다 — remember.php에서는 반드시 httponly를 켠다)
function pref_cookie_options(): array {
    return [
        'expires'  => time() + PREF_DAYS * 86400,
        'path'     => '/',
        'samesite' => 'Lax',
        'secure'   => !empty($_SERVER['HTTPS']),
    ];
}

// ── 게시판 정렬 기본값 ───────────────────────────────────────

// 방금 고른 정렬을 기억한다. (게시판이 ?sort= 를 받았을 때 호출)
function remember_sort(string $sort): void {
    setcookie(PREF_SORT_COOKIE, $sort, pref_cookie_options());
}

// 기억해 둔 정렬을 꺼낸다. 없거나 이상한 값이면 $default.
//   ★★ $allowed(허용 목록)를 반드시 받는다. 쿠키 값을 그대로 쓰면
//     ?sort= 자리에 아무 문자열이나 흘러들어가고, 그 값이 화면·쿼리로 퍼진다.
//     "쿠키는 입력이다" — 주소로 들어온 값과 똑같이 취급해야 한다.
function preferred_sort(array $allowed, string $default): string {
    $saved = $_COOKIE[PREF_SORT_COOKIE] ?? '';
    if (!is_string($saved) || !in_array($saved, $allowed, true)) {
        return $default;
    }
    return $saved;
}

// ── 최근 검색어 ──────────────────────────────────────────────

// 방금 검색한 말을 목록 맨 앞에 넣는다.
//   '최근 본 글'(세션)과 규칙이 같다 — 있으면 빼고, 앞에 넣고, 넘치면 자른다.
function remember_search(string $query): void {
    $recent = get_recent_searches();

    // 같은 말을 또 검색하면 줄이 늘지 않고 맨 앞으로 올라온다.
    $recent = array_values(array_filter($recent, fn($old) => $old !== $query));
    array_unshift($recent, $query);
    $recent = array_slice($recent, 0, RECENT_SEARCH_MAX);

    // 여러 개를 한 칸에 담아야 하므로 JSON 한 줄로 바꾼다.
    //   JSON_UNESCAPED_UNICODE = 한글을 \uXXXX로 바꾸지 않는다(쿠키가 쓸데없이 길어지지 않게).
    setcookie(RECENT_SEARCH_COOKIE, json_encode($recent, JSON_UNESCAPED_UNICODE), pref_cookie_options());
}

// 최근 검색어 목록. 쿠키가 없거나 이상하면 빈 배열.
//   ★★ 여기가 이 파일에서 제일 조심해야 하는 곳이다.
//     쿠키 값은 사용자가 통째로 바꿔 넣을 수 있다 → JSON이 아닐 수도, 배열이 아닐 수도,
//     안에 배열이나 숫자가 들어 있을 수도, 글자가 10만 자일 수도 있다.
//     그대로 화면에 뿌리면 깨지거나(치명적 오류) 이상한 게 섞여 들어간다.
//     → '문자열인 것만' '길이를 잘라서' '개수를 제한해' 받는다.
function get_recent_searches(): array {
    $raw = $_COOKIE[RECENT_SEARCH_COOKIE] ?? '';
    if (!is_string($raw) || $raw === '') {
        return [];
    }

    // true = 객체가 아니라 배열로 받는다. 깨진 JSON이면 null이 온다.
    $list = json_decode($raw, true);
    if (!is_array($list)) {
        return [];
    }

    $result = [];
    foreach ($list as $item) {
        if (!is_string($item) || $item === '') {
            continue;                                   // 문자열이 아닌 건 버린다
        }
        $result[] = mb_substr($item, 0, SEARCH_QUERY_MAX);   // 검색창과 같은 길이 제한
        if (count($result) >= RECENT_SEARCH_MAX) {
            break;                                      // 개수도 우리가 정한 만큼만
        }
    }
    return $result;
}
