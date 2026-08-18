<?php
// ============================================================
// prefs.php — '취향' 쿠키
//
//   [무엇을 담나]
//     세션에 남긴 것들(로그인·CSRF·조회기록·재인증 시각·유휴 시각)은 전부 **틀리면 손해 보는 값**이다.
//     반대로 알림·가려던 곳·폼 입력값은 고쳐봐야 자기 손해뿐이라 쿠키로 내보냈다 (handoff.php).
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

// ── 쿠키 수명 — 근거를 대고 정한다 ──────────────────────────
//   [★ 왜 '넉넉히'가 아니라 '근거'인가]
//     처음엔 *"취향은 오래 기억해도 손해가 없으니 넉넉히"* 로 90일을 줬다.
//     그런데 **동의 없이 심는 쿠키는 얼마나 살아도 되는지가 정해져 있다.**
//
//   📄 Article 29 WP, Opinion 04/2012 on Cookie Consent Exemption (WP194)
//      https://ec.europa.eu/justice/article-29/documentation/opinion-recommendation/files/2012/wp194_en.pdf
//      · 인증 쿠키              → **세션**            (우리: PHPSESSID ✅)
//      · 사용자 입력 쿠키        → 세션, 길어야 몇 시간 (우리: flash·intended·old_input ✅)
//      · 보안 쿠키              → **제한된 기간**      (우리: device)
//      · **화면 설정 쿠키**      → **세션 또는 약간 더** (우리: pref_* · per_page)
//      · 원칙 — *"면제 쿠키의 수명은 그 목적에 직접 관계된 기간이어야 하고,
//                필요 없어지면 만료돼야 한다"*
//
//   ★ 그래서 90일은 근거가 없었다. **"손해가 없다"는 우리 사정이지 기준이 아니다.**

// 화면 설정(정렬·감상 필터·글 수). 문서가 말하는 "세션 또는 약간 더"를 **하루**로 잡았다.
//   ★ 세션으로 두면 창을 닫을 때마다 초기화돼 기억하는 의미가 없고,
//     하루면 "오늘 보던 대로"는 유지되면서 문서의 범위 안에 든다.
const PREF_DAYS = 1;

// 동의를 받고 심는 선택 항목(최근 본 글·작품·검색어·마지막 방문).
//   ★ 이쪽은 **면제 대상이 아니라 동의를 받은 것**이라 위 기간 제한을 안 받는다.
//     그래도 90일은 길다 — **필요한 만큼만 갖는다**는 원칙은 동의 여부와 무관하다.
const OPTIONAL_DAYS = 30;

// 어느 쿠키를 심어도 되는지 판단하려면 '무엇에 동의했나'를 알아야 한다.
//   ★ 한 방향으로만 안다 — consent.php는 쿠키 이름을 하나도 모른다.
require_once __DIR__ . '/consent.php';
require_once __DIR__ . '/view_id.php';   // VIEW_ID_COOKIE — 선택 항목 목록에 넣으려고

// 쿠키 이름들
const PREF_SORT_COOKIE     = 'pref_sort';     // 게시판 정렬 기본값
const RECENT_SEARCH_COOKIE = 'recent_search'; // 최근 검색어 (JSON 배열)
const RECENT_POSTS_COOKIE  = 'recent_posts';  // 최근 본 글 번호 ("3,2,1")
const PREF_SENTIMENT_COOKIE = 'pref_sentiment'; // 게시판 감상 필터 기본값 ('' | 호평 | 보통 | 혹평)
const LAST_VISIT_COOKIE     = 'last_visit';     // 마지막으로 게시판을 본 시각(초)
const RECENT_WORKS_COOKIE   = 'recent_works';   // 최근 본 작품 slug ("tmdb-496243,tmdb-27205")
const PER_PAGE_COOKIE       = 'per_page';       // 게시판 한 페이지 글 수 (15 | 30 | 50)

// 최근 검색어를 몇 개까지 기억할지
const RECENT_SEARCH_MAX = 5;

// 최근 본 글을 몇 개까지 기억할지 (홈 사이드바에 보여줄 만큼만)
const RECENT_POSTS_MAX = 5;

