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
const PREF_SORT_COOKIE     = 'pref_sort';     // 게시판 정렬 기본값
const RECENT_SEARCH_COOKIE = 'recent_search'; // 최근 검색어 (JSON 배열)
const RECENT_POSTS_COOKIE  = 'recent_posts';  // 최근 본 글 번호 ("3,2,1")
const PREF_SENTIMENT_COOKIE = 'pref_sentiment'; // 게시판 감상 필터 기본값 ('' | 호평 | 보통 | 혹평)

// 최근 검색어를 몇 개까지 기억할지
const RECENT_SEARCH_MAX = 5;

// 최근 본 글을 몇 개까지 기억할지 (홈 사이드바에 보여줄 만큼만)
const RECENT_POSTS_MAX = 5;

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

// ── 게시판 감상 필터 기본값 ──────────────────────────────────
//   정렬(pref_sort)과 완전히 같은 자리다 — "나는 혹평만 골라 본다"도 취향이기 때문.
//   ★ 그래서 새 규칙을 만들지 않고 같은 함수 모양을 그대로 따른다.
//     같은 성격의 값이 서로 다른 방식으로 저장되면, 나중에 한쪽만 고치는 실수가 난다.

// 고른 감상 필터를 기억한다. ('전체'도 기억한다 — 빈 문자열이 곧 '전체'라는 선택이다)
function remember_sentiment(string $sentiment): void {
    setcookie(PREF_SENTIMENT_COOKIE, $sentiment, pref_cookie_options());
}

// 기억해 둔 감상 필터를 꺼낸다. 허용 목록에 없으면 $default.
//   ★★ 쿠키에서 읽은 값은 주소로 들어온 값과 똑같이 취급한다 — 반드시 허용 목록과 대조한다.
//     그냥 쓰면 ?sentiment= 자리에 아무 문자열이나 흘러들어가고, 그 값이 화면·쿼리로 퍼진다.
function preferred_sentiment(array $allowed, string $default): string {
    $saved = $_COOKIE[PREF_SENTIMENT_COOKIE] ?? '';
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


// ── 최근 본 글 ───────────────────────────────────────────────
//   [★ 왜 세션이 아니라 쿠키인가 — 판단을 한 번 바꾼 자리다]
//     처음에는 세션에 담았다. 그런데 우리가 세운 3조건에 비춰보니 셋 다 쿠키였다:
//       ① 브라우저를 닫아도 남아야 하나 → 그렇다. 세션에 두면 창을 껐다 켤 때마다 목록이 빈다
//       ② 로그인 안 한 사람도 쓰나       → 그렇다
//       ③ 조작당해도 손해가 없나         → 그렇다. 남의 글 번호를 넣어도 자기 화면에 그 글이 뜰 뿐
//     크기도 문제가 안 된다 — 번호 다섯 개면 "3,2,1,9,12" 남짓이라 쿠키 4KB에 한참 못 미친다.
//
//   [담는 모양]
//     최근 검색어는 한글이 섞여 JSON으로 담지만, 여기는 숫자뿐이라 콤마로 잇는다.
//     week15에 주소로 나르려다 포기했던 그 모양(?recent=1,5,9)과 같다 —
//     다만 이번엔 주소가 아니라 브라우저가 들고 다닌다.
//
//   [★★ 쿠키에서 읽은 값은 주소로 들어온 값과 똑같이 검증한다]
//     정수인지 · 양수인지 · 개수는 몇 개인지 전부 확인한다. (get_recent_post_ids)
//     '글이 실제로 있는지'는 여기서 안 본다 — 그건 글을 꺼내는 쪽(posts.php)이 하고,
//     지워진 글은 자연히 목록에서 빠진다.

// 이 글을 '방금 봤다'고 기록한다. (post/view.php가 부른다)
function remember_recent_post(int $id): void {
    if ($id <= 0) {
        return;
    }

    $recent = get_recent_post_ids();

    // 이미 있으면 빼고 맨 앞에 다시 넣는다 → 같은 글이 여러 줄로 쌓이지 않고 '가장 최근'이 된다.
    $recent = array_values(array_filter($recent, fn($seen) => $seen !== $id));
    array_unshift($recent, $id);
    $recent = array_slice($recent, 0, RECENT_POSTS_MAX);

    setcookie(RECENT_POSTS_COOKIE, implode(',', $recent), pref_cookie_options());
}

// 최근 본 글 번호 목록. 이상한 값은 전부 걸러낸다.
function get_recent_post_ids(): array {
    $raw = $_COOKIE[RECENT_POSTS_COOKIE] ?? '';
    if (!is_string($raw) || $raw === '') {
        return [];
    }

    $result = [];
    foreach (explode(',', $raw) as $piece) {
        // ctype_digit = '숫자로만 이뤄진 글자인가'. (int) 로 먼저 바꾸면 "3abc"가 3이 되어 통과한다.
        if (!ctype_digit($piece)) {
            continue;
        }
        $id = (int) $piece;
        if ($id <= 0 || in_array($id, $result, true)) {
            continue;                       // 0·중복은 버린다
        }
        $result[] = $id;
        if (count($result) >= RECENT_POSTS_MAX) {
            break;                          // 개수도 우리가 정한 만큼만
        }
    }
    return $result;
}