// 취향 쿠키에 공통으로 붙일 옵션.
//   ★ httponly를 켜지 '않는' 유일한 자리다 — 나중에 JS가 읽어 쓸 수도 있는 값이고,
//     훔쳐가 봐야 '이 사람은 조회순을 좋아한다' 정도라 가릴 이유가 없기 때문이다.
//     (세션 번호표는 반대다 — session.php에서 반드시 httponly를 켠다)
// 선택 항목(동의받고 심는 것) 전용 — 수명만 다르고 나머지는 같다.
//   ★ 함수를 나눈 이유: 같은 함수에 인자를 받게 하면 **부르는 쪽이 기간을 정하게** 된다.
//     그러면 언젠가 한 곳이 다른 숫자를 쓴다. 기간은 '그 쿠키의 성격'이 정해야 한다.
function optional_cookie_options(): array {
    $options = pref_cookie_options();
    $options['expires'] = time() + OPTIONAL_DAYS * 86400;

    return $options;
}

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
    // ★ 동의 게이트. 이 한 줄이 [거절] 버튼을 '진짜'로 만든다.
    //   기록만 하고 계속 심으면 거절 버튼은 장식이다.
    if (!has_consent('search')) {
        return;
    }
    $recent = get_recent_searches();

    // 같은 말을 또 검색하면 줄이 늘지 않고 맨 앞으로 올라온다.
    $recent = array_values(array_filter($recent, fn($old) => $old !== $query));
    array_unshift($recent, $query);
    $recent = array_slice($recent, 0, RECENT_SEARCH_MAX);

    // 여러 개를 한 칸에 담아야 하므로 JSON 한 줄로 바꾼다.
    //   JSON_UNESCAPED_UNICODE = 한글을 \uXXXX로 바꾸지 않는다(쿠키가 쓸데없이 길어지지 않게).
    setcookie(RECENT_SEARCH_COOKIE, json_encode($recent, JSON_UNESCAPED_UNICODE), optional_cookie_options());
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

    if (!has_consent('view')) {
        return;                       // 동의 안 함 → 기록하지 않는다
    }

    $recent = get_recent_post_ids();

    // 이미 있으면 빼고 맨 앞에 다시 넣는다 → 같은 글이 여러 줄로 쌓이지 않고 '가장 최근'이 된다.
    $recent = array_values(array_filter($recent, fn($seen) => $seen !== $id));
    array_unshift($recent, $id);
    $recent = array_slice($recent, 0, RECENT_POSTS_MAX);

    setcookie(RECENT_POSTS_COOKIE, implode(',', $recent), optional_cookie_options());
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


// ── 마지막 방문 시각 → 게시판의 🆕 배지 ──────────────────────
//   [무엇을 하나]
//     "지난번에 왔다 간 뒤로 올라온 글"에 🆕 를 붙인다. 커뮤니티에서 흔히 보는 그것.
//
//   [★ 지금까지의 쿠키와 다른 점]
//     정렬·감상·최근 검색어는 **사용자가 고른 값**이었다. 이건 **시각**이다 —
//     사용자가 고르는 게 아니라 우리가 자동으로 적어둔다.
//     그래도 쿠키가 맞는 자리다: ①브라우저를 닫아도 남아야 하고 ②비로그인도 쓰고
//     ③조작당해도 배지가 잘못 뜰 뿐이다.
//
//   [왜 '볼 때마다' 갱신하지 않나]
//     열자마자 갱신하면 새로고침 한 번에 배지가 전부 사라진다. 방금 뭐가 새 글이었는지
//     확인할 틈이 없다. 그래서 **VISIT_GAP(30분) 이상 지났을 때만** 갱신한다
//     → 한 번 둘러보는 동안에는 배지가 그대로 있고, 다음에 다시 올 때 새로 계산된다.
const VISIT_GAP = 1800;      // 30분. 이보다 짧은 간격의 재방문은 '같은 방문'으로 친다.

// 지난번에 왔던 시각. 없거나 이상하면 0(= 배지를 안 붙인다).
function last_visit_at(): int {
    $raw = $_COOKIE[LAST_VISIT_COOKIE] ?? '';

    // ★ 쿠키는 사용자가 고칠 수 있다 — 숫자인지부터 확인한다.
    //   ctype_digit을 쓰는 이유: (int)로 먼저 바꾸면 "17abc"가 17로 통과한다.
    if (!is_string($raw) || !ctype_digit($raw)) {
        return 0;
    }

    $at = (int) $raw;

    // 미래 시각이 적혀 있으면(시계를 앞당겨 놓았거나 조작) 믿지 않는다.
    //   그대로 쓰면 '모든 글이 옛날 글'이 되어 배지가 영영 안 뜬다.
    return $at > time() ? 0 : $at;
}

// 방문 시각을 갱신한다. (게시판을 그린 뒤에 부른다)
function touch_visit(): void {
    if (!has_consent('view')) {
        return;                       // 동의 안 함 → 방문 시각을 남기지 않는다(🆕 배지도 안 뜬다)
    }
    if (time() - last_visit_at() < VISIT_GAP) {
        return;                       // 아직 '같은 방문' — 그대로 두어 배지를 유지한다
    }
    setcookie(LAST_VISIT_COOKIE, (string) time(), optional_cookie_options());
}


// ★ 동의를 받아야 심을 수 있는 쿠키들. (나머지는 전부 '필수')
//   [왜 목록으로 따로 두나]
//     쿠키 설정 화면이 "지금 이 브라우저에 무엇이 있나"를 **필수/선택으로 갈라** 보여주려면
//     그 경계가 한 곳에 적혀 있어야 한다. 화면마다 각자 판단하면 언젠가 서로 어긋난다.
//   ★ 새 쿠키를 만들 때 여기 넣을지 말지를 반드시 정한다 — 그게 '물어봐야 하나'의 답이다.
const OPTIONAL_COOKIES = [
    RECENT_POSTS_COOKIE, RECENT_WORKS_COOKIE, LAST_VISIT_COOKIE, RECENT_SEARCH_COOKIE,
    VIEW_ID_COOKIE,
];

// ── 동의하지 않은 항목의 쿠키를 치운다 ───────────────────────
//   [왜 필요한가]
//     동의는 "앞으로 안 심겠다"가 아니라 **"지금 것도 치우겠다"** 까지다.
//     이게 없으면 [거절]을 눌러도 이미 쌓인 검색어·열람 기록이 브라우저에 90일간 남는다.
//   ★ 이 함수가 consent.php가 아니라 여기 있는 이유:
//     '어느 쿠키가 어느 항목에 속하는지'는 쿠키를 만든 이 파일이 아는 사실이다.
//     consent.php는 '무엇에 동의했나'만 알고, 쿠키 이름은 하나도 모른다. (서로를 덜 알수록 안 엉킨다)
function forget_unconsented_cookies(): void {
    $expired = pref_cookie_options();
    $expired['expires'] = time() - 3600;

    $toClear = [];
    if (!has_consent('view')) {
        $toClear = [RECENT_POSTS_COOKIE, RECENT_WORKS_COOKIE, LAST_VISIT_COOKIE];
    }
    if (!has_consent('search')) {
        $toClear[] = RECENT_SEARCH_COOKIE;
    }
    if (!has_consent('stats')) {
        $toClear[] = VIEW_ID_COOKIE;
    }

    foreach ($toClear as $name) {
        if (isset($_COOKIE[$name])) {
            unset($_COOKIE[$name]);             // 이 요청에서도 즉시 안 보이게
            setcookie($name, '', $expired);     // 브라우저에서도 지우게
        }
    }
}


// ── 쿠키 동의 배너 ───────────────────────────────────────────
//   [★ '확인'에서 '동의'로 바뀐 자리]
//     예전엔 `cookie_notice=1` 하나로 "안내를 읽었다"만 표시했고 버튼도 [확인]뿐이었다.
//     그런데 **거절할 수 없으면 물어본 게 아니다.** 지금은 항목별로 고르고 거절할 수 있다.
//     판단 근거와 저장 방식은 consent.php에 있다.
//
//   [★★ 그리고 이 쿠키만 하던 특별한 일이 사라졌다]
//     `cookie_notice`는 **우리 사이트에서 유일하게 브라우저(JS)가 직접 심던 쿠키**였다.
//     서버를 안 거쳐서 배너가 즉시 사라지는 게 장점이었는데, 동의로 바뀌며 그게 단점이 됐다 —
//     **서버가 모르는 동의는 나중에 증명할 수 없다.**
//     → 이제 폼으로 서버에 보낸다(POST /consent.php). JS는 한 줄도 안 쓴다.
//     ※ 남은 '읽는 쪽'도 httponly라 JS가 못 본다. **값의 무게가 달라지면 다루는 방식도 바뀐다.**
function needs_cookie_consent(): bool {
    return needs_consent();
}


// ── 최근 본 작품 ─────────────────────────────────────────────
//   최근 본 글(recent_posts)과 같은 자리·같은 방식이다. 담는 것만 '글 번호'에서 '작품 slug'로 바뀐다.
//   ★ 검증이 달라지는 지점: 글은 숫자라 ctype_digit이면 끝인데, slug는 글자다.
//     그래서 **모양을 정해두고 그 모양만 통과**시킨다 (영문 소문자·숫자·하이픈).
//     쿠키에서 온 글자를 그대로 화면·쿼리에 흘리면 안 되기 때문이다.

// 이 작품을 '방금 봤다'고 기록한다. (작품 게시판을 열 때 부른다)
function remember_recent_work(string $slug): void {
    if ($slug === '' || !is_valid_work_slug($slug) || !has_consent('view')) {
        return;
    }

    $recent = get_recent_work_slugs();
    $recent = array_values(array_filter($recent, fn($seen) => $seen !== $slug));
    array_unshift($recent, $slug);
    $recent = array_slice($recent, 0, RECENT_POSTS_MAX);   // 개수 기준은 최근 본 글과 같게

    setcookie(RECENT_WORKS_COOKIE, implode(',', $recent), optional_cookie_options());
}

// 최근 본 작품 slug 목록. 이상한 값은 전부 걸러낸다.
function get_recent_work_slugs(): array {
    $raw = $_COOKIE[RECENT_WORKS_COOKIE] ?? '';
    if (!is_string($raw) || $raw === '') {
        return [];
    }

    $result = [];
    foreach (explode(',', $raw) as $piece) {
        if (!is_valid_work_slug($piece) || in_array($piece, $result, true)) {
            continue;
        }
        $result[] = $piece;
        if (count($result) >= RECENT_POSTS_MAX) {
            break;
        }
    }
    return $result;
}

// slug 모양 검사 — 영문 소문자·숫자·하이픈만, 최대 50글자.
//   ★ '있는 작품인가'는 여기서 안 본다. 그건 작품을 꺼내는 쪽(works.php)이 판단하고,
//     없는 작품은 목록에서 자연히 빠진다. 여기서는 **모양만** 본다.
function is_valid_work_slug(string $slug): bool {
    return $slug !== '' && preg_match('/^[a-z0-9-]{1,50}$/', $slug) === 1;
}


// ── 게시판 한 페이지 글 수 ───────────────────────────────────
//   정렬·감상 필터와 완전히 같은 자리다 — "나는 한 화면에 많이 보는 게 편하다"도 취향이므로.
//   ★ 숫자라는 점만 다르다. 그래도 **허용 목록 대조**는 똑같이 한다 —
//     숫자라고 안심하고 그대로 쓰면 ?per=100000 으로 서버를 갈아버릴 수 있다.
//     "숫자니까 안전하다"가 아니라 "**우리가 정한 값인가**"가 기준이다.

// 고른 값을 기억한다.
function remember_per_page(int $perPage): void {
    setcookie(PER_PAGE_COOKIE, (string) $perPage, pref_cookie_options());
}

// 기억해 둔 값을 꺼낸다. 허용 목록에 없으면 $default.
function preferred_per_page(array $allowed, int $default): int {
    $raw = $_COOKIE[PER_PAGE_COOKIE] ?? '';
    if (!is_string($raw) || !ctype_digit($raw)) {
        return $default;
    }
    $value = (int) $raw;
    return in_array($value, $allowed, true) ? $value : $default;
}
